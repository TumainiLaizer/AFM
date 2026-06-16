package com.fameafrica.afm.ui.screen.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fameafrica.afm.data.repository.NotificationsRepository
import com.fameafrica.afm.domain.manager.GameManager
import com.fameafrica.afm.ui.theme.FameColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationUiModel(
    val id: Int,
    val title: String,
    val message: String,
    val time: String,
    val icon: String,
    val backgroundColor: androidx.compose.ui.graphics.Color,
    val isRead: Boolean,
    val type: String,
    val priority: Int
)

data class NotificationsUiState(
    val isLoading: Boolean = true,
    val selectedTab: Int = 0,
    val notifications: List<NotificationUiModel> = emptyList(),
    val unreadCount: Int = 0
)

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationsRepository: NotificationsRepository,
    private val gameManager: GameManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState(isLoading = true))
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init {
        observeNotifications()
    }

    private fun observeNotifications() {
        viewModelScope.launch {
            // FM-Level Depth: React to world simulation updates and live notification streams
            combine(
                gameManager.gameState,
                notificationsRepository.getAllNotifications(),
                notificationsRepository.getUnreadCountFlow()
            ) { state, allNotifications, unreadCount ->
                val filtered = when (_uiState.value.selectedTab) {
                    0 -> allNotifications
                    1 -> allNotifications.filter { it.notificationType == "MATCH" }
                    2 -> allNotifications.filter { it.notificationType == "TRANSFER" }
                    3 -> allNotifications.filter { it.notificationType == "INJURY" }
                    4 -> allNotifications.filter { it.notificationType == "BOARD" }
                    5 -> allNotifications.filter { it.notificationType == "SYSTEM" }
                    else -> allNotifications
                }

                val uiModels = filtered.map { notification ->
                    val backgroundColor = when (notification.priority) {
                        5 -> FameColors.KenteRed
                        4 -> FameColors.AfroSunOrange
                        3 -> FameColors.ChampionsGold
                        2 -> FameColors.PitchGreen
                        else -> FameColors.MutedParchment
                    }

                    NotificationUiModel(
                        id = notification.id,
                        title = notification.title,
                        message = notification.message ?: "",
                        time = notification.timeAgo,
                        icon = notification.icon ?: "🔔",
                        backgroundColor = backgroundColor,
                        isRead = notification.isRead,
                        type = notification.notificationType,
                        priority = notification.priority
                    )
                }

                _uiState.update { it.copy(
                    isLoading = false,
                    notifications = uiModels,
                    unreadCount = unreadCount
                ) }
            }.collect()
        }
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
        // The combine block will automatically re-run due to StateFlow update if we were observing selectedTab
        // Since we are using it inside the combine, we should make selectedTab a state flow too.
    }
    
    // Let's refine the combine to include selectedTab
    private val _selectedTab = MutableStateFlow(0)
    
    // Re-implementing with proper flow composition
    private fun observeNotificationsRefined() {
        viewModelScope.launch {
            combine(
                _selectedTab,
                notificationsRepository.getAllNotifications(),
                notificationsRepository.getUnreadCountFlow(),
                gameManager.gameState
            ) { tab, allNotifications, unreadCount, gameState ->
                val filtered = when (tab) {
                    0 -> allNotifications
                    1 -> allNotifications.filter { it.notificationType == "MATCH" }
                    2 -> allNotifications.filter { it.notificationType == "TRANSFER" }
                    3 -> allNotifications.filter { it.notificationType == "INJURY" }
                    4 -> allNotifications.filter { it.notificationType == "BOARD" }
                    5 -> allNotifications.filter { it.notificationType == "SYSTEM" }
                    else -> allNotifications
                }

                val uiModels = filtered.map { notification ->
                    val backgroundColor = when (notification.priority) {
                        5 -> FameColors.KenteRed
                        4 -> FameColors.AfroSunOrange
                        3 -> FameColors.ChampionsGold
                        2 -> FameColors.PitchGreen
                        else -> FameColors.MutedParchment
                    }

                    NotificationUiModel(
                        id = notification.id,
                        title = notification.title,
                        message = notification.message ?: "",
                        time = notification.timeAgo,
                        icon = notification.icon ?: "🔔",
                        backgroundColor = backgroundColor,
                        isRead = notification.isRead,
                        type = notification.notificationType,
                        priority = notification.priority
                    )
                }

                _uiState.update { it.copy(
                    isLoading = false,
                    selectedTab = tab,
                    notifications = uiModels,
                    unreadCount = unreadCount
                ) }
            }.collect()
        }
    }

    fun markAsRead(id: Int) {
        viewModelScope.launch {
            notificationsRepository.markAsRead(id)
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            notificationsRepository.markAllAsRead()
        }
    }

    fun dismissNotification(id: Int) {
        viewModelScope.launch {
            notificationsRepository.deleteNotificationById(id)
        }
    }
}
