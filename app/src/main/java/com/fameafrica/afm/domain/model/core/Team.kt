package com.fameafrica.afm.domain.model.core

/**
 * Core Domain Model for a Football Team.
 * Represents the essential state for both AFM and AFC modes.
 */
data class Team(
    val id: Int,
    val name: String,
    val league: String,
    val logoPath: String? = null,
    val stadiumName: String,
    val stadiumCapacity: Int,
    val budget: Long,
    val wageBill: Long,
    val boardConfidence: Int, // 0-100
    val fanSatisfaction: Int,  // 0-100
    val rating: Int, // Overall team strength
    val leaguePosition: Int,
    val points: Int
)
