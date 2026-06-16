package com.fameafrica.afm.ui.screen.infrastructure

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fameafrica.afm.ui.theme.AFM2026Theme
import com.fameafrica.afm.ui.theme.FootballThemePreset

@Composable
fun InfrastructureScreen(
    onBack: () -> Unit,
    viewModel: InfrastructureViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val themePreset = FootballThemePreset.MANAGER_MODE

    AFM2026Theme(themePreset = themePreset) {
        InfrastructureContent(
            uiState = uiState,
            onBack = onBack,
            onUpgrade = { type -> viewModel.startUpgrade(type) }
        )
    }
}
