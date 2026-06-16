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

        // ===== ELITE CAF LEAGUES =====
        "Egypt" to 1,
        "Morocco" to 2,
        "South Africa" to 3,
        "Algeria" to 4,
        "Tanzania" to 5,

        // ===== STRONG CONTINENTAL LEAGUES =====
        "Tunisia" to 6,
        "Angola" to 7,
        "Congo DRC" to 8,
        "Mali" to 9,
        "Ivory Coast" to 10,

        // ===== HIGHLY COMPETITIVE LEAGUES =====
        "Zambia" to 11,
        "Zanzibar" to 12, // Level 1 is slightly lower than Tanzania L1 but higher than Tanzania L2 (which would be effective rank 15)
        "Nigeria" to 13,
        "Sudan" to 14,
        "Cameroon" to 15,
        "Senegal" to 16,

        // ===== ESTABLISHED LEAGUES =====
        "Ghana" to 17,
        "Libya" to 18,
        "Uganda" to 19,
        "Kenya" to 20,
        "Congo Republic" to 21,

        // ===== DEVELOPING LEAGUES =====
        "Rwanda" to 22,
        "Zimbabwe" to 23,
        "Guinea" to 24,
        "Mozambique" to 25,
        "Botswana" to 26,

        "Burkina Faso" to 27,
        "Mauritania" to 28,
        "Madagascar" to 29,
        "Benin" to 30,
        "Togo" to 31,

        "Malawi" to 32,
        "Niger" to 33,
        "Burundi" to 34,
        "Namibia" to 35,
        "Cape Verde" to 36,

        "Central African Republic" to 37,
        "Gambia" to 38,
        "Liberia" to 39,
        "Sierra Leone" to 40,
        "Mauritius" to 41,

        "South Sudan" to 42,
        "Somalia" to 43,
        "Lesotho" to 44,
        "Djibouti" to 45,
        "Chad" to 46,

        "Equatorial Guinea" to 47
    )

    fun getLeagueRank(country: String, level: Int = 1): Int {
        return getRank(country, level)
    }

    /**
     * Returns effective rank based on country and league level.
     * Higher levels (lower quality) increase the rank value.
     */
    fun getRank(country: String?, level: Int = 1): Int {
        val baseRank = countryRanks[country] ?: 50
        // Every level down drops the effective rank by 10 positions
        return baseRank + (level - 1) * 10
    }

    fun getQualityMultiplier(country: String?, level: Int = 1): Double {
        val rank = getRank(country, level)
        return when {
            rank <= 3 -> 1.0    // Elite (Egypt, Morocco, SA)
            rank <= 6 -> 0.9    // Top (Algeria, Tunisia, Tanzania)
            rank <= 15 -> 0.82  // High (Nigeria, Ghana, etc.)
            rank <= 25 -> 0.75  // Medium
            rank <= 40 -> 0.65  // Emerging
            else -> 0.55        // Lower
        }
    }

    /**
     * Converts rank into reputation score (0–100)
     */
    fun getLeagueReputation(country: String, level: Int = 1): Int {
        val rank = getRank(country, level)

        return when (rank) {
            in 1..5 -> 92
            in 6..10 -> 86
            in 11..15 -> 80
            in 16..20 -> 74
            in 21..30 -> 66
            in 31..40 -> 55
            in 41..50 -> 45
            else -> 30
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
