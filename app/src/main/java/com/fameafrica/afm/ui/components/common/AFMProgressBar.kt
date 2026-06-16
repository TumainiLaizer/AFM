package com.fameafrica.afm.ui.components.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.fameafrica.afm.ui.theme.FameColors

@Composable
fun AFMProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = FameColors.TrophyGold,
    trackColor: Color = Color.White.copy(alpha = 0.1f)
) {
    LinearProgressIndicator(
        progress = { progress },
        modifier = modifier
            .fillMaxWidth()
            .height(4.dp)
            .clip(RoundedCornerShape(1.dp)),
        color = color,
        trackColor = trackColor,
        strokeCap = androidx.compose.ui.graphics.StrokeCap.Butt
    )
}
