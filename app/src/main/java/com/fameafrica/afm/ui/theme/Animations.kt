package com.fameafrica.afm.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush

/**
 * FAME Africa™ Motion Design
 * Subtle animations inspired by African patterns
 */
object FameAnimations {

    // Standard durations
    val fast = 200
    val normal = 300
    val slow = 500

    // Easing curves
    val authorityEasing = FastOutSlowInEasing
    val goldShimmerEasing = EaseInOutCubic

    // Kente-inspired geometric transition
    val kenteTransition = tween<Float>(
        durationMillis = normal,
        easing = LinearEasing
    )

    // Gold shimmer for Legend status
    //val goldShimmer = InfiniteTransition(tween(1200, easing = EaseInOutSine))

    // Tribal line sweep
    val lineSweep = tween<Float>(
        durationMillis = slow,
        easing = EaseInOutQuart
    )
}

/**
 * Gold shimmer effect for African Legend reputation
 */
@Composable
fun goldShimmerBrush(targetValue: Float): Brush {
    val infiniteTransition = rememberInfiniteTransition()
    val progress = infiniteTransition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    return Brush.linearGradient(
        colors = listOf(
            FameColors.ChampionsGold.copy(alpha = 0.1f),
            FameColors.ChampionsGold.copy(alpha = 0.3f),
            FameColors.ChampionsGold.copy(alpha = 0.1f)
        ),
        start = Offset(progress.value * 100f, 0f),
        end = Offset(progress.value * 100f + 100f, 100f)
    )
}

/**
 * Adinkra-inspired fade transition
 */
val adinkraFade = tween<Float>(
    durationMillis = 400,
    easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
)