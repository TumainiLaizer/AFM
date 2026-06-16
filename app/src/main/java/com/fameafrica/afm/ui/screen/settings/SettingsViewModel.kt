package com.fameafrica.afm.ui.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fameafrica.afm.data.database.entities.GameSettingsEntity
import com.fameafrica.afm.data.repository.GameSettingsRepository
import com.fameafrica.afm.domain.manager.GameManager
import com.fameafrica.afm.ui.audio.AudioManager
import com.fameafrica.afm.ui.theme.FootballThemePreset
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val settings: GameSettingsEntity? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val musicVolume: Float = 0.5f,
    val ambienceVolume: Float = 0.7f
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: GameSettingsRepository,
    private val audioManager: AudioManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
        loadAudioVolumes()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            repository.getSettings()
                .onStart { _uiState.update { it.copy(isLoading = true) } }
                .catch { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
                .collect { settings ->
                    if (settings == null) {
                        repository.insertSettings(GameSettingsEntity())
                    } else {
                        if (settings.music) audioManager.startBGM() else audioManager.pauseBGM()
                        _uiState.update { it.copy(settings = settings, isLoading = false) }
                    }
                }
        }
    }

    private fun loadAudioVolumes() {
        viewModelScope.launch {
            // Volume values are managed by AudioManager
        }
    }

    fun updateLanguage(language: String) {
        viewModelScope.launch { repository.updateLanguage(language) }
    }

    fun updateCurrency(currency: String) {
        viewModelScope.launch { repository.updateCurrency(currency) }
    }

    fun updateDifficulty(difficulty: String) {
        viewModelScope.launch { repository.updateDifficulty(difficulty) }
    }

    fun updateMatchSpeed(speed: Int) {
        viewModelScope.launch { repository.updateMatchSpeed(speed) }
    }

    fun updateThemePreset(preset: FootballThemePreset) {
        viewModelScope.launch {
            val current = _uiState.value.settings ?: return@launch
            repository.updateSettings(current.copy(themePreset = preset))
        }
    }

    fun toggleMusic(enabled: Boolean) {
        viewModelScope.launch {
            val current = _uiState.value.settings ?: return@launch
            repository.updateSettings(current.copy(music = enabled))
            if (enabled) audioManager.startBGM() else audioManager.pauseBGM()
        }
    }

    fun toggleSound(enabled: Boolean) {
        viewModelScope.launch {
            val current = _uiState.value.settings ?: return@launch
            repository.updateSettings(current.copy(soundEnabled = enabled))
        }
    }

    fun updateMusicVolume(volume: Float) {
        audioManager.setMusicVolume(volume)
        _uiState.update { it.copy(musicVolume = volume) }
    }

    fun updateAmbienceVolume(volume: Float) {
        audioManager.setAmbienceVolume(volume)
        _uiState.update { it.copy(ambienceVolume = volume) }
    }

    fun toggleAnimations(enabled: Boolean) {
        viewModelScope.launch {
            val current = _uiState.value.settings ?: return@launch
            repository.updateSettings(current.copy(animationsEnabled = enabled))
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            val current = _uiState.value.settings ?: return@launch
            repository.updateSettings(current.copy(notifications = enabled))
        }
    }

    fun toggleAutosave(enabled: Boolean) {
        viewModelScope.launch {
            val current = _uiState.value.settings ?: return@launch
            repository.updateSettings(current.copy(autosave = enabled))
        }
    }

    fun updateAutosaveFrequency(frequency: Int) {
        viewModelScope.launch {
            val current = _uiState.value.settings ?: return@launch
            repository.updateSettings(current.copy(autoSaveFrequency = frequency))
        }
    }

    fun updateFontSize(size: Int) {
        viewModelScope.launch {
            val current = _uiState.value.settings ?: return@launch
            repository.updateSettings(current.copy(fontSize = size))
        }
    }
}
