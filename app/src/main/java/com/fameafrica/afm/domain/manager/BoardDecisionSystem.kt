package com.fameafrica.afm.domain.manager

import com.fameafrica.afm.data.database.entities.*
import com.fameafrica.afm.data.repository.*
import com.fameafrica.afm.utils.GameDateManager
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Shared API for Manager-Board interactions across AFM and AFC flavors.
 * Handles request submission, approval logic, and decision persistence.
 */
@Singleton
class BoardDecisionSystem @Inject constructor(
    private val boardRequestsRepository: BoardRequestsRepository,
    private val clubVisionRepository: ClubVisionRepository,
    private val financesRepository: FinancesRepository,
    private val teamsRepository: TeamsRepository,
    private val managersRepository: ManagersRepository,
    private val newsRepository: NewsRepository,
    private val gameDateManager: GameDateManager,
    private val infrastructureUpgradesRepository: InfrastructureUpgradesRepository
) {

    /**
     * Submits a request from a Manager to the Board.
     * Used by AFM flavor when a human manager requests something, 
     * or by the engine for AI managers.
     */
    suspend fun submitRequest(
        managerId: Int,
        teamId: Int,
        type: BoardRequestType,
        description: String
    ): BoardRequestsEntity {
        val manager = managersRepository.getManagerById(managerId) 
            ?: throw IllegalArgumentException("Manager not found")
        val team = teamsRepository.getTeamById(teamId)
            ?: throw IllegalArgumentException("Team not found")

        return boardRequestsRepository.createRequest(
            managerId = managerId,
            managerName = manager.name,
            teamId = teamId,
            teamName = team.name,
            requestType = type.value,
            description = description
        )
    }

    /**
     * Gets all pending requests for a specific team.
     * Used by AFC flavor to display what the AI Manager (or human manager in a multi-user future) wants.
     */
    fun getPendingRequests(teamId: Int): Flow<List<BoardRequestsEntity>> {
        return boardRequestsRepository.getPendingRequestsByTeam(teamId)
    }

    /**
     * Resolves a board request (Approve/Reject).
     * Used by AFC flavor for human Chairmen, or by the engine for AI Chairmen.
     */
    suspend fun resolveRequest(requestId: Int, approved: Boolean): Boolean {
        val request = boardRequestsRepository.getRequestById(requestId) ?: return false
        
        val result = if (approved) {
            val success = boardRequestsRepository.approveRequest(requestId)
            if (success) {
                applyApprovedRequestBenefits(request)
            }
            success
        } else {
            boardRequestsRepository.rejectRequest(requestId)
        }

        if (result) {
            generateDecisionNews(request, approved)
        }

        return result
    }

    /**
     * Processes effects of an approved request.
     * E.g., actually increasing the budget or starting a facility upgrade.
     */
    private suspend fun applyApprovedRequestBenefits(request: BoardRequestsEntity) {
        val currentSeason = "2026" // Should be dynamic from a game session manager if available

        when (request.requestType) {
            BoardRequestType.TRANSFER_BUDGET.value -> {
                val finances = financesRepository.getTeamFinances(request.teamId, currentSeason)
                if (finances != null) {
                    // Increase transfer budget by 20% of bank balance or a fixed amount for now
                    val increase = (finances.bankBalance * 0.1).toLong().coerceAtLeast(500_000L)
                    financesRepository.updateTransferBudget(request.teamId, currentSeason, finances.budget + increase)
                }
            }
            BoardRequestType.WAGE_BUDGET.value -> {
                val finances = financesRepository.getTeamFinances(request.teamId, currentSeason)
                if (finances != null) {
                    // Increase wage budget (represented by wageBill limit in some contexts, but here we just update the entity)
                    val increase = (finances.wageBill * 0.1).toLong().coerceAtLeast(50_000L)
                    val updatedFinances = finances.copy(wageBill = finances.wageBill + increase)
                    financesRepository.updateFinances(updatedFinances)
                }
            }
            BoardRequestType.YOUTH_FACILITIES.value -> {
                val currentLevel = infrastructureUpgradesRepository.getCurrentUpgradeLevel(request.teamId, "YOUTH_ACADEMY")
                infrastructureUpgradesRepository.initiateUpgrade(
                    teamName = request.teamName,
                    teamId = request.teamId,
                    upgradeType = "YOUTH_ACADEMY",
                    targetLevel = currentLevel + 1,
                    currentLevel = currentLevel
                )
            }
            BoardRequestType.TRAINING_FACILITIES.value -> {
                val currentLevel = infrastructureUpgradesRepository.getCurrentUpgradeLevel(request.teamId, "TRAINING_GROUND")
                infrastructureUpgradesRepository.initiateUpgrade(
                    teamName = request.teamName,
                    teamId = request.teamId,
                    upgradeType = "TRAINING_GROUND",
                    targetLevel = currentLevel + 1,
                    currentLevel = currentLevel
                )
            }
            BoardRequestType.STADIUM_EXPANSION.value -> {
                 val currentLevel = infrastructureUpgradesRepository.getCurrentUpgradeLevel(request.teamId, "STADIUM")
                infrastructureUpgradesRepository.initiateUpgrade(
                    teamName = request.teamName,
                    teamId = request.teamId,
                    upgradeType = "STADIUM",
                    targetLevel = currentLevel + 1,
                    currentLevel = currentLevel
                )
            }
        }
    }

    /**
     * Generates a news article about the board's decision.
     */
    private suspend fun generateDecisionNews(request: BoardRequestsEntity, approved: Boolean) {
        val details = if (approved) {
            "The board has approved the request from manager ${request.managerName} for a ${request.requestTypeDisplay.lowercase()}."
        } else {
            "The board has rejected the request from manager ${request.managerName} for a ${request.requestTypeDisplay.lowercase()}."
        }

        newsRepository.createBoardDecisionNews(
            teamName = request.teamName,
            teamId = request.teamId,
            decisionType = request.requestType,
            details = details,
            isPositive = approved
        )
    }

    /**
     * Checks if a request is likely to be approved by an AI board.
     * Based on Club Vision, Finances, and Manager Reputation.
     */
    suspend fun evaluateAIPotentialApproval(request: BoardRequestsEntity): Int {
        val team = teamsRepository.getTeamById(request.teamId) ?: return 0
        val currentSeason = "2026"
        val finances = financesRepository.getTeamFinances(request.teamId, currentSeason)
        val vision = clubVisionRepository.getVisionForTeamSync(request.teamId)
        
        var score = 50 // Base neutral score

        // 1. Finance Check (Heavy weight)
        if (finances != null) {
            if (finances.bankBalance < 0) score -= 40
            else if (finances.bankBalance > 10_000_000) score += 20
            
            // FFP check if applicable
            val ffp = financesRepository.getFFPStatus(request.teamId, currentSeason)
            if (!ffp.isCompliant) score -= 30
        }

        // 2. Vision & Alignment Check
        if (vision != null) {
            score += (vision.visionAlignment - 50) / 2
            score -= (vision.boardPressure - 50) / 2
            
            // Specific policy check
            if (request.requestType == BoardRequestType.YOUTH_FACILITIES.value && vision.youthExpectation > 70) {
                score += 15
            }
            if (request.requestType == BoardRequestType.TRANSFER_BUDGET.value && vision.financialDiscipline > 80) {
                score -= 15
            }
        }

        // 3. Manager Reputation
        val manager = managersRepository.getManagerById(request.managerId)
        if (manager != null) {
            score += (manager.reputation - 50) / 4
        }

        return score.coerceIn(0, 100)
    }
}
