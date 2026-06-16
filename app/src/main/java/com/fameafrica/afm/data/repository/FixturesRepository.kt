package com.fameafrica.afm.data.repository

import android.annotation.SuppressLint
import com.fameafrica.afm.data.database.dao.FixturesDao
import com.fameafrica.afm.data.database.entities.FixturesEntity
import com.fameafrica.afm.data.database.entities.LeaguesEntity
import com.fameafrica.afm.data.database.entities.TeamsEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import com.fameafrica.afm.R
import java.util.*
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class FixturesRepository @Inject constructor(
    private val fixturesDaoProvider: Provider<FixturesDao>,
    private val teamsRepository: TeamsRepository,
    private val leaguesRepository: LeaguesRepository,
    private val cupsRepository: CupsRepository,
    private val refereesRepository: RefereesRepository
) {
    private val fixturesDao get() = fixturesDaoProvider.get()

    // ============ BASIC CRUD ============

    fun getAllFixtures(): Flow<List<FixturesEntity>> = fixturesDao.getAll()

    suspend fun getFixtureById(id: Int): FixturesEntity? = fixturesDao.getById(id)

    suspend fun insertFixture(fixture: FixturesEntity): Long = fixturesDao.insert(fixture)

    suspend fun insertAllFixtures(fixtures: List<FixturesEntity>): List<Long> = fixturesDao.insertAll(fixtures)

    suspend fun updateFixture(fixture: FixturesEntity) = fixturesDao.update(fixture)

    suspend fun updateFixturesBatch(fixtures: List<FixturesEntity>) = fixturesDao.updateAll(fixtures)

    suspend fun deleteFixture(fixture: FixturesEntity) = fixturesDao.delete(fixture)

    suspend fun deleteAllFixtures() = fixturesDao.deleteAll()

    suspend fun getFixtureCount(): Int = fixturesDao.getCount()

    // ============ DATE-BASED QUERIES ============

    fun getFixturesByDate(date: String): Flow<List<FixturesEntity>> =
        fixturesDao.getFixturesByDate(date)

    fun getFixturesToSimulate(date: String): Flow<List<FixturesEntity>> =
        fixturesDao.getFixturesToSimulate(date)

    suspend fun getFixturesToSimulateSync(date: String): List<FixturesEntity> =
        fixturesDao.getFixturesToSimulateStatic(date)

    fun getUpcomingFixtures(currentDate: String): Flow<List<FixturesEntity>> =
        fixturesDao.getUpcomingFixtures(currentDate)

    fun getUpcomingFixturesLimit(currentDate: String, limit: Int): Flow<List<FixturesEntity>> =
        fixturesDao.getUpcomingFixturesLimit(currentDate, limit)

    fun getRecentFixtures(currentDate: String, limit: Int): Flow<List<FixturesEntity>> =
        fixturesDao.getRecentFixtures(currentDate, limit)

    fun getFixturesBetween(startDate: String, endDate: String): Flow<List<FixturesEntity>> =
        fixturesDao.getFixturesBetween(startDate, endDate)

    suspend fun getNextMatchForTeam(teamId: Int, currentDate: String): FixturesEntity? {
        return fixturesDao.getUpcomingFixturesByTeam(teamId, currentDate)
            .firstOrNull()
            ?.firstOrNull()
    }

    suspend fun getFixtureForTeamOnDate(teamId: Int, date: String): FixturesEntity? {
        return fixturesDao.getFixturesByDate(date)
            .firstOrNull()
            ?.find { it.homeTeamId == teamId || it.awayTeamId == teamId }
    }

    // ============ TEAM-BASED QUERIES ============

    fun getFixturesByTeam(teamId: Int): Flow<List<FixturesEntity>> =
        fixturesDao.getFixturesByTeam(teamId)

    fun getUpcomingFixturesByTeam(teamId: Int, currentDate: String): Flow<List<FixturesEntity>> =
        fixturesDao.getUpcomingFixturesByTeam(teamId, currentDate)

    fun getRecentResultsByTeam(teamId: Int, currentDate: String, limit: Int = 5): Flow<List<FixturesEntity>> =
        fixturesDao.getRecentResultsByTeam(teamId, currentDate, limit)

    // ============ LEAGUE & CUP QUERIES ============

    fun getLeagueFixtures(leagueName: String, season: String): Flow<List<FixturesEntity>> =
        fixturesDao.getLeagueFixtures(leagueName, season)

    fun getCupFixtures(cupName: String, season: String): Flow<List<FixturesEntity>> =
        fixturesDao.getCupFixtures(cupName, season)

    fun getFixturesBySeason(season: String): Flow<List<FixturesEntity>> =
        fixturesDao.getFixturesBySeason(season)

    suspend fun getCurrentGameWeek(leagueName: String, season: String): Int {
        val completed = fixturesDao.getCompletedLeagueFixtures(leagueName, season).firstOrNull() ?: emptyList()
        return (completed.maxOfOrNull { it.position } ?: 0) + 1
    }

    suspend fun getLeagueStatistics(leagueName: String, season: String): LeagueStatistics {
        val fixtures = fixturesDao.getLeagueFixtures(leagueName, season).firstOrNull() ?: emptyList()
        val completed = fixtures.filter { it.isCompleted }
        val totalGoals = completed.sumOf { it.homeScore + it.awayScore }
        return LeagueStatistics(
            totalMatches = fixtures.size,
            completedMatches = completed.size,
            totalGoals = totalGoals,
            averageGoalsPerGame = if (completed.isNotEmpty()) totalGoals.toDouble() / completed.size else 0.0,
            homeWinPercentage = if (completed.isNotEmpty()) completed.count { it.homeScore > it.awayScore }.toDouble() / completed.size * 100 else 0.0,
            awayWinPercentage = if (completed.isNotEmpty()) completed.count { it.awayScore > it.homeScore }.toDouble() / completed.size * 100 else 0.0,
            drawPercentage = if (completed.isNotEmpty()) completed.count { it.homeScore == it.awayScore }.toDouble() / completed.size * 100 else 0.0
        )
    }

    // ============ DASHBOARD ============

    suspend fun getFixturesDashboard(): FixturesDashboard {
        val all = fixturesDao.getAll().firstOrNull() ?: emptyList()
        return FixturesDashboard(
            totalFixtures = all.size,
            upcoming = all.count { it.isScheduled },
            completed = all.count { it.isCompleted },
            live = all.count { it.isLive }
        )
    }

    suspend fun getTeamSeasonProgress(teamId: Int, season: String): TeamSeasonProgress {
        val teamFixtures = fixturesDao.getFixturesByTeam(teamId).firstOrNull()?.filter { it.season == season } ?: emptyList()
        val played = teamFixtures.count { it.isCompleted }
        val remaining = teamFixtures.count { it.isScheduled }
        return TeamSeasonProgress(
            played = played,
            remaining = remaining,
            total = teamFixtures.size,
            progressPercentage = if (teamFixtures.isNotEmpty()) played.toDouble() / teamFixtures.size else 0.0
        )
    }

    // ============ FIXTURE GENERATION (FM-LEVEL) ============

    /**
     * Legacy method used in CareerRepository initialization.
     */
    suspend fun generateLeagueFixtures(
        league: LeaguesEntity,
        season: String,
        teams: List<TeamsEntity>,
        startDate: String
    ): List<FixturesEntity> {
        return generateBalancedLeagueFixtures(
            competitionName = league.name,
            season = season,
            teams = teams,
            startDate = startDate
        )
    }

    /**
     * Generate balanced league fixtures using the Berger table algorithm.
     * This ensures every team plays each other home and away exactly once.
     */
    suspend fun generateBalancedLeagueFixtures(
        competitionName: String,
        season: String,
        teams: List<TeamsEntity>,
        startDate: String,
        isCup: Boolean = false,
        daysBetweenRounds: Int = 7
    ): List<FixturesEntity> {
        val teamList = teams.shuffled().toMutableList()
        val n = teamList.size
        
        // If odd, we'd need a BYE logic, but typically leagues are even.
        // For simplicity, assuming even. 
        
        val roundsPerHalf = n - 1
        val matchesPerRound = n / 2

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val baseDate = dateFormat.parse(startDate) ?: Date()
        val calendar = Calendar.getInstance().apply { time = baseDate }

        val firstHalfFixtures = mutableListOf<FixturesEntity>()

        // Generate first half (circle method)
        for (round in 0 until roundsPerHalf) {
            for (i in 0 until matchesPerRound) {
                val homeIdx = (round + i) % (n - 1)
                var awayIdx = (n - 1 - i + round) % (n - 1)
                if (i == 0) awayIdx = n - 1

                val hTeam = teamList[homeIdx]
                val aTeam = teamList[awayIdx]

                // Balanced Home/Away logic for Round Robin (Circle Method)
                // i=0 is the match involving the fixed team (last index)
                // For other matches, alternating based on i ensures rotating teams swap H/A
                val (h, a) = if (i == 0) {
                    if (round % 2 == 1) Pair(aTeam, hTeam) else Pair(hTeam, aTeam)
                } else {
                    if (i % 2 == 1) Pair(aTeam, hTeam) else Pair(hTeam, aTeam)
                }

                firstHalfFixtures.add(
                    createFixtureEntity(
                        h, a, dateFormat.format(calendar.time), season,
                        competitionName, round + 1, isCup
                    )
                )
            }
            calendar.add(Calendar.DAY_OF_YEAR, daysBetweenRounds)
        }

        // Generate second half (mirrored)
        calendar.time = dateFormat.parse(startDate) ?: Date()
        calendar.add(Calendar.WEEK_OF_YEAR, roundsPerHalf + 2)

        val secondHalfFixtures = firstHalfFixtures.map { match ->
            val matchDate = calendar.clone() as Calendar
            matchDate.add(Calendar.DAY_OF_YEAR, (match.position - 1) * daysBetweenRounds)

            createFixtureEntity(
                homeId = match.awayTeamId,
                homeName = match.awayTeam,
                awayId = match.homeTeamId,
                awayName = match.homeTeam,
                date = dateFormat.format(matchDate.time),
                season = season,
                compName = competitionName,
                round = roundsPerHalf + match.position,
                isCup = isCup
            )
        }

        val allFixtures = firstHalfFixtures + secondHalfFixtures
        val ids = insertAllFixtures(allFixtures)
        
        return allFixtures.zip(ids) { fixture, id ->
            fixture.copy(id = id.toInt())
        }
    }

    private suspend fun createFixtureEntity(
        home: TeamsEntity,
        away: TeamsEntity,
        date: String,
        season: String,
        compName: String,
        round: Int,
        isCup: Boolean
    ): FixturesEntity {
        return FixturesEntity(
            matchDate = date,
            homeTeamId = home.id,
            homeTeam = home.name,
            awayTeamId = away.id,
            awayTeam = away.name,
            stadium = home.homeStadium ?: "FAME Africa Stadium",
            matchType = if (isCup) "Cup" else "League",
            season = season,
            league = if (!isCup) compName else null,
            cupName = if (isCup) compName else null,
            position = round,
            round = if (isCup) compName else "Matchday $round",
            matchStatus = "SCHEDULED",
            weatherConditions = getWeatherForDate(date)
        )
    }

    private suspend fun createFixtureEntity(
        homeId: Int,
        homeName: String,
        awayId: Int,
        awayName: String,
        date: String,
        season: String,
        compName: String,
        round: Int,
        isCup: Boolean
    ): FixturesEntity {
        val homeTeamObj = teamsRepository.getTeamById(homeId)
        return FixturesEntity(
            matchDate = date,
            homeTeamId = homeId,
            homeTeam = homeName,
            awayTeamId = awayId,
            awayTeam = awayName,
            stadium = homeTeamObj?.homeStadium ?: "FAME Africa Stadium",
            matchType = if (isCup) "Cup" else "League",
            season = season,
            league = if (!isCup) compName else null,
            cupName = if (isCup) compName else null,
            position = round,
            round = if (isCup) compName else "Matchday $round",
            matchStatus = "SCHEDULED",
            weatherConditions = getWeatherForDate(date)
        )
    }

    /**
     * Create a friendly fixture (preseason match)
     */
    suspend fun createFriendlyFixture(
        homeTeamId: Int,
        homeTeamName: String,
        awayTeamId: Int,
        awayTeamName: String,
        matchDate: String,
        season: String,
        stadium: String
    ): FixturesEntity {
        val fixture = FixturesEntity(
            matchDate = matchDate,
            homeTeamId = homeTeamId,
            homeTeam = homeTeamName,
            awayTeamId = awayTeamId,
            awayTeam = awayTeamName,
            stadium = stadium,
            matchType = "Friendly",
            season = season,
            league = null,
            cupName = null,
            matchStatus = "SCHEDULED",
            weatherConditions = getWeatherForDate(matchDate)
        )
        val id = insertFixture(fixture)
        return fixture.copy(id = id.toInt())
    }

    /**
     * Create a cup fixture
     */
    suspend fun createCupFixture(
        homeTeamId: Int,
        homeTeamName: String,
        awayTeamId: Int,
        awayTeamName: String,
        matchDate: String,
        season: String,
        cupName: String,
        round: String
    ): FixturesEntity {
        val homeTeamObj = teamsRepository.getTeamById(homeTeamId)
        val fixture = FixturesEntity(
            matchDate = matchDate,
            homeTeamId = homeTeamId,
            homeTeam = homeTeamName,
            awayTeamId = awayTeamId,
            awayTeam = awayTeamName,
            stadium = homeTeamObj?.homeStadium ?: "National Stadium",
            matchType = "Cup",
            season = season,
            cupName = cupName,
            round = round,
            matchStatus = "SCHEDULED",
            weatherConditions = getWeatherForDate(matchDate)
        )
        val id = insertFixture(fixture)
        return fixture.copy(id = id.toInt())
    }

    suspend fun createShieldFixture(
        homeTeamId: Int,
        homeTeamName: String,
        awayTeamId: Int,
        awayTeamName: String,
        matchDate: String,
        season: String,
        competition: String,
        stadium: String,
        tvChannel: String
    ): FixturesEntity {
        val fixture = FixturesEntity(
            matchDate = matchDate,
            homeTeamId = homeTeamId,
            homeTeam = homeTeamName,
            awayTeamId = awayTeamId,
            awayTeam = awayTeamName,
            stadium = stadium,
            matchType = "Cup",
            season = season,
            cupName = competition,
            round = "Final",
            matchStatus = "SCHEDULED",
            tvChannel = tvChannel,
            weatherConditions = getWeatherForDate(matchDate)
        )
        val id = insertFixture(fixture)
        return fixture.copy(id = id.toInt())
    }

    suspend fun createKnockoutFixture(
        homeTeamId: Int,
        homeTeamName: String,
        awayTeamId: Int,
        awayTeamName: String,
        matchDate: String,
        season: String,
        cupName: String,
        round: String,
        stadium: String
    ): FixturesEntity {
        val fixture = FixturesEntity(
            matchDate = matchDate,
            homeTeamId = homeTeamId,
            homeTeam = homeTeamName,
            awayTeamId = awayTeamId,
            awayTeam = awayTeamName,
            stadium = stadium,
            matchType = "Cup",
            season = season,
            cupName = cupName,
            round = round,
            matchStatus = "SCHEDULED",
            weatherConditions = getWeatherForDate(matchDate)
        )
        val id = insertFixture(fixture)
        return fixture.copy(id = id.toInt())
    }

    suspend fun completeFixture(id: Int, homeScore: Int, awayScore: Int) {
        getFixtureById(id)?.let { updateFixture(it.updateScore(homeScore, awayScore)) }
    }

    // ============ WEATHER ============
    @SuppressLint("SimpleDateFormat")
    fun getWeatherForDate(dateStr: String): String {
        return try {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr) ?: return "Clear"
            val cal = Calendar.getInstance().apply { time = date }
            val month = cal.get(Calendar.MONTH)

            when (month) {
                Calendar.JUNE, Calendar.JULY -> listOf("Clear", "Cool", "Windy", "Dry", "Foggy").random()
                Calendar.AUGUST, Calendar.SEPTEMBER -> listOf("Clear", "Sunny", "Windy", "Hazy").random()
                Calendar.OCTOBER, Calendar.NOVEMBER, Calendar.DECEMBER -> listOf("Tropical Rain", "Heavy Rain", "Thunderstorm", "Humid", "Cloudy", "Clear").random()
                Calendar.JANUARY, Calendar.FEBRUARY, Calendar.MARCH -> listOf("Scorching Heat", "Very Hot", "Sunny", "Dusty (Harmattan)", "Clear", "Dry").random()
                else -> listOf("Heavy Rain", "Rainy", "Cloudy", "Cool", "Hazy").random()
            }
        } catch (e: Exception) {
            "Clear"
        }
    }

    // ============ WEATHER ICON HELPER (for UI) ============
    fun getWeatherIconResource(condition: String?): Int {
        return when (condition?.lowercase(Locale.ROOT)) {
            "clear", "sunny" -> R.drawable.clear
            "windy" -> R.drawable.windy
            "tropical rain", "heavy rain", "rainy", "thunderstorm" -> R.drawable.rainy
            "scorching heat", "very hot", "hot" -> R.drawable.hot
            "cool", "cold", "dry" -> R.drawable.cold
            else -> R.drawable.clear
        }
    }
}

data class LeagueStatistics(
    val totalMatches: Int,
    val completedMatches: Int,
    val totalGoals: Int,
    val averageGoalsPerGame: Double,
    val homeWinPercentage: Double,
    val awayWinPercentage: Double,
    val drawPercentage: Double
)

data class FixturesDashboard(
    val totalFixtures: Int,
    val upcoming: Int,
    val completed: Int,
    val live: Int
)

data class TeamSeasonProgress(
    val played: Int,
    val remaining: Int,
    val total: Int,
    val progressPercentage: Double
)
