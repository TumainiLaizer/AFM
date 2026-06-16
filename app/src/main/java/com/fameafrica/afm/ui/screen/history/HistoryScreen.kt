package com.fameafrica.afm.ui.screen.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fameafrica.afm.data.database.entities.SeasonAwardsEntity
import com.fameafrica.afm.data.database.entities.SeasonHistoryEntity
import com.fameafrica.afm.data.database.entities.TrophiesEntity
import com.fameafrica.afm.ui.components.common.GlassPanel
import com.fameafrica.afm.ui.theme.*

@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    AFM2026Theme(themePreset = FootballThemePreset.MANAGER_MODE) {
        BoardBackground {
            HistoryContent(uiState = uiState, onBack = onBack)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryContent(
    uiState: HistoryUiState,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "HALL OF FAME",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (uiState.trophies.isNotEmpty()) {
                    item { SectionHeader("TROPHY CABINET", Icons.Default.EmojiEvents) }
                    items(uiState.trophies) { trophy -> TrophyItem(trophy) }
                }

                if (uiState.coachAwards.isNotEmpty() || uiState.playerAwards.isNotEmpty()) {
                    item { SectionHeader("INDIVIDUAL HONOURS", Icons.Default.EmojiEvents) }
                    items(uiState.coachAwards) { award -> AwardItem(award) }
                    items(uiState.playerAwards) { award -> AwardItem(award) }
                }

                item { SectionHeader("SEASON BY SEASON", Icons.Default.History) }
                items(uiState.seasonHistory) { history -> SeasonHistoryItem(history) }
                
                if (uiState.seasonHistory.isEmpty() && uiState.trophies.isEmpty() && uiState.coachAwards.isEmpty()) {
                    item {
                        GlassPanel(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                "Your legend begins today. Lead your club to glory to fill these halls.",
                                style = AFMTextStyles.tableCell,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().padding(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, style = AFMTextStyles.tableHeader, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Black)
    }
}

@Composable
fun AwardItem(award: SeasonAwardsEntity) {
    GlassPanel(modifier = Modifier.fillMaxWidth(), cornerRadius = 16) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "🎖️", style = MaterialTheme.typography.headlineLarge)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(award.awardDisplay.uppercase(), style = AFMTextStyles.tableHeader, color = MaterialTheme.colorScheme.primary)
                Text("${award.recipient.uppercase()} • SEASON ${award.season}", style = AFMTextStyles.statLabel, color = MaterialTheme.colorScheme.onSurface)
                if (!award.leagueName.isNullOrEmpty()) {
                    Text(award.leagueName.uppercase(), style = AFMTextStyles.statLabel, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun TrophyItem(trophy: TrophiesEntity) {
    GlassPanel(modifier = Modifier.fillMaxWidth(), cornerRadius = 16) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "🏆", style = MaterialTheme.typography.headlineLarge)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(trophy.trophyName.uppercase(), style = AFMTextStyles.tableHeader, color = MaterialTheme.colorScheme.primary)
                Text("SEASON ${trophy.season} • ${trophy.competitionLevel.uppercase()}", style = AFMTextStyles.statLabel, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
fun SeasonHistoryItem(history: SeasonHistoryEntity) {
    GlassPanel(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(history.season, style = AFMTextStyles.tableHeader, color = MaterialTheme.colorScheme.onSurface)
                Surface(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp)) {
                    Text(
                        text = (history.leagueName ?: "").uppercase(),
                        style = AFMTextStyles.statLabel,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                HistoryStat("POS", "#${history.position}", MaterialTheme.colorScheme.primary)
                HistoryStat("PTS", "${history.points}", MaterialTheme.colorScheme.onSurface)
                HistoryStat("W-D-L", "${history.wins}-${history.draws}-${history.losses}", MaterialTheme.colorScheme.onSurface)
                HistoryStat("GD", "${history.goalDifference}", if ((history.goalDifference ?: 0) >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
            }
            
            if (history.trophiesWon > 0) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.EmojiEvents, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("CHAMPION: ${history.trophiesWon} TROPHIES", style = AFMTextStyles.statLabel, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
fun HistoryStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = AFMTextStyles.statLabel, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = AFMTextStyles.statValue, color = color)
    }
}
