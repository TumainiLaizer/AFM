package com.fameafrica.afm.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fameafrica.afm.ui.theme.AFM2026Theme
import com.fameafrica.afm.ui.theme.AFMTextStyles
import com.fameafrica.afm.ui.theme.FameColors
import com.fameafrica.afm.utils.extensions.formatCurrency
import com.fameafrica.afm.utils.extensions.toTitleCase

/**
 * Custom Colors based on League/Country context.
 */
object LeagueColors {
    val Tanzania = Color(0xFF3F51B5) // Blue
    val Egypt = Color(0xFFCE1126) // Red
    val Morocco = Color(0xFFC1272D) // Deep Red
    val SouthAfrica = Color(0xFF007A4D) // Green
    val Nigeria = Color(0xFF008751) // Green
    val Ghana = Color(0xFFFCD116) // Gold
    val Ethiopia = Color(0xFF5DA941) // Light Green
    
    fun getLeagueColor(country: String): Color {
        return when (country.uppercase()) {
            "TANZANIA", "ZANZIBAR" -> Tanzania
            "EGYPT" -> Egypt
            "MOROCCO" -> Morocco
            "SOUTH AFRICA" -> SouthAfrica
            "NIGERIA" -> Nigeria
            "GHANA" -> Ghana
            "ETHIOPIA" -> Ethiopia
            else -> FameColors.PitchGreen
        }
    }
}

/**
 * Sponsorship Offer Card - FCM26 Style
 */
@Composable
fun SponsorshipOfferCard(
    sponsorName: String,
    logoUrl: String?,
    sponsorType: String,
    value: Long,
    duration: Int,
    performanceBonus: Long,
    canFundTransfers: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val cardColor = if (sponsorName == "FAME Africa™" || sponsorName == "AFM2026") {
        FameColors.DeepNavyBlack
    } else {
        Color(0xFFA90303)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        color = cardColor,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sponsor Logo
                Surface(
                    modifier = Modifier.size(48.dp),
                    color = Color.White.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    AsyncImage(
                        model = logoUrl ?: "file:///android_asset/sponsors_logos/fame_africa.png",
                        contentDescription = sponsorName,
                        modifier = Modifier.padding(4.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = sponsorName.toTitleCase(),
                        style = AFMTextStyles.textMD.copy(fontWeight = FontWeight.Black),
                        color = Color.White
                    )
                    Text(
                        text = sponsorType.replace("_", " ").toTitleCase(),
                        fontSize = 10.sp,
                        color = FameColors.MutedParchment,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Premium Value Badge
                Surface(
                    color = FameColors.TrophyGold.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, FameColors.TrophyGold.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = value.formatCurrency(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = FameColors.TrophyGold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Details Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SponsorDetailItem(Icons.Default.Star, "$duration SEASONS", "DURATION")
                SponsorDetailItem(Icons.AutoMirrored.Filled.TrendingUp, performanceBonus.formatCurrency(), "PERF. BONUS")
                if (canFundTransfers) {
                    SponsorDetailItem(Icons.Default.Business, "YES", "TRANSFER FUND")
                }
            }
        }
    }
}

@Composable
private fun SponsorDetailItem(icon: ImageVector, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = FameColors.MutedParchment, modifier = Modifier.size(10.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(value, fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.White)
        }
        Text(label, fontSize = 7.sp, color = FameColors.MutedParchment, fontWeight = FontWeight.Bold)
    }
}

/**
 * Prize Money / Reward Card
 */
@Composable
fun PrizeMoneyCard(
    competitionName: String,
    position: Int,
    amount: Long,
    country: String,
    modifier: Modifier = Modifier
) {
    val leagueColor = LeagueColors.getLeagueColor(country)
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        color = Color.Black.copy(alpha = 0.4f),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, leagueColor.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Position Badge
            Surface(
                modifier = Modifier.size(32.dp),
                color = leagueColor,
                shape = RoundedCornerShape(2.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "${position}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = competitionName.toTitleCase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "SEASON REWARD",
                    fontSize = 8.sp,
                    color = FameColors.MutedParchment,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = amount.formatCurrency(),
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = FameColors.GrowthGreen
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF050E14)
@Composable
fun SponsorshipOfferCardPreview() {
    AFM2026Theme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            SponsorshipOfferCard(
                sponsorName = "FAME Africa™",
                logoUrl = null,
                sponsorType = "MAIN_SPONSOR",
                value = 750000000L,
                duration = 3,
                performanceBonus = 150000000L,
                canFundTransfers = true
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            SponsorshipOfferCard(
                sponsorName = "Vodacom Tanzania",
                logoUrl = null,
                sponsorType = "STADIUM_SPONSOR",
                value = 250000000L,
                duration = 2,
                performanceBonus = 50000000L,
                canFundTransfers = false
            )
        }
    }
}
