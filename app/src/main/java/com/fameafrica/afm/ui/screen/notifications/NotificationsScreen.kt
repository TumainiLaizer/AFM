package com.fameafrica.afm.ui.screen.notifications

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fameafrica.afm.ui.components.common.GlassPanel
import com.fameafrica.afm.ui.theme.*

@Composable
fun NotificationsScreen(
    onBack: () -> Unit,
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    AFM2026Theme(themePreset = FootballThemePreset.MANAGER_MODE) {
        StadiumBackground {
            NotificationsContent(
                uiState = uiState,
                onBack = onBack,
                onMarkAllRead = viewModel::markAllAsRead,
                onTabSelected = viewModel::selectTab,
                onNotificationClick = viewModel::markAsRead,
                onNotificationDismiss = viewModel::dismissNotification
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsContent(
    uiState: NotificationsUiState,
    onBack: () -> Unit,
    onMarkAllRead: () -> Unit,
    onTabSelected: (Int) -> Unit,
    onNotificationClick: (Int) -> Unit,
    onNotificationDismiss: (Int) -> Unit
) {
    val tabs = listOf("ALL", "MATCH", "TRANSFER", "INJURY", "BOARD")

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "COMMUNICATION HUB",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                actions = {
                    IconButton(onClick = onMarkAllRead) {
                        Icon(Icons.Default.DoneAll, null, tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            ScrollableTabRow(
                selectedTabIndex = uiState.selectedTab,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
                edgePadding = 16.dp,
                divider = {},
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[uiState.selectedTab]),
                        color = MaterialTheme.colorScheme.primary,
                        height = 2.dp
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = uiState.selectedTab == index,
                        onClick = { onTabSelected(index) },
                        text = {
                            Text(
                                title,
                                style = AFMTextStyles.tableHeader,
                                color = if (uiState.selectedTab == index) FameColors.ChampionsGold else FameColors.WarmIvory
                            )
                        }
                    )
                }
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = FameColors.ChampionsGold)
                }
            } else if (uiState.notifications.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No unread messages. High management efficiency detected.", style = AFMTextStyles.tableCell, color = FameColors.MutedParchment)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.notifications) { notification ->
                        NotificationItem(notification, onNotificationClick, onNotificationDismiss)
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItem(
    notification: NotificationUiModel,
    onClick: (Int) -> Unit,
    onDismiss: (Int) -> Unit
) {
    val isUnread = !notification.isRead
    val color = when (notification.type) {
        "MATCH" -> FameColors.PitchGreen
        "TRANSFER" -> FameColors.AfroSunOrange
        "INJURY", "BOARD" -> FameColors.KenteRed
        else -> FameColors.ChampionsGold
    }

    GlassPanel(
        modifier = Modifier.fillMaxWidth().clickable { onClick(notification.id) },
        containerColor = FameColors.HeaderDark,
        alpha = if (isUnread) 0.95f else 0.8f,
        border = BorderStroke(1.dp, if (isUnread) color.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.1f))
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(44.dp).clip(CircleShape).background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(notification.icon, fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(notification.title, style = AFMTextStyles.tableHeader, color = if (isUnread) color else FameColors.WarmIvory)
                    Text(notification.time, style = AFMTextStyles.statLabel, color = FameColors.MutedParchment)
                }
                Text(notification.message, style = AFMTextStyles.tableCell, color = if (isUnread) FameColors.WarmIvory else FameColors.MutedParchment, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            IconButton(onClick = { onDismiss(notification.id) }) {
                Icon(Icons.Default.Close, null, tint = FameColors.MutedParchment, modifier = Modifier.size(16.dp))
            }
        }
    }
}
