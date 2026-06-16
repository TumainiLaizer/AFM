package com.fameafrica.afm.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import com.squareup.moshi.Json
import com.fameafrica.afm.utils.GameDateManager

@Entity(
    tableName = "managers",
    indices = [
        Index(value = ["team_id"]),
        Index(value = ["nationality"]),
        Index(value = ["reputation"]),
        Index(value = ["reputation_level"]),
        Index(value = ["performance_rating"]),
        Index(value = ["age"]),
        Index(value = ["coaching_license"]),
        Index(value = ["man_management"]),
        Index(value = ["attacking_coaching"]),
        Index(value = ["defending_coaching"]),
        Index(value = ["judging_player_ability"])
    ]
)
data class ManagersEntity(
    @PrimaryKey(autoGenerate = true)
    @field:ColumnInfo(name = "id")
    val id: Int = 0,

    @param:Json(name = "team_id")
    @field:ColumnInfo(name = "team_id")
    val teamId: Int? = null,

    @field:ColumnInfo(name = "name", defaultValue = "Tumaini Joseph")
    val name: String = "Tumaini Joseph",

    @field:ColumnInfo(name = "nationality", defaultValue = "Tanzania")
    val nationality: String = "Tanzania",

    @field:ColumnInfo(name = "age", defaultValue = "30")
    val age: Int = 30,

    @param:Json(name = "contract_end_date")
    @field:ColumnInfo(name = "contract_end_date")
    val contractEndDate: Int? = null,

    @field:ColumnInfo(name = "salary")
    val salary: Int? = null,

    @param:Json(name = "matches_managed")
    @field:ColumnInfo(name = "matches_managed", defaultValue = "0")
    val matchesManaged: Int = 0,

    @field:ColumnInfo(name = "wins", defaultValue = "0")
    val wins: Int = 0,

    @field:ColumnInfo(name = "losses", defaultValue = "0")
    val losses: Int = 0,

    @field:ColumnInfo(name = "draws", defaultValue = "0")
    val draws: Int = 0,

    @param:Json(name = "trophies_won")
    @field:ColumnInfo(name = "trophies_won", defaultValue = "0")
    val trophiesWon: Int = 0,

    @param:Json(name = "league_titles")
    @field:ColumnInfo(name = "league_titles", defaultValue = "0")
    val leagueTitles: Int = 0,

    @param:Json(name = "cup_wins")
    @field:ColumnInfo(name = "cup_wins", defaultValue = "0")
    val cupWins: Int = 0,

    @param:Json(name = "promotions")
    @field:ColumnInfo(name = "promotions", defaultValue = "0")
    val promotions: Int = 0,

    @param:Json(name = "relegations")
    @field:ColumnInfo(name = "relegations", defaultValue = "0")
    val relegations: Int = 0,

    @param:Json(name = "clubs_managed")
    @field:ColumnInfo(name = "clubs_managed", defaultValue = "1")
    val clubsManaged: Int = 1,

    @param:Json(name = "preferred_formation")
    @field:ColumnInfo(name = "preferred_formation", defaultValue = "4-4-2")
    val preferredFormation: String = "4-4-2",

    @field:ColumnInfo(name = "style", defaultValue = "Balanced")
    val style: String = "Balanced",

    @param:Json(name = "performance_rating")
    @field:ColumnInfo(name = "performance_rating", defaultValue = "0")
    val performanceRating: Int = 0,

    @field:ColumnInfo(name = "reputation", defaultValue = "50")
    val reputation: Int = 50,

    @param:Json(name = "reputation_level")
    @field:ColumnInfo(name = "reputation_level", defaultValue = "Local")
    val reputationLevel: String = "Local",

    @param:Json(name = "face_image")
    @field:ColumnInfo(name = "face_image")
    val faceImage: String? = null,

    @param:Json(name = "assistant_manager")
    @field:ColumnInfo(name = "assistant_manager")
    val assistantManager: String? = null,

    @field:ColumnInfo(name = "staff")
    val staff: String? = null,

    @param:Json(name = "monthly_awards")
    @field:ColumnInfo(name = "monthly_awards", defaultValue = "0")
    val monthlyAwards: Int = 0,

    @param:Json(name = "yearly_awards")
    @field:ColumnInfo(name = "yearly_awards", defaultValue = "0")
    val yearlyAwards: Int = 0,

    @param:Json(name = "coach_of_the_month_titles")
    @field:ColumnInfo(name = "coach_of_the_month_titles", defaultValue = "0")
    val coachOfTheMonthTitles: Int = 0,

    @param:Json(name = "young_coach_of_the_month_titles")
    @field:ColumnInfo(name = "young_coach_of_the_month_titles", defaultValue = "0")
    val youngCoachOfTheMonthTitles: Int = 0,

    @param:Json(name = "coach_of_the_year_titles")
    @field:ColumnInfo(name = "coach_of_the_year_titles", defaultValue = "0")
    val coachOfTheYearTitles: Int = 0,

    @param:Json(name = "african_coach_of_the_year_titles")
    @field:ColumnInfo(name = "african_coach_of_the_year_titles", defaultValue = "0")
    val africanCoachOfTheYearTitles: Int = 0,

    @param:Json(name = "previous_club")
    @field:ColumnInfo(name = "previous_club")
    val previousClub: String? = null,

    @param:Json(name = "coaching_license")
    @field:ColumnInfo(name = "coaching_license")
    val coachingLicense: String? = null,  // NONE, NATIONAL_C, NATIONAL_B, NATIONAL_A, PRO, UEFA_PRO, CAF_PRO

    @param:Json(name = "special_ability")
    @field:ColumnInfo(name = "special_ability")
    val specialAbility: String? = null,  // TACTICAL_GENIUS, MOTIVATOR, YOUTH_DEVELOPER, DEFENSIVE_SPECIALIST, ATTACKING_SPECIALIST, SET_PIECE_GURU, etc.

    @param:Json(name = "transfer_fee")
    @field:ColumnInfo(name = "transfer_fee")
    val transferFee: Int? = null,  // Compensation fee if under contract

    @param:Json(name = "favorite_tactics")
    @field:ColumnInfo(name = "favorite_tactics")
    val favoriteTactics: String? = null,  // JSON string of preferred tactics

    @param:Json(name = "agent_id")
    @field:ColumnInfo(name = "agent_id")
    val agentId: Int? = null,

    @param:Json(name = "career_vision")
    @field:ColumnInfo(name = "career_vision")
    val careerVision: String? = null,

    @param:Json(name = "personality_profile")
    @field:ColumnInfo(name = "personality_profile")
    val personalityProfile: String? = null, // JSON string

    @param:Json(name = "youth_development_focus")
    @field:ColumnInfo(name = "youth_development_focus")
    val youthDevelopmentFocus: Int? = null,  // 0-100

    @param:Json(name = "media_handling")
    @field:ColumnInfo(name = "media_handling")
    val mediaHandling: Int? = null,  // 0-100

    @param:Json(name = "man_management")
    @field:ColumnInfo(name = "man_management", defaultValue = "50")
    val manManagement: Int = 50,

    @param:Json(name = "tactical_knowledge")
    @field:ColumnInfo(name = "tactical_knowledge", defaultValue = "50")
    val tacticalKnowledge: Int = 50,

    @field:ColumnInfo(name = "determination", defaultValue = "50")
    val determination: Int = 50,

    @param:Json(name = "judging_player_ability")
    @field:ColumnInfo(name = "judging_player_ability", defaultValue = "50")
    val judgingPlayerAbility: Int = 50,

    @param:Json(name = "judging_player_potential")
    @field:ColumnInfo(name = "judging_player_potential", defaultValue = "50")
    val judgingPlayerPotential: Int = 50,

    @param:Json(name = "attacking_coaching")
    @field:ColumnInfo(name = "attacking_coaching", defaultValue = "50")
    val attackingCoaching: Int = 50,

    @param:Json(name = "defending_coaching")
    @field:ColumnInfo(name = "defending_coaching", defaultValue = "50")
    val defendingCoaching: Int = 50,

    @param:Json(name = "fitness_coaching")
    @field:ColumnInfo(name = "fitness_coaching", defaultValue = "50")
    val fitnessCoaching: Int = 50,

    @param:Json(name = "mental_coaching")
    @field:ColumnInfo(name = "mental_coaching", defaultValue = "50")
    val mentalCoaching: Int = 50,

    @param:Json(name = "tactical_coaching")
    @field:ColumnInfo(name = "tactical_coaching", defaultValue = "50")
    val tacticalCoaching: Int = 50,

    @param:Json(name = "technical_coaching")
    @field:ColumnInfo(name = "technical_coaching", defaultValue = "50")
    val technicalCoaching: Int = 50,

    @field:ColumnInfo(name = "ambition", defaultValue = "50")
    val ambition: Int = 50,

    @field:ColumnInfo(name = "loyalty", defaultValue = "50")
    val loyalty: Int = 50,

    @param:Json(name = "pressure_handling")
    @field:ColumnInfo(name = "pressure_handling", defaultValue = "50")
    val pressureHandling: Int = 50,

    @field:ColumnInfo(name = "professionalism", defaultValue = "50")
    val professionalism: Int = 50,

    @param:Json(name = "fan_happiness")
    @field:ColumnInfo(name = "fan_happiness", defaultValue = "50")
    val fanHappiness: Int = 50,

    @param:Json(name = "manager_points")
    @field:ColumnInfo(name = "manager_points", defaultValue = "0")
    val managerPoints: Int = 0,

    @param:Json(name = "manager_level")
    @field:ColumnInfo(name = "manager_level", defaultValue = "1")
    val managerLevel: Int = 1,

    @param:Json(name = "manager_xp")
    @field:ColumnInfo(name = "manager_xp", defaultValue = "0")
    val managerXp: Int = 0,

    @param:Json(name = "tactical_flexibility")
    @field:ColumnInfo(name = "tactical_flexibility")
    val tacticalFlexibility: Int? = null,  // 0-100

    @param:Json(name = "player_motivation")
    @field:ColumnInfo(name = "player_motivation")
    val playerMotivation: Int? = null,  // 0-100

    @param:Json(name = "discipline_level")
    @field:ColumnInfo(name = "discipline_level")
    val disciplineLevel: Int? = null,  // 0-100

    @field:ColumnInfo(name = "adaptability", defaultValue = "50")
    val adaptability: Int = 50,

    @param:Json(name = "spending_habits")
    @field:ColumnInfo(name = "spending_habits", defaultValue = "BALANCED")
    val spendingHabits: String = "BALANCED", // FRUGAL, BALANCED, DEMANDING

    @param:Json(name = "job_security")
    @field:ColumnInfo(name = "job_security", defaultValue = "100")
    val jobSecurity: Int = 100,

    @param:Json(name = "pressure")
    @field:ColumnInfo(name = "pressure", defaultValue = "0")
    val pressure: Int = 0
) {

    // ============ COMPUTED PROPERTIES ============

    val winPercentage: Double
        get() = if (matchesManaged > 0) (wins.toDouble() / matchesManaged * 100) else 0.0

    val ppg: Double
        get() = if (matchesManaged > 0) (wins * 3.0 + draws) / matchesManaged else 0.0

    val totalAwards: Int
        get() = monthlyAwards + yearlyAwards

    val drawPercentage: Double
        get() = if (matchesManaged > 0) (draws.toDouble() / matchesManaged * 100) else 0.0

    val lossPercentage: Double
        get() = if (matchesManaged > 0) (losses.toDouble() / matchesManaged * 100) else 0.0

    val isEmployed: Boolean
        get() = teamId != null

    val isAvailable: Boolean
        get() = teamId == null

    val experienceLevel: String
        get() = when {
            matchesManaged >= 500 -> "Legendary"
            matchesManaged >= 300 -> "Elite"
            matchesManaged >= 200 -> "Experienced"
            matchesManaged >= 100 -> "Established"
            matchesManaged >= 50 -> "Developing"
            else -> "Rookie"
        }

    val reputationDescription: String
        get() = when (reputationLevel) {
            "Local" -> "Local Coach"
            "Respected" -> "Respected Coach"
            "Continental" -> "Continental Coach"
            "World Class" -> "World Class Coach"
            "Legendary" -> "Legendary Manager"
            else -> reputationLevel
        }

    val contractStatus: String
        get() = when {
            contractEndDate == null -> "Unemployed"
            contractEndDate == 0 -> "Rolling Contract"
            contractEndDate > 0 -> "Contract until ${contractEndDate}"
            else -> "Unknown"
        }

    val careerStage: String
        get() = when {
            age <= 35 -> "Young Manager"
            age <= 45 -> "Prime Years"
            age <= 55 -> "Experienced"
            age <= 65 -> "Veteran"
            else -> "Legend"
        }

    val licenseLevel: Int
        get() = when (coachingLicense) {
            "NONE" -> 0
            "NATIONAL_C" -> 1
            "NATIONAL_B" -> 2
            "NATIONAL_A" -> 3
            "PRO" -> 4
            "UEFA_PRO" -> 5
            "CAF_PRO" -> 5
            else -> 0
        }

    val overallRating: Int
        get() {
            val base = reputation
            val licenseBonus = licenseLevel * 2
            val experienceBonus = (matchesManaged / 100) * 2
            val trophyBonus = trophiesWon * 3
            val performanceBonus = performanceRating / 10

            val attributes = listOf(
                manManagement, tacticalKnowledge, determination,
                judgingPlayerAbility, judgingPlayerPotential,
                attackingCoaching, defendingCoaching, fitnessCoaching,
                mentalCoaching, tacticalCoaching, technicalCoaching,
                adaptability
            )
            val attrAvg = attributes.average().toInt()

            return ((base * 0.4) + (attrAvg * 0.4) + licenseBonus + experienceBonus + trophyBonus + performanceBonus).toInt().coerceIn(0, 100)
        }

    // ============ BUSINESS METHODS ============

    fun updateAfterMatch(won: Boolean, drew: Boolean, lost: Boolean): ManagersEntity {
        val newMatches = matchesManaged + 1
        val newWins = wins + (if (won) 1 else 0)
        val newDraws = draws + (if (drew) 1 else 0)
        val newLosses = losses + (if (lost) 1 else 0)

        val securityChange = when {
            won -> 5
            drew -> 1
            else -> -8
        }

        val fanChange = when {
            won -> 4
            drew -> 0
            else -> -6
        }

        val xpGain = when {
            won -> 25
            drew -> 10
            else -> 5
        }

        val newPerformanceRating = when {
            won -> (performanceRating + 5).coerceIn(0, 100)
            drew -> (performanceRating + 1).coerceIn(0, 100)
            else -> (performanceRating - 3).coerceIn(0, 100)
        }

        val totalXp = managerXp + xpGain
        val nextLevelThreshold = managerLevel * 100
        val (finalLevel, finalXp) = if (totalXp >= nextLevelThreshold) {
            Pair(managerLevel + 1, totalXp - nextLevelThreshold)
        } else {
            Pair(managerLevel, totalXp)
        }

        return this.copy(
            matchesManaged = newMatches,
            wins = newWins,
            draws = newDraws,
            losses = newLosses,
            performanceRating = newPerformanceRating,
            jobSecurity = (jobSecurity + securityChange).coerceIn(0, 100),
            fanHappiness = (fanHappiness + fanChange).coerceIn(0, 100),
            managerXp = finalXp,
            managerLevel = finalLevel,
            managerPoints = managerPoints + (if (totalXp >= nextLevelThreshold) 1 else 0)
        )
    }

    fun winTrophy(): ManagersEntity {
        val newTrophies = trophiesWon + 1

        val reputationGain = when (reputationLevel) {
            "Local" -> 6
            "Respected" -> 5
            "Continental" -> 4
            "World Class" -> 3
            "Legendary" -> 1
            else -> 3
        }

        val newReputation = (reputation + reputationGain).coerceIn(0, 100)
        val newReputationLevel = determineReputationLevel(newReputation)

        return this.copy(
            trophiesWon = newTrophies,
            reputation = newReputation,
            reputationLevel = newReputationLevel
        )
    }

    fun signContract(teamId: Int, salary: Int, contractYears: Int, currentYear: Int): ManagersEntity {
        return this.copy(
            teamId = teamId,
            salary = salary,
            contractEndDate = currentYear + contractYears
        )
    }

    fun leaveClub(): ManagersEntity {
        return this.copy(
            teamId = null,
            contractEndDate = null
        )
    }

    fun renewContract(newSalary: Int, additionalYears: Int, fallbackYear: Int): ManagersEntity {
        val currentEndYear = contractEndDate ?: fallbackYear

        return this.copy(
            salary = newSalary,
            contractEndDate = currentEndYear + additionalYears
        )
    }

    fun updateReputation(newReputation: Int): ManagersEntity {
        val clampedReputation = newReputation.coerceIn(0, 100)

        return this.copy(
            reputation = clampedReputation,
            reputationLevel = determineReputationLevel(clampedReputation)
        )
    }

    fun earnAward(awardType: String): ManagersEntity {
        val type = awardType.uppercase()
        val isMonthly = type.contains("MONTH")
        val isYearly = type.contains("YEAR") || type.contains("SEASON")
        val isYoung = type.contains("YOUNG")
        val isAfrican = type.contains("AFRICAN")

        return when {
            isMonthly && !isYoung -> this.copy(
                monthlyAwards = monthlyAwards + 1,
                coachOfTheMonthTitles = coachOfTheMonthTitles + 1,
                reputation = (reputation + 2).coerceIn(0, 100),
                reputationLevel = determineReputationLevel(reputation + 2)
            )
            isMonthly && isYoung -> this.copy(
                monthlyAwards = monthlyAwards + 1,
                youngCoachOfTheMonthTitles = youngCoachOfTheMonthTitles + 1,
                reputation = (reputation + 3).coerceIn(0, 100),
                reputationLevel = determineReputationLevel(reputation + 3)
            )
            isYearly && !isAfrican -> this.copy(
                yearlyAwards = yearlyAwards + 1,
                coachOfTheYearTitles = coachOfTheYearTitles + 1,
                reputation = (reputation + 10).coerceIn(0, 100),
                reputationLevel = determineReputationLevel(reputation + 10)
            )
            isYearly && isAfrican -> this.copy(
                yearlyAwards = yearlyAwards + 1,
                africanCoachOfTheYearTitles = africanCoachOfTheYearTitles + 1,
                reputation = (reputation + 15).coerceIn(0, 100),
                reputationLevel = determineReputationLevel(reputation + 15)
            )
            else -> this
        }
    }

    fun upgradeLicense(newLicense: String): ManagersEntity {
        return this.copy(
            coachingLicense = newLicense,
            reputation = (reputation + 5).coerceIn(0, 100),
            reputationLevel = determineReputationLevel(reputation + 5)
        )
    }

    fun calculateTransferFee(): Int {
        return when {
            transferFee != null -> transferFee!!
            salary != null -> (salary!! * 0.3).toInt()
            else -> 50000 * (reputation / 10) * (licenseLevel + 1)
        }
    }

    private fun determineReputationLevel(reputationValue: Int): String {
        return when {
            reputationValue >= 90 -> "Legendary"
            reputationValue >= 75 -> "World Class"
            reputationValue >= 55 -> "Continental"
            reputationValue >= 40 -> "Respected"
            else -> "Local"
        }
    }

    companion object {
        fun calculateAgeFromBirthYear(birthYear: Int): Int {
            return GameDateManager.START_YEAR - birthYear
        }
    }
}
