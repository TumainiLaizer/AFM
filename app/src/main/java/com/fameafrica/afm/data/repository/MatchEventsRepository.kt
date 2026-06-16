package com.fameafrica.afm.data.repository

import com.fameafrica.afm.data.database.dao.*
import com.fameafrica.afm.data.database.entities.*
import com.fameafrica.afm.utils.GameDateManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class MatchEventsRepository @Inject constructor(
    private val matchEventsDaoProvider: Provider<MatchEventsDao>,
    private val playersRepository: PlayersRepository,
    private val fixturesRepository: FixturesRepository,
    private val gameDateManager: GameDateManager
) {

    private val matchEventsDao: MatchEventsDao?
        get() = try {
            matchEventsDaoProvider.get()
        } catch (e: Exception) {
            null
        }

    // ============ BASIC CRUD ============

    fun getAllEvents(): Flow<List<MatchEventsEntity>> = matchEventsDao?.getAll() ?: flowOf(emptyList())

    suspend fun getEventById(eventId: Int): MatchEventsEntity? = matchEventsDao?.getById(eventId)

    suspend fun insertEvent(event: MatchEventsEntity): MatchEventsEntity {
        val id = matchEventsDao?.insert(event) ?: 0L
        return event.copy(eventId = id.toInt())
    }

    suspend fun insertAllEvents(events: List<MatchEventsEntity>) = matchEventsDao?.insertAll(events)

    suspend fun updateEvent(event: MatchEventsEntity) = matchEventsDao?.update(event)

    suspend fun deleteEvent(event: MatchEventsEntity) = matchEventsDao?.delete(event)

    suspend fun deleteEventsByMatch(matchId: Int) = matchEventsDao?.deleteByMatch(matchId)

    suspend fun getEventsCount(): Int = matchEventsDao?.getCount() ?: 0

    // ============ MATCH-BASED ============

    fun getEventsByMatch(matchId: Int): Flow<List<MatchEventsEntity>> =
        matchEventsDao?.getEventsByMatch(matchId) ?: flowOf(emptyList())

    fun getGoalsByMatch(matchId: Int): Flow<List<MatchEventsEntity>> =
        matchEventsDao?.getGoalsByMatch(matchId) ?: flowOf(emptyList())

    fun getYellowCardsByMatch(matchId: Int): Flow<List<MatchEventsEntity>> =
        matchEventsDao?.getYellowCardsByMatch(matchId) ?: flowOf(emptyList())

    fun getRedCardsByMatch(matchId: Int): Flow<List<MatchEventsEntity>> =
        matchEventsDao?.getRedCardsByMatch(matchId) ?: flowOf(emptyList())

    fun getSubstitutionsByMatch(matchId: Int): Flow<List<MatchEventsEntity>> =
        matchEventsDao?.getSubstitutionsByMatch(matchId) ?: flowOf(emptyList())

    fun getPenaltiesByMatch(matchId: Int): Flow<List<MatchEventsEntity>> =
        matchEventsDao?.getPenaltiesByMatch(matchId) ?: flowOf(emptyList())

    fun getVarEventsByMatch(matchId: Int): Flow<List<MatchEventsEntity>> =
        matchEventsDao?.getVarEventsByMatch(matchId) ?: flowOf(emptyList())

    suspend fun getMatchEventTimeline(matchId: Int): List<MatchEventsEntity> {
        return matchEventsDao?.getEventsByMatch(matchId)?.firstOrNull() ?: emptyList()
    }

    // ============ PLAYER-BASED ============

    fun getEventsByPlayer(playerId: Int): Flow<List<MatchEventsEntity>> =
        matchEventsDao?.getEventsByPlayer(playerId) ?: flowOf(emptyList())

    fun getGoalsByPlayer(playerId: Int): Flow<List<MatchEventsEntity>> =
        matchEventsDao?.getGoalsByPlayer(playerId) ?: flowOf(emptyList())

    fun getAssistsByPlayer(playerId: Int): Flow<List<MatchEventsEntity>> =
        matchEventsDao?.getAssistsByPlayer(playerId) ?: flowOf(emptyList())

    suspend fun getPlayerGoalCount(playerId: Int): Int =
        matchEventsDao?.getGoalCountByPlayer(playerId) ?: 0

    suspend fun getPlayerAssistCount(playerId: Int): Int =
        matchEventsDao?.getAssistCountByPlayer(playerId) ?: 0

    suspend fun getPlayerYellowCardCount(playerId: Int): Int =
        matchEventsDao?.getYellowCardCountByPlayer(playerId) ?: 0

    suspend fun getPlayerRedCardCount(playerId: Int): Int =
        matchEventsDao?.getRedCardCountByPlayer(playerId) ?: 0

    suspend fun getPlayerManOfTheMatchCount(playerId: Int): Int =
        matchEventsDao?.getPlayerManOfTheMatchCount(playerId) ?: 0

    suspend fun getPlayerTotalXG(playerId: Int): Double =
        matchEventsDao?.getTotalXGByPlayer(playerId) ?: 0.0
?: 0.0

    suspend fun getPlayerSeasonStats(playerId: Int, season: String): PlayerSeasonStats {
        val goals = matchEventsDao?.getGoalCountByPlayer(playerId) ?: 0
        val assists = matchEventsDao?.getAssistCountByPlayer(playerId) ?: 0
        val yellows = matchEventsDao?.getYellowCardCountByPlayer(playerId) ?: 0
        val reds = matchEventsDao?.getRedCardCountByPlayer(playerId) ?: 0
        val xg = matchEventsDao?.getTotalXGByPlayer(playerId) ?: 0.0

        return PlayerSeasonStats(
            playerId = playerId,
            season = season,
            goals = goals,
            assists = assists,
            yellowCards = yellows,
            redCards = reds,
            expectedGoals = xg
        )
    }

    suspend fun getPlayerLastMatchRatings(playerId: Int, limit: Int): List<Int> {
        return matchEventsDao?.getPlayerLastMatchRatings(playerId, limit) ?: emptyList()
    }

    // ============ TEAM-BASED ============

    fun getEventsByTeam(teamName: String): Flow<List<MatchEventsEntity>> =
        matchEventsDao?.getEventsByTeam(teamName) ?: flowOf(emptyList())

    suspend fun getTeamGoalCount(teamName: String): Int =
        matchEventsDao?.getGoalCountByTeam(teamName) ?: 0

    suspend fun getTeamYellowCardCount(teamName: String): Int =
        matchEventsDao?.getYellowCardCountByTeam(teamName) ?: 0

    suspend fun getTeamRedCardCount(teamName: String): Int =
        matchEventsDao?.getRedCardCountByTeam(teamName) ?: 0

    // ============ STATISTICS ============

    fun getTopScorers(season: String, limit: Int = 10): Flow<List<PlayerStats>> =
        matchEventsDao?.getTopScorers(season, limit) ?: flowOf(emptyList())

    fun getTopAssisters(season: String, limit: Int = 10): Flow<List<PlayerAssistStats>> =
        matchEventsDao?.getTopAssisters(season, limit) ?: flowOf(emptyList())

    fun getTeamStatsByLeague(leagueName: String, season: String): Flow<List<TeamMatchStats>> =
        matchEventsDao?.getTeamStatsByLeague(leagueName, season) ?: flowOf(emptyList())

    fun getMonthlyEventStats(season: String): Flow<List<MonthlyEventStats>> =
        matchEventsDao?.getMonthlyEventStats(season) ?: flowOf(emptyList())

    suspend fun getGoalTimingStats(season: String): GoalTimingStats? =
        matchEventsDao?.getGoalTimingStats(season)

    fun getXGEfficiency(minShots: Int = 10, season: String): Flow<List<XGStats>> =
        matchEventsDao?.getXGEfficiency(minShots, season) ?: flowOf(emptyList())

    fun getShotConversion(minShots: Int = 10, season: String): Flow<List<ShotStats>> =
        matchEventsDao?.getShotConversion(minShots, season) ?: flowOf(emptyList())

    // ============ EVENT CREATION ============

    suspend fun recordGoal(
        matchId: Int,
        minute: Int,
        playerId: Int,
        playerName: String,
        teamId: Int,
        teamName: String,
        opponentTeamId: Int? = null,
        opponentTeam: String? = null,
        assistPlayerId: Int? = null,
        assistPlayerName: String? = null,
        shotType: String? = null,
        shotDistance: Int? = null,
        expectedGoals: Double? = null,
        period: String = "REGULAR",
        stoppageTime: Int? = null,
        varReview: Boolean = false,
        varOverturned: Boolean = false,
        goalX: Float? = null,
        goalY: Float? = null,
        homeScore: Int? = null,
        awayScore: Int? = null
    ): MatchEventsEntity {
        val event = MatchEventsEntity(
            matchId = matchId,
            minute = minute,
            eventType = EventType.GOAL.value,
            playerName = playerName,
            playerId = playerId,
            assistPlayerName = assistPlayerName,
            assistPlayerId = assistPlayerId,
            teamId = teamId,
            teamName = teamName,
            opponentTeamId = opponentTeamId,
            opponentTeam = opponentTeam,
            homeScore = homeScore,
            awayScore = awayScore,
            period = period,
            stoppageTime = stoppageTime,
            varReview = varReview,
            varOverturned = varOverturned,
            shotType = shotType,
            shotDistance = shotDistance,
            expectedGoals = expectedGoals,
            goalX = goalX,
            goalY = goalY,
            description = "Goal scored by $playerName"
        )

        val id = matchEventsDao?.insert(event) ?: 0L
        val savedEvent = event.copy(eventId = id.toInt())

        // Update player stats
        playersRepository.incrementPlayerGoals(playerId)
        assistPlayerId?.let { playersRepository.incrementPlayerAssists(it) }

        return savedEvent
    }

    suspend fun recordPenalty(
        matchId: Int,
        minute: Int,
        playerId: Int,
        playerName: String,
        teamId: Int,
        teamName: String,
        opponentTeamId: Int? = null,
        opponentTeam: String? = null,
        scored: Boolean,
        saved: Boolean = false,
        hitPost: Boolean = false,
        period: String = "REGULAR",
        stoppageTime: Int? = null,
        varReview: Boolean = false,
        homeScore: Int? = null,
        awayScore: Int? = null
    ): MatchEventsEntity {
        val eventType = if (scored) EventType.PENALTY_SCORED.value else EventType.PENALTY_MISSED.value

        val event = MatchEventsEntity(
            matchId = matchId,
            minute = minute,
            eventType = eventType,
            playerName = playerName,
            playerId = playerId,
            teamId = teamId,
            teamName = teamName,
            opponentTeamId = opponentTeamId,
            opponentTeam = opponentTeam,
            homeScore = homeScore,
            awayScore = awayScore,
            period = period,
            stoppageTime = stoppageTime,
            varReview = varReview,
            penaltySaved = saved,
            penaltyPost = hitPost,
            description = if (scored) "Penalty scored by $playerName" else "Penalty missed by $playerName"
        )

        val id = matchEventsDao?.insert(event) ?: 0L
        val savedEvent = event.copy(eventId = id.toInt())

        if (scored) {
            playersRepository.incrementPlayerGoals(playerId)
        }

        return savedEvent
    }

    suspend fun recordOwnGoal(
        matchId: Int,
        minute: Int,
        playerId: Int,
        playerName: String,
        teamId: Int,
        teamName: String,  // The team that scored the own goal
        opponentTeamId: Int? = null,
        opponentTeam: String? = null,
        period: String = "REGULAR",
        stoppageTime: Int? = null,
        homeScore: Int? = null,
        awayScore: Int? = null
    ): MatchEventsEntity {
        val event = MatchEventsEntity(
            matchId = matchId,
            minute = minute,
            eventType = EventType.OWN_GOAL.value,
            playerName = playerName,
            playerId = playerId,
            teamId = teamId,
            teamName = teamName,
            opponentTeamId = opponentTeamId,
            opponentTeam = opponentTeam,
            homeScore = homeScore,
            awayScore = awayScore,
            period = period,
            stoppageTime = stoppageTime,
            ownGoal = true,
            description = "Own goal scored by $playerName"
        )

        val id = matchEventsDao?.insert(event) ?: 0L
        val savedEvent = event.copy(eventId = id.toInt())

        // Own goal counts against the player's team
        // No goal credited to scoring player

        return savedEvent
    }

    suspend fun recordCard(
        matchId: Int,
        minute: Int,
        playerId: Int,
        playerName: String,
        teamId: Int,
        teamName: String,
        opponentTeamId: Int? = null,
        opponentTeam: String? = null,
        isYellow: Boolean,
        isRed: Boolean = false,
        period: String = "REGULAR",
        stoppageTime: Int? = null,
        varReview: Boolean = false,
        varOverturned: Boolean = false,
        description: String? = null
    ): MatchEventsEntity {
        val eventType = when {
            isRed -> EventType.RED_CARD.value
            else -> EventType.YELLOW_CARD.value
        }

        val event = MatchEventsEntity(
            matchId = matchId,
            minute = minute,
            eventType = eventType,
            playerName = playerName,
            playerId = playerId,
            teamId = teamId,
            teamName = teamName,
            opponentTeamId = opponentTeamId,
            opponentTeam = opponentTeam,
            period = period,
            stoppageTime = stoppageTime,
            varReview = varReview,
            varOverturned = varOverturned,
            description = description ?: "$playerName shown ${if (isRed) "red" else "yellow"} card"
        )

        val id = matchEventsDao?.insert(event) ?: 0L
        val savedEvent = event.copy(eventId = id.toInt())

        // Update player discipline stats
        if (isRed) {
            playersRepository.incrementPlayerRedCards(playerId)
        } else {
            playersRepository.incrementPlayerYellowCards(playerId)
        }

        return savedEvent
    }

    suspend fun recordSubstitution(
        matchId: Int,
        minute: Int,
        teamId: Int,
        teamName: String,
        opponentTeamId: Int? = null,
        opponentTeam: String? = null,
        playerOutId: Int,
        playerOutName: String,
        playerInId: Int,
        playerInName: String,
        period: String = "REGULAR",
        stoppageTime: Int? = null
    ): MatchEventsEntity {
        val event = MatchEventsEntity(
            matchId = matchId,
            minute = minute,
            eventType = EventType.SUBSTITUTION.value,
            playerName = playerOutName,
            playerId = playerOutId,
            teamId = teamId,
            teamName = teamName,
            opponentTeamId = opponentTeamId,
            opponentTeam = opponentTeam,
            period = period,
            stoppageTime = stoppageTime,
            substitutionInPlayer = playerInName,
            substitutionInPlayerId = playerInId,
            substitutionOutPlayer = playerOutName,
            substitutionOutPlayerId = playerOutId,
            description = "$playerInName replaces $playerOutName"
        )

        val id = matchEventsDao?.insert(event) ?: 0L
        val savedEvent = event.copy(eventId = id.toInt())

        return savedEvent
    }

    suspend fun recordInjury(
        matchId: Int,
        minute: Int,
        playerId: Int,
        playerName: String,
        teamId: Int,
        teamName: String,
        opponentTeamId: Int? = null,
        opponentTeam: String? = null,
        injuryType: String,
        injuryMinutes: Int,
        currentWeek: Int,
        period: String = "REGULAR",
        stoppageTime: Int? = null
    ): MatchEventsEntity {
        val event = MatchEventsEntity(
            matchId = matchId,
            minute = minute,
            eventType = EventType.INJURY.value,
            playerName = playerName,
            playerId = playerId,
            teamId = teamId,
            teamName = teamName,
            opponentTeamId = opponentTeamId,
            opponentTeam = opponentTeam,
            period = period,
            stoppageTime = stoppageTime,
            injuryType = injuryType,
            injuryMinutes = injuryMinutes,
            description = "$playerName injured - estimated recovery: $injuryMinutes minutes"
        )

        val id = matchEventsDao?.insert(event) ?: 0L
        val savedEvent = event.copy(eventId = id.toInt())

        // Update player injury status
        playersRepository.setPlayerInjury(playerId, injuryType, injuryMinutes, currentWeek)

        return savedEvent
    }

    suspend fun recordShot(
        matchId: Int,
        minute: Int,
        playerId: Int,
        playerName: String,
        teamId: Int,
        teamName: String,
        opponentTeamId: Int? = null,
        opponentTeam: String? = null,
        onTarget: Boolean,
        shotType: String? = null,
        shotDistance: Int? = null,
        expectedGoals: Double? = null,
        period: String = "REGULAR",
        stoppageTime: Int? = null
    ): MatchEventsEntity {
        val eventType = if (onTarget) EventType.SHOT_ON_TARGET.value else EventType.SHOT_OFF_TARGET.value

        val event = MatchEventsEntity(
            matchId = matchId,
            minute = minute,
            eventType = eventType,
            playerName = playerName,
            playerId = playerId,
            teamId = teamId,
            teamName = teamName,
            opponentTeamId = opponentTeamId,
            opponentTeam = opponentTeam,
            period = period,
            stoppageTime = stoppageTime,
            shotType = shotType,
            shotDistance = shotDistance,
            expectedGoals = expectedGoals,
            description = "$playerName shoots"
        )

        val id = matchEventsDao?.insert(event) ?: 0L
        val savedEvent = event.copy(eventId = id.toInt())

        return savedEvent
    }

    suspend fun recordVarDecision(
        matchId: Int,
        minute: Int,
        playerId: Int? = null,
        playerName: String? = null,
        teamId: Int,
        teamName: String,
        opponentTeamId: Int? = null,
        opponentTeam: String? = null,
        decision: String,
        overturned: Boolean,
        period: String = "REGULAR"
    ): MatchEventsEntity {
        val event = MatchEventsEntity(
            matchId = matchId,
            minute = minute,
            eventType = EventType.VAR.value,
            playerName = playerName ?: "VAR",
            playerId = playerId ?: 0,
            teamId = teamId,
            teamName = teamName,
            opponentTeamId = opponentTeamId,
            opponentTeam = opponentTeam,
            period = period,
            varReview = true,
            varOverturned = overturned,
            description = "VAR: $decision ${if (overturned) "(Overturned)" else "(Confirmed)"}"
        )

        val id = matchEventsDao?.insert(event) ?: 0L
        val savedEvent = event.copy(eventId = id.toInt())

        return savedEvent
    }

    // ============ MATCH ANALYSIS ============

    suspend fun analyzeMatch(matchId: Int): MatchAnalysis {
        val events = matchEventsDao?.getEventsByMatch(matchId)?.firstOrNull() ?: emptyList()
        val fixture = fixturesRepository.getFixtureById(matchId)

        val homeTeam = fixture?.homeTeam ?: "Home"
        val awayTeam = fixture?.awayTeam ?: "Away"

        val homeGoals = events.count { it.teamName == homeTeam && it.isGoal }
        val awayGoals = events.count { it.teamName == awayTeam && it.isGoal }

        val homeShots = events.count { it.teamName == homeTeam && it.eventType in listOf("SHOT", "SHOT_ON_TARGET", "SHOT_OFF_TARGET", "GOAL") }
        val awayShots = events.count { it.teamName == awayTeam && it.eventType in listOf("SHOT", "SHOT_ON_TARGET", "SHOT_OFF_TARGET", "GOAL") }

        val homeShotsOnTarget = events.count { it.teamName == homeTeam && it.eventType in listOf("SHOT_ON_TARGET", "GOAL") }
        val awayShotsOnTarget = events.count { it.teamName == awayTeam && it.eventType in listOf("SHOT_ON_TARGET", "GOAL") }

        val homePossession = 50 // This would come from fixtures_results
        val awayPossession = 50

        return MatchAnalysis(
            matchId = matchId,
            homeTeam = homeTeam,
            awayTeam = awayTeam,
            homeGoals = homeGoals,
            awayGoals = awayGoals,
            homeShots = homeShots,
            awayShots = awayShots,
            homeShotsOnTarget = homeShotsOnTarget,
            awayShotsOnTarget = awayShotsOnTarget,
            homePossession = homePossession,
            awayPossession = awayPossession,
            homeYellowCards = events.count { it.teamName == homeTeam && it.eventType == "YELLOW_CARD" },
            awayYellowCards = events.count { it.teamName == awayTeam && it.eventType == "YELLOW_CARD" },
            homeRedCards = events.count { it.teamName == homeTeam && it.eventType == "RED_CARD" },
            awayRedCards = events.count { it.teamName == awayTeam && it.eventType == "RED_CARD" },
            homeCorners = events.count { it.teamName == homeTeam && it.eventType == "CORNER" },
            awayCorners = events.count { it.teamName == awayTeam && it.eventType == "CORNER" },
            homeFouls = events.count { it.teamName == homeTeam && it.eventType == "FOUL" },
            awayFouls = events.count { it.teamName == awayTeam && it.eventType == "FOUL" },
            homeOffsides = events.count { it.teamName == homeTeam && it.eventType == "OFFSIDE" },
            awayOffsides = events.count { it.teamName == awayTeam && it.eventType == "OFFSIDE" },
            homeXG = events.filter { it.teamName == homeTeam }.sumOf { it.expectedGoals ?: 0.0 },
            awayXG = events.filter { it.teamName == awayTeam }.sumOf { it.expectedGoals ?: 0.0 }
        )
    }

    suspend fun getPlayerHeatmap(playerId: Int, matchId: Int): List<ShotLocation> {
        val events = matchEventsDao?.getEventsByPlayer(playerId)?.firstOrNull() ?: emptyList()
        return events.filter { it.matchId == matchId && it.goalX != null && it.goalY != null }
            .map { ShotLocation(it.goalX!!, it.goalY!!, it.isGoal) }
    }
}

// ============ DATA CLASSES ============

data class PlayerSeasonStats(
    val playerId: Int,
    val season: String,
    val goals: Int,
    val assists: Int,
    val yellowCards: Int,
    val redCards: Int,
    val expectedGoals: Double
)

data class MatchAnalysis(
    val matchId: Int,
    val homeTeam: String,
    val awayTeam: String,
    val homeGoals: Int,
    val awayGoals: Int,
    val homeShots: Int,
    val awayShots: Int,
    val homeShotsOnTarget: Int,
    val awayShotsOnTarget: Int,
    val homePossession: Int,
    val awayPossession: Int,
    val homeYellowCards: Int,
    val awayYellowCards: Int,
    val homeRedCards: Int,
    val awayRedCards: Int,
    val homeCorners: Int,
    val awayCorners: Int,
    val homeFouls: Int,
    val awayFouls: Int,
    val homeOffsides: Int,
    val awayOffsides: Int,
    val homeXG: Double,
    val awayXG: Double
)

data class ShotLocation(
    val x: Float,
    val y: Float,
    val isGoal: Boolean
)
