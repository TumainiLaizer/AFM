package com.fameafrica.afm.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import com.squareup.moshi.Json

@Entity(
    tableName = "archetype_traits",
    indices = [
        Index(value = ["archetype_name"], unique = true)
    ]
)
data class ArchetypeTraitsEntity(
    @PrimaryKey(autoGenerate = true)
    @field:ColumnInfo(name = "id")
    val id: Int = 0,

    @param:Json(name = "archetype_name")
    @field:ColumnInfo(name = "archetype_name")
    val archetypeName: String,

    @param:Json(name = "primary_trait")
    @field:ColumnInfo(name = "primary_trait")
    val primaryTrait: String,

    @param:Json(name = "secondary_trait")
    @field:ColumnInfo(name = "secondary_trait")
    val secondaryTrait: String?,

    @param:Json(name = "gameplay_focus")
    @field:ColumnInfo(name = "gameplay_focus")
    val gameplayFocus: String,

    @param:Json(name = "attribute_boost")
    @field:ColumnInfo(name = "attribute_boost")  // JSON with attribute boosts
    val attributeBoost: String?,

    @field:ColumnInfo(name = "description")
    val description: String?
) {

    // ============ COMPUTED PROPERTIES ============

    val fullName: String
        get() = archetypeName.replace('_', ' ').split(' ').joinToString(" ") {
            it.lowercase().replaceFirstChar { char -> char.uppercase() }
        }

    val focusAreas: List<String>
        get() = gameplayFocus.split(',').map { it.trim() }

    val traitCombination: String
        get() = if (secondaryTrait != null) {
            "$primaryTrait + $secondaryTrait"
        } else {
            primaryTrait
        }
}
