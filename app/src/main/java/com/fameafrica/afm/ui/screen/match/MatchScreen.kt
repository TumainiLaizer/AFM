package com.fameafrica.afm.ui.screen.match

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
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
import com.fameafrica.afm.R
import com.fameafrica.afm.data.database.entities.*
import com.fameafrica.afm.data.database.model.match.MatchSpeed
import com.fameafrica.afm.ui.components.common.*
import com.fameafrica.afm.ui.screen.match.components.MatchVisualizerPitch
import com.fameafrica.afm.ui.theme.*
import com.fameafrica.afm.utils.ImmersiveModeManager
import com.fameafrica.afm.utils.ImmersiveModeManager.immersiveRoot

@Composable
fun MatchScreen(
    matchId: Int,
    onBack: () -> Unit,
    onNavigateToTactics: () -> Unit,
    onNavigateToCup: (String) -> Unit = {},
    viewModel: MatchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ImmersiveModeManager.ImmersiveScreen()

    LaunchedEffect(matchId) {
        viewModel.loadMatch(matchId)
    }

    MatchScreenContent(
        uiState = uiState,
        onBack = onBack,
        onNavigateToTactics = onNavigateToTactics,
        onNavigateToCup = onNavigateToCup,
        onProceedToNextState = { viewModel.proceedToNextState() },
        onSkipMatch = { viewModel.skipToResult() },
        onSubmitPressConferenceResponse = { viewModel.submitPressConferenceResponse(it) },
        onSetMatchSpeed = { viewModel.setMatchSpeed(it) },
        onTogglePause = { viewModel.togglePause() }
    )
}

@Composable
fun MatchScreenContent(
    uiState: MatchUiState,
    onBack: () -> Unit,
    onNavigateToTactics: () -> Unit,
    onNavigateToCup: (String) -> Unit,
    onProceedToNextState: () -> Unit,
    onSkipMatch: () -> Unit,
    onSubmitPressConferenceResponse: (String) -> Unit,
    onSetMatchSpeed: (com.fameafrica.afm.data.database.model.match.MatchSpeed) -> Unit,
    onTogglePause: () -> Unit
) {
    BackHandler(enabled = uiState.matchStatus !in listOf(MatchStatus.MATCH_SUMMARY, MatchStatus.FULL_TIME)) { }

    var showStatsOverlay by remember { mutableStateOf(false) }

    StadiumBackground(modifier = Modifier.immersiveRoot()) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (uiState.matchStatus) {
                MatchStatus.PRE_MATCH_ANALYSIS -> PreMatchBriefingScreen(
                    uiState = uiState,
                    onProceed = onProceedToNextState
                )

                MatchStatus.LINEUP_ANNOUNCEMENT -> LineupAnnouncementScreen(
                    uiState = uiState,
                    onProceed = onProceedToNextState
                )

                MatchStatus.FIRST_HALF, MatchStatus.SECOND_HALF, MatchStatus.HALFTIME -> LiveMatchScreen(
                    uiState = uiState,
                    onNavigateToTactics = onNavigateToTactics,
                    onProceedToNextState = onProceedToNextState,
                    onSkipMatch = onSkipMatch,
                    onSetMatchSpeed = onSetMatchSpeed,
                    onTogglePause = onTogglePause,
                    onToggleStats = { showStatsOverlay = !showStatsOverlay }
                )

                MatchStatus.FULL_TIME, MatchStatus.MATCH_SUMMARY -> PostMatchCelebrationScreen(
                    uiState = uiState,
                    onProceed = onProceedToNextState,
                    onBack = onBack,
                    onShowStats = { showStatsOverlay = true }
                )

                else -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = FameColors.TrophyGold)
                        LaunchedEffect(Unit) { 
                            if (uiState.matchStatus != MatchStatus.KICKOFF) onProceedToNextState() 
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = showStatsOverlay,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                MatchStatsOverlay(
                    uiState = uiState,
                    onClose = { showStatsOverlay = false }
                )
            }
        }
    }
}

@Composable
fun LiveMatchScreen(
    uiState: MatchUiState,
    onNavigateToTactics: () -> Unit,
    onProceedToNextState: () -> Unit,
    onSkipMatch: () -> Unit,
    onSetMatchSpeed: (com.fameafrica.afm.data.database.model.match.MatchSpeed) -> Unit,
    onTogglePause: () -> Unit,
    onToggleStats: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        LiveMatchHeader(uiState)

        Box(modifier = Modifier.weight(1f)) {
            MatchVisualizerPitch(
                uiState = uiState.visualizerState,
                modifier = Modifier.fillMaxSize()
            )
            
            Box(modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SpeedControlButtons(uiState.matchSpeed, onSetMatchSpeed)
                    
                    IconButton(
                        onClick = onNavigateToTactics,
                        modifier = Modifier.size(40.dp).background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                    ) {
                        Icon(Icons.Default.Dashboard, null, tint = FameColors.TrophyGold)
                    }
                }
            }
        }

        CommentaryStrip(uiState.commentary.lastOrNull()?.text ?: "MATCH IN PROGRESS...")

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(Color.Black.copy(alpha = 0.8f))
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onToggleStats,
                modifier = Modifier.weight(1f).fillMaxHeight(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("MATCH STATS", fontWeight = FontWeight.Black, color = Color.White, fontSize = 12.sp)
            }
            
            if (uiState.matchStatus == MatchStatus.HALFTIME) {
                Button(
                    onClick = onProceedToNextState,
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    colors = ButtonDefaults.buttonColors(containerColor = FameColors.AfroSunOrange),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("START 2ND HALF", fontWeight = FontWeight.Black, color = Color.Black, fontSize = 12.sp)
                }
            } else {
                Button(
                    onClick = onSkipMatch,
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    colors = ButtonDefaults.buttonColors(containerColor = FameColors.AfroSunOrange),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("SKIP MATCH", fontWeight = FontWeight.Black, color = Color.Black, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun SpeedControlButtons(currentSpeed: com.fameafrica.afm.data.database.model.match.MatchSpeed, onSet: (com.fameafrica.afm.data.database.model.match.MatchSpeed) -> Unit) {
    Surface(color = Color.Black.copy(alpha = 0.6f), shape = RoundedCornerShape(4.dp)) {
        Column(modifier = Modifier.padding(2.dp)) {
            _root_ide_package_.com.fameafrica.afm.data.database.model.match.MatchSpeed.entries.take(3).forEach { speed ->
                val isSelected = currentSpeed == speed
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (isSelected) FameColors.PitchGreen else Color.Transparent)
                        .clickable { onSet(speed) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when(speed) {
                            _root_ide_package_.com.fameafrica.afm.data.database.model.match.MatchSpeed.FAST -> "x4"
                            _root_ide_package_.com.fameafrica.afm.data.database.model.match.MatchSpeed.NORMAL -> "x2"
                            _root_ide_package_.com.fameafrica.afm.data.database.model.match.MatchSpeed.EXTENDED -> "x1"
                            else -> "x1"
                        },
                        color = if (isSelected) Color.Black else Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
fun LiveMatchHeader(uiState: MatchUiState) {
    val match = uiState.matchInfo ?: return
    
    Surface(
        color = FameColors.DeepNavyBlack,
        modifier = Modifier.fillMaxWidth().height(100.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TeamLogo(match.competition, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(match.competition.uppercase(), fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
                Text(match.stadium, fontSize = 9.sp, color = Color.White.copy(alpha = 0.7f))
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    match.homeTeam.uppercase(),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = AFMTextStyles.textLG.copy(fontWeight = FontWeight.Black),
                    color = Color.White
                )

                Surface(
                    color = Color.Yellow,
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.width(80.dp).height(40.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Text(
                            "${match.homeScore} : ${match.awayScore}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.Black
                        )
                        Text(
                            if (uiState.matchStatus == MatchStatus.HALFTIME) "HT" else "${uiState.currentMinute}:24",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }

                Text(
                    match.awayTeam.uppercase(),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = AFMTextStyles.textLG.copy(fontWeight = FontWeight.Black),
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun CommentaryStrip(text: String) {
    Surface(
        color = FameColors.DeepNavyBlack,
        modifier = Modifier.fillMaxWidth().height(40.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text.uppercase(),
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun PostMatchCelebrationScreen(uiState: MatchUiState, onProceed: () -> Unit, onBack: () -> Unit, onShowStats: () -> Unit) {
    val match = uiState.matchInfo ?: return
    val userTeamId = uiState.userTeamId
    
    val userWon = if (match.homeTeamId == userTeamId) match.homeScore > match.awayScore 
                  else if (match.awayTeamId == userTeamId) match.awayScore > match.homeScore
                  else false
                  
    val userLost = if (match.homeTeamId == userTeamId) match.awayScore > match.homeScore
                   else if (match.awayTeamId == userTeamId) match.homeScore > match.awayScore
                   else false

    val resultColor = when {
        userWon -> FameColors.GrowthGreen
        userLost -> FameColors.AlertRed
        else -> Color.Gray
    }

    val resultText = when {
        userWon -> "VICTORY!"
        userLost -> "DEFEAT"
        else -> "DRAW"
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(
            color = resultColor,
            modifier = Modifier.fillMaxWidth().height(220.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = R.drawable.stadium),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alpha = 0.4f
                )
                
                Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f)))))
                
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        resultText,
                        style = AFMTextStyles.textLG.copy(fontSize = 72.sp, fontWeight = FontWeight.Black),
                        color = Color.White
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(match.homeTeam.uppercase(), color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                        Surface(
                            modifier = Modifier.padding(horizontal = 12.dp), 
                            color = Color.White,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                             Text(
                                 "${match.homeScore} FT ${match.awayScore}", 
                                 modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), 
                                 fontWeight = FontWeight.Black,
                                 color = Color.Black,
                                 fontSize = 18.sp
                             )
                        }
                        Text(match.awayTeam.uppercase(), color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                    }
                }
            }
        }

        Column(
            modifier = Modifier.weight(1f).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val summaryText = when {
                userWon -> "AN OUTSTANDING PERFORMANCE BY THE TEAM TO SECURE THE THREE POINTS."
                userLost -> "A DISAPPOINTING RESULT. WE NEED TO ANALYZE WHAT WENT WRONG."
                else -> "A HARD-FOUGHT BATTLE ENDS IN A STALEMATE."
            }
            
            Text(
                summaryText,
                style = AFMTextStyles.textLG.copy(fontSize = 24.sp, fontWeight = FontWeight.Black),
                textAlign = TextAlign.Center,
                color = Color.White,
                lineHeight = 28.sp
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            uiState.manOfTheMatch?.let { motm ->
                Surface(
                    color = Color.Black.copy(alpha = 0.5f), 
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, FameColors.TrophyGold)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), 
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Star, null, tint = FameColors.TrophyGold, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("MAN OF THE MATCH", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = FameColors.TrophyGold)
                            Text(motm.name.uppercase(), color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Key Match Stats Row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                PostMatchStatItem("POSSESSION", "${uiState.stats.homePossession}% - ${uiState.stats.awayPossession}%")
                PostMatchStatItem("SHOTS", "${uiState.stats.homeShots} - ${uiState.stats.awayShots}")
                PostMatchStatItem("ON TARGET", "${uiState.stats.homeShotsOnTarget} - ${uiState.stats.awayShotsOnTarget}")
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RewardCard("GOLD", "+300", Icons.Default.MonetizationOn, FameColors.TrophyGold, managerPhoto = uiState.homeManager?.faceImage, modifier = Modifier.weight(1f))
                RewardCard("XP", "+100", Icons.Default.Token, Color.Magenta, managerPhoto = uiState.homeManager?.faceImage, modifier = Modifier.weight(1f))
                RewardCard("FANS", "+1", Icons.Default.Person, FameColors.TransferBlue, value2 = "83", managerPhoto = uiState.homeManager?.faceImage, modifier = Modifier.weight(1f))
                RewardCard("BOARD", "85", Icons.Default.Business, Color.White, managerPhoto = uiState.homeManager?.faceImage, modifier = Modifier.weight(1f))
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().height(72.dp).padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onShowStats, 
                modifier = Modifier.weight(1f).fillMaxHeight(), 
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray), 
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("STATS", fontWeight = FontWeight.Black, color = Color.White)
            }
            Button(
                onClick = { if (uiState.matchStatus == MatchStatus.MATCH_SUMMARY) onBack() else onProceed() }, 
                modifier = Modifier.weight(2f).fillMaxHeight(), 
                colors = ButtonDefaults.buttonColors(containerColor = FameColors.AfroSunOrange), 
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("CONTINUE", fontWeight = FontWeight.Black, color = Color.Black)
            }
        }
    }
}

@Composable
fun PostMatchStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = AFMTextStyles.textXXS, color = FameColors.MutedParchment, fontWeight = FontWeight.Bold)
        Text(value, style = AFMTextStyles.textSM, color = Color.White, fontWeight = FontWeight.Black)
    }
}

@Composable
fun RewardCard(label: String, value: String, icon: ImageVector, color: Color, value2: String? = null, managerPhoto: String? = null, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.height(100.dp),
        color = Color.Black.copy(alpha = 0.5f),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(4.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceEvenly) {
            Text(label, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = FameColors.MutedParchment)
            if (label == "XP") {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(32.dp))
                    Text("XP", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.White)
                }
            } else if (label == "FANS" || label == "BOARD") {
                 AsyncImage(
                    model = managerPhoto ?: "file:///android_asset/manager_faces/east_africa_1_1.jpg",
                    contentDescription = null,
                    modifier = Modifier.size(32.dp).clip(CircleShape).border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(icon, null, tint = color, modifier = Modifier.size(32.dp))
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(value, fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color.White)
                if (value2 != null) {
                    Surface(modifier = Modifier.padding(start = 4.dp), color = FameColors.AfroSunOrange, shape = RoundedCornerShape(2.dp)) {
                        Text(value2, fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.Black, modifier = Modifier.padding(horizontal = 2.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun MatchStatsOverlay(uiState: MatchUiState, onClose: () -> Unit) {
    val stats = uiState.stats
    val match = uiState.matchInfo ?: return

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black.copy(alpha = 0.95f)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("MATCH STATISTICS", style = AFMTextStyles.textLG, color = Color.White, fontWeight = FontWeight.Black)
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, null, tint = Color.White)
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    ScoreboardHeader(match)
                    Spacer(modifier = Modifier.height(24.dp))
                }

                item { StatRow("POSSESSION", "${stats.homePossession}%", "${stats.awayPossession}%", stats.homePossession.toFloat() / 100f) }
                item { StatRow("SHOTS", stats.homeShots.toString(), stats.awayShots.toString(), calculateStatRatio(stats.homeShots, stats.awayShots)) }
                item { StatRow("ON TARGET", stats.homeShotsOnTarget.toString(), stats.awayShotsOnTarget.toString(), calculateStatRatio(stats.homeShotsOnTarget, stats.awayShotsOnTarget)) }
                item { StatRow("CORNERS", stats.homeCorners.toString(), stats.awayCorners.toString(), calculateStatRatio(stats.homeCorners, stats.awayCorners)) }
                item { StatRow("FOULS", stats.homeFouls.toString(), stats.awayFouls.toString(), calculateStatRatio(stats.homeFouls, stats.awayFouls)) }
                item { StatRow("YELLOW CARDS", stats.homeYellowCards.toString(), stats.awayYellowCards.toString(), calculateStatRatio(stats.homeYellowCards, stats.awayYellowCards)) }
                item { StatRow("RED CARDS", stats.homeRedCards.toString(), stats.awayRedCards.toString(), calculateStatRatio(stats.homeRedCards, stats.awayRedCards)) }
                item { StatRow("SAVES", stats.homeSaves.toString(), stats.awaySaves.toString(), calculateStatRatio(stats.homeSaves, stats.awaySaves)) }
            }

            Button(
                onClick = onClose,
                modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FameColors.AfroSunOrange),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("CLOSE", fontWeight = FontWeight.Black, color = Color.Black)
            }
        }
    }
}

@Composable
fun ScoreboardHeader(match: MatchInfoUiModel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
            TeamLogo(match.homeTeam, modifier = Modifier.size(48.dp))
            Text(match.homeTeam.uppercase(), color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp, textAlign = TextAlign.Center)
        }
        
        Text("${match.homeScore} - ${match.awayScore}", style = AFMTextStyles.textLG.copy(fontSize = 32.sp, fontWeight = FontWeight.Black), color = FameColors.TrophyGold)

        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
            TeamLogo(match.awayTeam, modifier = Modifier.size(48.dp))
            Text(match.awayTeam.uppercase(), color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun StatRow(label: String, homeValue: String, awayValue: String, ratio: Float) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(homeValue, color = Color.White, fontWeight = FontWeight.Bold)
            Text(label, color = FameColors.MutedParchment, fontSize = 10.sp, fontWeight = FontWeight.Black)
            Text(awayValue, color = Color.White, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.1f))) {
            Box(modifier = Modifier.weight(ratio.coerceIn(0.01f, 0.99f)).fillMaxHeight().background(FameColors.TrophyGold))
            Box(modifier = Modifier.weight((1f - ratio).coerceIn(0.01f, 0.99f)).fillMaxHeight().background(Color.DarkGray))
        }
    }
}

fun calculateStatRatio(home: Int, away: Int): Float {
    if (home == 0 && away == 0) return 0.5f
    return home.toFloat() / (home + away).toFloat()
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun PreviewMatchScreenRefined() {
    AFM2026Theme {
        MatchScreenContent(
            uiState = MatchUiState(
                matchStatus = MatchStatus.FIRST_HALF,
                currentMinute = 24,
                matchInfo = MatchInfoUiModel(
                    id = 1, homeTeam = "West Ham", awayTeam = "Tottenham",
                    homeScore = 0, awayScore = 1,
                    competition = "Prem League", stadium = "London Stadium", kickoff = "15:00"
                ),
                commentary = listOf(CommentaryUiModel(1, "18'", "Y.Mansilla scored!", "GOAL"))
            ),
            onBack = {}, onNavigateToTactics = {}, onNavigateToCup = {}, onProceedToNextState = {},
            onSkipMatch = {},
            onSubmitPressConferenceResponse = {}, onSetMatchSpeed = {}, onTogglePause = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun PreviewPostMatchCelebration() {
    AFM2026Theme {
        MatchScreenContent(
            uiState = MatchUiState(
                matchStatus = MatchStatus.MATCH_SUMMARY,
                matchInfo = MatchInfoUiModel(
                    id = 1, homeTeam = "West Ham", awayTeam = "Tottenham",
                    homeScore = 3, awayScore = 1,
                    competition = "Prem League", stadium = "London Stadium", kickoff = "15:00"
                )
            ),
            onBack = {}, onNavigateToTactics = {}, onNavigateToCup = {}, onProceedToNextState = {},
            onSkipMatch = {},
            onSubmitPressConferenceResponse = {}, onSetMatchSpeed = {}, onTogglePause = {}
        )
    }
}
