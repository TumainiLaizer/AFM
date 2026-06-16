package com.fameafrica.afm.ui.screen.cup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fameafrica.afm.data.database.entities.CupsEntity
import com.fameafrica.afm.ui.theme.AFM2026Theme
import com.fameafrica.afm.ui.theme.StadiumBackground

@Composable
fun ContinentalCupScreen(
    cupName: String = "CAF Champions League",
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
fun CAFChampionsLeaguePreview() {
    AFM2026Theme {
        StadiumBackground {
            CupDetailLayout(
                cupName = "CAF Champions League",
                uiState = CupDrawUiState(
                    cup = CupsEntity(
                        id = 1,
                        name = "CAF Champions League",
                        type = "Continental",
                        countryId = null,
                        country = "Africa",
                        sponsor = "TotalEnergies",
                        prizeMoney = 4000000,
                        teamsInvolved = 16,
                        rules = null,
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

@Preview(showBackground = true)
@Composable
fun CAFConfederationCupPreview() {
    AFM2026Theme {
        StadiumBackground {
            CupDetailLayout(
                cupName = "CAF Confederation Cup",
                uiState = CupDrawUiState(
                    cup = CupsEntity(
                        id = 2,
                        name = "CAF Confederation Cup",
                        type = "Continental",
                        countryId = null,
                        country = "Africa",
                        sponsor = "TotalEnergies",
                        prizeMoney = 2500000,
                        teamsInvolved = 16,
                        rules = null,
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
