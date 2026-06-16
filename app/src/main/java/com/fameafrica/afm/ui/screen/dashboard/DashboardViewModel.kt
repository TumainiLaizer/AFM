package com.fameafrica.afm.ui.screen.dashboard

import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fameafrica.afm.data.database.entities.*
import com.fameafrica.afm.data.repository.*
import com.fameafrica.afm.domain.manager.CareerManager
import com.fameafrica.afm.domain.manager.GameManager
import com.fameafrica.afm.ui.theme.ClubThemeConfig
import com.fameafrica.afm.ui.theme.ClubThemeManager
import com.fameafrica.afm.ui.theme.FootballThemePreset
import com.fameafrica.afm.utils.extensions.formatCurrency
import com.fameafrica.afm.domain.model.CareerState
import com.fameafrica.afm.domain.model.SimulationEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

sealed class DashboardDisplayState {
    object Loading : DashboardDisplayState()
    object Ready : DashboardDisplayState()
    data class Error(val message: String, val isRecoverable: Boolean = true) : DashboardDisplayState()
}

@Immutable
data class DashboardUiState(
    val isLoading: Boolean = true,
    val displayState: DashboardDisplayState = DashboardDisplayState.Loading,
    val managerName: String = "",
    val managerNationality: String = "Tanzania",
    val managerAvatar: String? = null,
    val clubName: String = "",
    val clubId: Int = 0,
    val leagueName: String = "",
    val leaguePosition: Int = 0,
    val boardConfidence: Int = 50,
    val fanConfidence: Int = 50,
    val formattedTransferBudget: String = "",
    val formattedWageBudget: String = "",
    val unreadMessages: Int = 0,
    val gameDate: String = "",
    val season: String = "",
    val isPreseason: Boolean = false,
    val isTransferWindowOpen: Boolean = false,
    val clubTheme: ClubThemeConfig? = null,
    val gameMode: GameManager.CareerMode = GameManager.CareerMode.MANAGER,
    val isAdvancing: Boolean = false,
    val isMatchToday: Boolean = false,
    val nextMatchId: Int = 0,
    val nextMatchOpponent: String = "Unknown",
    val nextMatchDate: String = "",
    val nextMatchTime: String = "16:00",
    val nextMatchStadium: String = "Stadium",
    val nextMatchCompetition: String = "Competition",
    val squadDepth: Int = 0,
    val bankBalance: String = "0",
    val premiumCurrency: String = "0",
    val reputationValue: Int = 50,
    val managerLevel: Int = 1,
    val managerXp: Int = 0,
    val managerMaxXp: Int = 100,
    val activeSponsors: Int = 0,
    val openVacancies: Int = 0,
    val pendingTransfers: Int = 0,
    val liveHeadlines: List<String> = emptyList(),
    val feedItems: List<SimulationEvent> = emptyList(),
    val standings: List<StandingUiModel> = emptyList(),
    val pendingImmersiveEvent: SimulationEvent? = null
)

@Immutable
data class StandingUiModel(val position: Int, val teamName: String, val played: Int, val goalDifference: Int, val points: Int)

@Immutable
data class NewsUiModel(val id: Int, val title: String, val snippet: String, val category: String)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val gameManager: GameManager,
    private val careerManager: CareerManager,
    private val notificationsRepository: NotificationsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    val careerState: StateFlow<CareerState> = gameManager.careerState
    val gameEvents: SharedFlow<GameManager.GameEvent> = gameManager.gameEvents

    init {
        observeCareerState()
        observeInitialization()
        observeGameEvents()
        observeNextMatch()
    }

    private fun observeCareerState() {
        viewModelScope.launch {
            gameManager.isProcessing.collect { processing ->
                _uiState.update { it.copy(isAdvancing = processing) }
            }
        }

        viewModelScope.launch {
            careerState.collect { state ->
                if (state.manager.id != -1) {
                    val currentTheme = ClubThemeManager.getThemeForThemePreset(
                        if (gameManager.gameState.value is GameManager.GameState.Active && 
                            (gameManager.gameState.value as GameManager.GameState.Active).context.careerMode == GameManager.CareerMode.CHAIRMAN) 
                            FootballThemePreset.CHAIRMAN_MODE 
                        else FootballThemePreset.MANAGER_MODE
                    )
                    
                    withContext(Dispatchers.Default) {
                        val headlines = state.world.newsHeadlines.take(10).map { it.headline }
                        val standings = state.competition.leagueStandings.take(5).map { s ->
                            StandingUiModel(s.position, s.teamName, s.matchesPlayed, s.goalDifference, s.points)
                        }

                        _uiState.update { current ->
                            current.copy(
                                managerName = state.manager.name,
                                managerNationality = state.manager.nationality,
                                clubName = state.club.teamName,
                                clubId = state.club.teamId,
                                leagueName = state.competition.leagueName ?: "No League",
                                boardConfidence = state.club.boardConfidence,
                                fanConfidence = state.club.fanSentiment,
                                formattedTransferBudget = state.finance.transferBudget.formatCurrency(),
                                formattedWageBudget = state.finance.wageBudget.formatCurrency(),
                                gameDate = state.timeline.gameDateDisplay,
                                season = state.timeline.currentSeason,
                                isPreseason = state.timeline.isPreseason,
                                isTransferWindowOpen = state.transfer.isWindowOpen,
                                reputationValue = state.manager.reputation,
                                managerLevel = state.manager.managerLevel,
                                managerXp = state.manager.managerXp,
                                managerMaxXp = state.manager.managerLevel * 100,
                                bankBalance = state.finance.balance.formatCurrency(),
                                premiumCurrency = state.finance.coins.toString(),
                                squadDepth = state.club.squad.size,
                                pendingTransfers = state.transfer.activeBids.size,
                                openVacancies = state.world.newsHeadlines.count { it.headline.contains("Vacancy", true) || it.headline.contains("Sacked", true) },
                                liveHeadlines = headlines,
                                feedItems = state.world.dailyEvents,
                                standings = standings,
                                clubTheme = currentTheme
                            )
                        }
                    }
                }
            }
        }
    }

    private fun observeInitialization() {
        viewModelScope.launch {
            gameManager.initializationState.collect { state ->
                when (state) {
                    is GameManager.InitializationState.Loading -> {
                        _uiState.update { it.copy(isLoading = true, displayState = DashboardDisplayState.Loading) }
                    }
                    is GameManager.InitializationState.Ready -> {
                        _uiState.update { it.copy(isLoading = false, displayState = DashboardDisplayState.Ready) }
                    }
                    is GameManager.InitializationState.Failed -> {
                        _uiState.update { it.copy(isLoading = false, displayState = DashboardDisplayState.Error(state.error)) }
                    }
                    else -> {}
                }
            }
        }
    }

    private fun observeNextMatch() {
        viewModelScope.launch {
            gameManager.nextMatch.collect { match ->
                _uiState.update { current ->
                    current.copy(
                        isMatchToday = match != null && match.matchDate == current.gameDate,
                        nextMatchId = match?.id ?: 0,
                        nextMatchOpponent = match?.let { if (it.homeTeamId == current.clubId) it.awayTeam else it.homeTeam } ?: "No Match",
                        nextMatchDate = match?.matchDate ?: "",
                        nextMatchStadium = match?.stadium ?: "TBD",
                        nextMatchCompetition = match?.cupName ?: match?.league ?: "Friendly"
                    )
                }
            }
        }
    }

    fun startAdvancement() {
        gameManager.processNextTurn()
    }

    fun stopAdvancement() {
        gameManager.stopSimulation()
    }

    private val _pendingEvents = MutableStateFlow<List<SimulationEvent>>(emptyList())

    fun dismissImmersiveEvent(autoAdvance: Boolean = false) {
        val currentList = _pendingEvents.value
        if (currentList.isNotEmpty()) {
            val newList = currentList.drop(1)
            _pendingEvents.value = newList
            _uiState.update { it.copy(pendingImmersiveEvent = newList.firstOrNull()) }
            
            // If we've cleared all events and user requested auto-advance, resume simulation
            if (newList.isEmpty() && autoAdvance) {
                startAdvancement()
            }
        } else {
            _uiState.update { it.copy(pendingImmersiveEvent = null) }
            if (autoAdvance) startAdvancement()
        }
    }

    private fun observeGameEvents() {
        viewModelScope.launch {
            gameManager.dailyEvents.collect { events ->
                val immersiveEvents = events.filter { it.importance >= 10 }
                if (immersiveEvents.isNotEmpty()) {
                    _pendingEvents.value = immersiveEvents
                    _uiState.update { it.copy(pendingImmersiveEvent = immersiveEvents.firstOrNull()) }
                }
            }
        }
    }
}
