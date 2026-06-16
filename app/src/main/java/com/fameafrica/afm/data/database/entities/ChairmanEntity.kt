package com.fameafrica.afm.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.squareup.moshi.Json

@Entity(
    tableName = "chairmen",
    indices = [
        Index(value = ["team_id"]),
        Index(value = ["is_available"]),
        Index(value = ["nationality"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = TeamsEntity::class,
            parentColumns = ["id"],
            childColumns = ["team_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class ChairmanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @param:Json(name = "team_id")
    @field:ColumnInfo(name = "team_id")
    val teamId: Int? = null,

    @field:ColumnInfo(name = "name")
    val name: String,

    @field:ColumnInfo(name = "nationality")
    val nationality: String,

    @field:ColumnInfo(name = "age")
    val age: Int,

    @param:Json(name = "wealth_level")
    @field:ColumnInfo(name = "wealth_level")
    val wealthLevel: Int, // 0-100

    @param:Json(name = "patience_level")
    @field:ColumnInfo(name = "patience_level")
    val patienceLevel: Int, // 0-100

    @param:Json(name = "business_skill")
    @field:ColumnInfo(name = "business_skill")
    val businessSkill: Int, // 0-100

    @param:Json(name = "football_knowledge")
    @field:ColumnInfo(name = "football_knowledge")
    val footballKnowledge: Int, // 0-100

    @param:Json(name = "ambition_level")
    @field:ColumnInfo(name = "ambition_level")
    val ambitionLevel: Int, // 0-100

    @param:Json(name = "is_available")
    @field:ColumnInfo(name = "is_available", defaultValue = "1")
    val isAvailable: Boolean = true,

    @param:Json(name = "preferred_region")
    @field:ColumnInfo(name = "preferred_region")
    val preferredRegion: String? = null,

    @param:Json(name = "personality_type")
    @field:ColumnInfo(name = "personality_type")
    val personalityType: String? = null,

    @param:Json(name = "entry_mode")
    @field:ColumnInfo(name = "entry_mode")
    val entryMode: String? = null,

    @param:Json(name = "fan_trust")
    @field:ColumnInfo(name = "fan_trust", defaultValue = "50")
    val fanTrust: Int = 50,

    @param:Json(name = "board_pressure")
    @field:ColumnInfo(name = "board_pressure", defaultValue = "50")
    val boardPressure: Int = 50,

    @param:Json(name = "club_vision_id")
    @field:ColumnInfo(name = "club_vision_id")
    val clubVisionId: Int? = null,

    @param:Json(name = "total_investment")
    @field:ColumnInfo(name = "total_investment")
    val totalInvestment: Long = 0,

    @param:Json(name = "hire_date")
    @field:ColumnInfo(name = "hire_date")
    val hireDate: Long? = null
)
