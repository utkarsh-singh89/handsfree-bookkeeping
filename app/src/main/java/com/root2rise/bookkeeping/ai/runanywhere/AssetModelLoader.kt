package com.root2rise.bookkeeping.ai.runanywhere

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Loads GGUF model from assets to internal storage
 *
 * This avoids network downloads and ensures 100% offline operation.
 * The model is copied once from assets and reused across app sessions.
 */
object AssetModelLoader {

    private const val TAG = "AssetModelLoader"
    private const val ASSET_PATH = "models/tinyllama-1.1b-chat-v1.0-q4_k_m.gguf"
    private const val MODEL_FILENAME = "tinyllama-1.1b-chat-v1.0-q4_k_m.gguf"

    /**
     * Ensure model file exists in internal storage
     * Copies from assets if needed
     *
     * @param context Application context
     * @return Full path to model file, or null if failed
     */
    suspend fun ensureModelAvailable(context: Context): String? {
        return withContext(Dispatchers.IO) {
            try {
                // Create models directory in internal storage
                val modelsDir = File(context.filesDir, "models")
                if (!modelsDir.exists()) {
                    modelsDir.mkdirs()
                    Log.d(TAG, "Created models directory: ${modelsDir.absolutePath}")
                }

                val modelFile = File(modelsDir, MODEL_FILENAME)

                // Check if model already exists
                if (modelFile.exists() && modelFile.length() > 100_000_000) {
                    // Model exists and is > 100MB (sanity check)
                    Log.d(TAG, "✅ Model already exists: ${modelFile.absolutePath}")
                    Log.d(TAG, "   Size: ${modelFile.length() / (1024 * 1024)}MB")
                    return@withContext modelFile.absolutePath
                }

                // Copy from assets
                Log.d(TAG, "Copying model from assets: $ASSET_PATH")
                Log.d(TAG, "⚠️ This may take 30-60 seconds (streaming copy)...")

                val startTime = System.currentTimeMillis()
                var copiedBytes = 0L

                context.assets.open(ASSET_PATH).use { inputStream ->
                    FileOutputStream(modelFile).use { outputStream ->
                        val buffer = ByteArray(8192) // 8KB buffer
                        var bytesRead: Int

                        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                            outputStream.write(buffer, 0, bytesRead)
                            copiedBytes += bytesRead

                            // Log progress every 50MB
                            if (copiedBytes % (50 * 1024 * 1024) < 8192) {
                                Log.d(TAG, "Copied: ${copiedBytes / (1024 * 1024)}MB")
                            }
                        }
                    }
                }

                val duration = System.currentTimeMillis() - startTime
                Log.d(TAG, "✅ Model copied successfully in ${duration / 1000}s")
                Log.d(TAG, "   Path: ${modelFile.absolutePath}")
                Log.d(TAG, "   Size: ${modelFile.length() / (1024 * 1024)}MB")

                return@withContext modelFile.absolutePath

            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to load model from assets: ${e.message}", e)
                return@withContext null
            }
        }
    }

    /**
     * Get model path if already copied, null otherwise
     */
    fun getExistingModelPath(context: Context): String? {
        val modelFile = File(context.filesDir, "models/$MODEL_FILENAME")
        return if (modelFile.exists() && modelFile.length() > 100_000_000) {
            modelFile.absolutePath
        } else {
            null
        }
    }

    /**
     * Delete model file from internal storage
     */
    fun deleteModel(context: Context): Boolean {
        return try {
            val modelFile = File(context.filesDir, "models/$MODEL_FILENAME")
            if (modelFile.exists()) {
                modelFile.delete()
            } else {
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting model: ${e.message}")
            false
        }
    }
}
