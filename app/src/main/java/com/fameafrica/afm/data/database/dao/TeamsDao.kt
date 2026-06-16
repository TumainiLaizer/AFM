package com.fameafrica.afm.data.database.dao

import androidx.room.*
import com.fameafrica.afm.data.database.entities.TeamsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TeamsDao {

    // ============ BASIC CRUD ============

    @Query("SELECT * FROM teams ORDER BY name")
    fun getAll(): Flow<List<TeamsEntity>>

    @Query("SELECT * FROM teams WHERE id = :id")
    suspend fun getById(id: Int): TeamsEntity?

    @Query("SELECT * FROM teams WHERE id = :id")
    fun getByIdFlow(id: Int): Flow<TeamsEntity?>

    @Query("SELECT * FROM teams WHERE name = :name")
    suspend fun getByName(name: String): TeamsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(team: TeamsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(teams: List<TeamsEntity>)

    @Update
    suspend fun update(team: TeamsEntity)

    @Update
    suspend fun updateAll(teams: List<TeamsEntity>)

    @Delete
    suspend fun delete(team: TeamsEntity)

    @Query("SELECT COUNT(*) FROM teams")
    suspend fun getTotalTeamCount(): Int

    @Query("""
        UPDATE teams 
        SET board_confidence = :confidence, fan_sentiment = :sentiment, manager_security = :security 
        WHERE id = :teamId
    """)
    suspend fun updateSentiment(teamId: Int, confidence: Int, sentiment: Int, security: Int)

    @Query("UPDATE teams SET morale = morale + :change WHERE id = :teamId")
    suspend fun updateMorale(teamId: Int, change: Int)

    @Query("DELETE FROM teams")
    suspend fun deleteAll()

    @Query("SELECT * FROM teams")
    suspend fun getAllStatic(): List<TeamsEntity>

    @Query("SELECT COUNT(*) FROM teams")
    suspend fun getCount(): Int


    // ============ LEAGUE-BASED QUERIES ============

    @Query("SELECT COUNT(*) FROM teams WHERE league_id = :leagueId")
    fun getTeamsCountByLeagueId(leagueId: Int): Flow<Int>

    @Query("SELECT COUNT(*) FROM teams WHERE league = :leagueName")
    fun getTeamsCountByLeague(leagueName: String): Flow<Int>

    @Query("SELECT * FROM teams WHERE league_id = :leagueId ORDER BY points DESC")
    fun getTeamsByLeagueId(leagueId: Int): Flow<List<TeamsEntity>>

    @Query("SELECT * FROM teams WHERE league = :leagueName ORDER BY points DESC")
    fun getTeamsByLeague(leagueName: String): Flow<List<TeamsEntity>>

    @Query("SELECT * FROM teams WHERE league = :leagueName")
    suspend fun getTeamsByLeagueList(leagueName: String): List<TeamsEntity>

    @Query("SELECT * FROM teams WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<Int>): List<TeamsEntity>

    @Query("SELECT * FROM teams WHERE league = :leagueName ORDER BY elo_rating DESC")
    fun getTeamsByLeagueElo(leagueName: String): Flow<List<TeamsEntity>>

    @Query("""
        SELECT t.* 
        FROM teams t
        INNER JOIN league_standings ls ON t.id = ls.team_id 
        WHERE t.league = :leagueName AND ls.season_year = :seasonYear
        ORDER BY ls.points DESC, ls.goal_difference DESC, ls.goals_scored DESC
    """)
    fun getLeagueTable(leagueName: String, seasonYear: Int): Flow<List<TeamsEntity>>

    @Query("""
        SELECT t.* 
        FROM teams t
        INNER JOIN league_standings ls ON t.id = ls.team_id 
        WHERE t.league = :leagueName
        AND ls.season_year = (SELECT MAX(season_year) FROM league_standings WHERE league_name = :leagueName)
        ORDER BY ls.points DESC, ls.goal_difference DESC, ls.goals_scored DESC
    """)
    fun getCurrentLeagueTable(leagueName: String): Flow<List<TeamsEntity>>

    @Query("""
        SELECT 
            t.*,
            ls.position,
            ls.points as standing_points,
            ls.goal_difference,
            ls.matches_played,
            ls.wins,
            ls.draws,
            ls.losses,
            ls.goals_scored,
            ls.goals_conceded,
            ls.form
        FROM teams t
        INNER JOIN league_standings ls ON t.id = ls.team_id 
        WHERE t.league = :leagueName AND ls.season_year = :seasonYear
        ORDER BY ls.position ASC
    """)
    fun getTeamsWithStandings(leagueName: String, seasonYear: Int): Flow<List<TeamWithStanding>>

    @Query("SELECT AVG(reputation) FROM teams WHERE league = :leagueName")
    suspend fun getAverageReputationByLeague(leagueName: String): Double?

    @Query("""
        SELECT league, COUNT(*) as team_count, AVG(elo_rating) as avg_elo
        FROM teams
        GROUP BY league
        ORDER BY avg_elo DESC
    """)
    fun getLeagueStrengthRanking(): Flow<List<LeagueStrength>>

    // ============ MANAGER-BASED QUERIES ============

    @Query("SELECT * FROM teams WHERE manager_id = :managerId")
    suspend fun getTeamByManager(managerId: Int): TeamsEntity?

    @Query("SELECT * FROM teams WHERE manager_id IS NULL")
    fun getTeamsWithoutManager(): Flow<List<TeamsEntity>>

    // ============ CUP-BASED QUERIES ============

    @Query("SELECT * FROM teams WHERE cup_qualification = :cupName")
    fun getTeamsInCup(cupName: String): Flow<List<TeamsEntity>>

    @Query("SELECT * FROM teams WHERE cup_winner > 0 ORDER BY cup_winner DESC")
    fun getMostCupWinners(): Flow<List<TeamsEntity>>

    // ============ RANKINGS ============

    @Query("SELECT * FROM teams ORDER BY elo_rating DESC LIMIT :limit")
    fun getTopTeamsByElo(limit: Int): Flow<List<TeamsEntity>>

    @Query("SELECT * FROM teams ORDER BY reputation DESC LIMIT :limit")
    fun getMostReputableTeams(limit: Int): Flow<List<TeamsEntity>>

    @Query("SELECT * FROM teams ORDER BY revenue DESC LIMIT :limit")
    fun getRichestTeams(limit: Int): Flow<List<TeamsEntity>>

    @Query("SELECT * FROM teams ORDER BY fan_loyalty DESC LIMIT :limit")
    fun getTeamsWithBestFans(limit: Int): Flow<List<TeamsEntity>>

    // ============ RIVALRY QUERIES ============

    @Query("""
        SELECT * FROM teams 
        WHERE id IN (SELECT target_id FROM identity_mappings WHERE source_name = (SELECT name FROM teams WHERE id = :teamId) AND category = 'TEAM_RIVAL')
    """)
    fun getRivals(teamId: Int): Flow<List<TeamsEntity>>

    @Query("SELECT * FROM teams WHERE id = :team1Id OR id = :team2Id")
    suspend fun getDerbyTeams(team1Id: Int, team2Id: Int): List<TeamsEntity>

    // ============ SEARCH & STATS ============

    @Query("SELECT * FROM teams WHERE name LIKE '%' || :searchQuery || '%' ORDER BY name")
    fun searchTeams(searchQuery: String): Flow<List<TeamsEntity>>

    @Query("SELECT AVG(elo_rating) FROM teams WHERE league = :leagueName")
    suspend fun getAverageEloByLeague(leagueName: String): Double?

    // ============ JOIN QUERIES ============

    @Query("""
        SELECT t.*, l.name as league_name, l.country_id as league_country_id
        FROM teams t
        LEFT JOIN leagues l ON t.league_id = l.id
        WHERE t.id = :teamId
    """)
    suspend fun getTeamWithLeague(teamId: Int): TeamWithLeague?

    @Query("""
        SELECT t.*, m.name as manager_name, m.nationality as manager_nationality
        FROM teams t
        LEFT JOIN managers m ON t.manager_id = m.id
        WHERE t.id = :teamId
    """)
    suspend fun getTeamWithManager(teamId: Int): TeamWithManager?

    @Query("""
        SELECT t.*, c.name as cup_competition_name, c.type as cup_type
        FROM teams t
        LEFT JOIN cups c ON t.cup_qualification = c.name
    """)
    fun getTeamsWithCupDetails(): Flow<List<TeamWithCup>>
    @Query("UPDATE teams SET fan_loyalty = :loyalty WHERE id = :teamId")
    suspend fun updateFanLoyalty(teamId: Int, loyalty: Int)
}

// ============ DATA CLASSES FOR JOIN QUERIES ============

data class TeamWithStanding(
    @Embedded val team: TeamsEntity,
    @ColumnInfo(name = "position") val position: Int,
    @ColumnInfo(name = "standing_points") val standingPoints: Int,
    @ColumnInfo(name = "goal_difference") val goalDifference: Int,
    @ColumnInfo(name = "matches_played") val matchesPlayed: Int,
    @ColumnInfo(name = "wins") val wins: Int,
    @ColumnInfo(name = "draws") val draws: Int,
    @ColumnInfo(name = "losses") val losses: Int,
    @ColumnInfo(name = "goals_scored") val goalsScored: Int,
    @ColumnInfo(name = "goals_conceded") val goalsConceded: Int,
    @ColumnInfo(name = "form") val form: String?
)

data class TeamWithLeague(
    @Embedded val team: TeamsEntity,
    @ColumnInfo(name = "league_name") val leagueName: String,
    @ColumnInfo(name = "league_country_id") val leagueCountryId: Int?
)

data class TeamWithManager(
    @Embedded val team: TeamsEntity,
    @ColumnInfo(name = "manager_name") val managerName: String?,
    @ColumnInfo(name = "manager_nationality") val managerNationality: String?
)

data class TeamWithCup(
    @Embedded val team: TeamsEntity,
    @ColumnInfo(name = "cup_competition_name") val cupCompetitionName: String?,
    @ColumnInfo(name = "cup_type") val cupType: String?
)

data class LeagueStrength(
    @ColumnInfo(name = "league") val leagueName: String,
    @ColumnInfo(name = "team_count") val teamCount: Int,
    @ColumnInfo(name = "avg_elo") val averageElo: Double
)
