@file:OptIn(ExperimentalMaterial3Api::class)
package com.fameafrica.afm.ui.screen.career

import android.app.DatePickerDialog
import android.view.ViewGroup
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.graphics.Brush
import com.fameafrica.afm.utils.NationalityItem
import com.fameafrica.afm.utils.FootballRegion
import com.fameafrica.afm.ui.components.common.SidebarSectionTitle
import com.fameafrica.afm.ui.components.common.SidebarBroadcastHeader
import com.fameafrica.afm.ui.components.common.SidebarCard
import androidx.compose.foundation.BorderStroke

import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.fameafrica.afm.R
import com.fameafrica.afm.data.database.entities.ChairmanEntity
import com.fameafrica.afm.data.database.entities.ClubDNAEntity
import com.fameafrica.afm.data.database.entities.FinancialBehavior
import com.fameafrica.afm.data.database.entities.LeaguesEntity
import com.fameafrica.afm.data.database.entities.ManagersEntity
import com.fameafrica.afm.data.database.entities.TeamsEntity
import com.fameafrica.afm.ui.components.career.NationalityWheel
import com.fameafrica.afm.ui.components.common.GlassPanel
import com.fameafrica.afm.ui.components.common.DualModeBackground
import com.fameafrica.afm.ui.components.common.TeamLogo
import com.fameafrica.afm.ui.theme.*
import com.fameafrica.afm.ui.components.*
import com.fameafrica.afm.utils.ImmersiveModeManager
import com.fameafrica.afm.utils.ImmersiveModeManager.immersiveRoot
import com.fameafrica.afm.utils.LeagueLogoUtils
import com.fameafrica.afm.utils.NationalityUtils
import com.github.mikephil.charting.charts.RadarChart
import com.github.mikephil.charting.data.RadarData
import com.github.mikephil.charting.data.RadarDataSet
import com.github.mikephil.charting.data.RadarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

@Composable
fun CareerSetupScreen(
    onBack: () -> Unit,
    onStartCareer: (Long) -> Unit,
    viewModel: CareerSetupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val teamCounts by viewModel.leagueTeamCounts.collectAsStateWithLifecycle(initialValue = emptyMap())

    val themePreset = FootballThemePreset.MANAGER_MODE

    ImmersiveModeManager.ImmersiveScreen()

    AFM2026Theme(themePreset = themePreset) {
        Box(modifier = Modifier.immersiveRoot().fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            CareerSetupScreenContent(
                uiState = uiState,
                teamCounts = teamCounts,
                onBack = onBack,
                onNext = { viewModel.nextStep() },
                onPrevious = { viewModel.previousStep() },
                onStartCareer = { viewModel.startCareer() },
                onSelectCountry = { viewModel.selectCountry(it) },
                onSelectLeague = { viewModel.selectLeague(it) },
                onSelectDivision = { viewModel.selectDivision(it) },
                onSelectClub = { viewModel.selectClub(it) },
                onToggleDNADialog = { viewModel.toggleDNADialog(it) },
                onToggleOwnershipDialog = { viewModel.toggleOwnershipDialog(it) },
                onUpdateManagerName = { viewModel.updateManagerName(it) },
                onUpdateManagerNationality = { viewModel.updateManagerNationality(it) },
                onUpdateBirthDate = { y, m, d -> viewModel.updateBirthDate(y, m, d) },
                onUpdateManagerStyle = { viewModel.updateManagerStyle(it) },
                onUpdateCoachingLicense = { viewModel.updateCoachingLicense(it) },
                onUpdateSpecialAbility = { viewModel.updateSpecialAbility(it) },
                onSelectAvatar = { viewModel.selectAvatar(it) },
                onUpdateAvatarRegion = { viewModel.updateAvatarRegion(it) },
                onSelectAgent = { viewModel.selectAgent(it) },
                onUpdateAgentFilters = { q, p -> viewModel.updateAgentFilters(q, p) },
                onSelectDifficulty = { viewModel.selectDifficulty(it) },
                onSelectCareerVision = { viewModel.selectCareerVision(it) },
                onSetManagerSelectionMode = { viewModel.setManagerSelectionMode(it) },
                onSelectExistingManager = { viewModel.selectExistingManager(it) },
                onProceedFromManagerSelection = { viewModel.proceedFromManagerSelection() },
                onCancelOverwrite = { viewModel.cancelOverwrite() }
            )
        }
    }

    LaunchedEffect(viewModel.events) {
        viewModel.events.collect { event ->
            when (event) {
                is CareerEvent.NavigateToDashboard -> {
                    onStartCareer(event.careerId.toLong())
                }
                is CareerEvent.ShowError -> {
                    // Logic to show a snackbar or similar could go here
                }
            }
        }
    }
}

@Composable
fun CareerSetupScreenContent(
    uiState: CareerUiState,
    teamCounts: Map<String, Int>,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onStartCareer: () -> Unit,
    onSelectCountry: (Int) -> Unit,
    onSelectLeague: (LeaguesEntity) -> Unit,
    onSelectDivision: (Int?) -> Unit,
    onSelectClub: (TeamsEntity) -> Unit,
    onToggleDNADialog: (Boolean) -> Unit,
    onToggleOwnershipDialog: (Boolean) -> Unit,
    onUpdateManagerName: (String) -> Unit,
    onUpdateManagerNationality: (String) -> Unit,
    onUpdateBirthDate: (Int, Int, Int) -> Unit,
    onUpdateManagerStyle: (ManagerialStyle) -> Unit,
    onUpdateCoachingLicense: (CoachingLicense) -> Unit,
    onUpdateSpecialAbility: (String) -> Unit,
    onSelectAvatar: (String) -> Unit,
    onUpdateAvatarRegion: (String) -> Unit,
    onSelectAgent: (Agent) -> Unit,
    onUpdateAgentFilters: (String?, String?) -> Unit,
    onSelectDifficulty: (Difficulty) -> Unit,
    onSelectCareerVision: (CareerVision) -> Unit,
    onSetManagerSelectionMode: (ManagerSelectionMode) -> Unit,
    onSelectExistingManager: (ManagersEntity) -> Unit,
    onProceedFromManagerSelection: () -> Unit,
    onCancelOverwrite: () -> Unit
) {
    if (uiState.showOverwriteWarning) {
        AlertDialog(
            onDismissRequest = onCancelOverwrite,
            title = { Text("OVERWRITE SAVE?", style = AFMTextStyles.textMD, color = FameColors.AlertRed) },
            text = { Text("Starting a new career will permanently overwrite your current save. This action cannot be undone.", style = AFMTextStyles.textSM) },
            confirmButton = {
                TextButton(onClick = onStartCareer) {
                    Text("OVERWRITE", color = FameColors.AlertRed, fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = onCancelOverwrite) {
                    Text("CANCEL")
                }
            },
            containerColor = Color(0xFF1E1E1E),
            titleContentColor = Color.White,
            textContentColor = Color.White
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val activeColor = MaterialTheme.colorScheme.primary
        Scaffold(
            topBar = {
                if (uiState.currentStep != CareerStep.PREPARING_GAME) {
                    SidebarBroadcastHeader(
                        title = "NEW CAREER",
                        icon = Icons.Default.History,
                        subtitle = when(uiState.currentStep) {
                            CareerStep.MANAGER_SELECTION -> "CHOOSE YOUR PATH"
                            CareerStep.MANAGER_CREATION -> "PERSONAL PROFILE"
                            CareerStep.AVATAR_CUSTOMIZATION -> "IDENTITY"
                            CareerStep.AGENT_SELECTION -> "REPRESENTATION"
                            CareerStep.CAREER_VISION -> "STRATEGIC VISION"
                            CareerStep.COUNTRY_SELECTION -> "LOCATION"
                            CareerStep.LEAGUE_SELECTION -> "TIER SELECTION"
                            CareerStep.CLUB_SELECTION -> "CLUB APPOINTMENT"
                            CareerStep.DIFFICULTY_SELECTION -> "CHALLENGE LEVEL"
                            CareerStep.CAREER_SUMMARY -> "FINAL REVIEW"
                            else -> null
                        }?.uppercase(),
                        actions = {
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = activeColor)
                            }
                        }
                    )
                }
            },
            containerColor = Color.Transparent
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .consumeWindowInsets(padding)
                    .fillMaxSize()
            ) {
                if (uiState.currentStep != CareerStep.INITIALIZING && uiState.currentStep != CareerStep.PREPARING_GAME) {
                    StepProgressBar(uiState = uiState)
                }

                Box(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        if (uiState.errorMessage != null) {
                            ErrorMessage(uiState.errorMessage)
                        }
                        
                        AnimatedContent(
                            targetState = uiState.currentStep,
                            modifier = Modifier.weight(1f),
                            transitionSpec = { fadeIn(animationSpec = tween(400)) togetherWith fadeOut(animationSpec = tween(400)) },
                            label = "StepTransition"
                        ) { step ->
                            when (step) {
                                CareerStep.INITIALIZING -> InitializationStep(uiState)
                                CareerStep.MANAGER_SELECTION -> ManagerSelectionStepContent(uiState, onSetManagerSelectionMode)
                                CareerStep.EXISTING_MANAGER_SELECTION -> ExistingManagerSelectionStep(uiState, onSelectExistingManager)
                                CareerStep.MANAGER_CREATION -> ManagerCreationStep(
                                    uiState = uiState,
                                    onUpdateName = onUpdateManagerName,
                                    onUpdateNationality = onUpdateManagerNationality,
                                    onUpdateBirthDate = onUpdateBirthDate,
                                    onUpdateStyle = onUpdateManagerStyle,
                                    onUpdateLicense = onUpdateCoachingLicense,
                                    onUpdateSpecialAbility = onUpdateSpecialAbility
                                )
                                CareerStep.AVATAR_CUSTOMIZATION -> AvatarSelectionStep(uiState, onSelectAvatar, onUpdateAvatarRegion)
                                CareerStep.AGENT_SELECTION -> AgentSelectionStep(uiState, onSelectAgent, onUpdateAgentFilters)
                                CareerStep.CAREER_VISION -> CareerVisionSelectionStep(uiState, onSelectCareerVision)
                                CareerStep.COUNTRY_SELECTION -> CountrySelectionStep(uiState, onSelectCountry)
                                CareerStep.LEAGUE_SELECTION -> LeagueSelectionStep(uiState, teamCounts, onSelectLeague)
                                CareerStep.CLUB_SELECTION -> ClubSelectionStep(
                                    uiState,
                                    onSelectClub,
                                    onToggleDNADialog,
                                    onToggleOwnershipDialog
                                )
                                CareerStep.DIFFICULTY_SELECTION -> DifficultyStep(uiState, onSelectDifficulty)
                                CareerStep.CAREER_SUMMARY -> CareerSummaryStep(uiState)
                                CareerStep.PREPARING_GAME -> PreparingGameStep(uiState)
                                else -> {} // Safety fallback for removed steps
                            }
                        }
                    }
                }

                if (uiState.currentStep != CareerStep.INITIALIZING && uiState.currentStep != CareerStep.PREPARING_GAME) {
                    Box(modifier = Modifier.padding(16.dp).navigationBarsPadding()) {
                        NavigationButtons(uiState, onBack, onNext, onPrevious, onStartCareer)
                    }
                }
            }
        }

        if (uiState.isLoading && uiState.currentStep != CareerStep.INITIALIZING && uiState.currentStep != CareerStep.PREPARING_GAME) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)).clickable(enabled = false) { },
                contentAlignment = Alignment.Center
            ) {
                GlassPanel(modifier = Modifier.padding(32.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Processing...", style = AFMTextStyles.tableCell, color = FameColors.WarmIvory)
                    }
                }
            }
        }
    }
}

@Composable
fun ErrorMessage(message: String) {
    SidebarCard(
        modifier = Modifier.fillMaxWidth().padding(16.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Error, null, tint = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.width(12.dp))
            Text(message.uppercase(), style = AFMTextStyles.textXS, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
fun PreparingGameStep(uiState: CareerUiState) {
    val animatedProgress by animateFloatAsState(targetValue = uiState.preparationProgress, label = "prepProgress")

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SidebarCard {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val accentColor = MaterialTheme.colorScheme.primary
                val icon = Icons.Default.SportsSoccer
                val title = "PREPARING OFFICE"

                Icon(
                    icon,
                    null,
                    modifier = Modifier.size(48.dp),
                    tint = accentColor
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    title,
                    style = AFMTextStyles.textMD,
                    color = accentColor,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    uiState.preparationStatus.uppercase(),
                    style = AFMTextStyles.textXS,
                    color = Color.White.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))

                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { animatedProgress.coerceIn(0f, 1f) },
                        modifier = Modifier.size(80.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.dp,
                        trackColor = Color.White.copy(alpha = 0.05f),
                    )
                    Text(
                        "${(animatedProgress * 100).toInt()}%",
                        style = AFMTextStyles.textSM,
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Welcome Message Preview - Denser
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(2.dp),
                    color = Color.White.copy(alpha = 0.02f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Email, null, modifier = Modifier.size(12.dp), tint = accentColor)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("INBOX: SECURE CHANNEL", style = AFMTextStyles.textXS.copy(fontSize = 8.sp), color = accentColor, fontWeight = FontWeight.Black)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "CONTRACT DOCUMENTS: ${uiState.selectedClub?.name ?: "CLUB"}".uppercase(),
                            style = AFMTextStyles.textXS,
                            color = Color.White,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StepProgressBar(uiState: CareerUiState) {
    val currentStep = uiState.currentStep
    val steps = CareerStep.entries.filter { it != CareerStep.INITIALIZING && it != CareerStep.PREPARING_GAME }
    val currentIndex = steps.indexOf(currentStep)

    // Mode-specific color: Green for Manager, Gold for Chairman
    val activeColor = MaterialTheme.colorScheme.primary

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        steps.forEachIndexed { index, _ ->
            val color = when {
                index < currentIndex -> activeColor.copy(alpha = 0.6f)
                index == currentIndex -> activeColor
                else -> Color.White.copy(alpha = 0.05f)
            }
            Box(modifier = Modifier.weight(1f).height(2.dp).background(color))
        }
    }
}

@Composable
fun InitializationStep(uiState: CareerUiState) {
    val animatedProgress by animateFloatAsState(targetValue = uiState.initializationProgress, label = "initProgress")
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SidebarCard {
            Column(modifier = Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.SportsSoccer, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(24.dp))
                Text("PREPARING THE UNIVERSE", style = AFMTextStyles.textLG, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(8.dp))
                Text(uiState.initializationStatus.uppercase(), style = AFMTextStyles.textXS, color = Color.White.copy(alpha = 0.6f), textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(32.dp))
                LinearProgressIndicator(
                    progress = { animatedProgress.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(2.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = Color.White.copy(alpha = 0.05f),
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("${(animatedProgress * 100).toInt()}%", style = AFMTextStyles.playerRating, color = Color.White)
            }
        }
    }
}

@Composable
fun ManagerSelectionStepContent(
    uiState: CareerUiState,
    onSetManagerSelectionMode: (ManagerSelectionMode) -> Unit
) {
    DualModeBackground(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    "MANAGER PROFILE",
                    style = AFMTextStyles.textLG.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    ),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "START FRESH OR CONTINUE A LEGACY".uppercase(),
                    style = AFMTextStyles.textXS.copy(letterSpacing = 1.sp),
                    color = FameColors.TrophyGold,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            Row(
                modifier = Modifier.fillMaxWidth().height(280.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                SelectionModeHeroCard(
                    title = "NEW",
                    subtitle = "Fresh Start",
                    description = "Create a brand new manager profile.",
                    technicalDetails = listOf("Customization: FULL", "Legacy: ZERO", "XP Gain: 1.2x"),
                    icon = Icons.Default.Add,
                    color = MaterialTheme.colorScheme.primary,
                    isSelected = uiState.managerSelectionMode == ManagerSelectionMode.CREATE_NEW,
                    modifier = Modifier.weight(1f)
                ) {
                    onSetManagerSelectionMode(ManagerSelectionMode.CREATE_NEW)
                }

                SelectionModeHeroCard(
                    title = "EXISTING",
                    subtitle = "Legacy",
                    description = "Select a pre-defined profile.",
                    technicalDetails = listOf("Reputation: VARIES", "Experience: HIGH", "XP Gain: 1.0x"),
                    icon = Icons.Default.Person,
                    color = FameColors.TrophyGold,
                    isSelected = uiState.managerSelectionMode == ManagerSelectionMode.USE_EXISTING,
                    modifier = Modifier.weight(1f)
                ) {
                    onSetManagerSelectionMode(ManagerSelectionMode.USE_EXISTING)
                }
            }
        }
    }
}

@Composable
fun ExistingManagerSelectionStep(
    uiState: CareerUiState,
    onSelect: (ManagersEntity) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        SidebarSectionTitle("SELECT PRE-DEFINED PROFILE", modifier = Modifier.padding(bottom = 16.dp))
        
        if (uiState.isLoadingManagers) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = FameColors.TrophyGold)
            }
        } else if (uiState.availableManagers.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("NO LEGACY PROFILES FOUND", style = AFMTextStyles.textSM, color = Color.White.copy(alpha = 0.4f))
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(uiState.availableManagers) { manager ->
                    ManagerProfileCard(
                        manager = manager,
                        isSelected = uiState.selectedExistingManager?.id == manager.id,
                        onClick = { onSelect(manager) }
                    )
                }
            }
        }
    }
}

@Composable
fun SelectionModeHeroCard(
    title: String,
    subtitle: String,
    description: String,
    technicalDetails: List<String>,
    icon: ImageVector,
    color: Color,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxHeight(),
        shape = RoundedCornerShape(2.dp),
        color = Color.Black.copy(alpha = 0.6f),
        border = BorderStroke(
            if (isSelected) 2.dp else 1.dp,
            if (isSelected) color else Color.White.copy(alpha = 0.05f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Smaller Icon
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(2.dp),
                color = color.copy(alpha = 0.1f),
                border = BorderStroke(1.dp, color.copy(alpha = 0.4f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Compact Title - LG (16sp)
            Text(
                text = title,
                style = AFMTextStyles.textLG.copy(fontWeight = FontWeight.Black, letterSpacing = 2.sp),
                color = Color.White
            )

            // Subtitle - SM (12sp)
            Text(
                text = subtitle.uppercase(),
                style = AFMTextStyles.textSM.copy(fontWeight = FontWeight.Bold),
                color = color
            )

            // Description - XS (11sp)
            Text(
                text = description.uppercase(),
                style = AFMTextStyles.textXS.copy(lineHeight = 14.sp),
                color = Color.White.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Technical details - High Density Info
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                technicalDetails.forEach { detail ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(4.dp).background(color))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = detail.uppercase(),
                            style = AFMTextStyles.textXS.copy(fontSize = 9.sp),
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(color)
                )
            }
        }
    }
}

@Composable
fun AgentSelectionStep(
    uiState: CareerUiState,
    onSelectAgent: (Agent) -> Unit,
    onUpdateFilters: (String?, String?) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        SidebarSectionTitle("SELECT YOUR REPRESENTATIVE", modifier = Modifier.padding(bottom = 16.dp))
        
        // Search and Filter Bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = uiState.agentSearchQuery,
                onValueChange = { onUpdateFilters(it, null) },
                modifier = Modifier.weight(1f).height(48.dp),
                placeholder = { Text("Search agents...", style = AFMTextStyles.textXS) },
                leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(16.dp)) },
                shape = RoundedCornerShape(2.dp),
                singleLine = true,
                textStyle = AFMTextStyles.textXS,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                    focusedBorderColor = FameColors.TrophyGold
                )
            )
        }
        
        // Personality Tabs
        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val personalities = listOf("ALL", "Shark", "Loyal", "Hard Bargainer", "Networker")
            items(personalities) { p ->
                val isSelected = if (p == "ALL") uiState.agentPersonalityFilter == null else uiState.agentPersonalityFilter == p
                FilterChip(
                    selected = isSelected,
                    onClick = { onUpdateFilters(null, p) },
                    label = { Text(p.uppercase(), style = AFMTextStyles.textXXS, fontWeight = FontWeight.Black) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = FameColors.TrophyGold,
                        selectedLabelColor = Color.Black,
                        containerColor = Color.White.copy(alpha = 0.05f),
                        labelColor = Color.White
                    ),
                    shape = RoundedCornerShape(2.dp),
                    border = BorderStroke(1.dp, if (isSelected) FameColors.TrophyGold else Color.White.copy(alpha = 0.1f))
                )
            }
        }
        
        if (uiState.isLoadingAgents) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = FameColors.TrophyGold)
            }
        } else if (uiState.filteredAgents.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("NO AGENTS MATCHING FILTERS", style = AFMTextStyles.textSM, color = Color.White.copy(alpha = 0.4f))
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(uiState.filteredAgents) { agent ->
                    AgentCard(
                        agent = agent,
                        isSelected = uiState.assignedAgent?.id == agent.id,
                        onSelect = { onSelectAgent(agent) }
                    )
                }
            }
        }
    }
}

@Composable
fun AgentCard(agent: Agent, isSelected: Boolean, onSelect: () -> Unit) {
    val activeColor = FameColors.TrophyGold
    val agentFace = com.fameafrica.afm.utils.AgentAssetUtils.getAgentFace(agent.id, agent.nationality)
    
    SidebarCard(
        modifier = Modifier.fillMaxWidth().clickable { onSelect() }
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(64.dp),
                shape = RoundedCornerShape(2.dp),
                color = Color.Black.copy(alpha = 0.3f),
                border = BorderStroke(1.dp, if (isSelected) activeColor else Color.White.copy(alpha = 0.1f))
            ) {
                AsyncImage(
                    model = agentFace,
                    contentDescription = agent.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.default_manager),
                    error = painterResource(R.drawable.default_manager)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(agent.name.uppercase(), style = AFMTextStyles.textSM, color = Color.White, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    AsyncImage(
                        model = agent.nationalityFlag,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp, 10.dp),
                        contentScale = ContentScale.Fit
                    )
                }
                Text(agent.agency.uppercase(), style = AFMTextStyles.textXXS, color = activeColor, fontWeight = FontWeight.Black)
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AgentStat("SKILL", agent.negotiationSkill.toString())
                    AgentStat("FEE", "${(agent.commissionRate * 100).toInt()}%")
                    AgentStat("STYLE", agent.personality.uppercase())
                }
            }
            
            if (isSelected) {
                Icon(Icons.Default.CheckCircle, null, tint = activeColor, modifier = Modifier.size(24.dp))
            }
        }
    }
}

@Composable
fun AgentStat(label: String, value: String) {
    Column {
        Text(label.uppercase(), style = AFMTextStyles.textXS.copy(fontSize = 7.sp), color = Color.White.copy(alpha = 0.4f))
        Text(value, style = AFMTextStyles.textXS, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun AvatarSelectionStep(
    uiState: CareerUiState,
    onSelectAvatar: (String) -> Unit,
    onUpdateRegion: (String) -> Unit
) {
    val regions = listOf("EAST_AFRICA", "WEST_AFRICA", "NORTH_AFRICA", "SOUTHERN_AFRICA", "CENTRAL_AFRICA")
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("CHOOSE YOUR IDENTITY".uppercase(), style = AFMTextStyles.textLG, color = FameColors.TrophyGold, fontWeight = FontWeight.Black)
        Text("Select a region to browse available avatars".uppercase(), style = AFMTextStyles.textXS, color = Color.White.copy(alpha = 0.6f))

        Spacer(modifier = Modifier.height(16.dp))
        
        // Region Tabs
        ScrollableTabRow(
            selectedTabIndex = regions.indexOf(uiState.selectedAvatarRegion),
            containerColor = Color.Transparent,
            contentColor = FameColors.TrophyGold,
            edgePadding = 0.dp,
            divider = {},
            indicator = { tabPositions ->
                Box(
                    modifier = Modifier
                        .tabIndicatorOffset(tabPositions[regions.indexOf(uiState.selectedAvatarRegion)])
                        .height(2.dp)
                        .background(FameColors.TrophyGold)
                )
            }
        ) {
            regions.forEach { region ->
                Tab(
                    selected = uiState.selectedAvatarRegion == region,
                    onClick = { onUpdateRegion(region) },
                    text = { Text(region.replace("_", " "), style = AFMTextStyles.textXXS, fontWeight = FontWeight.Black) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Deterministic Pool based on region (matching ManagerAssetUtils logic)
        val avatars = remember(uiState.selectedAvatarRegion) {
            val prefix = when(uiState.selectedAvatarRegion) {
                "SOUTHERN_AFRICA" -> "south_africa"
                else -> uiState.selectedAvatarRegion.lowercase()
            }
            
            val (r1, r2) = when(uiState.selectedAvatarRegion) {
                "NORTH_AFRICA" -> 4 to 4
                "WEST_AFRICA" -> 5 to 4
                "CENTRAL_AFRICA" -> 3 to 4
                "SOUTHERN_AFRICA" -> 4 to 4
                else -> 6 to 4 // EAST
            }
            val list = mutableListOf<String>()
            // Pattern 1: prefix_i_j.jpg
            for (i in 1..r1) {
                for (j in 1..r2) {
                    list.add("file:///android_asset/manager_faces/${prefix}_${i}_${j}.webp")
                }
            }
            // Pattern 2: prefixX_Y_Z.jpg
            for (i in 1..r1) {
                for (j in 1..r2) {
                    list.add("file:///android_asset/manager_faces/${prefix}${i}_1_${j}.webp")
                }
            }
            list.distinct()
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(avatars) { avatarUrl ->
                val isSelected = uiState.selectedAvatar == avatarUrl
                Surface(
                    onClick = { onSelectAvatar(avatarUrl) },
                    modifier = Modifier.aspectRatio(1f),
                    shape = RoundedCornerShape(2.dp),
                    color = if (isSelected) FameColors.TrophyGold.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.3f),
                    border = BorderStroke(1.dp, if (isSelected) FameColors.TrophyGold else Color.White.copy(alpha = 0.1f))
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = avatarUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(R.drawable.default_manager),
                            error = painterResource(R.drawable.default_manager)
                        )
                        if (isSelected) {
                            Icon(
                                Icons.Default.CheckCircle,
                                null,
                                tint = FameColors.TrophyGold,
                                modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun AvatarCard(drawableName: String, isSelected: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "lightTransition")
    val borderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    val context = LocalContext.current
    val resourceId = context.resources.getIdentifier(drawableName, "drawable", context.packageName)
    val activeColor = MaterialTheme.colorScheme.primary

    Surface(
        modifier = Modifier.size(200.dp),
        shape = RoundedCornerShape(2.dp),
        color = if (isSelected) FameColors.HeaderDark else FameColors.DeepNavyBlack,
        border = BorderStroke(1.dp, if (isSelected) activeColor.copy(alpha = borderAlpha) else Color.White.copy(alpha = 0.1f))
    ) {
        Box(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            if (resourceId != 0) {
                Image(
                    painter = painterResource(id = resourceId),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(2.dp)),
                    contentScale = ContentScale.Fit
                )
            } else {
                Icon(Icons.Default.Person, null, modifier = Modifier.size(100.dp), tint = activeColor)
            }
        }
    }
}

@Composable
fun SelectionModeCard(title: String, subtitle: String, icon: ImageVector, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.height(140.dp).clickable { onClick() },
        shape = RoundedCornerShape(2.dp),
        color = if (isSelected) FameColors.HeaderDark else FameColors.DeepNavyBlack,
        border = BorderStroke(1.dp, if (isSelected) FameColors.TrophyGold else Color.White.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(2.dp),
                color = Color.Black.copy(alpha = 0.3f),
                border = BorderStroke(1.dp, if (isSelected) FameColors.TrophyGold else Color.White.copy(alpha = 0.1f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = if (isSelected) FameColors.TrophyGold else FameColors.MutedParchment, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(title.uppercase(), style = AFMTextStyles.textSM, color = if (isSelected) FameColors.WarmIvory else FameColors.MutedParchment, fontWeight = FontWeight.Black)
            Text(subtitle.uppercase(), style = AFMTextStyles.textXS, color = FameColors.TrophyGold, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun NewManagerPlaceholder() {
    val activeColor = MaterialTheme.colorScheme.primary
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(120.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.PersonAdd, null, modifier = Modifier.size(56.dp), tint = activeColor)
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text("FORGE A NEW LEGEND", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(12.dp))
        GlassPanel(modifier = Modifier.fillMaxWidth()) {
            Column {
                InfoPoint(Icons.Default.Edit, "Personalize your manager's identity.")
                InfoPoint(Icons.Default.Psychology, "Choose a unique coaching philosophy.")
                InfoPoint(Icons.Default.School, "Earn your CAF coaching licenses.")
            }
        }
    }
}

@Composable
fun InfoPoint(icon: ImageVector, text: String) {
    val activeColor = MaterialTheme.colorScheme.primary
    Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(
            modifier = Modifier.size(24.dp),
            shape = RoundedCornerShape(2.dp),
            color = Color.Black.copy(alpha = 0.3f),
            border = BorderStroke(1.dp, activeColor.copy(alpha = 0.3f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, modifier = Modifier.size(14.dp), tint = activeColor)
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(text.uppercase(), style = AFMTextStyles.textXS, color = Color.White)
    }
}

@Composable
fun ManagerProfileCard(manager: ManagersEntity, isSelected: Boolean, onClick: () -> Unit) {
    val context = LocalContext.current
    val resourceId = if (manager.faceImage != null) context.resources.getIdentifier(manager.faceImage, "drawable", context.packageName) else 0
    val activeColor = FameColors.TrophyGold

    SidebarCard(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        borderColor = if (isSelected) activeColor else Color.White.copy(alpha = 0.1f)
    ) {
        Row(modifier = Modifier.padding(12.dp).height(IntrinsicSize.Min), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(80.dp),
                shape = RoundedCornerShape(2.dp),
                color = Color.Black.copy(alpha = 0.3f),
                border = BorderStroke(1.dp, if (isSelected) activeColor else Color.White.copy(alpha = 0.1f))
            ) {
                Image(
                    painter = if (resourceId != 0) painterResource(id = resourceId) else painterResource(id = R.drawable.default_manager),
                    null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.width(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(manager.name.uppercase(), style = AFMTextStyles.textMD, color = Color.White, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(manager.style.uppercase(), style = AFMTextStyles.textXS, color = activeColor, fontWeight = FontWeight.Bold)
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatMetric("WIN RATE", "${manager.winPercentage.toInt()}%", Icons.AutoMirrored.Filled.TrendingUp, modifier = Modifier.weight(1f))
                    StatMetric("TROPHIES", "${manager.trophiesWon}", Icons.Default.EmojiEvents, modifier = Modifier.weight(1f))
                    StatMetric("REP", manager.reputationLevel, Icons.Default.Star, modifier = Modifier.weight(1.3f))
                }
            }
            if (isSelected) {
                Icon(Icons.Default.CheckCircle, null, tint = activeColor, modifier = Modifier.size(24.dp))
            }
        }
    }
}

@Composable
fun ClubDNADialog(dna: ClubDNAEntity?, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FameColors.HeaderDark,
        shape = RoundedCornerShape(4.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Hub, null, tint = FameColors.TrophyGold)
                Spacer(modifier = Modifier.width(8.dp))
                Text("CLUB DNA", style = AFMTextStyles.textMD, color = Color.White, fontWeight = FontWeight.Black)
            }
        },
        text = {
            Column {
                if (dna == null) {
                    Text("DNA PROFILE NOT DISCOVERED YET", style = AFMTextStyles.textXS, color = Color.White.copy(alpha = 0.5f))
                } else {
                    DNAMetric("PLAY STYLE", dna.playStyle)
                    DNAMetric("TRANSFER POLICY", dna.transferPolicy)
                    DNAMetric("YOUTH FOCUS", "${dna.youthPriority}%")
                    DNAMetric("IDENTITY STRENGTH", "${dna.identityStrength}%")
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "DNA defines how the club operates, the types of players they sign, and their football philosophy.",
                        style = AFMTextStyles.textXS,
                        color = Color.White.copy(alpha = 0.4f)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("CLOSE", color = FameColors.TrophyGold, fontWeight = FontWeight.Black)
            }
        }
    )
}

@Composable
fun DNAMetric(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(label.uppercase(), style = AFMTextStyles.textXS, color = Color.White.copy(alpha = 0.4f), fontSize = 8.sp)
        Text(value.replace("_", " ").uppercase(), style = AFMTextStyles.textSM, color = FameColors.TrophyGold, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ClubOwnershipDialog(chairman: ChairmanEntity?, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FameColors.HeaderDark,
        shape = RoundedCornerShape(4.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccountBalance, null, tint = FameColors.TrophyGold)
                Spacer(modifier = Modifier.width(8.dp))
                Text("CLUB PRESIDENT", style = AFMTextStyles.textMD, color = Color.White, fontWeight = FontWeight.Black)
            }
        },
        text = {
            Column {
                if (chairman == null) {
                    Text("OWNERSHIP INFORMATION CLASSIFIED", style = AFMTextStyles.textXS, color = Color.White.copy(alpha = 0.5f))
                } else {
                    Text(chairman.name.uppercase(), style = AFMTextStyles.textSM, color = Color.White, fontWeight = FontWeight.Black)
                    Text(chairman.personalityType?.uppercase() ?: "STABLE OWNER", style = AFMTextStyles.textXS, color = FameColors.TrophyGold, fontWeight = FontWeight.Bold)
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    AttributeRow("WEALTH", chairman.wealthLevel)
                    AttributeRow("AMBITION", chairman.ambitionLevel)
                    AttributeRow("PATIENCE", chairman.patienceLevel)
                    AttributeRow("KNOWLEDGE", chairman.footballKnowledge)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "The President's attributes affect your budget, job security, and overall club vision.",
                        style = AFMTextStyles.textXS,
                        color = Color.White.copy(alpha = 0.4f)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("CLOSE", color = FameColors.TrophyGold, fontWeight = FontWeight.Black)
            }
        }
    )
}

@Composable
fun RatingStars(elo: Int, maxStars: Int = 5) {
    val stars = when {
        elo >= 1700 -> 5
        elo >= 1600 -> 4
        elo >= 1500 -> 3
        elo >= 1400 -> 2
        else -> 1
    }
    Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
        repeat(maxStars) { index ->
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = if (index < stars) FameColors.TrophyGold else Color.White.copy(alpha = 0.1f),
                modifier = Modifier.size(10.dp)
            )
        }
    }
}

@Composable
fun StatMetric(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Surface(
            modifier = Modifier.size(18.dp),
            shape = RoundedCornerShape(2.dp),
            color = Color.Black.copy(alpha = 0.3f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, modifier = Modifier.size(10.dp), tint = Color.White.copy(alpha = 0.4f))
            }
        }
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(
                label.uppercase(),
                style = AFMTextStyles.textXXS.copy(fontSize = 7.sp),
                color = Color.White.copy(alpha = 0.4f),
                maxLines = 1
            )
            if (label == "REP") {
                ReputationBadge(level = value, modifier = Modifier.padding(top = 1.dp))
            } else {
                Text(
                    value.uppercase(),
                    style = if (value.length > 10) AFMTextStyles.textXS else AFMTextStyles.textSM,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    lineHeight = 11.sp
                )
            }
        }
    }
}

@Composable
fun CountrySelectionStep(uiState: CareerUiState, onSelectCountry: (Int) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        SidebarSectionTitle("SELECT NATION", modifier = Modifier.padding(bottom = 16.dp))
        LazyVerticalGrid(columns = GridCells.Fixed(2), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
            items(PLAYABLE_COUNTRIES) { country ->
                CountryChip(country.id, uiState.selectedCountryId == country.id, false) { onSelectCountry(country.id) }
            }
        }
    }
}

@Composable
fun ClubSelectionStep(
    uiState: CareerUiState,
    onSelectClub: (TeamsEntity) -> Unit,
    onToggleDNADialog: (Boolean) -> Unit,
    onToggleOwnershipDialog: (Boolean) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        SidebarSectionTitle("SELECT CLUB", modifier = Modifier.padding(bottom = 16.dp))
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(uiState.filteredClubs) { club ->
                ClubSelectionCard(
                    club = club,
                    isSelected = uiState.selectedClub?.id == club.id,
                    onSelect = { onSelectClub(club) },
                    onShowDNA = { onToggleDNADialog(true) },
                    onShowOwnership = { onToggleOwnershipDialog(true) }
                )
            }
        }
    }
    
    if (uiState.showDNADialog) {
        ClubDNADialog(dna = uiState.selectedClubDNA) { onToggleDNADialog(false) }
    }
    
    if (uiState.showOwnershipDialog) {
        ClubOwnershipDialog(chairman = uiState.selectedClubChairman) { onToggleOwnershipDialog(false) }
    }
}

@Composable
fun ClubSelectionCard(
    club: TeamsEntity,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onShowDNA: () -> Unit,
    onShowOwnership: () -> Unit
) {
    val activeColor = FameColors.TrophyGold
    
    val backgroundColor = when {
        club.eloRating >= 1700 -> FameColors.TrophyGold.copy(alpha = 0.12f)
        club.eloRating >= 1550 -> Color.White.copy(alpha = 0.08f)
        club.eloRating >= 1400 -> Color(0xFFCD7F32).copy(alpha = 0.12f) // Bronze
        else -> Color(0xFFAA1B2D).copy(alpha = 0.15f) // Kente Red
    }
    
    val borderColor = when {
        club.eloRating >= 1700 -> FameColors.TrophyGold.copy(alpha = 0.4f)
        club.eloRating >= 1550 -> Color.White.copy(alpha = 0.3f)
        club.eloRating >= 1400 -> Color(0xFFCD7F32).copy(alpha = 0.4f)
        else -> Color(0xFFAA1B2D).copy(alpha = 0.4f)
    }

    SidebarCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        backgroundColor = backgroundColor,
        borderColor = if (isSelected) activeColor else borderColor
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(2.dp),
                    color = Color.Black.copy(alpha = 0.3f),
                    border = BorderStroke(1.dp, if (isSelected) activeColor else Color.White.copy(alpha = 0.1f))
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(8.dp)) {
                        TeamLogo(
                            teamName = club.name,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        club.name.uppercase(),
                        style = AFMTextStyles.textSM,
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )
                    RatingStars(elo = club.eloRating)
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ClubBriefStat("ATT", club.avgAttackingAbility?.toInt()?.toString() ?: "50")
                        ClubBriefStat("MID", club.avgPlaymakingAbility?.toInt()?.toString() ?: "50")
                        ClubBriefStat("DEF", club.avgDefenceAbility?.toInt()?.toString() ?: "50")
                    }
                }
                
                if (isSelected) {
                    Icon(Icons.Default.CheckCircle, null, tint = activeColor, modifier = Modifier.size(24.dp))
                }
            }
            
            if (isSelected) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onShowDNA,
                        modifier = Modifier.weight(1f).height(32.dp),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.05f),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(2.dp)
                    ) {
                        Icon(Icons.Default.Hub, null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("CLUB DNA", style = AFMTextStyles.textXS.copy(fontSize = 9.sp))
                    }
                    
                    Button(
                        onClick = onShowOwnership,
                        modifier = Modifier.weight(1f).height(32.dp),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.05f),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(2.dp)
                    ) {
                        Icon(Icons.Default.AccountBalance, null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("OWNERSHIP", style = AFMTextStyles.textXS.copy(fontSize = 9.sp))
                    }
                }
            }
        }
    }
}

@Composable
fun ClubBriefStat(label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = AFMTextStyles.textXS.copy(fontSize = 7.sp, color = Color.White.copy(alpha = 0.4f)))
        Spacer(modifier = Modifier.width(4.dp))
        Text(value, style = AFMTextStyles.textXS.copy(fontWeight = FontWeight.Bold), color = Color.White)
    }
}

@Composable
fun LeagueSelectionStep(uiState: CareerUiState, teamCounts: Map<String, Int>, onSelectLeague: (LeaguesEntity) -> Unit) {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        SidebarSectionTitle("SELECT LEAGUE", modifier = Modifier.padding(bottom = 16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(vertical = 16.dp)) {
            items(uiState.availableLeagues) { league ->
                val isSelected = uiState.selectedLeague?.id == league.id
                val leagueLogo = LeagueLogoUtils.getLeagueLogo(context, league.name)
                
                SidebarCard(
                    modifier = Modifier.fillMaxWidth().clickable { onSelectLeague(league) }
                ) {
                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(56.dp),
                            shape = RoundedCornerShape(2.dp),
                            color = Color.Black.copy(alpha = 0.3f),
                            border = BorderStroke(1.dp, if (isSelected) FameColors.TrophyGold else Color.White.copy(alpha = 0.1f))
                        ) {
                            when (leagueLogo) {
                                is Int -> Icon(painterResource(leagueLogo), null, tint = Color.Unspecified, modifier = Modifier.padding(8.dp))
                                is String -> AsyncImage(model = leagueLogo, contentDescription = null, modifier = Modifier.padding(8.dp))
                                else -> Icon(Icons.Default.EmojiEvents, null, tint = FameColors.TrophyGold, modifier = Modifier.padding(8.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(league.name.uppercase(), style = AFMTextStyles.textSM, color = FameColors.WarmIvory, fontWeight = FontWeight.Black)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = league.country?.uppercase() ?: "TANZANIA",
                                    style = AFMTextStyles.textXS,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                                Text(" • ", color = Color.White.copy(alpha = 0.3f))
                                Text("${teamCounts[league.name] ?: 0} CLUBS".uppercase(), style = AFMTextStyles.textXS, color = FameColors.TrophyGold, fontWeight = FontWeight.Bold)
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("PRIZE FUND", style = AFMTextStyles.textXS.copy(fontSize = 7.sp, color = Color.White.copy(alpha = 0.4f)))
                            Text("${league.prizeMoney / 1000000}M TZS", style = AFMTextStyles.textXS.copy(fontWeight = FontWeight.Bold), color = FameColors.GrowthGreen)
                        }
                        if (isSelected) {
                            Spacer(modifier = Modifier.width(12.dp))
                            Surface(
                                modifier = Modifier.size(20.dp),
                                shape = RoundedCornerShape(2.dp),
                                color = FameColors.TrophyGold,
                                contentColor = FameColors.DeepNavyBlack
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Check, null, modifier = Modifier.size(12.dp))
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
fun CountryChip(countryId: Int, isSelected: Boolean, isLocked: Boolean, onClick: () -> Unit) {
    val country = PLAYABLE_COUNTRIES.find { it.id == countryId }
    SidebarCard(
        modifier = Modifier.fillMaxWidth().height(64.dp).clickable(enabled = !isLocked) { onClick() }
    ) {
        Row(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = RoundedCornerShape(2.dp),
                color = Color.Black.copy(alpha = 0.3f),
                border = BorderStroke(1.dp, if (isSelected) FameColors.TrophyGold else Color.White.copy(alpha = 0.1f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    getCountryIcon(countryId)?.let { Image(painter = painterResource(it), null, modifier = Modifier.fillMaxSize().padding(4.dp)) }
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(country?.displayName?.uppercase() ?: "NATION", style = AFMTextStyles.textXS, color = if (isLocked) FameColors.DisabledText else FameColors.WarmIvory, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            if (isSelected) {
                Surface(
                    modifier = Modifier.size(20.dp),
                    shape = RoundedCornerShape(2.dp),
                    color = FameColors.TrophyGold,
                    contentColor = FameColors.DeepNavyBlack
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Check, null, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CareerSummaryStep(uiState: CareerUiState) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val avatarUrl = uiState.selectedAvatar

    Column(modifier = Modifier.fillMaxSize().padding(12.dp).verticalScroll(scrollState)) {
        SidebarSectionTitle("CAREER DOSSIER", modifier = Modifier.padding(bottom = 12.dp))

        // Profile Panel
        SidebarCard(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(90.dp),
                    shape = RoundedCornerShape(2.dp),
                    color = Color.Black.copy(alpha = 0.3f),
                    border = BorderStroke(1.dp, FameColors.TrophyGold)
                ) {
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        placeholder = painterResource(R.drawable.default_manager),
                        error = painterResource(R.drawable.default_manager)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        uiState.managerName.uppercase(),
                        style = AFMTextStyles.textMD,
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = NationalityUtils.getWavingFlagUrl(uiState.managerNationality),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp, 10.dp),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            uiState.managerNationality,
                            style = AFMTextStyles.textXS,
                            color = FameColors.TrophyGold,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        uiState.careerStage,
                        style = AFMTextStyles.textXXS,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Strategy Panels
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryPanel("Identity", modifier = Modifier.weight(1f)) {
                SummaryCompactRow("Club", uiState.selectedClub?.name ?: "N/A")
                SummaryCompactRow("Tier", uiState.selectedLeague?.name ?: "N/A")
                SummaryCompactRow("Loc", uiState.selectedCountryName ?: "N/A")
            }
            SummaryPanel("Career", modifier = Modifier.weight(1f)) {
                SummaryCompactRow("Diff", uiState.selectedDifficulty?.displayName ?: "N/A")
                SummaryCompactRow("Vision", uiState.careerVision?.displayName ?: "N/A")
                SummaryCompactRow("Exp", uiState.experienceLevel)
            }
        }

        if (uiState.gameMode == GameMode.MANAGER) {
            Spacer(modifier = Modifier.height(12.dp))
            SummaryPanel("Coaching Philosophy") {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        AttributeRow("Tactics", uiState.tacticalFlexibility)
                        AttributeRow("Motiv", uiState.playerMotivation)
                        AttributeRow("Youth", uiState.youthDevelopmentFocus)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        AttributeRow("Media", uiState.mediaHandling)
                        AttributeRow("Disc", uiState.disciplineLevel)
                        AttributeRow("Adapt", uiState.adaptability)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            PersonalityProfileCard(uiState.personalityProfile, FameColors.TrophyGold)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun SummaryPanel(title: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    SidebarCard(modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, style = AFMTextStyles.textXS, color = FameColors.TrophyGold, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
fun SummaryCompactRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = AFMTextStyles.textXXS, color = Color.White.copy(alpha = 0.5f))
        Text(value, style = AFMTextStyles.textXXS, color = Color.White, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun SummaryRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label.uppercase(), style = AFMTextStyles.textXS, color = FameColors.MutedParchment, fontWeight = FontWeight.Bold)
        Text(value.uppercase(), style = AFMTextStyles.textSM, color = FameColors.WarmIvory, fontWeight = FontWeight.Black)
    }
}

@Composable
fun ManagerialStyleSelector(selectedStyle: ManagerialStyle, onSelect: (ManagerialStyle) -> Unit) {
    Column {
        Text("MANAGERIAL PHILOSOPHY", style = AFMTextStyles.textXS, color = Color.White.copy(alpha = 0.4f), modifier = Modifier.padding(bottom = 8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(ManagerialStyle.entries) { style ->
                val isSelected = selectedStyle == style
                Surface(
                    onClick = { onSelect(style) },
                    color = if (isSelected) FameColors.TrophyGold else Color.White.copy(alpha = 0.02f),
                    shape = RoundedCornerShape(2.dp),
                    border = BorderStroke(1.dp, if (isSelected) FameColors.TrophyGold else Color.White.copy(alpha = 0.08f)),
                    modifier = Modifier.width(110.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = when(style) {
                                ManagerialStyle.BALANCED -> Icons.Default.Balance
                                ManagerialStyle.ATTACKING -> Icons.Default.Bolt
                                ManagerialStyle.DEFENSIVE -> Icons.Default.Shield
                                ManagerialStyle.POSSESSION -> Icons.Default.Hub
                                ManagerialStyle.COUNTER -> Icons.Default.Speed
                                ManagerialStyle.YOUTH_DEVELOPMENT -> Icons.Default.School
                                ManagerialStyle.TACTICIAN -> Icons.Default.Psychology
                                ManagerialStyle.MOTIVATOR -> Icons.Default.Groups
                                ManagerialStyle.DISCIPLINARIAN -> Icons.Default.Gavel
                                ManagerialStyle.MEDIA_FRIENDLY -> Icons.Default.Cast
                            },
                            contentDescription = null,
                            tint = if (isSelected) Color.Black else FameColors.TrophyGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            style.displayName.uppercase(),
                            style = AFMTextStyles.textXS.copy(fontSize = 9.sp, fontWeight = FontWeight.Black),
                            color = if (isSelected) Color.Black else Color.White,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            selectedStyle.description.uppercase(),
            style = AFMTextStyles.textXS.copy(fontSize = 9.sp),
            color = Color.White.copy(alpha = 0.4f),
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

@Composable
fun CoachingLicenseSelector(selectedLicense: CoachingLicense, onSelect: (CoachingLicense) -> Unit) {
    Column {
        Text("COACHING LICENSE", style = AFMTextStyles.textXS, color = Color.White.copy(alpha = 0.4f), modifier = Modifier.padding(bottom = 8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(CoachingLicense.entries) { license ->
                val isSelected = selectedLicense == license
                Surface(
                    onClick = { onSelect(license) },
                    color = if (isSelected) FameColors.TrophyGold else Color.White.copy(alpha = 0.02f),
                    shape = RoundedCornerShape(2.dp),
                    border = BorderStroke(1.dp, if (isSelected) FameColors.TrophyGold else Color.White.copy(alpha = 0.08f)),
                    modifier = Modifier.width(110.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            license.displayName.uppercase(),
                            style = AFMTextStyles.textSM.copy(fontWeight = FontWeight.Black),
                            color = if (isSelected) Color.Black else Color.White
                        )
                        Text(
                            "REP +${license.reputationBonus}",
                            style = AFMTextStyles.textXS.copy(fontSize = 8.sp),
                            color = if (isSelected) Color.Black.copy(alpha = 0.6f) else FameColors.TrophyGold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ManagerCreationStep(
    uiState: CareerUiState,
    onUpdateName: (String) -> Unit,
    onUpdateNationality: (String) -> Unit,
    onUpdateBirthDate: (Int, Int, Int) -> Unit,
    onUpdateStyle: (ManagerialStyle) -> Unit,
    onUpdateLicense: (CoachingLicense) -> Unit,
    onUpdateSpecialAbility: (String) -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            onUpdateBirthDate(year, month + 1, dayOfMonth)
        },
        uiState.managerBirthYear,
        uiState.managerBirthMonth - 1,
        uiState.managerBirthDay
    )

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(scrollState)) {
        Text("MANAGER PROFILE".uppercase(), style = AFMTextStyles.textLG, color = FameColors.TrophyGold, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(16.dp))
        SidebarCard {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = uiState.managerName,
                    onValueChange = onUpdateName,
                    label = { Text("FULL NAME", style = AFMTextStyles.textXS) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(2.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                        focusedBorderColor = FameColors.TrophyGold,
                        unfocusedLabelColor = Color.White.copy(alpha = 0.4f),
                        focusedLabelColor = FameColors.TrophyGold,
                        unfocusedTextColor = Color.White,
                        focusedTextColor = Color.White
                    ),
                    textStyle = AFMTextStyles.textSM
                )
                Spacer(modifier = Modifier.height(16.dp))

                NationalitySelector(uiState.managerNationality, onUpdateNationality)

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    onClick = { datePickerDialog.show() },
                    color = Color.Transparent,
                    modifier = Modifier.fillMaxWidth().border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(2.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("DATE OF BIRTH", style = AFMTextStyles.textXS, color = Color.White.copy(alpha = 0.4f))
                            Text("${uiState.managerBirthDay}/${uiState.managerBirthMonth}/${uiState.managerBirthYear}", style = AFMTextStyles.textSM, color = Color.White)
                        }
                        Text("${uiState.managerAge} YEARS", style = AFMTextStyles.textSM, color = FameColors.TrophyGold, fontWeight = FontWeight.Black)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(uiState.careerStage.uppercase(), style = AFMTextStyles.textXS, color = FameColors.GrowthGreen, fontWeight = FontWeight.Black, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth())
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("PHILOSOPHY & ABILITIES".uppercase(), style = AFMTextStyles.textSM, color = FameColors.TrophyGold, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(12.dp))

        SpecialAbilitySelector(uiState.specialAbility, onUpdateSpecialAbility)

        Spacer(modifier = Modifier.height(16.dp))
        ManagerialStyleSelector(uiState.managerStyle, onUpdateStyle)

        Spacer(modifier = Modifier.height(16.dp))
        CoachingLicenseSelector(uiState.coachingLicense, onUpdateLicense)

        Spacer(modifier = Modifier.height(12.dp))
        PersonalityProfileCard(uiState.personalityProfile, FameColors.TrophyGold)

        Spacer(modifier = Modifier.height(24.dp))
        SidebarCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("SKILL ANALYSIS", style = AFMTextStyles.textXS, color = FameColors.TrophyGold, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(8.dp))
                ManagerAttributesRadarChart(uiState, modifier = Modifier.height(220.dp))
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 2-Column Attributes for Density
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        AttributeRow("TACT FLEX", uiState.tacticalFlexibility)
                        AttributeRow("MOTIV", uiState.playerMotivation)
                        AttributeRow("YOUTH DEV", uiState.youthDevelopmentFocus)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        AttributeRow("MEDIA", uiState.mediaHandling)
                        AttributeRow("DISC", uiState.disciplineLevel)
                        AttributeRow("ADAPT", uiState.adaptability)
                    }
                }
            }
        }
    }
}

@Composable
fun NationalitySelector(selectedNationality: String, onSelect: (String) -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedRegion by remember { mutableStateOf<String>("EAST_AFRICA") }
    
    val allNationalities = remember { NationalityUtils.nationalityItems }
    
    val filteredNationalities = remember(searchQuery, selectedRegion) {
        allNationalities.filter { item ->
            val matchesSearch = if (searchQuery.isBlank()) true 
                               else item.country.contains(searchQuery, ignoreCase = true)
            
            val matchesRegion = when (selectedRegion) {
                "OTHERS" -> item.region !in listOf<FootballRegion>(
                    FootballRegion.EAST_AFRICA, FootballRegion.NORTH_AFRICA, 
                    FootballRegion.SOUTHERN_AFRICA, FootballRegion.WEST_AFRICA, 
                    FootballRegion.CENTRAL_AFRICA
                )
                else -> item.region.name == selectedRegion
            }
            
            matchesSearch && matchesRegion
        }
    }

    val regions = listOf(
        "EAST_AFRICA" to "EAST",
        "WEST_AFRICA" to "WEST",
        "NORTH_AFRICA" to "NORTH",
        "SOUTHERN_AFRICA" to "SOUTH",
        "CENTRAL_AFRICA" to "CENTRAL",
        "OTHERS" to "OTHERS"
    )

    Column(modifier = Modifier.fillMaxWidth().height(420.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            placeholder = { Text("Search nationality...", style = AFMTextStyles.textXS) },
            leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(16.dp)) },
            shape = RoundedCornerShape(2.dp),
            singleLine = true,
            textStyle = AFMTextStyles.textSM,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                focusedBorderColor = FameColors.TrophyGold
            )
        )

        ScrollableTabRow(
            selectedTabIndex = regions.indexOfFirst { it.first == selectedRegion },
            containerColor = Color.Transparent,
            contentColor = FameColors.TrophyGold,
            edgePadding = 0.dp,
            divider = {},
            indicator = { tabPositions ->
                val index = regions.indexOfFirst { it.first == selectedRegion }
                if (index >= 0) {
                    Box(
                        modifier = Modifier
                            .tabIndicatorOffset(tabPositions[index])
                            .height(2.dp)
                            .background(FameColors.TrophyGold)
                    )
                }
            },
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            regions.forEach { (regionKey, label) ->
                Tab(
                    selected = selectedRegion == regionKey,
                    onClick = { selectedRegion = regionKey },
                    text = { Text(label, style = AFMTextStyles.textXXS, fontWeight = FontWeight.Black) }
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(filteredNationalities) { item ->
                val isSelected = selectedNationality == item.country
                Surface(
                    onClick = { onSelect(item.country) },
                    color = if (isSelected) FameColors.TrophyGold.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(2.dp),
                    border = BorderStroke(1.dp, if (isSelected) FameColors.TrophyGold else Color.White.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = NationalityUtils.getWavingFlagUrl(item.country),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp, 16.dp),
                            contentScale = ContentScale.Fit,
                            error = painterResource(R.drawable.default_flag)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            item.country.uppercase(),
                            style = AFMTextStyles.textXS,
                            color = if (isSelected) FameColors.TrophyGold else Color.White,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SpecialAbilitySelector(selectedAbility: String, onSelect: (String) -> Unit) {
    Column {
        Text("SPECIAL ABILITY", style = AFMTextStyles.textXS, color = Color.White.copy(alpha = 0.4f), modifier = Modifier.padding(bottom = 8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(SpecialAbility.entries) { ability ->
                val isSelected = selectedAbility == ability.name
                Surface(
                    onClick = { onSelect(ability.name) },
                    color = if (isSelected) FameColors.TrophyGold else Color.White.copy(alpha = 0.02f),
                    shape = RoundedCornerShape(2.dp),
                    border = BorderStroke(1.dp, if (isSelected) FameColors.TrophyGold else Color.White.copy(alpha = 0.08f)),
                    modifier = Modifier.width(110.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = when(ability) {
                                SpecialAbility.GENERAL -> Icons.Default.Public
                                SpecialAbility.TACTICAL_GENIUS -> Icons.Default.Psychology
                                SpecialAbility.MOTIVATOR -> Icons.Default.Groups
                                SpecialAbility.YOUTH_DEVELOPER -> Icons.Default.School
                                SpecialAbility.DEFENSIVE_SPECIALIST -> Icons.Default.Shield
                                SpecialAbility.ATTACKING_SPECIALIST -> Icons.Default.Bolt
                                SpecialAbility.SET_PIECE_GURU -> Icons.Default.Adjust
                            },
                            contentDescription = null,
                            tint = if (isSelected) Color.Black else FameColors.TrophyGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            ability.displayName.uppercase(),
                            style = AFMTextStyles.textXS.copy(fontSize = 9.sp, fontWeight = FontWeight.Black),
                            color = if (isSelected) Color.Black else Color.White,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        val currentAbility = SpecialAbility.entries.find { it.name == selectedAbility }
        currentAbility?.let {
            Text(
                it.description.uppercase(),
                style = AFMTextStyles.textXS.copy(fontSize = 9.sp),
                color = Color.White.copy(alpha = 0.4f),
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

@Composable
fun PersonalityProfileCard(profile: PersonalityProfile?, color: Color) {
    SidebarCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("PERSONALITY PROFILE", style = AFMTextStyles.textXS, color = color, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(8.dp))
            
            if (profile == null) {
                Text("CALCULATING PROFILE...", style = AFMTextStyles.textXS, color = Color.White.copy(alpha = 0.4f))
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        PersonalityMetric("TEMPER", profile.temperament, color)
                        PersonalityMetric("AMBITION", profile.ambition, color)
                        PersonalityMetric("LOYALTY", profile.loyalty, color)
                        PersonalityMetric("ADAPT", profile.adaptability, color)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        PersonalityMetric("EGO", profile.ego, color)
                        PersonalityMetric("PROF", profile.professionalism, color)
                        PersonalityMetric("PRESSURE", profile.pressure, color)
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                val trait = when {
                    profile.professionalism > 80 && profile.temperament > 70 -> "MODEL PROFESSIONAL"
                    profile.ambition > 85 && profile.loyalty < 40 -> "AMBITIOUS MERCENARY"
                    profile.loyalty > 80 && profile.professionalism > 75 -> "CLUB LEGEND POTENTIAL"
                    profile.ego > 85 && profile.temperament < 40 -> "VOLATILE PRIMADONNA"
                    profile.pressure > 80 -> "BIG MATCH SPECIALIST"
                    else -> "BALANCED CHARACTER"
                }
                
                Surface(
                    color = color.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = trait,
                        style = AFMTextStyles.textXS.copy(fontWeight = FontWeight.Black),
                        color = color,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PersonalityMetric(label: String, value: Int, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = AFMTextStyles.textXS.copy(fontSize = 8.sp), color = Color.White.copy(alpha = 0.5f))
        Text(value.toString(), style = AFMTextStyles.textXS.copy(fontWeight = FontWeight.Bold), color = color)
    }
}

@Composable
fun ManagerAttributesRadarChart(uiState: CareerUiState, modifier: Modifier = Modifier) {
    val labels = listOf("TACT", "MOT", "YTH", "MED", "DISC", "ADAP")
    val values = listOf(
        uiState.tacticalFlexibility.toFloat(),
        uiState.playerMotivation.toFloat(),
        uiState.youthDevelopmentFocus.toFloat(),
        uiState.mediaHandling.toFloat(),
        uiState.disciplineLevel.toFloat(),
        uiState.adaptability.toFloat()
    )

    AndroidView(
        modifier = modifier,
        factory = { context ->
            RadarChart(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                description.isEnabled = false
                legend.isEnabled = false
                setTouchEnabled(false)
                
                // Increase offsets to give the chart more room to expand
                setExtraOffsets(30f, 30f, 30f, 30f)

                // Web lines styling
                webColor = android.graphics.Color.GRAY
                webColorInner = android.graphics.Color.GRAY
                webLineWidth = 1f
                webAlpha = 100

                xAxis.apply {
                    textColor = android.graphics.Color.WHITE
                    textSize = 10f
                    setDrawLabels(true)
                    valueFormatter = IndexAxisValueFormatter(labels)
                }

                yAxis.apply {
                    axisMinimum = 0f
                    axisMaximum = 100f
                    setDrawLabels(false)
                    setLabelCount(5, true)
                }
            }
        },
        update = { chart ->
            val entries = values.map { RadarEntry(it) }
            val dataSet = RadarDataSet(entries, "Manager Profile").apply {
                color = FameColors.ChampionsGold.toArgb()
                fillColor = FameColors.ChampionsGold.toArgb()
                setDrawFilled(true)
                fillAlpha = 150
                lineWidth = 2f
                setDrawValues(false)
            }
            chart.data = RadarData(dataSet)
            chart.notifyDataSetChanged()
            chart.invalidate()
        }
    )
}

@Composable
fun AttributeRow(label: String, value: Int) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(
            label.uppercase(), 
            style = AFMTextStyles.textXS.copy(fontSize = 9.sp), 
            color = Color.White.copy(alpha = 0.5f), 
            modifier = Modifier.width(64.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        LinearProgressIndicator(
            progress = { value / 100f },
            modifier = Modifier.weight(1f).height(2.dp),
            color = if (value > 70) FameColors.GrowthGreen else if (value > 40) FameColors.TrophyGold else FameColors.AlertRed,
            trackColor = Color.White.copy(alpha = 0.05f)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            value.toString(), 
            style = AFMTextStyles.textXS.copy(fontWeight = FontWeight.Black), 
            color = if (value > 70) FameColors.GrowthGreen else FameColors.WarmIvory,
            modifier = Modifier.width(20.dp), 
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun CareerVisionSelectionStep(uiState: CareerUiState, onSelect: (CareerVision) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "CHOOSE YOUR STRATEGIC VISION",
            style = AFMTextStyles.textLG,
            color = FameColors.TrophyGold,
            fontWeight = FontWeight.Black
        )
        Text(
            text = "Define how you will be remembered in African football history.",
            style = AFMTextStyles.textXS,
            color = Color.White.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        CareerVision.entries.forEach { vision ->
            val isSelected = uiState.careerVision == vision
            val borderColor = if (isSelected) FameColors.TrophyGold else Color.White.copy(alpha = 0.1f)
            val backgroundColor = if (isSelected) FameColors.TrophyGold.copy(alpha = 0.1f) else Color.Transparent

            SidebarCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .background(backgroundColor, MaterialTheme.shapes.medium)
                    .clickable { onSelect(vision) }
                    .border(1.dp, borderColor, MaterialTheme.shapes.medium)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = vision.displayName.uppercase(),
                            style = AFMTextStyles.textSM,
                            color = if (isSelected) FameColors.TrophyGold else Color.White,
                            fontWeight = FontWeight.Black
                        )
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = FameColors.TrophyGold,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = vision.description,
                        style = AFMTextStyles.textXS,
                        color = Color.White.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        VisionBadge(label = "FOCUS", value = vision.primaryFocus)
                        Spacer(modifier = Modifier.width(12.dp))
                        VisionBadge(label = "HORIZON", value = vision.timeHorizon)
                    }
                }
            }
        }
    }
}

@Composable
fun VisionBadge(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = AFMTextStyles.textXS,
            color = Color.White.copy(alpha = 0.4f),
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value.uppercase(),
            style = AFMTextStyles.textXS,
            color = FameColors.GrowthGreen,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun DifficultyStep(uiState: CareerUiState, onSelect: (Difficulty) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("DIFFICULTY LEVEL".uppercase(), style = AFMTextStyles.textLG, color = FameColors.TrophyGold, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(16.dp))
        Difficulty.entries.forEach { diff ->
            val isSelected = uiState.selectedDifficulty == diff
            SidebarCard(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onSelect(diff) }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(diff.displayName.uppercase(), style = AFMTextStyles.textSM, color = if (isSelected) FameColors.TrophyGold else Color.White, fontWeight = FontWeight.Black)
                    Text(diff.description.uppercase(), style = AFMTextStyles.textXS, color = Color.White.copy(alpha = 0.4f))
                }
            }
        }
    }
}

@Composable
fun NavigationButtons(uiState: CareerUiState, onBack: () -> Unit, onNext: () -> Unit, onPrevious: () -> Unit, onStart: () -> Unit) {
    val activeColor = FameColors.GrowthGreen
    
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { if (uiState.currentStep == CareerStep.MANAGER_SELECTION) onBack() else onPrevious() },
            modifier = Modifier.weight(1f).height(48.dp),
            shape = RoundedCornerShape(2.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
        ) {
            Text("BACK", style = AFMTextStyles.textXS, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        }
        
        if (uiState.currentStep == CareerStep.CAREER_SUMMARY) {
            Button(
                onClick = onStart,
                modifier = Modifier.weight(1f).height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = activeColor, contentColor = Color.Black),
                shape = RoundedCornerShape(2.dp)
            ) {
                Text("START CAREER", style = AFMTextStyles.textXS, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            }
        } else {
            Button(
                enabled = uiState.canProceedToNextStep,
                onClick = onNext,
                modifier = Modifier.weight(1f).height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (uiState.canProceedToNextStep) activeColor else FameColors.HeaderDark, 
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(2.dp)
            ) {
                Text("CONTINUE", style = AFMTextStyles.textXS, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            }
        }
    }
}

private fun getCountryIcon(id: Int): Int? = when (id) {
    1 -> R.drawable.tanzania_premier_league
    2 -> R.drawable.egyptian_premier_league
    3 -> R.drawable.south_african_psl
    4 -> R.drawable.tunisia_league_1
    5 -> R.drawable.congo_dr_super_league
    6 -> R.drawable.algeria_league_1
    7 -> R.drawable.angola_girabola
    8 -> R.drawable.morocco_botola_pro
    9 -> R.drawable.nigerian_npfl
    10 -> R.drawable.cameroon_elite_one
    11 -> R.drawable.rwanda_premier_league
    12 -> R.drawable.kenyan_premier_league
    else -> null
}

@Preview(showBackground = true, name = "Stat Metric")
@Composable
fun StatMetricPreview() {
    AFM2026Theme {
        Surface(
            color = FameColors.DeepNavyBlack,
            modifier = Modifier.padding(16.dp)
        ) {
            StatMetric(
                label = "Win Rate",
                value = "75%",
                icon = Icons.AutoMirrored.Filled.TrendingUp
            )
        }
    }
}

@Preview(showBackground = true, name = "Step: Initializing")
@Composable
fun CareerSetupInitializationPreview() {
    AFM2026Theme(themePreset = FootballThemePreset.MANAGER_MODE) {
        BoardBackground {
            CareerSetupScreenContent(
                uiState = CareerUiState(
                    initializationProgress = 0.65f,
                    initializationStatus = "Generating League Schedules...",
                ),
                teamCounts = emptyMap(),
                onBack = {}, onNext = {}, onPrevious = {}, onStartCareer = {},
                onSelectCountry = {}, onSelectLeague = {}, onSelectDivision = {}, onSelectClub = {},
                onUpdateManagerName = {}, onUpdateManagerNationality = {}, onUpdateBirthDate = { _, _, _ -> },
                onUpdateManagerStyle = { _ -> }, onUpdateCoachingLicense = { _ -> }, onUpdateSpecialAbility = { _ -> },
                onSelectAvatar = {},
                onUpdateAvatarRegion = {},
                onSelectAgent = {},
                onUpdateAgentFilters = { _, _ -> },
                onSelectDifficulty = {}, onSelectCareerVision = {}, onSetManagerSelectionMode = {},
                onToggleDNADialog = {}, onToggleOwnershipDialog = {},
                onSelectExistingManager = {}, onProceedFromManagerSelection = {},
                onCancelOverwrite = {}
            )
        }
    }
}



@Preview(showBackground = true, name = "Club DNA Dialog")
@Composable
fun ClubDNADialogPreview() {
    AFM2026Theme {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            ClubDNADialog(
                dna = ClubDNAEntity(
                    teamId = 1,
                    region = "EAST_AFRICA",
                    playStyle = "POSSESSION",
                    playStyleSecondary = "GEGENPRESS",
                    identityStrength = 85,
                    transferPolicy = "YOUTH",
                    financialBehavior = FinancialBehavior.AGGRESSIVE,
                    youthPriority = 90
                ),
                onDismiss = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Step: Preparing Game")
@Composable
fun CareerSetupPreparingGamePreview() {
    AFM2026Theme(themePreset = FootballThemePreset.MANAGER_MODE) {
        BoardBackground {
            CareerSetupScreenContent(
                uiState = CareerUiState(
                    currentStep = CareerStep.PREPARING_GAME,
                    selectedClub = TeamsEntity(
                        id = 2, name = "Young Africans SC", league = "Tanzania Premier League", eloRating = 1825,
                        logoPath = null, rivalTeam = "Simba SC", formation = "4-3-3", cupQualification = null,
                        cupStage = null, cupName = null, cupStatus = null, managerId = null,
                        avgAttackingAbility = 75.0, avgDefenceAbility = 72.0, avgPlaymakingAbility = 74.0
                    ),
                    preparationProgress = 0.45f,
                    preparationStatus = "Initializing club systems...",
                ),
                teamCounts = emptyMap(),
                onBack = {}, onNext = {}, onPrevious = {}, onStartCareer = {},
                onSelectCountry = {}, onSelectLeague = {}, onSelectDivision = {}, onSelectClub = {},
                onUpdateManagerName = {}, onUpdateManagerNationality = {}, onUpdateBirthDate = { _, _, _ -> },
                onUpdateManagerStyle = { _ -> }, onUpdateCoachingLicense = { _ -> }, onUpdateSpecialAbility = { _ -> },
                onSelectAvatar = {},
                onUpdateAvatarRegion = {},
                onSelectAgent = {},
                onUpdateAgentFilters = { _, _ -> },
                onSelectDifficulty = {}, onSelectCareerVision = {}, onSetManagerSelectionMode = {},
                onToggleDNADialog = {}, onToggleOwnershipDialog = {},
                onSelectExistingManager = {}, onProceedFromManagerSelection = {},
                onCancelOverwrite = {}
            )
        }
    }
}



@Preview(showBackground = true, name = "Step: Manager Creation")
@Composable
fun CareerSetupManagerCreationPreview() {
    AFM2026Theme(themePreset = FootballThemePreset.MANAGER_MODE) {
        BoardBackground {
            CareerSetupScreenContent(
                uiState = CareerUiState(
                    currentStep = CareerStep.MANAGER_CREATION,
                ),
                teamCounts = emptyMap(),
                onBack = {}, onNext = {}, onPrevious = {}, onStartCareer = {},
                onSelectCountry = {}, onSelectLeague = {}, onSelectDivision = {}, onSelectClub = {},
                onUpdateManagerName = {}, onUpdateManagerNationality = {}, onUpdateBirthDate = { _, _, _ -> },
                onUpdateManagerStyle = { _ -> }, onUpdateCoachingLicense = { _ -> }, onUpdateSpecialAbility = { _ -> },
                onSelectAvatar = {},
                onUpdateAvatarRegion = {},
                onSelectAgent = {},
                onUpdateAgentFilters = { _, _ -> },
                onSelectDifficulty = {}, onSelectCareerVision = {}, onSetManagerSelectionMode = {},
                onToggleDNADialog = {}, onToggleOwnershipDialog = {},
                onSelectExistingManager = {}, onProceedFromManagerSelection = {},
                onCancelOverwrite = {}
            )
        }
    }
}



@Preview(showBackground = true, name = "Step: Summary")
@Composable
fun CareerSetupSummaryPreview() {
    AFM2026Theme(themePreset = FootballThemePreset.MANAGER_MODE) {
        BoardBackground {
            CareerSetupScreenContent(
                uiState = CareerUiState(
                    currentStep = CareerStep.CAREER_SUMMARY,
                    specialAbility = "TACTICAL_GENIUS",
                    selectedLeague = LeaguesEntity(
                        id = 1, name = "Tanzania Premier League", countryId = 1, country = "Tanzania",
                        level = 1, sponsor = "NBC", prizeMoney = 1000000, logo = "tanzania_premier_league"
                    ),
                    selectedClub = TeamsEntity(
                        id = 2, name = "Young Africans SC", league = "Tanzania Premier League", eloRating = 1825,
                        logoPath = null, rivalTeam = "Simba SC", formation = "4-3-3", cupQualification = null,
                        cupStage = null, cupName = null, cupStatus = null, managerId = null,
                        avgAttackingAbility = 75.0, avgDefenceAbility = 72.0, avgPlaymakingAbility = 74.0
                    ),
                    selectedDifficulty = Difficulty.NORMAL,
                ),
                teamCounts = mapOf("Tanzania Premier League" to 16),
                onBack = {}, onNext = {}, onPrevious = {}, onStartCareer = {},
                onSelectCountry = {}, onSelectLeague = {}, onSelectDivision = {}, onSelectClub = {},
                onUpdateManagerName = {}, onUpdateManagerNationality = {}, onUpdateBirthDate = { _, _, _ -> },
                onUpdateManagerStyle = { _ -> }, onUpdateCoachingLicense = { _ -> }, onUpdateSpecialAbility = { _ -> },
                onSelectAvatar = {},
                onUpdateAvatarRegion = {},
                onSelectAgent = {},
                onUpdateAgentFilters = { _, _ -> },
                onSelectDifficulty = {}, onSelectCareerVision = {}, onSetManagerSelectionMode = {},
                onToggleDNADialog = {}, onToggleOwnershipDialog = {},
                onSelectExistingManager = {}, onProceedFromManagerSelection = {},
                onCancelOverwrite = {}
            )
        }
    }
}



@Preview(showBackground = true, name = "Career Setup: Mode Selection")
@Composable
fun CareerSetupScreenPreview() {
    AFM2026Theme(themePreset = FootballThemePreset.MANAGER_MODE) {
        BoardBackground {
            CareerSetupScreenContent(
                uiState = CareerUiState(
                    currentStep = CareerStep.MODE_SELECTION,
                ),
                teamCounts = emptyMap(),
                onBack = {},
                onNext = {},
                onPrevious = {},
                onStartCareer = {},
                onSelectCountry = {},
                onSelectLeague = {},
                onSelectDivision = {}, onSelectClub = {},
                onUpdateManagerName = {},
                onUpdateManagerNationality = {},
                onUpdateBirthDate = { _, _, _ -> },
                onUpdateManagerStyle = { _ -> },
                onUpdateCoachingLicense = { _ -> },
                onUpdateSpecialAbility = { _ -> },
                onSelectAvatar = {},
                onUpdateAvatarRegion = {},
                onSelectAgent = {},
                onUpdateAgentFilters = { _, _ -> },
                onSelectDifficulty = {},
                onSelectCareerVision = {},
                onSetManagerSelectionMode = {},
                onToggleDNADialog = {},
                onToggleOwnershipDialog = {},
                onSelectExistingManager = {},
                onProceedFromManagerSelection = {},
                onCancelOverwrite = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Manager Attributes Radar Chart")
@Composable
fun ManagerAttributesRadarChartPreview() {
    AFM2026Theme(themePreset = FootballThemePreset.MANAGER_MODE) {
        BoardBackground {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                SidebarCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "PHILOSOPHY RADAR",
                            style = AFMTextStyles.textXS,
                            color = FameColors.TrophyGold,
                            fontWeight = FontWeight.Black
                        )
                        ManagerAttributesRadarChart(
                            uiState = CareerUiState(
                                tacticalFlexibility = 85,
                                playerMotivation = 70,
                                youthDevelopmentFocus = 90,
                                mediaHandling = 60,
                                disciplineLevel = 75,
                                adaptability = 80
                            ),
                            modifier = Modifier.height(250.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Nationality Selector")
@Composable
fun NationalitySelectorPreview() {
    var selectedNationality by remember { mutableStateOf("Tanzania") }
    AFM2026Theme {
        Surface(
            color = FameColors.DeepNavyBlack,
            modifier = Modifier.padding(16.dp).fillMaxWidth()
        ) {
            NationalitySelector(
                selectedNationality = selectedNationality,
                onSelect = { selectedNationality = it }
            )
        }
    }
}

@Preview(showBackground = true, name = "Agent Card")
@Composable
fun AgentCardPreview() {
    val mockAgent = Agent(
        id = 1,
        name = "Saidi King",
        agency = "East African Sports",
        reputation = 45,
        negotiationSkill = 60,
        personality = "LOYAL",
        commissionRate = 0.05f,
        connections = listOf("Tanzania", "Kenya"),
        nationality = "Tanzania",
        nationalityFlag = "file:///android_asset/flags/Tanzania.webp"
    )
    
    AFM2026Theme {
        Surface(color = FameColors.DeepNavyBlack, modifier = Modifier.padding(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                AgentCard(agent = mockAgent, isSelected = false, onSelect = {})
                AgentCard(agent = mockAgent, isSelected = true, onSelect = {})
                AgentCard(
                    agent = mockAgent.copy(nationalityFlag = null, name = "Generic Agent"), 
                    isSelected = false, 
                    onSelect = {}
                )
            }
        }
    }
}


