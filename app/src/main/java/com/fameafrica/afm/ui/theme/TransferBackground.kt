package com.fameafrica.afm.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Transfer Background for AFM2026.
 * Redirects to the unified FameBackground which handles flavor isolation.
 */
@Composable
fun TransferBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {}
) {
    FameBackground(modifier = modifier, content = content)
}
