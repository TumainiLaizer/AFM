package com.fameafrica.afm.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

/**
 * Main Navigation Container
 * Wraps the entire app with bottom nav and top app bar
 */
@Composable
fun AFMNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Hide bottom nav on nested screens
    val showBottomNav = Screen.mainTabs.any { it.route == currentRoute }

    Scaffold(
        topBar = {
            FameTopAppBar(
                title = getScreenTitle(currentRoute),
                reputationLevel = "Local", // This would come from ViewModel
                onNotificationClick = { /* Navigate to notifications */ },
                onProfileClick = { /* Navigate to profile */ }
            )
        },
        bottomBar = {
            if (showBottomNav) {
                FameBottomNavigation(navController = navController)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            FameNavGraph(
                navController = navController,
                startDestination = Screen.Dashboard.route,
                onNewCareer = {
                    navController.navigate(Screen.CareerSetup.route)
                },
                onContinue = {
                    navController.navigate(Screen.Dashboard.withArgs("1"))
                },
                onLoadGame = {
                    // Navigate to Load Game
                },
                onSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }
    }
}

/**
 * Get screen title based on current route
 */
private fun getScreenTitle(route: String?): String {
    return when (route) {
        Screen.Dashboard.route -> "Dashboard"
        Screen.Squad.route -> "Squad"
        Screen.Transfers.route -> "Transfers"
        Screen.Club.route -> "Club"
        Screen.World.route -> "World"
        Screen.Tactics.route -> "Tactics"
        Screen.Training.route -> "Training"
        Screen.Scout.route -> "Scouting"
        Screen.Finances.route -> "Finances"
        Screen.Infrastructure.route -> "Infrastructure"
        Screen.History.route -> "Club History"
        Screen.Settings.route -> "Settings"
        Screen.Media.route -> "Media Center"
        Screen.National.route -> "National Team"
        else -> "AFM 2026"
    }
}
