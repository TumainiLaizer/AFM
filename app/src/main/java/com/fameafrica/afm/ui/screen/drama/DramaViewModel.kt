package com.fameafrica.afm.ui.screen.drama

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fameafrica.afm.data.database.entities.MatchFixingCasesEntity
import com.fameafrica.afm.data.repository.MatchFixingCasesRepository
import com.fameafrica.afm.domain.manager.GameManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DramaUiState(
    val matchFixingCases: List<MatchFixingCasesEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val managerName: String = "",
    val currentLeagueLevel: Int = 0
)

@HiltViewModel
class DramaViewModel @Inject constructor(
    private val matchFixingRepository: MatchFixingCasesRepository,
    private val gameManager: GameManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(DramaUiState())
    val uiState: StateFlow<DramaUiState> = _uiState.asStateFlow()

    init {
        observeGameManager()
    }

    private fun observeGameManager() {
        viewModelScope.launch {
            // FM-Level Depth: Drama events (like match-fixing investigations) react to the world timeline
            gameManager.gameState.collect { state ->
                if (state is GameManager.GameState.Active) {
                    val context = state.context
                    loadDramaData(context.teamName)
                }
            }
        }
    }

    private fun loadDramaData(teamName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            // Assume league level logic or fetch it from context/repos
            val leagueLevel = 1 

            matchFixingRepository.getAllCases()
                .catch { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
                .collect { cases ->
                    _uiState.update { it.copy(
                        matchFixingCases = cases, 
                        isLoading = false,
                        managerName = teamName, // In this engine, manager name is often tied to club context
                        currentLeagueLevel = leagueLevel
                    ) }
                }
        }
    }

    fun handleDilemmaChoice(caseId: Int, accepted: Boolean) {
        viewModelScope.launch {
            val verdict = if (accepted) "Guilty" else "Not Guilty"
            if (accepted) {
                matchFixingRepository.completeInvestigation(
                    caseId = caseId,
                    verdict = verdict,
                    punishment = "Manager Ban & Fine",
                    managerBanned = true,
                    managerBanDuration = "1 Year",
                    fineAmount = 500000
                )
            } else {
                matchFixingRepository.completeInvestigation(
                    caseId = caseId,
                    verdict = verdict
                )
            }
        }
    }
}
