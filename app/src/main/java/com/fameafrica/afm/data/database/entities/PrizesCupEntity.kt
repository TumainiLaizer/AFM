package com.fameafrica.afm.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import com.squareup.moshi.Json

@Entity(
    tableName = "prizes_cup",
    foreignKeys = [
        ForeignKey(
            entity = CupsEntity::class,
            parentColumns = ["id"],
            childColumns = ["competition_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["competition_id", "stage", "position"], unique = true),
        Index(value = ["competition_id"]),
        Index(value = ["stage"]),
        Index(value = ["position"]),
        Index(value = ["paid_at_stage"])
    ]
)
data class PrizesCupEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @Json(name = "competition_id")
    @ColumnInfo(name = "competition_id")
    val competitionId: Int,

    @ColumnInfo(name = "stage")
    val stage: String,  // FINAL, SEMI_FINAL, QUARTER_FINAL, GROUP_STAGE, QUALIFICATION

    @ColumnInfo(name = "position")
    val position: Int,  // 1 = Winner, 2 = Runner-up, etc.

    @Json(name = "prize_money")
    @ColumnInfo(name = "prize_money")
    val prizeMoney: Int,

    @Json(name = "percentage_of_champion")
    @ColumnInfo(name = "percentage_of_champion")
    val percentageOfChampion: Double,

    @Json(name = "teams_at_stage")
    @ColumnInfo(name = "teams_at_stage")
    val teamsAtStage: Int? = null,  // Number of teams reaching this stage

    @Json(name = "paid_at_stage")
    @ColumnInfo(name = "paid_at_stage")
    val paidAtStage: Boolean = true,  // Prize is paid immediately when stage is reached

    @ColumnInfo(name = "description")
    val description: String? = null
) {

    // ============ COMPUTED PROPERTIES ============

    val prizeMoneyInMillions: Double
        get() = prizeMoney / 1_000_000.0

    val stageDisplay: String
        get() = when (stage) {
            "FINAL" -> when (position) {
                1 -> "Winner"
                2 -> "Runner-up"
                else -> "Finalist"
            }
            "SEMI_FINAL" -> "Semi-finalist"
            "QUARTER_FINAL" -> "Quarter-finalist"
            "ROUND_16" -> "Round of 16"
            "ROUND_32" -> "Round of 32"
            "GROUP_STAGE" -> "Group Stage"
            "QUALIFICATION" -> "Qualification Round"
            else -> stage
        }
}