package com.fameafrica.afm.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import com.squareup.moshi.Json

@Entity(
    tableName = "settings_history",
    foreignKeys = [
        ForeignKey(
            entity = GameSettingsEntity::class,
            parentColumns = ["id"],
            childColumns = ["settings_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["settings_id"]),
        Index(value = ["changed_at"])
    ]
)
data class SettingsHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @Json(name = "settings_id")
    @ColumnInfo(name = "settings_id")
    val settingsId: Int,

    @Json(name = "changed_field")
    @ColumnInfo(name = "changed_field")
    val changedField: String,

    @Json(name = "old_value")
    @ColumnInfo(name = "old_value")
    val oldValue: String? = null,

    @Json(name = "new_value")
    @ColumnInfo(name = "new_value")
    val newValue: String? = null,

    @Json(name = "changed_by")
    @ColumnInfo(name = "changed_by", defaultValue = "user")
    val changedBy: String = "user",

    @Json(name = "changed_at")
    @ColumnInfo(name = "changed_at")
    val changedAt: Long,

    @Json(name = "created_at")
    @ColumnInfo(name = "created_at", defaultValue = "CURRENT_TIMESTAMP")
    val createdAt: String? = null
) {

    val changeSummary: String
        get() = "$changedField: ${oldValue ?: "null"} → ${newValue ?: "null"}"
}