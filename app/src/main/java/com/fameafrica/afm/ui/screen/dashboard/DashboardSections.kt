package com.fameafrica.afm.ui.screen.dashboard

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fameafrica.afm.R
import com.fameafrica.afm.domain.model.SimulationEvent
import com.fameafrica.afm.ui.components.common.TeamLogo
import com.fameafrica.afm.ui.theme.*

@Composable
fun DashboardManagerHeader(
    name: String,
    level: Int,
    bankBalance: String,
    coins: String,
    reputation: Int,
    xpCurrent: Int,
    xpMax: Int,
    country: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(Color.Black, Color.Transparent)
                )
            )
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Portrait
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .border(2.dp, FameColors.TrophyGold, CircleShape)
                    .padding(2.dp)
                    .clip(CircleShape)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.default_manager),
                    contentDescription = "Manager",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "MANAGER",
                    style = AFMTextStyles.textXXS,
                    color = FameColors.GrowthGreen,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = name.uppercase(),
                    style = AFMTextStyles.textLG.copy(fontSize = 18.sp),
                    color = Color.White
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.default_flag),
                        contentDescription = country,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = country,
                        style = AFMTextStyles.textXXS,
                        color = FameColors.MutedParchment
                    )
                }
                
                Spacer(modifier = Modifier.height(6.dp))
                
                XpProgressBar(current = xpCurrent, max = xpMax)
            }

            Spacer(modifier = Modifier.width(12.dp))

            LevelBadge(level = level)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Resources Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ResourceWidget(
                label = "BANK BALANCE",
                value = bankBalance,
                icon = { Text("💵", fontSize = 18.sp) },
                modifier = Modifier.weight(1f)
            )
            ResourceWidget(
                label = "COINS",
                value = coins,
                icon = { Text("🪙", fontSize = 18.sp) },
                modifier = Modifier.weight(1f)
            )
            ResourceWidget(
                label = "REPUTATION",
                value = reputation.toString(),
                icon = {
                    Image(
                        painter = painterResource(id = R.drawable.ic_star_gold),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun XpProgressBar(current: Int, max: Int) {
    val progress = remember(current, max) { if (max > 0) current.toFloat() / max else 0f }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(4.dp)
                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .background(FameColors.GrowthGreen, RoundedCornerShape(2.dp))
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$current / $max XP",
            style = AFMTextStyles.textXXS,
            color = FameColors.MutedParchment,
            fontSize = 7.sp
        )
    }
}

@Composable
private fun LevelBadge(level: Int) {
    Surface(
        color = Color.Transparent,
        border = BorderStroke(1.dp, FameColors.TrophyGold),
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.size(40.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("LVL", fontSize = 8.sp, color = FameColors.MutedParchment, fontWeight = FontWeight.Bold)
            Text(level.toString(), fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun ResourceWidget(
    label: String,
    value: String,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color.Black.copy(alpha = 0.5f),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = value,
                    style = AFMTextStyles.textMD.copy(fontSize = 12.sp),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = label,
                    style = AFMTextStyles.textXXS,
                    color = FameColors.MutedParchment,
                    fontSize = 6.sp
                )
            }
        }
    }
}

@Composable
fun ClubIdentitySection(
    clubName: String,
    leagueName: String,
    boardConfidence: Int,
    fanConfidence: Int,
    squadDepth: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color.Black.copy(alpha = 0.3f),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TeamLogo(clubName, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(clubName.uppercase(), style = AFMTextStyles.textMD, color = Color.White, fontWeight = FontWeight.Black)
                Text(leagueName, style = AFMTextStyles.textXXS, color = FameColors.MutedParchment)
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ConfidenceWidget("BOARD", boardConfidence, Icons.Default.Shield, FameColors.TrophyGold)
                ConfidenceWidget("FANS", fanConfidence, Icons.Default.Favorite, FameColors.GrowthGreen)
                SquadDepthWidget(squadDepth)
            }
        }
    }
}

@Composable
private fun ConfidenceWidget(label: String, value: Int, icon: ImageVector, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = color.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
        Text("$value%", style = AFMTextStyles.textXS, color = color, fontWeight = FontWeight.Bold)
        Text(label, style = AFMTextStyles.textXXS, color = FameColors.MutedParchment, fontSize = 6.sp)
    }
}

@Composable
private fun SquadDepthWidget(count: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.Groups, null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
        Text(count.toString(), style = AFMTextStyles.textXS, color = Color.White, fontWeight = FontWeight.Bold)
        Text("SQUAD", style = AFMTextStyles.textXXS, color = FameColors.MutedParchment, fontSize = 6.sp)
    }
}

@Composable
fun DashboardNextMatchCard(
    homeTeam: String,
    awayTeam: String,
    matchDate: String,
    matchTime: String,
    stadium: String,
    leagueMatchday: String,
    isMatchToday: Boolean,
    isAdvancing: Boolean,
    onContinue: () -> Unit,
    onSimulate: () -> Unit,
    onPlayMatch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgImage = remember(leagueMatchday) {
        when {
            leagueMatchday.contains("PRESEASON", ignoreCase = true) || leagueMatchday.contains("FRIENDLY", ignoreCase = true) -> R.drawable.season_preseason_banner
            leagueMatchday.contains("CUP", ignoreCase = true) -> R.drawable.season_cup_round
            leagueMatchday.contains("CAF", ignoreCase = true) -> R.drawable.season_caf_stage
            else -> R.drawable.stadium_bg
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
        color = Color.Black,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, FameColors.TrophyGold.copy(alpha = 0.5f))
    ) {
        Box {
            // Background Image
            Image(
                painter = painterResource(id = bgImage),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(200.dp),
                contentScale = ContentScale.Crop,
                alpha = if (bgImage == R.drawable.stadium_bg) 0.5f else 0.35f
            )

            Column(modifier = Modifier.padding(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        if (isMatchToday) "MATCHDAY" else "NEXT MATCH", 
                        style = AFMTextStyles.textXS, 
                        color = FameColors.TrophyGold, 
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(2.dp)).padding(horizontal = 4.dp)
                    )
                    Text(
                        leagueMatchday, 
                        style = AFMTextStyles.textXXS, 
                        color = Color.White,
                        modifier = Modifier.background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(2.dp)).padding(horizontal = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        TeamLogo(homeTeam, modifier = Modifier.size(60.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(homeTeam.uppercase(), style = AFMTextStyles.textSM, color = Color.White, fontWeight = FontWeight.Black, textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }

                    Text("VS", style = AFMTextStyles.textLG, color = Color.White.copy(alpha = 0.6f))

                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        TeamLogo(awayTeam, modifier = Modifier.size(60.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(awayTeam.uppercase(), style = AFMTextStyles.textSM, color = Color.White, fontWeight = FontWeight.Black, textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarToday, null, tint = FameColors.TrophyGold, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("$matchDate | $matchTime", style = AFMTextStyles.textXXS, color = Color.White)
                    }
                    Text(stadium.uppercase(), style = AFMTextStyles.textXXS, color = FameColors.MutedParchment, fontSize = 7.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val buttonWeight = if (isMatchToday) 1f else 1.2f
                    Button(
                        onClick = if (isMatchToday) onPlayMatch else onContinue,
                        enabled = !isAdvancing,
                        modifier = Modifier.weight(buttonWeight).height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FameColors.TrophyGold),
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        if (isAdvancing && !isMatchToday) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.Black, strokeWidth = 2.dp)
                        } else {
                            Icon(if (isMatchToday) Icons.Default.SportsSoccer else Icons.Default.FastForward, null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isMatchToday) "PLAY MATCH" else "CONTINUE", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1)
                        }
                    }
                    
                    if (!isMatchToday) {
                        Button(
                            onClick = onSimulate,
                            enabled = !isAdvancing,
                            modifier = Modifier.weight(1f).height(44.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B3A26)),
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, FameColors.GrowthGreen.copy(alpha = 0.5f)),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("SIMULATE", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LiveHeadlinesWidget(headlines: List<String>, onViewAll: () -> Unit) {
    WidgetContainer(title = "BREAKING NEWS", onAction = onViewAll) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            headlines.take(3).forEach { headline ->
                HeadlineItem(headline)
            }
        }
    }
}

@Composable
private fun HeadlineItem(headline: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(Icons.Default.Podcasts, null, tint = FameColors.TrophyGold, modifier = Modifier.size(12.dp).padding(top = 2.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(headline, style = AFMTextStyles.textXXS, color = Color.White, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun ClubFeedWidget(feedItems: List<SimulationEvent>, onViewAll: () -> Unit) {
    WidgetContainer(title = "CLUB FEED", onAction = onViewAll) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            feedItems.take(5).forEach { event ->
                FeedItem(event)
            }
        }
    }
}

@Composable
private fun FeedItem(event: SimulationEvent) {
    val (title, icon) = when(event) {
        is SimulationEvent.MatchPlayed -> "Match Result" to Icons.Default.SportsSoccer
        is SimulationEvent.Injury -> "Injury Report" to Icons.Default.MedicalServices
        is SimulationEvent.TransferOffer -> "Transfer Talk" to Icons.Default.SwapHoriz
        else -> "Club Update" to Icons.Default.Info
    }
    
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = FameColors.MutedParchment, modifier = Modifier.size(12.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, style = AFMTextStyles.textXXS, color = FameColors.MutedParchment, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun DashboardLeagueStandings(
    standings: List<StandingUiModel>,
    onViewFull: () -> Unit,
    modifier: Modifier = Modifier
) {
    WidgetContainer(
        title = "LEAGUE TABLE",
        onAction = onViewFull,
        modifier = modifier
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            standings.forEach { standing ->
                StandingRow(standing)
            }
        }
    }
}

@Composable
private fun StandingRow(standing: StandingUiModel) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(standing.position.toString(), style = AFMTextStyles.textXXS, color = FameColors.MutedParchment, modifier = Modifier.width(20.dp))
        Text(standing.teamName, style = AFMTextStyles.textXXS, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(standing.points.toString(), style = AFMTextStyles.textXXS, color = FameColors.TrophyGold, fontWeight = FontWeight.Black, modifier = Modifier.width(25.dp), textAlign = TextAlign.End)
    }
}

@Composable
fun BoardObjectivesWidget(onViewAll: () -> Unit) {
    WidgetContainer(title = "BOARD OBJECTIVES", onAction = onViewAll) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            ObjectiveItem("League Finish", "Top 4", 0.65f, FameColors.GrowthGreen)
            ObjectiveItem("Cup Progress", "Quarter Final", 0.3f, FameColors.TrophyGold)
        }
    }
}

@Composable
private fun ObjectiveItem(title: String, target: String, progress: Float, color: Color) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(title, style = AFMTextStyles.textXXS, color = Color.White)
            Text(target, style = AFMTextStyles.textXXS, color = color, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(2.dp))
        LinearProgressIndicator(
        progress = { progress },
        modifier = Modifier.fillMaxWidth().height(2.dp),
        color = color,
        trackColor = Color.White.copy(alpha = 0.1f),
        strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
        )
    }
}

@Composable
fun FinancialSummaryWidget(transferBudget: String, wageBudget: String, onViewMore: () -> Unit) {
    WidgetContainer(title = "FINANCES", onAction = onViewMore) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            FinancialRow("Transfer", transferBudget, FameColors.TrophyGold)
            FinancialRow("Wage/w", wageBudget, Color.White)
        }
    }
}

@Composable
private fun FinancialRow(label: String, value: String, color: Color) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = AFMTextStyles.textXXS, color = FameColors.MutedParchment)
        Text(value, style = AFMTextStyles.textXXS, color = color, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun DashboardDualNavigation(
    activeTab: String,
    onTabClick: (String) -> Unit,
    unreadMessages: Int,
    pendingTransfers: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.95f))
    ) {
        // Quick Action Nav (Top Row)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            NavActionItem("INBOX", Icons.Default.Email, unreadMessages, onTabClick)
            NavActionItem("TRANSFERS", Icons.Default.SwapHoriz, pendingTransfers, onTabClick)
            NavActionItem("SQUAD", Icons.Default.Groups, 0, onTabClick)
            NavActionItem("TACTICS", Icons.Default.GridOn, 0, onTabClick)
            NavActionItem("TRAINING", Icons.Default.FitnessCenter, 0, onTabClick)
            NavActionItem("WORLD", Icons.Default.Public, 0, onTabClick)
        }
        
        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

        // Main Nav (Bottom Row)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MainNavItem("DASHBOARD", Icons.Default.Dashboard, activeTab == "DASHBOARD", onTabClick)
            MainNavItem("CLUB", Icons.Default.Shield, activeTab == "CLUB", onTabClick)
            MainNavItem("SCHEDULE", Icons.Default.CalendarToday, activeTab == "SCHEDULE", onTabClick)
            MainNavItem("COMPETITIONS", Icons.Default.EmojiEvents, activeTab == "COMPETITIONS", onTabClick)
            MainNavItem("SCOUTING", Icons.Default.Search, activeTab == "SCOUTING", onTabClick)
            MainNavItem("MORE", Icons.Default.MoreHoriz, activeTab == "MORE", onTabClick)
        }
    }
}

@Composable
private fun NavActionItem(label: String, icon: ImageVector, badgeCount: Int, onClick: (String) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick(label) }.padding(4.dp)
    ) {
        BadgedBox(
            badge = {
                if (badgeCount > 0) {
                    Badge(containerColor = FameColors.AlertRed) {
                        Text(badgeCount.toString(), color = Color.White, fontSize = 8.sp)
                    }
                }
            }
        ) {
            Icon(icon, null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
        }
        Text(label, style = AFMTextStyles.textXXS, color = FameColors.MutedParchment, fontSize = 6.sp)
    }
}

@Composable
private fun MainNavItem(label: String, icon: ImageVector, selected: Boolean, onClick: (String) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick(label) }.padding(4.dp)
    ) {
        Icon(
            icon, 
            null, 
            tint = if (selected) FameColors.TrophyGold else Color.White.copy(alpha = 0.5f),
            modifier = Modifier.size(24.dp)
        )
        Text(
            label, 
            style = AFMTextStyles.textXXS, 
            color = if (selected) FameColors.TrophyGold else FameColors.MutedParchment,
            fontSize = 7.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun WidgetContainer(
    title: String,
    subtitle: String? = null,
    backgroundColor: Color = Color.Black.copy(alpha = 0.5f),
    onAction: (() -> Unit)? = null,
    actionText: String = "VIEW ALL",
    modifier: Modifier = Modifier,
    content: @Composable (ColumnScope.() -> Unit)
) {
    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(title, style = AFMTextStyles.textXS, color = FameColors.TrophyGold, fontWeight = FontWeight.Black)
                    if (subtitle != null) {
                        Text(subtitle, style = AFMTextStyles.textXXS, color = FameColors.MutedParchment, fontSize = 7.sp)
                    }
                }
                if (onAction != null) {
                    Text(
                        text = actionText,
                        style = AFMTextStyles.textXXS.copy(fontWeight = FontWeight.Bold),
                        color = FameColors.GrowthGreen,
                        modifier = Modifier.clickable { onAction() }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}
