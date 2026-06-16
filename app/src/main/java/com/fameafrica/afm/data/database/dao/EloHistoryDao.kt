package com.fameafrica.afm.data.database.dao

import androidx.room.*
import com.fameafrica.afm.data.database.entities.EloHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EloHistoryDao {

    // ============ BASIC CRUD ============

    @Query("SELECT * FROM elo_history ORDER BY current_elo DESC")
    fun getAll(): Flow<List<EloHistoryEntity>>

    @Query("SELECT * FROM elo_history WHERE team_id = :teamId")
    suspend fun getByTeamId(teamId: Int): EloHistoryEntity?

    @Query("SELECT * FROM elo_history WHERE team_name = :teamName")
    suspend fun getByTeamName(teamName: String): EloHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(eloHistory: EloHistoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(eloHistories: List<EloHistoryEntity>)

    @Update
    suspend fun update(eloHistory: EloHistoryEntity)

    @Delete
    suspend fun delete(eloHistory: EloHistoryEntity)

    @Query("DELETE FROM elo_history")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM elo_history")
    suspend fun getCount(): Int

    @Query("SELECT * FROM elo_history")
    suspend fun getAllStatic(): List<EloHistoryEntity>

    // ============ ELO RANKING QUERIES ============

    @Query("SELECT * FROM elo_history ORDER BY current_elo DESC LIMIT :limit")
    fun getTopEloTeams(limit: Int): Flow<List<EloHistoryEntity>>

    @Query("SELECT * FROM elo_history ORDER BY current_elo ASC LIMIT :limit")
    fun getBottomEloTeams(limit: Int): Flow<List<EloHistoryEntity>>

    @Query("SELECT team_id, team_name, current_elo FROM elo_history ORDER BY current_elo DESC")
    fun getEloRankings(): Flow<List<EloRanking>>

    @Query("SELECT AVG(current_elo) FROM elo_history")
    suspend fun getAverageElo(): Double?

    @Query("SELECT MAX(current_elo) FROM elo_history")
    suspend fun getHighestElo(): Int?

    @Query("SELECT MIN(current_elo) FROM elo_history")
    suspend fun getLowestElo(): Int?

    // ============ ELO CHANGE QUERIES ============

    @Query("SELECT * FROM elo_history ORDER BY (current_elo - previous_elo) DESC LIMIT :limit")
    fun getBiggestEloGainers(limit: Int): Flow<List<EloHistoryEntity>>

    @Query("SELECT * FROM elo_history ORDER BY (current_elo - previous_elo) ASC LIMIT :limit")
    fun getBiggestEloLosers(limit: Int): Flow<List<EloHistoryEntity>>

    @Query("SELECT * FROM elo_history WHERE (current_elo - previous_elo) > :threshold ORDER BY (current_elo - previous_elo) DESC")
    fun getTeamsWithSignificantGain(threshold: Int): Flow<List<EloHistoryEntity>>

    @Query("SELECT * FROM elo_history WHERE (current_elo - previous_elo) < :threshold ORDER BY (current_elo - previous_elo) ASC")
    fun getTeamsWithSignificantLoss(threshold: Int): Flow<List<EloHistoryEntity>>

    // ============ TEAM PERFORMANCE QUERIES ============

    @Query("SELECT * FROM elo_history WHERE current_elo >= :minElo AND current_elo <= :maxElo")
    fun getTeamsInEloRange(minElo: Int, maxElo: Int): Flow<List<EloHistoryEntity>>

    @Query("SELECT COUNT(*) FROM elo_history WHERE current_elo >= :threshold")
    suspend fun getTeamsAboveElo(threshold: Int): Int

    @Query("SELECT COUNT(*) FROM elo_history WHERE current_elo <= :threshold")
    suspend fun getTeamsBelowElo(threshold: Int): Int

    // ============ UPDATE ELO ============

    @Query("""
        UPDATE elo_history 
        SET previous_elo = current_elo,
            current_elo = :newElo,
            matches_played_elo = matches_played_elo + 1,
            elo_change_total = elo_change_total + (:newElo - current_elo),
            last_updated = :lastUpdated,
            last_match_result = :lastResult,
            last_opponent = :opponent,
            last_opponent_elo = :opponentElo
        WHERE team_id = :teamId
    """)
    suspend fun updateElo(
        teamId: Int,
        newElo: Int,
        lastUpdated: String,
        lastResult: String,
        opponent: String,
        opponentElo: Int
    )

    @Query("UPDATE elo_history SET highest_elo = :highestElo WHERE team_id = :teamId")
    suspend fun updateHighestElo(teamId: Int, highestElo: Int)

    @Query("UPDATE elo_history SET lowest_elo = :lowestElo WHERE team_id = :teamId")
    suspend fun updateLowestElo(teamId: Int, lowestElo: Int)
}

// ============ DATA CLASSES ============

data class EloRanking(
    @ColumnInfo(name = "team_id")
    val teamId: Int,

    @ColumnInfo(name = "team_name")
    val teamName: String,

    @ColumnInfo(name = "current_elo")
    val currentElo: Int
)
