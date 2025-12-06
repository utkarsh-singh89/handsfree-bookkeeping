# RunAnywhere SDK - Quick Integration Guide

## 🚀 5-Minute Setup (Once You Have the SDK)

### Step 1: Add AAR Files (30 seconds)

```bash
# Create libs folder if it doesn't exist
mkdir -p app/libs/

# Copy your AAR files
cp /path/to/runanywhere-sdk.aar app/libs/
```

### Step 2: Update build.gradle.kts (30 seconds)

Add to `app/build.gradle.kts`:

```kotlin
dependencies {
    // ... existing dependencies ...
    
    // RunAnywhere SDK
    implementation(files("libs/runanywhere-sdk.aar"))
    
    // If SDK has dependencies, add them here
    // implementation("com.example:dependency:1.0.0")
}
```

### Step 3: Update LLMInferenceEngine (3 minutes)

In `RunAnywhereAiService.kt`, find the `LLMInferenceEngine` class and replace with:

```kotlin
import com.runanywhere.sdk.RunAnywhereModel  // Replace with actual import
import com.runanywhere.sdk.ModelConfig        // Replace with actual import

private class LLMInferenceEngine(
    private val context: Context,
    private val modelFile: File
) {
    private val TAG = "LLMEngine"
    private var model: RunAnywhereModel? = null
    
    fun load(): Boolean {
        return try {
            Log.d(TAG, "Loading model from: ${modelFile.path}")
            
            // REPLACE THIS with actual SDK API:
            // Example 1: Direct file path
            model = RunAnywhereModel.load(context, modelFile.path)
            
            // OR Example 2: With config
            // val config = ModelConfig.builder()
            //     .setModelPath(modelFile.path)
            //     .setNumThreads(4)
            //     .build()
            // model = RunAnywhereModel.create(context, config)
            
            Log.d(TAG, "✅ Model loaded successfully")
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "Model loading failed: ${e.message}", e)
            false
        }
    }
    
    fun generate(prompt: String, maxTokens: Int, temperature: Float): String {
        return try {
            // REPLACE THIS with actual SDK API:
            // Example 1: Simple generation
            model?.generate(prompt) ?: ""
            
            // OR Example 2: With parameters
            // model?.generate(prompt, maxTokens, temperature) ?: ""
            
            // OR Example 3: With options
            // val options = GenerationOptions.builder()
            //     .setMaxTokens(maxTokens)
            //     .setTemperature(temperature)
            //     .build()
            // model?.generate(prompt, options) ?: ""
            
        } catch (e: Exception) {
            Log.e(TAG, "Generation failed: ${e.message}", e)
            throw e
        }
    }
    
    fun close() {
        try {
            // REPLACE THIS with actual SDK cleanup:
            model?.release()
            // OR: model?.close()
            // OR: model?.destroy()
            model = null
            Log.d(TAG, "Model resources released")
        } catch (e: Exception) {
            Log.e(TAG, "Cleanup error: ${e.message}")
        }
    }
}
```

### Step 4: Sync & Build (1 minute)

```bash
# In Android Studio:
# 1. Click "Sync Now" in build.gradle.kts
# 2. Build > Rebuild Project
# 3. Run app
```

### Step 5: Test (30 seconds)

```bash
# Check logs
adb logcat | grep -E "RunAnywhereAI|LLMEngine"

# Expected output:
# RunAnywhereAI: Initializing AI model...
# LLMEngine: Loading model from: /data/.../models/smollm2...
# LLMEngine: ✅ Model loaded successfully
# RunAnywhereAI: ✅ Model loaded successfully
```

---

## 🔍 Finding the Right SDK Methods

### Method 1: Check SDK Documentation

Look for:

- `Model.load()` or `Model.create()`
- `Model.generate()` or `Model.infer()`
- `Model.close()` or `Model.release()`

### Method 2: Inspect AAR

```bash
# Extract AAR
unzip runanywhere-sdk.aar -d temp/

# Find classes
cat temp/classes.jar | jar tf | grep -i model

# Expected output:
# com/runanywhere/sdk/RunAnywhereModel.class
# com/runanywhere/sdk/ModelConfig.class
```

### Method 3: Use Android Studio Autocomplete

In the `load()` function, type:

```kotlin
model = RunAnywhereModel.  // <-- Ctrl+Space to see methods
```

### Method 4: Check Examples

SDK should include example code. Look for:

- `examples/` folder in SDK
- `README.md` with usage
- Sample app project

---

## 📋 Common SDK Patterns

### Pattern 1: Simple (Single Method)

```kotlin
// Load
model = RunAnywhereModel.load(context, modelPath)

// Generate
val output = model.generate(prompt)

// Close
model.close()
```

### Pattern 2: Builder Pattern

```kotlin
// Load with config
val config = ModelConfig.builder()
    .setModelPath(modelPath)
    .setNumThreads(4)
    .setMaxTokens(512)
    .build()
model = RunAnywhereModel.create(context, config)

// Generate with options
val options = GenerationOptions.builder()
    .setMaxTokens(256)
    .setTemperature(0.3f)
    .setTopP(0.9f)
    .build()
val output = model.generate(prompt, options)

// Close
model.release()
```

### Pattern 3: Callback-based

```kotlin
// Load
RunAnywhereModel.loadAsync(context, modelPath) { result ->
    if (result.isSuccess) {
        model = result.getModel()
    }
}

// Generate
model.generateAsync(prompt) { output ->
    // Handle output
}

// Close
model.destroy()
```

### Pattern 4: Coroutine-based

```kotlin
// Load
model = withContext(Dispatchers.IO) {
    RunAnywhereModel.loadSuspend(context, modelPath)
}

// Generate
val output = withContext(Dispatchers.IO) {
    model.generateSuspend(prompt)
}

// Close
model.close()
```

---

## 🧪 Testing After Integration

### Test 1: Model Loads

```kotlin
// Add to BookkeepingApplication.onCreate()
applicationScope.launch {
    val success = (aiService as? RunAnywhereAiService)?.initialize()
    Log.d("TEST", "Model loaded: $success")
    // Should log: "Model loaded: true"
}
```

### Test 2: Inference Works

```kotlin
// Test in ViewModel
viewModelScope.launch {
    val json = aiService.processUtterance("Ramesh se 500 liye udhar")
    Log.d("TEST", "JSON output: $json")
    // Should log valid JSON
}
```

### Test 3: JSON is Valid

```kotlin
// Add after processUtterance()
val response = AiResponseParser.parse(json)
Log.d("TEST", "Parsed: ${response?.kind}")
// Should log: "Parsed: transaction"
```

---

## ⚠️ Common Issues

### Issue: "Cannot resolve symbol 'RunAnywhereModel'"

**Cause:** AAR not added or gradle not synced

**Fix:**

```bash
# 1. Check app/libs/runanywhere-sdk.aar exists
# 2. Check build.gradle.kts has implementation(files("libs/..."))
# 3. File > Sync Project with Gradle Files
# 4. Build > Clean Project
# 5. Build > Rebuild Project
```

### Issue: "No such file or directory" (model path)

**Cause:** Model file not downloaded or wrong path

**Fix:**

```kotlin
// Add logging
Log.d("TEST", "Model path: ${modelFile.path}")
Log.d("TEST", "Model exists: ${modelFile.exists()}")
Log.d("TEST", "Model size: ${modelFile.length()} bytes")
```

### Issue: "UnsatisfiedLinkError: couldn't find librunanywhereXXX.so"

**Cause:** Native libraries not in AAR or wrong ABI

**Fix:**

```kotlin
// Check AAR contains .so files
unzip -l runanywhere-sdk.aar | grep ".so"

// Expected:
// jni/arm64-v8a/librunanywhereXXX.so
// jni/armeabi-v7a/librunanywhereXXX.so
```

If missing, SDK may need separate native library dependency:

```kotlin
implementation("com.runanywhere:native:1.0.0")
```

### Issue: Model loads but generates empty output

**Cause:** Wrong model format or prompt format

**Fix:**

```kotlin
// Test with simple prompt
val output = model.generate("Hello")
Log.d("TEST", "Output: '$output'")

// If empty, model may expect specific format:
val output = model.generate("<|user|>\nHello\n<|assistant|>")
```

---

## 📞 Need SDK-Specific Help?

### If SDK is from Hackss:

1. Check Hackss documentation
2. Look for example projects
3. Check Discord/Slack for support

### If SDK is proprietary:

1. Contact vendor for docs
2. Request sample code
3. Ask for API reference

### If SDK is open-source:

1. Check GitHub repo: [repo URL]
2. Read API docs: [docs URL]
3. Look at examples: [examples folder]

---

## ✅ Integration Checklist

- [ ] AAR file added to `app/libs/`
- [ ] `build.gradle.kts` updated with dependency
- [ ] Project synced and builds successfully
- [ ] `LLMInferenceEngine.load()` implemented
- [ ] `LLMInferenceEngine.generate()` implemented
- [ ] `LLMInferenceEngine.close()` implemented
- [ ] App runs without crashes
- [ ] Model initialization logs appear
- [ ] Test utterance returns valid JSON
- [ ] JSON parses to TransactionSchema/QuerySchema
- [ ] Transaction saves to database
- [ ] UI updates correctly

---

## 🎉 You're Done!

Once the checklist is complete, your app is using real on-device AI!

**Next:** Test with 50+ real utterances and measure accuracy.

---

## 📚 Additional Resources

- [Full Integration Guide](RUNANYWHERE_INTEGRATION_GUIDE.md)
- [Prompt Engineering Guide](AI_PROMPT_ENGINEERING.md)
- [Changes Summary](RUNANYWHERE_CHANGES_SUMMARY.md)
- [Original AI Guide](AI_PROMPTS_GUIDE.md)

