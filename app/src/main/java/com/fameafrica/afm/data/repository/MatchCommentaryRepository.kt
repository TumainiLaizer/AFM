package com.fameafrica.afm.data.repository

import com.fameafrica.afm.data.database.dao.CommentaryTypeDistribution
import com.fameafrica.afm.data.database.dao.MatchCommentaryDao
import com.fameafrica.afm.data.database.entities.*
import com.fameafrica.afm.utils.commentary.AfricanFootballCommentaryGenerator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class MatchCommentaryRepository @Inject constructor(
    private val matchCommentaryDaoProvider: Provider<MatchCommentaryDao>,
    private val fixturesRepository: FixturesRepository,
    private val teamsRepository: TeamsRepository,
    private val playersRepository: PlayersRepository,
    private val managersRepository: ManagersRepository,
    private val refereesRepository: RefereesRepository,
    private val matchEventsRepository: MatchEventsRepository
) {
    private val matchCommentaryDao get() = matchCommentaryDaoProvider.get()

    // ============ BASIC CRUD ============

    fun getCommentaryForMatch(matchId: Int): Flow<List<MatchCommentaryEntity>> =
        matchCommentaryDao.getCommentaryForMatch(matchId)

    suspend fun getCommentaryById(id: Int): MatchCommentaryEntity? = matchCommentaryDao.getById(id)

    suspend fun insertCommentary(commentary: MatchCommentaryEntity): Long = matchCommentaryDao.insert(commentary)

    suspend fun insertAllCommentary(commentaries: List<MatchCommentaryEntity>): List<Long> =
        matchCommentaryDao.insertAll(commentaries)

    suspend fun deleteCommentary(commentary: MatchCommentaryEntity) = matchCommentaryDao.delete(commentary)

    suspend fun deleteByMatch(matchId: Int) = matchCommentaryDao.deleteByMatch(matchId)

    suspend fun getCommentaryCountForMatch(matchId: Int): Int =
        matchCommentaryDao.getCommentaryCountForMatch(matchId)

    // ============ COMMENTARY GENERATION FROM EVENTS ============

    /**
     * Generate full match commentary from match events
     */
    suspend fun generateMatchCommentaryFromEvents(
        matchId: Int
    ): List<MatchCommentaryEntity> {
        // Delete existing commentary for this match
        deleteByMatch(matchId)

        // Get match details
        val fixture = fixturesRepository.getFixtureById(matchId) ?: return emptyList()
        val events = matchEventsRepository.getEventsByMatch(matchId).firstOrNull() ?: emptyList()
        val homeTeam = teamsRepository.getTeamByName(fixture.homeTeam)
        val awayTeam = teamsRepository.getTeamByName(fixture.awayTeam)
        val referee = fixture.refereeId?.let { refereesRepository.getRefereeById(it) }
        val homeManager = homeTeam?.managerId?.let { managersRepository.getManagerById(it) }
        val awayManager = awayTeam?.managerId?.let { managersRepository.getManagerById(it) }

        val commentaries = mutableListOf<MatchCommentaryEntity>()

        // Pre-match build-up
        commentaries.add(
            createPreMatchCommentary(matchId, fixture, referee)
        )

        // Process events in chronological order
        val sortedEvents = events.sortedBy { it.minute }

        for (event in sortedEvents) {
            val commentary = createCommentaryFromEvent(
                matchId = matchId,
                event = event,
                homeTeam = fixture.homeTeam,
                awayTeam = fixture.awayTeam,
                homeManager = homeManager,
                awayManager = awayManager,
                referee = referee
            )
            commentaries.add(commentary)
        }

        // Half-time commentary
        commentaries.add(
            createHalfTimeCommentary(
                matchId = matchId,
                fixture = fixture,
                events = events
            )
        )

        // Full-time commentary
        commentaries.add(
            createFullTimeCommentary(
                matchId = matchId,
                fixture = fixture,
                events = events
            )
        )

        // Insert all commentaries and update their IDs
        val ids = insertAllCommentary(commentaries)
        return commentaries.mapIndexed { index, commentary ->
            commentary.copy(id = ids[index].toInt())
        }
    }

    /**
     * Generate commentary for a single event (real-time)
     */
    suspend fun generateCommentaryForEvent(
        event: MatchEventsEntity
    ): MatchCommentaryEntity? {
        val fixture = fixturesRepository.getFixtureById(event.matchId) ?: return null
        val homeTeam = teamsRepository.getTeamByName(fixture.homeTeam)
        val awayTeam = teamsRepository.getTeamByName(fixture.awayTeam)
        val referee = fixture.refereeId?.let { refereesRepository.getRefereeById(it) }
        val homeManager = homeTeam?.managerId?.let { managersRepository.getManagerById(it) }
        val awayManager = awayTeam?.managerId?.let { managersRepository.getManagerById(it) }

        val commentary = createCommentaryFromEvent(
            matchId = event.matchId,
            event = event,
            homeTeam = fixture.homeTeam,
            awayTeam = fixture.awayTeam,
            homeManager = homeManager,
            awayManager = awayManager,
            referee = referee
        )

        val id = insertCommentary(commentary)
        return commentary.copy(id = id.toInt())
    }

    suspend fun createCommentaryFromEvent(
        matchId: Int,
        event: MatchEventsEntity,
        homeTeam: String,
        awayTeam: String,
        homeManager: ManagersEntity?,
        awayManager: ManagersEntity?,
        referee: RefereesEntity?
    ): MatchCommentaryEntity {

        val commentaryText = AfricanFootballCommentaryGenerator.generateCommentaryFromEvent(
            event = event,
            homeTeam = homeTeam,
            awayTeam = awayTeam,
            homeManager = homeManager,
            awayManager = awayManager,
            referee = referee
        )

        val isHomeTeam = event.teamName == homeTeam
        val crowdNoise = calculateCrowdNoise(event, isHomeTeam)
        val fanReaction = getFanReaction(event, isHomeTeam)
        val importance = calculateImportance(event)
        val isControversial = event.varReview ||
                event.eventType == "RED_CARD" ||
                event.eventType == "PENALTY_MISSED" ||
                (event.eventType == "GOAL" && (event.minute >= 88 || (event.stoppageTime ?: 0) > 0))

        return MatchCommentaryEntity(
            matchId = matchId,
            eventId = event.eventId,
            minute = event.minute,
            stoppageTime = event.stoppageTime,
            period = event.period,
            commentaryText = commentaryText,
            commentaryType = mapEventTypeToCommentaryType(event.eventType),
            importance = importance,
            playerId = event.playerId,
            playerName = event.playerName,
            assistPlayerId = event.assistPlayerId,
            assistPlayerName = event.assistPlayerName,
            teamName = event.teamName,
            opponentTeam = event.opponentTeam,
            managerId = if (isHomeTeam) homeManager?.id else awayManager?.id,
            managerName = if (isHomeTeam) homeManager?.name else awayManager?.name,
            refereeId = referee?.refereeId,
            refereeName = referee?.name,
            currentScore = "${event.homeScore ?: 0}-${event.awayScore ?: 0}",
            homeScore = event.homeScore,
            awayScore = event.awayScore,
            isControversial = isControversial,
            varReview = event.varReview,
            varOverturned = event.varOverturned,
            penaltySaved = event.penaltySaved,
            penaltyPost = event.penaltyPost,
            ownGoal = event.ownGoal,
            shotType = event.shotType,
            shotDistance = event.shotDistance,
            expectedGoals = event.expectedGoals,
            substitutionInPlayer = event.substitutionInPlayer,
            substitutionInPlayerId = event.substitutionInPlayerId,
            substitutionOutPlayer = event.substitutionOutPlayer,
            substitutionOutPlayerId = event.substitutionOutPlayerId,
            injuryType = event.injuryType,
            injuryMinutes = event.injuryMinutes,
            fanReaction = fanReaction,
            crowdNoiseLevel = crowdNoise,
            description = event.description
        )
    }

    private fun createPreMatchCommentary(
        matchId: Int,
        fixture: FixturesEntity,
        referee: RefereesEntity?
    ): MatchCommentaryEntity {
        return MatchCommentaryEntity(
            matchId = matchId,
            minute = -5,
            commentaryText = "The teams are in the tunnel. The atmosphere is ELECTRIC here at ${fixture.stadium}!",
            commentaryType = "DRAMA",
            importance = 2,
            refereeId = referee?.refereeId,
            refereeName = referee?.name,
            crowdNoiseLevel = 7,
            period = "REGULAR"
        )
    }

    private suspend fun createHalfTimeCommentary(
        matchId: Int,
        fixture: FixturesEntity,
        events: List<MatchEventsEntity>
    ): MatchCommentaryEntity {
        val homeGoals = events.count { it.eventType == "GOAL" && it.teamName == fixture.homeTeam }
        val awayGoals = events.count { it.eventType == "GOAL" && it.teamName == fixture.awayTeam }
        val homeShots = events.count { it.teamName == fixture.homeTeam && it.eventType.contains("SHOT") }
        val awayShots = events.count { it.teamName == fixture.awayTeam && it.eventType.contains("SHOT") }
        val homeCorners = events.count { it.teamName == fixture.homeTeam && it.eventType == "CORNER" }
        val awayCorners = events.count { it.teamName == fixture.awayTeam && it.eventType == "CORNER" }

        val commentaryText = AfricanFootballCommentaryGenerator.generateHalfTimeCommentary(
            homeTeam = fixture.homeTeam,
            awayTeam = fixture.awayTeam,
            homeScore = homeGoals,
            awayScore = awayGoals,
            homePossession = 52, // This would come from match statistics
            homeShots = homeShots,
            awayShots = awayShots,
            homeCorners = homeCorners,
            awayCorners = awayCorners
        )

        return MatchCommentaryEntity(
            matchId = matchId,
            minute = 45,
            commentaryText = commentaryText,
            commentaryType = "STATISTIC",
            importance = 3,
            period = "HALF_TIME"
        )
    }

    private suspend fun createFullTimeCommentary(
        matchId: Int,
        fixture: FixturesEntity,
        events: List<MatchEventsEntity>
    ): MatchCommentaryEntity {
        val homeGoals = events.count { it.eventType == "GOAL" && it.teamName == fixture.homeTeam }
        val awayGoals = events.count { it.eventType == "GOAL" && it.teamName == fixture.awayTeam }

        // Determine if it's an upset (simplified logic)
        val isUpset = (homeGoals < awayGoals && fixture.homeTeam < fixture.awayTeam) ||
                (awayGoals < homeGoals && fixture.awayTeam < fixture.homeTeam)

        val winner = when {
            homeGoals > awayGoals -> fixture.homeTeam
            awayGoals > homeGoals -> fixture.awayTeam
            else -> null
        }

        val commentaryText = AfricanFootballCommentaryGenerator.generateFullTimeCommentary(
            homeTeam = fixture.homeTeam,
            awayTeam = fixture.awayTeam,
            homeScore = homeGoals,
            awayScore = awayGoals,
            isUpset = isUpset,
            winner = winner,
            attendance = 5000 // This would come from match data
        )

        val crowdNoise = when {
            winner == fixture.homeTeam -> 10
            winner == fixture.awayTeam -> 8
            else -> 5
        }

        return MatchCommentaryEntity(
            matchId = matchId,
            minute = 90,
            commentaryText = commentaryText,
            commentaryType = "DRAMA",
            importance = 5,
            crowdNoiseLevel = crowdNoise,
            period = "FULL_TIME"
        )
    }

    // ============ UTILITY FUNCTIONS ============

    private fun mapEventTypeToCommentaryType(eventType: String): String {
        return when (eventType) {
            "GOAL" -> "GOAL"
            "PENALTY_SCORED", "PENALTY_MISSED" -> "PENALTY"
            "FREEKICK_SCORED", "FREEKICK_MISSED" -> "FREEKICK"
            "OWN_GOAL" -> "OWN_GOAL"
            "YELLOW_CARD", "RED_CARD" -> "CARD"
            "SUBSTITUTION" -> "SUBSTITUTION"
            "INJURY" -> "INJURY"
            "VAR" -> "VAR"
            "SHOT" -> "SHOT"
            "SHOT_ON_TARGET" -> "SHOT"
            "SHOT_OFF_TARGET" -> "SHOT"
            "SAVE" -> "SAVE"
            "CORNER" -> "CORNER"
            "FOUL" -> "FOUL"
            "OFFSIDE" -> "OFFSIDE"
            else -> "DRAMA"
        }
    }

    private fun calculateImportance(event: MatchEventsEntity): Int {
        return when (event.eventType) {
            "GOAL", "PENALTY_SCORED", "FREEKICK_SCORED" -> 5
            "RED_CARD" -> 5
            "OWN_GOAL" -> 4
            "PENALTY_MISSED" -> 4
            "VAR" -> 4
            "YELLOW_CARD" -> 3
            "INJURY" -> 3
            "SAVE" -> 3
            "SHOT_ON_TARGET" -> 2
            "SUBSTITUTION" -> 2
            else -> 1
        }
    }

    private fun calculateCrowdNoise(event: MatchEventsEntity, isHomeTeam: Boolean): Int {
        return when {
            event.eventType == "GOAL" && isHomeTeam -> 10
            event.eventType == "GOAL" && !isHomeTeam -> 3
            event.eventType == "PENALTY_SCORED" && isHomeTeam -> 10
            event.eventType == "PENALTY_MISSED" && isHomeTeam -> 2
            event.eventType == "RED_CARD" && isHomeTeam -> 2
            event.eventType == "RED_CARD" && !isHomeTeam -> 9
            event.eventType == "SAVE" && isHomeTeam -> 8
            event.eventType == "SHOT_ON_TARGET" -> 6
            else -> 5
        }
    }

    private fun getFanReaction(event: MatchEventsEntity, isHomeTeam: Boolean): String? {
        return when {
            event.eventType == "GOAL" && isHomeTeam -> "CHEERING"
            event.eventType == "GOAL" && !isHomeTeam -> "SILENCE"
            event.eventType == "PENALTY_SCORED" && isHomeTeam -> "CHEERING"
            event.eventType == "PENALTY_MISSED" && isHomeTeam -> "GROAN"
            event.eventType == "RED_CARD" && isHomeTeam -> "DISBELIEF"
            event.eventType == "RED_CARD" && !isHomeTeam -> "CHEERING"
            event.varReview -> "WHISTLING"
            else -> null
        }
    }

    // ============ TYPE-BASED QUERIES ============

    fun getGoalsCommentary(matchId: Int): Flow<List<MatchCommentaryEntity>> =
        matchCommentaryDao.getCommentaryByType(matchId, "GOAL")

    fun getCardCommentary(matchId: Int): Flow<List<MatchCommentaryEntity>> =
        matchCommentaryDao.getCommentaryByType(matchId, "CARD")

    fun getPenaltyCommentary(matchId: Int): Flow<List<MatchCommentaryEntity>> =
        matchCommentaryDao.getCommentaryByType(matchId, "PENALTY")

    fun getControversialMoments(matchId: Int): Flow<List<MatchCommentaryEntity>> =
        matchCommentaryDao.getControversialMoments(matchId)

    fun getImportantMoments(matchId: Int, minImportance: Int = 4): Flow<List<MatchCommentaryEntity>> =
        matchCommentaryDao.getImportantCommentary(matchId, minImportance)

    fun getCommentaryByPeriod(matchId: Int, period: String): Flow<List<MatchCommentaryEntity>> {
        return matchCommentaryDao.getCommentaryForMatch(matchId).map { list ->
            list.filter { it.period == period }
        }
    }

    // ============ PLAYER-BASED QUERIES ============

    fun getCommentaryForPlayer(playerId: Int): Flow<List<MatchCommentaryEntity>> =
        matchCommentaryDao.getCommentaryForPlayer(playerId)

    fun getCommentaryForPlayerName(playerName: String): Flow<List<MatchCommentaryEntity>> =
        matchCommentaryDao.getCommentaryForPlayerName(playerName)

    // ============ TEAM-BASED QUERIES ============

    fun getCommentaryForTeam(teamId: Int): Flow<List<MatchCommentaryEntity>> =
        matchCommentaryDao.getCommentaryForTeam(teamId)

    fun getGoalCommentaryForTeam(teamId: Int): Flow<List<MatchCommentaryEntity>> =
        matchCommentaryDao.getGoalCommentaryForTeam(teamId)

    // ============ REFEREE-BASED QUERIES ============

    fun getCommentaryForReferee(refereeId: Int): Flow<List<MatchCommentaryEntity>> =
        matchCommentaryDao.getCommentaryForReferee(refereeId)

    // ============ STATISTICS ============

    suspend fun getGoalCommentaryCount(matchId: Int): Int =
        matchCommentaryDao.getGoalCommentaryCount(matchId)

    suspend fun getControversialMomentCount(matchId: Int): Int =
        matchCommentaryDao.getControversialMomentCount(matchId)

    suspend fun getAverageCrowdNoise(matchId: Int): Double? =
        matchCommentaryDao.getAverageCrowdNoise(matchId)

    fun getCommentaryTypeDistribution(matchId: Int): Flow<List<CommentaryTypeDistribution>> =
        matchCommentaryDao.getCommentaryTypeDistribution(matchId)

    // ============ DASHBOARD ============

    suspend fun getMatchCommentaryDashboard(matchId: Int): MatchCommentaryDashboard {
        val allCommentary = matchCommentaryDao.getCommentaryForMatch(matchId).firstOrNull() ?: emptyList()
        val goals = allCommentary.filter { it.commentaryType == "GOAL" || it.commentaryType == "PENALTY" || it.commentaryType == "FREEKICK" }
        val cards = allCommentary.filter { it.commentaryType == "CARD" }
        val controversies = allCommentary.filter { it.isControversial }
        val fanMoments = allCommentary.filter { it.fanReaction != null }

        val averageCrowdNoise = allCommentary.map { it.crowdNoiseLevel }.average()
        val mostImportant = allCommentary.maxByOrNull { it.importance }

        val commentaryByPeriod = allCommentary.groupBy { it.period }
            .mapValues { it.value.size }

        val timelineSegments = allCommentary.groupBy { it.minute / 15 }
            .map { (interval, comments) ->
                TimelineSegment(
                    minuteRange = "${interval * 15}-${(interval + 1) * 15}",
                    commentaryCount = comments.size,
                    importanceScore = comments.sumOf { it.importance }
                )
            }.sortedBy { it.minuteRange }

        return MatchCommentaryDashboard(
            matchId = matchId,
            totalCommentary = allCommentary.size,
            goalCount = goals.size,
            cardCount = cards.size,
            controversialCount = controversies.size,
            fanMomentCount = fanMoments.size,
            averageCrowdNoise = averageCrowdNoise,
            mostImportantMoment = mostImportant,
            commentaryByPeriod = commentaryByPeriod,
            timelineSegments = timelineSegments,
            allCommentary = allCommentary
        )
    }
}

// ============ DATA CLASSES ============

data class TimelineSegment(
    val minuteRange: String,
    val commentaryCount: Int,
    val importanceScore: Int
)

data class MatchCommentaryDashboard(
    val matchId: Int,
    val totalCommentary: Int,
    val goalCount: Int,
    val cardCount: Int,
    val controversialCount: Int,
    val fanMomentCount: Int,
    val averageCrowdNoise: Double,
    val mostImportantMoment: MatchCommentaryEntity?,
    val commentaryByPeriod: Map<String, Int>,
    val timelineSegments: List<TimelineSegment>,
    val allCommentary: List<MatchCommentaryEntity>
)
