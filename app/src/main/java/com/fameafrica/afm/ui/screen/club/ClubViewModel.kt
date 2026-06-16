package com.fameafrica.afm.ui.screen.club

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fameafrica.afm.data.database.entities.ClubDNAEntity
import com.fameafrica.afm.data.database.entities.FinancesEntity
import com.fameafrica.afm.data.database.entities.LeaguesEntity
import com.fameafrica.afm.data.database.entities.ObjectivesEntity
import com.fameafrica.afm.data.database.entities.SeasonHistoryEntity
import com.fameafrica.afm.data.repository.*
import com.fameafrica.afm.domain.manager.GameManager
import com.fameafrica.afm.ui.theme.ClubThemeConfig
import com.fameafrica.afm.ui.theme.ClubThemeManager
import com.fameafrica.afm.ui.theme.FameColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

// ============ UI MODELS ============

data class ClubUiState(
    val isLoading: Boolean = true,
    val clubName: String = "",
    val reputationLevel: String = "Local",
    val clubInfo: ClubInfoUiModel? = null,
    val finances: FinancialUiModel? = null,
    val infrastructure: InfrastructureUiModel? = null,
    val revenueBreakdown: List<RevenueItemUiModel> = emptyList(),
    val sponsors: List<SponsorUiModel> = emptyList(),
    val activeUpgrades: List<UpgradeUiModel> = emptyList(),
    val recentHistory: List<HistoryUiModel> = emptyList(),
    val legends: List<LegendUiModel> = emptyList(),
    val quickStats: QuickStatsUiModel? = null,
    val objectives: List<ObjectivesEntity> = emptyList(),
    val dna: ClubDNAEntity? = null,
    val theme: ClubThemeConfig? = null
)

data class ClubInfoUiModel(
    val id: Int,
    val name: String,
    val league: String,
    val stadium: String,
    val stadiumCapacity: Int,
    val logoUrl: String?,
    val reputationLevel: String,
    val reputation: Int
)

data class FinancialUiModel(
    val revenue: Long,
    val revenueChange: Double,
    val expenses: Long,
    val expensesChange: Double,
    val profit: Long,
    val profitChange: Double,
    val budget: Long,
    val budgetUsed: Float
)

data class InfrastructureUiModel(
    val stadiumLevel: Int,
    val stadiumCapacity: Int,
    val trainingLevel: Int,
    val trainingEfficiency: Int,
    val youthLevel: Int,
    val youthTalent: Int,
    val medicalLevel: Int,
    val injuryRecovery: Int
)

data class RevenueItemUiModel(
    val label: String,
    val amount: Long,
    val percentage: Int,
    val color: Color
)

data class SponsorUiModel(
    val id: Int,
    val name: String,
    val type: String,
    val value: Long,
    val logoUrl: String?
)

data class UpgradeUiModel(
    val id: Int,
    val name: String,
    val type: String,
    val currentLevel: Int,
    val targetLevel: Int,
    val progress: Int,
    val remainingDays: String
)

data class HistoryUiModel(
    val id: Int,
    val title: String,
    val season: String,
    val achievement: String,
    val type: String
)

data class LegendUiModel(
    val id: Int,
    val name: String,
    val era: String,
    val titles: Int
)

data class QuickStatsUiModel(
    val leaguePosition: Int,
    val overallStars: Int,
    val fanLoyalty: Int,
    val stadiumCapacity: Int,
    val seasons: Int
)

@HiltViewModel
class ClubViewModel @Inject constructor(
    private val teamsRepository: TeamsRepository,
    private val financesRepository: FinancesRepository,
    private val infrastructureRepository: InfrastructureUpgradesRepository,
    private val sponsorsRepository: SponsorsRepository,
    private val seasonHistoryRepository: SeasonHistoryRepository,
    private val clubLegendsRepository: ClubLegendsRepository,
    private val leagueStandingsRepository: LeagueStandingsRepository,
    private val leaguesRepository: LeaguesRepository,
    private val objectivesRepository: ObjectivesRepository,
    private val clubDNARepository: ClubDNARepository,
    private val gameManager: GameManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClubUiState(isLoading = true))
    val uiState: StateFlow<ClubUiState> = _uiState.asStateFlow()

    init {
        observeGameManager()
    }

    private fun observeGameManager() {
        viewModelScope.launch {
            // FM-Level Depth: React to world simulation updates for real-time club status
            gameManager.gameState.collect { state ->
                if (state is GameManager.GameState.Active) {
                    val context = state.context
                    loadClubData(context)
                }
            }
        }
    }

    private suspend fun loadClubData(context: GameManager.GameContext) {
        val teamId = context.teamId
        val teamName = context.teamName
        val season = context.season
        
        val team = teamsRepository.getTeamById(teamId) ?: return

        // Fetching breakdowns and comparison data (Global World Simulation)
        val finances = financesRepository.getTeamFinances(teamId, season)
        val previousSeasonFinances = financesRepository.getTeamFinances(teamId, calculatePreviousSeason(season))
        val upgrades = infrastructureRepository.getTeamUpgrades(teamId).firstOrNull() ?: emptyList()

        val stadiumLevel = infrastructureRepository.getCurrentUpgradeLevel(teamId, "STADIUM")
        val trainingLevel = infrastructureRepository.getCurrentUpgradeLevel(teamId, "TRAINING_FACILITY")
        val youthLevel = infrastructureRepository.getCurrentUpgradeLevel(teamId, "YOUTH_ACADEMY")
        val medicalLevel = infrastructureRepository.getCurrentUpgradeLevel(teamId, "MEDICAL_CENTER")

        val sponsors = sponsorsRepository.getTeamSponsors(teamId).firstOrNull() ?: emptyList()
        val history = seasonHistoryRepository.getTeamHistory(teamId).firstOrNull() ?: emptyList()
        val legends = clubLegendsRepository.getLegendsByClub(teamId).firstOrNull() ?: emptyList()
        val leaguePosition = getLeaguePosition(team.league ?: "", season, teamId)
        val objectives = objectivesRepository.getObjectivesByTeam(teamId).firstOrNull() ?: emptyList()
        val dna = clubDNARepository.getClubDNA(teamId)

        _uiState.update {
            it.copy(
                isLoading = false,
                clubName = team.name,
                reputationLevel = getReputationLevel(team.reputation),
                dna = dna,
                clubInfo = ClubInfoUiModel(
                    id = team.id, name = team.name, league = team.league ?: "No League",
                    stadium = team.homeStadium, stadiumCapacity = team.stadiumCapacity,
                    logoUrl = team.logoPath, reputationLevel = getReputationLevel(team.reputation),
                    reputation = team.reputation
                ),
                finances = finances?.let { f ->
                    FinancialUiModel(
                        revenue = f.revenue,
                        revenueChange = calculateYearOverYearChange(f.revenue, previousSeasonFinances?.revenue),
                        expenses = f.expenses,
                        expensesChange = calculateYearOverYearChange(f.expenses, previousSeasonFinances?.expenses),
                        profit = f.profitLoss,
                        profitChange = calculateYearOverYearChange(f.profitLoss, previousSeasonFinances?.profitLoss),
                        budget = f.budget,
                        budgetUsed = if (f.budget > 0) (f.transferSpending.toFloat() / f.budget.toFloat()).coerceIn(0f, 1f) else 0f
                    )
                },
                infrastructure = InfrastructureUiModel(
                    stadiumLevel = stadiumLevel, stadiumCapacity = team.stadiumCapacity,
                    trainingLevel = trainingLevel, trainingEfficiency = 70 + (trainingLevel * 5),
                    youthLevel = youthLevel, youthTalent = 50 + (youthLevel * 3),
                    medicalLevel = medicalLevel, injuryRecovery = 60 + (medicalLevel * 4)
                ),
                revenueBreakdown = calculateRevenueBreakdown(finances),
                sponsors = sponsors.map { s -> SponsorUiModel(s.id, s.name, s.sponsorType, s.sponsorshipValue, s.logo) },
                activeUpgrades = upgrades.filter { u -> u.status == "In Progress" || u.status == "Pending" }.map { u ->
                    UpgradeUiModel(u.id, formatUpgradeName(u.upgradeType), u.upgradeType, u.upgradeLevel, u.targetLevel, calculateProgress(u.startDate, u.completionDate), calculateRemainingDays(u.completionDate))
                },
                recentHistory = history.take(5).map { h ->
                    HistoryUiModel(h.id, h.leagueName ?: "Season ${h.season}", h.season, formatAchievement(h, h.leagueName?.let { l -> leaguesRepository.getLeagueByName(l) }), if (h.trophiesWon > 0) "Trophy" else "League")
                },
                legends = legends.map { l -> LegendUiModel(l.id, l.playerName, "${l.yearsPlayed} years", l.majorTitlesWon) },
                quickStats = QuickStatsUiModel(leaguePosition, (team.reputation / 20).coerceIn(1, 5), team.fanLoyalty, team.stadiumCapacity, history.size),
                objectives = objectives,
                theme = ClubThemeManager.getThemeForTeam(team.name)
            )
        }
    }

    private suspend fun getLeaguePosition(leagueName: String, season: String, teamId: Int): Int {
        if (leagueName.isBlank()) return 0
        val seasonYear = try { season.split("/").first().toInt() } catch (e: Exception) { 2024 }
        val standings = leagueStandingsRepository.getStandingsByPosition(leagueName, seasonYear).firstOrNull() ?: emptyList()
        return standings.indexOfFirst { it.teamId == teamId } + 1
    }

    private fun getReputationLevel(reputation: Int): String = when {
        reputation >= 85 -> "African Legend"
        reputation >= 70 -> "Continental"
        reputation >= 50 -> "National"
        else -> "Local"
    }

    private fun calculateRevenueBreakdown(finances: FinancesEntity?): List<RevenueItemUiModel> {
        if (finances == null || finances.revenue == 0L) return emptyList()
        val items = mutableListOf<RevenueItemUiModel>()
        val total = finances.revenue
        val sources = listOf(
            Triple("Sponsorship", finances.sponsorshipRevenue, FameColors.ChampionsGold),
            Triple("Broadcasting", finances.broadcastingRevenue, FameColors.PitchGreen),
            Triple("Matchday", finances.matchdayRevenue, FameColors.AfroSunOrange),
            Triple("Merchandise", finances.merchandiseRevenue, FameColors.BaobabBrown),
            Triple("Prize Money", finances.prizeMoney + finances.continentalPrizeMoney, Color.Gray),
            Triple("Player Sales", finances.playerSales, FameColors.KenteRed)
        )
        sources.forEach { (label, amount, color) ->
            if (amount > 0) items.add(RevenueItemUiModel(label, amount, ((amount.toDouble() / total) * 100).toInt(), color))
        }
        return items.sortedByDescending { it.amount }
    }

    private fun calculateYearOverYearChange(current: Long, previous: Long?): Double {
        if (previous == null || previous == 0L) return 0.0
        return ((current - previous).toDouble() / previous) * 100
    }

    private fun calculatePreviousSeason(currentSeason: String): String {
        val parts = currentSeason.split("/")
        return if (parts.size == 2) "${parts[0].toInt() - 1}/${(parts[0].toInt()).toString().takeLast(2)}" else currentSeason
    }

    private fun formatUpgradeName(type: String): String = when (type) {
        "STADIUM" -> "Stadium Expansion"
        "TRAINING_FACILITY" -> "Training Facility Upgrade"
        "YOUTH_ACADEMY" -> "Youth Academy Upgrade"
        "MEDICAL_CENTER" -> "Medical Center Upgrade"
        else -> type.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
    }

    private fun calculateRemainingDays(completionDate: String): String {
        if (completionDate.isBlank()) return "TBD"
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return try {
            val end = format.parse(completionDate)?.time ?: return "TBD"
            val diffDays = (end - Date().time) / (1000 * 60 * 60 * 24)
            when {
                diffDays <= 0 -> "Complete"
                diffDays < 7 -> "$diffDays days"
                diffDays < 30 -> "${diffDays / 7} weeks"
                else -> "${diffDays / 30} months"
            }
        } catch (e: Exception) { "TBD" }
    }

    private fun calculateProgress(startDate: String, completionDate: String): Int {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return try {
            val start = format.parse(startDate)?.time ?: return 0
            val end = format.parse(completionDate)?.time ?: return 0
            val now = Date().time
            if (now >= end) 100 else if (now <= start) 0 else ((now - start).toDouble() / (end - start) * 100).toInt().coerceIn(0, 100)
        } catch (e: Exception) { 0 }
    }

    private suspend fun formatAchievement(seasonHistory: SeasonHistoryEntity, league: LeaguesEntity?): String {
        if (league == null) return if (seasonHistory.trophiesWon > 0) "Champions" else "${getOrdinal(seasonHistory.position ?: 0)} Place"
        val pos = seasonHistory.position ?: 0
        return when {
            seasonHistory.trophiesWon > 0 -> "Champions"
            pos == 1 -> "League Champions"
            pos == 2 -> "Runners Up"
            pos <= 4 -> "CAF Qualification"
            else -> "${getOrdinal(pos)} Place"
        }
    }

    private fun getOrdinal(i: Int): String = when {
        i % 100 in 11..13 -> "${i}th"
        i % 10 == 1 -> "${i}st"
        i % 10 == 2 -> "${i}nd"
        i % 10 == 3 -> "${i}rd"
        else -> "${i}th"
    }

    fun refreshData() {
        val state = gameManager.gameState.value
        if (state is GameManager.GameState.Active) {
            viewModelScope.launch { loadClubData(state.context) }
        }
    }

    fun upgradeInfrastructure(type: String) {
        val state = gameManager.gameState.value
        if (state !is GameManager.GameState.Active) return

        viewModelScope.launch {
            val teamId = state.context.teamId
            val teamName = state.context.teamName
            val currentLevel = infrastructureRepository.getCurrentUpgradeLevel(teamId, type)
            if (currentLevel < 5) {
                val upgrade = infrastructureRepository.initiateUpgrade(
                    teamName = teamName,
                    teamId = teamId,
                    upgradeType = type,
                    targetLevel = currentLevel + 1
                )
                if (upgrade != null) {
                    // Refresh data after successful initiation
                    loadClubData(state.context)
                }
            }
        }
    }
}
