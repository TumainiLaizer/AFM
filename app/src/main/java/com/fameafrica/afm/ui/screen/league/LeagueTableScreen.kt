package com.fameafrica.afm.ui.screen.league

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import java.util.Locale
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.fameafrica.afm.ui.components.common.TeamLogo
import com.fameafrica.afm.ui.screen.match.FixtureUiModel
import com.fameafrica.afm.ui.theme.*
import com.fameafrica.afm.utils.LeagueLogoUtils
import kotlinx.coroutines.launch

@Composable
fun LeagueTableScreen(
    leagueName: String,
    onBack: () -> Unit,
    viewModel: LeagueTableViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    LaunchedEffect(leagueName) {
        viewModel.loadLeagueData(leagueName)
    }

    AFM2026Theme {
        LeagueTableContent(
            leagueName = leagueName,
            uiState = uiState,
            onBack = onBack,
            onTOTWRoundChange = { round ->
                scope.launch {
                    viewModel.loadTOTW(uiState.leagueName, round, uiState.season)
                }
            },
            onLeagueChange = { newLeague ->
                viewModel.loadLeagueData(newLeague)
            }
        )
    }
}

@Composable
fun LeagueTableContent(
    leagueName: String,
    uiState: LeagueTableUiState,
    onBack: () -> Unit,
    onTOTWRoundChange: (Int) -> Unit,
    onLeagueChange: (String) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val scope = rememberCoroutineScope()
    val tabs = listOf("TABLE", "SCORERS", "FIXTURES", "TOTW")
    var showLeagueSelector by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        // "LEAGUE TABLE" Header
        Surface(
            color = Color.Black,
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("LEAGUE", color = Color.White, fontWeight = FontWeight.Black, fontSize = 24.sp)
                    Text("TABLE", color = Color.White, fontWeight = FontWeight.Light, fontSize = 24.sp, letterSpacing = 2.sp)
                }
            }
        }

        // League Sub-header with Selector
        Surface(
            color = FameColors.HeaderDark,
            modifier = Modifier.fillMaxWidth().height(48.dp).clickable { showLeagueSelector = true }
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val leagueLogo = LeagueLogoUtils.getLeagueLogo(context, uiState.leagueName)
                when (leagueLogo) {
                    is Int -> Icon(
                        painter = painterResource(leagueLogo),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = Color.Unspecified
                    )
                    is String -> AsyncImage(
                        model = leagueLogo,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        error = painterResource(com.fameafrica.afm.R.drawable.default_premier_league)
                    )
                    else -> Icon(
                        painter = painterResource(com.fameafrica.afm.R.drawable.default_premier_league),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = FameColors.TrophyGold
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(uiState.leagueName.uppercase(), color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp, letterSpacing = 1.sp)
                Icon(Icons.Default.ArrowDropDown, null, tint = Color.White)
            }
        }

        if (showLeagueSelector) {
            LeagueSelectorDialog(
                leagues = uiState.availableLeagues,
                onDismiss = { showLeagueSelector = false },
                onSelect = { 
                    onLeagueChange(it)
                    showLeagueSelector = false
                }
            )
        }

        // TabRow
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = Color.Black.copy(alpha = 0.5f),
            contentColor = FameColors.TrophyGold,
            divider = {},
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                    color = FameColors.TrophyGold
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                    text = { Text(title, style = AFMTextStyles.textXXS, fontWeight = FontWeight.Black) }
                )
            }
        }

        if (uiState.isLoading) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = FameColors.TrophyGold)
            }
        } else {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                when (page) {
                    0 -> LeagueTablePage(uiState)
                    1 -> TopScorersPage(uiState)
                    2 -> FixturesPage(uiState)
                    3 -> TeamOfTheWeekPage(
                        totwPlayers = uiState.teamOfTheWeek,
                        formation = uiState.totwFormation,
                        currentRound = uiState.teamOfTheWeekRound,
                        maxRounds = uiState.maxRounds,
                        onRoundChange = onTOTWRoundChange
                    )
                }
            }
        }
    }
}


@Composable
fun LeagueTablePage(uiState: LeagueTableUiState) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Table Header Row
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(30.dp)) // Rank
            Spacer(modifier = Modifier.width(20.dp)) // Logo
            Text("TEAM", modifier = Modifier.weight(1f), style = AFMTextStyles.textXXS, color = FameColors.MutedParchment, fontWeight = FontWeight.Bold)
            
            HeaderStatCell("PL")
            HeaderStatCell("GD")
            HeaderStatCell("PTS")
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            itemsIndexed(uiState.standings, key = { _, team -> team.id }) { index, team ->
                RefinedLeagueTableRow(index + 1, team, team.id == uiState.userTeamId)
                HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
            }
        }
    }
}

@Composable
fun TopScorersPage(uiState: LeagueTableUiState) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(uiState.topScorers) { scorer ->
            Surface(
                color = Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(scorer.position.toString(), style = AFMTextStyles.textSM, color = FameColors.TrophyGold, fontWeight = FontWeight.Black, modifier = Modifier.width(24.dp))
                    
                    TeamLogo(
                        teamName = scorer.teamName,
                        modifier = Modifier.size(24.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(scorer.playerName.uppercase(), style = AFMTextStyles.textSM, color = Color.White, fontWeight = FontWeight.Black)
                        Text(scorer.teamName, style = AFMTextStyles.textXXS, color = FameColors.MutedParchment)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(scorer.goals.toString(), style = AFMTextStyles.textMD, color = Color.White, fontWeight = FontWeight.Black)
                        Text("GOALS", style = AFMTextStyles.textXXS, color = FameColors.MutedParchment)
                    }
                }
            }
        }
    }
}

@Composable
fun FixturesPage(uiState: LeagueTableUiState) {
    val groupedFixtures = uiState.fixtures.groupBy { it.round }
    
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        groupedFixtures.forEach { (round, fixtures) ->
            item {
                Text("ROUND $round", style = AFMTextStyles.textXS, color = FameColors.TrophyGold, fontWeight = FontWeight.Black)
            }
            items(fixtures) { fixture ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(fixture.homeTeam, style = AFMTextStyles.textXXS, modifier = Modifier.weight(1f), textAlign = TextAlign.End, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    
                    Surface(
                        color = Color.White.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(2.dp),
                        modifier = Modifier.padding(horizontal = 12.dp)
                    ) {
                        Text(
                            text = if (fixture.status == "FT") "${fixture.homeScore} - ${fixture.awayScore}" else "VS",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = AFMTextStyles.textXXS,
                            fontWeight = FontWeight.Black,
                            color = if (fixture.status == "FT") Color.White else FameColors.TrophyGold
                        )
                    }
                    
                    Text(fixture.awayTeam, style = AFMTextStyles.textXXS, modifier = Modifier.weight(1f), textAlign = TextAlign.Start, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Composable
fun HeaderStatCell(text: String) {
    Text(
        text = text,
        modifier = Modifier.width(40.dp),
        textAlign = TextAlign.Center,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = FameColors.TrophyGold
    )
}

@Composable
fun RefinedLeagueTableRow(rank: Int, team: LeagueStandingUiModel, isUserTeam: Boolean) {
    Surface(
        color = if (isUserTeam) FameColors.PitchGreen.copy(alpha = 0.15f) else Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                rank.toString(), 
                modifier = Modifier.width(24.dp), 
                style = AFMTextStyles.textXXS, 
                color = if (rank <= 3) FameColors.TrophyGold else FameColors.MutedParchment,
                fontWeight = FontWeight.Black
            )
            
            TeamLogo(
                teamName = team.teamName,
                modifier = Modifier.size(16.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                team.teamName.uppercase(), 
                modifier = Modifier.weight(1f), 
                style = AFMTextStyles.textXXS, 
                color = if (isUserTeam) Color.White else FameColors.WarmIvory,
                fontWeight = if (isUserTeam) FontWeight.Black else FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Row(modifier = Modifier.width(120.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(team.played.toString(), modifier = Modifier.width(40.dp), textAlign = TextAlign.Center, style = AFMTextStyles.textXXS, color = Color.White)
                Text(team.goalDifference.toString(), modifier = Modifier.width(40.dp), textAlign = TextAlign.Center, style = AFMTextStyles.textXXS, color = FameColors.MutedParchment)
                Text(team.points.toString(), modifier = Modifier.width(40.dp), textAlign = TextAlign.Center, style = AFMTextStyles.textXXS.copy(fontWeight = FontWeight.Black), color = Color.White)
            }
        }
    }
}

@Composable
fun TrendIndicator(rank: Int) {
    val trend = when {
        rank % 5 == 0 -> "UP"
        rank % 7 == 0 -> "DOWN"
        else -> "STABLE"
    }
    
    val color = when(trend) {
        "UP" -> Color.Green
        "DOWN" -> Color.Red
        else -> Color.Gray.copy(alpha = 0.4f)
    }
    
    val icon = when(trend) {
        "UP" -> Icons.Default.ArrowDropUp
        "DOWN" -> Icons.Default.ArrowDropDown
        else -> Icons.Default.FiberManualRecord
    }

    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = color,
        modifier = Modifier.size(16.dp)
    )
}

@Composable
fun StatCell(text: String, fontWeight: FontWeight = FontWeight.Normal) {
    Text(
        text = text,
        modifier = Modifier.width(40.dp),
        textAlign = TextAlign.Center,
        fontSize = 13.sp,
        fontWeight = fontWeight,
        color = Color.Black
    )
}

@Composable
fun LeagueSelectorDialog(
    leagues: List<com.fameafrica.afm.data.database.entities.LeaguesEntity>,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("SELECT COMPETITION", style = AFMTextStyles.textMD, color = FameColors.TrophyGold, fontWeight = FontWeight.Black) },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                items(leagues) { league ->
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable { onSelect(league.name) },
                        color = Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val leagueLogo = LeagueLogoUtils.getLeagueLogo(context, league.name)
                            when (leagueLogo) {
                                is Int -> Icon(
                                    painter = painterResource(leagueLogo),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    tint = Color.Unspecified
                                )
                                is String -> AsyncImage(
                                    model = leagueLogo,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    error = painterResource(com.fameafrica.afm.R.drawable.default_premier_league)
                                )
                                else -> Icon(
                                    painter = painterResource(com.fameafrica.afm.R.drawable.default_premier_league),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    tint = FameColors.TrophyGold
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(league.name.uppercase(), style = AFMTextStyles.textSM, color = Color.White)
                        }
                    }
                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CLOSE", color = FameColors.MutedParchment)
            }
        },
        containerColor = FameColors.HeaderDark,
        shape = RoundedCornerShape(4.dp)
    )
}

@Composable
fun TeamOfTheWeekPage(
    totwPlayers: List<TOTWPlayerUiModel>,
    formation: String,
    currentRound: Int,
    maxRounds: Int,
    onRoundChange: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("TEAM OF THE WEEK", style = AFM2026Typography.titleMedium, color = FameColors.ChampionsGold, fontWeight = FontWeight.Black)
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { if (currentRound > 1) onRoundChange(currentRound - 1) }) {
                    Icon(Icons.Default.ChevronLeft, "Prev", tint = Color.White)
                }
                Text("RD $currentRound", style = AFM2026Typography.labelLarge, color = Color.White)
                IconButton(onClick = { if (currentRound < maxRounds) onRoundChange(currentRound + 1) }) {
                    Icon(Icons.Default.ChevronRight, "Next", tint = Color.White)
                }
            }
        }
        
        Text("FORMATION: $formation", style = AFM2026Typography.labelSmall, color = FameColors.MutedParchment, modifier = Modifier.padding(bottom = 16.dp))

        if (totwPlayers.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No TOTW data for this round", color = FameColors.MutedParchment)
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(totwPlayers) { player ->
                    TOTWPlayerRow(player)
                }
            }
        }
    }
}

@Composable
fun TOTWPlayerRow(player: TOTWPlayerUiModel) {
    Surface(
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(FameColors.ChampionsGold, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(player.position, style = AFM2026Typography.labelSmall, color = Color.Black, fontWeight = FontWeight.Black)
            }
            
            Spacer(Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TeamLogo(
                        teamName = player.teamName,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(player.playerName.uppercase(), style = AFM2026Typography.bodyMedium, color = Color.White, fontWeight = FontWeight.Bold)
                }
                Text(player.teamName, style = AFM2026Typography.labelSmall, color = FameColors.MutedParchment)
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(String.format(Locale.ROOT, "%.1f", player.matchRating), style = AFM2026Typography.titleSmall, color = FameColors.PitchGreen, fontWeight = FontWeight.Black)
                if (player.motm) {
                    Icon(Icons.Default.Star, "MOTM", tint = FameColors.ChampionsGold, modifier = Modifier.size(12.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LeagueTableScreenPreview() {
    AFM2026Theme {
        LeagueTableContent(
            leagueName = "TANZANIA PREMIER LEAGUE",
            uiState = LeagueTableUiState(
                isLoading = false,
                leagueName = "TANZANIA PREMIER LEAGUE",
                standings = listOf(
                    LeagueStandingUiModel(
                        id = 1,
                        position = 1,
                        teamName = "Al Ahly",
                        played = 10,
                        wins = 8,
                        draws = 1,
                        losses = 1,
                        gf = 25,
                        ga = 10,
                        goalDifference = 15,
                        points = 25,
                        form = "WWWLW",
                        logoPath = null
                    ),
                    LeagueStandingUiModel(
                        id = 2,
                        position = 2,
                        teamName = "Simba SC",
                        played = 10,
                        wins = 7,
                        draws = 2,
                        losses = 1,
                        gf = 30,
                        ga = 12,
                        goalDifference = 18,
                        points = 23,
                        form = "WWDWW",
                        logoPath = null
                    ),
                    LeagueStandingUiModel(
                        id = 3,
                        position = 3,
                        teamName = "TP Mazembe",
                        played = 10,
                        wins = 7,
                        draws = 1,
                        losses = 2,
                        gf = 22,
                        ga = 11,
                        goalDifference = 11,
                        points = 22,
                        form = "WWLWL",
                        logoPath = null
                    )
                ),
                topScorers = listOf(
                    TopScorerUiModel(1, 101, "Mabrouk", "Al Ahly", 12, 10, 24),
                    TopScorerUiModel(2, 102, "Chama", "Simba SC", 10, 10, 32)
                ),
                fixtures = listOf(
                    FixtureUiModel(1, "Al Ahly", "Simba SC", 2, 1, "FT", 1),
                    FixtureUiModel(2, "TP Mazembe", "Sundowns", 0, 0, "SCHEDULED", 1)
                ),
                teamOfTheWeek = listOf(
                    TOTWPlayerUiModel(101, "Mabrouk", "Al Ahly", "ST", 9.5, null, 0, 1, 0, false, true),
                    TOTWPlayerUiModel(102, "Chama", "Simba SC", "RW", 8.8, null, 0, 0, 1, false, false)
                )
            ),
            onBack = {},
            onTOTWRoundChange = {},
            onLeagueChange = {}
        )
    }
}
