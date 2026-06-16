package com.fameafrica.afm.data.database.dao

import androidx.room.*
import com.fameafrica.afm.data.database.entities.CupBracketsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CupBracketsDao {

    // ============ BASIC CRUD ============

    @Query("SELECT * FROM cup_brackets ORDER BY season DESC, cupName, round_number, bracket_position")
    fun getAll(): Flow<List<CupBracketsEntity>>

    @Query("SELECT * FROM cup_brackets WHERE id = :id")
    suspend fun getById(id: Int): CupBracketsEntity?

    @Query("SELECT * FROM cup_brackets WHERE fixtureId = :fixtureId")
    suspend fun getByFixtureId(fixtureId: Int): CupBracketsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bracket: CupBracketsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(brackets: List<CupBracketsEntity>)

    @Update
    suspend fun update(bracket: CupBracketsEntity)

    @Delete
    suspend fun delete(bracket: CupBracketsEntity)

    @Query("DELETE FROM cup_brackets WHERE cupName = :cupName AND season = :season")
    suspend fun deleteByCupAndSeason(cupName: String, season: Int)

    @Query("DELETE FROM cup_brackets")
    suspend fun deleteAll()

    @Query("SELECT * FROM cup_brackets")
    suspend fun getAllStatic(): List<CupBracketsEntity>

    // ============ CUP-BASED QUERIES ============

    @Query("SELECT * FROM cup_brackets WHERE cupName = :cupName AND season = :season ORDER BY round_number, bracket_position")
    fun getBracketsByCupAndSeason(cupName: String, season: Int): Flow<List<CupBracketsEntity>>

    @Query("SELECT * FROM cup_brackets WHERE cupName = :cupName AND season = :season AND round = :round ORDER BY bracket_position")
    fun getBracketsByRound(cupName: String, season: Int, round: String): Flow<List<CupBracketsEntity>>

    @Query("SELECT * FROM cup_brackets WHERE cupName = :cupName AND season = :season AND teamName = :teamName")
    fun getTeamBrackets(cupName: String, season: Int, teamName: String): Flow<List<CupBracketsEntity>>

    @Query("SELECT * FROM cup_brackets WHERE cupName = :cupName AND season = :season AND winner = :teamName")
    fun getTeamWins(cupName: String, season: Int, teamName: String): Flow<List<CupBracketsEntity>>

    // ============ ROUND-BASED QUERIES ============

    @Query("SELECT DISTINCT round FROM cup_brackets WHERE cupName = :cupName AND season = :season ORDER BY round_number")
    fun getRoundsByCup(cupName: String, season: Int): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM cup_brackets WHERE cupName = :cupName AND season = :season AND round = :round")
    suspend fun getMatchCountInRound(cupName: String, season: Int, round: String): Int

    // ============ PROGRESSION QUERIES ============

    @Query("SELECT * FROM cup_brackets WHERE next_bracket_id = :bracketId")
    suspend fun getPreviousBracket(bracketId: Int): CupBracketsEntity?

    @Query("SELECT * FROM cup_brackets WHERE parent_bracket_id = :bracketId")
    suspend fun getChildBrackets(bracketId: Int): List<CupBracketsEntity>

    @Query("SELECT * FROM cup_brackets WHERE cupName = :cupName AND season = :season AND round = 'FINAL'")
    suspend fun getFinalBracket(cupName: String, season: Int): CupBracketsEntity?

    @Query("SELECT * FROM cup_brackets WHERE cupName = :cupName AND season = :season AND round = 'SEMI_FINAL'")
    fun getSemiFinals(cupName: String, season: Int): Flow<List<CupBracketsEntity>>

    @Query("SELECT * FROM cup_brackets WHERE cupName = :cupName AND season = :season AND round = 'QUARTER_FINAL'")
    fun getQuarterFinals(cupName: String, season: Int): Flow<List<CupBracketsEntity>>

    // ============ STATUS QUERIES ============

    @Query("SELECT * FROM cup_brackets WHERE cupName = :cupName AND season = :season AND result IS NULL AND opponentName IS NOT NULL")
    fun getPendingMatches(cupName: String, season: Int): Flow<List<CupBracketsEntity>>

    @Query("SELECT * FROM cup_brackets WHERE cupName = :cupName AND season = :season AND result IS NOT NULL")
    fun getCompletedMatches(cupName: String, season: Int): Flow<List<CupBracketsEntity>>

    @Query("SELECT * FROM cup_brackets WHERE is_walkover = 1")
    fun getWalkovers(): Flow<List<CupBracketsEntity>>

    // ============ TEAM-BASED QUERIES ============

    @Query("SELECT * FROM cup_brackets WHERE teamName = :teamName OR opponentName = :teamName ORDER BY season DESC, cupName, round_number")
    fun getTeamCupHistory(teamName: String): Flow<List<CupBracketsEntity>>

    @Query("SELECT * FROM cup_brackets WHERE winner = :teamName ORDER BY season DESC")
    fun getTeamCupWins(teamName: String): Flow<List<CupBracketsEntity>>

    // ============ STATISTICS QUERIES ============

    @Query("""
        SELECT 
            cupName,
            COUNT(*) as total_matches,
            COUNT(CASE WHEN result IS NOT NULL THEN 1 END) as completed_matches,
            COUNT(CASE WHEN is_walkover = 1 THEN 1 END) as walkovers
        FROM cup_brackets 
        WHERE season = :season
        GROUP BY cupName
    """)
    fun getCupStatistics(season: Int): Flow<List<CupStatistics>>

    @Query("""
        SELECT 
            teamName,
            COUNT(*) as matches_played,
            COUNT(CASE WHEN winner = teamName THEN 1 END) as wins
        FROM cup_brackets 
        WHERE teamName IS NOT NULL AND teamName != 'BYE'
        GROUP BY teamName
        ORDER BY wins DESC
        LIMIT :limit
    """)
    fun getTopCupPerformers(limit: Int): Flow<List<CupPerformerStats>>

    // ============ JOIN QUERIES ============

    @Query("""
        SELECT 
            cb.*,
            c.name as detailed_cup_name,
            c.type as cup_type,
            c.country_id as cup_country_id,
            t.logo_path as team_logo,
            o.logo_path as opponent_logo,
            f.match_date as fixture_date,
            f.stadium as fixture_stadium,
            f.match_status as fixture_status
        FROM cup_brackets cb
        LEFT JOIN cups c ON cb.cupName = c.name
        LEFT JOIN teams t ON cb.teamName = t.name
        LEFT JOIN teams o ON cb.opponentName = o.name
        LEFT JOIN fixtures f ON cb.fixtureId = f.id
        WHERE cb.id = :bracketId
    """)
    suspend fun getBracketWithDetails(bracketId: Int): BracketWithDetails?

    @Query("""
        SELECT 
            cb.*,
            c.logo as cup_logo,
            c.prize_money as cup_prize,
            t.logo_path as team_logo
        FROM cup_brackets cb
        LEFT JOIN cups c ON cb.cupName = c.name
        LEFT JOIN teams t ON cb.teamName = t.name
        WHERE cb.cupName = :cupName AND cb.season = :season
        ORDER BY cb.round_number, cb.bracket_position
    """)
    fun getFullBracketWithDetails(cupName: String, season: Int): Flow<List<BracketWithCupDetails>>
}

// ============ DATA CLASSES ============

data class CupStatistics(
    @ColumnInfo(name = "cupName")
    val cupName: String,

    @ColumnInfo(name = "total_matches")
    val totalMatches: Int,

    @ColumnInfo(name = "completed_matches")
    val completedMatches: Int,

    @ColumnInfo(name = "walkovers")
    val walkovers: Int
)

data class CupPerformerStats(
    @ColumnInfo(name = "teamName")
    val teamName: String,

    @ColumnInfo(name = "matches_played")
    val matchesPlayed: Int,

    @ColumnInfo(name = "wins")
    val wins: Int
)

data class BracketWithDetails(
    @Embedded
    val bracket: CupBracketsEntity,

    @ColumnInfo(name = "detailed_cup_name")
    val detailedCupName: String?,

    @ColumnInfo(name = "cup_type")
    val cupType: String?,

    @ColumnInfo(name = "cup_country_id")
    val cupCountryId: Int?,

    @ColumnInfo(name = "team_logo")
    val teamLogo: String?,

    @ColumnInfo(name = "opponent_logo")
    val opponentLogo: String?,

    @ColumnInfo(name = "fixture_date")
    val fixtureDate: String?,

    @ColumnInfo(name = "fixture_stadium")
    val fixtureStadium: String?,

    @ColumnInfo(name = "fixture_status")
    val fixtureStatus: String?
)

data class BracketWithCupDetails(
    @Embedded
    val bracket: CupBracketsEntity,

    @ColumnInfo(name = "cup_logo")
    val cupLogo: String?,

    @ColumnInfo(name = "cup_prize")
    val cupPrize: Int?,

    @ColumnInfo(name = "team_logo")
    val teamLogo: String?
)
