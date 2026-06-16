package com.fameafrica.afm.data.database.dao

import androidx.room.*
import com.fameafrica.afm.data.database.entities.PreseasonScheduleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PreseasonScheduleDao {

    // ============ BASIC CRUD ============

    @Query("SELECT * FROM preseason_schedule ORDER BY match_date ASC")
    fun getAll(): Flow<List<PreseasonScheduleEntity>>

    @Query("SELECT * FROM preseason_schedule WHERE id = :id")
    suspend fun getById(id: Int): PreseasonScheduleEntity?

    @Query("SELECT * FROM preseason_schedule")
    suspend fun getAllStatic(): List<PreseasonScheduleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(match: PreseasonScheduleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(matches: List<PreseasonScheduleEntity>)

    @Update
    suspend fun update(match: PreseasonScheduleEntity)

    @Delete
    suspend fun delete(match: PreseasonScheduleEntity)

    @Query("DELETE FROM preseason_schedule WHERE season = :season")
    suspend fun deleteBySeason(season: String)

    // ============ TEAM-BASED QUERIES ============

    @Query("SELECT * FROM preseason_schedule WHERE team_id = :teamId AND season = :season ORDER BY match_date ASC")
    fun getTeamPreseasonSchedule(teamId: Int, season: String): Flow<List<PreseasonScheduleEntity>>

    @Query("SELECT * FROM preseason_schedule WHERE team_id = :teamId AND season = :season AND status = 'Scheduled' ORDER BY match_date ASC")
    fun getUpcomingPreseasonMatches(teamId: Int, season: String): Flow<List<PreseasonScheduleEntity>>

    @Query("SELECT * FROM preseason_schedule WHERE team_id = :teamId AND season = :season AND status = 'Completed' ORDER BY match_date DESC")
    fun getCompletedPreseasonMatches(teamId: Int, season: String): Flow<List<PreseasonScheduleEntity>>

    // ============ USER TEAM QUERIES ============

    @Query("SELECT * FROM preseason_schedule WHERE is_user_team = 1 AND season = :season ORDER BY match_date ASC")
    fun getUserPreseasonSchedule(season: String): Flow<List<PreseasonScheduleEntity>>

    // ============ OPPONENT QUERIES ============

    @Query("SELECT DISTINCT opponent FROM preseason_schedule WHERE team_id = :teamId AND season = :season")
    fun getPreseasonOpponents(teamId: Int, season: String): Flow<List<String>>

    // ============ SEASON QUERIES ============

    @Query("SELECT DISTINCT season FROM preseason_schedule ORDER BY season DESC")
    fun getSeasons(): Flow<List<String>>

    // ============ STATISTICS QUERIES ============

    @Query("""
        SELECT 
            COUNT(*) as total_matches,
            COUNT(CASE WHEN home_score > opponent_score THEN 1 END) as wins,
            COUNT(CASE WHEN home_score < opponent_score THEN 1 END) as losses,
            COUNT(CASE WHEN home_score = opponent_score THEN 1 END) as draws
        FROM preseason_schedule 
        WHERE team_id = :teamId AND season = :season AND status = 'Completed'
    """)
    suspend fun getPreseasonStats(teamId: Int, season: String): PreseasonStats?
}

// ============ DATA CLASSES ============

data class PreseasonStats(
    @ColumnInfo(name = "total_matches")
    val totalMatches: Int,

    @ColumnInfo(name = "wins")
    val wins: Int,

    @ColumnInfo(name = "losses")
    val losses: Int,

    @ColumnInfo(name = "draws")
    val draws: Int
)
