package com.fameafrica.afm.ui.screen.dashboard

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fameafrica.afm.domain.model.SimulationEvent
import com.fameafrica.afm.ui.components.common.ProcessingOverlay
import com.fameafrica.afm.ui.components.common.WorldNewsTicker
import com.fameafrica.afm.ui.screen.events.SeasonEventOverlay
import com.fameafrica.afm.ui.theme.*
import com.fameafrica.afm.utils.ImmersiveModeManager.immersiveRoot

@Composable
fun DashboardScreen(
    onNavigateToMatch: (Int) -> Unit,
    onNavigateToSquad: () -> Unit,
    onNavigateToTransfers: () -> Unit,
    onNavigateToClub: () -> Unit,
    onNavigateToWorld: () -> Unit,
    onNavigateToManager: () -> Unit,
    onNotificationsClick: () -> Unit,
    onNavigateToLeagueTable: (String) -> Unit,
    onNavigateToNews: () -> Unit,
    onNavigateToFinances: () -> Unit,
    onNavigateToScout: () -> Unit = {},
    onNavigateToTraining: () -> Unit = {},
    onNavigateToTactics: () -> Unit = {},
    onNavigateToSchedule: () -> Unit = {},
    onNavigateToCompetitions: () -> Unit = {},
    onNavigateToPreseason: () -> Unit = {},
    onNavigateToLeagueKickoff: () -> Unit = {},
    onNavigateToDeadlineDay: () -> Unit = {},
    onNavigateToSeasonReview: () -> Unit = {},
    onNavigateToAwardsGala: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel(),
    onHandleInterview: () -> Unit,
    onBoardRequest: () -> Unit,
    onNavigateToCup: (String) -> Unit,
    onNavigateToInfrastructure: () -> Unit,
    onLogout: () -> Unit,
    careerId: Int,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DashboardContent(
        uiState = uiState,
        onNavigateToMatch = onNavigateToMatch,
        onNavigateToSquad = onNavigateToSquad,
        onNavigateToTransfers = onNavigateToTransfers,
        onNavigateToClub = onNavigateToClub,
        onNavigateToWorld = onNavigateToWorld,
        onNavigateToManager = onNavigateToManager,
        onNotificationsClick = onNotificationsClick,
        onNavigateToLeagueTable = onNavigateToLeagueTable,
        onNavigateToNews = onNavigateToNews,
        onNavigateToFinances = onNavigateToFinances,
        onNavigateToScout = onNavigateToScout,
        onNavigateToTraining = onNavigateToTraining,
        onNavigateToTactics = onNavigateToTactics,
        onNavigateToSchedule = onNavigateToSchedule,
        onNavigateToCompetitions = onNavigateToCompetitions,
        onNavigateToPreseason = onNavigateToPreseason,
        onNavigateToLeagueKickoff = onNavigateToLeagueKickoff,
        onNavigateToDeadlineDay = onNavigateToDeadlineDay,
        onNavigateToSeasonReview = onNavigateToSeasonReview,
        onNavigateToAwardsGala = onNavigateToAwardsGala,
        onNavigateToCup = onNavigateToCup,
        onAdvanceDay = { viewModel.startAdvancement() },
        onSimulate = { viewModel.startAdvancement() },
        onDismissEvent = { viewModel.dismissImmersiveEvent(it) },
    )
}

@Composable
fun DashboardContent(
    uiState: DashboardUiState,
    onNavigateToMatch: (Int) -> Unit,
    onNavigateToSquad: () -> Unit,
    onNavigateToTransfers: () -> Unit,
    onNavigateToClub: () -> Unit,
    onNavigateToWorld: () -> Unit,
    onNavigateToManager: () -> Unit,
    onNotificationsClick: () -> Unit,
    onNavigateToLeagueTable: (String) -> Unit,
    onNavigateToNews: () -> Unit,
    onNavigateToFinances: () -> Unit,
    onNavigateToScout: () -> Unit,
    onNavigateToTraining: () -> Unit,
    onNavigateToTactics: () -> Unit,
    onNavigateToSchedule: () -> Unit,
    onNavigateToCompetitions: () -> Unit,
    onNavigateToPreseason: () -> Unit,
    onNavigateToLeagueKickoff: () -> Unit,
    onNavigateToDeadlineDay: () -> Unit,
    onNavigateToSeasonReview: () -> Unit,
    onNavigateToAwardsGala: () -> Unit,
    onNavigateToCup: (String) -> Unit,
    onAdvanceDay: () -> Unit,
    onSimulate: () -> Unit,
    onDismissEvent: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize().background(FameColors.StadiumBlack).immersiveRoot(),
            containerColor = FameColors.StadiumBlack,
            topBar = {
                WorldNewsTicker(
                    newsItems = uiState.liveHeadlines,
                    backgroundColor = Color.Black.copy(alpha = 0.8f),
                    accentColor = FameColors.TrophyGold
                )
            },
            bottomBar = {
                DashboardDualNavigation(
                    activeTab = "DASHBOARD",
                    onTabClick = { tab ->
                         when(tab) {
                             "INBOX" -> onNotificationsClick()
                             "SQUAD" -> onNavigateToSquad()
                             "TRANSFERS" -> onNavigateToTransfers()
                             "CLUB" -> onNavigateToClub()
                             "WORLD" -> onNavigateToWorld()
                             "SCHEDULE" -> onNavigateToSchedule()
                             "COMPETITIONS" -> onNavigateToCompetitions()
                             "TACTICS" -> onNavigateToTactics()
                             "TRAINING" -> onNavigateToTraining()
                             "SCOUTING" -> onNavigateToScout()
                             "MORE" -> onNavigateToManager()
                         }
                    },
                    unreadMessages = uiState.unreadMessages,
                    pendingTransfers = uiState.pendingTransfers
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = { if (uiState.isMatchToday) onNavigateToMatch(uiState.nextMatchId) else onAdvanceDay() },
                    containerColor = FameColors.TrophyGold,
                    contentColor = Color.Black,
                    shape = RoundedCornerShape(28.dp),
                    icon = {
                        if (uiState.isAdvancing) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.Black, strokeWidth = 2.dp)
                        } else {
                            Icon(if (uiState.isMatchToday) Icons.Default.SportsSoccer else Icons.Default.FastForward, null)
                        }
                    },
                    text = {
                        Text(
                            text = if (uiState.isMatchToday) "PLAY MATCH" else "CONTINUE",
                            fontWeight = FontWeight.Black
                        )
                    }
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 80.dp) // Extra padding for FAB
            ) {
                item {
                    DashboardManagerHeader(
                        uiState = uiState,
                        modifier = modifier
                    )
                }

                item {
                    ClubIdentitySection(
                        clubName = uiState.clubName,
                        leagueName = uiState.leagueName,
                        boardConfidence = uiState.boardConfidence,
                        fanConfidence = uiState.fanConfidence,
                        squadDepth = uiState.squadDepth
                    )
                }

                item {
                    if (!uiState.isPreseason || uiState.nextMatchCompetition.isNotEmpty()) {
                        DashboardNextMatchCard(
                            homeTeam = uiState.clubName,
                            awayTeam = uiState.nextMatchOpponent,
                            matchDate = uiState.nextMatchDate,
                            matchTime = uiState.nextMatchTime,
                            stadium = uiState.nextMatchStadium,
                            leagueMatchday = uiState.nextMatchCompetition,
                            isMatchToday = uiState.isMatchToday,
                            isAdvancing = uiState.isAdvancing,
                            onContinue = onAdvanceDay,
                            onSimulate = onSimulate,
                            onPlayMatch = { onNavigateToMatch(uiState.nextMatchId) }
                        )
                    }
                }

                item {
                    DashboardWidgetsSection(
                        uiState = uiState,
                        onNavigateToNews = onNavigateToNews,
                        onNavigateToLeague = { onNavigateToLeagueTable(uiState.leagueName) },
                        onNavigateToFinances = onNavigateToFinances
                    )
                }
            }
        }

        // Season Immersive Event Overlay
        uiState.pendingImmersiveEvent?.let { event ->
            SeasonEventOverlay(
                event = event,
                onContinue = {
                    onDismissEvent(true) // Auto-advance enabled
                },
                onNavigate = {
                    onDismissEvent(false) // Just dismiss, don't auto-advance
                    
                    // --- SMOOTH SEASON FLOW NAVIGATION (Workflow Integration) ---
                    when (event) {
                        is SimulationEvent.PreseasonStart -> {
                            onNavigateToPreseason()
                        }
                        is SimulationEvent.LeagueKickoff -> {
                            onNavigateToLeagueKickoff()
                        }
                        is SimulationEvent.CupMilestone -> {
                            onNavigateToCup(event.tournamentName)
                        }
                        is SimulationEvent.CAFGroupDraw -> {
                            onNavigateToCup("CAF Champions League")
                        }
                        is SimulationEvent.CAFKnockoutDraw -> {
                            onNavigateToCup(event.tournament)
                        }
                        is SimulationEvent.TransferWindowOpen -> {
                            onNavigateToTransfers()
                        }
                        is SimulationEvent.DeadlineDay -> {
                            onNavigateToTransfers()
                        }
                        is SimulationEvent.AwardsGala -> {
                            onNavigateToAwardsGala()
                        }
                        is SimulationEvent.SeasonEnd -> {
                            onNavigateToSeasonReview()
                        }
                        else -> { /* Stay on dashboard for other events */ }
                    }
                }
            )
        }

        if (uiState.isAdvancing) {
            ProcessingOverlay()
        }
    }
}

@Composable
fun DashboardWidgetsSection(
    uiState: DashboardUiState,
    onNavigateToNews: () -> Unit,
    onNavigateToLeague: () -> Unit,
    onNavigateToFinances: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Left Column
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            LiveHeadlinesWidget(headlines = uiState.liveHeadlines, onViewAll = onNavigateToNews)
            ClubFeedWidget(feedItems = uiState.feedItems, onViewAll = {})
        }

        // Right Column
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            DashboardLeagueStandings(standings = uiState.standings, onViewFull = onNavigateToLeague)
            BoardObjectivesWidget(onViewAll = {})
            FinancialSummaryWidget(
                transferBudget = uiState.formattedTransferBudget,
                wageBudget = uiState.formattedWageBudget,
                onViewMore = onNavigateToFinances
            )
        }
    }
}

@Preview(showBackground = true, device = Devices.PIXEL_5, showSystemUi = true)
@Composable
fun DashboardScreenV2Preview() {
    AFM2026Theme {
        DashboardContent(
            uiState = DashboardUiState(
                isLoading = false,
                managerName = "JULIUS MWANZA",
                managerNationality = "TANZANIA",
                clubName = "YOUNG AFRICANS SC",
                leagueName = "NBC PREMIER LEAGUE",
                bankBalance = "5.24M",
                premiumCurrency = "1,250",
                reputationValue = 78,
                managerLevel = 12,
                boardConfidence = 82,
                fanConfidence = 88,
                squadDepth = 27,
                unreadMessages = 5,
                liveHeadlines = listOf(
                    "Young Africans maintain top spot after hard-fought win",
                    "Simba SC drop points in surprise draw",
                    "CAF Champions League: Draw takes place tomorrow",
                    "Injury update: Midfielder out for 3 weeks"
                ),
                standings = listOf(
                    StandingUiModel(1, "Young Africans", 17, 24, 39),
                    StandingUiModel(2, "Simba SC", 17, 18, 36),
                    StandingUiModel(3, "Azam FC", 17, 11, 30),
                    StandingUiModel(4, "Coastal Union", 17, 5, 28),
                    StandingUiModel(5, "Singida Black", 17, 3, 27)
                ),
                formattedTransferBudget = "$ 2.34M",
                formattedWageBudget = "$ 1.21M",
                managerXp = 2450,
                managerMaxXp = 4000,
                pendingTransfers = 3
            ),
            onNavigateToMatch = {},
            onNavigateToSquad = {},
            onNavigateToTransfers = {},
            onNavigateToClub = {},
            onNavigateToWorld = {},
            onNavigateToManager = {},
            onNotificationsClick = {},
            onNavigateToLeagueTable = {},
            onNavigateToNews = {},
            onNavigateToFinances = {},
            onNavigateToScout = {},
            onNavigateToTraining = {},
            onNavigateToTactics = {},
            onNavigateToSchedule = {},
            onNavigateToCompetitions = {},
            onNavigateToPreseason = {},
            onNavigateToLeagueKickoff = {},
            onNavigateToDeadlineDay = {},
            onNavigateToSeasonReview = {},
            onNavigateToAwardsGala = {},
            onNavigateToCup = {},
            onAdvanceDay = {},
            onSimulate = {},
            onDismissEvent = {}
        )
    }
}
