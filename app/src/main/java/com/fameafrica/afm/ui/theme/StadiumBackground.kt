package com.fameafrica.afm.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.fameafrica.afm.R
import com.fameafrica.afm.ui.components.common.AfricanBackground

/**
 * Immersive Stadium Background for AFM2026.
 * Enhanced with African identity decorations (Geometric patterns, tribal textures).
 */
@Composable
fun StadiumBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FameColors.SharedNeutralBase)
    ) {
        // Immersive Stadium Background
        Image(
            painter = painterResource(R.drawable.stadium_bg_2),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .blur(radiusX = 16.dp, radiusY = 16.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.3f),
                            Color.Black.copy(alpha = 0.6f),
                            Color.Black.copy(alpha = 0.9f)
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
