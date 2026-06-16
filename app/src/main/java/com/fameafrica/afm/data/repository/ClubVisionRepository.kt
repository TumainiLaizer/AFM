package com.fameafrica.afm.data.repository

import com.fameafrica.afm.data.database.dao.ClubVisionDao
import com.fameafrica.afm.data.database.entities.ClubVisionEntity
import com.fameafrica.afm.data.database.entities.PlayersEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import kotlin.math.max
import kotlin.math.min

@Singleton
class ClubVisionRepository @Inject constructor(
    private val clubVisionDaoProvider: Provider<ClubVisionDao>,
    private val boardEvaluationRepository: BoardEvaluationRepository,
    private val teamsRepository: TeamsRepository
) {
    private val clubVisionDao get() = clubVisionDaoProvider.get()

    fun getVisionForTeam(teamId: Int): Flow<ClubVisionEntity?> =
        clubVisionDao.getVisionForTeam(teamId)

    suspend fun getVisionForTeamSync(teamId: Int): ClubVisionEntity? =
        clubVisionDao.getVisionForTeam(teamId).firstOrNull()

    suspend fun updateVision(vision: ClubVisionEntity) {
        clubVisionDao.update(vision)
    }

    // =========================================================
    // 🔥 SIGNING EVALUATION SYSTEM (NOW FULLY FUNCTIONAL)
    // =========================================================
    suspend fun evaluateSigning(teamId: Int, player: PlayersEntity) {

        val vision = clubVisionDao.getVisionForTeam(teamId).firstOrNull() ?: return
        val team = teamsRepository.getTeamById(teamId) ?: return
        val managerId = team.managerId ?: return

        var alignmentChange = 0
        var pressureChange = 0

        // 🎯 Transfer Policy Check
        val fitsPolicy = when (vision.transferExpectation) {
            "LOCAL_TALENT_ONLY" -> player.nationality == team.country
            "YOUTH" -> player.age <= 23
            "HIGH_REPUTATION" -> player.reputation > 70
            "FREE_AGENT_FOCUSED" -> player.contractStatus == "FREE_AGENT"
            else -> true
        }

        if (fitsPolicy) {
            alignmentChange += 5
            pressureChange -= 2
        } else {
            alignmentChange -= 8
            pressureChange += 5
        }

        // 💰 Financial Discipline Impact (rough proxy)
        if (player.marketValue > 0) {
            val financialPenalty = when {
                vision.financialDiscipline > 80 && player.marketValue > 5_000_000 -> 10
                vision.financialDiscipline > 60 && player.marketValue > 2_000_000 -> 5
                else -> 0
            }
            pressureChange += financialPenalty
        }

        applyVisionImpact(teamId, alignmentChange, pressureChange, managerId)
    }

    // =========================================================
    // 🔄 WEEKLY VISION ENGINE (CORE LOOP)
    // =========================================================
    suspend fun processWeeklyVisionUpdate(
        teamId: Int,
        performanceScore: Int,   // 0–100
        leaguePosition: Int,
        expectedPosition: Int,
        youthUsage: Int,
        managerStyle: String,
        transferType: String = "NEUTRAL"
    ) {

        val vision = clubVisionDao.getVisionForTeam(teamId).firstOrNull() ?: return
        val team = teamsRepository.getTeamById(teamId) ?: return
        val managerId = team.managerId ?: return

        // 1. Evaluate Alignment
        val alignmentScore = vision.evaluateManagerAlignment(
            managerStyle = managerStyle,
            youthUsage = youthUsage,
            transferType = transferType
        )

        // 2. Update Board Pressure
        val newPressure = vision.updateBoardPressure(
            currentPosition = leaguePosition,
            expectedPosition = expectedPosition,
            alignmentScore = alignmentScore
        )

        // 3. Update Job Security (inverse of pressure)
        val newSecurity = (100 - newPressure).coerceIn(0, 100)

        // 4. Persistence
        clubVisionDao.updateManagerState(
            teamId = teamId,
            philosophy = alignmentScore,
            alignment = alignmentScore,
            pressure = newPressure,
            security = newSecurity
        )

        // 5. Apply effects to the manager entity (Legacy support or double-sync)
        applyVisionImpact(teamId, alignmentScore - vision.visionAlignment, newPressure - vision.boardPressure, managerId)
    }

    // =========================================================
    // 🧠 CORE IMPACT ENGINE (REUSABLE)
    // =========================================================
    private suspend fun applyVisionImpact(
        teamId: Int,
        alignmentDelta: Int,
        pressureDelta: Int,
        managerId: Int
    ) {

        val vision = clubVisionDao.getVisionForTeam(teamId).firstOrNull() ?: return

        val newAlignment = (vision.visionAlignment + alignmentDelta).coerceIn(0, 100)
        val newPressure = (vision.boardPressure + pressureDelta).coerceIn(0, 100)

        // 🎯 Job Security Logic
        val newSecurity = when {
            newPressure > 85 -> max(10, vision.jobSecurity - 15)
            newPressure > 70 -> max(20, vision.jobSecurity - 10)
            newPressure < 30 -> min(100, vision.jobSecurity + 5)
            else -> vision.jobSecurity
        }

        clubVisionDao.updateManagerState(
            teamId = teamId,
            philosophy = newAlignment,
            alignment = newAlignment,
            pressure = newPressure,
            security = newSecurity
        )

        // 🔥 Sync with Board System
        boardEvaluationRepository.updateBoardSatisfaction(
            managerId,
            (100 - newPressure)
        )
    }
}