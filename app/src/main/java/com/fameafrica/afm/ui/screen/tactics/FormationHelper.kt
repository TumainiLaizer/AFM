package com.fameafrica.afm.ui.screen.tactics

/**
 * Helper file for formation positions and labels.
 * Provides normalized coordinates (x, y) where 0.0 is top/left and 1.0 is bottom/right.
 * ORIENTATION: VERTICAL (Goal at top, Goal at bottom)
 */

fun getPositionsForFormation(formation: String): List<Pair<Float, Float>> {
    return when (formation) {
        "4-3-3", "4-3-3 Flat" -> listOf(
            0.50f to 0.92f, // GK (Bottom)
            0.15f to 0.75f, // LB
            0.38f to 0.78f, // CB
            0.62f to 0.78f, // CB
            0.85f to 0.75f, // RB
            0.25f to 0.55f, // CM
            0.50f to 0.58f, // CM
            0.75f to 0.55f, // CM
            0.20f to 0.28f, // LW
            0.80f to 0.28f, // RW
            0.50f to 0.18f  // ST (Top)
        )
        "4-3-3 Defensive" -> listOf(
            0.50f to 0.92f, // GK
            0.15f to 0.75f, // LB
            0.38f to 0.78f, // CB
            0.62f to 0.78f, // CB
            0.85f to 0.75f, // RB
            0.30f to 0.52f, // CM
            0.50f to 0.65f, // CDM
            0.70f to 0.52f, // CM
            0.20f to 0.28f, // LW
            0.80f to 0.28f, // RW
            0.50f to 0.18f  // ST
        )
        "4-3-3 Attacking" -> listOf(
            0.50f to 0.92f, // GK
            0.15f to 0.75f, // LB
            0.38f to 0.78f, // CB
            0.62f to 0.78f, // CB
            0.85f to 0.75f, // RB
            0.30f to 0.58f, // CM
            0.50f to 0.42f, // CAM
            0.70f to 0.58f, // CM
            0.20f to 0.28f, // LW
            0.80f to 0.28f, // RW
            0.50f to 0.18f  // ST
        )
        "4-4-2" -> listOf(
            0.50f to 0.92f, // GK
            0.15f to 0.75f, // LB
            0.38f to 0.78f, // CB
            0.62f to 0.78f, // CB
            0.85f to 0.75f, // RB
            0.15f to 0.50f, // LM
            0.40f to 0.52f, // CM
            0.60f to 0.52f, // CM
            0.85f to 0.50f, // RM
            0.38f to 0.22f, // ST
            0.62f to 0.22f  // ST
        )
        "4-4-1-1" -> listOf(
            0.50f to 0.92f, // GK
            0.15f to 0.75f, // LB
            0.38f to 0.78f, // CB
            0.62f to 0.78f, // CB
            0.85f to 0.75f, // RB
            0.15f to 0.50f, // LM
            0.40f to 0.52f, // CM
            0.60f to 0.52f, // CM
            0.85f to 0.50f, // RM
            0.50f to 0.32f, // CF
            0.50f to 0.18f  // ST
        )
        "4-4-2 Diamond" -> listOf(
            0.50f to 0.92f, // GK
            0.15f to 0.75f, // LB
            0.38f to 0.78f, // CB
            0.62f to 0.78f, // CB
            0.85f to 0.75f, // RB
            0.50f to 0.62f, // CDM
            0.25f to 0.50f, // LM
            0.75f to 0.50f, // RM
            0.50f to 0.38f, // CAM
            0.38f to 0.20f, // ST
            0.62f to 0.20f  // ST
        )
        "4-2-3-1" -> listOf(
            0.50f to 0.92f, // GK
            0.15f to 0.75f, // LB
            0.38f to 0.78f, // CB
            0.62f to 0.78f, // CB
            0.85f to 0.75f, // RB
            0.38f to 0.62f, // CDM
            0.62f to 0.62f, // CDM
            0.20f to 0.38f, // LM
            0.50f to 0.38f, // CAM
            0.80f to 0.38f, // RM
            0.50f to 0.18f  // ST
        )
        "4-3-2-1" -> listOf(
            0.50f to 0.92f, // GK
            0.15f to 0.75f, // LB
            0.38f to 0.78f, // CB
            0.62f to 0.78f, // CB
            0.85f to 0.75f, // RB
            0.28f to 0.58f, // CM
            0.50f to 0.60f, // CM
            0.72f to 0.58f, // CM
            0.38f to 0.35f, // CAM
            0.62f to 0.35f, // CAM
            0.50f to 0.18f  // ST
        )
        "4-1-4-1" -> listOf(
            0.50f to 0.92f, // GK
            0.15f to 0.75f, // LB
            0.38f to 0.78f, // CB
            0.62f to 0.78f, // CB
            0.85f to 0.75f, // RB
            0.50f to 0.65f, // CDM
            0.18f to 0.45f, // LM
            0.38f to 0.45f, // CM
            0.62f to 0.45f, // CM
            0.82f to 0.45f, // RM
            0.50f to 0.22f  // ST
        )
        "4-1-2-1-2" -> listOf(
            0.50f to 0.92f, // GK
            0.15f to 0.75f, // LB
            0.38f to 0.78f, // CB
            0.62f to 0.78f, // CB
            0.85f to 0.75f, // RB
            0.50f to 0.65f, // CDM
            0.32f to 0.52f, // CM
            0.68f to 0.52f, // CM
            0.50f to 0.38f, // CAM
            0.40f to 0.20f, // ST
            0.60f to 0.20f  // ST
        )
        "4-2-4" -> listOf(
            0.50f to 0.92f, // GK
            0.15f to 0.75f, // LB
            0.38f to 0.78f, // CB
            0.62f to 0.78f, // CB
            0.85f to 0.75f, // RB
            0.40f to 0.55f, // CM
            0.60f to 0.55f, // CM
            0.18f to 0.28f, // LW
            0.40f to 0.20f, // ST
            0.60f to 0.20f, // ST
            0.82f to 0.28f  // RW
        )
        "4-5-1" -> listOf(
            0.50f to 0.92f, // GK
            0.15f to 0.75f, // LB
            0.38f to 0.78f, // CB
            0.62f to 0.78f, // CB
            0.85f to 0.75f, // RB
            0.18f to 0.52f, // LM
            0.38f to 0.52f, // CM
            0.50f to 0.62f, // CDM
            0.62f to 0.52f, // CM
            0.82f to 0.52f, // RM
            0.50f to 0.22f  // ST
        )
        "3-5-2" -> listOf(
            0.50f to 0.92f, // GK
            0.25f to 0.78f, // CB
            0.50f to 0.80f, // CB
            0.75f to 0.78f, // CB
            0.15f to 0.52f, // LWB
            0.38f to 0.55f, // CM
            0.50f to 0.65f, // CDM
            0.62f to 0.55f, // CM
            0.85f to 0.52f, // RWB
            0.40f to 0.22f, // ST
            0.60f to 0.22f  // ST
        )
        "3-4-3" -> listOf(
            0.50f to 0.92f, // GK
            0.25f to 0.78f, // CB
            0.50f to 0.80f, // CB
            0.75f to 0.78f, // CB
            0.18f to 0.52f, // LM
            0.40f to 0.55f, // CM
            0.60f to 0.55f, // CM
            0.82f to 0.52f, // RM
            0.20f to 0.28f, // LW
            0.50f to 0.18f, // ST
            0.80f to 0.28f  // RW
        )
        "3-4-1-2" -> listOf(
            0.50f to 0.92f, // GK
            0.25f to 0.78f, // CB
            0.50f to 0.80f, // CB
            0.75f to 0.78f, // CB
            0.18f to 0.52f, // LM
            0.40f to 0.55f, // CM
            0.60f to 0.55f, // CM
            0.82f to 0.52f, // RM
            0.50f to 0.38f, // CAM
            0.40f to 0.20f, // ST
            0.60f to 0.20f  // ST
        )
        "3-4-2-1" -> listOf(
            0.50f to 0.92f, // GK
            0.25f to 0.78f, // CB
            0.50f to 0.80f, // CB
            0.75f to 0.78f, // CB
            0.18f to 0.52f, // LM
            0.40f to 0.55f, // CM
            0.60f to 0.55f, // CM
            0.82f to 0.52f, // RM
            0.38f to 0.35f, // CAM
            0.62f to 0.35f, // CAM
            0.50f to 0.18f  // ST
        )
        "5-3-2" -> listOf(
            0.50f to 0.92f, // GK
            0.12f to 0.68f, // LWB
            0.32f to 0.78f, // CB
            0.50f to 0.80f, // CB
            0.68f to 0.78f, // CB
            0.88f to 0.68f, // RWB
            0.35f to 0.52f, // CM
            0.50f to 0.62f, // CDM
            0.65f to 0.52f, // CM
            0.40f to 0.22f, // ST
            0.60f to 0.22f  // ST
        )
        "5-4-1" -> listOf(
            0.50f to 0.92f, // GK
            0.12f to 0.68f, // LWB
            0.32f to 0.78f, // CB
            0.50f to 0.80f, // CB
            0.68f to 0.78f, // CB
            0.88f to 0.68f, // RWB
            0.20f to 0.48f, // LM
            0.40f to 0.50f, // CM
            0.60f to 0.50f, // CM
            0.80f to 0.48f, // RM
            0.50f to 0.22f  // ST
        )
        "5-2-1-2" -> listOf(
            0.50f to 0.92f, // GK
            0.12f to 0.68f, // LWB
            0.32f to 0.78f, // CB
            0.50f to 0.80f, // CB
            0.68f to 0.78f, // CB
            0.88f to 0.68f, // RWB
            0.40f to 0.55f, // CM
            0.60f to 0.55f, // CM
            0.50f to 0.40f, // CAM
            0.40f to 0.20f, // ST
            0.60f to 0.20f  // ST
        )
        "4-2-2-2" -> listOf(
            0.50f to 0.92f, // GK
            0.15f to 0.75f, // LB
            0.38f to 0.78f, // CB
            0.62f to 0.78f, // CB
            0.85f to 0.75f, // RB
            0.38f to 0.62f, // CDM
            0.62f to 0.62f, // CDM
            0.35f to 0.42f, // CAM
            0.65f to 0.42f, // CAM
            0.40f to 0.20f, // ST
            0.60f to 0.20f  // ST
        )
        "4-3-1-2" -> listOf(
            0.50f to 0.92f, // GK
            0.15f to 0.75f, // LB
            0.38f to 0.78f, // CB
            0.62f to 0.78f, // CB
            0.85f to 0.75f, // RB
            0.28f to 0.58f, // CM
            0.50f to 0.65f, // CDM
            0.72f to 0.58f, // CM
            0.50f to 0.42f, // CAM
            0.40f to 0.20f, // ST
            0.60f to 0.20f  // ST
        )
        else -> listOf(
            0.50f to 0.92f, // GK
            0.15f to 0.75f, // LB
            0.38f to 0.78f, // CB
            0.62f to 0.78f, // CB
            0.85f to 0.75f, // RB
            0.15f to 0.50f, // LM
            0.40f to 0.52f, // CM
            0.60f to 0.52f, // CM
            0.85f to 0.50f, // RM
            0.40f to 0.25f, // ST
            0.60f to 0.25f  // ST
        )
    }
}

fun getPositionLabelsForFormation(formation: String): List<String> {
    return when (formation) {
        "4-3-3", "4-3-3 Flat" -> listOf("GK", "LB", "CB", "CB", "RB", "CM", "CM", "CM", "LW", "RW", "ST")
        "4-3-3 Defensive" -> listOf("GK", "LB", "CB", "CB", "RB", "CM", "CDM", "CM", "LW", "RW", "ST")
        "4-3-3 Attacking" -> listOf("GK", "LB", "CB", "CB", "RB", "CM", "CAM", "CM", "LW", "RW", "ST")
        "4-4-2" -> listOf("GK", "LB", "CB", "CB", "RB", "LM", "CM", "CM", "RM", "ST", "ST")
        "4-4-1-1" -> listOf("GK", "LB", "CB", "CB", "RB", "LM", "CM", "CM", "RM", "CF", "ST")
        "4-4-2 Diamond" -> listOf("GK", "LB", "CB", "CB", "RB", "CDM", "LM", "RM", "CAM", "ST", "ST")
        "4-2-3-1" -> listOf("GK", "LB", "CB", "CB", "RB", "CDM", "CDM", "LM", "CAM", "RM", "ST")
        "4-3-2-1" -> listOf("GK", "LB", "CB", "CB", "RB", "CM", "CM", "CM", "CAM", "CAM", "ST")
        "4-1-4-1" -> listOf("GK", "LB", "CB", "CB", "RB", "CDM", "LM", "CM", "CM", "RM", "ST")
        "4-1-2-1-2" -> listOf("GK", "LB", "CB", "CB", "RB", "CDM", "CM", "CM", "CAM", "ST", "ST")
        "4-2-4" -> listOf("GK", "LB", "CB", "CB", "RB", "CM", "CM", "LW", "ST", "ST", "RW")
        "4-5-1" -> listOf("GK", "LB", "CB", "CB", "RB", "LM", "CM", "CDM", "CM", "RM", "ST")
        "3-5-2" -> listOf("GK", "CB", "CB", "CB", "LWB", "CM", "CDM", "CM", "RWB", "ST", "ST")
        "3-4-3" -> listOf("GK", "CB", "CB", "CB", "LM", "CM", "CM", "RM", "LW", "ST", "RW")
        "3-4-1-2" -> listOf("GK", "CB", "CB", "CB", "LM", "CM", "CM", "RM", "CAM", "ST", "ST")
        "3-4-2-1" -> listOf("GK", "CB", "CB", "CB", "LM", "CM", "CM", "RM", "CAM", "CAM", "ST")
        "5-3-2" -> listOf("GK", "LWB", "CB", "CB", "CB", "RWB", "CM", "CDM", "CM", "ST", "ST")
        "5-4-1" -> listOf("GK", "LWB", "CB", "CB", "CB", "RWB", "LM", "CM", "CM", "RM", "ST")
        "5-2-1-2" -> listOf("GK", "LWB", "CB", "CB", "CB", "RWB", "CM", "CM", "CAM", "ST", "ST")
        "4-2-2-2" -> listOf("GK", "LB", "CB", "CB", "RB", "CDM", "CDM", "CAM", "CAM", "ST", "ST")
        "4-3-1-2" -> listOf("GK", "LB", "CB", "CB", "RB", "CM", "CDM", "CM", "CAM", "ST", "ST")
        else -> listOf("GK", "LB", "CB", "CB", "RB", "LM", "CM", "CM", "RM", "ST", "ST")
    }
}
