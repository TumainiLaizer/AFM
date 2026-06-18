package com.fameafrica.afm.ui.screen.cup

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fameafrica.afm.data.database.entities.CupBracketsEntity
import com.fameafrica.afm.data.database.entities.CupGroupStandingsEntity
import com.fameafrica.afm.ui.components.common.*
import com.fameafrica.afm.ui.screen.league.*
import com.fameafrica.afm.ui.theme.*
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun CupFixturesPage(uiState: CupDrawUiState) {
    if (uiState.fixtures.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.EventBusy, null, tint = FameColors.MutedParchment, modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(16.dp))
                Text("No fixtures scheduled yet", color = FameColors.MutedParchment)
                Text("Check back after the draw", style = AFM2026Typography.labelSmall, color = FameColors.ChampionsGold)
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(uiState.fixtures) { fixture ->
                FixtureItem(fixture)
            }
        }
    }
}

@Composable
fun FixtureItem(fixture: FixtureUiModel) {
    GlassPanel {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = fixture.homeTeam.uppercase(),
                style = AFM2026Typography.bodyMedium,
                color = Color.White,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(80.dp)
            ) {
                if (fixture.isCompleted) {
                    Text(
                        text = "${fixture.homeScore ?: 0} - ${fixture.awayScore ?: 0}",
                        style = AFMTextStyles.statValue,
                        color = FameColors.ChampionsGold,
                        fontWeight = FontWeight.Black
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .background(FameColors.HeaderDark, RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("VS", style = AFM2026Typography.labelSmall, color = FameColors.MutedParchment)
                    }
                }
                Text(
                    text = fixture.date.substringAfter(" "),
                    style = AFM2026Typography.labelSmall,
                    color = FameColors.MutedParchment,
                    fontSize = 9.sp
                )
            }

            Text(
                text = fixture.awayTeam.uppercase(),
                style = AFM2026Typography.bodyMedium,
                color = Color.White,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun CupHistoryPage(history: List<CupHistoryUiModel>) {
    if (history.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No history recorded for this competition", color = FameColors.MutedParchment)
        }
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(history) { item ->
            GlassPanel {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.season, style = AFM2026Typography.labelLarge, color = FameColors.ChampionsGold, fontWeight = FontWeight.Black)
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TeamLogo(item.winner, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(item.winner.uppercase(), style = AFM2026Typography.bodyLarge, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Text("RUNNER-UP: ${item.runnerUp}", style = AFM2026Typography.labelSmall, color = FameColors.MutedParchment)
                    }
                    Icon(Icons.Default.EmojiEvents, null, tint = FameColors.ChampionsGold, modifier = Modifier.size(40.dp))
                }
            }
        }
    }
}

@Composable
fun CupStatsPage(stats: CupStatsUiModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(), 
        contentPadding = PaddingValues(16.dp), 
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            GlassPanel {
                Text("TOURNAMENT OVERVIEW", style = AFMTextStyles.tableHeader, color = FameColors.ChampionsGold)
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    CupStatItem("Total Goals", stats.totalGoals.toString())
                    CupStatItem("Avg Goals", String.format(Locale.ROOT, "%.2f", stats.averageGoals))
                    CupStatItem("Avg Attend", stats.averageAttendance.toString())
                }
            }
        }
        
        item { ExpandablePlayerStats("TOP SCORERS", stats.topScorers) }
        item { ExpandablePlayerStats("ASSISTS", stats.topAssisters) }
        item { ExpandablePlayerStats("GOALS + ASSISTS", stats.topGoalsAssists) }
        item { ExpandablePlayerStats("CLEAN SHEETS", stats.topGKs) }
    }
}

@Composable
fun ExpandablePlayerStats(title: String, stats: List<CupPlayerStatUiModel>) {
    var expanded by remember { mutableStateOf(false) }
    val canExpand = stats.size > 2
    
    GlassPanel(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = canExpand) { expanded = !expanded }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = AFMTextStyles.tableHeader, color = FameColors.ChampionsGold)
                if (canExpand) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (stats.isEmpty()) {
                Text("No data available yet", style = AFM2026Typography.bodySmall, color = FameColors.MutedParchment, modifier = Modifier.padding(8.dp))
            } else {
                stats.take(2).forEachIndexed { index, stat ->
                    StatPlayerRow(stat.rank, stat.playerName, stat.teamName, stat.value.toString(), stat.label, stat.nationality)
                    if (index < 1 && index < stats.size - 1) {
                        HorizontalDivider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
                
                AnimatedVisibility(
                    visible = expanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column {
                        if (stats.size > 2) {
                            HorizontalDivider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(vertical = 4.dp))
                            stats.drop(2).forEachIndexed { index, stat ->
                                StatPlayerRow(stat.rank, stat.playerName, stat.teamName, stat.value.toString(), stat.label, stat.nationality)
                                if (index < stats.size - 3) {
                                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(vertical = 4.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatPlayerRow(rank: Int, name: String, team: String, value: String, label: String, nationality: String? = null) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp, horizontal = 8.dp)) {
        Text(rank.toString(), style = AFMTextStyles.statValue, color = FameColors.ChampionsGold, modifier = Modifier.width(32.dp))
        TeamLogo(teamName = team, modifier = Modifier.size(32.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = name.uppercase(Locale.ROOT), 
                    style = AFM2026Typography.titleSmall, 
                    color = Color.White, 
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (nationality != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    NationalityFlag(nationality = nationality, modifier = Modifier.size(16.dp, 10.dp))
                }
            }
            Text(team.uppercase(Locale.ROOT), style = AFM2026Typography.labelSmall, color = FameColors.MutedParchment)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(value, style = AFMTextStyles.statValue, color = Color.White)
            Text(label, style = AFM2026Typography.labelSmall, color = FameColors.ChampionsGold, fontSize = 8.sp)
        }
    }
}

@Composable
fun NationalityFlag(nationality: String, modifier: Modifier = Modifier) {
    AsyncImage(
        model = "file:///android_asset/flags/$nationality.webp",
        contentDescription = nationality,
        modifier = modifier
            .clip(RoundedCornerShape(2.dp))
            .border(0.5.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(2.dp)),
        contentScale = ContentScale.Crop
    )
}

@Composable
fun CupStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = AFMTextStyles.statValue, color = Color.White)
        Text(label, style = AFM2026Typography.labelSmall, color = FameColors.MutedParchment)
    }
}

enum class TableConfig { SHORT, FULL }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CupGroupsView(groupStandings: Map<String, List<CupGroupStandingsEntity>>?) {
    if (groupStandings.isNullOrEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No group standings available", color = FameColors.MutedParchment)
        }
        return
    }
    
    val groupNames = groupStandings.keys.toList()
    val pagerState = rememberPagerState(pageCount = { groupNames.size })
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        if (groupNames.size > 1) {
            ScrollableTabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = Color.Transparent,
                contentColor = FameColors.ChampionsGold,
                edgePadding = 16.dp,
                indicator = { tabPositions ->
                    if (pagerState.currentPage < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                            color = FameColors.ChampionsGold,
                            height = 2.dp
                        )
                    }
                },
                divider = {}
            ) {
                groupNames.forEachIndexed { index, groupName ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = {
                            Text(
                                text = groupName,
                                style = AFM2026Typography.labelLarge,
                                fontWeight = if (pagerState.currentPage == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            beyondViewportPageCount = 1
        ) { pageIndex ->
            val groupName = groupNames[pageIndex]
            val teams = groupStandings[groupName] ?: emptyList()
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    GroupTable(groupName = groupName, teams = teams)
                }
            }
        }
    }
}

@Composable
fun TableStatCell(value: String, width: androidx.compose.ui.unit.Dp, color: Color = Color.White, fontWeight: FontWeight = FontWeight.Normal) {
    Text(
        text = value,
        style = AFMTextStyles.tableCell,
        color = color,
        fontWeight = fontWeight,
        modifier = Modifier.width(width),
        textAlign = TextAlign.Center
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupTable(groupName: String, teams: List<CupGroupStandingsEntity>) {
    var currentConfig by remember { mutableStateOf(TableConfig.SHORT) }

    GlassPanel {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = groupName.uppercase(),
                    style = AFM2026Typography.titleMedium,
                    color = FameColors.ChampionsGold,
                    fontWeight = FontWeight.Bold
                )

                SingleChoiceSegmentedButtonRow {
                    listOf(TableConfig.SHORT, TableConfig.FULL).forEachIndexed { index, config ->
                        SegmentedButton(
                            selected = currentConfig == config,
                            onClick = { currentConfig = config },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = 2),
                            colors = SegmentedButtonDefaults.colors(
                                activeContainerColor = FameColors.ChampionsGold.copy(alpha = 0.2f),
                                activeContentColor = FameColors.ChampionsGold,
                                inactiveContainerColor = Color.Transparent,
                                inactiveContentColor = FameColors.MutedParchment
                            )
                        ) {
                            Text(if (config == TableConfig.SHORT) "SHORT" else "FULL", style = AFM2026Typography.labelSmall, fontSize = 8.sp)
                        }
                    }
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("#", style = AFMTextStyles.tableHeader, color = FameColors.MutedParchment, modifier = Modifier.width(24.dp))
                Text("TEAM", style = AFMTextStyles.tableHeader, color = FameColors.MutedParchment, modifier = Modifier.weight(1f))
                
                if (currentConfig == TableConfig.FULL) {
                    Text("P", style = AFMTextStyles.tableHeader, color = FameColors.MutedParchment, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
                    Text("W", style = AFMTextStyles.tableHeader, color = FameColors.MutedParchment, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
                    Text("D", style = AFMTextStyles.tableHeader, color = FameColors.MutedParchment, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
                    Text("L", style = AFMTextStyles.tableHeader, color = FameColors.MutedParchment, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
                    Text("GD", style = AFMTextStyles.tableHeader, color = FameColors.MutedParchment, modifier = Modifier.width(32.dp), textAlign = TextAlign.Center)
                    Text("PTS", style = AFMTextStyles.tableHeader, color = FameColors.ChampionsGold, modifier = Modifier.width(32.dp), textAlign = TextAlign.Center)
                } else {
                    Text("P", style = AFMTextStyles.tableHeader, color = FameColors.MutedParchment, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
                    Text("GD", style = AFMTextStyles.tableHeader, color = FameColors.MutedParchment, modifier = Modifier.width(32.dp), textAlign = TextAlign.Center)
                    Text("PTS", style = AFMTextStyles.tableHeader, color = FameColors.ChampionsGold, modifier = Modifier.width(32.dp), textAlign = TextAlign.Center)
                }
            }
            
            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
            
            teams.forEachIndexed { index, team ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = (index + 1).toString(),
                        style = AFMTextStyles.tableCell,
                        color = if (index < 2) FameColors.PitchGreen else Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(24.dp)
                    )
                    
                    TeamLogo(teamName = team.teamName, modifier = Modifier.size(20.dp))
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = team.teamName,
                        style = AFM2026Typography.bodyMedium,
                        color = Color.White,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (currentConfig == TableConfig.FULL) {
                        TableStatCell(team.matchesPlayed.toString(), 24.dp)
                        TableStatCell(team.wins.toString(), 24.dp)
                        TableStatCell(team.draws.toString(), 24.dp)
                        TableStatCell(team.losses.toString(), 24.dp)
                        TableStatCell(team.goalDifference.toString(), 32.dp, color = if (team.goalDifference > 0) FameColors.PitchGreen else if (team.goalDifference < 0) FameColors.KenteRed else Color.White)
                        TableStatCell(team.points.toString(), 32.dp, color = FameColors.ChampionsGold, fontWeight = FontWeight.Black)
                    } else {
                        TableStatCell(team.matchesPlayed.toString(), 24.dp)
                        TableStatCell(team.goalDifference.toString(), 32.dp, color = if (team.goalDifference > 0) FameColors.PitchGreen else if (team.goalDifference < 0) FameColors.KenteRed else Color.White)
                        TableStatCell(team.points.toString(), 32.dp, color = FameColors.ChampionsGold, fontWeight = FontWeight.Black)
                    }
                }
                if (index < teams.size - 1) {
                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                }
            }
        }
    }
}

@Composable
fun KnockoutView(uiState: CupDrawUiState) {
    var viewMode by remember { mutableStateOf(ViewMode.TREE) }
    
    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState.matches.isEmpty() && uiState.bracketsByRound == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.AccountTree, null, tint = FameColors.MutedParchment, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Knockout stage hasn't started yet.",
                        style = AFM2026Typography.bodyLarge,
                        color = FameColors.MutedParchment
                    )
                }
            }
        } else {
            Crossfade(targetState = viewMode, label = "ViewModeFade") { mode ->
                when (mode) {
                    ViewMode.LIST -> CupListView(uiState)
                    ViewMode.TREE -> CupTreeView(uiState)
                }
            }
            
            FloatingActionButton(
                onClick = { viewMode = if (viewMode == ViewMode.LIST) ViewMode.TREE else ViewMode.LIST },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(16.dp),
                containerColor = FameColors.PitchGreen,
                contentColor = Color.White,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = if (viewMode == ViewMode.LIST) Icons.Default.AccountTree else Icons.AutoMirrored.Filled.List,
                    contentDescription = "Switch View"
                )
            }
        }
    }
}

@Composable
fun CupListView(uiState: CupDrawUiState) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        uiState.bracketsByRound?.forEach { (roundNum, brackets) ->
            item {
                val roundName = brackets.firstOrNull()?.round ?: "Round $roundNum"
                Text(
                    text = roundName.replace("_", " ").uppercase(),
                    style = AFMTextStyles.tableHeader,
                    color = FameColors.ChampionsGold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            items(brackets, key = { it.id }) { bracket ->
                BracketMatchItem(bracket = bracket)
            }
        }
    }
}

@Composable
fun CupTreeView(uiState: CupDrawUiState) {
    val scrollState = rememberScrollState()
    val rounds = uiState.bracketsByRound?.values?.toList() ?: emptyList()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .horizontalScroll(scrollState)
    ) {
        if (rounds.isNotEmpty()) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val columnWidth = 260.dp.toPx()
                val spacing = 48.dp.toPx()
                val topPadding = 60.dp.toPx()
                val totalHeight = size.height - topPadding
                
                for (i in 0 until rounds.size - 1) {
                    val currentRound = rounds[i]
                    val nextRound = rounds[i + 1]
                    val startX = (i * (columnWidth + spacing)) + columnWidth
                    val endX = startX + spacing
                    
                    currentRound.forEachIndexed { index, _ ->
                        val nextMatchIndex = index / 2
                        if (nextMatchIndex < nextRound.size) {
                            val startY = topPadding + (index + 0.5f) * (totalHeight / currentRound.size)
                            val endY = topPadding + (nextMatchIndex + 0.5f) * (totalHeight / nextRound.size)
                            val path = Path().apply {
                                moveTo(startX, startY)
                                cubicTo(startX + spacing / 2, startY, startX + spacing / 2, endY, endX, endY)
                            }
                            drawPath(path = path, color = Color.White.copy(alpha = 0.1f), style = Stroke(width = 2.dp.toPx()))
                        }
                    }
                }
            }
        }
        
        Row(
            modifier = Modifier.padding(16.dp).fillMaxHeight(),
            horizontalArrangement = Arrangement.spacedBy(48.dp)
        ) {
            uiState.bracketsByRound?.forEach { (roundNum, brackets) ->
                val roundName = brackets.firstOrNull()?.round ?: "Round $roundNum"
                Column(
                    modifier = Modifier.width(260.dp).fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = roundName.replace("_", " ").uppercase(),
                        style = AFMTextStyles.tableHeader,
                        color = FameColors.ChampionsGold,
                        modifier = Modifier.height(44.dp).padding(top = 8.dp)
                    )
                    
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.SpaceAround
                    ) {
                        brackets.forEach { bracket ->
                            BracketMatchItem(bracket = bracket)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BracketMatchItem(bracket: CupBracketsEntity) {
    GlassPanel(
        modifier = Modifier.width(260.dp).padding(vertical = 4.dp),
        cornerRadius = 12
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            TeamScoreRow(
                teamName = bracket.teamName ?: "TBD",
                score = bracket.homeScore?.toString() ?: "-",
                isWinner = bracket.winner == bracket.teamName && bracket.winner != null
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.White.copy(alpha = 0.1f))
            TeamScoreRow(
                teamName = bracket.opponentName ?: "TBD",
                score = bracket.awayScore?.toString() ?: "-",
                isWinner = bracket.winner == bracket.opponentName && bracket.winner != null
            )
            
            if (bracket.penaltyScore != null) {
                Text(
                    text = "(${bracket.penaltyScore} PENS)",
                    style = AFM2026Typography.labelSmall,
                    color = FameColors.ChampionsGold,
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun TeamScoreRow(teamName: String, score: String, isWinner: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            TeamLogo(teamName = teamName, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = teamName,
                style = AFM2026Typography.bodyMedium,
                fontWeight = if (isWinner) FontWeight.Bold else FontWeight.Medium,
                color = if (isWinner) FameColors.PitchGreen else Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = score,
            style = AFMTextStyles.statValue,
            color = if (isWinner) FameColors.ChampionsGold else if (score == "-") FameColors.MutedParchment else Color.White
        )
    }
}

enum class ViewMode { LIST, TREE }
