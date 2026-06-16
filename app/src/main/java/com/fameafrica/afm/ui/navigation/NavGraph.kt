package com.fameafrica.afm.ui.navigation

import android.R.attr.name
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.fameafrica.afm.ui.screen.career.CareerSetupScreen
import com.fameafrica.afm.ui.main.MainMenuScreen
import com.fameafrica.afm.ui.main.MainViewModel
import com.fameafrica.afm.ui.main.SplashScreen
import com.fameafrica.afm.ui.screen.board.BoardScreen
import com.fameafrica.afm.ui.screen.career.CareerScreen
import com.fameafrica.afm.ui.screen.career.PreseasonTourScreen
import com.fameafrica.afm.ui.screen.dashboard.DashboardScreen
import com.fameafrica.afm.ui.screen.squad.SquadScreen
import com.fameafrica.afm.ui.screen.transfers.TransfersScreen
import com.fameafrica.afm.ui.screen.club.ClubScreen
import com.fameafrica.afm.ui.screen.club.YouthAcademyScreen
import com.fameafrica.afm.ui.screen.career.CareerLoadingScreen
import com.fameafrica.afm.domain.manager.GameManager
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.fameafrica.afm.ui.screen.cup.CupDrawScreen
import com.fameafrica.afm.ui.screen.cup.CupDetailScreen
import com.fameafrica.afm.ui.screen.cup.DomesticCupScreen
import com.fameafrica.afm.ui.screen.cup.ContinentalCupScreen
import com.fameafrica.afm.ui.screen.drama.DramaScreen
import com.fameafrica.afm.ui.screen.fans.FansScreen
import com.fameafrica.afm.ui.screen.finances.FinancesScreen
import com.fameafrica.afm.ui.screen.history.HistoryScreen
import com.fameafrica.afm.ui.screen.infrastructure.InfrastructureScreen
import com.fameafrica.afm.ui.screen.league.LeagueTableScreen
import com.fameafrica.afm.ui.screen.main.LoadGameScreen
import com.fameafrica.afm.ui.screen.match.MatchScreen
import com.fameafrica.afm.ui.screen.media.MediaScreen
import com.fameafrica.afm.ui.screen.media.NewsScreen
import com.fameafrica.afm.ui.screen.media.NewsDetailScreen
import com.fameafrica.afm.ui.screen.national.NationalScreen
import com.fameafrica.afm.ui.screen.negotiation.NegotiationScreen
import com.fameafrica.afm.ui.screen.notifications.NotificationsScreen
import com.fameafrica.afm.ui.screen.scout.ScoutScreen
import com.fameafrica.afm.ui.screen.settings.SettingScreen
import com.fameafrica.afm.ui.screen.squad.PlayerDetailScreen
import com.fameafrica.afm.ui.screen.staff.StaffScreen
import com.fameafrica.afm.ui.screen.staff.StaffDetailScreen
import com.fameafrica.afm.ui.screen.tactics.TacticsScreen
import com.fameafrica.afm.ui.screen.training.TrainingScreen
import com.fameafrica.afm.ui.screen.manager.ManagerScreen
import com.fameafrica.afm.ui.screen.manager.JobCentreScreen
import com.fameafrica.afm.ui.screen.world.WorldScreen
import com.fameafrica.afm.ui.screen.shop.*
import com.fameafrica.afm.ui.screen.club.SponsorshipNegotiationScreen
import com.fameafrica.afm.ui.screen.events.*
import kotlinx.coroutines.launch

@Composable
fun FameNavGraph(
    navController: NavHostController,
    onNewCareer: () -> Unit,
    onContinue: (Int) -> Unit,
    onLoadGame: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier,
    startDestination: String = Screen.Splash.route,
    mainViewModel: MainViewModel = hiltViewModel()
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Main.route) {
            val scope = rememberCoroutineScope()
            var hasActiveCareer by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
            
            androidx.compose.runtime.LaunchedEffect(Unit) {
                hasActiveCareer = mainViewModel.getLastActiveCareerId() != null
            }

            MainMenuScreen(
                hasActiveCareer = hasActiveCareer,
                onNewCareerClick = onNewCareer,
                onContinueClick = {
                    scope.launch {
                        val lastId = mainViewModel.getLastActiveCareerId()
                        if (lastId != null) {
                            onContinue(lastId)
                        } else {
                            onNewCareer()
                        }
                    }
                },
                onLoadGameClick = onLoadGame,
                onSettingsClick = onSettings
            )
        }

        composable(Screen.ManagerCareer.route) {
            CareerScreen(
                onBack = { navController.popBackStack() },
                onNewCareer = { navController.navigate(Screen.CareerSetup.route) },
                onLoadCareer = { id ->
                    navController.navigate(Screen.GameMain.withArgs(id.toString())) {
                        popUpTo(Screen.Main.route) { inclusive = false }
                    }
                }
            )
        }

        composable("load_game") {
            LoadGameScreen(
                onBack = { navController.popBackStack() },
                onLoadSave = { id ->
                    navController.navigate(Screen.GameMain.withArgs(id.toString())) {
                        popUpTo(Screen.Main.route) { inclusive = false }
                    }
                }
            )
        }

        composable(Screen.CareerSetup.route) {
            CareerSetupScreen(
                onBack = { navController.popBackStack() },
                onStartCareer = { careerId ->
                    navController.navigate(Screen.GameMain.withArgs(careerId.toString())) {
                        popUpTo(Screen.Main.route) { inclusive = false }
                    }
                }
            )
        }

        composable(
            route = Screen.GameMain.route,
            arguments = listOf(
                navArgument("careerId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val careerId = backStackEntry.arguments?.getInt("careerId") ?: -1
            val initializationState by mainViewModel.initializationState.collectAsState()

            CareerLoadingScreen(
                status = when (initializationState) {
                    is GameManager.InitializationState.Loading -> (initializationState as GameManager.InitializationState.Loading).phase.name
                    is GameManager.InitializationState.Failed -> "Error: ${(initializationState as GameManager.InitializationState.Failed).error}"
                    else -> "Preparing Stadium..."
                },
                progress = when (initializationState) {
                    is GameManager.InitializationState.Loading -> (initializationState as GameManager.InitializationState.Loading).progress
                    is GameManager.InitializationState.Ready -> 1.0f
                    is GameManager.InitializationState.Failed -> -0.3f
                    else -> 0.05f
                },
                onLoadingComplete = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.GameMain.route) { inclusive = true }
                    }
                }
            )

            // Trigger initialization if not already started for this ID
            androidx.compose.runtime.LaunchedEffect(careerId) {
                mainViewModel.initializeWorldAndObserve(careerId)
            }
        }

        composable(Screen.Dashboard.route) {
            val gameState by mainViewModel.currentGameState.collectAsState()
            val careerId = (gameState as? GameManager.GameState.Active)?.context?.careerId ?: -1
            DashboardScreen(
                careerId = careerId,
                onNavigateToMatch = { id -> navController.navigate(Screen.Match.withArgs(id.toString())) },
                onNavigateToSquad = { navController.navigate(Screen.Squad.route) },
                onNavigateToTransfers = { navController.navigate(Screen.Transfers.route) },
                onNavigateToClub = { navController.navigate(Screen.Club.route) },
                onNavigateToWorld = { navController.navigate(Screen.World.route) },
                onHandleInterview = { navController.navigate(Screen.Media.route) },
                onBoardRequest = { navController.navigate(Screen.Board.route) },
                onNavigateToManager = { navController.navigate(Screen.Manager.route) },
                onNotificationsClick = { navController.navigate(Screen.Notifications.route) },
                onNavigateToLeagueTable = { name -> navController.navigate(Screen.LeagueTable.withArgs(name)) },
                onNavigateToNews = { navController.navigate(Screen.News.route) },
                onNavigateToFinances = { navController.navigate(Screen.Finances.route) },
                onNavigateToCup = { name -> navController.navigate(Screen.CupDetail.withArgs(name)) },
                onNavigateToInfrastructure = { navController.navigate(Screen.Infrastructure.route) },
                onNavigateToScout = { navController.navigate(Screen.Scout.route) },
                onNavigateToTraining = { navController.navigate(Screen.Training.route) },
                onNavigateToPreseason = { navController.navigate(Screen.PreseasonTour.route) },
                onNavigateToTactics = { navController.navigate(Screen.Tactics.route) },
                onNavigateToSchedule = { navController.navigate(Screen.Matches.route) },
                onNavigateToCompetitions = { navController.navigate(Screen.CupDraw.route) },
                onNavigateToLeagueKickoff = { navController.navigate(Screen.LeagueKickoff.route) },
                onNavigateToDeadlineDay = { navController.navigate(Screen.DeadlineDay.route) },
                onNavigateToSeasonReview = { navController.navigate(Screen.SeasonReview.route) },
                onNavigateToAwardsGala = { navController.navigate(Screen.AwardsGala.route) },
                onLogout = { navController.navigate(Screen.Main.route) { popUpTo(0) } }
            )
        }

        composable(Screen.Squad.route) {
            val gameState by mainViewModel.currentGameState.collectAsState()
            SquadScreen(
                currentGameState = gameState,
                onPlayerClick = { id -> navController.navigate(Screen.PlayerDetail.withArgs(id.toString())) },
                onTacticsClick = { navController.navigate(Screen.Tactics.route) },
                onTrainingClick = { navController.navigate(Screen.Training.route) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Transfers.route) {
            TransfersScreen(
                onNegotiationClick = { id -> navController.navigate(Screen.Negotiation.withArgs(id.toString())) },
                onPlayerClick = { id -> navController.navigate(Screen.PlayerDetail.withArgs(id.toString())) }
            )
        }

        composable(Screen.Club.route) {
            ClubScreen(
                onBack = { navController.popBackStack() },
                onFinancesClick = { navController.navigate(Screen.Finances.route) },
                onInfrastructureClick = { navController.navigate(Screen.Infrastructure.route) },
                onHistoryClick = { navController.navigate(Screen.History.route) },
                onRenegotiateSponsor = { id -> navController.navigate(Screen.SponsorNegotiation.withArgs(id.toString())) },
                onSearchSponsorsClick = { navController.navigate(Screen.Sponsorships.route) },
                onNavigateToYouth = { navController.navigate(Screen.YouthAcademy.route) }
            )
        }

        composable(Screen.YouthAcademy.route) {
            YouthAcademyScreen(
                onBack = { navController.popBackStack() },
                onPlayerClick = { id -> navController.navigate(Screen.PlayerDetail.withArgs(id.toString())) }
            )
        }

        composable(Screen.Manager.route) {
            ManagerScreen(
                onBack = { navController.popBackStack() },
                onNavigateToStaff = { navController.navigate(Screen.Staff.route) },
                onNavigateToJobCentre = { navController.navigate(Screen.JobCentre.route) }
            )
        }

        composable(Screen.JobCentre.route) {
            JobCentreScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Matches.route) {
            com.fameafrica.afm.ui.screen.match.MatchesTabScreen(
                onMatchClick = { id: Int -> navController.navigate(Screen.Match.withArgs(id.toString())) },
                fixturesRepository = hiltViewModel<com.fameafrica.afm.ui.screen.match.FixturesViewModel>().fixturesRepository // This is a bit ugly, but let's see
            )
        }

        composable(Screen.PreseasonTour.route) {
            PreseasonTourScreen(
                onBack = { navController.popBackStack() },
                onStartSeason = { navController.popBackStack() },
                onNavigateToTactics = { navController.navigate(Screen.Tactics.route) },
                onNavigateToPlayerDetail = { id -> navController.navigate(Screen.PlayerDetail.withArgs(id.toString())) },
                onNavigateToFinances = { navController.navigate(Screen.Finances.route) },
                onNavigateToInfrastructure = { navController.navigate(Screen.Infrastructure.route) },
                onNavigateToHistory = { navController.navigate(Screen.History.route) },
                onNavigateToSponsor = { id -> navController.navigate(Screen.Negotiation.withArgs(id.toString())) },
                onNavigateToYouth = { navController.navigate(Screen.YouthAcademy.route) }
            )
        }

        composable(Screen.LeagueKickoff.route) {
            LeagueKickoffScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.DeadlineDay.route) {
            DeadlineDayScreen(onBack = { 
                navController.popBackStack()
                navController.navigate(Screen.Transfers.route)
            })
        }

        composable(Screen.SeasonReview.route) {
            SeasonReviewScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.AwardsGala.route) {
            AwardsGalaScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = Screen.PlayerDetail.route,
            arguments = listOf(navArgument("playerId") { type = NavType.StringType })
        ) { backStackEntry ->
            val playerId = backStackEntry.arguments?.getString("playerId")?.toIntOrNull() ?: 0
            PlayerDetailScreen(
                playerId = playerId, 
                onBack = { navController.popBackStack() },
                onNavigateToTransfer = { navController.navigate(Screen.Negotiation.route) }
            )
        }

        composable(Screen.Tactics.route) { TacticsScreen(
            onBack = { navController.popBackStack() },
            onNavigateToPlayerDetails = { id -> navController.navigate(Screen.PlayerDetail.withArgs(id.toString())) },
            viewModel = hiltViewModel()
        ) }
        composable(Screen.Training.route) { TrainingScreen(onBack = { navController.popBackStack() }) }
        
        composable(Screen.Scout.route) { 
            ScoutScreen(
                onBack = { navController.popBackStack() },
                onNavigateToPlayer = { id -> navController.navigate(Screen.PlayerDetail.withArgs(id.toString())) }
            )
        }

        composable(
            route = Screen.Negotiation.route,
            arguments = listOf(navArgument("transferId") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("transferId")?.toIntOrNull() ?: 0
            NegotiationScreen(
                transferId = id,
                onBack = { navController.popBackStack() },
                onNavigateToPlayer = { pid -> navController.navigate(Screen.PlayerDetail.withArgs(pid.toString())) }
            )
        }

        composable(
            route = Screen.Finances.route,
            arguments = listOf(navArgument("tab") { type = NavType.IntType; defaultValue = 0 })
        ) { backStackEntry ->
            val tab = backStackEntry.arguments?.getInt("tab") ?: 0
            FinancesScreen(
                onBack = { navController.popBackStack() },
                onUpgradeInfrastructure = { navController.navigate(Screen.Infrastructure.route) },
                initialTab = tab
            )
        }

        composable(Screen.Infrastructure.route) {
            InfrastructureScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = Screen.LeagueTable.route,
            arguments = listOf(navArgument("leagueName") { type = NavType.StringType })
        ) { backStackEntry ->
            val name = backStackEntry.arguments?.getString("leagueName") ?: ""
            LeagueTableScreen(leagueName = name, onBack = { navController.popBackStack() })
        }

        composable(Screen.CupDraw.route) {
            CupDrawScreen(
                onBack = { navController.popBackStack() },
                onCupClick = { name -> navController.navigate(Screen.CupDetail.withArgs(name)) }
            )
        }

        composable(
            route = Screen.CupDetail.route,
            arguments = listOf(navArgument("cupName") { type = NavType.StringType })
        ) { backStackEntry ->
            val name = backStackEntry.arguments?.getString("cupName") ?: ""
            CupDetailScreen(cupName = name, onBack = { navController.popBackStack() })
        }

        composable(Screen.CAFChampionsLeague.route) {
            ContinentalCupScreen(cupName = "CAF Champions League", onBack = { navController.popBackStack() })
        }

        composable(Screen.CAFFederationCup.route) {
            ContinentalCupScreen(cupName = "CAF Confederation Cup", onBack = { navController.popBackStack() })
        }

        composable(Screen.DomesticCup.route) {
            DomesticCupScreen(cupName = "CRDB Federation Cup", onBack = { navController.popBackStack() })
        }

        composable(Screen.WorldCup.route) {
            ContinentalCupScreen(cupName = "World Cup", onBack = { navController.popBackStack() })
        }

        composable(Screen.AFCON.route) {
            ContinentalCupScreen(cupName = "AFCON", onBack = { navController.popBackStack() })
        }

        composable(
            route = Screen.Match.route,
            arguments = listOf(navArgument("matchId") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("matchId")?.toIntOrNull() ?: 0
            MatchScreen(
                matchId = id,
                onBack = { navController.popBackStack() },
                onNavigateToTactics = { navController.navigate(Screen.Tactics.route) }
            )
        }

        composable(Screen.Staff.route) { 
            StaffScreen(
                onBack = { navController.popBackStack() },
                onHireStaff = { navController.navigate(Screen.Scout.route) },
                onStaffClick = { id -> navController.navigate(Screen.StaffDetail.withArgs(id.toString())) }
            )
        }

        composable(
            route = Screen.StaffDetail.route,
            arguments = listOf(navArgument("staffId") { type = NavType.IntType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("staffId") ?: 0
            StaffDetailScreen(
                staffId = id,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Board.route) { 
            BoardScreen(
                onBack = { navController.popBackStack() },
                onNavigateToFFP = { navController.navigate(Screen.FFP.route) }
            ) 
        }

        composable(Screen.FFP.route) {
            com.fameafrica.afm.ui.screen.board.FFPDashboardScreen(
                onBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.World.route) { 
            WorldScreen(
                onNavigateToLeague = { n -> navController.navigate(Screen.LeagueTable.withArgs(n)) },
                onNavigateToCup = { n -> navController.navigate(Screen.CupDetail.withArgs(n)) },
                onNavigateToMatch = { id -> navController.navigate(Screen.Match.withArgs(id.toString())) }
            )
        }
        
        composable(Screen.News.route) {
            NewsScreen(
                onBack = { navController.popBackStack() },
                onArticleClick = { id -> navController.navigate(Screen.NewsDetail.withArgs(id.toString())) }
            )
        }

        composable(
            route = Screen.NewsDetail.route,
            arguments = listOf(navArgument("newsId") { type = NavType.IntType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("newsId") ?: 0
            NewsDetailScreen(newsId = id, onBack = { navController.popBackStack() })
        }

        composable(Screen.Media.route) {
            MediaScreen(
                onBack = { navController.popBackStack() },
                onNewsClick = { id -> navController.navigate(Screen.NewsDetail.withArgs(id.toString())) }
            )
        }

        composable(Screen.Settings.route) { SettingScreen(onBackClick = { navController.popBackStack() }) }
        composable(Screen.Notifications.route) { NotificationsScreen(onBack = { navController.popBackStack() }) }
        composable(Screen.History.route) { HistoryScreen(onBack = { navController.popBackStack() }) }
        composable(Screen.Drama.route) { DramaScreen(onBack = { navController.popBackStack() }) }
        composable(Screen.National.route) { 
            NationalScreen(
                onBack = { navController.popBackStack() },
                onTeamClick = { id -> }
            )
        }
        composable(
            route = Screen.Fans.route,
            arguments = listOf(
                navArgument("teamId") { type = NavType.IntType },
                navArgument("teamName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val teamId = backStackEntry.arguments?.getInt("teamId") ?: 0
            val teamName = backStackEntry.arguments?.getString("teamName") ?: ""
            FansScreen(teamId = teamId, teamName = teamName, onBack = { navController.popBackStack() })
        }

        composable(Screen.Shop.route) {
            ShopScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Sponsorships.route) {
            SponsorshipNegotiationScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = Screen.SponsorNegotiation.route,
            arguments = listOf(navArgument("sponsorId") { type = NavType.StringType })
        ) {
            SponsorshipNegotiationScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = Screen.AzamPesaPayment.route,
            arguments = listOf(navArgument("bundleId") { type = NavType.StringType })
        ) { backStackEntry ->
            val bundleId = backStackEntry.arguments?.getString("bundleId") ?: ""
            val bundle = coinBundles.find { it.id == bundleId }!!
            AzamPesaPaymentScreen(
                bundle = bundle,
                onSuccess = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.CardPayment.route,
            arguments = listOf(navArgument("bundleId") { type = NavType.StringType })
        ) { backStackEntry ->
            val bundleId = backStackEntry.arguments?.getString("bundleId") ?: ""
            val bundle = coinBundles.find { it.id == bundleId }!!
            CardPaymentScreen(
                bundle = bundle,
                onSuccess = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }
    }
}
