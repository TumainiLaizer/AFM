package com.fameafrica.afm.ui.screen.media

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fameafrica.afm.data.database.entities.InterviewsEntity
import com.fameafrica.afm.data.database.entities.NewsEntity
import com.fameafrica.afm.data.database.entities.PressConferencesEntity
import com.fameafrica.afm.data.repository.InterviewsRepository
import com.fameafrica.afm.data.repository.NewsRepository
import com.fameafrica.afm.data.repository.PressConferencesRepository
import com.fameafrica.afm.domain.manager.GameManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MediaUiState(
    val news: List<NewsEntity> = emptyList(),
    val pendingInterviews: List<InterviewsEntity> = emptyList(),
    val pendingPressConferences: List<PressConferencesEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentManagerId: Int? = null
)

@HiltViewModel
class MediaViewModel @Inject constructor(
    private val newsRepository: NewsRepository,
    private val interviewsRepository: InterviewsRepository,
    private val pressConferencesRepository: PressConferencesRepository,
    private val gameManager: GameManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(MediaUiState())
    val uiState: StateFlow<MediaUiState> = _uiState.asStateFlow()

    init {
        observeMediaData()
    }

    private fun observeMediaData() {
        viewModelScope.launch {
            // FM-Level Depth: Media inbox reacts to world events and timeline advances
            gameManager.gameState.collect { state ->
                if (state is GameManager.GameState.Active) {
                    val context = state.context
                    _uiState.update { it.copy(currentManagerId = context.managerId) }
                    loadMediaData(context.managerId)
                } else {
                    // Load global news even if no active game
                    newsRepository.getAllNews().collect { news ->
                        _uiState.update { it.copy(news = news.sortedByDescending { it.timestamp }) }
                    }
                }
            }
        }
    }

    private fun loadMediaData(managerId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            combine(
                newsRepository.getAllNews(),
                interviewsRepository.getPendingManagerInterviews(managerId),
                pressConferencesRepository.getPendingPressConferences(managerId)
            ) { news, interviews, press ->
                MediaUiState(
                    news = news.sortedByDescending { it.timestamp },
                    pendingInterviews = interviews,
                    pendingPressConferences = press,
                    currentManagerId = managerId,
                    isLoading = false
                )
            }.catch { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun submitPressResponse(pressId: Int, option: String) {
        viewModelScope.launch {
            pressConferencesRepository.submitResponse(pressId, option)
            // Refresh will happen via flow observation
        }
    }

    fun handleInterview(interviewId: Int, response: String, responseType: String) {
        viewModelScope.launch {
            interviewsRepository.completeInterview(
                interviewId = interviewId,
                response = response,
                responseType = responseType,
                impactOnMorale = if (responseType == "POSITIVE") 5 else -5,
                reputationChange = if (responseType == "POSITIVE") 2 else -1,
                fanPopularityChange = 2
            )
        }
    }
}
