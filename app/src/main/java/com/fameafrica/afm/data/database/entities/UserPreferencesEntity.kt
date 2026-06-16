package com.fameafrica.afm.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import com.squareup.moshi.Json

@Entity(
    tableName = "user_preferences",
    indices = [
        Index(value = ["preference_key"], unique = true)
    ]
)
data class UserPreferencesEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @Json(name = "preference_key")
    @ColumnInfo(name = "preference_key")
    val preferenceKey: String,

    @Json(name = "preference_value")
    @ColumnInfo(name = "preference_value")
    val preferenceValue: String,

    @Json(name = "preference_type")
    @ColumnInfo(name = "preference_type", defaultValue = "string")
    val preferenceType: String = "string",  // string, boolean, integer, float

    @Json(name = "created_at")
    @ColumnInfo(name = "created_at", defaultValue = "CURRENT_TIMESTAMP")
    val createdAt: String? = null,

    @Json(name = "updated_at")
    @ColumnInfo(name = "updated_at", defaultValue = "CURRENT_TIMESTAMP")
    val updatedAt: String? = null
)