package com.fameafrica.afm.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import com.squareup.moshi.Json

@Entity(
    tableName = "scout_assignments",
    foreignKeys = [
        ForeignKey(
            entity = StaffEntity::class,
            parentColumns = ["id"],
            childColumns = ["scout_id"],
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
        Index(value = ["scout_id"]),
        Index(value = ["player_id"]),
        Index(value = ["assigned_date"]),
        Index(value = ["completion_date"]),
        Index(value = ["report_status"]),
        Index(value = ["priority"]),
        Index(value = ["scout_id", "player_id"], unique = true)
    ]
)
data class ScoutAssignmentsEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @Json(name = "scout_id")
    @ColumnInfo(name = "scout_id")
    val scoutId: Int,

    @Json(name = "scout_name")
    @ColumnInfo(name = "scout_name")
    val scoutName: String,

    @Json(name = "player_id")
    @ColumnInfo(name = "player_id")
    val playerId: Int,

    @Json(name = "player_name")
    @ColumnInfo(name = "player_name")
    val playerName: String,

    @Json(name = "assigned_date")
    @ColumnInfo(name = "assigned_date")
    val assignedDate: Long = System.currentTimeMillis(),

    @Json(name = "completion_date")
    @ColumnInfo(name = "completion_date")
    val completionDate: Long? = null,

    @Json(name = "report_status")
    @ColumnInfo(name = "report_status")
    val reportStatus: String = "In Progress",  // In Progress, Completed, Failed

    @ColumnInfo(name = "priority")
    val priority: String = "Normal",  // Low, Normal, High, Urgent

    @Json(name = "scouting_focus")
    @ColumnInfo(name = "scouting_focus")
    val scoutingFocus: String? = null,  // Technical, Tactical, Physical, Mental, All

    @Json(name = "assignment_notes")
    @ColumnInfo(name = "assignment_notes")
    val assignmentNotes: String? = null,

    @Json(name = "scout_report")
    @ColumnInfo(name = "scout_report")
    val scoutReport: String? = null,

    @Json(name = "scout_rating")
    @ColumnInfo(name = "scout_rating")
    val scoutRating: Int? = null,  // 1-100

    @Json(name = "estimated_value")
    @ColumnInfo(name = "estimated_value")
    val estimatedValue: Int? = null,

    @Json(name = "potential_rating")
    @ColumnInfo(name = "potential_rating")
    val potentialRating: Int? = null,

    @ColumnInfo(name = "strengths")
    val strengths: String? = null,  // JSON array of strengths

    @ColumnInfo(name = "weaknesses")
    val weaknesses: String? = null,  // JSON array of weaknesses

    @ColumnInfo(name = "verdict")
    val verdict: String? = null,  // Recommended, Not Recommended, Watch
) {

    // ============ COMPUTED PROPERTIES ============

    val isInProgress: Boolean
        get() = reportStatus == "In Progress"

    val isCompleted: Boolean
        get() = reportStatus == "Completed"

    val isFailed: Boolean
        get() = reportStatus == "Failed"

    val isHighPriority: Boolean
        get() = priority == "High" || priority == "Urgent"

    val assignmentDuration: Long?
        get() = if (completionDate != null) completionDate - assignedDate else null

    val assignmentDays: Int?
        get() = assignmentDuration?.let { (it / (1000 * 60 * 60 * 24)).toInt() }

    val summary: String
        get() = "$playerName - $reportStatus (Priority: $priority)"
}

// ============ ENUMS ============

enum class ScoutReportStatus(val value: String) {
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    FAILED("Failed")
}

enum class ScoutPriority(val value: String) {
    LOW("Low"),
    NORMAL("Normal"),
    HIGH("High"),
    URGENT("Urgent")
}

enum class ScoutingFocus(val value: String) {
    TECHNICAL("Technical"),
    TACTICAL("Tactical"),
    PHYSICAL("Physical"),
    MENTAL("Mental"),
    ALL("All")
}

enum class ScoutVerdict(val value: String) {
    RECOMMENDED("Recommended"),
    NOT_RECOMMENDED("Not Recommended"),
    WATCH("Watch")
}