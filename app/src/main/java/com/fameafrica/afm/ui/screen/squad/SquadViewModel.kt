package com.fameafrica.afm.ui.screen.squad

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fameafrica.afm.data.database.entities.PlayersEntity
import com.fameafrica.afm.data.repository.*
import com.fameafrica.afm.domain.manager.GameManager
import com.fameafrica.afm.utils.SettingsManager
import com.fameafrica.afm.utils.NationalityUtils
import com.fameafrica.afm.utils.formatters.CurrencyFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class SquadUiState(
    val isLoading: Boolean = true,
    val selectedTab: String = "ALL",
    val players: List<PlayerUiModel> = emptyList(),
    val filteredPlayers: List<PlayerUiModel> = emptyList(),
    val searchQuery: String = "",
    val sortBy: SortOption = SortOption.RATING,
    val sortAscending: Boolean = false,
    val squadStats: SquadStatsUiModel = SquadStatsUiModel(),
    val analysedData: SquadAnalysedData = SquadAnalysedData(),
    val teamName: String = "",
    val formation: String = "4-4-2",
    val currencyContext: CurrencyFormatter.CurrencyContext? = null
)

@Immutable
data class PlayerUiModel(
    val id: Int,
    val name: String,
    val age: Int,
    val height: Int,
    val position: String,
    val secondaryPosition: String? = null,
    val positionCategory: String,
    val rating: Int,
    val potential: Int,
    val form: Int,
    val morale: Int,
    val condition: Int, // 0-100
    val fatigue: Int,   // 0-100
    val nationality: String,
    val nationalityFlag: String?,
    val shirtNumber: Int,
    val marketValue: Int,
    val wage: Int,
    val contractExpiry: String,
    val isInjured: Boolean,
    val injuryStatus: String?,
    val isSuspended: Boolean,
    val isCaptain: Boolean,
    val isViceCaptain: Boolean,
    val goals: Int,
    val assists: Int,
    val appearances: Int,
    val cleanSheets: Int,
    val yellowCards: Int,
    val redCards: Int,
    val manOfMatch: Int,
    val leadership: Int,
    val loyalty: Int,
    val reactionEmoji: String
) {
    val displayName: String
        get() {
            val parts = name.trim().split("\\s+".toRegex())
            if (parts.size <= 1) return name
            val initials = parts.dropLast(1).joinToString("") { it.take(1).uppercase() + "." }
            return "$initials${parts.last()}"
        }
}

@Immutable
data class SquadStatsUiModel(
    val totalPlayers: Int = 0,
    val averageRating: Double = 0.0,
    val averageAge: Double = 0.0,
    val averageHeight: Double = 0.0,
    val totalMarketValue: Long = 0,
    val injuredCount: Int = 0,
    val suspendedCount: Int = 0,
    val goalkeepers: Int = 0,
    val defenders: Int = 0,
    val midfielders: Int = 0,
    val forwards: Int = 0
)

@Immutable
data class SquadAnalysedData(
    val ageDistribution: Map<String, Int> = emptyMap(),
    val valueByPosition: Map<String, Long> = emptyMap(),
    val potentialVsRating: List<Pair<Int, Int>> = emptyList(),
    val averageAttributes: Map<String, Double> = emptyMap()
)

enum class SortOption {
    RATING, NAME, AGE, VALUE, FORM, GOALS, ASSISTS, MP, CLEAN_SHEETS, HEIGHT, LEADERSHIP, LOYALTY, SHIRT_NUMBER, POSITION
}

@HiltViewModel
class SquadViewModel @Inject constructor(
    private val gameManager: GameManager,
    private val playersRepository: PlayersRepository,
    private val teamsRepository: TeamsRepository,
    private val gameStateRepository: GameStatesRepository,
    private val playerContractsRepository: PlayerContractsRepository,
    private val playerTrainingRepository: PlayerTrainingRepository,
    private val playerReactionsRepository: PlayerReactionsRepository,
    private val settingsManager: SettingsManager,
    private val currencyFormatter: CurrencyFormatter
) : ViewModel() {

    private val _uiState = MutableStateFlow(SquadUiState(isLoading = true))
    val uiState: StateFlow<SquadUiState> = _uiState

    private var allPlayers: List<PlayerUiModel> = emptyList()

    init {
        observeSquadData()
        loadCurrencyContext()
    }

    private fun loadCurrencyContext() {
        viewModelScope.launch {
            val context = currencyFormatter.getCurrentContext()
            _uiState.update { it.copy(currencyContext = context) }
        }
    }

    private fun observeSquadData() {
        viewModelScope.launch {
            gameManager.gameState.collect { state ->
                if (state is GameManager.GameState.Active) {
                    loadSquadData(state.context)
                }
            }
        }
    }

    private suspend fun loadSquadData(context: GameManager.GameContext) {
        val teamId = context.teamId
        val teamName = context.teamName

        val team = teamsRepository.getTeamById(teamId)
        val players = playersRepository.getPlayersByTeamId(teamId).firstOrNull() ?: emptyList()

        // Phase 6 & 5 Optimization: Bulk load related data to avoid N+1 queries
        val playerIds = players.map { it.id }
        val allContracts = playerContractsRepository.getContractsByPlayerIds(playerIds).associateBy { it.playerId }
        val allReactions = playerReactionsRepository.getLatestReactionsForPlayers(playerIds).associateBy { it.playerId }

        val playerModels = players.map { player ->
                val contract = allContracts[player.id]
                val latestReaction = allReactions[player.id]

                val loyaltyValue = when (player.personalityType) {
                    "LOYAL" -> 90
                    "PROFESSIONAL" -> 75
                    "TEAM_PLAYER" -> 80
                    "AMBITIOUS" -> 50
                    "TEMPERAMENTAL" -> 40
                    else -> 60
                }

                PlayerUiModel(
                    id = player.id,
                    name = player.name,
                    age = player.age,
                    height = player.height,
                    position = player.position,
                    secondaryPosition = null,
                    positionCategory = player.positionCategory,
                    rating = player.rating,
                    potential = player.potential,
                    form = player.currentForm,
                    morale = player.morale,
                    condition = player.stamina,
                    fatigue = 100 - player.stamina,
                    nationality = player.nationality,
                    nationalityFlag = NationalityUtils.getWavingFlagUrl(player.nationality),
                    shirtNumber = player.shirtNumber,
                    marketValue = player.marketValue,
                    wage = contract?.salary ?: 1000,
                    contractExpiry = contract?.contractEndDate ?: player.contractExpiry ?: "2029-06-30",
                    isInjured = player.injuryStatus != "HEALTHY",
                    injuryStatus = player.injuryStatus,
                    isSuspended = player.suspended,
                    isCaptain = player.isCaptain,
                    isViceCaptain = player.isViceCaptain,
                    goals = player.goals,
                    assists = player.assists,
                    appearances = player.matches,
                    cleanSheets = player.cleanSheets,
                    yellowCards = player.yellowCards,
                    redCards = player.redCards,
                    manOfMatch = player.manOfMatch,
                    leadership = player.leadership,
                    loyalty = loyaltyValue,
                    reactionEmoji = latestReaction?.reactionEmoji ?: getMoraleEmoji(player.morale)
                )
            }.sortedByDescending { it.rating }

            allPlayers = playerModels

            val stats = SquadStatsUiModel(
                totalPlayers = playerModels.size,
                averageRating = if (playerModels.isNotEmpty()) playerModels.map { it.rating }.average() else 0.0,
                averageAge = if (playerModels.isNotEmpty()) playerModels.map { it.age }.average() else 0.0,
                averageHeight = if (playerModels.isNotEmpty()) playerModels.map { it.height.toDouble() }.average() else 0.0,
                totalMarketValue = playerModels.sumOf { it.marketValue.toLong() },
                injuredCount = playerModels.count { it.isInjured },
                suspendedCount = playerModels.count { it.isSuspended },
                goalkeepers = playerModels.count { it.position == "GK" },
                defenders = playerModels.count { it.positionCategory == "DEFENDER" },
                midfielders = playerModels.count { it.positionCategory == "MIDFIELDER" },
                forwards = playerModels.count { it.positionCategory == "FORWARD" }
            )

            _uiState.value = SquadUiState(
                isLoading = false,
                players = playerModels,
                filteredPlayers = playerModels,
                squadStats = stats,
                analysedData = analyseSquad(players),
                teamName = teamName,
                formation = team?.formation ?: "4-4-2"
            )
    }

    private fun getMoraleEmoji(morale: Int): String = when {
        morale >= 85 -> "😊"
        morale >= 70 -> "🙂"
        morale >= 50 -> "😐"
        morale >= 30 -> "😞"
        else -> "😠"
    }

    fun selectTab(tab: String) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
        filterPlayers()
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        filterPlayers()
    }

    fun updateSortOption(option: SortOption) {
        val current = _uiState.value
        val newAscending = if (current.sortBy == option) !current.sortAscending else when (option) {
            SortOption.NAME -> true
            SortOption.AGE -> true
            SortOption.HEIGHT -> true
            SortOption.SHIRT_NUMBER -> true
            SortOption.POSITION -> true
            else -> false
        }

        _uiState.value = current.copy(
            sortBy = option,
            sortAscending = newAscending
        )
        sortPlayers()
    }

    private fun filterPlayers() {
        val state = _uiState.value
        val filtered = allPlayers.filter { player ->
            val matchesTab = when (state.selectedTab) {
                "ALL" -> true
                "GK" -> player.position == "GK"
                "DEF" -> player.positionCategory == "DEFENDER"
                "MID" -> player.positionCategory == "MIDFIELDER"
                "FWD" -> player.positionCategory == "FORWARD"
                "INJURED" -> player.isInjured
                "SUSPENDED" -> player.isSuspended
                "ANALYSIS" -> true
                else -> true
            }

            val matchesSearch = if (state.searchQuery.isBlank()) {
                true
            } else {
                player.name.contains(state.searchQuery, ignoreCase = true) ||
                        player.nationality.contains(state.searchQuery, ignoreCase = true)
            }

            matchesTab && matchesSearch
        }

        _uiState.value = state.copy(filteredPlayers = filtered)
        sortPlayers()
    }

    private fun sortPlayers() {
        val state = _uiState.value
        val sorted = state.filteredPlayers.sortedWith { a, b ->
            val comparison = when (state.sortBy) {
                SortOption.RATING -> a.rating.compareTo(b.rating)
                SortOption.NAME -> a.name.compareTo(b.name)
                SortOption.AGE -> a.age.compareTo(b.age)
                SortOption.VALUE -> a.marketValue.compareTo(b.marketValue)
                SortOption.FORM -> a.form.compareTo(b.form)
                SortOption.GOALS -> a.goals.compareTo(b.goals)
                SortOption.ASSISTS -> a.assists.compareTo(b.assists)
                SortOption.MP -> a.appearances.compareTo(b.appearances)
                SortOption.CLEAN_SHEETS -> a.cleanSheets.compareTo(b.cleanSheets)
                SortOption.HEIGHT -> a.height.compareTo(b.height)
                SortOption.LEADERSHIP -> a.leadership.compareTo(b.leadership)
                SortOption.LOYALTY -> a.loyalty.compareTo(b.loyalty)
                SortOption.SHIRT_NUMBER -> a.shirtNumber.compareTo(b.shirtNumber)
                SortOption.POSITION -> comparePositions(a.position, b.position)
            }

            if (state.sortAscending) comparison else -comparison
        }

        _uiState.value = state.copy(filteredPlayers = sorted)
    }

    private fun comparePositions(posA: String, posB: String): Int {
        val hierarchy = listOf("GK", "RB", "LB", "CB", "RWB", "LWB", "CDM", "CM", "RM", "LM", "CAM", "RW", "LW", "CF", "ST")
        val indexA = hierarchy.indexOf(posA)
        val indexB = hierarchy.indexOf(posB)
        return indexA.compareTo(indexB)
    }

    private fun analyseSquad(entities: List<PlayersEntity>): SquadAnalysedData {
        if (entities.isEmpty()) return SquadAnalysedData()

        val ageDist = entities.groupBy {
            when {
                it.age <= 21 -> "U21"
                it.age <= 25 -> "22-25"
                it.age <= 29 -> "26-29"
                else -> "30+"
            }
        }.mapValues { it.value.size }

        val valByPos = entities.groupBy { it.positionCategory }
            .mapValues { it.value.sumOf { p -> p.marketValue.toLong() } }

        val scatter = entities.map { it.rating to it.potential }

        val avgAttrs: Map<String, Double> = mapOf(
            "ATT" to entities.map { it.finishing.toDouble() }.average(),
            "MID" to entities.map { it.passing.toDouble() }.average(),
            "DEF" to entities.map { it.defending.toDouble() }.average(),
            "PHY" to entities.map { it.pace.toDouble() }.average()
        )

        return SquadAnalysedData(ageDist, valByPos, scatter, avgAttrs)
    }

    fun selectFormation(formation: String) {
        viewModelScope.launch {
            val teamId = gameManager.gameState.value.let { 
                if (it is GameManager.GameState.Active) it.context.teamId else null 
            } ?: return@launch
            
            val team = teamsRepository.getTeamById(teamId) ?: return@launch
            teamsRepository.updateTeam(team.copy(formation = formation))
            
            _uiState.value = _uiState.value.copy(formation = formation)
        }
    }

    fun getPlayerById(playerId: Int): PlayerUiModel? {
        return allPlayers.find { it.id == playerId }
    }

    fun updateShirtNumber(playerId: Int, newNumber: Int) {
        viewModelScope.launch {
            val player = playersRepository.getPlayerById(playerId) ?: return@launch
            playersRepository.updatePlayer(player.copy(shirtNumber = newNumber))
            
            val state = gameManager.gameState.value
            if (state is GameManager.GameState.Active) {
                loadSquadData(state.context)
            }
        }
    }
}
