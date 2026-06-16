package com.fameafrica.afm.utils.constants

/**
 * Helper class containing metadata and simulation parameters for African Leagues and Cups.
 * Data is calibrated based on historical trends from footystats.org.
 */
object AfricanFootballDataHelper {

    data class CompetitionMetadata(
        val name: String,
        val country: String,
        val countryId: Int?,
        val level: Int,
        val sponsor: String,
        val prizeMoney: Long,
        // Simulation modifiers
        val homeAdvantageWeight: Double = 1.1, // 1.0 is neutral
        val averageGoalsModifier: Double = 1.0, // 1.0 is ~2.4 goals per game
        val physicalityModifier: Double = 1.0,  // 1.0 is normal foul/card rate
        val drawingTendency: Double = 1.0       // High value = more draws (e.g., Tunisia, Algeria)
    )

    val LEAGUES = mapOf(
        "Tanzania Premier League" to CompetitionMetadata("Tanzania Premier League", "Tanzania", 1, 1, "NBC", 230400, 1.15, 0.95, 1.1, 1.05),
        "Tanzania Championship League" to CompetitionMetadata("Tanzania Championship League", "Tanzania", 1, 2, "NBC", 92160, 1.2, 0.9, 1.2, 1.1),
        "Tanzania First League" to CompetitionMetadata("Tanzania First League", "Tanzania", 1, 3, "CRDB", 46080, 1.2, 0.85, 1.3, 1.15),
        "Tanzania Regional Champions League" to CompetitionMetadata("Tanzania Regional Champions League", "Tanzania", 1, 4, "CRDB", 23040, 1.2, 0.8, 1.4, 1.2),
        "Zanzibar Premier League" to CompetitionMetadata("Zanzibar Premier League", "Zanzibar", 1, 1, "PBZ", 115200, 1.1, 1.0, 1.0, 1.0),
        "Zanzibar Championship League" to CompetitionMetadata("Zanzibar Championship League", "Zanzibar", 1, 2, "PBZ", 28800, 1.15, 0.95, 1.1, 1.05),
        "Tanzania Regional Division League 2" to CompetitionMetadata("Tanzania Regional Division League 2", "Tanzania", 1, 5, "CRDB", 11520, 1.25, 0.75, 1.5, 1.25),
        "Egyptian Premier League" to CompetitionMetadata("Egyptian Premier League", "Egypt", 25, 1, "Vodafone", 480000, 1.1, 0.95, 0.9, 1.2),
        "Morocco Botola Pro" to CompetitionMetadata("Morocco Botola Pro", "Morocco", 17, 1, "Inwi", 572160, 1.12, 0.9, 1.0, 1.25),
        "Algeria League 1" to CompetitionMetadata("Algeria Ligue 1", "Algeria", 23, 1, "Mobilis", 250000, 1.2, 0.85, 1.1, 1.3),
        "South African PSL" to CompetitionMetadata("South African PSL", "South Africa", 19, 1, "ABSA", 770880, 1.05, 1.05, 0.8, 1.1),
        "Tunisia Ligue 1" to CompetitionMetadata("Tunisia Ligue 1", "Tunisia", 24, 1, "Halkbank", 288000, 1.15, 0.8, 1.2, 1.4),
        "Congo DR Super League" to CompetitionMetadata("Congo DR Super League", "Congo DRC", 6, 1, "Vodacom", 150000, 1.2, 1.0, 1.2, 1.1),
        "Kenyan Premier League" to CompetitionMetadata("Kenyan Premier League", "Kenya", 2, 1, "SportPesa", 96339, 1.1, 1.0, 1.0, 1.1),
        "Uganda Premier League" to CompetitionMetadata("Uganda Premier League", "Uganda", 3, 1, "StarTimes", 50000, 1.1, 0.95, 1.1, 1.1),
        "Sudani Premier League" to CompetitionMetadata("Sudani Premier League", "Sudan", 41, 1, "MTN", 90000, 1.2, 0.9, 1.1, 1.15),
        "Angola Girabola" to CompetitionMetadata("Angola Girabola", "Angola", 26, 1, "Unitel", 147976, 1.15, 0.9, 1.1, 1.2),
        "Nigerian NPFL" to CompetitionMetadata("Nigerian NPFL", "Nigeria", 12, 1, "Bet9ja", 182400, 1.3, 0.9, 1.2, 1.1),
        "Ivory Coast League 1" to CompetitionMetadata("Ivory Coast League 1", "Ivory Coast", 14, 1, "MTN", 120000, 1.1, 0.95, 1.1, 1.2),
        "South Sudan Premier League" to CompetitionMetadata("South Sudan Premier League", "South Sudan", 20, 1, "MTN", 10000, 1.15, 1.0, 1.1, 1.05),
        "Senegal Premier League" to CompetitionMetadata("Senegal Premier League", "Senegal", 15, 1, "Orange", 140000, 1.1, 0.85, 1.1, 1.3),
        "Botswana Premier League" to CompetitionMetadata("Botswana Premier League", "Botswana", 22, 1, "BTC", 52800, 1.1, 1.0, 1.0, 1.1),
        "Rwanda Premier League" to CompetitionMetadata("Rwanda Premier League", "Rwanda", 4, 1, "Primus", 70000, 1.1, 0.95, 1.1, 1.1),
        "Cameroon Elite One" to CompetitionMetadata("Cameroon Elite One", "Cameroon", 13, 1, "MTN", 152000, 1.1, 0.95, 1.2, 1.1),
        "Mozambique Mocambola" to CompetitionMetadata("Mozambique Mocambola", "Mozambique", 21, 1, "Mozal", 60000, 1.15, 0.9, 1.1, 1.15),
        "Burundi Premier League" to CompetitionMetadata("Burundi Premier League", "Burundi", 5, 1, "Primus", 50000, 1.1, 1.0, 1.1, 1.05),
        "Malawi Super League" to CompetitionMetadata("Malawi Super League", "Malawi", 18, 1, "TNM", 45000, 1.1, 1.0, 1.1, 1.1),
        "Zambia Super League" to CompetitionMetadata("Zambia Super League", "Zambia", 8, 1, "MTN", 90000, 1.1, 1.0, 1.0, 1.1),
        "Guinea League 1 Pro" to CompetitionMetadata("Guinea League 1 Pro", "Guinea", 31, 1, "Orange", 70000, 1.15, 0.95, 1.2, 1.1),
        "Libyan Premier League" to CompetitionMetadata("Libyan Premier League", "Libya", 53, 1, "Libyana", 85000, 1.2, 0.9, 1.2, 1.2),
        "Malian Première Division" to CompetitionMetadata("Malian Première Division", "Mali", 16, 1, "Orange", 75000, 1.1, 0.85, 1.1, 1.3),
        "Zimbabwe Premier Soccer League" to CompetitionMetadata("Zimbabwe Premier Soccer League", "Zimbabwe", 9, 1, "Delta", 70000, 1.1, 0.95, 1.1, 1.1),
        "Ghana Premier League" to CompetitionMetadata("Ghana Premier League", "Ghana", 11, 1, "BetPawa", 150000, 1.15, 0.9, 1.1, 1.2),
        "Congo Premier League" to CompetitionMetadata("Congo Premier League", "Congo Republic", 7, 1, "MTN", 80000, 1.15, 0.95, 1.1, 1.15),
        "Ethiopia Premier League" to CompetitionMetadata("Ethiopia Premier League", "Ethiopia", 117, 1, "Ethio Telecom", 187200, 1.1, 1.1, 0.9, 1.05),
        "Somali First Division" to CompetitionMetadata("Somali First Division", "Somalia", 40, 1, "Hormuud", 15000, 1.1, 1.05, 1.1, 1.05),
        "CAR Premier League" to CompetitionMetadata("CAR Premier League", "Central African Republic", 33, 1, "Orange", 25000, 1.15, 0.95, 1.1, 1.1),
        "Niger Super League" to CompetitionMetadata("Niger Super League", "Niger", 46, 1, "MTN", 20000, 1.15, 0.9, 1.1, 1.2),
        "Gambia GFA Premier League" to CompetitionMetadata("Gambia GFA Premier League", "Gambia", 30, 1, "Gambia Ports Authority", 25000, 1.1, 0.95, 1.1, 1.2),
        "Burkinabe Premier League" to CompetitionMetadata("Burkinabe Premier League", "Burkina Faso", 10, 1, "Orange", 20000, 1.1, 0.9, 1.1, 1.2),
        "Liberia First Division" to CompetitionMetadata("Liberia First Division", "Liberia", 42, 1, "Lonestar", 15000, 1.1, 1.05, 1.1, 1.1),
        "Sierra Leone National Premier League" to CompetitionMetadata("Sierra Leone National Premier League", "Sierra Leone", 43, 1, "Africell", 15000, 1.1, 1.0, 1.1, 1.1),
        "Togo Championnat National" to CompetitionMetadata("Togo Championnat National", "Togo", 45, 1, "Moov", 15000, 1.1, 0.9, 1.1, 1.2),
        "Madagascar Pro League" to CompetitionMetadata("Madagascar Pro League", "Madagascar", 47, 1, "Orange", 20000, 1.1, 1.0, 1.0, 1.1),
        "Mauritania Super D1 League" to CompetitionMetadata("Mauritania Super D1 League", "Mauritania", 48, 1, "Mauritel", 20000, 1.1, 0.85, 1.1, 1.3),
        "Mauritius Super League" to CompetitionMetadata("Mauritius Super League", "Mauritius", 49, 1, "Orange", 15000, 1.05, 1.1, 0.9, 1.05),
        "Benin Championnat National" to CompetitionMetadata("Benin Championnat National", "Benin", 44, 1, "Moov", 15000, 1.1, 0.9, 1.1, 1.2),
        "Chad Premiere Division" to CompetitionMetadata("Chad Premiere Division", "Chad", 35, 1, "Tigo", 15000, 1.15, 0.9, 1.2, 1.15),
        "Namibian Premier League" to CompetitionMetadata("Namibian Premier League", "Namibia", 27, 1, "MTC", 15000, 1.1, 1.0, 1.0, 1.1),
        "Cape Verde Campionato Nacional" to CompetitionMetadata("Cape Verde Campionato Nacional", "Cape Verde", 51, 1, "Cabo Verde Telecom", 15000, 1.1, 1.1, 0.9, 1.05),
        "Lesotho Premier League" to CompetitionMetadata("Lesotho Premier League", "Lesotho", 28, 1, "Lesotho Post", 15000, 1.1, 0.95, 1.1, 1.1),
        "Djibouti Division 1" to CompetitionMetadata("Djibouti Division 1", "Djibouti", 37, 1, "Djibouti Telecom", 15000, 1.1, 1.1, 0.9, 1.05),
        "Equatorial Guinea Premier League" to CompetitionMetadata("Equatorial Guinea Premier League", "Equatorial Guinea", 34, 1, "GEPetrol", 15000, 1.15, 0.95, 1.2, 1.1)
    )

    data class CupMetadata(
        val name: String,
        val type: String, // National, Continental, Local, International
        val country: String? = null,
        val countryId: Int? = null,
        val sponsor: String,
        val prizeMoney: Long,
        val teamsInvolved: Int,
        val rules: String,
        val homeAdvantageWeight: Double = 1.05,
        val intensityModifier: Double = 1.2 // Cups are more intense
    )

    val CUPS = mapOf(
        "CAF Confederation Cup" to CupMetadata("CAF Confederation Cup", "Continental", "Africa", null, "CAF", 2500000, 16, "Format: Preliminary Rounds → Playoff Round → Group Stage → Knockout Phase. Teams: Clubs that win domestic cups or finish high in leagues.", 1.1, 1.3),
        "Muungano Cup" to CupMetadata("Muungano Cup", "National", "Tanzania", 1, "Azam Media", 80000, 8, "Format: Single-Elimination (Knockout). Teams: 8 clubs. Top 4 teams from TPL and Top 4 from ZPL.", 1.05, 1.2),
        "CRDB Federations Cup" to CupMetadata("CRDB Federations Cup", "National", "Tanzania", 1, "CRDB Bank", 80000, 64, "Format: Single-Elimination (Knockout). Teams: 64 clubs. All tiers included. Winner qualifies for CAF CC.", 1.1, 1.25),
        "CAF Champions League Qualification" to CupMetadata("CAF Champions League Qualification", "Continental", "Africa", null, "Total Energies", 700132, 59, "Preliminary rounds determine entry into Group Stage. Playoffs held before group stage.", 1.15, 1.4),
        "CAF Confederation Cup Qualification" to CupMetadata("CAF Confederation Cup Qualification", "Continental", "Africa", null, "Total Energies", 400075, 52, "Similar format to CAF CL Qualification. Includes teams from CAF CL Playoff.", 1.15, 1.4),
        "PBZ Cup" to CupMetadata("PBZ Cup", "National", "Tanzania", 1, "PBZ Bank", 40000, 32, "Format: Single-Elimination (Knockout). Teams: 32 clubs. ZPL and ZCL teams. Winner qualifies for CAF CC.", 1.1, 1.2),
        "AFCON" to CupMetadata("AFCON", "Continental", "Africa", null, "Total Energies", 7131718, 24, "The premier national team competition in Africa. 24 teams qualify. Held every 2 years.", 1.0, 1.5),
        "FAME Africa™ Cup" to CupMetadata("FAME Africa™ Cup", "Local", "Tanzania", 1, "FAME Africa™", 20000, 16, "Format: Single-Elimination (Knockout). Teams: 16 clubs. Teams from Tanzania Regional Division League 2.", 1.05, 1.2),
        "AFCON Qualifiers" to CupMetadata("AFCON Qualifiers", "Continental", "Africa", null, "Total Energies", 535028, 48, "Determines teams for AFCON. Group-stage qualification.", 1.1, 1.3),
        "FIFA Club World Cup" to CupMetadata("FIFA Club World Cup", "International", "Global", null, "The Coca-Cola Company", 101881695, 32, "Winners of CAF CL qualify. Competes against other continental champions.", 1.0, 1.4),
        "FIFA World Cup" to CupMetadata("FIFA World Cup", "International", "Global", null, "The Coca-Cola Company", 913615183, 32, "AFCON determines CAF qualifiers. Top teams qualify for the World Cup.", 1.0, 1.6),
        "FIFA World Cup Qualification CAF" to CupMetadata("FIFA World Cup Qualification CAF", "International", "Africa", null, "VISA", 9556840, 54, "Qualifying for the World Cup.", 1.15, 1.4),
        "CAF Super Cup" to CupMetadata("CAF Super Cup", "Continental", "Africa", null, "Total Energies", 462427, 2, "Winner of CAF CL vs Winner of CAF CC.", 1.0, 1.5),
        "Egyptian FA Cup" to CupMetadata("Egyptian FA Cup", "National", "Egypt", 25, "", 80000, 18, "Format: Single-Elimination (Knockout)", 1.05, 1.2),
        "Morocco FA Cup" to CupMetadata("Morocco FA Cup", "National", "Morocco", 17, "", 75000, 16, "Format: Single-Elimination (Knockout)", 1.05, 1.2),
        "Algerian FA Cup" to CupMetadata("Algerian FA Cup", "National", "Algeria", 23, "", 70000, 16, "Format: Single-Elimination (Knockout)", 1.1, 1.3),
        "South African FA Cup" to CupMetadata("South African FA Cup", "National", "South Africa", 19, "", 90000, 16, "Format: Single-Elimination (Knockout)", 1.05, 1.2),
        "Tunisian FA Cup" to CupMetadata("Tunisian FA Cup", "National", "Tunisia", 24, "", 65000, 16, "Format: Single-Elimination (Knockout)", 1.1, 1.3),
        "Coupe du Congo DRC" to CupMetadata("Coupe du Congo DRC", "National", "Congo DRC", 6, "", 60000, 25, "Format: Single-Elimination (Knockout)", 1.1, 1.3),
        "Kenyan FA Cup" to CupMetadata("Kenyan FA Cup", "National", "Kenya", 2, "", 40000, 18, "Format: Single-Elimination (Knockout)", 1.05, 1.2),
        "Ugandan FA Cup" to CupMetadata("Ugandan FA Cup", "National", "Uganda", 3, "", 30000, 16, "Format: Single-Elimination (Knockout)", 1.05, 1.2),
        "Sudani FA Cup" to CupMetadata("Sudani FA Cup", "National", "Sudan", 41, "", 40000, 25, "Format: Single-Elimination (Knockout)", 1.1, 1.3),
        "Angola FA Cup" to CupMetadata("Angola FA Cup", "National", "Angola", 26, "", 30000, 16, "Format: Single-Elimination (Knockout)", 1.1, 1.2),
        "Nigerian FA Cup" to CupMetadata("Nigerian FA Cup", "National", "Nigeria", 12, "", 35000, 20, "Format: Single-Elimination (Knockout)", 1.1, 1.3),
        "Côte d'Ivoire FA Cup" to CupMetadata("Côte d'Ivoire FA Cup", "National", "Ivory Coast", 14, "", 30000, 18, "Format: Single-Elimination (Knockout)", 1.05, 1.2),
        "South Sudan FA Cup" to CupMetadata("South Sudan FA Cup", "National", "South Sudan", 20, "", 20000, 14, "Format: Single-Elimination (Knockout)", 1.1, 1.2),
        "Coupe du Senegal" to CupMetadata("Coupe du Senegal", "National", "Senegal", 15, "", 25000, 16, "Format: Single-Elimination (Knockout)", 1.1, 1.2),
        "Botswana FA Cup" to CupMetadata("Botswana FA Cup", "National", "Botswana", 22, "", 20000, 16, "Format: Single-Elimination (Knockout)", 1.05, 1.2),
        "Rwandan FA Cup" to CupMetadata("Rwandan FA Cup", "National", "Rwanda", 4, "", 20000, 16, "Format: Single-Elimination (Knockout)", 1.05, 1.2),
        "Cameroon FA Cup" to CupMetadata("Cameroon FA Cup", "National", "Cameroon", 13, "", 30000, 16, "Format: Single-Elimination (Knockout)", 1.1, 1.3),
        "Mozambique FA Cup" to CupMetadata("Mozambique FA Cup", "National", "Mozambique", 21, "", 15000, 14, "Format: Single-Elimination (Knockout)", 1.1, 1.2),
        "Burundi FA Cup" to CupMetadata("Burundi FA Cup", "National", "Burundi", 5, "", 15000, 16, "Format: Single-Elimination (Knockout)", 1.05, 1.2),
        "Malawi Super Cup" to CupMetadata("Malawi Super Cup", "National", "Malawi", 18, "", 10000, 16, "Format: Single-Elimination (Knockout)", 1.05, 1.2),
        "Zambian FA Cup" to CupMetadata("Zambian FA Cup", "National", "Zambia", 8, "", 20000, 18, "Format: Single-Elimination (Knockout)", 1.1, 1.2),
        "Copa de Guinea" to CupMetadata("Copa de Guinea", "National", "Guinea", 31, "", 15000, 14, "Format: Single-Elimination (Knockout)", 1.1, 1.2),
        "Libyan FA Cup" to CupMetadata("Libyan FA Cup", "National", "Libya", 53, "", 12000, 12, "Format: Single-Elimination (Knockout)", 1.1, 1.3),
        "Coupe du Mali" to CupMetadata("Coupe du Mali", "National", "Mali", 16, "", 15000, 14, "Format: Single-Elimination (Knockout)", 1.1, 1.2),
        "Zimbabwean FA Cup" to CupMetadata("Zimbabwean FA Cup", "National", "Zimbabwe", 9, "", 20000, 18, "Format: Single-Elimination (Knockout)", 1.05, 1.2),
        "Ghana FA Cup" to CupMetadata("Ghana FA Cup", "National", "Ghana", 11, "", 25000, 18, "Format: Single-Elimination (Knockout)", 1.05, 1.2),
        "Coupe du Congo" to CupMetadata("Coupe du Congo", "National", "Congo Republic", 7, "", 15000, 17, "Format: Single-Elimination (Knockout)", 1.1, 1.2),
        "Ethiopian FA Cup" to CupMetadata("Ethiopian FA Cup", "National", "Ethiopia", 117, "", 15000, 20, "Format: Single-Elimination (Knockout)", 1.05, 1.2),
        "Somali FA Cup" to CupMetadata("Somali FA Cup", "National", "Somalia", 40, "", 10000, 12, "Format: Single-Elimination (Knockout)", 1.05, 1.2),
        "CAR FA Cup" to CupMetadata("CAR FA Cup", "National", "Central African Republic", 33, "", 10000, 13, "Format: Single-Elimination (Knockout)", 1.1, 1.2),
        "Coupe du Niger" to CupMetadata("Coupe du Niger", "National", "Niger", 46, "", 12000, 16, "Format: Single-Elimination (Knockout)", 1.1, 1.2),
        "Gambian FA Cup" to CupMetadata("Gambian FA Cup", "National", "Gambia", 30, "", 12000, 16, "Format: Single-Elimination (Knockout)", 1.05, 1.2),
        "Coupe du Burkina Faso" to CupMetadata("Coupe du Burkina Faso", "National", "Burkina Faso", 10, "", 12000, 16, "Format: Single-Elimination (Knockout)", 1.1, 1.2),
        "Liberian FA Cup" to CupMetadata("Liberian FA Cup", "National", "Liberia", 42, "", 10000, 15, "Format: Single-Elimination (Knockout)", 1.05, 1.2),
        "Sierra Leone FA Cup" to CupMetadata("Sierra Leone FA Cup", "National", "Sierra Leone", 43, "", 12000, 18, "Format: Single-Elimination (Knockout)", 1.05, 1.2),
        "Coupe du Togo" to CupMetadata("Coupe du Togo", "National", "Togo", 45, "", 10000, 14, "Format: Single-Elimination (Knockout)", 1.05, 1.2),
        "Coupe de Madagascar" to CupMetadata("Coupe de Madagascar", "National", "Madagascar", 47, "", 8000, 12, "Format: Single-Elimination (Knockout)", 1.05, 1.2),
        "Coupe du Président de la République" to CupMetadata("Coupe du Président de la République", "National", "Mauritania", 48, "", 8000, 14, "Format: Single-Elimination (Knockout)", 1.1, 1.3),
        "Mauritius FA Cup" to CupMetadata("Mauritius FA Cup", "National", "Mauritius", 49, "", 6000, 10, "Format: Single-Elimination (Knockout)", 1.0, 1.2),
        "Coupe du Bénin" to CupMetadata("Coupe du Bénin", "National", "Benin", 44, "", 8000, 18, "Format: Single-Elimination (Knockout)", 1.05, 1.2),
        "Coupe du Tchad" to CupMetadata("Coupe du Tchad", "National", "Chad", 35, "", 7000, 14, "Format: Single-Elimination (Knockout)", 1.1, 1.2),
        "NFA Cup" to CupMetadata("NFA Cup", "National", "Namibia", 27, "", 8000, 16, "Format: Single-Elimination (Knockout)", 1.05, 1.2),
        "Swazi FA Cup" to CupMetadata("Swazi FA Cup", "National", "Eswatini", 29, "", 8000, 16, "Format: Single-Elimination (Knockout)", 1.05, 1.2),
        "Taça de Cabo Verde" to CupMetadata("Taça de Cabo Verde", "National", "Cape Verde", 51, "", 6000, 10, "Format: Single-Elimination (Knockout)", 1.05, 1.2),
        "Lesotho FA Cup" to CupMetadata("Lesotho FA Cup", "National", "Lesotho", 28, "", 6000, 16, "Format: Single-Elimination (Knockout)", 1.05, 1.2),
        "Coupe de Djibouti" to CupMetadata("Coupe de Djibouti", "National", "Djibouti", 37, "", 5000, 10, "Format: Single-Elimination (Knockout)", 1.05, 1.2),
        "Copa de Guinea Equatorial" to CupMetadata("Copa de Guinea Equatorial", "National", "Equatorial Guinea", 34, "", 15000, 24, "Format: Single-Elimination (Knockout)", 1.1, 1.3)
    )

    fun getLeagueMetadata(name: String): CompetitionMetadata? = LEAGUES[name] ?: LEAGUES.values.find { name.contains(it.name, ignoreCase = true) }
    
    fun getCupMetadata(name: String): CupMetadata? = CUPS[name] ?: CUPS.values.find { name.contains(it.name, ignoreCase = true) }

    fun getSimulationParams(competitionName: String?): SimulationParams {
        if (competitionName == null) return SimulationParams()

        val league = getLeagueMetadata(competitionName)
        if (league != null) {
            return SimulationParams(
                homeAdvantage = league.homeAdvantageWeight,
                goalFrequency = league.averageGoalsModifier,
                foulFrequency = league.physicalityModifier,
                drawTendency = league.drawingTendency
            )
        }

        val cup = getCupMetadata(competitionName)
        if (cup != null) {
            return SimulationParams(
                homeAdvantage = cup.homeAdvantageWeight,
                goalFrequency = 1.0,
                foulFrequency = cup.intensityModifier,
                drawTendency = 0.9 // Fewer draws in cups usually, or handled by ET
            )
        }

        return SimulationParams()
    }

    data class SimulationParams(
        val homeAdvantage: Double = 1.1,
        val goalFrequency: Double = 1.0,
        val foulFrequency: Double = 1.0,
        val drawTendency: Double = 1.0
    )
}
