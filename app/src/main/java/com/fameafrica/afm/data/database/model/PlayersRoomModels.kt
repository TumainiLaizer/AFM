package com.fameafrica.afm.data.database.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import com.fameafrica.afm.data.database.entities.PlayersEntity

data class SquadAnalysis(
    @ColumnInfo(name = "position_category") val positionCategory: String,
    @ColumnInfo(name = "player_count") val playerCount: Int,
    @ColumnInfo(name = "avg_rating") val avgRating: Double?,
    @ColumnInfo(name = "avg_age") val avgAge: Double?,
    @ColumnInfo(name = "avg_height") val avgHeight: Double?,
    @ColumnInfo(name = "avg_value") val avgValue: Double?
)

data class NationalityDistribution(
    val nationality: String,
    @ColumnInfo(name = "player_count") val playerCount: Int,
    @ColumnInfo(name = "avg_rating") val avgRating: Double?
)

data class ArchetypeDistribution(
    val archetype: String?,
    @ColumnInfo(name = "player_count") val playerCount: Int,
    @ColumnInfo(name = "avg_rating") val avgRating: Double?
)

data class PersonalityDistribution(
    @ColumnInfo(name = "personality_type") val personalityType: String?,
    @ColumnInfo(name = "player_count") val playerCount: Int,
    @ColumnInfo(name = "avg_rating") val avgRating: Double?
)

data class TraitDistribution(
    @ColumnInfo(name = "primary_trait") val primaryTrait: String?,
    @ColumnInfo(name = "player_count") val playerCount: Int
)

data class PlayerWithDetails(
    @Embedded val player: PlayersEntity,
    @ColumnInfo(name = "detail_team_name") val teamName: String?,
    @ColumnInfo(name = "detail_team_logo") val teamLogo: String?,
    @ColumnInfo(name = "detail_team_league") val teamLeague: String?,
    @ColumnInfo(name = "detail_team_reputation") val teamReputation: Int?
)

data class PlayerWithTeamDetails(
    @Embedded val player: PlayersEntity,
    @ColumnInfo(name = "detail_team_name") val teamName: String?,
    @ColumnInfo(name = "detail_team_logo") val teamLogo: String?
)