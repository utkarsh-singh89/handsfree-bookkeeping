package com.root2rise.bookkeeping.model

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

// --- Data classes for parsing JSON from TransactionAiService ---

data class TransactionUpdateResponse(
    val intent: String,
    val fields: TransactionUpdateFields?
)

data class TransactionUpdateFields(
    val amount: Double?,
    val notes: String?
)

// --- Parser for Transaction AI responses ---

object TransactionAiResponseParser {
    private val gson = Gson()

    fun parse(json: String): TransactionUpdateResponse? {
        return try {
            gson.fromJson(json, TransactionUpdateResponse::class.java)
        } catch (e: JsonSyntaxException) {
            null
        }
    }
}
