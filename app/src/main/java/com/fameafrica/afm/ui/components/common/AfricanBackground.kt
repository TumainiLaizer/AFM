package com.fameafrica.afm.ui.components.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.fameafrica.afm.R
import com.fameafrica.afm.ui.theme.FameColors

/**
 * Universal Background Decoration for AFM2026.
 * Injects African identity into every screen with subtle patterns and textures.
 */
@Composable
fun AfricanBackground(
    modifier: Modifier = Modifier,
    backgroundColor: Color = FameColors.SharedNeutralBase,
    showPatterns: Boolean = true,
    showMaasaiBorders: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        if (showPatterns) {
            // 1. Subtle Tribal Texture
            Image(
                painter = painterResource(id = R.drawable.ic_tribal_texture),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.03f),
                contentScale = ContentScale.Crop,
                colorFilter = ColorFilter.tint(Color.White)
            )

            // 2. African Geometric Pattern (Low Opacity)
            Image(
                painter = painterResource(id = R.drawable.african_pattern),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.05f), // Very subtle (5%)
                contentScale = ContentScale.Crop,
                colorFilter = ColorFilter.tint(Color.White)
            )

            // 3. Triangle Patterns in corners
            GeometricTriangleMotif(Alignment.TopEnd)
            GeometricTriangleMotif(Alignment.BottomStart)

            // 4. Chevron Patterns
            ChevronPattern(Alignment.Center)

            // 4. Kente-inspired Motifs
            KenteMotif(Alignment.TopStart)
            KenteMotif(Alignment.BottomEnd)
        }

        if (showMaasaiBorders) {
            // 3. Maasai-inspired side borders (Geometric beads pattern)
            MaasaiSideBorder(Alignment.CenterStart)
            MaasaiSideBorder(Alignment.CenterEnd)
        }

        // 4. Gradient overlay for depth and content readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.2f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.4f)
                        )
                    )
                )
        )

        // Content Layer
        Box(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}

@Composable
private fun BoxScope.GeometricTriangleMotif(alignment: Alignment) {
    // Triangle patterns
    Image(
        painter = painterResource(id = R.drawable.ic_pattern_triangles),
        contentDescription = null,
        modifier = Modifier
            .size(150.dp)
            .align(alignment)
            .offset(x = if (alignment == Alignment.TopEnd) 50.dp else (-50).dp, y = if (alignment == Alignment.TopEnd) (-50).dp else 50.dp)
            .alpha(0.04f),
        colorFilter = ColorFilter.tint(FameColors.ChampionsGold)
    )
}

@Composable
private fun BoxScope.ChevronPattern(alignment: Alignment) {
    // Africa Pattern
    Image(
        painter = painterResource(id = R.drawable.ic_tribal_texture),
        contentDescription = null,
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .align(alignment)
            .alpha(0.02f),
        contentScale = ContentScale.FillBounds,
        colorFilter = ColorFilter.tint(Color.White)
    )
}

@Composable
private fun BoxScope.KenteMotif(alignment: Alignment) {
    // Kente-inspired motifs
    Icon(
        painter = painterResource(id = R.drawable.ic_kente_motif),
        contentDescription = null,
        modifier = Modifier
            .size(80.dp)
            .align(alignment)
            .offset(x = if (alignment == Alignment.BottomCenter) (-20).dp else 20.dp, y = if (alignment == Alignment.TopStart) (-20).dp else 20.dp)
            .alpha(0.04f),
        tint = FameColors.KenteRed
    )
}

@Composable
private fun BoxScope.MaasaiSideBorder(alignment: Alignment) {
    // Thin vertical strip with geometric bead-like patterns
    // Enhanced with a subtle repeating pattern or dash effect
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(6.dp)
            .align(alignment)
            .alpha(0.15f)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        FameColors.KenteRed,
                        FameColors.ChampionsGold,
                        FameColors.PitchGreen,
                        FameColors.AfroSunOrange,
                        FameColors.KenteRed
                    )
                )
            )
    ) {
        // Subtle divider to separate border from content
        Box(modifier = Modifier.fillMaxHeight().width(1.dp).background(Color.White.copy(alpha = 0.2f)).align(if (alignment == Alignment.CenterStart) Alignment.End else Alignment.Start))
    }
}
