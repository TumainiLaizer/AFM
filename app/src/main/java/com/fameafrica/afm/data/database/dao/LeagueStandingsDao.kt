package com.fameafrica.afm.data.database.dao

import androidx.room.*
import com.fameafrica.afm.data.database.entities.LeagueStandingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LeagueStandingsDao {

    // ============ BASIC CRUD ============

    @Query("SELECT * FROM league_standings")
    fun getAll(): Flow<List<LeagueStandingsEntity>>

    @Query("SELECT * FROM league_standings WHERE id = :id")
    suspend fun getById(id: Int): LeagueStandingsEntity?

    @Query("SELECT * FROM league_standings WHERE team_id = :teamId AND league_name = :leagueName AND season_year = :seasonYear")
    suspend fun getTeamStanding(teamId: Int, leagueName: String, seasonYear: Int): LeagueStandingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(standing: LeagueStandingsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(standings: List<LeagueStandingsEntity>)

    @Update
    suspend fun update(standing: LeagueStandingsEntity)

    @Update
    suspend fun updateAll(standings: List<LeagueStandingsEntity>)

    @Delete
    suspend fun delete(standing: LeagueStandingsEntity)

    @Query("DELETE FROM league_standings WHERE league_name = :leagueName AND season_year = :seasonYear")
    suspend fun deleteByLeagueAndSeason(leagueName: String, seasonYear: Int)

    @Query("DELETE FROM league_standings")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM league_standings WHERE league_name = :leagueName AND season_year = :seasonYear")
    suspend fun getTeamCount(leagueName: String, seasonYear: Int): Int

    @Query("SELECT * FROM league_standings")
    suspend fun getAllStatic(): List<LeagueStandingsEntity>

    @Query("SELECT * FROM league_standings WHERE season_year = :seasonYear")
    suspend fun getAllStaticBySeason(seasonYear: Int): List<LeagueStandingsEntity>

    // ============ STANDINGS QUERIES ============

    @Query("""
        SELECT * FROM league_standings 
        WHERE league_name = :leagueName AND season_year = :seasonYear 
        ORDER BY points DESC, goal_difference DESC, goals_scored DESC
    """)
    fun getStandings(leagueName: String, seasonYear: Int): Flow<List<LeagueStandingsEntity>>

    @Query("""
        SELECT * FROM league_standings 
        WHERE league_name = :leagueName AND season_year = :seasonYear 
        ORDER BY position ASC
    """)
    fun getStandingsByPosition(leagueName: String, seasonYear: Int): Flow<List<LeagueStandingsEntity>>

    @Query("""
        SELECT * FROM league_standings 
        WHERE league_name = :leagueName AND season_year = :seasonYear 
        AND position <= :topN
        ORDER BY position ASC
    """)
    fun getTopN(leagueName: String, seasonYear: Int, topN: Int): Flow<List<LeagueStandingsEntity>>

    @Query("""
        SELECT * FROM league_standings 
        WHERE league_name = :leagueName AND season_year = :seasonYear 
        AND position >= :bottomStart
        ORDER BY position ASC
    """)
    fun getBottomN(leagueName: String, seasonYear: Int, bottomStart: Int): Flow<List<LeagueStandingsEntity>>

    @Query("""
        SELECT * FROM league_standings 
        WHERE league_name = :leagueName AND season_year = :seasonYear 
        AND team_id = :teamId
    """)
    suspend fun getTeamPosition(leagueName: String, seasonYear: Int, teamId: Int): LeagueStandingsEntity?

    @Query("""
        SELECT * FROM league_standings 
        WHERE league_name = :leagueName AND season_year = :seasonYear 
        AND points = (SELECT MAX(points) FROM league_standings WHERE league_name = :leagueName AND season_year = :seasonYear)
    """)
    suspend fun getLeagueLeader(leagueName: String, seasonYear: Int): LeagueStandingsEntity?

    @Query("""
        SELECT * FROM league_standings 
        WHERE league_name = :leagueName AND season_year = :seasonYear 
        ORDER BY points DESC, goal_difference DESC 
        LIMIT 1
    """)
    suspend fun getChampion(leagueName: String, seasonYear: Int): LeagueStandingsEntity?

    // ============ TEAM HISTORY QUERIES ============

    @Query("""
        SELECT * FROM league_standings 
        WHERE team_id = :teamId 
        ORDER BY season_year DESC
    """)
    fun getTeamHistory(teamId: Int): Flow<List<LeagueStandingsEntity>>

    @Query("""
        SELECT * FROM league_standings 
        WHERE team_id = :teamId 
        AND position = 1
        ORDER BY season_year DESC
    """)
    fun getTeamTitles(teamId: Int): Flow<List<LeagueStandingsEntity>>

    @Query("""
        SELECT COUNT(*) FROM league_standings 
        WHERE team_id = :teamId AND position = 1
    """)
    suspend fun getTeamTitleCount(teamId: Int): Int

    // ============ STATISTICS QUERIES ============

    @Query("""
        SELECT 
            AVG(points) as avg_points,
            AVG(goals_scored) as avg_goals_for,
            AVG(goals_conceded) as avg_goals_against,
            AVG(goal_difference) as avg_goal_diff,
            SUM(wins) as total_wins,
            SUM(draws) as total_draws,
            SUM(losses) as total_losses
        FROM league_standings 
        WHERE league_name = :leagueName AND season_year = :seasonYear
    """)
    suspend fun getLeagueStatistics(leagueName: String, seasonYear: Int): LeagueStatistics?

    @Query("""
        SELECT 
            team_name,
            SUM(wins) as total_wins,
            SUM(draws) as total_draws,
            SUM(losses) as total_losses,
            SUM(goals_scored) as total_goals_for,
            SUM(goals_conceded) as total_goals_against,
            AVG(position) as avg_position,
            COUNT(CASE WHEN position = 1 THEN 1 END) as championships
        FROM league_standings 
        WHERE team_id = :teamId
        GROUP BY team_name
    """)
    suspend fun getTeamAllTimeStats(teamId: Int): TeamAllTimeStats?

    @Query("""
        SELECT 
            season_year,
            AVG(points) as avg_points,
            AVG(goals_scored) as avg_goals
        FROM league_standings 
        WHERE league_name = :leagueName
        GROUP BY season_year
        ORDER BY season_year DESC
    """)
    fun getLeagueTrends(leagueName: String): Flow<List<LeagueTrend>>

    // ============ FORM QUERIES ============

    @Query("""
        SELECT * FROM league_standings 
        WHERE league_name = :leagueName AND season_year = :seasonYear 
        AND form LIKE '%W%W%'  -- Teams on winning streaks
        ORDER BY points DESC
    """)
    fun getTeamsOnWinningStreak(leagueName: String, seasonYear: Int): Flow<List<LeagueStandingsEntity>>

    @Query("""
        SELECT * FROM league_standings 
        WHERE league_name = :leagueName AND season_year = :seasonYear 
        AND form LIKE '%L%L%'  -- Teams on losing streaks
        ORDER BY points ASC
    """)
    fun getTeamsOnLosingStreak(leagueName: String, seasonYear: Int): Flow<List<LeagueStandingsEntity>>

    @Query("""
        SELECT * FROM league_standings 
        WHERE league_name = :leagueName AND season_year = :seasonYear 
        AND form LIKE '%D%D%'  -- Teams on drawing streaks
        ORDER BY points DESC
    """)
    fun getTeamsOnDrawingStreak(leagueName: String, seasonYear: Int): Flow<List<LeagueStandingsEntity>>

    @Query("""
        SELECT * FROM league_standings 
        WHERE league_name = :leagueName AND season_year = :seasonYear 
        AND form NOT LIKE '%L%'  -- Undefeated teams
        ORDER BY points DESC
    """)
    fun getUndefeatedTeams(leagueName: String, seasonYear: Int): Flow<List<LeagueStandingsEntity>>

    @Query("""
        SELECT * FROM league_standings 
        WHERE league_name = :leagueName AND season_year = :seasonYear 
        AND form NOT LIKE '%W%'  -- Winless teams
        ORDER BY points ASC
    """)
    fun getWinlessTeams(leagueName: String, seasonYear: Int): Flow<List<LeagueStandingsEntity>>

    // ============ PROMOTION/RELEGATION ============

    @Query("""
        SELECT * FROM league_standings 
        WHERE league_name = :leagueName AND season_year = :seasonYear 
        AND position <= 2
        ORDER BY position ASC
    """)
    fun getPromotionSpots(leagueName: String, seasonYear: Int): Flow<List<LeagueStandingsEntity>>

    @Query("""
        SELECT * FROM league_standings 
        WHERE league_name = :leagueName AND season_year = :seasonYear 
        AND position BETWEEN 3 AND 4
        ORDER BY position ASC
    """)
    fun getPlayoffSpots(leagueName: String, seasonYear: Int): Flow<List<LeagueStandingsEntity>>

    @Query("""
        SELECT * FROM league_standings 
        WHERE league_name = :leagueName AND season_year = :seasonYear 
        AND position >= 14
        ORDER BY position ASC
    """)
    fun getRelegationSpots(leagueName: String, seasonYear: Int): Flow<List<LeagueStandingsEntity>>

    // ============ JOIN QUERIES ============

    @Query("""
        SELECT 
            ls.*,
            t.logo_path as team_logo,
            t.home_stadium as team_stadium,
            t.elo_rating as team_elo,
            l.country as league_country,
            l.level as league_level
        FROM league_standings ls
        LEFT JOIN teams t ON ls.team_id = t.id
        LEFT JOIN leagues l ON ls.league_name = l.name
        WHERE ls.league_name = :leagueName AND ls.season_year = :seasonYear
        ORDER BY ls.position ASC
    """)
    fun getFullStandings(leagueName: String, seasonYear: Int): Flow<List<FullStandingEntry>>

    @Query("""
        SELECT 
            ls.*,
            t.logo_path as team_logo,
            t.manager_id,
            m.name as manager_name
        FROM league_standings ls
        LEFT JOIN teams t ON ls.team_id = t.id
        LEFT JOIN managers m ON t.manager_id = m.id
        WHERE ls.team_id = :teamId AND ls.season_year = :seasonYear
    """)
    suspend fun getTeamStandingWithDetails(teamId: Int, seasonYear: Int): TeamStandingWithDetails?

    @Query("""
        SELECT * FROM league_standings 
        WHERE league_name IN (:leagueNames) AND season_year = :seasonYear
    """)
    suspend fun getStandingsByLeagues(leagueNames: List<String>, seasonYear: Int): List<LeagueStandingsEntity>

    @Query("""
        SELECT * FROM league_standings 
        WHERE league_name = :leagueName AND season_year = :seasonYear
    """)
    suspend fun getStandingsByLeagueStatic(leagueName: String, seasonYear: Int): List<LeagueStandingsEntity>
}

// ============ DATA CLASSES FOR COMPLEX QUERIES ============

data class LeagueStatistics(
    @ColumnInfo(name = "avg_points")
    val averagePoints: Double,

    @ColumnInfo(name = "avg_goals_for")
    val averageGoalsFor: Double,

    @ColumnInfo(name = "avg_goals_against")
    val averageGoalsAgainst: Double,

    @ColumnInfo(name = "avg_goal_diff")
    val averageGoalDifference: Double,

    @ColumnInfo(name = "total_wins")
    val totalWins: Int,

    @ColumnInfo(name = "total_draws")
    val totalDraws: Int,

    @ColumnInfo(name = "total_losses")
    val totalLosses: Int
)

data class TeamAllTimeStats(
    @ColumnInfo(name = "team_name")
    val teamName: String,

    @ColumnInfo(name = "total_wins")
    val totalWins: Int,

    @ColumnInfo(name = "total_draws")
    val totalDraws: Int,

    @ColumnInfo(name = "total_losses")
    val totalLosses: Int,

    @ColumnInfo(name = "total_goals_for")
    val totalGoalsFor: Int,

    @ColumnInfo(name = "total_goals_against")
    val totalGoalsAgainst: Int,

    @ColumnInfo(name = "avg_position")
    val averagePosition: Double,

    @ColumnInfo(name = "championships")
    val championships: Int
)

data class LeagueTrend(
    @ColumnInfo(name = "season_year")
    val seasonYear: Int,

    @ColumnInfo(name = "avg_points")
    val averagePoints: Double,

    @ColumnInfo(name = "avg_goals")
    val averageGoals: Double
)

data class FullStandingEntry(
    @Embedded
    val standing: LeagueStandingsEntity,

    @ColumnInfo(name = "team_logo")
    val teamLogo: String?,

    @ColumnInfo(name = "team_stadium")
    val teamStadium: String?,

    @ColumnInfo(name = "team_elo")
    val teamElo: Int?,

    @ColumnInfo(name = "league_country")
    val leagueCountry: String?,

    @ColumnInfo(name = "league_level")
    val leagueLevel: Int?
)

data class TeamStandingWithDetails(
    @Embedded
    val standing: LeagueStandingsEntity,

    @ColumnInfo(name = "team_logo")
    val teamLogo: String?,

    @ColumnInfo(name = "manager_id")
    val managerId: Int?,

    @ColumnInfo(name = "manager_name")
    val managerName: String?
)