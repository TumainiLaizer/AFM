package com.fameafrica.afm.ui.screen.match

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fameafrica.afm.R
import com.fameafrica.afm.ui.components.common.TeamLogo
import com.fameafrica.afm.ui.shared.FameCard
import com.fameafrica.afm.ui.theme.AFMTextStyles
import com.fameafrica.afm.ui.theme.FameColors
import com.fameafrica.afm.utils.extensions.toTitleCase

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.fameafrica.afm.ui.components.common.SidebarSectionTitle
import androidx.compose.ui.tooling.preview.Preview
import com.fameafrica.afm.ui.theme.AFM2026Theme
@Composable
fun PreMatchBriefingScreen(
    uiState: MatchUiState,
    onProceed: () -> Unit
) {
    val match = uiState.matchInfo ?: return
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        // 1. Cinematic Header
        Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
            Image(
                painter = painterResource(id = R.drawable.stadium),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.4f
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, FameColors.DeepNavyBlack)
                        )
                    )
            )
            
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    text = match.competition.uppercase(),
                    style = AFMTextStyles.textXS.copy(fontWeight = FontWeight.Black, letterSpacing = 2.sp),
                    color = FameColors.TrophyGold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TeamColumn(match.homeTeam)
                    Text("VS", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.White, modifier = Modifier.padding(horizontal = 24.dp))
                    TeamColumn(match.awayTeam)
                }
            }
        }

        // Tabs
        @OptIn(ExperimentalMaterial3Api::class)
        SecondaryTabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier,
            containerColor = Color.Black,
            contentColor = FameColors.TrophyGold,
            indicator = {
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(selectedTab),
                    color = FameColors.TrophyGold
                )
            },
            divider = { HorizontalDivider() }
        ) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                Text("OVERVIEW", modifier = Modifier.padding(16.dp), style = AFMTextStyles.textXS.copy(fontWeight = FontWeight.Bold))
            }
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                Text("ANALYSIS", modifier = Modifier.padding(16.dp), style = AFMTextStyles.textXS.copy(fontWeight = FontWeight.Bold))
            }
        }

        // 2. Details Content
        LazyColumn(
            modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (selectedTab == 0) {
                item { Spacer(modifier = Modifier.height(12.dp)) }
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        InfoCard(label = "VENUE", value = match.stadium, icon = Icons.Default.Stadium, modifier = Modifier.weight(1f))
                        InfoCard(label = "WEATHER", value = match.weather, icon = Icons.Default.WbSunny, modifier = Modifier.weight(1f))
                    }
                }

                item {
                    SidebarSectionTitle("RECENT FORM & H2H")
                    FameCard(containerColor = Color.White.copy(alpha = 0.05f)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                FormColumn(match.homeTeam, match.homeForm)
                                H2HSummary(match.h2hHomeWins, match.h2hDraws, match.h2hAwayWins)
                                FormColumn(match.awayTeam, match.awayForm)
                            }
                        }
                    }
                }
            } else {
                item { Spacer(modifier = Modifier.height(12.dp)) }
                uiState.preMatchReport?.let { report ->
                    item {
                        SidebarSectionTitle("WIN PROBABILITY (PYTHON AI)")
                        FameCard(containerColor = Color.Black.copy(alpha = 0.4f)) {
                            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                                ProbabilityIndicator("WIN", report.winProbability, FameColors.GrowthGreen)
                                ProbabilityIndicator("DRAW", report.drawProbability, FameColors.TrophyGold)
                                ProbabilityIndicator("LOSS", report.lossProbability, FameColors.AlertRed)
                            }
                        }
                    }

                    item {
                        SidebarSectionTitle("DATA ANALYST REPORT")
                        FameCard(containerColor = Color.White.copy(alpha = 0.05f)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(report.analystTitle, style = AFMTextStyles.textSM.copy(fontWeight = FontWeight.Bold), color = FameColors.TrophyGold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(report.analystAdvice, style = AFMTextStyles.textXS, color = Color.White)
                            }
                        }
                    }

                    item {
                        SidebarSectionTitle("CHIEF SCOUT BRIEF")
                        FameCard(containerColor = Color.White.copy(alpha = 0.05f)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(report.scoutTitle, style = AFMTextStyles.textSM.copy(fontWeight = FontWeight.Bold), color = FameColors.TrophyGold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(report.scoutAdvice, style = AFMTextStyles.textXS, color = Color.White)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("KEY MATCHUP", style = AFMTextStyles.textXXS.copy(fontWeight = FontWeight.Bold), color = FameColors.MutedParchment)
                                Text(report.keyMatchup, style = AFMTextStyles.textXS.copy(fontWeight = FontWeight.Bold), color = FameColors.GrowthGreen)
                            }
                        }
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }

        // 3. Action Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.Black,
            border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Button(
                onClick = onProceed,
                modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FameColors.AfroSunOrange),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("KICKOFF", fontWeight = FontWeight.Black, color = Color.Black, fontSize = 18.sp)
            }
        }
    }
}

@Composable
fun TeamColumn(name: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        TeamLogo(name, modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Text(name.toTitleCase(), style = AFMTextStyles.textSM.copy(fontWeight = FontWeight.Black), color = Color.White)
    }
}

@Composable
fun InfoCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    FameCard(modifier = modifier, containerColor = Color.White.copy(alpha = 0.05f)) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = FameColors.TrophyGold, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, style = AFMTextStyles.textXXS.copy(fontWeight = FontWeight.Bold), color = FameColors.MutedParchment)
            Text(value.uppercase(), style = AFMTextStyles.textXS.copy(fontWeight = FontWeight.Black), color = Color.White, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun ProbabilityIndicator(label: String, value: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = AFMTextStyles.textXXS.copy(fontWeight = FontWeight.Bold), color = FameColors.MutedParchment)
        Text("$value%", style = AFMTextStyles.textLG.copy(fontWeight = FontWeight.Black), color = color)
        Box(modifier = Modifier.width(60.dp).height(4.dp).background(color.copy(alpha = 0.2f), RoundedCornerShape(2.dp))) {
            Box(modifier = Modifier.fillMaxWidth(value / 100f).fillMaxHeight().background(color, RoundedCornerShape(2.dp)))
        }
    }
}

@Composable
fun FormColumn(team: String, form: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(team.uppercase(), style = AFMTextStyles.textXXS, color = FameColors.MutedParchment)
        Spacer(modifier = Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            form.takeLast(5).forEach { char ->
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            when (char) {
                                'W' -> FameColors.GrowthGreen
                                'D' -> FameColors.TrophyGold
                                'L' -> FameColors.AlertRed
                                else -> Color.Gray
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(char.toString(), fontSize = 8.sp, fontWeight = FontWeight.Black, color = if (char == 'D') Color.Black else Color.White)
                }
            }
        }
    }
}

@Composable
fun H2HSummary(w: Int, d: Int, l: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("H2H", style = AFMTextStyles.textXXS, color = FameColors.MutedParchment)
        Text("$w - $d - $l", style = AFMTextStyles.textSM, fontWeight = FontWeight.Black, color = Color.White)
    }
}


@Preview(showBackground = true)
@Composable
fun PreMatchBriefingScreenPreview() {
    AFM2026Theme {
        PreMatchBriefingScreen(
            uiState = MatchUiState(
                matchInfo = MatchInfoUiModel(
                    id = 1,
                    homeTeam = "Simba SC",
                    awayTeam = "Young Africans",
                    competition = "NBC Premier League",
                    stadium = "Benjamin Mkapa Stadium",
                    kickoff = "19:00",
                    homeForm = "WWWDL",
                    awayForm = "WWWWW",
                    h2hHomeWins = 12,
                    h2hDraws = 8,
                    h2hAwayWins = 10,
                    weather = "Clear"
                ),
                preMatchReport = PreMatchReportUiModel(
                    analystTitle = "DOMINANCE EXPECTED",
                    analystAdvice = "The home team has been dominant in midfield lately. Look to exploit the wide areas.",
                    scoutTitle = "KEY PLAYER ALERT",
                    scoutAdvice = "Watch out for the opposition striker who has scored 5 in 5.",
                    keyMatchup = "Inonga vs Clement Mzize",
                    winProbability = 45,
                    drawProbability = 25,
                    lossProbability = 30
                )
            ),
            onProceed = {}
        )
    }
}
