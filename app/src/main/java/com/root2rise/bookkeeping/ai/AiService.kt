package com.root2rise.bookkeeping.ai

/**
 * Interface for the on-device AI service.
 *
 * This should be implemented using RunAnywhere SDK + Firebender plugin.
 * The model receives natural language utterances (Hinglish) and returns
 * strict JSON conforming to TransactionSchema or QuerySchema.
 *
 * Implementation example with Firebender:
 * ```
 * class FirebenderAiService(context: Context) : AiService {
 *     private val model = RunAnywhereModel.load(context, "bookkeeping_model.tflite")
 *
 *     override suspend fun processUtterance(utterance: String): String {
 *         return model.generate(utterance)
 *     }
 * }
 * ```
 */
interface AiService {
    /**
     * Process a natural language utterance and return JSON.
     *
     * @param utterance User's speech-to-text input (Hinglish)
     * @return JSON string conforming to TransactionSchema or QuerySchema
     */
    suspend fun processUtterance(utterance: String): String
}

/**
 * Mock implementation for testing without actual AI model.
 * Replace this with FirebenderAiService in production.
 */
class MockAiService : AiService {

    override suspend fun processUtterance(utterance: String): String {
        // Simple pattern matching for demo purposes
        // In production, this will be replaced by the actual AI model

        val lowerUtterance = utterance.lowercase().trim()

        // Query patterns
        when {
            // Balance queries
            lowerUtterance.contains("ka kitna") || lowerUtterance.contains("ka balance") -> {
                val partyName = extractPartyName(utterance)
                return """
                    {
                        "kind": "query",
                        "action": "query_balance",
                        "party_name": "$partyName",
                        "time_range": null
                    }
                """.trimIndent()
            }

            // Sales queries
            lowerUtterance.contains("bikri") && (lowerUtterance.contains("kitni") || lowerUtterance.contains(
                "total"
            )) -> {
                val timeRange = extractTimeRange(lowerUtterance)
                return """
                    {
                        "kind": "query",
                        "action": "query_total_sales",
                        "party_name": null,
                        "time_range": "$timeRange"
                    }
                """.trimIndent()
            }

            // Expense queries
            lowerUtterance.contains("kharcha") || lowerUtterance.contains("expense") -> {
                val timeRange = extractTimeRange(lowerUtterance)
                return """
                    {
                        "kind": "query",
                        "action": "query_total_expenses",
                        "party_name": null,
                        "time_range": "$timeRange"
                    }
                """.trimIndent()
            }

            // Summary queries
            lowerUtterance.contains("profit") || lowerUtterance.contains("summary") || lowerUtterance.contains(
                "overall"
            ) -> {
                return """
                    {
                        "kind": "query",
                        "action": "query_overall_summary",
                        "party_name": null,
                        "time_range": "all"
                    }
                """.trimIndent()
            }
        }

        // Transaction patterns
        return when {
            // Expense
            lowerUtterance.contains("bill") ||
            lowerUtterance.contains("kharcha") ||
            lowerUtterance.contains("bhar diya") ||
            ((lowerUtterance.contains("diya") || lowerUtterance.contains("diye")) && !lowerUtterance.contains("udhar")) -> {
                val amount = extractAmount(utterance)
                val notes = utterance // Use the original utterance for a more descriptive note
                """
                    {
                        "kind": "transaction",
                        "action": "add_transaction",
                        "direction": "out",
                        "type": "expense",
                        "party_name": null,
                        "amount": $amount,
                        "date": "today",
                        "notes": "$notes"
                    }
                """.trimIndent()
            }

            // Loan taken
            lowerUtterance.contains("liye") && lowerUtterance.contains("udhar") -> {
                val amount = extractAmount(utterance)
                val partyName = extractPartyName(utterance)
                """
                    {
                        "kind": "transaction",
                        "action": "add_transaction",
                        "direction": "in",
                        "type": "loan_taken",
                        "party_name": "$partyName",
                        "amount": $amount,
                        "date": "today",
                        "notes": "Loan taken $amount from $partyName"
                    }
                """.trimIndent()
            }

            // Loan given
            lowerUtterance.contains("diya") && lowerUtterance.contains("udhar") -> {
                val amount = extractAmount(utterance)
                val partyName = extractPartyName(utterance)
                """
                    {
                        "kind": "transaction",
                        "action": "add_transaction",
                        "direction": "out",
                        "type": "loan_given",
                        "party_name": "$partyName",
                        "amount": $amount,
                        "date": "today",
                        "notes": "Loan given $amount to $partyName"
                    }
                """.trimIndent()
            }

            // Sale
            lowerUtterance.contains("bikri") || lowerUtterance.contains("becha") || lowerUtterance.contains(
                "sale"
            ) -> {
                val amount = extractAmount(utterance)
                """
                    {
                        "kind": "transaction",
                        "action": "add_transaction",
                        "direction": "in",
                        "type": "sale",
                        "party_name": null,
                        "amount": $amount,
                        "date": "today",
                        "notes": "Daily sales $amount"
                    }
                """.trimIndent()
            }

            // Default fallback to income/sale
            else -> {
                val amount = extractAmount(utterance)
                """
                    {
                        "kind": "transaction",
                        "action": "add_transaction",
                        "direction": "in",
                        "type": "sale",
                        "party_name": null,
                        "amount": $amount,
                        "date": "today",
                        "notes": "Transaction: $utterance"
                    }
                """.trimIndent()
            }
        }
    }

    private fun extractAmount(utterance: String): Double {
        // Extract numeric value from utterance
        val regex = Regex("""(\d+(?:\.\d+)?)""")
        val match = regex.find(utterance)
        return match?.value?.toDoubleOrNull() ?: 0.0
    }

    private fun extractPartyName(utterance: String): String {
        // Extract name (word before "se" or "ko")
        val regex = Regex("""(\w+)\s+(?:se|ko|ka)""", RegexOption.IGNORE_CASE)
        val match = regex.find(utterance)
        return match?.groupValues?.get(1)?.capitalize() ?: "Unknown"
    }

    private fun extractTimeRange(utterance: String): String {
        return when {
            utterance.contains("aaj") || utterance.contains("today") -> "today"
            utterance.contains("kal") && !utterance.contains("baad") -> "yesterday"
            utterance.contains("week") || utterance.contains("hafte") -> "this_week"
            utterance.contains("month") || utterance.contains("mahine") -> "this_month"
            utterance.contains("ab tak") || utterance.contains("overall") || utterance.contains("total") -> "all"
            else -> "today"
        }
    }

    private fun String.capitalize(): String {
        return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}
