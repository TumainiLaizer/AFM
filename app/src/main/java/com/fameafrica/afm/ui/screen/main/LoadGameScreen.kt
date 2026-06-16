package com.fameafrica.afm.ui.screen.main

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fameafrica.afm.R
import com.fameafrica.afm.data.database.model.CareerSaveModel
import com.fameafrica.afm.ui.components.common.GlassPanel
import com.fameafrica.afm.ui.components.common.TeamLogo
import com.fameafrica.afm.ui.theme.AFMTextStyles
import com.fameafrica.afm.ui.theme.FameColors
import com.fameafrica.afm.ui.theme.StadiumBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoadGameScreen(
    onBack: () -> Unit,
    onLoadSave: (Int) -> Unit,
    viewModel: LoadGameViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LoadGameScreenContent(
        uiState = uiState,
        onBack = onBack,
        onLoadSave = onLoadSave,
        onDeleteSave = { viewModel.deleteSave(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoadGameScreenContent(
    uiState: LoadGameUiState,
    onBack: () -> Unit,
    onLoadSave: (Int) -> Unit,
    onDeleteSave: (Int) -> Unit
) {
    StadiumBackground {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "CAREER ARCHIVES",
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
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            },
            containerColor = Color.Transparent,
            modifier = Modifier.fillMaxSize()
        ) { padding ->
            Box(
                modifier = Modifier
                    .padding(padding)
                    .consumeWindowInsets(padding)
                    .fillMaxSize()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = FameColors.ChampionsGold)
                } else if (uiState.saveGames.isEmpty()) {
                    EmptySavesContent()
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp, 
                            end = 16.dp, 
                            top = 16.dp, 
                            bottom = 16.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.saveGames, key = { it.careerId }) { save ->
                            SaveSlotCard(
                                save = save,
                                onClick = { onLoadSave(save.careerId) },
                                onDelete = { onDeleteSave(save.careerId) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SaveSlotCard(save: CareerSaveModel, onClick: () -> Unit, onDelete: () -> Unit) {
    val context = LocalContext.current
    val avatarResId = if (save.managerAvatar != null) {
        context.resources.getIdentifier(save.managerAvatar, "drawable", context.packageName)
    } else 0

    GlassPanel(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp), 
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Manager Avatar Thumbnail
            Surface(
                modifier = Modifier.size(54.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.1f),
                border = BorderStroke(1.dp, FameColors.ChampionsGold.copy(alpha = 0.3f))
            ) {
                Image(
                    painter = if (avatarResId != 0) painterResource(id = avatarResId) else painterResource(id = R.drawable.default_manager),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text((save.saveName ?: "Untitled Career").uppercase(), style = AFMTextStyles.tableHeader, color = FameColors.WarmIvory, maxLines = 1)
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TeamLogo(save.teamName, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(save.teamName, style = AFMTextStyles.statLabel, color = FameColors.ChampionsGold)
                }
                
                Text("SEASON ${save.season} | WEEK ${save.week}", style = AFMTextStyles.statLabel.copy(fontSize = 9.sp), color = FameColors.MutedParchment)
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, null, tint = FameColors.KenteRed.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
                }
                Icon(Icons.Default.PlayArrow, null, tint = FameColors.PitchGreen, modifier = Modifier.size(24.dp))
            }
        }
    }
}

@Composable
fun EmptySavesContent() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp), 
        contentAlignment = Alignment.Center
    ) {
        GlassPanel {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally, 
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(Icons.Default.History, null, modifier = Modifier.size(64.dp), tint = Color.White.copy(alpha = 0.1f))
                Text("NO CAREER ARCHIVES FOUND", style = AFMTextStyles.tableHeader, color = FameColors.MutedParchment)
                Text(
                    "Start your journey into African football management to see your saves here.", 
                    style = AFMTextStyles.tableCell, 
                    color = FameColors.WarmIvory.copy(alpha = 0.6f), 
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoadGameScreenPreview() {
    com.fameafrica.afm.ui.theme.AFM2026Theme {
        LoadGameScreenContent(
            uiState = LoadGameUiState(
                isLoading = false,
                saveGames = listOf(
                    CareerSaveModel(
                        careerId = 1,
                        managerId = 1,
                        managerName = "Tumaini Joseph",
                        managerAvatar = null,
                        teamId = 2,
                        teamName = "Young Africans SC",
                        season = "2025/26",
                        week = 12,
                        gameDate = "2025-11-20",
                        difficulty = "Normal",
                        lastPlayed = System.currentTimeMillis().toString(),
                        saveName = "Yanga Domination",
                        gameVersion = "1.0.0"
                    ),
                    CareerSaveModel(
                        careerId = 2,
                        managerId = 2,
                        managerName = "Pitso Mosimane",
                        managerAvatar = null,
                        teamId = 101,
                        teamName = "Al Ahly",
                        season = "2024/25",
                        week = 1,
                        gameDate = "2024-08-01",
                        difficulty = "Hard",
                        lastPlayed = System.currentTimeMillis().toString(),
                        saveName = "Pharaoh Legacy",
                        gameVersion = "1.0.0"
                    )
                )
            ),
            onBack = {},
            onLoadSave = {},
            onDeleteSave = {}
        )
    }
}
