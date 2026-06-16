package com.fameafrica.afm.ui.screen.scout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fameafrica.afm.data.database.dao.PriorityDistribution
import com.fameafrica.afm.data.database.dao.ScoutPerformanceStats
import com.fameafrica.afm.data.database.entities.*
import com.fameafrica.afm.data.repository.*
import com.fameafrica.afm.domain.manager.GameManager
import com.fameafrica.afm.domain.scouting.ScoutingIntelligenceService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScoutViewModel @Inject constructor(
    private val scoutAssignmentsRepository: ScoutAssignmentsRepository,
    private val scoutingMissionsRepository: ScoutingMissionsRepository,
    private val staffRepository: StaffRepository,
    private val playersRepository: PlayersRepository,
    private val nationalitiesRepository: NationalitiesRepository,
    private val leaguesRepository: LeaguesRepository,
    private val clubDNARepository: ClubDNARepository,
    private val gameManager: GameManager,
    private val scoutingService: ScoutingIntelligenceService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScoutUiState())
    val uiState: StateFlow<ScoutUiState> = _uiState.asStateFlow()

    private val _selectedScoutId = MutableStateFlow<Int?>(null)

    init {
        observeGameManager()
    }

    private fun observeGameManager() {
        viewModelScope.launch {
            gameManager.gameState.collect { state ->
                if (state is GameManager.GameState.Active) {
                    loadInitialData()
                    refreshData()
                    analyzeNetworkCoverage()
                }
            }
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            val countries = nationalitiesRepository.getAfricanNations().firstOrNull() ?: emptyList()
            val leagues = leaguesRepository.getAllLeagues().firstOrNull() ?: emptyList()
            val regions = listOf("North Africa", "West Africa", "East Africa", "Central Africa", "Southern Africa")
            
            _uiState.update { it.copy(
                availableCountries = countries,
                availableLeagues = leagues,
                availableRegions = regions
            ) }
        }
    }

    private fun refreshData() {
        loadScouts()
        loadAssignments()
        loadMissions()
        _selectedScoutId.value?.let { loadScoutDetails(it) }
    }

    private fun analyzeNetworkCoverage() {
        viewModelScope.launch {
            val teamId = gameManager.gameState.value.let { 
                if (it is GameManager.GameState.Active) it.context.teamId else null
            } ?: return@launch
            
            val clubDNA = clubDNARepository.getClubDNA(teamId)

            scoutingMissionsRepository.getActiveMissions().collect { missions ->
                val coverage = missions.map { it.targetIdentifier }.toSet()
                val advice = mutableListOf<String>()
                
                // Regional Identity Advice
                if (!coverage.contains("West Africa")) advice.add("Network Alert: West Africa is a hotbed for elite wingers. No scout currently assigned there.")
                if (!coverage.contains("North Africa")) advice.add("Market Insight: North African leagues have high technical standards. Consider deploying a scout to Egypt or Morocco.")
                
                // DNA Alignment Advice based on Regional Identity
                clubDNA?.region?.let { region ->
                    val regionName = region.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
                    if (!coverage.contains(regionName)) {
                        advice.add("Identity Focus: You haven't assigned scouts to $regionName. Domestic missions are 1.5x more efficient.")
                    }
                }

                if (missions.size < 3) advice.add("Efficiency: You have idle scouts. Deploy them on Regional Missions to build your player database.")
                
                _uiState.update { it.copy(networkAdvice = advice) }
            }
        }
    }

    fun onEvent(event: ScoutEvent) {
        when (event) {
            is ScoutEvent.SelectScout -> {
                _selectedScoutId.value = event.scoutId
                loadScoutDetails(event.scoutId)
            }
            is ScoutEvent.ChangeTab -> {
                _uiState.update { it.copy(selectedTab = event.tabIndex) }
            }
            is ScoutEvent.CreateAssignment -> createAssignment(event.scoutId, event.playerId, event.priority)
            is ScoutEvent.AssignMission -> assignMission(event.scoutId, event.missionType, event.target, event.priority)
            is ScoutEvent.CancelMission -> cancelMission(event.missionId)
            is ScoutEvent.CompleteAssignment -> completeAssignment(event.assignmentId, event.report)
            is ScoutEvent.FailAssignment -> failAssignment(event.assignmentId)
            is ScoutEvent.AutoGenerateReport -> autoGenerateReport(event.assignmentId)
            is ScoutEvent.SearchPlayers -> searchPlayers(event.query)
            is ScoutEvent.ClearSearch -> clearSearch()
            ScoutEvent.ClearSelectedScout -> {
                _selectedScoutId.value = null
                _uiState.update { it.copy(selectedScout = null, scoutDashboard = null) }
            }
        }
    }

    private fun loadScouts() {
        viewModelScope.launch {
            staffRepository.getAllStaff().collect { staff ->
                val scouts = staff.filter { it.isScout }
                _uiState.update { it.copy(scouts = scouts, isLoading = false) }
            }
        }
    }

    private fun loadAssignments() {
        viewModelScope.launch {
            combine(
                scoutAssignmentsRepository.getAllAssignments(),
                scoutAssignmentsRepository.getScoutPerformanceStats(),
                scoutAssignmentsRepository.getPriorityDistribution()
            ) { assignments, performanceStats, priorityDist ->
                _uiState.update {
                    it.copy(
                        allAssignments = assignments,
                        performanceStats = performanceStats,
                        priorityDistribution = priorityDist
                    )
                }
            }.collect()
        }
    }

    private fun loadMissions() {
        viewModelScope.launch {
            scoutingMissionsRepository.getAllMissions().collect { missions ->
                _uiState.update { it.copy(activeMissions = missions) }
            }
        }
    }

    private fun loadScoutDetails(scoutId: Int) {
        viewModelScope.launch {
            val scout = staffRepository.getStaffById(scoutId)
            val dashboard = scoutAssignmentsRepository.getScoutAssignmentsDashboard(scoutId)
            _uiState.update {
                it.copy(
                    selectedScout = scout,
                    scoutDashboard = dashboard,
                    selectedScoutId = scoutId
                )
            }
        }
    }

    private fun createAssignment(scoutId: Int, playerId: Int, priority: String) {
        viewModelScope.launch {
            val assignment = scoutAssignmentsRepository.assignScoutToPlayer(
                scoutId = scoutId,
                playerId = playerId,
                priority = priority
            )
            _uiState.update {
                it.copy(snackbarMessage = if (assignment != null) "Scout assigned to player" else "Scout is already busy with this player")
            }
        }
    }

    private fun assignMission(scoutId: Int, type: MissionType, target: String, priority: String) {
        viewModelScope.launch {
            val success = scoutingMissionsRepository.assignMission(scoutId, type, target, priority)
            _uiState.update {
                it.copy(snackbarMessage = if (success) "Scouting mission started: $target" else "Scout already has an active mission")
            }
            if (success) {
                _selectedScoutId.value?.let { loadScoutDetails(it) }
            }
        }
    }

    private fun cancelMission(missionId: Int) {
        viewModelScope.launch {
            scoutingMissionsRepository.cancelMission(missionId)
            _uiState.update { it.copy(snackbarMessage = "Mission cancelled") }
        }
    }

    private fun completeAssignment(assignmentId: Int, report: String) {
        viewModelScope.launch {
            val assignment = scoutAssignmentsRepository.getAssignmentById(assignmentId) ?: return@launch
            val player = playersRepository.getPlayerById(assignment.playerId) ?: return@launch
            val scout = staffRepository.getStaffById(assignment.scoutId)
            
            val scoutScore = scoutingService.calculateScoutScore(player, assignment, scout)
            val verdict = scoutingService.getRecommendationLabel(scoutScore)
            
            val success = scoutAssignmentsRepository.completeAssignment(
                assignmentId = assignmentId,
                report = report,
                rating = scoutScore, 
                estimatedValue = player.marketValue, 
                potentialRating = player.potential, 
                verdict = verdict
            )
            if (success) _uiState.update { it.copy(snackbarMessage = "Report filed: $verdict") }
        }
    }

    private fun failAssignment(assignmentId: Int) {
        viewModelScope.launch {
            val success = scoutAssignmentsRepository.failAssignment(assignmentId)
            if (success) _uiState.update { it.copy(snackbarMessage = "Scout failed to find information") }
        }
    }

    private fun autoGenerateReport(assignmentId: Int) {
        viewModelScope.launch {
            val success = scoutAssignmentsRepository.autoGenerateScoutReport(assignmentId)
            if (success) _uiState.update { it.copy(snackbarMessage = "New report received!") }
        }
    }

    private fun searchPlayers(query: String) {
        viewModelScope.launch {
            if (query.length < 2) {
                _uiState.update { it.copy(searchResults = emptyList(), isSearching = false) }
                return@launch
            }
            val results = playersRepository.searchPlayers(query).firstOrNull() ?: emptyList()
            _uiState.update { it.copy(searchResults = results, isSearching = true) }
        }
    }

    private fun clearSearch() {
        _uiState.update { it.copy(searchResults = emptyList(), isSearching = false) }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}

data class ScoutUiState(
    val isLoading: Boolean = true,
    val scouts: List<StaffEntity> = emptyList(),
    val selectedScout: StaffEntity? = null,
    val allAssignments: List<ScoutAssignmentsEntity> = emptyList(),
    val activeMissions: List<ScoutingMissionsEntity> = emptyList(),
    val selectedScoutId: Int? = null,
    val selectedTab: Int = 0,
    val scoutDashboard: ScoutAssignmentsDashboard? = null,
    val performanceStats: List<ScoutPerformanceStats> = emptyList(),
    val priorityDistribution: List<PriorityDistribution> = emptyList(),
    val searchResults: List<PlayersEntity> = emptyList(),
    val networkAdvice: List<String> = emptyList(),
    val isSearching: Boolean = false,
    val snackbarMessage: String? = null,
    
    val availableRegions: List<String> = emptyList(),
    val availableCountries: List<NationalitiesEntity> = emptyList(),
    val availableLeagues: List<LeaguesEntity> = emptyList()
)

sealed class ScoutEvent {
    data class SelectScout(val scoutId: Int) : ScoutEvent()
    object ClearSelectedScout : ScoutEvent()
    data class ChangeTab(val tabIndex: Int) : ScoutEvent()
    data class CreateAssignment(val scoutId: Int, val playerId: Int, val priority: String) : ScoutEvent()
    data class AssignMission(val scoutId: Int, val missionType: MissionType, val target: String, val priority: String) : ScoutEvent()
    data class CancelMission(val missionId: Int) : ScoutEvent()
    data class CompleteAssignment(val assignmentId: Int, val report: String) : ScoutEvent()
    data class FailAssignment(val assignmentId: Int) : ScoutEvent()
    data class AutoGenerateReport(val assignmentId: Int) : ScoutEvent()
    data class SearchPlayers(val query: String) : ScoutEvent()
    object ClearSearch : ScoutEvent()
}
