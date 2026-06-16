package com.fameafrica.afm.data.repository

import com.fameafrica.afm.data.database.dao.NationalityRefereeCount
import com.fameafrica.afm.data.database.dao.RefereeWithStats
import com.fameafrica.afm.data.database.dao.RefereesDao
import com.fameafrica.afm.data.database.entities.RefereesEntity
import com.fameafrica.afm.data.database.entities.LeaguesEntity
import com.fameafrica.afm.data.database.entities.CupsEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class RefereesRepository @Inject constructor(
    private val refereesDaoProvider: Provider<RefereesDao>
) {
    private val refereesDao get() = refereesDaoProvider.get()

    // ============ BASIC CRUD ============

    fun getAllReferees(): Flow<List<RefereesEntity>> = refereesDao.getAll()

    suspend fun getRefereeById(id: Int): RefereesEntity? = refereesDao.getById(id)

    suspend fun getRefereeByName(name: String): RefereesEntity? = refereesDao.getByName(name)

    suspend fun insertReferee(referee: RefereesEntity) = refereesDao.insert(referee)

    suspend fun insertAllReferees(referees: List<RefereesEntity>) = refereesDao.insertAll(referees)

    suspend fun updateReferee(referee: RefereesEntity) = refereesDao.update(referee)

    suspend fun deleteReferee(referee: RefereesEntity) = refereesDao.delete(referee)

    suspend fun getRefereeCount(): Int = refereesDao.getCount()

    // ============ NATIONALITY-BASED ============

    fun getRefereesByNationality(nationId: Int): Flow<List<RefereesEntity>> =
        refereesDao.getRefereesByNationality(nationId)

    fun getRefereesByNationalityWithRating(nationId: Int, minRating: Int): Flow<List<RefereesEntity>> =
        refereesDao.getRefereesByNationalityWithRating(nationId, minRating)

    fun getRefereeCountByNationality(): Flow<List<NationalityRefereeCount>> =
        refereesDao.getRefereeCountByNationality()

    // ============ MATCH ASSIGNMENT - LEAGUE ============
    // Rule: League matches MUST use referees from the SAME country

    /**
     * Assign referee for a league match
     * @param league The league entity containing country_id
     * @param matchType REGULAR, IMPORTANT, DERBY, FINAL
     * @return Appropriate referee based on match context
     */
    suspend fun assignRefereeForLeagueMatch(
        league: LeaguesEntity,
        matchType: LeagueMatchType = LeagueMatchType.REGULAR
    ): RefereesEntity? {
        requireNotNull(league.countryId) { "League must have country_id" }

        return when (matchType) {
            LeagueMatchType.FINAL -> {
                // Final needs the absolute best referee from that country
                refereesDao.getRefereesForFinal(league.countryId)
                    .firstOrNull()
            }
            LeagueMatchType.DERBY -> {
                // Derby needs a strict referee to control emotions
                refereesDao.getStrictRefereesForDerby(league.countryId)
                    .firstOrNull()
                    ?: refereesDao.getRefereesForLeague(league.countryId, 70)
                        .firstOrNull()
            }
            LeagueMatchType.IMPORTANT -> {
                // Important match needs high rated referee
                refereesDao.getRefereesForLeague(league.countryId, 70)
                    .firstOrNull()
            }
            LeagueMatchType.REGULAR -> {
                // Regular match - any qualified local referee
                refereesDao.getRefereesForLeague(league.countryId, 60)
                    .firstOrNull()
            }
        }
    }

    // ============ MATCH ASSIGNMENT - DOMESTIC CUP ============
    // Rule: Domestic cup matches MUST use referees from the SAME country

    /**
     * Assign referee for a domestic cup match
     * @param cup The cup entity containing country_id
     * @param stage EARLY, QUARTER, SEMI, FINAL
     * @return Appropriate referee based on cup stage
     */
    suspend fun assignRefereeForDomesticCup(
        cup: CupsEntity,
        stage: CupStage = CupStage.EARLY
    ): RefereesEntity? {
        requireNotNull(cup.countryId) { "Domestic cup must have country_id" }
        require(cup.isDomesticCup) { "Cup must be a domestic cup" }

        val minRating = when (stage) {
            CupStage.FINAL -> 75
            CupStage.SEMI, CupStage.QUARTER -> 70
            CupStage.EARLY -> 65
        }

        val referees = refereesDao.getRefereesForDomesticCup(
            cupCountryId = cup.countryId,
            minRating = minRating
        )

        return when (stage) {
            CupStage.FINAL -> referees.maxByOrNull { it.rating }
            else -> referees.firstOrNull()
        }
    }

    // ============ MATCH ASSIGNMENT - NATIONAL TEAM ============
    // Rule: National team matches MUST use NEUTRAL referees

    /**
     * Assign referee for a national team match
     * @param homeNationId Home team's nationality_id
     * @param awayNationId Away team's nationality_id
     * @param isTournamentMatch Whether this is AFCON/World Cup or qualifier
     * @param isFriendly Whether this is a friendly match
     * @return Neutral referee not from either country
     */
    suspend fun assignRefereeForNationalMatch(
        homeNationId: Int,
        awayNationId: Int,
        isTournamentMatch: Boolean = false,
        isFriendly: Boolean = false
    ): RefereesEntity? {

        val minRating = when {
            isTournamentMatch -> 80  // AFCON/World Cup finals
            !isFriendly -> 75        // Competitive qualifiers
            else -> 68              // Friendlies
        }

        val neutralReferees = refereesDao.getNeutralRefereesForNationalMatch(
            homeNationId = homeNationId,
            awayNationId = awayNationId,
            minRating = minRating
        )

        return if (isTournamentMatch) {
            // Tournament matches get the highest rated referee
            neutralReferees.maxByOrNull { it.rating }
        } else {
            // Qualifiers and friendlies - any neutral referee
            neutralReferees.firstOrNull()
        }
    }

    // ============ MATCH ASSIGNMENT - CAF COMPETITIONS ============
    // Rule: CAF matches MUST use NEUTRAL AFRICAN referees

    /**
     * Assign referee for CAF competition match
     * @param homeNationId Home team's nationality_id
     * @param awayNationId Away team's nationality_id
     * @param stage GROUP, R16, QF, SF, FINAL
     * @return Neutral African referee not from either country
     */
    suspend fun assignRefereeForCAFCompetition(
        homeNationId: Int,
        awayNationId: Int,
        stage: CAFStage = CAFStage.GROUP
    ): RefereesEntity? {

        val (minRating, isFinal) = when (stage) {
            CAFStage.FINAL -> 85 to true
            CAFStage.SEMI, CAFStage.QUARTER -> 80 to false
            CAFStage.R16, CAFStage.R32 -> 78 to false
            CAFStage.GROUP -> 75 to false
        }

        return if (isFinal) {
            // Final needs elite FIFA/AFCON referee (bias ≤ 9)
            refereesDao.getEliteCAFRefereesForFinal(homeNationId, awayNationId)
                .firstOrNull()
                ?: refereesDao.getNeutralAfricanRefereesForCAF(homeNationId, awayNationId, minRating)
                    .maxByOrNull { it.rating }
        } else {
            refereesDao.getNeutralAfricanRefereesForCAF(homeNationId, awayNationId, minRating)
                .maxByOrNull { it.rating }
        }
    }

    // ============ MATCH ASSIGNMENT - INTERNATIONAL FRIENDLY ============
    // Rule: Friendlies PREFER neutral, but can use any if needed

    /**
     * Assign referee for international friendly
     * First tries to get neutral referee, falls back to any qualified referee
     */
    suspend fun assignRefereeForInternationalFriendly(
        homeNationId: Int,
        awayNationId: Int
    ): RefereesEntity? {

        // First try to get neutral referees
        val neutralReferees = refereesDao.getNeutralRefereesForFriendly(
            homeNationId = homeNationId,
            awayNationId = awayNationId
        )

        if (neutralReferees.isNotEmpty()) {
            return neutralReferees.maxByOrNull { it.rating }
        }

        // Fallback to any qualified referee (min rating 65)
        val allReferees = refereesDao.getAll().firstOrNull() ?: emptyList()
        return allReferees.filter { it.rating >= 65 }
            .maxByOrNull { it.rating }
    }

    // ============ SMART ASSIGNMENT - UNIVERSAL ============

    /**
     * Universal referee assignment based on match context
     * This is the main entry point for the match engine
     */
    suspend fun assignReferee(
        context: MatchContext
    ): RefereesEntity? {
        return when (context.matchType) {
            MatchType.LEAGUE -> {
                requireNotNull(context.league) { "League context required for league match" }
                assignRefereeForLeagueMatch(
                    league = context.league,
                    matchType = context.leagueMatchType ?: LeagueMatchType.REGULAR
                )
            }
            MatchType.DOMESTIC_CUP -> {
                requireNotNull(context.cup) { "Cup context required for domestic cup match" }
                assignRefereeForDomesticCup(
                    cup = context.cup,
                    stage = context.cupStage ?: CupStage.EARLY
                )
            }
            MatchType.NATIONAL_TEAM -> {
                requireNotNull(context.homeNationId) { "Home nation required for national match" }
                requireNotNull(context.awayNationId) { "Away nation required for national match" }
                assignRefereeForNationalMatch(
                    homeNationId = context.homeNationId,
                    awayNationId = context.awayNationId,
                    isTournamentMatch = context.isTournamentMatch ?: false,
                    isFriendly = context.isFriendly ?: false
                )
            }
            MatchType.CAF_CHAMPIONS_LEAGUE,
            MatchType.CAF_CONFEDERATION_CUP,
            MatchType.CAF_SUPER_CUP -> {
                requireNotNull(context.homeNationId) { "Home nation required for CAF match" }
                requireNotNull(context.awayNationId) { "Away nation required for CAF match" }
                assignRefereeForCAFCompetition(
                    homeNationId = context.homeNationId,
                    awayNationId = context.awayNationId,
                    stage = context.cafStage ?: CAFStage.GROUP
                )
            }
            MatchType.INTERNATIONAL_FRIENDLY -> {
                requireNotNull(context.homeNationId) { "Home nation required for friendly" }
                requireNotNull(context.awayNationId) { "Away nation required for friendly" }
                assignRefereeForInternationalFriendly(
                    homeNationId = context.homeNationId,
                    awayNationId = context.awayNationId
                )
            }
            else -> {
                // Default fallback - any referee
                refereesDao.getAll().firstOrNull()?.firstOrNull()
            }
        }
    }

    // ============ PERFORMANCE & STATISTICS ============

    /**
     * Update referee stats after a match and recalculate rating
     */
    suspend fun processMatchPerformance(
        refereeId: Int,
        yellowCards: Int,
        redCards: Int,
        matchImportance: Double = 1.0
    ) {
        // Update match statistics
        refereesDao.updateMatchStats(refereeId, yellowCards, redCards)

        // Get updated referee
        val referee = refereesDao.getById(refereeId) ?: return

        // Calculate new rating based on performance
        val cardPenalty = (yellowCards * 0.5 + redCards * 2.0) * matchImportance
        var newRating = (referee.rating - cardPenalty).toInt()

        // Ensure rating stays within tier range
        val (minRating, maxRating) = referee.expectedRatingRange
        newRating = newRating.coerceIn(minRating, maxRating)

        // Update rating
        refereesDao.updateRating(refereeId, newRating)
    }

    /**
     * Get referee statistics dashboard
     */
    suspend fun getRefereeDashboard(): RefereeDashboard {
        val allReferees = refereesDao.getAll().firstOrNull() ?: emptyList()
        val topRated = refereesDao.getTopRatedReferees(5).firstOrNull() ?: emptyList()
        val mostExperienced = refereesDao.getMostExperiencedReferees(5).firstOrNull() ?: emptyList()
        val controversial = refereesDao.getMostControversialReferees(5).firstOrNull() ?: emptyList()

        return RefereeDashboard(
            totalReferees = allReferees.size,
            fifaElite = refereesDao.getFIFAEliteReferees().firstOrNull()?.size ?: 0,
            continental = refereesDao.getContinentalReferees().firstOrNull()?.size ?: 0,
            topLeague = refereesDao.getTopLeagueReferees().firstOrNull()?.size ?: 0,
            average = refereesDao.getAverageReferees().firstOrNull()?.size ?: 0,
            local = refereesDao.getLocalReferees().firstOrNull()?.size ?: 0,
            topRatedReferees = topRated,
            mostExperiencedReferees = mostExperienced,
            mostControversialReferees = controversial
        )
    }

    // ============ VALIDATION ============

    /**
     * Validate if a referee is eligible for a specific match
     */
    suspend fun validateRefereeEligibility(
        referee: RefereesEntity,
        context: MatchContext
    ): ValidationResult {
        val isValid = when (context.matchType) {
            MatchType.LEAGUE -> {
                context.league?.countryId == referee.nationalityId
            }
            MatchType.DOMESTIC_CUP -> {
                context.cup?.countryId == referee.nationalityId
            }
            MatchType.NATIONAL_TEAM,
            MatchType.INTERNATIONAL_FRIENDLY -> {
                referee.nationalityId != context.homeNationId &&
                        referee.nationalityId != context.awayNationId
            }
            MatchType.CAF_CHAMPIONS_LEAGUE,
            MatchType.CAF_CONFEDERATION_CUP,
            MatchType.CAF_SUPER_CUP -> {
                referee.nationalityId != context.homeNationId &&
                        referee.nationalityId != context.awayNationId
            }
            else -> true
        }

        return ValidationResult(
            isValid = isValid,
            message = when {
                !isValid && context.matchType == MatchType.LEAGUE ->
                    "Referee must be from the same country as the league"
                !isValid && context.matchType == MatchType.DOMESTIC_CUP ->
                    "Referee must be from the same country as the cup"
                !isValid && context.matchType in listOf(MatchType.NATIONAL_TEAM, MatchType.CAF_CHAMPIONS_LEAGUE) ->
                    "Referee must be neutral (cannot be from either team's country)"
                else -> "Referee is eligible"
            }
        )
    }
}

// ============ ENUMS & DATA CLASSES ============

enum class LeagueMatchType {
    REGULAR,
    IMPORTANT,
    DERBY,
    FINAL
}

enum class CupStage {
    EARLY,
    QUARTER,
    SEMI,
    FINAL
}

enum class CAFStage {
    GROUP,
    R32,
    R16,
    QUARTER,
    SEMI,
    FINAL
}

enum class MatchType {
    LEAGUE,
    DOMESTIC_CUP,
    NATIONAL_TEAM,
    CAF_CHAMPIONS_LEAGUE,
    CAF_CONFEDERATION_CUP,
    CAF_SUPER_CUP,
    INTERNATIONAL_FRIENDLY,
    CLUB_FRIENDLY,
    OTHER
}

data class MatchContext(
    val matchType: MatchType,
    val league: LeaguesEntity? = null,
    val cup: CupsEntity? = null,
    val homeNationId: Int? = null,
    val awayNationId: Int? = null,
    val leagueMatchType: LeagueMatchType? = null,
    val cupStage: CupStage? = null,
    val cafStage: CAFStage? = null,
    val isTournamentMatch: Boolean? = null,
    val isFriendly: Boolean? = null,
    val matchDate: String? = null
)

data class RefereeDashboard(
    val totalReferees: Int,
    val fifaElite: Int,
    val continental: Int,
    val topLeague: Int,
    val average: Int,
    val local: Int,
    val topRatedReferees: List<RefereesEntity>,
    val mostExperiencedReferees: List<RefereesEntity>,
    val mostControversialReferees: List<RefereeWithStats>
)