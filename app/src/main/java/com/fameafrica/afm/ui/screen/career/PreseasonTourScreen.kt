package com.fameafrica.afm.ui.screen.career

import android.graphics.BitmapFactory
import android.graphics.RectF
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.fameafrica.afm.R
import com.fameafrica.afm.data.database.entities.NationalitiesEntity
import com.fameafrica.afm.data.database.entities.TeamsEntity
import com.fameafrica.afm.domain.manager.GameManager
import com.fameafrica.afm.ui.components.common.GlassPanel
import com.fameafrica.afm.ui.components.common.SidebarBroadcastHeader
import com.fameafrica.afm.ui.map.AfricaMapBounds
import com.fameafrica.afm.ui.screen.club.ClubScreen
import com.fameafrica.afm.ui.screen.squad.SquadScreen
import com.fameafrica.afm.ui.theme.AFM2026Theme
import com.fameafrica.afm.ui.theme.AFM2026Typography
import com.fameafrica.afm.ui.theme.FameColors
import com.fameafrica.afm.ui.components.common.AfricanBackground
import com.fameafrica.afm.utils.NationalityUtils

@Composable
fun PreseasonTourScreen(
    onBack: () -> Unit,
    onStartSeason: () -> Unit,
    onNavigateToTactics: () -> Unit,
    onNavigateToPlayerDetail: (Int) -> Unit,
    onNavigateToFinances: () -> Unit,
    onNavigateToInfrastructure: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSponsor: (Int) -> Unit,
    onNavigateToYouth: () -> Unit,
    viewModel: PreseasonViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState(PreseasonUiState())

    PreseasonTourContent(
        uiState = uiState,
        onBack = onBack,
        onStartSeason = onStartSeason,
        onSelectRegion = viewModel::selectRegion,
        onSelectCountry = viewModel::selectCountry,
        onApplyFilters = viewModel::applyFilters,
        onToggleTeamSelection = viewModel::toggleTeamSelection,
        onViewTeamDetails = { /* Handle team details */ },
        onProceedToSummary = viewModel::proceedToSummary,
        onConfirmTour = viewModel::confirmTour,
        onBackStep = viewModel::backStep,
        onSimulatePreseason = viewModel::simulatePreseason,
        onTabSelected = viewModel::setActiveTab,
        squadContent = {
            val gameState = uiState.gameState ?: GameManager.GameState.Loading
            SquadScreen(
                currentGameState = gameState,
                onPlayerClick = onNavigateToPlayerDetail,
                onTacticsClick = onNavigateToTactics,
                onTrainingClick = { },
                onBack = { viewModel.setActiveTab(ActiveTourTab.OVERVIEW) }
            )
        },
        clubContent = {
            ClubScreen(
                onBack = { viewModel.setActiveTab(ActiveTourTab.OVERVIEW) },
                onFinancesClick = onNavigateToFinances,
                onInfrastructureClick = onNavigateToInfrastructure,
                onHistoryClick = onNavigateToHistory,
                onRenegotiateSponsor = onNavigateToSponsor,
                onSearchSponsorsClick = { },
                onNavigateToYouth = onNavigateToYouth
            )
        }
    )
}

@Composable
fun PreseasonTourContent(
    uiState: PreseasonUiState,
    onBack: () -> Unit,
    onStartSeason: () -> Unit,
    onSelectRegion: (PreseasonRegion) -> Unit,
    onSelectCountry: (NationalitiesEntity) -> Unit,
    onApplyFilters: (Int?, String?) -> Unit,
    onToggleTeamSelection: (TeamsEntity) -> Unit,
    onViewTeamDetails: (TeamsEntity) -> Unit,
    onProceedToSummary: () -> Unit,
    onConfirmTour: () -> Unit,
    onBackStep: () -> Unit,
    onSimulatePreseason: () -> Unit,
    onTabSelected: (ActiveTourTab) -> Unit,
    squadContent: @Composable () -> Unit,
    clubContent: @Composable () -> Unit
) {
    var showAiClubScreen by remember { mutableStateOf<TeamsEntity?>(null) }

    AfricanBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                if (uiState.currentStep != TourStep.ACTIVE || uiState.activeTourTab == ActiveTourTab.OVERVIEW) {
                    val subtitleText = when(uiState.currentStep) {
                        TourStep.REGION_SELECTION -> "SELECT REGION"
                        TourStep.MAP -> uiState.selectedRegion?.displayName ?: "SELECT DESTINATION"
                        TourStep.SELECTION -> uiState.selectedCountry?.nationality?.uppercase() ?: "SELECT TEAMS"
                        TourStep.SUMMARY -> "CONFIRM TOUR"
                        else -> "PRESEASON"
                    }

                    SidebarBroadcastHeader(
                        title = "PRESEASON TOUR",
                        icon = Icons.Default.FlightTakeoff,
                        subtitle = subtitleText,
                        actions = {
                            if (uiState.currentStep != TourStep.REGION_SELECTION && uiState.currentStep != TourStep.ACTIVE) {
                                IconButton(onClick = onBackStep) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                                }
                            }
                            IconButton(onClick = onBack) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                            }
                        }
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        ) { padding ->
            Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                AnimatedContent(
                    targetState = uiState.currentStep,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "TourStepTransition"
                ) { step ->
                    when (step) {
                        TourStep.REGION_SELECTION -> RegionSelectionView(onSelectRegion)
                        TourStep.MAP -> MapSelectionView(uiState, onSelectCountry)
                        TourStep.SELECTION -> TeamSelectionView(
                            uiState,
                            onApplyFilters,
                            onToggleTeamSelection,
                            onViewTeamDetails = { showAiClubScreen = it },
                            onProceedToSummary
                        )
                        TourStep.SUMMARY -> TourSummaryView(uiState, onBackStep, onConfirmTour)
                        TourStep.ACTIVE -> TourActiveView(
                            uiState = uiState,
                            onStartSeason = onStartSeason,
                            onSimulatePreseason = onSimulatePreseason,
                            onTabSelected = onTabSelected,
                            squadContent = squadContent,
                            clubContent = clubContent
                        )
                    }
                }
                
                if (uiState.isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = FameColors.ChampionsGold)
                    }
                }

                if (showAiClubScreen != null) {
                    AiClubDetailsDialog(
                        team = showAiClubScreen!!,
                        onDismiss = { showAiClubScreen = null }
                    )
                }
            }
        }
    }
}

@Composable
fun RegionSelectionView(onSelectRegion: (PreseasonRegion) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "CHOOSE TOUR REGION",
                style = AFM2026Typography.headlineSmall,
                color = FameColors.ChampionsGold,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        items(PreseasonRegion.entries) { region ->
            RegionCard(region = region, onClick = { onSelectRegion(region) })
        }
    }
}

@Composable
fun RegionCard(region: PreseasonRegion, onClick: () -> Unit) {
    val iconResId = when(region) {
        PreseasonRegion.EAST_AFRICA -> R.drawable.ic_eastafrica
        PreseasonRegion.NORTH_AFRICA -> R.drawable.ic_northafrica
        PreseasonRegion.WEST_AFRICA -> R.drawable.ic_westafrica
        PreseasonRegion.SOUTHERN_AFRICA -> R.drawable.ic_southernafrica
        PreseasonRegion.CENTRAL_AFRICA -> R.drawable.ic_centralafrica
    }

    GlassPanel(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = iconResId),
                contentDescription = region.displayName,
                modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Fit
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    region.displayName,
                    style = AFM2026Typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Black
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RegionBenefitChip("REVENUE", FameColors.ChampionsGold)
                    RegionBenefitChip("REPUTATION", FameColors.PitchGreen)
                    RegionBenefitChip("FAN GROWTH", FameColors.AfricanLegendEmerald)
                }
            }
            
            Icon(Icons.Default.ChevronRight, null, tint = Color.White.copy(0.5f))
        }
    }
}

@Composable
fun RegionBenefitChip(label: String, color: Color) {
    Surface(
        color = color.copy(0.1f),
        shape = RoundedCornerShape(4.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, color.copy(0.5f))
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = AFM2026Typography.labelSmall.copy(fontSize = 8.sp),
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun MapSelectionView(
    uiState: PreseasonUiState,
    onSelectCountry: (NationalitiesEntity) -> Unit
) {
    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    
    // Zoom and Pan State
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val state = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 4f)
        offset += offsetChange
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { containerSize = it.size }
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(0.2f))
            .transformable(state = state)
            .pointerInput(Unit) {
                detectTapGestures(onDoubleTap = {
                    scale = if (scale > 1f) 1f else 2f
                    offset = Offset.Zero
                }, onTap = {
                    // Prevent accidental selection while panning
                })
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                )
        ) {
            Image(
                painter = painterResource(id = R.drawable.africa_realistic_map),
                contentDescription = "Africa Map",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )

            AfricaMapBounds.bounds.forEach { (countryName, rect) ->
                val nation = uiState.africanCountries.find {
                    it.nationality.lowercase() == countryName.replace("_", " ")
                }

                if (nation != null) {
                    TourFlagPoint(
                        rect = rect,
                        containerSize = containerSize,
                        countryName = nation.nationality,
                        zoomScale = scale,
                        onSelect = { onSelectCountry(nation) }
                    )
                }
            }
        }

        // Overlay controls
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FloatingActionButton(
                onClick = { scale = (scale + 0.5f).coerceAtMost(4f) },
                containerColor = Color.Black.copy(0.6f),
                contentColor = Color.White,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(Icons.Default.Add, null)
            }
            FloatingActionButton(
                onClick = { 
                    scale = (scale - 0.5f).coerceAtLeast(1f)
                    if (scale == 1f) offset = Offset.Zero
                },
                containerColor = Color.Black.copy(0.6f),
                contentColor = Color.White,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(Icons.Default.Remove, null)
            }
        }

        GlassPanel(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .fillMaxWidth(0.8f)
        ) {
            Text(
                "TAP A FLAG TO START YOUR TOUR • PINCH TO ZOOM",
                style = AFM2026Typography.labelMedium,
                color = FameColors.ChampionsGold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
fun TourFlagPoint(
    rect: RectF,
    containerSize: IntSize,
    countryName: String,
    zoomScale: Float,
    onSelect: () -> Unit
) {
    if (containerSize == IntSize.Zero) return

    val infiniteTransition = rememberInfiniteTransition(label = "Blink")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "Alpha"
    )

    // ACTUAL IMAGE SIZE (496x520)
    val originalWidth = 496f
    val originalHeight = 520f
    // REFERENCE COORDINATE SYSTEM (620f)
    val referenceSize = 620f

    // Calculate scale and offsets based on ContentScale.Fit
    val fitScale = minOf(
        containerSize.width.toFloat() / originalWidth,
        containerSize.height.toFloat() / originalHeight
    )
    
    val offsetX = (containerSize.width - originalWidth * fitScale) / 2f
    val offsetY = (containerSize.height - originalHeight * fitScale) / 2f

    // Map coordinates from 620 reference system to 496x520 image space, then to screen space
    val x = offsetX + (rect.centerX() * (originalWidth / referenceSize) * fitScale)
    val y = offsetY + (rect.centerY() * (originalHeight / referenceSize) * fitScale)

    val density = LocalDensity.current
    // SCALED DOWN BASE SIZE
    val baseSize = 14.dp
    // Adjust size based on zoom but keep it readable
    val flagSize = baseSize / (zoomScale.coerceAtLeast(1f).let { if (it > 2.5f) it * 0.8f else 1f })
    val flagSizePx = with(density) { flagSize.toPx() }

    val context = LocalContext.current
    val isInspectionMode = LocalInspectionMode.current
    
    // Improved flag loading for both runtime and preview
    val flagPainter: Painter = if (isInspectionMode) {
        val bitmap = remember(countryName) {
            try {
                context.assets.open("flags/$countryName.webp").use { 
                    BitmapFactory.decodeStream(it)
                }?.asImageBitmap()
            } catch (e: Exception) {
                null
            }
        }
        if (bitmap != null) BitmapPainter(bitmap) else painterResource(id = R.drawable.default_flag)
    } else {
        rememberAsyncImagePainter(
            model = NationalityUtils.getFlagUrl(nationality = countryName),
            placeholder = painterResource(id = R.drawable.default_flag),
            error = painterResource(id = R.drawable.default_flag)
        )
    }

    Box(
        modifier = Modifier
            .offset { 
                IntOffset(
                    (x - flagSizePx / 2).toInt(), 
                    (y - flagSizePx / 2).toInt()
                ) 
            }
            .size(flagSize)
            .alpha(alpha)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onSelect
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (zoomScale > 2.0f) {
                Text(
                    text = countryName.uppercase(),
                    style = AFM2026Typography.labelSmall.copy(fontSize = (4 / zoomScale * 1.5f).coerceAtLeast(3.sp.value).sp),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(2.dp))
                        .padding(horizontal = 2.dp, vertical = 1.dp)
                )
                Spacer(modifier = Modifier.height(1.dp))
            }
            
            Image(
                painter = flagPainter,
                contentDescription = countryName,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .border(0.5.dp, Color.White.copy(0.8f), CircleShape),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TeamSelectionView(
    uiState: PreseasonUiState,
    onApplyFilters: (Int?, String?) -> Unit,
    onToggleTeamSelection: (TeamsEntity) -> Unit,
    onViewTeamDetails: (TeamsEntity) -> Unit,
    onProceedToSummary: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Filters
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = uiState.levelFilter == 1,
                onClick = { onApplyFilters(if (uiState.levelFilter == 1) null else 1, uiState.starFilter) },
                label = { Text("TOP DIVISION") }
            )
            FilterChip(
                selected = uiState.starFilter == "Gold",
                onClick = { onApplyFilters(uiState.levelFilter, if (uiState.starFilter == "Gold") null else "Gold") },
                label = { Text("GOLD STARS") }
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                "${uiState.selectedTourTeams.size}/5 SELECTED",
                style = AFM2026Typography.labelSmall,
                color = FameColors.ChampionsGold,
                fontWeight = FontWeight.Black
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(uiState.filteredTeams) { team ->
                TeamSelectionItem(
                    team = team,
                    isSelected = uiState.selectedTourTeams.any { it.id == team.id },
                    onSelect = { onToggleTeamSelection(team) },
                    onInfo = { onViewTeamDetails(team) }
                )
            }
        }

        Button(
            onClick = onProceedToSummary,
            enabled = uiState.selectedTourTeams.size in 3..6,
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = FameColors.PitchGreen)
        ) {
            Text("PROCEED TO TOUR SUMMARY", fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun TeamSelectionItem(team: TeamsEntity, isSelected: Boolean, onSelect: () -> Unit, onInfo: () -> Unit) {
    val starColor = when {
        team.reputation >= 70 -> FameColors.ChampionsGold
        team.reputation >= 40 -> Color(0xFFC0C0C0) // Silver
        else -> Color(0xFFCD7F32) // Bronze
    }

    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onSelect() },
        color = if (isSelected) FameColors.PitchGreen.copy(0.1f) else Color.White.copy(0.05f),
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, FameColors.PitchGreen) else null
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Team Logo
            Image(
                painter = painterResource(id = R.drawable.default_club), // Replace with actual logo loading if available
                contentDescription = team.name,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(0.1f))
                    .padding(4.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(team.name.uppercase(), style = AFM2026Typography.bodyLarge, color = Color.White, fontWeight = FontWeight.Black)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    repeat(5) { index ->
                        val active = (index + 1) * 20 <= team.reputation
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = if (active) starColor else Color.Gray.copy(0.3f)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(team.league, style = AFM2026Typography.bodySmall, color = FameColors.MutedParchment)
                }
            }

            IconButton(
                onClick = onInfo,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Team Info",
                    tint = FameColors.ChampionsGold,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun AiClubDetailsDialog(team: TeamsEntity, onDismiss: () -> Unit) {
    var activeTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("OVERVIEW", "SQUAD")

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = FameColors.StadiumBlack
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                SidebarBroadcastHeader(
                    title = team.name.uppercase(),
                    icon = Icons.Default.Shield,
                    subtitle = team.league.uppercase(),
                    actions = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, null, tint = Color.White)
                        }
                    }
                )

                TabRow(
                    selectedTabIndex = activeTab,
                    containerColor = Color.Transparent,
                    contentColor = FameColors.ChampionsGold,
                    divider = { HorizontalDivider(color = Color.White.copy(0.1f)) }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = activeTab == index,
                            onClick = { activeTab = index },
                            text = { Text(title, style = AFM2026Typography.labelSmall) }
                        )
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    when (activeTab) {
                        0 -> ClubScreen(
                            onBack = onDismiss,
                            onFinancesClick = {},
                            onInfrastructureClick = {},
                            onHistoryClick = {},
                            onRenegotiateSponsor = {},
                            onSearchSponsorsClick = {},
                            onNavigateToYouth = {}
                        )
                        1 -> SquadScreen(
                            currentGameState = GameManager.GameState.Loading, // Use a static view if possible
                            onPlayerClick = { },
                            onTacticsClick = { },
                            onTrainingClick = { },
                            onBack = onDismiss
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TourSummaryView(
    uiState: PreseasonUiState,
    onBackStep: () -> Unit,
    onConfirmTour: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            "TOUR TO ${uiState.selectedCountry?.nationality?.uppercase()}",
            style = AFM2026Typography.headlineSmall,
            color = FameColors.ChampionsGold,
            fontWeight = FontWeight.Black
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        GlassPanel(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("SELECTED TEAMS", style = AFM2026Typography.labelMedium, color = FameColors.MutedParchment)
                Spacer(modifier = Modifier.height(12.dp))
                uiState.selectedTourTeams.forEach { team ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(team.name, color = Color.White)
                        Text(team.league, color = FameColors.MutedParchment, style = AFM2026Typography.bodySmall)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = onBackStep,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
            ) {
                Text("BACK")
            }
            Button(
                onClick = onConfirmTour,
                modifier = Modifier.weight(2f),
                colors = ButtonDefaults.buttonColors(containerColor = FameColors.PitchGreen)
            ) {
                Text("CONFIRM TOUR", fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun TourActiveView(
    uiState: PreseasonUiState,
    onStartSeason: () -> Unit,
    onSimulatePreseason: () -> Unit,
    onTabSelected: (ActiveTourTab) -> Unit,
    squadContent: @Composable () -> Unit,
    clubContent: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Tab Row for Active Tour
        TabRow(
            selectedTabIndex = uiState.activeTourTab.ordinal,
            containerColor = FameColors.StadiumBlack.copy(alpha = 0.8f),
            contentColor = FameColors.ChampionsGold,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[uiState.activeTourTab.ordinal]),
                    color = FameColors.ChampionsGold
                )
            }
        ) {
            ActiveTourTab.entries.forEach { tab ->
                Tab(
                    selected = uiState.activeTourTab == tab,
                    onClick = { onTabSelected(tab) },
                    text = {
                        Text(
                            tab.name,
                            style = AFM2026Typography.labelSmall,
                            fontWeight = if (uiState.activeTourTab == tab) FontWeight.Black else FontWeight.Normal
                        )
                    }
                )
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            when (uiState.activeTourTab) {
                ActiveTourTab.OVERVIEW -> TourOverview(uiState, onSimulatePreseason, onStartSeason)
                ActiveTourTab.SQUAD -> squadContent()
                ActiveTourTab.CLUB -> clubContent()
                ActiveTourTab.FIXTURES -> PreseasonFixturesView(uiState.matches)
            }
        }
    }
}

@Composable
fun TourOverview(
    uiState: PreseasonUiState,
    onSimulatePreseason: () -> Unit,
    onStartSeason: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(100.dp).clip(CircleShape).background(FameColors.PitchGreen.copy(0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.FlightTakeoff, null, modifier = Modifier.size(50.dp), tint = FameColors.PitchGreen)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            "TOUR IN PROGRESS",
            style = AFM2026Typography.headlineSmall,
            color = Color.White,
            fontWeight = FontWeight.Black
        )
        
        Text(
            "Location: ${uiState.selectedCountry?.nationality}",
            style = AFM2026Typography.bodyMedium,
            color = FameColors.MutedParchment
        )
        
        Spacer(modifier = Modifier.height(24.dp))

        // Progress Indicators
        GlassPanel(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                TourProgressItem("SQUAD FITNESS", uiState.fitnessProgress, FameColors.PitchGreen)
                TourProgressItem("TEAM COHESION", uiState.cohesionProgress, FameColors.AfricanLegendEmerald)
                
                HorizontalDivider(color = Color.White.copy(0.1f))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("TOUR REVENUE", style = AFM2026Typography.labelSmall, color = FameColors.MutedParchment)
                    Text("€${uiState.commercialIncome}", style = AFM2026Typography.labelSmall, color = FameColors.ChampionsGold, fontWeight = FontWeight.Black)
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        val allMatchesCompleted = uiState.completedMatches >= uiState.totalMatches && uiState.totalMatches > 0

        Button(
            onClick = onSimulatePreseason,
            enabled = !allMatchesCompleted,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = FameColors.ChampionsGold),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(if (allMatchesCompleted) "TOUR COMPLETED" else "SIMULATE NEXT MATCH", color = Color.Black, fontWeight = FontWeight.Black)
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Button(
            onClick = onStartSeason,
            enabled = allMatchesCompleted,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = FameColors.PitchGreen),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("START NEW SEASON", fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun TourProgressItem(label: String, progress: Float, color: Color) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = AFM2026Typography.labelSmall, color = FameColors.MutedParchment)
            Text("${(progress * 100).toInt()}%", style = AFM2026Typography.labelSmall, color = Color.White)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
            color = color,
            trackColor = Color.White.copy(alpha = 0.1f)
        )
    }
}

@Composable
fun PreseasonFixturesView(matches: List<com.fameafrica.afm.data.database.entities.PreseasonScheduleEntity>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(matches) { match ->
            GlassPanel {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(match.opponent.uppercase(), style = AFM2026Typography.bodyLarge, color = Color.White, fontWeight = FontWeight.Bold)
                        Text(match.matchDate, style = AFM2026Typography.bodySmall, color = FameColors.MutedParchment)
                    }
                    
                    if (match.isCompleted) {
                        Text(
                            "${match.homeScore} - ${match.opponentScore}",
                            style = AFM2026Typography.headlineSmall,
                            color = FameColors.ChampionsGold,
                            fontWeight = FontWeight.Black
                        )
                    } else {
                        Surface(
                            color = Color.White.copy(0.1f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "SCHEDULED",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = AFM2026Typography.labelSmall,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreseasonTourScreenPreview() {
    AFM2026Theme {
        PreseasonTourContent(
            uiState = PreseasonUiState(
                currentStep = TourStep.REGION_SELECTION,
                isLoading = false
            ),
            onBack = {},
            onStartSeason = {},
            onSelectRegion = {},
            onSelectCountry = {},
            onApplyFilters = { _, _ -> },
            onToggleTeamSelection = {},
            onViewTeamDetails = {},
            onProceedToSummary = {},
            onConfirmTour = {},
            onBackStep = {},
            onSimulatePreseason = {},
            onTabSelected = {},
            squadContent = {},
            clubContent = {}
        )
    }
}
