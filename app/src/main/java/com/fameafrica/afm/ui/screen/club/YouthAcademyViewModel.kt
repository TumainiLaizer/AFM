package com.fameafrica.afm.ui.screen.club

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fameafrica.afm.data.database.entities.PlayersEntity
import com.fameafrica.afm.data.repository.PlayersRepository
import com.fameafrica.afm.domain.manager.GameManager
import com.fameafrica.afm.domain.manager.YouthAcademyEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class YouthAcademyUiState(
    val isLoading: Boolean = false,
    val prospects: List<PlayersEntity> = emptyList(),
    val intakeTeamId: Int? = null,
    val intakeTeamName: String? = null
)

@HiltViewModel
class YouthAcademyViewModel @Inject constructor(
    private val youthAcademyEngine: YouthAcademyEngine,
    private val playersRepository: PlayersRepository,
    private val gameManager: GameManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(YouthAcademyUiState())
    val uiState: StateFlow<YouthAcademyUiState> = _uiState.asStateFlow()

    init {
        observeGameManager()
    }

    private fun observeGameManager() {
        viewModelScope.launch {
            gameManager.gameState.collect { state ->
                if (state is GameManager.GameState.Active) {
                    _uiState.update { it.copy(
                        intakeTeamId = state.context.teamId,
                        intakeTeamName = state.context.teamName
                    ) }
                    loadProspects(state.context.teamId)
                }
            }
        }
    }

    private fun loadProspects(teamId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // Filter players by team and age (youth academy players are 15-18)
            playersRepository.getPlayersByTeamId(teamId).collect { squad ->
                val academyPlayers = squad.filter { it.age <= 18 }.sortedByDescending { it.potential }
                _uiState.update { it.copy(isLoading = false, prospects = academyPlayers) }
            }
        }
    }

    /**
     * Manual trigger for intake (usually handled by simulation engine)
     */
    fun triggerNewIntake() {
        viewModelScope.launch {
            val teamId = _uiState.value.intakeTeamId ?: return@launch
            _uiState.update { it.copy(isLoading = true) }
            val newProspects = youthAcademyEngine.generateAnnualIntake(teamId)
            _uiState.update { it.copy(isLoading = false, prospects = newProspects.sortedByDescending { p -> p.potential }) }
        }
    }
}
