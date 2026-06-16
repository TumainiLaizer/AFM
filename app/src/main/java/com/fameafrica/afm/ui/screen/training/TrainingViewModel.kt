package com.fameafrica.afm.ui.screen.training

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fameafrica.afm.data.database.entities.TrainingDayEntity
import com.fameafrica.afm.data.database.entities.TrainingScheduleEntity
import com.fameafrica.afm.data.repository.*
import com.fameafrica.afm.domain.manager.GameManager
import com.fameafrica.afm.domain.manager.TrainingSchedulerEngine
import com.fameafrica.afm.utils.GameDateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TrainingHQState(
    val isLoading: Boolean = true,
    val schedule: TrainingScheduleEntity? = null,
    val trainingDays: List<TrainingDayEntity> = emptyList(),
    val department: TrainingSchedulerEngine.TrainingDepartment? = null,
    val squadSharpness: List<PlayerSharpnessUiModel> = emptyList(),
    val teamId: Int = -1,
    val currentMonth: Int = 1,
    val currentYear: Int = 2025
)

data class PlayerSharpnessUiModel(
    val id: Int,
    val name: String,
    val position: String,
    val sharpness: Int,
    val stamina: Int,
    val morale: Int
)

@HiltViewModel
class TrainingViewModel @Inject constructor(
    private val trainingRepository: TrainingRepository,
    private val playersRepository: PlayersRepository,
    private val gameManager: GameManager,
    private val trainingSchedulerEngine: TrainingSchedulerEngine,
    private val gameDateManager: GameDateManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrainingHQState())
    val uiState: StateFlow<TrainingHQState> = _uiState.asStateFlow()

    init {
        observeGameState()
    }

    private fun observeGameState() {
        viewModelScope.launch {
            gameManager.gameState.collect { state ->
                if (state is GameManager.GameState.Active) {
                    val dateModel = gameDateManager.getGameDateModel(state.context.week)
                    loadTrainingData(state.context.teamId, dateModel.month, dateModel.year)
                }
            }
        }
        
        viewModelScope.launch {
            gameManager.currentSquad.collect { players ->
                _uiState.update { state ->
                    state.copy(
                        squadSharpness = players.map { 
                            PlayerSharpnessUiModel(it.id, it.name, it.position, it.sharpness, it.stamina, it.morale)
                        }.sortedBy { it.sharpness }
                    )
                }
            }
        }
    }

    private suspend fun loadTrainingData(teamId: Int, month: Int, year: Int) {
        _uiState.update { it.copy(isLoading = true, teamId = teamId, currentMonth = month, currentYear = year) }
        
        val schedule = trainingRepository.getSchedule(teamId, month, year)
            ?: run {
                // Generate if missing
                val newId = trainingSchedulerEngine.generateMonthlySchedule(teamId, month, year)
                trainingRepository.getSchedule(teamId, month, year)
            }
            
        val days = schedule?.let { trainingRepository.getDaysForSchedule(it.id).firstOrNull() } ?: emptyList()
        val dept = trainingSchedulerEngine.calculateDepartmentRatings(teamId)
        
        _uiState.update { 
            it.copy(
                isLoading = false,
                schedule = schedule,
                trainingDays = days,
                department = dept
            )
        }
    }

    fun updateIntensity(intensity: String) {
        val currentSchedule = _uiState.value.schedule ?: return
        viewModelScope.launch {
            val updated = currentSchedule.copy(globalIntensity = intensity)
            trainingRepository.updateSchedule(updated)
            _uiState.update { it.copy(schedule = updated) }
        }
    }

    fun updateFocus(focus: String) {
        val currentSchedule = _uiState.value.schedule ?: return
        viewModelScope.launch {
            val updated = currentSchedule.copy(primaryFocus = focus)
            trainingRepository.updateSchedule(updated)
            
            // Re-generate days based on new focus
            trainingRepository.deleteDaysBySchedule(updated.id)
            trainingSchedulerEngine.generateMonthlySchedule(updated.teamId, updated.month, updated.year, updated.globalIntensity, focus)
            
            val newDays = trainingRepository.getDaysForSchedule(updated.id).firstOrNull() ?: emptyList()
            _uiState.update { it.copy(schedule = updated, trainingDays = newDays) }
        }
    }

    fun approveSchedule() {
        val currentSchedule = _uiState.value.schedule ?: return
        viewModelScope.launch {
            trainingSchedulerEngine.approveSchedule(currentSchedule.id)
            val updated = currentSchedule.copy(isApproved = true)
            _uiState.update { it.copy(schedule = updated) }
        }
    }
}
