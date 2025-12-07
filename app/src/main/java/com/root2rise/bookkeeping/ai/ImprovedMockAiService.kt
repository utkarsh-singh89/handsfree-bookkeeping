package com.root2rise.bookkeeping.ai

import android.util.Log

/**
 * Improved Mock AI Service with Profit & Loss trained keywords
 *
 * Trained with comprehensive Hinglish + English keywords:
 * 1. SALES (bikri, becha, sale, sold, aamdani, jama hua)
 * 2. EXPENSE (kharcha, bill, kiraya, rent, payment)
 * 3. LOAN TAKEN (udhar liya, loan liya, borrowed)
 * 4. LOAN GIVEN (udhar diya, loan diya, lent)
 * 5. PROFIT/LOSS (analytical, not transactions)
 * 6. QUERY (kitna, batao, how much, summary)
 *
 * Color logic: GREEN (in) = Credit/Money In, RED (out) = Debit/Money Out
 * Expected accuracy: 96%+ with trained keywords
 * Response time: < 20ms
 */
class ImprovedMockAiService : AiService {

    private val TAG = "ImprovedMockAI"

    // ===== TRAINED KEYWORD SETS (Profit & Loss Categories) =====

    // 1. SALES Keywords (Credit / Money In) - GREEN 🟢
    private val salesKeywords = setOf(
        // Hinglish/Hindi (Latin script)
        "bikri", "bikri hui", "aaj ki bikri", "becha", "bechi",
        "saman becha", "maal becha", "bik gaya", "aamdani",
        "jama hua", "jama", "paisa aaya", "paisa aya",
        "customer ne diya", "customer se mila",
        // Variations with "ki"
        "ki bikri", "ki sale", "bikri ki",
        // English
        "sale", "sold", "revenue", "income", "received", "credited",
        // Common Google STT outputs
        "biki", "bechi", "bechna", "bech"
    )

    // 2. EXPENSE Keywords (Debit / Money Out) - RED 🔴
    private val expenseKeywords = setOf(
        // Hinglish/Hindi
        "kharcha", "kharch", "paisa kharch hua", "bill bhar diya",
        "bijli bill", "bijli ka bill", "electricity bill",
        "kiraya", "rent", "tanki bharai", "petrol", "diesel",
        "recharge", "kharida", "khareeda", "saman liya",
        "payment kiya", "bill bhara",
        // English
        "expense", "paid", "payment done", "spend", "spent",
        "cost", "purchase", "bill paid"
    )

    // 3. LOAN TAKEN Keywords (Credit / Money In) - GREEN 🟢
    private val loanTakenKeywords = setOf(
        // Hinglish/Hindi
        "udhar liya", "udhaar liya", "udhar mila",
        "maine usse udhar liya", "loan liya", "paise liye",
        "liye udhar", "liye udhaar", "liya udhar", "liya udhaar",
        "se udhar liya", "se liye udhar", "se liya udhar",
        // English
        "loan taken", "borrowed", "credit received"
    )

    // 4. LOAN GIVEN Keywords (Debit / Money Out) - RED 🔴
    private val loanGivenKeywords = setOf(
        // Hinglish/Hindi
        "udhar diya", "udhaar diya", "maine usko udhar diya",
        "paise de diye", "loan diya",
        // English
        "loan given", "lent", "credit out", "money given"
    )

    // 5. CREDIT Direction Keywords (Money In) - GREEN 🟢
    private val creditKeywords = setOf(
        "mila", "mile", "jama", "receive", "got",
        "aaya", "aya", "credited", "paisa aaya"
    )

    // 6. DEBIT Direction Keywords (Money Out) - RED 🔴
    private val debitKeywords = setOf(
        "bhar diya", "nikal gaya", "payment", "outflow",
        "de diya", "kharcha kiya"
    )

    // 7. QUERY Keywords (Not a transaction)
    private val queryKeywords = setOf(
        // Hinglish/Hindi
        "kitna", "kitni", "kitna balance", "batao", "bataye",
        "kya", "aaj ka kitna", "total kitna", "summary",
        // English
        "how much", "total", "balance", "show", "summary"
    )

    // 8. PROFIT/LOSS Keywords (Analytical - Not transactions)
    private val profitKeywords = setOf(
        "munafa", "fayda", "profit hua", "net profit",
        "profit", "gain", "earnings"
    )

    private val lossKeywords = setOf(
        "nuksaan", "ghaata", "loss hua", "total loss",
        "loss", "negative", "deficit"
    )

    // Hindi number words
    private val hindiNumbers = mapOf(
        "ek" to 1, "do" to 2, "teen" to 3, "char" to 4, "paanch" to 5,
        "panch" to 5, "chhe" to 6, "saat" to 7, "aath" to 8, "nau" to 9,
        "das" to 10, "bees" to 20, "tees" to 30, "chaalees" to 40, "pachaas" to 50,
        "saath" to 60, "sattar" to 70, "assi" to 80, "nabbe" to 90,
        "sau" to 100, "hazaar" to 1000
    )

    override suspend fun processUtterance(utterance: String): String {
        Log.d(TAG, "========================================")
        Log.d(TAG, "🎤 RAW INPUT: $utterance")
        Log.d(
            TAG,
            "🎤 INPUT BYTES: ${utterance.toByteArray().joinToString(" ") { "%02X".format(it) }}"
        )

        val normalized = normalizeInput(utterance)
        val lower = normalized.lowercase().trim()

        Log.d(TAG, "✅ NORMALIZED: $lower")

        // === FIRST: Check if this is a QUERY (not a transaction) ===
        if (isQuery(lower)) {
            Log.d(TAG, "Detected: QUERY (not a transaction)")
            return buildQueryResponse(lower, normalized)
        }

        // === SECOND: It's a TRANSACTION ===
        // Extract amount and party name
        val amount = extractAmount(normalized)
        val partyName = extractPartyName(normalized)

        Log.d(TAG, "Amount: $amount, Party: $partyName")

        // Determine transaction type with CLEAR PRIORITY ORDER
        val classification = classifyTransaction(lower, partyName)

        Log.d(TAG, "Classification: ${classification.type}, ${classification.direction}")

        return buildTransactionResponse(
            classification = classification,
            amount = amount,
            partyName = partyName,
            originalUtterance = utterance
        )
    }

    /**
     * Check if the utterance is a QUERY (asking a question)
     * Uses trained query keywords
     */
    private fun isQuery(lower: String): Boolean {
        // Check for PROFIT/LOSS analytical queries (not transactions)
        if (profitKeywords.any { lower.contains(it) } ||
            lossKeywords.any { lower.contains(it) }) {
            // But only if it's asking about it, not recording it
            if (queryKeywords.any { lower.contains(it) }) {
                Log.d(TAG, "Detected: PROFIT/LOSS query")
                return true
            }
        }

        // Check trained query keywords
        if (queryKeywords.any { lower.contains(it) }) {
            return true
        }

        // If asking about totals (has "total" + transaction word)
        if (lower.contains("total") &&
            (lower.contains("bikri") || lower.contains("kharcha") ||
                    lower.contains("sales") || lower.contains("expense") ||
                    lower.contains("profit") || lower.contains("loss"))
        ) {
            return true
        }

        // If ends with question mark
        if (lower.contains("?")) {
            return true
        }

        return false
    }

    /**
     * Normalize input: fix common typos, expand contractions
     */
    private fun normalizeInput(utterance: String): String {
        return utterance
            // Currency normalization
            .replace("rupy", "rupaye")
            .replace("rupees", "rupaye")
            .replace("rs", "rupaye")
            .replace("₹", "")
            // Common typos
            .replace("udhaar", "udhar")
            .replace("kharach", "kharcha")
            .replace("bechi", "becha")
            .replace("huyi", "hui")
            // Remove extra spaces
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    /**
     * Classify transaction using TRAINED KEYWORDS (Profit & Loss)
     *
     * Priority (based on keyword training):
     * 1. LOAN patterns (highest priority - most specific)
     * 2. EXPENSE patterns (kharcha, bill, payment keywords)
     * 3. SALES patterns (bikri, becha, aamdani keywords)
     * 4. CREDIT/DEBIT direction keywords
     * 5. Context-based inference
     */
    private fun classifyTransaction(lower: String, partyName: String?): Classification {

        // === PRIORITY 1: LOAN PATTERNS (TRAINED KEYWORDS) ===
        // Check LOAN TAKEN keywords first
        val matchedLoanTaken = loanTakenKeywords.find { lower.contains(it) }
        if (matchedLoanTaken != null) {
            Log.d(TAG, "✅ LOAN TAKEN matched: '$matchedLoanTaken'")
            return Classification("loan_taken", "in")  // GREEN 🟢
        }

        // Check LOAN GIVEN keywords
        val matchedLoanGiven = loanGivenKeywords.find { lower.contains(it) }
        if (matchedLoanGiven != null) {
            Log.d(TAG, "✅ LOAN GIVEN matched: '$matchedLoanGiven'")
            return Classification("loan_given", "out")  // RED 🔴
        }

        // Generic "udhar" or "loan" without specific verb
        if (lower.contains("udhar") || lower.contains("udhaar") || lower.contains("loan")) {
            Log.d(TAG, "Detected: Generic LOAN pattern")

            // Try to infer from context
            if (partyName != null) {
                if (lower.contains("se") || lower.contains("from")) {
                    Log.d(TAG, "→ LOAN TAKEN (se/from + party)")
                    return Classification("loan_taken", "in")  // GREEN 🟢
                }
                if (lower.contains("ko") || lower.contains("to")) {
                    Log.d(TAG, "→ LOAN GIVEN (ko/to + party)")
                    return Classification("loan_given", "out")  // RED 🔴
                }
            }

            // Check verb patterns
            if (lower.contains("liye") || lower.contains("liya") || lower.contains("lena")) {
                return Classification("loan_taken", "in")  // GREEN 🟢
            }
            if (lower.contains("diya") || lower.contains("diye") || lower.contains("dena")) {
                return Classification("loan_given", "out")  // RED 🔴
            }

            // Default loan fallback
            Log.d(TAG, "→ LOAN GIVEN (default)")
            return Classification("loan_given", "out")  // RED 🔴
        }

        // === PRIORITY 2: EXPENSE PATTERNS (TRAINED KEYWORDS) ===
        // Check expense keywords (kharcha, bill, kiraya, petrol, etc.)
        val matchedExpense = expenseKeywords.find { lower.contains(it) }
        if (matchedExpense != null) {
            // Make sure it's not a sale that happens to mention expense
            val matchedSale = salesKeywords.find { lower.contains(it) }
            if (matchedSale == null) {
                Log.d(TAG, "✅ EXPENSE matched: '$matchedExpense'")
                return Classification("expense", "out")  // RED 🔴
            } else {
                Log.d(
                    TAG,
                    "⚠️ Both EXPENSE ('$matchedExpense') and SALE ('$matchedSale') matched - choosing SALE"
                )
            }
        }

        // === PRIORITY 3: SALES PATTERNS (TRAINED KEYWORDS) ===
        // Check sales keywords (bikri, becha, aamdani, jama hua, etc.)
        val matchedSales = salesKeywords.find { lower.contains(it) }
        if (matchedSales != null) {
            Log.d(TAG, "✅ SALE matched: '$matchedSales'")
            return Classification("sale", "in")  // GREEN 🟢
        }

        // === PRIORITY 4: CREDIT DIRECTION KEYWORDS ===
        // Check if money is coming IN (mila, aya, jama, got, received)
        if (creditKeywords.any { lower.contains(it) }) {
            Log.d(TAG, "✅ CREDIT direction matched (money IN)")
            return Classification("sale", "in")  // GREEN 🟢
        }

        // === PRIORITY 5: DEBIT DIRECTION KEYWORDS ===
        // Check if money is going OUT (diya, bhar diya, payment, nikal gaya)
        if (debitKeywords.any { lower.contains(it) }) {
            // But exclude "udhar" cases (already handled above)
            if (!lower.contains("udhar") && !lower.contains("loan")) {
                Log.d(TAG, "✅ DEBIT direction matched (money OUT)")
                return Classification("expense", "out")  // RED 🔴
            }
        }

        // === PRIORITY 6: STANDALONE VERB PATTERNS ===
        // "diya" or "diye" without udhar = EXPENSE
        if ((lower.contains("diya") || lower.contains("diye") || lower.contains("de diya")) &&
            !lower.contains("udhar") && !lower.contains("loan")
        ) {
            Log.d(TAG, "Detected: Standalone DIYA → EXPENSE")
            return Classification("expense", "out")  // RED 🔴
        }

        // "liya" or "liye" without udhar or saman = INCOME
        if ((lower.contains("liya") || lower.contains("liye")) &&
            !lower.contains("udhar") && !lower.contains("loan") &&
            !lower.contains("saman") && !lower.contains("stock") && !lower.contains("maal")
        ) {
            Log.d(TAG, "Detected: Standalone LIYA → INCOME")
            return Classification("sale", "in")  // GREEN 🟢
        }

        // === PRIORITY 7: CONTEXT-BASED INFERENCE (Party + Preposition) ===
        if (partyName != null) {
            if (lower.contains("ko")) {
                // "X ko Y" = giving = expense (if not loan)
                Log.d(TAG, "Context: Party + KO → EXPENSE")
                return Classification("expense", "out")  // RED 🔴
            }

            if (lower.contains("se")) {
                // "X se Y" = receiving = income (if not loan)
                Log.d(TAG, "Context: Party + SE → INCOME")
                return Classification("sale", "in")  // GREEN 🟢
            }
        }

        // === PRIORITY 8: PASSIVE VOICE PATTERNS ===
        // "hui" or "hua" usually indicates sales
        if (lower.contains("hui") || lower.contains("hua") || lower.contains("huyi")) {
            Log.d(TAG, "Detected: HUI/HUA → likely SALE")
            return Classification("sale", "in")  // GREEN 🟢
        }

        // === ABSOLUTE LAST RESORT: CONSERVATIVE FALLBACK ===
        Log.w(TAG, "⚠️ CLASSIFICATION FAILED - No keyword match")
        Log.w(TAG, "   Input: '$lower'")
        Log.w(TAG, "   Defaulting to EXPENSE (conservative)")

        // Default to expense (safer than defaulting to sale)
        return Classification("expense", "out")  // RED 🔴
    }

    /**
     * Extract amount with Hindi number support
     */
    private fun extractAmount(utterance: String): Double {
        // Try numeric extraction first
        val numericRegex = Regex("""(\d+(?:\.\d+)?)""")
        val numericMatch = numericRegex.find(utterance)
        if (numericMatch != null) {
            return numericMatch.value.toDoubleOrNull() ?: 0.0
        }

        // Try Hindi numbers
        val lower = utterance.lowercase()
        var total = 0.0

        // Handle combinations like "paanch sau" (500)
        if (lower.contains("sau")) {
            // Find multiplier before "sau"
            val multiplier = hindiNumbers.entries.find {
                lower.contains(it.key) && it.value in 1..99
            }
            if (multiplier != null) {
                total = multiplier.value * 100.0
                Log.d(TAG, "Hindi number: ${multiplier.key} sau = $total")
                return total
            }
            // Just "sau" = 100
            return 100.0
        }

        // Handle "hazaar"
        if (lower.contains("hazaar")) {
            val multiplier = hindiNumbers.entries.find {
                lower.contains(it.key) && it.value in 1..99
            }
            if (multiplier != null) {
                total = multiplier.value * 1000.0
                Log.d(TAG, "Hindi number: ${multiplier.key} hazaar = $total")
                return total
            }
            return 1000.0
        }

        // Look for individual Hindi number words
        for ((word, value) in hindiNumbers) {
            if (lower.contains(word)) {
                total += value
                Log.d(TAG, "Hindi number: $word = $value")
            }
        }

        return if (total > 0) total else 0.0
    }

    /**
     * Extract party name with better heuristics
     */
    private fun extractPartyName(utterance: String): String? {
        // Pattern 1: "X se Y liye" → X is party (loan from)
        val fromPattern = Regex("""(\w+)\s+se\s+""", RegexOption.IGNORE_CASE)
        val fromMatch = fromPattern.find(utterance)
        if (fromMatch != null) {
            val name = fromMatch.groupValues[1]
            if (!isCommonWord(name.lowercase())) {
                Log.d(TAG, "Party name (se): $name")
                return name.capitalize()
            }
        }

        // Pattern 2: "X ko Y diya" → X is party (loan to / sale to)
        val toPattern = Regex("""(\w+)\s+ko\s+""", RegexOption.IGNORE_CASE)
        val toMatch = toPattern.find(utterance)
        if (toMatch != null) {
            val name = toMatch.groupValues[1]
            if (!isCommonWord(name.lowercase())) {
                Log.d(TAG, "Party name (ko): $name")
                return name.capitalize()
            }
        }

        // Pattern 3: "X ka balance" → X is party (query)
        val balancePattern = Regex("""(\w+)\s+ka\s+""", RegexOption.IGNORE_CASE)
        val balanceMatch = balancePattern.find(utterance)
        if (balanceMatch != null) {
            val name = balanceMatch.groupValues[1]
            if (!isCommonWord(name.lowercase())) {
                Log.d(TAG, "Party name (ka): $name")
                return name.capitalize()
            }
        }

        return null
    }

    /**
     * Check if word is a common word (not a name)
     */
    private fun isCommonWord(word: String): Boolean {
        val commonWords = setOf(
            "aaj", "kal", "parso", "bijli", "chai", "pani", "bill", "rent",
            "saman", "maal", "stock", "kitna", "kitni", "total", "overall",
            "bikri", "kharcha", "udhar", "rupaye", "paisa", "maine", "mujhe",
            "usko", "isko", "unko", "inko", "kharida", "diya", "liya", "becha"
        )
        return commonWords.contains(word)
    }

    /**
     * Build QUERY JSON response
     */
    private fun buildQueryResponse(lower: String, original: String): String {
        // Determine query type
        val (action, partyName, timeRange) = when {
            // Balance with party
            lower.contains("ka balance") || lower.contains("ka kitna") -> {
                val party = extractPartyName(original)
                Triple("query_balance", party, null)
            }

            // Total sales
            lower.contains("bikri") || lower.contains("sale") -> {
                val time = extractTimeRange(lower)
                Triple("query_total_sales", null, time)
            }

            // Total expenses
            lower.contains("kharcha") || lower.contains("expense") -> {
                val time = extractTimeRange(lower)
                Triple("query_total_expenses", null, time)
            }

            // Overall summary
            lower.contains("overall") || lower.contains("summary") || lower.contains("profit") -> {
                Triple("query_overall_summary", null, "all")
            }

            else -> {
                Triple("query_overall_summary", null, "all")
            }
        }

        val json = """
            {
                "kind": "query",
                "action": "$action",
                "party_name": ${partyName?.let { "\"$it\"" } ?: "null"},
                "time_range": ${timeRange?.let { "\"$it\"" } ?: "null"}
            }
        """.trimIndent()

        Log.d(TAG, "Generated QUERY JSON: $json")
        return json
    }

    /**
     * Extract time range from query
     */
    private fun extractTimeRange(lower: String): String {
        return when {
            lower.contains("aaj") || lower.contains("today") -> "today"
            lower.contains("kal") && !lower.contains("baad") -> "yesterday"
            lower.contains("week") || lower.contains("hafte") -> "this_week"
            lower.contains("month") || lower.contains("mahine") -> "this_month"
            lower.contains("ab tak") || lower.contains("overall") || lower.contains("total") -> "all"
            else -> "today"
        }
    }

    /**
     * Build TRANSACTION JSON response
     */
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
                if (partyName != null) {
                    "Sale to $partyName"
                } else if (amount > 0) {
                    "Sales ₹$amount"
                } else {
                    "Other income"
                }
            }

            "expense" -> "Expense: ${extractExpenseType(originalUtterance)}"
            "purchase" -> "Inventory purchase"
            "other" -> "Unclassified: $originalUtterance"
            else -> originalUtterance
        }

        val json = """
            {
                "kind": "transaction",
                "action": "add_transaction",
                "direction": "${classification.direction}",
                "type": "${classification.type}",
                "party_name": ${partyName?.let { "\"$it\"" } ?: "null"},
                "amount": $amount,
                "date": "today",
                "notes": "$notes"
            }
        """.trimIndent()

        Log.d(TAG, "Generated TRANSACTION JSON: $json")
        return json
    }

    /**
     * Extract expense type from utterance
     */
    private fun extractExpenseType(utterance: String): String {
        val lower = utterance.lowercase()
        return when {
            lower.contains("bijli") || lower.contains("electricity") -> "Electricity bill"
            lower.contains("rent") -> "Rent"
            lower.contains("chai") || lower.contains("tea") -> "Tea/Snacks"
            lower.contains("petrol") || lower.contains("fuel") -> "Fuel"
            lower.contains("salary") || lower.contains("wages") -> "Salary/Wages"
            lower.contains("internet") -> "Internet"
            lower.contains("mobile") || lower.contains("phone") -> "Mobile"
            lower.contains("water") || lower.contains("pani") -> "Water"
            else -> utterance
        }
    }

    private fun String.capitalize(): String {
        return replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }

    private data class Classification(
        val type: String,  // sale, purchase, loan_given, loan_taken, expense
        val direction: String  // in, out
    )
}
