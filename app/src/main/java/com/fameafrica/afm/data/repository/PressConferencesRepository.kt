package com.fameafrica.afm.data.repository

import com.fameafrica.afm.data.database.dao.ManagerPressStats
import com.fameafrica.afm.data.database.dao.PressConferencesDao
import com.fameafrica.afm.data.database.entities.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class PressConferencesRepository @Inject constructor(
    private val pressConferencesDaoProvider: Provider<PressConferencesDao>,
    private val journalistsRepository: JournalistsRepository,
    private val managersRepository: ManagersRepository,
    private val newsRepository: NewsRepository,
    private val teamsRepository: TeamsRepository
) {

    private val pressConferencesDao: PressConferencesDao?
        get() = try {
            pressConferencesDaoProvider.get()
        } catch (e: Exception) {
            null
        }

    // ============ BASIC CRUD ============

    fun getAllPressConferences(): Flow<List<PressConferencesEntity>> = pressConferencesDao?.getAll() ?: kotlinx.coroutines.flow.flowOf(emptyList())

    suspend fun getPressConferenceById(id: Int): PressConferencesEntity? = pressConferencesDao?.getById(id)

    suspend fun insertPressConference(pressConference: PressConferencesEntity) {
        pressConferencesDao?.insert(pressConference)
    }

    suspend fun updatePressConference(pressConference: PressConferencesEntity) {
        pressConferencesDao?.update(pressConference)
    }

    suspend fun deletePressConference(pressConference: PressConferencesEntity) {
        pressConferencesDao?.delete(pressConference)
    }

    // ============ MANAGER-BASED ============

    fun getPressConferencesByManager(managerId: Int): Flow<List<PressConferencesEntity>> =
        pressConferencesDao?.getByManager(managerId) ?: kotlinx.coroutines.flow.flowOf(emptyList())

    fun getPendingPressConferences(managerId: Int): Flow<List<PressConferencesEntity>> =
        pressConferencesDao?.getPendingPressConferences(managerId) ?: kotlinx.coroutines.flow.flowOf(emptyList())

    // ============ PRESS CONFERENCE GENERATION ============

    suspend fun generatePressConference(
        managerId: Int,
        context: String,
        category: QuestionCategory,
        customQuestion: String? = null,
        relatedTeam: String? = null,
        relatedPlayer: String? = null
    ): PressConferencesEntity? {

        val manager = managersRepository.getManagerById(managerId) ?: return null
        val journalist = journalistsRepository.getRandomJournalist() ?: return null

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val timestamp = dateFormat.format(Date())

        // Get question and response options based on category, journalist personality and game context
        val questionData = getQuestionWithResponses(category, journalist.personality, context, manager.name, relatedTeam, relatedPlayer)

        val options = listOf(
            Triple(questionData.positiveResponse, ResponseType.POSITIVE.value, questionData.positiveImpact),
            Triple(questionData.neutralResponse, ResponseType.NEUTRAL.value, questionData.neutralImpact),
            Triple(questionData.negativeResponse, ResponseType.NEGATIVE.value, questionData.negativeImpact)
        )

        val shuffledOptions = options.shuffled()

        val pressConference = PressConferencesEntity(
            managerId = managerId,
            journalistId = journalist.id,
            journalistName = journalist.name,
            journalistPersonality = journalist.personality,
            questionCategory = category.value,
            question = customQuestion ?: questionData.question,
            optionA = shuffledOptions[0].first,
            optionB = shuffledOptions[1].first,
            optionC = shuffledOptions[2].first,
            responseTypeA = shuffledOptions[0].second,
            responseTypeB = shuffledOptions[1].second,
            responseTypeC = shuffledOptions[2].second,
            timestamp = timestamp,
            isPublished = false
        )

        pressConferencesDao?.insert(pressConference)
        return pressConference
    }

    suspend fun submitResponse(
        pressConferenceId: Int,
        selectedOption: String
    ): PressConferencesEntity? {

        val pressConference = pressConferencesDao?.getById(pressConferenceId) ?: return null
        val manager = managersRepository.getManagerById(pressConference.managerId) ?: return null

        val (respType, impact, repChange) = when (selectedOption) {
            pressConference.optionA -> Triple(pressConference.responseTypeA, getImpactValue(pressConference.responseTypeA, manager), getReputationChange(pressConference.responseTypeA, manager))
            pressConference.optionB -> Triple(pressConference.responseTypeB, getImpactValue(pressConference.responseTypeB, manager), getReputationChange(pressConference.responseTypeB, manager))
            pressConference.optionC -> Triple(pressConference.responseTypeC, getImpactValue(pressConference.responseTypeC, manager), getReputationChange(pressConference.responseTypeC, manager))
            else -> return null
        }

        val updated = pressConference.copy(
            selectedResponse = selectedOption,
            responseText = selectedOption,
            impactOnTeam = impact,
            reputationChange = repChange,
            isPublished = true
        )

        pressConferencesDao?.update(updated)
        managersRepository.updateReputation(pressConference.managerId, repChange)
        
        // Update team morale based on impact
        manager.teamId?.let { teamsRepository.updateTeamMorale(it, impact) }

        generatePressConferenceNews(updated)

        return updated
    }

    private fun getImpactValue(responseType: String, manager: ManagersEntity): Int {
        val base = when (responseType) {
            ResponseType.POSITIVE.value -> Random.nextInt(2, 6)
            ResponseType.NEUTRAL.value -> Random.nextInt(-1, 2)
            ResponseType.NEGATIVE.value -> Random.nextInt(-6, -2)
            else -> 0
        }
        // Personality influence
        val personalityBonus = ((manager.playerMotivation ?: 50) - 50) / 10
        return base + personalityBonus
    }

    private fun getReputationChange(responseType: String, manager: ManagersEntity): Int {
        val base = when (responseType) {
            ResponseType.POSITIVE.value -> Random.nextInt(1, 4)
            ResponseType.NEUTRAL.value -> 0
            ResponseType.NEGATIVE.value -> Random.nextInt(-3, 0)
            else -> 0
        }
        // Media handling bonus
        val mediaBonus = if ((manager.mediaHandling ?: 50) > 70 && responseType == ResponseType.POSITIVE.value) 1 else 0
        return base + mediaBonus
    }

    private suspend fun generatePressConferenceNews(pc: PressConferencesEntity) {
        val manager = managersRepository.getManagerById(pc.managerId) ?: return
        
        val headline = when (pc.selectedResponseType) {
            ResponseType.POSITIVE.value -> "${manager.name} issues rallying cry: \"We are ready\""
            ResponseType.NEGATIVE.value -> "Tension at the presser as ${manager.name} hits back at critics"
            else -> "${manager.name} addresses ${pc.questionCategory.lowercase()} speculation"
        }

        val content = "During a scheduled press conference, ${manager.name} was asked about ${pc.questionCategory.lowercase()}. \"${pc.responseText}\" was the manager's firm reply. Journalists noted that the mood in the room was ${if (pc.selectedResponseType == ResponseType.NEGATIVE.value) "hostile" else "professional"}."

        newsRepository.createNewsArticle(
            headline = headline,
            content = content,
            category = "PRESS",
            relatedManager = manager.name
        )
    }

    private fun getQuestionWithResponses(
        category: QuestionCategory,
        personality: String,
        context: String,
        managerName: String,
        relatedTeam: String?,
        relatedPlayer: String?
    ): QuestionData {
        return when (category) {
            QuestionCategory.MATCH_PERFORMANCE -> getMatchPerformanceQuestion(personality, context)
            QuestionCategory.TRANSFER_RUMORS -> getTransferRumorQuestion(personality, relatedPlayer ?: context, relatedTeam ?: "the club")
            QuestionCategory.PLAYER_FORM -> getPlayerFormQuestion(personality, relatedPlayer ?: "your key players")
            QuestionCategory.BOARD -> getBoardQuestion(personality)
            QuestionCategory.RIVALS -> getRivalryQuestion(personality, relatedTeam ?: "your rivals")
            else -> getGeneralQuestion(personality, context)
        }
    }

    private fun getMatchPerformanceQuestion(personality: String, context: String): QuestionData {
        val q = when (personality) {
            "Hostile" -> "That was a shambolic display. Are you out of your depth here?"
            "Sensationalist" -> "Fans are calling for your head after today. Is this the end of the road?"
            "Friendly" -> "A tough result today, boss. How do you plan to lift the spirits in the dressing room?"
            else -> "What is your honest assessment of the team's performance today?"
        }
        return QuestionData(
            question = q,
            positiveResponse = "We showed glimpses of quality. The results will come if we keep believing in the process.",
            neutralResponse = "It wasn't our best day. We'll analyze the footage and work on our mistakes in training.",
            negativeResponse = "I'm extremely disappointed. Some players simply didn't turn up and that is unacceptable.",
            positiveImpact = 5, neutralImpact = 0, negativeImpact = -5
        )
    }

    private fun getTransferRumorQuestion(personality: String, player: String, team: String): QuestionData {
        val q = when (personality) {
            "Sensationalist" -> "EXCLUSIVE: Is it true that $player has already signed a pre-contract with $team?"
            "Hostile" -> "Are you prepared to lose your best player just to balance the books?"
            else -> "Can you clarify the situation regarding $player and the interest from $team?"
        }
        return QuestionData(
            question = q,
            positiveResponse = "$player is an integral part of our plans. He's not for sale at any price.",
            neutralResponse = "We don't comment on speculation. Every player has a valuation, but we are focused on the league.",
            negativeResponse = "If an offer comes in that meets our requirements, we'll have a decision to make.",
            positiveImpact = 4, neutralImpact = 1, negativeImpact = -6
        )
    }

    private fun getPlayerFormQuestion(personality: String, player: String): QuestionData {
        val q = when (personality) {
            "Hostile" -> "$player has been a passenger for weeks. Why do you keep picking him?"
            "Analyst" -> "The data shows $player's contribution has dropped by 40%. Is it tactical or physical?"
            else -> "Are you worried about the dip in form we've seen from $player lately?"
        }
        return QuestionData(
            question = q,
            positiveResponse = "$player has my full support. Class is permanent and he will be back to his best soon.",
            neutralResponse = "Form is temporary. We are working with him on the training pitch to find that spark again.",
            negativeResponse = "Nobody is undroppable. If performances don't improve, changes will be made.",
            positiveImpact = 6, neutralImpact = 0, negativeImpact = -4
        )
    }

    private fun getBoardQuestion(personality: String): QuestionData {
        val q = when (personality) {
            "Sensationalist" -> "Rumors suggest the Chairman hasn't spoken to you in weeks. Is there a rift?"
            "Hostile" -> "Are you frustrated by the lack of financial backing from the board?"
            else -> "How would you describe your current working relationship with the board of directors?"
        }
        return QuestionData(
            question = q,
            positiveResponse = "We are completely aligned. The board shares my vision for the club's future.",
            neutralResponse = "It's a professional relationship. We discuss matters internally and move forward.",
            negativeResponse = "I have my views on how the club should be run. Sometimes those views aren't shared.",
            positiveImpact = 3, neutralImpact = -1, negativeImpact = -8
        )
    }

    private fun getRivalryQuestion(personality: String, rival: String): QuestionData {
        val q = when (personality) {
            "Sensationalist" -> "The fans hate $rival. Will you give them the 'war' they are asking for?"
            "Hostile" -> "You've lost your last two games against $rival. Are you intimidated by them?"
            else -> "The derby against $rival is approaching. How high are the stakes for you personally?"
        }
        return QuestionData(
            question = q,
            positiveResponse = "This is what football is about. We respect them, but we are going there to win.",
            neutralResponse = "It's three points like any other game. We'll prepare well and stick to our plan.",
            negativeResponse = "The pressure is on them. They are the favorites, we have nothing to lose.",
            positiveImpact = 7, neutralImpact = 0, negativeImpact = -3
        )
    }

    private fun getGeneralQuestion(personality: String, context: String): QuestionData {
        return QuestionData(
            question = "How are you finding the challenge of managing in this league so far?",
            positiveResponse = "It's a fantastic challenge. The passion here is incredible and I'm loving every minute.",
            neutralResponse = "It's tough, as expected. Every game is a battle but we are making progress.",
            negativeResponse = "It's been harder than I anticipated. There are many factors outside of my control.",
            positiveImpact = 4, neutralImpact = 0, negativeImpact = -4
        )
    }

    suspend fun getManagerPressStats(managerId: Int): ManagerPressStats? =
        pressConferencesDao?.getManagerPressStats(managerId)

    suspend fun getPressConferenceDashboard(managerId: Int): PressConferenceDashboard {
        val allPress = pressConferencesDao?.getByManager(managerId)?.firstOrNull() ?: emptyList()
        val pending = allPress.filter { it.selectedResponse == null }
        val published = allPress.filter { it.isPublished }

        val totalImpact = published.sumOf { it.impactOnTeam }
        val totalRepChange = published.sumOf { it.reputationChange }
        val avgImpact = if (published.isNotEmpty()) totalImpact.toDouble() / published.size else 0.0
        val avgRepChange = if (published.isNotEmpty()) totalRepChange.toDouble() / published.size else 0.0

        return PressConferenceDashboard(
            totalPressConferences = allPress.size,
            pendingResponses = pending.size,
            publishedResponses = published.size,
            averageImpact = avgImpact,
            averageReputationChange = avgRepChange,
            positiveResponseCount = published.count { it.selectedResponseType == ResponseType.POSITIVE.value },
            neutralResponseCount = published.count { it.selectedResponseType == ResponseType.NEUTRAL.value },
            negativeResponseCount = published.count { it.selectedResponseType == ResponseType.NEGATIVE.value },
            recentPressConferences = published.sortedByDescending { it.timestamp }.take(10),
            pendingPressConferences = pending.sortedBy { it.timestamp }
        )
    }

    data class QuestionData(
        val question: String,
        val positiveResponse: String,
        val neutralResponse: String,
        val negativeResponse: String,
        val positiveImpact: Int,
        val neutralImpact: Int,
        val negativeImpact: Int
    )

    data class PressConferenceDashboard(
        val totalPressConferences: Int,
        val pendingResponses: Int,
        val publishedResponses: Int,
        val averageImpact: Double,
        val averageReputationChange: Double,
        val positiveResponseCount: Int,
        val neutralResponseCount: Int,
        val negativeResponseCount: Int,
        val recentPressConferences: List<PressConferencesEntity>,
        val pendingPressConferences: List<PressConferencesEntity>
    )
}
