package com.fameafrica.afm.ui.screen.career

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.fameafrica.afm.ui.components.common.SidebarCard
import com.fameafrica.afm.ui.components.common.TeamLogo
import com.fameafrica.afm.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CareerScreen(
    onBack: () -> Unit,
    onNewCareer: () -> Unit,
    onLoadCareer: (Int) -> Unit,
    viewModel: CareerViewModel = hiltViewModel()
) {
    val savedCareers by viewModel.savedCareers.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    CareerScreenContent(
        savedCareers = savedCareers,
        isLoading = isLoading,
        onBack = onBack,
        onNewCareer = onNewCareer,
        onLoadCareer = onLoadCareer,
        onDeleteCareer = viewModel::deleteCareer
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CareerScreenContent(
    savedCareers: List<CareerSaveModel>,
    isLoading: Boolean,
    onBack: () -> Unit,
    onNewCareer: () -> Unit,
    onLoadCareer: (Int) -> Unit,
    onDeleteCareer: (Int) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                Surface(
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    color = FameColors.HeaderDark,
                    border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Box(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp), contentAlignment = Alignment.Center) {
                        IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                        }
                        
                        Text(
                            "MANAGEMENT CAREERS",
                            style = AFMTextStyles.textLG.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            ),
                            color = Color.White
                        )
                    }
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onNewCareer,
                    containerColor = FameColors.TrophyGold,
                    contentColor = Color.Black,
                    shape = RoundedCornerShape(2.dp),
                    modifier = Modifier.navigationBarsPadding()
                ) {
                    Icon(Icons.Default.Add, contentDescription = "New Career")
                }
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
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = FameColors.TrophyGold)
                } else if (savedCareers.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                        SidebarCard {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Icon(Icons.Default.History, null, modifier = Modifier.size(48.dp), tint = FameColors.TrophyGold.copy(alpha = 0.2f))
                                Text("NO ACTIVE CAREERS", style = AFMTextStyles.sectionHeader, color = FameColors.TrophyGold)
                                Text(
                                    "Start a new journey to build your legacy in African football.",
                                    style = AFMTextStyles.textSM,
                                    color = Color.White.copy(alpha = 0.6f),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(savedCareers, key = { it.careerId }) { career ->
                            CareerItem(career, onLoadCareer, onDeleteCareer)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CareerItem(
    career: CareerSaveModel,
    onLoad: (Int) -> Unit,
    onDelete: (Int) -> Unit
) {
    val context = LocalContext.current
    val avatarResId = if (career.managerAvatar != null) {
        context.resources.getIdentifier(career.managerAvatar, "drawable", context.packageName)
    } else 0
    
    val lastPlayedFormatted = try {
        val lastPlayedLong = career.lastPlayed.toLongOrNull() ?: 0L
        if (lastPlayedLong > 0) {
            val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
            sdf.format(Date(lastPlayedLong))
        } else career.lastPlayed
    } catch (e: Exception) {
        career.lastPlayed
    }

    SidebarCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onLoad(career.careerId) }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Manager Avatar Thumbnail (Selected Avatar)
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(2.dp),
                color = FameColors.HeaderDark,
                border = BorderStroke(1.dp, FameColors.TrophyGold.copy(alpha = 0.5f))
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
                Text(
                    text = career.saveName.uppercase(),
                    style = AFMTextStyles.textSM.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp),
                    color = Color.White,
                    maxLines = 1
                )
                
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    TeamLogo(career.teamName, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = career.teamName.uppercase(),
                        style = AFMTextStyles.textXS.copy(fontWeight = FontWeight.Bold),
                        color = FameColors.TrophyGold
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Icon(Icons.Default.CalendarToday, null, tint = Color.White.copy(alpha = 0.4f), modifier = Modifier.size(10.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "SEASON ${career.season} | WEEK ${career.week}".uppercase(),
                        style = AFMTextStyles.textXS,
                        color = Color.White.copy(alpha = 0.4f)
                    )
                }

                Text(
                    text = "LAST PLAYED: $lastPlayedFormatted".uppercase(),
                    style = AFMTextStyles.textXS.copy(fontSize = 8.sp),
                    color = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onDelete(career.careerId) }) {
                    Icon(Icons.Default.Delete, null, tint = FameColors.AlertRed.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                }
                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = RoundedCornerShape(2.dp),
                    color = FameColors.TrophyGold.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, FameColors.TrophyGold)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.PlayArrow, null, tint = FameColors.TrophyGold, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CareerScreenPreview() {
    AFM2026Theme(themePreset = FootballThemePreset.MANAGER_MODE) {
        CareerScreenContent(
            savedCareers = listOf(
                CareerSaveModel(
                    careerId = 1,
                    managerId = 1,
                    managerName = "Tumaini Joseph",
                    managerAvatar = "coach_male_east",
                    teamId = 2,
                    teamName = "Young Africans SC",
                    season = "2025/26",
                    week = 12,
                    gameDate = "Dec 15, 2025",
                    difficulty = "Normal",
                    lastPlayed = System.currentTimeMillis().toString(),
                    saveName = "Yanga Domination",
                    gameVersion = "1.0.0"
                ),
                CareerSaveModel(
                    careerId = 2,
                    managerId = 2,
                    managerName = "Pitso Mosimane",
                    managerAvatar = "coach_male_south",
                    teamId = 101,
                    teamName = "Al Ahly",
                    season = "2024/25",
                    week = 1,
                    gameDate = "Aug 15, 2024",
                    difficulty = "Hard",
                    lastPlayed = (System.currentTimeMillis() - 86400000).toString(),
                    saveName = "Pharaoh Legacy",
                    gameVersion = "1.0.0"
                ),
                CareerSaveModel(
                    careerId = 3,
                    managerId = 3,
                    managerName = "Aliou Cissé",
                    managerAvatar = "coach_male_west",
                    teamId = 120,
                    teamName = "Mamelodi Sundowns",
                    season = "2025/26",
                    week = 5,
                    gameDate = "Sep 20, 2025",
                    difficulty = "Legend",
                    lastPlayed = (System.currentTimeMillis() - 172800000).toString(),
                    saveName = "Masandawana Project",
                    gameVersion = "1.0.0"
                )
            ),
            isLoading = false,
            onBack = {},
            onNewCareer = {},
            onLoadCareer = {},
            onDeleteCareer = {}
        )
    }
}
