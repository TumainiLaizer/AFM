package com.fameafrica.afm.data.database.dao

import androidx.room.*
import com.fameafrica.afm.data.database.entities.ObjectivesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ObjectivesDao {

    // ============ BASIC CRUD ============

    @Query("SELECT * FROM objectives ORDER BY id DESC")
    fun getAll(): Flow<List<ObjectivesEntity>>

    @Query("SELECT * FROM objectives WHERE id = :id")
    suspend fun getById(id: Int): ObjectivesEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(objective: ObjectivesEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(objectives: List<ObjectivesEntity>)

    @Update
    suspend fun update(objective: ObjectivesEntity)

    @Delete
    suspend fun delete(objective: ObjectivesEntity)

    @Query("DELETE FROM objectives WHERE status = 'achieved' OR status = 'failed'")
    suspend fun deleteCompleted()

    // ============ TEAM-BASED QUERIES ============

    @Query("SELECT * FROM objectives WHERE team_id = :teamId ORDER BY id DESC")
    fun getObjectivesByTeam(teamId: Int): Flow<List<ObjectivesEntity>>

    @Query("SELECT * FROM objectives WHERE team_id = :teamId AND status = 'pending' ORDER BY deadline ASC")
    fun getPendingObjectivesByTeam(teamId: Int): Flow<List<ObjectivesEntity>>

    @Query("SELECT * FROM objectives WHERE team_id = :teamId AND status = 'achieved' ORDER BY completion_date DESC")
    fun getAchievedObjectivesByTeam(teamId: Int): Flow<List<ObjectivesEntity>>

    @Query("SELECT * FROM objectives WHERE team_id = :teamId AND status = 'failed' ORDER BY id DESC")
    fun getFailedObjectivesByTeam(teamId: Int): Flow<List<ObjectivesEntity>>

    // ============ SEASON-BASED QUERIES ============

    @Query("SELECT * FROM objectives WHERE season = :season ORDER BY team_name")
    fun getObjectivesBySeason(season: String): Flow<List<ObjectivesEntity>>

    @Query("SELECT * FROM objectives WHERE team_id = :teamId AND season = :season")
    suspend fun getTeamSeasonObjectives(teamId: Int, season: String): List<ObjectivesEntity>

    // ============ TYPE-BASED QUERIES ============

    @Query("SELECT * FROM objectives WHERE objective_type = :objectiveType AND status = 'pending'")
    fun getPendingObjectivesByType(objectiveType: String): Flow<List<ObjectivesEntity>>

    // ============ STATUS QUERIES ============

    @Query("SELECT * FROM objectives WHERE status = 'pending' ORDER BY deadline ASC")
    fun getAllPendingObjectives(): Flow<List<ObjectivesEntity>>

    @Query("SELECT COUNT(*) FROM objectives WHERE team_id = :teamId AND status = 'pending'")
    suspend fun getPendingCountByTeam(teamId: Int): Int

    @Query("SELECT * FROM objectives")
    suspend fun getAllStatic(): List<ObjectivesEntity>

    // ============ STATISTICS QUERIES ============

    @Query("""
        SELECT 
            objective_type,
            COUNT(*) as total,
            COUNT(CASE WHEN status = 'achieved' THEN 1 END) as achieved,
            COUNT(CASE WHEN status = 'failed' THEN 1 END) as failed
        FROM objectives 
        WHERE team_id = :teamId
        GROUP BY objective_type
    """)
    fun getObjectiveStatisticsByTeam(teamId: Int): Flow<List<ObjectiveStatistics>>
}

// ============ DATA CLASSES ============

data class ObjectiveStatistics(
    @ColumnInfo(name = "objective_type")
    val objectiveType: String,

    @ColumnInfo(name = "total")
    val total: Int,

    @ColumnInfo(name = "achieved")
    val achieved: Int,

    @ColumnInfo(name = "failed")
    val failed: Int
)
