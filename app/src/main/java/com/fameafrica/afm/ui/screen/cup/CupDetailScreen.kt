package com.fameafrica.afm.ui.screen.cup

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fameafrica.afm.data.database.entities.CupsEntity
import com.fameafrica.afm.ui.theme.*
import com.fameafrica.afm.utils.LeagueLogoUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CupDetailLayout(
    cupName: String,
    uiState: CupDrawUiState,
    onBack: () -> Unit,
    onRefresh: () -> Unit
) {
    val context = LocalContext.current
    val currentCupName = uiState.cup?.name ?: cupName
    val cupLogo = remember(currentCupName) { LeagueLogoUtils.getLeagueLogo(context, currentCupName) }
    
    val themeConfig = remember(currentCupName, uiState.cup?.type) {
        determineCupTheme(currentCupName, uiState.cup?.type)
    }

    val accentColor = themeConfig.accentColor
    val headerImage = themeConfig.headerRes

    val hasGroups = !uiState.groupStandings.isNullOrEmpty()
    val tabs = remember(hasGroups) {
        buildList {
            if (hasGroups) add("STANDINGS")
            add("BRACKETS")
            add("FIXTURES")
            add("STATS")
            add("TOTW")
            add("HISTORY")
        }
    }
    
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            CupDetailHeader(
                currentCupName = currentCupName,
                season = uiState.season,
                cupLogo = cupLogo,
                headerImage = headerImage,
                accentColor = accentColor,
                pagerState = pagerState,
                tabs = tabs,
                onBack = onBack,
                onRefresh = onRefresh,
                onTabSelected = { index ->
                    scope.launch { pagerState.animateScrollToPage(index) }
                }
            )
        },
        containerColor = Color.Transparent,
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
                .fillMaxSize()
        ) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = FameColors.AfroSunOrange)
                }
            } else {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    userScrollEnabled = true,
                    key = { page -> tabs.getOrNull(page) ?: page }
                ) { page ->
                    CupDetailTabContent(tabs.getOrNull(page), uiState)
                }
            }
        }
    }
}

@Composable
fun CupDetailScreen(
    cupName: String,
    onBack: () -> Unit,
    viewModel: CupDrawViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(cupName) {
        viewModel.loadCupData(cupName)
    }

    StadiumBackground {
        CupDetailLayout(
            cupName = cupName,
            uiState = uiState,
            onBack = onBack,
            onRefresh = { viewModel.refreshData() }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CAFChampionsLeagueDetailPreview() {
    AFM2026Theme {
        StadiumBackground {
            CupDetailLayout(
                cupName = "CAF Champions League",
                uiState = CupDrawUiState(
                    cup = CupsEntity(id = 1, name = "CAF Champions League", type = "Continental", countryId = null, country = "Africa", sponsor = "TotalEnergies", prizeMoney = 4000000, teamsInvolved = 16, rules = null, logo = null),
                    season = "2025/26",
                    isLoading = false
                ),
                onBack = {},
                onRefresh = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MuunganoCupDetailPreview() {
    AFM2026Theme {
        StadiumBackground {
            CupDetailLayout(
                cupName = "Muungano Cup",
                uiState = CupDrawUiState(
                    cup = CupsEntity(id = 4, name = "Muungano Cup", type = "Domestic", countryId = 1, country = "Tanzania", sponsor = "NBC", prizeMoney = 50000, teamsInvolved = 4, rules = null, logo = null),
                    season = "2025/26",
                    isLoading = false
                ),
                onBack = {},
                onRefresh = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EgyptianFACupDetailPreview() {
    AFM2026Theme {
        StadiumBackground {
            CupDetailLayout(
                cupName = "Egyptian FA Cup",
                uiState = CupDrawUiState(
                    cup = CupsEntity(
                        id = 15,
                        name = "Egyptian FA Cup",
                        type = "National",
                        countryId = 25,
                        country = "Egypt",
                        sponsor = null,
                        prizeMoney = 80000,
                        teamsInvolved = 18,
                        rules = "Format: Single-Elimination (Knockout)",
                        logo = null
                    ),
                    season = "2025/26",
                    isLoading = false
                ),
                onBack = {},
                onRefresh = {}
            )
        }
    }
}
