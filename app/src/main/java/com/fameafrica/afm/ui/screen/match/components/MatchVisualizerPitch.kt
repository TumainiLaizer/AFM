package com.fameafrica.afm.ui.screen.match.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.fameafrica.afm.ui.screen.match.PitchCanvas
import com.fameafrica.afm.ui.screen.match.PitchPattern
import com.fameafrica.afm.ui.screen.match.model.AttackPhase
import com.fameafrica.afm.ui.screen.match.model.MatchVisualizerUiState
import com.fameafrica.afm.ui.theme.FameColors

@Composable
fun MatchVisualizerPitch(
    uiState: MatchVisualizerUiState,
    modifier: Modifier = Modifier,
    pitchPattern: PitchPattern = PitchPattern.VERTICAL_STRIPES
) {
    val infiniteTransition = rememberInfiniteTransition(label = "AttackGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Alpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(FameColors.StadiumBlack)
    ) {
        PitchCanvas(modifier = Modifier.fillMaxSize(), pattern = pitchPattern)
        
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            
            // Draw Attack Danger Zones
            when (uiState.attackPhase) {
                AttackPhase.DANGEROUS_ATTACK_HOME -> {
                    drawRect(
                        color = FameColors.PitchGreen.copy(alpha = glowAlpha),
                        topLeft = Offset(w * 0.7f, 0f),
                        size = Size(w * 0.3f, h)
                    )
                }
                AttackPhase.DANGEROUS_ATTACK_AWAY -> {
                    drawRect(
                        color = FameColors.KenteRed.copy(alpha = glowAlpha),
                        topLeft = Offset(0f, 0f),
                        size = Size(w * 0.3f, h)
                    )
                }
                else -> {}
            }
            
            // Draw Ball
            val ballX = uiState.ballPosition.x * w
            val ballY = uiState.ballPosition.y * h
            
            drawCircle(
                color = Color.White,
                radius = 4.dp.toPx(),
                center = Offset(ballX, ballY)
            )
            
            drawCircle(
                color = Color.White.copy(alpha = 0.3f),
                radius = 8.dp.toPx() * glowAlpha,
                center = Offset(ballX, ballY),
                style = Stroke(2.dp.toPx())
            )
        }
    }
}
