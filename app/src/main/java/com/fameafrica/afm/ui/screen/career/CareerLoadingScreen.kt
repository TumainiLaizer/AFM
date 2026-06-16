package com.fameafrica.afm.ui.screen.career

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fameafrica.afm.ui.components.common.GlassPanel
import com.fameafrica.afm.ui.theme.*

@Composable
fun CareerLoadingScreen(
    status: String,
    progress: Float,
    onLoadingComplete: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    LaunchedEffect(progress) {
        if (progress >= 1.0f) {
            onLoadingComplete()
        }
    }

    StadiumBackground {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding(), // Edge-to-edge: handle system bars
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(32.dp)
            ) {
                // Professional Spinner
                Icon(
                    imageVector = Icons.Default.SportsSoccer,
                    contentDescription = null,
                    tint = FameColors.ChampionsGold,
                    modifier = Modifier
                        .size(80.dp)
                        .graphicsLayer { rotationZ = rotation }
                )

                Spacer(modifier = Modifier.height(48.dp))

                Text(
                    text = "INITIALIZING CAREER",
                    style = AFMTextStyles.tableHeader,
                    color = FameColors.ChampionsGold,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = status.uppercase(),
                    style = AFMTextStyles.statLabel,
                    color = FameColors.WarmIvory,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.height(40.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Professional Progress Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress.coerceIn(0f, 1f))
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(FameColors.PitchGreen, FameColors.ChampionsGold)
                                )
                            )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = AFMTextStyles.statValue,
                    color = FameColors.WarmIvory
                )

                Spacer(modifier = Modifier.height(64.dp))

                // UI Recommendation: Contextual Tip
                GlassPanel(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 12
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            "MANAGEMENT TIP",
                            style = AFMTextStyles.statLabel,
                            color = FameColors.ChampionsGold,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Scout the African youth leagues early to secure continental gems before European clubs arrive.",
                            style = AFMTextStyles.tableCell,
                            color = FameColors.WarmIvory.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CareerLoadingScreenPreview() {
    AFM2026Theme {
        CareerLoadingScreen(
            status = "Generating League Schedule...",
            progress = 0.45f,
            onLoadingComplete = {}
        )
    }
}
