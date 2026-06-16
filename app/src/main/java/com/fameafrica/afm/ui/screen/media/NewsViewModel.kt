package com.fameafrica.afm.ui.screen.media

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fameafrica.afm.data.database.entities.NewsEntity
import com.fameafrica.afm.data.repository.NewsRepository
import com.fameafrica.afm.domain.manager.GameManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NewsUiState(
    val allNews: List<NewsEntity> = emptyList(),
    val topNews: List<NewsEntity> = emptyList(),
    val transferNews: List<NewsEntity> = emptyList(),
    val teamNews: List<NewsEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedCategory: String = "ALL"
)

@HiltViewModel
class NewsViewModel @Inject constructor(
    private val newsRepository: NewsRepository,
    private val gameManager: GameManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewsUiState())
    val uiState: StateFlow<NewsUiState> = _uiState.asStateFlow()

    init {
        observeGameManager()
    }

    private fun observeGameManager() {
        viewModelScope.launch {
            gameManager.gameState.collect { state ->
                if (state is GameManager.GameState.Active) {
                    loadNews(state.context.teamId)
                }
            }
        }
    }

    /**
     * FM-Level Depth: Pulls the latest headlines from the world simulation.
     */
    fun refresh() {
        val state = gameManager.gameState.value
        if (state is GameManager.GameState.Active) {
            loadNews(state.context.teamId)
        }
    }

    private fun loadNews(teamId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Using Dispatchers.IO for database access
            val news = withContext(Dispatchers.IO) {
                val allFlow = newsRepository.getAllNews().firstOrNull() ?: emptyList()
                val top = newsRepository.getTopNews(10).firstOrNull() ?: emptyList()
                val transfers = newsRepository.getTransferNews().firstOrNull() ?: emptyList()
                val team = newsRepository.getNewsByTeam(teamId).firstOrNull() ?: emptyList()

                NewsUiState(
                    allNews = allFlow.take(50).sortedByDescending { it.timestamp },
                    topNews = top,
                    transferNews = transfers.take(20),
                    teamNews = team.take(20),
                    isLoading = false
                )
            }
            _uiState.value = news
        }
    }

    fun setCategory(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
    }
}
