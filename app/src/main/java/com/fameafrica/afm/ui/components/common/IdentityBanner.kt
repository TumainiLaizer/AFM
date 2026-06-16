package com.fameafrica.afm.ui.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fameafrica.afm.R
import com.fameafrica.afm.ui.theme.AFMTextStyles
import com.fameafrica.afm.ui.theme.FameColors

@Composable
fun IdentityBanner(
    managerName: String,
    role: String,
    clubName: String,
    clubLogo: String?,
    reputation: Int,
    season: String,
    boardConfidence: Int,
    fanSentiment: Int,
    recentForm: List<String>
) {
    Surface(
        modifier = Modifier.fillMaxWidth().height(100.dp),
        color = Color.Black.copy(alpha = 0.8f),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Manager Portrait & Club Crest Overlay
            Box(modifier = Modifier.size(64.dp)) {
                Box(
                    modifier = Modifier.size(56.dp).clip(CircleShape).background(Color.Gray)
                ) {
                    if (clubLogo != null) {
                        AsyncImage(
                            model = clubLogo,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Default.Star, null, modifier = Modifier.size(40.dp).align(Alignment.Center), tint = Color.White.copy(0.2f))
                    }
                }
                Box(
                    modifier = Modifier.size(28.dp).align(Alignment.BottomEnd).clip(CircleShape).background(Color.Black).padding(2.dp)
                ) {
                    TeamLogo(clubName, modifier = Modifier.fillMaxSize())
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Info Section
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    managerName.uppercase(),
                    style = AFMTextStyles.textSM,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Text(
                    "$role • $clubName".uppercase(),
                    style = AFMTextStyles.textXXS,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    repeat(5) { i ->
                        Icon(
                            Icons.Default.Star,
                            null,
                            modifier = Modifier.size(10.dp),
                            tint = if (i < reputation) FameColors.TrophyGold else Color.Gray.copy(0.3f)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(season, style = AFMTextStyles.textXXS, color = Color.White.copy(0.5f))
                }
            }

            // Confidence & Stats
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ConfidenceWidget("BOARD", boardConfidence)
                ConfidenceWidget("FANS", fanSentiment)
            }
        }
    }
}

@Composable
fun ConfidenceWidget(label: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = AFMTextStyles.textXXS, fontWeight = FontWeight.Black, color = Color.White.copy(0.4f))
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(36.dp)) {
            CircularProgressIndicator(
                progress = { value / 100f },
                modifier = Modifier.fillMaxSize(),
                color = if (value > 70) FameColors.GrowthGreen else if (value > 40) FameColors.AfroSunOrange else FameColors.AlertRed,
                strokeWidth = 3.dp,
                trackColor = Color.White.copy(0.05f)
            )
            Text("$value%", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.White)
        }
    }
}

@Composable
fun DashboardStatItem(label: String, value: String, subValue: String? = null) {
    Column(
        modifier = Modifier
            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(2.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .widthIn(min = 80.dp)
    ) {
        Text(label.uppercase(), style = AFMTextStyles.textXXS, color = Color.White.copy(alpha = 0.5f), fontWeight = FontWeight.Bold)
        Text(value.uppercase(), style = AFMTextStyles.textSM, color = Color.White, fontWeight = FontWeight.Black)
        if (subValue != null) {
            Text(subValue.uppercase(), style = AFMTextStyles.textXXS, color = FameColors.GrowthGreen, fontWeight = FontWeight.Bold)
        }
    }
}
