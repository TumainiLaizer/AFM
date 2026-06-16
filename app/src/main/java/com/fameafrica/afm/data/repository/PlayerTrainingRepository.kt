package com.fameafrica.afm.data.repository

import com.fameafrica.afm.data.database.dao.PlayerTrainingDao
import com.fameafrica.afm.data.database.dao.TeamTrainingStats
import com.fameafrica.afm.data.database.dao.TrainingEffectiveness
import com.fameafrica.afm.data.database.entities.*
import com.fameafrica.afm.utils.GameDateManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class PlayerTrainingRepository @Inject constructor(
    private val playerTrainingDaoProvider: Provider<PlayerTrainingDao>,
    private val playersRepository: PlayersRepository,
    private val staffRepository: StaffRepository,
    private val gameDateManager: GameDateManager
) {

    private val playerTrainingDao: PlayerTrainingDao?
        get() = try {
            playerTrainingDaoProvider.get()
        } catch (e: Exception) {
            null
        }

    // ============ BASIC CRUD ============

    fun getAllTraining(): Flow<List<PlayerTrainingEntity>> = playerTrainingDao?.getAll() ?: kotlinx.coroutines.flow.flowOf(emptyList())

    suspend fun getTrainingById(id: Int): PlayerTrainingEntity? = playerTrainingDao?.getById(id)

    suspend fun insertTraining(training: PlayerTrainingEntity) {
        playerTrainingDao?.insert(training)
    }

    suspend fun updateTraining(training: PlayerTrainingEntity) {
        playerTrainingDao?.update(training)
    }

    suspend fun deleteTraining(training: PlayerTrainingEntity) {
        playerTrainingDao?.delete(training)
    }

    // ============ TRAINING CREATION ============

    /**
     * Create a new training program for a player
     */
    suspend fun createTrainingProgram(
        playerName: String,
        playerId: Int,
        drillType: String,
        focusArea: String,
        currentWeek: Int,
        specificAttribute: String? = null,
        coachId: Int? = null,
        durationDays: Int = 7,
        totalSessions: Int = 5
    ): PlayerTrainingEntity? {

        val player = playersRepository.getPlayerById(playerId) ?: return null

        // Check if player already has active training
        val active = playerTrainingDao?.getActiveTrainingForPlayer(playerName)
        if (active != null) return null

        val startDate = gameDateManager.formatGameDateForDb(currentWeek)
        val endDate = gameDateManager.formatGameDateForDb(currentWeek + 1) // Assuming ~1 week duration

        // Get coach name if coach ID provided
        var coachName: String? = null
        if (coachId != null) {
            coachName = staffRepository.getStaffById(coachId)?.name
        }

        // Calculate base injury risk based on drill type and player age
        val baseInjuryRisk = calculateBaseInjuryRisk(drillType, player.age)

        val training = PlayerTrainingEntity(
            playerName = playerName,
            playerId = playerId,
            coachId = coachId,
            coachName = coachName,
            drillType = drillType,
            focusArea = focusArea,
            specificAttribute = specificAttribute,
            startDate = startDate,
            endDate = endDate,
            durationDays = durationDays,
            totalSessions = totalSessions,
            progress = 0,
            injuryRisk = baseInjuryRisk,
            status = TrainingStatus.ACTIVE.value,
            attributeBefore = getCurrentAttributeValue(player, specificAttribute)
        )

        playerTrainingDao?.insert(training)
        return training
    }

    private fun calculateBaseInjuryRisk(drillType: String, playerAge: Int): Int {
        val baseRisk = when (drillType) {
            DrillType.PHYSICAL.value -> 15
            DrillType.TECHNICAL.value -> 8
            DrillType.TACTICAL.value -> 5
            DrillType.GOALKEEPING.value -> 10
            DrillType.MENTAL.value -> 2
            else -> 7
        }

        // Older players have higher injury risk
        val ageRisk = when {
            playerAge >= 35 -> 15
            playerAge >= 30 -> 8
            else -> 0
        }

        return (baseRisk + ageRisk + Random.nextInt(-3, 4)).coerceIn(1, 40)
    }

    private fun getCurrentAttributeValue(player: PlayersEntity, attribute: String?): Int? {
        if (attribute == null) return null

        return when (attribute.uppercase()) {
            "FINISHING" -> player.finishing
            "PASSING" -> player.passing
            "DRIBBLING" -> player.dribbling
            "CROSSING" -> player.crossing
            "DEFENDING" -> player.defending
            "HEADING" -> player.heading
            "LONG_SHOTS" -> player.longShots
            "PACE" -> player.pace
            "STAMINA" -> player.stamina
            "STRENGTH" -> player.strength
            "ACCELERATION" -> player.acceleration
            "AGILITY" -> player.agility
            "COMPOSURE" -> player.composure
            "DECISIONS" -> player.decisions
            "ANTICIPATION" -> player.anticipation
            "LEADERSHIP" -> player.leadership
            "REFLEXES" -> player.reflexes
            "HANDLING" -> player.handling
            else -> null
        }
    }

    /**
     * Process weekly training for a player (Natural growth + Active programs)
     */
    suspend fun processPlayerWeeklyTraining(playerId: Int) {
        val player = playersRepository.getPlayerById(playerId) ?: return
        val activeTraining = playerTrainingDao?.getTrainingByPlayer(player.name)?.firstOrNull()
            ?.firstOrNull { it.status == TrainingStatus.ACTIVE.value }

        if (activeTraining != null) {
            updateTrainingProgress(activeTraining.id, 1) // Just passing a dummy week for now
        } else {
            // Natural growth for young players
            if (player.age < 23 && Random.nextInt(100) < 15) {
                val attributeToGrow = listOf("finishing", "passing", "defending", "pace", "stamina").random()
                val currentValue = getCurrentAttributeValue(player, attributeToGrow) ?: 50
                if (currentValue < 90) {
                    playersRepository.updatePlayerAttributes(playerId, mapOf(attributeToGrow to currentValue + 1), 1)
                }
            }
        }
    }

    /**
     * Update training progress (called after each session)
     */
    suspend fun updateTrainingProgress(trainingId: Int, currentWeek: Int): PlayerTrainingEntity? {
        val training = playerTrainingDao?.getById(trainingId) ?: return null

        val newSessionsCompleted = training.sessionsCompleted + 1
        val newProgress = (newSessionsCompleted * 100 / training.totalSessions).coerceIn(0, 100)

        // Random injury risk increase
        val riskIncrease = Random.nextInt(1, 6)
        val newInjuryRisk = (training.injuryRisk + riskIncrease).coerceIn(0, 100)

        // Fatigue increase
        val newFatigue = (training.fatigueLevel + 10).coerceIn(0, 100)

        val updated = training.copy(
            sessionsCompleted = newSessionsCompleted,
            progress = newProgress,
            injuryRisk = newInjuryRisk,
            fatigueLevel = newFatigue
        )

        playerTrainingDao?.update(updated)

        // Check if training is complete
        if (newProgress >= 100) {
            completeTraining(trainingId, currentWeek)
        }

        return updated
    }

    /**
     * Complete training and calculate improvement
     */
    suspend fun completeTraining(trainingId: Int, currentWeek: Int): PlayerTrainingEntity? {
        val training = playerTrainingDao?.getById(trainingId) ?: return null

        // Calculate improvement (based on coach quality, player age, etc.)
        val improvement = calculateImprovement(training)

        val attributeAfter = training.attributeBefore?.plus(improvement)?.coerceIn(1, 99)

        // Update player attribute
        if (training.specificAttribute != null && attributeAfter != null) {
            updatePlayerAttribute(training.playerId, training.specificAttribute!!, attributeAfter, currentWeek)
        }

        val updated = training.copy(
            status = TrainingStatus.COMPLETED.value,
            progress = 100,
            resultRating = improvement * 10,
            attributeAfter = attributeAfter,
            improvementAmount = improvement,
            endDate = gameDateManager.formatGameDateForDb(currentWeek)
        )

        playerTrainingDao?.update(updated)
        return updated
    }

    private suspend fun calculateImprovement(training: PlayerTrainingEntity): Int {
        var improvement = Random.nextInt(1, 4) // Base 1-3 improvement

        // Coach impact
        if (training.coachId != null) {
            improvement += Random.nextInt(0, 2)
        }

        // Age impact (younger players improve more)
        val player = playersRepository.getPlayerById(training.playerId)
        if (player != null) {
            if (player.age <= 21) improvement += Random.nextInt(1, 3)
            if (player.age >= 30) improvement = (improvement * 0.7).toInt()
        }

        return improvement.coerceAtLeast(1)
    }

    private suspend fun updatePlayerAttribute(playerId: Int, attribute: String, newValue: Int, currentWeek: Int) {
        val updates = mapOf(attribute.lowercase() to newValue)
        playersRepository.updatePlayerAttributes(playerId, updates, currentWeek)
    }

    /**
     * Cancel training
     */
    suspend fun cancelTraining(trainingId: Int, reason: String): PlayerTrainingEntity? {
        val training = playerTrainingDao?.getById(trainingId) ?: return null

        val updated = training.copy(
            status = TrainingStatus.CANCELLED.value,
            notes = "Cancelled: $reason"
        )

        playerTrainingDao?.update(updated)
        return updated
    }

    /**
     * Pause training
     */
    suspend fun pauseTraining(trainingId: Int): PlayerTrainingEntity? {
        val training = playerTrainingDao?.getById(trainingId) ?: return null

        val updated = training.copy(
            status = TrainingStatus.PAUSED.value
        )

        playerTrainingDao?.update(updated)
        return updated
    }

    /**
     * Resume paused training
     */
    suspend fun resumeTraining(trainingId: Int): PlayerTrainingEntity? {
        val training = playerTrainingDao?.getById(trainingId) ?: return null

        val updated = training.copy(
            status = TrainingStatus.ACTIVE.value
        )

        playerTrainingDao?.update(updated)
        return updated
    }

    // ============ PLAYER-BASED ============

    fun getTrainingByPlayer(playerName: String): Flow<List<PlayerTrainingEntity>> =
        playerTrainingDao?.getTrainingByPlayer(playerName) ?: kotlinx.coroutines.flow.flowOf(emptyList())

    suspend fun getActiveTrainingForPlayer(playerName: String): PlayerTrainingEntity? =
        playerTrainingDao?.getActiveTrainingForPlayer(playerName)

    // ============ TEAM-BASED ============

    fun getTeamActiveTraining(teamId: Int): Flow<List<PlayerTrainingEntity>> =
        playerTrainingDao?.getTeamActiveTraining(teamId) ?: kotlinx.coroutines.flow.flowOf(emptyList())

    fun getTeamTrainingStats(teamId: Int): Flow<List<TeamTrainingStats>> =
        playerTrainingDao?.getTeamTrainingStats(teamId) ?: kotlinx.coroutines.flow.flowOf(emptyList())

    // ============ DASHBOARD ============

    suspend fun getTrainingDashboard(): TrainingDashboard {
        val activeTraining = playerTrainingDao?.getActiveTraining()?.firstOrNull() ?: emptyList()
        val highRisk = playerTrainingDao?.getHighRiskTraining()?.firstOrNull() ?: emptyList()
        val recentCompleted = playerTrainingDao?.getRecentCompletedTraining(10)?.firstOrNull() ?: emptyList()

        return TrainingDashboard(
            activeSessions = activeTraining.size,
            highRiskSessions = highRisk.size,
            recentCompleted = recentCompleted,
            activeTrainingList = activeTraining,
            trainingEffectiveness = playerTrainingDao?.getTrainingEffectiveness()?.firstOrNull() ?: emptyList()
        )
    }
}

// ============ DATA CLASSES ============

data class TrainingDashboard(
    val activeSessions: Int,
    val highRiskSessions: Int,
    val recentCompleted: List<PlayerTrainingEntity>,
    val activeTrainingList: List<PlayerTrainingEntity>,
    val trainingEffectiveness: List<TrainingEffectiveness>
)
