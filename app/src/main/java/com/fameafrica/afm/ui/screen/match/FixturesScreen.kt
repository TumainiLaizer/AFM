package com.fameafrica.afm.ui.screen.match

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fameafrica.afm.data.database.entities.FixturesEntity
import com.fameafrica.afm.data.database.entities.FixturesResultsEntity
import com.fameafrica.afm.data.repository.FixturesRepository
import com.fameafrica.afm.ui.components.*
import com.fameafrica.afm.ui.components.common.*
import com.fameafrica.afm.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MatchesTabScreen(
    onMatchClick: (Int) -> Unit,
    fixturesRepository: FixturesRepository,
    viewModel: FixturesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    MatchesTabScreenContent(
        uiState = uiState,
        onMatchClick = onMatchClick,
        fixturesRepository = fixturesRepository
    )
}

@Composable
fun MatchesTabScreenContent(
    uiState: FixturesUiState,
    fixturesRepository: FixturesRepository?,
    onMatchClick: (Int) -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("FIXTURES", "RESULTS", "STATS")

    AFM2026Theme {
        StadiumBackground {
            Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
                
                // IdentityBanner removed

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(2.dp))
                        .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(2.dp))
                        .padding(2.dp)
                ) {
                    tabs.forEachIndexed { index, label ->
                        val isSelected = selectedTabIndex == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(1.dp))
                                .background(if (isSelected) FameColors.TrophyGold else Color.Transparent)
                                .clickable { selectedTabIndex = index }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                label,
                                style = AFMTextStyles.textXS,
                                color = if (isSelected) Color.Black else FameColors.MutedParchment,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    when (selectedTabIndex) {
                        0 -> FixturesSchedulePage(uiState, fixturesRepository, onMatchClick)
                        1 -> ResultsHistoryPage(uiState.results, uiState.userTeamName, onMatchClick)
                        2 -> MatchStatsPage(uiState)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FixturesSchedulePage(
    uiState: FixturesUiState,
    fixturesRepository: FixturesRepository?,
    onMatchClick: (Int) -> Unit
) {
    var selectedFixtureForDetails by remember { mutableStateOf<FixturesEntity?>(null) }
    val sheetState = rememberModalBottomSheetState()

    val fixtureDates = remember(uiState.upcomingFixtures) {
        uiState.upcomingFixtures.map { it.matchDate.substringBefore(" ") }.toSet()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        CalendarStrip(uiState.currentDate, fixtureDates)
        
        if (uiState.upcomingFixtures.isEmpty()) {
            EmptyMatchesState("No upcoming fixtures scheduled.")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(1.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                val grouped = uiState.upcomingFixtures.groupBy { it.matchDate.substringBefore(" ") }
                grouped.forEach { (date, matches) ->
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color.Black.copy(alpha = 0.4f),
                            border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.05f))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = formatDateHeader(date).uppercase(Locale.ROOT),
                                    style = AFMTextStyles.textXS,
                                    color = FameColors.TrophyGold,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                                val firstMatch = matches.firstOrNull()
                                if (firstMatch != null) {
                                    val compType = (firstMatch.league ?: firstMatch.cupName ?: firstMatch.matchType).uppercase(Locale.ROOT)
                                    Text(
                                        text = compType,
                                        style = AFMTextStyles.textXS,
                                        color = FameColors.MutedParchment,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                    items(matches, key = { it.id }) { fixture ->
                        FixtureRow(
                            fixture = fixture,
                            fixturesRepository = fixturesRepository,
                            userTeam = uiState.userTeamName,
                            currentDate = uiState.currentDate,
                            onClick = { 
                                val matchDateOnly = fixture.matchDate.substringBefore(" ")
                                if (matchDateOnly == uiState.currentDate) {
                                    onMatchClick(fixture.id)
                                } else {
                                    selectedFixtureForDetails = fixture
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (selectedFixtureForDetails != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedFixtureForDetails = null },
            sheetState = sheetState,
            containerColor = Color(0xFF011018),
            dragHandle = { BottomSheetDefaults.DragHandle(color = FameColors.TrophyGold) }
        ) {
            FixtureDetailsBottomSheetContent(selectedFixtureForDetails!!)
        }
    }
}

@Composable
fun CalendarStrip(currentDate: String, fixtureDates: Set<String>) {
    val dates = remember {
        val list = mutableListOf<Date>()
        val calendar = Calendar.getInstance()
        calendar.set(2025, Calendar.JUNE, 1) // Start of season
        repeat(365) {
            list.add(calendar.time)
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        list
    }

    val selectedDateIndex = remember(currentDate) {
        val index = dates.indexOfFirst { 
            SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(it) == currentDate 
        }
        if (index == -1) 0 else index
    }

    val listState = rememberLazyListState()

    LaunchedEffect(selectedDateIndex) {
        if (selectedDateIndex >= 0) {
            listState.animateScrollToItem((selectedDateIndex - 3).coerceAtLeast(0))
        }
    }
    
    LazyRow(
        state = listState,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.4f))
            .padding(vertical = 4.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        itemsIndexed(dates) { index, date ->
            val df = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT)
            val dateStr = df.format(date)
            val isToday = dateStr == currentDate
            val hasFixture = fixtureDates.contains(dateStr)

            val dayName = SimpleDateFormat("EEE", Locale.ROOT).format(date).uppercase(Locale.ROOT)
            val dayNum = SimpleDateFormat("dd", Locale.ROOT).format(date)
            val monthName = SimpleDateFormat("MMM", Locale.ROOT).format(date).uppercase(Locale.ROOT)

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(42.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (isToday) FameColors.TrophyGold else Color.White.copy(0.05f))
                    .border(0.5.dp, if (isToday) FameColors.TrophyGold else Color.White.copy(0.1f), RoundedCornerShape(2.dp))
                    .padding(vertical = 4.dp)
            ) {
                Text(monthName, style = AFMTextStyles.textXS.copy(fontSize = 7.sp), fontWeight = FontWeight.Bold, color = if (isToday) Color.Black else FameColors.TrophyGold)
                Text(dayName, style = AFMTextStyles.textXS.copy(fontSize = 9.sp), fontWeight = FontWeight.Bold, color = if (isToday) Color.Black else FameColors.MutedParchment)
                Text(dayNum, style = AFMTextStyles.textMD, color = if (isToday) Color.Black else Color.White, fontWeight = FontWeight.Black)
                
                if (hasFixture) {
                    Box(
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(if (isToday) Color.Black else FameColors.GrowthGreen)
                    )
                }
            }
        }
    }
}

@Composable
fun FixtureRow(
    fixture: FixturesEntity,
    fixturesRepository: FixturesRepository?,
    userTeam: String,
    currentDate: String,
    onClick: () -> Unit
) {
    val isHome = fixture.homeTeam == userTeam
    val opponent = if (isHome) fixture.awayTeam else fixture.homeTeam
    val typeColor = getMatchTypeColor(fixture.matchType)

    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        color = Color.Black.copy(alpha = 0.5f),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(45.dp)) {
                Text(
                    text = fixture.matchDate.substringAfter(" "),
                    style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Black),
                    color = Color.White
                )
                Text(
                    text = if (isHome) "HOME" else "AWAY",
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isHome) FameColors.TransferBlue else FameColors.TrophyGold
                )
            }

            Spacer(Modifier.width(12.dp))

            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                TeamLogo(teamName = fixture.homeTeam, modifier = Modifier.size(24.dp))
                Text(
                    " VS ",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White.copy(0.3f),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                TeamLogo(teamName = fixture.awayTeam, modifier = Modifier.size(24.dp))
                
                Spacer(Modifier.width(12.dp))
                
                Text(
                    text = opponent.uppercase(),
                    style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Black),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = fixture.stadium.uppercase(),
                    fontSize = 8.sp,
                    color = Color.White.copy(alpha = 0.4f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(80.dp)
                )
                Box(
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .height(3.dp)
                        .width(20.dp)
                        .background(typeColor, RoundedCornerShape(1.dp))
                )
            }
        }
    }
}

@Composable
fun ResultsHistoryPage(
    results: List<FixturesResultsEntity>,
    userTeam: String,
    onMatchClick: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Surface(
            color = Color.Black.copy(alpha = 0.6f),
            modifier = Modifier.fillMaxWidth().height(40.dp),
            border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f))
        ) {
             Box(contentAlignment = Alignment.Center) {
                Text("MATCH RESULTS", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp, letterSpacing = 2.sp)
             }
        }

        if (results.isEmpty()) {
            EmptyMatchesState("No match results recorded yet.")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(1.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(results, key = { it.fixtureId }) { result ->
                    RefinedResultRow(result, userTeam, onMatchClick)
                }
            }
        }
    }
}

@Composable
fun RefinedResultRow(
    result: FixturesResultsEntity,
    userTeam: String,
    onClick: (Int) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick(result.fixtureId) },
        color = Color.Black.copy(alpha = 0.4f),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Home Team
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End) {
                Text(
                    text = result.homeTeam.uppercase(),
                    style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                TeamLogo(teamName = result.homeTeam, modifier = Modifier.size(20.dp))
            }

            // Score (Centered)
            Surface(
                color = Color.Black.copy(alpha = 0.8f),
                shape = RoundedCornerShape(2.dp),
                modifier = Modifier.padding(horizontal = 16.dp),
                border = BorderStroke(1.dp, FameColors.TrophyGold.copy(alpha = 0.3f))
            ) {
                Text(
                    text = " ${result.homeScore} - ${result.awayScore} ",
                    style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Black),
                    color = FameColors.TrophyGold,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            // Away Team
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                TeamLogo(teamName = result.awayTeam, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = result.awayTeam.uppercase(),
                    style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun FixtureDetailsBottomSheetContent(fixture: FixturesEntity) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "MATCH PREVIEW",
            style = AFMTextStyles.textXS,
            color = FameColors.TrophyGold,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                TeamLogo(teamName = fixture.homeTeam, modifier = Modifier.size(72.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text(fixture.homeTeam.uppercase(Locale.ROOT), style = AFMTextStyles.textSM, fontWeight = FontWeight.Black, color = Color.White, textAlign = TextAlign.Center)
            }
            
            Text("VS", style = AFMTextStyles.textLG.copy(fontSize = 24.sp), fontWeight = FontWeight.Black, color = Color.White.copy(alpha = 0.2f))
            
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                TeamLogo(teamName = fixture.awayTeam, modifier = Modifier.size(72.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text(fixture.awayTeam.uppercase(Locale.ROOT), style = AFMTextStyles.textSM, fontWeight = FontWeight.Black, color = Color.White, textAlign = TextAlign.Center)
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White.copy(alpha = 0.05f),
            shape = RoundedCornerShape(4.dp),
            border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DetailItem(Icons.Default.LocationOn, "VENUE", fixture.stadium)
                DetailItem(Icons.Default.Info, "COMPETITION", fixture.league ?: fixture.cupName ?: fixture.matchType)
                DetailItem(Icons.AutoMirrored.Filled.EventNote, "ROUND", fixture.round)
            }
        }
    }
}

@Composable
fun DetailItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = FameColors.TrophyGold, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, style = AFMTextStyles.textXS.copy(fontSize = 8.sp), color = FameColors.MutedParchment)
            Text(value.uppercase(Locale.ROOT), style = AFMTextStyles.textMD, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun MatchStatsPage(uiState: FixturesUiState) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("SEASON OVERVIEW", style = AFMTextStyles.textXS, color = FameColors.TrophyGold, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatMetricItem("MATCHES", uiState.results.size.toString())
                StatMetricItem("GOALS", uiState.results.sumOf { it.homeScore + it.awayScore }.toString())
                StatMetricItem("CARDS", uiState.results.sumOf { it.totalYellowCards + it.totalRedCards }.toString())
            }
        }
    }
}

@Composable
fun StatMetricItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value.uppercase(Locale.ROOT), style = AFMTextStyles.textLG.copy(fontSize = 24.sp), fontWeight = FontWeight.Black, color = Color.White)
        Text(label.uppercase(Locale.ROOT), style = AFMTextStyles.textXS, color = FameColors.MutedParchment, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun EmptyMatchesState(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.AutoMirrored.Filled.EventNote, null, modifier = Modifier.size(64.dp), tint = Color.White.copy(alpha = 0.05f))
            Spacer(modifier = Modifier.height(16.dp))
            Text(message.uppercase(Locale.ROOT), color = FameColors.MutedParchment, style = AFMTextStyles.textXS, fontWeight = FontWeight.Bold)
        }
    }
}

private fun getMatchTypeColor(type: String): Color = when (type) {
    "League" -> FameColors.PitchGreen
    "Cup" -> FameColors.ChampionsGold
    "Friendly" -> FameColors.WarmIvory
    "International" -> FameColors.AfricanLegendEmerald
    "Preseason Tour" -> FameColors.AfroSunOrange
    "Playoff" -> Color.Magenta
    else -> Color.White
}

private fun formatDateHeader(dateStr: String): String {
    return try {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).parse(dateStr)
        SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.ROOT).format(date!!)
    } catch (_: Exception) {
        dateStr
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun MatchesTabScreenPreview() {
    AFM2026Theme {
        MatchesTabScreenContent(
            uiState = FixturesUiState(userTeamName = "Young Africans"),
            onMatchClick = {},
            fixturesRepository = null
        )
    }
}
