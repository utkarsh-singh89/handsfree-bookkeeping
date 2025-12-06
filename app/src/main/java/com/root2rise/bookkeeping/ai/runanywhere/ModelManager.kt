package com.root2rise.bookkeeping.ai.runanywhere

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.security.MessageDigest

/**
 * Manages model downloads, caching, and verification for RunAnywhere SDK
 */
class ModelManager(private val context: Context) {

    private val TAG = "ModelManager"
    private val modelsDir: File = File(context.filesDir, "models")

    init {
        if (!modelsDir.exists()) {
            modelsDir.mkdirs()
        }
    }

    /**
     * Ensure model is available locally, downloading if necessary
     *
     * @param modelId Unique model identifier
     * @param modelName Model filename (e.g., model.gguf)
     * @param downloadUrl URL to download from
     * @param checksum Optional SHA256 checksum for verification
     * @param forceRedownload Force re-download even if file exists
     * @return Local file path if successful, null if failed
     */
    suspend fun ensureModelAvailable(
        modelId: String,
        modelName: String,
        downloadUrl: String,
        checksum: String? = null,
        forceRedownload: Boolean = false
    ): String? {
        return withContext(Dispatchers.IO) {
            try {
                val modelFile = File(modelsDir, modelName)

                // Check if model already exists and is valid
                if (modelFile.exists() && !forceRedownload) {
                    Log.d(TAG, "Model found in cache: ${modelFile.path}")

                    // Verify checksum if provided
                    if (checksum != null) {
                        if (verifyChecksum(modelFile, checksum)) {
                            Log.d(TAG, "✅ Checksum verified")
                            return@withContext modelFile.absolutePath
                        } else {
                            Log.w(TAG, "⚠️ Checksum mismatch, re-downloading")
                            modelFile.delete()
                        }
                    } else {
                        // No checksum, just verify file size is reasonable
                        if (modelFile.length() > 1_000_000) { // > 1MB
                            return@withContext modelFile.absolutePath
                        } else {
                            Log.w(TAG, "⚠️ Model file too small, re-downloading")
                            modelFile.delete()
                        }
                    }
                }

                // Download model
                Log.d(TAG, "Downloading model from: $downloadUrl")
                val success = downloadModel(downloadUrl, modelFile)

                if (!success) {
                    Log.e(TAG, "❌ Model download failed")
                    return@withContext null
                }

                // Verify checksum after download
                if (checksum != null && !verifyChecksum(modelFile, checksum)) {
                    Log.e(TAG, "❌ Downloaded model checksum mismatch")
                    modelFile.delete()
                    return@withContext null
                }

                Log.d(TAG, "✅ Model ready: ${modelFile.absolutePath}")
                return@withContext modelFile.absolutePath

            } catch (e: Exception) {
                Log.e(TAG, "Error ensuring model availability: ${e.message}", e)
                return@withContext null
            }
        }
    }

    /**
     * Download model file from URL
     */
    private fun downloadModel(url: String, destination: File): Boolean {
        return try {
            Log.d(TAG, "⬇️ Starting download to: ${destination.absolutePath}")
            Log.d(TAG, "⚠️ This may take 2-5 minutes depending on network speed")

            val connection = URL(url).openConnection()
            connection.connectTimeout = 30_000 // 30 seconds
            connection.readTimeout = 60_000 // 60 seconds
            connection.connect()

            val totalSize = connection.contentLength
            var downloadedSize = 0L
            var lastProgressLog = 0

            connection.getInputStream().use { input ->
                FileOutputStream(destination).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        downloadedSize += bytesRead

                        // Log progress every 10%
                        val progress = ((downloadedSize * 100) / totalSize).toInt()
                        if (progress >= lastProgressLog + 10) {
                            Log.d(
                                TAG,
                                "Download progress: $progress% (${downloadedSize / (1024 * 1024)}MB / ${totalSize / (1024 * 1024)}MB)"
                            )
                            lastProgressLog = progress
                        }
                    }
                }
            }

            Log.d(TAG, "✅ Download complete: ${destination.length() / (1024 * 1024)}MB")
            true

        } catch (e: Exception) {
            Log.e(TAG, "❌ Download failed: ${e.message}", e)
            destination.delete()
            false
        }
    }

    /**
     * Verify file checksum (SHA-256)
     */
    private fun verifyChecksum(file: File, expectedChecksum: String): Boolean {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val buffer = ByteArray(8192)

            file.inputStream().use { input ->
                var bytesRead: Int
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    digest.update(buffer, 0, bytesRead)
                }
            }

            val actualChecksum = digest.digest().joinToString("") { "%02x".format(it) }
            val match = actualChecksum.equals(expectedChecksum, ignoreCase = true)

            if (!match) {
                Log.w(TAG, "Checksum mismatch:")
                Log.w(TAG, "  Expected: $expectedChecksum")
                Log.w(TAG, "  Actual:   $actualChecksum")
            }

            match
        } catch (e: Exception) {
            Log.e(TAG, "Checksum verification error: ${e.message}")
            false
        }
    }

    /**
     * Delete a model file
     */
    fun deleteModel(modelName: String): Boolean {
        return try {
            val modelFile = File(modelsDir, modelName)
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

    /**
     * Get model file if it exists
     */
    fun getModelPath(modelName: String): String? {
        val modelFile = File(modelsDir, modelName)
        return if (modelFile.exists() && modelFile.length() > 1_000_000) {
            modelFile.absolutePath
        } else {
            null
        }
    }

    /**
     * List all cached models
     */
    fun listCachedModels(): List<File> {
        return modelsDir.listFiles()?.filter { it.isFile && it.length() > 1_000_000 } ?: emptyList()
    }

    /**
     * Get total size of cached models in bytes
     */
    fun getCacheSize(): Long {
        return listCachedModels().sumOf { it.length() }
    }

    /**
     * Clear all cached models
     */
    fun clearCache(): Boolean {
        return try {
            listCachedModels().forEach { it.delete() }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing cache: ${e.message}")
            false
        }
    }
}
