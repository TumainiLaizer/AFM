package com.fameafrica.afm.domain.model.match

import com.fameafrica.afm.data.database.entities.MatchCommentaryEntity
import com.fameafrica.afm.data.database.entities.MatchEventsEntity

sealed class MatchUpdate {
    data class MinuteUpdate(val minute: Int) : MatchUpdate()
    data class EventUpdate(
        val event: MatchEventsEntity,
        val commentary: MatchCommentaryEntity? = null,
        val intensity: MatchIntensity = MatchIntensity.LOW
    ) : MatchUpdate()
    data class ScoreUpdate(val homeScore: Int, val awayScore: Int) : MatchUpdate()
    object HalfTime : MatchUpdate()
    object FullTime : MatchUpdate()
}

enum class MatchIntensity {
    LOW,
    BUILD_UP,
    BIG_CHANCE,
    GOAL,
    DRAMA,
    CARD,
    VAR,
    FINAL_MOMENTS
}

enum class MatchVisualizerIntensity {
    LOW,
    BUILD_UP,
    ATTACK,
    BIG_CHANCE,
    GOAL,
    DRAMA
}

enum class MatchSpeed {
    INSTANT,
    FAST,
    NORMAL,
    EXTENDED
}
