package com.fameafrica.afm.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import com.squareup.moshi.Json

@Entity(
    tableName = "prizes_leagues",
    foreignKeys = [
        ForeignKey(
            entity = LeaguesEntity::class,
            parentColumns = ["id"],
            childColumns = ["competition_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["competition_id", "position"], unique = true),
        Index(value = ["competition_id"]),
        Index(value = ["position"])
    ]
)
data class PrizesLeaguesEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @Json(name = "competition_id")
    @ColumnInfo(name = "competition_id")
    val competitionId: Int,

    @ColumnInfo(name = "position")
    val position: Int,

    @Json(name = "prize_money")
    @ColumnInfo(name = "prize_money")
    val prizeMoney: Int,

    @Json(name = "percentage_of_champion")
    @ColumnInfo(name = "percentage_of_champion")
    val percentageOfChampion: Double,

    @ColumnInfo(name = "tier")
    val tier: String? = null,  // CHAMPION, RUNNER_UP, TOP_TIER, MID_TIER, BOTTOM_TIER

    @ColumnInfo(name = "description")
    val description: String? = null
) {

    // ============ COMPUTED PROPERTIES ============

    val prizeMoneyInMillions: Double
        get() = prizeMoney / 1_000_000.0

    val positionDisplay: String
        get() = when (position) {
            1 -> "Champion"
            2 -> "Runner-up"
            3 -> "Third Place"
            4 -> "Fourth Place"
            else -> "${position}th Place"
        }
}

// ============ ENUMS ============

enum class LeaguePrizeTier(val value: String) {
    CHAMPION("CHAMPION"),
    RUNNER_UP("RUNNER_UP"),
    THIRD_PLACE("THIRD_PLACE"),
    FOURTH_PLACE("FOURTH_PLACE"),
    TOP_TIER("TOP_TIER"),
    MID_TIER("MID_TIER"),
    BOTTOM_TIER("BOTTOM_TIER")
}