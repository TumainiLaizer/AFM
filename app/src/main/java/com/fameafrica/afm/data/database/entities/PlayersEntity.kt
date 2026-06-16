package com.fameafrica.afm.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Ignore
import androidx.room.Index
import com.squareup.moshi.Json
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale

@Entity(
    tableName = "players",
    indices = [
        Index(value = ["team_id"]),
        Index(value = ["manager_id"]),
        Index(value = ["nationality"]),
        Index(value = ["position"]),
        Index(value = ["position_category"]),
        Index(value = ["rating"]),
        Index(value = ["potential"]),
        Index(value = ["market_value"])
    ]
)
data class PlayersEntity(
    @PrimaryKey(autoGenerate = true)
    @field:ColumnInfo(name = "id")
    val id: Int = 0,

    @param:Json(name = "manager_id")
    @field:ColumnInfo(name = "manager_id", defaultValue = "0")
    val managerId: Int = 0,

    @field:ColumnInfo(name = "name")
    val name: String,

    @param:Json(name = "team_id")
    @field:ColumnInfo(name = "team_id")
    val teamId: Int?,

    @param:Json(name = "team_name")
    @field:ColumnInfo(name = "team_name")
    val teamName: String = "Free Agent",

    @field:ColumnInfo(name = "region")
    val region: String?,

    @field:ColumnInfo(name = "nationality", defaultValue = "Tanzania")
    val nationality: String = "Tanzania",

    @field:ColumnInfo(name = "age")
    val age: Int,

    @field:ColumnInfo(name = "height")
    val height: Int,

    @param:Json(name = "preferred_foot")
    @field:ColumnInfo(name = "preferred_foot", defaultValue = "RIGHT")
    val preferredFoot: String = "RIGHT",

    @field:ColumnInfo(name = "position")
    val position: String,

    @param:Json(name = "position_category")
    @field:ColumnInfo(name = "position_category")
    val positionCategory: String,

    @param:Json(name = "shirt_number")
    @field:ColumnInfo(name = "shirt_number")
    val shirtNumber: Int,

    @param:Json(name = "personality_type")
    @field:ColumnInfo(name = "personality_type", defaultValue = "PROFESSIONAL")
    val personalityType: String = "PROFESSIONAL",

    @field:ColumnInfo(name = "archetype")
    val archetype: String?,

    @param:Json(name = "primary_trait")
    @field:ColumnInfo(name = "primary_trait")
    val primaryTrait: String?,

    @param:Json(name = "secondary_trait")
    @field:ColumnInfo(name = "secondary_trait")
    val secondaryTrait: String?,

    @param:Json(name = "gameplay_focus")
    @field:ColumnInfo(name = "gameplay_focus")
    val gameplayFocus: String?,

    @field:ColumnInfo(name = "rating", defaultValue = "60")
    val rating: Int = 60,

    @field:ColumnInfo(name = "reputation", defaultValue = "50")
    val reputation: Int = 50,

    @field:ColumnInfo(name = "potential", defaultValue = "85")
    val potential: Int = 85,

    @param:Json(name = "current_form")
    @field:ColumnInfo(name = "current_form", defaultValue = "5") // Scale 1-10
    val currentForm: Int = 5,

    @field:ColumnInfo(name = "experience", defaultValue = "0")
    val experience: Int = 0,

    @field:ColumnInfo(name = "morale", defaultValue = "75")
    val morale: Int = 75,

    @field:ColumnInfo(name = "finishing", defaultValue = "50")
    val finishing: Int = 50,

    @field:ColumnInfo(name = "passing", defaultValue = "50")
    val passing: Int = 50,

    @field:ColumnInfo(name = "dribbling", defaultValue = "50")
    val dribbling: Int = 50,

    @field:ColumnInfo(name = "skill", defaultValue = "50")
    val skill: Int = 50,

    @field:ColumnInfo(name = "crossing", defaultValue = "50")
    val crossing: Int = 50,

    @field:ColumnInfo(name = "defending", defaultValue = "50")
    val defending: Int = 50,

    @field:ColumnInfo(name = "heading", defaultValue = "50")
    val heading: Int = 50,

    @param:Json(name = "long_shots")
    @field:ColumnInfo(name = "long_shots", defaultValue = "50")
    val longShots: Int = 50,

    @field:ColumnInfo(name = "pace", defaultValue = "50")
    val pace: Int = 50,

    @field:ColumnInfo(name = "stamina", defaultValue = "99")
    val stamina: Int = 99,

    @field:ColumnInfo(name = "strength", defaultValue = "50")
    val strength: Int = 50,

    @field:ColumnInfo(name = "acceleration", defaultValue = "50")
    val acceleration: Int = 50,

    @field:ColumnInfo(name = "agility", defaultValue = "50")
    val agility: Int = 50,

    @field:ColumnInfo(name = "aggression", defaultValue = "30")
    val aggression: Int = 30,

    @field:ColumnInfo(name = "leadership", defaultValue = "50")
    val leadership: Int = 50,

    @field:ColumnInfo(name = "motivation", defaultValue = "50")
    val motivation: Int = 50,

    @field:ColumnInfo(name = "composure", defaultValue = "50")
    val composure: Int = 50,

    @field:ColumnInfo(name = "vision", defaultValue = "50")
    val vision: Int = 50,

    @field:ColumnInfo(name = "positioning", defaultValue = "50")
    val positioning: Int = 50,

    @field:ColumnInfo(name = "anticipation", defaultValue = "50")
    val anticipation: Int = 50,

    @field:ColumnInfo(name = "decisions", defaultValue = "50")
    val decisions: Int = 50,

    @field:ColumnInfo(name = "creativity", defaultValue = "50")
    val creativity: Int = 50,

    @field:ColumnInfo(name = "teamwork", defaultValue = "50")
    val teamwork: Int = 50,

    @field:ColumnInfo(name = "goalkeeping", defaultValue = "10")
    val goalkeeping: Int = 10,

    @field:ColumnInfo(name = "reflexes", defaultValue = "50")
    val reflexes: Int = 50,

    @field:ColumnInfo(name = "handling", defaultValue = "50")
    val handling: Int = 50,

    @param:Json(name = "aerial_ability")
    @field:ColumnInfo(name = "aerial_ability", defaultValue = "50")
    val aerialAbility: Int = 50,

    @param:Json(name = "command_of_area")
    @field:ColumnInfo(name = "command_of_area", defaultValue = "50")
    val commandOfArea: Int = 50,

    @field:ColumnInfo(name = "kicking", defaultValue = "50")
    val kicking: Int = 50,

    @param:Json(name = "injury_risk")
    @field:ColumnInfo(name = "injury_risk", defaultValue = "10")
    val injuryRisk: Int = 10,

    @param:Json(name = "injury_status")
    @field:ColumnInfo(name = "injury_status", defaultValue = "HEALTHY")
    val injuryStatus: String = "HEALTHY",

    @param:Json(name = "recovery_time")
    @field:ColumnInfo(name = "recovery_time", defaultValue = "0")
    val recoveryTime: Int = 0,

    @field:ColumnInfo(name = "suspended", defaultValue = "0")
    val suspended: Boolean = false,

    @param:Json(name = "market_value")
    @field:ColumnInfo(name = "market_value")
    val marketValue: Int,

    @field:ColumnInfo(name = "salary", defaultValue = "500000")
    val salary: Double = 500000.0,

    @param:Json(name = "contract_expiry")
    @field:ColumnInfo(name = "contract_expiry", defaultValue = "2029-06-30")
    val contractExpiry: String? = "2029-06-30",

    @param:Json(name = "free_agent")
    @field:ColumnInfo(name = "free_agent", defaultValue = "0")
    val freeAgent: Boolean = false,

    @param:Json(name = "transfer_list_status")
    @field:ColumnInfo(name = "transfer_list_status", defaultValue = "NOT_LISTED")
    val transferListStatus: String = "NOT_LISTED",

    @param:Json(name = "is_homegrown")
    @field:ColumnInfo(name = "is_homegrown", defaultValue = "0")
    val isHomegrown: Boolean = false,

    @param:Json(name = "has_release_clause")
    @field:ColumnInfo(name = "has_release_clause", defaultValue = "0")
    val hasReleaseClause: Boolean = false,

    @field:ColumnInfo(name = "matches", defaultValue = "0")
    val matches: Int = 0,

    @field:ColumnInfo(name = "goals", defaultValue = "0")
    val goals: Int = 0,

    @field:ColumnInfo(name = "assists", defaultValue = "0")
    val assists: Int = 0,

    @param:Json(name = "clean_sheets")
    @field:ColumnInfo(name = "clean_sheets", defaultValue = "0")
    val cleanSheets: Int = 0,

    @param:Json(name = "red_cards")
    @field:ColumnInfo(name = "red_cards", defaultValue = "0")
    val redCards: Int = 0,

    @param:Json(name = "yellow_cards")
    @field:ColumnInfo(name = "yellow_cards", defaultValue = "0")
    val yellowCards: Int = 0,

    @field:ColumnInfo(name = "trophies", defaultValue = "0")
    val trophies: Int = 0,

    @param:Json(name = "man_of_match")
    @field:ColumnInfo(name = "man_of_match", defaultValue = "0")
    val manOfMatch: Int = 0,

    @param:Json(name = "is_starting_xi")
    @field:ColumnInfo(name = "is_starting_xi", defaultValue = "0")
    val isStartingXi: Boolean = false,

    @param:Json(name = "is_captain")
    @field:ColumnInfo(name = "is_captain", defaultValue = "0")
    val isCaptain: Boolean = false,

    @param:Json(name = "is_vice_captain")
    @field:ColumnInfo(name = "is_vice_captain", defaultValue = "0")
    val isViceCaptain: Boolean = false,

    @param:Json(name = "work_rate")
    @field:ColumnInfo(name = "work_rate", defaultValue = "MEDIUM")
    val workRate: String = "MEDIUM",

    @field:ColumnInfo(name = "retired", defaultValue = "0")
    val retired: Boolean = false,

    @param:Json(name = "future_role")
    @field:ColumnInfo(name = "future_role")
    val futureRole: String?,

    @param:Json(name = "player_coach")
    @field:ColumnInfo(name = "player_coach", defaultValue = "0")
    val playerCoach: Boolean = false,

    @field:ColumnInfo(name = "season")
    val season: String?,

    @param:Json(name = "media_handling")
    @field:ColumnInfo(name = "media_handling", defaultValue = "50")
    val mediaHandling: Int = 50,

    @param:Json(name = "fan_popularity")
    @field:ColumnInfo(name = "fan_popularity", defaultValue = "50")
    val fanPopularity: Int = 50,

    @param:Json(name = "dressing_room_influence")
    @field:ColumnInfo(name = "dressing_room_influence", defaultValue = "50")
    val dressingRoomInfluence: Int = 50,

    @field:ColumnInfo(name = "sharpness", defaultValue = "50")
    val sharpness: Int = 50,

    @param:Json(name = "tactical_familiarity")
    @field:ColumnInfo(name = "tactical_familiarity", defaultValue = "30")
    val tacticalFamiliarity: Int = 30,

    @param:Json(name = "face_image")
    @field:ColumnInfo(name = "face_image")
    val faceImage: String?,

    @param:Json(name = "image_url")
    @field:ColumnInfo(name = "image_url")
    val imageUrl: String?,

    @param:Json(name = "created_at")
    @field:ColumnInfo(name = "created_at", defaultValue = "CURRENT_TIMESTAMP")
    val createdAt: String = "",

    @param:Json(name = "updated_at")
    @field:ColumnInfo(name = "updated_at", defaultValue = "CURRENT_TIMESTAMP")
    val updatedAt: String = ""
) {

    @Ignore
    var isSelected: Boolean = false

    @get:Ignore
    val goalsConceded: Int
        get() = 0

    val overallRating: Int
        get() = when {
            isGoalkeeper -> calculateGoalkeeperRating()
            isDefender -> calculateDefenderRating()
            isMidfielder -> calculateMidfielderRating()
            isForward -> calculateForwardRating()
            else -> rating
        }

    val isGoalkeeper: Boolean
        get() = position == "GK"

    val isDefender: Boolean
        get() = position in listOf("CB", "LB", "RB", "SW", "LWB", "RWB")

    val isMidfielder: Boolean
        get() = position in listOf("CDM", "CM", "CAM", "LM", "RM")

    val isForward: Boolean
        get() = position in listOf("LW", "RW", "ST", "CF")

    val ageGroup: String
        get() = when {
            age <= 19 -> "Youth"
            age <= 23 -> "Young"
            age <= 29 -> "Prime"
            age <= 34 -> "Veteran"
            else -> "Senior"
        }

    val potentialGrade: String
        get() = when {
            potential >= 90 -> "World Class"
            potential >= 85 -> "Elite"
            potential >= 80 -> "Very Good"
            potential >= 75 -> "Good"
            potential >= 70 -> "Decent"
            else -> "Average"
        }

    val currentGrade: String
        get() = when {
            rating >= 90 -> "World Class"
            rating >= 85 -> "Elite"
            rating >= 80 -> "Very Good"
            rating >= 75 -> "Good"
            rating >= 70 -> "Decent"
            else -> "Average"
        }

    val isInjured: Boolean
        get() = injuryStatus != "HEALTHY"

    val isAvailable: Boolean
        get() = !isInjured && !suspended && !retired

    val isTransferListed: Boolean
        get() = transferListStatus == "AVAILABLE"

    val isLoanListed: Boolean
        get() = transferListStatus == "LOAN_LISTED"

    val contractStatus: String
        get() {
            val expiry = contractExpiry?.split("-") ?: return "Free Agent"
            if (expiry.size != 3) return "Unknown"
            val expiryYear = expiry[0].toIntOrNull() ?: return "Unknown"
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            return when {
                expiryYear <= currentYear -> "Expired"
                expiryYear == currentYear + 1 -> "Expiring Soon"
                expiryYear <= currentYear + 2 -> "Negotiable"
                else -> "Long Term"
            }
        }

    @get:Ignore
    val contractExpiryWeek: Int
        get() {
            return try {
                val expiryStr = contractExpiry ?: return 0
                val format = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val expiryDate = format.parse(expiryStr) ?: return 0
                val startDate = Calendar.getInstance().apply {
                    set(2025, Calendar.JUNE, 1, 0, 0, 0)
                }.time
                val diff = expiryDate.time - startDate.time
                (diff / (7L * 24 * 60 * 60 * 1000)).toInt()
            } catch (e: Exception) {
                0
            }
        }

    val valueInMillions: Double
        get() = marketValue / 1_000_000.0

    val salaryInMillions: Double
        get() = salary / 1_000_000.0

    val fullName: String
        get() = name

    val displayName: String
        get() {
            val parts = name.trim().split("\\s+".toRegex())
            if (parts.size <= 1) return name
            val initials = parts.dropLast(1).joinToString("") { it.take(1).uppercase() + "." }
            return "$initials${parts.last()}"
        }

    fun formatCurrency(value: Long): String {
        return when {
            value >= 1_000_000 -> "€${String.format("%.1f", value / 1_000_000.0)}M"
            value >= 1_000 -> "€${value / 1_000}K"
            else -> "€$value"
        }
    }

    private fun calculateGoalkeeperRating(): Int {
        val gkAttributes = listOf(goalkeeping, reflexes, handling, aerialAbility, commandOfArea, kicking)
        return (gkAttributes.average() * 0.7 + rating * 0.3).toInt()
    }

    private fun calculateDefenderRating(): Int {
        val defAttributes = listOf(defending, heading, positioning, anticipation, decisions, strength)
        val physAttributes = listOf(pace, stamina, acceleration)
        return (defAttributes.average() * 0.6 + physAttributes.average() * 0.2 + rating * 0.2).toInt()
    }

    private fun calculateMidfielderRating(): Int {
        val midAttributes = listOf(passing, dribbling, vision, creativity, decisions, teamwork)
        val physAttributes = listOf(pace, stamina, acceleration, agility)
        return (midAttributes.average() * 0.6 + physAttributes.average() * 0.2 + rating * 0.2).toInt()
    }

    private fun calculateForwardRating(): Int {
        val fwdAttributes = listOf(finishing, dribbling, pace, acceleration, composure, longShots)
        val techAttributes = listOf(skill, crossing, heading)
        return (fwdAttributes.average() * 0.6 + techAttributes.average() * 0.2 + rating * 0.2).toInt()
    }

    fun updateAfterMatch(
        goalsScored: Int, 
        assistsMade: Int, 
        isManOfMatch: Boolean, 
        matchRating: Double,
        staminaDepletion: Int = 15, // Base stamina drop for playing 90 min
        fatigueImpact: Int = 0      // Extra drop for travel/weather
    ): PlayersEntity {
        // Form Logic (1-10 Scale)
        val formDelta = when {
            isManOfMatch -> 2.0
            matchRating >= 8.5 -> 1.5
            matchRating >= 7.5 -> 0.5
            matchRating < 5.0 -> -1.5
            matchRating < 6.0 -> -0.5
            else -> 0.0
        }
        val newForm = (currentForm + formDelta).coerceIn(1.0, 10.0).toInt()

        // Morale Logic
        val moraleDelta = ((matchRating - 6.7) * 4).toInt()
        val newMorale = (morale + moraleDelta + (if (isManOfMatch) 5 else 0)).coerceIn(0, 100)

        // Reputation Logic
        var repDelta = 0
        if (isManOfMatch) repDelta += 1
        if (matchRating >= 8.5) repDelta += 1
        val newReputation = (reputation + repDelta).coerceIn(0, 100)

        // Fan Popularity
        var fanDelta = (goalsScored * 2) + assistsMade + (if (isManOfMatch) 3 else 0)
        if (matchRating >= 8.0) fanDelta += 1
        val newFanPopularity = (fanPopularity + fanDelta).coerceIn(0, 100)

        // Dressing Room Influence
        var influenceDelta = 0
        if (matches > 0 && matches % 10 == 0) influenceDelta += 1
        if (isManOfMatch && rating > 75) influenceDelta += 1
        val newInfluence = (dressingRoomInfluence + influenceDelta).coerceIn(0, 100)

        // Media Handling (Small improvement from experience)
        var mediaDelta = 0
        if (isManOfMatch && (1..10).random() > 7) mediaDelta += 1
        val newMediaHandling = (mediaHandling + mediaDelta).coerceIn(0, 100)

        // Dynamic Potential (Very rare growth)
        var newPotential = potential
        if (matchRating >= 9.0 && age < 23 && (1..100).random() > 98) {
            newPotential = (newPotential + 1).coerceIn(0, 99)
        }

        // Stamina Depletion
        val totalStaminaDrop = staminaDepletion + fatigueImpact
        val newStamina = (stamina - totalStaminaDrop).coerceIn(0, 100)

        return this.copy(
            matches = matches + 1,
            goals = goals + goalsScored,
            assists = assists + assistsMade,
            manOfMatch = manOfMatch + (if (isManOfMatch) 1 else 0),
            currentForm = newForm,
            morale = newMorale,
            reputation = newReputation,
            fanPopularity = newFanPopularity,
            dressingRoomInfluence = newInfluence,
            mediaHandling = newMediaHandling,
            potential = newPotential,
            stamina = newStamina,
            experience = experience + 1,
            updatedAt = Calendar.getInstance().time.toString()
        )
    }

    fun winTrophy(): PlayersEntity {
        return this.copy(
            trophies = trophies + 1,
            reputation = (reputation + 3).coerceIn(0, 100),
            fanPopularity = (fanPopularity + 5).coerceIn(0, 100),
            morale = (morale + 25).coerceIn(0, 100),
            updatedAt = Calendar.getInstance().time.toString()
        )
    }

    fun earnAward(awardType: String): PlayersEntity {
        val type = awardType.uppercase()
        val isSeason = type.contains("SEASON") || type.contains("YEAR") || type.contains("SCORER") || type.contains("ASSISTER")

        val repGain = if (isSeason) 8 else 3
        val fanGain = if (isSeason) 12 else 4

        return this.copy(
            reputation = (reputation + repGain).coerceIn(0, 100),
            fanPopularity = (fanPopularity + fanGain).coerceIn(0, 100),
            morale = (morale + 20).coerceIn(0, 100),
            updatedAt = Calendar.getInstance().time.toString()
        )
    }

    fun updateMorale(change: Int): PlayersEntity {
        return this.copy(morale = (morale + change).coerceIn(0, 100))
    }

    fun setInjury(status: String, recoveryDays: Int): PlayersEntity {
        return this.copy(
            injuryStatus = status,
            recoveryTime = recoveryDays,
            updatedAt = Calendar.getInstance().time.toString()
        )
    }

    fun recoverFromInjury(): PlayersEntity {
        return this.copy(
            injuryStatus = "HEALTHY",
            recoveryTime = 0,
            updatedAt = Calendar.getInstance().time.toString()
        )
    }

    fun addSuspension(): PlayersEntity {
        return this.copy(suspended = true, updatedAt = Calendar.getInstance().time.toString())
    }

    fun removeSuspension(): PlayersEntity {
        return this.copy(suspended = false, updatedAt = Calendar.getInstance().time.toString())
    }

    fun transferTo(newTeamId: Int, newTeamName: String, newMarketValue: Int? = null): PlayersEntity {
        return this.copy(
            teamId = newTeamId,
            teamName = newTeamName,
            marketValue = newMarketValue ?: marketValue,
            updatedAt = Calendar.getInstance().time.toString()
        )
    }

    fun renewContract(newSalary: Double, newExpiry: String): PlayersEntity {
        return this.copy(
            salary = newSalary,
            contractExpiry = newExpiry,
            updatedAt = Calendar.getInstance().time.toString()
        )
    }

    fun retire(): PlayersEntity {
        return this.copy(retired = true, updatedAt = Calendar.getInstance().time.toString())
    }

    fun updateRating(newRating: Int): PlayersEntity {
        return this.copy(rating = newRating.coerceIn(1, 99), updatedAt = Calendar.getInstance().time.toString())
    }

    fun updatePotential(newPotential: Int): PlayersEntity {
        return this.copy(potential = newPotential.coerceIn(1, 99), updatedAt = Calendar.getInstance().time.toString())
    }

    fun updateAttributes(attributeUpdates: Map<String, Int>): PlayersEntity {
        var updated = this
        attributeUpdates.forEach { (attribute, value) ->
            updated = when (attribute) {
                "finishing" -> updated.copy(finishing = value.coerceIn(1, 99))
                "passing" -> updated.copy(passing = value.coerceIn(1, 99))
                "dribbling" -> updated.copy(dribbling = value.coerceIn(1, 99))
                "skill" -> updated.copy(skill = value.coerceIn(1, 99))
                "crossing" -> updated.copy(crossing = value.coerceIn(1, 99))
                "defending" -> updated.copy(defending = value.coerceIn(1, 99))
                "heading" -> updated.copy(heading = value.coerceIn(1, 99))
                "long_shots" -> updated.copy(longShots = value.coerceIn(1, 99))
                "pace" -> updated.copy(pace = value.coerceIn(1, 99))
                "stamina" -> updated.copy(stamina = value.coerceIn(1, 99))
                "strength" -> updated.copy(strength = value.coerceIn(1, 99))
                "acceleration" -> updated.copy(acceleration = value.coerceIn(1, 99))
                "agility" -> updated.copy(agility = value.coerceIn(1, 99))
                "aggression" -> updated.copy(aggression = value.coerceIn(1, 99))
                "leadership" -> updated.copy(leadership = value.coerceIn(1, 99))
                "motivation" -> updated.copy(motivation = value.coerceIn(1, 99))
                "composure" -> updated.copy(composure = value.coerceIn(1, 99))
                "vision" -> updated.copy(vision = value.coerceIn(1, 99))
                "positioning" -> updated.copy(positioning = value.coerceIn(1, 99))
                "anticipation" -> updated.copy(anticipation = value.coerceIn(1, 99))
                "decisions" -> updated.copy(decisions = value.coerceIn(1, 99))
                "creativity" -> updated.copy(creativity = value.coerceIn(1, 99))
                "teamwork" -> updated.copy(teamwork = value.coerceIn(1, 99))
                "goalkeeping" -> updated.copy(goalkeeping = value.coerceIn(1, 99))
                "reflexes" -> updated.copy(reflexes = value.coerceIn(1, 99))
                "handling" -> updated.copy(handling = value.coerceIn(1, 99))
                "aerial_ability" -> updated.copy(aerialAbility = value.coerceIn(1, 99))
                "command_of_area" -> updated.copy(commandOfArea = value.coerceIn(1, 99))
                "kicking" -> updated.copy(kicking = value.coerceIn(1, 99))
                else -> updated
            }
        }
        return updated.copy(updatedAt = Calendar.getInstance().time.toString())
    }

    companion object {
        fun calculatePositionCategory(position: String): String {
            return when (position) {
                "GK" -> "GOALKEEPER"
                in listOf("CB", "LB", "RB", "SW", "LWB", "RWB") -> "DEFENDER"
                in listOf("CDM", "CM", "CAM", "LM", "RM") -> "MIDFIELDER"
                in listOf("LW", "RW", "ST", "CF") -> "FORWARD"
                else -> "OTHER"
            }
        }
    }
}
