package com.fameafrica.afm.ui.main

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.fameafrica.afm.data.database.entities.ManagersEntity
import com.fameafrica.afm.domain.manager.GameManager
import com.fameafrica.afm.ui.components.FameBottomStatusBar
import com.fameafrica.afm.ui.navigation.FameNavGraph
import com.fameafrica.afm.ui.navigation.Screen
import com.fameafrica.afm.ui.navigation.SidebarDrawerContent
import com.fameafrica.afm.utils.ImmersiveModeManager
import com.fameafrica.afm.utils.ImmersiveModeManager.immersiveRoot
import com.fameafrica.afm.ui.screen.dashboard.*
import com.fameafrica.afm.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    navController: NavHostController = rememberNavController(),
    viewModel: MainViewModel = hiltViewModel(),
    careerId: Int = -1
) {
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val sidebarState by viewModel.sidebarState.collectAsStateWithLifecycle()
    val currentGameState by viewModel.currentGameState.collectAsStateWithLifecycle()
    val currentManager by viewModel.currentManager.collectAsStateWithLifecycle()
    val isWorldInitialized by viewModel.isWorldInitialized.collectAsStateWithLifecycle()
    val isProcessing by viewModel.isProcessing.collectAsStateWithLifecycle()
    val processingStatus by viewModel.processingStatus.collectAsStateWithLifecycle()

    val currentRoute = currentDestination?.route

    // Enable immersive mode
    ImmersiveModeManager.ImmersiveScreen()

    // 🔥 FIXED: Initialize world when careerId is provided or when needed
    LaunchedEffect(Unit) {  // Run once when composable first loads
        if (careerId != -1 && !isWorldInitialized) {
            Log.d("MainScreen", "Initializing world for careerId: $careerId")
            viewModel.initializeWorldAndObserve(careerId)
        } else if (careerId == -1 && !isWorldInitialized) {
            // Try to load last active career
            Log.d("MainScreen", "No careerId provided, attempting to load last active career")
            viewModel.initializeWorldAndObserve(null)
        }
    }

    // Show loading state while initializing
    if (!isWorldInitialized && careerId != -1) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator()
                Text(
                    text = "Loading your career...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        return
    }

    AFM2026Theme(
        clubTheme = sidebarState.clubTheme,
        themePreset = FootballThemePreset.MANAGER_MODE
    ) {
        MainScreenContent(
            drawerState = drawerState,
            sidebarState = sidebarState,
            currentGameState = currentGameState,
            currentManager = currentManager,
            currentRoute = currentRoute,
            isProcessing = isProcessing,
            processingStatus = processingStatus,
            onSidebarItemClick = { route ->
                scope.launch { drawerState.close() }
                navController.navigate(route) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            onTabSelected = { route ->
                navController.navigate(route) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            onOpenNotifications = {
                navController.navigate(Screen.Notifications.route)
            },
            onProcessNextTurn = { viewModel.processNextTurn() },
            content = {
                FameNavGraph(
                    navController = navController,
                    onNewCareer = {
                        navController.navigate(Screen.CareerSetup.route)
                    },
                    onContinue = { id ->
                        navController.navigate(Screen.GameMain.withArgs(id.toString()))
                    },
                    onLoadGame = {
                        navController.navigate("load_game")
                    },
                    onSettings = {
                        navController.navigate(Screen.Settings.route)
                    },
                    mainViewModel = viewModel
                )
            }
        )
    }
}

@Composable
fun MainScreenContent(
    drawerState: DrawerState,
    sidebarState: SidebarUiState,
    currentGameState: GameManager.GameState,
    currentManager: ManagersEntity?,
    currentRoute: String?,
    isProcessing: Boolean,
    processingStatus: String,
    onSidebarItemClick: (String) -> Unit,
    onTabSelected: (String) -> Unit,
    onOpenNotifications: () -> Unit,
    onProcessNextTurn: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val bottomBarRoutes = buildList {
        add(Screen.Dashboard.baseRoute)
        add(Screen.Squad.route)
        add(Screen.Matches.route)
        add(Screen.Transfers.route)
        add(Screen.Club.route)
        add(Screen.World.route)
    }

    val showBars = bottomBarRoutes.any { route ->
        currentRoute?.startsWith(route) == true
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        modifier = modifier.immersiveRoot(),
        drawerContent = {
            if (showBars) {
                ModalDrawerSheet(
                    drawerContainerColor = FameColors.DeepNavyBlack.copy(alpha = 0.98f),
                    drawerContentColor = FameColors.WarmIvory,
                    modifier = Modifier.width(300.dp),
                    windowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Start + WindowInsetsSides.Vertical),
                    drawerShape = RoundedCornerShape(0.dp)
                ) {
                    SidebarDrawerContent(
                        currentRoute = currentRoute,
                        gameState = currentGameState,
                        manager = currentManager,
                        managerName = sidebarState.managerName,
                        clubName = sidebarState.clubName,
                        reputation = sidebarState.reputation,
                        balance = sidebarState.balance,
                        nextMatch = sidebarState.nextMatch,
                        notifications = sidebarState.notificationsCount,
                        clubTheme = sidebarState.clubTheme,
                        onItemClick = onSidebarItemClick
                    )
                }
            }
        },
        gesturesEnabled = showBars
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize().padding(bottom = if (showBars) 80.dp else 0.dp)) {
                content()
            }

            if (showBars) {
                val selectedTabIndex = when {
                    currentRoute?.startsWith(Screen.Dashboard.baseRoute) == true -> 0
                    currentRoute == Screen.Squad.route -> 1
                    currentRoute == Screen.Matches.route -> 2
                    currentRoute == Screen.Transfers.route -> 3
                    currentRoute == Screen.Club.route -> 4
                    currentRoute == Screen.World.route -> 5
                    else -> -1
                }

                Column(modifier = Modifier.align(Alignment.BottomCenter)) {
                    FameBottomNav(
                        selectedTab = selectedTabIndex,
                        onTabSelected = { index: Int ->
                            onTabSelected(bottomBarRoutes[index])
                        }
                    )
                    
                    FameBottomStatusBar(
                        gameState = currentGameState,
                        isProcessing = isProcessing,
                        processingStatus = processingStatus
                    )
                }
            }

            if (isProcessing && showBars) {
                val gameDate = (currentGameState as? GameManager.GameState.Active)?.context?.gameDateDisplay ?: "Processing"
            }
        }
    }
}




@Composable
fun FameBottomNav(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    backgroundColor: Color = Color(0xFF011018)
) {
    val selectedColor = FameColors.TrophyGold
    val unselectedColor = Color.White.copy(alpha = 0.7f)

    Surface(
        color = backgroundColor,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val items = listOf(
                Triple(Icons.Default.Home, "HOME", 0),
                Triple(Icons.Default.Groups, "SQUAD", 1),
                Triple(Icons.Default.SportsSoccer, "MATCHES", 2),
                Triple(Icons.Default.SwapHoriz, "TRANSFERS", 3),
                Triple(Icons.Default.Shield, "CLUB", 4),
                Triple(Icons.Default.Public, "WORLD", 5)
            )

            items.forEachIndexed { index, (icon, label, tabIndex) ->
                val isSelected = selectedTab == tabIndex
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { onTabSelected(tabIndex) }
                        .padding(horizontal = 4.dp, vertical = 4.dp)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = Color.Transparent,
                        shape = RoundedCornerShape(4.dp),
                        border = if (isSelected) BorderStroke(1.dp, selectedColor) else null
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                modifier = Modifier.size(20.dp),
                                tint = if (isSelected) selectedColor else unselectedColor
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = label,
                                fontSize = 8.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                color = if (isSelected) selectedColor else unselectedColor,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                    
                    // Vertical divider
                    if (index < items.size - 1) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .width(0.5.dp)
                                .height(24.dp)
                                .background(Color.White.copy(alpha = 0.05f))
                        )
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true, widthDp = 360, heightDp = 1000)
@Composable
fun MainScreenPreview() {
    AFM2026Theme(
        themePreset = FootballThemePreset.MANAGER_MODE
    ) {
        MainScreenContent(
            drawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
            sidebarState = SidebarUiState(
                managerName = "Tumaini Joseph",
                clubName = "Young Africans",
                reputation = "National",
                balance = "4,015",
                nextMatch = "vs Simba SC (H)",
                notificationsCount = 3
            ),
            currentGameState = GameManager.GameState.Active(
                GameManager.GameContext(
                    careerId = 1,
                    managerId = 1,
                    teamId = 1,
                    teamName = "Young Africans",
                    managerName = "Tumaini Joseph",
                    season = "2025/26",
                    week = 10,
                    gameDateDisplay = "Oct 12, 2025",
                    currentDate = "2025-10-12",
                    isPreseason = false,
                    isTransferWindowOpen = false,
                    leagueName = "Tanzania Premier League",
                    domesticCupName = "Azam Sports Federation Cup",
                    saveName = "Yanga Career",
                    difficulty = "Normal",
                    managerAvatar = "coach_male_east.webp"
                )
            ),
            currentManager = ManagersEntity(name = "Tumaini Joseph"),
            currentRoute = Screen.Dashboard.baseRoute,
            isProcessing = false,
            processingStatus = "",
            onSidebarItemClick = {},
            onTabSelected = {},
            onOpenNotifications = {},
            onProcessNextTurn = {},
            content = {
                DashboardScreen(
                    onNavigateToMatch = {},
                    onNavigateToSquad = {},
                    onNavigateToTransfers = {},
                    onNavigateToClub = {},
                    onNavigateToWorld = {},
                    onNavigateToManager = {},
                    onNotificationsClick = {},
                    onNavigateToLeagueTable = {},
                    onNavigateToNews = {},
                    onNavigateToCup = {},
                    onNavigateToFinances = {},
                    onNavigateToInfrastructure = {},
                    onBoardRequest = {},
                    onHandleInterview = {},
                    onNavigateToPreseason = {},
                    careerId = 1,
                    onLogout = {}
                )
            }
        )
    }
}
