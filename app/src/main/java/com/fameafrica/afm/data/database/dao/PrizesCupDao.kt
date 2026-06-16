package com.fameafrica.afm.data.database.dao

import androidx.room.*
import com.fameafrica.afm.data.database.entities.PrizesCupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PrizesCupDao {

    // ============ BASIC CRUD ============

    @Query("SELECT * FROM prizes_cup ORDER BY competition_id, stage, position")
    fun getAll(): Flow<List<PrizesCupEntity>>

    @Query("SELECT * FROM prizes_cup WHERE id = :id")
    suspend fun getById(id: Int): PrizesCupEntity?

    @Query("SELECT * FROM prizes_cup WHERE competition_id = :competitionId AND stage = :stage AND position = :position")
    suspend fun getPrizeByCompetitionStageAndPosition(
        competitionId: Int,
        stage: String,
        position: Int
    ): PrizesCupEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(prize: PrizesCupEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(prizes: List<PrizesCupEntity>)

    @Update
    suspend fun update(prize: PrizesCupEntity)

    @Delete
    suspend fun delete(prize: PrizesCupEntity)

    @Query("DELETE FROM prizes_cup WHERE competition_id = :competitionId")
    suspend fun deleteByCompetition(competitionId: Int)

    @Query("DELETE FROM prizes_cup")
    suspend fun deleteAll()

    // ============ COMPETITION-BASED QUERIES ============

    @Query("""
        SELECT * FROM prizes_cup WHERE competition_id = :competitionId ORDER BY 
            CASE stage
            WHEN 'FINAL' THEN 1
            WHEN 'THIRD_PLACE' THEN 2
            WHEN 'SEMI_FINAL' THEN 3
            WHEN 'QUARTER_FINAL' THEN 4
            WHEN 'ROUND_16' THEN 5
            WHEN 'ROUND_32' THEN 6
            WHEN 'GROUP_STAGE' THEN 7
            ELSE 8
            END, position
    """)
    fun getPrizesByCompetition(competitionId: Int): Flow<List<PrizesCupEntity>>

    @Query("SELECT * FROM prizes_cup WHERE competition_id = :competitionId AND stage = :stage ORDER BY position")
    fun getPrizesByStage(competitionId: Int, stage: String): Flow<List<PrizesCupEntity>>

    @Query("SELECT prize_money FROM prizes_cup WHERE competition_id = :competitionId AND stage = 'FINAL' AND position = 1")
    suspend fun getWinnerPrize(competitionId: Int): Int?

    @Query("SELECT prize_money FROM prizes_cup WHERE competition_id = :competitionId AND stage = 'FINAL' AND position = 2")
    suspend fun getRunnerUpPrize(competitionId: Int): Int?

    @Query("SELECT SUM(prize_money) FROM prizes_cup WHERE competition_id = :competitionId")
    suspend fun getTotalPrizePool(competitionId: Int): Int?

    @Query("SELECT * FROM prizes_cup")
    suspend fun getAllStatic(): List<PrizesCupEntity>

    // ============ STATISTICS QUERIES ============

    @Query("""
        SELECT 
            competition_id,
            COUNT(*) as prize_positions,
            SUM(prize_money) as total_pool,
            MAX(prize_money) as max_prize,
            MIN(prize_money) as min_prize
        FROM prizes_cup 
        GROUP BY competition_id
    """)
    fun getCupPrizeStatistics(): Flow<List<CupPrizeStats>>
}

// ============ DATA CLASSES ============

data class CupPrizeStats(
    @ColumnInfo(name = "competition_id")
    val competitionId: Int,

    @ColumnInfo(name = "prize_positions")
    val prizePositions: Int,

    @ColumnInfo(name = "total_pool")
    val totalPool: Int,

    @ColumnInfo(name = "max_prize")
    val maxPrize: Int,

    @ColumnInfo(name = "min_prize")
    val minPrize: Int
)
