package com.fameafrica.afm.data.database.entities

import androidx.room.*
import com.squareup.moshi.Json

@Entity(
    tableName = "trophies",
    foreignKeys = [
        ForeignKey(
            entity = ManagersEntity::class,
            parentColumns = ["id"],
            childColumns = ["manager_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TeamsEntity::class,
            parentColumns = ["id"],
            childColumns = ["club_id"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SeasonAwardsEntity::class,
            parentColumns = ["id"],
            childColumns = ["season_award_id"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SeasonHistoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["season_history_id"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["manager_id"]),
        Index(value = ["club_id"]),
        Index(value = ["club_name"]),
        Index(value = ["season"]),
        Index(value = ["trophy_name"]),
        Index(value = ["competition_level"]),
        Index(value = ["trophy_type"]),
        Index(value = ["season_award_id"]),
        Index(value = ["season_history_id"]),
        Index(value = ["manager_id", "season"]),
        Index(value = ["club_id", "season"])
    ]
)
data class TrophiesEntity(
    @PrimaryKey(autoGenerate = true)
    @field:ColumnInfo(name = "id")
    val id: Int = 0,

    @param:Json(name = "manager_id")
    @field:ColumnInfo(name = "manager_id")
    val managerId: Int,

    @param:Json(name = "manager_name")
    @field:ColumnInfo(name = "manager_name")
    val managerName: String? = null,

    @param:Json(name = "club_id")
    @field:ColumnInfo(name = "club_id")
    val clubId: Int? = null,

    @param:Json(name = "club_name")
    @field:ColumnInfo(name = "club_name")
    val clubName: String,

    @param:Json(name = "trophy_name")
    @field:ColumnInfo(name = "trophy_name")
    val trophyName: String,

    @param:Json(name = "trophy_type")
    @field:ColumnInfo(name = "trophy_type")
    val trophyType: String,  // LEAGUE_TITLE, CUP_TITLE, CONTINENTAL_TITLE, SUPER_CUP, AWARD

    @param:Json(name = "competition_id")
    @field:ColumnInfo(name = "competition_id")
    val competitionId: Int? = null,  // References leagues(id) or cups(id)

    @param:Json(name = "competition_name")
    @field:ColumnInfo(name = "competition_name")
    val competitionName: String? = null,

    @param:Json(name = "competition_level")
    @field:ColumnInfo(name = "competition_level")
    val competitionLevel: String = "Domestic",  // Domestic, Continental, International

    @field:ColumnInfo(name = "season")
    val season: String,

    @param:Json(name = "season_year")
    @field:ColumnInfo(name = "season_year")
    val seasonYear: Int,

    @param:Json(name = "match_played")
    @field:ColumnInfo(name = "match_played")
    val matchPlayed: String? = null,  // e.g., "2-1 vs Al Ahly"

    @field:ColumnInfo(name = "opponent")
    val opponent: String? = null,

    @field:ColumnInfo(name = "venue")
    val venue: String? = null,

    @field:ColumnInfo(name = "attendance")
    val attendance: Int? = null,

    @param:Json(name = "win_type")
    @field:ColumnInfo(name = "win_type")
    val winType: String? = null,  // Regular, Penalties, Walkover

    @param:Json(name = "icon_path")
    @field:ColumnInfo(name = "icon_path")
    val iconPath: String? = null,

    @param:Json(name = "image_url")
    @field:ColumnInfo(name = "image_url")
    val imageUrl: String? = null,

    @param:Json(name = "season_award_id")
    @field:ColumnInfo(name = "season_award_id")
    val seasonAwardId: Int? = null,  // Link to season_awards if this trophy is an individual award

    @param:Json(name = "season_history_id")
    @field:ColumnInfo(name = "season_history_id")
    val seasonHistoryId: Int? = null,  // Link to season_history

    @field:ColumnInfo(name = "notes")
    val notes: String? = null,

    @param:Json(name = "date_won")
    @field:ColumnInfo(name = "date_won")
    val dateWon: String? = null,

    @param:Json(name = "created_at")
    @field:ColumnInfo(name = "created_at")
    val createdAt: String = getCurrentDateTime()
) {

    // ============ COMPUTED PROPERTIES ============

    @get:Ignore
    val isLeagueTitle: Boolean
        get() = trophyType == "LEAGUE_TITLE"

    @get:Ignore
    val isCupTitle: Boolean
        get() = trophyType == "CUP_TITLE"

    @get:Ignore
    val isContinentalTitle: Boolean
        get() = trophyType == "CONTINENTAL_TITLE"

    @get:Ignore
    val isSuperCup: Boolean
        get() = trophyType == "SUPER_CUP"

    @get:Ignore
    val isIndividualAward: Boolean
        get() = trophyType == "AWARD"

    @get:Ignore
    val isDomestic: Boolean
        get() = competitionLevel == "Domestic"

    @get:Ignore
    val isContinental: Boolean
        get() = competitionLevel == "Continental"

    @get:Ignore
    val isInternational: Boolean
        get() = competitionLevel == "International"

    @get:Ignore
    val trophyDisplay: String
        get() = "$trophyName - $season"

    @get:Ignore
    val fullDescription: String
        get() = buildString {
            append("$trophyName")
            if (opponent != null) append(" vs $opponent")
            if (matchPlayed != null) append(" ($matchPlayed)")
            if (winType != null) append(" - $winType")
        }

    @get:Ignore
    val icon: String
        get() = when {
            isLeagueTitle -> "🏆"
            isCupTitle -> "🏆"
            isContinentalTitle -> "🌍🏆"
            isSuperCup -> "🛡️"
            isIndividualAward -> "🏅"
            else -> "🏆"
        }

    companion object {
        private fun getCurrentDateTime(): String {
            val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
            return dateFormat.format(java.util.Date())
        }
    }
}

// ============ ENUMS ============

enum class TrophyType(val value: String) {
    LEAGUE_TITLE("LEAGUE_TITLE"),
    CUP_TITLE("CUP_TITLE"),
    CONTINENTAL_TITLE("CONTINENTAL_TITLE"),
    SUPER_CUP("SUPER_CUP"),
    AWARD("AWARD")
}

enum class CompetitionLevel(val value: String) {
    DOMESTIC("Domestic"),
    CONTINENTAL("Continental"),
    INTERNATIONAL("International")
}
