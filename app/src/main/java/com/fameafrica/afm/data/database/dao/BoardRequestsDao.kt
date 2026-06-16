package com.fameafrica.afm.data.database.dao

import androidx.room.*
import com.fameafrica.afm.data.database.entities.BoardRequestsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BoardRequestsDao {

    // ============ BASIC CRUD ============

    @Query("SELECT * FROM board_requests ORDER BY id DESC")
    fun getAll(): Flow<List<BoardRequestsEntity>>

    @Query("SELECT * FROM board_requests WHERE id = :id")
    suspend fun getById(id: Int): BoardRequestsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(request: BoardRequestsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(requests: List<BoardRequestsEntity>)

    @Update
    suspend fun update(request: BoardRequestsEntity)

    @Delete
    suspend fun delete(request: BoardRequestsEntity)

    @Query("DELETE FROM board_requests WHERE requestStatus IN ('Approved', 'Rejected', 'Completed')")
    suspend fun deleteResolved()

    @Query("DELETE FROM board_requests")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM board_requests")
    suspend fun getCount(): Int

    @Query("SELECT * FROM board_requests")
    suspend fun getAllStatic(): List<BoardRequestsEntity>

    // ============ MANAGER-BASED QUERIES ============

    @Query("SELECT * FROM board_requests WHERE managerName = :managerName ORDER BY id DESC")
    fun getRequestsByManager(managerName: String): Flow<List<BoardRequestsEntity>>

    @Query("SELECT * FROM board_requests WHERE managerName = :managerName AND requestStatus = 'Pending' ORDER BY id DESC")
    fun getPendingRequestsByManager(managerName: String): Flow<List<BoardRequestsEntity>>

    @Query("SELECT * FROM board_requests WHERE managerName = :managerName AND requestStatus = 'Approved' ORDER BY id DESC")
    fun getApprovedRequestsByManager(managerName: String): Flow<List<BoardRequestsEntity>>

    @Query("SELECT * FROM board_requests WHERE managerName = :managerName AND requestStatus = 'Rejected' ORDER BY id DESC")
    fun getRejectedRequestsByManager(managerName: String): Flow<List<BoardRequestsEntity>>

    // ============ TEAM-BASED QUERIES ============

    @Query("SELECT * FROM board_requests WHERE team_id = :teamId ORDER BY id DESC")
    fun getRequestsByTeam(teamId: Int): Flow<List<BoardRequestsEntity>>

    @Query("SELECT * FROM board_requests WHERE team_id = :teamId AND requestStatus = 'Pending' ORDER BY id DESC")
    fun getPendingRequestsByTeam(teamId: Int): Flow<List<BoardRequestsEntity>>

    // ============ TYPE-BASED QUERIES ============

    @Query("SELECT * FROM board_requests WHERE requestType = :requestType ORDER BY id DESC")
    fun getRequestsByType(requestType: String): Flow<List<BoardRequestsEntity>>

    @Query("SELECT * FROM board_requests WHERE requestType = :requestType AND requestStatus = 'Pending' ORDER BY id DESC")
    fun getPendingRequestsByType(requestType: String): Flow<List<BoardRequestsEntity>>

    // ============ STATUS-BASED QUERIES ============

    @Query("SELECT * FROM board_requests WHERE requestStatus = :status ORDER BY id DESC")
    fun getRequestsByStatus(status: String): Flow<List<BoardRequestsEntity>>

    @Query("SELECT COUNT(*) FROM board_requests WHERE requestStatus = 'Pending'")
    suspend fun getPendingCount(): Int

    // ============ STATISTICS QUERIES ============

    @Query("""
        SELECT 
            requestType,
            COUNT(*) as request_count,
            COUNT(CASE WHEN requestStatus = 'Approved' THEN 1 END) as approved_count,
            COUNT(CASE WHEN requestStatus = 'Rejected' THEN 1 END) as rejected_count
        FROM board_requests 
        GROUP BY requestType
        ORDER BY request_count DESC
    """)
    fun getRequestTypeStatistics(): Flow<List<RequestTypeStatistics>>

    @Query("""
        SELECT 
            managerName,
            COUNT(*) as request_count,
            COUNT(CASE WHEN requestStatus = 'Approved' THEN 1 END) as approved_count
        FROM board_requests 
        GROUP BY managerName
        ORDER BY request_count DESC
        LIMIT :limit
    """)
    fun getMostRequestingManagers(limit: Int): Flow<List<ManagerRequestStatistics>>
}

// ============ DATA CLASSES ============

data class RequestTypeStatistics(
    @ColumnInfo(name = "requestType")
    val requestType: String,

    @ColumnInfo(name = "request_count")
    val requestCount: Int,

    @ColumnInfo(name = "approved_count")
    val approvedCount: Int,

    @ColumnInfo(name = "rejected_count")
    val rejectedCount: Int
)

data class ManagerRequestStatistics(
    @ColumnInfo(name = "managerName")
    val managerName: String,

    @ColumnInfo(name = "request_count")
    val requestCount: Int,

    @ColumnInfo(name = "approved_count")
    val approvedCount: Int
)
