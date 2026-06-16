package com.fameafrica.afm.utils

enum class FootballRegion(val displayName: String) {
    EAST_AFRICA("East Africa"),
    NORTH_AFRICA("North Africa"),
    SOUTHERN_AFRICA("Southern Africa"),
    WEST_AFRICA("West Africa"),
    CENTRAL_AFRICA("Central Africa"),
    EUROPE("Europe"),
    AMERICAS("Americas"),
    ASIA("Asia"),
    OCEANIA("Oceania")
}

data class NationalityItem(
    val country: String,
    val code: String,
    val region: FootballRegion,
    val confederation: String
)

/**
 * Utility helper for nationality-related data and assets.
 */
object NationalityUtils {

    val nationalityItems = listOf(
        // East Africa (CAF/CECAFA)
        NationalityItem("Tanzania", "tz", FootballRegion.EAST_AFRICA, "CAF"),
        NationalityItem("Kenya", "ke", FootballRegion.EAST_AFRICA, "CAF"),
        NationalityItem("Uganda", "ug", FootballRegion.EAST_AFRICA, "CAF"),
        NationalityItem("Rwanda", "rw", FootballRegion.EAST_AFRICA, "CAF"),
        NationalityItem("Burundi", "bi", FootballRegion.EAST_AFRICA, "CAF"),
        NationalityItem("Ethiopia", "et", FootballRegion.EAST_AFRICA, "CAF"),
        NationalityItem("Somalia", "so", FootballRegion.EAST_AFRICA, "CAF"),
        NationalityItem("Eritrea", "er", FootballRegion.EAST_AFRICA, "CAF"),
        NationalityItem("South Sudan", "ss", FootballRegion.EAST_AFRICA, "CAF"),
        NationalityItem("Djibouti", "dj", FootballRegion.EAST_AFRICA, "CAF"),
        NationalityItem("Seychelles", "sc", FootballRegion.EAST_AFRICA, "CAF"),
        NationalityItem("Comoros", "km", FootballRegion.EAST_AFRICA, "CAF"),

        // North Africa (CAF/UNAF)
        NationalityItem("Morocco", "ma", FootballRegion.NORTH_AFRICA, "CAF"),
        NationalityItem("Algeria", "dz", FootballRegion.NORTH_AFRICA, "CAF"),
        NationalityItem("Tunisia", "tn", FootballRegion.NORTH_AFRICA, "CAF"),
        NationalityItem("Egypt", "eg", FootballRegion.NORTH_AFRICA, "CAF"),
        NationalityItem("Libya", "ly", FootballRegion.NORTH_AFRICA, "CAF"),
        NationalityItem("Sudan", "sd", FootballRegion.NORTH_AFRICA, "CAF"),

        // Southern Africa (CAF/COSAFA)
        NationalityItem("Angola", "ao", FootballRegion.SOUTHERN_AFRICA, "CAF"),
        NationalityItem("Zambia", "zm", FootballRegion.SOUTHERN_AFRICA, "CAF"),
        NationalityItem("Zimbabwe", "zw", FootballRegion.SOUTHERN_AFRICA, "CAF"),
        NationalityItem("Botswana", "bw", FootballRegion.SOUTHERN_AFRICA, "CAF"),
        NationalityItem("Namibia", "na", FootballRegion.SOUTHERN_AFRICA, "CAF"),
        NationalityItem("South Africa", "za", FootballRegion.SOUTHERN_AFRICA, "CAF"),
        NationalityItem("Mozambique", "mz", FootballRegion.SOUTHERN_AFRICA, "CAF"),
        NationalityItem("Malawi", "mw", FootballRegion.SOUTHERN_AFRICA, "CAF"),
        NationalityItem("Lesotho", "ls", FootballRegion.SOUTHERN_AFRICA, "CAF"),
        NationalityItem("Eswatini", "sz", FootballRegion.SOUTHERN_AFRICA, "CAF"),
        NationalityItem("Mauritius", "mu", FootballRegion.SOUTHERN_AFRICA, "CAF"),
        NationalityItem("Madagascar", "mg", FootballRegion.SOUTHERN_AFRICA, "CAF"),

        // West Africa (CAF/WAFU)
        NationalityItem("Nigeria", "ng", FootballRegion.WEST_AFRICA, "CAF"),
        NationalityItem("Ghana", "gh", FootballRegion.WEST_AFRICA, "CAF"),
        NationalityItem("Senegal", "sn", FootballRegion.WEST_AFRICA, "CAF"),
        NationalityItem("Ivory Coast", "ci", FootballRegion.WEST_AFRICA, "CAF"),
        NationalityItem("Mali", "ml", FootballRegion.WEST_AFRICA, "CAF"),
        NationalityItem("Mauritania", "mr", FootballRegion.WEST_AFRICA, "CAF"),
        NationalityItem("Gambia", "gm", FootballRegion.WEST_AFRICA, "CAF"),
        NationalityItem("Togo", "tg", FootballRegion.WEST_AFRICA, "CAF"),
        NationalityItem("Guinea-Bissau", "gw", FootballRegion.WEST_AFRICA, "CAF"),
        NationalityItem("Niger", "ne", FootballRegion.WEST_AFRICA, "CAF"),
        NationalityItem("Liberia", "lr", FootballRegion.WEST_AFRICA, "CAF"),
        NationalityItem("Cape Verde", "cv", FootballRegion.WEST_AFRICA, "CAF"),
        NationalityItem("Burkina Faso", "bf", FootballRegion.WEST_AFRICA, "CAF"),
        NationalityItem("Sierra Leone", "sl", FootballRegion.WEST_AFRICA, "CAF"),
        NationalityItem("Guinea", "gn", FootballRegion.WEST_AFRICA, "CAF"),
        NationalityItem("Benin", "bj", FootballRegion.WEST_AFRICA, "CAF"),

        // Central Africa (CAF/UNIFFAC)
        NationalityItem("Congo DRC", "cd", FootballRegion.CENTRAL_AFRICA, "CAF"),
        NationalityItem("Cameroon", "cm", FootballRegion.CENTRAL_AFRICA, "CAF"),
        NationalityItem("Equatorial Guinea", "gq", FootballRegion.CENTRAL_AFRICA, "CAF"),
        NationalityItem("Chad", "td", FootballRegion.CENTRAL_AFRICA, "CAF"),
        NationalityItem("Gabon", "ga", FootballRegion.CENTRAL_AFRICA, "CAF"),
        NationalityItem("Central African Republic", "cf", FootballRegion.CENTRAL_AFRICA, "CAF"),
        NationalityItem("Congo Republic", "cg", FootballRegion.CENTRAL_AFRICA, "CAF"),
        NationalityItem("Sao Tome and Principe", "st", FootballRegion.CENTRAL_AFRICA, "CAF"),

        // Europe (UEFA)
        NationalityItem("England", "gb-eng", FootballRegion.EUROPE, "UEFA"),
        NationalityItem("France", "fr", FootballRegion.EUROPE, "UEFA"),
        NationalityItem("Spain", "es", FootballRegion.EUROPE, "UEFA"),
        NationalityItem("Italy", "it", FootballRegion.EUROPE, "UEFA"),
        NationalityItem("Germany", "de", FootballRegion.EUROPE, "UEFA"),
        NationalityItem("Netherlands", "nl", FootballRegion.EUROPE, "UEFA"),
        NationalityItem("Portugal", "pt", FootballRegion.EUROPE, "UEFA"),
        NationalityItem("Türkiye", "tr", FootballRegion.EUROPE, "UEFA"),
        NationalityItem("Sweden", "se", FootballRegion.EUROPE, "UEFA"),
        NationalityItem("Croatia", "hr", FootballRegion.EUROPE, "UEFA"),
        NationalityItem("Scotland", "gb-sct", FootballRegion.EUROPE, "UEFA"),
        NationalityItem("Republic of Ireland", "ie", FootballRegion.EUROPE, "UEFA"),
        NationalityItem("Poland", "pl", FootballRegion.EUROPE, "UEFA"),
        NationalityItem("Ukraine", "ua", FootballRegion.EUROPE, "UEFA"),
        NationalityItem("Belgium", "be", FootballRegion.EUROPE, "UEFA"),
        NationalityItem("Denmark", "dk", FootballRegion.EUROPE, "UEFA"),
        NationalityItem("Wales", "gb-wls", FootballRegion.EUROPE, "UEFA"),
        NationalityItem("Northern Ireland", "gb-nir", FootballRegion.EUROPE, "UEFA"),
        NationalityItem("Norway", "no", FootballRegion.EUROPE, "UEFA"),
        NationalityItem("Switzerland", "ch", FootballRegion.EUROPE, "UEFA"),
        NationalityItem("Greece", "gr", FootballRegion.EUROPE, "UEFA"),
        NationalityItem("Austria", "at", FootballRegion.EUROPE, "UEFA"),
        NationalityItem("Finland", "fi", FootballRegion.EUROPE, "UEFA"),
        NationalityItem("Iceland", "is", FootballRegion.EUROPE, "UEFA"),
        NationalityItem("Czech Republic", "cz", FootballRegion.EUROPE, "UEFA"),
        NationalityItem("Slovakia", "sk", FootballRegion.EUROPE, "UEFA"),
        NationalityItem("Hungary", "hu", FootballRegion.EUROPE, "UEFA"),
        NationalityItem("Romania", "ro", FootballRegion.EUROPE, "UEFA"),
        NationalityItem("Bulgaria", "bg", FootballRegion.EUROPE, "UEFA"),
        NationalityItem("Serbia", "rs", FootballRegion.EUROPE, "UEFA"),
        NationalityItem("Bosnia and Herzegovina", "ba", FootballRegion.EUROPE, "UEFA"),
        NationalityItem("Montenegro", "me", FootballRegion.EUROPE, "UEFA"),
        NationalityItem("North Macedonia", "mk", FootballRegion.EUROPE, "UEFA"),
        NationalityItem("Slovenia", "si", FootballRegion.EUROPE, "UEFA"),
        NationalityItem("Estonia", "ee", FootballRegion.EUROPE, "UEFA"),
        NationalityItem("Latvia", "lv", FootballRegion.EUROPE, "UEFA"),
        NationalityItem("Lithuania", "lt", FootballRegion.EUROPE, "UEFA"),
        NationalityItem("Russia", "ru", FootballRegion.EUROPE, "UEFA"),
        NationalityItem("Belarus", "by", FootballRegion.EUROPE, "UEFA"),
        NationalityItem("Georgia", "ge", FootballRegion.EUROPE, "UEFA"),
        NationalityItem("Armenia", "am", FootballRegion.EUROPE, "UEFA"),
        NationalityItem("Azerbaijan", "az", FootballRegion.EUROPE, "UEFA"),
        NationalityItem("Israel", "il", FootballRegion.EUROPE, "UEFA"),
        NationalityItem("Kazakhstan", "kz", FootballRegion.EUROPE, "UEFA"),

        // Americas (CONMEBOL/CONCACAF)
        NationalityItem("United States", "us", FootballRegion.AMERICAS, "CONCACAF"),
        NationalityItem("Canada", "ca", FootballRegion.AMERICAS, "CONCACAF"),
        NationalityItem("Mexico", "mx", FootballRegion.AMERICAS, "CONCACAF"),
        NationalityItem("Brazil", "br", FootballRegion.AMERICAS, "CONMEBOL"),
        NationalityItem("Argentina", "ar", FootballRegion.AMERICAS, "CONMEBOL"),
        NationalityItem("Colombia", "co", FootballRegion.AMERICAS, "CONMEBOL"),
        NationalityItem("Peru", "pe", FootballRegion.AMERICAS, "CONMEBOL"),
        NationalityItem("Chile", "cl", FootballRegion.AMERICAS, "CONMEBOL"),
        NationalityItem("Uruguay", "uy", FootballRegion.AMERICAS, "CONMEBOL"),
        NationalityItem("Bolivia", "bo", FootballRegion.AMERICAS, "CONMEBOL"),
        NationalityItem("Paraguay", "py", FootballRegion.AMERICAS, "CONMEBOL"),
        NationalityItem("Ecuador", "ec", FootballRegion.AMERICAS, "CONMEBOL"),
        NationalityItem("Venezuela", "ve", FootballRegion.AMERICAS, "CONMEBOL"),
        NationalityItem("Jamaica", "jm", FootballRegion.AMERICAS, "CONCACAF"),
        NationalityItem("Costa Rica", "cr", FootballRegion.AMERICAS, "CONCACAF"),
        NationalityItem("Panama", "pa", FootballRegion.AMERICAS, "CONCACAF"),
        NationalityItem("Honduras", "hn", FootballRegion.AMERICAS, "CONCACAF"),
        NationalityItem("El Salvador", "sv", FootballRegion.AMERICAS, "CONCACAF"),
        NationalityItem("Guatemala", "gt", FootballRegion.AMERICAS, "CONCACAF"),
        NationalityItem("Cuba", "cu", FootballRegion.AMERICAS, "CONCACAF"),
        NationalityItem("Dominican Republic", "do", FootballRegion.AMERICAS, "CONCACAF"),
        NationalityItem("Haiti", "ht", FootballRegion.AMERICAS, "CONCACAF"),
        NationalityItem("Trinidad and Tobago", "tt", FootballRegion.AMERICAS, "CONCACAF"),
        NationalityItem("Guyana", "gy", FootballRegion.AMERICAS, "CONCACAF"),
        NationalityItem("Suriname", "sr", FootballRegion.AMERICAS, "CONCACAF"),

        // Asia (AFC)
        NationalityItem("Japan", "jp", FootballRegion.ASIA, "AFC"),
        NationalityItem("China", "cn", FootballRegion.ASIA, "AFC"),
        NationalityItem("India", "in", FootballRegion.ASIA, "AFC"),
        NationalityItem("South Korea", "kr", FootballRegion.ASIA, "AFC"),
        NationalityItem("Palestine", "ps", FootballRegion.ASIA, "AFC"),
        NationalityItem("Saudi Arabia", "sa", FootballRegion.ASIA, "AFC"),
        NationalityItem("United Arab Emirates", "ae", FootballRegion.ASIA, "AFC"),
        NationalityItem("Qatar", "qa", FootballRegion.ASIA, "AFC"),
        NationalityItem("Kuwait", "kw", FootballRegion.ASIA, "AFC"),
        NationalityItem("Oman", "om", FootballRegion.ASIA, "AFC"),
        NationalityItem("Jordan", "jo", FootballRegion.ASIA, "AFC"),
        NationalityItem("Lebanon", "lb", FootballRegion.ASIA, "AFC"),
        NationalityItem("Syria", "sy", FootballRegion.ASIA, "AFC"),
        NationalityItem("Iraq", "iq", FootballRegion.ASIA, "AFC"),
        NationalityItem("Iran", "ir", FootballRegion.ASIA, "AFC"),
        NationalityItem("Afghanistan", "af", FootballRegion.ASIA, "AFC"),
        NationalityItem("Pakistan", "pk", FootballRegion.ASIA, "AFC"),
        NationalityItem("Bangladesh", "bd", FootballRegion.ASIA, "AFC"),
        NationalityItem("Sri Lanka", "lk", FootballRegion.ASIA, "AFC"),
        NationalityItem("Nepal", "np", FootballRegion.ASIA, "AFC"),
        NationalityItem("Thailand", "th", FootballRegion.ASIA, "AFC"),
        NationalityItem("Vietnam", "vn", FootballRegion.ASIA, "AFC"),
        NationalityItem("Indonesia", "id", FootballRegion.ASIA, "AFC"),
        NationalityItem("Malaysia", "my", FootballRegion.ASIA, "AFC"),
        NationalityItem("Singapore", "sg", FootballRegion.ASIA, "AFC"),
        NationalityItem("Philippines", "ph", FootballRegion.ASIA, "AFC"),
        NationalityItem("Taiwan", "tw", FootballRegion.ASIA, "AFC"),
        NationalityItem("Hong Kong", "hk", FootballRegion.ASIA, "AFC"),
        NationalityItem("Uzbekistan", "uz", FootballRegion.ASIA, "AFC"),
        NationalityItem("Turkmenistan", "tm", FootballRegion.ASIA, "AFC"),
        NationalityItem("Kyrgyzstan", "kg", FootballRegion.ASIA, "AFC"),
        NationalityItem("Tajikistan", "tj", FootballRegion.ASIA, "AFC"),
        NationalityItem("Mongolia", "mn", FootballRegion.ASIA, "AFC"),
        NationalityItem("North Korea", "kp", FootballRegion.ASIA, "AFC"),
        NationalityItem("Australia", "au", FootballRegion.ASIA, "AFC"),

        // Oceania (OFC)
        NationalityItem("New Zealand", "nz", FootballRegion.OCEANIA, "OFC"),
        NationalityItem("Fiji", "fj", FootballRegion.OCEANIA, "OFC"),
        NationalityItem("Papua New Guinea", "pg", FootballRegion.OCEANIA, "OFC")
    )

    /**
     * Backward compatible list of nationalities for the FAME 2026 ecosystem.
     */
    val nationalities: List<String> by lazy {
        nationalityItems.map { it.country }.distinct().sorted()
    }

    private val countryToCode: Map<String, String> by lazy {
        nationalityItems.associate { it.country to it.code }
    }

    /**
     * Returns the asset URL for a nationality flag.
     * Flags are expected to be in "assets/flags/" and named exactly as the country name (e.g., "Tanzania.png").
     *
     * @param nationality The exact name of the country/nationality.
     * @return The formatted file URL for the asset.
     */
    fun getFlagUrl(nationality: String): String {
        return "file:///android_asset/flags/$nationality.png"
    }

    /**
     * Returns the asset URL for a waving nationality flag.
     * Flags are expected to be in "assets/flags_waving/" and named as 2-letter codes (e.g., "tz.png").
     *
     * @param nationality The exact name of the country/nationality.
     * @return The formatted file URL for the asset.
     */
    fun getWavingFlagUrl(nationality: String): String {
        val code = countryToCode[nationality] ?: "default"
        return "file:///android_asset/flags_waving/$code.png"
    }

    /**
     * Returns the NationalityItem for a given country name.
     */
    fun getNationalityItem(country: String): NationalityItem? {
        return nationalityItems.find { it.country == country }
    }
}
