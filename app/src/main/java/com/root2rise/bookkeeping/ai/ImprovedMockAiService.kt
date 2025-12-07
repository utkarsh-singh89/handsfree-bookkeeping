package com.root2rise.bookkeeping.ai

import android.util.Log
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

/**
 * ImprovedMockAiService - Option A (fixed & improved)
 *
 * - Keyword based classifier for: sale, expense, loan_taken, loan_given, profit, loss, other
 * - Supports Hinglish (Latin) and common Hindi words (Devanagari)
 * - Returns transaction or query JSON compatible with your app
 * - Adds "ui_color" field ("green" or "red") for direct UI use
 */
class ImprovedMockAiService : AiService {
    private val TAG = "ImprovedMockAI"

    // --- Keyword sets (Hinglish, English, common Hindi words) ---
    private val salesKeywords = setOf(
        // Latin / Hinglish
        "bikri", "bikri hui", "becha", "bechi", "bik gaya", "aamdani", "jama hua", "jama",
        "paisa aaya", "paisa aya", "customer ne diya", "sale", "sold", "revenue", "income", "received",
        // Devanagari
        "बिक्री", "बेचा", "आयी", "आया", "आया है"
    )

    private val expenseKeywords = setOf(
        "kharcha", "kharch", "paisa kharch hua", "bill bhar diya", "bijli bill", "bijli ka bill",
        "kiraya", "rent", "petrol", "diesel", "recharge", "kharida", "khareeda", "payment kiya",
        "expense", "paid", "payment", "spent", "cost", "purchase", "bill paid",
        // Devanagari
        "खर्च", "खर्चा", "बिल", "किराया", "पेट्रोल", "डीज़ल", "नुकसान"
    )

    private val loanTakenKeywords = setOf(
        "udhar liya", "udhaar liya", "udhar mila", "loan liya", "liye udhar", "liye udhaar",
        "borrowed", "loan taken", "paise liye",
        // Devanagari
        "उधार लिया", "लोन लिया", "उधार मिला"
    )

    private val loanGivenKeywords = setOf(
        "udhar diya", "udhaar diya", "loan diya", "maine usko udhar diya", "loan given", "lent",
        // Devanagari
        "उधार दिया", "लोन दिया"
    )

    private val creditKeywords = setOf(
        "mila", "mile", "jama", "receive", "got", "aaya", "aya", "credited", "paisa aaya",
        "मिला", "आया", "जमा"
    )

    private val debitKeywords = setOf(
        "bhar diya", "nikal gaya", "payment", "outflow", "de diya", "kharcha kiya", "diya", "diye",
        "दिया", "दिए", "भरा"
    )

    private val queryKeywords = setOf(
        "kitna", "kitni", "kitna balance", "batao", "bataye", "kya", "aaj ka kitna", "total kitna",
        "summary", "how much", "total", "balance", "show", "summary",
        "कितना", "बताओ", "कुल"
    )

    private val profitKeywords = setOf("munafa", "fayda", "profit hua", "net profit", "profit", "gain", "earnings", "मुनाफा", "फायदा")
    private val lossKeywords = setOf("nuksaan", "ghaata", "loss hua", "total loss", "loss", "negative", "deficit", "नुकसान", "घाटा")

    // Hindi number words (comprehensive mapping)
    private val hindiNumbers = mapOf(
        // 1-10
        "ek" to 1, "do" to 2, "teen" to 3, "char" to 4,
        "paanch" to 5, "panch" to 5, "chhe" to 6, "chhah" to 6,
        "saat" to 7, "aath" to 8, "aat" to 8, "nau" to 9, "das" to 10,
        // 11-19
        "gyarah" to 11, "barah" to 12, "terah" to 13, "chaudah" to 14,
        "pandrah" to 15, "solah" to 16, "satrah" to 17, "atharah" to 18, "unnees" to 19,
        // Multiples of 10
        "bees" to 20, "tees" to 30, "chaalees" to 40, "chalees" to 40,
        "pachaas" to 50, "pachass" to 50, "saath" to 60, "sath" to 60,
        "sattar" to 70, "assi" to 80, "aasi" to 80, "nabbe" to 90, "nabbey" to 90,
        // Multipliers (handled separately but included for standalone use)
        "sau" to 100, "so" to 100,
        "hazaar" to 1000, "hazar" to 1000, "hajaar" to 1000,
        "lakh" to 100000, "lac" to 100000, "laakh" to 100000
    )

    override suspend fun processUtterance(utterance: String): String {
        Log.d(TAG, "========================================")
        Log.d(TAG, "🎤 RAW INPUT: $utterance")
        val normalized = normalizeInput(utterance)
        val lower = normalized.lowercase(Locale.getDefault()).trim()
        Log.d(TAG, "✅ NORMALIZED: $lower")

        // Query detection
        if (isQuery(lower)) {
            Log.d(TAG, "Detected: QUERY")
            return buildQueryResponse(lower, normalized)
        }

        // Transaction processing
        val amount = extractAmount(normalized)
        val partyName = extractPartyName(normalized)
        Log.d(TAG, "Amount: $amount, Party: $partyName")

        val classification = classifyTransaction(lower, partyName)
        Log.d(TAG, "Classification: ${classification.type}, ${classification.direction}")

        return buildTransactionResponse(
            classification = classification,
            amount = amount,
            partyName = partyName,
            originalUtterance = utterance
        )
    }

    // ----------------- Helpers -----------------

    private fun isQuery(lower: String): Boolean {
        // Profit/loss analytical queries
        if ((profitKeywords.any { lower.contains(it) } || lossKeywords.any { lower.contains(it) }) &&
            queryKeywords.any { lower.contains(it) }) {
            return true
        }
        if (queryKeywords.any { lower.contains(it) }) return true
        if (lower.contains("total") && (lower.contains("bikri") || lower.contains("kharcha") || lower.contains("sales") || lower.contains("expense") || lower.contains("profit") || lower.contains("loss"))) return true
        if (lower.contains("?")) return true
        return false
    }

    private fun normalizeInput(utterance: String): String {
        return utterance
            .replace("₹", " ")
            .replace("rs.", " ")
            .replace("rupees", "rupaye")
            .replace("rupy", "rupaye")
            .replace("udhaar", "udhar")
            .replace(Regex("\\s+"), " ").trim()
    }

    private data class Classification(val type: String, val direction: String)

    private fun classifyTransaction(lower: String, partyName: String?): Classification {
        // PRIORITY 1: Loan specific phrases
        loanTakenKeywords.forEach {
            if (lower.contains(it)) {
                Log.d(TAG, "✅ LOAN TAKEN matched: '$it'")
                return Classification("loan_taken", "in")
            }
        }
        loanGivenKeywords.forEach {
            if (lower.contains(it)) {
                Log.d(TAG, "✅ LOAN GIVEN matched: '$it'")
                return Classification("loan_given", "out")
            }
        }

        // Generic "udhar" handling
        if (lower.contains("udhar") || lower.contains("loan")) {
            Log.d(TAG, "Detected: Generic LOAN mention")
            // prefer verb context
            if (lower.contains("liye") || lower.contains("liya") || lower.contains("mila")) {
                return Classification("loan_taken", "in")
            }
            if (lower.contains("diya") || lower.contains("diye") || lower.contains("de diya")) {
                return Classification("loan_given", "out")
            }
            // preposition heuristics: "X se" => taken, "X ko" => given
            if (partyName != null) {
                if (lower.contains(" se ") || lower.contains(" from ")) return Classification("loan_taken", "in")
                if (lower.contains(" ko ") || lower.contains(" to ")) return Classification("loan_given", "out")
            }
            // fallback
            return Classification("loan_given", "out")
        }

        // PRIORITY 2: Expense
        expenseKeywords.forEach {
            if (lower.contains(it)) {
                Log.d(TAG, "✅ EXPENSE matched: '$it'")
                // if sale keywords also present, choose stronger proximity: treat sale if sale keyword near number (but rare)
                if (salesKeywords.any { s -> lower.contains(s) } && keywordNearNumber(lower, salesKeywords)) {
                    Log.d(TAG, "⚠️ Both EXPENSE and SALE present; choosing SALE due to proximity")
                    return Classification("sale", "in")
                }
                return Classification("expense", "out")
            }
        }

        // PRIORITY 3: Sale
        salesKeywords.forEach {
            if (lower.contains(it)) {
                Log.d(TAG, "✅ SALE matched: '$it'")
                return Classification("sale", "in")
            }
        }

        // PRIORITY 4: Credit / Debit verbs
        if (creditKeywords.any { lower.contains(it) }) {
            Log.d(TAG, "✅ CREDIT verb matched")
            return Classification("sale", "in")
        }
        if (debitKeywords.any { lower.contains(it) }) {
            Log.d(TAG, "✅ DEBIT verb matched")
            return Classification("expense", "out")
        }

        // PRIORITY 5: Verb heuristics
        if ((lower.contains("diya") || lower.contains("diye") || lower.contains("de diya")) &&
            !lower.contains("udhar") && !lower.contains("loan")) {
            Log.d(TAG, "Detected: DIYA -> EXPENSE")
            return Classification("expense", "out")
        }
        if ((lower.contains("liya") || lower.contains("liye") || lower.contains("mila")) &&
            !lower.contains("udhar") && !lower.contains("loan")) {
            Log.d(TAG, "Detected: LIYA -> INCOME")
            return Classification("sale", "in")
        }

        // CONTEXT: party + preposition
        if (partyName != null) {
            if (lower.contains(" ko ") || lower.contains(" को ")) {
                Log.d(TAG, "Context: 'ko' with party -> EXPENSE")
                return Classification("expense", "out")
            }
            if (lower.contains(" se ") || lower.contains(" से ")) {
                Log.d(TAG, "Context: 'se' with party -> INCOME")
                return Classification("sale", "in")
            }
        }

        // Passive forms often indicate sale (e.g., "bik gayi", "hui")
        if (lower.contains("hui") || lower.contains("hua") || lower.contains("huyi") || lower.contains("हुई") || lower.contains("हुआ")) {
            Log.d(TAG, "Detected: passive 'hui/hua' -> SALE")
            return Classification("sale", "in")
        }

        // If profit/loss terms present but not query (rare), treat as summary
        if (profitKeywords.any { lower.contains(it) }) return Classification("profit", "in")
        if (lossKeywords.any { lower.contains(it) }) return Classification("loss", "out")

        // FALLBACK: conservative -> expense/out/red
        Log.w(TAG, "⚠️ CLASSIFICATION FAILED - No keyword match, defaulting to EXPENSE/OUT")
        return Classification("expense", "out")
    }

    // Check if any keyword from list appears near a number token
    private fun keywordNearNumber(text: String, keywords: Set<String>, window: Int = 3): Boolean {
        val tokens = text.split(Regex("\\s+"))
        val numberIndices = tokens.mapIndexedNotNull { i, t ->
            if (Regex("""\d+(\.\d+)?""").containsMatchIn(t)) i else null
        }
        if (numberIndices.isEmpty()) return false
        for (kw in keywords) {
            val kwTokens = kw.split(Regex("\\s+"))
            val pos = indexOfPhrase(tokens, kwTokens)
            if (pos >= 0) {
                if (numberIndices.any { ni -> kotlin.math.abs(ni - pos) <= window }) return true
            } else {
                // check token contains as fallback
                tokens.forEachIndexed { i, t ->
                    if (t.contains(kw)) {
                        if (numberIndices.any { ni -> kotlin.math.abs(ni - i) <= window }) return true
                    }
                }
            }
        }
        return false
    }

    private fun indexOfPhrase(tokens: List<String>, phraseTokens: List<String>): Int {
        if (phraseTokens.isEmpty()) return -1
        for (i in 0..(tokens.size - phraseTokens.size)) {
            var match = true
            for (j in phraseTokens.indices) {
                if (!tokens[i + j].contains(phraseTokens[j])) {
                    match = false; break
                }
            }
            if (match) return i
        }
        return -1
    }

    // Amount extraction: SMART priority - numbers >= 1000 first, then multipliers
    private fun extractAmount(utterance: String): Double {
        val lower = utterance.lowercase(Locale.getDefault())
        Log.d(TAG, "🔢 Extracting amount from: '$utterance'")

        // === STRATEGY 1: PRIORITIZE LARGE STANDALONE NUMBERS (>= 1000) ===
        // This catches "4000", "2000", etc. BEFORE checking for "sau"/"hazaar"
        // Important: Check for numbers >= 1000 first to avoid false positives
        val largeNumericRegex = Regex("""(\d{4,})""")  // 4+ digits (1000+)
        val largeMatch = largeNumericRegex.find(utterance)
        if (largeMatch != null) {
            val result = largeMatch.value.toDoubleOrNull() ?: 0.0
            Log.d(TAG, "✅ Large number (>=1000) detected: $result")
            return result
        }

        // === STRATEGY 2: CHECK FOR HINDI MULTIPLIERS ===
        // "4 हजार" (4 hazaar), "ek lakh", etc.

        // Pattern: [digit(s)] + [lakh/lac]
        val lakhNumericPattern = Regex("""(\d+)\s*(?:lakh|lac|लाख|laakh)""")
        val lakhNumericMatch = lakhNumericPattern.find(lower)
        if (lakhNumericMatch != null) {
            val multiplier = lakhNumericMatch.groupValues[1].toDoubleOrNull() ?: 1.0
            val result = multiplier * 100000.0
            Log.d(TAG, "✅ Lakh with digit: $multiplier lakh = $result")
            return result
        }

        // Pattern: [digit(s)] + [hazaar/hazar]
        val hazaarNumericPattern = Regex("""(\d+)\s*(?:hazaar|hazar|hajaar|thousand|हजार)""")
        val hazaarNumericMatch = hazaarNumericPattern.find(lower)
        if (hazaarNumericMatch != null) {
            val multiplier = hazaarNumericMatch.groupValues[1].toDoubleOrNull() ?: 1.0
            val result = multiplier * 1000.0
            Log.d(TAG, "✅ Hazaar with digit: $multiplier hazaar = $result")
            return result
        }

        // Pattern: [digit(s)] + [sau/so]
        val sauNumericPattern = Regex("""(\d+)\s*(?:sau|so|hundred|सौ)""")
        val sauNumericMatch = sauNumericPattern.find(lower)
        if (sauNumericMatch != null) {
            val multiplier = sauNumericMatch.groupValues[1].toDoubleOrNull() ?: 1.0
            val result = multiplier * 100.0
            Log.d(TAG, "✅ Sau with digit: $multiplier sau = $result")
            return result
        }

        // Pattern: [Hindi word] + [lakh]
        val lakhWordPattern =
            Regex("""(ek|do|teen|char|paanch|panch|chhe|saat|aath|nau|das)\s*(?:lakh|lac|लाख|laakh)""")
        val lakhWordMatch = lakhWordPattern.find(lower)
        if (lakhWordMatch != null) {
            val wordStr = lakhWordMatch.groupValues[1]
            val multiplier = hindiNumbers[wordStr]?.toDouble() ?: 1.0
            val result = multiplier * 100000.0
            Log.d(TAG, "✅ Lakh with Hindi word: $wordStr lakh = $result")
            return result
        }

        // Pattern: [Hindi word] + [hazaar]
        val hazaarWordPattern =
            Regex("""(ek|do|teen|char|paanch|panch|chhe|saat|aath|nau|das|bees|tees|chaalees|pachaas|saath|sattar|assi|nabbe)\s*(?:hazaar|hazar|hajaar|thousand|हजार)""")
        val hazaarWordMatch = hazaarWordPattern.find(lower)
        if (hazaarWordMatch != null) {
            val wordStr = hazaarWordMatch.groupValues[1]
            val multiplier = hindiNumbers[wordStr]?.toDouble() ?: 1.0
            val result = multiplier * 1000.0
            Log.d(TAG, "✅ Hazaar with Hindi word: $wordStr hazaar = $result")
            return result
        }

        // Pattern: [Hindi word] + [sau]
        val sauWordPattern =
            Regex("""(ek|do|teen|char|paanch|panch|chhe|saat|aath|nau|das)\s*(?:sau|so|hundred|सौ)""")
        val sauWordMatch = sauWordPattern.find(lower)
        if (sauWordMatch != null) {
            val wordStr = sauWordMatch.groupValues[1]
            val multiplier = hindiNumbers[wordStr]?.toDouble() ?: 1.0
            val result = multiplier * 100.0
            Log.d(TAG, "✅ Sau with Hindi word: $wordStr sau = $result")
            return result
        }

        // === STRATEGY 3: SMALLER STANDALONE NUMBERS (100-999) ===
        val mediumNumericRegex = Regex("""(\d{3})""")  // 3 digits (100-999)
        val mediumMatch = mediumNumericRegex.find(utterance)
        if (mediumMatch != null) {
            val result = mediumMatch.value.toDoubleOrNull() ?: 0.0
            Log.d(TAG, "✅ Medium number (100-999) detected: $result")
            return result
        }

        // === STRATEGY 4: SMALL NUMBERS (1-99) ===
        val smallNumericRegex = Regex("""(\d{1,2})""")
        val smallMatch = smallNumericRegex.find(utterance)
        if (smallMatch != null) {
            val result = smallMatch.value.toDoubleOrNull() ?: 0.0
            Log.d(TAG, "✅ Small number (1-99) detected: $result")
            return result
        }

        // === STRATEGY 5: FALLBACK - Hindi standalone words ===
        var total = 0.0
        hindiNumbers.forEach { (word, value) ->
            if (lower.contains(word) && value >= 100) {
                total += value
                Log.d(TAG, "Hindi multiplier word: $word = $value")
            }
        }

        if (total > 0) {
            Log.d(TAG, "✅ Total from Hindi words: $total")
            return total
        }

        Log.w(TAG, "⚠️ No amount found, defaulting to 0")
        return 0.0
    }

    // Party extraction: Unicode-aware patterns, returns null if none
    private fun extractPartyName(utterance: String): String? {
        // Pattern1: "<name> se" or "<name> se " (from)
        val sePattern = Regex("""([\p{L}\p{N}._-]{2,})\s+(?:se|से)\b""", RegexOption.IGNORE_CASE)
        val seMatch = sePattern.find(utterance)
        if (seMatch != null) {
            val name = seMatch.groupValues[1]
            if (!isCommonWord(name.lowercase(Locale.getDefault()))) {
                return name.capitalizeWords()
            }
        }
        // Pattern2: "<name> ko" (to)
        val koPattern = Regex("""([\p{L}\p{N}._-]{2,})\s+(?:ko|को)\b""", RegexOption.IGNORE_CASE)
        val koMatch = koPattern.find(utterance)
        if (koMatch != null) {
            val name = koMatch.groupValues[1]
            if (!isCommonWord(name.lowercase(Locale.getDefault()))) {
                return name.capitalizeWords()
            }
        }
        // Pattern3: "<name> ka" (possessive, often used in balance queries)
        val kaPattern = Regex("""([\p{L}\p{N}._-]{2,})\s+(?:ka|का)\b""", RegexOption.IGNORE_CASE)
        val kaMatch = kaPattern.find(utterance)
        if (kaMatch != null) {
            val name = kaMatch.groupValues[1]
            if (!isCommonWord(name.lowercase(Locale.getDefault()))) {
                return name.capitalizeWords()
            }
        }
        return null
    }

    private fun String.capitalizeWords(): String {
        return this.split(Regex("\\s+")).joinToString(" ") { it.replaceFirstChar { ch -> if (ch.isLowerCase()) ch.titlecase(Locale.getDefault()) else ch.toString() } }
    }

    private fun isCommonWord(word: String): Boolean {
        val commonWords = setOf(
            "aaj", "kal", "parso", "bijli", "chai", "pani", "bill", "rent", "saman", "maal", "stock",
            "kitna", "kitni", "total", "overall", "bikri", "kharcha", "udhar", "rupaye", "paisa",
            "maine", "mujhe", "usko", "isko", "unko", "inko", "kharida", "diya", "liya", "becha"
        )
        return commonWords.contains(word)
    }

    // Query JSON builder
    private fun buildQueryResponse(lower: String, original: String): String {
        val (action, partyName, timeRange) = when {
            lower.contains("ka balance") || (lower.contains("ka") && lower.contains("balance")) || lower.contains("ka kitna") -> {
                val p = extractPartyName(original)
                Triple("query_balance", p, null)
            }
            lower.contains("bikri") || lower.contains("sale") -> {
                val t = extractTimeRange(lower)
                Triple("query_total_sales", null, t)
            }
            lower.contains("kharcha") || lower.contains("expense") -> {
                val t = extractTimeRange(lower)
                Triple("query_total_expenses", null, t)
            }
            lower.contains("profit") || lower.contains("munafa") -> Triple("query_profit", null, extractTimeRange(lower))
            lower.contains("loss") || lower.contains("nuksaan") -> Triple("query_loss", null, extractTimeRange(lower))
            else -> Triple("query_overall_summary", null, extractTimeRange(lower))
        }

        val obj = JSONObject()
        obj.put("kind", "query")
        obj.put("action", action)
        obj.put("party_name", partyName ?: JSONObject.NULL)
        obj.put("time_range", timeRange ?: JSONObject.NULL)
        val json = obj.toString()
        Log.d(TAG, "Generated QUERY JSON: $json")
        return json
    }

    private fun extractTimeRange(lower: String): String? {
        return when {
            lower.contains("aaj") || lower.contains("today") -> "today"
            lower.contains("kal") && !lower.contains("baad") -> "yesterday"
            lower.contains("hafta") || lower.contains("week") -> "this_week"
            lower.contains("mahina") || lower.contains("month") || lower.contains("mahine") -> "this_month"
            lower.contains("ab tak") || lower.contains("overall") || lower.contains("total") -> "all"
            else -> "today"
        }
    }

    // Transaction JSON builder: adds ui_color (green/red)
    private fun buildTransactionResponse(
        classification: Classification,
        amount: Double,
        partyName: String?,
        originalUtterance: String
    ): String {

        val notes = when (classification.type) {
            "loan_taken" -> partyName?.let { "Loan from $it" } ?: "Loan received"
            "loan_given" -> partyName?.let { "Loan to $it" } ?: "Loan given"
            "sale" -> {
                // Show full description for sales too
                if (partyName != null) {
                    "Sale to $partyName: ${originalUtterance.take(80)}"
                } else {
                    // Use original utterance as description
                    originalUtterance.take(100)
                }
            }

            "expense" -> "Expense: ${extractExpenseType(originalUtterance)}"
            "purchase" -> "Inventory purchase: ${originalUtterance.take(80)}"
            "profit" -> "Profit: ${originalUtterance.take(120)}"
            "loss" -> "Loss: ${originalUtterance.take(120)}"
            else -> "Unclassified: ${originalUtterance.take(200)}"
        }

        val direction = classification.direction
        val uiColor = if (direction == "in") "green" else "red"

        val obj = JSONObject()
        obj.put("kind", "transaction")
        obj.put("action", "add_transaction")
        obj.put("direction", direction) // "in" or "out"
        obj.put("type", classification.type)
        obj.put("party_name", partyName ?: JSONObject.NULL)
        obj.put("amount", amount)
        obj.put("date", "today")
        obj.put("notes", notes)
        obj.put("ui_color", uiColor) // explicit UI hint

        val json = obj.toString()
        Log.d(TAG, "Generated TRANSACTION JSON: $json")
        return json
    }

    private fun extractExpenseType(utterance: String): String {
        val lower = utterance.lowercase(Locale.getDefault())
        return when {
            lower.contains("bijli") || lower.contains("electricity") -> "Electricity bill"
            lower.contains("rent") || lower.contains("kiraya") -> "Rent"
            lower.contains("chai") || lower.contains("tea") -> "Tea/Snacks"
            lower.contains("petrol") || lower.contains("fuel") -> "Fuel"
            lower.contains("salary") || lower.contains("wages") -> "Salary/Wages"
            lower.contains("internet") -> "Internet"
            lower.contains("mobile") || lower.contains("phone") -> "Mobile"
            lower.contains("water") || lower.contains("pani") -> "Water"
            else -> utterance.take(100)
        }
    }
}
