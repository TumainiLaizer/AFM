package com.fameafrica.afm.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import java.text.SimpleDateFormat
import java.util.*

@Entity(
    tableName = "transfer_windows",
    indices = [
        Index(value = ["start_date", "end_date"]),
        Index(value = ["season"]),
        Index(value = ["is_open"])
    ]
)
data class TransferWindowsEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "season")
    val season: String,

    @ColumnInfo(name = "window_type")
    val windowType: String,  // SUMMER, WINTER, EMERGENCY

    @ColumnInfo(name = "start_date")
    val startDate: String,

    @ColumnInfo(name = "end_date")
    val endDate: String,

    @ColumnInfo(name = "start_week")
    val startWeek: Int? = null,

    @ColumnInfo(name = "end_week")
    val endWeek: Int? = null,

    @ColumnInfo(name = "is_open")
    val isOpen: Boolean = false,

    @ColumnInfo(name = "registration_deadline")
    val registrationDeadline: String? = null,

    // Base limits - will be overridden by country-specific rules
    @ColumnInfo(name = "max_foreign_players", defaultValue = "5")
    val maxForeignPlayers: Int = 5,

    @ColumnInfo(name = "max_players_in")
    val maxPlayersIn: Int = 10,  // Maximum players a club can register in window

    @ColumnInfo(name = "max_players_out")
    val maxPlayersOut: Int = 10  // Maximum players a club can sell in window
) {

    // ============ COMPUTED PROPERTIES ============

    val isSummerWindow: Boolean
        get() = windowType == "SUMMER"

    val isWinterWindow: Boolean
        get() = windowType == "WINTER"

    val isEmergencyWindow: Boolean
        get() = windowType == "EMERGENCY"

    val daysRemaining: Int
        get() {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return try {
                val end = format.parse(endDate)
                val now = Date()
                if (end != null) {
                    ((end.time - now.time) / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(0)
                } else {
                    0
                }
            } catch (e: Exception) {
                0
            }
        }

    val isActive: Boolean
        get() = isOpen && daysRemaining > 0

    val windowDisplay: String
        get() = "$season $windowType Window"
}

// ============ ENUMS ============

enum class TransferWindowType(val value: String) {
    SUMMER("SUMMER"),
    WINTER("WINTER"),
    EMERGENCY("EMERGENCY")
}

enum class TransferWindowStatus {
    OPEN,
    CLOSED,
    PENDING
}