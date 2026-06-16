package com.fameafrica.afm.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Main App Background for AFM2026.
 * Redirects to the unified FameBackground which handles flavor isolation.
 */
@Composable
fun MainBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {}
) {
    FameBackground(modifier = modifier, content = content)
}
