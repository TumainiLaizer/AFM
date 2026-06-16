package com.fameafrica.afm.data.repository

import com.fameafrica.afm.data.database.dao.PurchaseHistoryDao
import com.fameafrica.afm.data.database.entities.PurchaseHistoryEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class PurchaseHistoryRepository @Inject constructor(
    private val purchaseHistoryDaoProvider: Provider<PurchaseHistoryDao>
) {
    private val purchaseHistoryDao get() = purchaseHistoryDaoProvider.get()

    fun getAllPurchases(): Flow<List<PurchaseHistoryEntity>> = purchaseHistoryDao.getAll()

    suspend fun insertPurchase(purchase: PurchaseHistoryEntity): Long = 
        purchaseHistoryDao.insert(purchase)

    suspend fun updateStatus(transactionId: String, status: String) = 
        purchaseHistoryDao.updateStatus(transactionId, status)
}
