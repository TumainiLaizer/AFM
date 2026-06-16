package com.fameafrica.afm.data.database.dao

import androidx.room.*
import com.fameafrica.afm.data.database.entities.SeasonHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SeasonHistoryDao {

    // ============ BASIC CRUD ============

    @Query("SELECT * FROM season_history ORDER BY season DESC, position")
    fun getAll(): Flow<List<SeasonHistoryEntity>>

    @Query("SELECT * FROM season_history WHERE id = :id")
    suspend fun getById(id: Int): SeasonHistoryEntity?

    @Query("SELECT * FROM season_history WHERE team_id = :teamId AND season = :season")
    suspend fun getTeamSeason(teamId: Int, season: String): SeasonHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: SeasonHistoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(histories: List<SeasonHistoryEntity>)

    @Update
    suspend fun update(history: SeasonHistoryEntity)

    @Delete
    suspend fun delete(history: SeasonHistoryEntity)

    @Query("DELETE FROM season_history WHERE season = :season")
    suspend fun deleteBySeason(season: String)

    @Query("SELECT * FROM season_history")
    suspend fun getAllStatic(): List<SeasonHistoryEntity>

    // ============ TEAM-BASED QUERIES ============

    @Query("SELECT * FROM season_history WHERE team_id = :teamId ORDER BY season DESC")
    fun getTeamHistory(teamId: Int): Flow<List<SeasonHistoryEntity>>

    @Query("SELECT * FROM season_history WHERE team_id = :teamId AND position = 1 ORDER BY season DESC")
    fun getTeamTitles(teamId: Int): Flow<List<SeasonHistoryEntity>>

    @Query("SELECT COUNT(*) FROM season_history WHERE team_id = :teamId AND position = 1")
    suspend fun getTeamTitleCount(teamId: Int): Int

    @Query("SELECT * FROM season_history WHERE team_id = :teamId AND promoted = 1 ORDER BY season DESC")
    fun getTeamPromotions(teamId: Int): Flow<List<SeasonHistoryEntity>>

    @Query("SELECT * FROM season_history WHERE team_id = :teamId AND relegated = 1 ORDER BY season DESC")
    fun getTeamRelegations(teamId: Int): Flow<List<SeasonHistoryEntity>>

    // ============ SEASON-BASED QUERIES ============

    @Query("SELECT * FROM season_history WHERE season = :season ORDER BY position")
    fun getSeasonStandings(season: String): Flow<List<SeasonHistoryEntity>>

    @Query("SELECT * FROM season_history WHERE season = :season AND position = 1")
    suspend fun getSeasonChampion(season: String): SeasonHistoryEntity?

    @Query("SELECT * FROM season_history WHERE season = :season AND promoted = 1 ORDER BY position")
    fun getSeasonPromotions(season: String): Flow<List<SeasonHistoryEntity>>

    @Query("SELECT * FROM season_history WHERE season = :season AND relegated = 1 ORDER BY position")
    fun getSeasonRelegations(season: String): Flow<List<SeasonHistoryEntity>>

    @Query("SELECT DISTINCT season FROM season_history ORDER BY season DESC")
    fun getSeasons(): Flow<List<String>>

    // ============ STATISTICS QUERIES ============

    @Query("""
        SELECT 
            team_id,
            team_name,
            COUNT(*) as seasons_played,
            SUM(CASE WHEN position = 1 THEN 1 ELSE 0 END) as titles,
            SUM(trophies_won) as total_trophies,
            AVG(position) as avg_position,
            SUM(promoted) as promotions,
            SUM(relegated) as relegations
        FROM season_history 
        GROUP BY team_id
        ORDER BY titles DESC, avg_position
    """)
    fun getAllTimeTeamStats(): Flow<List<AllTimeTeamStats>>

    @Query("""
        SELECT 
            season,
            AVG(goals_for) as avg_goals_for,
            AVG(goals_against) as avg_goals_against,
            SUM(CASE WHEN promoted THEN 1 ELSE 0 END) as total_promotions,
            SUM(CASE WHEN relegated THEN 1 ELSE 0 END) as total_relegations
        FROM season_history 
        GROUP BY season
        ORDER BY season DESC
    """)
    fun getSeasonStatistics(): Flow<List<SeasonStats>>
}

// ============ DATA CLASSES ============

data class AllTimeTeamStats(
    @ColumnInfo(name = "team_id")
    val teamId: Int,

    @ColumnInfo(name = "team_name")
    val teamName: String,

    @ColumnInfo(name = "seasons_played")
    val seasonsPlayed: Int,

    @ColumnInfo(name = "titles")
    val titles: Int,

    @ColumnInfo(name = "total_trophies")
    val totalTrophies: Int,

    @ColumnInfo(name = "avg_position")
    val averagePosition: Double,

    @ColumnInfo(name = "promotions")
    val promotions: Int,

    @ColumnInfo(name = "relegations")
    val relegations: Int
)

data class SeasonStats(
    @ColumnInfo(name = "season")
    val season: String,

    @ColumnInfo(name = "avg_goals_for")
    val averageGoalsFor: Double,

    @ColumnInfo(name = "avg_goals_against")
    val averageGoalsAgainst: Double,

    @ColumnInfo(name = "total_promotions")
    val totalPromotions: Int,

    @ColumnInfo(name = "total_relegations")
    val totalRelegations: Int
)
