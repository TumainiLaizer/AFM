package com.fameafrica.afm.ui.screen.board

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fameafrica.afm.data.database.entities.BoardEvaluationEntity
import com.fameafrica.afm.data.database.entities.BoardRequestsEntity
import com.fameafrica.afm.data.database.entities.ClubVisionEntity
import com.fameafrica.afm.data.database.entities.ObjectivesEntity
import com.fameafrica.afm.ui.components.common.AFMProgressBar
import com.fameafrica.afm.ui.components.common.GlassPanel
import com.fameafrica.afm.ui.theme.*

@Composable
fun BoardScreen(
    onBack: () -> Unit,
    onNavigateToFFP: () -> Unit = {},
    viewModel: BoardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    AFM2026Theme(themePreset = FootballThemePreset.MANAGER_MODE) {
        BoardScreenContent(
            uiState = uiState,
            onBack = onBack,
            onNavigateToFFP = onNavigateToFFP,
            onSubmitRequest = { type, description ->
                viewModel.submitRequest(type, description)
            }
        )
    }
}

@Composable
fun BoardScreen(
    uiState: BoardUiState,
    onBack: () -> Unit,
    onSubmitRequest: (String, String) -> Unit
) {
    BoardScreenContent(
        uiState = uiState,
        onBack = onBack,
        onSubmitRequest = onSubmitRequest
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardScreenContent(
    uiState: BoardUiState,
    onBack: () -> Unit,
    onNavigateToFFP: () -> Unit = {},
    onSubmitRequest: (String, String) -> Unit
) {
    var showRequestDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "THE BOARDROOM",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToFFP) {
                        Icon(Icons.Default.AccountBalance, null, tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showRequestDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(2.dp),
                modifier = Modifier.navigationBarsPadding()
            ) {
                Icon(Icons.Default.Add, contentDescription = "NEW REQUEST")
            }
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        FameBackground {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(paddingValues).consumeWindowInsets(paddingValues),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 16.dp,
                        bottom = 16.dp + 80.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { BoardEvaluationCard(evaluation = uiState.evaluation) }

                    // Board Expectations Summary
                    uiState.chairman?.let { chairman ->
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = FameColors.HeaderDark.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(2.dp),
                                border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.05f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        "CHAIRMAN EXPECTATIONS",
                                        style = AFMTextStyles.textXS,
                                        color = Color.White.copy(alpha = 0.6f),
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    val expectationText = when {
                                        (chairman.ambitionLevel ?: 50) > 80 -> "The chairman is highly ambitious and expects immediate success and elite performance."
                                        (chairman.patienceLevel ?: 50) < 30 -> "The chairman is notoriously impatient. Results must be delivered quickly to maintain job security."
                                        (chairman.wealthLevel ?: 50) > 80 -> "With significant backing, the chairman expects the club to dominate and play attractive football."
                                        else -> "The board expects steady growth and financial stability in the coming seasons."
                                    }
                                    Text(
                                        expectationText,
                                        style = AFMTextStyles.textSM,
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                }
                            }
                        }
                    }

                    uiState.vision?.let { vision ->
                        item { SectionHeader(title = "CLUB VISION & PHILOSOPHY", icon = Icons.Default.Visibility) }
                        item { ClubVisionDetailedCard(vision = vision) }
                        
                        item { SectionHeader(title = "5-YEAR GROWTH PLAN", icon = Icons.AutoMirrored.Filled.TrendingUp) }
                        item { FiveYearPlanCard(vision = vision) }
                    }
                    
                    item { SectionHeader(title = "SEASON OBJECTIVES", icon = Icons.Default.Flag) }
                    
                    items(uiState.objectives, key = { "objective_${it.id}" }) { objective ->
                        SeasonObjectiveItem(objective = objective)
                    }
                    if (uiState.objectives.isEmpty()) {
                        item {
                            GlassPanel(modifier = Modifier.fillMaxWidth(), cornerRadius = 2) {
                                Text(
                                    text = "NO OBJECTIVES SET FOR THIS SEASON.",
                                    style = AFMTextStyles.textXS,
                                    color = Color.White.copy(alpha = 0.5f),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    item { SectionHeader(title = "ACTIVE BOARD REQUESTS", icon = Icons.AutoMirrored.Filled.Assignment) }
                    
                    items(uiState.requests, key = { "request_${it.id}" }) { request ->
                        BoardRequestItem(request = request)
                    }
                    if (uiState.requests.isEmpty()) {
                        item {
                            GlassPanel(modifier = Modifier.fillMaxWidth(), cornerRadius = 2) {
                                Text(
                                    text = "NO ACTIVE REQUESTS. HIGH PERFORMANCE MAINTAINS BOARD TRUST.",
                                    style = AFMTextStyles.textXS,
                                    color = Color.White.copy(alpha = 0.5f),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showRequestDialog) {
        NewRequestDialog(
            onDismiss = { showRequestDialog = false },
            onSubmit = { type, description ->
                onSubmitRequest(type, description)
                showRequestDialog = false
            }
        )
    }
}

@Composable
fun FiveYearPlanCard(vision: ClubVisionEntity) {
    GlassPanel(modifier = Modifier.fillMaxWidth(), cornerRadius = 2) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("COMMERCIAL GROWTH TARGET", style = AFMTextStyles.textXS, color = FameColors.GrowthGreen, fontWeight = FontWeight.Black)
                Text("${vision.commercialGrowthTarget}%", style = AFMTextStyles.textLG, color = Color.White, fontWeight = FontWeight.Black)
            }
            
            AFMProgressBar(
                progress = vision.commercialGrowthTarget / 100f,
                modifier = Modifier.fillMaxWidth(),
                color = FameColors.GrowthGreen
            )

            HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

            Text("PROJECTED MILESTONES", style = AFMTextStyles.textXS, color = Color.White.copy(alpha = 0.5f), fontWeight = FontWeight.Bold)
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PlanYearItem(year = "YEAR 1", target = "FINANCIAL STABILIZATION", status = "CURRENT")
                PlanYearItem(year = "YEAR 2", target = "CONTINENTAL QUALIFICATION", status = "UPCOMING")
                PlanYearItem(year = "YEAR 3", target = "SQUAD EXPANSION", status = "FUTURE")
                PlanYearItem(year = "YEAR 5", target = "LEAGUE SUPREMACY", status = "VISION")
            }
        }
    }
}

@Composable
fun PlanYearItem(year: String, target: String, status: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(year, style = AFMTextStyles.textXS, color = FameColors.GrowthGreen, fontWeight = FontWeight.Black, modifier = Modifier.width(60.dp))
        Text(target, style = AFMTextStyles.textXS, color = Color.White, modifier = Modifier.weight(1f))
        Text(status, style = AFMTextStyles.textXS, fontWeight = FontWeight.Black, color = if (status == "CURRENT") FameColors.GrowthGreen else Color.White.copy(alpha = 0.4f))
    }
}

@Composable
fun SeasonObjectiveItem(objective: ObjectivesEntity) {
    val statusColor = when (objective.status.uppercase()) {
        "ACHIEVED" -> FameColors.GrowthGreen
        "FAILED" -> FameColors.AlertRed
        else -> MaterialTheme.colorScheme.secondary
    }

    GlassPanel(modifier = Modifier.fillMaxWidth(), cornerRadius = 2) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = RoundedCornerShape(2.dp),
                    color = statusColor.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, statusColor.copy(alpha = 0.3f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = when (objective.objectiveType) {
                                "LEAGUE" -> Icons.Default.EmojiEvents
                                "CUP" -> Icons.Default.SportsSoccer
                                "FINANCIAL" -> Icons.Default.AccountBalance
                                "YOUTH" -> Icons.Default.School
                                else -> Icons.Default.Star
                            },
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = objective.objectiveType.uppercase(),
                    style = AFMTextStyles.textXS,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = objective.status.uppercase(),
                    style = AFMTextStyles.textXS,
                    color = statusColor,
                    fontWeight = FontWeight.Black
                )
            }
            
            Text(
                text = (objective.objective ?: "MAINTAIN STANDARD PERFORMANCE").uppercase(),
                style = AFMTextStyles.textSM,
                color = Color.White,
                fontWeight = FontWeight.Black
            )
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("REWARD", style = AFMTextStyles.textXS, color = Color.White.copy(alpha = 0.5f), fontWeight = FontWeight.Bold)
                    Text((objective.reward ?: "BOARD TRUST").uppercase(), style = AFMTextStyles.textXS, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("PENALTY", style = AFMTextStyles.textXS, color = Color.White.copy(alpha = 0.5f), fontWeight = FontWeight.Bold)
                    Text((objective.penalty ?: "REDUCED SECURITY").uppercase(), style = AFMTextStyles.textXS, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = FameColors.TrophyGold, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(title.uppercase(), style = AFMTextStyles.textXS, color = FameColors.WarmIvory, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
    }
}

@Composable
fun BoardEvaluationCard(evaluation: BoardEvaluationEntity?) {
    val satisfaction = evaluation?.boardSatisfaction ?: 78
    val dnaAlignment = evaluation?.dnaAlignment ?: 50
    val patiencePressure = evaluation?.patiencePressure ?: 0
    val ambitionPressure = evaluation?.ambitionPressure ?: 0
    
    val statusColor = if (satisfaction > 75) FameColors.GrowthGreen 
                     else if (satisfaction > 50) FameColors.TrophyGold 
                     else if (satisfaction > 35) FameColors.AfroSunOrange 
                     else FameColors.AlertRed

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = FameColors.HeaderDark,
        shape = RoundedCornerShape(2.dp),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "BOARD CONFIDENCE", 
                    style = AFMTextStyles.textXS, 
                    color = Color.White.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                
                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(1.dp),
                    border = BorderStroke(0.5.dp, statusColor)
                ) {
                    Text(
                        text = evaluation?.status?.uppercase() ?: "SATISFIED",
                        style = AFMTextStyles.textXS,
                        color = statusColor,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(
                        "$satisfaction%", 
                        style = AFMTextStyles.textLG.copy(fontSize = 32.sp), 
                        color = FameColors.GrowthGreen,
                        fontWeight = FontWeight.Black
                    )
                    Text("OVERALL RATING", style = AFMTextStyles.textXS, color = Color.White.copy(alpha = 0.4f), fontWeight = FontWeight.Bold)
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "$dnaAlignment%", 
                        style = AFMTextStyles.textLG.copy(fontSize = 32.sp), 
                        color = if (dnaAlignment > 70) FameColors.GrowthGreen else if (dnaAlignment > 40) FameColors.TrophyGold else FameColors.AlertRed,
                        fontWeight = FontWeight.Black
                    )
                    Text("DNA ALIGNMENT", style = AFMTextStyles.textXS, color = Color.White.copy(alpha = 0.4f), fontWeight = FontWeight.Bold)
                }
            }

            AFMProgressBar(
                progress = satisfaction / 100f,
                modifier = Modifier.fillMaxWidth(),
                color = statusColor
            )

            // Chairman Pressure Section
            if (patiencePressure > 0 || ambitionPressure > 0) {
                HorizontalDivider(color = Color.White.copy(alpha = 0.05f), thickness = 0.5.dp)
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (patiencePressure > 0) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Timer, contentDescription = null, tint = FameColors.AfroSunOrange, modifier = Modifier.size(12.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("PATIENCE PRESSURE", style = AFMTextStyles.textXS.copy(fontSize = 10.sp), color = Color.White.copy(alpha = 0.6f))
                            }
                            AFMProgressBar(
                                progress = (patiencePressure * 5) / 100f,
                                modifier = Modifier.padding(top = 4.dp),
                                color = FameColors.AfroSunOrange
                            )
                        }
                    }
                    
                    if (ambitionPressure > 0) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(12.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("AMBITION PRESSURE", style = AFMTextStyles.textXS.copy(fontSize = 10.sp), color = Color.White.copy(alpha = 0.6f))
                            }
                            AFMProgressBar(
                                progress = (ambitionPressure * 5) / 100f,
                                modifier = Modifier.padding(top = 4.dp),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ClubVisionDetailedCard(vision: ClubVisionEntity) {
    GlassPanel(modifier = Modifier.fillMaxWidth(), cornerRadius = 2) {
        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                VisionStatItem(
                    label = "PLAY STYLE",
                    value = vision.requiredPlayStyle.uppercase(),
                    icon = Icons.Default.SportsSoccer,
                    modifier = Modifier.weight(1f)
                )
                VisionStatItem(
                    label = "SIGNING POLICY",
                    value = vision.transferExpectation.uppercase(),
                    icon = Icons.Default.PersonSearch,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                VisionStatItem(
                    label = "YOUTH FOCUS",
                    value = "${vision.youthExpectation}%",
                    icon = Icons.Default.School,
                    modifier = Modifier.weight(1f)
                )
                VisionStatItem(
                    label = "REGIONAL BIASED",
                    value = if (vision.regionalDominanceRequired) "YES" else "NO",
                    icon = Icons.Default.Public,
                    modifier = Modifier.weight(1f)
                )
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("PHILOSOPHY ADHERENCE", style = AFMTextStyles.textXS, color = Color.White.copy(alpha = 0.5f), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    AFMProgressBar(
                        progress = vision.philosophyScore / 100f,
                        modifier = Modifier.fillMaxWidth(),
                        color = if (vision.philosophyScore > 70) FameColors.GrowthGreen else if (vision.philosophyScore > 40) FameColors.TrophyGold else FameColors.AlertRed
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "${vision.philosophyScore}%",
                    style = AFMTextStyles.textMD,
                    color = Color.White,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
fun VisionStatItem(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label.uppercase(), style = AFMTextStyles.textXS, color = Color.White.copy(alpha = 0.5f), fontWeight = FontWeight.Bold)
            Text(value.uppercase(), style = AFMTextStyles.textSM, color = Color.White, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun BoardRequestItem(request: BoardRequestsEntity) {
    val statusColor = when {
        request.isApproved -> FameColors.GrowthGreen
        request.isRejected -> FameColors.AlertRed
        else -> MaterialTheme.colorScheme.secondary
    }
    
    GlassPanel(modifier = Modifier.fillMaxWidth(), cornerRadius = 2) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(2.dp),
                color = statusColor.copy(alpha = 0.1f),
                border = BorderStroke(1.dp, statusColor.copy(alpha = 0.3f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (request.isApproved) Icons.Default.CheckCircle else if (request.isRejected) Icons.Default.Error else Icons.Default.Pending,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(request.requestTypeDisplay.uppercase(), style = AFMTextStyles.textSM, color = Color.White, fontWeight = FontWeight.Black)
                Text(request.requestDescription.uppercase(), style = AFMTextStyles.textXS, color = Color.White.copy(alpha = 0.5f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            
            Text(request.requestStatus.uppercase(), style = AFMTextStyles.textXS, color = statusColor, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun NewRequestDialog(onDismiss: () -> Unit, onSubmit: (String, String) -> Unit) {
    var selectedType by remember { mutableStateOf("TRANSFER_BUDGET") }
    var description by remember { mutableStateOf("") }
    val requestTypes = listOf(
        "TRANSFER_BUDGET" to "EXTRA TRANSFER BUDGET", 
        "WAGE_BUDGET" to "INCREASE WAGE CAP", 
        "STADIUM_EXPANSION" to "STADIUM UPGRADE", 
        "TRAINING_FACILITIES" to "FACILITY UPGRADE"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FameColors.HeaderDark,
        shape = RoundedCornerShape(2.dp),
        title = { Text("NEW BOARD REQUEST", style = AFMTextStyles.textLG, color = Color.White, fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                requestTypes.forEach { (type, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedType = type }
                            .padding(vertical = 4.dp), 
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedType == type, 
                            onClick = { selectedType = type }, 
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.primary,
                                unselectedColor = Color.White.copy(alpha = 0.3f)
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(label, style = AFMTextStyles.textSM, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("JUSTIFICATION", style = AFMTextStyles.textXS, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(2.dp),
                    textStyle = AFMTextStyles.textSM.copy(color = Color.White),
                    placeholder = { Text("ENTER REASONING...", style = AFMTextStyles.textXS, color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedContainerColor = Color.White.copy(alpha = 0.05f)
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(selectedType, description) }, 
                enabled = description.isNotBlank(), 
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(2.dp)
            ) {
                Text("SUBMIT", style = AFMTextStyles.textSM, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", style = AFMTextStyles.textSM, color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
            }
        }
    )
}

// ============ PREVIEWS ============

private fun getMockBoardState() = BoardUiState(
    evaluation = BoardEvaluationEntity(
        managerId = 1,
        managerName = "Tumaini Joseph",
        boardSatisfaction = 85,
        status = "Safe",
        financialStatus = "Healthy"
    ),
    objectives = listOf(
        ObjectivesEntity(
            id = 1,
            teamId = 2,
            teamName = "Young Africans SC",
            season = "2025/26",
            objectiveType = "LEAGUE",
            objective = "Win the Tanzania Premier League",
            targetValue = "1",
            rewardType = "BUDGET",
            reward = "Increased Transfer Budget",
            penaltyType = "JOB_SECURITY",
            penalty = "Reduced Job Security",
            status = "pending",
            deadline = "2026-06-01"
        ),
        ObjectivesEntity(
            id = 2,
            teamId = 2,
            teamName = "Young Africans SC",
            season = "2025/26",
            objectiveType = "CUP",
            objective = "Reach the Quarter Finals of CAF Champions League",
            targetValue = "8",
            rewardType = "REPUTATION",
            reward = "Club Reputation Boost",
            penaltyType = "NONE",
            penalty = "None",
            status = "achieved",
            deadline = "2026-05-15"
        )
    ),
    requests = listOf(
        BoardRequestsEntity(
            id = 1,
            requestType = "TRANSFER_BUDGET",
            requestDescription = "I need funds to sign a world-class striker for the continental campaign.",
            requestStatus = "Pending",
            managerId = 1,
            managerName = "Tumaini Joseph",
            teamId = 1,
            teamName = "Young Africans SC"
        )
    ),
    isLoading = false
)

@Preview(showBackground = true, name = "Board Room - Portrait")
@Composable
fun BoardScreenPreview() {
    AFM2026Theme {
        BoardScreen(
            uiState = getMockBoardState(),
            onBack = {},
            onSubmitRequest = { _, _ -> }
        )
    }
}

@Preview(showBackground = true, name = "Board Room - Landscape", device = "spec:width=1280dp,height=800dp,orientation=landscape")
@Composable
fun BoardScreenLandscapePreview() {
    AFM2026Theme {
        BoardScreen(
            uiState = getMockBoardState(),
            onBack = {},
            onSubmitRequest = { _, _ -> }
        )
    }
}
