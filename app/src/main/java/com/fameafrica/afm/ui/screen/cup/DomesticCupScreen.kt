package com.fameafrica.afm.ui.screen.cup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fameafrica.afm.ui.theme.StadiumBackground

import androidx.compose.ui.tooling.preview.Preview
import com.fameafrica.afm.ui.theme.AFM2026Theme
import com.fameafrica.afm.data.database.entities.CupsEntity

@Composable
fun DomesticCupScreen(
    cupName: String = "CRDB Federation Cup",
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
fun CRDBFederationCupPreview() {
    AFM2026Theme {
        StadiumBackground {
            CupDetailLayout(
                cupName = "CRDB Federation Cup",
                uiState = CupDrawUiState(
                    cup = CupsEntity(
                        id = 3,
                        name = "CRDB Federation Cup",
                        type = "Domestic",
                        countryId = 1,
                        country = "Tanzania",
                        sponsor = "CRDB Bank",
                        prizeMoney = 150000,
                        teamsInvolved = 64,
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
fun MuunganoCupPreview() {
    AFM2026Theme {
        StadiumBackground {
            CupDetailLayout(
                cupName = "Muungano Cup",
                uiState = CupDrawUiState(
                    cup = CupsEntity(
                        id = 4,
                        name = "Muungano Cup",
                        type = "Domestic",
                        countryId = 1,
                        country = "Tanzania",
                        sponsor = "NBC Bank",
                        prizeMoney = 50000,
                        teamsInvolved = 4,
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
fun FameAfricaCupPreview() {
    AFM2026Theme {
        StadiumBackground {
            CupDetailLayout(
                cupName = "FAME Africa Cup",
                uiState = CupDrawUiState(
                    cup = CupsEntity(
                        id = 5,
                        name = "FAME Africa Cup",
                        type = "Continental",
                        countryId = null,
                        country = "Africa",
                        sponsor = "FAME Africa",
                        prizeMoney = 1000000,
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
