package com.fameafrica.afm.domain.manager

import com.fameafrica.afm.data.database.model.match.MatchIntensity
import com.fameafrica.afm.data.database.model.match.MatchSpeed
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DynamicPacingEngine @Inject constructor() {

    fun calculateDelay(
        intensity: com.fameafrica.afm.data.database.model.match.MatchIntensity,
        speed: com.fameafrica.afm.data.database.model.match.MatchSpeed,
        minute: Int,
        scoreDifference: Int,
        isDerby: Boolean = false,
        isFinal: Boolean = false
    ): Long {
        var baseDelay = when (intensity) {
            _root_ide_package_.com.fameafrica.afm.data.database.model.match.MatchIntensity.LOW -> 1500L
            _root_ide_package_.com.fameafrica.afm.data.database.model.match.MatchIntensity.BUILD_UP -> 1200L
            _root_ide_package_.com.fameafrica.afm.data.database.model.match.MatchIntensity.BIG_CHANCE -> 800L
            _root_ide_package_.com.fameafrica.afm.data.database.model.match.MatchIntensity.GOAL -> 3000L
            _root_ide_package_.com.fameafrica.afm.data.database.model.match.MatchIntensity.CARD -> 2000L
            _root_ide_package_.com.fameafrica.afm.data.database.model.match.MatchIntensity.VAR -> 3500L
            _root_ide_package_.com.fameafrica.afm.data.database.model.match.MatchIntensity.DRAMA -> 2500L
            _root_ide_package_.com.fameafrica.afm.data.database.model.match.MatchIntensity.FINAL_MOMENTS -> 1000L
        }

        // Contextual modifiers
        if (isDerby) baseDelay = (baseDelay * 1.2).toLong()
        if (isFinal) baseDelay = (baseDelay * 1.3).toLong()

        // Tension modifier: close game in final minutes
        if (minute >= 85 && scoreDifference <= 1) {
            baseDelay = (baseDelay * 1.25).toLong()
        }

        return when (speed) {
            _root_ide_package_.com.fameafrica.afm.data.database.model.match.MatchSpeed.INSTANT -> 0L
            _root_ide_package_.com.fameafrica.afm.data.database.model.match.MatchSpeed.FAST -> (baseDelay * 0.4).toLong()
            _root_ide_package_.com.fameafrica.afm.data.database.model.match.MatchSpeed.NORMAL -> baseDelay
            _root_ide_package_.com.fameafrica.afm.data.database.model.match.MatchSpeed.EXTENDED -> (baseDelay * 1.8).toLong()
        }
    }
}
