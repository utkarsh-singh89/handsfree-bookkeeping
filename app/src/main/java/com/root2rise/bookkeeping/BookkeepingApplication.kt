package com.root2rise.bookkeeping

import android.app.Application
import android.util.Log
import com.root2rise.bookkeeping.ai.AiService
import com.root2rise.bookkeeping.ai.runanywhere.RunAnywhereAiServiceImpl
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

    // AI Service instance - using RunAnywhere SDK with llama.cpp
    // Falls back to ImprovedMockAiService if model fails to load
    val aiService: AiService by lazy {
        RunAnywhereAiServiceImpl(this)
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
        // This improves first-use performance by pre-loading the model
        applicationScope.launch {
            try {
                Log.d(TAG, "Starting RunAnywhere SDK initialization...")
                val success = (aiService as? RunAnywhereAiServiceImpl)?.initialize() ?: false

                if (success) {
                    Log.d(
                        TAG,
                        "✅ RunAnywhere SDK ready - Status: ${(aiService as? RunAnywhereAiServiceImpl)?.getStatus()}"
                    )
                } else {
                    Log.w(TAG, "⚠️ RunAnywhere SDK initialization failed, using fallback service")
                    Log.w(
                        TAG,
                        "   This is expected if model download failed or device is not arm64-v8a"
                    )
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
            (aiService as? RunAnywhereAiServiceImpl)?.shutdown()
        }
    }
}
