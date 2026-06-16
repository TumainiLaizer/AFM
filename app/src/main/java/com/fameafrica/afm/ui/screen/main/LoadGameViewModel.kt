package com.fameafrica.afm.ui.screen.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fameafrica.afm.domain.manager.CareerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoadGameUiState(
    val isLoading: Boolean = true,
    val saveGames: List<com.fameafrica.afm.data.model.CareerSaveModel> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class LoadGameViewModel @Inject constructor(
    private val careerManager: CareerManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoadGameUiState())
    val uiState: StateFlow<LoadGameUiState> = _uiState.asStateFlow()

    init {
        loadSaveGames()
    }

    fun loadSaveGames() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val careers = careerManager.listCareers()
                _uiState.update { it.copy(isLoading = false, saveGames = careers) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun deleteSave(id: Int) {
        viewModelScope.launch {
            careerManager.deleteCareer(id)
            loadSaveGames()
        }
    }
}
