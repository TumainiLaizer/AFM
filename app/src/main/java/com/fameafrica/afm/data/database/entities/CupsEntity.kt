package com.fameafrica.afm.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import com.squareup.moshi.Json

@Entity(
    tableName = "cups",
    indices = [
        Index(value = ["name"], unique = true),
        Index(value = ["country_id"]),
        Index(value = ["sponsor"]),
        Index(value = ["type"])
    ]
)
data class CupsEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "type")
    val type: String?,

    @Json(name = "country_id")
    @ColumnInfo(name = "country_id")
    val countryId: Int?,  // Foreign key to nationalities (removed FK constraint for stability)

    @ColumnInfo(name = "country")
    val country: String?,  // Keep for backward compatibility

    @ColumnInfo(name = "sponsor")
    val sponsor: String?,

    @Json(name = "prize_money")
    @ColumnInfo(name = "prize_money")
    val prizeMoney: Int,

    @Json(name = "teams_involved")
    @ColumnInfo(name = "teams_involved")
    val teamsInvolved: Int,

    @ColumnInfo(name = "rules")
    val rules: String?,

    @ColumnInfo(name = "logo")
    val logo: String?
) {
    /**
     * Check if this is a domestic cup (has country_id)
     */
    val isDomesticCup: Boolean
        get() = countryId != null

    /**
     * Check if this is a continental cup (no country_id)
     */
    val isContinentalCup: Boolean
        get() = countryId == null

    /**
     * Check if referee is eligible for this cup
     * - Domestic cups: MUST use referees from SAME country
     * - Continental cups: MUST use NEUTRAL referees (different from both teams)
     */
    fun isRefereeEligible(referee: RefereesEntity, homeNationId: Int? = null, awayNationId: Int? = null): Boolean {
        return if (isDomesticCup) {
            // Domestic cup - referee must be from host country
            countryId != null && referee.nationalityId == countryId
        } else {
            // Continental cup - referee must be neutral
            homeNationId != null && awayNationId != null &&
                    referee.nationalityId != homeNationId &&
                    referee.nationalityId != awayNationId
        }
    }

    /**
     * Get display country name
     */
    fun getDisplayCountry(): String = when {
        country != null -> country
        isDomesticCup -> "Domestic Cup"
        else -> "Continental"
    }

    /**
     * Get cup tier based on prize money and teams involved
     */
    val cupTier: String
        get() = when {
            prizeMoney >= 5_000_000 -> "Elite"
            prizeMoney >= 1_000_000 -> "Premium"
            prizeMoney >= 500_000 -> "Standard"
            else -> "Basic"
        }
}
