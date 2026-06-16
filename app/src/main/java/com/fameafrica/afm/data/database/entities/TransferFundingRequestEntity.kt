package com.fameafrica.afm.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import com.squareup.moshi.Json

@Entity(
    tableName = "transfer_funding_requests",
    foreignKeys = [
        ForeignKey(
            entity = SponsorsEntity::class,
            parentColumns = ["id"],
            childColumns = ["sponsor_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TeamsEntity::class,
            parentColumns = ["id"],
            childColumns = ["team_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PlayersEntity::class,
            parentColumns = ["id"],
            childColumns = ["player_id"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["sponsor_id"]),
        Index(value = ["team_id"]),
        Index(value = ["player_id"]),
        Index(value = ["status"]),
        Index(value = ["request_date"])
    ]
)
data class TransferFundingRequestEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @Json(name = "sponsor_id")
    @ColumnInfo(name = "sponsor_id")
    val sponsorId: Int,

    @Json(name = "sponsor_name")
    @ColumnInfo(name = "sponsor_name")
    val sponsorName: String,

    @Json(name = "team_id")
    @ColumnInfo(name = "team_id")
    val teamId: Int,

    @Json(name = "team_name")
    @ColumnInfo(name = "team_name")
    val teamName: String,

    @Json(name = "player_id")
    @ColumnInfo(name = "player_id")
    val playerId: Int? = null,

    @Json(name = "player_name")
    @ColumnInfo(name = "player_name")
    val playerName: String? = null,

    @Json(name = "requested_amount")
    @ColumnInfo(name = "requested_amount")
    val requestedAmount: Long,

    @Json(name = "approved_amount")
    @ColumnInfo(name = "approved_amount")
    val approvedAmount: Long? = null,

    @Json(name = "request_date")
    @ColumnInfo(name = "request_date")
    val requestDate: String,

    @Json(name = "decision_date")
    @ColumnInfo(name = "decision_date")
    val decisionDate: String? = null,

    @ColumnInfo(name = "status")
    val status: String,  // PENDING, APPROVED, REJECTED, PARTIALLY_APPROVED

    @Json(name = "requires_objectives_check")
    @ColumnInfo(name = "requires_objectives_check")
    val requiresObjectivesCheck: Boolean = false,

    @Json(name = "objectives_met")
    @ColumnInfo(name = "objectives_met")
    val objectivesMet: Boolean? = null,

    @Json(name = "team_performance_rating")
    @ColumnInfo(name = "team_performance_rating")
    val teamPerformanceRating: Int? = null,

    @Json(name = "is_foreign_player")
    @ColumnInfo(name = "is_foreign_player")
    val isForeignPlayer: Boolean = false,

    @Json(name = "player_nationality")
    @ColumnInfo(name = "player_nationality")
    val playerNationality: String? = null,

    @ColumnInfo(name = "notes")
    val notes: String? = null
) {

    val isPending: Boolean
        get() = status == "PENDING"

    val isApproved: Boolean
        get() = status == "APPROVED"

    val isRejected: Boolean
        get() = status == "REJECTED"

    val isPartiallyApproved: Boolean
        get() = status == "PARTIALLY_APPROVED"

    val requestedAmountInMillions: Double
        get() = requestedAmount / 1_000_000.0

    val approvedAmountInMillions: Double?
        get() = approvedAmount?.let { it / 1_000_000.0 }
}

enum class FundingRequestStatus(val value: String) {
    PENDING("PENDING"),
    APPROVED("APPROVED"),
    REJECTED("REJECTED"),
    PARTIALLY_APPROVED("PARTIALLY_APPROVED")
}