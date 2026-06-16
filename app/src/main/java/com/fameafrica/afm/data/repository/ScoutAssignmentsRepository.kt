package com.fameafrica.afm.data.repository

import com.fameafrica.afm.data.database.dao.PriorityDistribution
import com.fameafrica.afm.data.database.dao.ScoutAssignmentsDao
import com.fameafrica.afm.data.database.dao.ScoutPerformanceStats
import com.fameafrica.afm.data.database.dao.ScoutingMissionsDao
import com.fameafrica.afm.data.database.entities.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class ScoutAssignmentsRepository @Inject constructor(
    private val scoutAssignmentsDaoProvider: Provider<ScoutAssignmentsDao>,
    private val scoutingMissionsDaoProvider: Provider<ScoutingMissionsDao>,
    private val staffRepository: StaffRepository,
    private val playersRepository: PlayersRepository
) {
    private val scoutAssignmentsDao get() = scoutAssignmentsDaoProvider.get()
    private val scoutingMissionsDao get() = scoutingMissionsDaoProvider.get()

    // ============ BASIC CRUD ============

    fun getAllAssignments(): Flow<List<ScoutAssignmentsEntity>> = scoutAssignmentsDao.getAll()

    suspend fun getAssignmentById(id: Int): ScoutAssignmentsEntity? = scoutAssignmentsDao.getById(id)

    suspend fun insertAssignment(assignment: ScoutAssignmentsEntity) = scoutAssignmentsDao.insert(assignment)

    suspend fun updateAssignment(assignment: ScoutAssignmentsEntity) = scoutAssignmentsDao.update(assignment)

    suspend fun deleteAssignment(assignment: ScoutAssignmentsEntity) = scoutAssignmentsDao.delete(assignment)

    // ============ ASSIGNMENT CREATION ============

    /**
     * Assign a scout to scout a player
     */
    suspend fun assignScoutToPlayer(
        scoutId: Int,
        playerId: Int,
        priority: String = ScoutPriority.NORMAL.value,
        focus: String? = null,
        notes: String? = null
    ): ScoutAssignmentsEntity? {

        val scout = staffRepository.getStaffById(scoutId) ?: return null
        val player = playersRepository.getPlayerById(playerId) ?: return null

        // Check if scout is already assigned to this player
        val existing = scoutAssignmentsDao.getAssignmentsByPlayer(playerId).firstOrNull()
            ?.find { it.scoutId == scoutId && it.reportStatus == ScoutReportStatus.IN_PROGRESS.value }

        if (existing != null) return null

        val assignment = ScoutAssignmentsEntity(
            scoutId = scoutId,
            scoutName = scout.name,
            playerId = playerId,
            playerName = player.name,
            reportStatus = ScoutReportStatus.IN_PROGRESS.value,
            priority = priority,
            scoutingFocus = focus,
            assignmentNotes = notes
        )

        scoutAssignmentsDao.insert(assignment)
        return assignment
    }

    /**
     * Complete a scouting assignment with report
     */
    suspend fun completeAssignment(
        assignmentId: Int,
        report: String,
        rating: Int,
        estimatedValue: Int,
        potentialRating: Int,
        strengths: List<String>? = null,
        weaknesses: List<String>? = null,
        verdict: String
    ): Boolean {

        val assignment = scoutAssignmentsDao.getById(assignmentId) ?: return false

        val strengthsJson = strengths?.joinToString(",")
        val weaknessesJson = weaknesses?.joinToString(",")

        scoutAssignmentsDao.completeAssignment(
            id = assignmentId,
            completionDate = System.currentTimeMillis(),
            report = report,
            rating = rating.coerceIn(1, 100),
            estimatedValue = estimatedValue,
            potentialRating = potentialRating.coerceIn(1, 100),
            strengths = strengthsJson,
            weaknesses = weaknessesJson,
            verdict = verdict
        )

        return true
    }

    /**
     * Fail a scouting assignment (scout couldn't complete)
     */
    suspend fun failAssignment(assignmentId: Int): Boolean {
        val assignment = scoutAssignmentsDao.getById(assignmentId) ?: return false
        scoutAssignmentsDao.failAssignment(assignmentId, System.currentTimeMillis())
        return true
    }

    // ============ SCOUT-BASED ============

    fun getAssignmentsByScout(scoutId: Int): Flow<List<ScoutAssignmentsEntity>> =
        scoutAssignmentsDao.getAssignmentsByScout(scoutId)

    fun getActiveAssignmentsByScout(scoutId: Int): Flow<List<ScoutAssignmentsEntity>> =
        scoutAssignmentsDao.getActiveAssignmentsByScout(scoutId)

    suspend fun getActiveAssignmentCount(scoutId: Int): Int =
        scoutAssignmentsDao.getActiveAssignmentCount(scoutId)

    // ============ PLAYER-BASED ============

    fun getAssignmentsByPlayer(playerId: Int): Flow<List<ScoutAssignmentsEntity>> =
        scoutAssignmentsDao.getAssignmentsByPlayer(playerId)

    suspend fun getLatestReportForPlayer(playerId: Int): ScoutAssignmentsEntity? =
        scoutAssignmentsDao.getLatestReportForPlayer(playerId)

    // ============ AUTOMATIC REPORT GENERATION ============

    /**
     * Automatically generate scout report based on player attributes
     * This simulates the scout's evaluation
     */
    suspend fun autoGenerateScoutReport(assignmentId: Int): Boolean {
        val assignment = scoutAssignmentsDao.getById(assignmentId) ?: return false
        val player = playersRepository.getPlayerById(assignment.playerId) ?: return false

        // Scout's ability affects report accuracy
        val scout = staffRepository.getStaffById(assignment.scoutId)
        val scoutAbility = scout?.impactRating ?: 70

        // Calculate estimated value (with some randomness based on scout ability)
        val errorMargin = (100 - scoutAbility) / 200.0 // Higher ability = smaller error margin
        val baseValue = player.marketValue
        val valueVariance = (baseValue * (Random.nextDouble(-errorMargin, errorMargin))).toInt()
        val estimatedValue = (baseValue + valueVariance).coerceAtLeast(0)

        // Calculate scout rating (player's rating with some variance)
        val ratingError = (100 - scoutAbility) / 10
        val ratingVariance = if (ratingError > 0) Random.nextInt(-ratingError, ratingError + 1) else 0
        val scoutRating = (player.rating + ratingVariance).coerceIn(1, 99)

        // Generate strengths and weaknesses
        val strengths = generateStrengths(player)
        val weaknesses = generateWeaknesses(player)

        // Generate report text
        val report = generateScoutReportText(player, scoutRating, estimatedValue, strengths, weaknesses)

        // Determine verdict
        val verdict = when {
            scoutRating >= 80 -> ScoutVerdict.RECOMMENDED.value
            scoutRating >= 65 -> ScoutVerdict.WATCH.value
            else -> ScoutVerdict.NOT_RECOMMENDED.value
        }

        return completeAssignment(
            assignmentId = assignmentId,
            report = report,
            rating = scoutRating,
            estimatedValue = estimatedValue,
            potentialRating = player.potential,
            strengths = strengths,
            weaknesses = weaknesses,
            verdict = verdict
        )
    }

    private fun generateStrengths(player: PlayersEntity): List<String> {
        val strengths = mutableListOf<String>()

        if (player.finishing >= 75) strengths.add("Clinical finishing")
        if (player.passing >= 75) strengths.add("Excellent passer")
        if (player.dribbling >= 75) strengths.add("Dribbling ability")
        if (player.pace >= 75) strengths.add("Lightning pace")
        if (player.strength >= 75) strengths.add("Physical presence")
        if (player.leadership >= 75) strengths.add("Natural leader")
        if (player.composure >= 75) strengths.add("Cool under pressure")
        if (player.vision >= 75) strengths.add("Exceptional vision")
        if (player.defending >= 75) strengths.add("Strong tackler")
        if (player.heading >= 75) strengths.add("Aerial threat")

        // If no specific strengths, add generic ones
        if (strengths.isEmpty()) {
            strengths.add("Consistent performer")
            strengths.add("Good team player")
        }

        return strengths.take(3) // Return top 3 strengths
    }

    private fun generateWeaknesses(player: PlayersEntity): List<String> {
        val weaknesses = mutableListOf<String>()

        if (player.finishing <= 45) weaknesses.add("Poor finishing")
        if (player.passing <= 45) weaknesses.add("Inconsistent passing")
        if (player.dribbling <= 45) weaknesses.add("Loses possession easily")
        if (player.pace <= 45) weaknesses.add("Lacks pace")
        if (player.strength <= 45) weaknesses.add("Physically weak")
        if (player.composure <= 45) weaknesses.add("Easily rattled")
        if (player.decisions <= 45) weaknesses.add("Poor decision making")
        if (player.workRate == "LOW") weaknesses.add("Work rate questionable")

        // If no specific weaknesses, add generic ones
        if (weaknesses.isEmpty()) {
            weaknesses.add("Needs to develop consistency")
            weaknesses.add("Raw talent needs refinement")
        }

        return weaknesses.take(2) // Return top 2 weaknesses
    }

    private fun generateScoutReportText(
        player: PlayersEntity,
        scoutRating: Int,
        estimatedValue: Int,
        strengths: List<String>,
        weaknesses: List<String>
    ): String {
        return buildString {
            appendLine("SCOUTING REPORT: ${player.name}")
            appendLine()
            appendLine("Position: ${player.position} | Age: ${player.age}")
            appendLine("Current Club: ${player.teamName}")
            appendLine()
            appendLine("SCOUT ASSESSMENT")
            appendLine("Rating: ${scoutRating}/100")
            appendLine("Estimated Value: ${estimatedValue / 1_000_000}M")
            appendLine("Potential: ${player.potential}/100")
            appendLine()
            appendLine("STRENGTHS")
            strengths.forEach { appendLine("• $it") }
            appendLine()
            appendLine("WEAKNESSES")
            weaknesses.forEach { appendLine("• $it") }
            appendLine()
            appendLine("VERDICT: ${getVerdictText(scoutRating)}")
        }
    }

    private fun getVerdictText(rating: Int): String {
        return when {
            rating >= 85 -> "Elite talent - sign immediately if possible"
            rating >= 75 -> "Very good player - would strengthen squad"
            rating >= 65 -> "Decent player - good squad option"
            rating >= 55 -> "Development prospect - one for the future"
            else -> "Not recommended - look elsewhere"
        }
    }

    // ============ STATISTICS ============

    fun getScoutPerformanceStats(): Flow<List<ScoutPerformanceStats>> =
        scoutAssignmentsDao.getScoutPerformanceStats()

    fun getPriorityDistribution(): Flow<List<PriorityDistribution>> =
        scoutAssignmentsDao.getPriorityDistribution()

    // ============ DASHBOARD ============

    suspend fun getScoutAssignmentsDashboard(scoutId: Int): ScoutAssignmentsDashboard {
        val allAssignments = scoutAssignmentsDao.getAssignmentsByScout(scoutId).firstOrNull() ?: emptyList()
        val active = allAssignments.filter { it.reportStatus == ScoutReportStatus.IN_PROGRESS.value }
        val completed = allAssignments.filter { it.reportStatus == ScoutReportStatus.COMPLETED.value }
        val failed = allAssignments.filter { it.reportStatus == ScoutReportStatus.FAILED.value }

        val activeMission = scoutingMissionsDao.getActiveMissionByScout(scoutId)

        val highPriority = active.filter { it.isHighPriority }

        val avgRating = if (completed.isNotEmpty()) {
            completed.mapNotNull { it.scoutRating }.average()
        } else 0.0

        return ScoutAssignmentsDashboard(
            totalAssignments = allAssignments.size,
            activeAssignments = active.size,
            completedAssignments = completed.size,
            failedAssignments = failed.size,
            highPriorityAssignments = highPriority.size,
            averageScoutRating = avgRating,
            activeList = active,
            recentCompleted = completed.sortedByDescending { it.completionDate }.take(5),
            activeMission = activeMission
        )
    }
}

// ============ DATA CLASSES ============

data class ScoutAssignmentsDashboard(
    val totalAssignments: Int,
    val activeAssignments: Int,
    val completedAssignments: Int,
    val failedAssignments: Int,
    val highPriorityAssignments: Int,
    val averageScoutRating: Double,
    val activeList: List<ScoutAssignmentsEntity>,
    val recentCompleted: List<ScoutAssignmentsEntity>,
    val activeMission: ScoutingMissionsEntity? = null
)
