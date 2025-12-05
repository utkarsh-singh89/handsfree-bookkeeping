package com.root2rise.bookkeeping.ai

import java.util.Locale
import kotlin.math.max

/**
 * Interface for the on-device AI service that processes commands
 * for existing transactions.
 */
interface TransactionAiService {
    /**
     * Processes a natural language command for an existing transaction
     * and returns a JSON object with the detected intent.
     *
     * @param utterance User's speech-to-text input (Hinglish/English).
     * @return JSON string conforming to the specified schemas (modify, delete, read).
     */
    suspend fun processTransactionUpdate(utterance: String): String
}

/**
 * Mock implementation of TransactionAiService for testing.
 * This uses simple pattern matching to simulate the AI model's behavior.
 */
class MockTransactionAiService : TransactionAiService {

    override suspend fun processTransactionUpdate(utterance: String): String {
        val lowerUtterance = utterance.lowercase().trim()

        // 1. Check for Delete Intent
        if (lowerUtterance.contains("delete") || lowerUtterance.contains("hata do") || lowerUtterance.contains("mita do") || lowerUtterance.contains("remove this entry")) {
            return """
                {
                  "intent": "delete_transaction"
                }
            """.trimIndent()
        }

        // 2. Check for Read Aloud Intent
        if (lowerUtterance.contains("read aloud") || lowerUtterance.contains("sunao") || lowerUtterance.contains("bolkar batao") || lowerUtterance.contains("suna do") || lowerUtterance.contains("hear this")) {
            return """
                {
                  "intent": "read_aloud_transaction"
                }
            """.trimIndent()
        }

        // 3. Check for Modify Intent
        val amount = extractAmount(lowerUtterance)
        val notes = extractNotes(utterance, lowerUtterance)

        if (amount != null || notes != null) {
            val amountJson = amount?.toString() ?: "null"
            val notesJson = if (notes != null) "\"$notes\"" else "null"

            return """
                {
                  "intent": "modify_transaction",
                  "fields": {
                    "amount": $amountJson,
                    "notes": $notesJson
                  }
                }
            """.trimIndent()
        }

        // Check for general modification phrases if specific fields aren't found yet
        val isGeneralModify = lowerUtterance.contains("badal do") ||
                lowerUtterance.contains("kar do") ||
                lowerUtterance.contains("galat hai") ||
                lowerUtterance.contains("change karo") ||
                lowerUtterance.contains("jod do") ||
                lowerUtterance.contains("kam hai")

        if(isGeneralModify) {
             return """
                {
                  "intent": "modify_transaction",
                  "fields": {
                    "amount": null,
                    "notes": "unclear modification"
                  }
                }
            """.trimIndent()
        }

        // 4. Failure Case (Default)
        return """
            {
              "intent": "modify_transaction",
              "fields": {
                "amount": null,
                "notes": "unclear modification"
              }
            }
        """.trimIndent()
    }

    private fun extractAmount(utterance: String): Double? {
        val regex = Regex("""(\d+(?:\.\d+)?)""")
        val match = regex.find(utterance)
        return match?.value?.toDoubleOrNull()
    }

    private fun extractNotes(originalUtterance: String, lowerUtterance: String): String? {
        // Pattern 1: Keywords for explicit note setting (e.g., "likho ...", "description ...")
        val keywordPatterns = listOf(
            Regex("""likho\s+(?:ki\s+)?(.+)""", RegexOption.IGNORE_CASE),
            Regex("""(?:description|notes)\s+(?:change karo|badal do)?\s*[,]?\s*(.+)""", RegexOption.IGNORE_CASE),
            Regex("""(.+)\s+likho$""", RegexOption.IGNORE_CASE)
        )

        for (pattern in keywordPatterns) {
            val match = pattern.find(originalUtterance)
            if (match != null) {
                return match.groupValues[1].trim()
            }
        }

        // Pattern 2: Check if it's a simple phrase just for changing the amount.
        // If so, we don't want to create a note.
        val simpleAmountPhrase = Regex("""^\s*(?:amount|is|iska)?\s*\d+(?:\.\d+)?\s*(?:kar do|rupey|rs)?\s*$""", RegexOption.IGNORE_CASE)
        if (simpleAmountPhrase.matches(lowerUtterance)) {
            return null // It's just an amount change, no new note.
        }

        // Pattern 3: If it's not a simple amount change and it has a number,
        // the user is likely describing the whole transaction. Use the full utterance as the note.
        if (Regex("""\d""").containsMatchIn(lowerUtterance)) {
            return originalUtterance
        }

        // Pattern 4: If no amount is present, the whole phrase is the note.
        if (!Regex("""\d""").containsMatchIn(lowerUtterance)) {
            val isModifyCommand = lowerUtterance.contains("badal do") || lowerUtterance.contains("change karo")
            if(isModifyCommand) return originalUtterance
        }

        return null
    }
}
