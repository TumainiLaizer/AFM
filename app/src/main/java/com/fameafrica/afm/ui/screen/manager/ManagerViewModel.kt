package com.fameafrica.afm.ui.screen.manager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fameafrica.afm.data.database.entities.*
import com.fameafrica.afm.data.repository.*
import com.fameafrica.afm.domain.manager.GameManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ManagerUiState(
    val isLoading: Boolean = true,
    val manager: ManagersEntity? = null,
    val clubStaff: List<StaffEntity> = emptyList(),
    val delegationSettings: Map<String, Boolean> = emptyMap(),
    val activeObjectives: List<ObjectivesEntity> = emptyList(),
    val careerHistory: List<FixturesResultsEntity> = emptyList(),
    val jobOffers: List<ManagerOffersEntity> = emptyList(),
    val agentName: String? = null,
    val errorMessage: String? = null
)

enum class ManagerTab {
    OVERVIEW, CAREER, STAFF, JOBS, PROFILE, HISTORY
}

@HiltViewModel
class ManagerViewModel @Inject constructor(
    private val managersRepository: ManagersRepository,
    private val staffRepository: StaffRepository,
    private val managerOffersRepository: ManagerOffersRepository,
    private val playerAgentsRepository: PlayerAgentsRepository,
    private val objectivesRepository: ObjectivesRepository,
    private val fixturesResultsRepository: FixturesResultsRepository,
    private val gameManager: GameManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManagerUiState())
    val uiState: StateFlow<ManagerUiState> = _uiState.asStateFlow()

    init {
        observeManagerData()
    }

    private fun observeManagerData() {
        viewModelScope.launch {
            gameManager.gameState.collect { state ->
                if (state is GameManager.GameState.Active) {
                    refreshManagerInfo(state.context)
                }
            }
        }
    }

    private suspend fun refreshManagerInfo(context: GameManager.GameContext) {
        try {
            val manager = managersRepository.getManagerById(context.managerId)
            val staff = staffRepository.getStaffByTeam(context.teamId).firstOrNull() ?: emptyList()
            val objectives = objectivesRepository.getObjectivesByTeam(context.teamId).firstOrNull() ?: emptyList()
            val history = fixturesResultsRepository.getRecentResultsByTeam(context.teamId, 20).firstOrNull() ?: emptyList()
            val offers = managerOffersRepository.getPendingOffersByManager(context.managerId).firstOrNull() ?: emptyList()

            val agentName = manager?.agentId?.let { playerAgentsRepository.getAgentById(it)?.agentName }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    manager = manager,
                    clubStaff = staff,
                    activeObjectives = objectives,
                    careerHistory = history,
                    jobOffers = offers,
                    agentName = agentName,
                    delegationSettings = mapOf(
                        "Training" to false,
                        "Scouting" to true,
                        "Contracts" to false,
                        "Press" to false
                    )
                )
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
        }
    }

    fun toggleDelegation(task: String) {
        val current = _uiState.value.delegationSettings.toMutableMap()
        current[task] = !(current[task] ?: false)
        _uiState.update { it.copy(delegationSettings = current) }
    }

    fun fireStaff(staffId: Int) {
        viewModelScope.launch {
            staffRepository.fireStaff(staffId)
            gameManager.gameState.value.let { state ->
                if (state is GameManager.GameState.Active) refreshManagerInfo(state.context)
            }
        }
    }

    fun resign() {
        viewModelScope.launch {
            val managerId = _uiState.value.manager?.id ?: return@launch
            managersRepository.leaveClub(managerId)
        }
    }

    fun acceptJobOffer(offerId: Int) {
        viewModelScope.launch {
            val currentState = gameManager.gameState.value
            if (currentState is GameManager.GameState.Active) {
                managerOffersRepository.acceptOffer(offerId, currentState.context.week)
            }
        }
    }

    fun rejectJobOffer(offerId: Int) {
        viewModelScope.launch {
            managerOffersRepository.rejectOffer(offerId)
        }
    }
}
