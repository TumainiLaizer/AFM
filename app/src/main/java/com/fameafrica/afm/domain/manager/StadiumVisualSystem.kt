package com.fameafrica.afm.domain.manager

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StadiumVisualSystem @Inject constructor() {

    /**
     * Determines the visual asset key for a stadium based on its capacity.
     * This key is used by the UI to select the correct illustration/sprite.
     */
    fun getStadiumAssetKey(capacity: Int): String {
        return when {
            capacity < 500 -> "pitch_dirt"
            capacity < 2000 -> "pitch_basic"
            capacity < 5000 -> "stadium_small"
            capacity < 15000 -> "stadium_medium"
            capacity < 40000 -> "stadium_large"
            else -> "stadium_continental"
        }
    }

    /**
     * Returns a human-readable description of the stadium tier.
     */
    fun getStadiumTier(capacity: Int): String {
        return when {
            capacity < 500 -> "Community Pitch"
            capacity < 2000 -> "Local Ground"
            capacity < 5000 -> "Regional Stadium"
            capacity < 15000 -> "Professional Arena"
            capacity < 40000 -> "National Stadium"
            else -> "Elite Continental Hub"
        }
    }
}
