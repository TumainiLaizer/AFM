package com.fameafrica.afm.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.squareup.moshi.Json

@Entity(
    tableName = "club_vision",
    foreignKeys = [
        ForeignKey(
            entity = TeamsEntity::class,
            parentColumns = ["id"],
            childColumns = ["teamId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["teamId"], unique = true)]
)
data class ClubVisionEntity(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "teamId")
    val teamId: Int,

    // 🔗 LINKED DNA SNAPSHOT (for fast access + UI)
    @Json(name = "dna_play_style")
    @ColumnInfo(name = "dna_play_style")
    val dnaPlayStyle: String,

    @Json(name = "dna_transfer_policy")
    @ColumnInfo(name = "dna_transfer_policy")
    val dnaTransferPolicy: String,

    @Json(name = "dna_financial_behavior")
    @ColumnInfo(name = "dna_financial_behavior")
    val dnaFinancialBehavior: FinancialBehavior,

    // --- 🎯 BOARD EXPECTATIONS (DERIVED FROM DNA) ---

    @Json(name = "required_play_style")
    @ColumnInfo(name = "required_play_style")
    val requiredPlayStyle: String, // What board expects manager to follow

    @Json(name = "youth_expectation")
    @ColumnInfo(name = "youth_expectation")
    val youthExpectation: Int, // 1–100 (mapped from DNA youthPriority)

    @Json(name = "transfer_expectation")
    @ColumnInfo(name = "transfer_expectation")
    val transferExpectation: String, // mirrors transfer_policy but can evolve

    @Json(name = "financial_discipline")
    @ColumnInfo(name = "financial_discipline")
    val financialDiscipline: Int, // 1–100 strictness

    @Json(name = "regional_dominance_required")
    @ColumnInfo(name = "regional_dominance_required")
    val regionalDominanceRequired: Boolean = false,

    // --- 🧭 LONG TERM STRATEGY ---
    @Json(name = "five_year_plan")
    @ColumnInfo(name = "five_year_plan")
    val fiveYearPlan: String? = null,

    @Json(name = "commercial_growth_target")
    @ColumnInfo(name = "commercial_growth_target")
    val commercialGrowthTarget: Int = 50,

    // --- 📊 MANAGER EVALUATION ---
    @Json(name = "philosophy_score")
    @ColumnInfo(name = "philosophy_score")
    val philosophyScore: Int = 50,

    @Json(name = "vision_alignment")
    @ColumnInfo(name = "vision_alignment")
    val visionAlignment: Int = 50,

    @Json(name = "board_pressure")
    @ColumnInfo(name = "board_pressure")
    val boardPressure: Int = 50, // NEW: dynamic pressure system

    @Json(name = "job_security")
    @ColumnInfo(name = "job_security")
    val jobSecurity: Int = 70 // NEW: linked to pressure
) {

    /**
     * Evaluate how well manager matches DNA expectations
     */
    fun evaluateManagerAlignment(
        managerStyle: String,
        youthUsage: Int,
        transferType: String
    ): Int {
        var score = 0

        if (managerStyle == requiredPlayStyle) score += 40
        if (youthUsage >= youthExpectation) score += 30
        if (transferType == transferExpectation) score += 30

        return score.coerceIn(0, 100)
    }

    /**
     * Helper for transfer validation
     */
    fun fitsSigningPolicy(
        playerNationality: String,
        playerAge: Int,
        playerReputation: Int,
        teamCountry: String
    ): Boolean {
        return when (transferExpectation) {
            "LOCAL_TALENT_ONLY" -> playerNationality == teamCountry
            "YOUTH" -> playerAge <= 23
            "EXPERIENCED" -> playerAge >= 28
            "HIGH_REPUTATION" -> playerReputation >= 75
            else -> true
        }
    }

    /**
     * Dynamic board pressure update
     */
    fun updateBoardPressure(
        currentPosition: Int,
        expectedPosition: Int,
        alignmentScore: Int
    ): Int {
        var pressure = boardPressure

        if (currentPosition > expectedPosition) pressure += 10
        if (alignmentScore < 40) pressure += 10
        if (alignmentScore > 70) pressure -= 5

        return pressure.coerceIn(0, 100)
    }
}

fun mapDNAtoVision(dna: ClubDNAEntity): ClubVisionEntity {
    return ClubVisionEntity(
        teamId = dna.teamId,

        // Snapshot
        dnaPlayStyle = dna.playStyle,
        dnaTransferPolicy = dna.transferPolicy,
        dnaFinancialBehavior = dna.financialBehavior,

        // Expectations derived from DNA
        requiredPlayStyle = dna.playStyle,

        youthExpectation = dna.youthPriority,

        transferExpectation = dna.transferPolicy,

        financialDiscipline = when (dna.financialBehavior) {
            FinancialBehavior.FRUGAL -> 90
            FinancialBehavior.CORPORATE_STRUCTURED -> 80
            FinancialBehavior.SPENDER -> 40
            FinancialBehavior.RISKY -> 30
            FinancialBehavior.UNSTABLE -> 20
            else -> 60
        },

        regionalDominanceRequired = dna.region == "NORTH_AFRICA",

        commercialGrowthTarget = when (dna.financialBehavior) {
            FinancialBehavior.CORPORATE_STRUCTURED -> 80
            FinancialBehavior.SPONSOR_DEPENDENT -> 70
            FinancialBehavior.PLAYER_SALES_DEPENDENT -> 60
            FinancialBehavior.LOW_REVENUE_SURVIVAL -> 40
            FinancialBehavior.GOVERNMENT_BACKED -> 30
            FinancialBehavior.FRUGAL -> 20
            FinancialBehavior.UNSTABLE -> 10
            else -> 50
        }
    )
}