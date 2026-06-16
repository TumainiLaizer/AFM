package com.fameafrica.afm.data.database.dao

import androidx.room.*
import com.fameafrica.afm.data.database.entities.SeasonAwardsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SeasonAwardsDao {

    // ============ BASIC CRUD ============

    @Query("SELECT * FROM season_awards ORDER BY season DESC, award_type")
    fun getAll(): Flow<List<SeasonAwardsEntity>>

    @Query("SELECT * FROM season_awards WHERE id = :id")
    suspend fun getById(id: Int): SeasonAwardsEntity?

    @Query("SELECT * FROM season_awards WHERE season = :season AND award_type = :awardType")
    suspend fun getAward(season: String, awardType: String): SeasonAwardsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(award: SeasonAwardsEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(awards: List<SeasonAwardsEntity>)

    @Update
    suspend fun update(award: SeasonAwardsEntity)

    @Delete
    suspend fun delete(award: SeasonAwardsEntity)

    @Query("DELETE FROM season_awards WHERE season = :season")
    suspend fun deleteBySeason(season: String)

    @Query("SELECT * FROM season_awards")
    suspend fun getAllStatic(): List<SeasonAwardsEntity>

    // ============ SEASON-BASED QUERIES ============

    @Query("SELECT * FROM season_awards WHERE season = :season ORDER BY award_type")
    fun getAwardsBySeason(season: String): Flow<List<SeasonAwardsEntity>>

    @Query("SELECT * FROM season_awards WHERE season = :season AND award_category = :category ORDER BY award_type")
    fun getAwardsBySeasonAndCategory(season: String, category: String): Flow<List<SeasonAwardsEntity>>

    // ============ PLAYER-BASED QUERIES ============

    @Query("SELECT * FROM season_awards WHERE player_id = :playerId ORDER BY season DESC")
    fun getPlayerAwards(playerId: Int): Flow<List<SeasonAwardsEntity>>

    @Query("SELECT COUNT(*) FROM season_awards WHERE player_id = :playerId")
    suspend fun getPlayerAwardCount(playerId: Int): Int

    // ============ TEAM-BASED QUERIES ============

    @Query("SELECT * FROM season_awards WHERE team_id = :teamId ORDER BY season DESC")
    fun getTeamAwards(teamId: Int): Flow<List<SeasonAwardsEntity>>

    @Query("SELECT COUNT(*) FROM season_awards WHERE team_id = :teamId")
    suspend fun getTeamAwardCount(teamId: Int): Int

    // ============ COACH-BASED QUERIES ============

    @Query("SELECT * FROM season_awards WHERE coach_name = :coachName ORDER BY season DESC")
    fun getCoachAwards(coachName: String): Flow<List<SeasonAwardsEntity>>

    // ============ STATISTICS QUERIES ============

    @Query("""
        SELECT 
            award_type,
            COUNT(*) as total_awards,
            player_name,
            COUNT(*) as wins
        FROM season_awards 
        GROUP BY award_type, player_name
        ORDER BY award_type, wins DESC
    """)
    fun getAwardWinnersByType(): Flow<List<AwardWinnersByType>>

    @Query("""
        SELECT 
            player_name,
            COUNT(*) as total_awards
        FROM season_awards 
        WHERE player_id IS NOT NULL
        GROUP BY player_id
        ORDER BY total_awards DESC
        LIMIT :limit
    """)
    fun getMostAwardedPlayers(limit: Int): Flow<List<MostAwardedPlayer>>
}

// ============ DATA CLASSES ============

data class AwardWinnersByType(
    @ColumnInfo(name = "award_type")
    val awardType: String,

    @ColumnInfo(name = "total_awards")
    val totalAwards: Int,

    @ColumnInfo(name = "player_name")
    val playerName: String,

    @ColumnInfo(name = "wins")
    val wins: Int
)

data class MostAwardedPlayer(
    @ColumnInfo(name = "player_name")
    val playerName: String,

    @ColumnInfo(name = "total_awards")
    val totalAwards: Int
)
