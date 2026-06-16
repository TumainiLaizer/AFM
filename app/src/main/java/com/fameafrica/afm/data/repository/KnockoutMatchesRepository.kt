package com.fameafrica.afm.data.repository

import com.fameafrica.afm.data.database.dao.KnockoutMatchesDao
import com.fameafrica.afm.data.database.entities.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class KnockoutMatchesRepository @Inject constructor(
    private val knockoutMatchesDaoProvider: Provider<KnockoutMatchesDao>,
    private val cupsRepository: CupsRepository,
    private val teamsRepository: TeamsRepository,
    private val fixturesRepository: FixturesRepository,
    private val cupBracketsRepository: CupBracketsRepository,
    private val refereesRepository: RefereesRepository,
    private val leaguesRepository: LeaguesRepository,
    private val leagueStandingsRepository: LeagueStandingsRepository
) {

    private val knockoutMatchesDao: KnockoutMatchesDao?
        get() = try {
            knockoutMatchesDaoProvider.get()
        } catch (e: Exception) {
            null
        }

    // ============ BASIC CRUD ============

    fun getAllMatches(): Flow<List<KnockoutMatchesEntity>> = knockoutMatchesDao?.getAll() ?: kotlinx.coroutines.flow.flowOf(emptyList())

    suspend fun getMatchById(id: Int): KnockoutMatchesEntity? = knockoutMatchesDao?.getById(id)

    suspend fun getMatchByFixtureId(fixtureId: Int): KnockoutMatchesEntity? =
        knockoutMatchesDao?.getByFixtureId(fixtureId)

    suspend fun insertMatch(match: KnockoutMatchesEntity) {
        knockoutMatchesDao?.insert(match)
    }

    suspend fun insertAllMatches(matches: List<KnockoutMatchesEntity>) {
        knockoutMatchesDao?.insertAll(matches)
    }

    suspend fun updateMatch(match: KnockoutMatchesEntity) {
        knockoutMatchesDao?.update(match)
    }

    suspend fun deleteMatch(match: KnockoutMatchesEntity) {
        knockoutMatchesDao?.delete(match)
    }

    suspend fun deleteByCupAndSeason(cupName: String, season: String) {
        knockoutMatchesDao?.deleteByCupAndSeason(cupName, season)
    }

    // ============ CUP-BASED ============

    fun getMatchesByCupAndSeason(cupName: String, season: String): Flow<List<KnockoutMatchesEntity>> =
        knockoutMatchesDao?.getMatchesByCupAndSeason(cupName, season) ?: kotlinx.coroutines.flow.flowOf(emptyList())

    fun getMatchesByRound(cupName: String, season: String, round: String): Flow<List<KnockoutMatchesEntity>> =
        knockoutMatchesDao?.getMatchesByRound(cupName, season, round) ?: kotlinx.coroutines.flow.flowOf(emptyList())

    suspend fun getFinalMatch(cupName: String, season: String): KnockoutMatchesEntity? =
        knockoutMatchesDao?.getFinalMatch(cupName, season)

    fun getSemiFinals(cupName: String, season: String): Flow<List<KnockoutMatchesEntity>> =
        knockoutMatchesDao?.getSemiFinals(cupName, season) ?: kotlinx.coroutines.flow.flowOf(emptyList())

    fun getQuarterFinals(cupName: String, season: String): Flow<List<KnockoutMatchesEntity>> =
        knockoutMatchesDao?.getQuarterFinals(cupName, season) ?: kotlinx.coroutines.flow.flowOf(emptyList())

    // ============ KNOCKOUT BRACKET GENERATION ============

    /**
     * Generate knockout bracket for a cup
     */
    suspend fun generateKnockoutBracket(
        cupId: Int,
        cupName: String,
        season: String,
        qualifiedTeams: List<TeamsEntity>,
        startDate: String,
        isTwoLegged: Boolean = false
    ): List<KnockoutMatchesEntity> {

        // Delete existing matches
        knockoutMatchesDao?.deleteByCupAndSeason(cupName, season)

        val matches = mutableListOf<KnockoutMatchesEntity>()
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        calendar.time = dateFormat.parse(startDate) ?: Calendar.getInstance().time

        val numTeams = qualifiedTeams.size
        val rounds = calculateRounds(numTeams)

        // Create first round matches
        var matchNumber = 1
        for (i in qualifiedTeams.indices step 2) {
            if (i + 1 < qualifiedTeams.size) {
                val match = KnockoutMatchesEntity(
                    cupId = cupId,
                    cupName = cupName,
                    season = season,
                    round = getRoundName(1, rounds),
                    roundNumber = 1,
                    matchNumber = matchNumber++,
                    homeTeamId = qualifiedTeams[i].id,
                    homeTeam = qualifiedTeams[i].name,
                    awayTeamId = qualifiedTeams[i+1].id,
                    awayTeam = qualifiedTeams[i + 1].name,
                    matchDate = dateFormat.format(calendar.time),
                    matchResult = "DRAW", // Default until played
                    stadium = qualifiedTeams[i].homeStadium,
                    isTwoLegged = isTwoLegged,
                    isPlayed = false
                )
                matches.add(match)
                calendar.add(Calendar.DAY_OF_YEAR, 3)
            }
        }

        // Create subsequent round placeholders
        var teamsInRound = matches.size
        var roundNum = 2

        while (teamsInRound > 1) {
            val matchesInRound = teamsInRound / 2

            for (i in 0 until matchesInRound) {
                val match = KnockoutMatchesEntity(
                    cupId = cupId,
                    cupName = cupName,
                    season = season,
                    round = getRoundName(roundNum, rounds),
                    roundNumber = roundNum,
                    matchNumber = i + 1,
                    homeTeamId = 0,
                    homeTeam = "TBD",
                    awayTeamId = 0,
                    awayTeam = "TBD",
                    matchDate = dateFormat.format(calendar.time),
                    matchResult = "DRAW",
                    stadium = null,
                    isTwoLegged = isTwoLegged && roundNum < rounds,
                    isPlayed = false
                )
                matches.add(match)
                calendar.add(Calendar.DAY_OF_YEAR, 3)
            }

            teamsInRound = matchesInRound
            roundNum++
        }

        // Link matches to next round
        linkMatchesToNextRound(matches)

        knockoutMatchesDao?.insertAll(matches)
        return matches
    }

    private fun calculateRounds(numTeams: Int): Int {
        return when {
            numTeams <= 2 -> 1
            numTeams <= 4 -> 2
            numTeams <= 8 -> 3
            numTeams <= 16 -> 4
            numTeams <= 32 -> 5
            numTeams <= 64 -> 6
            else -> 7
        }
    }

    private fun getRoundName(roundNum: Int, totalRounds: Int): String {
        return when {
            roundNum == totalRounds -> "Final"
            roundNum == totalRounds - 1 -> "Semi-final"
            roundNum == totalRounds - 2 -> "Quarter-final"
            roundNum == totalRounds - 3 -> "Round of 16"
            roundNum == totalRounds - 4 -> "Round of 32"
            roundNum == totalRounds - 5 -> "Round of 64"
            else -> "Round $roundNum"
        }
    }

    private fun linkMatchesToNextRound(matches: MutableList<KnockoutMatchesEntity>) {
        val matchesByRound = matches.groupBy { it.roundNumber }

        // Iterate through the sorted round numbers, excluding the last one
        val sortedRounds = matchesByRound.keys.sorted()
        if (sortedRounds.size > 1) {
            for (i in 0 until sortedRounds.size - 1) {
                val round = sortedRounds[i]
                val currentRoundMatches = matchesByRound[round] ?: continue
                val nextRoundMatches = matchesByRound[sortedRounds[i + 1]] ?: continue

                for ((index, match) in currentRoundMatches.withIndex()) {
                    val nextMatchIndex = index / 2
                    if (nextMatchIndex < nextRoundMatches.size) {
                        val updatedMatch = match.copy(
                            nextMatchId = nextRoundMatches[nextMatchIndex].id
                        )
                        matches[matches.indexOf(match)] = updatedMatch
                    }
                }
            }
        }
    }

    private suspend fun getTeamStadium(teamName: String): String? {
        return teamsRepository.getTeamByName(teamName)?.homeStadium
    }

    // ============ TWO-LEGGED TIE GENERATION ============

    /**
     * Generate a two-legged tie
     */
    suspend fun generateTwoLeggedTie(
        cupId: Int,
        cupName: String,
        season: String,
        round: String,
        roundNumber: Int,
        matchNumber: Int,
        homeTeamId: Int,
        homeTeam: String,
        awayTeamId: Int,
        awayTeam: String,
        firstLegDate: String,
        secondLegDate: String
    ): Pair<KnockoutMatchesEntity, KnockoutMatchesEntity> {

        // First leg
        val firstLeg = KnockoutMatchesEntity(
            cupId = cupId,
            cupName = cupName,
            season = season,
            round = round,
            roundNumber = roundNumber,
            matchNumber = matchNumber,
            homeTeamId = homeTeamId,
            homeTeam = homeTeam,
            awayTeamId = awayTeamId,
            awayTeam = awayTeam,
            matchDate = firstLegDate,
            matchResult = "DRAW",
            stadium = getTeamStadium(homeTeam),
            isTwoLegged = true,
            leg = MatchLeg.FIRST.value,
            isPlayed = false
        )

        knockoutMatchesDao?.insert(firstLeg)

        // Second leg
        val secondLeg = KnockoutMatchesEntity(
            cupId = cupId,
            cupName = cupName,
            season = season,
            round = round,
            roundNumber = roundNumber,
            matchNumber = matchNumber,
            homeTeamId = awayTeamId,
            homeTeam = awayTeam,
            awayTeamId = homeTeamId,
            awayTeam = homeTeam,
            matchDate = secondLegDate,
            matchResult = "DRAW",
            stadium = getTeamStadium(awayTeam),
            isTwoLegged = true,
            leg = MatchLeg.SECOND.value,
            firstLegId = firstLeg.id,
            isPlayed = false
        )

        knockoutMatchesDao?.insert(secondLeg)

        // Update first leg with second leg ID
        val updatedFirstLeg = firstLeg.copy(secondLegId = secondLeg.id)
        knockoutMatchesDao?.update(updatedFirstLeg)

        return Pair(updatedFirstLeg, secondLeg)
    }

    // ============ MATCH RESULT UPDATES ============

    /**
     * Update single leg match result
     */
    suspend fun updateMatchResult(
        matchId: Int,
        homeScore: Int,
        awayScore: Int,
        attendance: Int? = null,
        refereeId: Int? = null,
        weatherConditions: String = "Clear"
    ): KnockoutMatchesEntity? {

        val match = knockoutMatchesDao?.getById(matchId) ?: return null

        val winner = when {
            homeScore > awayScore -> match.homeTeam
            awayScore > homeScore -> match.awayTeam
            else -> null
        }

        val winnerId = when {
            homeScore > awayScore -> match.homeTeamId
            awayScore > homeScore -> match.awayTeamId
            else -> null
        }

        val loser = when {
            homeScore > awayScore -> match.awayTeam
            awayScore > homeScore -> match.homeTeam
            else -> null
        }

        val loserId = when {
            homeScore > awayScore -> match.awayTeamId
            awayScore > homeScore -> match.homeTeamId
            else -> null
        }

        val matchResult = when {
            homeScore > awayScore -> MatchResultType.HOME_WIN.value
            awayScore > homeScore -> MatchResultType.AWAY_WIN.value
            else -> MatchResultType.DRAW.value
        }

        val updatedMatch = match.copy(
            homeScore = homeScore,
            awayScore = awayScore,
            winner = winner,
            winnerId = winnerId,
            loser = loser,
            loserId = loserId,
            matchResult = matchResult,
            attendance = attendance ?: match.attendance,
            refereeId = refereeId ?: match.refereeId,
            weatherConditions = weatherConditions,
            isPlayed = true
        )

        knockoutMatchesDao?.update(updatedMatch)

        // Update next round match with winner
        match.nextMatchId?.let { nextMatchId ->
            updateNextRoundMatch(nextMatchId, winnerId ?: 0, winner ?: return@let)
        }

        return updatedMatch
    }

    /**
     * Update two-legged tie result
     */
    suspend fun updateTwoLeggedTieResult(
        firstLegId: Int,
        secondLegId: Int,
        firstLegHomeScore: Int,
        firstLegAwayScore: Int,
        secondLegHomeScore: Int,
        secondLegAwayScore: Int,
        homePenaltyScore: Int? = null,
        awayPenaltyScore: Int? = null,
        attendance: Int? = null,
        refereeId: Int? = null
    ): Pair<KnockoutMatchesEntity, KnockoutMatchesEntity>? {

        val firstLeg = knockoutMatchesDao?.getById(firstLegId) ?: return null
        val secondLeg = knockoutMatchesDao?.getById(secondLegId) ?: return null

        // Update first leg
        val updatedFirstLeg = firstLeg.copy(
            homeScore = firstLegHomeScore,
            awayScore = firstLegAwayScore,
            isPlayed = true,
            attendance = attendance ?: firstLeg.attendance,
            refereeId = refereeId ?: firstLeg.refereeId
        )
        knockoutMatchesDao?.update(updatedFirstLeg)

        // Calculate aggregate scores
        val aggregateHome = firstLegHomeScore + secondLegAwayScore // Home team in first leg is away in second
        val aggregateAway = firstLegAwayScore + secondLegHomeScore // Away team in first leg is home in second

        // Determine winner
        val winner = when {
            aggregateHome > aggregateAway -> firstLeg.homeTeam
            aggregateAway > aggregateHome -> firstLeg.awayTeam
            homePenaltyScore != null && awayPenaltyScore != null -> {
                if (homePenaltyScore > awayPenaltyScore) firstLeg.homeTeam else firstLeg.awayTeam
            }
            else -> null // Extra time would be handled separately
        }

        val winnerId = when {
            aggregateHome > aggregateAway -> firstLeg.homeTeamId
            aggregateAway > aggregateHome -> firstLeg.awayTeamId
            homePenaltyScore != null && awayPenaltyScore != null -> {
                if (homePenaltyScore > awayPenaltyScore) firstLeg.homeTeamId else firstLeg.awayTeamId
            }
            else -> null
        }

        val matchResult = when {
            aggregateHome > aggregateAway -> MatchResultType.AGGREGATE_HOME.value
            aggregateAway > aggregateHome -> MatchResultType.AGGREGATE_AWAY.value
            homePenaltyScore != null && awayPenaltyScore != null -> {
                if (homePenaltyScore > awayPenaltyScore)
                    MatchResultType.HOME_WIN_PENS.value
                else
                    MatchResultType.AWAY_WIN_PENS.value
            }
            else -> MatchResultType.DRAW.value
        }

        // Update second leg
        val updatedSecondLeg = secondLeg.copy(
            homeScore = secondLegHomeScore,
            awayScore = secondLegAwayScore,
            homePenaltyScore = homePenaltyScore,
            awayPenaltyScore = awayPenaltyScore,
            aggregateHomeScore = aggregateHome,
            aggregateAwayScore = aggregateAway,
            winner = winner,
            winnerId = winnerId,
            loser = if (winner == firstLeg.homeTeam) firstLeg.awayTeam else firstLeg.homeTeam,
            loserId = if (winnerId == firstLeg.homeTeamId) firstLeg.awayTeamId else firstLeg.homeTeamId,
            matchResult = matchResult,
            isPlayed = true,
            attendance = attendance ?: secondLeg.attendance,
            refereeId = refereeId ?: secondLeg.refereeId
        )
        knockoutMatchesDao?.update(updatedSecondLeg)

        // Update next round match with winner
        secondLeg.nextMatchId?.let { nextMatchId ->
            updateNextRoundMatch(nextMatchId, winnerId ?: 0, winner ?: return@let)
        }

        return Pair(updatedFirstLeg, updatedSecondLeg)
    }

    private suspend fun updateNextRoundMatch(nextMatchId: Int, winnerId: Int, winner: String) {
        val nextMatch = knockoutMatchesDao?.getById(nextMatchId) ?: return

        val updatedNextMatch = if (nextMatch.homeTeam == "TBD") {
            nextMatch.copy(homeTeamId = winnerId, homeTeam = winner, stadium = getTeamStadium(winner))
        } else if (nextMatch.awayTeam == "TBD") {
            nextMatch.copy(awayTeamId = winnerId, awayTeam = winner)
        } else {
            nextMatch
        }

        knockoutMatchesDao?.update(updatedNextMatch)
    }

    // ============ ELIGIBLE TEAMS LOGIC ============

    /**
     * Get teams to involve in a cup based on rules
     */
    suspend fun getTeamsToInvolve(cup: CupsEntity, previousYear: Int? = null): List<TeamsEntity> {
        val allTeams = teamsRepository.getAllTeams().firstOrNull() ?: return emptyList()

        return when (cup.name) {
            "Muungano Cup" -> {
                // Top 4 teams from Tanzania Premier League and Top 4 Teams from Zanzibar Premier League
                val tplTop4Names = if (previousYear != null) {
                    leagueStandingsRepository.getStandings("Tanzania Premier League", previousYear)
                        .firstOrNull()?.sortedBy { it.position }?.take(4)?.map { it.teamName }
                } else null
                
                val zplTop4Names = if (previousYear != null) {
                    leagueStandingsRepository.getStandings("Zanzibar Premier League", previousYear)
                        .firstOrNull()?.sortedBy { it.position }?.take(4)?.map { it.teamName }
                } else null

                val finalTpl = if (tplTop4Names != null) {
                    allTeams.filter { it.name in tplTop4Names }
                } else {
                    allTeams.filter { it.league == "Tanzania Premier League" }
                        .sortedByDescending { it.eloRating }.take(4)
                }
                
                val finalZpl = if (zplTop4Names != null) {
                    allTeams.filter { it.name in zplTop4Names }
                } else {
                    allTeams.filter { it.league == "Zanzibar Premier League" }
                        .sortedByDescending { it.eloRating }.take(4)
                }
                
                finalTpl + finalZpl
            }
            "CRDB Federations Cup" -> {
                // Rule: 64 clubs from the top 4 divisions in Tanzania.
                // Excludes Tanzania Regional Division League 2 as they play in FAME Africa™ Cup.
                allTeams.filter { team ->
                    team.league in listOf(
                        "Tanzania Premier League", 
                        "Tanzania Championship League", 
                        "Tanzania First League", 
                        "Tanzania Regional Champions League"
                    )
                }.sortedByDescending { it.reputation }.take(cup.teamsInvolved)
            }
            "PBZ Cup" -> {
                // Teams from Zanzibar Premier League and Zanzibar Championship League.
                allTeams.filter { team ->
                    team.league == "Zanzibar Premier League" || team.league == "Zanzibar Championship League"
                }.sortedByDescending { it.reputation }.take(cup.teamsInvolved)
            }
            "FAME Africa™ Cup" -> {
                // Teams from Tanzania Regional Division League 2.
                allTeams.filter { team ->
                    team.league == "Tanzania Regional Division League 2"
                }.sortedByDescending { it.reputation }.take(cup.teamsInvolved)
            }
            else -> {
                // Default logic based on country_id
                if (cup.isDomesticCup) {
                    allTeams.filter { team ->
                        leaguesRepository.getLeagueByName(team.league)?.countryId == cup.countryId
                    }.take(cup.teamsInvolved)
                } else {
                    // Continental/International
                    allTeams.filter { it.cupQualification == cup.name }
                }
            }
        }
    }
}
