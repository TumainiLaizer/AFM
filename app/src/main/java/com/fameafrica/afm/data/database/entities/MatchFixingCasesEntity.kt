package com.fameafrica.afm.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "match_fixing_cases",
    foreignKeys = [
        ForeignKey(
            entity = TeamsEntity::class,
            parentColumns = ["id"],
            childColumns = ["team_involved_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ManagersEntity::class,
            parentColumns = ["id"],
            childColumns = ["manager_id"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["team_involved_id"]),
        Index(value = ["manager_id"]),
        Index(value = ["status"]),
        Index(value = ["league_level"]),
        Index(value = ["is_investigation_complete"])
    ]
)
data class MatchFixingCasesEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "team_involved_id")
    val teamInvolvedId: Int,

    @ColumnInfo(name = "team_involved")
    val teamInvolved: String,

    @ColumnInfo(name = "manager_id")
    val managerId: Int?,

    @ColumnInfo(name = "manager_name")
    val managerName: String, // Kept for display purposes if manager is deleted

    @ColumnInfo(name = "league_name")
    val leagueName: String,

    @ColumnInfo(name = "league_level")
    val leagueLevel: Int,  // Only 4 or 5 (lower leagues)

    @ColumnInfo(name = "season")
    val season: String,

    @ColumnInfo(name = "allegation_date")
    val allegationDate: String,

    @ColumnInfo(name = "allegation_details")
    val allegationDetails: String,

    @ColumnInfo(name = "evidence_description")
    val evidenceDescription: String?,

    @ColumnInfo(name = "investigation_findings")
    val investigationFindings: String = "",

    @ColumnInfo(name = "status", defaultValue = "Investigating")
    val status: String = "Investigating",  // Investigating, Proven, Not Proven, Closed

    @ColumnInfo(name = "verdict")
    val verdict: String? = null,  // Guilty, Not Guilty, Insufficient Evidence

    @ColumnInfo(name = "punishment")
    val punishment: String? = null,  // Points Deduction, Fine, Relegation, Ban

    @ColumnInfo(name = "points_deducted")
    val pointsDeducted: Int? = null,

    @ColumnInfo(name = "fine_amount")
    val fineAmount: Int? = null,

    @ColumnInfo(name = "manager_banned")
    val managerBanned: Boolean = false,

    @ColumnInfo(name = "manager_ban_duration")
    val managerBanDuration: String? = null,  // 6 months, 1 year, Lifetime

    @ColumnInfo(name = "is_investigation_complete")
    val isInvestigationComplete: Boolean = false,

    @ColumnInfo(name = "resolution_date")
    val resolutionDate: String? = null
) {

    // ============ COMPUTED PROPERTIES ============

    val isInvestigating: Boolean
        get() = status == "Investigating"

    val isProven: Boolean
        get() = status == "Proven"

    val isNotProven: Boolean
        get() = status == "Not Proven"

    val isClosed: Boolean
        get() = status == "Closed"

    val verdictColor: String
        get() = when (verdict) {
            "Guilty" -> "Red"
            "Not Guilty" -> "Green"
            else -> "Yellow"
        }

    val severityLevel: String
        get() = when {
            pointsDeducted != null && pointsDeducted >= 10 -> "Severe"
            pointsDeducted != null && pointsDeducted >= 5 -> "Moderate"
            pointsDeducted != null -> "Minor"
            fineAmount != null && fineAmount >= 1000000 -> "Severe"
            fineAmount != null && fineAmount >= 500000 -> "Moderate"
            fineAmount != null -> "Minor"
            managerBanned -> "Severe"
            else -> "Minor"
        }
}

// ============ ENUMS ============

enum class MatchFixingStatus(val value: String) {
    INVESTIGATING("Investigating"),
    PROVEN("Proven"),
    NOT_PROVEN("Not Proven"),
    CLOSED("Closed")
}

enum class VerdictType(val value: String) {
    GUILTY("Guilty"),
    NOT_GUILTY("Not Guilty"),
    INSUFFICIENT_EVIDENCE("Insufficient Evidence")
}

enum class PunishmentType(val value: String) {
    POINTS_DEDUCTION("Points Deduction"),
    FINE("Fine"),
    RELEGATION("Relegation"),
    MANAGER_BAN("Manager Ban"),
    TRANSFER_BAN("Transfer Ban"),
    EXPULSION("Expulsion from League")
}
