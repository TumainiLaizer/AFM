package com.fameafrica.afm.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import com.squareup.moshi.Json

@Entity(
    tableName = "scouting_missions",
    foreignKeys = [
        ForeignKey(
            entity = StaffEntity::class,
            parentColumns = ["id"],
            childColumns = ["scout_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["scout_id"]),
        Index(value = ["status"]),
        Index(value = ["mission_type", "target_identifier"])
    ]
)
data class ScoutingMissionsEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @Json(name = "scout_id")
    @ColumnInfo(name = "scout_id")
    val scoutId: Int,

    @Json(name = "scout_name")
    @ColumnInfo(name = "scout_name")
    val scoutName: String,

    @Json(name = "mission_type")
    @ColumnInfo(name = "mission_type")
    val missionType: String, // REGION, COUNTRY, LEAGUE, DIVISION

    @Json(name = "target_identifier")
    @ColumnInfo(name = "target_identifier")
    val targetIdentifier: String, // e.g. "East Africa", "Tanzania", "Tanzania Premier League"

    @Json(name = "start_date")
    @ColumnInfo(name = "start_date")
    val startDate: Long = System.currentTimeMillis(),

    @Json(name = "end_date")
    @ColumnInfo(name = "end_date")
    val endDate: Long? = null,

    @ColumnInfo(name = "status")
    val status: String = "Active", // Active, Completed, Cancelled

    @ColumnInfo(name = "priority")
    val priority: String = "Normal",

    @Json(name = "found_players_count")
    @ColumnInfo(name = "found_players_count")
    val foundPlayersCount: Int = 0,

    @ColumnInfo(name = "notes")
    val notes: String? = null
)

enum class MissionType(val value: String) {
    REGION("REGION"),
    COUNTRY("COUNTRY"),
    LEAGUE("LEAGUE"),
    DIVISION("DIVISION")
}

enum class MissionStatus(val value: String) {
    ACTIVE("Active"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled")
}
