package com.fameafrica.afm.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import com.squareup.moshi.Json

@Entity(
    tableName = "player_agents",
    indices = [
        Index(value = ["agent_name"]),
        Index(value = ["agency"]),
        Index(value = ["reputation"]),
        Index(value = ["specialization"]),
        Index(value = ["personality"]),
        Index(value = ["email"], unique = true)
    ]
)
data class PlayerAgentsEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @Json(name = "agent_name")
    @ColumnInfo(name = "agent_name")
    val agentName: String,

    @ColumnInfo(name = "agency")
    val agency: String? = null,

    @Json(name = "negotiation_power")
    @ColumnInfo(name = "negotiation_power", defaultValue = "50")
    val negotiationPower: Int = 50,

    @Json(name = "commission_rate")
    @ColumnInfo(name = "commission_rate", defaultValue = "10")
    val commissionRate: Int = 10,

    @ColumnInfo(name = "reputation", defaultValue = "50")
    val reputation: Int = 50,

    @ColumnInfo(name = "nationality")
    val nationality: String? = null,

    @ColumnInfo(name = "languages")
    val languages: String? = null, // Comma-separated (e.g. "English,Swahili,French")

    @ColumnInfo(name = "specialization")
    val specialization: String? = null, // YOUNG_TALENT, STARS, LOCAL, INTERNATIONAL, ALL_ROUNDER

    @ColumnInfo(name = "personality", defaultValue = "BALANCED")
    val personality: String = "BALANCED", // GREEDY, LOYAL, AGGRESSIVE, DEVELOPMENT_FOCUSED, BALANCED

    @Json(name = "years_experience")
    @ColumnInfo(name = "years_experience", defaultValue = "0")
    val yearsExperience: Int = 0,

    @Json(name = "active_clients")
    @ColumnInfo(name = "active_clients", defaultValue = "0")
    val activeClients: Int = 0,

    @Json(name = "successful_deals")
    @ColumnInfo(name = "successful_deals", defaultValue = "0")
    val successfulDeals: Int = 0,

    @Json(name = "total_deal_value")
    @ColumnInfo(name = "total_deal_value", defaultValue = "0")
    val totalDealValue: Long = 0L,

    @ColumnInfo(name = "email")
    val email: String? = null,

    @ColumnInfo(name = "phone")
    val phone: String? = null,

    @Json(name = "photo_url")
    @ColumnInfo(name = "photo_url")
    val photoUrl: String? = null,

    @Json(name = "created_at")
    @ColumnInfo(name = "created_at", defaultValue = "CURRENT_TIMESTAMP")
    val createdAt: String = "",

    @Json(name = "updated_at")
    @ColumnInfo(name = "updated_at", defaultValue = "CURRENT_TIMESTAMP")
    val updatedAt: String = ""
) {
    // ============ COMPUTED PROPERTIES ============

    val negotiationLevel: String
        get() = when {
            negotiationPower >= 90 -> "Legendary Negotiator"
            negotiationPower >= 80 -> "Elite Agent"
            negotiationPower >= 70 -> "Experienced"
            negotiationPower >= 60 -> "Competent"
            else -> "Average"
        }

    val reputationLevel: String
        get() = when {
            reputation >= 90 -> "World Class"
            reputation >= 80 -> "Respected"
            reputation >= 70 -> "Well-Known"
            reputation >= 60 -> "Emerging"
            else -> "Local"
        }

    val isGreedy: Boolean get() = personality == "GREEDY"
    val isLoyal: Boolean get() = personality == "LOYAL"
    val isAggressive: Boolean get() = personality == "AGGRESSIVE"
    val isDevelopmentFocused: Boolean get() = personality == "DEVELOPMENT_FOCUSED"

    val isSuperAgent: Boolean
        get() = reputation >= 85 && activeClients >= 10

    val languagesList: List<String>
        get() = languages?.split(",")?.map { it.trim() } ?: emptyList()
}

enum class AgentSpecialization(val value: String) {
    YOUNG_TALENT("YOUNG_TALENT"),
    STARS("STARS"),
    LOCAL("LOCAL"),
    INTERNATIONAL("INTERNATIONAL"),
    ALL_ROUNDER("ALL_ROUNDER")
}

enum class AgentPersonality(val value: String) {
    GREEDY("GREEDY"),
    LOYAL("LOYAL"),
    AGGRESSIVE("AGGRESSIVE"),
    DEVELOPMENT_FOCUSED("DEVELOPMENT_FOCUSED"),
    BALANCED("BALANCED")
}
