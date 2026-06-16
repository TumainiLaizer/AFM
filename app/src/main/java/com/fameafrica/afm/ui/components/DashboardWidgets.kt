package com.fameafrica.afm.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Feed
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.fameafrica.afm.R
import com.fameafrica.afm.ui.components.common.TeamLogo
import com.fameafrica.afm.ui.screen.dashboard.*
import com.fameafrica.afm.ui.shared.FameCard
import com.fameafrica.afm.ui.theme.AFMTextStyles
import com.fameafrica.afm.ui.theme.Dimensions
import com.fameafrica.afm.ui.theme.FameColors
import com.fameafrica.afm.utils.extensions.toTitleCase
import kotlinx.coroutines.delay
import java.util.Locale

/**
 * Universal Broadcast Identity Banner.
 * Designed for both Manager and Chairman modes.
 */
@Composable
fun IdentityBanner(
    managerName: String,
    role: String,
    clubName: String,
    reputationValue: Int,
    level: Int,
    coins: String,
    premiumCurrency: String,
    currentSeason: String = "2025/26",
    boardConfidence: Int = 85,
    fanConfidence: Int = 78,
    clubForm: List<String> = listOf("W", "W", "D", "W", "L"),
    managerAvatar: String? = null,
    modifier: Modifier = Modifier,
    onProfileClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onAddCurrency: () -> Unit = {}
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        color = FameColors.DeepNavyBlack,
        shape = RoundedCornerShape(2.dp),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            // Top Bar: Resources & Quick Actions
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ResourcePillCompact(amount = coins, icon = Icons.Default.MonetizationOn, color = FameColors.TrophyGold, onAdd = onAddCurrency)
                Spacer(modifier = Modifier.width(4.dp))
                ResourcePillCompact(amount = premiumCurrency, icon = Icons.Default.Token, color = FameColors.GrowthGreen, onAdd = onAddCurrency)
                
                Spacer(modifier = Modifier.weight(1f))
                
                Text(
                    text = currentSeason,
                    style = AFMTextStyles.textXXS.copy(fontWeight = FontWeight.Black),
                    color = FameColors.WarmIvory
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                IconButton(onClick = onNotificationsClick, modifier = Modifier.size(20.dp)) {
                    Icon(Icons.Default.Notifications, null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onSettingsClick, modifier = Modifier.size(20.dp)) {
                    Icon(Icons.Default.Settings, null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }

            // Main Content Area
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Portrait & Basic Info
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .clickable { onProfileClick() }
                ) {
                    AsyncImage(
                        model = managerAvatar ?: "file:///android_asset/manager_faces/east_africa_1_1.jpg",
                        contentDescription = "Manager",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // Level Badge
                    Surface(
                        color = FameColors.TrophyGold,
                        modifier = Modifier.align(Alignment.BottomEnd).size(18.dp),
                        shape = RoundedCornerShape(topStart = 4.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(level.toString(), fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.Black)
                        }
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = managerName.toTitleCase(),
                        style = AFMTextStyles.textMD.copy(fontWeight = FontWeight.Black),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = role.toTitleCase(),
                        fontSize = 9.sp,
                        color = FameColors.MutedParchment,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TeamLogo(clubName, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = clubName.toTitleCase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }

                // 2. Performance & Confidence Widgets
                Column(
                    modifier = Modifier.width(120.dp),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        BoardConfidenceWidget(confidence = boardConfidence)
                        FanSentimentWidget(sentiment = fanConfidence)
                    }
                    
                    // Club Form
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        clubForm.takeLast(5).forEach { res -> FormIndicatorMicro(res) }
                    }
                }
            }
        }
    }
}

@Composable
fun BoardConfidenceWidget(confidence: Int) {
    Surface(
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(2.dp),
        modifier = Modifier.width(58.dp).height(24.dp),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(Icons.Default.BusinessCenter, null, tint = FameColors.TrophyGold, modifier = Modifier.size(10.dp))
            Text("${confidence}%", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.White)
        }
    }
}

@Composable
fun FanSentimentWidget(sentiment: Int) {
    Surface(
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(2.dp),
        modifier = Modifier.width(58.dp).height(24.dp),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(Icons.Default.Groups, null, tint = FameColors.GrowthGreen, modifier = Modifier.size(10.dp))
            Text("${sentiment}%", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.White)
        }
    }
}

@Composable
fun DashboardStatItem(
    label: String,
    value: String,
    icon: Int? = null,
    color: Color = Color.White,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(40.dp),
        color = Color.Black.copy(alpha = 0.4f),
        shape = RoundedCornerShape(2.dp),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Image(painter = painterResource(id = icon), contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
            }
            Column {
                Text(value, fontSize = 12.sp, fontWeight = FontWeight.Black, color = color)
                Text(label.toTitleCase(), fontSize = 6.sp, color = FameColors.MutedParchment, fontWeight = FontWeight.Bold)
            }
        }
    }
}
@Composable
fun CalendarDayItem(label: String, isActive: Boolean, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(color = Color.LightGray.copy(alpha = 0.3f), modifier = Modifier.fillMaxWidth().height(20.dp)) {
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
        }
        Surface(
            color = Color.White,
            modifier = Modifier.fillMaxWidth().height(if (isActive) 120.dp else 100.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize().padding(2.dp)) {
                Box(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(4.dp).background(if(isActive) Color.Red else Color.Green))
            }
        }
    }
}

@Composable
fun MiniDashboardWidget(
    label: String,
    value: String,
    icon: Int,
    color: Color,
    modifier: Modifier = Modifier,
    isHorizontal: Boolean = false
) {
    Surface(
        modifier = modifier,
        color = Color.Black.copy(alpha = 0.4f),
        shape = RoundedCornerShape(2.dp),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.05f))
    ) {
        if (isHorizontal) {
            Row(modifier = Modifier.padding(4.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Image(painter = painterResource(id = icon), contentDescription = null, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(value, fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.White, modifier = Modifier.weight(1f))
                Text(label, fontSize = 7.sp, color = FameColors.MutedParchment, fontWeight = FontWeight.Bold)
            }
        } else {
            Column(modifier = Modifier.padding(4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Image(painter = painterResource(id = icon), contentDescription = null, modifier = Modifier.size(16.dp))
                Text(value, fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.White)
                Text(label, fontSize = 6.sp, color = FameColors.MutedParchment, fontWeight = FontWeight.Bold)
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(color.copy(alpha = 0.5f)))
            }
        }
    }
}

@Composable
fun FinanceHealthRow(
    health: String,
    availableFunds: String,
    profit: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth().padding(horizontal = Dimensions.panelPadding),
        color = Color.Transparent
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            FinanceStatItem("FINANCIAL HEALTH", health, FameColors.GrowthGreen)
            FinanceStatItem("AVAILABLE FUNDS", availableFunds, Color.White)
            FinanceStatItem("PROFIT/LOSS", profit, Color.White)
        }
    }
}

@Composable
fun FinanceStatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Black, color = color)
        Text(label, fontSize = 7.sp, color = FameColors.MutedParchment, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ResourcePillCompact(amount: String, icon: ImageVector, color: Color, onAdd: () -> Unit) {
    Surface(
        color = Color.Black.copy(alpha = 0.6f),
        shape = RoundedCornerShape(2.dp),
        border = BorderStroke(0.5.dp, color.copy(alpha = 0.4f)),
        modifier = Modifier.height(24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 4.dp)
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(14.dp))
            Text(
                text = amount,
                fontSize = 10.sp,
                color = Color.White,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(18.dp)
                    .background(color.copy(alpha = 0.15f))
                    .clickable { onAdd() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, null, tint = color, modifier = Modifier.size(10.dp))
            }
        }
    }
}

@Composable
fun NextMatchHeroCard(
    opponentName: String,
    competitionName: String,
    matchDate: String,
    matchTime: String,
    stadium: String,
    weather: String,
    rivalryLevel: String?,
    importance: String = "Normal",
    myTeamName: String,
    myTeamForm: List<String>,
    opponentForm: List<String>,
    myTeamLeaguePos: Int,
    opponentLeaguePos: Int,
    tacticalAdvice: String? = null,
    formation: String = "4-2-3-1",
    mentality: String = "ATTACKING",
    isAdvancing: Boolean = false,
    modifier: Modifier = Modifier,
    onAdvanceClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.95f else 1f, label = "ButtonScale")

    FameCard(
        modifier = modifier.fillMaxWidth(),
        containerColor = Color(0xFF011018),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
        contentPadding = 0.dp
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text("Next Match", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.White, letterSpacing = 1.sp)
            Text(matchDate.toTitleCase(), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, fontSize = 9.sp, color = FameColors.MutedParchment)
            Spacer(modifier = Modifier.height(4.dp))
            Text(competitionName.toTitleCase(), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    TeamLogo(myTeamName, modifier = Modifier.size(60.dp))
                    Text("${myTeamLeaguePos}th", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    Text(myTeamName.toTitleCase(), fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Black)
                    Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) { myTeamForm.takeLast(5).forEach { res -> FormIndicatorMicro(res) } }
                }
                Text("vs", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.White.copy(alpha = 0.2f))
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    TeamLogo(opponentName, modifier = Modifier.size(60.dp))
                    Text("${opponentLeaguePos}st", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    Text(opponentName.toTitleCase(), fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Black)
                    Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) { opponentForm.takeLast(5).forEach { res -> FormIndicatorMicro(res) } }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = { if (!isAdvancing) onAdvanceClick() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .graphicsLayer(scaleX = scale, scaleY = scale),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isAdvancing) FameColors.SharedNeutralBase else FameColors.AfroSunOrange,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(4.dp),
                interactionSource = interactionSource,
                enabled = !isAdvancing
            ) {
                if (isAdvancing) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.Black,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Advancing...",
                            style = AFMTextStyles.textLG.copy(fontSize = 20.sp, fontWeight = FontWeight.Black),
                            color = Color.Black
                        )
                    }
                } else {
                    Text(
                        text = "Advance",
                        style = AFMTextStyles.textLG.copy(fontSize = 28.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp),
                        color = Color.Black
                    )
                }
            }

            // Simulate Days Movement (Animated Calendar Ticker when advancing)
            if (isAdvancing) {
                Spacer(modifier = Modifier.height(8.dp))
                CalendarTickerAnimation()
            }
        }
    }
}

@Composable
fun CalendarTickerAnimation() {
    val days = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
    var dayIndex by remember { mutableStateOf(0) }
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(200)
            dayIndex = (dayIndex + 1) % days.size
        }
    }
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Schedule, null, tint = FameColors.TrophyGold, modifier = Modifier.size(12.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "Processing: ${days[dayIndex]} ${10 + dayIndex} OCT",
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = FameColors.MutedParchment
        )
    }
}

@Composable
fun FormIndicatorMicro(result: String) {
    val color = when (result.uppercase()) {
        "W" -> FameColors.GrowthGreen
        "D" -> FameColors.TrophyGold
        "L" -> FameColors.AlertRed
        else -> FameColors.MutedParchment
    }
    Box(modifier = Modifier.size(10.dp).background(color, RoundedCornerShape(1.dp)), contentAlignment = Alignment.Center) {
        Text(result, fontSize = 6.sp, fontWeight = FontWeight.Black, color = Color.Black)
    }
}

@Composable
fun LeagueSnapshotWidget(
    title: String,
    standings: List<StandingUiModel>,
    userTeamName: String,
    onNavigateToLeague: () -> Unit,
    modifier: Modifier = Modifier
) {
    FameCard(modifier = modifier.fillMaxWidth().clickable(onClick = onNavigateToLeague), containerColor = Color.Black.copy(alpha = 0.4f)) {
        Column(modifier = Modifier.padding(Dimensions.xs)) {
            Text(title.uppercase(), style = AFMTextStyles.textXXS.copy(fontWeight = FontWeight.Black), color = FameColors.TrophyGold)
            Spacer(modifier = Modifier.height(4.dp))
            standings.take(5).forEach { standing ->
                val isUserTeam = standing.teamName.uppercase().contains(userTeamName.uppercase())
                Row(modifier = Modifier.fillMaxWidth().height(18.dp).background(if(isUserTeam) Color.White.copy(alpha = 0.1f) else Color.Transparent, RoundedCornerShape(2.dp)), verticalAlignment = Alignment.CenterVertically) {
                    Text(standing.position.toString(), style = AFMTextStyles.textXXS, color = FameColors.MutedParchment, modifier = Modifier.width(16.dp))
                    TeamLogo(standing.teamName, modifier = Modifier.size(10.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(standing.teamName.uppercase(), style = AFMTextStyles.textXXS, color = if(isUserTeam) Color.White else FameColors.WarmIvory, fontWeight = if(isUserTeam) FontWeight.Black else FontWeight.Normal, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("P${standing.played}", style = AFMTextStyles.textXXS, color = FameColors.MutedParchment, modifier = Modifier.width(24.dp))
                    Text(standing.points.toString(), style = AFMTextStyles.statValue.copy(fontSize = 9.sp), color = Color.White, modifier = Modifier.width(20.dp), textAlign = TextAlign.End)
                }
            }
        }
    }
}

@Composable
fun LiveFeedItem(news: NewsUiModel, modifier: Modifier = Modifier) {
    val categoryColor = when (news.category.uppercase()) {
        "INJURY" -> FameColors.AlertRed
        "TRANSFER" -> FameColors.TransferBlue
        "YOUTH" -> FameColors.TrophyGold
        "BOARD" -> FameColors.BaobabBrown
        else -> FameColors.PitchGreen
    }
    FameCard(modifier = modifier.height(64.dp), containerColor = Color.Black.copy(alpha = 0.35f), border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)), contentPadding = 8.dp) {
        Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(40.dp), shape = RoundedCornerShape(4.dp), color = Color.Black.copy(alpha = 0.5f), border = BorderStroke(1.dp, categoryColor.copy(alpha = 0.5f))) {
                Box(contentAlignment = Alignment.Center) { Icon(Icons.AutoMirrored.Filled.Feed, null, tint = categoryColor, modifier = Modifier.size(20.dp)) }
            }
            Spacer(modifier = Modifier.width(Dimensions.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = news.category.uppercase(), fontSize = 8.sp, color = categoryColor, fontWeight = FontWeight.Black)
                Text(text = news.title, style = AFMTextStyles.textXXS.copy(fontWeight = FontWeight.Bold), color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = news.snippet, fontSize = 7.sp, color = FameColors.MutedParchment, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text("2H AGO", fontSize = 8.sp, color = FameColors.MutedParchment, fontWeight = FontWeight.Bold)
        }
    }
}
