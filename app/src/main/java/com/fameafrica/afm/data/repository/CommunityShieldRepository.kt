package com.fameafrica.afm.data.repository

import com.fameafrica.afm.data.database.dao.CommunityShieldDao
import com.fameafrica.afm.data.database.dao.ShieldWinnerStats
import com.fameafrica.afm.data.database.entities.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class CommunityShieldRepository @Inject constructor(
    private val communityShieldDaoProvider: Provider<CommunityShieldDao>,
    private val leaguesRepository: LeaguesRepository,
    private val teamsRepository: TeamsRepository,
    private val fixturesRepository: FixturesRepository,
    private val leagueStandingsRepository: LeagueStandingsRepository
) {
    private val communityShieldDao get() = communityShieldDaoProvider.get()

    // ============ BASIC CRUD ============

    fun getAllShields(): Flow<List<CommunityShieldEntity>> = communityShieldDao.getAll()

    suspend fun getShieldById(id: Int): CommunityShieldEntity? = communityShieldDao.getById(id)

    suspend fun getShieldByLeagueAndSeason(leagueId: Int, season: String): CommunityShieldEntity? =
        communityShieldDao.getByLeagueAndSeason(leagueId, season)

    suspend fun insertShield(shield: CommunityShieldEntity) = communityShieldDao.insert(shield)

    suspend fun updateShield(shield: CommunityShieldEntity) = communityShieldDao.update(shield)

    suspend fun deleteShield(shield: CommunityShieldEntity) = communityShieldDao.delete(shield)

    // ============ SEASON OPENING LOGIC ============

    /**
     * Generate Community Shield for a new season
     * Called AFTER preseason tour/friendlies and BEFORE league starts
     *
     * TIMING: Preseason (July) → Community Shield (Early August) → League Starts (Mid-Late August)
     */
    suspend fun generateSeasonOpeningShield(
        leagueName: String,
        season: String,
        previousSeason: String
    ): CommunityShieldEntity? {

        val league = leaguesRepository.getLeagueByName(leagueName) ?: return null

        // Check if already exists for this season
        val existing = communityShieldDao.getByLeagueAndSeason(league.id, season)
        if (existing != null) return existing

        // Get previous season's final standings
        val previousSeasonYear = previousSeason.split("/").first().toInt()
        val standings = leagueStandingsRepository.getStandings(leagueName, previousSeasonYear)
            .firstOrNull() ?: return null

        if (standings.size < 2) return null

        // Determine format based on league
        val format = determineShieldFormat(leagueName)

        // Get participants
        val champion = standings.firstOrNull { it.position == 1 }
        val runnerUp = standings.firstOrNull { it.position == 2 }
        val third = standings.firstOrNull { it.position == 3 }
        val fourth = standings.firstOrNull { it.position == 4 }

        // Get team IDs
        val championTeam = champion?.let { teamsRepository.getTeamByName(it.teamName) }
        val runnerUpTeam = runnerUp?.let { teamsRepository.getTeamByName(it.teamName) }
        val thirdTeam = third?.let { teamsRepository.getTeamByName(it.teamName) }
        val fourthTeam = fourth?.let { teamsRepository.getTeamByName(it.teamName) }

        // Calculate match date (early August, after preseason)
        val matchDate = calculateShieldDate(season)

        // Determine home and away teams for final
        val (homeTeamName, awayTeamName) = determineHomeAndAway(champion, runnerUp, format)
        val homeTeam = if (homeTeamName == champion?.teamName) championTeam else runnerUpTeam
        val awayTeam = if (awayTeamName == runnerUp?.teamName) runnerUpTeam else championTeam

        // Get country-specific TV channel
        val tvChannel = getTvChannelForLeague(league)

        // Create shield entity
        val shield = CommunityShieldEntity(
            leagueId = league.id,
            leagueName = leagueName,
            season = season,
            matchDate = matchDate,
            leagueWinnerId = championTeam?.id,
            leagueWinner = champion?.teamName,
            leagueRunnerUpId = runnerUpTeam?.id,
            leagueRunnerUp = runnerUp?.teamName,
            leagueThirdId = thirdTeam?.id,
            leagueThird = third?.teamName,
            leagueFourthId = fourthTeam?.id,
            leagueFourth = fourth?.teamName,
            participantsFormat = format.value,
            homeTeam = homeTeamName,
            awayTeam = awayTeamName,
            prizeMoney = calculatePrizeMoney(league),
            stadium = getStadiumForShield(homeTeamName, leagueName),
            tvChannel = tvChannel,
            notes = "Season ${season} Opening Match"
        )

        communityShieldDao.insert(shield)

        // Fetch inserted shield to get its generated ID if needed, though communityShieldDao.insert might not return it if not defined.
        // Usually, it's better to fetch it again or define @Insert to return Long.
        val newlyInserted = communityShieldDao.getByLeagueAndSeason(league.id, season)
        if (newlyInserted != null) {
            // Create fixture for the shield match with country-specific TV channel
            createShieldFixture(newlyInserted.id, tvChannel)
        }

        return newlyInserted
    }

    /**
     * Get country-specific TV channel for the Community Shield
     */
    private fun getTvChannelForLeague(league: LeaguesEntity): String {
        return when (league.countryId) {
            1 -> { // Tanzania & Zanzibar
                when {
                    league.name.contains("Zanzibar") -> "ZBC TV"
                    else -> "Azam Sports TV"
                }
            }
            2 -> "Citizen TV" // Kenya
            3 -> "NBS Sport" // Uganda
            4 -> "Rwanda TV" // Rwanda
            5 -> "RTNB" // Burundi
            6 -> "RTNC" // Congo DRC
            7 -> "Tele Congo" // Congo Republic
            8 -> "ZNBC" // Zambia
            9 -> "ZBC TV" // Zimbabwe
            10 -> "RTB" // Burkina Faso
            11 -> "GTV" // Ghana
            12 -> "SuperSport Nigeria" // Nigeria
            13 -> "CRTV" // Cameroon
            14 -> "RTI" // Ivory Coast
            15 -> "RTS" // Senegal
            16 -> "ORTM" // Mali
            17 -> "SNRT" // Morocco
            18 -> "MBC" // Malawi
            19 -> "SuperSport SA" // South Africa
            20 -> "SSBC" // South Sudan
            21 -> "TVM" // Mozambique
            22 -> "BTV" // Botswana
            23 -> "EPTV" // Algeria
            24 -> "Tunisia TV" // Tunisia
            25 -> "ONTime Sports" // Egypt
            26 -> "TPA" // Angola
            27 -> "NBC Namibia" // Namibia
            28 -> "LTV" // Lesotho
            30 -> "GRTS" // Gambia
            31 -> "RTG" // Guinea
            33 -> "TCF" // Central African Republic
            34 -> "TVGE" // Equatorial Guinea
            35 -> "RTV" // Chad
            37 -> "RTD" // Djibouti
            40 -> "SNTV" // Somalia
            41 -> "Sudan TV" // Sudan
            42 -> "LBS" // Liberia
            43 -> "SLBC" // Sierra Leone
            44 -> "ORTB" // Benin
            45 -> "TVT" // Togo
            46 -> "ORTN" // Niger
            47 -> "TVM" // Madagascar
            48 -> "TVM" // Mauritania
            49 -> "MBC" // Mauritius
            51 -> "TCV" // Cape Verde
            53 -> "Libya TV" // Libya
            117 -> "ETV" // Ethiopia
            else -> "Azam Sports TV" // Default
        }
    }

    /**
     * Get country-specific stadium for the Community Shield
     */
    private suspend fun getStadiumForShield(homeTeam: String?, leagueName: String): String? {
        if (homeTeam != null) {
            val team = teamsRepository.getTeamByName(homeTeam)
            if (team?.homeStadium != null) return team.homeStadium
        }

        // Neutral venues by country
        return when {
            leagueName.contains("Tanzania") -> "Benjamin Mkapa Stadium, Dar es Salaam"
            leagueName.contains("Zanzibar") -> "Amaan Stadium, Zanzibar"
            leagueName.contains("Kenya") -> "Moi International Sports Centre, Nairobi"
            leagueName.contains("Uganda") -> "Mandela National Stadium, Kampala"
            leagueName.contains("Rwanda") -> "Amahoro Stadium, Kigali"
            leagueName.contains("South African") -> "FNB Stadium, Johannesburg"
            leagueName.contains("Egyptian") -> "Cairo International Stadium, Cairo"
            leagueName.contains("Morocco") -> "Stade Mohammed V, Casablanca"
            leagueName.contains("Algeria") -> "Stade du 5 Juillet, Algiers"
            leagueName.contains("Tunisia") -> "Stade Olympique de Radès, Tunis"
            leagueName.contains("Nigeria") -> "Moshood Abiola Stadium, Abuja"
            leagueName.contains("Ghana") -> "Accra Sports Stadium, Accra"
            leagueName.contains("Cameroon") -> "Stade Ahmadou Ahidjo, Yaoundé"
            leagueName.contains("Ivory Coast") -> "Stade Félix Houphouët-Boigny, Abidjan"
            leagueName.contains("Senegal") -> "Stade Léopold Sédar Senghor, Dakar"
            leagueName.contains("Mali") -> "Stade du 26 Mars, Bamako"
            leagueName.contains("Angola") -> "Estádio 11 de Novembro, Luanda"
            leagueName.contains("Zambia") -> "National Heroes Stadium, Lusaka"
            leagueName.contains("Zimbabwe") -> "National Sports Stadium, Harare"
            leagueName.contains("Congo DRC") -> "Stade des Martyrs, Kinshasa"
            leagueName.contains("Ethiopia") -> "Addis Ababa Stadium, Addis Ababa"
            else -> "FAME Africa Stadium"
        }
    }

    /**
     * Determine shield format based on league
     */
    private fun determineShieldFormat(leagueName: String): ShieldFormat {
        return when {
            leagueName.contains("Tanzania") || leagueName.contains("Zanzibar") || 
                    leagueName.contains("Kenya") || leagueName.contains("Uganda") || 
                    leagueName.contains("Rwanda") ->
                ShieldFormat.CHAMPION_VS_RUNNER_UP

            leagueName.contains("South African") || leagueName.contains("Nigeria") ||
                    leagueName.contains("Ghana") -> ShieldFormat.TOP_FOUR

            leagueName.contains("Egyptian") || leagueName.contains("Morocco") ||
                    leagueName.contains("Algeria") || leagueName.contains("Tunisia") ->
                ShieldFormat.CHAMPION_VS_CUP_WINNER

            else -> ShieldFormat.CHAMPION_VS_RUNNER_UP
        }
    }

    /**
     * Calculate shield date - early August, after preseason
     */
    private fun calculateShieldDate(season: String): String {
        val calendar = Calendar.getInstance()
        val seasonYear = season.split("/").first().toInt()

        // Set to August 5th of the season year at 4:00 PM
        calendar.set(seasonYear, Calendar.AUGUST, 5, 16, 0)

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    /**
     * Determine home and away teams for the shield match
     */
    private fun determineHomeAndAway(
        champion: LeagueStandingsEntity?,
        runnerUp: LeagueStandingsEntity?,
        format: ShieldFormat
    ): Pair<String?, String?> {
        return when (format) {
            ShieldFormat.CHAMPION_VS_RUNNER_UP -> {
                // Champion hosts the match
                Pair(champion?.teamName, runnerUp?.teamName)
            }
            ShieldFormat.TOP_FOUR -> {
                // For top four format, champion hosts runner-up in final
                Pair(champion?.teamName, runnerUp?.teamName)
            }
            ShieldFormat.CHAMPION_VS_CUP_WINNER -> {
                // Need cup winner data - default to champion vs runner-up
                Pair(champion?.teamName, runnerUp?.teamName)
            }
        }
    }

    /**
     * Calculate prize money based on league
     */
    private fun calculatePrizeMoney(league: LeaguesEntity): Int {
        return when (league.level) {
            1 -> league.prizeMoney / 10 // 10% of league prize money
            else -> 10000
        }
    }

    /**
     * Create fixture for shield match with country-specific TV channel
     */
    private suspend fun createShieldFixture(shieldId: Int, tvChannel: String): Boolean {
        val shield = communityShieldDao.getById(shieldId) ?: return false

        if (shield.homeTeam == null || shield.awayTeam == null) return false

        val homeTeam = teamsRepository.getTeamByName(shield.homeTeam)
        val awayTeam = teamsRepository.getTeamByName(shield.awayTeam)

        if (homeTeam == null || awayTeam == null) return false

        // Create fixture with country-specific TV channel
        val fixture = fixturesRepository.createShieldFixture(
            homeTeamId = homeTeam.id,
            homeTeamName = homeTeam.name,
            awayTeamId = awayTeam.id,
            awayTeamName = awayTeam.name,
            matchDate = shield.matchDate,
            season = shield.season,
            competition = shield.displayName,
            stadium = shield.stadium ?: "FAME Africa Stadium",
            tvChannel = tvChannel
        )

        // Update shield with fixture ID
        val updatedShield = shield.copy(fixtureId = fixture.id)
        communityShieldDao.update(updatedShield)
        return true
    }

    // ============ MATCH RESULT UPDATES ============

    /**
     * Update shield with match result
     */
    suspend fun updateShieldResult(
        shieldId: Int,
        homeScore: Int,
        awayScore: Int,
        winner: String,
        attendance: Int? = null
    ): CommunityShieldEntity? {

        val shield = communityShieldDao.getById(shieldId) ?: return null

        val result = "$homeScore-$awayScore"

        val updatedShield = shield.copy(
            homeScore = homeScore,
            awayScore = awayScore,
            winner = winner,
            result = result,
            isPlayed = true,
            attendance = attendance ?: shield.attendance
        )

        communityShieldDao.update(updatedShield)

        // Award prize money to winner
        awardPrizeMoney(winner, shield.prizeMoney)

        return updatedShield
    }

    /**
     * Award prize money to winner
     */
    private suspend fun awardPrizeMoney(winner: String, prizeMoney: Int) {
        val team = teamsRepository.getTeamByName(winner)
        team?.let {
            teamsRepository.updateTeamRevenue(team.id, prizeMoney.toDouble())
        }
    }

    // ============ SHIELD HISTORY ============

    suspend fun getShieldsByLeague(leagueName: String): Flow<List<CommunityShieldEntity>> {
        val league = leaguesRepository.getLeagueByName(leagueName)
        return communityShieldDao.getShieldsByLeague(league?.id ?: 0)
    }

    suspend fun getShieldsByTeam(teamName: String): Flow<List<CommunityShieldEntity>> {
        val team = teamsRepository.getTeamByName(teamName)
        return communityShieldDao.getShieldsByTeam(team?.id ?: 0)
    }

    suspend fun getShieldWinsByTeam(teamName: String): Int =
        communityShieldDao.getShieldWinCount(teamName)

    fun getTopShieldWinners(limit: Int): Flow<List<ShieldWinnerStats>> =
        communityShieldDao.getTopShieldWinners(limit)

    // ============ SEASON MANAGEMENT ============

    /**
     * Process all shields for a new season
     * Called at the start of the game year
     */
    suspend fun processNewSeason(
        newSeason: String,
        previousSeason: String
    ): SeasonOpeningResult {

        val generatedShields = generateAllSeasonOpeningShields(newSeason, previousSeason)

        return SeasonOpeningResult(
            season = newSeason,
            shieldsGenerated = generatedShields.size,
            shields = generatedShields
        )
    }

    /**
     * Generate all Community Shields for all top leagues at season start
     */
    suspend fun generateAllSeasonOpeningShields(
        season: String,
        previousSeason: String
    ): List<CommunityShieldEntity> {
        val shields = mutableListOf<CommunityShieldEntity>()

        // Get all top division leagues (level = 1)
        val topLeagues = leaguesRepository.getTopDivisionLeagues().firstOrNull() ?: return emptyList()

        for (league in topLeagues) {
            generateSeasonOpeningShield(league.name, season, previousSeason)?.let {
                shields.add(it)
            }
        }

        return shields
    }

    /**
     * Get upcoming shields for current season
     */
    fun getUpcomingShields(season: String): Flow<List<CommunityShieldEntity>> =
        communityShieldDao.getUpcomingShieldsBySeason(season)

    /**
     * Get played shields for current season
     */
    fun getPlayedShields(season: String): Flow<List<CommunityShieldEntity>> =
        communityShieldDao.getPlayedShieldsBySeason(season)

    // ============ DASHBOARD ============

    suspend fun getShieldDashboard(season: String): ShieldDashboard {
        val allShields = communityShieldDao.getShieldsBySeason(season).firstOrNull() ?: emptyList()
        val upcoming = allShields.filter { !it.isPlayed }
        val completed = allShields.filter { it.isPlayed }

        val totalPrizeMoney = allShields.sumOf { it.prizeMoney }
        val biggestPrize = allShields.maxByOrNull { it.prizeMoney }

        val recentWinners = completed
            .associate { it.leagueName to (it.winner ?: "Unknown") }

        return ShieldDashboard(
            season = season,
            totalShields = allShields.size,
            upcomingShields = upcoming.size,
            completedShields = completed.size,
            totalPrizeMoney = totalPrizeMoney,
            biggestPrizeShield = biggestPrize?.displayName,
            recentWinners = recentWinners,
            shields = allShields
        )
    }
}

// ============ DATA CLASSES ============

/**
 * Result of processing a new season's Community Shields
 */
data class SeasonOpeningResult(
    val season: String,
    val shieldsGenerated: Int,
    val shields: List<CommunityShieldEntity>
)

/**
 * Dashboard data for Community Shields in a season
 */
data class ShieldDashboard(
    val season: String,
    val totalShields: Int,
    val upcomingShields: Int,
    val completedShields: Int,
    val totalPrizeMoney: Int,
    val biggestPrizeShield: String?,
    val recentWinners: Map<String, String>,
    val shields: List<CommunityShieldEntity>
)
