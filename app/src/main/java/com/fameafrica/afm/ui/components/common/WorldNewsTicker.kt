package com.fameafrica.afm.ui.components.common

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fameafrica.afm.ui.theme.AFMTextStyles
import com.fameafrica.afm.ui.theme.FameColors

@Composable
fun WorldNewsTicker(
    newsItems: List<String>,
    modifier: Modifier = Modifier,
    backgroundColor: Color = FameColors.StadiumBlack,
    accentColor: Color = FameColors.ChampionsGold,
    speed: Int = 40 // Pixels per second
) {
    if (newsItems.isEmpty()) return

    val tickerText = remember(newsItems) { newsItems.joinToString("   |   ").uppercase() }
    var containerWidth by remember { mutableFloatStateOf(0f) }
    var textWidth by remember { mutableFloatStateOf(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "ticker")
    
    // Phase 1 Optimization: Use graphicsLayer for translation to avoid recomposition
    val xOffset by if (textWidth > 0f && containerWidth > 0f) {
        val totalDistance = textWidth + containerWidth
        val durationMillis = (totalDistance / speed * 1000).toInt()
        
        infiniteTransition.animateFloat(
            initialValue = containerWidth,
            targetValue = -textWidth,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "xOffset"
        )
    } else {
        remember { mutableFloatStateOf(0f) }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(24.dp)
            .background(backgroundColor)
            .onSizeChanged { containerWidth = it.width.toFloat() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Ticker Label
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .background(accentColor)
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "WORLD NEWS",
                style = AFMTextStyles.tickerText,
                color = Color.Black,
                fontWeight = FontWeight.Black
            )
        }

        // Scrolling Content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(horizontal = 4.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = tickerText,
                style = AFMTextStyles.tickerText,
                color = Color.White,
                modifier = Modifier
                    .graphicsLayer { translationX = xOffset }
                    .onSizeChanged { textWidth = it.width.toFloat() },
                softWrap = false
            )
        }
    }
}
