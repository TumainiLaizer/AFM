package com.fameafrica.afm.ui.screen.infrastructure

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fameafrica.afm.data.database.entities.InfrastructureUpgradesEntity
import com.fameafrica.afm.data.repository.InfrastructureUpgradesRepository
import com.fameafrica.afm.data.repository.TeamsRepository
import com.fameafrica.afm.domain.manager.GameManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InfrastructureViewModel @Inject constructor(
    private val infrastructureRepository: InfrastructureUpgradesRepository,
    private val teamsRepository: TeamsRepository,
    private val gameManager: GameManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(InfrastructureUiState())
    val uiState: StateFlow<InfrastructureUiState> = _uiState.asStateFlow()

    init {
        observeGameManager()
    }

    private fun observeGameManager() {
        viewModelScope.launch {
            // FM-Level Depth: React to world simulation updates for infrastructure progress
            gameManager.gameState.collect { state ->
                if (state is GameManager.GameState.Active) {
                    val context = state.context
                    loadInfrastructureData(context.teamId)
                }
            }
        }
    }

    private fun loadInfrastructureData(teamId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val stadiumLevel = infrastructureRepository.getCurrentUpgradeLevel(teamId, "STADIUM")
            val trainingLevel = infrastructureRepository.getCurrentUpgradeLevel(teamId, "TRAINING_FACILITY")
            val youthLevel = infrastructureRepository.getCurrentUpgradeLevel(teamId, "YOUTH_ACADEMY")
            val medicalLevel = infrastructureRepository.getCurrentUpgradeLevel(teamId, "MEDICAL_CENTER")

            val levels = mapOf(
                "STADIUM" to stadiumLevel,
                "TRAINING_FACILITY" to trainingLevel,
                "YOUTH_ACADEMY" to youthLevel,
                "MEDICAL_CENTER" to medicalLevel
            )

            infrastructureRepository.getTeamUpgrades(teamId).combine(
                flowOf(teamsRepository.getTeamById(teamId))
            ) { upgrades, team ->
                InfrastructureUiState(
                    upgrades = upgrades,
                    stadiumName = team?.homeStadium ?: "Main Stadium",
                    stadiumCapacity = team?.stadiumCapacity ?: 0,
                    isLoading = false,
                    levels = levels
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun startUpgrade(type: String) {
        viewModelScope.launch {
            val state = gameManager.gameState.value
            if (state is GameManager.GameState.Active) {
                val currentLevel = uiState.value.levels[type] ?: 1
                if (currentLevel < 5) {
                    infrastructureRepository.initiateUpgrade(
                        teamName = state.context.teamName,
                        teamId = state.context.teamId,
                        upgradeType = type,
                        targetLevel = currentLevel + 1
                    )
                }
            }
        }
    }
}

data class InfrastructureUiState(
    val upgrades: List<InfrastructureUpgradesEntity> = emptyList(),
    val stadiumName: String = "",
    val stadiumCapacity: Int = 0,
    val isLoading: Boolean = false,
    val levels: Map<String, Int> = emptyMap()
)
