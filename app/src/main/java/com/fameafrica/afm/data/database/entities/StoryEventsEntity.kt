package com.fameafrica.afm.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "story_events")
data class StoryEventsEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String,
    val category: String, // SCANDAL, TAKEOVER, SQUAD_UNREST, FAN_PROTEST, SPONSOR_BONUS
    val impactType: String, // REPUTATION, FINANCES, MORALE, STATUS
    val impactValue: Int,
    val teamId: Int? = null,
    val playerId: Int? = null,
    val isGlobal: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val isResolved: Boolean = false
)
