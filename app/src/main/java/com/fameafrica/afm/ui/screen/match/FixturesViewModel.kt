package com.fameafrica.afm.ui.screen.match

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fameafrica.afm.data.database.dao.MonthlyStatistics
import com.fameafrica.afm.data.database.entities.FixturesEntity
import com.fameafrica.afm.data.database.entities.FixturesResultsEntity
import com.fameafrica.afm.data.repository.*
import com.fameafrica.afm.domain.manager.GameManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FixturesUiState(
    val isLoading: Boolean = true,
    val upcomingFixtures: List<FixturesEntity> = emptyList(),
    val results: List<FixturesResultsEntity> = emptyList(),
    val dashboard: FixturesDashboard? = null,
    val resultsDashboard: ResultsDashboard? = null,
    val teamProgress: TeamSeasonProgress? = null,
    val monthlyStats: List<MonthlyStatistics> = emptyList(),
    val selectedTab: Int = 0,
    val userTeamName: String = "",
    val season: String = "2025/26",
    val currentDate: String = ""
)

@HiltViewModel
class FixturesViewModel @Inject constructor(
    val fixturesRepository: FixturesRepository,
    private val resultsRepository: FixturesResultsRepository,
    private val gameManager: GameManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(FixturesUiState())
    val uiState: StateFlow<FixturesUiState> = _uiState.asStateFlow()

    init {
        observeGameManager()
    }

    private fun observeGameManager() {
        viewModelScope.launch {
            // FM-Level Depth: React to world simulation updates
            gameManager.gameState.collect { state ->
                if (state is GameManager.GameState.Active) {
                    val context = state.context
                    _uiState.update { it.copy(
                        currentDate = context.currentDate, 
                        userTeamName = context.teamName
                    ) }
                    loadData(context.teamId, context.season)
                }
            }
        }
    }

    private fun loadData(teamId: Int, season: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, season = season) }
            
            val year = season.split("/").first()
            
            // Correctly use combine for more than 5 flows using the vararg/array variant
            @Suppress("UNCHECKED_CAST")
            combine(
                fixturesRepository.getFixturesByTeam(teamId),
                resultsRepository.getResultsByTeam(teamId),
                resultsRepository.getMonthlyStats(year),
                flowOf(fixturesRepository.getFixturesDashboard()),
                flowOf(resultsRepository.getResultsDashboard()),
                flowOf(fixturesRepository.getTeamSeasonProgress(teamId, season))
            ) { args: Array<Any?> ->
                val teamFixtures = args[0] as List<FixturesEntity>
                val teamResults = args[1] as List<FixturesResultsEntity>
                val monthly = args[2] as List<MonthlyStatistics>
                val dash = args[3] as FixturesDashboard
                val resDash = args[4] as ResultsDashboard
                val progress = args[5] as TeamSeasonProgress

                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        upcomingFixtures = teamFixtures.filter { f -> !f.isCompleted }.sortedBy { f -> f.matchDate },
                        results = teamResults.sortedByDescending { r -> r.matchDate },
                        dashboard = dash,
                        resultsDashboard = resDash,
                        teamProgress = progress,
                        monthlyStats = monthly
                    )
                }
            }.collect()
        }
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }
}
