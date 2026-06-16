package com.fameafrica.afm.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import com.squareup.moshi.Json

@Entity(
    tableName = "board_requests",
    foreignKeys = [
        ForeignKey(
            entity = ManagersEntity::class,
            parentColumns = ["id"],
            childColumns = ["manager_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TeamsEntity::class,
            parentColumns = ["id"],
            childColumns = ["team_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["manager_id"]),
        Index(value = ["team_id"]),
        Index(value = ["managerName"]),
        Index(value = ["teamName"]),
        Index(value = ["requestStatus"]),
        Index(value = ["requestType"])
    ]
)
data class BoardRequestsEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "requestType")
    val requestType: String,

    @ColumnInfo(name = "requestDescription")
    val requestDescription: String,

    @ColumnInfo(name = "requestStatus")
    val requestStatus: String,

    @Json(name = "manager_id")
    @ColumnInfo(name = "manager_id")
    val managerId: Int,

    @ColumnInfo(name = "managerName")
    val managerName: String,

    @Json(name = "team_id")
    @ColumnInfo(name = "team_id")
    val teamId: Int,

    @ColumnInfo(name = "teamName")
    val teamName: String
) {

    // ============ COMPUTED PROPERTIES ============

    val isPending: Boolean
        get() = requestStatus == "Pending"

    val isApproved: Boolean
        get() = requestStatus == "Approved"

    val isRejected: Boolean
        get() = requestStatus == "Rejected"

    val isCompleted: Boolean
        get() = requestStatus == "Completed"

    val requestTypeDisplay: String
        get() = when (requestType) {
            "TRANSFER_BUDGET" -> "Transfer Budget Increase"
            "WAGE_BUDGET" -> "Wage Budget Increase"
            "SCOUTING_BUDGET" -> "Scouting Budget"
            "YOUTH_FACILITIES" -> "Youth Facility Upgrade"
            "TRAINING_FACILITIES" -> "Training Facility Upgrade"
            "STADIUM_EXPANSION" -> "Stadium Expansion"
            "NEW_CONTRACT" -> "Contract Renewal"
            "HIRE_STAFF" -> "Hire Staff"
            "FIRE_STAFF" -> "Fire Staff"
            "OTHER" -> "Other Request"
            else -> requestType
        }
}

// ============ ENUMS ============

enum class BoardRequestType(val value: String) {
    TRANSFER_BUDGET("TRANSFER_BUDGET"),
    WAGE_BUDGET("WAGE_BUDGET"),
    SCOUTING_BUDGET("SCOUTING_BUDGET"),
    YOUTH_FACILITIES("YOUTH_FACILITIES"),
    TRAINING_FACILITIES("TRAINING_FACILITIES"),
    STADIUM_EXPANSION("STADIUM_EXPANSION"),
    NEW_CONTRACT("NEW_CONTRACT"),
    HIRE_STAFF("HIRE_STAFF"),
    FIRE_STAFF("FIRE_STAFF"),
    OTHER("OTHER")
}

enum class BoardRequestStatus(val value: String) {
    PENDING("Pending"),
    APPROVED("Approved"),
    REJECTED("Rejected"),
    COMPLETED("Completed")
}
