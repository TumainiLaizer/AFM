package com.fameafrica.afm.data.database.entities

/**
 * Country-specific foreign player limits based on league regulations
 * These rules are applied per club in each country's leagues
 */
object ForeignPlayerRules {

    /**
     * Get maximum foreign players allowed per club for a given country
     * Based on actual league regulations across Africa
     */
    fun getMaxForeignPlayersByCountry(countryId: Int, leagueName: String? = null): Int {
        return when (countryId) {
            1 -> getTanzaniaRules(leagueName)        // Tanzania & Zanzibar
            2 -> 5   // Kenya
            3 -> 5   // Uganda
            4 -> 5   // Rwanda
            5 -> 5   // Burundi
            6 -> 5   // Congo DRC
            7 -> 5   // Congo Republic
            8 -> 5   // Zambia
            9 -> 5   // Zimbabwe
            10 -> 5  // Burkina Faso
            11 -> 5  // Ghana
            12 -> 5  // Nigeria
            13 -> 5  // Cameroon
            14 -> 5  // Ivory Coast
            15 -> 5  // Senegal
            16 -> 5  // Mali
            17 -> 5  // Morocco
            18 -> 5  // Malawi
            19 -> 5  // South Africa
            20 -> 5  // South Sudan
            21 -> 5  // Mozambique
            22 -> 5  // Botswana
            23 -> 5  // Algeria
            24 -> 5  // Tunisia
            25 -> 4  // Egypt - CAF rule: max 4 foreign players
            26 -> 5  // Angola
            27 -> 5  // Namibia
            28 -> 5  // Lesotho
            29 -> 5  // Eswatini
            30 -> 5  // Gambia
            31 -> 5  // Guinea
            32 -> 5  // Guinea-Bissau
            33 -> 5  // Central African Republic
            34 -> 5  // Equatorial Guinea
            35 -> 5  // Chad
            36 -> 5  // Gabon
            37 -> 5  // Djibouti
            38 -> 5  // Comoros
            39 -> 5  // Seychelles
            40 -> 5  // Somalia
            41 -> 5  // Sudan
            42 -> 5  // Liberia
            43 -> 5  // Sierra Leone
            44 -> 5  // Benin
            45 -> 5  // Togo
            46 -> 5  // Niger
            47 -> 5  // Madagascar
            48 -> 5  // Mauritania
            49 -> 5  // Mauritius
            50 -> 5  // Sao Tome
            51 -> 5  // Cape Verde
            52 -> 5  // Eritrea
            53 -> 5  // Libya
            117 -> 5 // Ethiopia
            else -> 5 // Default
        }
    }

    /**
     * Tanzania has special rules based on league level
     */
    private fun getTanzaniaRules(leagueName: String?): Int {
        return when (leagueName) {
            "Tanzania Premier League" -> 12      // Top tier allows 12 foreign players
            "Tanzania Championship League" -> 8   // Second tier allows 8
            "Tanzania First League" -> 6          // Third tier allows 6
            "Tanzania Regional Champions League" -> 4  // Regional league
            "Zanzibar Premier League" -> 10       // Zanzibar allows 10
            "Zanzibar Championship League" -> 7    // Zanzibar second tier
            else -> 5                               // Default for other Tanzanian leagues
        }
    }

    /**
     * Check if a player is considered foreign in a given country
     */
    fun isPlayerForeign(playerNationality: String, leagueCountryId: Int): Boolean {
        // Get country code for the league
        val leagueCountryCode = getCountryCode(leagueCountryId)

        // Players from the same country are not foreign
        if (playerNationality == leagueCountryCode) return false

        // Special cases for regional agreements (e.g., CECAFA, COSAFA)
        return when (leagueCountryId) {
            1 -> { // Tanzania - CECAFA region
                !isCECAFAMember(playerNationality)
            }
            19 -> { // South Africa - COSAFA region
                !isCOSAFAMember(playerNationality)
            }
            12 -> { // Nigeria - WAFU region
                !isWAFUMember(playerNationality)
            }
            else -> true // Default: any different nationality is foreign
        }
    }

    /**
     * CECAFA member countries (East Africa)
     */
    private fun isCECAFAMember(nationality: String): Boolean {
        return nationality in setOf(
            "TAN", "KEN", "UGA", "RWA", "BDI", "SSD", "SDN", "ETH", "ERI", "SOM", "DJI"
        )
    }

    /**
     * COSAFA member countries (Southern Africa)
     */
    private fun isCOSAFAMember(nationality: String): Boolean {
        return nationality in setOf(
            "RSA", "ZAM", "ZWE", "ANG", "MOZ", "MWI", "NAM", "BOT", "SWZ", "LES",
            "MAD", "COM", "SEY", "MRI"
        )
    }

    /**
     * WAFU member countries (West Africa)
     */
    private fun isWAFUMember(nationality: String): Boolean {
        return nationality in setOf(
            "NGA", "GHA", "SEN", "CIV", "MLI", "BFA", "GUI", "GNB", "GAM", "SLE",
            "LBR", "TOG", "BEN", "NIG", "CPV", "MRT"
        )
    }

    /**
     * Map country_id to FIFA country code
     */
    private fun getCountryCode(countryId: Int): String {
        return when (countryId) {
            1 -> "TAN"  // Tanzania
            2 -> "KEN"  // Kenya
            3 -> "UGA"  // Uganda
            4 -> "RWA"  // Rwanda
            5 -> "BDI"  // Burundi
            6 -> "COD"  // Congo DRC
            7 -> "CGO"  // Congo Republic
            8 -> "ZAM"  // Zambia
            9 -> "ZWE"  // Zimbabwe
            10 -> "BFA" // Burkina Faso
            11 -> "GHA" // Ghana
            12 -> "NGA" // Nigeria
            13 -> "CMR" // Cameroon
            14 -> "CIV" // Ivory Coast
            15 -> "SEN" // Senegal
            16 -> "MLI" // Mali
            17 -> "MAR" // Morocco
            18 -> "MWI" // Malawi
            19 -> "RSA" // South Africa
            20 -> "SSD" // South Sudan
            21 -> "MOZ" // Mozambique
            22 -> "BOT" // Botswana
            23 -> "ALG" // Algeria
            24 -> "TUN" // Tunisia
            25 -> "EGY" // Egypt
            26 -> "ANG" // Angola
            27 -> "NAM" // Namibia
            28 -> "LES" // Lesotho
            29 -> "SWZ" // Eswatini
            30 -> "GAM" // Gambia
            31 -> "GUI" // Guinea
            32 -> "GNB" // Guinea-Bissau
            33 -> "CAF" // Central African Republic
            34 -> "EQG" // Equatorial Guinea
            35 -> "CHA" // Chad
            36 -> "GAB" // Gabon
            37 -> "DJI" // Djibouti
            38 -> "COM" // Comoros
            39 -> "SEY" // Seychelles
            40 -> "SOM" // Somalia
            41 -> "SDN" // Sudan
            42 -> "LBR" // Liberia
            43 -> "SLE" // Sierra Leone
            44 -> "BEN" // Benin
            45 -> "TOG" // Togo
            46 -> "NIG" // Niger
            47 -> "MAD" // Madagascar
            48 -> "MRT" // Mauritania
            49 -> "MRI" // Mauritius
            50 -> "STP" // Sao Tome
            51 -> "CPV" // Cape Verde
            52 -> "ERI" // Eritrea
            53 -> "LBY" // Libya
            117 -> "ETH" // Ethiopia
            else -> "UNK"
        }
    }
}