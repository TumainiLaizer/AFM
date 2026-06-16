package com.fameafrica.afm.ui.screen.national

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fameafrica.afm.data.database.entities.NationalTeamsEntity
import com.fameafrica.afm.data.database.entities.PlayersEntity
import com.fameafrica.afm.data.repository.NationalTeamDashboard
import com.fameafrica.afm.ui.components.common.GlassPanel
import com.fameafrica.afm.ui.theme.*
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun NationalScreen(
    onBack: () -> Unit,
    onTeamClick: (Int) -> Unit,
    viewModel: NationalViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    StadiumBackground {
        NationalScreenContent(
            uiState = uiState,
            onBack = onBack,
            onTeamClick = onTeamClick,
            onRefresh = { viewModel.refresh() },
            onResign = { viewModel.resignFromJob() },
            onApply = { viewModel.applyForJob(it) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NationalScreenContent(
    uiState: NationalUiState,
    onBack: () -> Unit,
    onTeamClick: (Int) -> Unit,
    onRefresh: () -> Unit,
    onResign: () -> Unit,
    onApply: (Int) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val coroutineScope = rememberCoroutineScope()
    val tabTitles = listOf("DASHBOARD", "CAF RANKINGS", "WORLD RANKINGS", "JOB BOARD")

    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "CONTINENTAL HUB",
                            style = MaterialTheme.typography.headlineSmall,
                            color = FameColors.WarmIvory,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = FameColors.WarmIvory)
                        }
                    },
                    actions = {
                        IconButton(onClick = onRefresh) {
                            Icon(Icons.Default.Refresh, null, tint = FameColors.WarmIvory)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )

                ScrollableTabRow(
                    selectedTabIndex = pagerState.currentPage,
                    containerColor = Color.Transparent,
                    contentColor = FameColors.ChampionsGold,
                    edgePadding = 16.dp,
                    divider = {},
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                            color = FameColors.ChampionsGold,
                            height = 2.dp
                        )
                    }
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } },
                            text = {
                                Text(
                                    title,
                                    style = AFMTextStyles.tableHeader,
                                    color = if (pagerState.currentPage == index) FameColors.ChampionsGold else FameColors.WarmIvory
                                )
                            }
                        )
                    }
                }
            }
        },
        containerColor = Color.Transparent,
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues) // Proper edge-to-edge handling
                .fillMaxSize()
        ) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = FameColors.ChampionsGold)
                }
            } else {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    beyondViewportPageCount = 1,
                    verticalAlignment = Alignment.Top
                ) { page ->
                    when (page) {
                        0 -> DashboardPage(uiState.managedTeamDashboard, onResign)
                        1 -> RankingsPage(uiState.africanRankings, "CAF CONTINENTAL RANKINGS", onTeamClick)
                        2 -> RankingsPage(uiState.worldRankings, "FIFA WORLD RANKINGS", onTeamClick)
                        3 -> JobBoardPage(uiState.availableJobs, onApply)
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardPage(
    dashboard: NationalTeamDashboard?,
    onResign: () -> Unit
) {
    if (dashboard == null) {
        Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
            GlassPanel {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Icon(Icons.Default.Flag, null, tint = FameColors.MutedParchment.copy(alpha = 0.3f), modifier = Modifier.size(72.dp))
                    Text("NO INTERNATIONAL POST", style = AFMTextStyles.tableHeader, color = FameColors.WarmIvory)
                    Text("Check the Job Board for open head coach positions in African nations.", style = AFMTextStyles.tableCell, color = FameColors.MutedParchment, textAlign = TextAlign.Center)
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp, 
                end = 16.dp, 
                top = 16.dp, 
                bottom = 16.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { NationalTeamHeaderCard(dashboard.team) }
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    NationalStatCard("CAF RANKING", "#${dashboard.cafRanking ?: "N/A"}", Icons.Default.Public, FameColors.ChampionsGold, Modifier.weight(1f))
                    NationalStatCard("SQUAD AVG", String.format(Locale.getDefault(), "%.1f", dashboard.averageRating), Icons.Default.Star, FameColors.PitchGreen, Modifier.weight(1f))
                }
            }
            item { SectionHeader("NATIONAL SQUAD", Icons.Default.Groups) }
            items(dashboard.starters, key = { it.id }) { player -> PlayerCard(player = player) }
            item {
                Button(
                    onClick = onResign,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FameColors.KenteRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("RESIGN FROM POST", style = AFMTextStyles.tableHeader, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun NationalTeamHeaderCard(team: NationalTeamsEntity) {
    GlassPanel(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(
                modifier = Modifier.size(60.dp).clip(CircleShape).background(FameColors.SurfaceLight).border(1.dp, FameColors.ChampionsGold, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(team.fifaCode, style = AFMTextStyles.statValue, color = FameColors.ChampionsGold)
            }
            Column {
                Text(team.name.uppercase(), style = MaterialTheme.typography.titleLarge, color = FameColors.WarmIvory, fontWeight = FontWeight.Black)
                Text(team.confederation, style = AFMTextStyles.statLabel, color = FameColors.PitchGreen, fontWeight = FontWeight.Bold)
                Text("ELO RATING: ${team.eloRating}", style = AFMTextStyles.statLabel, color = FameColors.MutedParchment)
            }
        }
    }
}

@Composable
fun NationalStatCard(label: String, value: String, icon: ImageVector, color: Color, modifier: Modifier) {
    GlassPanel(modifier = modifier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            Text(value, style = AFMTextStyles.statValue, color = FameColors.WarmIvory)
            Text(label, style = AFMTextStyles.statLabel, color = FameColors.MutedParchment)
        }
    }
}

@Composable
fun PlayerCard(player: PlayersEntity) {
    GlassPanel(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(modifier = Modifier.size(40.dp), shape = CircleShape, color = FameColors.ChampionsGold.copy(alpha = 0.1f), border = BorderStroke(1.dp, FameColors.ChampionsGold.copy(alpha = 0.3f))) {
                Box(contentAlignment = Alignment.Center) {
                    Text(player.rating.toString(), style = AFMTextStyles.statValue, color = FameColors.ChampionsGold)
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(player.name, style = AFMTextStyles.tableCell, color = FameColors.WarmIvory, fontWeight = FontWeight.Bold)
                Text(player.position, style = AFMTextStyles.statLabel, color = FameColors.PitchGreen)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(player.teamName, style = AFMTextStyles.statLabel, color = FameColors.MutedParchment, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("€${player.marketValue / 1_000_000}M", style = AFMTextStyles.statValue, color = FameColors.PitchGreen)
            }
        }
    }
}

@Composable
fun RankingsPage(teams: List<NationalTeamsEntity>, title: String, onTeamClick: (Int) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(), 
        contentPadding = PaddingValues(
            start = 16.dp, 
            end = 16.dp, 
            top = 16.dp, 
            bottom = 16.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        ), 
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { SectionHeader(title, Icons.Default.Leaderboard) }
        itemsIndexed(teams, key = { _, team -> team.id }) { index, team -> // Performance: Keyed items
            RankingItem(index + 1, team) { onTeamClick(team.id) } 
        }
    }
}

@Composable
fun RankingItem(rank: Int, team: NationalTeamsEntity, onClick: () -> Unit) {
    GlassPanel(modifier = Modifier.fillMaxWidth().clickable { onClick() }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(rank.toString(), style = AFMTextStyles.statValue, color = if (rank <= 3) FameColors.ChampionsGold else FameColors.MutedParchment, modifier = Modifier.width(32.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(team.name, style = AFMTextStyles.tableCell, color = FameColors.WarmIvory)
                Text(team.fifaCode, style = AFMTextStyles.statLabel, color = FameColors.BaobabBrown)
            }
            Text(team.eloRating.toString(), style = AFMTextStyles.statValue, color = FameColors.PitchGreen)
        }
    }
}

@Composable
fun JobBoardPage(jobs: List<NationalTeamsEntity>, onApply: (Int) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(), 
        contentPadding = PaddingValues(
            start = 16.dp, 
            end = 16.dp, 
            top = 16.dp, 
            bottom = 16.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        ), 
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { SectionHeader("AVAILABLE VACANCIES", Icons.Default.Work) }
        if (jobs.isEmpty()) {
            item { Text("No current vacancies. Maintain a high club reputation to attract offers.", style = AFMTextStyles.tableCell, color = FameColors.MutedParchment, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(32.dp)) }
        } else {
            items(jobs, key = { it.id }) { team -> // Performance: Keyed items
                JobCard(team) { onApply(team.id) } 
            }
        }
    }
}

@Composable
fun JobCard(team: NationalTeamsEntity, onApply: () -> Unit) {
    GlassPanel(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(FameColors.PitchGreen.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                    Text(team.fifaCode, style = AFMTextStyles.tableHeader, color = FameColors.PitchGreen)
                }
                Column {
                    Text(team.name.uppercase(), style = AFMTextStyles.tableHeader, color = FameColors.WarmIvory)
                    Text(team.confederation, style = AFMTextStyles.statLabel, color = FameColors.PitchGreen)
                }
            }
            Text("Seeking a visionary head coach to elevate the national program. High performance required.", style = AFMTextStyles.statLabel, color = FameColors.MutedParchment)
            Button(onClick = onApply, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = FameColors.ChampionsGold), shape = RoundedCornerShape(12.dp)) {
                Text("APPLY FOR JOB", style = AFMTextStyles.tableHeader, color = FameColors.StadiumBlack)
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, icon: ImageVector) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = FameColors.ChampionsGold, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, style = AFMTextStyles.tableHeader, color = FameColors.WarmIvory, fontWeight = FontWeight.Black)
    }
}
