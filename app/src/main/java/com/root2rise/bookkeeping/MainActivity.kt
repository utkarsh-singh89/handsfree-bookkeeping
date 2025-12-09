package com.root2rise.bookkeeping

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.root2rise.bookkeeping.data.BookkeepingDatabase
import com.root2rise.bookkeeping.repository.BookkeepingRepository
import com.root2rise.bookkeeping.ui.theme.BookkeepingTheme
import com.root2rise.bookkeeping.viewmodel.BookkeepingViewModel
import com.root2rise.bookkeeping.viewmodel.BookkeepingViewModelFactory

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: BookkeepingViewModel

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if(isGranted) {
                Log.d("MainActivity", "Microphone permission granted")
            } else{
                Toast.makeText(this, "Microphone permission is required for voice features", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        requestAudioPermission()

        val app = application as BookkeepingApplication
        val aiService = app.aiService
        val transactionAiService = app.transactionAiService
        val voiceService = app.voiceService

        val database = BookkeepingDatabase.getDatabase(this)
        val repository = BookkeepingRepository(database)

        val factory = BookkeepingViewModelFactory(repository, aiService, transactionAiService)
        viewModel = ViewModelProvider(this, factory)[BookkeepingViewModel::class.java]

        setContent {
            BookkeepingTheme {
                com.root2rise.bookkeeping.ui.navigation.AppNavigation(
                    viewModel = viewModel,
                    onStartVoiceInput = {
                        voiceService.startListening { transcription ->
                            viewModel.processTranscription(transcription, voiceService::speak)
                        }
                    },
                    onStartModificationVoiceInput = { transaction ->
                        voiceService.startListening { transcription ->
                            viewModel.processTransactionUpdate(transaction, transcription, voiceService::speak)
                        }
                    },
                    speaker = voiceService::speak
                )
            }
        }
    }

    private fun requestAudioPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                Log.d("MainActivity", "Microphone permission already granted")
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        (application as BookkeepingApplication).voiceService.shutdown()
    }
}
