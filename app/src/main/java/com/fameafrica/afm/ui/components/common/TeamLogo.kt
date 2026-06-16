package com.fameafrica.afm.ui.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.fameafrica.afm.ui.theme.ClubThemeManager

/**
 * Common Team Logo component using a shield with sharp half-and-half color split.
 * Ensures no gradient and fully saturated colors.
 */
@Composable
fun TeamLogo(teamName: String, modifier: Modifier = Modifier) {
    val theme = remember(teamName) { ClubThemeManager.getThemeForTeam(teamName) }

    Box(
        modifier = modifier
            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(2.dp))
            .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(2.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Shield,
            contentDescription = "$teamName logo",
            modifier = Modifier
                .fillMaxSize(0.7f)
                .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                .drawWithContent {
                    drawContent()

                    // Draw primary color on the left half (fully saturated)
                    drawRect(
                        color = theme.primaryColor.copy(alpha = 1f),
                        size = size.copy(width = size.width / 2f),
                        blendMode = BlendMode.SrcIn
                    )
                    // Draw secondary color on the right half (fully saturated)
                    drawRect(
                        color = theme.secondaryColor.copy(alpha = 1f),
                        topLeft = Offset(size.width / 2f, 0f),
                        size = size.copy(width = size.width / 2f),
                        blendMode = BlendMode.SrcIn
                    )
                },
            tint = Color.Unspecified
        )
    }
}