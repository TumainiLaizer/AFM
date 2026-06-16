package com.fameafrica.afm.ui.screen.world

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.fameafrica.afm.data.database.model.GlobalClubRanking
import com.fameafrica.afm.data.database.model.GlobalLeagueRanking
import com.fameafrica.afm.data.database.model.GlobalManagerRanking
import com.fameafrica.afm.domain.model.SimulationEvent
import com.fameafrica.afm.ui.components.common.SidebarSectionTitle
import com.fameafrica.afm.ui.components.common.TeamLogo
import com.fameafrica.afm.ui.components.*
import com.fameafrica.afm.ui.screen.dashboard.NewsUiModel
import com.fameafrica.afm.ui.theme.AFMTextStyles
import com.fameafrica.afm.ui.theme.AFM2026Theme
import com.fameafrica.afm.ui.theme.FameColors
import com.fameafrica.afm.ui.theme.StadiumBackground
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun WorldScreen(
    onNavigateToLeague: (String) -> Unit,
    onNavigateToCup: (String) -> Unit,
    onNavigateToMatch: (Int) -> Unit,
    viewModel: WorldViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AFM2026Theme {
        StadiumBackground {
            WorldContent(
                uiState = uiState,
                onNavigateToLeague = onNavigateToLeague,
                onNavigateToCup = onNavigateToCup,
                onNavigateToMatch = onNavigateToMatch,
                onContinentSelected = { viewModel.selectContinent(it) }
            )
        }
    }
}

@Composable
fun WorldContent(
    uiState: WorldUiState,
    onNavigateToLeague: (String) -> Unit,
    onNavigateToCup: (String) -> Unit,
    onNavigateToMatch: (Int) -> Unit,
    onContinentSelected: (String) -> Unit
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    var selectedTab by remember { mutableIntStateOf(0) }

    Surface(
        modifier = Modifier.fillMaxSize(), 
        color = Color.Transparent
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            WorldBroadcastHeaderAAA(uiState.selectedContinent)
            
            WorldNewsTicker(uiState.latestNews)
            
            if (isLandscape) {
                WorldLandscapeLayout(uiState, onNavigateToLeague, onNavigateToMatch, onContinentSelected)
            } else {
                WorldTabSelectorAAA(selectedTab) { selectedTab = it }
                
                Box(modifier = Modifier.weight(1f)) {
                    AnimatedContent(
                        targetState = selectedTab,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "WorldTabTransition"
                    ) { tab ->
                        when (tab) {
                            0 -> WorldHubTabAAA(uiState, onNavigateToMatch, onNavigateToCup, onContinentSelected)
                            1 -> WorldRankingsTabAAA(uiState, onNavigateToLeague)
                            2 -> WorldStatisticsTabAAA(uiState)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WorldNewsTicker(news: List<NewsUiModel>) {
    val headlines = news.joinToString("   •   ") { it.title.uppercase() }
    val scrollState = rememberScrollState()
    
    LaunchedEffect(headlines) {
        if (headlines.isNotEmpty()) {
            while(true) {
                scrollState.animateScrollTo(
                    scrollState.maxValue,
                    animationSpec = tween(durationMillis = headlines.length * 150, easing = LinearEasing)
                )
                scrollState.scrollTo(0)
                delay(1000)
            }
        }
    }

    Surface(
        color = FameColors.AlertRed,
        modifier = Modifier.fillMaxWidth().height(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().horizontalScroll(scrollState, enabled = false),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "BREAKING NEWS: $headlines",
                style = AFMTextStyles.textXS.copy(fontWeight = FontWeight.Black, fontSize = 10.sp),
                color = Color.White,
                modifier = Modifier.padding(horizontal = 16.dp),
                maxLines = 1
            )
        }
    }
}

@Composable
fun WorldBroadcastHeaderAAA(region: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "LiveBlink")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 0.2f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse), label = ""
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Black,
        border = BorderStroke(0.5.dp, FameColors.TrophyGold.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "AFRICAN FOOTBALL CONTROL ROOM",
                    style = AFMTextStyles.textXS.copy(fontWeight = FontWeight.Black, letterSpacing = 1.5.sp),
                    color = FameColors.TrophyGold
                )
                Text(
                    text = region.uppercase(),
                    style = AFMTextStyles.textLG.copy(fontWeight = FontWeight.Black, fontSize = 20.sp),
                    color = Color.White
                )
            }
            
            Surface(
                color = FameColors.AlertRed.copy(alpha = 0.1f),
                shape = RoundedCornerShape(2.dp),
                border = BorderStroke(1.dp, FameColors.AlertRed.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(FameColors.AlertRed.copy(alpha = alpha)))
                    Spacer(Modifier.width(6.dp))
                    Text("LIVE FEED", style = AFMTextStyles.textXS.copy(fontWeight = FontWeight.Black, fontSize = 8.sp), color = Color.White)
                }
            }
        }
    }
}

@Composable
fun WorldTabSelectorAAA(selected: Int, onSelected: (Int) -> Unit) {
    TabRow(
        selectedTabIndex = selected,
        containerColor = Color.Black.copy(alpha = 0.5f),
        contentColor = FameColors.TrophyGold,
        divider = {},
        indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
                Modifier.tabIndicatorOffset(tabPositions[selected]),
                color = FameColors.TrophyGold,
                height = 2.dp
            )
        }
    ) {
        val tabs = listOf("HUB", "RANKINGS", "STATS")
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = selected == index,
                onClick = { onSelected(index) },
                text = {
                    Text(
                        text = title,
                        style = AFMTextStyles.textXS.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp),
                        maxLines = 1
                    )
                }
            )
        }
    }
}

@Composable
fun WorldHubTabAAA(
    uiState: WorldUiState,
    onNavigateToMatch: (Int) -> Unit,
    onNavigateToCup: (String) -> Unit,
    onContinentSelected: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { TrendingStorylinesSection() }

        item {
            SidebarSectionTitle("LIVE FOOTBALL FEED")
            WorldSocialFeedAAA(uiState.dailySimulationEvents, uiState.latestNews)
        }

        item {
            RegionalSelectorAAA(uiState.selectedContinent, onContinentSelected)
        }

        item {
            SidebarSectionTitle("MAJOR COMPETITIONS")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(uiState.featuredCompetitions) { comp ->
                    FeaturedCompCardAAA(comp) { onNavigateToCup(comp.name) }
                }
            }
        }

        item {
            SidebarSectionTitle("WONDERKID TRACKER")
            WonderkidTrackerPanel(uiState.wonderkids)
        }

        item {
            SidebarSectionTitle("MARKET ACTIVITY")
            TransferActivityPanelAAA(uiState.transferNews)
        }

        item {
            SidebarSectionTitle("CONTINENTAL FIXTURES")
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                uiState.internationalFixtures.take(5).forEach { InternationalFixtureRowAAA(it) }
            }
        }

        item { Spacer(Modifier.height(40.dp)) }
    }
}

@Composable
fun TrendingStorylinesSection() {
    Column {
        SidebarSectionTitle("TRENDING STORYLINES")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            item { StorylineCard("CAF CL TITLE RACE", "Al Ahly and Mamelodi Sundowns locked in fierce continental battle.", Icons.Default.LocalFireDepartment) }
            item { StorylineCard("SHOCK SACKING", "Simba SC manager under intense pressure after derby loss.", Icons.Default.Warning) }
            item { StorylineCard("WONDERKID WATCH", "Young striker in West Africa attracting European giants.", Icons.Default.Visibility) }
        }
    }
}

@Composable
fun StorylineCard(title: String, desc: String, icon: ImageVector) {
    Surface(
        modifier = Modifier.width(220.dp),
        color = Color.White.copy(alpha = 0.03f),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = FameColors.AfroSunOrange, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(title, style = AFMTextStyles.textXS.copy(fontWeight = FontWeight.Black), color = FameColors.AfroSunOrange)
            }
            Spacer(Modifier.height(8.dp))
            Text(desc.uppercase(), style = AFMTextStyles.textXS, color = Color.White, lineHeight = 14.sp)
        }
    }
}

@Composable
fun WorldRankingsTabAAA(uiState: WorldUiState, onNavigateToLeague: (String) -> Unit) {
    var selectedRegion by remember { mutableStateOf("ALL") }
    val regions = listOf("ALL", "East Africa", "North Africa", "Southern Africa", "West Africa", "Central Africa", "Others")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            RegionalFilterChairmanAAA(selectedRegion, regions) { selectedRegion = it }
        }

        item {
            val filteredLeagues = if (selectedRegion == "ALL") uiState.globalLeagueRankings else uiState.globalLeagueRankings.filter { it.region == selectedRegion }
            SidebarSectionTitle("LEAGUE RANKINGS")
            LeagueRankingsTableAAA(filteredLeagues, onNavigateToLeague)
        }

        item {
            SidebarSectionTitle("CLUB RANKINGS")
            ClubRankingsTableAAA(uiState.globalClubRankings)
        }

        item {
            SidebarSectionTitle("MANAGER RANKINGS")
            ManagerRankingsTableAAA(uiState.globalManagerRankings)
        }

        item {
            RankingUpdateFooterAAA(uiState.rankingsLastUpdated)
        }

        item { Spacer(Modifier.height(40.dp)) }
    }
}

@Composable
fun WorldStatisticsTabAAA(uiState: WorldUiState) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { SidebarSectionTitle("CONTINENTAL PERFORMANCE") }
        item { WorldStatisticsCenterDenseAAA(uiState.worldStats) }
        
        item { SidebarSectionTitle("INTERNATIONAL FIXTURES") }
        items(uiState.internationalFixtures.take(15)) { fixture ->
            InternationalFixtureRowAAA(fixture)
            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
fun WorldLandscapeLayout(
    uiState: WorldUiState,
    onNavigateToLeague: (String) -> Unit,
    onNavigateToMatch: (Int) -> Unit,
    onContinentSelected: (String) -> Unit
) {
    Row(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .width(220.dp)
                .fillMaxHeight()
                .background(Color.Black)
                .border(width = 0.5.dp, color = Color.White.copy(alpha = 0.1f))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "WORLD BROADCAST",
                style = AFMTextStyles.textXS.copy(fontWeight = FontWeight.Black, color = FameColors.AlertRed),
                letterSpacing = 1.sp
            )
            WorldSocialFeedAAA(uiState.dailySimulationEvents.take(4), uiState.latestNews.take(4))
            
            Spacer(modifier = Modifier.weight(1f))
            
            RegionalSelectorAAA(uiState.selectedContinent, onContinentSelected)
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(horizontal = 12.dp)
        ) {
            WorldBroadcastHeaderAAA(uiState.selectedContinent)
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    SidebarSectionTitle("TOP LEAGUE RANKINGS")
                    LeagueRankingsTableAAA(uiState.globalLeagueRankings.take(8), onNavigateToLeague)
                }
            }
        }

        Column(
            modifier = Modifier
                .width(260.dp)
                .fillMaxHeight()
                .background(FameColors.HeaderDark.copy(alpha = 0.7f))
                .border(width = 0.5.dp, color = Color.White.copy(alpha = 0.1f))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "GLOBAL INSIGHTS",
                style = AFMTextStyles.textXS.copy(fontWeight = FontWeight.Black, color = FameColors.TrophyGold)
            )
            WorldStatisticsCenterDenseAAA(uiState.worldStats)
            
            SidebarSectionTitle("MARKET ACTIVITY")
            TransferActivityPanelAAA(uiState.transferNews)
        }
    }
}

@Composable
fun WorldSocialFeedAAA(events: List<SimulationEvent>, news: List<NewsUiModel>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // High Impact Events first
        events.filter { it.importance >= 7 }.forEach { event ->
            SimulationEventCard(event)
        }

        // News Items
        news.take(10).forEach { item ->
            NewsFeedCard(item)
        }
        
        // Minor events
        events.filter { it.importance < 7 }.take(5).forEach { event ->
            SimulationEventCard(event)
        }
    }
}

@Composable
fun NewsFeedCard(item: NewsUiModel) {
    Surface(
        color = Color.Black.copy(alpha = 0.3f),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = item.category.uppercase(),
                style = AFMTextStyles.textXS.copy(fontWeight = FontWeight.Black, color = FameColors.AfroSunOrange)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = item.title.uppercase(),
                style = AFMTextStyles.textSM.copy(fontWeight = FontWeight.Black),
                color = Color.White
            )
        }
    }
}

@Composable
fun TeamColumnAAA(name: String, isLeft: Boolean) {
    Column(
        horizontalAlignment = if (isLeft) Alignment.End else Alignment.Start,
        modifier = Modifier.width(120.dp)
    ) {
        TeamLogo(name, Modifier.size(36.dp))
        Spacer(Modifier.height(4.dp))
        Text(
            name.uppercase(),
            style = AFMTextStyles.textSM.copy(fontWeight = FontWeight.Black, fontSize = 12.sp),
            color = Color.White,
            textAlign = if (isLeft) TextAlign.End else TextAlign.Start,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun RegionalSelectorAAA(selected: String, onSelected: (String) -> Unit) {
    val regions = listOf("East Africa", "North Africa", "Southern Africa", "West Africa", "Central Africa", "Others")
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("BROWSE REGIONS", style = AFMTextStyles.textXS.copy(fontWeight = FontWeight.Black, color = Color.White.copy(alpha = 0.4f), letterSpacing = 1.sp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(regions) { region ->
                val isSelected = selected == region
                Surface(
                    modifier = Modifier.clickable { onSelected(region) },
                    color = if (isSelected) FameColors.TrophyGold else FameColors.HeaderDark.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(2.dp),
                    border = BorderStroke(0.5.dp, if (isSelected) FameColors.TrophyGold else Color.White.copy(0.1f))
                ) {
                    Text(
                        region.uppercase(),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        style = AFMTextStyles.textXS.copy(fontWeight = FontWeight.Black, fontSize = 10.sp),
                        color = if (isSelected) Color.Black else Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun WonderkidTrackerPanel(wonderkids: List<PlayerStatUiModel>) {
    DashboardSectionCard(modifier = Modifier.fillMaxWidth()) {
        if (wonderkids.isEmpty()) {
            Text(
                "Searching for the next African legend...",
                style = AFMTextStyles.textXS,
                color = FameColors.MutedParchment,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            wonderkids.take(5).forEach { player ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(player.name.uppercase(), style = AFMTextStyles.textSM.copy(fontWeight = FontWeight.Black), color = Color.White)
                        Text(player.teamName, style = AFMTextStyles.textXXS, color = FameColors.MutedParchment)
                    }
                    Surface(
                        color = FameColors.ChampionsGold.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(2.dp),
                        border = BorderStroke(0.5.dp, FameColors.ChampionsGold)
                    ) {
                        Text(
                            "POT: ${player.value}",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = AFMTextStyles.textXXS.copy(fontWeight = FontWeight.Black),
                            color = FameColors.ChampionsGold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FeaturedCompCardAAA(comp: CompetitionUiModel, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .width(170.dp)
            .height(100.dp)
            .clickable { onClick() },
        color = Color.Black.copy(alpha = 0.6f),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, Color.White.copy(0.05f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp), 
            verticalArrangement = Arrangement.Center, 
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.EmojiEvents, null, tint = FameColors.TrophyGold, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(6.dp))
            Text(
                comp.name.uppercase(),
                style = AFMTextStyles.textXS.copy(fontWeight = FontWeight.Black, fontSize = 10.sp),
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun TransferActivityPanelAAA(transfers: List<TransferNewsUiModel>) {
    DashboardSectionCard(modifier = Modifier.fillMaxWidth()) {
        transfers.take(3).forEach { transfer ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(transfer.player.uppercase(), style = AFMTextStyles.textSM.copy(fontWeight = FontWeight.Black), color = Color.White)
                    Text("${transfer.fromTeam} → ${transfer.toTeam}", style = AFMTextStyles.textXXS, color = FameColors.MutedParchment)
                }
                Text("€${transfer.fee / 1_000_000}M", style = AFMTextStyles.textXXS.copy(fontWeight = FontWeight.Black), color = FameColors.TrophyGold)
            }
        }
    }
}

@Composable
fun RegionalFilterChairmanAAA(selected: String, regions: List<String>, onSelected: (String) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        items(regions) { region ->
            FilterChip(
                selected = selected == region,
                onClick = { onSelected(region) },
                label = { Text(region.uppercase()) }
            )
        }
    }
}

@Composable
fun LeagueRankingsTableAAA(rankings: List<GlobalLeagueRanking>, onNavigateToLeague: (String) -> Unit) {
    DashboardSectionCard(modifier = Modifier.fillMaxWidth()) {
        rankings.forEach { r ->
            Row(
                modifier = Modifier.fillMaxWidth().clickable { onNavigateToLeague(r.leagueName) }.padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${r.rank}", modifier = Modifier.width(20.dp), style = AFMTextStyles.textXXS, color = FameColors.TrophyGold)
                
                AsyncImage(
                    model = r.logoPath,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp).clip(CircleShape),
                    error = painterResource(com.fameafrica.afm.R.drawable.default_premier_league)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    r.leagueName.uppercase(), 
                    modifier = Modifier.weight(1f), 
                    style = AFMTextStyles.textXXS, 
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                RatingBadge(
                    rating = (r.averageRating * 20).toInt(),
                    textStyle = AFMTextStyles.textXXS.copy(fontWeight = FontWeight.Black)
                )
            }
        }
    }
}

@Composable
fun ClubRankingsTableAAA(rankings: List<GlobalClubRanking>) {
    DashboardSectionCard(modifier = Modifier.fillMaxWidth()) {
        rankings.take(10).forEach { r ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${r.rank}", modifier = Modifier.width(20.dp), style = AFMTextStyles.textXXS, color = FameColors.TrophyGold)
                
                AsyncImage(
                    model = r.logoPath,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp).clip(CircleShape),
                    error = painterResource(com.fameafrica.afm.R.drawable.default_club)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    r.clubName.uppercase(), 
                    modifier = Modifier.weight(1f), 
                    style = AFMTextStyles.textXXS, 
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text("€${r.totalMarketValue / 1_000_000}M", style = AFMTextStyles.textXXS, color = FameColors.GrowthGreen)
            }
        }
    }
}

@Composable
fun ManagerRankingsTableAAA(rankings: List<GlobalManagerRanking>) {
    DashboardSectionCard(modifier = Modifier.fillMaxWidth()) {
        rankings.take(10).forEach { r ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${r.rank}", modifier = Modifier.width(20.dp), style = AFMTextStyles.textXXS, color = FameColors.TrophyGold)
                Text(
                    r.managerName.uppercase(), 
                    modifier = Modifier.weight(1f), 
                    style = AFMTextStyles.textXXS, 
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text("${String.format(Locale.US, "%.1f", r.winPercentage)}%", style = AFMTextStyles.textXXS, color = Color.White)
            }
        }
    }
}

@Composable
fun InternationalFixtureRowAAA(fixture: InternationalFixtureUiModel) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(fixture.homeTeam.uppercase(), modifier = Modifier.weight(1f), textAlign = TextAlign.End, color = Color.White)
        Text(
            if (fixture.isCompleted) "${fixture.homeScore}-${fixture.awayScore}" else "VS",
            modifier = Modifier.padding(horizontal = 8.dp),
            color = FameColors.TrophyGold,
            fontWeight = FontWeight.Black
        )
        Text(fixture.awayTeam.uppercase(), modifier = Modifier.weight(1f), color = Color.White)
    }
}

@Composable
fun WorldStatisticsCenterDenseAAA(stats: WorldStatsUiModel) {
    Column {
        Text("TOP SCORERS", style = AFMTextStyles.textXS, color = FameColors.TrophyGold)
        stats.topScorers.take(3).forEach { player ->
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(player.name, modifier = Modifier.weight(1f), color = Color.White)
                Text("${player.value}", color = FameColors.GrowthGreen)
            }
        }
    }
}

@Composable
fun RankingUpdateFooterAAA(lastUpdated: Long) {
    Text(
        text = "LAST SYNC: ${if (lastUpdated > 0) java.text.SimpleDateFormat("dd MMM yyyy", Locale.US).format(java.util.Date(lastUpdated)) else "PENDING"}",
        style = AFMTextStyles.textXS,
        color = FameColors.MutedParchment,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center
    )
}
