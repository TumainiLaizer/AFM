package com.fameafrica.afm.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import com.squareup.moshi.Json

@Entity(
    tableName = "referees",
    foreignKeys = [
        ForeignKey(
            entity = NationalitiesEntity::class,
            parentColumns = ["id"],
            childColumns = ["nationality_id"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["name"]),
        Index(value = ["nationality_id"]),
        Index(value = ["rating"]),
        Index(value = ["strictness", "bias"])
    ]
)
data class RefereesEntity(
    @Json(name = "referee_id")
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "referee_id")
    val refereeId: Int = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "strictness")
    val strictness: Int,

    @ColumnInfo(name = "bias")
    val bias: Int,

    @Json(name = "nationality_id")
    @ColumnInfo(name = "nationality_id")
    val nationalityId: Int,

    @ColumnInfo(name = "rating", defaultValue = "50")
    val rating: Int = 50,

    @Json(name = "matches_officiated")
    @ColumnInfo(name = "matches_officiated", defaultValue = "0")
    val matchesOfficiated: Int = 0,

    @Json(name = "yellow_cards")
    @ColumnInfo(name = "yellow_cards", defaultValue = "0")
    val yellowCards: Int = 0,

    @Json(name = "red_cards")
    @ColumnInfo(name = "red_cards", defaultValue = "0")
    val redCards: Int = 0
) {
    // Tier calculation remains the same
    val refereeTier: String
        get() = when {
            bias in 0..9 -> "FIFA/AFCON Elite"
            bias in 10..19 -> "Continental"
            bias in 20..39 -> "Top League"
            bias in 40..59 -> "Average"
            else -> "Local"
        }

    val expectedRatingRange: Pair<Int, Int>
        get() = when {
            bias in 0..9 -> 90 to 95
            bias in 10..19 -> 82 to 88
            bias in 20..39 -> 75 to 81
            bias in 40..59 -> 68 to 74
            else -> 60 to 67
        }

    /**
     * CHECK 1: For NATIONAL TEAM matches (International, AFCON, World Cup, etc.)
     * Referee MUST be neutral - cannot share nationality with either team
     */
    fun isNeutralForNationalMatch(homeNationId: Int, awayNationId: Int): Boolean {
        return nationalityId != homeNationId && nationalityId != awayNationId
    }

    /**
     * CHECK 2: For LEAGUE matches (Domestic leagues)
     * Referee SHOULD be from the same country as the league
     * This is normal and expected - local referees officiate local leagues
     */
    fun isEligibleForLeague(leagueCountryNationalityId: Int): Boolean {
        return nationalityId == leagueCountryNationalityId
    }

    /**
     * CHECK 3: For DOMESTIC CUP matches (FA Cup, Mapinduzi Cup, etc.)
     * Referee SHOULD be from the same country as the cup's host nation
     * Domestic cups use local referees
     */
    fun isEligibleForDomesticCup(cupCountryNationalityId: Int): Boolean {
        return nationalityId == cupCountryNationalityId
    }

    /**
     * CHECK 4: For CONTINENTAL COMPETITIONS (CAF Champions League, CAF Confederation Cup)
     * Referee MUST be neutral - from a DIFFERENT African nation than both teams
     */
    fun isEligibleForContinentalCompetition(homeNationId: Int, awayNationId: Int): Boolean {
        return nationalityId != homeNationId && nationalityId != awayNationId
    }

    /**
     * CHECK 5: For FRIENDLY MATCHES
     * - Club friendlies: Can be any referee (prefer local)
     * - International friendlies: Preferably neutral, but not strictly required
     */
    fun isEligibleForFriendly(isInternational: Boolean, leagueCountryId: Int? = null): Boolean {
        return if (isInternational) {
            // International friendly - prefer neutral but not mandatory
            true  // Any referee can be assigned, we'll sort by preference later
        } else {
            // Club friendly - use local referees
            leagueCountryId == nationalityId
        }
    }

    /**
     * Universal eligibility checker based on match context
     */
    fun isEligibleForMatch(
        matchType: String,
        homeNationId: Int? = null,
        awayNationId: Int? = null,
        leagueCountryId: Int? = null,
        cupCountryId: Int? = null
    ): Boolean {
        return when (matchType.uppercase()) {
            "NATIONAL_TEAM", "AFCON", "WORLD_CUP", "WORLD_CUP_QUALIFIER", "AFCON_QUALIFIER" -> {
                // International matches require neutrality
                homeNationId != null && awayNationId != null &&
                        isNeutralForNationalMatch(homeNationId, awayNationId)
            }
            "LEAGUE" -> {
                // League matches use referees from that country
                leagueCountryId != null && isEligibleForLeague(leagueCountryId)
            }
            "CUP", "DOMESTIC_CUP" -> {
                // Domestic cups use referees from host country
                cupCountryId != null && isEligibleForDomesticCup(cupCountryId)
            }
            "CONTINENTAL", "CAF_CHAMPIONS_LEAGUE", "CAF_CONFEDERATION_CUP", "CAF_SUPER_CUP" -> {
                // CAF competitions require neutral referees from different African nations
                homeNationId != null && awayNationId != null &&
                        isEligibleForContinentalCompetition(homeNationId, awayNationId)
            }
            "FRIENDLY" -> {
                // Friendly matches - depends on context
                val isInternational = homeNationId != null && awayNationId != null
                isEligibleForFriendly(isInternational, leagueCountryId)
            }
            else -> true // Default - allow
        }
    }

    // Update stats method remains the same
    fun updateAfterMatch(yellow: Int, red: Int): RefereesEntity {
        return this.copy(
            matchesOfficiated = matchesOfficiated + 1,
            yellowCards = yellowCards + yellow,
            redCards = redCards + red
        )
    }
}