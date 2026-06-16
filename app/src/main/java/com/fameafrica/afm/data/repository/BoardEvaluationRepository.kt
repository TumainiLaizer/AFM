package com.fameafrica.afm.data.repository

import com.fameafrica.afm.data.database.dao.BoardEvaluationDao
import com.fameafrica.afm.data.database.dao.EvaluationWithDetails
import com.fameafrica.afm.data.database.entities.BoardEvaluationEntity
import com.fameafrica.afm.data.database.entities.FinancialBehavior
import com.fameafrica.afm.data.database.model.enums.BoardStatus
import com.fameafrica.afm.data.database.model.enums.FinancialStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class BoardEvaluationRepository @Inject constructor(
    private val boardEvaluationDaoProvider: Provider<BoardEvaluationDao>,
    private val financesDaoProvider: Provider<com.fameafrica.afm.data.database.dao.FinancesDao>,
    private val teamsDaoProvider: Provider<com.fameafrica.afm.data.database.dao.TeamsDao>,
    private val managersDaoProvider: Provider<com.fameafrica.afm.data.database.dao.ManagersDao>,
    private val chairmanDaoProvider: Provider<com.fameafrica.afm.data.database.dao.ChairmanDao>,
    private val clubDNARepository: ClubDNARepository,
    private val leagueStandingsRepository: LeagueStandingsRepository,
    private val newsRepository: Provider<NewsRepository>
) {
    private val boardEvaluationDao get() = boardEvaluationDaoProvider.get()
    private val financesDao get() = financesDaoProvider.get()
    private val teamsDao get() = teamsDaoProvider.get()
    private val managersDao get() = managersDaoProvider.get()
    private val chairmanDao get() = chairmanDaoProvider.get()

    // ============ BASIC CRUD ============

    fun getAllEvaluations(): Flow<List<BoardEvaluationEntity>> = boardEvaluationDao.getAll()

    suspend fun getEvaluationById(id: Int): BoardEvaluationEntity? = boardEvaluationDao.getById(id)

    suspend fun getEvaluationByManagerId(managerId: Int): BoardEvaluationEntity? =
        boardEvaluationDao.getByManagerId(managerId)

    suspend fun insertEvaluation(evaluation: BoardEvaluationEntity) = boardEvaluationDao.insert(evaluation)

    suspend fun updateEvaluation(evaluation: BoardEvaluationEntity) = boardEvaluationDao.update(evaluation)

    suspend fun deleteEvaluation(evaluation: BoardEvaluationEntity) = boardEvaluationDao.delete(evaluation)

    suspend fun performMonthlyReview(managerId: Int, teamId: Int, season: String): List<com.fameafrica.afm.domain.model.SimulationEvent> {
        val finances = financesDao.getByTeamAndSeason(teamId, season)
        val team = teamsDao.getById(teamId)
        val manager = managersDao.getById(managerId)
        val dna = clubDNARepository.getClubDNA(teamId)
        val chairman = chairmanDao.getByTeamId(teamId)

        var satisfactionAdjustment = 0
        var patiencePressure = 0
        var ambitionPressure = 0

        // 1. Chairman Personality Influences
        if (chairman != null) {
            // Patience: Lower patience means higher negative adjustments for poor results
            val patience = chairman.patienceLevel
            if (patience < 40) patiencePressure = (50 - patience) / 5
            
            // Ambition: Higher ambition means higher pressure for top results
            val ambition = chairman.ambitionLevel
            if (ambition > 70) ambitionPressure = (ambition - 70) / 5
        }

        // 2. Financial Evaluation
        if (finances != null && dna != null) {
            val wageBillRatio = if (finances.revenue > 0) finances.wageBill.toDouble() / finances.revenue.toDouble() else 1.0
            
            // Financial satisfaction based on DNA behavior
            when (dna.financialBehavior) {
                FinancialBehavior.FRUGAL -> {
                    if (finances.profitLoss > 0) satisfactionAdjustment += 5
                    if (wageBillRatio > 0.6) satisfactionAdjustment -= 10
                }
                FinancialBehavior.SPENDER, FinancialBehavior.RISKY -> {
                    if (finances.bankBalance < -1_000_000) satisfactionAdjustment -= 15
                    // Spenders care less about profit as long as they have some balance
                }
                else -> {}
            }

            val financialStatus = when {
                finances.bankBalance > 10_000_000 -> _root_ide_package_.com.fameafrica.afm.data.database.model.enums.FinancialStatus.RICH.value
                finances.profitLoss > 0 && wageBillRatio < 0.7 -> _root_ide_package_.com.fameafrica.afm.data.database.model.enums.FinancialStatus.STABLE.value
                wageBillRatio > 0.9 || finances.bankBalance < 0 -> _root_ide_package_.com.fameafrica.afm.data.database.model.enums.FinancialStatus.IN_DEBT.value
                else -> _root_ide_package_.com.fameafrica.afm.data.database.model.enums.FinancialStatus.STABLE.value
            }
            
            if (financialStatus == _root_ide_package_.com.fameafrica.afm.data.database.model.enums.FinancialStatus.IN_DEBT.value) satisfactionAdjustment -= 5
            if (financialStatus == _root_ide_package_.com.fameafrica.afm.data.database.model.enums.FinancialStatus.RICH.value) satisfactionAdjustment += 2
            
            updateFinancialStatus(managerId, financialStatus)
        }

        // 2. DNA Alignment Evaluation
        if (manager != null && dna != null) {
            val alignmentScore = dna.calculateManagerCompatibility(manager.style)
            val alignmentInt = (alignmentScore * 100).toInt()
            
            // Update the alignment in the evaluation record
            val evaluation = boardEvaluationDao.getByManagerId(managerId)
            evaluation?.let {
                boardEvaluationDao.update(it.copy(dnaAlignment = alignmentInt))
            }

            // High alignment provides a buffer for bad results, low alignment accelerates sacking
            if (alignmentScore < 0.4) satisfactionAdjustment -= 5
            if (alignmentScore > 0.8) satisfactionAdjustment += 3
        }

        // 3. Performance Evaluation (Granular PPG vs Expectations)
        var performanceScore = 50.0
        val seasonYear = season.split("/").first().toInt()
        val teamStanding = team?.let { leagueStandingsRepository.getTeamStanding(it.id, it.league, seasonYear) }
        
        teamStanding?.let { s ->
            val ppg = s.pointsPerGame
            val expectedPPG = (team.reputation / 100.0) * 2.2
            
            val diff = ppg - expectedPPG
            performanceScore += diff * 40.0
        }
        
        // 4. Transfer Market Success (Selling high is good for board)
        var transferImpact = 0
        if (finances != null) {
            val netSpend = finances.transferSpending - finances.playerSales
            if (netSpend < 0) transferImpact += 10 // Profit from transfers
            if (netSpend > (finances.budget / 2)) transferImpact -= 15 // Overspending
        }

        // 5. Apply final adjustment
        val performanceImpact = ((performanceScore - 50) / 10).toInt()
        satisfactionAdjustment += performanceImpact + transferImpact

        val finalAdjustment = satisfactionAdjustment - patiencePressure - ambitionPressure
        
        val currentEvaluation = boardEvaluationDao.getByManagerId(managerId)
        currentEvaluation?.let {
            boardEvaluationDao.update(it.copy(
                chairmanId = chairman?.id,
                patiencePressure = patiencePressure,
                ambitionPressure = ambitionPressure,
                boardSatisfaction = (it.boardSatisfaction + finalAdjustment).coerceIn(0, 100)
            ))
        }

        return evaluateBoardStatus(managerId)
    }

    // ============ SATISFACTION MANAGEMENT ============

    suspend fun updateBoardSatisfaction(managerId: Int, newSatisfaction: Int) {
        val evaluation = boardEvaluationDao.getByManagerId(managerId)
        evaluation?.let {
            val updated = it.copy(boardSatisfaction = newSatisfaction.coerceIn(0, 100))
            boardEvaluationDao.update(updated)
        }
    }

    suspend fun adjustBoardSatisfaction(managerId: Int, adjustment: Int) {
        val evaluation = boardEvaluationDao.getByManagerId(managerId)
        evaluation?.let {
            val team = teamsDao.getTeamByManager(managerId)
            val dna = team?.let { t -> clubDNARepository.getClubDNA(t.id) }
            
            // DNA Factor: If result matches DNA (e.g. winning with "Tiki-Taka" when expected), bonus is higher
            var modifiedAdjustment = adjustment
            if (adjustment > 0 && dna != null) {
                modifiedAdjustment += (dna.identityStrength / 50)
            }
            
            val newSatisfaction = (evaluation.boardSatisfaction + modifiedAdjustment).coerceIn(0, 100)
            val updated = it.copy(boardSatisfaction = newSatisfaction)
            boardEvaluationDao.update(updated)
            
            // Sync with Manager Entity
            managersDao.getById(managerId)?.let { manager ->
                managersDao.update(manager.copy(jobSecurity = newSatisfaction))
            }
        }
    }

    // ============ STATUS MANAGEMENT ============

    suspend fun updateBoardStatus(managerId: Int, newStatus: String) {
        val evaluation = boardEvaluationDao.getByManagerId(managerId)
        evaluation?.let {
            val updated = it.copy(status = newStatus)
            boardEvaluationDao.update(updated)
        }
    }

    suspend fun evaluateBoardStatus(managerId: Int): List<com.fameafrica.afm.domain.model.SimulationEvent> {
        val evaluation = boardEvaluationDao.getByManagerId(managerId) ?: return emptyList()
        val events = mutableListOf<com.fameafrica.afm.domain.model.SimulationEvent>()

        val newStatus = when {
            evaluation.boardSatisfaction >= 60 -> _root_ide_package_.com.fameafrica.afm.data.database.model.enums.BoardStatus.SAFE.value
            evaluation.boardSatisfaction >= 45 -> _root_ide_package_.com.fameafrica.afm.data.database.model.enums.BoardStatus.UNDER_REVIEW.value
            evaluation.boardSatisfaction >= 30 -> _root_ide_package_.com.fameafrica.afm.data.database.model.enums.BoardStatus.ON_THIN_ICE.value
            evaluation.boardSatisfaction >= 15 -> _root_ide_package_.com.fameafrica.afm.data.database.model.enums.BoardStatus.CRITICAL.value
            else -> _root_ide_package_.com.fameafrica.afm.data.database.model.enums.BoardStatus.SACKED.value
        }

        if (evaluation.status != newStatus) {
            updateBoardStatus(managerId, newStatus)
            
            // Trigger specific events based on status change
            when (newStatus) {
                _root_ide_package_.com.fameafrica.afm.data.database.model.enums.BoardStatus.CRITICAL.value -> {
                    triggerBoardMeeting(managerId)
                    events.add(com.fameafrica.afm.domain.model.SimulationEvent.BoardMeeting(
                        title = "Critical Performance Review",
                        message = "The board is extremely disappointed with recent results. Your job is at immediate risk."
                    ))
                }
                _root_ide_package_.com.fameafrica.afm.data.database.model.enums.BoardStatus.ON_THIN_ICE.value -> triggerMediaLeaks(managerId)
                _root_ide_package_.com.fameafrica.afm.data.database.model.enums.BoardStatus.SACKED.value -> {
                    processDismissal(managerId)
                    events.add(com.fameafrica.afm.domain.model.SimulationEvent.BoardMeeting(
                        title = "Termination of Contract",
                        message = "The board has decided to terminate your contract effective immediately."
                    ))
                }
            }
        }
        return events
    }

    private suspend fun triggerBoardMeeting(managerId: Int) {
        val team = teamsDao.getTeamByManager(managerId) ?: return
        
        if (team.reputation > 60) {
            val newsRepo = newsRepository.get()
            newsRepo.createNewsArticle(
                headline = "CRISIS TALKS: Board meeting scheduled for ${team.name} manager",
                content = "With the club currently underperforming, the board of ${team.name} has called an emergency meeting to discuss the future of manager ${managerId}. Speculation is mounting that a change could be imminent.",
                category = "MANAGER",
                relatedTeamId = team.id,
                relatedTeam = team.name,
                relatedManagerId = managerId,
                isTopNews = true
            )
        }
    }

    private suspend fun triggerMediaLeaks(managerId: Int) {
        val team = teamsDao.getTeamByManager(managerId) ?: return
        if (Random.nextInt(100) < 40) {
            val newsRepo = newsRepository.get()
            newsRepo.createNewsArticle(
                headline = "LEAK: Board losing patience with ${team.name} manager",
                content = "Sources close to the ${team.name} board suggest that faith in the current management is at an all-time low. While no official statement has been made, insiders believe the next few results will be crucial.",
                category = "MANAGER",
                relatedTeamId = team.id,
                relatedTeam = team.name,
                relatedManagerId = managerId,
                isTopNews = false
            )
        }
    }

    suspend fun processDismissal(managerId: Int) {
        val manager = managersDao.getById(managerId) ?: return
        val team = teamsDao.getTeamByManager(managerId) ?: return
        val newsRepo = newsRepository.get()

        // Create News before updating IDs
        newsRepo.createSackingNews(
            managerName = manager.name,
            managerId = manager.id,
            teamName = team.name,
            teamId = team.id,
            reason = "unacceptable results and poor alignment with board vision"
        )

        // 1. Update Manager Status
        managersDao.update(manager.copy(
            teamId = null,
            contractEndDate = null,
            jobSecurity = 0,
            previousClub = team.name
        ))

        // 2. Update Team Status
        teamsDao.update(team.copy(managerId = null))
    }

    // ============ FINANCIAL STATUS ============

    suspend fun updateFinancialStatus(managerId: Int, financialStatus: String) {
        val evaluation = boardEvaluationDao.getByManagerId(managerId)
        evaluation?.let {
            val updated = it.copy(financialStatus = financialStatus)
            boardEvaluationDao.update(updated)
        }
    }

    // ============ RECENT RESULTS ============

    suspend fun updateRecentResults(managerId: Int, resultsJson: String) {
        val evaluation = boardEvaluationDao.getByManagerId(managerId)
        evaluation?.let {
            val updated = it.copy(recentResults = resultsJson)
            boardEvaluationDao.update(updated)
        }
    }

    suspend fun addMatchResult(managerId: Int, result: String) {
        val evaluation = boardEvaluationDao.getByManagerId(managerId) ?: return

        // Parse existing results
        val currentResults = evaluation.recentResults?.let {
            if (it.startsWith("[") && it.endsWith("]")) {
                it.substring(1, it.length - 1).split(",").map { s -> s.trim('"') }.filter { s -> s.isNotEmpty() }
            } else emptyList()
        } ?: emptyList()

        // Add new result, keep last 5
        val updatedResults = (currentResults + result).takeLast(5)
        val resultsJson = "[\"${updatedResults.joinToString("\",\"")}\"]"

        val updated = evaluation.copy(recentResults = resultsJson)
        boardEvaluationDao.update(updated)
    }

    // ============ INITIALIZATION ============

    suspend fun initializeBoardEvaluation(managerId: Int, managerName: String): BoardEvaluationEntity {
        val existing = boardEvaluationDao.getByManagerId(managerId)

        return if (existing != null) {
            existing
        } else {
            val evaluation = BoardEvaluationEntity(
                managerId = managerId,
                managerName = managerName,
                boardSatisfaction = 50,
                financialStatus = _root_ide_package_.com.fameafrica.afm.data.database.model.enums.FinancialStatus.STABLE.value,
                status = _root_ide_package_.com.fameafrica.afm.data.database.model.enums.BoardStatus.SAFE.value
            )
            boardEvaluationDao.insert(evaluation)
            evaluation
        }
    }

    // ============ JOINED DATA ============

    suspend fun getBoardEvaluationWithDetails(managerId: Int): EvaluationWithDetails? =
        boardEvaluationDao.getBoardEvaluationWithDetails(managerId)

    fun getAllEvaluationsWithDetails(): Flow<List<EvaluationWithDetails>> =
        boardEvaluationDao.getAllEvaluationsWithDetails()

    // ============ DASHBOARD ============

    suspend fun getBoardDashboard(managerId: Int): BoardDashboard {
        val evaluation = boardEvaluationDao.getByManagerId(managerId)
            ?: return BoardDashboard.empty()

        val details = boardEvaluationDao.getBoardEvaluationWithDetails(managerId)

        return BoardDashboard(
            managerName = evaluation.managerName,
            boardSatisfaction = evaluation.boardSatisfaction,
            satisfactionLevel = evaluation.satisfactionLevel,
            status = evaluation.status,
            financialStatus = evaluation.financialStatus ?: "Unknown",
            recentResults = evaluation.recentResults,
            dnaAlignment = evaluation.dnaAlignment,
            teamName = details?.teamName,
            teamLeague = details?.teamLeague,
            isAtRisk = evaluation.status in listOf(
                _root_ide_package_.com.fameafrica.afm.data.database.model.enums.BoardStatus.UNDER_REVIEW.value,
                _root_ide_package_.com.fameafrica.afm.data.database.model.enums.BoardStatus.ON_THIN_ICE.value,
                _root_ide_package_.com.fameafrica.afm.data.database.model.enums.BoardStatus.CRITICAL.value
            )
        )
    }
}

// ============ DATA CLASSES ============

data class BoardDashboard(
    val managerName: String,
    val boardSatisfaction: Int,
    val satisfactionLevel: String,
    val status: String,
    val financialStatus: String,
    val recentResults: String?,
    val dnaAlignment: Int,
    val teamName: String?,
    val teamLeague: String?,
    val isAtRisk: Boolean
) {
    companion object {
        fun empty(): BoardDashboard = BoardDashboard(
            managerName = "",
            boardSatisfaction = 0,
            satisfactionLevel = "",
            status = "",
            financialStatus = "",
            recentResults = null,
            dnaAlignment = 0,
            teamName = null,
            teamLeague = null,
            isAtRisk = false
        )
    }
}
