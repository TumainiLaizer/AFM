package com.fameafrica.afm.ui.screen.staff

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fameafrica.afm.data.database.entities.StaffEntity
import com.fameafrica.afm.data.database.entities.StaffRole
import com.fameafrica.afm.data.repository.StaffDashboard
import com.fameafrica.afm.data.repository.StaffRepository
import com.fameafrica.afm.domain.manager.GameManager
import com.fameafrica.afm.utils.formatters.CurrencyFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StaffUiState(
    val dashboard: StaffDashboard? = null,
    val coaches: List<StaffEntity> = emptyList(),
    val scouts: List<StaffEntity> = emptyList(),
    val medical: List<StaffEntity> = emptyList(),
    val admin: List<StaffEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentTeamName: String = "",
    val currentTeamId: Int = 0,
    val currencyContext: CurrencyFormatter.CurrencyContext? = null,
    val managerName: String = "",
    val managerAvatar: String? = null
)

@HiltViewModel
class StaffViewModel @Inject constructor(
    private val staffRepository: StaffRepository,
    private val gameManager: GameManager,
    private val currencyFormatter: CurrencyFormatter
) : ViewModel() {

    private val _uiState = MutableStateFlow(StaffUiState())
    val uiState: StateFlow<StaffUiState> = _uiState.asStateFlow()

    init {
        observeStaffData()
    }

    private fun observeStaffData() {
        viewModelScope.launch {
            // FM-Level Depth: React to Global Game State (Auto-refreshes after simulation ticks)
            gameManager.gameState.collect { state ->
                if (state is GameManager.GameState.Active) {
                    val context = state.context
                    _uiState.update { it.copy(
                        currentTeamName = context.teamName,
                        managerName = context.managerName,
                        managerAvatar = context.managerAvatar
                    ) }
                    refreshStaffInfo(context)
                }
            }
        }
    }

    private suspend fun refreshStaffInfo(context: GameManager.GameContext) {
        _uiState.update { it.copy(isLoading = true, currentTeamName = context.teamName, currentTeamId = context.teamId) }
        val currencyContext = currencyFormatter.getCurrentContext()

        try {
            combine(
                staffRepository.getCoachesByTeam(context.teamId),
                staffRepository.getScoutsByTeam(context.teamId),
                staffRepository.getMedicalStaffByTeam(context.teamId),
                staffRepository.getAdminStaffByTeam(context.teamId)
            ) { coaches, scouts, medical, admin ->
                val dashboard = staffRepository.getStaffDashboard(context.teamId)
                _uiState.update {
                    it.copy(
                        dashboard = dashboard,
                        coaches = coaches,
                        scouts = scouts,
                        medical = medical,
                        admin = admin,
                        isLoading = false,
                        currencyContext = currencyContext
                    )
                }
            }.catch { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }.collect()
        } catch (e: Exception) {
            _uiState.update { it.copy(isLoading = false, error = e.message) }
        }
    }

    fun hireStaff(
        name: String,
        role: StaffRole,
        specialization: String,
        impactRating: Int,
        salary: Int,
        age: Int
    ) {
        viewModelScope.launch {
            val teamId = _uiState.value.currentTeamId
            val teamName = _uiState.value.currentTeamName
            if (teamId != 0) {
                staffRepository.hireStaff(
                    name = name,
                    role = role,
                    teamId = teamId,
                    teamName = teamName,
                    specialization = specialization,
                    impactRating = impactRating,
                    salary = salary,
                    age = age
                )
            }
        }
    }

    fun fireStaff(staffId: Int) {
        viewModelScope.launch {
            staffRepository.fireStaff(staffId)
        }
    }

    fun renewContract(staffId: Int, years: Int, newSalary: Int? = null) {
        viewModelScope.launch {
            staffRepository.renewContract(staffId, years, newSalary)
        }
    }

    fun promoteToHeadOfDepartment(staffId: Int) {
        viewModelScope.launch {
            staffRepository.promoteToHeadOfDepartment(staffId)
        }
    }
}
