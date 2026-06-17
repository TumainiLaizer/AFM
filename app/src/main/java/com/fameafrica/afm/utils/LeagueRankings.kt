package com.fameafrica.afm.utils

object LeagueRankings {

    /**
     * AFM 2026 African League Strength Rankings
     * Based on:
     * - IFFHS 2025 CAF League Rankings
     * - CAF club performances
     * - Continental competition consistency
     * - League professionalism & infrastructure
     */
    val countryRanks: Map<String, Int> = linkedMapOf(

        // ===== CAF TOP LEAGUES =====
        "Egypt" to 1,
        "Morocco" to 2,
        "South Africa" to 3,
        "Algeria" to 4,
        "Tanzania" to 5,

        // ===== CONTINENTAL CHALLENGERS =====
        "Tunisia" to 6,
        "Angola" to 7,
        "Congo DRC" to 8,
        "Sudan" to 9,
        "Libya" to 10,

        // ===== STRONG CAF PARTICIPANTS =====
        "Guinea" to 11,
        "Ivory Coast" to 12,
        "Nigeria" to 13,
        "Mali" to 14,
        "Cameroon" to 15,

        // ===== ESTABLISHED LEAGUES =====
        "Ghana" to 16,
        "Zambia" to 17,
        "Uganda" to 18,
        "Kenya" to 19,
        "Congo Republic" to 20,

        // ===== DEVELOPING LEAGUES =====
        "Rwanda" to 21,
        "Zimbabwe" to 22,
        "Mozambique" to 23,
        "Botswana" to 24,
        "Burkina Faso" to 25,

        "Mauritania" to 26,
        "Benin" to 27,
        "Togo" to 28,
        "Madagascar" to 29,
        "Malawi" to 30,

        "Niger" to 31,
        "Burundi" to 32,
        "Namibia" to 33,
        "Cape Verde" to 34,
        "Gambia" to 35,

        "Mauritius" to 36,
        "Central African Republic" to 37,
        "Liberia" to 38,
        "Sierra Leone" to 39,
        "South Sudan" to 40,

        "Zanzibar" to 41,
        "Equatorial Guinea" to 42,
        "Lesotho" to 43,
        "Somalia" to 44,
        "Djibouti" to 45,

        "Chad" to 46
    )

    fun getLeagueRank(country: String, level: Int = 1): Int {
        return getRank(country, level)
    }

    /**
     * Returns effective rank based on country and league level.
     * Higher levels (lower quality) increase the rank value.
     */
    /**
     * Returns the effective rank after applying league level penalties.
     *
     * Level 1 = Top Division
     * Level 2 = Second Division
     * Level 3 = Third Division
     * Level 4 = Regional
     * Level 5 = Amateur
     */
    fun getRank(country: String?, level: Int = 1): Int {
        val baseRank = countryRanks[country] ?: 50

        val levelPenalty = when (level) {
            1 -> 0
            2 -> 5
            3 -> 12
            4 -> 20
            5 -> 29
            else -> 25 + ((level - 4) * 5)
        }

        return baseRank + levelPenalty
    }

    /**
     * League quality multiplier used for:
     * - Transfer values
     * - Wage expectations
     * - Match intensity
     * - Youth development
     */
    fun getQualityMultiplier(country: String?, level: Int = 1): Double {
        val rank = getRank(country, level)

        return when {
            rank <= 5 -> 1.00      // Egypt, Morocco, South Africa, Algeria, Tanzania
            rank <= 10 -> 0.95
            rank <= 15 -> 0.90
            rank <= 20 -> 0.85
            rank <= 30 -> 0.80
            rank <= 40 -> 0.72
            rank <= 50 -> 0.65
            else -> 0.55
        }
    }

    /**
     * Converts rank into league reputation (0–100).
     *
     * Reputation affects:
     * - Sponsorships
     * - Fan growth
     * - Player attraction
     * - Media attention
     * - Continental seeding
     */
    fun getLeagueReputation(country: String, level: Int = 1): Int {
        val rank = getRank(country, level)

        return when {
            rank == 1 -> 95
            rank == 2 -> 94
            rank == 3 -> 93
            rank <= 5 -> 91
            rank <= 10 -> 88
            rank <= 15 -> 84
            rank <= 20 -> 80
            rank <= 25 -> 76
            rank <= 30 -> 72
            rank <= 35 -> 68
            rank <= 40 -> 64
            rank <= 45 -> 58
            rank <= 50 -> 52
            else -> 45
        }
    }

    fun getLeagueTier(country: String, level: Int = 1): String {
        val rank = getRank(country, level)
        return when (rank) {
            in 1..5 -> "Elite"
            in 6..10 -> "Strong"
            in 11..20 -> "Competitive"
            in 21..35 -> "Developing"
            in 36..50 -> "Semi-Pro"
            else -> "Regional"
        }
    }

    fun getCountryRegion(country: String?): String {
        val east = setOf("Tanzania", "Zanzibar", "Uganda", "Kenya", "Rwanda", "Burundi", "Ethiopia", "South Sudan", "Somalia", "Djibouti", "Eritrea")
        val north = setOf("Egypt", "Morocco", "Algeria", "Tunisia", "Libya", "Sudan")
        val southern = setOf("South Africa", "Zambia", "Zimbabwe", "Mozambique", "Botswana", "Namibia", "Malawi", "Angola", "Lesotho", "Eswatini", "Mauritius", "Madagascar", "Seychelles", "Comoros")
        val west = setOf("Nigeria", "Senegal", "Ghana", "Ivory Coast", "Mali", "Guinea", "Burkina Faso", "Benin", "Togo", "Sierra Leone", "Liberia", "Gambia", "Mauritania", "Cape Verde", "Guinea-Bissau")
        val central = setOf("Congo DRC", "Cameroon", "Congo Republic", "Gabon", "Equatorial Guinea", "Central African Republic", "Chad", "Sao Tome and Principe")
        
        return when {
            east.contains(country) -> "East Africa"
            north.contains(country) -> "North Africa"
            southern.contains(country) -> "Southern Africa"
            west.contains(country) -> "West Africa"
            central.contains(country) -> "Central Africa"
            else -> "Others"
        }
    }
}
