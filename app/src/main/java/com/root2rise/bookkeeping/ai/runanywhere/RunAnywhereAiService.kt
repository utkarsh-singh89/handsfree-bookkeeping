package com.root2rise.bookkeeping.ai.runanywhere

import android.content.Context
import android.util.Log
import com.root2rise.bookkeeping.ai.AiService
import com.root2rise.bookkeeping.ai.ImprovedMockAiService
import com.runanywhere.sdk.components.llm.LLMComponent
import com.runanywhere.sdk.components.llm.LLMConfiguration
import com.runanywhere.sdk.models.ModelInfo
import com.runanywhere.sdk.models.enums.LLMFramework
import com.runanywhere.sdk.models.enums.ModelCategory
import com.runanywhere.sdk.models.enums.ModelFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

/**
 * Production AI Service using RunAnywhere SDK with TinyLlama model from assets
 *
 * Architecture:
 * 1. Loads GGUF model from app/src/main/assets/models/
 * 2. Uses RunAnywhere SDK for inference
 * 3. Falls back to ImprovedMockAiService on failure
 * 4. Thread-safe singleton initialization
 * 5. Completely offline (no network)
 *
 * Supported: arm64-v8a only
 */
class RunAnywhereAiService private constructor(private val context: Context) : AiService {

    private val TAG = "RunAnywhereAI"

    // Thread-safe initialization
    private val initMutex = Mutex()

    // Model configuration
    private val MODEL_ID = "tinyllama-1.1b-chat-q4"
    private val CONTEXT_LENGTH = 2048
    private val MAX_TOKENS = 256
    private val TEMPERATURE = 0.3f
    private val INFERENCE_TIMEOUT_MS = 60_000L // 60 seconds

    // Fallback service
    private val fallbackService = ImprovedMockAiService()

    // SDK components
    private var llmComponent: LLMComponent? = null
    private var isInitialized = false

    companion object {
        @Volatile
        private var INSTANCE: RunAnywhereAiService? = null

        fun getInstance(context: Context): RunAnywhereAiService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: RunAnywhereAiService(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }

    /**
     * Initialize RunAnywhere SDK with local model
     * Thread-safe, idempotent
     */
    suspend fun initialize(): Boolean = initMutex.withLock {
        if (isInitialized) {
            Log.d(TAG, "Already initialized")
            return true
        }

        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Initializing RunAnywhere SDK with local model...")

                // Load model from assets
                val modelPath = AssetModelLoader.ensureModelAvailable(context)
                if (modelPath == null) {
                    Log.e(TAG, "❌ Failed to load model from assets")
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

                // Create model info
                val modelInfo = ModelInfo(
                    id = MODEL_ID,
                    name = "TinyLlama-1.1B-Chat",
                    category = ModelCategory.LANGUAGE_MODEL,
                    format = ModelFormat.GGUF,
                    downloadURL = null,
                    localPath = modelPath,
                    downloadSize = null,
                    memoryRequired = null,
                    sha256Checksum = null,
                    md5Checksum = null,
                    compatibleFrameworks = emptyList(),
                    preferredFramework = LLMFramework.LLAMACPP,
                    contextLength = CONTEXT_LENGTH,
                    supportsThinking = false
                )

                // Load model
                llmComponent?.loadModel(modelInfo)

                // Verify loaded
                if (llmComponent?.isModelLoaded == true) {
                    isInitialized = true
                    Log.d(TAG, "✅ RunAnywhere SDK initialized successfully")
                    Log.d(TAG, "   Model: TinyLlama-1.1B-Chat-Q4_K_M")
                    Log.d(TAG, "   Framework: llama.cpp")
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
     * Process Hinglish utterance and return JSON
     * Falls back to ImprovedMockAiService on any failure
     */
    override suspend fun processUtterance(utterance: String): String {
        return withContext(Dispatchers.IO) {
            try {
                // Check initialization
                if (!isInitialized || llmComponent == null) {
                    Log.w(TAG, "SDK not initialized, using fallback")
                    return@withContext fallbackService.processUtterance(utterance)
                }

                // Check model loaded
                if (llmComponent?.isModelLoaded != true) {
                    Log.w(TAG, "Model not loaded, using fallback")
                    return@withContext fallbackService.processUtterance(utterance)
                }

                Log.d(TAG, "Processing with TinyLlama: $utterance")

                // Build few-shot prompt
                val prompt = buildPrompt(utterance)

                // Run inference with timeout
                val startTime = System.currentTimeMillis()
                val rawOutput = withTimeout<Any?>(INFERENCE_TIMEOUT_MS) {
                    llmComponent?.generate(prompt)
                }
                val duration = System.currentTimeMillis() - startTime

                Log.d(TAG, "⏱️ Inference took: ${duration}ms")

                if (rawOutput == null) {
                    Log.w(TAG, "⚠️ Null output, using fallback")
                    return@withContext fallbackService.processUtterance(utterance)
                }

                Log.d(TAG, "Raw output: ${rawOutput.toString().take(200)}")

                // Extract JSON
                val json = extractJson(rawOutput.toString())

                if (json.isNotEmpty()) {
                    Log.d(TAG, "✅ Valid JSON extracted")
                    return@withContext json
                } else {
                    Log.w(TAG, "⚠️ Invalid JSON, using fallback")
                    return@withContext fallbackService.processUtterance(utterance)
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Inference error: ${e.message}", e)
                return@withContext fallbackService.processUtterance(utterance)
            }
        }
    }

    /**
     * Build few-shot prompt for Hinglish bookkeeping
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
     * System prompt for the model
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
            else -> "Ready (TinyLlama-1.1B)"
        }
    }
}
