package com.fameafrica.afm.data.database.dao

import androidx.room.*
import com.fameafrica.afm.data.database.entities.KnockoutMatchesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface KnockoutMatchesDao {

    // ============ BASIC CRUD ============

    @Query("SELECT * FROM knockout_matches ORDER BY season DESC, cup_name, round_number, match_number")
    fun getAll(): Flow<List<KnockoutMatchesEntity>>

    @Query("SELECT * FROM knockout_matches WHERE id = :id")
    suspend fun getById(id: Int): KnockoutMatchesEntity?

    @Query("SELECT * FROM knockout_matches WHERE fixture_id = :fixtureId")
    suspend fun getByFixtureId(fixtureId: Int): KnockoutMatchesEntity?

    @Query("SELECT * FROM knockout_matches WHERE bracket_id = :bracketId")
    suspend fun getByBracketId(bracketId: Int): KnockoutMatchesEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(match: KnockoutMatchesEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(matches: List<KnockoutMatchesEntity>)

    @Update
    suspend fun update(match: KnockoutMatchesEntity)

    @Delete
    suspend fun delete(match: KnockoutMatchesEntity)

    @Query("DELETE FROM knockout_matches WHERE cup_name = :cupName AND season = :season")
    suspend fun deleteByCupAndSeason(cupName: String, season: String)

    @Query("DELETE FROM knockout_matches")
    suspend fun deleteAll()

    @Query("SELECT * FROM knockout_matches")
    suspend fun getAllStatic(): List<KnockoutMatchesEntity>

    // ============ CUP-BASED QUERIES ============

    @Query("SELECT * FROM knockout_matches WHERE cup_name = :cupName AND season = :season ORDER BY round_number, match_number")
    fun getMatchesByCupAndSeason(cupName: String, season: String): Flow<List<KnockoutMatchesEntity>>

    @Query("SELECT * FROM knockout_matches WHERE cup_name = :cupName AND season = :season AND round = :round ORDER BY match_number")
    fun getMatchesByRound(cupName: String, season: String, round: String): Flow<List<KnockoutMatchesEntity>>

    @Query("SELECT * FROM knockout_matches WHERE cup_name = :cupName AND season = :season AND round_number = :roundNumber ORDER BY match_number")
    fun getMatchesByRoundNumber(cupName: String, season: String, roundNumber: Int): Flow<List<KnockoutMatchesEntity>>

    @Query("SELECT * FROM knockout_matches WHERE cup_name = :cupName AND season = :season AND round = 'Final'")
    suspend fun getFinalMatch(cupName: String, season: String): KnockoutMatchesEntity?

    @Query("SELECT * FROM knockout_matches WHERE cup_name = :cupName AND season = :season AND round = 'Semi-final'")
    fun getSemiFinals(cupName: String, season: String): Flow<List<KnockoutMatchesEntity>>

    @Query("SELECT * FROM knockout_matches WHERE cup_name = :cupName AND season = :season AND round = 'Quarter-final'")
    fun getQuarterFinals(cupName: String, season: String): Flow<List<KnockoutMatchesEntity>>

    // ============ TEAM-BASED QUERIES ============

    @Query("SELECT * FROM knockout_matches WHERE home_team = :teamName OR away_team = :teamName ORDER BY season DESC, match_date DESC")
    fun getMatchesByTeam(teamName: String): Flow<List<KnockoutMatchesEntity>>

    @Query("SELECT * FROM knockout_matches WHERE winner = :teamName ORDER BY season DESC")
    fun getWinsByTeam(teamName: String): Flow<List<KnockoutMatchesEntity>>

    @Query("SELECT COUNT(*) FROM knockout_matches WHERE winner = :teamName AND cup_name = :cupName")
    suspend fun getTeamCupWinsCount(teamName: String, cupName: String): Int

    // ============ TWO-LEGGED TIE QUERIES ============

    @Query("SELECT * FROM knockout_matches WHERE (first_leg_id = :matchId OR second_leg_id = :matchId) OR id = :matchId OR id = :firstLegId OR id = :secondLegId")
    suspend fun getTwoLeggedTie(matchId: Int, firstLegId: Int, secondLegId: Int): List<KnockoutMatchesEntity>

    @Query("SELECT * FROM knockout_matches WHERE first_leg_id = :firstLegId")
    suspend fun getSecondLegByFirstLeg(firstLegId: Int): KnockoutMatchesEntity?

    @Query("SELECT * FROM knockout_matches WHERE second_leg_id = :secondLegId")
    suspend fun getFirstLegBySecondLeg(secondLegId: Int): KnockoutMatchesEntity?

    // ============ PROGRESSION QUERIES ============

    @Query("SELECT * FROM knockout_matches WHERE next_match_id = :nextMatchId")
    suspend fun getPreviousMatches(nextMatchId: Int): List<KnockoutMatchesEntity>

    @Query("SELECT * FROM knockout_matches WHERE id IN (:matchIds)")
    suspend fun getMatchesByIds(matchIds: List<Int>): List<KnockoutMatchesEntity>

    // ============ STATUS QUERIES ============

    @Query("SELECT * FROM knockout_matches WHERE cup_name = :cupName AND season = :season AND is_played = 0 ORDER BY round_number, match_date")
    fun getUpcomingMatches(cupName: String, season: String): Flow<List<KnockoutMatchesEntity>>

    @Query("SELECT * FROM knockout_matches WHERE cup_name = :cupName AND season = :season AND is_played = 1 ORDER BY match_date DESC")
    fun getPlayedMatches(cupName: String, season: String): Flow<List<KnockoutMatchesEntity>>

    @Query("SELECT * FROM knockout_matches WHERE match_date < date('now') AND is_played = 0")
    fun getOverdueMatches(): Flow<List<KnockoutMatchesEntity>>

    // ============ REFEREE QUERIES ============

    @Query("SELECT * FROM knockout_matches WHERE referee_id = :refereeId ORDER BY match_date DESC")
    fun getMatchesByReferee(refereeId: Int): Flow<List<KnockoutMatchesEntity>>

    // ============ STATISTICS QUERIES ============

    @Query("""
        SELECT 
            cup_name,
            COUNT(*) as total_matches,
            COUNT(CASE WHEN is_played = 1 THEN 1 END) as played_matches,
            AVG(home_score + away_score) as avg_goals,
            SUM(attendance) as total_attendance
        FROM knockout_matches 
        WHERE season = :season
        GROUP BY cup_name
    """)
    fun getCupKnockoutStatistics(season: String): Flow<List<CupKnockoutStats>>

    @Query("""
        SELECT 
            round,
            COUNT(*) as match_count,
            AVG(home_score + away_score) as avg_goals
        FROM knockout_matches 
        WHERE cup_name = :cupName AND season = :season AND is_played = 1
        GROUP BY round
        ORDER BY round_number
    """)
    fun getRoundStatistics(cupName: String, season: String): Flow<List<RoundStats>>

    @Query("""
        SELECT 
            winner,
            COUNT(*) as wins
        FROM knockout_matches 
        WHERE cup_name = :cupName AND winner IS NOT NULL
        GROUP BY winner
        ORDER BY wins DESC
        LIMIT :limit
    """)
    fun getTopPerformers(cupName: String, limit: Int): Flow<List<CupTopPerformer>>

    // ============ JOIN QUERIES ============

    @Query("""
        SELECT 
            km.*,
            c.logo as cup_logo,
            c.type as cup_type,
            c.country_id as cup_country_id,
            ht.logo_path as h_team_logo,
            at.logo_path as a_team_logo,
            r.name as detail_referee_name,
            r.strictness as detail_referee_strictness,
            f.match_status as fixture_status,
            cb.bracket_position as bracket_position
        FROM knockout_matches km
        LEFT JOIN cups c ON km.cup_name = c.name
        LEFT JOIN teams ht ON km.home_team = ht.name
        LEFT JOIN teams at ON km.away_team = at.name
        LEFT JOIN referees r ON km.referee_id = r.referee_id
        LEFT JOIN fixtures f ON km.fixture_id = f.id
        LEFT JOIN cup_brackets cb ON km.bracket_id = cb.id
        WHERE km.id = :matchId
    """)
    suspend fun getKnockoutMatchWithDetails(matchId: Int): KnockoutMatchWithDetails?

    @Query("""
        SELECT 
            km.*,
            ht.logo_path as h_team_logo,
            at.logo_path as a_team_logo
        FROM knockout_matches km
        LEFT JOIN teams ht ON km.home_team = ht.name
        LEFT JOIN teams at ON km.away_team = at.name
        WHERE km.cup_name = :cupName AND km.season = :season
        ORDER BY km.round_number, km.match_number
    """)
    fun getKnockoutBracketWithLogos(cupName: String, season: String): Flow<List<KnockoutMatchWithLogos>>
}

// ============ DATA CLASSES ============

data class CupKnockoutStats(
    @ColumnInfo(name = "cup_name")
    val cupName: String,

    @ColumnInfo(name = "total_matches")
    val totalMatches: Int,

    @ColumnInfo(name = "played_matches")
    val playedMatches: Int,

    @ColumnInfo(name = "avg_goals")
    val averageGoals: Double,

    @ColumnInfo(name = "total_attendance")
    val totalAttendance: Int
)

data class RoundStats(
    @ColumnInfo(name = "round")
    val round: String,

    @ColumnInfo(name = "match_count")
    val matchCount: Int,

    @ColumnInfo(name = "avg_goals")
    val averageGoals: Double
)

data class CupTopPerformer(
    @ColumnInfo(name = "winner")
    val teamName: String,

    @ColumnInfo(name = "wins")
    val wins: Int
)

data class KnockoutMatchWithDetails(
    @Embedded
    val match: KnockoutMatchesEntity,

    @ColumnInfo(name = "cup_logo")
    val cupLogo: String?,

    @ColumnInfo(name = "cup_type")
    val cupType: String?,

    @ColumnInfo(name = "cup_country_id")
    val cupCountryId: Int?,

    @ColumnInfo(name = "h_team_logo")
    val homeTeamLogo: String?,

    @ColumnInfo(name = "a_team_logo")
    val awayTeamLogo: String?,

    @ColumnInfo(name = "detail_referee_name")
    val refereeName: String?,

    @ColumnInfo(name = "detail_referee_strictness")
    val refereeStrictness: Int?,

    @ColumnInfo(name = "fixture_status")
    val fixtureStatus: String?,

    @ColumnInfo(name = "bracket_position")
    val bracketPosition: Int?
)

data class KnockoutMatchWithLogos(
    @Embedded
    val match: KnockoutMatchesEntity,

    @ColumnInfo(name = "h_team_logo")
    val homeTeamLogo: String?,

    @ColumnInfo(name = "a_team_logo")
    val awayTeamLogo: String?
)
