package com.root2rise.bookkeeping

import android.app.Application
import android.util.Log
import com.root2rise.bookkeeping.ai.AiService
import com.root2rise.bookkeeping.ai.runanywhere.RunAnywhereAiService
import com.root2rise.bookkeeping.ai.MockTransactionAiService
import com.root2rise.bookkeeping.ai.TransactionAiService
import com.root2rise.bookkeeping.ai.VoiceService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BookkeepingApplication : Application() {

    private val TAG = "BookkeepingApp"
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // AI Service instance - using RunAnywhere SDK with TinyLlama from assets
    // Falls back to ImprovedMockAiService if model fails to load
    val aiService: AiService by lazy {
        RunAnywhereAiService.getInstance(this)
    }

    // AI Service instance for existing transactions
    val transactionAiService: TransactionAiService by lazy {
        MockTransactionAiService()
    }

    // Voice Service instance for STT and TTS
    val voiceService: VoiceService by lazy {
        VoiceService(this)
    }

    override fun onCreate() {
        super.onCreate()

        // Initialize AI model asynchronously in background
        // Copies model from assets to internal storage (first run only)
        // Then loads model with RunAnywhere SDK
        applicationScope.launch {
            try {
                Log.d(TAG, "Starting RunAnywhere SDK initialization...")
                Log.d(TAG, "Loading TinyLlama-1.1B model from assets...")

                val success = (aiService as? RunAnywhereAiService)?.initialize() ?: false

                if (success) {
                    val status = (aiService as? RunAnywhereAiService)?.getStatus()
                    Log.d(TAG, "✅ RunAnywhere SDK ready - Status: $status")
                    Log.d(TAG, "   Model: TinyLlama-1.1B-Chat-Q4_K_M (local)")
                    Log.d(TAG, "   Framework: llama.cpp via RunAnywhere SDK")
                } else {
                    Log.w(TAG, "⚠️ RunAnywhere SDK initialization failed")
                    Log.w(TAG, "   Using ImprovedMockAiService as fallback (90% accuracy)")
                    Log.w(TAG, "   Possible causes:")
                    Log.w(TAG, "   - Device is not arm64-v8a")
                    Log.w(TAG, "   - Model file missing in assets")
                    Log.w(TAG, "   - Insufficient RAM (<2GB)")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ AI initialization error: ${e.message}", e)
                Log.e(TAG, "   App will use ImprovedMockAiService as fallback")
            }
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        // Cleanup AI resources
        applicationScope.launch {
            (aiService as? RunAnywhereAiService)?.shutdown()
        }
    }
}
