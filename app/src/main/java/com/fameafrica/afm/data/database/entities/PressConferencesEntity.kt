package com.fameafrica.afm.data.database.entities

import androidx.room.*

@Entity(
    tableName = "press_conferences",
    foreignKeys = [
        ForeignKey(
            entity = ManagersEntity::class,
            parentColumns = ["id"],
            childColumns = ["manager_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = JournalistsEntity::class,
            parentColumns = ["id"],
            childColumns = ["journalist_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["manager_id"]),
        Index(value = ["journalist_id"]),
        Index(value = ["journalist_name"]),
        Index(value = ["question_category"]),
        Index(value = ["timestamp"])
    ]
)
data class PressConferencesEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "manager_id")
    val managerId: Int,

    @ColumnInfo(name = "journalist_id")
    val journalistId: Int,

    @ColumnInfo(name = "journalist_name")
    val journalistName: String,

    @ColumnInfo(name = "journalist_personality")
    val journalistPersonality: String,

    @ColumnInfo(name = "question_category")
    val questionCategory: String,  // MATCH_PERFORMANCE, TRANSFER_RUMORS, PLAYER_FORM, TACTICS, BOARD, FANS, RIVALS

    @ColumnInfo(name = "question")
    val question: String,

    @ColumnInfo(name = "option_a")
    val optionA: String,  // First response option

    @ColumnInfo(name = "option_b")
    val optionB: String,  // Second response option

    @ColumnInfo(name = "option_c")
    val optionC: String,  // Third response option

    @ColumnInfo(name = "response_type_a")
    val responseTypeA: String,  // POSITIVE, NEUTRAL, NEGATIVE

    @ColumnInfo(name = "response_type_b")
    val responseTypeB: String,

    @ColumnInfo(name = "response_type_c")
    val responseTypeC: String,

    @ColumnInfo(name = "selected_response")
    val selectedResponse: String? = null,

    @ColumnInfo(name = "response_text")
    val responseText: String? = null,

    @ColumnInfo(name = "impact_on_team", defaultValue = "0")
    val impactOnTeam: Int = 0,  // -10 to +10

    @ColumnInfo(name = "reputation_change")
    val reputationChange: Int = 0,  // -5 to +5

    @ColumnInfo(name = "timestamp")
    val timestamp: String,

    @ColumnInfo(name = "is_published")
    val isPublished: Boolean = false
) {

    // ============ COMPUTED PROPERTIES ============

    @get:Ignore
    val impactColor: String
        get() = when {
            impactOnTeam > 0 -> "Positive"
            impactOnTeam < 0 -> "Negative"
            else -> "Neutral"
        }

    @get:Ignore
    val reputationColor: String
        get() = when {
            reputationChange > 0 -> "Positive"
            reputationChange < 0 -> "Negative"
            else -> "Neutral"
        }

    @get:Ignore
    val selectedResponseType: String?
        get() = when (selectedResponse) {
            optionA -> responseTypeA
            optionB -> responseTypeB
            optionC -> responseTypeC
            else -> null
        }
}

// ============ ENUMS ============

enum class QuestionCategory(val value: String) {
    MATCH_PERFORMANCE("Match Performance"),
    TRANSFER_RUMORS("Transfer Rumors"),
    PLAYER_FORM("Player Form"),
    TACTICS("Tactics"),
    BOARD("Board Relations"),
    FANS("Fan Relations"),
    RIVALS("Rivalry"),
    INJURY("Injury Update"),
    CONTRACT("Contract Situation"),
    FUTURE("Future Plans"),
    GENERAL("General"),
    TEAM_MORALE("Team Morale")
}

enum class ResponseType(val value: String) {
    POSITIVE("POSITIVE"),
    NEUTRAL("NEUTRAL"),
    NEGATIVE("NEGATIVE")
}
