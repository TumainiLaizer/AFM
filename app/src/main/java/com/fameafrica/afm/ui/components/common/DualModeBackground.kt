package com.fameafrica.afm.ui.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.fameafrica.afm.ui.theme.FameColors

/**
 * Dual Panel Background for Mode Selection.
 * Left: Manager Mode (Greenish/Pitch)
 * Right: Chairman Mode (Navy/Gold)
 */
@Composable
fun DualModeBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Manager Side (Left)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF1B5E20), // Pitch Green
                                Color(0xFF1B5E20).copy(alpha = 0.8f)
                            )
                        )
                    )
            )
            // Chairman Side (Right)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                FameColors.HeaderDark,
                                FameColors.DeepNavyBlack
                            )
                        )
                    )
            )
        }

        // Overlay gradient for depth
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.4f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.6f)
                        )
                    )
                )
        )

        AfricanBackground(
            backgroundColor = Color.Transparent,
            showPatterns = true,
            showMaasaiBorders = true
        ) {
            content()
        }
    }
}
