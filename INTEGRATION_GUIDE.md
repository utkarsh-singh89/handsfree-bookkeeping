# Integration Guide - RunAnywhere SDK & Firebender

This guide explains how to integrate the actual on-device AI model using RunAnywhere SDK and
Firebender plugin.

## 📦 Current Setup

The app currently uses `MockAiService` which simulates AI responses using pattern matching. This is
useful for:

- Development without AI model
- Testing the app architecture
- Demonstrating the app flow

## 🎯 Production Integration Steps

### Step 1: Add RunAnywhere SDK Dependency

**File**: `app/build.gradle.kts`

```kotlin
dependencies {
    // ... existing dependencies ...
    
    // RunAnywhere SDK for on-device inference
    implementation("com.runanywhere:sdk:1.0.0") // Replace with actual version
    
    // Firebender plugin for LLM optimization
    implementation("com.firebender:plugin:1.0.0") // Replace with actual version
}
```

### Step 2: Add Model to Assets

1. Train or obtain your bookkeeping model (see AI_PROMPTS_GUIDE.md)
2. Convert to TensorFlow Lite format (.tflite)
3. Place the model file in: `app/src/main/assets/bookkeeping_model.tflite`

**Recommended model specs:**

- Size: < 50MB
- Format: TensorFlow Lite
- Input: String (natural language)
- Output: JSON string
- Base model: Phi-2, Gemma-2B, or similar

### Step 3: Create FirebenderAiService

**File**: `app/src/main/java/com/root2rise/bookkeeping/ai/FirebenderAiService.kt`

```kotlin
package com.root2rise.bookkeeping.ai

import android.content.Context
import com.runanywhere.sdk.RunAnywhereModel
import com.runanywhere.sdk.ModelConfig
import com.firebender.optimizer.Optimizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Production implementation of AiService using RunAnywhere SDK and Firebender.
 * 
 * This service:
 * 1. Loads the on-device TFLite model
 * 2. Processes Hinglish utterances
 * 3. Returns JSON conforming to TransactionSchema or QuerySchema
 */
class FirebenderAiService(private val context: Context) : AiService {
    
    private var model: RunAnywhereModel? = null
    private var isInitialized = false
    
    /**
     * Initialize the model (call this in Application.onCreate or splash screen)
     */
    suspend fun initialize() = withContext(Dispatchers.IO) {
        if (isInitialized) return@withContext
        
        try {
            // Configure model
            val config = ModelConfig.Builder()
                .setModelPath("bookkeeping_model.tflite")
                .setNumThreads(4) // Use 4 CPU threads
                .setMaxTokens(512) // Max response length
                .setTemperature(0.1f) // Low temperature for consistent JSON
                .build()
            
            // Load model
            model = RunAnywhereModel.load(context, config)
            
            // Apply Firebender optimizations
            Optimizer.optimize(model, Optimizer.SPEED_OPTIMIZED)
            
            isInitialized = true
        } catch (e: Exception) {
            throw RuntimeException("Failed to initialize AI model", e)
        }
    }
    
    override suspend fun processUtterance(utterance: String): String = withContext(Dispatchers.IO) {
        if (!isInitialized) {
            throw IllegalStateException("Model not initialized. Call initialize() first.")
        }
        
        try {
            // Prepare prompt with system instructions
            val prompt = buildPrompt(utterance)
            
            // Generate response
            val response = model?.generate(prompt) ?: throw RuntimeException("Model is null")
            
            // Extract JSON from response (model might add extra text)
            extractJson(response)
            
        } catch (e: Exception) {
            // Fallback to error response
            """
            {
                "kind": "transaction",
                "action": "add_transaction",
                "direction": "in",
                "type": "other",
                "party_name": null,
                "amount": 0,
                "date": "today",
                "notes": "Error processing: ${e.message}"
            }
            """.trimIndent()
        }
    }
    
    /**
     * Build the prompt with system instructions and user input
     */
    private fun buildPrompt(utterance: String): String {
        return """
        You are a bookkeeping NLP assistant for small shopkeepers in India.
        You ONLY output valid JSON, nothing else.
        
        User speaks Hinglish (Hindi + English mix) using words like:
        - rupaye (money), udhar (loan), bikri (sale), kharcha (expense)
        - liya (took), diya (gave), baki (remaining), aaj (today), kal (yesterday)
        
        For TRANSACTIONS (user describes what happened):
        Output: {
          "kind": "transaction",
          "action": "add_transaction",
          "direction": "in|out",
          "type": "sale|purchase|loan_given|loan_taken|expense|other",
          "party_name": "string or null",
          "amount": 0,
          "date": "today|yesterday|yyyy-mm-dd",
          "notes": "string or null"
        }
        
        For QUERIES (user asks a question):
        Output: {
          "kind": "query",
          "action": "query_total_sales|query_total_expenses|query_overall_summary|query_balance",
          "party_name": "string or null",
          "time_range": "today|yesterday|this_week|this_month|all"
        }
        
        User input: "$utterance"
        
        JSON output:
        """.trimIndent()
    }
    
    /**
     * Extract JSON from model response (in case model adds extra text)
     */
    private fun extractJson(response: String): String {
        // Find JSON object in response
        val jsonStart = response.indexOf('{')
        val jsonEnd = response.lastIndexOf('}')
        
        if (jsonStart != -1 && jsonEnd != -1 && jsonEnd > jsonStart) {
            return response.substring(jsonStart, jsonEnd + 1)
        }
        
        // If no JSON found, return the response as-is and let parser handle it
        return response.trim()
    }
    
    /**
     * Release model resources (call in onDestroy)
     */
    fun release() {
        model?.release()
        model = null
        isInitialized = false
    }
}
```

### Step 4: Create Application Class

**File**: `app/src/main/java/com/root2rise/bookkeeping/BookkeepingApplication.kt`

```kotlin
package com.root2rise.bookkeeping

import android.app.Application
import com.root2rise.bookkeeping.ai.FirebenderAiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BookkeepingApplication : Application() {
    
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    // Singleton AI service instance
    lateinit var aiService: FirebenderAiService
        private set
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize AI service asynchronously
        aiService = FirebenderAiService(this)
        
        applicationScope.launch {
            try {
                aiService.initialize()
                // Model loaded successfully
            } catch (e: Exception) {
                // Handle initialization error
                e.printStackTrace()
                // Could show a notification or fallback to MockAiService
            }
        }
    }
    
    override fun onTerminate() {
        super.onTerminate()
        aiService.release()
    }
}
```

**Update AndroidManifest.xml**:

```xml
<application
    android:name=".BookkeepingApplication"
    android:allowBackup="true"
    ...>
    <!-- activities -->
</application>
```

### Step 5: Update MainActivity

**File**: `app/src/main/java/com/root2rise/bookkeeping/MainActivity.kt`

```kotlin
class MainActivity : ComponentActivity() {
    
    private lateinit var viewModel: BookkeepingViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Get AI service from Application
        val app = application as BookkeepingApplication
        val aiService = app.aiService
        
        // Initialize dependencies
        val database = BookkeepingDatabase.getDatabase(this)
        val repository = BookkeepingRepository(database)
        
        // Create ViewModel with production AI service
        val factory = BookkeepingViewModelFactory(repository, aiService)
        viewModel = ViewModelProvider(this, factory)[BookkeepingViewModel::class.java]
        
        setContent {
            BookkeepingTheme {
                HomeScreen(viewModel = viewModel)
            }
        }
    }
}
```

## 🔧 Configuration Options

### Model Configuration

```kotlin
val config = ModelConfig.Builder()
    .setModelPath("bookkeeping_model.tflite")
    .setNumThreads(4)              // CPU threads (2-4 recommended)
    .setMaxTokens(512)             // Max output length
    .setTemperature(0.1f)          // Low for consistent JSON
    .setTopK(5)                    // Sampling parameter
    .setTopP(0.9f)                 // Nucleus sampling
    .setRepetitionPenalty(1.1f)    // Prevent repetition
    .build()
```

### Firebender Optimization Modes

```kotlin
// Speed-optimized (faster inference, slightly lower accuracy)
Optimizer.optimize(model, Optimizer.SPEED_OPTIMIZED)

// Balanced (good trade-off)
Optimizer.optimize(model, Optimizer.BALANCED)

// Accuracy-optimized (better accuracy, slower)
Optimizer.optimize(model, Optimizer.ACCURACY_OPTIMIZED)
```

## 📊 Performance Benchmarks

### Target Performance

- **Model Load Time**: < 3 seconds
- **Inference Time**: < 500ms per utterance
- **Memory Usage**: < 100MB
- **Battery Impact**: Minimal (< 2% per hour of active use)

### Measuring Performance

```kotlin
class FirebenderAiService(private val context: Context) : AiService {
    
    override suspend fun processUtterance(utterance: String): String {
        val startTime = System.currentTimeMillis()
        
        val response = model?.generate(buildPrompt(utterance))
        
        val inferenceTime = System.currentTimeMillis() - startTime
        Log.d("AI_PERF", "Inference time: ${inferenceTime}ms")
        
        // Log to analytics (optional)
        Analytics.logEvent("ai_inference", mapOf(
            "time_ms" to inferenceTime,
            "input_length" to utterance.length
        ))
        
        return extractJson(response ?: "")
    }
}
```

## 🐛 Debugging & Testing

### Test with Mock First

Keep `MockAiService` for testing without the actual model:

```kotlin
// In tests or development
val aiService = if (BuildConfig.DEBUG && USE_MOCK) {
    MockAiService()
} else {
    FirebenderAiService(context)
}
```

### Model Output Validation

```kotlin
private fun validateJsonOutput(json: String): Boolean {
    return try {
        val response = AiResponseParser.parse(json)
        response != null
    } catch (e: Exception) {
        Log.e("AI_VALIDATION", "Invalid JSON output: $json", e)
        false
    }
}
```

### Fallback Strategy

```kotlin
override suspend fun processUtterance(utterance: String): String {
    return try {
        // Try production model
        val response = model?.generate(buildPrompt(utterance))
        val json = extractJson(response ?: "")
        
        if (validateJsonOutput(json)) {
            json
        } else {
            // Fallback to mock service
            MockAiService().processUtterance(utterance)
        }
    } catch (e: Exception) {
        // Fallback on error
        MockAiService().processUtterance(utterance)
    }
}
```

## 🔐 Security Considerations

### Model Protection

1. **Obfuscate model file**: Use ProGuard/R8 to make reverse engineering harder
2. **Encrypt assets**: Encrypt the .tflite file in assets
3. **Integrity check**: Verify model hash before loading

```kotlin
private fun verifyModelIntegrity(): Boolean {
    val modelBytes = context.assets.open("bookkeeping_model.tflite").readBytes()
    val hash = MessageDigest.getInstance("SHA-256").digest(modelBytes)
    val expectedHash = "your_model_hash_here"
    return hash.contentEquals(expectedHash.decodeHex())
}
```

## 📈 Monitoring

### Key Metrics to Track

1. **Model load success rate**
2. **Average inference time**
3. **JSON parsing success rate**
4. **User correction rate** (how often users edit AI outputs)
5. **Crash rate related to AI**

```kotlin
// Example analytics
Analytics.logEvent("model_initialized", mapOf(
    "success" to true,
    "load_time_ms" to loadTime
))

Analytics.logEvent("inference_completed", mapOf(
    "utterance_length" to utterance.length,
    "inference_time_ms" to inferenceTime,
    "json_valid" to isValid
))
```

## 🚀 Deployment Checklist

Before releasing with the real AI model:

- [ ] Model file added to assets
- [ ] Model integrity verification implemented
- [ ] Performance benchmarks met
- [ ] Memory usage within limits
- [ ] Battery impact tested
- [ ] Fallback to mock service working
- [ ] Error handling comprehensive
- [ ] Analytics tracking setup
- [ ] Tested on low-end devices (2GB RAM)
- [ ] Tested on various Android versions (7.0+)
- [ ] Offline functionality verified
- [ ] Model accuracy > 90% on test dataset

## 🆘 Troubleshooting

### Issue: Model fails to load

**Solutions:**

- Check model file exists in assets
- Verify model format (must be TFLite)
- Check available memory
- Ensure device supports required operations

### Issue: Inference is slow (> 1s)

**Solutions:**

- Reduce model size
- Increase CPU threads (but not > device cores)
- Use Firebender SPEED_OPTIMIZED mode
- Consider quantization (int8)

### Issue: Invalid JSON output

**Solutions:**

- Improve prompt engineering
- Fine-tune model with more training data
- Implement JSON post-processing/repair
- Fall back to mock service

### Issue: High memory usage

**Solutions:**

- Use quantized model (int8 instead of float32)
- Release model after idle time
- Lazy load model (only when needed)
- Optimize model with Firebender

## 📚 Additional Resources

- [RunAnywhere SDK Documentation](https://docs.runanywhere.ai)
- [Firebender Optimization Guide](https://firebender.dev/docs)
- [TensorFlow Lite Guide](https://www.tensorflow.org/lite)
- [On-Device ML Best Practices](https://www.tensorflow.org/lite/performance/best_practices)

---

**Note**: Replace placeholder SDK names and versions with actual values when the real SDKs are
available. The current implementation uses `MockAiService` which works perfectly for development and
testing.
