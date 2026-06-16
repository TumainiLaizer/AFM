package com.fameafrica.afm.data.database.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import com.fameafrica.afm.data.database.entities.PlayersEntity

data class SquadAnalysis(
    @ColumnInfo(name = "position_category") val positionCategory: String,
    @ColumnInfo(name = "player_count") val playerCount: Int,
    @ColumnInfo(name = "avg_rating") val avgRating: Double?,
    @ColumnInfo(name = "avg_age") val avgAge: Double?,
    @ColumnInfo(name = "avg_height") val avgHeight: Double?,
    @ColumnInfo(name = "avg_value") val avgValue: Double?
)

data class NationalityDistribution(
    val nationality: String,
    @ColumnInfo(name = "player_count") val playerCount: Int,
    @ColumnInfo(name = "avg_rating") val avgRating: Double?
)

data class ArchetypeDistribution(
    val archetype: String?,
    @ColumnInfo(name = "player_count") val playerCount: Int,
    @ColumnInfo(name = "avg_rating") val avgRating: Double?
)

data class PersonalityDistribution(
    @ColumnInfo(name = "personality_type") val personalityType: String?,
    @ColumnInfo(name = "player_count") val playerCount: Int,
    @ColumnInfo(name = "avg_rating") val avgRating: Double?
)

data class TraitDistribution(
    @ColumnInfo(name = "primary_trait") val primaryTrait: String?,
    @ColumnInfo(name = "player_count") val playerCount: Int
)

data class PlayerWithDetails(
    @Embedded val player: PlayersEntity,
    @ColumnInfo(name = "detail_team_name") val teamName: String?,
    @ColumnInfo(name = "detail_team_logo") val teamLogo: String?,
    @ColumnInfo(name = "detail_team_league") val teamLeague: String?,
    @ColumnInfo(name = "detail_team_reputation") val teamReputation: Int?
)

data class PlayerWithTeamDetails(
    @Embedded val player: PlayersEntity,
    @ColumnInfo(name = "detail_team_name") val teamName: String?,
    @ColumnInfo(name = "detail_team_logo") val teamLogo: String?
)

data class PlayerSummary(
    val id: Int,
    val name: String,
    val age: Int,
    val position: String,
    @ColumnInfo(name = "position_category") val positionCategory: String,
    val nationality: String,
    @ColumnInfo(name = "team_name") val teamName: String,
    @ColumnInfo(name = "team_id") val teamId: Int?,
    val rating: Int,
    val potential: Int,
    @ColumnInfo(name = "market_value") val marketValue: Int,
    val salary: Double,
    val morale: Int,
    @ColumnInfo(name = "current_form") val currentForm: Int,
    @ColumnInfo(name = "injury_status") val injuryStatus: String,
    val suspended: Boolean,
    val retired: Boolean,
    @ColumnInfo(name = "free_agent") val freeAgent: Boolean,
    @ColumnInfo(name = "transfer_list_status") val transferListStatus: String,
    @ColumnInfo(name = "personality_type") val personalityType: String,
    val pace: Int,
    val acceleration: Int,
    val stamina: Int,
    val passing: Int,
    val vision: Int,
    val finishing: Int,
    val defending: Int,
    val strength: Int,
    val dribbling: Int,
    val crossing: Int,
    val heading: Int,
    val creativity: Int,
    val skill: Int,
    val agility: Int,
    val positioning: Int,
    val anticipation: Int,
    val decisions: Int,
    val teamwork: Int,
    val composure: Int,
    val aggression: Int,
    @ColumnInfo(name = "injury_risk") val injuryRisk: Int,
    val region: String?,
    @ColumnInfo(name = "contract_expiry") val contractExpiry: String?
) {
    val isTransferListed: Boolean get() = transferListStatus == "AVAILABLE"
    val isLoanListed: Boolean get() = transferListStatus == "LOAN_LISTED"
    val isInjured: Boolean get() = injuryStatus != "HEALTHY"
    val isAvailable: Boolean get() = !isInjured && !suspended && !retired

    val contractExpiryWeek: Int
        get() {
            return try {
                val expiryStr = contractExpiry ?: return 1000 // Future
                val format = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                val expiryDate = format.parse(expiryStr) ?: return 1000
                val startDate = java.util.Calendar.getInstance().apply {
                    set(2025, java.util.Calendar.JUNE, 1, 0, 0, 0)
                }.time
                val diff = expiryDate.time - startDate.time
                (diff / (7L * 24 * 60 * 60 * 1000)).toInt()
            } catch (e: Exception) {
                1000
            }
        }
}
