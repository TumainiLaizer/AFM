package com.fameafrica.afm.ui.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.fameafrica.afm.ui.theme.AFMTextStyles
import com.fameafrica.afm.ui.theme.Dimensions
import com.fameafrica.afm.ui.theme.FameColors

/**
 * A reusable badge for displaying short status or ratings.
 */
@Composable
fun FameBadge(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = FameColors.DeepNavyBlack,
    textColor: Color = Color.White
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(Dimensions.micro))
            .background(backgroundColor)
            .padding(horizontal = Dimensions.xs, vertical = Dimensions.micro),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = AFMTextStyles.textXS,
            color = textColor
        )
    }
}
