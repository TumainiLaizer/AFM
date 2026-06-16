package com.fameafrica.afm.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import com.squareup.moshi.Json

@Entity(
    tableName = "user_analytics",
    indices = [
        Index(value = ["event_type", "created_at"]),
        Index(value = ["user_id"]),
        Index(value = ["session_id"])
    ]
)
data class UserAnalyticsEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @Json(name = "event_type")
    @ColumnInfo(name = "event_type")
    val eventType: String,  // GAME_START, SETTINGS_CHANGE, MATCH_PLAYED, TRANSFER_MADE, etc.

    @Json(name = "event_data")
    @ColumnInfo(name = "event_data")
    val eventData: String? = null,  // JSON data

    @Json(name = "user_id")
    @ColumnInfo(name = "user_id")
    val userId: String? = null,  // Anonymous user ID

    @Json(name = "session_id")
    @ColumnInfo(name = "session_id")
    val sessionId: String? = null,

    @Json(name = "created_at")
    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    @Json(name = "device_info")
    @ColumnInfo(name = "device_info")
    val deviceInfo: String? = null,

    @Json(name = "app_version")
    @ColumnInfo(name = "app_version")
    val appVersion: String? = null,

    @Json(name = "country_code")
    @ColumnInfo(name = "country_code")
    val countryCode: String? = null
)