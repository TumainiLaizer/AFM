package com.fameafrica.afm.ui.shared

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.fameafrica.afm.ui.theme.Dimensions
import com.fameafrica.afm.ui.theme.FameColors

/**
 * A themed progress indicator for confidence or status bars.
 */
@Composable
fun FameProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = FameColors.TrophyGold,
    trackColor: Color = FameColors.DeepNavyBlack
) {
    LinearProgressIndicator(
        progress = { progress },
        modifier = modifier
            .fillMaxWidth()
            .height(Dimensions.playerStatBarHeight)
            .clip(RoundedCornerShape(Dimensions.micro)),
        color = color,
        trackColor = trackColor
    )
}
