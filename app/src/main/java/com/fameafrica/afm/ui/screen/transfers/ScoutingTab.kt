package com.fameafrica.afm.ui.screen.transfers

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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fameafrica.afm.ui.components.*
import com.fameafrica.afm.ui.theme.AFM2026Theme
import com.fameafrica.afm.ui.theme.FootballThemePreset
import coil.compose.AsyncImage
import com.fameafrica.afm.R
import com.fameafrica.afm.ui.theme.AFMTextStyles
import com.fameafrica.afm.ui.theme.FameColors

@Composable
fun ScoutingTab(
    uiState: TransfersUiState,
    onAddInstruction: () -> Unit,
    onPlayerClick: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Active Missions Header & Horizontal Row
        item {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "SCOUTING MISSIONS",
                        style = AFMTextStyles.textXS,
                        color = FameColors.TrophyGold,
                        fontWeight = FontWeight.Black
                    )
                    Surface(
                        onClick = onAddInstruction,
                        color = FameColors.TrophyGold.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(14.dp), tint = FameColors.TrophyGold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("NEW PLAN", style = AFMTextStyles.textXXS, fontWeight = FontWeight.Black, color = FameColors.TrophyGold)
                        }
                    }
                }

                if (uiState.activeScoutInstructions.isEmpty()) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        EmptyScoutingState()
                    }
                } else {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.activeScoutInstructions) { instruction ->
                            ScoutInstructionCard(instruction)
                        }
                    }
                }
            }
        }

        // Scout Recommendations / Reports
        item {
            Text(
                "SCOUT REPORTS & TOP TARGETS",
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp),
                style = AFMTextStyles.textXS,
                color = FameColors.MutedParchment,
                fontWeight = FontWeight.Black
            )
        }

        // Top Target Card (Featured Suggestion)
        if (uiState.aiSuggestions.isNotEmpty()) {
            item {
                TopTargetFeatureCard(uiState.aiSuggestions.first(), onPlayerClick)
            }
        }

        // Other Recommendations
        items(uiState.aiSuggestions.drop(1)) { suggestion ->
            RecommendationCard(suggestion, onPlayerClick)
        }
    }
}

@Composable
fun ScoutInstructionCard(instruction: ScoutInstruction) {
    Surface(
        color = Color.White.copy(alpha = 0.04f),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.width(260.dp),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(FameColors.TransferBlue.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Public,
                        null,
                        modifier = Modifier.size(16.dp),
                        tint = FameColors.TransferBlue
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        instruction.region?.name?.replace("_", " ") ?: "Global Search",
                        style = AFMTextStyles.textXS,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        "Exp: ${instruction.durationWeeks} Weeks",
                        style = AFMTextStyles.textXXS,
                        color = FameColors.MutedParchment
                    )
                }
                IconButton(onClick = { /* TODO */ }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Outlined.Edit, null, modifier = Modifier.size(14.dp), tint = FameColors.MutedParchment)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            
            // Segmented Progress Bar
            SegmentedProgressBar(progress = instruction.progress)

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MissionTag(instruction.position ?: "ANY")
                MissionTag("U${instruction.maxAge}")
                MissionTag(instruction.quality.name.replace("_", " "))
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Payments, null, modifier = Modifier.size(10.dp), tint = FameColors.MutedParchment)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "Cost: ${instruction.costPerWeek}/wk",
                    style = AFMTextStyles.textXXS,
                    color = FameColors.MutedParchment
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    "${instruction.progress}%",
                    style = AFMTextStyles.textXXS,
                    fontWeight = FontWeight.Black,
                    color = FameColors.GrowthGreen
                )
            }
        }
    }
}

@Composable
fun SegmentedProgressBar(progress: Int) {
    val totalSegments = 10
    val activeSegments = (progress / 10).coerceIn(0, totalSegments)
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        repeat(totalSegments) { index ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(
                        if (index < activeSegments) FameColors.GrowthGreen 
                        else Color.White.copy(alpha = 0.05f)
                    )
            )
        }
    }
}

@Composable
fun TopTargetFeatureCard(suggestion: RecruitmentSuggestion, onPlayerClick: (Int) -> Unit) {
    Surface(
        onClick = { onPlayerClick(suggestion.player.id) },
        color = FameColors.TrophyGold.copy(alpha = 0.05f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, FameColors.TrophyGold.copy(alpha = 0.2f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Featured Label
                Surface(
                    color = FameColors.TrophyGold,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        "TOP TARGET",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = AFMTextStyles.textXXS.copy(fontSize = 7.sp, fontWeight = FontWeight.Black),
                        color = Color.Black
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                RatingBadge("${suggestion.scoutScore}% Confidence")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Player Image Placeholder
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                ) {
                    Icon(Icons.Default.Person, null, modifier = Modifier.align(Alignment.BottomCenter).size(50.dp), tint = Color.White.copy(alpha = 0.2f))
                    Icon(Icons.Default.Verified, null, modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(16.dp), tint = FameColors.TransferBlue)
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        suggestion.player.name.uppercase(),
                        style = AFMTextStyles.textMD,
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        suggestion.recommendation,
                        style = AFMTextStyles.textXXS,
                        color = FameColors.MutedParchment,
                        maxLines = 1
                    )
                    Row(modifier = Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(color = FameColors.GrowthGreen.copy(alpha = 0.1f), shape = RoundedCornerShape(2.dp)) {
                            Text(
                                "CL ELIGIBLE",
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                style = AFMTextStyles.textXXS.copy(fontSize = 7.sp, fontWeight = FontWeight.Black),
                                color = FameColors.GrowthGreen
                            )
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        suggestion.scoutScore.toString(),
                        style = AFMTextStyles.textLG,
                        color = FameColors.GrowthGreen,
                        fontWeight = FontWeight.Black
                    )
                    Text("Scout Score", style = AFMTextStyles.textXXS.copy(fontSize = 7.sp), color = FameColors.MutedParchment)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FeatureStatItem("POTENTIAL", suggestion.player.potentialRange, FameColors.TrophyGold, Modifier.weight(1f))
                FeatureStatItem("MARKET VALUE", "${suggestion.player.value / 1000000}M", Color.White, Modifier.weight(1f))
                FeatureStatItem("ITC STATUS", suggestion.player.cafStatus.name, FameColors.GrowthGreen, Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun RecommendationCard(suggestion: RecruitmentSuggestion, onPlayerClick: (Int) -> Unit) {
    Surface(
        onClick = { onPlayerClick(suggestion.player.id) },
        color = Color.White.copy(alpha = 0.03f),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, null, modifier = Modifier.size(24.dp), tint = Color.White.copy(alpha = 0.2f))
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    suggestion.player.name,
                    style = AFMTextStyles.textXS,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Text(
                    suggestion.recommendation,
                    style = AFMTextStyles.textXXS.copy(fontSize = 8.sp),
                    color = FameColors.MutedParchment,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    suggestion.scoutScore.toString(),
                    style = AFMTextStyles.textSM,
                    color = if(suggestion.scoutScore >= 80) FameColors.GrowthGreen else FameColors.TrophyGold,
                    fontWeight = FontWeight.Black
                )
                Icon(Icons.Default.ChevronRight, null, modifier = Modifier.size(14.dp), tint = FameColors.MutedParchment)
            }
        }
    }
}

@Composable
fun FeatureStatItem(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(label, style = AFMTextStyles.textXXS.copy(fontSize = 7.sp), color = FameColors.MutedParchment, fontWeight = FontWeight.Bold)
        Text(value, style = AFMTextStyles.textXS, color = color, fontWeight = FontWeight.Black)
    }
}

@Composable
fun MissionTag(text: String) {
    Surface(
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(2.dp)
    ) {
        Text(
            text.uppercase(),
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
            style = AFMTextStyles.textXXS.copy(fontSize = 7.sp, fontWeight = FontWeight.Bold),
            color = FameColors.MutedParchment
        )
    }
}

@Composable
fun InfoTag(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
        shape = RoundedCornerShape(2.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Text(
            text.uppercase(),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = AFMTextStyles.textXXS,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun EmptyScoutingState() {
    Surface(
        modifier = Modifier.fillMaxWidth().height(120.dp),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.02f),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.PersonSearch,
                null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "NO ACTIVE SCOUTING MISSIONS",
                style = AFMTextStyles.textXS,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun String.capitalize() = replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp,dpi=420", showSystemUi = true)
@Composable
fun ScoutingTabPreview() {
    AFM2026Theme(themePreset = FootballThemePreset.MANAGER_MODE) {
        Surface(color = FameColors.DeepNavyBlack) {
            ScoutingTab(
                uiState = TransfersUiState(
                    activeScoutInstructions = listOf(
                        ScoutInstruction(
                            id = "1",
                            region = AfricanRegion.WEST_AFRICA,
                            specificCountries = null,
                            position = "ST",
                            minAge = 16,
                            maxAge = 21,
                            quality = SquadQuality.STAR_PLAYER,
                            durationWeeks = 4,
                            costPerWeek = 2500,
                            progress = 65
                        ),
                        ScoutInstruction(
                            id = "2",
                            region = AfricanRegion.NORTH_AFRICA,
                            specificCountries = null,
                            position = "DM",
                            minAge = 18,
                            maxAge = 24,
                            quality = SquadQuality.FIRST_TEAM,
                            durationWeeks = 8,
                            costPerWeek = 1800,
                            progress = 30
                        )
                    ),
                    aiSuggestions = listOf(
                        RecruitmentSuggestion(
                            player = TransferPlayerUiModel(
                                id = 1, name = "Ibrahim Sangaré", age = 21, position = "AM (RLC), ST",
                                nationality = "MLI", club = "ASEC Mimosas", rating = 78, potentialRange = "85-92",
                                value = 1800000, wage = 18000, morale = 85, form = 8,
                                injuryStatus = "Healthy", role = "Inside Forward", height = 178, foot = "Right",
                                cafStatus = CAFStatus.ITC_COMPLETE, potential = 90
                            ),
                            scoutScore = 92,
                            recommendation = "Exceptional talent from ASEC Academy. Highly recommended for immediate first-team role."
                        ),
                        RecruitmentSuggestion(
                            player = TransferPlayerUiModel(
                                id = 2, name = "Abdallah Mahmoud", age = 23, position = "DM, M (C)",
                                nationality = "EGY", club = "Zamalek SC", rating = 75, potentialRange = "80-86",
                                value = 1200000, wage = 16000, morale = 80, form = 7,
                                injuryStatus = "Healthy", role = "Deep Lying Playmaker", height = 182, foot = "Right",
                                cafStatus = CAFStatus.ELIGIBLE, potential = 84
                            ),
                            scoutScore = 84,
                            recommendation = "Reliable defensive midfielder with strong tactical awareness."
                        )
                    )
                ),
                onAddInstruction = {},
                onPlayerClick = {}
            )
        }
    }
}
