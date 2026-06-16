package com.fameafrica.afm.ui.screen.squad

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.fameafrica.afm.R
import com.fameafrica.afm.ui.components.common.shimmerEffect
import com.fameafrica.afm.ui.shared.FameCard
import com.fameafrica.afm.ui.theme.*
import com.fameafrica.afm.utils.PlayerAssetUtils
import com.fameafrica.afm.utils.NationalityUtils
import com.fameafrica.afm.ui.common.formatCurrency
import com.fameafrica.afm.ui.common.formatCompactCurrency
import com.fameafrica.afm.ui.common.toHumanReadable
import com.fameafrica.afm.utils.formatters.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DynamicPlayerCard(
    player: PlayerUiModel,
    clubName: String,
    currencyContext: CurrencyFormatter.CurrencyContext? = null,
    onClick: () -> Unit
) {
    val clubColors = remember(clubName) { ClubThemeManager.getColorsForTeam(clubName) }
    val faceUrl = PlayerAssetUtils.getPlayerFace(player.id, player.nationality, clubName, clubColors)
    val positionColor = getPositionColor(player.positionCategory)

    FameCard(
        modifier = Modifier
            .fillMaxWidth()
            .shimmerEffect(show = player.form >= 9 || player.potential > player.rating + 10)
            .clickable { onClick() },
        containerColor = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, positionColor.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Player Face with attached Flag
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(faceUrl)
                        .size(128, 128) // Phase 12 Optimization: Cap decoder size
                        .crossfade(true)
                        .build(),
                    contentDescription = player.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                    placeholder = painterResource(R.drawable.default_player)
                )
                
                // Small attached flag
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(NationalityUtils.getFlagUrl(player.nationality))
                        .size(48, 48) // Optimized flag size
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.TopEnd)
                        .padding(2.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .border(0.5.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(1.dp))
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Name (Flag removed from here as it's now on the face)
                Text(
                    text = player.displayName.toHumanReadable().uppercase(),
                    style = AFMTextStyles.textSM.copy(fontWeight = FontWeight.Black, letterSpacing = 0.5.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Stats Row: Age, P, G, A, Star
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "${player.age}",
                        style = AFMTextStyles.textXS,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    PlayerStatMini(label = "P", value = player.appearances.toString())
                    PlayerStatMini(iconRes = R.drawable.ic_goal_scored, value = player.goals.toString())
                    PlayerStatMini(iconRes = R.drawable.ic_assist, value = player.assists.toString())
                    
                    if (player.manOfMatch > 0) {
                        Image(
                            painter = painterResource(R.drawable.ic_star_gold),
                            contentDescription = "MOTM",
                            modifier = Modifier.size(9.dp)
                        )
                        Text(
                            text = player.manOfMatch.toString(),
                            style = AFMTextStyles.textXS.copy(fontSize = 9.sp),
                            color = FameColors.TrophyGold
                        )
                    }

                    // Status Icons
                    if (player.isCaptain) {
                        Image(painter = painterResource(R.drawable.ic_captain), contentDescription = "C", modifier = Modifier.size(11.dp))
                    } else if (player.isViceCaptain) {
                        Image(painter = painterResource(R.drawable.ic_vice_captain), contentDescription = "VC", modifier = Modifier.size(11.dp))
                    }
                    
                    if (player.isInjured) {
                        Icon(Icons.Default.MedicalServices, null, tint = FameColors.AlertRed, modifier = Modifier.size(11.dp))
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Position, Morale, and Financials
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = player.position,
                        style = AFMTextStyles.textXS.copy(fontWeight = FontWeight.Bold),
                        color = positionColor
                    )
                    player.secondaryPosition?.let {
                        Text(
                            text = "/$it",
                            style = AFMTextStyles.textXS,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(player.reactionEmoji, fontSize = 11.sp)
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    Text(
                        text = "VALUE: ${formatCompactCurrency(player.marketValue.toLong(), context = currencyContext)}",
                        style = AFMTextStyles.textXXS.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SALARY: ${formatCompactCurrency(player.wage.toLong(), context = currencyContext)}/mo",
                        style = AFMTextStyles.textXXS.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
                
                Spacer(modifier = Modifier.height(2.dp))
                
                // Fatigue Bar
                FatigueBar(condition = player.condition)
            }

            // Right side: OVR (Big) and Form Arrow
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = player.rating.toString(),
                    style = AFMTextStyles.textLG.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp
                    ),
                    color = getSquadRatingColor(player.rating)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FormArrowIndicator(player.form)
                    if (player.potential > player.rating + 5) {
                        Spacer(modifier = Modifier.width(1.dp))
                        Icon(
                            Icons.AutoMirrored.Filled.TrendingUp,
                            null,
                            tint = FameColors.GrowthGreen,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PlayerStatMini(label: String? = null, iconRes: Int? = null, value: String) {
    val contentColor = MaterialTheme.colorScheme.onSurface
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        if (iconRes != null) {
            Image(painter = painterResource(iconRes), contentDescription = null, modifier = Modifier.size(10.dp))
        } else if (label != null) {
            Text(text = label, style = AFMTextStyles.textXS.copy(fontSize = 9.sp), color = contentColor.copy(alpha = 0.5f))
        }
        Text(text = value, style = AFMTextStyles.textXS.copy(fontSize = 9.sp), color = contentColor)
    }
}

@Composable
fun FatigueBar(condition: Int) {
    val barColor = when {
        condition >= 90 -> FameColors.GrowthGreen
        condition >= 75 -> Color.Yellow
        condition >= 60 -> FameColors.AfroSunOrange
        else -> FameColors.AlertRed
    }
    
    Box(
        modifier = Modifier
            .width(60.dp)
            .height(4.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.1f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(condition / 100f)
                .fillMaxHeight()
                .background(barColor)
        )
    }
}

@Composable
fun FormArrowIndicator(form: Int) {
    val (icon, color) = when {
        form >= 8 -> Icons.Default.ArrowUpward to FameColors.GrowthGreen
        form >= 6 -> Icons.Default.NorthEast to FameColors.GrowthGreen.copy(alpha = 0.7f)
        form >= 4 -> Icons.AutoMirrored.Filled.ArrowForward to Color.Gray
        form >= 2 -> Icons.Default.SouthEast to FameColors.AlertRed.copy(alpha = 0.7f)
        else -> Icons.Default.ArrowDownward to FameColors.AlertRed
    }
    Icon(icon, null, modifier = Modifier.size(14.dp), tint = color)
}

private fun getSquadRatingColor(rating: Int): Color = when {
    rating >= 80 -> FameColors.ChampionsGold
    rating >= 65 -> FameColors.NationalSilver
    rating >= 55 -> FameColors.LocalBronze
    rating >= 45 -> FameColors.KenteRed
    else -> Color.Gray
}

private fun getPositionColor(category: String): Color = when (category.uppercase()) {
    "GK", "GOALKEEPER" -> Color(0xFF00A86B)
    "DEF", "DEFENDER" -> Color(0xFF1B5E20)
    "MID", "MIDFIELDER" -> Color(0xFFFF7A00)
    "FWD", "FORWARD" -> Color(0xFF9E1B1B)
    else -> Color.Gray
}

@Preview(name = "Normal - Dark", showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun PreviewDynamicPlayerCard_Normal() {
    val samplePlayer = PlayerUiModel(
        id = 1,
        name = "Victor Osimhen",
        age = 25,
        height = 185,
        position = "ST",
        secondaryPosition = "CF",
        positionCategory = "FORWARD",
        rating = 88,
        potential = 92,
        form = 9,
        morale = 95,
        condition = 98,
        fatigue = 2,
        nationality = "Nigeria",
        nationalityFlag = "NG",
        shirtNumber = 9,
        marketValue = 120000000,
        wage = 250000,
        contractExpiry = "2027-06-30",
        isInjured = false,
        injuryStatus = null,
        isSuspended = false,
        isCaptain = true,
        isViceCaptain = false,
        goals = 15,
        assists = 4,
        appearances = 20,
        cleanSheets = 0,
        yellowCards = 2,
        redCards = 0,
        manOfMatch = 5,
        leadership = 85,
        loyalty = 90,
        reactionEmoji = "🔥"
    )

    AFM2026Theme {
        Box(modifier = Modifier.padding(16.dp)) {
            DynamicPlayerCard(
                player = samplePlayer,
                clubName = "Super Eagles",
                onClick = {}
            )
        }
    }
}

@Preview(name = "Injured", showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun PreviewDynamicPlayerCard_Injured() {
    val samplePlayer = PlayerUiModel(
        id = 2,
        name = "Mohamed Salah",
        age = 31,
        height = 175,
        position = "RW",
        secondaryPosition = null,
        positionCategory = "FORWARD",
        rating = 89,
        potential = 89,
        form = 5,
        morale = 80,
        condition = 45,
        fatigue = 55,
        nationality = "Egypt",
        nationalityFlag = "EG",
        shirtNumber = 11,
        marketValue = 80000000,
        wage = 350000,
        contractExpiry = "2025-06-30",
        isInjured = true,
        injuryStatus = "Hamstring Strain",
        isSuspended = false,
        isCaptain = false,
        isViceCaptain = true,
        goals = 12,
        assists = 8,
        appearances = 18,
        cleanSheets = 0,
        yellowCards = 1,
        redCards = 0,
        manOfMatch = 3,
        leadership = 80,
        loyalty = 85,
        reactionEmoji = "🤕"
    )

    AFM2026Theme {
        Box(modifier = Modifier.padding(16.dp)) {
            DynamicPlayerCard(
                player = samplePlayer,
                clubName = "Pharaohs",
                onClick = {}
            )
        }
    }
}
