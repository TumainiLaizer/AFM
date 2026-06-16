package com.fameafrica.afm.data.database.dao

import androidx.room.*
import com.fameafrica.afm.data.database.entities.PrizesLeaguesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PrizesLeaguesDao {

    // ============ BASIC CRUD ============

    @Query("SELECT * FROM prizes_leagues ORDER BY competition_id, position")
    fun getAll(): Flow<List<PrizesLeaguesEntity>>

    @Query("SELECT * FROM prizes_leagues WHERE id = :id")
    suspend fun getById(id: Int): PrizesLeaguesEntity?

    @Query("SELECT * FROM prizes_leagues WHERE competition_id = :competitionId AND position = :position")
    suspend fun getPrizeByCompetitionAndPosition(competitionId: Int, position: Int): PrizesLeaguesEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(prize: PrizesLeaguesEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(prizes: List<PrizesLeaguesEntity>)

    @Update
    suspend fun update(prize: PrizesLeaguesEntity)

    @Delete
    suspend fun delete(prize: PrizesLeaguesEntity)

    @Query("DELETE FROM prizes_leagues WHERE competition_id = :competitionId")
    suspend fun deleteByCompetition(competitionId: Int)

    @Query("DELETE FROM prizes_leagues")
    suspend fun deleteAll()

    // ============ COMPETITION-BASED QUERIES ============

    @Query("SELECT * FROM prizes_leagues WHERE competition_id = :competitionId ORDER BY position")
    fun getPrizesByCompetition(competitionId: Int): Flow<List<PrizesLeaguesEntity>>

    @Query("SELECT prize_money FROM prizes_leagues WHERE competition_id = :competitionId AND position = 1")
    suspend fun getWinnerPrize(competitionId: Int): Int?

    @Query("SELECT prize_money FROM prizes_leagues WHERE competition_id = :competitionId AND position = 2")
    suspend fun getRunnerUpPrize(competitionId: Int): Int?

    @Query("SELECT SUM(prize_money) FROM prizes_leagues WHERE competition_id = :competitionId")
    suspend fun getTotalPrizePool(competitionId: Int): Int?

    @Query("SELECT COUNT(*) FROM prizes_leagues WHERE competition_id = :competitionId")
    suspend fun getPrizePositionsCount(competitionId: Int): Int

    @Query("SELECT * FROM prizes_leagues")
    suspend fun getAllStatic(): List<PrizesLeaguesEntity>

    // ============ STATISTICS QUERIES ============

    @Query("""
        SELECT 
            competition_id,
            COUNT(*) as prize_positions,
            SUM(prize_money) as total_pool,
            MAX(prize_money) as max_prize,
            MIN(prize_money) as min_prize,
            AVG(prize_money) as avg_prize
        FROM prizes_leagues 
        GROUP BY competition_id
    """)
    fun getLeaguePrizeStatistics(): Flow<List<LeaguePrizeStats>>
}

// ============ DATA CLASSES ============

data class LeaguePrizeStats(
    @ColumnInfo(name = "competition_id")
    val competitionId: Int,

    @ColumnInfo(name = "prize_positions")
    val prizePositions: Int,

    @ColumnInfo(name = "total_pool")
    val totalPool: Int,

    @ColumnInfo(name = "max_prize")
    val maxPrize: Int,

    @ColumnInfo(name = "min_prize")
    val minPrize: Int,

    @ColumnInfo(name = "avg_prize")
    val averagePrize: Double
)