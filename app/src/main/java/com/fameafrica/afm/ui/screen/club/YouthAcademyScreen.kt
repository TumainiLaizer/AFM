package com.fameafrica.afm.ui.screen.club

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.fameafrica.afm.ui.shared.FameCard
import com.fameafrica.afm.ui.theme.*
import com.fameafrica.afm.utils.PlayerAssetUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YouthAcademyScreen(
    onBack: () -> Unit,
    onPlayerClick: (Int) -> Unit,
    viewModel: YouthAcademyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var revealStarted by remember { mutableStateOf(false) }

    AFM2026Theme {
        FameBackground {
            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    TopAppBar(
                        title = { Text("YOUTH ACADEMY", style = AFMTextStyles.textMD.copy(fontWeight = FontWeight.Black)) },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = FameColors.HeaderDark, titleContentColor = Color.White)
                    )
                }
            ) { padding ->
                if (uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = FameColors.TrophyGold)
                    }
                } else if (!revealStarted && uiState.prospects.isEmpty()) {
                    IntakeRevealCover { viewModel.triggerNewIntake(); revealStarted = true }
                } else {
                    IntakeListView(padding, uiState.prospects, onPlayerClick)
                }
            }
        }
    }
}

@Composable
fun IntakeRevealCover(onStart: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        FameCard(modifier = Modifier.padding(32.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.School, null, modifier = Modifier.size(64.dp), tint = FameColors.TrophyGold)
                Spacer(Modifier.height(16.dp))
                Text("YOUTH INTAKE REVEAL DAY", style = AFMTextStyles.textLG.copy(fontWeight = FontWeight.Black), color = Color.White)
                Text("THE CLASS OF 2026 HAS ARRIVED", style = AFMTextStyles.textXS, color = FameColors.MutedParchment)
                Spacer(Modifier.height(32.dp))
                Button(
                    onClick = onStart,
                    colors = ButtonDefaults.buttonColors(containerColor = FameColors.TrophyGold),
                    shape = RoundedCornerShape(2.dp)
                ) {
                    Text("REVEAL PROSPECTS", color = FameColors.DeepNavyBlack, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
fun IntakeListView(
    padding: PaddingValues,
    prospects: List<com.fameafrica.afm.data.database.entities.PlayersEntity>,
    onPlayerClick: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.padding(padding).fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { 
            Text("NEW ACADEMY GRADUATES", style = AFMTextStyles.textXS.copy(fontWeight = FontWeight.Black), color = FameColors.TrophyGold)
            Spacer(Modifier.height(8.dp))
        }
        
        items(prospects) { player ->
            ProspectCard(player, onPlayerClick)
        }
    }
}

@Composable
fun ProspectCard(
    player: com.fameafrica.afm.data.database.entities.PlayersEntity,
    onClick: (Int) -> Unit
) {
    FameCard(modifier = Modifier.clickable { onClick(player.id) }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(56.dp).clip(RoundedCornerShape(4.dp)).background(Color.White.copy(0.05f))) {
                AsyncImage(
                    model = PlayerAssetUtils.getPlayerFace(player.id, player.nationality),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(player.name.uppercase(), style = AFMTextStyles.textSM.copy(fontWeight = FontWeight.Black), color = Color.White)
                Text(player.position.uppercase(), style = AFMTextStyles.textXS, color = FameColors.MutedParchment)
            }
            if (player.potential >= 85) {
                StatusBadge("WONDERKID", FameColors.ChampionsGold)
            }
            
            Spacer(Modifier.width(8.dp))
            
            Column(horizontalAlignment = Alignment.End) {
                Text(player.rating.toString(), style = AFMTextStyles.textMD, color = FameColors.GrowthGreen, fontWeight = FontWeight.Black)
                Text("RTG", style = AFMTextStyles.textXXS, color = FameColors.MutedParchment)
            }
        }
    }
}

@Composable
fun StatusBadge(text: String, color: Color) {
    Surface(color = color.copy(alpha = 0.1f), border = BorderStroke(1.dp, color), shape = RoundedCornerShape(2.dp)) {
        Text(text, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = AFMTextStyles.textXS.copy(fontWeight = FontWeight.Black, fontSize = 8.sp), color = color)
    }
}
