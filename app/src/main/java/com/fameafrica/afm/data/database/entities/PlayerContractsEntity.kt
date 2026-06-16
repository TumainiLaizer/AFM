package com.fameafrica.afm.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "player_contracts",
    foreignKeys = [
        ForeignKey(
            entity = PlayersEntity::class,
            parentColumns = ["id"],
            childColumns = ["playerId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TeamsEntity::class,
            parentColumns = ["id"],
            childColumns = ["teamId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["playerId"], unique = true),
        Index(value = ["teamId"]),
        Index(value = ["salary"]),
        Index(value = ["contractEndDate"]),
        Index(value = ["isNegotiable"]),
        Index(value = ["releaseClause"]),
        Index(value = ["contractStatus"])
    ]
)
data class PlayerContractsEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "playerId")
    val playerId: Int,

    @ColumnInfo(name = "playerName")
    val playerName: String,

    @ColumnInfo(name = "teamId")
    val teamId: Int,

    @ColumnInfo(name = "teamName")
    val teamName: String,

    @ColumnInfo(name = "salary")
    val salary: Int,

    @ColumnInfo(name = "signingBonus")
    val signingBonus: Int? = null,

    @ColumnInfo(name = "contractLength", defaultValue = "3")
    val contractLength: Int = 3,  // In years

    @ColumnInfo(name = "contractStartDate")
    val contractStartDate: String,

    @ColumnInfo(name = "contractEndDate")
    val contractEndDate: String,

    @ColumnInfo(name = "releaseClause", defaultValue = "500000000")
    val releaseClause: Int = 500000000,

    @ColumnInfo(name = "isNegotiable", defaultValue = "1")
    val isNegotiable: Boolean = true,

    @ColumnInfo(name = "bonusGoals", defaultValue = "150000")
    val bonusGoals: Int = 150000,

    @ColumnInfo(name = "bonusAssists", defaultValue = "100000")
    val bonusAssists: Int = 100000,

    @ColumnInfo(name = "bonusCleanSheets")
    val bonusCleanSheets: Int? = null,

    @ColumnInfo(name = "bonusTrophies", defaultValue = "5000000")
    val bonusTrophies: Int = 5000000,

    @ColumnInfo(name = "bonusPromotion")
    val bonusPromotion: Int? = null,

    @ColumnInfo(name = "bonusTopScorer")
    val bonusTopScorer: Int? = null,

    @ColumnInfo(name = "wageIncreaseAfterMatches")
    val wageIncreaseAfterMatches: Int? = null,  // After X matches

    @ColumnInfo(name = "matchesForIncrease")
    val matchesForIncrease: Int? = null,

    @ColumnInfo(name = "relegationWageCut")
    val relegationWageCut: Int? = null,  // Percentage

    @ColumnInfo(name = "agentCommission")
    val agentCommission: Int? = null,

    @ColumnInfo(name = "contractStatus")
    val contractStatus: String = "ACTIVE",  // ACTIVE, EXPIRING, EXPIRED, TERMINATED

    @ColumnInfo(name = "lastRenegotiationDate")
    val lastRenegotiationDate: String? = null,

    @ColumnInfo(name = "notes")
    val notes: String? = null
) {

    // ============ COMPUTED PROPERTIES ============

    val salaryInMillions: Double
        get() = salary / 1_000_000.0

    val releaseClauseInMillions: Double
        get() = releaseClause / 1_000_000.0

    val isActive: Boolean
        get() = contractStatus == "ACTIVE"

    val isExpiring: Boolean
        get() = contractStatus == "EXPIRING"

    val isExpired: Boolean
        get() = contractStatus == "EXPIRED"

    val yearsRemaining: Int
        get() {
            val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
            val endYear = contractEndDate.split("-").first().toIntOrNull() ?: 0
            return (endYear - currentYear).coerceAtLeast(0)
        }

    val contractSummary: String
        get() = "$playerName: ${salaryInMillions}M p/a until $contractEndDate"

    val hasReleaseClause: Boolean
        get() = releaseClause < 500_000_000 // If less than max default

    val totalBonusPotential: Int
        get() = bonusGoals + bonusAssists + (bonusCleanSheets ?: 0) + bonusTrophies + (bonusPromotion ?: 0)
}

// ============ ENUMS ============

enum class ContractStatus(val value: String) {
    ACTIVE("ACTIVE"),
    EXPIRING("EXPIRING"),
    EXPIRED("EXPIRED"),
    TERMINATED("TERMINATED")
}
