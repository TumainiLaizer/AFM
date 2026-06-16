package com.fameafrica.afm.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json

@Entity(
    tableName = "club_dna",
    foreignKeys = [
        androidx.room.ForeignKey(
            entity = TeamsEntity::class,
            parentColumns = ["id"],
            childColumns = ["teamId"],
            onDelete = androidx.room.ForeignKey.CASCADE
        )
    ]
)
data class ClubDNAEntity(
    @PrimaryKey
    @field:ColumnInfo(name = "teamId")
    val teamId: Int,

    @field:ColumnInfo(name = "region")
    val region: String, // EAST_AFRICA, NORTH_AFRICA, WEST_AFRICA, SOUTHERN_AFRICA, CENTRAL_AFRICA

    // PRIMARY IDENTITY
    @param:Json(name = "play_style")
    @field:ColumnInfo(name = "play_style")
    val playStyle: String, // POSSESSION, COUNTER, DEFENSIVE, GEGENPRESS, DIRECT_PHYSICAL, FLAIR_EXPRESSIVE, WING_PLAY, TRANSITION_HEAVY, TACTICAL_DISCIPLINE, YOUTH_ENERGY, HYBRID_BALANCED

    // SECONDARY STYLE (for hybrid tactics)
    @param:Json(name = "play_style_secondary")
    @field:ColumnInfo(name = "play_style_secondary")
    val playStyleSecondary: String? = null,

    // HOW STRONGLY CLUB STICKS TO ITS DNA (1-100)
    @param:Json(name = "identity_strength")
    @field:ColumnInfo(name = "identity_strength")
    val identityStrength: Int = 50,

    @param:Json(name = "transfer_policy")
    @field:ColumnInfo(name = "transfer_policy")
    val transferPolicy: String, // AGGRESSIVE, SELLING, YOUTH, BALANCED, EXPORT_FOCUSED, FREE_AGENT_FOCUSED, LOCAL_TALENT_ONLY, DIASPORA_SCOUTING, SHORT_TERM_FIXES, AGENT_DRIVEN, LOW_BUDGET_OPPORTUNISTIC, ACADEMY_TO_FIRST_TEAM

    @param:Json(name = "financial_behavior")
    @field:ColumnInfo(name = "financial_behavior")
    val financialBehavior: FinancialBehavior, // Enum: FRUGAL, SPENDER, RISKY, etc.

    @param:Json(name = "youth_priority")
    @field:ColumnInfo(name = "youth_priority")
    val youthPriority: Int // 1-100
) {
    // Helper to calculate tactical weights for the engine
    val primaryWeight: Double
        get() = identityStrength / 100.0

    val secondaryWeight: Double
        get() = 1.0 - primaryWeight

    /**
     * Determines if a manager's style is compatible with the club's DNA.
     * Higher score means better compatibility (0.0 to 1.0)
     */
    fun calculateManagerCompatibility(managerStyle: String): Double {
        return when {
            managerStyle.uppercase() == playStyle.uppercase() -> 1.0
            managerStyle.uppercase() == playStyleSecondary?.uppercase() -> 0.7
            else -> 0.3
        }
    }

    /**
     * Determines if a chairman's leadership style is compatible with the club's DNA.
     */
    fun calculateChairmanCompatibility(chairmanStyle: String): Double {
        return when (chairmanStyle) {
            "Tycoon" -> if (financialBehavior == FinancialBehavior.AGGRESSIVE) 1.0 else 0.5
            "Long-Term Builder" -> if (youthPriority > 70) 1.0 else 0.6
            "Commercial Expert" -> 0.8 // Generally good for any club
            "Technical Owner" -> if (identityStrength > 80) 1.0 else 0.7
            else -> 0.7
        }
    }
}
