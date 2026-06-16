package com.fameafrica.afm.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import com.squareup.moshi.Json

@Entity(
    tableName = "staff",
    indices = [
        Index(value = ["team_id"]),
        Index(value = ["role"]),
        Index(value = ["specialization"]),
        Index(value = ["impact_rating"]),
        Index(value = ["experience_level"]),
        Index(value = ["previous_player_id"]),
        Index(value = ["staff_type"])
    ]
)
data class StaffEntity(
    @PrimaryKey(autoGenerate = true)
    @field:ColumnInfo(name = "id")
    val id: Int = 0,

    @field:ColumnInfo(name = "name", defaultValue = "J.Nsajigwa")
    val name: String = "J.Nsajigwa",

    @field:ColumnInfo(name = "role")
    val role: String,

    @param:Json(name = "staff_type")
    @field:ColumnInfo(name = "staff_type")
    val staffType: String,

    @param:Json(name = "team_id")
    @field:ColumnInfo(name = "team_id")
    val teamId: Int,

    @param:Json(name = "team_name")
    @field:ColumnInfo(name = "team_name")
    val teamName: String,

    @field:ColumnInfo(name = "specialization", defaultValue = "General")
    val specialization: String,

    @param:Json(name = "impact_rating")
    @field:ColumnInfo(name = "impact_rating", defaultValue = "70")
    val impactRating: Int = 70,

    @field:ColumnInfo(name = "salary", defaultValue = "1200000")
    val salary: Int = 1200000,

    @param:Json(name = "experience_level")
    @field:ColumnInfo(name = "experience_level", defaultValue = "0")
    val experienceLevel: Int = 0,

    @param:Json(name = "face_image")
    @field:ColumnInfo(name = "face_image")
    val faceImage: String? = null,

    @param:Json(name = "previous_player_id")
    @field:ColumnInfo(name = "previous_player_id")
    val previousPlayerId: Int? = null,

    @param:Json(name = "previous_player")
    @field:ColumnInfo(name = "previous_player")
    val previousPlayer: String? = null,

    @field:ColumnInfo(name = "nationality")
    val nationality: String? = null,

    @field:ColumnInfo(name = "age")
    val age: Int? = null,

    @param:Json(name = "contract_end_date")
    @field:ColumnInfo(name = "contract_end_date")
    val contractEndDate: String? = null,

    @param:Json(name = "is_head_of_department")
    @field:ColumnInfo(name = "is_head_of_department")
    val isHeadOfDepartment: Boolean = false,

    @param:Json(name = "mentoring_ability")
    @field:ColumnInfo(name = "mentoring_ability")
    val mentoringAbility: Int = 50,

    @field:ColumnInfo(name = "loyalty")
    val loyalty: Int = 50,

    @field:ColumnInfo(name = "adaptability")
    val adaptability: Int = 50
) {
    val impactLevel: String get() = when {
        impactRating >= 90 -> "World Class"
        impactRating >= 80 -> "Elite"
        impactRating >= 70 -> "Very Good"
        impactRating >= 60 -> "Good"
        impactRating >= 50 -> "Decent"
        else -> "Average"
    }
    val experienceLevelDescription: String get() = when {
        experienceLevel >= 20 -> "Legendary"
        experienceLevel >= 15 -> "Veteran"
        experienceLevel >= 10 -> "Experienced"
        experienceLevel >= 5 -> "Established"
        else -> "Developing"
    }
    val isCoach: Boolean get() = staffType == "COACHING"
    val isScout: Boolean get() = staffType == "SCOUTING"
    val isMedical: Boolean get() = staffType == "MEDICAL"
    val isAdmin: Boolean get() = staffType == "ADMIN"
    val isFormerPlayer: Boolean get() = previousPlayerId != null
    val salaryInMillions: Double get() = salary / 1_000_000.0
    val roleDisplay: String get() = role.split('_').joinToString(" ") { it.lowercase().replaceFirstChar { char -> char.uppercase() } }
    val specializationDisplay: String get() = specialization.split('_').joinToString(" ") { it.lowercase().replaceFirstChar { char -> char.uppercase() } }
}

enum class StaffRole(val value: String, val staffType: String) {
    ASSISTANT_MANAGER("ASSISTANT_MANAGER", "COACHING"),
    FIRST_TEAM_COACH("FIRST_TEAM_COACH", "COACHING"),
    GOALKEEPER_COACH("GOALKEEPER_COACH", "COACHING"),
    FITNESS_COACH("FITNESS_COACH", "COACHING"),
    YOUTH_COACH("YOUTH_COACH", "COACHING"),
    TECHNICAL_COACH("TECHNICAL_COACH", "COACHING"),
    SET_PIECE_COACH("SET_PIECE_COACH", "COACHING"),
    ATTACKING_COACH("ATTACKING_COACH", "COACHING"),
    DEFENSIVE_COACH("DEFENSIVE_COACH", "COACHING"),
    CHIEF_SCOUT("CHIEF_SCOUT", "SCOUTING"),
    SCOUT("SCOUT", "SCOUTING"),
    REGIONAL_SCOUT("REGIONAL_SCOUT", "SCOUTING"),
    YOUTH_SCOUT("YOUTH_SCOUT", "SCOUTING"),
    DATA_ANALYST("DATA_ANALYST", "SCOUTING"),
    HEAD_PHYSIO("HEAD_PHYSIO", "MEDICAL"),
    PHYSIOTHERAPIST("PHYSIOTHERAPIST", "MEDICAL"),
    SPORTS_SCIENTIST("SPORTS_SCIENTIST", "MEDICAL"),
    NUTRITIONIST("NUTRITIONIST", "MEDICAL"),
    DOCTOR("DOCTOR", "MEDICAL"),
    MASSAGE_THERAPIST("MASSAGE_THERAPIST", "MEDICAL"),
    SPORTING_DIRECTOR("SPORTING_DIRECTOR", "ADMIN"),
    TECHNICAL_DIRECTOR("TECHNICAL_DIRECTOR", "ADMIN"),
    ACADEMY_DIRECTOR("ACADEMY_DIRECTOR", "ADMIN"),
    HEAD_OF_YOUTH("HEAD_OF_YOUTH", "ADMIN"),
    CLUB_SECRETARY("CLUB_SECRETARY", "ADMIN"),
    MEDIA_OFFICER("MEDIA_OFFICER", "ADMIN"),
    KIT_MANAGER("KIT_MANAGER", "ADMIN")
}

enum class Specialization(val value: String) {
    GENERAL("General"),
    ATTACKING("Attacking"),
    DEFENSIVE("Defensive"),
    MIDFIELD("Midfield"),
    GOALKEEPING("Goalkeeping"),
    FITNESS("Fitness"),
    YOUTH_DEVELOPMENT("Youth Development"),
    SET_PIECES("Set Pieces"),
    TACTICAL("Tactical"),
    TECHNICAL("Technical"),
    DOMESTIC("Domestic"),
    INTERNATIONAL("International"),
    YOUTH("Youth"),
    OPPOSITION("Opposition Analysis"),
    ADVANCED_ANALYTICS("Advanced Analytics"),
    INJURY_PREVENTION("Injury Prevention"),
    REHABILITATION("Rehabilitation"),
    SPORTS_SCIENCE("Sports Science"),
    NUTRITION("Nutrition"),
    TRANSFERS("Transfers"),
    CONTRACTS("Contracts"),
    FINANCE("Finance"),
    MEDIA("Media"),
    OPERATIONS("Operations")
}
