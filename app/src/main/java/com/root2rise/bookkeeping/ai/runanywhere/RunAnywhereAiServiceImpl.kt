package com.root2rise.bookkeeping.ai.runanywhere

import android.content.Context
import android.util.Log
import com.root2rise.bookkeeping.ai.AiService
import com.root2rise.bookkeeping.ai.ImprovedMockAiService
import com.runanywhere.sdk.components.llm.LLMComponent
import com.runanywhere.sdk.components.llm.LLMConfiguration
import com.runanywhere.sdk.models.ModelInfo
import com.runanywhere.sdk.models.RunAnywhereGenerationOptions
import com.runanywhere.sdk.models.enums.ModelCategory
import com.runanywhere.sdk.models.enums.ModelFormat
import com.runanywhere.sdk.models.enums.LLMFramework
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.File

/**
 * Production AI Service using RunAnywhere SDK with llama.cpp backend
 *
 * This implementation uses the official RunAnywhere SDK to run on-device LLM inference
 * with automatic fallback to ImprovedMockAiService if the SDK fails.
 *
 * Architecture:
 * 1. Model Manager: Downloads and manages GGUF model files
 * 2. LLMComponent: RunAnywhere's main inference engine
 * 3. Prompt Template: Few-shot examples for Hinglish NLP
 * 4. Fallback: Graceful degradation to rule-based service
 *
 * Supported ABIs: arm64-v8a only (modern Android devices)
 */
class RunAnywhereAiServiceImpl(private val context: Context) : AiService {

    private val TAG = "RunAnywhereAI"
    
    // Model configuration - Using SmolLM2-360M for fast inference
    private val MODEL_ID = "smollm2-360m-instruct-q4"
    private val MODEL_NAME = "smollm2-360m-instruct-q4_k_m.gguf"
    private val MODEL_URL = "https://huggingface.co/HuggingFaceTB/SmolLM2-360M-Instruct-GGUF/resolve/main/smollm2-360m-instruct-q4_k_m.gguf"
    private val MODEL_CHECKSUM = "" // Optional: Add SHA256 if available
    
    // Inference configuration
    private val CONTEXT_LENGTH = 2048
    private val MAX_TOKENS = 256
    private val TEMPERATURE = 0.3f
    private val INFERENCE_TIMEOUT_MS = 60_000L // 60 seconds
    
    // Fallback service for when SDK fails
    private val fallbackService = ImprovedMockAiService()
    
    // SDK components
    private var llmComponent: LLMComponent? = null
    private var modelManager: ModelManager? = null
    private var isInitialized = false
    
    /**
     * Initialize the RunAnywhere SDK and download model if needed
     * Should be called from Application.onCreate()
     */
    suspend fun initialize(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Initializing RunAnywhere SDK...")
                
                // Initialize model manager
                modelManager = ModelManager(context)
                
                // Ensure model is available locally
                val modelPath = modelManager?.ensureModelAvailable(
                    modelId = MODEL_ID,
                    modelName = MODEL_NAME,
                    downloadUrl = MODEL_URL,
                    checksum = MODEL_CHECKSUM.ifEmpty { null }
                )
                
                if (modelPath == null) {
                    Log.e(TAG, "Failed to obtain model file")
                    return@withContext false
                }
                
                Log.d(TAG, "Model ready at: $modelPath")
                
                // Create LLM configuration
                val config = LLMConfiguration(
                    modelId = MODEL_ID,
                    contextLength = CONTEXT_LENGTH,
                    useGPUIfAvailable = false, // CPU-only for compatibility
                    maxTokens = MAX_TOKENS,
                    temperature = TEMPERATURE.toDouble(),
                    systemPrompt = getSystemPrompt(),
                    streamingEnabled = false,
                    verboseLogging = false,
                    performanceMonitoring = true
                )
                
                // Create LLM component
                llmComponent = LLMComponent(config)
                
                // Load model
                val modelInfo = ModelInfo(
                    id = MODEL_ID,
                    name = "SmolLM2-360M-Instruct",
                    category = ModelCategory.LANGUAGE_MODEL,
                    format = ModelFormat.GGUF,
                    downloadURL = MODEL_URL,
                    localPath = modelPath,
                    downloadSize = null,
                    memoryRequired = null,
                    sha256Checksum = MODEL_CHECKSUM.ifEmpty { null },
                    md5Checksum = null,
                    compatibleFrameworks = emptyList(),
                    preferredFramework = LLMFramework.LLAMACPP,
                    contextLength = CONTEXT_LENGTH,
                    supportsThinking = false
                )
                
                llmComponent?.loadModel(modelInfo)
                
                // Verify model loaded
                if (llmComponent?.isModelLoaded == true) {
                    isInitialized = true
                    Log.d(TAG, "✅ RunAnywhere SDK initialized successfully")
                    return@withContext true
                } else {
                    Log.e(TAG, "❌ Model loaded but not ready")
                    return@withContext false
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ SDK initialization failed: ${e.message}", e)
                return@withContext false
            }
        }
    }
    
    /**
     * Process natural language utterance and return JSON
     * Falls back to ImprovedMockAiService on any failure
     */
    override suspend fun processUtterance(utterance: String): String {
        return withContext(Dispatchers.IO) {
            try {
                // Check if SDK is initialized
                if (!isInitialized || llmComponent == null) {
                    Log.w(TAG, "SDK not initialized, using fallback service")
                    return@withContext fallbackService.processUtterance(utterance)
                }
                
                // Check if model is loaded
                if (llmComponent?.isModelLoaded != true) {
                    Log.w(TAG, "Model not loaded, using fallback service")
                    return@withContext fallbackService.processUtterance(utterance)
                }
                
                Log.d(TAG, "Processing with RunAnywhere SDK: $utterance")
                
                // Build prompt with few-shot examples
                val prompt = buildPrompt(utterance)
                
                // Run inference with timeout
                val rawOutput = withTimeout<Any?>(INFERENCE_TIMEOUT_MS) {
                    llmComponent?.generate(prompt)
                }
                
                if (rawOutput == null) {
                    Log.w(TAG, "⚠️ Null output from model, using fallback")
                    return@withContext fallbackService.processUtterance(utterance)
                }
                
                Log.d(TAG, "Raw model output: $rawOutput")
                
                // Extract JSON from output
                val json = extractJson(rawOutput.toString())
                
                if (json.isNotEmpty()) {
                    Log.d(TAG, "✅ Valid JSON extracted")
                    return@withContext json
                } else {
                    Log.w(TAG, "⚠️ Invalid JSON from model, using fallback")
                    return@withContext fallbackService.processUtterance(utterance)
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Inference error: ${e.message}", e)
                // Always fallback on error
                return@withContext fallbackService.processUtterance(utterance)
            }
        }
    }
    
    /**
     * Build few-shot prompt for the model
     * Teaches the model Hinglish bookkeeping patterns
     */
    private fun buildPrompt(utterance: String): String {
        return """
You are a bookkeeping assistant for Indian shopkeepers. Convert Hinglish utterances to JSON.

SCHEMAS:

Transaction (money in/out):
{"kind":"transaction","action":"add_transaction","direction":"in|out","type":"sale|purchase|loan_given|loan_taken|expense|other","party_name":"Name or null","amount":0,"date":"today","notes":"text"}

Query (asking question):
{"kind":"query","action":"query_total_sales|query_total_expenses|query_overall_summary|query_balance","party_name":"Name or null","time_range":"today|yesterday|this_week|this_month|all"}

RULES:
- "in" = money coming to shopkeeper
- "out" = money going from shopkeeper
- "udhar" + "liye" = loan_taken (IN)
- "udhar" + "diya" = loan_given (OUT)
- "bikri/becha/sale" = sale (IN)
- "bill/bhar/payment" = expense (OUT)
- "kharcha/expense" = expense (OUT)
- Extract party name from "X se" (from) or "X ko" (to)
- Output ONLY valid JSON, nothing else

EXAMPLES:

Input: Ramesh se 500 liye udhar
Output: {"kind":"transaction","action":"add_transaction","direction":"in","type":"loan_taken","party_name":"Ramesh","amount":500,"date":"today","notes":"Loan from Ramesh"}

Input: Sunil ko 300 diya udhar
Output: {"kind":"transaction","action":"add_transaction","direction":"out","type":"loan_given","party_name":"Sunil","amount":300,"date":"today","notes":"Loan to Sunil"}

Input: Aaj 2000 ki bikri hui
Output: {"kind":"transaction","action":"add_transaction","direction":"in","type":"sale","party_name":null,"amount":2000,"date":"today","notes":"Daily sales"}

Input: Bijli ka bill 900 bhar diya
Output: {"kind":"transaction","action":"add_transaction","direction":"out","type":"expense","party_name":null,"amount":900,"date":"today","notes":"Electricity bill"}

Input: Aaj ki total bikri kitni hai?
Output: {"kind":"query","action":"query_total_sales","party_name":null,"time_range":"today"}

Input: Ramesh ka balance kitna hai?
Output: {"kind":"query","action":"query_balance","party_name":"Ramesh","time_range":null}

NOW PROCESS:

Input: $utterance
Output:""".trimIndent()
    }
    
    /**
     * Get system prompt for the model
     */
    private fun getSystemPrompt(): String {
        return "You are a bookkeeping assistant that converts Hinglish utterances to JSON. " +
                "Output ONLY valid JSON, nothing else."
    }
    
    /**
     * Extract JSON from model output
     * Handles cases where model adds explanations
     */
    private fun extractJson(rawOutput: String): String {
        try {
            // Find JSON object boundaries
            val startIndex = rawOutput.indexOf('{')
            val endIndex = rawOutput.lastIndexOf('}')
            
            if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
                val json = rawOutput.substring(startIndex, endIndex + 1)
                
                // Basic validation
                if (json.contains("\"kind\"") &&
                    (json.contains("\"transaction\"") || json.contains("\"query\""))
                ) {
                    return json
                }
            }
            
            return ""
        } catch (e: Exception) {
            Log.e(TAG, "JSON extraction error: ${e.message}")
            return ""
        }
    }
    
    /**
     * Cleanup SDK resources
     */
    suspend fun shutdown() {
        try {
            llmComponent?.unloadModel()
            llmComponent = null
            isInitialized = false
            Log.d(TAG, "RunAnywhere SDK shutdown complete")
        } catch (e: Exception) {
            Log.e(TAG, "Error during shutdown: ${e.message}")
        }
    }
    
    /**
     * Check if SDK is ready
     */
    fun isReady(): Boolean {
        return isInitialized && llmComponent?.isModelLoaded == true
    }

    /**
     * Get current status for debugging
     */
    fun getStatus(): String {
        return when {
            !isInitialized -> "Not initialized"
            llmComponent == null -> "LLM component null"
            llmComponent?.isModelLoaded != true -> "Model not loaded"
            else -> "Ready (Model: ${llmComponent?.loadedModelId})"
        }
    }
}
