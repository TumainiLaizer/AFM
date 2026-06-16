package com.fameafrica.afm.ui.screen.training

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fameafrica.afm.ui.components.common.*
import com.fameafrica.afm.ui.theme.*
import java.util.Locale

@Composable
fun TrainingScreen(
    onBack: () -> Unit,
    viewModel: TrainingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    TrainingContent(
        uiState = uiState,
        onBack = onBack,
        onCategorySelected = { cat -> viewModel.updateFocus(cat) },
        onApprove = { viewModel.approveSchedule() }
    )
}

@Composable
fun TrainingContent(
    uiState: TrainingHQState,
    onBack: () -> Unit,
    onCategorySelected: (String) -> Unit,
    onApprove: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    StadiumBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                SidebarBroadcastHeader("PERFORMANCE CENTER", Icons.Default.FitnessCenter)
                
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (uiState.schedule != null && !uiState.schedule.isApproved) {
                        item {
                            ApprovalBanner(
                                generatedBy = uiState.schedule.generatedBy ?: "Assistant Manager",
                                onApprove = onApprove
                            )
                        }
                    }

                    item {
                        TrainingSectionCard {
                            Text("SQUAD READINESS", style = AFMTextStyles.sectionHeader, color = FameColors.TrophyGold)
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                val avgSharpness = if (uiState.squadSharpness.isNotEmpty()) uiState.squadSharpness.map { it.sharpness }.average().toInt() else 0
                                val avgStamina = if (uiState.squadSharpness.isNotEmpty()) uiState.squadSharpness.map { it.stamina }.average().toInt() else 0
                                
                                TrainingMetric("Sharpness", "$avgSharpness%", Icons.Default.Bolt, FameColors.AfroSunOrange)
                                TrainingMetric("Stamina", "$avgStamina%", Icons.Default.Favorite, FameColors.GrowthGreen)
                                TrainingMetric("Intensity", uiState.schedule?.globalIntensity ?: "NORMAL", Icons.AutoMirrored.Filled.TrendingUp, FameColors.AlertRed)
                            }
                        }
                    }

                    item {
                        Text("TRAINING FOCUS", style = AFMTextStyles.sectionHeader, color = Color.White)
                        Spacer(modifier = Modifier.height(12.dp))
                        CategoryFilters(selected = uiState.schedule?.primaryFocus ?: "BALANCED", onSelect = onCategorySelected)
                    }

                    item {
                        TrainingSectionCard {
                            Text("SCHEDULED DRILLS", style = AFMTextStyles.sectionHeader, color = FameColors.TrophyGold)
                            Spacer(modifier = Modifier.height(12.dp))
                            if (uiState.trainingDays.isEmpty()) {
                                Text("NO DRILLS SCHEDULED FOR THIS MONTH.", style = AFMTextStyles.textXS, color = FameColors.MutedParchment)
                            } else {
                                uiState.trainingDays.take(5).forEach { day ->
                                    DrillItem(day)
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                    }
                    
                    item {
                        Text("PLAYER PERFORMANCE", style = AFMTextStyles.sectionHeader, color = Color.White)
                        Spacer(modifier = Modifier.height(12.dp))
                        uiState.squadSharpness.take(10).forEach { player ->
                            PlayerTrainingRow(player)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }

            IconButton(
                onClick = onBack,
                modifier = Modifier.padding(top = 16.dp, start = 8.dp).align(Alignment.TopStart)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
            }
        }
    }
}

@Composable
fun ApprovalBanner(generatedBy: String, onApprove: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = FameColors.AfroSunOrange.copy(alpha = 0.9f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("MONTHLY PLAN READY", style = AFMTextStyles.textSM, color = Color.Black, fontWeight = FontWeight.Bold)
                Text("PROPOSED BY $generatedBy", style = AFMTextStyles.textXXS, color = Color.Black.copy(alpha = 0.7f))
            }
            Button(
                onClick = onApprove,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                shape = RoundedCornerShape(2.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("APPROVE", style = AFMTextStyles.textXS, color = Color.White, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun DrillItem(day: com.fameafrica.afm.data.database.entities.TrainingDayEntity) {
    Row(
        modifier = Modifier.fillMaxWidth().background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(4.dp)).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.SportsScore, null, tint = FameColors.ChampionsGold, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(day.morningSession.uppercase().replace("_", " "), style = AFMTextStyles.textXS, color = Color.White, fontWeight = FontWeight.Bold)
            Text("AFTERNOON: ${day.afternoonSession.uppercase().replace("_", " ")}", style = AFMTextStyles.textXXS, color = FameColors.MutedParchment)
        }
        Text(day.date.takeLast(2), style = AFMTextStyles.statValue, color = FameColors.AfroSunOrange)
    }
}

@Composable
fun PlayerTrainingRow(player: PlayerSharpnessUiModel) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White.copy(alpha = 0.03f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(player.name.uppercase(), style = AFMTextStyles.textXS, color = Color.White, fontWeight = FontWeight.Bold)
                Text(player.position, style = AFMTextStyles.textXXS, color = FameColors.MutedParchment)
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("SHP", style = AFMTextStyles.textXXS, color = FameColors.MutedParchment)
                    Text("${player.sharpness}", style = AFMTextStyles.textXS, color = if(player.sharpness > 80) FameColors.GrowthGreen else Color.White)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("STA", style = AFMTextStyles.textXXS, color = FameColors.MutedParchment)
                    Text("${player.stamina}", style = AFMTextStyles.textXS, color = if(player.stamina < 50) FameColors.AlertRed else Color.White)
                }
            }
        }
    }
}

@Composable
fun TrainingSectionCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = FameColors.SurfaceDark.copy(alpha = 0.4f),
        shape = RoundedCornerShape(2.dp),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
fun TrainingMetric(label: String, value: String, icon: ImageVector, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(value.uppercase(Locale.ROOT), style = AFMTextStyles.textLG, color = Color.White)
        Text(label.uppercase(Locale.ROOT), style = AFMTextStyles.textXXS, color = FameColors.MutedParchment, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun CategoryFilters(selected: String, onSelect: (String) -> Unit) {
    val cats = listOf("BALANCED", "TECHNICAL", "TACTICAL", "PHYSICAL", "RECOVERY")
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(cats) { cat ->
            val isSelected = cat == selected
            Surface(
                onClick = { onSelect(cat) },
                color = if (isSelected) FameColors.PitchGreen else Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(2.dp),
                border = BorderStroke(1.dp, if (isSelected) FameColors.PitchGreen else Color.White.copy(alpha = 0.1f))
            ) {
                Text(
                    cat.uppercase(Locale.ROOT),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    style = AFMTextStyles.textXXS,
                    color = Color.White,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TrainingScreenPreview() {
    AFM2026Theme {
        TrainingContent(
            uiState = TrainingHQState(
                isLoading = false,
                trainingDays = listOf(),
                squadSharpness = listOf(
                    PlayerSharpnessUiModel(1, "Player One", "ST", 85, 90, 80),
                    PlayerSharpnessUiModel(2, "Player Two", "CB", 75, 80, 70)
                )
            ),
            onBack = {},
            onCategorySelected = {},
            onApprove = {}
        )
    }
}
