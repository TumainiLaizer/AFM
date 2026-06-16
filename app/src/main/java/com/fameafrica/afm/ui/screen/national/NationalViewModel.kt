package com.fameafrica.afm.ui.screen.national

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fameafrica.afm.data.database.entities.NationalTeamsEntity
import com.fameafrica.afm.data.repository.NationalTeamDashboard
import com.fameafrica.afm.data.repository.NationalTeamsRepository
import com.fameafrica.afm.domain.manager.GameManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NationalUiState(
    val managedTeamDashboard: NationalTeamDashboard? = null,
    val africanRankings: List<NationalTeamsEntity> = emptyList(),
    val worldRankings: List<NationalTeamsEntity> = emptyList(),
    val availableJobs: List<NationalTeamsEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val managerId: Int? = null
)

@HiltViewModel
class NationalViewModel @Inject constructor(
    private val nationalTeamsRepository: NationalTeamsRepository,
    private val gameManager: GameManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(NationalUiState())
    val uiState: StateFlow<NationalUiState> = _uiState.asStateFlow()

    init {
        observeNationalData()
    }

    private fun observeNationalData() {
        viewModelScope.launch {
            // FM-Level Depth: React to world simulation ticks for ranking updates and job vacancies
            gameManager.gameState.collect { state ->
                if (state is GameManager.GameState.Active) {
                    val context = state.context
                    _uiState.update { it.copy(managerId = context.managerId) }
                    loadNationalData(context)
                }
            }
        }
    }

    private suspend fun loadNationalData(context: GameManager.GameContext) {
        _uiState.update { it.copy(isLoading = true) }

        try {
            // Check if manager manages a national team
            val managedTeam = nationalTeamsRepository.getTeamByManager(context.managerId)
            
            combine(
                nationalTeamsRepository.getTopAfricanTeams(20),
                nationalTeamsRepository.getTopTeams(20),
                nationalTeamsRepository.getTeamsWithoutManager()
            ) { african, world, jobs ->
                val dashboard = managedTeam?.let { 
                    nationalTeamsRepository.getNationalTeamDashboard(it.id)
                }
                
                _uiState.update {
                    it.copy(
                        managedTeamDashboard = dashboard,
                        africanRankings = african,
                        worldRankings = world,
                        availableJobs = jobs,
                        isLoading = false
                    )
                }
            }.catch { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }.collect()
        } catch (e: Exception) {
            _uiState.update { it.copy(isLoading = false, error = e.message) }
        }
    }

    fun applyForJob(teamId: Int) {
        viewModelScope.launch {
            val managerId = _uiState.value.managerId ?: return@launch
            nationalTeamsRepository.assignManager(teamId, managerId)
            // Trigger a refresh if needed, though observation should handle it
            val state = gameManager.gameState.value
            if (state is GameManager.GameState.Active) {
                loadNationalData(state.context)
            }
        }
    }

    fun resignFromJob() {
        viewModelScope.launch {
            val teamId = _uiState.value.managedTeamDashboard?.team?.id ?: return@launch
            nationalTeamsRepository.assignManager(teamId, null)
            val state = gameManager.gameState.value
            if (state is GameManager.GameState.Active) {
                loadNationalData(state.context)
            }
        }
    }

    fun refresh() {
        val state = gameManager.gameState.value
        if (state is GameManager.GameState.Active) {
            viewModelScope.launch {
                loadNationalData(state.context)
            }
        }
    }
}
