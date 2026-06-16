package com.fameafrica.afm.ui.screen.match

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.fameafrica.afm.data.database.entities.MatchEventsEntity
import com.fameafrica.afm.ui.theme.AFM2026Theme
import com.fameafrica.afm.ui.theme.FameColors
import kotlin.math.*

enum class PitchPattern {
    VERTICAL_STRIPES,
    HORIZONTAL_STRIPES,
    CHECKERED,
    CIRCULAR,
    DIAMOND,
    MERGED_STRIPES,
    NONE
}

enum class TeamSide { HOME, AWAY }

data class TacticalPlayer(
    val id: Int,
    val side: TeamSide,
    val isGk: Boolean,
    val number: String,
    val baseX: Float,
    val baseY: Float
)

/**
 * Live Match Visualizer - Pro Tactics Board Style
 * Draws real-time events on a 2D pitch with 22 animated player dots reacting to the play.
 */
@Composable
fun LiveMatchVisualizer(
    modifier: Modifier = Modifier,
    events: List<MatchEventsEntity>,
    currentMinute: Int,
    matchSpeed: Int = 1,
    pitchPattern: PitchPattern = PitchPattern.VERTICAL_STRIPES,
    homeColor: Color = FameColors.Success, // Default e.g. Yanga
    awayColor: Color = Color.Red,          // Default e.g. Simba
    homeGkColor: Color = Color(0xFFE040FB),
    awayGkColor: Color = Color(0xFF00E5FF)
) {
    val recentEvents = events.takeLast(3)
    val currentEvent = events.lastOrNull()

    Box(modifier = modifier
        .fillMaxWidth()
        .height(280.dp)
        .clip(RoundedCornerShape(8.dp))
        .background(FameColors.StadiumBlack)
    ) {
        // 1. Base Pitch
        PitchCanvas(
            modifier = Modifier.fillMaxSize(),
            pattern = pitchPattern
        )

        // 2. Tactical Player Dots (22 Players)
        TacticsBoardOverlay(
            currentEvent = currentEvent,
            homeColor = homeColor,
            awayColor = awayColor,
            homeGkColor = homeGkColor,
            awayGkColor = awayGkColor
        )

        // 3. Event Tails (Ghosting)
        recentEvents.dropLast(1).forEach { event ->
            EventPathOverlay(event = event, alpha = 0.2f)
        }

        // 4. Active Event Animation (Ball/Action)
        EventOverlay(event = currentEvent, matchSpeed = matchSpeed)

        // 5. Dynamic Event Label
        if (currentEvent != null) {
            EventLabel(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp),
                event = currentEvent
            )
        }
    }
}

@Composable
fun TacticsBoardOverlay(
    currentEvent: MatchEventsEntity?,
    homeColor: Color,
    awayColor: Color,
    homeGkColor: Color,
    awayGkColor: Color
) {
    // Generate Standard 4-3-3 vs 4-2-3-1 formations for visual appeal
    val players = remember { generateFormations() }

    // Idle breathing animation for realism (simulates players staying on their toes)
    val infiniteTransition = rememberInfiniteTransition(label = "player_breathing")
    val idleTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "idle_movement"
    )

    // Calculate ball/event gravity to shift the team shapes naturally
    val eventX = currentEvent?.goalX ?: 0.5f
    val eventY = currentEvent?.goalY ?: 0.5f

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val dotRadius = 6.dp.toPx()
        val strokeWidth = 1.5.dp.toPx()

        // Paint for player numbers
        val textPaint = Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 8.sp.toPx()
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        players.forEach { player ->
            // 1. Base Tactical Position
            var targetX = player.baseX
            var targetY = player.baseY

            // 2. Tactical Shifting (Ball Gravity / Defensive Sliding)
            // Players shift slightly towards the ball to simulate tactical compactness
            val distanceToBall = sqrt((eventX - targetX).pow(2) + (eventY - targetY).pow(2))
            val shiftFactor = if (player.isGk) 0.05f else 0.25f // GKs stay near line, outfielders shift more

            targetX += (eventX - targetX) * shiftFactor * (1f - distanceToBall)
            targetY += (eventY - targetY) * shiftFactor * (1f - distanceToBall)

            // 3. Apply Idle Breathing
            val idleOffsetX = cos(idleTime + player.id) * 0.005f
            val idleOffsetY = sin(idleTime + player.id) * 0.005f

            val finalX = (targetX + idleOffsetX) * w
            val finalY = (targetY + idleOffsetY) * h

            val center = Offset(finalX, finalY)
            val color = when {
                player.side == TeamSide.HOME && player.isGk -> homeGkColor
                player.side == TeamSide.HOME -> homeColor
                player.side == TeamSide.AWAY && player.isGk -> awayGkColor
                else -> awayColor
            }

            // Draw Shadow
            drawCircle(
                color = Color.Black.copy(alpha = 0.4f),
                radius = dotRadius,
                center = Offset(center.x + 2f, center.y + 4f)
            )

            // Draw Player Fill
            drawCircle(
                color = color,
                radius = dotRadius,
                center = center
            )

            // Draw Player Border
            drawCircle(
                color = Color.White,
                radius = dotRadius,
                center = center,
                style = Stroke(width = strokeWidth)
            )

            // Draw Number
            drawContext.canvas.nativeCanvas.drawText(
                player.number,
                center.x,
                center.y - ((textPaint.descent() + textPaint.ascent()) / 2),
                textPaint
            )
        }
    }
}

private fun generateFormations(): List<TacticalPlayer> {
    val list = mutableListOf<TacticalPlayer>()

    // HOME TEAM (Attacking Left to Right) - 4-3-3
    list.add(TacticalPlayer(1, TeamSide.HOME, true, "1", 0.06f, 0.5f)) // GK
    list.add(TacticalPlayer(2, TeamSide.HOME, false, "2", 0.22f, 0.15f)) // RB
    list.add(TacticalPlayer(3, TeamSide.HOME, false, "5", 0.18f, 0.38f)) // RCB
    list.add(TacticalPlayer(4, TeamSide.HOME, false, "4", 0.18f, 0.62f)) // LCB
    list.add(TacticalPlayer(5, TeamSide.HOME, false, "3", 0.22f, 0.85f)) // LB
    list.add(TacticalPlayer(6, TeamSide.HOME, false, "6", 0.35f, 0.5f))  // CDM
    list.add(TacticalPlayer(7, TeamSide.HOME, false, "8", 0.42f, 0.3f))  // RCM
    list.add(TacticalPlayer(8, TeamSide.HOME, false, "10", 0.42f, 0.7f)) // LCM
    list.add(TacticalPlayer(9, TeamSide.HOME, false, "7", 0.55f, 0.2f))  // RW
    list.add(TacticalPlayer(10, TeamSide.HOME, false, "9", 0.60f, 0.5f)) // ST
    list.add(TacticalPlayer(11, TeamSide.HOME, false, "11", 0.55f, 0.8f)) // LW

    // AWAY TEAM (Attacking Right to Left) - 4-2-3-1
    list.add(TacticalPlayer(12, TeamSide.AWAY, true, "1", 0.94f, 0.5f)) // GK
    list.add(TacticalPlayer(13, TeamSide.AWAY, false, "2", 0.78f, 0.15f)) // LB (Mirrored Y for alignment visually)
    list.add(TacticalPlayer(14, TeamSide.AWAY, false, "6", 0.82f, 0.38f)) // LCB
    list.add(TacticalPlayer(15, TeamSide.AWAY, false, "5", 0.82f, 0.62f)) // RCB
    list.add(TacticalPlayer(16, TeamSide.AWAY, false, "3", 0.78f, 0.85f)) // RB
    list.add(TacticalPlayer(17, TeamSide.AWAY, false, "8", 0.65f, 0.35f)) // LDM
    list.add(TacticalPlayer(18, TeamSide.AWAY, false, "4", 0.65f, 0.65f)) // RDM
    list.add(TacticalPlayer(19, TeamSide.AWAY, false, "11", 0.55f, 0.25f)) // LW
    list.add(TacticalPlayer(20, TeamSide.AWAY, false, "10", 0.58f, 0.5f))  // CAM
    list.add(TacticalPlayer(21, TeamSide.AWAY, false, "7", 0.55f, 0.75f))  // RW
    list.add(TacticalPlayer(22, TeamSide.AWAY, false, "9", 0.45f, 0.5f))  // ST

    return list
}

@Composable
fun PitchCanvas(
    modifier: Modifier = Modifier,
    pattern: PitchPattern = PitchPattern.VERTICAL_STRIPES
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = 1.5.dp.toPx()
        val color = Color.White.copy(alpha = 0.4f)
        val darkGreen = FameColors.PitchGreen
        val lightGreen = FameColors.MatchPitch

        drawRect(darkGreen)

        when (pattern) {
            PitchPattern.VERTICAL_STRIPES -> {
                val stripeWidth = w / 10
                for (i in 0..10) if (i % 2 != 0) drawRect(lightGreen, Offset(i * stripeWidth, 0f), Size(stripeWidth, h))
            }
            PitchPattern.HORIZONTAL_STRIPES -> {
                val stripeHeight = h / 6
                for (i in 0..6) if (i % 2 != 0) drawRect(lightGreen, Offset(0f, i * stripeHeight), Size(w, stripeHeight))
            }
            PitchPattern.CHECKERED -> {
                val cellW = w / 10
                val cellH = h / 6
                for (r in 0 until 6) for (c in 0 until 10) if ((r + c) % 2 != 0) drawRect(lightGreen, Offset(c * cellW, r * cellH), Size(cellW, cellH))
            }
            PitchPattern.CIRCULAR -> {
                val step = 40.dp.toPx()
                var r = sqrt((w/2)*(w/2) + (h/2)*(h/2))
                var toggle = false
                while (r > 0) {
                    drawCircle(if (toggle) lightGreen else darkGreen, r, Offset(w / 2, h / 2))
                    r -= step
                    toggle = !toggle
                }
            }
            PitchPattern.DIAMOND -> {
                val step = 60.dp.toPx()
                var r = 0f
                var i = 0
                while (r < w + h) {
                    val path = Path().apply {
                        moveTo(w/2, h/2 - r); lineTo(w/2 + r, h/2); lineTo(w/2, h/2 + r); lineTo(w/2 - r, h/2); close()
                    }
                    if (i % 2 != 0) drawPath(path, lightGreen)
                    r += step; i++
                }
            }
            PitchPattern.MERGED_STRIPES -> {
                for (i in 0 until 10) if (i % 2 != 0) drawRect(lightGreen, Offset(i * (w/10), 0f), Size(w/10, h))
                for (i in 0 until 6) if (i % 2 != 0) drawRect(lightGreen.copy(alpha = 0.5f), Offset(0f, i * (h/6)), Size(w, h/6))
            }
            PitchPattern.NONE -> {}
        }

        // Markings
        drawRect(color, style = Stroke(stroke))
        drawLine(color, Offset(w / 2, 0f), Offset(w / 2, h), stroke)
        drawCircle(color, h * 0.2f, Offset(w / 2, h / 2), style = Stroke(stroke))
        drawCircle(color, 3.dp.toPx(), Offset(w / 2, h / 2))

        // Penalty Areas
        drawRect(color, Offset(0f, h * 0.2f), Size(w * 0.16f, h * 0.6f), style = Stroke(stroke))
        drawRect(color, Offset(w * 0.84f, h * 0.2f), Size(w * 0.16f, h * 0.6f), style = Stroke(stroke))

        // 6 Yard Boxes
        drawRect(color, Offset(0f, h * 0.36f), Size(w * 0.06f, h * 0.28f), style = Stroke(stroke))
        drawRect(color, Offset(w * 0.94f, h * 0.36f), Size(w * 0.06f, h * 0.28f), style = Stroke(stroke))

        // Goals
        drawRect(Color.White, Offset(-2.dp.toPx(), h * 0.42f), Size(4.dp.toPx(), h * 0.16f))
        drawRect(Color.White, Offset(w - 2.dp.toPx(), h * 0.42f), Size(4.dp.toPx(), h * 0.16f))
    }
}

@Composable
fun EventPathOverlay(event: MatchEventsEntity, alpha: Float) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val startX = 0.5f
        val startY = 0.5f
        val endX = event.goalX ?: 0.5f
        val endY = event.goalY ?: 0.5f

        drawLine(
            color = Color.White.copy(alpha = alpha),
            start = Offset(startX * size.width, startY * size.height),
            end = Offset(endX * size.width, endY * size.height),
            strokeWidth = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f))
        )
    }
}

@Composable
fun EventOverlay(event: MatchEventsEntity?, matchSpeed: Int) {
    if (event == null) return

    val animDuration = (1200 / matchSpeed).coerceAtLeast(400)
    val animatedProgress = remember { Animatable(0f) }

    val startX = 0.5f
    val startY = 0.5f
    val endX = event.goalX ?: 0.5f
    val endY = event.goalY ?: 0.5f

    LaunchedEffect(event) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(1f, tween(animDuration, easing = FastOutSlowInEasing))
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val start = Offset(startX * size.width, startY * size.height)
        val end = Offset(endX * size.width, endY * size.height)
        val current = Offset(lerp(start.x, end.x, animatedProgress.value), lerp(start.y, end.y, animatedProgress.value))

        // Draw Active Ball (High contrast to stand out from players)
        if (event.eventType !in listOf("YELLOW_CARD", "RED_CARD", "SUBSTITUTION", "INJURY", "VAR")) {
            drawCircle(Color.Black, 8f, current)
            drawCircle(Color.White, 6f, current)
        }

        when (event.eventType) {
            "GOAL", "PENALTY_SCORED", "OWN_GOAL" -> {
                val trailColor = if (event.eventType == "OWN_GOAL") Color.Red else FameColors.ChampionsGold
                drawLine(trailColor.copy(0.6f), start, end, 3.dp.toPx())
                if (animatedProgress.value > 0.7f) {
                    val rippleAlpha = (1f - animatedProgress.value) * 3.3f
                    drawCircle(trailColor.copy(alpha = rippleAlpha.coerceIn(0f, 1f)), radius = 200f * (animatedProgress.value - 0.7f), center = end, style = Stroke(3.dp.toPx()))
                }
            }
            "SHOT", "SHOT_ON_TARGET", "SHOT_OFF_TARGET", "PENALTY_MISSED" -> {
                val isMiss = event.eventType == "SHOT_OFF_TARGET" || event.eventType == "PENALTY_MISSED"
                val dash = if (isMiss) PathEffect.dashPathEffect(floatArrayOf(10f, 10f)) else null
                drawLine(Color.White.copy(0.8f), start, end, 2.dp.toPx(), pathEffect = dash)
                if (isMiss && animatedProgress.value > 0.9f) {
                    drawLine(Color.Red, Offset(end.x - 10f, end.y - 10f), Offset(end.x + 10f, end.y + 10f), 3.dp.toPx())
                    drawLine(Color.Red, Offset(end.x + 10f, end.y - 10f), Offset(end.x - 10f, end.y + 10f), 3.dp.toPx())
                }
            }
            "PASS" -> {
                drawLine(Color.White.copy(0.6f), start, end, 2.dp.toPx())
            }
            "ASSIST", "CORNER", "CROSS" -> {
                val arcPath = Path().apply {
                    moveTo(start.x, start.y)
                    val controlX = (start.x + end.x) / 2
                    val controlY = min(start.y, end.y) - 60.dp.toPx()
                    quadraticTo(controlX, controlY, end.x, end.y)
                }
                drawPath(arcPath, Color.White.copy(0.6f), style = Stroke(2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))))
            }
            "DRIBBLING", "DRIBBLE" -> {
                val wigglePath = Path().apply {
                    moveTo(start.x, start.y)
                    val segments = 40
                    val dx = end.x - start.x; val dy = end.y - start.y
                    val angle = atan2(dy, dx); val perpAngle = angle + (PI / 2).toFloat()
                    for (i in 1..segments) {
                        val p = i.toFloat() / segments
                        if (p > animatedProgress.value) break
                        val lx = lerp(start.x, end.x, p); val ly = lerp(start.y, end.y, p)
                        val wiggle = 12f * sin(p * PI * 10).toFloat()
                        lineTo(lx + cos(perpAngle.toDouble()).toFloat() * wiggle, ly + sin(perpAngle.toDouble()).toFloat() * wiggle)
                    }
                }
                drawPath(wigglePath, Color.Yellow.copy(0.7f), style = Stroke(2.dp.toPx()))
            }
            "YELLOW_CARD", "RED_CARD" -> {
                val cardColor = if (event.eventType == "RED_CARD") Color.Red else Color.Yellow
                drawCircle(cardColor.copy(alpha = (1f - animatedProgress.value).coerceIn(0f, 0.4f)), radius = 100f * animatedProgress.value, center = end)
                rotate(degrees = 15f, pivot = end) {
                    drawRect(Color.Black.copy(0.3f), Offset(end.x - 13f, end.y - 18f), Size(26f, 36f)) // Shadow
                    drawRect(cardColor, Offset(end.x - 12f, end.y - 17f), Size(24f, 34f))
                }
            }
            "SAVE" -> {
                drawCircle(Color.Cyan.copy(alpha = 0.5f), 50f * animatedProgress.value, end, style = Stroke(4.dp.toPx()))
            }
            "SUBSTITUTION" -> {
                rotate(degrees = 360f * animatedProgress.value, pivot = end) {
                    drawCircle(FameColors.Success.copy(0.4f), 35f, end)
                    drawLine(FameColors.Success, Offset(end.x - 14f, end.y), Offset(end.x + 14f, end.y), 4.dp.toPx())
                    drawLine(Color.Red, Offset(end.x, end.y - 14f), Offset(end.x, end.y + 14f), 4.dp.toPx())
                }
            }
            "INJURY" -> {
                val crossSize = 20f
                drawLine(Color.Red, Offset(end.x - crossSize, end.y), Offset(end.x + crossSize, end.y), 6.dp.toPx())
                drawLine(Color.Red, Offset(end.x, end.y - crossSize), Offset(end.x, end.y + crossSize), 6.dp.toPx())
            }
            "VAR" -> {
                drawRect(Color.White.copy(0.8f), Offset(end.x - 30f, end.y - 20f), Size(60f, 40f), style = Stroke(3.dp.toPx()))
                drawRect(Color.White.copy(0.2f), Offset(end.x - 24f, end.y - 14f), Size(48f, 28f))
            }
            "FOUL", "OFFSIDE", "BLOCK" -> {
                drawCircle(Color.White.copy(alpha = 0.3f), 40f * animatedProgress.value, end, style = Stroke(2.dp.toPx()))
                drawLine(Color.White, Offset(end.x - 10f, end.y - 10f), Offset(end.x + 10f, end.y + 10f), 3.dp.toPx())
                drawLine(Color.White, Offset(end.x + 10f, end.y - 10f), Offset(end.x - 10f, end.y + 10f), 3.dp.toPx())
            }
            else -> {
                drawLine(Color.White.copy(0.4f), start, end, 2.dp.toPx())
            }
        }
    }
}

@Composable
fun EventLabel(modifier: Modifier = Modifier, event: MatchEventsEntity) {
    Surface(
        modifier = modifier,
        color = Color.Black.copy(alpha = 0.85f),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val labelColor = when {
                event.eventType.contains("GOAL") -> FameColors.PitchGreen
                event.eventType == "RED_CARD" || event.eventType == "OWN_GOAL" -> Color.Red
                event.eventType == "YELLOW_CARD" -> Color.Yellow
                event.eventType == "SAVE" || event.eventType == "BLOCK" -> Color.Cyan
                event.eventType == "VAR" -> Color.White
                else -> FameColors.ChampionsGold
            }

            Text(
                text = event.eventType.replace("_", " "),
                color = labelColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = event.playerName.uppercase(),
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview(showBackground = true, name = "Goal Tactical View")
@Composable
fun PreviewLiveMatchTacticalGoal() {
    AFM2026Theme {
        LiveMatchVisualizer(
            events = listOf(
                MatchEventsEntity(matchId = 1, minute = 11, eventType = "GOAL", playerName = "Kennedy Musonda", playerId = 1, teamName = "Young Africans", teamId = 1, goalX = 0.90f, goalY = 0.5f)
            ),
            currentMinute = 11
        )
    }
}

@Preview(showBackground = true, name = "Dribble Tactical View")
@Composable
fun PreviewLiveMatchTacticalDribble() {
    AFM2026Theme {
        LiveMatchVisualizer(
            events = listOf(
                MatchEventsEntity(matchId = 1, minute = 20, eventType = "DRIBBLING", playerName = "Stephane Aziz Ki", playerId = 6, teamName = "Young Africans", teamId = 1, goalX = 0.7f, goalY = 0.8f)
            ),
            currentMinute = 20
        )
    }
}