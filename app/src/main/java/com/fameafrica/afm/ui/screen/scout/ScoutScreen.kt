package com.fameafrica.afm.ui.screen.scout

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.fameafrica.afm.R
import com.fameafrica.afm.data.database.entities.*
import com.fameafrica.afm.data.repository.ScoutAssignmentsDashboard
import com.fameafrica.afm.ui.components.common.*
import com.fameafrica.afm.ui.theme.*
import com.fameafrica.afm.utils.LeagueLogoUtils
import com.fameafrica.afm.utils.NationalityUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoutScreen(
    onBack: () -> Unit,
    onNavigateToPlayer: (Int) -> Unit,
    viewModel: ScoutViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showGuide by remember { mutableStateOf(false) }

    AFM2026Theme(themePreset = FootballThemePreset.MANAGER_MODE) {
        ScoutContent(
            uiState = uiState,
            onBack = onBack,
            onNavigateToPlayer = onNavigateToPlayer,
            onSearch = { viewModel.onEvent(ScoutEvent.SearchPlayers(it)) },
            onChangeTab = { viewModel.onEvent(ScoutEvent.ChangeTab(it)) },
            onScoutClick = { viewModel.onEvent(ScoutEvent.SelectScout(it)) },
            onClearSelectedScout = { viewModel.onEvent(ScoutEvent.ClearSelectedScout) },
            onAssignMission = { type, target, priority ->
                uiState.selectedScout?.id?.let { id ->
                    viewModel.onEvent(ScoutEvent.AssignMission(id, type, target, priority))
                }
            },
            onCancelMission = { viewModel.onEvent(ScoutEvent.CancelMission(it)) },
            onShowGuide = { showGuide = true }
        )

        if (showGuide) {
            ScoutingIntelligenceGuide(
                networkAdvice = uiState.networkAdvice,
                onDismiss = { showGuide = false }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoutContent(
    uiState: ScoutUiState,
    onBack: () -> Unit,
    onNavigateToPlayer: (Int) -> Unit,
    onSearch: (String) -> Unit,
    onChangeTab: (Int) -> Unit,
    onScoutClick: (Int) -> Unit,
    onClearSelectedScout: () -> Unit,
    onAssignMission: (MissionType, String, String) -> Unit,
    onCancelMission: (Int) -> Unit,
    onShowGuide: () -> Unit
) {
    var showMissionDialog by remember { mutableStateOf(false) }

    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                ) {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                (if (uiState.selectedScout != null) "SCOUT DOSSIER" else "CONTINENTAL NETWORK").uppercase(),
                                style = AFMTextStyles.textLG,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { if (uiState.selectedScout != null) onClearSelectedScout() else onBack() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = MaterialTheme.colorScheme.primary)
                            }
                        },
                        actions = {
                            IconButton(onClick = onShowGuide) {
                                Icon(Icons.Outlined.Psychology, "Network Intelligence", tint = MaterialTheme.colorScheme.primary)
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                    )
                }
            },
            containerColor = Color.Transparent,
            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                if (uiState.selectedScout != null) {
                    ScoutDetailView(
                        scout = uiState.selectedScout,
                        dashboard = uiState.scoutDashboard,
                        onAssignMissionClick = { showMissionDialog = true },
                        onCancelMission = onCancelMission
                    )
                } else {
                    ScoutMainView(
                        uiState = uiState,
                        onNavigateToPlayer = onNavigateToPlayer,
                        onSearch = onSearch,
                        onChangeTab = onChangeTab,
                        onScoutClick = onScoutClick
                    )
                }

                if (showMissionDialog && uiState.selectedScout != null) {
                    AssignMissionDialog(
                        onDismiss = { showMissionDialog = false },
                        onConfirm = { type, target, priority ->
                            onAssignMission(type, target, priority)
                            showMissionDialog = false
                        },
                        availableRegions = uiState.availableRegions,
                        availableCountries = uiState.availableCountries.map { it.nationality },
                        availableLeagues = uiState.availableLeagues.map { it.name }
                    )
                }
            }
        }
    }
}

@Composable
fun ScoutMainView(
    uiState: ScoutUiState,
    onNavigateToPlayer: (Int) -> Unit,
    onSearch: (String) -> Unit,
    onChangeTab: (Int) -> Unit,
    onScoutClick: (Int) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()

    val tabToPage = mapOf(0 to 0, 1 to 1, 3 to 2)
    val pageToTab = mapOf(0 to 0, 1 to 1, 2 to 3)

    LaunchedEffect(uiState.selectedTab) {
        val page = tabToPage[uiState.selectedTab] ?: 0
        if (pagerState.currentPage != page) {
            pagerState.animateScrollToPage(page)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        onChangeTab(pageToTab[pagerState.currentPage] ?: 0)
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        ScoutNetworkSummary(uiState)
        
        Spacer(modifier = Modifier.height(20.dp))
        ScoutSearchBar(onSearch = onSearch)
        
        Spacer(modifier = Modifier.height(24.dp))
        ScoutTabRow(uiState.selectedTab, onChangeTab)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
            beyondViewportPageCount = 1,
            userScrollEnabled = true
        ) { page ->
            when (page) {
                0 -> ScoutList(uiState.scouts, onScoutClick)
                1 -> ReportsList(uiState.allAssignments.filter { it.isCompleted })
                2 -> SearchResultsList(uiState.searchResults, onNavigateToPlayer)
            }
        }
    }
}

@Composable
fun ScoutNetworkSummary(uiState: ScoutUiState) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        ScoutStatBox("STAFF", uiState.scouts.size.toString(), Modifier.weight(1f))
        ScoutStatBox("ACTIVE", (uiState.allAssignments.count { it.isInProgress }).toString(), Modifier.weight(1f))
        ScoutStatBox("REPORTS", uiState.allAssignments.count { it.isCompleted }.toString(), Modifier.weight(1f))
    }
}

@Composable
fun ScoutingIntelligenceGuide(networkAdvice: List<String>, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(2.dp)
            ) {
                Text("OPTIMIZE NETWORK", fontWeight = FontWeight.Black, style = AFMTextStyles.textXS, color = MaterialTheme.colorScheme.onPrimary)
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Psychology, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Text("SCOUTING BRAIN", color = MaterialTheme.colorScheme.onSurface, style = AFMTextStyles.textLG, fontWeight = FontWeight.Black)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (networkAdvice.isNotEmpty()) {
                    SidebarCard {
                        Column {
                            Text("NETWORK ANALYSIS", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black, style = AFMTextStyles.textXS)
                            networkAdvice.forEach { advice ->
                                Text("• ${advice.uppercase()}", color = MaterialTheme.colorScheme.onSurface, style = AFMTextStyles.textXS, modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                    }
                }
                
                ScoutGuideItem(Icons.Default.Explore, "REGIONAL MISSIONS", "ASSIGN SCOUTS TO EAST, WEST, OR NORTH AFRICA TO DISCOVER LOCAL GEMS BEFORE THEY REACH THE GLOBAL MARKET.")
                ScoutGuideItem(Icons.Default.QueryStats, "JUDGING ABILITY", "A SCOUT'S ACCURACY DEPENDS ON THEIR JUDGING ABILITY. HIGH-RATED SCOUTS PROVIDE PRECISE POTENTIAL VALUES.")
                ScoutGuideItem(Icons.Default.Star, "WONDERKID TAGS", "ELITE PROSPECTS (POTENTIAL 85+) ARE FLAGGED AUTOMATICALLY IN YOUR REPORTS.")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(2.dp)
    )
}

@Composable
fun ScoutDetailView(
    scout: StaffEntity,
    dashboard: ScoutAssignmentsDashboard?,
    onAssignMissionClick: () -> Unit,
    onCancelMission: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // High-End Scout Header
        SidebarCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(contentAlignment = Alignment.Center) {
                    Surface(
                        modifier = Modifier.size(84.dp),
                        shape = RoundedCornerShape(2.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Image(
                            painter = painterResource(R.drawable.scout),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().alpha(0.8f)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(20.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(scout.name.uppercase(), style = AFMTextStyles.textLG, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.width(12.dp))
                        TeamLogo(scout.teamName, modifier = Modifier.size(24.dp))
                    }
                    Text(scout.roleDisplay.uppercase(), style = AFMTextStyles.textMD, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = scout.nationality?.let { NationalityUtils.getWavingFlagUrl(it) },
                            contentDescription = null,
                            modifier = Modifier.size(16.dp).clip(RoundedCornerShape(1.dp)),
                            error = painterResource(R.drawable.default_flag)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("${scout.nationality} • ${scout.age}Y".uppercase(), style = AFMTextStyles.textXS, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }
            }
        }

        // Metrics Section
        Column {
            ScoutSectionHeader("TECHNICAL PROFICIENCY")
            SidebarCard(borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    ScoutAbilityIconRow("JUDGING CURRENT ABILITY", scout.impactRating, MaterialTheme.colorScheme.primary, Icons.Default.Visibility)
                    ScoutAbilityIconRow("JUDGING POTENTIAL", (scout.impactRating * 0.95).toInt(), FameColors.AfroSunOrange,
                        Icons.AutoMirrored.Filled.TrendingUp
                    )
                    ScoutAbilityIconRow("REGIONAL KNOWLEDGE", scout.mentoringAbility, FameColors.TransferBlue, Icons.Default.Map)
                    ScoutAbilityIconRow("OPERATIONAL ADAPTABILITY", scout.adaptability, MaterialTheme.colorScheme.onSurface, Icons.Default.SettingsSuggest)
                }
            }
        }

        // Current Operation
        Column {
            ScoutSectionHeader("OPERATIONAL DEPLOYMENT")
            if (dashboard?.activeMission != null) {
                val mission = dashboard.activeMission
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val pulseScale by infiniteTransition.animateFloat(
                    initialValue = 1f, targetValue = 1.02f,
                    animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse), label = "pulse"
                )

                SidebarCard(
                    modifier = Modifier.scale(pulseScale),
                    borderWidth = 1.dp,
                    borderColor = FameColors.GrowthGreen.copy(alpha = 0.5f)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text(mission.missionType.uppercase(), style = AFMTextStyles.textXS, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
                            Surface(color = FameColors.GrowthGreen.copy(alpha = 0.1f), shape = RoundedCornerShape(2.dp), border = BorderStroke(0.5.dp, FameColors.GrowthGreen.copy(alpha = 0.4f))) {
                                Text("ACTIVE", modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), style = AFMTextStyles.textXS, color = FameColors.GrowthGreen, fontWeight = FontWeight.Black)
                            }
                        }
                        Text(mission.targetIdentifier.uppercase(), style = AFMTextStyles.textLG, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Black)
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.WifiTethering, null, tint = FameColors.GrowthGreen, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("DATABASE EXPANSION: ${mission.foundPlayersCount} PROSPECTS IDENTIFIED", style = AFMTextStyles.textXS, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { onCancelMission(mission.id) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = FameColors.AlertRed.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(2.dp),
                            border = BorderStroke(1.dp, FameColors.AlertRed.copy(alpha = 0.5f))
                        ) {
                            Text("TERMINATE MISSION", fontWeight = FontWeight.Black, style = AFMTextStyles.textXS, color = FameColors.AlertRed)
                        }
                    }
                }
            } else {
                SidebarCard(
                    modifier = Modifier.clickable { onAssignMissionClick() },
                    borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp)) {
                        Icon(Icons.Default.AddLocation, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(44.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("SCOUT IS CURRENTLY IDLE", style = AFMTextStyles.textMD, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Black)
                        Text("DEPLOY TO A HIGH-VALUE REGION TO BEGIN TALENT DISCOVERY", style = AFMTextStyles.textXS, color = Color.Gray, textAlign = TextAlign.Center)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun ScoutStatBox(label: String, value: String, modifier: Modifier) {
    SidebarCard(modifier = modifier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(label.uppercase(), style = AFMTextStyles.textXS, color = MaterialTheme.colorScheme.primary, letterSpacing = 1.5.sp, fontWeight = FontWeight.Black)
            Text(value, style = AFMTextStyles.textLG.copy(fontSize = 20.sp), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun ScoutSearchBar(onSearch: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    OutlinedTextField(
        value = text,
        onValueChange = { text = it; onSearch(it) },
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("SEARCH SCOUTED DATABASE...", color = Color.Gray, style = AFMTextStyles.textXS) },
        leadingIcon = { Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.primary) },
        shape = RoundedCornerShape(2.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
            focusedContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
            unfocusedContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.02f)
        )
    )
}

@Composable
fun ScoutTabRow(selectedTab: Int, onChangeTab: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(2.dp))
            .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(2.dp))
            .padding(4.dp)
    ) {
        ScoutTabItem("STAFF", selectedTab == 0, Modifier.weight(1f)) { onChangeTab(0) }
        ScoutTabItem("REPORTS", selectedTab == 1, Modifier.weight(1f)) { onChangeTab(1) }
        ScoutTabItem("DATABASE", selectedTab == 3, Modifier.weight(1f)) { onChangeTab(3) }
    }
}

@Composable
fun ScoutTabItem(label: String, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(2.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            style = AFMTextStyles.textXS,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun ScoutAbilityRow(label: String, value: Int, color: Color) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label.uppercase(), style = AFMTextStyles.textXS, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Text("$value%", style = AFMTextStyles.textXS, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Black)
        }
        Spacer(modifier = Modifier.height(4.dp))
        AFMProgressBar(
            progress = value / 100f,
            modifier = Modifier.fillMaxWidth(),
            color = color,
            trackColor = color.copy(alpha = 0.1f)
        )
    }
}

@Composable
fun ScoutAbilityIconRow(label: String, value: Int, color: Color, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Box(modifier = Modifier.weight(1f)) {
            ScoutAbilityRow(label, value, color)
        }
    }
}

@Composable
fun ScoutSectionHeader(text: String) {
    SidebarSectionTitle(title = text, modifier = Modifier.padding(bottom = 8.dp))
}

@Composable
fun ScoutGuideItem(icon: ImageVector, title: String, desc: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(modifier = Modifier.size(32.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(2.dp)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(title.uppercase(), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Black, style = AFMTextStyles.textXS)
            Text(desc.uppercase(), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), style = AFMTextStyles.textXS.copy(fontSize = 9.sp), lineHeight = 14.sp)
        }
    }
}

@Composable
fun ScoutList(scouts: List<StaffEntity>, onScoutClick: (Int) -> Unit) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp)
    ) {
        items(scouts, key = { it.id }) { scout ->
            SidebarCard(
                modifier = Modifier.clickable { onScoutClick(scout.id) }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(2.dp),
                        modifier = Modifier.size(52.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Image(
                                painter = painterResource(R.drawable.scout),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize().alpha(0.85f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(scout.name.uppercase(), style = AFMTextStyles.textMD, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Black)
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = scout.nationality?.let { NationalityUtils.getWavingFlagUrl(it) },
                                contentDescription = null,
                                modifier = Modifier.size(14.dp).clip(RoundedCornerShape(1.dp)),
                                error = painterResource(R.drawable.default_flag)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            TeamLogo(scout.teamName, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(scout.roleDisplay.uppercase(), style = AFMTextStyles.textXS, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Surface(
                            color = MaterialTheme.colorScheme.surface, 
                            shape = RoundedCornerShape(2.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                        ) {
                            Text(scout.impactRating.toString(), modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black, style = AFMTextStyles.textMD)
                        }
                        Text("EXPERTISE", style = AFMTextStyles.textXS.copy(fontSize = 7.sp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReportsList(reports: List<ScoutAssignmentsEntity>) {
    if (reports.isEmpty()) {
        ScoutEmptyStateView(Icons.Default.FindInPage, "NO SCOUT REPORTS", "Reports will appear once your scouts finish evaluating assigned players.")
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp)
        ) {
            items(reports, key = { it.id }) { report ->
                SidebarCard(
                    borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Player Mini-Avatar
                            Box(modifier = Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                                Surface(shape = RoundedCornerShape(2.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), modifier = Modifier.fillMaxSize(), border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))) {
                                    Image(painter = painterResource(R.drawable.player), contentDescription = null, contentScale = ContentScale.Crop)
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(report.playerName.uppercase(), style = AFMTextStyles.textMD, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Black)
                                Text("VERIFIED DOSSIER: ${report.scoutName.uppercase()}", style = AFMTextStyles.textXS, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                            Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(2.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)) {
                                Text(
                                    report.scoutRating?.toString() ?: "??",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Black,
                                    style = AFMTextStyles.textLG
                                )
                            }
                        }
                        
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), thickness = 0.5.dp)
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Verified, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(report.verdict?.uppercase() ?: "FINAL EVALUATION PENDING", style = AFMTextStyles.textXS, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
                        }
                        
                        if (report.strengths != null) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                report.strengths!!.split(",").forEach { strength ->
                                    Surface(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), shape = RoundedCornerShape(2.dp), border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))) {
                                        Text(strength.trim().uppercase(), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = AFMTextStyles.textXS.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.onSurface)
                                    }
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
fun SearchResultsList(results: List<PlayersEntity>, onNavigateToPlayer: (Int) -> Unit) {
    if (results.isEmpty()) {
        ScoutEmptyStateView(Icons.Default.TravelExplore, "GLOBAL DATABASE", "Search for any registered player across the continent.")
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp)
        ) {
            items(results, key = { it.id }) { player ->
                SidebarCard(
                    modifier = Modifier.clickable { onNavigateToPlayer(player.id) },
                    borderColor = getRatingColor(player.rating).copy(alpha = 0.2f)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Player Badge
                        Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                            Surface(shape = RoundedCornerShape(2.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), modifier = Modifier.fillMaxSize(), border = BorderStroke(1.dp, getRatingColor(player.rating).copy(alpha = 0.3f))) {
                                Image(
                                    painter = painterResource(if (player.rating >= 80) R.drawable.player_superstar else R.drawable.player),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(player.name.uppercase(), style = AFMTextStyles.textMD, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Black)
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(
                                    model = NationalityUtils.getWavingFlagUrl(player.nationality),
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp).clip(RoundedCornerShape(1.dp)),
                                    error = painterResource(R.drawable.default_flag)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("${player.position.uppercase()} • ".uppercase(), style = AFMTextStyles.textXS, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                TeamLogo(player.teamName, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(player.teamName.uppercase(), style = AFMTextStyles.textXS, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.surface, 
                            shape = RoundedCornerShape(2.dp),
                            border = BorderStroke(1.dp, getRatingColor(player.rating).copy(alpha = 0.6f))
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = player.rating.toString(), 
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp), 
                                    style = AFMTextStyles.textMD, 
                                    color = getRatingColor(player.rating), 
                                    fontWeight = FontWeight.Black
                                )
                                // DNA Indicator
                                if (player.potential > 85) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Elite Potential",
                                        modifier = Modifier.size(10.dp).padding(bottom = 2.dp),
                                        tint = Color(0xFFFFD700)
                                    )
                                }
                                if (player.isHomegrown) {
                                    Icon(
                                        imageVector = Icons.Default.Diversity3,
                                        contentDescription = "Domestic Talent",
                                        modifier = Modifier.size(10.dp).padding(bottom = 2.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
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
fun ScoutEmptyStateView(icon: ImageVector, title: String, subtitle: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
        Spacer(modifier = Modifier.height(24.dp))
        Text(title.uppercase(), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black, textAlign = TextAlign.Center, letterSpacing = 2.sp, style = AFMTextStyles.textLG)
        Spacer(modifier = Modifier.height(12.dp))
        Text(subtitle.uppercase(), color = Color.Gray, style = AFMTextStyles.textXS, textAlign = TextAlign.Center, lineHeight = 16.sp, letterSpacing = 1.sp)
    }
}

@Composable
fun AssignMissionDialog(
    onDismiss: () -> Unit,
    onConfirm: (MissionType, String, String) -> Unit,
    availableRegions: List<String>,
    availableCountries: List<String>,
    availableLeagues: List<String>
) {
    var selectedType by remember { mutableStateOf(MissionType.REGION) }
    var selectedTarget by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf("Normal") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("DEPLOY SCOUTING MISSION", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black, style = AFMTextStyles.textLG) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("SELECT STRATEGIC FOCUS", style = AFMTextStyles.textXS, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ScoutMissionTypeTab("REGION", selectedType == MissionType.REGION, Modifier.weight(1f)) { selectedType = MissionType.REGION; selectedTarget = "" }
                    ScoutMissionTypeTab("COUNTRY", selectedType == MissionType.COUNTRY, Modifier.weight(1f)) { selectedType = MissionType.COUNTRY; selectedTarget = "" }
                    ScoutMissionTypeTab("LEAGUE", selectedType == MissionType.LEAGUE, Modifier.weight(1f)) { selectedType = MissionType.LEAGUE; selectedTarget = "" }
                }

                Text("TARGET IDENTIFIER", style = AFMTextStyles.textXS, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
                val targets = when(selectedType) {
                    MissionType.REGION -> availableRegions
                    MissionType.COUNTRY -> availableCountries
                    MissionType.LEAGUE -> availableLeagues
                    else -> emptyList()
                }
                
                LazyColumn(modifier = Modifier.height(160.dp).fillMaxWidth().background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), RoundedCornerShape(2.dp)).border(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(2.dp))) {
                    items(targets) { target ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedTarget = target }
                                .background(if (selectedTarget == target) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent)
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                when(selectedType) {
                                    MissionType.COUNTRY -> {
                                        AsyncImage(
                                            model = NationalityUtils.getWavingFlagUrl(target),
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp).clip(RoundedCornerShape(1.dp)),
                                            error = painterResource(R.drawable.default_flag)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                    }
                                    MissionType.LEAGUE -> {
                                        val logo = LeagueLogoUtils.getLeagueLogo(LocalContext.current, target)
                                        if (logo is Int) {
                                            Icon(painterResource(logo), null, tint = Color.Unspecified, modifier = Modifier.size(20.dp))
                                        } else if (logo is String) {
                                            AsyncImage(model = logo, contentDescription = null, modifier = Modifier.size(20.dp))
                                        } else {
                                            Icon(Icons.Default.EmojiEvents, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                    }
                                    MissionType.REGION -> {
                                        Icon(Icons.Default.Public, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(12.dp))
                                    }
                                    else -> {}
                                }
                                Text(target.uppercase(), color = if (selectedTarget == target) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontWeight = if (selectedTarget == target) FontWeight.Black else FontWeight.Normal, style = AFMTextStyles.textXS, letterSpacing = 1.sp)
                            }
                        }
                    }
                }

                Text("PRIORITY LEVEL", style = AFMTextStyles.textXS, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ScoutPriorityChip("LOW", selectedPriority == "LOW") { selectedPriority = "LOW" }
                    ScoutPriorityChip("NORMAL", selectedPriority == "NORMAL") { selectedPriority = "NORMAL" }
                    ScoutPriorityChip("HIGH", selectedPriority == "HIGH") { selectedPriority = "HIGH" }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (selectedTarget.isNotEmpty()) onConfirm(selectedType, selectedTarget, selectedPriority) },
                enabled = selectedTarget.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(2.dp),
                modifier = Modifier.height(44.dp)
            ) {
                Text("INITIATE MISSION", fontWeight = FontWeight.Black, style = AFMTextStyles.textXS, color = MaterialTheme.colorScheme.onPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("ABORT", color = FameColors.AlertRed, fontWeight = FontWeight.Black, style = AFMTextStyles.textXS) }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(2.dp)
    )
}

@Composable
fun ScoutMissionTypeTab(label: String, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.clickable { onClick() },
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
        shape = RoundedCornerShape(2.dp),
        border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
    ) {
        Text(
            label,
            modifier = Modifier.padding(vertical = 10.dp),
            textAlign = TextAlign.Center,
            style = AFMTextStyles.textXS,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
fun ScoutPriorityChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        color = if (isSelected) FameColors.AfroSunOrange else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
        shape = RoundedCornerShape(2.dp),
        border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = AFMTextStyles.textXS,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            fontWeight = FontWeight.Black
        )
    }
}

private fun getRatingColor(rating: Int): Color {
    return when {
        rating >= 80 -> FameColors.GrowthGreen
        rating >= 70 -> FameColors.TrophyGold
        rating >= 60 -> FameColors.AfroSunOrange
        else -> FameColors.AlertRed
    }
}

// ============ PREVIEWS ============

private fun getMockScoutUiState() = ScoutUiState(
    isLoading = false,
    scouts = listOf(
        StaffEntity(id = 1, name = "Kassim Dewji", role = "CHIEF_SCOUT", staffType = "SCOUTING", teamId = 2, teamName = "Simba SC", specialization = "West Africa", impactRating = 88, adaptability = 85, mentoringAbility = 90, loyalty = 95, nationality = "Tanzania", age = 45),
        StaffEntity(id = 2, name = "Senzo Mazingisa", role = "SCOUT", staffType = "SCOUTING", teamId = 2, teamName = "Simba SC", specialization = "Domestic", impactRating = 78, adaptability = 70, mentoringAbility = 65, loyalty = 88, nationality = "South Africa", age = 52)
    ),
    allAssignments = listOf(
        ScoutAssignmentsEntity(id = 1, scoutId = 1, scoutName = "Kassim Dewji", playerId = 10, playerName = "F. Mayele", reportStatus = "Completed", scoutRating = 84, verdict = "Strong Recommendation", strengths = "Clinical finishing, Lightning pace", assignedDate = System.currentTimeMillis()),
        ScoutAssignmentsEntity(id = 2, scoutId = 1, scoutName = "Kassim Dewji", playerId = 11, playerName = "C. Chama", reportStatus = "In Progress", priority = "High", assignedDate = System.currentTimeMillis())
    ),
    activeMissions = listOf(
        ScoutingMissionsEntity(id = 1, scoutId = 1, scoutName = "Kassim Dewji", missionType = "REGION", targetIdentifier = "West Africa", status = "Active", foundPlayersCount = 12)
    ),
    networkAdvice = listOf(
        "Coverage Alert: You have no coverage in North Africa. This region is critical for technical playmakers.",
        "Operational Tip: Deploy Kassim Dewji to Nigeria for high-impact youth prospects."
    ),
    availableRegions = listOf("North Africa", "West Africa", "East Africa"),
    availableCountries = listOf(
        NationalitiesEntity(id = 1, nationality = "Nigeria", fifaCode = "NGR", flagPath = null, isAfrican = true, region = "West Africa"),
        NationalitiesEntity(id = 2, nationality = "Egypt", fifaCode = "EGY", flagPath = null, isAfrican = true, region = "North Africa")
    ),
    availableLeagues = listOf(
        LeaguesEntity(id = 1, name = "Tanzania Premier League", countryId = 1, country = "Tanzania", level = 1, sponsor = "NBC", prizeMoney = 1000000, logo = null)
    )
)

@Preview(showBackground = true, name = "Scouting - Network Overview")
@Composable
fun PreviewScoutNetwork() {
    AFM2026Theme {
        ScoutContent(
            uiState = getMockScoutUiState().copy(selectedTab = 0),
            onBack = {},
            onNavigateToPlayer = {},
            onSearch = {},
            onChangeTab = {},
            onScoutClick = {},
            onClearSelectedScout = {},
            onAssignMission = { _, _, _ -> },
            onCancelMission = {},
            onShowGuide = {}
        )
    }
}

@Preview(showBackground = true, name = "Scouting - Scout Profile")
@Composable
fun PreviewScoutProfile() {
    val mockState = getMockScoutUiState()
    AFM2026Theme {
        ScoutContent(
            uiState = mockState.copy(
                selectedScout = mockState.scouts.first(),
                scoutDashboard = ScoutAssignmentsDashboard(
                    totalAssignments = 5,
                    activeAssignments = 1,
                    completedAssignments = 4,
                    failedAssignments = 0,
                    highPriorityAssignments = 1,
                    averageScoutRating = 82.0,
                    activeList = mockState.allAssignments.filter { it.isInProgress },
                    recentCompleted = mockState.allAssignments.filter { it.isCompleted },
                    activeMission = mockState.activeMissions.first()
                )
            ),
            onBack = {},
            onNavigateToPlayer = {},
            onSearch = {},
            onChangeTab = {},
            onScoutClick = {},
            onClearSelectedScout = {},
            onAssignMission = { _, _, _ -> },
            onCancelMission = {},
            onShowGuide = {}
        )
    }
}

@Preview(showBackground = true, name = "Scouting - Brain Guide")
@Composable
fun PreviewScoutingGuide() {
    AFM2026Theme {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            ScoutingIntelligenceGuide(
                networkAdvice = getMockScoutUiState().networkAdvice,
                onDismiss = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Scouting - Mission Deployment")
@Composable
fun PreviewMissionDialog() {
    val mockState = getMockScoutUiState()
    AFM2026Theme {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            AssignMissionDialog(
                onDismiss = {},
                onConfirm = { _, _, _ -> },
                availableRegions = mockState.availableRegions,
                availableCountries = mockState.availableCountries.map { it.nationality },
                availableLeagues = mockState.availableLeagues.map { it.name }
            )
        }
    }
}
