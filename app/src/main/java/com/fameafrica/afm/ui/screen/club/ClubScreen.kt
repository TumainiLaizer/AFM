package com.fameafrica.afm.ui.screen.club

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.fameafrica.afm.ui.theme.StadiumBackground
import com.fameafrica.afm.ui.components.common.GlassPanel
import com.fameafrica.afm.ui.theme.*
import com.fameafrica.afm.ui.components.*
import java.util.Locale

@Composable
fun ClubScreen(
    onBack: () -> Unit,
    onFinancesClick: () -> Unit,
    onInfrastructureClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onRenegotiateSponsor: (Int) -> Unit,
    onSearchSponsorsClick: () -> Unit,
    onNavigateToYouth: () -> Unit,
    viewModel: ClubViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("OVERVIEW", "FINANCES", "FACILITIES", "HISTORY")

    StadiumBackground {
        Scaffold(
            topBar = {
                ClubTopBar(
                    clubName = uiState.clubName,
                    reputationLevel = uiState.reputationLevel,
                    onBack = onBack
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                // Professional Tab Row (Glassy)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                        .padding(4.dp)
                ) {
                    tabs.forEachIndexed { index, label ->
                        val isSelected = selectedTab == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) FameColors.PitchGreen else Color.Transparent)
                                .clickable { selectedTab = index }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                label,
                                style = AFMTextStyles.textXXS,
                                color = if (isSelected) Color.White else FameColors.MutedParchment,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
                    if (uiState.isLoading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = FameColors.ChampionsGold)
                        }
                    } else {
                        when (selectedTab) {
                            0 -> ClubOverviewTab(uiState, onFinancesClick, onInfrastructureClick, onHistoryClick)
                            1 -> ClubFinancesTab(uiState, onFinancesClick, onRenegotiateSponsor, onSearchSponsorsClick)
                            2 -> ClubFacilitiesTab(uiState, onInfrastructureClick)
                            3 -> ClubHistoryTab(uiState, onHistoryClick)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ClubOverviewTab(
    uiState: ClubUiState,
    onFinancesClick: () -> Unit,
    onInfrastructureClick: () -> Unit,
    onHistoryClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            ClubHeaderCard(uiState.clubInfo)
        }
        item {
            QuickStatsRow(uiState.quickStats)
        }
        item {
            GlassPanel {
                Text("RECENT ACHIEVEMENTS", style = AFMTextStyles.tableHeader, color = FameColors.ChampionsGold)
                Spacer(modifier = Modifier.height(12.dp))
                RecentHistoryList(uiState.recentHistory, onHistoryClick)
            }
        }
        if (uiState.legends.isNotEmpty()) {
            item {
                Text("CLUB LEGENDS", style = AFMTextStyles.tableHeader, color = FameColors.ChampionsGold)
                Spacer(modifier = Modifier.height(8.dp))
                ClubLegendsList(uiState.legends)
            }
        }
    }
}

@Composable
fun ClubFinancesTab(uiState: ClubUiState, onFinancesClick: () -> Unit, onRenegotiateSponsor: (Int) -> Unit, onSearchSponsorsClick: () -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
        item { FinancialOverviewCard(uiState.finances, onFinancesClick) }
        item { 
            GlassPanel {
                Text("REVENUE STREAMS", style = AFMTextStyles.tableHeader, color = FameColors.ChampionsGold)
                Spacer(modifier = Modifier.height(12.dp))
                RevenueBreakdownList(uiState.revenueBreakdown)
            }
        }
        item { 
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("ACTIVE SPONSORSHIPS", style = AFMTextStyles.tableHeader, color = FameColors.ChampionsGold)
                TextButton(onClick = onSearchSponsorsClick) {
                    Text("FIND NEW", style = AFMTextStyles.textXXS, color = FameColors.TrophyGold)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            SponsorsList(uiState.sponsors, onRenegotiateSponsor) 
        }
    }
}

@Composable
fun ClubFacilitiesTab(uiState: ClubUiState, onInfrastructureClick: () -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
        item { InfrastructureStatusCard(uiState.infrastructure, onInfrastructureClick) }
        if (uiState.activeUpgrades.isNotEmpty()) {
            item { 
                GlassPanel {
                    Text("DEVELOPMENT PROJECTS", style = AFMTextStyles.tableHeader, color = FameColors.ChampionsGold)
                    Spacer(modifier = Modifier.height(12.dp))
                    ActiveUpgradesList(uiState.activeUpgrades)
                }
            }
        }
    }
}

@Composable
fun ClubHistoryTab(uiState: ClubUiState, onHistoryClick: () -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
        items(uiState.recentHistory) { item ->
            HistoryItem(item)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClubTopBar(clubName: String, reputationLevel: String, onBack: () -> Unit) {
    CenterAlignedTopAppBar(
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    clubName.uppercase(),
                    style = AFMTextStyles.textLG,
                    color = Color.White,
                    fontWeight = FontWeight.Black
                )
                ReputationBadge(level = reputationLevel)
            }
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
    )
}

@Composable
fun ClubHeaderCard(club: ClubInfoUiModel?) {
    if (club == null) return
    GlassPanel {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(64.dp).clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = 0.05f)), contentAlignment = Alignment.Center) {
                AsyncImage(model = club.logoUrl, contentDescription = null, modifier = Modifier.size(48.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(club.name, style = AFMTextStyles.textMD, color = Color.White, fontWeight = FontWeight.Black)
                Text(club.league.uppercase(), style = AFMTextStyles.textXXS, color = FameColors.ChampionsGold, fontWeight = FontWeight.Bold)
                Text(club.stadium, style = AFMTextStyles.textXS, color = FameColors.MutedParchment)
            }
        }
    }
}

@Composable
fun QuickStatsRow(stats: QuickStatsUiModel?) {
    if (stats == null) return
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StatPillSmall("POS", stats.leaguePosition.toString(), FameColors.ChampionsGold, Modifier.weight(1f))
        StatPillSmall("LOYALTY", "${stats.fanLoyalty}%", FameColors.PitchGreen, Modifier.weight(1f))
        StatPillSmall("CAP", "${stats.stadiumCapacity / 1000}K", FameColors.AfroSunOrange, Modifier.weight(1f))
    }
}

@Composable
fun StatPillSmall(label: String, value: String, color: Color, modifier: Modifier) {
    GlassPanel(modifier = modifier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = AFMTextStyles.textXXS, color = FameColors.MutedParchment)
            Text(value, style = AFMTextStyles.statValue, color = Color.White)
        }
    }
}

@Composable
fun FinancialOverviewCard(finances: FinancialUiModel?, onViewAll: () -> Unit) {
    if (finances == null) return
    GlassPanel {
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("BANK BALANCE", style = AFMTextStyles.tableHeader, color = FameColors.ChampionsGold)
                Text("€${finances.budget / 1000000}M", style = AFMTextStyles.statValue, color = Color.White)
            }
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { finances.budgetUsed },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                color = FameColors.PitchGreen,
                trackColor = Color.White.copy(alpha = 0.1f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                FinancialMetricItem("REVENUE", "€${finances.revenue / 1000000}M", finances.revenueChange, FameColors.PitchGreen)
                FinancialMetricItem("EXPENSES", "€${finances.expenses / 1000000}M", finances.expensesChange, FameColors.KenteRed)
            }
        }
    }
}

@Composable
fun FinancialMetricItem(label: String, value: String, change: Double, color: Color) {
    Column {
        Text(label, style = AFMTextStyles.textXXS, color = FameColors.MutedParchment)
        Text(value, style = AFMTextStyles.tableCell, color = Color.White, fontWeight = FontWeight.Black)
        Text("${if (change >= 0) "+" else ""}${String.format(Locale.getDefault(), "%.1f", change)}%", style = AFMTextStyles.textXXS, color = if (change >= 0) FameColors.PitchGreen else FameColors.KenteRed)
    }
}

@Composable
fun InfrastructureStatusCard(infra: InfrastructureUiModel?, onManage: () -> Unit) {
    if (infra == null) return
    GlassPanel(modifier = Modifier.clickable { onManage() }) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("CLUB FACILITIES", style = AFMTextStyles.tableHeader, color = FameColors.ChampionsGold)
            Icon(Icons.Default.ChevronRight, null, tint = FameColors.MutedParchment)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            FacilityProgressBar("STADIUM", infra.stadiumLevel, FameColors.ChampionsGold)
            FacilityProgressBar("TRAINING", infra.trainingLevel, FameColors.AfroSunOrange)
            FacilityProgressBar("YOUTH", infra.youthLevel, FameColors.AfricanLegendEmerald)
            FacilityProgressBar("MEDICAL", infra.medicalLevel, FameColors.KenteRed)
        }
    }
}

@Composable
fun FacilityProgressBar(label: String, level: Int, color: Color) {
    Column {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(label, style = AFMTextStyles.textXXS, color = Color.White, fontWeight = FontWeight.Bold)
            Text("LVL $level", style = AFMTextStyles.textXXS, color = color, fontWeight = FontWeight.Black)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(5) { i ->
                Box(modifier = Modifier.weight(1f).height(4.dp).clip(CircleShape).background(if (i < level) color else Color.White.copy(alpha = 0.1f)))
            }
        }
    }
}

@Composable
fun RevenueBreakdownList(data: List<RevenueItemUiModel>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        data.forEach { item ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(item.color))
                Spacer(modifier = Modifier.width(12.dp))
                Text(item.label, style = AFMTextStyles.textXS, color = Color.White, modifier = Modifier.weight(1f))
                Text("€${item.amount / 1000000}M", style = AFMTextStyles.tableCell, color = Color.White)
            }
        }
    }
}

@Composable
fun SponsorsList(sponsors: List<SponsorUiModel>, onClick: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        sponsors.forEach { sponsor ->
            GlassPanel(modifier = Modifier.clickable { onClick(sponsor.id) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.05f)), contentAlignment = Alignment.Center) {
                        AsyncImage(model = sponsor.logoUrl, contentDescription = null, modifier = Modifier.size(28.dp))
                    }
                    Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                        Text(sponsor.name, style = AFMTextStyles.textSM, color = Color.White, fontWeight = FontWeight.Bold)
                        Text(sponsor.type.uppercase(), style = AFMTextStyles.textXXS, color = FameColors.ChampionsGold)
                    }
                    Text("€${sponsor.value / 1000000}M", style = AFMTextStyles.tableCell, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun ActiveUpgradesList(upgrades: List<UpgradeUiModel>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        upgrades.forEach { upgrade ->
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(upgrade.name, style = AFMTextStyles.textSM, color = Color.White, fontWeight = FontWeight.Bold)
                    Text("${upgrade.progress}%", style = AFMTextStyles.textXXS, color = FameColors.AfroSunOrange)
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(progress = { upgrade.progress / 100f }, modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape), color = FameColors.AfroSunOrange, trackColor = Color.White.copy(alpha = 0.1f))
            }
        }
    }
}

@Composable
fun RecentHistoryList(history: List<HistoryUiModel>, onClick: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        history.take(3).forEach { item ->
            HistoryItem(item)
        }
    }
}

@Composable
fun HistoryItem(item: HistoryUiModel) {
    GlassPanel {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = if (item.type == "Trophy") Icons.Default.EmojiEvents else Icons.Default.Leaderboard, contentDescription = null, tint = if (item.type == "Trophy") FameColors.ChampionsGold else FameColors.PitchGreen, modifier = Modifier.size(24.dp))
            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(item.title, style = AFMTextStyles.textSM, color = Color.White, fontWeight = FontWeight.Bold)
                Text(item.season, style = AFMTextStyles.textXXS, color = FameColors.MutedParchment)
            }
            Text(item.achievement.uppercase(), style = AFMTextStyles.textXXS, color = Color.White, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun ClubLegendsList(legends: List<LegendUiModel>) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(legends) { legend ->
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(80.dp)) {
                Box(modifier = Modifier.size(56.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.05f)).border(1.dp, FameColors.ChampionsGold, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(32.dp))
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(legend.name, style = AFMTextStyles.textXXS, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
            }
        }
    }
}
