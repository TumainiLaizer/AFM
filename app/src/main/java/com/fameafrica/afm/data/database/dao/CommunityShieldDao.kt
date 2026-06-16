package com.fameafrica.afm.data.database.dao

import androidx.room.*
import com.fameafrica.afm.data.database.entities.CommunityShieldEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CommunityShieldDao {

    // ============ BASIC CRUD ============

    @Query("SELECT * FROM community_shield ORDER BY season DESC, match_date DESC")
    fun getAll(): Flow<List<CommunityShieldEntity>>

    @Query("SELECT * FROM community_shield WHERE id = :id")
    suspend fun getById(id: Int): CommunityShieldEntity?

    @Query("SELECT * FROM community_shield")
    suspend fun getAllStatic(): List<CommunityShieldEntity>

    @Query("SELECT * FROM community_shield WHERE league_id = :leagueId AND season = :season")
    suspend fun getByLeagueAndSeason(leagueId: Int, season: String): CommunityShieldEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(shield: CommunityShieldEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(shields: List<CommunityShieldEntity>)

    @Update
    suspend fun update(shield: CommunityShieldEntity)

    @Delete
    suspend fun delete(shield: CommunityShieldEntity)

    @Query("DELETE FROM community_shield WHERE league_id = :leagueId AND season = :season")
    suspend fun deleteByLeagueAndSeason(leagueId: Int, season: String)

    @Query("DELETE FROM community_shield")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM community_shield WHERE season = :season")
    suspend fun getCountForSeason(season: String): Int

    // ============ SEASON-BASED QUERIES ============

    @Query("SELECT * FROM community_shield WHERE season = :season ORDER BY match_date")
    fun getShieldsBySeason(season: String): Flow<List<CommunityShieldEntity>>

    @Query("SELECT * FROM community_shield WHERE season = :season AND is_played = 0 ORDER BY match_date")
    fun getUpcomingShieldsBySeason(season: String): Flow<List<CommunityShieldEntity>>

    @Query("SELECT * FROM community_shield WHERE season = :season AND is_played = 1 ORDER BY match_date DESC")
    fun getPlayedShieldsBySeason(season: String): Flow<List<CommunityShieldEntity>>

    @Query("SELECT DISTINCT season FROM community_shield ORDER BY season DESC")
    fun getSeasons(): Flow<List<String>>

    // ============ LEAGUE-BASED QUERIES ============

    @Query("SELECT * FROM community_shield WHERE league_id = :leagueId ORDER BY season DESC")
    fun getShieldsByLeague(leagueId: Int): Flow<List<CommunityShieldEntity>>

    @Query("SELECT * FROM community_shield WHERE league_id = :leagueId AND is_played = 1 ORDER BY season DESC")
    fun getPlayedShieldsByLeague(leagueId: Int): Flow<List<CommunityShieldEntity>>

    // ============ TEAM-BASED QUERIES ============

    @Query("SELECT * FROM community_shield WHERE league_winner_id = :teamId OR league_runner_up_id = :teamId OR league_third_id = :teamId OR league_fourth_id = :teamId ORDER BY season DESC")
    fun getShieldsByTeam(teamId: Int): Flow<List<CommunityShieldEntity>>

    @Query("SELECT * FROM community_shield WHERE winner = :teamName ORDER BY season DESC")
    fun getShieldWinsByTeam(teamName: String): Flow<List<CommunityShieldEntity>>

    @Query("SELECT COUNT(*) FROM community_shield WHERE winner = :teamName")
    suspend fun getShieldWinCount(teamName: String): Int

    // ============ STATUS QUERIES ============

    @Query("SELECT * FROM community_shield WHERE is_played = 0 AND match_date < date('now')")
    fun getOverdueShields(): Flow<List<CommunityShieldEntity>>

    @Query("SELECT * FROM community_shield WHERE is_played = 1 ORDER BY match_date DESC LIMIT :limit")
    fun getRecentShields(limit: Int): Flow<List<CommunityShieldEntity>>

    // ============ FIXTURE QUERIES ============

    @Query("SELECT * FROM community_shield WHERE fixture_id = :fixtureId")
    suspend fun getByFixtureId(fixtureId: Int): CommunityShieldEntity?

    @Query("UPDATE community_shield SET fixture_id = :fixtureId WHERE id = :id")
    suspend fun updateFixtureId(id: Int, fixtureId: Int)

    // ============ STATISTICS QUERIES ============

    @Query("""
        SELECT 
            league_name,
            COUNT(*) as total_editions,
            COUNT(CASE WHEN is_played = 1 THEN 1 END) as completed_editions,
            AVG(prize_money) as avg_prize_money
        FROM community_shield 
        GROUP BY league_name
        ORDER BY total_editions DESC
    """)
    fun getLeagueShieldStatistics(): Flow<List<LeagueShieldStats>>

    @Query("""
        SELECT 
            winner,
            COUNT(*) as shield_wins
        FROM community_shield 
        WHERE winner IS NOT NULL
        GROUP BY winner
        ORDER BY shield_wins DESC
        LIMIT :limit
    """)
    fun getTopShieldWinners(limit: Int): Flow<List<ShieldWinnerStats>>

    // ============ JOIN QUERIES ============

    @Query("""
        SELECT 
            cs.*,
            l.country_id as league_country_id,
            l.logo as league_logo,
            l.level as league_level,
            w.logo_path as winner_logo,
            ru.logo_path as runner_up_logo,
            f.match_date as fixture_date,
            f.stadium as fixture_stadium,
            f.match_status as fixture_status
        FROM community_shield cs
        LEFT JOIN leagues l ON cs.league_id = l.id
        LEFT JOIN teams w ON cs.winner = w.name
        LEFT JOIN teams ru ON cs.league_runner_up = ru.name
        LEFT JOIN fixtures f ON cs.fixture_id = f.id
        WHERE cs.id = :shieldId
    """)
    suspend fun getShieldWithDetails(shieldId: Int): ShieldWithDetails?

    @Query("""
        SELECT 
            cs.*,
            l.country as league_country,
            w.logo_path as winner_logo
        FROM community_shield cs
        LEFT JOIN leagues l ON cs.league_id = l.id
        LEFT JOIN teams w ON cs.winner = w.name
        WHERE cs.season = :season
        ORDER BY cs.match_date
    """)
    fun getShieldsForSeasonWithDetails(season: String): Flow<List<ShieldWithBasicDetails>>
}

// ============ DATA CLASSES ============

data class LeagueShieldStats(
    @ColumnInfo(name = "league_name")
    val leagueName: String,

    @ColumnInfo(name = "total_editions")
    val totalEditions: Int,

    @ColumnInfo(name = "completed_editions")
    val completedEditions: Int,

    @ColumnInfo(name = "avg_prize_money")
    val averagePrizeMoney: Double
)

data class ShieldWinnerStats(
    @ColumnInfo(name = "winner")
    val teamName: String,

    @ColumnInfo(name = "shield_wins")
    val shieldWins: Int
)

data class ShieldWithDetails(
    @Embedded
    val shield: CommunityShieldEntity,

    @ColumnInfo(name = "league_country_id")
    val leagueCountryId: Int?,

    @ColumnInfo(name = "league_logo")
    val leagueLogo: String?,

    @ColumnInfo(name = "league_level")
    val leagueLevel: Int?,

    @ColumnInfo(name = "winner_logo")
    val winnerLogo: String?,

    @ColumnInfo(name = "runner_up_logo")
    val runnerUpLogo: String?,

    @ColumnInfo(name = "fixture_date")
    val fixtureDate: String?,

    @ColumnInfo(name = "fixture_stadium")
    val fixtureStadium: String?,

    @ColumnInfo(name = "fixture_status")
    val fixtureStatus: String?
)

data class ShieldWithBasicDetails(
    @Embedded
    val shield: CommunityShieldEntity,

    @ColumnInfo(name = "league_country")
    val leagueCountry: String?,

    @ColumnInfo(name = "winner_logo")
    val winnerLogo: String?
)
