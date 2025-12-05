package com.root2rise.bookkeeping.model

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

/**
 * Base interface for all AI response schemas
 */
sealed interface AiResponse {
    val kind: String
}

/**
 * Transaction schema for when user describes a business transaction
 */
data class TransactionSchema(
    @SerializedName("kind")
    override val kind: String = "transaction",

    @SerializedName("action")
    val action: String = "add_transaction",

    @SerializedName("direction")
    val direction: String, // "in" or "out"

    @SerializedName("type")
    val type: String, // "sale", "purchase", "loan_given", "loan_taken", "expense", "other"

    @SerializedName("party_name")
    val partyName: String?,

    @SerializedName("amount")
    val amount: Double,

    @SerializedName("date")
    val date: String?, // "today", "yesterday", "yyyy-mm-dd", or null

    @SerializedName("notes")
    val notes: String?
) : AiResponse

/**
 * Query schema for when user asks about their records
 */
data class QuerySchema(
    @SerializedName("kind")
    override val kind: String = "query",

    @SerializedName("action")
    val action: String, // "query_total_sales", "query_total_expenses", "query_overall_summary", "query_balance"

    @SerializedName("party_name")
    val partyName: String?,

    @SerializedName("time_range")
    val timeRange: String? // "today", "yesterday", "this_week", "this_month", "all", null
) : AiResponse

/**
 * Utility to parse AI JSON response
 */
object AiResponseParser {
    private val gson = Gson()

    fun parse(jsonString: String): AiResponse? {
        return try {
            // First, parse to determine the kind
            val baseResponse = gson.fromJson(jsonString, Map::class.java)
            val kind = baseResponse["kind"] as? String

            when (kind) {
                "transaction" -> gson.fromJson(jsonString, TransactionSchema::class.java)
                "query" -> gson.fromJson(jsonString, QuerySchema::class.java)
                else -> null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
