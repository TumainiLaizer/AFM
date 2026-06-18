package com.fameafrica.afm.ui.screen.transfers

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fameafrica.afm.R
import com.fameafrica.afm.ui.components.common.SidebarCard
import com.fameafrica.afm.ui.components.common.TeamLogo
import com.fameafrica.afm.ui.theme.AFMTextStyles
import com.fameafrica.afm.ui.theme.FameColors
import com.fameafrica.afm.utils.NationalityUtils
import com.fameafrica.afm.utils.PlayerAssetUtils
import com.fameafrica.afm.utils.formatters.CurrencyFormatter
import java.util.Locale

@Composable
fun PlayerTransferCard(player: TransferPlayerUiModel, context: CurrencyFormatter.CurrencyContext?, onClick: (Int) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick(player.id) },
        color = Color.Transparent,
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Rating Badge
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(getTransferRatingColor(player.rating)),
                contentAlignment = Alignment.Center
            ) {
                Text(player.rating.toString(), fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.Black)
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 2. Name & Club
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        player.name.uppercase(), 
                        style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Black), 
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (player.isWonderkid) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.Verified, null, tint = FameColors.ChampionsGold, modifier = Modifier.size(10.dp))
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = NationalityUtils.getWavingFlagUrl(player.nationality),
                        contentDescription = null,
                        modifier = Modifier.size(10.dp).clip(RoundedCornerShape(1.dp)),
                        error = painterResource(R.drawable.default_flag)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "${player.position} • ${player.club.uppercase()}", 
                        style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold), 
                        color = FameColors.MutedParchment
                    )
                }
            }

            // 3. Interest & CAF Status
            Column(modifier = Modifier.width(70.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                InterestBadge(player.interestLevel)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    CAFEligibilityHelper.getEligibilityStatus(player),
                    style = AFMTextStyles.textXXS,
                    color = if (player.cafStatus == CAFStatus.ITC_PENDING) FameColors.AlertRed else FameColors.GrowthGreen.copy(0.7f),
                    fontWeight = FontWeight.Bold
                )
            }

            // 4. Value
            Column(horizontalAlignment = Alignment.End, modifier = Modifier.width(80.dp)) {
                Text(
                    formatCurrency(player.value, context), 
                    style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Black), 
                    color = FameColors.ChampionsGold
                )
                Text(
                    "VALUE", 
                    style = TextStyle(fontSize = 8.sp, fontWeight = FontWeight.Black), 
                    color = Color.White.copy(alpha = 0.3f)
                )
            }
        }
    }
}

@Composable
fun InterestBadge(level: InterestLevel) {
    val (color, text) = when (level) {
        InterestLevel.VERY_INTERESTED -> FameColors.GrowthGreen to "HIGH"
        InterestLevel.INTERESTED -> FameColors.GrowthGreen.copy(0.7f) to "MED"
        InterestLevel.CAUTIOUS -> FameColors.AfroSunOrange to "LOW"
        InterestLevel.NOT_INTERESTED -> FameColors.AlertRed to "NONE"
        InterestLevel.UNSURE -> Color.Gray to "???"
    }
    
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(1.dp),
        border = BorderStroke(0.5.dp, color)
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
            fontSize = 8.sp,
            fontWeight = FontWeight.Black,
            color = color
        )
    }
}

@Composable
fun RecruitmentSuggestionCard(suggestion: RecruitmentSuggestion, onClick: (Int) -> Unit) {
    SidebarCard(
        modifier = Modifier.fillMaxWidth().clickable { onClick(suggestion.player.id) },
        borderColor = if (suggestion.player.isWonderkid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
        borderWidth = 0.5.dp
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { suggestion.scoutScore / 100f },
                    modifier = Modifier.size(44.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(0.1f),
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
                Text("${suggestion.scoutScore}%", style = AFMTextStyles.textXS, fontWeight = FontWeight.Black)
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(suggestion.player.name.uppercase(), style = AFMTextStyles.textSM, fontWeight = FontWeight.Black)
                Text(suggestion.recommendation.uppercase(), style = AFMTextStyles.textXS, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
            
            IconButton(onClick = { onClick(suggestion.player.id) }) {
                Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun TransferEmptyState(icon: ImageVector, title: String, subtitle: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
        Spacer(modifier = Modifier.height(16.dp))
        Text(title.uppercase(), color = MaterialTheme.colorScheme.onSurface, style = AFMTextStyles.textSM, fontWeight = FontWeight.Black, textAlign = TextAlign.Center, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(subtitle.uppercase(), color = MaterialTheme.colorScheme.onSurfaceVariant, style = AFMTextStyles.textXS, textAlign = TextAlign.Center, lineHeight = 16.sp)
    }
}

@Composable
fun OfferStatusCard(offer: TransferOfferUiModel, context: CurrencyFormatter.CurrencyContext?, onClick: (Int) -> Unit) {
    SidebarCard(
        modifier = Modifier.clickable { onClick(offer.id) },
        borderWidth = 0.5.dp
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp)) {
                TeamLogo(offer.fromTeam, modifier = Modifier.size(24.dp).align(Alignment.TopStart))
                TeamLogo(offer.toTeam, modifier = Modifier.size(24.dp).align(Alignment.BottomEnd))
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(offer.playerName.uppercase(), style = AFMTextStyles.textSM, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Black)
                Text("${offer.fromTeam.uppercase()} \u2192 ${offer.toTeam.uppercase()}", style = AFMTextStyles.textXS, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(formatCurrency(offer.fee, context), style = AFMTextStyles.textSM, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
                Surface(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), shape = RoundedCornerShape(1.dp)) {
                    Text(offer.status.uppercase(), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = AFMTextStyles.textXS, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
fun DealSummaryCard(deal: TransferOfferUiModel, context: CurrencyFormatter.CurrencyContext?) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TeamLogo(deal.toTeam, modifier = Modifier.size(32.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(deal.playerName.uppercase(), color = MaterialTheme.colorScheme.onSurface, style = AFMTextStyles.textSM, fontWeight = FontWeight.Black)
            Text("${deal.fromTeam.uppercase()} \u2192 ${deal.toTeam.uppercase()}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = AFMTextStyles.textXS)
        }
        Text(formatCurrency(deal.fee, context), color = MaterialTheme.colorScheme.primary, style = AFMTextStyles.textMD, fontWeight = FontWeight.Black)
    }
}

// Renamed this one to avoid conflict
@Composable
fun TransferPlayerTileCompact(
    player: TransferPlayerUiModel,
    currencyContext: CurrencyFormatter.CurrencyContext?,
    onScout: () -> Unit,
    onBid: () -> Unit,
    onShortlist: () -> Unit,
    onClick: () -> Unit
) {
    val clubColors = remember(player.club) { com.fameafrica.afm.ui.theme.ClubThemeManager.getColorsForTeam(player.club) }
    val faceUrl = PlayerAssetUtils.getPlayerFace(player.id, player.nationality, player.club, clubColors)

    Surface(
        onClick = onClick,
        color = Color.White.copy(alpha = 0.04f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Player Face with attached Flag
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                ) {
                    AsyncImage(
                        model = faceUrl,
                        contentDescription = player.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                        placeholder = painterResource(R.drawable.default_player)
                    )
                    
                    // Small attached flag
                    AsyncImage(
                        model = com.fameafrica.afm.utils.NationalityUtils.getFlagUrl(player.nationality),
                        contentDescription = null,
                        modifier = Modifier
                            .size(14.dp)
                            .align(Alignment.TopEnd)
                            .padding(2.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .border(0.5.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(1.dp))
                    )
                }
                
                Spacer(modifier = Modifier.width(10.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            player.name.uppercase(),
                            style = AFMTextStyles.textXXS.copy(fontWeight = FontWeight.Black, fontSize = 10.sp),
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (player.isWonderkid) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.Verified, null, tint = FameColors.TrophyGold, modifier = Modifier.size(10.dp))
                        }
                    }
                    
                    Text(
                        "${player.age} • ${player.position}",
                        style = AFMTextStyles.textXXS.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
                        color = FameColors.MutedParchment
                    )
                }

                IconButton(onClick = onShortlist, modifier = Modifier.size(24.dp)) {
                    Icon(
                        if (player.interestLevel == InterestLevel.INTERESTED) Icons.Default.Star else Icons.Default.StarOutline,
                        null,
                        modifier = Modifier.size(16.dp),
                        tint = if (player.interestLevel == InterestLevel.INTERESTED) FameColors.TrophyGold else FameColors.MutedParchment
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Value / Fee with Arrow
            Row(verticalAlignment = Alignment.CenterVertically) {
                TeamLogo(player.club, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "\u2192 ${formatCurrency(player.value, currencyContext)}",
                    style = AFMTextStyles.textXS.copy(fontWeight = FontWeight.Black),
                    color = FameColors.TrophyGold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "${formatCurrency(player.wage, currencyContext)}/wk",
                    style = AFMTextStyles.textXXS.copy(fontSize = 7.sp),
                    color = FameColors.MutedParchment
                )
            }

            // Rating & Potential Stars
            Row(
                modifier = Modifier.padding(top = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, tint = FameColors.TrophyGold, modifier = Modifier.size(10.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(player.rating.toString(), style = AFMTextStyles.textXXS.copy(fontWeight = FontWeight.Black), color = Color.White)
                }
                
                Text(
                    buildAnnotatedString {
                        append("POT ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Black, color = FameColors.TrophyGold)) {
                            append(player.potentialRange)
                        }
                    },
                    style = AFMTextStyles.textXXS.copy(fontSize = 7.sp),
                    color = FameColors.MutedParchment
                )
            }

            // Interest Indicator & CAF Status
            Row(
                modifier = Modifier.padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InterestIndicator(player.interestLevel)
                CAFEligibilityBadge(player.cafStatus)
            }

            // Quick Actions
            Row(
                modifier = Modifier.padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                SmallActionButton("SCOUT", onClick = onScout, modifier = Modifier.weight(1f))
                SmallActionButton("BID", onClick = onBid, modifier = Modifier.weight(1f), containerColor = FameColors.GrowthGreen.copy(alpha = 0.2f))
            }
        }
    }
}

@Composable
fun CAFEligibilityBadge(status: CAFStatus) {
    val color = if (status == CAFStatus.ITC_PENDING) FameColors.AlertRed else FameColors.GrowthGreen.copy(0.7f)
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(2.dp),
        border = BorderStroke(0.5.dp, color.copy(alpha = 0.3f))
    ) {
        Text(
            if (status == CAFStatus.ELIGIBLE) "CAF OK" else "ITC REQ",
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
            style = AFMTextStyles.textXXS.copy(fontSize = 7.sp, fontWeight = FontWeight.Black),
            color = color
        )
    }
}

@Composable
fun InterestIndicator(level: InterestLevel) {
    val (color, text) = when (level) {
        InterestLevel.VERY_INTERESTED -> FameColors.GrowthGreen to "VERY INTERESTED"
        InterestLevel.INTERESTED -> FameColors.GrowthGreen to "INTERESTED"
        InterestLevel.CAUTIOUS -> FameColors.AfroSunOrange to "CAUTIOUS"
        InterestLevel.NOT_INTERESTED -> FameColors.AlertRed to "NOT INTERESTED"
        InterestLevel.UNSURE -> Color.Gray to "UNSURE"
    }

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 6.dp)) {
        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text, style = AFMTextStyles.textXXS.copy(fontSize = 8.sp), color = color, fontWeight = FontWeight.Black)
    }
}

@Composable
fun SmallActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = Color.White.copy(alpha = 0.05f)
) {
    Surface(
        onClick = onClick,
        color = containerColor,
        shape = RoundedCornerShape(2.dp),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f)),
        modifier = modifier.height(24.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text, style = AFMTextStyles.textXXS.copy(fontSize = 8.sp), color = Color.White, fontWeight = FontWeight.Black)
        }
    }
}


fun formatCurrency(amount: Long, context: CurrencyFormatter.CurrencyContext?): String {
    if (context == null) return amount.toString()
    val absAmount = Math.abs(amount)
    val converted = absAmount * context.rate
    val sign = if (amount < 0) "-" else ""
    
    val formatted = when {
        converted >= 1_000_000_000 -> String.format(Locale.US, "%.2fB", converted / 1_000_000_000.0)
        converted >= 1_000_000 -> String.format(Locale.US, "%.1fM", converted / 1_000_000.0)
        converted >= 1_000 -> String.format(Locale.US, "%.0fK", converted / 1_000.0)
        else -> String.format(Locale.US, "%.0f", converted)
    }
    return "$sign${context.symbol}$formatted"
}

fun getTransferRatingColor(rating: Int): Color {
    return when {
        rating >= 80 -> FameColors.ChampionsGold
        rating >= 70 -> FameColors.NationalSilver
        rating >= 60 -> FameColors.AfroSunOrange
        else -> FameColors.AlertRed
    }
}
