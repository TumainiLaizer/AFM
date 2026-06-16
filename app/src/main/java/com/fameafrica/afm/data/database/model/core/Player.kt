package com.fameafrica.afm.data.database.model.core

/**
 * Core Domain Model for a Football Player.
 * Simplified for simulation and UI consumption.
 */
data class Player(
    val id: Int,
    val name: String,
    val teamId: Int?,
    val teamName: String,
    val position: String,
    val rating: Int,
    val age: Int,
    val nationality: String,
    val fitness: Int, // 0-100
    val morale: Int,  // 0-100
    val form: Double, // 1.0 - 10.0
    val marketValue: Long,
    val salary: Double,
    val imageUrl: String? = null
) {
    val isGoalkeeper: Boolean get() = position == "GK"
}
