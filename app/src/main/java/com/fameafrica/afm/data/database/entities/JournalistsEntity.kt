package com.fameafrica.afm.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import com.squareup.moshi.Json

@Entity(
    tableName = "journalists",
    indices = [
        Index(value = ["name"], unique = true),
        Index(value = ["personality"]),
        Index(value = ["media_company"])
    ]
)
data class JournalistsEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "name", defaultValue = "Azam Sports")
    val name: String = "Azam Sports",

    @Json(name = "media_company")
    @ColumnInfo(name = "media_company", defaultValue = "Azam Media")
    val mediaCompany: String = "Azam Media",

    @ColumnInfo(name = "expertise", defaultValue = "Match Reporting")
    val expertise: String = "Match Reporting",

    @ColumnInfo(name = "logo")
    val logo: String? = null,

    @ColumnInfo(name = "personality", defaultValue = "Neutral")
    val personality: String = "Neutral"  // Friendly, Neutral, Hostile, Sensationalist, Analyst
) {

    // ============ COMPUTED PROPERTIES ============

    val isFriendly: Boolean
        get() = personality == "Friendly"

    val isHostile: Boolean
        get() = personality == "Hostile"

    val isSensationalist: Boolean
        get() = personality == "Sensationalist"

    val isAnalyst: Boolean
        get() = personality == "Analyst"

    val personalityColor: String
        get() = when (personality) {
            "Friendly" -> "Green"
            "Neutral" -> "Blue"
            "Hostile" -> "Red"
            "Sensationalist" -> "Orange"
            "Analyst" -> "Purple"
            else -> "Gray"
        }
}

// ============ ENUMS ============

enum class JournalistPersonality(val value: String) {
    FRIENDLY("Friendly"),
    NEUTRAL("Neutral"),
    HOSTILE("Hostile"),
    SENSATIONALIST("Sensationalist"),
    ANALYST("Analyst")
}

enum class MediaCompany(val value: String) {
    AZAM_MEDIA("Azam Media"),
    CITIZEN_TV("Citizen TV"),
    KBC("Kenya Broadcasting Corporation"),
    GBC("Ghana Broadcasting Corporation"),
    NTA("Nigerian Television Authority"),
    SABC("South African Broadcasting Corporation"),
    SUPERSPORT("SuperSport"),
    BBC_AFRICA("BBC Africa"),
    CAF_MEDIA("CAF Media"),
    FRANCE24_AFRIQUE("France24 Afrique")
}

enum class JournalistExpertise(val value: String) {
    MATCH_REPORTING("Match Reporting"),
    TRANSFER_NEWS("Transfer News"),
    TACTICAL_ANALYSIS("Tactical Analysis"),
    INVESTIGATIVE("Investigative"),
    INTERVIEWS("Interviews"),
    OPINION("Opinion"),
    BREAKING_NEWS("Breaking News")
}