# 🔍 Debugging Guide - Classification Issues

## Problem: Everything classified as "Expense"

If you see all transactions being classified as expenses, follow this debugging guide.

---

## Step 1: Enable Detailed Logs

The updated app now shows detailed classification logs.

### Watch logs in real-time:

```bash
adb logcat | grep ImprovedMockAI
```

### What you'll see:

```
ImprovedMockAI: ========================================
ImprovedMockAI: 🎤 RAW INPUT: आज की बिकी 2000
ImprovedMockAI: 🎤 INPUT BYTES: E0 A4 86 E0 A4 9C ... (hex dump)
ImprovedMockAI: ✅ NORMALIZED: आज की बिकी 2000
ImprovedMockAI: Amount: 2000.0, Party: null
ImprovedMockAI: ✅ SALE matched: 'biki'
ImprovedMockAI: Classification: sale, in
```

---

## Step 2: Test Specific Inputs

Try these test cases and watch the logs:

### Test 1: Sales

```
Say: "aaj ki bikri 2000"
Expected log: ✅ SALE matched: 'bikri'
Expected result: Sale ₹2000 (GREEN)
```

### Test 2: Expense

```
Say: "bijli ka bill 900"
Expected log: ✅ EXPENSE matched: 'bijli bill'
Expected result: Expense ₹900 (RED)
```

### Test 3: Loan Taken

```
Say: "Ramesh se 500 liye udhar"
Expected log: ✅ LOAN TAKEN matched: 'liye udhar'
Expected result: Loan Taken ₹500 (GREEN)
```

---

## Step 3: Check What Google STT is Sending

The raw input might be in **Devanagari script** or **Latin script**.

### If you see Devanagari (आज की बिकी):

- Google STT is sending Hindi script
- Keywords need Devanagari support
- **SOLUTION**: Added in latest update

### If you see Latin (aaj ki bikri):

- Google STT is sending transliterated Hindi
- Should work with current keywords
- ✅ Already supported

---

## Step 4: Common Issues & Solutions

### Issue 1: "Everything is Expense"

**Symptom**: All transactions show as red expenses

**Cause**: Keywords not matching due to:

- Google STT sending unexpected format
- Typos in voice recognition
- Missing keyword variations

**Solution**:

1. Check logs: `adb logcat | grep "🎤 RAW INPUT"`
2. See what exact text Google STT sends
3. Add that variation to keywords

**Example**:

```
Log shows: 🎤 RAW INPUT: बिकी की 2000
Missing keyword: "बिकी की"
```

Add to `salesKeywords`:

```kotlin
"बिकी की", "बिकी" 
```

### Issue 2: "No keyword matched"

**Symptom**: Logs show "⚠️ CLASSIFICATION FAILED"

**Cause**: Input doesn't contain any trained keyword

**Solution**:

1. Check raw input in logs
2. Identify the word user said
3. Add it to appropriate keyword set

### Issue 3: "Wrong transaction type"

**Symptom**: Sale classified as expense (or vice versa)

**Cause**: Both keyword sets match, wrong priority

**Solution**: Check logs for "⚠️ Both EXPENSE and SALE matched"

- Should show which keyword won
- Adjust priority if needed

---

## Step 5: How to Add New Keywords

### Example: "customer ne payment kiya"

1. **Identify the transaction type**: This is income (sale)

2. **Check logs** to see exact text:

```
🎤 RAW INPUT: customer ne payment kiya 500
```

3. **Add to salesKeywords**:

```kotlin
private val salesKeywords = setOf(
    // ... existing keywords ...
    "customer ne payment kiya",  // ADD THIS
    "customer ne payment"         // AND THIS
)
```

4. **Rebuild**:

```bash
./gradlew installDebug
```

5. **Test again**

---

## Step 6: Test All Categories

After fixing, test all 5 categories:

| Category | Test Input | Expected Keyword Match | Expected Result |
|----------|------------|----------------------|-----------------|
| **Sale** | "aaj ki bikri 2000" | 'bikri' or 'ki bikri' | Sale ₹2000 (GREEN) |
| **Expense** | "bijli ka bill 900" | 'bijli bill' | Expense ₹900 (RED) |
| **Loan Taken** | "Ramesh se 500 liye udhar" | 'liye udhar' | Loan Taken ₹500 (GREEN) |
| **Loan Given** | "Sunil ko 300 diya udhar" | 'diya udhar' | Loan Given ₹300 (RED) |
| **Query** | "aaj ki bikri kitni" | 'kitni' | Query Result |

---

## Step 7: Understanding the Logs

### Log Format:

```
🎤 RAW INPUT: [what Google STT sent]
🎤 INPUT BYTES: [hex dump for debugging]
✅ NORMALIZED: [after cleanup]
Amount: [extracted amount]
Party: [extracted party name]
✅ [TYPE] matched: '[matched keyword]'
Classification: [type], [direction]
```

### What to look for:

1. **RAW INPUT** - Did Google understand correctly?
2. **NORMALIZED** - Is cleanup working?
3. **Matched keyword** - Which keyword was triggered?
4. **Classification** - Final result

---

## Step 8: Report Issues

If still not working, share these logs:

```bash
adb logcat | grep ImprovedMockAI > debug_log.txt
```

Include:

1. What you said (in English/Hindi)
2. What the RAW INPUT shows
3. What result you got
4. What result you expected

---

## Quick Fixes

### Fix 1: Clear app data

```bash
adb shell pm clear com.root2rise.bookkeeping
```

### Fix 2: Reinstall

```bash
./gradlew installDebug
```

### Fix 3: Check Google STT language

Settings → Language → Hindi (India)

### Fix 4: Restart app

Force stop and relaunch

---

## Advanced: Keyword Coverage

Current coverage:

| Category | Latin Script | Devanagari | Variations |
|----------|--------------|------------|------------|
| **Sales** | ✅ 20+ | ⏳ Partial | ✅ Good |
| **Expense** | ✅ 25+ | ⏳ Partial | ✅ Good |
| **Loan** | ✅ 15+ | ⏳ Partial | ✅ Good |

If Google sends Devanagari, we need to add those keywords.

---

## Next Steps

1. **Test the app** with voice input
2. **Watch the logs** to see keyword matches
3. **Report** any misclassifications
4. **Share logs** if issues persist

The enhanced logging will show exactly what's happening! 🔍
