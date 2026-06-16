package com.fameafrica.afm.data.repository

import com.fameafrica.afm.data.database.dao.InterviewsDao
import com.fameafrica.afm.data.database.entities.InterviewsEntity
import com.fameafrica.afm.data.database.entities.FixturesResultsEntity
import com.fameafrica.afm.utils.GameDateManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class InterviewsRepository @Inject constructor(
    private val interviewsDaoProvider: Provider<InterviewsDao>,
    private val journalistsRepository: JournalistsRepository,
    private val managersRepository: ManagersRepository,
    private val playersRepository: PlayersRepository,
    private val newsRepository: NewsRepository,
    private val gameDateManager: GameDateManager
) {

    private val interviewsDao: InterviewsDao?
        get() = try {
            interviewsDaoProvider.get()
        } catch (e: Exception) {
            null
        }

    // ============ BASIC CRUD ============

    fun getAllInterviews(): Flow<List<InterviewsEntity>> = interviewsDao?.getAll() ?: kotlinx.coroutines.flow.flowOf(emptyList())

    suspend fun getInterviewById(id: Int): InterviewsEntity? = interviewsDao?.getById(id)

    fun getManagerInterviews(managerId: Int): Flow<List<InterviewsEntity>> =
        interviewsDao?.getManagerInterviews(managerId) ?: kotlinx.coroutines.flow.flowOf(emptyList())

    suspend fun insertInterview(interview: InterviewsEntity): Long = interviewsDao?.insert(interview) ?: 0L

    suspend fun updateInterview(interview: InterviewsEntity) {
        interviewsDao?.update(interview)
    }

    suspend fun deleteInterview(interview: InterviewsEntity) {
        interviewsDao?.delete(interview)
    }

    // ============ BUSINESS LOGIC ============

    suspend fun generateInterview(
        managerId: Int,
        teamName: String,
        context: String,
        currentWeek: Int,
        targetPlayerId: Int? = null,
        matchResult: FixturesResultsEntity? = null
    ): InterviewsEntity {
        val journalist = journalistsRepository.getAllJournalists().firstOrNull()?.randomOrNull()
            ?: throw IllegalStateException("No journalists found")

        val manager = managersRepository.getManagerById(managerId)
            ?: throw IllegalArgumentException("Manager not found")

        val targetPlayerName = targetPlayerId?.let { playersRepository.getPlayerById(it)?.name }

        val question = when (context) {
            "POST_MATCH" -> generatePostMatchQuestion(teamName, matchResult)
            "TRANSFER_RUMOR" -> generateTransferQuestion(targetPlayerName, teamName)
            "PLAYER_FORM" -> "There's been a lot of talk about ${targetPlayerName ?: "your key players"}'s recent form. Are you concerned?"
            "TITLE_RACE" -> "With the title race heating up, do you believe your squad has the mental strength to go all the way?"
            "RELEGATION_BATTLE" -> "The threat of relegation is looming large. How do you plan to keep the team focused on survival?"
            "FINANCIAL_CRISIS" -> "Rumors of financial instability at $teamName are circulating. How is this affecting the players?"
            else -> "How are things progressing at $teamName under your leadership?"
        }
        
        val interview = InterviewsEntity(
            intervieweeId = managerId,
            intervieweeName = manager.name,
            intervieweeType = "MANAGER",
            journalistId = journalist.id,
            journalistName = journalist.name,
            journalistPersonality = journalist.personality,
            dateRequested = gameDateManager.formatGameDate(currentWeek),
            interviewType = context,
            topic = generateTopic(context, teamName, matchResult),
            question = question,
            playerId = targetPlayerId
        )

        val id = interviewsDao?.insert(interview) ?: 0L
        return interview.copy(id = id.toInt())
    }

    private fun generatePostMatchQuestion(teamName: String, res: FixturesResultsEntity?): String {
        if (res == null) return "How do you assess today's performance?"
        
        val isWin = (res.homeTeam == teamName && res.homeTeamWin) || (res.awayTeam == teamName && res.awayTeamWin)
        val isLoss = (res.homeTeam == teamName && res.awayTeamWin) || (res.awayTeam == teamName && res.homeTeamWin)
        val isDraw = res.isDraw
        
        return when {
            res.isUpset && isWin -> "That was a stunning victory against the odds. Did you always believe you could pull off such a result?"
            res.isUpset && isLoss -> "Many saw your team as favorites today. What went wrong in such a shocking defeat?"
            res.isThrashing && isWin -> "A dominant performance from start to finish. Is this the standard you expect from your players every week?"
            res.isThrashing && isLoss -> "That was a painful result for the fans. How do you plan to pick up the pieces after being outclassed like that?"
            isWin -> "Congratulations on the win. What was the turning point in the match for you?"
            isLoss -> "A disappointing result today. Do you feel the scoreline was a fair reflection of the game?"
            isDraw && res.totalGoals >= 4 -> "A high-scoring draw for the neutrals, but frustrating for a manager. How do you view your defensive display?"
            else -> "A tough battle out there today. Are you satisfied with a point, or do you feel it's two points dropped?"
        }
    }

    private fun generateTransferQuestion(playerName: String?, teamName: String): String {
        if (playerName == null) return "The transfer window is active. Are you looking to make any major moves?"
        
        return when (Random.nextInt(3)) {
            0 -> "Sources suggest $playerName is high on your list of targets. Can you confirm your interest?"
            1 -> "There are reports that $playerName has already agreed personal terms with $teamName. Is an announcement imminent?"
            else -> "With other clubs also monitoring $playerName, how confident are you of securing his signature?"
        }
    }

    private fun generateTopic(context: String, teamName: String, res: FixturesResultsEntity?): String {
        return when (context) {
            "POST_MATCH" -> {
                val opponent = if (res?.homeTeam == teamName) res.awayTeam else res?.homeTeam
                "Reaction: $teamName vs $opponent"
            }
            "TRANSFER_RUMOR" -> "Transfer Speculation at $teamName"
            "PLAYER_FORM" -> "Individual Performances"
            else -> "Club Outlook"
        }
    }

    suspend fun completeInterview(
        interviewId: Int,
        response: String,
        responseType: String,
        impactOnMorale: Int = 0,
        reputationChange: Int = 0,
        fanPopularityChange: Int = 0
    ): InterviewResult {
        val interview = interviewsDao?.getById(interviewId)
            ?: throw IllegalArgumentException("Interview not found")

        if (reputationChange != 0 && interview.intervieweeId != null) {
            val manager = managersRepository.getManagerById(interview.intervieweeId)
            manager?.let {
                managersRepository.updateReputation(it.id, it.reputation + reputationChange)
            }
        }

        if (impactOnMorale != 0 && interview.playerId != null) {
            playersRepository.updatePlayerMorale(interview.playerId, impactOnMorale)
        }

        val updatedInterview = interview.copy(
            response = response,
            responseType = responseType,
            status = "Completed",
            impactOnMorale = impactOnMorale,
            reputationChange = reputationChange,
            fanPopularityChange = fanPopularityChange,
            isPublished = true
        )
        interviewsDao?.update(updatedInterview)

        // Generate a news piece about the interview
        newsRepository.createNewsArticle(
            headline = "${interview.intervieweeName} speaks out on ${interview.topic}",
            content = "In a recent interview with ${interview.journalistName}, ${interview.intervieweeName} addressed the media regarding ${interview.topic}. \"$response,\" said ${interview.intervieweeName}.",
            category = "INTERVIEW",
            relatedManager = interview.intervieweeName,
            relatedTeam = null // Could be passed if we had it
        )

        return InterviewResult(
            message = "Interview completed",
            reputationChange = reputationChange,
            moraleChange = impactOnMorale
        )
    }

    suspend fun declineInterview(interviewId: Int) {
        val interview = interviewsDao?.getById(interviewId) ?: return
        interviewsDao?.update(interview.copy(status = "Declined"))
    }

    suspend fun scheduleInterview(interviewId: Int, date: String) {
        val interview = interviewsDao?.getById(interviewId) ?: return
        interviewsDao?.update(interview.copy(status = "Scheduled", interviewDate = date))
    }

    fun getPendingManagerInterviews(managerId: Int): Flow<List<InterviewsEntity>> =
        interviewsDao?.getPendingManagerInterviews(managerId) ?: kotlinx.coroutines.flow.flowOf(emptyList())

    fun getPlayerInterviews(playerId: Int): Flow<List<InterviewsEntity>> =
        interviewsDao?.getPlayerInterviews(playerId) ?: kotlinx.coroutines.flow.flowOf(emptyList())

    data class InterviewResult(
        val message: String,
        val reputationChange: Int,
        val moraleChange: Int
    )
}
