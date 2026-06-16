package com.fameafrica.afm.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * A unified background component that automatically selects the appropriate
 * background based on the current app flavor (AFM or AFC).
 */
@Composable
fun FameBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    StadiumBackground(modifier = modifier, content = content)
}
