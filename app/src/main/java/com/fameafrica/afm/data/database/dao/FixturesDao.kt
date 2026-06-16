package com.fameafrica.afm.data.database.dao

import androidx.room.*
import com.fameafrica.afm.data.database.entities.FixturesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FixturesDao {

    // ============ BASIC CRUD ============

    @Query("SELECT * FROM fixtures ORDER BY match_date DESC")
    fun getAll(): Flow<List<FixturesEntity>>

    @Query("SELECT * FROM fixtures WHERE id = :id")
    suspend fun getById(id: Int): FixturesEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(fixture: FixturesEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(fixtures: List<FixturesEntity>): List<Long>

    @Update
    suspend fun update(fixture: FixturesEntity)

    @Update
    suspend fun updateAll(fixtures: List<FixturesEntity>)

    @Delete
    suspend fun delete(fixture: FixturesEntity)

    @Query("DELETE FROM fixtures")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM fixtures")
    suspend fun getCount(): Int

    // ============ DATE-BASED QUERIES ============

    @Query("SELECT * FROM fixtures WHERE date(match_date) = date(:date) ORDER BY match_date")
    fun getFixturesByDate(date: String): Flow<List<FixturesEntity>>

    @Query("""
        SELECT * FROM fixtures 
        WHERE date(match_date) <= date(:currentDate) 
        AND (match_status = 'SCHEDULED' OR match_status = 'LIVE')
        ORDER BY match_date ASC
    """)
    fun getFixturesToSimulate(currentDate: String): Flow<List<FixturesEntity>>

    @Query("""
        SELECT * FROM fixtures 
        WHERE date(match_date) <= date(:currentDate) 
        AND (match_status = 'SCHEDULED' OR match_status = 'LIVE')
        ORDER BY match_date ASC
    """)
    suspend fun getFixturesToSimulateStatic(currentDate: String): List<FixturesEntity>

    @Query("""
        SELECT * FROM fixtures 
        WHERE date(match_date) >= date(:currentDate) 
        AND match_status IN ('SCHEDULED', 'LIVE')
        ORDER BY match_date ASC
    """)
    fun getUpcomingFixtures(currentDate: String): Flow<List<FixturesEntity>>

    @Query("""
        SELECT * FROM fixtures 
        WHERE date(match_date) >= date(:currentDate) 
        AND match_status IN ('SCHEDULED', 'LIVE')
        ORDER BY match_date ASC
        LIMIT :limit
    """)
    fun getUpcomingFixturesLimit(currentDate: String, limit: Int): Flow<List<FixturesEntity>>

    @Query("""
        SELECT * FROM fixtures 
        WHERE date(match_date) < date(:currentDate) 
        AND match_status = 'COMPLETED'
        ORDER BY match_date DESC
        LIMIT :limit
    """)
    fun getRecentFixtures(currentDate: String, limit: Int): Flow<List<FixturesEntity>>

    @Query("""
        SELECT * FROM fixtures 
        WHERE date(match_date) BETWEEN date(:startDate) AND date(:endDate)
        ORDER BY match_date
    """)
    fun getFixturesBetween(startDate: String, endDate: String): Flow<List<FixturesEntity>>

    // ============ TEAM-BASED QUERIES ============

    @Query("""
        SELECT * FROM fixtures 
        WHERE home_team_id = :teamId OR away_team_id = :teamId 
        ORDER BY match_date DESC
    """)
    fun getFixturesByTeam(teamId: Int): Flow<List<FixturesEntity>>

    @Query("""
        SELECT * FROM fixtures 
        WHERE (home_team_id = :teamId OR away_team_id = :teamId)
        AND date(match_date) >= date(:currentDate)
        AND match_status IN ('SCHEDULED', 'LIVE')
        ORDER BY match_date ASC
    """)
    fun getUpcomingFixturesByTeam(teamId: Int, currentDate: String): Flow<List<FixturesEntity>>

    @Query("""
        SELECT * FROM fixtures 
        WHERE (home_team_id = :teamId OR away_team_id = :teamId)
        AND date(match_date) < date(:currentDate)
        AND match_status = 'COMPLETED'
        ORDER BY match_date DESC
        LIMIT :limit
    """)
    fun getRecentResultsByTeam(teamId: Int, currentDate: String, limit: Int = 5): Flow<List<FixturesEntity>>

    @Query("""
        SELECT * FROM fixtures 
        WHERE (home_team_id = :team1Id AND away_team_id = :team2Id) 
        OR (home_team_id = :team2Id AND away_team_id = :team1Id)
        ORDER BY match_date DESC
    """)
    fun getHeadToHead(team1Id: Int, team2Id: Int): Flow<List<FixturesEntity>>

    // ============ LEAGUE-BASED QUERIES ============

    @Query("""
        SELECT * FROM fixtures 
        WHERE league_id = :leagueId 
        AND season = :season
        ORDER BY position, match_date
    """)
    fun getLeagueFixturesById(leagueId: Int, season: String): Flow<List<FixturesEntity>>

    @Query("""
        SELECT * FROM fixtures 
        WHERE league = :leagueName 
        AND season = :season
        ORDER BY position, match_date
    """)
    fun getLeagueFixtures(leagueName: String, season: String): Flow<List<FixturesEntity>>

    @Query("""
        SELECT * FROM fixtures 
        WHERE league = :leagueName 
        AND season = :season
        AND date(match_date) >= date(:currentDate)
        AND match_status = 'SCHEDULED'
        ORDER BY match_date ASC
    """)
    fun getUpcomingLeagueFixtures(leagueName: String, season: String, currentDate: String): Flow<List<FixturesEntity>>

    @Query("""
        SELECT * FROM fixtures 
        WHERE league = :leagueName 
        AND season = :season
        AND match_status = 'COMPLETED'
        ORDER BY position, match_date DESC
    """)
    fun getCompletedLeagueFixtures(leagueName: String, season: String): Flow<List<FixturesEntity>>

    @Query("""
        SELECT * FROM fixtures 
        WHERE league = :leagueName 
        AND season = :season
        AND position = :gameWeek
        ORDER BY match_date
    """)
    fun getLeagueFixturesByRound(leagueName: String, season: String, gameWeek: Int): Flow<List<FixturesEntity>>

    // ============ CUP-BASED QUERIES ============

    @Query("""
        SELECT * FROM fixtures 
        WHERE cup_id = :cupId 
        AND season = :season
        ORDER BY 
            CASE 
                WHEN round = 'FINAL' OR round = 'Final' THEN 1
                WHEN round = 'SEMI_FINAL' OR round = 'Semi-final' THEN 2
                WHEN round = 'QUARTER_FINAL' OR round = 'Quarter-final' THEN 3
                WHEN round = 'ROUND_OF_16' OR round = 'Round of 16' THEN 4
                WHEN round = 'ROUND_OF_32' OR round = 'Round of 32' THEN 5
                WHEN round = 'ROUND_OF_64' OR round = 'Round of 64' THEN 6
                ELSE 7
            END,
            match_date
    """)
    fun getCupFixturesById(cupId: Int, season: String): Flow<List<FixturesEntity>>

    @Query("""
        SELECT * FROM fixtures 
        WHERE cup_name = :cupName 
        AND season = :season
        ORDER BY 
            CASE 
                WHEN round = 'FINAL' OR round = 'Final' THEN 1
                WHEN round = 'SEMI_FINAL' OR round = 'Semi-final' THEN 2
                WHEN round = 'QUARTER_FINAL' OR round = 'Quarter-final' THEN 3
                WHEN round = 'ROUND_OF_16' OR round = 'Round of 16' THEN 4
                WHEN round = 'ROUND_OF_32' OR round = 'Round of 32' THEN 5
                WHEN round = 'ROUND_OF_64' OR round = 'Round of 64' THEN 6
                ELSE 7
            END,
            match_date
    """)
    fun getCupFixtures(cupName: String, season: String): Flow<List<FixturesEntity>>

    @Query("""
        SELECT * FROM fixtures 
        WHERE cup_name = :cupName 
        AND season = :season
        AND round = :round
        ORDER BY match_date
    """)
    fun getCupFixturesByRound(cupName: String, season: String, round: String): Flow<List<FixturesEntity>>

    // ============ STATUS-BASED QUERIES ============

    @Query("SELECT * FROM fixtures WHERE match_status = :status ORDER BY match_date")
    fun getFixturesByStatus(status: String): Flow<List<FixturesEntity>>

    @Query("SELECT * FROM fixtures WHERE match_status = 'LIVE'")
    fun getLiveFixtures(): Flow<List<FixturesEntity>>

    @Query("SELECT * FROM fixtures WHERE match_status = 'SCHEDULED' AND date(match_date) = date(:currentDate)")
    fun getTodaysFixtures(currentDate: String): Flow<List<FixturesEntity>>

    // ============ SEASON-BASED QUERIES ============

    @Query("SELECT DISTINCT season FROM fixtures ORDER BY season DESC")
    fun getSeasons(): Flow<List<String>>

    @Query("SELECT * FROM fixtures WHERE season = :season ORDER BY match_date")
    fun getFixturesBySeason(season: String): Flow<List<FixturesEntity>>

    // ============ TYPE-BASED QUERIES ============

    @Query("SELECT * FROM fixtures WHERE match_type = :matchType ORDER BY match_date DESC")
    fun getFixturesByType(matchType: String): Flow<List<FixturesEntity>>

    // ============ STATISTICS QUERIES ============

    @Query("""
        SELECT * FROM fixtures 
        WHERE (home_team_id = :teamId OR away_team_id = :teamId)
        AND match_status = 'COMPLETED'
        ORDER BY match_date DESC
        LIMIT 5
    """)
    suspend fun getTeamRecentForm(teamId: Int): List<FixturesEntity>

    @Query("""
        SELECT * FROM fixtures 
        WHERE home_team_id = :teamId
        AND match_status = 'COMPLETED'
        ORDER BY match_date DESC
    """)
    fun getTeamHomeFixtures(teamId: Int): Flow<List<FixturesEntity>>

    @Query("""
        SELECT * FROM fixtures 
        WHERE away_team_id = :teamId
        AND match_status = 'COMPLETED'
        ORDER BY match_date DESC
    """)
    fun getTeamAwayFixtures(teamId: Int): Flow<List<FixturesEntity>>

    @Query("""
        SELECT * FROM fixtures 
        WHERE season = :season 
        AND strftime('%m', match_date) = :month 
        AND match_status = 'COMPLETED'
    """)
    suspend fun getCompletedFixturesByMonth(season: String, month: String): List<FixturesEntity>

    // ============ JOIN QUERIES ============

    @Query("""
        SELECT 
            f.*,
            ht.name as h_team_name,
            ht.logo_path as h_team_logo,
            ht.league as h_team_league,
            at.name as a_team_name,
            at.logo_path as a_team_logo,
            at.league as a_team_league
        FROM fixtures f
        LEFT JOIN teams ht ON f.home_team_id = ht.id
        LEFT JOIN teams at ON f.away_team_id = at.id
        WHERE f.id = :fixtureId
    """)
    suspend fun getFixtureWithTeams(fixtureId: Int): FixtureWithTeams?

    @Query("""
        SELECT 
            f.*,
            r.name as ref_name,
            r.strictness as ref_strictness,
            r.bias as ref_bias,
            r.rating as ref_rating,
            n.nationality as ref_nationality,
            n.fifa_code as ref_fifa_code
        FROM fixtures f
        LEFT JOIN referees r ON f.referee_id = r.referee_id
        LEFT JOIN nationalities n ON r.nationality_id = n.id
        WHERE f.id = :fixtureId
    """)
    suspend fun getFixtureWithReferee(fixtureId: Int): FixtureWithReferee?

    @Query("""
        SELECT 
            f.*,
            l.name as l_name,
            l.level as l_level,
            l.country_id as l_country_id,
            c.name as c_name,
            c.type as c_type,
            c.country_id as c_country_id
        FROM fixtures f
        LEFT JOIN leagues l ON f.league_id = l.id
        LEFT JOIN cups c ON f.cup_id = c.id
        WHERE f.id = :fixtureId
    """)
    suspend fun getFixtureWithCompetition(fixtureId: Int): FixtureWithCompetition?

    @Query("""
        SELECT 
            f.*,
            ht.name as h_team_name,
            ht.logo_path as h_team_logo,
            ht.league as h_team_league,
            ht.elo_rating as h_team_elo,
            at.name as a_team_name,
            at.logo_path as a_team_logo,
            at.league as a_team_league,
            at.elo_rating as a_team_elo,
            r.name as ref_name,
            r.strictness as ref_strictness,
            r.bias as ref_bias,
            r.rating as ref_rating,
            n.nationality as ref_nationality,
            l.name as l_name,
            l.level as l_level,
            c.name as c_comp_name,
            c.type as c_comp_type
        FROM fixtures f
        LEFT JOIN teams ht ON f.home_team_id = ht.id
        LEFT JOIN teams at ON f.away_team_id = at.id
        LEFT JOIN referees r ON f.referee_id = r.referee_id
        LEFT JOIN nationalities n ON r.nationality_id = n.id
        LEFT JOIN leagues l ON f.league_id = l.id
        LEFT JOIN cups c ON f.cup_id = c.id
        WHERE f.id = :fixtureId
    """)
    suspend fun getCompleteFixtureDetails(fixtureId: Int): CompleteFixtureDetails?
    @Query("SELECT * FROM fixtures ORDER BY match_date ASC")
    suspend fun getAllStatic(): List<FixturesEntity>
}

data class FixtureWithTeams(
    @Embedded val fixture: FixturesEntity,
    @ColumnInfo(name = "h_team_name") val homeTeamName: String?,
    @ColumnInfo(name = "h_team_logo") val homeTeamLogo: String?,
    @ColumnInfo(name = "h_team_league") val homeTeamLeague: String?,
    @ColumnInfo(name = "a_team_name") val awayTeamName: String?,
    @ColumnInfo(name = "a_team_logo") val awayTeamLogo: String?,
    @ColumnInfo(name = "a_team_league") val awayTeamLeague: String?
)

data class FixtureWithReferee(
    @Embedded val fixture: FixturesEntity,
    @ColumnInfo(name = "ref_name") val refereeName: String?,
    @ColumnInfo(name = "ref_strictness") val refereeStrictness: Int?,
    @ColumnInfo(name = "ref_bias") val refereeBias: Int?,
    @ColumnInfo(name = "ref_rating") val refereeRating: Int?,
    @ColumnInfo(name = "ref_nationality") val refereeNationality: String?,
    @ColumnInfo(name = "ref_fifa_code") val refereeFifaCode: String?
)

data class FixtureWithCompetition(
    @Embedded val fixture: FixturesEntity,
    @ColumnInfo(name = "l_name") val leagueName: String?,
    @ColumnInfo(name = "l_level") val leagueLevel: Int?,
    @ColumnInfo(name = "l_country_id") val leagueCountryId: Int?,
    @ColumnInfo(name = "c_name") val cupCompetitionName: String?,
    @ColumnInfo(name = "c_type") val cupCompetitionType: String?,
    @ColumnInfo(name = "c_country_id") val cupCountryId: Int?
)

data class CompleteFixtureDetails(
    @Embedded val fixture: FixturesEntity,
    @ColumnInfo(name = "h_team_name") val homeTeamName: String?,
    @ColumnInfo(name = "h_team_logo") val homeTeamLogo: String?,
    @ColumnInfo(name = "h_team_league") val homeTeamLeague: String?,
    @ColumnInfo(name = "h_team_elo") val homeTeamElo: Int?,
    @ColumnInfo(name = "a_team_name") val awayTeamName: String?,
    @ColumnInfo(name = "a_team_logo") val awayTeamLogo: String?,
    @ColumnInfo(name = "a_team_league") val awayTeamLeague: String?,
    @ColumnInfo(name = "a_team_elo") val awayTeamElo: Int?,
    @ColumnInfo(name = "ref_name") val refereeName: String?,
    @ColumnInfo(name = "ref_strictness") val refereeStrictness: Int?,
    @ColumnInfo(name = "ref_bias") val refereeBias: Int?,
    @ColumnInfo(name = "ref_rating") val refereeRating: Int?,
    @ColumnInfo(name = "ref_nationality") val refereeNationality: String?,
    @ColumnInfo(name = "l_name") val leagueCompetitionName: String?,
    @ColumnInfo(name = "l_level") val leagueLevel: Int?,
    @ColumnInfo(name = "c_comp_name") val cupCompetitionName: String?,
    @ColumnInfo(name = "c_comp_type") val cupCompetitionType: String?
)
