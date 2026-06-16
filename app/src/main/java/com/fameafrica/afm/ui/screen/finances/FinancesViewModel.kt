package com.fameafrica.afm.ui.screen.finances

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fameafrica.afm.data.repository.FinancesRepository
import com.fameafrica.afm.data.repository.SponsorsRepository
import com.fameafrica.afm.data.repository.TeamsRepository
import com.fameafrica.afm.domain.manager.GameManager
import com.fameafrica.afm.utils.formatters.CurrencyFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

// ============ UI MODELS ============

data class FinancesUiState(
    val isLoading: Boolean = true,
    val financialSummary: FinancialSummaryUiModel? = null,
    val budget: Long = 0,
    val bankBalance: Long = 0,
    val wageBill: Long = 0,
    val financialTier: String = "Unknown",
    val financialHealth: String = "Unknown",
    val isProfitable: Boolean = false,
    val revenueBreakdown: Map<String, Long> = emptyMap(),
    val expenseBreakdown: Map<String, Long> = emptyMap(),
    val profitLossHistory: List<ProfitLossEntry> = emptyList(),
    val sponsors: List<SponsorUiModel> = emptyList(),
    val leagueAverageRevenue: Long = 0,
    val leagueHighestRevenue: Long = 0,
    val currencyContext: CurrencyFormatter.CurrencyContext? = null
)

data class FinancialSummaryUiModel(
    val revenue: Long,
    val expenses: Long,
    val profitLoss: Long,
    val bankBalance: Long,
    val isProfitable: Boolean
)

data class ProfitLossEntry(
    val label: String,
    val amount: Long
)

data class SponsorUiModel(
    val id: Int,
    val name: String,
    val type: String,
    val annualValue: Long,
    val yearsRemaining: Int
)

@HiltViewModel
class FinancesViewModel @Inject constructor(
    private val gameManager: GameManager,
    private val financesRepository: FinancesRepository,
    private val sponsorsRepository: SponsorsRepository,
    private val teamsRepository: TeamsRepository,
    private val currencyFormatter: CurrencyFormatter
) : ViewModel() {

    private val _uiState = MutableStateFlow(FinancesUiState(isLoading = true))
    val uiState: StateFlow<FinancesUiState> = _uiState.asStateFlow()

    init {
        observeFinances()
    }

    private fun observeFinances() {
        viewModelScope.launch {
            // Combine game state and finances for real-time updates
            combine(
                gameManager.gameState,
                gameManager.currentFinances
            ) { state, finances ->
                Pair(state, finances)
            }.collect { (state, finances) ->
                when (state) {
                    is GameManager.GameState.Active -> {
                        if (finances != null) {
                            loadDetailedFinances(finances, state.context)
                        } else {
                            _uiState.update { it.copy(isLoading = false) }
                        }
                    }
                    is GameManager.GameState.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is GameManager.GameState.NoSave -> {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                    is GameManager.GameState.Error -> {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                }
            }
        }
    }

    private suspend fun loadDetailedFinances(
        finances: com.fameafrica.afm.data.database.entities.FinancesEntity,
        context: GameManager.GameContext
    ) {
        val currencyContext = currencyFormatter.getCurrentContext()
        val team = teamsRepository.getTeamById(context.teamId)

        val financeDashboard = financesRepository.getTeamFinanceDashboard(context.teamId, context.season)
        val sponsors = sponsorsRepository.getTeamSponsors(context.teamId).firstOrNull() ?: emptyList()

        val leagueName = team?.league ?: ""
        val allTeamsInLeague = teamsRepository.getTeamsByLeague(leagueName).firstOrNull() ?: emptyList()

        var leagueTotalRevenue = 0L
        var leagueMaxRevenue = 0L

        for (leagueTeam in allTeamsInLeague) {
            val teamFin = financesRepository.getTeamFinances(leagueTeam.id, context.season)
            val teamRevenue = teamFin?.revenue ?: 0
            leagueTotalRevenue += teamRevenue
            if (teamRevenue > leagueMaxRevenue) leagueMaxRevenue = teamRevenue
        }

        val leagueAverageRevenue = if (allTeamsInLeague.isNotEmpty()) leagueTotalRevenue / allTeamsInLeague.size else 0
        val profitLossHistory = generateProfitLossHistory(context.teamId, context.season)

        _uiState.update {
            it.copy(
                isLoading = false,
                financialSummary = FinancialSummaryUiModel(
                    revenue = financeDashboard.revenue,
                    expenses = financeDashboard.expenses,
                    profitLoss = financeDashboard.profitLoss,
                    bankBalance = financeDashboard.bankBalance,
                    isProfitable = financeDashboard.isProfitable
                ),
                budget = financeDashboard.budget,
                bankBalance = financeDashboard.bankBalance,
                wageBill = financeDashboard.expenseBreakdown["Player Wages"] ?: 0,
                financialTier = financeDashboard.financialTier,
                financialHealth = financeDashboard.financialHealth,
                isProfitable = financeDashboard.isProfitable,
                revenueBreakdown = financeDashboard.revenueBreakdown,
                expenseBreakdown = financeDashboard.expenseBreakdown,
                profitLossHistory = profitLossHistory,
                sponsors = sponsors.map { s ->
                    SponsorUiModel(
                        s.id,
                        s.name,
                        s.sponsorType,
                        s.sponsorshipValue,
                        s.contractRemainingYears
                    )
                },
                leagueAverageRevenue = leagueAverageRevenue,
                leagueHighestRevenue = leagueMaxRevenue,
                currencyContext = currencyContext
            )
        }
    }

    private suspend fun generateProfitLossHistory(teamId: Int, currentSeason: String): List<ProfitLossEntry> {
        val history = mutableListOf<ProfitLossEntry>()
        var season = currentSeason
        for (i in 0..4) {
            val fin = financesRepository.getTeamFinances(teamId, season)
            history.add(0, ProfitLossEntry(season.take(4), fin?.profitLoss ?: 0))
            season = getPreviousSeason(season)
        }
        return history
    }

    private fun getPreviousSeason(season: String): String {
        val parts = season.split("/")
        return if (parts.size == 2) {
            val startYear = parts[0].toInt() - 1
            val endYear = (startYear + 1).toString().takeLast(2)
            "$startYear/$endYear"
        } else season
    }

    fun requestBudgetIncrease() {
        viewModelScope.launch {
            val state = gameManager.gameState.value
            if (state is GameManager.GameState.Active) {
                val newBudget = (uiState.value.budget * 1.2).toLong()
                financesRepository.updateTransferBudget(state.context.teamId, state.context.season, newBudget)
                // Refresh data
                gameManager.currentFinances.value?.let { finances ->
                    loadDetailedFinances(finances, state.context)
                }
            }
        }
    }

    /**
     * FM-Level Depth: Simulates sponsor negotiation logic.
     * Success depends on club reputation and financial health.
     */
    fun renegotiateSponsor(sponsorId: Int) {
        viewModelScope.launch {
            val state = gameManager.gameState.value
            if (state is GameManager.GameState.Active) {
                val team = teamsRepository.getTeamById(state.context.teamId)
                val successChance = (team?.reputation ?: 50) / 100.0
                if (Random.nextDouble() < successChance) {
                    val currentSponsor = uiState.value.sponsors.find { it.id == sponsorId }
                    currentSponsor?.let {
                        val newValue = (it.annualValue * 1.15).toLong()
                        sponsorsRepository.updateSponsorshipValue(sponsorId, newValue)
                        // Refresh data
                        gameManager.currentFinances.value?.let { finances ->
                            loadDetailedFinances(finances, state.context)
                        }
                    }
                }
            }
        }
    }
}