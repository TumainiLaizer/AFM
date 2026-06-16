package com.fameafrica.afm.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fameafrica.afm.data.database.entities.PurchaseHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PurchaseHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(purchase: PurchaseHistoryEntity): Long

    @Query("SELECT * FROM purchase_history ORDER BY timestamp DESC")
    fun getAll(): Flow<List<PurchaseHistoryEntity>>

    @Query("SELECT * FROM purchase_history WHERE transaction_id = :transactionId")
    suspend fun getByTransactionId(transactionId: String): PurchaseHistoryEntity?

    @Query("UPDATE purchase_history SET verification_status = :status WHERE transaction_id = :transactionId")
    suspend fun updateStatus(transactionId: String, status: String)
}
