package com.fameafrica.afm.data.repository

import com.fameafrica.afm.data.database.dao.ObjectivesDao
import com.fameafrica.afm.data.database.entities.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class ObjectivesRepository @Inject constructor(
    private val objectivesDaoProvider: Provider<ObjectivesDao>,
    private val teamsRepository: TeamsRepository
) {
    private val objectivesDao get() = objectivesDaoProvider.get()

    // ============ BASIC CRUD ============

    fun getAllObjectives(): Flow<List<ObjectivesEntity>> = objectivesDao.getAll()

    suspend fun getObjectiveById(id: Int): ObjectivesEntity? = objectivesDao.getById(id)

    suspend fun insertObjective(objective: ObjectivesEntity) = objectivesDao.insert(objective)

    suspend fun updateObjective(objective: ObjectivesEntity) = objectivesDao.update(objective)

    suspend fun deleteObjective(objective: ObjectivesEntity) = objectivesDao.delete(objective)

    // ============ TEAM-BASED ============

    fun getObjectivesByTeam(teamId: Int): Flow<List<ObjectivesEntity>> =
        objectivesDao.getObjectivesByTeam(teamId)

    fun getPendingObjectivesByTeam(teamId: Int): Flow<List<ObjectivesEntity>> =
        objectivesDao.getPendingObjectivesByTeam(teamId)

    suspend fun getPendingCountByTeam(teamId: Int): Int =
        objectivesDao.getPendingCountByTeam(teamId)

    // ============ OBJECTIVE GENERATION ============

    suspend fun generateSeasonObjectives(
        teamId: Int,
        teamName: String,
        season: String,
        leagueLevel: Int,
        clubReputation: Int
    ): List<ObjectivesEntity> {
        val objectives = mutableListOf<ObjectivesEntity>()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val endOfSeason = Calendar.getInstance().apply {
            set(Calendar.MONTH, Calendar.MAY)
            set(Calendar.DAY_OF_MONTH, 25)
        }.time

        // League Objective based on club reputation
        val leagueObjective = when {
            clubReputation >= 80 -> "Win the league"
            clubReputation >= 60 -> "Top 4 finish"
            clubReputation >= 40 -> "Top half finish"
            else -> "Avoid relegation"
        }

        val leagueTarget = when {
            clubReputation >= 80 -> "1"
            clubReputation >= 60 -> "4"
            clubReputation >= 40 -> "10"
            else -> "16"
        }

        objectives.add(
            ObjectivesEntity(
                teamId = teamId,
                teamName = teamName,
                season = season,
                objectiveType = ObjectiveType.LEAGUE.value,
                objective = leagueObjective,
                targetValue = leagueTarget,
                rewardType = RewardType.JOB_SECURITY.value,
                reward = "Board confidence increased",
                penaltyType = PenaltyType.SACKED.value,
                penalty = "Risk of being sacked",
                deadline = dateFormat.format(endOfSeason),
                status = ObjectiveStatus.PENDING.value
            )
        )

        // Cup objective (always try to reach later stages)
        objectives.add(
            ObjectivesEntity(
                teamId = teamId,
                teamName = teamName,
                season = season,
                objectiveType = ObjectiveType.CUP.value,
                objective = "Reach the quarter-finals",
                targetValue = "Quarter-final",
                rewardType = RewardType.FAN_LOYALTY.value,
                reward = "Fan support increases",
                penaltyType = PenaltyType.FAN_ANGER.value,
                penalty = "Fans disappointed",
                deadline = dateFormat.format(endOfSeason),
                status = ObjectiveStatus.PENDING.value
            )
        )

        // Financial objective for lower leagues
        if (leagueLevel >= 3) {
            objectives.add(
                ObjectivesEntity(
                    teamId = teamId,
                    teamName = teamName,
                    season = season,
                    objectiveType = ObjectiveType.FINANCIAL.value,
                    objective = "Maintain positive balance",
                    targetValue = "0",
                    rewardType = RewardType.TRANSFER_BUDGET.value,
                    reward = "Additional transfer funds",
                    penaltyType = PenaltyType.TRANSFER_BUDGET_CUT.value,
                    penalty = "Transfer budget reduced",
                    deadline = dateFormat.format(endOfSeason),
                    status = ObjectiveStatus.PENDING.value
                )
            )
        }

        // Youth objective for teams with good academy
        objectives.add(
            ObjectivesEntity(
                teamId = teamId,
                teamName = teamName,
                season = season,
                objectiveType = ObjectiveType.YOUTH.value,
                objective = "Promote at least 1 youth player",
                targetValue = "1",
                rewardType = RewardType.REPUTATION.value,
                reward = "Club reputation increases",
                penaltyType = PenaltyType.REPUTATION_DROP.value,
                penalty = "Reputation drops",
                deadline = dateFormat.format(endOfSeason),
                status = ObjectiveStatus.PENDING.value
            )
        )

        objectivesDao.insertAll(objectives)
        return objectives
    }

    suspend fun updateObjectiveProgress(
        objectiveId: Int,
        currentValue: String
    ): Boolean {
        val objective = objectivesDao.getById(objectiveId) ?: return false

        val updated = objective.copy(currentProgress = currentValue)
        objectivesDao.update(updated)

        // Check if objective is achieved
        checkObjectiveCompletion(objectiveId)

        return true
    }

    suspend fun checkObjectiveCompletion(objectiveId: Int) {
        val objective = objectivesDao.getById(objectiveId) ?: return

        val target = objective.targetValue?.toIntOrNull()
        val current = objective.currentProgress?.toIntOrNull()

        if (target != null && current != null && current >= target) {
            completeObjective(objectiveId)
        }
    }

    suspend fun completeObjective(objectiveId: Int): Boolean {
        val objective = objectivesDao.getById(objectiveId) ?: return false

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = dateFormat.format(Date())

        val updated = objective.copy(
            status = ObjectiveStatus.ACHIEVED.value,
            isCompleted = true,
            completionDate = today
        )

        objectivesDao.update(updated)
        return true
    }

    suspend fun failObjective(objectiveId: Int): Boolean {
        val objective = objectivesDao.getById(objectiveId) ?: return false

        val updated = objective.copy(
            status = ObjectiveStatus.FAILED.value,
            isCompleted = false
        )

        objectivesDao.update(updated)
        return true
    }

    // ============ DASHBOARD ============

    suspend fun getTeamObjectivesDashboard(teamId: Int): ObjectivesDashboard {
        val allObjectives = objectivesDao.getObjectivesByTeam(teamId).firstOrNull() ?: emptyList()
        val pending = allObjectives.filter { it.status == ObjectiveStatus.PENDING.value }
        val achieved = allObjectives.filter { it.status == ObjectiveStatus.ACHIEVED.value }
        val failed = allObjectives.filter { it.status == ObjectiveStatus.FAILED.value }

        val completionRate = if (allObjectives.isNotEmpty()) {
            (achieved.size.toDouble() / allObjectives.size * 100)
        } else 0.0

        return ObjectivesDashboard(
            totalObjectives = allObjectives.size,
            pendingObjectives = pending.size,
            achievedObjectives = achieved.size,
            failedObjectives = failed.size,
            completionRate = completionRate,
            pendingList = pending,
            recentCompleted = achieved.takeLast(5)
        )
    }
}

// ============ DATA CLASSES ============

data class ObjectivesDashboard(
    val totalObjectives: Int,
    val pendingObjectives: Int,
    val achievedObjectives: Int,
    val failedObjectives: Int,
    val completionRate: Double,
    val pendingList: List<ObjectivesEntity>,
    val recentCompleted: List<ObjectivesEntity>
)