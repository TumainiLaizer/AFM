package com.fameafrica.afm.ui.screen.cup

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fameafrica.afm.ui.theme.AFM2026Typography
import com.fameafrica.afm.ui.theme.FameColors

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CupDetailHeader(
    currentCupName: String,
    season: String,
    cupLogo: Any?,
    headerImage: Int,
    accentColor: Color,
    pagerState: PagerState,
    tabs: List<String>,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onTabSelected: (Int) -> Unit,
) {
    Box {
        Image(
            painter = painterResource(id = headerImage),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .alpha(0.6f),
            contentScale = ContentScale.Crop
        )
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Black.copy(alpha = 0.8f), Color.Transparent, Color.Black.copy(alpha = 0.9f))
                    )
                )
        )

        Column {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CupLogoImage(cupLogo)
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = currentCupName.uppercase(),
                                style = AFM2026Typography.headlineSmall.copy(fontSize = 18.sp),
                                color = FameColors.WarmIvory,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "SEASON $season",
                                style = AFM2026Typography.labelSmall,
                                color = accentColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = FameColors.WarmIvory
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = accentColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
            
            SecondaryScrollableTabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = Color.Transparent,
                contentColor = accentColor,
                edgePadding = 16.dp,
                indicator = {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(pagerState.currentPage),
                        color = accentColor
                    )
                },
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { onTabSelected(index) },
                        text = {
                            Text(
                                text = title,
                                style = AFM2026Typography.labelLarge,
                                color = if (pagerState.currentPage == index) accentColor else FameColors.WarmIvory.copy(alpha = 0.7f),
                                fontWeight = if (pagerState.currentPage == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CupLogoImage(logo: Any?) {
    when (logo) {
        is Int -> Image(
            painter = painterResource(id = logo),
            contentDescription = null,
            modifier = Modifier.size(32.dp).padding(end = 8.dp)
        )
        is String -> AsyncImage(
            model = logo,
            contentDescription = null,
            modifier = Modifier.size(32.dp).padding(end = 8.dp)
        )
    }
}
