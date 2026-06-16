package com.fameafrica.afm.data.database.entities

import androidx.room.*
import com.squareup.moshi.Json

@Entity(
    tableName = "player_loans",
    foreignKeys = [
        ForeignKey(
            entity = PlayersEntity::class,
            parentColumns = ["id"],
            childColumns = ["player_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TeamsEntity::class,
            parentColumns = ["id"],
            childColumns = ["loaning_team_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TeamsEntity::class,
            parentColumns = ["id"],
            childColumns = ["receiving_team_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["player_id"]),
        Index(value = ["loaning_team_id"]),
        Index(value = ["receiving_team_id"]),
        Index(value = ["player_name"]),
        Index(value = ["loaning_team"]),
        Index(value = ["receiving_team"]),
        Index(value = ["status"]),
        Index(value = ["start_date"]),
        Index(value = ["end_date"]),
        Index(value = ["option_to_buy"]),
        Index(value = ["season"])
    ]
)
data class PlayerLoansEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @Json(name = "player_id")
    @ColumnInfo(name = "player_id")
    val playerId: Int,

    @Json(name = "player_name")
    @ColumnInfo(name = "player_name")
    val playerName: String,

    @Json(name = "loaning_team_id")
    @ColumnInfo(name = "loaning_team_id")
    val loaningTeamId: Int,

    @Json(name = "loaning_team")
    @ColumnInfo(name = "loaning_team")
    val loaningTeam: String,  // Owner club

    @Json(name = "receiving_team_id")
    @ColumnInfo(name = "receiving_team_id")
    val receivingTeamId: Int,

    @Json(name = "receiving_team")
    @ColumnInfo(name = "receiving_team")
    val receivingTeam: String,  // Loanee club

    @ColumnInfo(name = "season")
    val season: String,

    @Json(name = "start_date")
    @ColumnInfo(name = "start_date")
    val startDate: String,

    @Json(name = "end_date")
    @ColumnInfo(name = "end_date")
    val endDate: String,

    @ColumnInfo(name = "duration", defaultValue = "6")
    val duration: Int = 6,  // In months

    @Json(name = "loan_fee")
    @ColumnInfo(name = "loan_fee")
    val loanFee: Int? = null,

    @Json(name = "wage_contribution")
    @ColumnInfo(name = "wage_contribution")
    val wageContribution: Int = 100,  // Percentage (50-100)

    @Json(name = "option_to_buy")
    @ColumnInfo(name = "option_to_buy", defaultValue = "0")
    val optionToBuy: Boolean = false,

    @Json(name = "buy_option_fee")
    @ColumnInfo(name = "buy_option_fee")
    val buyOptionFee: Int? = null,

    @Json(name = "mandatory_buy")
    @ColumnInfo(name = "mandatory_buy")
    val mandatoryBuy: Boolean = false,

    @Json(name = "mandatory_buy_fee")
    @ColumnInfo(name = "mandatory_buy_fee")
    val mandatoryBuyFee: Int? = null,

    @Json(name = "games_played")
    @ColumnInfo(name = "games_played")
    val gamesPlayed: Int = 0,

    @Json(name = "goals_scored")
    @ColumnInfo(name = "goals_scored")
    val goalsScored: Int = 0,

    @Json(name = "assists_made")
    @ColumnInfo(name = "assists_made")
    val assistsMade: Int = 0,

    @ColumnInfo(name = "status", defaultValue = "Active")
    val status: String = "Active",  // Active, Completed, EarlyReturn, BuyOptionTriggered

    @Json(name = "recall_option")
    @ColumnInfo(name = "recall_option")
    val recallOption: Boolean = false,

    @Json(name = "recall_date")
    @ColumnInfo(name = "recall_date")
    val recallDate: String? = null,

    @ColumnInfo(name = "notes")
    val notes: String? = null
) {

    // ============ COMPUTED PROPERTIES ============

    @get:Ignore
    val isActive: Boolean
        get() = status == "Active"

    @get:Ignore
    val isCompleted: Boolean
        get() = status == "Completed"

    @get:Ignore
    val loanFeeInMillions: Double
        get() = (loanFee ?: 0) / 1_000_000.0

    @get:Ignore
    val buyOptionInMillions: Double
        get() = (buyOptionFee ?: 0) / 1_000_000.0

    @get:Ignore
    val loanSummary: String
        get() = "$playerName: $loaningTeam → $receivingTeam (${duration} months)"

    @get:Ignore
    val monthsRemaining: Int
        get() {
            // Calculate based on end_date
            return duration
        }
}

// ============ ENUMS ============

enum class LoanStatus(val value: String) {
    ACTIVE("Active"),
    PENDING("Pending"),
    COMPLETED("Completed"),
    EARLY_RETURN("EarlyReturn"),
    BUY_OPTION_TRIGGERED("BuyOptionTriggered")
}
