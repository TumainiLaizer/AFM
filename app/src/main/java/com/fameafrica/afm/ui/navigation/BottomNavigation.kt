package com.fameafrica.afm.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import com.fameafrica.afm.ui.theme.AFM2026Typography
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.fameafrica.afm.ui.theme.ClubThemeConfig
import com.fameafrica.afm.ui.theme.FameColors

/**
 * FAME Africa™ Bottom Navigation Bar
 * Optimized for Mobile-First 5-Tab System.
 */
@Composable
fun FameBottomNavigation(
    navController: NavController,
    modifier: Modifier = Modifier,
    clubTheme: ClubThemeConfig? = null
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val primaryColor = FameColors.PitchGreen
    val inactiveColor = Color.White.copy(alpha = 0.5f)

    val tabs = buildList {
        add(Screen.Dashboard)
        add(Screen.Squad)
        add(Screen.Matches)
        add(Screen.Transfers)
        add(Screen.Club)
        add(Screen.World)
    }


    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        color = FameColors.DeepNavyBlack,
        shadowElevation = 8.dp,
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEach { screen ->
                val isSelected = currentRoute?.startsWith(screen.baseRoute) == true
                
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            if (!isSelected) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                        .padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = screen.title,
                        tint = if (isSelected) primaryColor else inactiveColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = screen.title.uppercase(),
                        style = AFM2026Typography.labelSmall.copy(
                            fontSize = 8.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                            letterSpacing = 0.5.sp
                        ),
                        color = if (isSelected) primaryColor else inactiveColor
                    )
                }
            }
        }
    }
}

/**
 * Top App Bar with reputation-based gold shimmer
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FameTopAppBar(
    title: String,
    reputationLevel: String = "Local",
    onMenuClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    clubTheme: ClubThemeConfig? = null
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        color = FameColors.HeaderDark,
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Menu, null, tint = Color.White)
            }
            
            Text(
                text = title.uppercase(),
                style = AFM2026Typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    fontSize = 16.sp
                ),
                color = Color.White,
                modifier = Modifier.weight(1f).padding(start = 8.dp)
            )
            
            IconButton(onClick = onNotificationClick) {
                BadgedBox(
                    badge = {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(FameColors.TrophyGold, CircleShape)
                        )
                    }
                ) {
                    Icon(Icons.Default.Notifications, null, tint = FameColors.TrophyGold)
                }
            }
        }
    }
}
