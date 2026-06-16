package com.fameafrica.afm.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "objectives",
    foreignKeys = [
        ForeignKey(
            entity = TeamsEntity::class,
            parentColumns = ["id"],
            childColumns = ["team_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["team_id"]),
        Index(value = ["status"]),
        Index(value = ["objective_type"]),
        Index(value = ["season"])
    ]
)
data class ObjectivesEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "team_id")
    val teamId: Int,

    @ColumnInfo(name = "team_name")
    val teamName: String?,

    @ColumnInfo(name = "season")
    val season: String,

    @ColumnInfo(name = "objective_type")
    val objectiveType: String,  // LEAGUE, CUP, CONTINENTAL, FINANCIAL, YOUTH, BOARD

    @ColumnInfo(name = "objective")
    val objective: String?,

    @ColumnInfo(name = "target_value")
    val targetValue: String?,

    @ColumnInfo(name = "current_progress")
    val currentProgress: String? = "0",

    @ColumnInfo(name = "reward_type")
    val rewardType: String?,

    @ColumnInfo(name = "reward")
    val reward: String?,

    @ColumnInfo(name = "penalty_type")
    val penaltyType: String?,

    @ColumnInfo(name = "penalty")
    val penalty: String?,

    @ColumnInfo(name = "status", defaultValue = "pending")
    val status: String = "pending",

    @ColumnInfo(name = "deadline")
    val deadline: String?,

    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean = false,

    @ColumnInfo(name = "completion_date")
    val completionDate: String? = null
) {

    // ============ COMPUTED PROPERTIES ============

    val isPending: Boolean
        get() = status == "pending"

    val isAchieved: Boolean
        get() = status == "achieved"

    val isFailed: Boolean
        get() = status == "failed"

    val progressPercentage: Int
        get() {
            val target = targetValue?.toIntOrNull() ?: return 0
            val current = currentProgress?.toIntOrNull() ?: return 0
            return if (target > 0) (current * 100 / target).coerceIn(0, 100) else 0
        }

    val displayObjective: String
        get() = when (objectiveType) {
            "LEAGUE" -> "League Position: $objective"
            "CUP" -> "Cup Progress: $objective"
            "CONTINENTAL" -> "Continental Competition: $objective"
            "FINANCIAL" -> "Financial Target: $objective"
            "YOUTH" -> "Youth Development: $objective"
            "BOARD" -> "Board Expectation: $objective"
            else -> objective ?: "No objective"
        }
}

// ============ ENUMS ============

enum class ObjectiveType(val value: String) {
    LEAGUE("LEAGUE"),
    CUP("CUP"),
    CONTINENTAL("CONTINENTAL"),
    FINANCIAL("FINANCIAL"),
    YOUTH("YOUTH"),
    BOARD("BOARD")
}

enum class ObjectiveStatus(val value: String) {
    PENDING("pending"),
    ACHIEVED("achieved"),
    FAILED("failed")
}

enum class RewardType(val value: String) {
    TRANSFER_BUDGET("Transfer Budget"),
    WAGE_BUDGET("Wage Budget"),
    REPUTATION("Reputation Boost"),
    JOB_SECURITY("Job Security"),
    FAN_LOYALTY("Fan Loyalty")
}

enum class PenaltyType(val value: String) {
    SACKED("Sacked"),
    TRANSFER_BUDGET_CUT("Transfer Budget Cut"),
    WAGE_BUDGET_CUT("Wage Budget Cut"),
    REPUTATION_DROP("Reputation Drop"),
    FAN_ANGER("Fan Anger")
}
