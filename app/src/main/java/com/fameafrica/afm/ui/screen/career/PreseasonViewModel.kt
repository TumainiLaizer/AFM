package com.fameafrica.afm.ui.screen.career

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fameafrica.afm.data.database.dao.NationalitiesDao
import com.fameafrica.afm.data.database.entities.*
import com.fameafrica.afm.data.repository.*
import com.fameafrica.afm.domain.manager.GameManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.min

data class PreseasonUiState(
    val isLoading: Boolean = true,
    val matches: List<PreseasonScheduleEntity> = emptyList(),
    val userTeamName: String = "",
    val season: String = "",
    val fitnessProgress: Float = 0f,
    val cohesionProgress: Float = 0f,
    val commercialIncome: Long = 0,
    val isTourActive: Boolean = false,
    
    // Country Selection
    val africanCountries: List<NationalitiesEntity> = emptyList(),
    val selectedCountry: NationalitiesEntity? = null,
    
    // Team Selection
    val teamsInCountry: List<TeamsEntity> = emptyList(),
    val leaguesInCountry: List<LeaguesEntity> = emptyList(),
    val filteredTeams: List<TeamsEntity> = emptyList(),
    val selectedTourTeams: List<TeamsEntity> = emptyList(),
    
    // Filters
    val levelFilter: Int? = null,
    val starFilter: String? = null, // "Gold", "Silver", "Bronze"
    
    // UI Navigation
    val currentStep: TourStep = TourStep.MAP,
    val activeTourTab: ActiveTourTab = ActiveTourTab.OVERVIEW,
    val completedMatches: Int = 0,
    val totalMatches: Int = 0,
    val gameState: GameManager.GameState? = null,
    val selectedRegion: PreseasonRegion? = null
)

enum class TourStep {
    REGION_SELECTION,
    MAP,
    SELECTION,
    SUMMARY,
    ACTIVE
}

enum class PreseasonRegion(val displayName: String, val iconRes: String) {
    EAST_AFRICA("EAST AFRICA", "ic_eastafrica.webp"),
    NORTH_AFRICA("NORTH AFRICA", "ic_northafrica.webp"),
    WEST_AFRICA("WEST AFRICA", "ic_westafrica.webp"),
    SOUTHERN_AFRICA("SOUTHERN AFRICA", "ic_southernafrica.webp"),
    CENTRAL_AFRICA("CENTRAL AFRICA", "ic_centralafrica.webp")
}

enum class ActiveTourTab {
    OVERVIEW,
    SQUAD,
    CLUB,
    FIXTURES
}

@HiltViewModel
class PreseasonViewModel @Inject constructor(
    private val preseasonRepository: PreseasonScheduleRepository,
    private val teamsRepository: TeamsRepository,
    private val leaguesRepository: LeaguesRepository,
    private val nationalitiesDao: NationalitiesDao,
    private val gameManager: GameManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(PreseasonUiState())
    val uiState: StateFlow<PreseasonUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            gameManager.gameState.collect { state ->
                if (state is GameManager.GameState.Active) {
                    val teamId = state.context.teamId
                    val teamName = state.context.teamName
                    val season = state.context.season
                    val nations = nationalitiesDao.getAfricanNations().firstOrNull() ?: emptyList()

                    preseasonRepository.getTeamPreseasonSchedule(teamId, season).collect { matches ->
                        if (matches.isNotEmpty()) {
                            val completed = matches.count { it.isCompleted }
                            _uiState.update { 
                                it.copy(
                                    matches = matches,
                                    userTeamName = teamName,
                                    season = season,
                                    fitnessProgress = min(1f, completed * 0.25f),
                                    cohesionProgress = min(1f, completed * 0.20f),
                                    commercialIncome = completed.toLong() * 35000,
                                    isTourActive = true,
                                    currentStep = TourStep.ACTIVE,
                                    isLoading = false,
                                    completedMatches = completed,
                                    totalMatches = matches.size,
                                    gameState = state
                                )
                            }
                        } else {
                            _uiState.update { 
                                it.copy(
                                    userTeamName = teamName,
                                    season = season,
                                    africanCountries = nations,
                                    isLoading = false,
                                    currentStep = TourStep.REGION_SELECTION,
                                    gameState = state
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    fun selectRegion(region: PreseasonRegion) {
        _uiState.update { it.copy(selectedRegion = region, currentStep = TourStep.MAP) }
    }

    fun selectCountry(country: NationalitiesEntity) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, selectedCountry = country) }
            
            val leagues = leaguesRepository.getLeaguesByCountry(country.id).firstOrNull() ?: emptyList()
            val allTeams = mutableListOf<TeamsEntity>()
            leagues.forEach { league ->
                val leagueTeams = teamsRepository.getTeamsByLeague(league.name).firstOrNull() ?: emptyList()
                allTeams.addAll(leagueTeams)
            }

            _uiState.update { 
                it.copy(
                    isLoading = false,
                    leaguesInCountry = leagues,
                    teamsInCountry = allTeams,
                    filteredTeams = allTeams,
                    currentStep = TourStep.SELECTION,
                    levelFilter = null,
                    starFilter = null,
                    selectedTourTeams = emptyList()
                )
            }
        }
    }

    fun applyFilters(level: Int?, stars: String?) {
        _uiState.update { state ->
            val filtered = state.teamsInCountry.filter { team ->
                val levelMatch = level == null || state.leaguesInCountry.find { it.name == team.league }?.level == level
                val starMatch = stars == null || getTeamStarLabel(team.reputation) == stars
                levelMatch && starMatch
            }
            state.copy(
                levelFilter = level,
                starFilter = stars,
                filteredTeams = filtered
            )
        }
    }

    private fun getTeamStarLabel(reputation: Int): String {
        return when {
            reputation >= 70 -> "Gold"
            reputation >= 40 -> "Silver"
            else -> "Bronze"
        }
    }

    fun toggleTeamSelection(team: TeamsEntity) {
        _uiState.update { state ->
            val current = state.selectedTourTeams.toMutableList()
            if (current.any { it.id == team.id }) {
                current.removeAll { it.id == team.id }
            } else if (current.size < 5) {
                current.add(team)
            }
            state.copy(selectedTourTeams = current)
        }
    }

    fun proceedToSummary() {
        if (uiState.value.selectedTourTeams.isNotEmpty()) {
            _uiState.update { it.copy(currentStep = TourStep.SUMMARY) }
        }
    }

    fun confirmTour() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val state = uiState.value
            val gameState = state.gameState as? GameManager.GameState.Active ?: return@launch
            
            preseasonRepository.generateCustomPreseasonTour(
                userTeamId = gameState.context.teamId,
                userTeamName = state.userTeamName,
                season = state.season,
                selectedTeams = state.selectedTourTeams,
                tourCountry = state.selectedCountry?.nationality ?: "Africa"
            )
            
            // Reload
            loadInitialData()
        }
    }

    fun backStep() {
        _uiState.update { 
            when (it.currentStep) {
                TourStep.MAP -> it.copy(currentStep = TourStep.REGION_SELECTION, selectedRegion = null)
                TourStep.SELECTION -> it.copy(currentStep = TourStep.MAP, selectedCountry = null)
                TourStep.SUMMARY -> it.copy(currentStep = TourStep.SELECTION)
                else -> it
            }
        }
    }

    fun setActiveTab(tab: ActiveTourTab) {
        _uiState.update { it.copy(activeTourTab = tab) }
    }

    fun simulatePreseason() {
        viewModelScope.launch {
            gameManager.processNextTurn()
        }
    }
}
