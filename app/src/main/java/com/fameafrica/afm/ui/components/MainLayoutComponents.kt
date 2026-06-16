package com.fameafrica.afm.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fameafrica.afm.domain.manager.GameManager
import com.fameafrica.afm.ui.main.SidebarUiState
import com.fameafrica.afm.ui.theme.FameColors

@Composable
fun FameCommandTopBar(
    state: SidebarUiState,
    gameState: GameManager.GameState,
    isProcessing: Boolean,
    onContinueClick: () -> Unit,
    onMenuClick: () -> Unit,
    onNotificationsClick: () -> Unit
) {
    val activeContext = (gameState as? GameManager.GameState.Active)?.context

    Surface(
        color = FameColors.HeaderDark.copy(alpha = 0.95f),
        tonalElevation = 8.dp,
        modifier = Modifier.fillMaxWidth().statusBarsPadding()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. Menu Trigger
                IconButton(onClick = onMenuClick) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu")
                }

                // 2. Club Identity
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1.5f)) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        // Logo Placeholder
                        Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.padding(8.dp))
                    }
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            text = state.clubName.uppercase(),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = activeContext?.gameDateDisplay ?: "Pre-season",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                // 3. Financials (Dense)
                Row(
                    modifier = Modifier.weight(2f),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FinancialItem(label = "BAL", value = state.balance, color = FameColors.ChampionsGold)
                    VerticalDivider(Modifier.height(24.dp).padding(horizontal = 8.dp))
                    FinancialItem(label = "WAGE", value = "€840K", color = Color.White)
                }

                // 4. Board Confidence
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 8.dp)) {
                    Text("BOARD", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    CircularProgressIndicator(
                        progress = { 0.72f },
                        modifier = Modifier.size(24.dp),
                        color = FameColors.GrowthGreen,
                        strokeWidth = 3.dp,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeCap = ProgressIndicatorDefaults.CircularDeterminateStrokeCap,
                    )
                }

                // 5. Actions
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box {
                        IconButton(onClick = onNotificationsClick) {
                            Icon(Icons.Outlined.Notifications, contentDescription = "Inbox")
                        }
                        if (state.notificationsCount > 0) {
                            Badge(Modifier.align(Alignment.TopEnd).offset(x = (-4).dp, y = 4.dp)) {
                                Text(state.notificationsCount.toString(), fontSize = 8.sp)
                            }
                        }
                    }

                    Button(
                        onClick = onContinueClick,
                        enabled = !isProcessing,
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        modifier = Modifier.height(40.dp)
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Text("CONTINUE", fontWeight = FontWeight.Black, fontSize = 12.sp)
                        }
                    }
                }
            }

            // 6. World News Ticker (Persistent)
            NewsTickerStrip()
        }
    }
}

@Composable
fun FinancialItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(label, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        Text(value, fontSize = 11.sp, fontWeight = FontWeight.Black, color = color)
    }
}

@Composable
fun NewsTickerStrip() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
            .background(Color.Black.copy(alpha = 0.3f))
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                color = MaterialTheme.colorScheme.error,
                shape = RoundedCornerShape(2.dp),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text(
                    "BREAKING",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                    color = Color.White
                )
            }
            Text(
                "SIMBA SC SHOCK AFRICA: Late goal seals historic win in Cairo...",
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.9f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun FameBottomStatusBar(
    gameState: GameManager.GameState,
    isProcessing: Boolean,
    processingStatus: String
) {
    Surface(
        color = Color.Black.copy(alpha = 0.9f),
        modifier = Modifier.fillMaxWidth().navigationBarsPadding().height(28.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Dns,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = if (isProcessing) FameColors.ChampionsGold else FameColors.GrowthGreen
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = if (isProcessing) processingStatus else "AFM2026 Engine: Stable",
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )
            }

            Text(
                text = "WORLD SIMULATION v1.0.4 | TICK: WEEKLY",
                fontSize = 9.sp,
                color = Color.White.copy(alpha = 0.4f),
                fontWeight = FontWeight.Bold
            )
        }
    }
}
