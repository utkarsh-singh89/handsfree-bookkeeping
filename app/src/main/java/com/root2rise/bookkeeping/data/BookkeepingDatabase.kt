package com.root2rise.bookkeeping.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC, id DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT SUM(amount) FROM transactions WHERE direction = 'in' AND (:dateFilter IS NULL OR date = :dateFilter)")
    suspend fun getTotalSales(dateFilter: String?): Double?

    @Query("SELECT SUM(amount) FROM transactions WHERE direction = 'out' AND type = 'expense' AND (:dateFilter IS NULL OR date = :dateFilter)")
    suspend fun getTotalExpenses(dateFilter: String?): Double?

    @Query("SELECT SUM(CASE WHEN direction = 'in' THEN amount ELSE -amount END) FROM transactions")
    suspend fun getOverallBalance(): Double?

    @Query("SELECT SUM(CASE WHEN direction = 'in' THEN amount ELSE -amount END) FROM transactions WHERE partyName = :partyName")
    suspend fun getBalanceWithParty(partyName: String): Double?
}

@Database(entities = [TransactionEntity::class], version = 1, exportSchema = false)
abstract class BookkeepingDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: BookkeepingDatabase? = null

        fun getDatabase(context: Context): BookkeepingDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BookkeepingDatabase::class.java,
                    "bookkeeping_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
