package com.fameafrica.afm.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "manager_offers_for_retired_players",
    foreignKeys = [
        ForeignKey(
            entity = PlayersEntity::class,
            parentColumns = ["id"],
            childColumns = ["player_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TeamsEntity::class,
            parentColumns = ["id"],
            childColumns = ["offered_team_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["player_id"]),
        Index(value = ["offered_team_id"]),
        Index(value = ["status"]),
        Index(value = ["role_type"]),
        Index(value = ["offer_date"])
    ]
)
data class ManagerOffersForRetiredPlayersEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "player_id")
    val playerId: Int,

    @ColumnInfo(name = "player_name")
    val playerName: String,

    @ColumnInfo(name = "offered_team_id")
    val offeredTeamId: Int,

    @ColumnInfo(name = "offered_team")
    val offeredTeam: String,

    @ColumnInfo(name = "league_name")
    val leagueName: String,

    @ColumnInfo(name = "league_level")
    val leagueLevel: Int,

    @ColumnInfo(name = "offered_salary", defaultValue = "500000")
    val offeredSalary: Int = 500000,

    @ColumnInfo(name = "contract_years")
    val contractYears: Int = 2,

    @ColumnInfo(name = "status", defaultValue = "Pending")
    val status: String = "Pending",

    @ColumnInfo(name = "role_type")
    val roleType: String,  // PLAYER_COACH, ASSISTANT_MANAGER, SPORTING_DIRECTOR, SCOUT, etc.

    @ColumnInfo(name = "role_description")
    val roleDescription: String? = null,

    @ColumnInfo(name = "offer_date")
    val offerDate: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "expiry_date")
    val expiryDate: Long,

    @ColumnInfo(name = "message")
    val message: String? = null,

    @ColumnInfo(name = "logo")
    val logo: String? = null
) {

    // ============ COMPUTED PROPERTIES ============

    val isExpired: Boolean
        get() = System.currentTimeMillis() > expiryDate

    val isPending: Boolean
        get() = status == "Pending" && !isExpired

    val isAccepted: Boolean
        get() = status == "Accepted"

    val isRejected: Boolean
        get() = status == "Rejected"

    val salaryInMillions: Double
        get() = offeredSalary / 1_000_000.0

    val daysRemaining: Int
        get() = ((expiryDate - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(0)
}

// ============ ENUMS ============

enum class RetiredPlayerRoleType(val value: String) {
    PLAYER_COACH("PLAYER_COACH"),
    ASSISTANT_MANAGER("ASSISTANT_MANAGER"),
    FIRST_TEAM_COACH("FIRST_TEAM_COACH"),
    GOALKEEPER_COACH("GOALKEEPER_COACH"),
    FITNESS_COACH("FITNESS_COACH"),
    YOUTH_COACH("YOUTH_COACH"),
    TECHNICAL_COACH("TECHNICAL_COACH"),
    SET_PIECE_COACH("SET_PIECE_COACH"),
    ATTACKING_COACH("ATTACKING_COACH"),
    DEFENSIVE_COACH("DEFENSIVE_COACH"),
    CHIEF_SCOUT("CHIEF_SCOUT"),
    SCOUT("SCOUT"),
    REGIONAL_SCOUT("REGIONAL_SCOUT"),
    YOUTH_SCOUT("YOUTH_SCOUT"),
    DATA_ANALYST("DATA_ANALYST"),
    HEAD_PHYSIO("HEAD_PHYSIO"),
    PHYSIOTHERAPIST("PHYSIOTHERAPIST"),
    SPORTS_SCIENTIST("SPORTS_SCIENTIST"),
    SPORTING_DIRECTOR("SPORTING_DIRECTOR"),
    TECHNICAL_DIRECTOR("TECHNICAL_DIRECTOR"),
    ACADEMY_DIRECTOR("ACADEMY_DIRECTOR"),
    HEAD_OF_YOUTH("HEAD_OF_YOUTH"),
    CLUB_MEDIA_OFFICER("CLUB_MEDIA_OFFICER"),
    KIT_MANAGER("KIT_MANAGER"),
    PLAYER_AGENT("PLAYER_AGENT")
}
