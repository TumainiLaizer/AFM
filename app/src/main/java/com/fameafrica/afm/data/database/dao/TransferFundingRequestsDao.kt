package com.fameafrica.afm.data.database.dao

import androidx.room.*
import com.fameafrica.afm.data.database.entities.TransferFundingRequestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransferFundingRequestsDao {

    @Query("SELECT * FROM transfer_funding_requests ORDER BY request_date DESC")
    fun getAll(): Flow<List<TransferFundingRequestEntity>>

    @Query("SELECT * FROM transfer_funding_requests WHERE id = :id")
    suspend fun getById(id: Int): TransferFundingRequestEntity?

    @Query("SELECT * FROM transfer_funding_requests WHERE team_id = :teamId ORDER BY request_date DESC")
    fun getRequestsByTeam(teamId: Int): Flow<List<TransferFundingRequestEntity>>

    @Query("SELECT * FROM transfer_funding_requests WHERE sponsor_id = :sponsorId ORDER BY request_date DESC")
    fun getRequestsBySponsor(sponsorId: Int): Flow<List<TransferFundingRequestEntity>>

    @Query("SELECT * FROM transfer_funding_requests WHERE status = 'PENDING' ORDER BY request_date")
    fun getPendingRequests(): Flow<List<TransferFundingRequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(request: TransferFundingRequestEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(requests: List<TransferFundingRequestEntity>)

    @Query("SELECT * FROM transfer_funding_requests")
    suspend fun getAllStatic(): List<TransferFundingRequestEntity>

    @Update
    suspend fun update(request: TransferFundingRequestEntity)

    @Delete
    suspend fun delete(request: TransferFundingRequestEntity)

    @Query("UPDATE transfer_funding_requests SET status = :status, decision_date = :decisionDate, approved_amount = :approvedAmount WHERE id = :id")
    suspend fun updateRequestStatus(id: Int, status: String, decisionDate: String, approvedAmount: Long?)
}