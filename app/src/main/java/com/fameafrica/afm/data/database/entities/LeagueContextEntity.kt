package com.fameafrica.afm.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json

@Entity(tableName = "league_context")
data class LeagueContextEntity(
    @PrimaryKey val leagueName: String,
    @param:Json(name = "title_race_teams") @field:ColumnInfo(name = "title_race_teams") val titleRaceTeams: String, // JSON List<Int>
    @param:Json(name = "relegation_battle_teams") @field:ColumnInfo(name = "relegation_battle_teams") val relegationBattleTeams: String, // JSON List<Int>
    @param:Json(name = "top_4_race_teams") @field:ColumnInfo(name = "top_4_race_teams") val top4RaceTeams: String, // JSON List<Int>
    @param:Json(name = "surprise_team_id") @field:ColumnInfo(name = "surprise_team_id") val surpriseTeamId: Int?,
    @param:Json(name = "underperforming_team_id") @field:ColumnInfo(name = "underperforming_team_id") val underperformingTeamId: Int?,
    @param:Json(name = "last_updated_week") @field:ColumnInfo(name = "last_updated_week") val lastUpdatedWeek: Int
)
