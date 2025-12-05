package com.root2rise.bookkeeping

import android.app.Application
import com.root2rise.bookkeeping.ai.AiService
import com.root2rise.bookkeeping.ai.MockAiService
import com.root2rise.bookkeeping.ai.MockTransactionAiService
import com.root2rise.bookkeeping.ai.TransactionAiService
import com.root2rise.bookkeeping.ai.VoiceService

class BookkeepingApplication : Application() {

    // AI Service instance - shared across the app
    val aiService: AiService by lazy {
        MockAiService()
    }

    // AI Service instance for existing transactions
    val transactionAiService: TransactionAiService by lazy {
        MockTransactionAiService()
    }

    // Voice Service instance for STT and TTS
    val voiceService: VoiceService by lazy {
        VoiceService(this)
    }
}
