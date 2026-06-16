package com.fameafrica.afm.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import com.squareup.moshi.Json

@Entity(
    tableName = "personality_types",
    indices = [
        Index(value = ["name"], unique = true)
    ]
)
data class PersonalityTypesEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "name")
    val name: String, // e.g. PROFESSIONAL, AGGRESSIVE

    @ColumnInfo(name = "description")
    val description: String,

    @Json(name = "positive_effects")
    @ColumnInfo(name = "positive_effects")
    val positiveEffects: String = "", // Comma-separated list

    @Json(name = "negative_effects")
    @ColumnInfo(name = "negative_effects")
    val negativeEffects: String = "", // Comma-separated list

    @Json(name = "morale_effect")
    @ColumnInfo(name = "morale_effect", defaultValue = "1.0")
    val moraleEffect: Double = 1.0,

    @Json(name = "form_consistency")
    @ColumnInfo(name = "form_consistency", defaultValue = "1.0")
    val formConsistency: Double = 1.0,

    @Json(name = "icon_name")
    @ColumnInfo(name = "icon_name")
    val iconName: String? = null
)
