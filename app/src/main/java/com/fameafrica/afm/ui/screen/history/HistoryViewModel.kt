package com.fameafrica.afm.ui.screen.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fameafrica.afm.data.database.entities.SeasonAwardsEntity
import com.fameafrica.afm.data.database.entities.SeasonHistoryEntity
import com.fameafrica.afm.data.database.entities.TrophiesEntity
import com.fameafrica.afm.data.repository.SeasonAwardsRepository
import com.fameafrica.afm.data.repository.SeasonHistoryRepository
import com.fameafrica.afm.data.repository.TrophiesRepository
import com.fameafrica.afm.domain.manager.GameManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val seasonHistoryRepository: SeasonHistoryRepository,
    private val trophiesRepository: TrophiesRepository,
    private val seasonAwardsRepository: SeasonAwardsRepository,
    private val gameManager: GameManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        observeHistoryData()
    }

    private fun observeHistoryData() {
        viewModelScope.launch {
            gameManager.gameState.collect { state ->
                if (state is GameManager.GameState.Active) {
                    val context = state.context
                    refreshHistory(context.teamId, context.managerName)
                }
            }
        }
    }

    private suspend fun refreshHistory(teamId: Int, managerName: String) {
        _uiState.update { it.copy(isLoading = true) }
        
        combine(
            seasonHistoryRepository.getTeamHistory(teamId),
            trophiesRepository.getTrophiesByClub(teamId),
            seasonAwardsRepository.getTeamAwards(teamId),
            seasonAwardsRepository.getCoachAwards(managerName)
        ) { history, trophies, playerAwards, coachAwards ->
            HistoryUiState(
                seasonHistory = history.sortedByDescending { it.season },
                trophies = trophies,
                playerAwards = playerAwards.sortedByDescending { it.season },
                coachAwards = coachAwards.sortedByDescending { it.season },
                isLoading = false
            )
        }.collect { newState ->
            _uiState.value = newState
        }
    }
}

data class HistoryUiState(
    val seasonHistory: List<SeasonHistoryEntity> = emptyList(),
    val trophies: List<TrophiesEntity> = emptyList(),
    val playerAwards: List<SeasonAwardsEntity> = emptyList(),
    val coachAwards: List<SeasonAwardsEntity> = emptyList(),
    val isLoading: Boolean = false
)
