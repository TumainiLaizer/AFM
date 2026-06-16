package com.fameafrica.afm.data.database.dao

import androidx.room.*
import com.fameafrica.afm.data.database.entities.FixturesResultsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FixturesResultsDao {

    // ============ BASIC CRUD ============

    @Query("SELECT * FROM fixtures_results ORDER BY match_date DESC")
    fun getAll(): Flow<List<FixturesResultsEntity>>

    @Query("SELECT * FROM fixtures_results WHERE fixture_id = :fixtureId")
    suspend fun getByFixtureId(fixtureId: Int): FixturesResultsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(result: FixturesResultsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(results: List<FixturesResultsEntity>)

    @Update
    suspend fun update(result: FixturesResultsEntity)

    @Delete
    suspend fun delete(result: FixturesResultsEntity)

    @Query("DELETE FROM fixtures_results")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM fixtures_results")
    suspend fun getCount(): Int

    @Query("SELECT * FROM fixtures_results")
    suspend fun getAllStatic(): List<FixturesResultsEntity>

    // ============ DATE-BASED QUERIES ============

    @Query("SELECT * FROM fixtures_results WHERE date(match_date) = date(:date) ORDER BY match_date")
    fun getResultsByDate(date: String): Flow<List<FixturesResultsEntity>>

    @Query("""
        SELECT * FROM fixtures_results 
        WHERE date(match_date) BETWEEN date(:startDate) AND date(:endDate)
        ORDER BY match_date
    """)
    fun getResultsBetween(startDate: String, endDate: String): Flow<List<FixturesResultsEntity>>

    @Query("SELECT * FROM fixtures_results WHERE strftime('%Y', match_date) = :year")
    fun getResultsByYear(year: String): Flow<List<FixturesResultsEntity>>

    @Query("SELECT * FROM fixtures_results WHERE strftime('%m', match_date) = :month AND strftime('%Y', match_date) = :year")
    fun getResultsByMonth(month: String, year: String): Flow<List<FixturesResultsEntity>>

    // ============ TEAM-BASED QUERIES ============

    @Query("""
        SELECT * FROM fixtures_results 
        WHERE home_team_id = :teamId OR away_team_id = :teamId 
        ORDER BY match_date DESC
    """)
    fun getResultsByTeam(teamId: Int): Flow<List<FixturesResultsEntity>>

    @Query("""
        SELECT * FROM fixtures_results 
        WHERE (home_team_id = :team1Id AND away_team_id = :team2Id) 
        OR (home_team_id = :team2Id AND away_team_id = :team1Id)
        ORDER BY match_date DESC
    """)
    fun getHeadToHead(team1Id: Int, team2Id: Int): Flow<List<FixturesResultsEntity>>

    @Query("""
        SELECT * FROM fixtures_results 
        WHERE home_team_id = :teamId 
        ORDER BY match_date DESC
    """)
    fun getHomeResults(teamId: Int): Flow<List<FixturesResultsEntity>>

    @Query("""
        SELECT * FROM fixtures_results 
        WHERE away_team_id = :teamId 
        ORDER BY match_date DESC
    """)
    fun getAwayResults(teamId: Int): Flow<List<FixturesResultsEntity>>

    @Query("""
        SELECT * FROM fixtures_results 
        WHERE (home_team_id = :teamId AND home_score > away_score)
        OR (away_team_id = :teamId AND away_score > home_score)
        ORDER BY match_date DESC
    """)
    fun getWinsByTeam(teamId: Int): Flow<List<FixturesResultsEntity>>

    @Query("""
        SELECT * FROM fixtures_results 
        WHERE (home_team_id = :teamId AND home_score < away_score)
        OR (away_team_id = :teamId AND away_score < home_score)
        ORDER BY match_date DESC
    """)
    fun getLossesByTeam(teamId: Int): Flow<List<FixturesResultsEntity>>

    @Query("""
        SELECT * FROM fixtures_results 
        WHERE (home_team_id = :teamId OR away_team_id = :teamId)
        AND home_score = away_score
        AND home_penalty_score IS NULL
        ORDER BY match_date DESC
    """)
    fun getDrawsByTeam(teamId: Int): Flow<List<FixturesResultsEntity>>

    // ============ LEAGUE-BASED QUERIES ============

    @Query("""
        SELECT * FROM fixtures_results 
        WHERE league_name = :leagueName 
        AND season = :season
        ORDER BY match_date
    """)
    fun getLeagueResults(leagueName: String, season: String): Flow<List<FixturesResultsEntity>>

    @Query("""
        SELECT * FROM fixtures_results 
        WHERE league_name = :leagueName 
        AND season = :season
        AND (home_team_id = :teamId OR away_team_id = :teamId)
        ORDER BY match_date
    """)
    fun getTeamLeagueResults(leagueName: String, season: String, teamId: Int): Flow<List<FixturesResultsEntity>>

    // ============ CUP-BASED QUERIES ============

    @Query("""
        SELECT * FROM fixtures_results 
        WHERE cup_name = :cupName 
        AND season = :season
        ORDER BY match_date
    """)
    fun getCupResults(cupName: String, season: String): Flow<List<FixturesResultsEntity>>

    @Query("""
        SELECT * FROM fixtures_results 
        WHERE cup_name = :cupName 
        AND season = :season
        AND cup_round = :round
        ORDER BY match_date
    """)
    fun getCupRoundResults(cupName: String, season: String, round: String): Flow<List<FixturesResultsEntity>>

    // ============ REFEREE-BASED QUERIES ============

    @Query("SELECT * FROM fixtures_results WHERE referee_id = :refereeId ORDER BY match_date DESC")
    fun getResultsByReferee(refereeId: Int): Flow<List<FixturesResultsEntity>>

    // ============ PLAYER-BASED QUERIES ============

    @Query("SELECT * FROM fixtures_results WHERE man_of_match_id = :playerId ORDER BY match_date DESC")
    fun getResultsByManOfMatch(playerId: Int): Flow<List<FixturesResultsEntity>>

    // ============ STATISTICS QUERIES ============

    @Query("""
        SELECT 
            strftime('%m', match_date) as month,
            COUNT(*) as matches,
            AVG(home_score + away_score) as avg_goals
        FROM fixtures_results 
        WHERE strftime('%Y', match_date) = :year
        GROUP BY month
        ORDER BY month
    """)
    fun getMonthlyStats(year: String): Flow<List<MonthlyStatistics>>

    // ============ JOIN QUERIES ============

    @Query("""
        SELECT 
            fr.*,
            ht.logo_path as h_team_logo,
            ht.league as h_team_league,
            ht.elo_rating as h_team_current_elo,
            at.logo_path as a_team_logo,
            at.league as a_team_league,
            at.elo_rating as a_team_current_elo,
            r.name as ref_name,
            r.strictness as ref_strictness,
            r.bias as ref_bias,
            n.nationality as ref_nationality,
            p.name as mom_player_name,
            p.position as mom_position,
            p.rating as mom_rating_val
        FROM fixtures_results fr
        LEFT JOIN teams ht ON fr.home_team_id = ht.id
        LEFT JOIN teams at ON fr.away_team_id = at.id
        LEFT JOIN referees r ON fr.referee_id = r.referee_id
        LEFT JOIN nationalities n ON r.nationality_id = n.id
        LEFT JOIN players p ON fr.man_of_match_id = p.id
        WHERE fr.fixture_id = :fixtureId
    """)
    suspend fun getCompleteResultDetails(fixtureId: Int): CompleteResultDetails?
}

// ============ DATA CLASSES FOR STATISTICS ============

data class MonthlyStatistics(
    @ColumnInfo(name = "month")
    val month: String,

    @ColumnInfo(name = "matches")
    val matchesPlayed: Int,

    @ColumnInfo(name = "avg_goals")
    val averageGoals: Double?
)

// ============ DATA CLASSES FOR JOIN QUERIES ============

data class CompleteResultDetails(
    @Embedded
    val result: FixturesResultsEntity,

    @ColumnInfo(name = "h_team_logo")
    val homeTeamLogo: String?,

    @ColumnInfo(name = "h_team_league")
    val homeTeamLeague: String?,

    @ColumnInfo(name = "h_team_current_elo")
    val homeTeamCurrentElo: Int?,

    @ColumnInfo(name = "a_team_logo")
    val awayTeamLogo: String?,

    @ColumnInfo(name = "a_team_league")
    val awayTeamLeague: String?,

    @ColumnInfo(name = "a_team_current_elo")
    val awayTeamCurrentElo: Int?,

    @ColumnInfo(name = "ref_name")
    val refereeName: String?,

    @ColumnInfo(name = "ref_strictness")
    val refereeStrictness: Int?,

    @ColumnInfo(name = "ref_bias")
    val refereeBias: Int?,

    @ColumnInfo(name = "ref_nationality")
    val refereeNationality: String?,

    @ColumnInfo(name = "mom_player_name")
    val manOfMatchPlayerName: String?,

    @ColumnInfo(name = "mom_position")
    val manOfMatchPosition: String?,

    @ColumnInfo(name = "mom_rating_val")
    val manOfMatchRatingValue: Int?
)
