package com.fameafrica.afm.ui.components.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SportsScore
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.fameafrica.afm.data.database.entities.PlayersEntity
import com.fameafrica.afm.ui.screen.match.PitchPattern
import com.fameafrica.afm.ui.screen.tactics.TacticsDisplayMode
import com.fameafrica.afm.ui.screen.tactics.TacticsVisualizationMode
import com.fameafrica.afm.ui.screen.tactics.TeamRoles
import com.fameafrica.afm.ui.theme.AFMTextStyles
import com.fameafrica.afm.ui.theme.FameColors
import com.fameafrica.afm.utils.NationalityUtils
import com.fameafrica.afm.utils.PlayerAssetUtils
import kotlin.math.sqrt

@Composable
fun getRatingColor(rating: Int): Color {
    return when {
        rating >= 85 -> MaterialTheme.colorScheme.primary // Trophy Gold / Growth Green
        rating >= 75 -> FameColors.PitchGreen
        rating >= 65 -> Color(0xFFFFA000) // Orange
        else -> Color(0xFFEF5350) // Red
    }
}

fun isFullyAdaptable(playerPos: String, assignedPos: String): Boolean {
    return when (playerPos) {
        "CDM" -> assignedPos == "CM"
        "CM" -> assignedPos == "CDM" || assignedPos == "CAM"
        "CAM" -> assignedPos == "CM" || assignedPos == "CF" || assignedPos == "ST"
        "LM" -> assignedPos == "LW" || assignedPos == "RW" || assignedPos == "RM"
        "RM" -> assignedPos == "RW" || assignedPos == "LW" || assignedPos == "LM"
        "LW" -> assignedPos == "RW" || assignedPos == "LM" || assignedPos == "RM"
        "RW" -> assignedPos == "LW" || assignedPos == "RM" || assignedPos == "LM"
        "CF" -> assignedPos == "ST" || assignedPos == "CAM"
        "ST" -> assignedPos == "CF" || assignedPos == "CAM"
        else -> false
    }
}

fun calculateEffectiveRating(player: PlayersEntity, assignedPos: String): Int {
    if (assignedPos == "SUB" || assignedPos == "" || assignedPos == "RES") return player.overallRating
    if (player.position == assignedPos) return player.overallRating

    val fullAdaptability = isFullyAdaptable(player.position, assignedPos)

    if (fullAdaptability) return player.overallRating

    val categories = mapOf(
        "GK" to listOf("GK"),
        "DEF" to listOf("CB", "LB", "RB", "LWB", "RWB"),
        "MID" to listOf("CDM", "CM", "CAM", "LM", "RM"),
        "FWD" to listOf("ST", "CF", "LW", "RW")
    )

    val playerCat = categories.entries.find { it.value.contains(player.position) }?.key
    val targetCat = categories.entries.find { it.value.contains(assignedPos) }?.key

    return when {
        player.position == "CB" && (assignedPos == "LB" || assignedPos == "RB") -> {
            if (player.pace > 70) (player.overallRating * 0.92).toInt()
            else (player.overallRating * 0.85).toInt()
        }
        playerCat == targetCat -> (player.overallRating * 0.95).toInt()
        else -> (player.overallRating * 0.75).toInt()
    }
}

@Composable
fun PlayerOnPitchCard(
    player: PlayersEntity,
    assignedPosition: String,
    isSelected: Boolean = false,
    displayMode: TacticsDisplayMode = TacticsDisplayMode.RATING,
    roles: TeamRoles = TeamRoles(),
    onClick: () -> Unit = {}
) {
    val effectiveRating = calculateEffectiveRating(player, assignedPosition)
    val ratingColor = getRatingColor(effectiveRating)

    val fullAdaptability = isFullyAdaptable(player.position, assignedPosition)
    val isWeakFit = effectiveRating < player.overallRating && !fullAdaptability

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(62.dp)
            .clickable { onClick() }
    ) {
        Box(contentAlignment = Alignment.Center) {
            // Face Container
            Surface(
                shape = RoundedCornerShape(2.dp),
                modifier = Modifier.size(46.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.4f),
                border = BorderStroke(
                    width = 1.dp,
                    color = when {
                        isSelected -> MaterialTheme.colorScheme.primary
                        isWeakFit -> FameColors.AlertRed.copy(alpha = 0.8f)
                        else -> Color.White.copy(alpha = 0.15f)
                    }
                )
            ) {
                Box(contentAlignment = Alignment.Center) {
                    // Dynamic Player Face
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(PlayerAssetUtils.getPlayerFace(player))
                            .crossfade(true)
                            .build(),
                        contentDescription = player.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Optional Overlay for display modes
                    if (displayMode == TacticsDisplayMode.NATIONALITY) {
                        Surface(
                            color = Color.Black.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxSize()
                        ) {}
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(NationalityUtils.getWavingFlagUrl(player.nationality))
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }

            // Nationality Flag Tag (Always show a small one)
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(NationalityUtils.getFlagUrl(player.nationality))
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .size(12.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 2.dp, y = (-2).dp)
                    .clip(RoundedCornerShape(1.dp))
                    .border(0.5.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(1.dp)),
                contentScale = ContentScale.FillBounds
            )

            RoleIconsOverlay(player.id, roles)

            // Rating/Condition Bubble
            val displayValue = if (displayMode == TacticsDisplayMode.RATING) effectiveRating.toString() else player.currentForm.toString()
            Surface(
                color = if (displayMode == TacticsDisplayMode.RATING) ratingColor else FameColors.DeepNavyBlack,
                shape = RoundedCornerShape(1.dp),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = 6.dp)
            ) {
                Text(
                    text = displayValue,
                    style = AFMTextStyles.textXS,
                    color = if (displayMode == TacticsDisplayMode.RATING) Color.Black else Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 9.sp,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = player.name.split(" ").last().uppercase(),
            style = AFMTextStyles.textXS.copy(fontSize = 9.sp),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )

        Text(
            assignedPosition.uppercase(),
            style = AFMTextStyles.textXS.copy(fontSize = 7.sp),
            color = if (isWeakFit) FameColors.AlertRed else MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
fun RoleIconsOverlay(playerId: Int, roles: TeamRoles) {
    Box(modifier = Modifier.size(44.dp)) {
        if (roles.captainId == playerId) {
            Surface(
                modifier = Modifier.size(10.dp).align(Alignment.TopStart).offset(x = (-2).dp, y = (-2).dp),
                color = FameColors.TrophyGold,
                shape = RoundedCornerShape(1.dp)
            ) {
                Text(
                    "C", 
                    color = Color.Black, 
                    fontSize = 7.sp, 
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )
            }
        }

        if (roles.penaltyTakerId == playerId) {
            Icon(
                imageVector = Icons.Default.SportsScore,
                contentDescription = "Penalty Taker",
                tint = Color.White,
                modifier = Modifier.size(10.dp).align(Alignment.TopEnd).offset(x = 2.dp, y = (-2).dp)
            )
        }
    }
}

@Composable
fun PitchCanvas(
    modifier: Modifier = Modifier,
    pattern: PitchPattern = PitchPattern.VERTICAL_STRIPES,
    mode: TacticsVisualizationMode = TacticsVisualizationMode.NONE,
    showMarkings: Boolean = true
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = 1.2.dp.toPx()
        val color = Color.White.copy(alpha = 0.25f)
        val darkGreen = FameColors.PitchGreen
        val lightGreen = FameColors.MatchPitch

        drawRect(darkGreen)

        when (pattern) {
            PitchPattern.VERTICAL_STRIPES -> {
                val stripeWidth = w / 10
                for (i in 0..10) {
                    if (i % 2 != 0) {
                        drawRect(
                            color = lightGreen,
                            topLeft = Offset(i * stripeWidth, 0f),
                            size = Size(stripeWidth, h)
                        )
                    }
                }
            }
            PitchPattern.HORIZONTAL_STRIPES -> {
                val stripeHeight = h / 8
                for (i in 0..8) {
                    if (i % 2 != 0) {
                        drawRect(
                            color = lightGreen,
                            topLeft = Offset(0f, i * stripeHeight),
                            size = Size(w, stripeHeight)
                        )
                    }
                }
            }
            PitchPattern.CHECKERED -> {
                val rows = 8
                val cols = 6
                val cellW = w / cols
                val cellH = h / rows
                for (r in 0 until rows) {
                    for (c in 0 until cols) {
                        if ((r + c) % 2 != 0) {
                            drawRect(
                                color = lightGreen,
                                topLeft = Offset(c * cellW, r * cellH),
                                size = Size(cellW, cellH)
                            )
                        }
                    }
                }
            }
            PitchPattern.CIRCULAR -> {
                val centerX = w / 2
                val centerY = h / 2
                val maxRadius = sqrt(centerX * centerX + centerY * centerY)
                val step = 40.dp.toPx()
                var r = maxRadius
                var toggle = false
                while (r > 0) {
                    drawCircle(
                        color = if (toggle) lightGreen else darkGreen,
                        radius = r,
                        center = Offset(centerX, centerY)
                    )
                    r -= step
                    toggle = !toggle
                }
            }
            PitchPattern.DIAMOND -> {
                val centerX = w / 2
                val centerY = h / 2
                val step = 60.dp.toPx()
                var r = 0f
                var i = 0
                while (r < w + h) {
                    val path = Path().apply {
                        moveTo(centerX, centerY - r)
                        lineTo(centerX + r, centerY)
                        lineTo(centerX, centerY + r)
                        lineTo(centerX - r, centerY)
                        close()
                    }
                    if (i % 2 != 0) {
                        drawPath(path, lightGreen)
                    }
                    r += step
                    i++
                }
            }
            PitchPattern.MERGED_STRIPES -> {
                val vCount = 10
                val hCount = 8
                val vWidth = w / vCount
                val hHeight = h / hCount
                for (i in 0 until vCount) {
                    if (i % 2 != 0) {
                        drawRect(color = lightGreen, topLeft = Offset(i * vWidth, 0f), size = Size(vWidth, h))
                    }
                }
                for (i in 0 until hCount) {
                    if (i % 2 != 0) {
                        drawRect(color = lightGreen.copy(alpha = 0.5f), topLeft = Offset(0f, i * hHeight), size = Size(w, hHeight))
                    }
                }
            }
            else -> { }
        }

        if (showMarkings) {
            drawRect(color, style = Stroke(stroke))
            drawLine(color, Offset(0f, h / 2), Offset(w, h / 2), stroke)
            drawCircle(color, w * 0.18f, Offset(w / 2, h / 2), style = Stroke(stroke))
            drawCircle(color, 1.5.dp.toPx(), Offset(w / 2, h / 2))
            drawRect(color, Offset(w * 0.2f, 0f), Size(w * 0.6f, h * 0.14f), style = Stroke(stroke))
            drawRect(color, Offset(w * 0.35f, 0f), Size(w * 0.3f, h * 0.05f), style = Stroke(stroke))
            drawCircle(color, 1.5.dp.toPx(), Offset(w / 2, h * 0.10f))
            drawRect(color, Offset(w * 0.2f, h * 0.86f), Size(w * 0.6f, h * 0.14f), style = Stroke(stroke))
            drawRect(color, Offset(w * 0.35f, h * 0.95f), Size(w * 0.3f, h * 0.05f), style = Stroke(stroke))
            drawCircle(color, 1.5.dp.toPx(), Offset(w / 2, h * 0.90f))
            drawRect(Color.White.copy(0.6f), Offset(w * 0.42f, -1.dp.toPx()), Size(w * 0.16f, 3.dp.toPx()))
            drawRect(Color.White.copy(0.6f), Offset(w * 0.42f, h - 2.dp.toPx()), Size(w * 0.16f, 3.dp.toPx()))
        }

        // --- NEW TACTICAL VISUALIZATIONS ---
        when (mode) {
            TacticsVisualizationMode.HEATMAP -> {
                // FAKE DETAIL: Draw hotspots
                val hotspots = listOf(
                    Offset(w * 0.2f, h * 0.3f), Offset(w * 0.8f, h * 0.3f),
                    Offset(w * 0.5f, h * 0.7f), Offset(w * 0.5f, h * 0.1f)
                )
                hotspots.forEach { spot ->
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(FameColors.AfroSunOrange.copy(alpha = 0.4f), Color.Transparent),
                            center = spot,
                            radius = w * 0.15f
                        ),
                        radius = w * 0.15f,
                        center = spot
                    )
                }
            }
            TacticsVisualizationMode.PASSING_LINES -> {
                // Draw lines between core positions
                val nodes = listOf(
                    Offset(w * 0.5f, h * 0.9f), // GK
                    Offset(w * 0.3f, h * 0.7f), Offset(w * 0.7f, h * 0.7f), // CBs
                    Offset(w * 0.5f, h * 0.5f), // MID
                    Offset(w * 0.2f, h * 0.3f), Offset(w * 0.8f, h * 0.3f), // Wings
                    Offset(w * 0.5f, h * 0.2f) // ST
                )
                for (i in 0 until nodes.size - 1) {
                    for (j in i + 1 until nodes.size) {
                        if (kotlin.math.abs(i - j) < 3) {
                            drawLine(
                                color = Color.White.copy(alpha = 0.2f),
                                start = nodes[i],
                                end = nodes[j],
                                strokeWidth = 1.dp.toPx(),
                                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                            )
                        }
                    }
                }
            }
            TacticsVisualizationMode.PRESS_ZONES -> {
                drawRect(
                    color = FameColors.AlertRed.copy(alpha = 0.1f),
                    topLeft = Offset(0f, 0f),
                    size = Size(w, h * 0.3f)
                )
                drawRect(
                    color = FameColors.GrowthGreen.copy(alpha = 0.05f),
                    topLeft = Offset(0f, h * 0.3f),
                    size = Size(w, h * 0.4f)
                )
            }
            else -> { }
        }
    }
}
