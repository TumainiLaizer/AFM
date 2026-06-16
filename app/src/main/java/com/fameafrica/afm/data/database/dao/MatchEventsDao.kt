package com.fameafrica.afm.data.database.dao

import androidx.room.*
import com.fameafrica.afm.data.database.entities.MatchEventsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MatchEventsDao {

    // ============ BASIC CRUD ============

    @Query("SELECT * FROM match_events ORDER BY match_id, minute")
    fun getAll(): Flow<List<MatchEventsEntity>>

    @Query("SELECT * FROM match_events WHERE event_id = :eventId")
    suspend fun getById(eventId: Int): MatchEventsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: MatchEventsEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<MatchEventsEntity>)

    @Update
    suspend fun update(event: MatchEventsEntity)

    @Delete
    suspend fun delete(event: MatchEventsEntity)

    @Query("DELETE FROM match_events WHERE match_id = :matchId")
    suspend fun deleteByMatch(matchId: Int)

    @Query("DELETE FROM match_events")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM match_events")
    suspend fun getCount(): Int

    @Query("SELECT * FROM match_events")
    suspend fun getAllStatic(): List<MatchEventsEntity>

    // ============ MATCH-BASED QUERIES ============

    @Query("SELECT * FROM match_events WHERE match_id = :matchId ORDER BY minute, timestamp")
    fun getEventsByMatch(matchId: Int): Flow<List<MatchEventsEntity>>

    @Query("SELECT * FROM match_events WHERE match_id = :matchId AND event_type IN (:eventTypes) ORDER BY minute")
    fun getEventsByType(matchId: Int, eventTypes: List<String>): Flow<List<MatchEventsEntity>>

    @Query("SELECT * FROM match_events WHERE match_id = :matchId AND event_type = 'GOAL' ORDER BY minute")
    fun getGoalsByMatch(matchId: Int): Flow<List<MatchEventsEntity>>

    @Query("SELECT * FROM match_events WHERE match_id = :matchId AND event_type = 'YELLOW_CARD' ORDER BY minute")
    fun getYellowCardsByMatch(matchId: Int): Flow<List<MatchEventsEntity>>

    @Query("SELECT * FROM match_events WHERE match_id = :matchId AND event_type = 'RED_CARD' ORDER BY minute")
    fun getRedCardsByMatch(matchId: Int): Flow<List<MatchEventsEntity>>

    @Query("SELECT * FROM match_events WHERE match_id = :matchId AND event_type = 'SUBSTITUTION' ORDER BY minute")
    fun getSubstitutionsByMatch(matchId: Int): Flow<List<MatchEventsEntity>>

    @Query("SELECT * FROM match_events WHERE match_id = :matchId AND event_type = 'PENALTY_SCORED' ORDER BY minute")
    fun getPenaltiesByMatch(matchId: Int): Flow<List<MatchEventsEntity>>

    @Query("SELECT * FROM match_events WHERE match_id = :matchId AND event_type = 'VAR' ORDER BY minute")
    fun getVarEventsByMatch(matchId: Int): Flow<List<MatchEventsEntity>>

    @Query("SELECT * FROM match_events WHERE match_id = :matchId AND minute BETWEEN :startMinute AND :endMinute ORDER BY minute")
    fun getEventsInTimeRange(matchId: Int, startMinute: Int, endMinute: Int): Flow<List<MatchEventsEntity>>

    // ============ PLAYER-BASED QUERIES ============

    @Query("SELECT * FROM match_events WHERE player_id = :playerId ORDER BY match_id DESC, minute")
    fun getEventsByPlayer(playerId: Int): Flow<List<MatchEventsEntity>>

    @Query("SELECT * FROM match_events WHERE player_id = :playerId AND event_type = 'GOAL' ORDER BY match_id DESC")
    fun getGoalsByPlayer(playerId: Int): Flow<List<MatchEventsEntity>>

    @Query("SELECT * FROM match_events WHERE assist_player_id = :playerId ORDER BY match_id DESC")
    fun getAssistsByPlayer(playerId: Int): Flow<List<MatchEventsEntity>>

    @Query("SELECT * FROM match_events WHERE player_id = :playerId AND event_type = 'YELLOW_CARD' ORDER BY match_id DESC")
    fun getYellowCardsByPlayer(playerId: Int): Flow<List<MatchEventsEntity>>

    @Query("SELECT * FROM match_events WHERE player_id = :playerId AND event_type = 'RED_CARD' ORDER BY match_id DESC")
    fun getRedCardsByPlayer(playerId: Int): Flow<List<MatchEventsEntity>>

    @Query("SELECT COUNT(*) FROM match_events WHERE player_id = :playerId AND event_type = 'GOAL'")
    suspend fun getGoalCountByPlayer(playerId: Int): Int

    @Query("SELECT COUNT(*) FROM match_events WHERE assist_player_id = :playerId")
    suspend fun getAssistCountByPlayer(playerId: Int): Int

    @Query("SELECT COUNT(*) FROM match_events WHERE player_id = :playerId AND event_type = 'YELLOW_CARD'")
    suspend fun getYellowCardCountByPlayer(playerId: Int): Int

    @Query("SELECT COUNT(*) FROM match_events WHERE player_id = :playerId AND event_type = 'RED_CARD'")
    suspend fun getRedCardCountByPlayer(playerId: Int): Int

    @Query("SELECT COUNT(*) FROM match_events WHERE player_id = :playerId AND man_of_the_match = 1")
    suspend fun getPlayerManOfTheMatchCount(playerId: Int): Int

    @Query("SELECT rating FROM match_events WHERE player_id = :playerId ORDER BY match_id DESC LIMIT :limit")
    suspend fun getPlayerLastMatchRatings(playerId: Int, limit: Int): List<Int>

    @Query("SELECT SUM(expected_goals) FROM match_events WHERE player_id = :playerId AND event_type = 'SHOT'")
    suspend fun getTotalXGByPlayer(playerId: Int): Double?

    // ============ TEAM-BASED QUERIES ============

    @Query("SELECT * FROM match_events WHERE team_name = :teamName ORDER BY match_id DESC, minute")
    fun getEventsByTeam(teamName: String): Flow<List<MatchEventsEntity>>

    @Query("SELECT * FROM match_events WHERE team_name = :teamName AND match_id = :matchId ORDER BY minute")
    fun getTeamEventsByMatch(teamName: String, matchId: Int): Flow<List<MatchEventsEntity>>

    @Query("SELECT COUNT(*) FROM match_events WHERE team_name = :teamName AND event_type = 'GOAL'")
    suspend fun getGoalCountByTeam(teamName: String): Int

    @Query("SELECT COUNT(*) FROM match_events WHERE team_name = :teamName AND event_type = 'YELLOW_CARD'")
    suspend fun getYellowCardCountByTeam(teamName: String): Int

    @Query("SELECT COUNT(*) FROM match_events WHERE team_name = :teamName AND event_type = 'RED_CARD'")
    suspend fun getRedCardCountByTeam(teamName: String): Int

    // ============ STATISTICS QUERIES ============

    @Query("""
        SELECT 
            player_id,
            COUNT(CASE WHEN event_type = 'GOAL' THEN 1 END) as goals,
            COUNT(CASE WHEN assist_player_id IS NOT NULL THEN 1 END) as assists,
            COUNT(CASE WHEN event_type = 'YELLOW_CARD' THEN 1 END) as yellow_cards,
            COUNT(CASE WHEN event_type = 'RED_CARD' THEN 1 END) as red_cards
        FROM match_events 
        WHERE match_id IN (SELECT id FROM fixtures WHERE season = :season)
        GROUP BY player_id 
        ORDER BY goals DESC
        LIMIT :limit
    """)
    fun getTopScorers(season: String, limit: Int = 10): Flow<List<PlayerStats>>

    @Query("""
        SELECT 
            assist_player_id as player_id,
            COUNT(*) as assists
        FROM match_events 
        WHERE assist_player_id IS NOT NULL 
        AND match_id IN (SELECT id FROM fixtures WHERE season = :season)
        GROUP BY assist_player_id 
        ORDER BY assists DESC
        LIMIT :limit
    """)
    fun getTopAssisters(season: String, limit: Int = 10): Flow<List<PlayerAssistStats>>

    @Query("""
        SELECT 
            team_name,
            COUNT(CASE WHEN event_type = 'GOAL' THEN 1 END) as goals_scored,
            COUNT(CASE WHEN event_type = 'YELLOW_CARD' THEN 1 END) as yellow_cards,
            COUNT(CASE WHEN event_type = 'RED_CARD' THEN 1 END) as red_cards,
            AVG(expected_goals) as avg_xg
        FROM match_events 
        WHERE match_id IN (SELECT id FROM fixtures WHERE league = :leagueName AND season = :season)
        GROUP BY team_name
    """)
    fun getTeamStatsByLeague(leagueName: String, season: String): Flow<List<TeamMatchStats>>

    @Query("""
        SELECT 
            player_id,
            player_name,
            team_id,
            team_name,
            COUNT(CASE WHEN event_type = 'GOAL' THEN 1 END) as goals,
            COUNT(CASE WHEN assist_player_id IS NOT NULL THEN 1 END) as assists,
            AVG(rating) as avg_rating
        FROM match_events 
        WHERE strftime('%m', datetime(timestamp/1000, 'unixepoch')) = :month
        AND match_id IN (SELECT id FROM fixtures WHERE season = :season)
        GROUP BY player_id 
        ORDER BY (goals * 3 + assists * 2 + (avg_rating - 60) * 0.5) DESC
        LIMIT :limit
    """)
    suspend fun getTopPerformersByMonth(season: String, month: String, limit: Int = 10): List<MonthlyPlayerStats>

    @Query("""
        SELECT 
            player_id,
            player_name,
            team_id,
            team_name,
            COUNT(CASE WHEN event_type = 'SAVE' THEN 1 END) as saves,
            AVG(rating) as avg_rating
        FROM match_events 
        WHERE strftime('%m', datetime(timestamp/1000, 'unixepoch')) = :month
        AND match_id IN (SELECT id FROM fixtures WHERE season = :season)
        AND player_id IN (SELECT id FROM players WHERE position = 'GK')
        GROUP BY player_id 
        ORDER BY (saves * 0.5 + (avg_rating - 60) * 0.8) DESC
        LIMIT :limit
    """)
    suspend fun getTopGoalkeepersByMonth(season: String, month: String, limit: Int = 5): List<MonthlyGKStats>

    @Query("""
        SELECT 
            strftime('%m', datetime(timestamp/1000, 'unixepoch')) as month,
            COUNT(CASE WHEN event_type = 'GOAL' THEN 1 END) as goals,
            COUNT(CASE WHEN event_type = 'YELLOW_CARD' THEN 1 END) as yellows,
            COUNT(CASE WHEN event_type = 'RED_CARD' THEN 1 END) as reds
        FROM match_events 
        WHERE match_id IN (SELECT id FROM fixtures WHERE season = :season)
        GROUP BY month
        ORDER BY month
    """)
    fun getMonthlyEventStats(season: String): Flow<List<MonthlyEventStats>>

    @Query("""
        SELECT 
            AVG(minute) as avg_goal_minute,
            COUNT(CASE WHEN minute <= 15 THEN 1 END) as goals_0_15,
            COUNT(CASE WHEN minute BETWEEN 16 AND 30 THEN 1 END) as goals_16_30,
            COUNT(CASE WHEN minute BETWEEN 31 AND 45 THEN 1 END) as goals_31_45,
            COUNT(CASE WHEN minute BETWEEN 46 AND 60 THEN 1 END) as goals_46_60,
            COUNT(CASE WHEN minute BETWEEN 61 AND 75 THEN 1 END) as goals_61_75,
            COUNT(CASE WHEN minute BETWEEN 76 AND 90 THEN 1 END) as goals_76_90,
            COUNT(CASE WHEN minute > 90 THEN 1 END) as goals_90_plus
        FROM match_events 
        WHERE event_type = 'GOAL'
        AND match_id IN (SELECT id FROM fixtures WHERE season = :season)
    """)
    suspend fun getGoalTimingStats(season: String): GoalTimingStats?

    // ============ ADVANCED ANALYTICS ============

    @Query("""
        SELECT 
            player_id,
            AVG(expected_goals) as avg_xg_per_shot,
            SUM(expected_goals) as total_xg,
            COUNT(CASE WHEN event_type = 'GOAL' THEN 1 END) as actual_goals,
            (COUNT(CASE WHEN event_type = 'GOAL' THEN 1 END) - SUM(expected_goals)) as xg_difference
        FROM match_events 
        WHERE event_type IN ('SHOT', 'GOAL', 'PENALTY_SCORED')
        AND match_id IN (SELECT id FROM fixtures WHERE season = :season)
        GROUP BY player_id
        HAVING COUNT(*) >= :minShots
        ORDER BY xg_difference DESC
    """)
    fun getXGEfficiency(minShots: Int = 10, season: String): Flow<List<XGStats>>

    @Query("""
        SELECT 
            player_id,
            COUNT(*) as shot_count,
            COUNT(CASE WHEN event_type = 'GOAL' THEN 1 END) as goal_count,
            (COUNT(CASE WHEN event_type = 'GOAL' THEN 1 END) * 100.0 / COUNT(*)) as conversion_rate,
            AVG(shot_distance) as avg_shot_distance
        FROM match_events 
        WHERE event_type IN ('SHOT', 'GOAL', 'PENALTY_SCORED')
        AND match_id IN (SELECT id FROM fixtures WHERE season = :season)
        GROUP BY player_id
        HAVING COUNT(*) >= :minShots
        ORDER BY conversion_rate DESC
    """)
    fun getShotConversion(minShots: Int = 10, season: String): Flow<List<ShotStats>>
}

// ============ DATA CLASSES FOR STATISTICS ============

data class PlayerStats(
    @ColumnInfo(name = "player_id")
    val playerId: Int,

    @ColumnInfo(name = "goals")
    val goals: Int,

    @ColumnInfo(name = "assists")
    val assists: Int,

    @ColumnInfo(name = "yellow_cards")
    val yellowCards: Int,

    @ColumnInfo(name = "red_cards")
    val redCards: Int
)

data class PlayerAssistStats(
    @ColumnInfo(name = "player_id")
    val playerId: Int,

    @ColumnInfo(name = "assists")
    val assists: Int
)

data class TeamMatchStats(
    @ColumnInfo(name = "team_name")
    val teamName: String,

    @ColumnInfo(name = "goals_scored")
    val goalsScored: Int,

    @ColumnInfo(name = "yellow_cards")
    val yellowCards: Int,

    @ColumnInfo(name = "red_cards")
    val redCards: Int,

    @ColumnInfo(name = "avg_xg")
    val averageXG: Double
)

data class MonthlyEventStats(
    @ColumnInfo(name = "month")
    val month: String,

    @ColumnInfo(name = "goals")
    val goals: Int,

    @ColumnInfo(name = "yellows")
    val yellows: Int,

    @ColumnInfo(name = "reds")
    val reds: Int
)

data class MonthlyPlayerStats(
    @ColumnInfo(name = "player_id")
    val playerId: Int,

    @ColumnInfo(name = "player_name")
    val playerName: String,

    @ColumnInfo(name = "team_id")
    val teamId: Int,

    @ColumnInfo(name = "team_name")
    val teamName: String,

    @ColumnInfo(name = "goals")
    val goals: Int,

    @ColumnInfo(name = "assists")
    val assists: Int,

    @ColumnInfo(name = "avg_rating")
    val avgRating: Double
)

data class MonthlyGKStats(
    @ColumnInfo(name = "player_id")
    val playerId: Int,

    @ColumnInfo(name = "player_name")
    val playerName: String,

    @ColumnInfo(name = "team_id")
    val teamId: Int,

    @ColumnInfo(name = "team_name")
    val teamName: String,

    @ColumnInfo(name = "saves")
    val saves: Int,

    @ColumnInfo(name = "avg_rating")
    val avgRating: Double
)

data class GoalTimingStats(
    @ColumnInfo(name = "avg_goal_minute")
    val averageGoalMinute: Double,

    @ColumnInfo(name = "goals_0_15")
    val goals0to15: Int,

    @ColumnInfo(name = "goals_16_30")
    val goals16to30: Int,

    @ColumnInfo(name = "goals_31_45")
    val goals31to45: Int,

    @ColumnInfo(name = "goals_46_60")
    val goals46to60: Int,

    @ColumnInfo(name = "goals_61_75")
    val goals61to75: Int,

    @ColumnInfo(name = "goals_76_90")
    val goals76to90: Int,

    @ColumnInfo(name = "goals_90_plus")
    val goals90plus: Int
)

data class XGStats(
    @ColumnInfo(name = "player_id")
    val playerId: Int,

    @ColumnInfo(name = "avg_xg_per_shot")
    val averageXGPerShot: Double,

    @ColumnInfo(name = "total_xg")
    val totalXG: Double,

    @ColumnInfo(name = "actual_goals")
    val actualGoals: Int,

    @ColumnInfo(name = "xg_difference")
    val xgDifference: Double
)

data class ShotStats(
    @ColumnInfo(name = "player_id")
    val playerId: Int,

    @ColumnInfo(name = "shot_count")
    val shotCount: Int,

    @ColumnInfo(name = "goal_count")
    val goalCount: Int,

    @ColumnInfo(name = "conversion_rate")
    val conversionRate: Double,

    @ColumnInfo(name = "avg_shot_distance")
    val averageShotDistance: Double
)
