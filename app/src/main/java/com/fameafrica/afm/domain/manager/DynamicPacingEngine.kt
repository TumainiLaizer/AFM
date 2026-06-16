package com.fameafrica.afm.domain.manager

import com.fameafrica.afm.domain.model.match.MatchIntensity
import com.fameafrica.afm.domain.model.match.MatchSpeed
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DynamicPacingEngine @Inject constructor() {

    fun calculateDelay(
        intensity: MatchIntensity,
        speed: MatchSpeed,
        minute: Int,
        scoreDifference: Int,
        isDerby: Boolean = false,
        isFinal: Boolean = false
    ): Long {
        var baseDelay = when (intensity) {
            MatchIntensity.LOW -> 1500L
            MatchIntensity.BUILD_UP -> 1200L
            MatchIntensity.BIG_CHANCE -> 800L
            MatchIntensity.GOAL -> 3000L
            MatchIntensity.CARD -> 2000L
            MatchIntensity.VAR -> 3500L
            MatchIntensity.DRAMA -> 2500L
            MatchIntensity.FINAL_MOMENTS -> 1000L
        }

        // Contextual modifiers
        if (isDerby) baseDelay = (baseDelay * 1.2).toLong()
        if (isFinal) baseDelay = (baseDelay * 1.3).toLong()

        // Tension modifier: close game in final minutes
        if (minute >= 85 && scoreDifference <= 1) {
            baseDelay = (baseDelay * 1.25).toLong()
        }

        return when (speed) {
            MatchSpeed.INSTANT -> 0L
            MatchSpeed.FAST -> (baseDelay * 0.4).toLong()
            MatchSpeed.NORMAL -> baseDelay
            MatchSpeed.EXTENDED -> (baseDelay * 1.8).toLong()
        }
    }
}
