package com.fameafrica.afm.ui.screen.fans

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fameafrica.afm.data.repository.FanExpectationsDashboard
import com.fameafrica.afm.data.repository.FanExpectationsRepository
import com.fameafrica.afm.data.repository.FanReactionsDashboard
import com.fameafrica.afm.data.repository.FanReactionsRepository
import com.fameafrica.afm.domain.manager.GameManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FansViewModel @Inject constructor(
    private val fanReactionsRepository: FanReactionsRepository,
    private val fanExpectationsRepository: FanExpectationsRepository,
    private val gameManager: GameManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(FansUiState())
    val uiState: StateFlow<FansUiState> = _uiState.asStateFlow()

    init {
        observeFansData()
    }

    private fun observeFansData() {
        viewModelScope.launch {
            gameManager.gameState.collect { state ->
                if (state is GameManager.GameState.Active) {
                    val context = state.context
                    refreshFanData(context.teamId, context.teamName)
                }
            }
        }
    }

    /**
     * FM-Level Depth: Manual refresh that re-evaluates fan sentiment based on recent matches
     * and transfer activity directly from the simulation engine.
     */
    fun loadFanData(teamId: Int, teamName: String) {
        viewModelScope.launch {
            refreshFanData(teamId, teamName)
        }
    }

    fun refreshData() {
        val teamId = _uiState.value.teamId
        val teamName = _uiState.value.teamName
        if (teamId != 0) {
            viewModelScope.launch {
                refreshFanData(teamId, teamName)
            }
        }
    }

    private suspend fun refreshFanData(teamId: Int, teamName: String) {
        _uiState.update { it.copy(isLoading = true, teamId = teamId, teamName = teamName) }

        try {
            val dashboard = fanReactionsRepository.getFanReactionsDashboard(teamId)
            val expectationsDashboard = fanExpectationsRepository.getFanExpectationsDashboard(teamName)
            val repoDist = fanReactionsRepository.getReactionTypeDistribution().firstOrNull() ?: emptyList()
            
            val reactionDistribution = repoDist.map { 
                ReactionTypeDistribution(it.reaction, it.count)
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    dashboard = dashboard,
                    expectations = expectationsDashboard,
                    reactionTypeDistribution = reactionDistribution
                )
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    snackbarMessage = "Error loading fan data: ${e.message}"
                )
            }
        }
    }

    fun addPositiveReaction() {
        viewModelScope.launch {
            val teamId = _uiState.value.teamId
            val teamName = _uiState.value.teamName
            if (teamId != 0) {
                try {
                    fanReactionsRepository.addPositiveReaction(teamId, teamName)
                    refreshFanData(teamId, teamName)
                } catch (e: Exception) {
                    _uiState.update { it.copy(snackbarMessage = "Error: ${e.message}") }
                }
            }
        }
    }

    fun addNegativeReaction() {
        viewModelScope.launch {
            val teamId = _uiState.value.teamId
            val teamName = _uiState.value.teamName
            if (teamId != 0) {
                try {
                    fanReactionsRepository.addNegativeReaction(teamId, teamName)
                    refreshFanData(teamId, teamName)
                } catch (e: Exception) {
                    _uiState.update { it.copy(snackbarMessage = "Error: ${e.message}") }
                }
            }
        }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}

data class FansUiState(
    val isLoading: Boolean = false,
    val teamId: Int = 0,
    val teamName: String = "",
    val dashboard: FanReactionsDashboard? = null,
    val expectations: FanExpectationsDashboard? = null,
    val reactionTypeDistribution: List<ReactionTypeDistribution> = emptyList(),
    val snackbarMessage: String? = null
)

data class ReactionTypeDistribution(
    val reactionType: String,
    val count: Int
)
