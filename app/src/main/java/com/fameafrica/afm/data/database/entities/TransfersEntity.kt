package com.fameafrica.afm.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import com.squareup.moshi.Json

enum class SquadRole(val value: String) {
    STAR_PLAYER("Star Player"),
    FIRST_TEAM("First Team"),
    ROTATION("Rotation"),
    PROSPECT("Prospect"),
    BACKUP("Backup")
}

@Entity(
    tableName = "transfers",
    foreignKeys = [
        ForeignKey(
            entity = TeamsEntity::class,
            parentColumns = ["id"],
            childColumns = ["current_team_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TeamsEntity::class,
            parentColumns = ["id"],
            childColumns = ["target_team_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PlayersEntity::class,
            parentColumns = ["id"],
            childColumns = ["player_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["player_id"]),
        Index(value = ["current_team_id"]),
        Index(value = ["target_team_id"]),
        Index(value = ["transfer_status"]),
        Index(value = ["transfer_type"]),
        Index(value = ["timestamp"]),
        Index(value = ["scout_rating"]),
        Index(value = ["window_id"])
    ]
)
data class TransfersEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @Json(name = "player_id")
    @ColumnInfo(name = "player_id")
    val playerId: Int,

    @Json(name = "player_name")
    @ColumnInfo(name = "player_name")
    val playerName: String,

    @Json(name = "current_team_id")
    @ColumnInfo(name = "current_team_id")
    val currentTeamId: Int,

    @Json(name = "current_team")
    @ColumnInfo(name = "current_team")
    val currentTeam: String,

    @Json(name = "target_team_id")
    @ColumnInfo(name = "target_team_id")
    val targetTeamId: Int,

    @Json(name = "target_team")
    @ColumnInfo(name = "target_team")
    val targetTeam: String,

    @Json(name = "transfer_fee")
    @ColumnInfo(name = "transfer_fee")
    val transferFee: Long,

    @Json(name = "contract_length")
    @ColumnInfo(name = "contract_length")
    val contractLength: Int = 3,

    @Json(name = "monthly_wage")
    @ColumnInfo(name = "monthly_wage")
    val monthlyWage: Long,

    @Json(name = "transfer_type")
    @ColumnInfo(name = "transfer_type")
    val transferType: String,

    @Json(name = "transfer_status")
    @ColumnInfo(name = "transfer_status")
    val transferStatus: String = "Pending",

    @ColumnInfo(name = "rumours")
    val rumours: String? = null,

    @Json(name = "scout_rating")
    @ColumnInfo(name = "scout_rating")
    val scoutRating: Int = 70,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis(),

    @Json(name = "window_id")
    @ColumnInfo(name = "window_id")
    val windowId: Int? = null,

    // Club Negotiation Fields
    @ColumnInfo(name = "installments")
    val installments: Int = 0, // Number of years to pay
    
    @Json(name = "sell_on_percentage")
    @ColumnInfo(name = "sell_on_percentage")
    val sellOnPercentage: Int = 0,
    
    @Json(name = "goal_bonus_fee")
    @ColumnInfo(name = "goal_bonus_fee")
    val goalBonusFee: Long = 0,
    
    @Json(name = "appearance_bonus_fee")
    @ColumnInfo(name = "appearance_bonus_fee")
    val appearanceBonusFee: Long = 0,
    
    @Json(name = "trophy_bonus_fee")
    @ColumnInfo(name = "trophy_bonus_fee")
    val trophyBonusFee: Long = 0,

    // Player Negotiation Fields
    @Json(name = "signing_bonus")
    @ColumnInfo(name = "signing_bonus")
    val signingBonus: Long = 0,
    
    @Json(name = "squad_role")
    @ColumnInfo(name = "squad_role")
    val squadRole: String = SquadRole.ROTATION.value,
    
    @Json(name = "is_loan_to_buy")
    @ColumnInfo(name = "is_loan_to_buy")
    val isLoanToBuy: Boolean = false,
    
    @Json(name = "loan_buy_fee")
    @ColumnInfo(name = "loan_buy_fee")
    val loanBuyFee: Long? = null,
    
    @Json(name = "agent_fee")
    @ColumnInfo(name = "agent_fee")
    val agentFee: Long? = null,

    @Json(name = "relegation_release_clause")
    @ColumnInfo(name = "relegation_release_clause")
    val relegationReleaseClause: Boolean = false,

    @Json(name = "minimum_fee_release_clause")
    @ColumnInfo(name = "minimum_fee_release_clause")
    val minimumFeeReleaseClause: Long? = null,

    @Json(name = "completed_date")
    @ColumnInfo(name = "completed_date")
    val completedDate: String? = null,

    @Json(name = "last_action_date")
    @ColumnInfo(name = "last_action_date")
    val lastActionDate: Long = System.currentTimeMillis(),

    @Json(name = "is_user_offer")
    @ColumnInfo(name = "is_user_offer")
    val isUserOffer: Boolean = true
) {
    val isPending: Boolean get() = transferStatus == "Pending"
    val isNegotiating: Boolean get() = transferStatus == "Negotiating"
    val isAccepted: Boolean get() = transferStatus == "Accepted"
    val isRejected: Boolean get() = transferStatus == "Rejected"
    val isCompleted: Boolean get() = transferStatus == "Completed"
}
