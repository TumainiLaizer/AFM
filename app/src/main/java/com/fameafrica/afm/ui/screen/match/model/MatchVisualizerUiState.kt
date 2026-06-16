package com.fameafrica.afm.ui.screen.match.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import com.fameafrica.afm.data.database.model.match.MatchVisualizerIntensity

@Immutable
data class MatchVisualizerUiState(
    val currentPossessionId: Int = -1,
    val ballPosition: Offset = Offset(0.5f, 0.5f),
    val targetPosition: Offset = Offset(0.5f, 0.5f),
    val intensity: MatchVisualizerIntensity = MatchVisualizerIntensity.LOW,
    val momentumPoints: List<MomentumPoint> = emptyList(),
    val attackPhase: AttackPhase = AttackPhase.NEUTRAL,
    val cameraFocus: Offset = Offset(0.5f, 0.5f),
    val cameraZoom: Float = 1.0f
)

@Immutable
data class MomentumPoint(
    val minute: Int,
    val value: Float // Positive for Home, Negative for Away
)

enum class AttackPhase {
    NEUTRAL,
    BUILD_UP_HOME,
    BUILD_UP_AWAY,
    DANGEROUS_ATTACK_HOME,
    DANGEROUS_ATTACK_AWAY,
    GOAL_HOME,
    GOAL_AWAY
}
