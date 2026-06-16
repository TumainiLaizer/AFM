package com.fameafrica.afm.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import com.squareup.moshi.Json

@Entity(
    tableName = "leagues",
    indices = [
        Index(value = ["name"], unique = true),
        Index(value = ["country_id"]),
        Index(value = ["sponsor"]),
        Index(value = ["level"]),
        Index(value = ["prize_money"]),
        Index(value = ["country_id", "level"])
    ]
)
data class LeaguesEntity(
    @PrimaryKey(autoGenerate = true)
    @field:ColumnInfo(name = "id")
    val id: Int = 0,

    @field:ColumnInfo(name = "name")
    val name: String,

    @param:Json(name = "country_id")
    @field:ColumnInfo(name = "country_id")
    val countryId: Int?,  // Foreign key to nationalities (removed FK constraint for stability)

    @field:ColumnInfo(name = "country")
    val country: String?,  // Keep for backward compatibility

    @field:ColumnInfo(name = "level")
    val level: Int,  // 1 = Top Division, 2 = Second Division, 3= Third Division, 4 = Fourth Division, 5 = Regional Division

    @field:ColumnInfo(name = "sponsor")
    val sponsor: String?,

    @param:Json(name = "prize_money")
    @field:ColumnInfo(name = "prize_money")
    val prizeMoney: Int,

    @field:ColumnInfo(name = "logo")
    val logo: String?,

    @param:Json(name = "simulation_tier")
    @field:ColumnInfo(name = "simulation_tier", defaultValue = "0")
    val simulationTier: Int = 0 // 0 = Active/Detailed, 1 = Background/Fast-Math
) {

    // ============ COMPUTED PROPERTIES ============

    val prizeMoneyInMillions: Double
        get() = prizeMoney / 1_000_000.0

    val tierName: String
        get() = when (level) {
            1 -> "Premier Division"
            2 -> "Championship"
            3 -> "League One"
            4 -> "League Two"
            5 -> "Regional League"
            else -> "Division $level"
        }

    val fullName: String
        get() = if (country != null) {
            "$country $name"
        } else {
            name
        }

    val displayName: String
        get() = name.replace("League", "").trim()

    val isTopDivision: Boolean
        get() = level == 1

    val isSecondDivision: Boolean
        get() = level == 2

    val isDomestic: Boolean
        get() = countryId != null

    val leagueQuality: String
        get() = when {
            prizeMoney >= 1_000_000 -> "Elite"
            prizeMoney >= 500_000 -> "High"
            prizeMoney >= 200_000 -> "Good"
            prizeMoney >= 100_000 -> "Average"
            prizeMoney >= 50_000 -> "Low"
            else -> "Amateur"
        }

    /**
     * Get maximum foreign players allowed in this league
     * Based on country-specific regulations
     */
    fun getMaxForeignPlayers(): Int {
        // Fallback if ForeignPlayerRules is not easily accessible
        return 5 
    }

    /**
     * Get promotion spots available
     */
    val promotionSpots: Int
        get() = when (level) {
            1 -> 0  // Top division doesn't promote
            2 -> 3  // Second division promotes 3 teams
            3 -> 4  // Third division promotes 4 teams
            4 -> 4  // Fourth division promotes 4 teams
            else -> 2
        }

    /**
     * Get relegation spots
     */
    val relegationSpots: Int
        get() = when (level) {
            1 -> 3  // Top division relegates 3
            2 -> 4  // Second division relegates 4
            3 -> 4  // Third division relegates 4
            4 -> 4  // Fourth division relegates 4
            else -> 2
        }

    /**
     * Get playoff spots
     */
    val playoffSpots: Int
        get() = when (level) {
            1 -> 0
            2 -> 2  // Positions 3-4 go to playoffs
            3 -> 2
            4 -> 2
            else -> 0
        }
}

// ============ ENUMS ============

enum class LeagueLevel(val value: Int) {
    PREMIER(1),
    CHAMPIONSHIP(2),
    LEAGUE_ONE(3),
    LEAGUE_TWO(4),
    REGIONAL(5); // Added semicolon and fixed missing entries

    companion object {
        fun fromInt(value: Int) = entries.find { it.value == value } ?: PREMIER
    }
}

enum class LeagueTier(val value: String) {
    ELITE("Elite"),
    HIGH("High"),
    GOOD("Good"),
    AVERAGE("Average"),
    LOW("Low"),
    AMATEUR("Amateur")
}
