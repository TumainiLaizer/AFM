package com.fameafrica.afm.ui.screen.tactics

import android.annotation.SuppressLint
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fameafrica.afm.data.database.entities.PlayersEntity
import com.fameafrica.afm.data.database.entities.Playstyle
import com.fameafrica.afm.ui.components.common.SidebarCard
import com.fameafrica.afm.ui.components.common.PitchCanvas
import com.fameafrica.afm.ui.components.common.PlayerOnPitchCard
import com.fameafrica.afm.ui.components.common.getRatingColor
import com.fameafrica.afm.ui.screen.match.PitchPattern
import com.fameafrica.afm.ui.theme.*
import com.fameafrica.afm.data.database.entities.Formation
import com.fameafrica.afm.utils.extensions.toTitleCase
import com.fameafrica.afm.utils.tactics.TacticalInsight
import com.fameafrica.afm.ui.screen.tactics.getPositionsForFormation
import com.fameafrica.afm.ui.screen.tactics.getPositionLabelsForFormation
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun TacticsScreen(
    onBack: () -> Unit,
    onNavigateToPlayerDetails: (Int) -> Unit,
    viewModel: TacticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AFM2026Theme {
        TacticsScreenContent(
            uiState = uiState,
            onBack = onBack,
            onReset = { viewModel.resetTactics() },
            onSave = { viewModel.saveTactics() },
            onSelectFormation = { viewModel.selectFormation(it) },
            onUpdateSlider = { slider, value -> viewModel.updateSlider(slider, value) },
            onFilterChange = { viewModel.setFilterType(it) },
            onLineupStrategyChange = { viewModel.setLineupStrategy(it) },
            onSubstitutionStrategyChange = { viewModel.setSubstitutionStrategy(it) },
            onSwapPlayers = { from, to -> viewModel.swapPlayers(from, to) },
            onPlayerClick = onNavigateToPlayerDetails,
            onAutoSelect = { viewModel.autoSelectLineup() },
            onSetPitchPattern = { viewModel.setPitchPattern(it) },
            onToggleDisplayMode = { viewModel.toggleDisplayMode() },
            onUpdateRole = { role, id -> viewModel.updateRole(role, id) },
            onUpdateStyle = { viewModel.updateStyle(it) },
            onLoadSlot = { viewModel.loadSlot(it) },
            onSaveToSlot = { viewModel.saveToSlot(it) },
            onFavoriteSlot = { viewModel.favoriteSlot(it) },
            onDeleteSlot = { viewModel.deleteSlot(it) },
            onSetFilter = { viewModel.setFilter(it) },
            onSetSortBy = { viewModel.setSortBy(it) },
            onSetMentality = { viewModel.setMentality(it) }
        )
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TacticsScreenContent(
    uiState: TacticsUiState,
    onBack: () -> Unit,
    onReset: () -> Unit,
    onSave: () -> Unit,
    onSelectFormation: (String) -> Unit,
    onUpdateSlider: (String, Int) -> Unit,
    onFilterChange: (String) -> Unit,
    onLineupStrategyChange: (LineupStrategy) -> Unit,
    onSubstitutionStrategyChange: (SubstitutionStrategy) -> Unit,
    onSwapPlayers: (Int, Int) -> Unit,
    onPlayerClick: (Int) -> Unit,
    onAutoSelect: () -> Unit,
    onSetPitchPattern: (PitchPattern) -> Unit,
    onToggleDisplayMode: () -> Unit,
    onUpdateRole: (String, Int) -> Unit,
    onUpdateStyle: (String) -> Unit,
    onLoadSlot: (Int) -> Unit,
    onSaveToSlot: (Int) -> Unit,
    onFavoriteSlot: (Int) -> Unit,
    onDeleteSlot: (Int) -> Unit,
    onSetFilter: (PlayerFilterType) -> Unit,
    onSetSortBy: (PlayerSortType) -> Unit,
    onSetMentality: (TacticsMentality) -> Unit
) {
    var showFloatingControls by remember { mutableStateOf(false) }
    var selectedPlayerId by remember { mutableStateOf<Int?>(null) }
    val pagerState = rememberPagerState(pageCount = { 4 })
    var showRoleSelectorForPlayerId by remember { mutableStateOf<Int?>(null) }

    val dragOffsets = remember { mutableStateMapOf<Int, Offset>() }
    val componentPositions = remember { mutableStateMapOf<Int, Offset>() }
    var draggingPlayerId by remember { mutableStateOf<Int?>(null) }

    Scaffold(
        topBar = {
            TacticalHeader(
                uiState = uiState,
                onBack = onBack,
                currentPage = pagerState.currentPage,
                onPageSelected = { /* Navigate */ }
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            TacticalBottomActions(onAutoSelect, onSave, onReset)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TacticSlotsBar(uiState, onLoadSlot, onSaveToSlot, onFavoriteSlot, onDeleteSlot)

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                beyondViewportPageCount = 2
            ) { page ->
                when (page) {
                    0 -> LineupView(
                        uiState = uiState,
                        selectedPlayerId = selectedPlayerId,
                        draggingPlayerId = draggingPlayerId,
                        dragOffsets = dragOffsets,
                        componentPositions = componentPositions,
                        onPlayerSelect = { id ->
                            showRoleSelectorForPlayerId = id
                        },
                        onDragStart = { draggingPlayerId = it },
                        onDrag = { id, offset -> dragOffsets[id] = (dragOffsets[id] ?: Offset.Zero) + offset },
                        onDragEnd = { id ->
                            val currentDragOffset = dragOffsets[id] ?: Offset.Zero
                            val currentPos = componentPositions[id] ?: Offset.Zero
                            val dropPos = currentPos + currentDragOffset

                            if (dropPos.y > 1500f) {
                                selectedPlayerId = id
                            } else {
                                var targetId: Int? = null
                                componentPositions.forEach { (otherId, pos) ->
                                    if (otherId != id && (dropPos - pos).getDistance() < 100f) {
                                        targetId = otherId
                                    }
                                }
                                targetId?.let { onSwapPlayers(id, it) }
                            }
                            dragOffsets[id] = Offset.Zero
                            draggingPlayerId = null
                        },
                        onPlayerLongClick = onPlayerClick,
                        onSetPitchPattern = onSetPitchPattern,
                        onToggleDisplayMode = onToggleDisplayMode,
                        onSelectFormation = onSelectFormation,
                        onSetMentality = onSetMentality,
                        onSetFilter = onSetFilter,
                        onSetSortBy = onSetSortBy
                    )
                    1 -> PlaystyleTab(uiState, onUpdateStyle, onUpdateSlider)
                    2 -> RolesTab(uiState, onUpdateRole)
                    3 -> FeedbackTab(uiState)
                }
            }
        }

        if (showFloatingControls) {
            TacticalGlassOverlay(
                uiState = uiState,
                onDismiss = { showFloatingControls = false },
                onSelectFormation = onSelectFormation,
                onLineupStrategyChange = onLineupStrategyChange,
                onFilterChange = onFilterChange,
                onSubstitutionStrategyChange = onSubstitutionStrategyChange,
                onAutoSelect = { onAutoSelect(); showFloatingControls = false },
                onSave = onSave,
                onReset = onReset
            )
        }

        if (showRoleSelectorForPlayerId != null) {
            RoleSelectorPopup(
                playerId = showRoleSelectorForPlayerId!!,
                players = uiState.teamPlayers,
                onDismiss = { showRoleSelectorForPlayerId = null },
                onSelectRole = { role ->
                    onUpdateRole(role, showRoleSelectorForPlayerId!!)
                    showRoleSelectorForPlayerId = null
                }
            )
        }
    }
}

@Composable
fun RoleSelectorPopup(
    playerId: Int,
    players: List<PlayersEntity>,
    onDismiss: () -> Unit,
    onSelectRole: (String) -> Unit
) {
    val player = players.find { it.id == playerId } ?: return
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = FameColors.DeepNavyBlack,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(player.name.uppercase(), style = AFMTextStyles.textMD, color = Color.White, fontWeight = FontWeight.Black)
                Text(player.position, style = AFMTextStyles.textXS, color = FameColors.ChampionsGold)
                Spacer(modifier = Modifier.height(16.dp))
                
                val roles = listOf("Captain", "Penalty Taker", "Free-kick Taker", "Corner Taker")
                roles.forEach { role ->
                    Surface(
                        onClick = { onSelectRole(role) },
                        color = Color.White.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(role.uppercase(), style = AFMTextStyles.textXS, color = Color.White)
                            Icon(Icons.Default.Add, null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(14.dp))
                        }
                    }
                }
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f))
                ) {
                    Text("CANCEL", style = AFMTextStyles.textXS)
                }
            }
        }
    }
}

@Composable
fun TacticalHeader(uiState: TacticsUiState, onBack: () -> Unit, currentPage: Int, onPageSelected: (Int) -> Unit) {
    Surface(
        color = FameColors.DeepNavyBlack,
        modifier = Modifier.fillMaxWidth().statusBarsPadding()
    ) {
        Column {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "BACK", tint = Color.White)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "TACTICAL HUB",
                        style = AFMTextStyles.textXS,
                        color = FameColors.ChampionsGold,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        uiState.selectedFormation,
                        style = AFMTextStyles.textLG,
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )
                }

                IconButton(onClick = { /* Help */ }) {
                    Icon(Icons.Default.Info, "INFO", tint = Color.White.copy(alpha = 0.6f))
                }
            }

            TabRow(
                selectedTabIndex = currentPage,
                containerColor = Color.Transparent,
                contentColor = FameColors.ChampionsGold,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[currentPage]),
                        color = FameColors.ChampionsGold,
                        height = 2.dp
                    )
                },
                divider = {}
            ) {
                val tabs = listOf("LINEUP", "PLAYSTYLE", "ROLES", "FEEDBACK")
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = currentPage == index,
                        onClick = { onPageSelected(index) },
                        text = { 
                            Text(
                                title, 
                                style = AFMTextStyles.textXS.copy(
                                    fontWeight = if (currentPage == index) FontWeight.Black else FontWeight.Bold
                                )
                            ) 
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TacticSlotsBar(
    uiState: TacticsUiState,
    onLoadSlot: (Int) -> Unit,
    onSaveToSlot: (Int) -> Unit,
    onFavoriteSlot: (Int) -> Unit,
    onDeleteSlot: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.3f)).padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("SLOTS:", style = AFMTextStyles.textXS, color = Color.White.copy(alpha = 0.5f), fontWeight = FontWeight.Black)
        uiState.savedTactics.forEach { slot ->
            val isActive = slot.slotNumber == uiState.currentSlot
            Surface(
                color = if (isActive) FameColors.ChampionsGold else Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(2.dp),
                modifier = Modifier.weight(1f).height(32.dp).combinedClickable(
                    onClick = { onLoadSlot(slot.slotNumber) },
                    onLongClick = { onSaveToSlot(slot.slotNumber) }
                ),
                border = BorderStroke(0.5.dp, if (isActive) FameColors.ChampionsGold else Color.White.copy(alpha = 0.1f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!slot.isEmpty) {
                            IconButton(onClick = { onDeleteSlot(slot.slotNumber) }, modifier = Modifier.size(16.dp)) {
                                Icon(Icons.Default.Close, null, tint = if (isActive) Color.Black else Color.White.copy(alpha = 0.3f), modifier = Modifier.size(10.dp))
                            }
                        }
                        if (slot.isFavorite) {
                            IconButton(onClick = { onFavoriteSlot(slot.slotNumber) }, modifier = Modifier.size(16.dp)) {
                                Icon(Icons.Default.Star, null, tint = if (isActive) Color.Black else FameColors.ChampionsGold, modifier = Modifier.size(10.dp))
                            }
                        } else {
                            IconButton(onClick = { onFavoriteSlot(slot.slotNumber) }, modifier = Modifier.size(16.dp)) {
                                Icon(Icons.Default.StarBorder, null, tint = if (isActive) Color.Black else Color.White.copy(alpha = 0.3f), modifier = Modifier.size(10.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            if (slot.isEmpty) "SLOT ${slot.slotNumber}" else slot.formation,
                            style = AFMTextStyles.textXS.copy(fontSize = 8.sp),
                            color = if (isActive) Color.Black else Color.White,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LineupView(
    uiState: TacticsUiState,
    selectedPlayerId: Int?,
    draggingPlayerId: Int?,
    dragOffsets: Map<Int, Offset>,
    componentPositions: MutableMap<Int, Offset>,
    onPlayerSelect: (Int) -> Unit,
    onDragStart: (Int) -> Unit,
    onDrag: (Int, Offset) -> Unit,
    onDragEnd: (Int) -> Unit,
    onPlayerLongClick: (Int) -> Unit,
    onSetPitchPattern: (PitchPattern) -> Unit,
    onToggleDisplayMode: () -> Unit,
    onSelectFormation: (String) -> Unit,
    onSetMentality: (TacticsMentality) -> Unit,
    onSetFilter: (PlayerFilterType) -> Unit,
    onSetSortBy: (PlayerSortType) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            PitchArea(
                uiState = uiState,
                selectedPlayerId = selectedPlayerId,
                draggingPlayerId = draggingPlayerId,
                dragOffsets = dragOffsets,
                componentPositions = componentPositions,
                onPlayerSelect = onPlayerSelect,
                onDragStart = onDragStart,
                onDrag = onDrag,
                onDragEnd = onDragEnd
            )
            
            Column(
                modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                var showPatternSelector by remember { mutableStateOf(false) }
                PitchToolButton(Icons.Default.GridOn) { showPatternSelector = true }
                PitchToolButton(Icons.Default.Layers) { onToggleDisplayMode() }

                Box {
                    DropdownMenu(expanded = showPatternSelector, onDismissRequest = { showPatternSelector = false }) {
                        PitchPattern.entries.forEach { pattern ->
                            DropdownMenuItem(text = { Text(pattern.name) }, onClick = { onSetPitchPattern(pattern); showPatternSelector = false })
                        }
                    }
                }
            }

            // Quick Mentality Adjustments
            Row(
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TacticsMentality.entries.forEach { mentality ->
                    val isSelected = uiState.mentality == mentality
                    Surface(
                        onClick = { onSetMentality(mentality) },
                        color = if (isSelected) FameColors.ChampionsGold else Color.Black.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(2.dp),
                        modifier = Modifier.padding(horizontal = 4.dp),
                        border = BorderStroke(1.dp, if (isSelected) FameColors.ChampionsGold else Color.White.copy(alpha = 0.2f))
                    ) {
                        Text(
                            mentality.label,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = AFMTextStyles.textXS.copy(fontSize = 9.sp),
                            color = if (isSelected) Color.Black else Color.White,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            // Team Strength Insight
            var showFormationSelector by remember { mutableStateOf(false) }
            Box(modifier = Modifier.align(Alignment.TopStart)) {
                Surface(
                    modifier = Modifier.padding(12.dp).clickable { showFormationSelector = true },
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(2.dp)
                ) {
                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Bolt, null, tint = FameColors.ChampionsGold, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "STR: ${String.format(Locale.ROOT, "%.1f", uiState.teamStrength)}",
                            style = AFMTextStyles.textXS,
                            color = Color.White,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
                DropdownMenu(expanded = showFormationSelector, onDismissRequest = { showFormationSelector = false }) {
                    uiState.formations.forEach { formation ->
                        DropdownMenuItem(text = { Text(formation) }, onClick = { onSelectFormation(formation); showFormationSelector = false })
                    }
                }
            }

            // Tactical Feedback Warning
            uiState.insights.find { it.type == "NEGATIVE" }?.let { insight ->
                Surface(
                    modifier = Modifier.align(Alignment.BottomStart).padding(12.dp).fillMaxWidth(0.6f),
                    color = FameColors.AlertRed.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(2.dp)
                ) {
                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(insight.message.uppercase(), style = AFMTextStyles.textXS.copy(fontSize = 8.sp), color = Color.White, fontWeight = FontWeight.Black)
                    }
                }
            }
        }

        SquadBottomPanel(
            uiState = uiState,
            selectedPlayerId = selectedPlayerId,
            onPlayerSelect = onPlayerSelect,
            onPlayerLongClick = onPlayerLongClick,
            onSetFilter = onSetFilter,
            onSetSortBy = onSetSortBy
        )
    }
}

@Composable
fun PitchToolButton(icon: ImageVector, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = Color.Black.copy(alpha = 0.6f),
        shape = CircleShape,
        modifier = Modifier.size(36.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun TacticalBottomActions(onAutoSelect: () -> Unit, onSave: () -> Unit, onReset: () -> Unit) {
    Surface(
        color = FameColors.DeepNavyBlack,
        modifier = Modifier.fillMaxWidth().height(64.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onAutoSelect,
                modifier = Modifier.weight(1f).fillMaxHeight(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("AUTO-ALIGN", style = AFMTextStyles.textXS, color = Color.White, fontWeight = FontWeight.Black)
            }
            Button(
                onClick = onSave,
                modifier = Modifier.weight(1f).fillMaxHeight(),
                colors = ButtonDefaults.buttonColors(containerColor = FameColors.ChampionsGold),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("SAVE TACTIC", style = AFMTextStyles.textXS, color = Color.Black, fontWeight = FontWeight.Black)
            }
            IconButton(onClick = onReset, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Default.Refresh, null, tint = Color.White)
            }
        }
    }
}

@Composable
fun PitchArea(
    uiState: TacticsUiState,
    selectedPlayerId: Int?,
    draggingPlayerId: Int?,
    dragOffsets: Map<Int, Offset>,
    componentPositions: MutableMap<Int, Offset>,
    onPlayerSelect: (Int) -> Unit,
    onDragStart: (Int) -> Unit,
    onDrag: (Int, Offset) -> Unit,
    onDragEnd: (Int) -> Unit
) {
    val density = LocalDensity.current
    val formation = uiState.selectedFormation
    val positions = getPositionsForFormation(formation)
    val labels = getPositionLabelsForFormation(formation)

    val starters = uiState.teamPlayers
        .filter { uiState.startingXiIds.contains(it.id) }
        .sortedBy { uiState.startingXiIds.indexOf(it.id) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(4.dp)) {
        val fieldWidth = constraints.maxWidth.toFloat()
        val fieldHeight = constraints.maxHeight.toFloat()

        PitchCanvas(modifier = Modifier.fillMaxSize(), pattern = uiState.pitchPattern, mode = uiState.visualizationMode)

        starters.forEachIndexed { index, player ->
            val targetPos = positions.getOrNull(index) ?: (0.5f to 0.5f)
            val label = labels.getOrNull(index) ?: ""

            val isDragging = draggingPlayerId == player.id
            val dragOffset = dragOffsets[player.id] ?: Offset.Zero

            val animatedX by animateFloatAsState(
                targetValue = targetPos.first * fieldWidth,
                animationSpec = if (isDragging) snap() else tween(600, easing = FastOutSlowInEasing),
                label = "x"
            )
            val animatedY by animateFloatAsState(
                targetValue = targetPos.second * fieldHeight,
                animationSpec = if (isDragging) snap() else tween(600, easing = FastOutSlowInEasing),
                label = "y"
            )

            Box(
                modifier = Modifier
                    .onGloballyPositioned { componentPositions[player.id] = it.positionInWindow() }
                    .absoluteOffset {
                        IntOffset(
                            (animatedX + dragOffset.x).roundToInt() - with(density) { 32.dp.toPx().roundToInt() },
                            (animatedY + dragOffset.y).roundToInt() - with(density) { 42.dp.toPx().roundToInt() }
                        )
                    }
                    .zIndex(if (isDragging || selectedPlayerId == player.id) 10f else 1f)
                    .pointerInput(player.id) {
                        detectDragGestures(
                            onDragStart = { onDragStart(player.id) },
                            onDragEnd = { onDragEnd(player.id) },
                            onDragCancel = { onDragEnd(player.id) },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                onDrag(player.id, dragAmount)
                            }
                        )
                    }
            ) {
                PlayerOnPitchCard(
                    player = player,
                    assignedPosition = label,
                    isSelected = selectedPlayerId == player.id,
                    displayMode = uiState.displayMode,
                    roles = uiState.roles,
                    onClick = { onPlayerSelect(player.id) }
                )
            }
        }
    }
}

@Composable
fun SquadBottomPanel(
    uiState: TacticsUiState,
    selectedPlayerId: Int?,
    onPlayerSelect: (Int) -> Unit,
    onPlayerLongClick: (Int) -> Unit,
    onSetFilter: (PlayerFilterType) -> Unit,
    onSetSortBy: (PlayerSortType) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val currentList = if (selectedTab == 0) {
        uiState.teamPlayers.filter { uiState.substituteIds.contains(it.id) }
    } else {
        uiState.teamPlayers.filter { !uiState.startingXiIds.contains(it.id) && !uiState.substituteIds.contains(it.id) }
    }

    Column(modifier = Modifier.fillMaxWidth().height(200.dp).background(FameColors.DeepNavyBlack.copy(alpha = 0.5f))) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = FameColors.ChampionsGold,
                modifier = Modifier.weight(1f),
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = FameColors.ChampionsGold
                    )
                },
                divider = {}
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("BENCH", style = AFMTextStyles.textXS) })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("RESERVES", style = AFMTextStyles.textXS) })
            }

            var showSortSelector by remember { mutableStateOf(false) }
            Box {
                IconButton(onClick = { showSortSelector = true }) {
                    Icon(Icons.AutoMirrored.Filled.Sort, null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
                DropdownMenu(expanded = showSortSelector, onDismissRequest = { showSortSelector = false }) {
                    PlayerSortType.entries.forEach { sort ->
                        DropdownMenuItem(text = { Text(sort.label) }, onClick = { onSetSortBy(sort); showSortSelector = false })
                    }
                }
            }
        }

        // Quick Filters
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PlayerFilterType.entries.take(5).forEach { filter ->
                val isSelected = uiState.filter == filter
                FilterChip(
                    selected = isSelected,
                    onClick = { onSetFilter(filter) },
                    label = { Text(filter.label, style = AFMTextStyles.textXS.copy(fontSize = 7.sp)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = FameColors.ChampionsGold,
                        selectedLabelColor = Color.Black
                    ),
                    modifier = Modifier.height(24.dp)
                )
            }
        }

        LazyHorizontalGrid(
            rows = GridCells.Fixed(1),
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(8.dp)
        ) {
            items(currentList) { player ->
                CompactSquadCard(
                    player = player,
                    isSelected = selectedPlayerId == player.id,
                    onClick = { onPlayerSelect(player.id) },
                    onLongClick = { onPlayerLongClick(player.id) }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CompactSquadCard(player: PlayersEntity, isSelected: Boolean, onClick: () -> Unit, onLongClick: () -> Unit) {
    Surface(
        modifier = Modifier.width(80.dp).combinedClickable(onClick = onClick, onLongClick = onLongClick),
        color = if (isSelected) FameColors.ChampionsGold.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(2.dp),
        border = BorderStroke(1.dp, if (isSelected) FameColors.ChampionsGold else Color.White.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(player.name.uppercase(), style = AFMTextStyles.textXS.copy(fontSize = 8.sp), color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(player.position, style = AFMTextStyles.textXS.copy(fontSize = 7.sp), color = FameColors.ChampionsGold)
            Text(player.overallRating.toString(), style = AFMTextStyles.textSM, color = Color.White, fontWeight = FontWeight.Black)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PlaystyleTab(uiState: TacticsUiState, onUpdateStyle: (String) -> Unit, onUpdateSlider: (String, Int) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text("TACTICAL STYLE", style = AFMTextStyles.textSM, color = FameColors.ChampionsGold, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Playstyle.entries.forEach { style ->
                    val isSelected = uiState.selectedStyle == style.value
                    FilterChip(
                        selected = isSelected,
                        onClick = { onUpdateStyle(style.value) },
                        label = { Text(style.value.uppercase(), style = AFMTextStyles.textXS) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FameColors.ChampionsGold,
                            selectedLabelColor = Color.Black
                        )
                    )
                }
            }
        }
        item {
            Text("GLOBAL INSTRUCTIONS", style = AFMTextStyles.textSM, color = FameColors.ChampionsGold, fontWeight = FontWeight.Black)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                CompactTacticalSlider("TEMPO", uiState.tempo) { onUpdateSlider("tempo", it) }
                CompactTacticalSlider("WIDTH", uiState.width) { onUpdateSlider("width", it) }
                CompactTacticalSlider("DEPTH", uiState.depth) { onUpdateSlider("depth", it) }
                CompactTacticalSlider("PRESSING", uiState.pressIntensity) { onUpdateSlider("press", it) }
            }
        }
    }
}

@Composable
fun CompactTacticalSlider(label: String, value: Int, onValueChange: (Int) -> Unit) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = AFMTextStyles.textXS, color = Color.White.copy(alpha = 0.6f))
            Text(value.toString(), style = AFMTextStyles.textXS, color = FameColors.ChampionsGold, fontWeight = FontWeight.Black)
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = 0f..100f,
            colors = SliderDefaults.colors(thumbColor = FameColors.ChampionsGold, activeTrackColor = FameColors.ChampionsGold)
        )
    }
}

@Composable
fun FeedbackTab(uiState: TacticsUiState) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text("TACTICAL FEEDBACK", style = AFMTextStyles.textSM, color = FameColors.ChampionsGold, fontWeight = FontWeight.Black)
            Text("Based on your squad composition", style = AFMTextStyles.textXS, color = Color.White.copy(alpha = 0.6f))
        }

        if (uiState.feedback.strengths.isNotEmpty()) {
            item {
                Text("STRENGTHS DETECTED", style = AFMTextStyles.textXS, color = FameColors.PitchGreen, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                uiState.feedback.strengths.forEach { strength ->
                    FeedbackItem(text = strength, isPositive = true)
                }
            }
        }

        if (uiState.feedback.weaknesses.isNotEmpty()) {
            item {
                Text("WEAKNESSES DETECTED", style = AFMTextStyles.textXS, color = FameColors.AlertRed, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                uiState.feedback.weaknesses.forEach { weakness ->
                    FeedbackItem(text = weakness, isPositive = false)
                }
            }
        }

        if (uiState.feedback.formationSpecific.isNotEmpty()) {
            item {
                Text("REAL-TIME FEEDBACK (${uiState.selectedFormation})", style = AFMTextStyles.textXS, color = FameColors.ChampionsGold, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                uiState.feedback.formationSpecific.forEach { feedback ->
                    FeedbackItem(text = feedback, isPositive = null)
                }
            }
        }
    }
}

@Composable
fun FeedbackItem(text: String, isPositive: Boolean?) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
        val icon = when (isPositive) {
            true -> "✅"
            false -> "⚠️"
            null -> "├──"
        }
        Text(icon, style = AFMTextStyles.textXS, modifier = Modifier.width(24.dp))
        Text(text, style = AFMTextStyles.textXS, color = Color.White)
    }
}

@Composable
fun RolesTab(uiState: TacticsUiState, onUpdateRole: (String, Int) -> Unit) {
    val starters = uiState.teamPlayers.filter { uiState.startingXiIds.contains(it.id) }
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text("SET PIECE TAKERS", style = AFMTextStyles.textSM, color = FameColors.ChampionsGold, fontWeight = FontWeight.Black)
        }
        items(listOf("Captain", "Penalty Taker", "Free-kick Taker", "Corner Taker")) { role ->
            RoleSelectorRefined(role, starters) { onUpdateRole(role, it) }
        }
    }
}

@Composable
fun RoleSelectorRefined(role: String, players: List<PlayersEntity>, onSelect: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Surface(
            onClick = { expanded = true },
            color = Color.White.copy(alpha = 0.05f),
            shape = RoundedCornerShape(2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(role.uppercase(), style = AFMTextStyles.textXS, color = Color.White.copy(alpha = 0.6f))
                Icon(Icons.Default.ArrowDropDown, null, tint = Color.White)
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            players.forEach { player ->
                DropdownMenuItem(text = { Text(player.name) }, onClick = { onSelect(player.id); expanded = false })
            }
        }
    }
}

@Composable
fun TacticalGlassOverlay(
    uiState: TacticsUiState,
    onDismiss: () -> Unit,
    onSelectFormation: (String) -> Unit,
    onLineupStrategyChange: (LineupStrategy) -> Unit,
    onFilterChange: (String) -> Unit,
    onSubstitutionStrategyChange: (SubstitutionStrategy) -> Unit,
    onAutoSelect: () -> Unit,
    onSave: () -> Unit,
    onReset: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(color = Color.Black.copy(alpha = 0.9f), modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("TACTICAL SETTINGS", style = AFMTextStyles.textMD, color = FameColors.ChampionsGold, fontWeight = FontWeight.Black)
                
                Text("FORMATION", style = AFMTextStyles.textXS, color = Color.White.copy(alpha = 0.6f))
                var formationExpanded by remember { mutableStateOf(false) }
                Box {
                    Surface(
                        onClick = { formationExpanded = true },
                        color = Color.White.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(uiState.selectedFormation, modifier = Modifier.padding(12.dp), style = AFMTextStyles.textSM, color = Color.White)
                    }
                    DropdownMenu(expanded = formationExpanded, onDismissRequest = { formationExpanded = false }) {
                        uiState.formations.forEach { formation ->
                            DropdownMenuItem(text = { Text(formation) }, onClick = { onSelectFormation(formation); formationExpanded = false })
                        }
                    }
                }

                Text("LINEUP STRATEGY", style = AFMTextStyles.textXS, color = Color.White.copy(alpha = 0.6f))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LineupStrategy.entries.forEach { strategy ->
                        FilterChip(
                            selected = uiState.lineupStrategy == strategy,
                            onClick = { onLineupStrategyChange(strategy) },
                            label = { Text(strategy.name, style = AFMTextStyles.textXS) }
                        )
                    }
                }

                Text("SUBSTITUTION STRATEGY", style = AFMTextStyles.textXS, color = Color.White.copy(alpha = 0.6f))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SubstitutionStrategy.entries.forEach { strategy ->
                        FilterChip(
                            selected = uiState.substitutionStrategy == strategy,
                            onClick = { onSubstitutionStrategyChange(strategy) },
                            label = { Text(strategy.toString(), style = AFMTextStyles.textXS) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onAutoSelect, modifier = Modifier.fillMaxWidth()) { Text("AUTO-ALIGN") }
                Button(onClick = { onSave(); onDismiss() }, modifier = Modifier.fillMaxWidth()) { Text("SAVE GLOBAL TACTIC") }
                Button(onClick = { onReset(); onDismiss() }, modifier = Modifier.fillMaxWidth()) { Text("RESET") }
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("CLOSE") }
            }
        }
    }
}

@Preview(showBackground = true, name = "Tactics Screen Preview")
@Composable
fun TacticsScreenPreview() {
    val players = listOf(
        PlayersEntity(id = 1, name = "Aishi Salum Manula", age = 28, height = 185, position = "GK", positionCategory = "Goalkeeper", shirtNumber = 1, rating = 75, nationality = "Tanzania", teamId = 1, region = "East Africa", archetype = "Shot Stopper", primaryTrait = null, secondaryTrait = null, gameplayFocus = null, marketValue = 500000, futureRole = null, season = "2025/26", faceImage = null, imageUrl = null),
        PlayersEntity(id = 2, name = "Shomari Kapombe", age = 32, height = 175, position = "RB", positionCategory = "Defender", shirtNumber = 2, rating = 72, nationality = "Tanzania", teamId = 1, region = "East Africa", archetype = "Wing Back", primaryTrait = null, secondaryTrait = null, gameplayFocus = null, marketValue = 300000, futureRole = null, season = "2025/26", faceImage = null, imageUrl = null),
        PlayersEntity(id = 3, name = "Mohamed Hussein", age = 27, height = 170, position = "LB", positionCategory = "Defender", shirtNumber = 15, rating = 73, nationality = "Tanzania", teamId = 1, region = "East Africa", archetype = "Wing Back", primaryTrait = null, secondaryTrait = null, gameplayFocus = null, marketValue = 400000, futureRole = null, season = "2025/26", faceImage = null, imageUrl = null),
        PlayersEntity(id = 4, name = "Henock Inonga", age = 30, height = 188, position = "CB", positionCategory = "Defender", shirtNumber = 29, rating = 76, nationality = "DR Congo", teamId = 1, region = "Central Africa", archetype = "No-Nonsense CB", primaryTrait = null, secondaryTrait = null, gameplayFocus = null, marketValue = 800000, futureRole = null, season = "2025/26", faceImage = null, imageUrl = null),
        PlayersEntity(id = 5, name = "Che Fondoh Malone", age = 25, height = 186, position = "CB", positionCategory = "Defender", shirtNumber = 20, rating = 74, nationality = "Cameroon", teamId = 1, region = "Central Africa", archetype = "Ball Playing CB", primaryTrait = null, secondaryTrait = null, gameplayFocus = null, marketValue = 600000, futureRole = null, season = "2025/26", faceImage = null, imageUrl = null),
        PlayersEntity(id = 6, name = "Fabrice Ngoma", age = 30, height = 182, position = "CDM", positionCategory = "Midfielder", shirtNumber = 6, rating = 75, nationality = "DR Congo", teamId = 1, region = "Central Africa", archetype = "Anchor", primaryTrait = null, secondaryTrait = null, gameplayFocus = null, marketValue = 700000, futureRole = null, season = "2025/26", faceImage = null, imageUrl = null),
        PlayersEntity(id = 7, name = "Sadio Kanoute", age = 27, height = 185, position = "CM", positionCategory = "Midfielder", shirtNumber = 8, rating = 73, nationality = "Mali", teamId = 1, region = "West Africa", archetype = "Box-to-Box", primaryTrait = null, secondaryTrait = null, gameplayFocus = null, marketValue = 500000, futureRole = null, season = "2025/26", faceImage = null, imageUrl = null),
        PlayersEntity(id = 8, name = "Clatous Chama", age = 32, height = 178, position = "CAM", positionCategory = "Midfielder", shirtNumber = 17, rating = 78, nationality = "Zambia", teamId = 1, region = "Southern Africa", archetype = "Playmaker", primaryTrait = null, secondaryTrait = null, gameplayFocus = null, marketValue = 1200000, futureRole = null, season = "2025/26", faceImage = null, imageUrl = null),
        PlayersEntity(id = 9, name = "Kibu Denis", age = 25, height = 180, position = "RW", positionCategory = "Forward", shirtNumber = 38, rating = 71, nationality = "Tanzania", teamId = 1, region = "East Africa", archetype = "Winger", primaryTrait = null, secondaryTrait = null, gameplayFocus = null, marketValue = 400000, futureRole = null, season = "2025/26", faceImage = null, imageUrl = null),
        PlayersEntity(id = 10, name = "Saido Ntibazonkiza", age = 36, height = 172, position = "LW", positionCategory = "Forward", shirtNumber = 10, rating = 76, nationality = "Burundi", teamId = 1, region = "East Africa", archetype = "Inverted Winger", primaryTrait = null, secondaryTrait = null, gameplayFocus = null, marketValue = 300000, futureRole = null, season = "2025/26", faceImage = null, imageUrl = null),
        PlayersEntity(id = 11, name = "Freddy Michael", age = 26, height = 188, position = "ST", positionCategory = "Forward", shirtNumber = 27, rating = 72, nationality = "Nigeria", teamId = 1, region = "West Africa", archetype = "Target Man", primaryTrait = null, secondaryTrait = null, gameplayFocus = null, marketValue = 600000, futureRole = null, season = "2025/26", faceImage = null, imageUrl = null)
    )

    AFM2026Theme {
        TacticsScreenContent(
            uiState = TacticsUiState(
                isLoading = false,
                teamName = "Simba SC",
                selectedFormation = Formation.FORMATION_433.value,
                selectedStyle = Playstyle.TIKI_TAKA.value,
                teamPlayers = players,
                startingXiIds = (1..11).toList(),
                substituteIds = emptyList(),
                feedback = TacticalFeedback(
                    strengths = listOf("Strong CB depth (5 center-backs)", "Multiple aerial threats"),
                    weaknesses = listOf("Only 1 natural CDM", "GK depth limited"),
                    formationSpecific = listOf("Wide overload potential with 4-3-3", "Single pivot vulnerable to counter-attacks")
                ),
                savedTactics = listOf(
                    TacticSlot(1, "PRIMARY", "4-3-3", "Tiki-Taka", isFavorite = true, isEmpty = false),
                    TacticSlot(2, "COUNTER", "5-3-2", "Direct", isFavorite = false, isEmpty = false),
                    TacticSlot(3, "Slot 3"),
                    TacticSlot(4, "Slot 4")
                )
            ),
            onBack = {},
            onReset = {},
            onSave = {},
            onSelectFormation = {},
            onUpdateSlider = { _, _ -> },
            onFilterChange = {},
            onLineupStrategyChange = {},
            onSubstitutionStrategyChange = {},
            onSwapPlayers = { _, _ -> },
            onPlayerClick = {},
            onAutoSelect = {},
            onSetPitchPattern = {},
            onToggleDisplayMode = {},
            onUpdateRole = { _, _ -> },
            onUpdateStyle = {},
            onLoadSlot = {},
            onSaveToSlot = {},
            onFavoriteSlot = {},
            onDeleteSlot = {},
            onSetFilter = {},
            onSetSortBy = {},
            onSetMentality = {}
        )
    }
}
