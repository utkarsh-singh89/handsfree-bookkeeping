package com.root2rise.bookkeeping.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val direction: String, // "in" or "out"
    val type: String, // "sale", "purchase", "loan_given", "loan_taken", "expense", "other"
    val partyName: String?,
    val amount: Double,
    val date: String, // ISO format yyyy-mm-dd or "today"/"yesterday"
    val notes: String?,
    val timestamp: Long = System.currentTimeMillis()
)
