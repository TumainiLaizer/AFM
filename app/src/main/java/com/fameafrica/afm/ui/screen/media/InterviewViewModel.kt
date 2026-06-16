package com.fameafrica.afm.ui.screen.media

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fameafrica.afm.data.database.entities.InterviewsEntity
import com.fameafrica.afm.data.repository.GameStatesRepository
import com.fameafrica.afm.data.repository.InterviewsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InterviewUiState(
    val pendingInterviews: List<InterviewsEntity> = emptyList(),
    val scheduledInterviews: List<InterviewsEntity> = emptyList(),
    val completedInterviews: List<InterviewsEntity> = emptyList(),
    val selectedInterview: InterviewsEntity? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val managerId: Int? = null
)

@HiltViewModel
class InterviewViewModel @Inject constructor(
    private val interviewsRepository: InterviewsRepository,
    private val gameStatesRepository: GameStatesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InterviewUiState())
    val uiState: StateFlow<InterviewUiState> = _uiState.asStateFlow()

    init {
        loadManagerData()
    }

    private fun loadManagerData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Get current manager from latest save
            val latestSave = gameStatesRepository.getValidSaveGames().firstOrNull()?.firstOrNull()
            val managerId = latestSave?.managerId

            if (managerId != null) {
                _uiState.update { it.copy(managerId = managerId) }
                observeInterviews(managerId)
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Manager profile not found") }
            }
        }
    }

    private fun observeInterviews(managerId: Int) {
        interviewsRepository.getManagerInterviews(managerId)
            .onEach { allInterviews ->
                _uiState.update { state ->
                    state.copy(
                        pendingInterviews = allInterviews.filter { it.status == "Pending" },
                        scheduledInterviews = allInterviews.filter { it.status == "Scheduled" },
                        completedInterviews = allInterviews.filter { it.status == "Completed" },
                        isLoading = false
                    )
                }
            }
            .catch { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
            .launchIn(viewModelScope)
    }

    fun selectInterview(interviewId: Int) {
        viewModelScope.launch {
            val interview = interviewsRepository.getInterviewById(interviewId)
            _uiState.update { it.copy(selectedInterview = interview) }
        }
    }

    /**
     * Submit an answer to an interview question
     * Different response types have different impacts on morale and reputation
     */
    fun answerQuestion(
        interviewId: Int,
        response: String,
        responseType: String // POSITIVE, NEUTRAL, NEGATIVE
    ) {
        viewModelScope.launch {
            // Logic for impact calculation based on response type and journalist personality
            // This could be more complex, but for now we use deterministic values
            val impact = when (responseType) {
                "POSITIVE" -> 5
                "NEGATIVE" -> -5
                else -> 0
            }

            val reputationChange = when (responseType) {
                "POSITIVE" -> 2
                "NEGATIVE" -> -1
                else -> 0
            }

            val fanChange = when (responseType) {
                "POSITIVE" -> 3
                "NEGATIVE" -> -2
                else -> 1
            }

            interviewsRepository.completeInterview(
                interviewId = interviewId,
                response = response,
                responseType = responseType,
                impactOnMorale = impact,
                reputationChange = reputationChange,
                fanPopularityChange = fanChange
            )

            // Clear selection after completion
            _uiState.update { it.copy(selectedInterview = null) }
        }
    }

    fun declineInterview(interviewId: Int) {
        viewModelScope.launch {
            interviewsRepository. declineInterview(interviewId)
        }
    }

    fun scheduleInterview(interviewId: Int, date: String) {
        viewModelScope.launch {
            interviewsRepository.scheduleInterview(interviewId, date)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}