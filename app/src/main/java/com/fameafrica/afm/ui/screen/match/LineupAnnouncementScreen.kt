package com.fameafrica.afm.ui.screen.match

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fameafrica.afm.ui.components.common.PitchCanvas
import com.fameafrica.afm.ui.screen.tactics.getPositionsForFormation
import com.fameafrica.afm.ui.screen.tactics.getPositionLabelsForFormation
import com.fameafrica.afm.ui.theme.AFMTextStyles
import com.fameafrica.afm.ui.theme.FameColors
import androidx.compose.ui.tooling.preview.Preview
import com.fameafrica.afm.ui.theme.AFM2026Theme
import kotlinx.coroutines.launch

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun PreviewLineupAnnouncement() {
    AFM2026Theme {
        LineupAnnouncementScreen(
            uiState = MatchUiState(
                matchInfo = MatchInfoUiModel(
                    id = 1, homeTeam = "West Ham", awayTeam = "Tottenham",
                    competition = "Prem League", stadium = "London Stadium", kickoff = "15:00",
                    homeFormation = "4-3-3", awayFormation = "4-4-2",
                ),
                homePlayers = List(11) { PlayerLineupUiModel(it, "Player $it", "POS", 75.0 + it) },
                awayPlayers = List(11) { PlayerLineupUiModel(it + 11, "Opponent $it", "POS", 70.0 + it) }
            )
        ) {}
    }
}

@Composable
fun LineupAnnouncementScreen(
    uiState: MatchUiState,
    onProceed: () -> Unit
) {
    val match = uiState.matchInfo ?: return
    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        // Team Selector / Header
        LineupHeader(
            homeTeam = match.homeTeam,
            awayTeam = match.awayTeam,
            currentPage = pagerState.currentPage,
            onPageSelected = { scope.launch { pagerState.animateScrollToPage(it) } }
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            val players = if (page == 0) uiState.homePlayers else uiState.awayPlayers
            val formation = if (page == 0) match.homeFormation else match.awayFormation
            
            LineupView(players, formation)
        }

        // Bottom Action
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.Black.copy(alpha = 0.8f),
            border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Button(
                onClick = onProceed,
                modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FameColors.AfroSunOrange),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("KICK OFF", fontWeight = FontWeight.Black, color = Color.Black, fontSize = 18.sp)
            }
        }
    }
}

@Composable
fun LineupHeader(
    homeTeam: String,
    awayTeam: String,
    currentPage: Int,
    onPageSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(64.dp).background(Color.Black),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TeamTab(homeTeam, isSelected = currentPage == 0, onClick = { onPageSelected(0) }, modifier = Modifier.weight(1f))
        TeamTab(awayTeam, isSelected = currentPage == 1, onClick = { onPageSelected(1) }, modifier = Modifier.weight(1f))
    }
}

@Composable
fun TeamTab(name: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(if (isSelected) FameColors.DeepNavyBlack else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                name.uppercase(),
                style = AFMTextStyles.textSM.copy(fontWeight = FontWeight.Black),
                color = if (isSelected) FameColors.TrophyGold else Color.Gray
            )
            if (isSelected) {
                Box(modifier = Modifier.width(40.dp).height(2.dp).background(FameColors.TrophyGold))
            }
        }
    }
}

@Composable
fun LineupView(players: List<PlayerLineupUiModel>, formation: String) {
    Box(modifier = Modifier.fillMaxSize()) {
        PitchCanvas(modifier = Modifier.fillMaxSize())
        
        val positions = getPositionsForFormation(formation)
        val labels = getPositionLabelsForFormation(formation)
        
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val pitchWidth = maxWidth
            val pitchHeight = maxHeight

            players.take(11).forEachIndexed { index, player ->
                if (index < positions.size) {
                    val pos = positions[index]
                    val label = labels[index]
                    
                    PlayerLineupCard(
                        player = player,
                        assignedPosition = label,
                        modifier = Modifier.offset(
                            x = (pos.first * pitchWidth.value).dp - 31.dp, // Center the card (width 62)
                            y = (pos.second * pitchHeight.value).dp - 40.dp // Offset for height
                        )
                    )
                }
            }
        }
        
        // Formation Text Overlay
        Surface(
            color = Color.Black.copy(alpha = 0.6f),
            shape = RoundedCornerShape(topEnd = 8.dp),
            modifier = Modifier.align(Alignment.BottomStart)
        ) {
            Text(
                text = formation,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                color = FameColors.TrophyGold,
                fontWeight = FontWeight.Black,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun PlayerLineupCard(
    player: PlayerLineupUiModel,
    assignedPosition: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.width(62.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Surface(
                shape = RoundedCornerShape(2.dp),
                modifier = Modifier.size(44.dp),
                color = Color.Black.copy(alpha = 0.6f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
            ) {
                AsyncImage(
                    model = player.photo ?: "file:///android_asset/players/default.webp",
                    contentDescription = player.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // Rating Bubble
            Surface(
                color = FameColors.TrophyGold,
                shape = RoundedCornerShape(1.dp),
                modifier = Modifier.align(Alignment.BottomCenter).offset(y = 6.dp)
            ) {
                Text(
                    text = player.rating.toInt().toString(),
                    style = AFMTextStyles.textXS,
                    color = Color.Black,
                    fontWeight = FontWeight.Black,
                    fontSize = 9.sp,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = player.name.split(" ").last().uppercase(),
            style = AFMTextStyles.textXS.copy(fontSize = 9.sp),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )

        Text(
            assignedPosition.uppercase(),
            style = AFMTextStyles.textXS.copy(fontSize = 7.sp),
            color = FameColors.TrophyGold,
            fontWeight = FontWeight.Black
        )
    }
}
