package com.fameafrica.afm.ui.screen.match.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.fameafrica.afm.ui.screen.match.model.MomentumPoint
import com.fameafrica.afm.ui.theme.FameColors

@Composable
fun AttackMomentumGraph(
    momentumPoints: List<MomentumPoint>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(FameColors.SurfaceDark.copy(alpha = 0.5f))
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(vertical = 4.dp)) {
            val width = size.width
            val height = size.height
            val centerY = height / 2f
            
            if (momentumPoints.isEmpty()) return@Canvas
            
            val maxPoints = 90
            val xStep = width / maxPoints
            
            val pathHome = Path()
            val pathAway = Path()
            
            pathHome.moveTo(0f, centerY)
            pathAway.moveTo(0f, centerY)
            
            momentumPoints.forEach { point ->
                val x = point.minute * xStep
                if (point.value >= 0) {
                    val y = centerY - (point.value * (height / 2f))
                    pathHome.lineTo(x, y)
                    pathAway.lineTo(x, centerY)
                } else {
                    val y = centerY + (Math.abs(point.value) * (height / 2f))
                    pathAway.lineTo(x, y)
                    pathHome.lineTo(x, centerY)
                }
            }
            
            // Draw Home Momentum (Up)
            drawPath(
                path = pathHome,
                brush = Brush.verticalGradient(
                    colors = listOf(FameColors.PitchGreen.copy(alpha = 0.8f), FameColors.PitchGreen.copy(alpha = 0.1f)),
                    startY = 0f,
                    endY = centerY
                )
            )
            drawPath(path = pathHome, color = FameColors.PitchGreen, style = Stroke(1.dp.toPx()))
            
            // Draw Away Momentum (Down)
            drawPath(
                path = pathAway,
                brush = Brush.verticalGradient(
                    colors = listOf(FameColors.KenteRed.copy(alpha = 0.1f), FameColors.KenteRed.copy(alpha = 0.8f)),
                    startY = centerY,
                    endY = height
                )
            )
            drawPath(path = pathAway, color = FameColors.KenteRed, style = Stroke(1.dp.toPx()))
            
            // Center line
            drawLine(
                color = Color.White.copy(alpha = 0.2f),
                start = androidx.compose.ui.geometry.Offset(0f, centerY),
                end = androidx.compose.ui.geometry.Offset(width, centerY),
                strokeWidth = 1.dp.toPx()
            )
        }
    }
}
