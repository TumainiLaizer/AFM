package com.fameafrica.afm.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import com.squareup.moshi.Json

@Entity(
    tableName = "nationalities",
    indices = [
        Index(value = ["nationality"], unique = true),
        Index(value = ["fifa_code"], unique = true),
        Index(value = ["flag_path"], unique = true),
        Index(value = ["confederation"]),
        Index(value = ["region"]),
        Index(value = ["is_african"])
    ]
)
data class NationalitiesEntity(
    @PrimaryKey(autoGenerate = false)  // Manual ID from your data
    @field:ColumnInfo(name = "id")
    val id: Int,

    @field:ColumnInfo(name = "nationality")
    val nationality: String,

    @param:Json(name = "fifa_code")
    @field:ColumnInfo(name = "fifa_code")
    val fifaCode: String,

    @param:Json(name = "flag_path")
    @field:ColumnInfo(name = "flag_path")
    val flagPath: String?,

    @field:ColumnInfo(name = "confederation")
    val confederation: String? = null,  // CAF, UEFA, CONMEBOL, etc.

    @field:ColumnInfo(name = "region")
    val region: String? = null,  // West Africa, East Africa, Southern Africa, etc.

    @param:Json(name = "is_african")
    @field:ColumnInfo(name = "is_african")
    val isAfrican: Boolean = false,

    @field:ColumnInfo(name = "population")
    val population: Long? = null,

    @param:Json(name = "capital_city")
    @field:ColumnInfo(name = "capital_city")
    val capitalCity: String? = null,

    @field:ColumnInfo(name = "currency")
    val currency: String? = null,

    @field:ColumnInfo(name = "language")
    val language: String? = null,

    @param:Json(name = "caf_zone")
    @field:ColumnInfo(name = "caf_zone", defaultValue = "CECAFA")
    val cafZone: String = "CECAFA", // UNAF, WAFU, UNIFFAC, CECAFA, COSAFA

    @param:Json(name = "reputation_stars")
    @field:ColumnInfo(name = "reputation_stars", defaultValue = "1")
    val reputationStars: Int = 1,

    @field:ColumnInfo(name = "latitude")
    val latitude: Double? = null,

    @field:ColumnInfo(name = "longitude")
    val longitude: Double? = null
) {

    // ============ COMPUTED PROPERTIES ============

    val displayName: String
        get() = nationality

    val flagUrl: String
        get() = flagPath ?: "flags/default.png"

    val isCAFMember: Boolean
        get() = confederation == "CAF"

    val isUEFAMember: Boolean
        get() = confederation == "UEFA"

    val isCONMEBOLMember: Boolean
        get() = confederation == "CONMEBOL"

    val isCONCACAFMember: Boolean
        get() = confederation == "CONCACAF"

    val isAFCMember: Boolean
        get() = confederation == "AFC"

    val isOFCMember: Boolean
        get() = confederation == "OFC"
}

// ============ ENUMS ============

enum class Confederation(val value: String) {
    CAF("CAF"),
    UEFA("UEFA"),
    CONMEBOL("CONMEBOL"),
    CONCACAF("CONCACAF"),
    AFC("AFC"),
    OFC("OFC")
}
