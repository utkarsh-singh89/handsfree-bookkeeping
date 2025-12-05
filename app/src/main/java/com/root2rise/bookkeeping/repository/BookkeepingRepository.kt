package com.root2rise.bookkeeping.repository

import com.root2rise.bookkeeping.data.BookkeepingDatabase
import com.root2rise.bookkeeping.data.TransactionEntity
import com.root2rise.bookkeeping.model.QuerySchema
import com.root2rise.bookkeeping.model.TransactionSchema
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

class BookkeepingRepository(private val database: BookkeepingDatabase) {

    private val transactionDao = database.transactionDao()

    fun getAllTransactions(): Flow<List<TransactionEntity>> {
        return transactionDao.getAllTransactions()
    }

    suspend fun addTransaction(schema: TransactionSchema): Result<Long> {
        return try {
            val entity = TransactionEntity(
                direction = schema.direction,
                type = schema.type,
                partyName = schema.partyName,
                amount = schema.amount,
                date = normalizeDateString(schema.date),
                notes = schema.notes
            )
            val id = transactionDao.insertTransaction(entity)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateTransaction(transaction: TransactionEntity) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) {
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun executeQuery(schema: QuerySchema): Result<QueryResult> {
        return try {
            val dateFilter = when (schema.timeRange) {
                "today" -> getTodayDateString()
                "yesterday" -> getYesterdayDateString()
                else -> null
            }

            val result = when (schema.action) {
                "query_total_sales" -> {
                    val total = transactionDao.getTotalSales(dateFilter) ?: 0.0
                    QueryResult.Sales(total, schema.timeRange)
                }

                "query_total_expenses" -> {
                    val total = transactionDao.getTotalExpenses(dateFilter) ?: 0.0
                    QueryResult.Expenses(total, schema.timeRange)
                }

                "query_overall_summary" -> {
                    val balance = transactionDao.getOverallBalance() ?: 0.0
                    QueryResult.Summary(balance)
                }

                "query_balance" -> {
                    val partyName = schema.partyName
                    if (partyName.isNullOrBlank()) {
                        return Result.failure(Exception("Party name required for balance query"))
                    }
                    val balance = transactionDao.getBalanceWithParty(partyName) ?: 0.0
                    QueryResult.PartyBalance(partyName, balance)
                }

                else -> QueryResult.Error("Unknown query action: ${schema.action}")
            }

            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun normalizeDateString(date: String?): String {
        return when (date) {
            "today", null -> getTodayDateString()
            "yesterday" -> getYesterdayDateString()
            else -> date // Assume it's already in yyyy-mm-dd format
        }
    }

    private fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun getYesterdayDateString(): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(calendar.time)
    }
}

sealed class QueryResult {
    data class Sales(val total: Double, val timeRange: String?) : QueryResult()
    data class Expenses(val total: Double, val timeRange: String?) : QueryResult()
    data class Summary(val balance: Double) : QueryResult()
    data class PartyBalance(val partyName: String, val balance: Double) : QueryResult()
    data class Error(val message: String) : QueryResult()
}
