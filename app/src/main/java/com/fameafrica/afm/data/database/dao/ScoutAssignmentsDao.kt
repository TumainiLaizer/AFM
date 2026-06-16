package com.fameafrica.afm.data.database.dao

import androidx.room.*
import com.fameafrica.afm.data.database.entities.ScoutAssignmentsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScoutAssignmentsDao {

    // ============ BASIC CRUD ============

    @Query("SELECT * FROM scout_assignments ORDER BY assigned_date DESC")
    fun getAll(): Flow<List<ScoutAssignmentsEntity>>

    @Query("SELECT * FROM scout_assignments WHERE id = :id")
    suspend fun getById(id: Int): ScoutAssignmentsEntity?

    @Query("SELECT * FROM scout_assignments")
    suspend fun getAllStatic(): List<ScoutAssignmentsEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(assignment: ScoutAssignmentsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(assignments: List<ScoutAssignmentsEntity>)

    @Update
    suspend fun update(assignment: ScoutAssignmentsEntity)

    @Delete
    suspend fun delete(assignment: ScoutAssignmentsEntity)

    @Query("DELETE FROM scout_assignments")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM scout_assignments")
    suspend fun getCount(): Int

    // ============ SCOUT-BASED QUERIES ============

    @Query("SELECT * FROM scout_assignments WHERE scout_id = :scoutId ORDER BY assigned_date DESC")
    fun getAssignmentsByScout(scoutId: Int): Flow<List<ScoutAssignmentsEntity>>

    @Query("SELECT * FROM scout_assignments WHERE scout_id = :scoutId AND report_status = 'In Progress' ORDER BY priority DESC, assigned_date ASC")
    fun getActiveAssignmentsByScout(scoutId: Int): Flow<List<ScoutAssignmentsEntity>>

    @Query("SELECT * FROM scout_assignments WHERE scout_id = :scoutId AND report_status = 'Completed' ORDER BY completion_date DESC")
    fun getCompletedAssignmentsByScout(scoutId: Int): Flow<List<ScoutAssignmentsEntity>>

    @Query("SELECT COUNT(*) FROM scout_assignments WHERE scout_id = :scoutId AND report_status = 'In Progress'")
    suspend fun getActiveAssignmentCount(scoutId: Int): Int

    // ============ PLAYER-BASED QUERIES ============

    @Query("SELECT * FROM scout_assignments WHERE player_id = :playerId ORDER BY assigned_date DESC")
    fun getAssignmentsByPlayer(playerId: Int): Flow<List<ScoutAssignmentsEntity>>

    @Query("SELECT * FROM scout_assignments WHERE player_id = :playerId AND report_status = 'Completed' ORDER BY assigned_date DESC LIMIT 1")
    suspend fun getLatestReportForPlayer(playerId: Int): ScoutAssignmentsEntity?

    // ============ STATUS QUERIES ============

    @Query("SELECT * FROM scout_assignments WHERE report_status = :status ORDER BY priority DESC, assigned_date ASC")
    fun getAssignmentsByStatus(status: String): Flow<List<ScoutAssignmentsEntity>>

    @Query("SELECT * FROM scout_assignments WHERE report_status = 'In Progress' AND priority IN ('High', 'Urgent') ORDER BY assigned_date ASC")
    fun getHighPriorityAssignments(): Flow<List<ScoutAssignmentsEntity>>

    // ============ COMPLETION QUERIES ============

    @Query("UPDATE scout_assignments SET report_status = 'Completed', completion_date = :completionDate, scout_report = :report, scout_rating = :rating, estimated_value = :estimatedValue, potential_rating = :potentialRating, strengths = :strengths, weaknesses = :weaknesses, verdict = :verdict WHERE id = :id")
    suspend fun completeAssignment(
        id: Int,
        completionDate: Long,
        report: String,
        rating: Int,
        estimatedValue: Int,
        potentialRating: Int,
        strengths: String?,
        weaknesses: String?,
        verdict: String
    )

    @Query("UPDATE scout_assignments SET report_status = 'Failed', completion_date = :completionDate WHERE id = :id")
    suspend fun failAssignment(id: Int, completionDate: Long)

    // ============ STATISTICS QUERIES ============

    @Query("""
        SELECT 
            scout_id,
            scout_name,
            COUNT(*) as total_assignments,
            COUNT(CASE WHEN report_status = 'Completed' THEN 1 END) as completed,
            COUNT(CASE WHEN report_status = 'Failed' THEN 1 END) as failed,
            AVG(scout_rating) as avg_rating
        FROM scout_assignments 
        WHERE completion_date IS NOT NULL
        GROUP BY scout_id, scout_name
        ORDER BY avg_rating DESC
    """)
    fun getScoutPerformanceStats(): Flow<List<ScoutPerformanceStats>>

    @Query("""
        SELECT 
            priority,
            COUNT(*) as count
        FROM scout_assignments 
        GROUP BY priority
        ORDER BY 
            CASE priority
                WHEN 'Urgent' THEN 1
                WHEN 'High' THEN 2
                WHEN 'Normal' THEN 3
                WHEN 'Low' THEN 4
            END
    """)
    fun getPriorityDistribution(): Flow<List<PriorityDistribution>>
}

// ============ DATA CLASSES ============

data class ScoutPerformanceStats(
    @ColumnInfo(name = "scout_id")
    val scoutId: Int,

    @ColumnInfo(name = "scout_name")
    val scoutName: String,

    @ColumnInfo(name = "total_assignments")
    val totalAssignments: Int,

    @ColumnInfo(name = "completed")
    val completed: Int,

    @ColumnInfo(name = "failed")
    val failed: Int,

    @ColumnInfo(name = "avg_rating")
    val averageRating: Double?
)

data class PriorityDistribution(
    @ColumnInfo(name = "priority")
    val priority: String,

    @ColumnInfo(name = "count")
    val count: Int
)