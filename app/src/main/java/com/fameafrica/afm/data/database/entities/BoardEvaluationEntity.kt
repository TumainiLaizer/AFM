package com.fameafrica.afm.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import com.squareup.moshi.Json

@Entity(
    tableName = "board_evaluation",
    foreignKeys = [
        ForeignKey(
            entity = ManagersEntity::class,
            parentColumns = ["id"], // Changed from "name" to "id" for better data integrity
            childColumns = ["manager_id"], // Use manager_id instead of manager_name for the FK
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["manager_id"], unique = true), // Keep unique on manager_id, as one manager has one evaluation record
        Index(value = ["board_satisfaction"]),
        Index(value = ["status"])
    ]
)
data class BoardEvaluationEntity(
    @PrimaryKey(autoGenerate = true)
    @field:ColumnInfo(name = "id")
    val id: Int = 0,

    @param:Json(name = "manager_id")
    @field:ColumnInfo(name = "manager_id")
    val managerId: Int, // Use ID as the reference

    @param:Json(name = "chairman_id")
    @field:ColumnInfo(name = "chairman_id")
    val chairmanId: Int? = null, // Linked Chairman

    @param:Json(name = "manager_name")
    @field:ColumnInfo(name = "manager_name")
    val managerName: String, // Keep the name for display/convenience if needed

    @param:Json(name = "board_satisfaction")
    @field:ColumnInfo(name = "board_satisfaction", defaultValue = "50")
    val boardSatisfaction: Int = 50,

    @param:Json(name = "patience_pressure")
    @field:ColumnInfo(name = "patience_pressure", defaultValue = "0")
    val patiencePressure: Int = 0, // Pressure from Chairman's patience level

    @param:Json(name = "ambition_pressure")
    @field:ColumnInfo(name = "ambition_pressure", defaultValue = "0")
    val ambitionPressure: Int = 0, // Pressure from Chairman's ambition level

    @param:Json(name = "recent_results")
    @field:ColumnInfo(name = "recent_results")
    val recentResults: String? = null,  // JSON string of last 5 results

    @param:Json(name = "financial_status")
    @field:ColumnInfo(name = "financial_status")
    val financialStatus: String? = null,  // Rich, Healthy, Stable, Breaking Even, In Debt

    @field:ColumnInfo(name = "status", defaultValue = "Safe")
    val status: String = "Safe",  // Safe, Under Review, On Thin Ice, Critical, Sacked

    @param:Json(name = "dna_alignment")
    @field:ColumnInfo(name = "dna_alignment", defaultValue = "50")
    val dnaAlignment: Int = 50 // 0-100 score of how well manager fits club DNA
) {

    // ============ COMPUTED PROPERTIES ============

    val satisfactionLevel: String
        get() = when {
            boardSatisfaction >= 90 -> "Ecstatic"
            boardSatisfaction >= 75 -> "Very Happy"
            boardSatisfaction >= 60 -> "Satisfied"
            boardSatisfaction >= 45 -> "Neutral"
            boardSatisfaction >= 30 -> "Disappointed"
            boardSatisfaction >= 15 -> "Angry"
            else -> "Furious"
        }

    val isSafe: Boolean
        get() = status == "Safe"

    val isUnderReview: Boolean
        get() = status == "Under Review"

    val isOnThinIce: Boolean
        get() = status == "On Thin Ice"

    val isCritical: Boolean
        get() = status == "Critical"

    val isSacked: Boolean
        get() = status == "Sacked"

    val statusColor: String
        get() = when (status) {
            "Safe" -> "Green"
            "Under Review" -> "Yellow"
            "On Thin Ice" -> "Orange"
            "Critical" -> "Red"
            "Sacked" -> "Dark Red"
            else -> "Gray"
        }
}
