package com.fameafrica.afm.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fameafrica.afm.R
import com.fameafrica.afm.ui.components.common.TeamLogo
import com.fameafrica.afm.ui.theme.AFMTextStyles
import com.fameafrica.afm.ui.theme.FameColors
import com.fameafrica.afm.utils.extensions.toTitleCase

/**
 * Infinite horizontal scrolling news ticker.
 */
@Composable
fun InfiniteNewsTicker(
    headlines: List<String>,
    modifier: Modifier = Modifier
) {
    if (headlines.isEmpty()) return

    val scrollState = rememberScrollState()
    val tickerText = headlines.joinToString("   •   ")

    LaunchedEffect(Unit) {
        while (true) {
            scrollState.animateScrollTo(
                value = scrollState.maxValue,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 30000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            )
            scrollState.scrollTo(0)
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(28.dp),
        color = Color.Black.copy(alpha = 0.8f),
        border = BorderStroke(0.5.dp, FameColors.TrophyGold.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(scrollState, enabled = false),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = tickerText,
                style = AFMTextStyles.textXXS.copy(fontWeight = FontWeight.Black),
                color = FameColors.TrophyGold,
                modifier = Modifier.padding(horizontal = 16.dp),
                maxLines = 1
            )
            // Repeat to ensure seamless scrolling
            Text(
                text = tickerText,
                style = AFMTextStyles.textXXS.copy(fontWeight = FontWeight.Black),
                color = FameColors.TrophyGold,
                modifier = Modifier.padding(horizontal = 16.dp),
                maxLines = 1
            )
        }
    }
}

/**
 * Dense FCM26-style header.
 */
@Composable
fun CompactManagerHeader(
    managerName: String,
    clubName: String,
    level: Int,
    reputation: Int,
    coins: String,
    premium: String,
    notifications: Int,
    managerAvatar: String?,
    onProfileClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onAddCurrency: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            // Top: Resources
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ResourcePillCompact(amount = coins, icon = Icons.Default.MonetizationOn, color = FameColors.TrophyGold, onAdd = onAddCurrency)
                ResourcePillCompact(amount = premium, icon = Icons.Default.Token, color = FameColors.GrowthGreen, onAdd = onAddCurrency)
                
                Spacer(modifier = Modifier.weight(1f))
                
                Box(contentAlignment = Alignment.Center) {
                    IconButton(onClick = onNotificationsClick, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Notifications, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    if (notifications > 0) {
                        Surface(
                            modifier = Modifier.align(Alignment.TopEnd).size(14.dp),
                            color = FameColors.AlertRed,
                            shape = CircleShape
                        ) {
                            Text(
                                text = notifications.toString(),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                modifier = Modifier.wrapContentSize()
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Info
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(FameColors.TrophyGold.copy(alpha = 0.1f))
                        .border(1.dp, FameColors.TrophyGold.copy(alpha = 0.5f), CircleShape)
                        .clickable { onProfileClick() }
                ) {
                    AsyncImage(
                        model = managerAvatar ?: "file:///android_asset/manager_faces/east_africa_1_1.jpg",
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = managerName.toTitleCase(),
                        style = AFMTextStyles.textMD.copy(fontWeight = FontWeight.Black),
                        color = Color.White
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TeamLogo(clubName, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = clubName.toTitleCase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = FameColors.MutedParchment
                        )
                    }
                }

                // Level & Reputation
                Column(horizontalAlignment = Alignment.End) {
                    Surface(
                        color = FameColors.TrophyGold,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.height(18.dp).padding(horizontal = 6.dp)
                    ) {
                        Text("LVL $level", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.Black)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { reputation / 100f },
                        modifier = Modifier.width(60.dp).height(4.dp).clip(CircleShape),
                        color = FameColors.GrowthGreen,
                        trackColor = Color.White.copy(alpha = 0.1f)
                    )
                }
            }
        }
    }
}

/**
 * FCM26-style quick action row.
 */
@Composable
fun QuickActionRow(
    onActionClick: (String) -> Unit
) {
    val actions = listOf(
        Triple("Inbox", Icons.Default.Email, "INBOX"),
        Triple("Training", Icons.Default.FitnessCenter, "TRAINING"),
        Triple("Tactics", Icons.AutoMirrored.Filled.ListAlt, "TACTICS"),
        Triple("Scouts", Icons.Default.Search, "SCOUT"),
        Triple("Academy", Icons.Default.School, "ACADEMY"),
        Triple("Medical", Icons.Default.MedicalServices, "MEDICAL")
    )

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(actions) { (label, icon, id) ->
            QuickActionCard(label, icon) { onActionClick(id) }
        }
    }
}

@Composable
fun QuickActionCard(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.size(64.dp).clickable { onClick() },
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, tint = FameColors.TrophyGold, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

/**
 * Horizontal insight carousels.
 */
@Composable
fun InsightCarouselSection(
    uiState: com.fameafrica.afm.ui.screen.dashboard.DashboardUiState
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            InsightCard(
                title = "Squad Readiness",
                value = "84%",
                desc = "Team morale is high",
                icon = Icons.Default.Groups,
                color = FameColors.GrowthGreen
            )
        }
        item {
            InsightCard(
                title = "Financials",
                value = uiState.bankBalance,
                desc = "Monthly profit: +£1.2M",
                icon = Icons.Default.AccountBalanceWallet,
                color = FameColors.TrophyGold
            )
        }
        item {
            InsightCard(
                title = "Board Support",
                value = "${uiState.boardConfidence}%",
                desc = "Satisfied with transfers",
                icon = Icons.Default.BusinessCenter,
                color = FameColors.TransferBlue
            )
        }
    }
}

@Composable
fun InsightCard(
    title: String,
    value: String,
    desc: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Surface(
        modifier = Modifier.width(160.dp).height(80.dp),
        color = Color.Black.copy(alpha = 0.4f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(10.dp)) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(title.uppercase(), fontSize = 9.sp, fontWeight = FontWeight.Black, color = color)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(value, fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.White)
                Text(desc, fontSize = 9.sp, color = FameColors.MutedParchment, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}
