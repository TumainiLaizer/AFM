package com.fameafrica.afm.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "world_state")
data class WorldStateEntity(
    @PrimaryKey val id: Int = 1, // Single row for global state
    @ColumnInfo(name = "continental_rankings") val continentalRankings: String, // JSON Map<String, Int>
    @ColumnInfo(name = "league_reputation") val leagueReputation: String, // JSON Map<String, Double>
    @ColumnInfo(name = "dominant_clubs") val dominantClubs: String, // JSON List<Int>
    @ColumnInfo(name = "rising_clubs") val risingClubs: String, // JSON List<Int>
    @ColumnInfo(name = "fallen_giants") val fallenGiants: String, // JSON List<Int>
    @ColumnInfo(name = "club_rankings") val clubRankings: String = "[]", // JSON List<ClubRanking>
    @ColumnInfo(name = "last_updated_week") val lastUpdatedWeek: Int,
    @ColumnInfo(name = "season") val season: String
)
