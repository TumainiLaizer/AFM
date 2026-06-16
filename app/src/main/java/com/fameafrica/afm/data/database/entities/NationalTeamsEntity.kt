package com.fameafrica.afm.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import com.squareup.moshi.Json

@Entity(
    tableName = "national_teams",
    indices = [
        Index(value = ["name"]),
        Index(value = ["fifa_code"], unique = true),
        Index(value = ["confederation"]),
        Index(value = ["manager_id"]),
        Index(value = ["elo_rating"]),
        Index(value = ["reputation"]),
        Index(value = ["rival_fifa_code"]),
        Index(value = ["world_ranking"]),
        Index(value = ["continental_ranking"])
    ]
)
data class NationalTeamsEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "confederation")
    val confederation: String,

    @Json(name = "fifa_code")
    @ColumnInfo(name = "fifa_code")
    val fifaCode: String,

    @Json(name = "nationality_id")
    @ColumnInfo(name = "nationality_id")
    val nationalityId: Int,  // References nationalities.id

    @Json(name = "elo_rating")
    @ColumnInfo(name = "elo_rating")
    val eloRating: Int = 1500,

    @Json(name = "fifa_ranking")
    @ColumnInfo(name = "fifa_ranking")
    val fifaRanking: Int? = null,

    @Json(name = "world_ranking")
    @ColumnInfo(name = "world_ranking")
    val worldRanking: Int? = null,

    @Json(name = "continental_ranking")
    @ColumnInfo(name = "continental_ranking")
    val continentalRanking: Int? = null,

    @ColumnInfo(name = "reputation", defaultValue = "50")
    val reputation: Int = 50,

    @Json(name = "continental_titles")
    @ColumnInfo(name = "continental_titles", defaultValue = "0")
    val continentalTitles: Int = 0,

    @Json(name = "world_cup_appearances")
    @ColumnInfo(name = "world_cup_appearances", defaultValue = "0")
    val worldCupAppearances: Int = 0,

    @Json(name = "best_finish")
    @ColumnInfo(name = "best_finish")
    val bestFinish: String? = null,

    @Json(name = "home_stadium")
    @ColumnInfo(name = "home_stadium")
    val homeStadium: String? = null,

    @Json(name = "stadium_capacity")
    @ColumnInfo(name = "stadium_capacity")
    val stadiumCapacity: Int? = null,

    @Json(name = "fan_loyalty")
    @ColumnInfo(name = "fan_loyalty", defaultValue = "50")
    val fanLoyalty: Int = 50,

    @Json(name = "rival_fifa_code")
    @ColumnInfo(name = "rival_fifa_code")
    val rivalFifaCode: String? = null,

    @Json(name = "rival_team")
    @ColumnInfo(name = "rival_team")
    val rivalTeam: String? = null,

    @Json(name = "avg_attacking_ability")
    @ColumnInfo(name = "avg_attacking_ability")
    val avgAttackingAbility: Double? = null,

    @Json(name = "avg_defence_ability")
    @ColumnInfo(name = "avg_defence_ability")
    val avgDefenceAbility: Double? = null,

    @Json(name = "avg_playmaking_ability")
    @ColumnInfo(name = "avg_playmaking_ability")
    val avgPlaymakingAbility: Double? = null,

    @ColumnInfo(name = "crowdSupport", defaultValue = "30")
    val crowdSupport: Int = 30,

    @ColumnInfo(name = "sponsorships")
    val sponsorships: String? = null,

    @Json(name = "continental_competition")
    @ColumnInfo(name = "continental_competition")
    val continentalCompetition: String? = null,

    @Json(name = "recent_form")
    @ColumnInfo(name = "recent_form")
    val recentForm: String? = null,

    @Json(name = "manager_id")
    @ColumnInfo(name = "manager_id")
    val managerId: Int? = null,

    @Json(name = "squad_size")
    @ColumnInfo(name = "squad_size")
    val squadSize: Int = 0,

    @Json(name = "average_age")
    @ColumnInfo(name = "average_age")
    val averageAge: Double? = null,

    @Json(name = "captain_id")
    @ColumnInfo(name = "captain_id")
    val captainId: Int? = null,

    @Json(name = "top_scorer_id")
    @ColumnInfo(name = "top_scorer_id")
    val topScorerId: Int? = null,

    @Json(name = "most_capped_id")
    @ColumnInfo(name = "most_capped_id")
    val mostCappedId: Int? = null,

    @Json(name = "last_updated")
    @ColumnInfo(name = "last_updated")
    val lastUpdated: String? = null
) {
    @androidx.room.Ignore
    var rivalTeamId: Int? = null
}
