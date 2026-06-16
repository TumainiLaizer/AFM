package com.fameafrica.afm.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fameafrica.afm.ui.shared.FameCard
import com.fameafrica.afm.ui.theme.AFMTextStyles
import com.fameafrica.afm.ui.theme.Dimensions
import com.fameafrica.afm.ui.theme.FameColors

import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import com.fameafrica.afm.ui.shared.FameProgressIndicator
import com.fameafrica.afm.domain.model.SimulationEvent
import com.fameafrica.afm.utils.NationalityUtils
import com.fameafrica.afm.utils.PlayerAssetUtils
import com.fameafrica.afm.utils.extensions.formatCurrency

/**
 * A mobile-first feed card for the management timeline.
 * Highlights immediate actionable information or emotional beats.
 */
@Composable
fun FameFeedCard(
    title: String,
    content: String,
    category: String,
    icon: ImageVector,
    iconColor: Color = FameColors.TrophyGold,
    timestamp: String? = null,
    onClick: (() -> Unit)? = null,
    actions: @Composable (RowScope.() -> Unit)? = null
) {
    FameCard(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(Dimensions.sm)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dimensions.sm)) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(iconColor.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(14.dp))
                    }
                    Text(
                        text = category.uppercase(),
                        style = AFMTextStyles.statLabel,
                        color = iconColor,
                        letterSpacing = 1.sp
                    )
                }
                if (timestamp != null) {
                    Text(text = timestamp, style = AFMTextStyles.textXS, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Text(text = title, style = AFMTextStyles.textMD, color = MaterialTheme.colorScheme.onSurface)
            
            Text(
                text = content,
                style = AFMTextStyles.textSM,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )

            if (actions != null) {
                Spacer(modifier = Modifier.height(Dimensions.xs))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    content = actions
                )
            }
        }
    }
}

/**
 * High-density dynamic player card with region-based face and form indicators.
 */
@Composable
fun DynamicPlayerCard(
    name: String,
    rating: Int,
    position: String,
    nationality: String,
    form: Int,
    morale: Int,
    isInjured: Boolean = false,
    onClick: () -> Unit
) {
    FameCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(Dimensions.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Player Face with attached Flag
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)), RoundedCornerShape(4.dp))
            ) {
                AsyncImage(
                    model = PlayerAssetUtils.getPlayerFace(name.hashCode(), nationality),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Small attached flag
                AsyncImage(
                    model = NationalityUtils.getFlagUrl(nationality),
                    contentDescription = null,
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.TopEnd)
                        .padding(2.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .border(androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.3f)), RoundedCornerShape(1.dp))
                )
                
                // Rating Overlay
                RatingBadge(
                    rating = rating,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(2.dp)
                )
            }

            Spacer(modifier = Modifier.width(Dimensions.md))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name.uppercase(),
                    style = AFMTextStyles.textMD.copy(fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = position,
                        style = AFMTextStyles.textXS,
                        color = FameColors.TrophyGold,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = " • ${nationality.uppercase()}",
                        style = AFMTextStyles.textXS,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Morale/Sharpness Progress Mini-Bars
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MiniProgress(label = "MOR", value = morale / 100f, color = FameColors.GrowthGreen)
                    MiniProgress(label = "FRM", value = form / 10f, color = FameColors.TrophyGold)
                }
            }

            // Trend Icon
            FormIndicator(form)
            
            if (isInjured) {
                Icon(
                    Icons.Default.MedicalServices,
                    null,
                    tint = FameColors.AlertRed,
                    modifier = Modifier.size(16.dp).padding(start = 4.dp)
                )
            }
        }
    }
}

@Composable
fun MiniProgress(label: String, value: Float, color: Color) {
    Column(modifier = Modifier.width(40.dp)) {
        Text(label, fontSize = 6.sp, color = FameColors.MutedParchment)
        FameProgressIndicator(
            progress = value,
            modifier = Modifier.height(2.dp),
            color = color
        )
    }
}

@Composable
fun FormIndicator(form: Int) {
    val icon = when {
        form >= 8 -> Icons.AutoMirrored.Filled.TrendingUp
        form >= 4 -> Icons.AutoMirrored.Filled.TrendingFlat
        else -> Icons.AutoMirrored.Filled.TrendingDown
    }
    val color = when {
        form >= 8 -> FameColors.GrowthGreen
        form >= 4 -> FameColors.TrophyGold
        else -> FameColors.AlertRed
    }
    Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
}

private fun getRatingColor(rating: Int): Color = when {
    rating >= 80 -> FameColors.ContinentalGold
    rating >= 60 -> FameColors.NationalSilver
    else -> FameColors.LocalBronze
}

/**
 * Consistently styled square rating badge with rounded edges.
 * Color scales based on levels: Local (Bronze), National (Silver), Continental (Gold).
 */
@Composable
fun RatingBadge(
    rating: Int,
    modifier: Modifier = Modifier,
    textStyle: androidx.compose.ui.text.TextStyle = AFMTextStyles.textXS.copy(fontWeight = FontWeight.Black)
) {
    val backgroundColor = getRatingColor(rating)
    val contentColor = if (backgroundColor == FameColors.NationalSilver) Color.Black else Color.White

    Surface(
        modifier = modifier,
        color = backgroundColor,
        shape = RoundedCornerShape(Dimensions.micro)
    ) {
        Text(
            text = rating.toString(),
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
            style = textStyle,
            color = contentColor
        )
    }
}

/**
 * Overloaded RatingBadge for text-based ratings (e.g. potential ranges).
 */
@Composable
fun RatingBadge(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = FameColors.ContinentalGold,
    textStyle: androidx.compose.ui.text.TextStyle = AFMTextStyles.textXS.copy(fontWeight = FontWeight.Bold)
) {
    val contentColor = if (backgroundColor == FameColors.NationalSilver) Color.Black else Color.White

    Surface(
        modifier = modifier,
        color = backgroundColor,
        shape = RoundedCornerShape(Dimensions.micro)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
            style = textStyle,
            color = contentColor
        )
    }
}

/**
 * Consistently styled reputation badge for managers and clubs.
 */
@Composable
fun ReputationBadge(
    level: String,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (level.lowercase()) {
        "continental", "world", "legendary", "elite", "pro" -> FameColors.ContinentalGold
        "national", "regional" -> FameColors.NationalSilver
        else -> FameColors.LocalBronze
    }
    val contentColor = if (backgroundColor == FameColors.NationalSilver) Color.Black else Color.White

    Surface(
        modifier = modifier,
        color = backgroundColor,
        shape = RoundedCornerShape(Dimensions.micro)
    ) {
        Text(
            text = level.uppercase(),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = AFMTextStyles.textXS.copy(fontWeight = FontWeight.Bold, fontSize = 8.sp),
            color = contentColor
        )
    }
}

/**
 * Consistently styled reputation badge for managers and clubs using numeric reputation.
 */
@Composable
fun ReputationBadge(
    reputation: Int,
    modifier: Modifier = Modifier
) {
    val level = when {
        reputation >= 80 -> "Continental"
        reputation >= 60 -> "National"
        else -> "Local"
    }
    ReputationBadge(level = level, modifier = modifier)
}

/**
 * A horizontal carousel for browsing high-density items like staff or formations.
 */
@Composable
fun <T> FameCarousel(
    items: List<T>,
    itemContent: @Composable (T) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = Dimensions.md)
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(Dimensions.md)
    ) {
        items(items) { item ->
            itemContent(item)
        }
    }
}

/**
 * A high-density metric widget for top-level dashboards.
 */
@Composable
fun FameMetricWidget(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    trend: String? = null,
    trendColor: Color = FameColors.GrowthGreen
) {
    Surface(
        modifier = modifier.height(52.dp),
        color = Color.Black.copy(alpha = 0.3f),
        shape = RoundedCornerShape(Dimensions.micro)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = Dimensions.md, vertical = Dimensions.sm),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label.uppercase(),
                style = AFMTextStyles.statLabel,
                color = FameColors.MutedParchment,
                fontSize = 8.sp
            )
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(Dimensions.xs)) {
                Text(
                    text = value,
                    style = AFMTextStyles.statValue,
                    color = FameColors.WarmIvory,
                    fontSize = 14.sp
                )
                if (trend != null) {
                    Text(
                        text = trend,
                        style = AFMTextStyles.tickerText,
                        color = trendColor,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

/**
 * A standard section card for dashboards.
 */
@Composable
fun DashboardSectionCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(12.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = FameColors.HeaderDark.copy(alpha = 0.6f),
        shape = RoundedCornerShape(2.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(contentPadding)) {
            content()
        }
    }
}

/**
 * Shared Simulation Event Card for the management timeline and world feed.
 */
@Composable
fun SimulationEventCard(event: SimulationEvent, modifier: Modifier = Modifier) {
    val categoryColor = when (event) {
        is SimulationEvent.UserMatchPlayed -> FameColors.GrowthGreen
        is SimulationEvent.MatchPlayed -> FameColors.WarmIvory.copy(alpha = 0.6f)
        is SimulationEvent.TransferOffer -> FameColors.TransferBlue
        is SimulationEvent.Injury -> FameColors.AlertRed
        is SimulationEvent.BoardMeeting -> FameColors.BaobabBrown
        is SimulationEvent.FinancialAlert -> FameColors.TrophyGold
        is SimulationEvent.ContractOffer -> FameColors.PitchGreen
        is SimulationEvent.JobOffer -> FameColors.ChampionsGold
        is SimulationEvent.SponsorshipOffer -> FameColors.GrowthGreen
        is SimulationEvent.FacilityCompletion -> FameColors.PitchGreen
        is SimulationEvent.AwardCeremony -> FameColors.TrophyGold
        is SimulationEvent.YouthIntake -> FameColors.AfroSunOrange
        is SimulationEvent.SeasonEnd -> FameColors.BaobabBrown
        else -> FameColors.MutedParchment
    }

    val icon = when (event) {
        is SimulationEvent.UserMatchPlayed, is SimulationEvent.MatchPlayed -> Icons.Default.SportsScore
        is SimulationEvent.TransferOffer -> Icons.Default.SwapHoriz
        is SimulationEvent.Injury -> Icons.Default.HealthAndSafety
        is SimulationEvent.BoardMeeting -> Icons.Default.Gavel
        is SimulationEvent.FinancialAlert -> Icons.Default.AccountBalanceWallet
        is SimulationEvent.ContractOffer -> Icons.Default.Description
        is SimulationEvent.JobOffer -> Icons.Default.Work
        is SimulationEvent.SponsorshipOffer -> Icons.Default.Handshake
        is SimulationEvent.FacilityCompletion -> Icons.Default.Business
        is SimulationEvent.AwardCeremony -> Icons.Default.EmojiEvents
        is SimulationEvent.YouthIntake -> Icons.Default.Groups
        is SimulationEvent.SeasonEnd -> Icons.Default.CalendarMonth
        else -> Icons.AutoMirrored.Filled.Feed
    }

    val title = when (event) {
        is SimulationEvent.MatchPlayed -> "${event.result.homeTeam} ${event.result.homeScore}-${event.result.awayScore} ${event.result.awayTeam}"
        is SimulationEvent.UserMatchPlayed -> "MATCHDAY: ${event.result.homeTeam} ${event.result.homeScore}-${event.result.awayScore} ${event.result.awayTeam}"
        is SimulationEvent.TransferOffer -> "OFFER: ${event.offeringTeam} for ${event.playerName}"
        is SimulationEvent.Injury -> "INJURY: ${event.playerName} (${event.duration}d)"
        is SimulationEvent.BoardMeeting -> "BOARD: ${event.title}"
        is SimulationEvent.NewsHeadline -> event.news.headline.uppercase()
        is SimulationEvent.ContractOffer -> "CONTRACT: ${event.playerName}"
        is SimulationEvent.JobOffer -> "JOB: Vacancy at ${event.teamName}"
        is SimulationEvent.SponsorshipOffer -> "SPONSOR: ${event.sponsorName}"
        is SimulationEvent.FacilityCompletion -> "COMPLETE: ${event.facilityName} (Lvl ${event.level})"
        is SimulationEvent.AwardCeremony -> "AWARD: ${event.ceremonyName}"
        is SimulationEvent.YouthIntake -> "YOUTH: Intake at ${event.teamName}"
        is SimulationEvent.SeasonEnd -> "SEASON END: ${event.season}"
        else -> "WORLD UPDATE"
    }

    val snippet = when (event) {
        is SimulationEvent.NewsHeadline -> event.news.content
        is SimulationEvent.TransferOffer -> "Fee: ${event.fee.formatCurrency()}"
        is SimulationEvent.SponsorshipOffer -> "Value: ${event.value.formatCurrency()}"
        else -> ""
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        color = Color.Black.copy(alpha = 0.4f),
        shape = RoundedCornerShape(2.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(32.dp),
                shape = RoundedCornerShape(2.dp),
                color = categoryColor.copy(alpha = 0.1f),
                border = androidx.compose.foundation.BorderStroke(1.dp, categoryColor.copy(alpha = 0.3f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = categoryColor, modifier = Modifier.size(18.dp))
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = AFMTextStyles.textXS.copy(fontWeight = FontWeight.Black),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (snippet.isNotEmpty()) {
                    Text(
                        text = snippet,
                        fontSize = 8.sp,
                        color = FameColors.MutedParchment,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// ============ PREVIEWS ============

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "Fame Feed Card")
@Composable
fun FameFeedCardPreview() {
    Column(modifier = Modifier.padding(16.dp).background(FameColors.DeepNavyBlack)) {
        FameFeedCard(
            title = "Tactical Readiness",
            content = "Squad familiarity with 4-3-3 is building. Training intensity is currently high.",
            category = "TACTICS",
            icon = Icons.Default.SportsSoccer,
            iconColor = FameColors.GrowthGreen,
            timestamp = "2h ago",
            actions = {
                TextButton(onClick = {}) {
                    Text("VIEW TACTICS", color = FameColors.PitchGreen)
                }
            }
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "Fame Metric Widget")
@Composable
fun FameMetricWidgetPreview() {
    Row(modifier = Modifier.padding(16.dp).background(FameColors.DeepNavyBlack), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        FameMetricWidget(label = "Morale", value = "85%", modifier = Modifier.weight(1f))
        FameMetricWidget(label = "Balance", value = "€12.4M", trend = "+2.4M", modifier = Modifier.weight(1f))
        FameMetricWidget(label = "Injuries", value = "2", modifier = Modifier.weight(1f), trendColor = FameColors.AlertRed)
    }
}
