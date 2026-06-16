package com.fameafrica.afm.ui.main

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fameafrica.afm.data.database.entities.*
import com.fameafrica.afm.data.model.CareerSaveModel
import com.fameafrica.afm.data.repository.*
import com.fameafrica.afm.domain.manager.CareerManager
import com.fameafrica.afm.domain.manager.GameManager
import com.fameafrica.afm.ui.theme.ClubThemeConfig
import com.fameafrica.afm.ui.theme.ClubThemeManager
import com.fameafrica.afm.utils.formatters.CurrencyFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SidebarUiState(
    val managerName: String = "",
    val clubName: String = "",
    val reputation: String = "",
    val balance: String = "",
    val nextMatch: String = "",
    val notificationsCount: Int = 0,
    val domesticCupName: String = "FA Cup",
    val clubTheme: ClubThemeConfig? = null
)

@HiltViewModel
class MainViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val managersRepository: ManagersRepository,
    private val notificationsRepository: NotificationsRepository,
    val fixturesRepository: FixturesRepository,
    private val gameManager: GameManager,
    private val careerManager: CareerManager,
    private val currencyFormatter: CurrencyFormatter
) : ViewModel() {

    private val careerId: Int = savedStateHandle.get<Int>("careerId") ?: -1

    private val _sidebarState = MutableStateFlow(SidebarUiState())
    val sidebarState: StateFlow<SidebarUiState> = _sidebarState.asStateFlow()

    private val _isWorldInitialized = MutableStateFlow(false)
    val isWorldInitialized: StateFlow<Boolean> = _isWorldInitialized.asStateFlow()

    val currentGameState: StateFlow<GameManager.GameState> = gameManager.gameState

    val isProcessing: StateFlow<Boolean> = gameManager.isProcessing

    val processingStatus: StateFlow<String> = gameManager.processingStatus

    val initializationState: StateFlow<GameManager.InitializationState> = gameManager.initializationState

    val currentManager: StateFlow<ManagersEntity?> = gameManager.gameState
        .map { state ->
            if (state is GameManager.GameState.Active) {
                managersRepository.getManagerById(state.context.managerId)
            } else null
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        // Load UI metadata from JSON immediately (this is safe without database)
        loadInitialMetadata()

        // Initialize the world and THEN observe game state
        val targetId = if (careerId != -1) careerId else null
        initializeWorldAndObserve(targetId)
    }

    private fun loadInitialMetadata() {
        val idToLoad = if (careerId != -1) careerId else -2

        viewModelScope.launch {
            val careers = careerManager.listCareers()
            val meta = if (idToLoad != -2) {
                careers.find { it.careerId == idToLoad }
            } else {
                careers.firstOrNull()
            }

            meta?.let { m ->
                _sidebarState.update {
                    it.copy(
                        managerName = m.managerName,
                        clubName = m.teamName,
                        reputation = m.difficulty,
                        clubTheme = ClubThemeManager.getThemeForTeam(m.teamName),
                        balance = "Connecting..."
                    )
                }
            }
        }
    }

    /**
     * Initialize world and ONLY THEN start observing game state
     */
    fun initializeWorldAndObserve(targetCareerId: Int?) {
        viewModelScope.launch {
            try {
                val idToLoad = targetCareerId ?: getLastActiveCareerId()

                if (idToLoad != null && idToLoad != -1) {
                    if (!_isWorldInitialized.value) {
                        // 🔥 CRITICAL: First ensure the career is loaded and database is initialized
                        careerManager.loadCareer(idToLoad)
                        _isWorldInitialized.value = true

                        // 🔥 Wait a moment for the database to be fully ready
                        delay(100)

                        // 🔥 NOW start observing game state AFTER database is ready
                        observeGameState()
                    }
                } else {
                    _isWorldInitialized.value = false
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Failed to initialize world", e)
                _isWorldInitialized.value = false
            }
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private fun observeGameState() {
        viewModelScope.launch {
            gameManager.gameState
                .flatMapLatest { state ->
                    if (state is GameManager.GameState.Active) {
                        combine(
                            gameManager.currentFinances,
                            gameManager.nextMatch,
                            notificationsRepository.getUnreadCountFlow()
                        ) { finances, nextMatch, unread ->
                            val context = state.context
                            val manager = managersRepository.getManagerById(context.managerId)

                            val formattedBalance = currencyFormatter.formatEuroAmount(finances?.bankBalance?.toDouble() ?: 0.0)

                            val nextMatchTitle = nextMatch?.let {
                                val opponent = if (it.homeTeam == context.teamName) it.awayTeam else it.homeTeam
                                val venue = if (it.homeTeam == context.teamName) "(H)" else "(A)"
                                "vs $opponent $venue"
                            } ?: "No fixture"

                            SidebarUiState(
                                managerName = manager?.name ?: context.managerName,
                                clubName = context.teamName,
                                reputation = manager?.reputationLevel ?: "Local",
                                balance = formattedBalance,
                                nextMatch = nextMatchTitle,
                                notificationsCount = unread,
                                domesticCupName = context.domesticCupName ?: "FA Cup",
                                clubTheme = ClubThemeManager.getThemeForTeam(context.teamName)
                            )
                        }
                    } else {
                        flowOf(_sidebarState.value)
                    }
                }
                .collect { newState ->
                    _sidebarState.value = newState
                }
        }
    }

    suspend fun getLastActiveCareerId(): Int? {
        return try {
            // First check if a career is already loaded in the manager
            val loadedId = careerManager.getCurrentCareerId()
            if (loadedId != null) return loadedId

            // If not, scan the career database files for the most recently played one
            // This is safer than querying the repository which might trigger a DB fallback
            careerManager.listCareers().firstOrNull()?.careerId
        } catch (e: Exception) {
            Log.e("MainViewModel", "Error getting last active career ID", e)
            null
        }
    }

    suspend fun hasActiveSave(): Boolean {
        return try {
            careerManager.listCareers().isNotEmpty()
        } catch (_: Exception) {
            false
        }
    }

    suspend fun getLatestSave(): CareerSaveModel? {
        return try {
            careerManager.listCareers().firstOrNull()
        } catch (_: Exception) {
            null
        }
    }

    fun processNextTurn() {
        gameManager.processNextTurn()
    }
}
