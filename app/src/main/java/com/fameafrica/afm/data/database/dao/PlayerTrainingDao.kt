package com.fameafrica.afm.data.database.dao

import androidx.room.*
import com.fameafrica.afm.data.database.entities.PlayerTrainingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerTrainingDao {

    // ============ BASIC CRUD ============

    @Query("SELECT * FROM player_training ORDER BY start_date DESC")
    fun getAll(): Flow<List<PlayerTrainingEntity>>

    @Query("SELECT * FROM player_training WHERE id = :id")
    suspend fun getById(id: Int): PlayerTrainingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(training: PlayerTrainingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(training: List<PlayerTrainingEntity>)

    @Update
    suspend fun update(training: PlayerTrainingEntity)

    @Delete
    suspend fun delete(training: PlayerTrainingEntity)

    @Query("DELETE FROM player_training")
    suspend fun deleteAll()

    // ============ PLAYER-BASED QUERIES ============

    @Query("SELECT * FROM player_training WHERE player_name = :playerName ORDER BY start_date DESC")
    fun getTrainingByPlayer(playerName: String): Flow<List<PlayerTrainingEntity>>

    @Query("SELECT * FROM player_training WHERE player_name = :playerName AND status = 'ACTIVE'")
    suspend fun getActiveTrainingForPlayer(playerName: String): PlayerTrainingEntity?

    @Query("SELECT * FROM player_training WHERE player_name = :playerName AND drill_type = :drillType ORDER BY start_date DESC LIMIT 5")
    fun getPlayerTrainingHistory(playerName: String, drillType: String): Flow<List<PlayerTrainingEntity>>

    // ============ TEAM-BASED QUERIES ============

    @Query("SELECT * FROM player_training WHERE player_id IN (SELECT id FROM players WHERE team_id = :teamId) AND status = 'ACTIVE'")
    fun getTeamActiveTraining(teamId: Int): Flow<List<PlayerTrainingEntity>>

    @Query("""
        SELECT 
            drill_type,
            COUNT(*) as training_count,
            AVG(progress) as avg_progress
        FROM player_training 
        WHERE player_id IN (SELECT id FROM players WHERE team_id = :teamId)
        GROUP BY drill_type
    """)
    fun getTeamTrainingStats(teamId: Int): Flow<List<TeamTrainingStats>>

    // ============ STATUS QUERIES ============

    @Query("SELECT * FROM player_training WHERE status = 'ACTIVE' ORDER BY start_date")
    fun getActiveTraining(): Flow<List<PlayerTrainingEntity>>

    @Query("SELECT * FROM player_training WHERE status = 'ACTIVE' AND injury_risk >= 70 ORDER BY injury_risk DESC")
    fun getHighRiskTraining(): Flow<List<PlayerTrainingEntity>>

    @Query("SELECT * FROM player_training WHERE status = 'ACTIVE' AND end_date < date('now')")
    fun getOverdueTraining(): Flow<List<PlayerTrainingEntity>>

    @Query("SELECT * FROM player_training ORDER BY end_date DESC LIMIT :limit")
    fun getRecentCompletedTraining(limit: Int): Flow<List<PlayerTrainingEntity>>

    @Query("SELECT * FROM player_training")
    suspend fun getAllStatic(): List<PlayerTrainingEntity>

    // ============ STATISTICS QUERIES ============

    @Query("""
        SELECT 
            drill_type,
            COUNT(*) as total_sessions,
            AVG(improvement_amount) as avg_improvement,
            AVG(injury_risk) as avg_injury_risk
        FROM player_training 
        WHERE status = 'COMPLETED'
        GROUP BY drill_type
        ORDER BY avg_improvement DESC
    """)
    fun getTrainingEffectiveness(): Flow<List<TrainingEffectiveness>>
}

// ============ DATA CLASSES ============

data class TeamTrainingStats(
    @ColumnInfo(name = "drill_type")
    val drillType: String,

    @ColumnInfo(name = "training_count")
    val trainingCount: Int,

    @ColumnInfo(name = "avg_progress")
    val averageProgress: Double
)

data class TrainingEffectiveness(
    @ColumnInfo(name = "drill_type")
    val drillType: String,

    @ColumnInfo(name = "total_sessions")
    val totalSessions: Int,

    @ColumnInfo(name = "avg_improvement")
    val averageImprovement: Double,

    @ColumnInfo(name = "avg_injury_risk")
    val averageInjuryRisk: Double
)