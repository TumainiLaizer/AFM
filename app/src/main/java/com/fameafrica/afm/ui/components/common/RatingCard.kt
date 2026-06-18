package com.fameafrica.afm.ui.components.common

import androidx.compose.foundation.BorderStroke

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fameafrica.afm.ui.theme.AFMTextStyles
import com.fameafrica.afm.ui.theme.FameColors

@Composable
fun RatingCard(
    label: String?,
    value: String,
    rating: Int,
    modifier: Modifier = Modifier,
    isReputation: Boolean = false
) {
    val (color, labelText) = remember(rating, isReputation) {
        val catColor = when {
            rating >= 80 -> FameColors.TrophyGold
            rating >= 50 -> Color(0xFFC0C0C0) // Silver
            else -> Color(0xFFCD7F32) // Bronze
        }
        val lbl = label ?: if (isReputation) "REP" else "RATING"
        catColor to lbl
    }

    Surface(
        color = Color.Black.copy(alpha = 0.6f),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, color),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(labelText, style = AFMTextStyles.textXXS, color = color, fontSize = 6.sp, fontWeight = FontWeight.Bold)
            Text(value, style = AFMTextStyles.textXS, color = Color.White, fontWeight = FontWeight.Black)
        }
    }
}
