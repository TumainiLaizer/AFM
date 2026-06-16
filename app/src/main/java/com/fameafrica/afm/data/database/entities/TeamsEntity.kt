package com.fameafrica.afm.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import com.squareup.moshi.Json

@Entity(
    tableName = "teams",
    indices = [
        Index(value = ["name"]), // ✅ NOT UNIQUE
        Index(value = ["league"]),
        Index(value = ["manager_id"]),
        Index(value = ["elo_rating"]),
        Index(value = ["reputation"]),
        Index(value = ["cup_name"])
    ]
)
data class TeamsEntity(
    @PrimaryKey(autoGenerate = true)
    @field:ColumnInfo(name = "id")
    val id: Int = 0,

    @field:ColumnInfo(name = "name")
    val name: String,

    @param:Json(name = "league_id")
    @field:ColumnInfo(name = "league_id")
    val leagueId: Int? = null,

    @field:ColumnInfo(name = "league")
    val league: String,

    @param:Json(name = "elo_rating")
    @field:ColumnInfo(name = "elo_rating", defaultValue = "1500")
    val eloRating: Int = 1500,

    @field:ColumnInfo(name = "reputation", defaultValue = "50")
    val reputation: Int = 50,

    @field:ColumnInfo(name = "points", defaultValue = "0")
    val points: Int = 0,

    @field:ColumnInfo(name = "revenue", defaultValue = "10000000")
    val revenue: Double = 10000000.0,

    @field:ColumnInfo(name = "morale", defaultValue = "50")
    val morale: Int = 50,

    @param:Json(name = "logo_path")
    @field:ColumnInfo(name = "logo_path")
    val logoPath: String? = null,

    @param:Json(name = "home_stadium")
    @field:ColumnInfo(name = "home_stadium", defaultValue = "FAME Stadium")
    val homeStadium: String = "FAME Stadium",

    @param:Json(name = "stadium_capacity")
    @field:ColumnInfo(name = "stadium_capacity", defaultValue = "3000")
    val stadiumCapacity: Int = 3000,

    @field:ColumnInfo(name = "country", defaultValue = "Tanzania")
    val country: String = "Tanzania",

    @field:ColumnInfo(name = "region", defaultValue = "East Africa")
    val region: String = "East Africa",

    @param:Json(name = "fan_loyalty")
    @field:ColumnInfo(name = "fan_loyalty", defaultValue = "50")
    val fanLoyalty: Int = 50,

    @param:Json(name = "fan_sentiment")
    @field:ColumnInfo(name = "fan_sentiment", defaultValue = "50")
    val fanSentiment: Int = 50,

    @param:Json(name = "board_confidence")
    @field:ColumnInfo(name = "board_confidence", defaultValue = "50")
    val boardConfidence: Int = 50,

    @param:Json(name = "manager_security")
    @field:ColumnInfo(name = "manager_security", defaultValue = "50")
    val managerSecurity: Int = 50,

    @param:Json(name = "debt_level")
    @field:ColumnInfo(name = "debt_level", defaultValue = "0")
    val debtLevel: Long = 0,

    @param:Json(name = "financial_behavior")
    @field:ColumnInfo(name = "financial_behavior")
    val financialBehavior: FinancialBehavior = FinancialBehavior.FRUGAL,

    @param:Json(name = "financial_trend")
    @field:ColumnInfo(name = "financial_trend")
    val financialTrend: List<Long> = emptyList(),

    @param:Json(name = "performance_trend")
    @field:ColumnInfo(name = "performance_trend")
    val performanceTrend: List<Int> = emptyList(),

    @param:Json(name = "identity_strength")
    @field:ColumnInfo(name = "identity_strength", defaultValue = "50")
    var identityStrength: Int = 50,

    @param:Json(name = "morale_momentum")
    @field:ColumnInfo(name = "morale_momentum", defaultValue = "1.0")
    val moraleMomentum: Float = 1.0f,

    @param:Json(name = "form_streak")
    @field:ColumnInfo(name = "form_streak", defaultValue = "")
    val formStreak: String = "",

    @param:Json(name = "tactical_stability")
    @field:ColumnInfo(name = "tactical_stability", defaultValue = "0")
    val tacticalStability: Int = 0,

    @param:Json(name = "rival_team")
    @field:ColumnInfo(name = "rival_team")
    val rivalTeam: String? = null,

    @field:ColumnInfo(name = "formation")
    val formation: String? = null,

    @param:Json(name = "cup_qualification")
    @field:ColumnInfo(name = "cup_qualification")
    val cupQualification: String? = null,

    @param:Json(name = "cup_winner")
    @field:ColumnInfo(name = "cup_winner", defaultValue = "0")
    val cupWinner: Int = 0,

    @param:Json(name = "cup_stage")
    @field:ColumnInfo(name = "cup_stage")
    val cupStage: String? = null,

    @param:Json(name = "cup_name")
    @field:ColumnInfo(name = "cup_name")
    val cupName: String? = null,

    @field:ColumnInfo(name = "crowdSupport", defaultValue = "30")
    val crowdSupport: Int = 30,

    @field:ColumnInfo(name = "sponsorships")
    val sponsorships: String? = null,

    @param:Json(name = "cup_status")
    @field:ColumnInfo(name = "cup_status")
    val cupStatus: String? = null,

    @param:Json(name = "manager_id")
    @field:ColumnInfo(name = "manager_id")
    val managerId: Int? = null,

    @param:Json(name = "avg_attacking_ability")
    @field:ColumnInfo(name = "avg_attacking_ability")
    val avgAttackingAbility: Double? = null,

    @param:Json(name = "avg_defence_ability")
    @field:ColumnInfo(name = "avg_defence_ability")
    val avgDefenceAbility: Double? = null,

    @param:Json(name = "avg_playmaking_ability")
    @field:ColumnInfo(name = "avg_playmaking_ability")
    val avgPlaymakingAbility: Double? = null,

    @param:Json(name = "transfer_fee")
    @field:ColumnInfo(name = "transfer_fee")
    val transferFee: Long? = null,
    
    @param:Json(name = "wage_budget")
    @field:ColumnInfo(name = "wage_budget")
    val wageBudget: Long? = null,

    @param:Json(name = "primary_style")
    @field:ColumnInfo(name = "primary_style")
    val primaryStyle: Playstyle? = null,

    @param:Json(name = "rival_team_id")
    @field:ColumnInfo(name = "rival_team_id")
    val rivalTeamId: Int? = null,

    @param:Json(name = "nation_id")
    @field:ColumnInfo(name = "nation_id")
    val nationId: Int? = null,

    @field:ColumnInfo(name = "latitude")
    val latitude: Double? = null,

    @field:ColumnInfo(name = "longitude")
    val longitude: Double? = null,

    @param:Json(name = "is_playable")
    @field:ColumnInfo(name = "is_playable", defaultValue = "1")
    val isPlayable: Boolean = true
    ) {

    // ============ COMPUTED PROPERTIES ============

    val overallRating: Double
        get() {
            val attack = avgAttackingAbility ?: 50.0
            val defence = avgDefenceAbility ?: 50.0
            val playmaking = avgPlaymakingAbility ?: 50.0
            return (attack + defence + playmaking) / 3.0
        }

    val tier: String
        get() = when {
            eloRating >= 1700 -> "Elite"
            eloRating >= 1600 -> "Championship"
            eloRating >= 1500 -> "Professional"
            eloRating >= 1400 -> "Semi-Professional"
            else -> "Amateur"
        }

    val fanSupport: String
        get() = when {
            fanLoyalty >= 80 -> "Passionate"
            fanLoyalty >= 60 -> "Loyal"
            fanLoyalty >= 40 -> "Supportive"
            fanLoyalty >= 20 -> "Fair-weather"
            else -> "Indifferent"
        }

    val financialHealth: String
        get() = when {
            revenue >= 50000000 -> "Rich"
            revenue >= 20000000 -> "Healthy"
            revenue >= 10000000 -> "Stable"
            revenue >= 5000000 -> "Breaking Even"
            else -> "Poor"
        }

    val formDescription: String
        get() = when {
            morale >= 80 -> "Excellent"
            morale >= 60 -> "Good"
            morale >= 50 -> "Average"
            morale >= 30 -> "Poor"
            else -> "Very Poor"
        }

    // ============ BUSINESS METHODS ============

    fun updateAfterMatch(result: FixturesResultsEntity): TeamsEntity {
        val pointsEarned = when {
            result.homeTeamId == id && result.homeTeamWin -> 3
            result.awayTeamId == id && result.awayTeamWin -> 3
            result.isDraw -> 1
            else -> 0
        }

        val resultChar = when (pointsEarned) {
            3 -> 'W'
            1 -> 'D'
            else -> 'L'
        }

        val newFormStreak = (formStreak + resultChar).takeLast(10)

        val moraleChange = when {
            pointsEarned == 3 -> +5
            pointsEarned == 1 -> +1
            else -> -3
        }

        val sentimentChange = when {
            pointsEarned == 3 -> +2
            pointsEarned == 1 -> 0
            else -> -4
        }

        return this.copy(
            points = this.points + pointsEarned,
            morale = (this.morale + moraleChange).coerceIn(0, 100),
            fanSentiment = (this.fanSentiment + sentimentChange).coerceIn(0, 100),
            formStreak = newFormStreak
        )
    }

    fun updateElo(newElo: Int): TeamsEntity {
        return this.copy(eloRating = newElo)
    }

    fun updateRevenue(amount: Double): TeamsEntity {
        return this.copy(revenue = this.revenue + amount)
    }

    fun updateMorale(change: Int): TeamsEntity {
        return this.copy(morale = (this.morale + change).coerceIn(0, 100))
    }

    fun updateFanLoyalty(change: Int): TeamsEntity {
        return this.copy(fanLoyalty = (this.fanLoyalty + change).coerceIn(0, 100))
    }

    fun assignManager(managerId: Int?): TeamsEntity {
        return this.copy(managerId = managerId)
    }

    fun updateCupProgress(cupName: String, stage: String, status: String): TeamsEntity {
        return this.copy(
            cupName = cupName,
            cupStage = stage,
            cupStatus = status
        )
    }

    fun winCup(cupName: String): TeamsEntity {
        return this.copy(
            cupWinner = this.cupWinner + 1,
            cupStage = "Winner",
            cupStatus = "Completed"
        )
    }

    fun calculateAverageAbilities(players: List<PlayersEntity>): TeamsEntity {
        if (players.isEmpty()) return this

        val attackPlayers = players.filter { it.positionCategory == "FORWARD" }
        val defencePlayers = players.filter { it.positionCategory == "DEFENDER" }
        val midfieldPlayers = players.filter { it.positionCategory == "MIDFIELDER" }

        val avgAttack = if (attackPlayers.isNotEmpty())
            attackPlayers.map { it.rating }.average() else 50.0

        val avgDefence = if (defencePlayers.isNotEmpty())
            defencePlayers.map { it.rating }.average() else 50.0

        val avgPlaymaking = if (midfieldPlayers.isNotEmpty())
            midfieldPlayers.map { it.rating }.average() else 50.0

        return this.copy(
            avgAttackingAbility = avgAttack,
            avgDefenceAbility = avgDefence,
            avgPlaymakingAbility = avgPlaymaking
        )
    }
}
