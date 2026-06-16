package com.fameafrica.afm.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fameafrica.afm.ui.theme.AFMTextStyles
import com.fameafrica.afm.ui.theme.FameColors

/**
 * Premium broadcast-style header for Manager and Chairman careers.
 */
@Composable
fun FameProfileHeader(
    profileName: String,
    roleTitle: String,
    clubName: String,
    reputation: Int,
    confidence: Int,
    fanMood: Int,
    avatar: Any?,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        color = FameColors.HeaderDark.copy(alpha = 0.85f),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Portrait with Status Glow
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(FameColors.SurfaceMedium)
                        .border(1.dp, FameColors.TrophyGold.copy(alpha = 0.5f), CircleShape)
                ) {
                    AsyncImage(
                        model = avatar,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                
                // Active Pulse Indicator
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(FameColors.GrowthGreen)
                        .border(1.5.dp, FameColors.HeaderDark, CircleShape)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = profileName.uppercase(),
                        style = AFMTextStyles.textMD,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    ReputationStars(reputation)
                }
                Text(
                    text = "$roleTitle • $clubName".uppercase(),
                    style = AFMTextStyles.textXS,
                    color = FameColors.TrophyGold,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }

            // Compact Confidence Meters
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusMiniMeter(label = "BRD", value = confidence)
                StatusMiniMeter(label = "FAN", value = fanMood)
            }
        }
    }
}

@Composable
fun ReputationStars(level: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
        repeat(5) { i ->
            Icon(
                Icons.Default.Star,
                null,
                modifier = Modifier.size(10.dp),
                tint = if (i < level) FameColors.TrophyGold else Color.White.copy(alpha = 0.1f)
            )
        }
    }
}

@Composable
fun StatusMiniMeter(label: String, value: Int) {
    val color = when {
        value >= 75 -> FameColors.GrowthGreen
        value >= 40 -> FameColors.TrophyGold
        else -> FameColors.AlertRed
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = AFMTextStyles.textXS, fontSize = 7.sp, color = FameColors.MutedParchment)
        Box(
            modifier = Modifier
                .width(28.dp)
                .height(14.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color.Black.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "$value%",
                style = AFMTextStyles.textXS,
                fontSize = 8.sp,
                fontWeight = FontWeight.Black,
                color = color
            )
        }
    }
}
