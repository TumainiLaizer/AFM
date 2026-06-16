package com.fameafrica.afm.data.database.dao

import androidx.room.*
import com.fameafrica.afm.data.database.entities.TransfersEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransfersDao {

    // ============ BASIC CRUD ============

    @Query("SELECT * FROM transfers ORDER BY timestamp DESC")
    fun getAll(): Flow<List<TransfersEntity>>

    @Query("SELECT * FROM transfers WHERE id = :id")
    suspend fun getById(id: Int): TransfersEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transfer: TransfersEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transfers: List<TransfersEntity>)

    @Update
    suspend fun update(transfer: TransfersEntity)

    @Delete
    suspend fun delete(transfer: TransfersEntity)

    @Query("DELETE FROM transfers")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM transfers")
    suspend fun getCount(): Int

    @Query("SELECT * FROM transfers")
    suspend fun getAllStatic(): List<TransfersEntity>

    // ============ PLAYER-BASED QUERIES ============

    @Query("SELECT * FROM transfers WHERE player_id = :playerId ORDER BY timestamp DESC")
    fun getTransfersByPlayer(playerId: Int): Flow<List<TransfersEntity>>

    @Query("SELECT * FROM transfers WHERE player_name LIKE '%' || :playerName || '%' ORDER BY timestamp DESC")
    fun searchTransfersByPlayer(playerName: String): Flow<List<TransfersEntity>>

    // ============ TEAM-BASED QUERIES ============

    @Query("SELECT * FROM transfers WHERE current_team_id = :teamId ORDER BY timestamp DESC")
    fun getOutgoingTransfers(teamId: Int): Flow<List<TransfersEntity>>

    @Query("SELECT * FROM transfers WHERE target_team_id = :teamId ORDER BY timestamp DESC")
    fun getIncomingTransfers(teamId: Int): Flow<List<TransfersEntity>>

    @Query("SELECT * FROM transfers WHERE current_team_id = :teamId OR target_team_id = :teamId ORDER BY timestamp DESC")
    fun getAllTransfersByTeam(teamId: Int): Flow<List<TransfersEntity>>

    @Query("SELECT * FROM transfers WHERE target_team_id = :teamId AND transfer_status = 'Pending' ORDER BY timestamp ASC")
    fun getPendingIncomingTransfers(teamId: Int): Flow<List<TransfersEntity>>

    // ============ STATUS QUERIES ============

    @Query("SELECT * FROM transfers WHERE transfer_status = :status ORDER BY timestamp DESC")
    fun getTransfersByStatus(status: String): Flow<List<TransfersEntity>>

    @Query("SELECT * FROM transfers WHERE transfer_status = 'Pending' ORDER BY timestamp ASC")
    fun getPendingTransfers(): Flow<List<TransfersEntity>>

    @Query("SELECT * FROM transfers WHERE transfer_status = 'Completed' ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentCompletedTransfers(limit: Int): Flow<List<TransfersEntity>>

    // ============ TYPE QUERIES ============

    @Query("SELECT * FROM transfers WHERE transfer_type = :type ORDER BY timestamp DESC")
    fun getTransfersByType(type: String): Flow<List<TransfersEntity>>

    @Query("SELECT * FROM transfers WHERE transfer_type = 'Loan' AND is_loan_to_buy = 1 ORDER BY timestamp DESC")
    fun getLoanToBuyTransfers(): Flow<List<TransfersEntity>>

    // ============ WINDOW QUERIES ============

    @Query("SELECT * FROM transfers WHERE window_id = :windowId ORDER BY timestamp DESC")
    fun getTransfersByWindow(windowId: Int): Flow<List<TransfersEntity>>

    @Query("SELECT * FROM transfers WHERE window_id = :windowId AND transfer_status = 'Completed' ORDER BY transfer_fee DESC")
    fun getCompletedTransfersByWindow(windowId: Int): Flow<List<TransfersEntity>>

    // ============ SCOUT RATING QUERIES ============

    @Query("SELECT * FROM transfers WHERE scout_rating >= :minRating ORDER BY scout_rating DESC")
    fun getHighRatedTransfers(minRating: Int): Flow<List<TransfersEntity>>

    // ============ FEE QUERIES ============

    @Query("SELECT * FROM transfers WHERE transfer_fee >= :minFee ORDER BY transfer_fee DESC")
    fun getHighValueTransfers(minFee: Int): Flow<List<TransfersEntity>>

    @Query("SELECT * FROM transfers WHERE transfer_fee BETWEEN :minFee AND :maxFee ORDER BY transfer_fee DESC")
    fun getTransfersByFeeRange(minFee: Int, maxFee: Int): Flow<List<TransfersEntity>>

    @Query("SELECT SUM(transfer_fee) FROM transfers WHERE target_team_id = :teamId AND transfer_status = 'Completed' AND transfer_type = 'Buy'")
    suspend fun getTotalSpentByTeam(teamId: Int): Long?

    @Query("SELECT SUM(transfer_fee) FROM transfers WHERE current_team_id = :teamId AND transfer_status = 'Completed' AND transfer_type = 'Buy'")
    suspend fun getTotalReceivedByTeam(teamId: Int): Long?

    // ============ RUMOUR QUERIES ============

    @Query("SELECT * FROM transfers WHERE rumours IS NOT NULL AND transfer_status != 'Completed' ORDER BY timestamp DESC")
    fun getActiveRumours(): Flow<List<TransfersEntity>>

    @Query("SELECT * FROM transfers WHERE rumours IS NOT NULL AND (target_team_id = :teamId OR current_team_id = :teamId) AND transfer_status != 'Completed' ORDER BY timestamp DESC")
    fun getTeamRumours(teamId: Int): Flow<List<TransfersEntity>>

    // ============ TIMESTAMP QUERIES ============

    @Query("SELECT * FROM transfers WHERE timestamp >= :startTime ORDER BY timestamp DESC")
    fun getTransfersSince(startTime: Long): Flow<List<TransfersEntity>>

    // ============ STATISTICS QUERIES ============

    @Query("""
        SELECT 
            transfer_type,
            COUNT(*) as count,
            AVG(transfer_fee) as avg_fee,
            SUM(transfer_fee) as total_fee
        FROM transfers 
        WHERE transfer_status = 'Completed'
        GROUP BY transfer_type
    """)
    fun getTransferTypeStatistics(): Flow<List<TransferTypeStats>>

    @Query("""
        SELECT 
            target_team_id as team_id,
            target_team as team_name,
            COUNT(*) as purchases,
            SUM(transfer_fee) as total_spent
        FROM transfers 
        WHERE transfer_status = 'Completed' AND transfer_type = 'Buy'
        GROUP BY target_team_id
        ORDER BY total_spent DESC
        LIMIT :limit
    """)
    fun getBiggestSpenders(limit: Int): Flow<List<TeamTransferStats>>

    @Query("""
        SELECT 
            current_team_id as team_id,
            current_team as team_name,
            COUNT(*) as sales,
            SUM(transfer_fee) as total_received
        FROM transfers 
        WHERE transfer_status = 'Completed' AND transfer_type = 'Buy'
        GROUP BY current_team_id
        ORDER BY total_received DESC
        LIMIT :limit
    """)
    fun getBiggestSellers(limit: Int): Flow<List<TeamSalesStats>>
}

// ============ DATA CLASSES ============

data class TransferTypeStats(
    @ColumnInfo(name = "transfer_type")
    val transferType: String,

    @ColumnInfo(name = "count")
    val count: Int,

    @ColumnInfo(name = "avg_fee")
    val averageFee: Double?,

    @ColumnInfo(name = "total_fee")
    val totalFee: Long?
)

data class TeamTransferStats(
    @ColumnInfo(name = "team_id")
    val teamId: Int,

    @ColumnInfo(name = "team_name")
    val teamName: String,

    @ColumnInfo(name = "purchases")
    val purchases: Int,

    @ColumnInfo(name = "total_spent")
    val totalSpent: Long?
)

data class TeamSalesStats(
    @ColumnInfo(name = "team_id")
    val teamId: Int,

    @ColumnInfo(name = "team_name")
    val teamName: String,

    @ColumnInfo(name = "sales")
    val sales: Int,

    @ColumnInfo(name = "total_received")
    val totalReceived: Long?
)