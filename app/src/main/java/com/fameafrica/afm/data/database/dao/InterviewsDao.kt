package com.fameafrica.afm.data.database.dao

import androidx.room.*
import com.fameafrica.afm.data.database.entities.InterviewsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InterviewsDao {

    // ============ BASIC CRUD ============

    @Query("SELECT * FROM interviews ORDER BY date_requested DESC")
    fun getAll(): Flow<List<InterviewsEntity>>

    @Query("SELECT * FROM interviews WHERE id = :id")
    suspend fun getById(id: Int): InterviewsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(interview: InterviewsEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(interviews: List<InterviewsEntity>)

    @Update
    suspend fun update(interview: InterviewsEntity)

    @Delete
    suspend fun delete(interview: InterviewsEntity)

    // ============ MANAGER INTERVIEWS ============

    @Query("SELECT * FROM interviews WHERE interviewee_id = :managerId AND interviewee_type = 'MANAGER' ORDER BY date_requested DESC")
    fun getManagerInterviews(managerId: Int): Flow<List<InterviewsEntity>>

    @Query("SELECT * FROM interviews WHERE interviewee_id = :managerId AND interviewee_type = 'MANAGER' AND status = 'Pending' ORDER BY date_requested ASC")
    fun getPendingManagerInterviews(managerId: Int): Flow<List<InterviewsEntity>>

    // ============ PLAYER INTERVIEWS ============

    @Query("SELECT * FROM interviews WHERE player_id = :playerId AND interviewee_type = 'PLAYER' ORDER BY date_requested DESC")
    fun getPlayerInterviews(playerId: Int): Flow<List<InterviewsEntity>>

    @Query("SELECT * FROM interviews WHERE player_id = :playerId AND interviewee_type = 'PLAYER' AND status = 'Pending' ORDER BY date_requested ASC")
    fun getPendingPlayerInterviews(playerId: Int): Flow<List<InterviewsEntity>>

    // ============ STATUS QUERIES ============

    @Query("SELECT * FROM interviews WHERE status = :status ORDER BY date_requested DESC")
    fun getByStatus(status: String): Flow<List<InterviewsEntity>>

    @Query("SELECT * FROM interviews")
    suspend fun getAllStatic(): List<InterviewsEntity>

    // ============ TOPIC QUERIES ============

    @Query("SELECT * FROM interviews WHERE topic LIKE '%' || :searchTerm || '%' ORDER BY date_requested DESC")
    fun searchByTopic(searchTerm: String): Flow<List<InterviewsEntity>>

    // ============ STATISTICS QUERIES ============

    @Query("""
        SELECT 
            interview_type,
            COUNT(*) as count
        FROM interviews 
        WHERE status = 'Completed'
        GROUP BY interview_type
        ORDER BY count DESC
    """)
    fun getInterviewTypeDistribution(): Flow<List<InterviewTypeDistribution>>

    @Query("""
        SELECT 
            interviewee_type,
            COUNT(*) as count
        FROM interviews 
        GROUP BY interviewee_type
    """)
    fun getIntervieweeDistribution(): Flow<List<IntervieweeDistribution>>
}

// ============ DATA CLASSES ============

data class InterviewTypeDistribution(
    @ColumnInfo(name = "interview_type")
    val interviewType: String,

    @ColumnInfo(name = "count")
    val count: Int
)

data class IntervieweeDistribution(
    @ColumnInfo(name = "interviewee_type")
    val intervieweeType: String,

    @ColumnInfo(name = "count")
    val count: Int
)
