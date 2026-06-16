package com.fameafrica.afm.ui.screen.events

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.fameafrica.afm.R
import com.fameafrica.afm.domain.model.SimulationEvent
import com.fameafrica.afm.ui.theme.*
import com.fameafrica.afm.ui.util.ImmersiveModeManager

import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

@Composable
fun AfricanGeometricPattern(modifier: Modifier = Modifier, color: Color = Color.White) {
    Canvas(modifier = modifier) {
        val size = this.size
        val step = 40.dp.toPx()
        
        // Draw a grid of Adinkra-inspired shapes or geometric motifs
        for (x in 0..(size.width / step).toInt()) {
            for (y in 0..(size.height / step).toInt()) {
                val cx = x * step + step / 2
                val cy = y * step + step / 2
                
                // Alternating patterns
                if ((x + y) % 2 == 0) {
                    // Diamond pattern
                    val path = Path().apply {
                        moveTo(cx, cy - step / 3)
                        lineTo(cx + step / 3, cy)
                        lineTo(cx, cy + step / 3)
                        lineTo(cx - step / 3, cy)
                        close()
                    }
                    drawPath(path, color, alpha = 0.3f, style = Stroke(width = 1.dp.toPx()))
                } else {
                    // Cross/Star pattern
                    drawLine(color, Offset(cx - step / 4, cy), Offset(cx + step / 4, cy), strokeWidth = 1.dp.toPx(), alpha = 0.3f)
                    drawLine(color, Offset(cx, cy - step / 4), Offset(cx, cy + step / 4), strokeWidth = 1.dp.toPx(), alpha = 0.3f)
                }
            }
        }
    }
}

@Composable
fun SeasonEventOverlay(
    event: SimulationEvent,
    onContinue: () -> Unit,
    onNavigate: () -> Unit
) {
    // Ensure immersive mode is active for cinematic experience
    ImmersiveModeManager.ImmersiveScreen()

    val backgroundImage = when (event) {
        is SimulationEvent.PreseasonStart -> R.drawable.season_preseason_banner
        is SimulationEvent.CommunityShield -> R.drawable.season_community_shield
        is SimulationEvent.LeagueKickoff -> R.drawable.season_league_kickoff
        is SimulationEvent.CupMilestone -> R.drawable.season_cup_round
        is SimulationEvent.CAFGroupDraw, is SimulationEvent.CAFKnockoutDraw -> R.drawable.season_caf_stage
        is SimulationEvent.TransferWindowOpen -> R.drawable.season_transfer_window
        is SimulationEvent.DeadlineDay -> R.drawable.season_deadline_day
        is SimulationEvent.CAFFinal -> R.drawable.season_caf_stage // Or a specific final banner if added
        is SimulationEvent.AwardsGala -> R.drawable.season_awards_gala
        else -> null
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(enabled = false) {} // Intercept clicks
    ) {
        // Fullscreen Cinematic Background
        if (backgroundImage != null) {
            Image(
                painter = painterResource(id = backgroundImage),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.4f // Keep it subtle enough for text readability
            )
        }

        // African Geometric Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.7f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.9f)
                        )
                    )
                )
        )

        // Subtle Background Pattern (African Geometric)
        AfricanGeometricPattern(
            modifier = Modifier.fillMaxSize().alpha(0.08f),
            color = FameColors.TrophyGold
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            when (event) {
                is SimulationEvent.PreseasonStart -> PreseasonEventContent(event)
                is SimulationEvent.CommunityShield -> CommunityShieldEventContent(event)
                is SimulationEvent.LeagueKickoff -> LeagueKickoffEventContent(event)
                is SimulationEvent.CupMilestone -> CupRoundEventContent(event)
                is SimulationEvent.CAFGroupDraw -> CAFGroupDrawContent(event)
                is SimulationEvent.CAFKnockoutDraw -> CAFKnockoutDrawContent(event)
                is SimulationEvent.TransferWindowOpen -> TransferWindowOpenContent(event)
                is SimulationEvent.DeadlineDay -> DeadlineDayContent(event)
                is SimulationEvent.CAFFinal -> CAFFinalContent(event)
                is SimulationEvent.AwardsGala -> AwardsGalaContent(event)
                is SimulationEvent.SponsorshipOffer -> SponsorshipEventContent(event)
                else -> {
                    // Fallback for other events
                    Text("MAJOR EVENT", style = AFMTextStyles.textLG, color = FameColors.TrophyGold)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Secondary Action: Just Continue (Stay on Dashboard)
                OutlinedButton(
                    onClick = onContinue,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    border = BorderStroke(1.dp, FameColors.TrophyGold),
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = FameColors.TrophyGold)
                ) {
                    Text(
                        text = "CONTINUE",
                        style = AFMTextStyles.textMD,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Primary Action: Navigate to appropriate screen
                Button(
                    onClick = onNavigate,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FameColors.TrophyGold),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    val actionText = when (event) {
                        is SimulationEvent.TransferWindowOpen, is SimulationEvent.DeadlineDay -> "GO TO TRANSFERS"
                        is SimulationEvent.PreseasonStart -> "GO TO PRESEASON"
                        is SimulationEvent.LeagueKickoff -> "VIEW LEAGUE"
                        is SimulationEvent.CupMilestone -> "VIEW CUP"
                        is SimulationEvent.SeasonEnd -> "SEASON REVIEW"
                        is SimulationEvent.AwardsGala -> "GO TO GALA"
                        is SimulationEvent.SponsorshipOffer -> "NEGOTIATE"
                        else -> "TAKE ACTION"
                    }
                    Text(
                        text = actionText,
                        style = AFMTextStyles.textMD,
                        color = Color.Black,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun EventHeader(title: String, subtitle: String, color: Color = FameColors.TrophyGold) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title.uppercase(),
            style = AFMTextStyles.textLG.copy(fontSize = 24.sp, letterSpacing = 2.sp),
            color = color,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )
        Text(
            text = subtitle.uppercase(),
            style = AFMTextStyles.textXS.copy(letterSpacing = 4.sp),
            color = FameColors.MutedParchment,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(modifier = Modifier.width(60.dp), color = color, thickness = 2.dp)
    }
}

@Composable
private fun PreseasonEventContent(event: SimulationEvent.PreseasonStart) {
    EventHeader("New Season Approach", "Preseason ${event.season}")
    
    Spacer(modifier = Modifier.height(40.dp))
    
    EventCard(title = "Board Expectations") {
        ExpectationRow("League", "Finish in Top 4", Icons.Default.EmojiEvents)
        ExpectationRow("Cup", "Reach Quarter-Finals", Icons.Default.EmojiEvents)
    }

    Spacer(modifier = Modifier.height(20.dp))

    EventCard(title = "Initial Budget") {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Payments, null, tint = FameColors.GrowthGreen)
            Spacer(modifier = Modifier.width(12.dp))
            Text("${event.budget / 1000000}M USD", style = AFMTextStyles.textLG, color = Color.White)
        }
    }
}

@Composable
private fun CommunityShieldEventContent(event: SimulationEvent.CommunityShield) {
    EventHeader("Community Shield", "Season Opener")
    
    Spacer(modifier = Modifier.height(60.dp))
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TeamLogoLarge(event.team1, FameColors.GrowthGreen)
        Text("VS", style = AFMTextStyles.textLG, color = FameColors.TrophyGold)
        TeamLogoLarge(event.team2, FameColors.AlertRed)
    }

    Spacer(modifier = Modifier.height(40.dp))
    Text("The first trophy of the season is on the line.", textAlign = TextAlign.Center, color = Color.White)
}

@Composable
private fun LeagueKickoffEventContent(event: SimulationEvent.LeagueKickoff) {
    EventHeader(event.leagueName, "Opening Day")
    
    Spacer(modifier = Modifier.height(40.dp))
    
    EventCard(title = "Media Prediction") {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("Predicted Finish", color = FameColors.MutedParchment)
            Text("${event.predictedFinish}th", color = FameColors.TrophyGold, fontWeight = FontWeight.Bold)
        }
    }

    Spacer(modifier = Modifier.height(20.dp))

    EventCard(title = "Opening Fixture") {
        Text("vs ${event.openingOpponent}", style = AFMTextStyles.textMD, color = Color.White, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun CupRoundEventContent(event: SimulationEvent.CupMilestone) {
    EventHeader(event.tournamentName, event.roundName)
    
    Spacer(modifier = Modifier.height(60.dp))
    
    Box(
        modifier = Modifier
            .size(120.dp)
            .clip(CircleShape)
            .background(FameColors.TrophyGold.copy(alpha = 0.1f))
            .border(2.dp, FameColors.TrophyGold, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Default.EmojiEvents, null, modifier = Modifier.size(60.dp), tint = FameColors.TrophyGold)
    }

    Spacer(modifier = Modifier.height(40.dp))
    Text("Opponent Drawn:", style = AFMTextStyles.textXS, color = FameColors.MutedParchment)
    Text(event.opponentName, style = AFMTextStyles.textLG, color = Color.White, fontWeight = FontWeight.Black)
}

@Composable
private fun CAFGroupDrawContent(event: SimulationEvent.CAFGroupDraw) {
    EventHeader("CAF Champions League", "Group Stage Draw")
    
    Spacer(modifier = Modifier.height(40.dp))
    
    EventCard(title = event.groupName) {
        event.opponents.forEach { opponent ->
            Row(modifier = Modifier.padding(vertical = 4.dp)) {
                Icon(Icons.Default.Star, null, tint = FameColors.TrophyGold, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(opponent, color = Color.White)
            }
        }
    }
}

@Composable
private fun CAFKnockoutDrawContent(event: SimulationEvent.CAFKnockoutDraw) {
    EventHeader(event.tournament, event.round)
    
    Spacer(modifier = Modifier.height(60.dp))
    
    TeamLogoLarge(event.opponent, FameColors.TrophyGold)
    
    Spacer(modifier = Modifier.height(40.dp))
    Text("The road to the final becomes a knockout battle.", textAlign = TextAlign.Center, color = Color.White)
}

@Composable
private fun TransferWindowOpenContent(event: SimulationEvent.TransferWindowOpen) {
    EventHeader("Transfer Window Open", "Market Activity Begins")
    
    Spacer(modifier = Modifier.height(40.dp))
    
    EventCard(title = "Financial Report") {
        InfoRow("Transfer Budget", "${event.budget / 1000000}M USD")
        InfoRow("Wage Budget", "${event.wageBudget}/wk")
        InfoRow("Deadline Day", event.deadlineDay)
    }
}

@Composable
private fun DeadlineDayContent(event: SimulationEvent.DeadlineDay) {
    EventHeader("DEADLINE DAY", "${event.hoursRemaining} Hours Remaining", color = FameColors.AlertRed)
    
    Spacer(modifier = Modifier.height(60.dp))
    
    Box(
        modifier = Modifier
            .size(150.dp)
            .border(4.dp, FameColors.AlertRed, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text("${event.hoursRemaining}", style = AFMTextStyles.textLG.copy(fontSize = 48.sp), color = FameColors.AlertRed)
    }
    
    Spacer(modifier = Modifier.height(40.dp))
    Text("TRANSFER WINDOW CLOSING SOON", color = Color.White, fontWeight = FontWeight.Black)
}

@Composable
private fun CAFFinalContent(event: SimulationEvent.CAFFinal) {
    EventHeader("CAF FINAL", "The Pinnacle of African Football")
    
    Spacer(modifier = Modifier.height(60.dp))
    
    Text("vs ${event.opponent}", style = AFMTextStyles.textLG, color = Color.White)
    Text(event.stadium, style = AFMTextStyles.textXS, color = FameColors.MutedParchment)
    
    Spacer(modifier = Modifier.height(40.dp))
    if (event.isFirstFinal) {
        Surface(color = FameColors.TrophyGold.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
            Text("FIRST CONTINENTAL FINAL IN CLUB HISTORY", modifier = Modifier.padding(12.dp), color = FameColors.TrophyGold, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun AwardsGalaContent(event: SimulationEvent.AwardsGala) {
    EventHeader("Season Awards Gala", "Review of ${event.season}")
    
    Spacer(modifier = Modifier.height(40.dp))
    
    EventCard(title = "Final Standings") {
        InfoRow("Position", "${event.position}th")
        InfoRow("Trophies", event.trophiesWon.joinToString(", ").ifEmpty { "None" })
        InfoRow("Top Scorer", event.topScorer)
    }

    Spacer(modifier = Modifier.height(20.dp))

    EventCard(title = "Board Verdict") {
        Text("Fan Approval: ${event.fanApproval}%", color = FameColors.GrowthGreen, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun SponsorshipEventContent(event: SimulationEvent.SponsorshipOffer) {
    EventHeader("New Partnership Offer", event.sponsorName)
    
    Spacer(modifier = Modifier.height(40.dp))
    
    EventCard(title = "Contract Details") {
        InfoRow("Sponsor", event.sponsorName)
        InfoRow("Annual Value", "${event.value / 1000}K USD")
        InfoRow("Duration", "1 Season")
    }

    Spacer(modifier = Modifier.height(20.dp))

    Text(
        "A local business is interested in sponsoring your club. Accepting this offer will boost your bank balance immediately.",
        style = AFMTextStyles.textSM,
        color = Color.White.copy(alpha = 0.8f),
        textAlign = TextAlign.Center
    )
}

@Composable
private fun EventCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title.uppercase(), style = AFMTextStyles.textXXS, color = FameColors.TrophyGold, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun ExpectationRow(label: String, value: String, icon: ImageVector) {
    Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = FameColors.MutedParchment, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, color = FameColors.MutedParchment, modifier = Modifier.weight(1f))
        Text(value, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = FameColors.MutedParchment)
        Text(value, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun TeamLogoLarge(name: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.1f))
                .border(2.dp, color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Shield, null, modifier = Modifier.size(40.dp), tint = color)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(name, style = AFMTextStyles.textXS, color = Color.White, fontWeight = FontWeight.Black)
    }
}
