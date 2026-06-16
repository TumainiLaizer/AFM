package com.fameafrica.afm.ui.screen.manager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fameafrica.afm.data.database.entities.ManagerOffersEntity
import com.fameafrica.afm.data.repository.ManagerOffersRepository
import com.fameafrica.afm.domain.manager.GameManager
import com.fameafrica.afm.domain.manager.JobMarketManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class JobMarketUiState(
    val isLoading: Boolean = true,
    val vacancies: List<ManagerOffersEntity> = emptyList(),
    val myApplications: List<ManagerOffersEntity> = emptyList(),
    val isEmployed: Boolean = true,
    val currentTeamName: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class JobMarketViewModel @Inject constructor(
    private val managerOffersRepository: ManagerOffersRepository,
    private val jobMarketManager: JobMarketManager,
    private val gameManager: GameManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(JobMarketUiState())
    val uiState: StateFlow<JobMarketUiState> = _uiState.asStateFlow()

    init {
        observeGameManager()
    }

    private fun observeGameManager() {
        viewModelScope.launch {
            gameManager.careerState.collect { state ->
                _uiState.update { it.copy(
                    isEmployed = state.club.teamId != -1,
                    currentTeamName = if (state.club.teamId != -1) state.club.teamName else null
                ) }
                loadJobMarket()
            }
        }
    }

    fun loadJobMarket() {
        viewModelScope.launch {
            val gameState = gameManager.gameState.value
            if (gameState !is GameManager.GameState.Active) return@launch
            val managerId = gameState.context.managerId

            managerOffersRepository.getAllOffers()
                .onStart { _uiState.update { it.copy(isLoading = true) } }
                .catch { e -> _uiState.update { it.copy(isLoading = false, errorMessage = e.message) } }
                .collect { allOffers ->
                    val vacanciesList = allOffers.filter { it.isVacancy && it.status == "open" }
                    val applicationsList = allOffers.filter { it.managerId == managerId && it.isApplication }
                    
                    _uiState.update { currentState -> currentState.copy(
                        isLoading = false, 
                        vacancies = vacanciesList.sortedBy { it.leagueLevel },
                        myApplications = applicationsList
                    ) }
                }
        }
    }

    fun applyForJob(teamId: Int) {
        viewModelScope.launch {
            val gameState = gameManager.gameState.value
            if (gameState !is GameManager.GameState.Active) return@launch
            
            val success = jobMarketManager.applyForJob(
                managerId = gameState.context.managerId,
                teamId = teamId,
                currentWeek = gameState.context.week
            )
            
            if (!success) {
                _uiState.update { it.copy(errorMessage = "Application failed. You may already have a pending application for this club.") }
            }
        }
    }

    fun resign() {
        viewModelScope.launch {
            val gameState = gameManager.gameState.value
            if (gameState !is GameManager.GameState.Active) return@launch
            
            jobMarketManager.resign(
                managerId = gameState.context.managerId,
                teamId = gameState.context.teamId
            )
        }
    }
}
