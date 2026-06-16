package com.fameafrica.afm.data.repository

import com.fameafrica.afm.data.database.dao.ManagerOffersDao
import com.fameafrica.afm.data.database.dao.ManagersDao
import com.fameafrica.afm.data.database.dao.TeamsDao
import com.fameafrica.afm.data.database.dao.LeaguesDao
import com.fameafrica.afm.data.database.dao.OfferWithDetails
import com.fameafrica.afm.data.database.entities.*
import com.fameafrica.afm.utils.GameDateManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import java.util.Calendar
import kotlin.random.Random

@Singleton
class ManagerOffersRepository @Inject constructor(
    private val managerOffersDaoProvider: Provider<ManagerOffersDao>,
    private val managersDaoProvider: Provider<ManagersDao>,
    private val teamsDaoProvider: Provider<TeamsDao>,
    private val leaguesDaoProvider: Provider<LeaguesDao>,
    private val gameDateManager: GameDateManager
) {

    private val managerOffersDao: ManagerOffersDao?
        get() = try {
            managerOffersDaoProvider.get()
        } catch (e: Exception) {
            null
        }

    private val managersDao: ManagersDao?
        get() = try {
            managersDaoProvider.get()
        } catch (e: Exception) {
            null
        }

    private val teamsDao: TeamsDao?
        get() = try {
            teamsDaoProvider.get()
        } catch (e: Exception) {
            null
        }

    private val leaguesDao: LeaguesDao?
        get() = try {
            leaguesDaoProvider.get()
        } catch (e: Exception) {
            null
        }

    // ============ BASIC CRUD ============

    fun getAllOffers(): Flow<List<ManagerOffersEntity>> = managerOffersDao?.getAll() ?: kotlinx.coroutines.flow.flowOf(emptyList())

    suspend fun getOfferById(id: Int): ManagerOffersEntity? = managerOffersDao?.getById(id)

    suspend fun insertOffer(offer: ManagerOffersEntity) {
        managerOffersDao?.insert(offer)
    }

    suspend fun insertAllOffers(offers: List<ManagerOffersEntity>) {
        managerOffersDao?.insertAll(offers)
    }

    suspend fun updateOffer(offer: ManagerOffersEntity) {
        managerOffersDao?.update(offer)
    }

    suspend fun deleteOffer(offer: ManagerOffersEntity) {
        managerOffersDao?.delete(offer)
    }

    suspend fun deleteExpiredOffers() {
        val currentTime = System.currentTimeMillis()
        managerOffersDao?.deleteExpiredOffers(currentTime)
    }

    // ============ OFFER MANAGEMENT ============

    suspend fun generateRandomOfferIfEligible(managerId: Int): ManagerOffersEntity? {
        val manager = managersDao?.getById(managerId) ?: return null
        
        // Only 5% chance per week
        if (Random.nextInt(100) >= 5) return null

        val reputation = manager.reputation
        val targetLevel = when {
            reputation >= 80 -> 1
            reputation >= 65 -> 2
            reputation >= 50 -> 3
            reputation >= 35 -> 4
            else -> 5
        }

        val leagues = leaguesDao?.getLeaguesByLevel(targetLevel)?.firstOrNull() ?: emptyList()
        if (leagues.isEmpty()) return null
        
        val league = leagues.random()
        val teams = teamsDao?.getTeamsByLeague(league.name)?.firstOrNull() ?: emptyList()
        val availableTeams = teams.filter { it.managerId == null || it.managerId != managerId }
        
        if (availableTeams.isEmpty()) return null
        
        val team = availableTeams.random()
        
        val offer = ManagerOffersEntity(
            managerId = managerId,
            managerName = manager.name,
            offeringTeam = team.name,
            offeringTeamId = team.id,
            leagueName = league.name,
            leagueLevel = targetLevel,
            offeredSalary = (reputation * 10000) + (Random.nextInt(50000, 200000)),
            contractYears = Random.nextInt(2, 5),
            transferFee = 0,
            offerType = ManagerOfferType.HEAD_COACH.value,
            offerDate = System.currentTimeMillis(),
            expiryDate = Calendar.getInstance().apply { add(Calendar.WEEK_OF_YEAR, 2) }.timeInMillis,
            message = "We are impressed by your career progress and would like to offer you the position of Head Coach."
        )
        
        managerOffersDao?.insert(offer)
        return offer
    }

    /**
     * GENERATE INITIAL OFFER FOR NEW USER
     */
    suspend fun generateInitialOfferForNewUser(managerId: Int): ManagerOffersEntity? {
        val manager = managersDao?.getById(managerId) ?: return null

        val level5Leagues = leaguesDao?.getLeaguesByLevel(5)?.firstOrNull() ?: emptyList()
        if (level5Leagues.isEmpty()) return null

        val selectedLeague = level5Leagues.random()
        val teamsInLeague = teamsDao?.getTeamsByLeague(selectedLeague.name)?.firstOrNull() ?: emptyList()
        val teamsWithoutManager = teamsInLeague.filter { it.managerId == null }

        if (teamsWithoutManager.isEmpty()) return null

        val selectedTeam = teamsWithoutManager.random()
        val offeredSalary = 500000 + (100000..300000).random()

        // Expiry in 1 game week
        val expiryDate = gameDateManager.getGameDate(2).time

        val offer = ManagerOffersEntity(
            managerId = manager.id,
            managerName = manager.name,
            offeringTeam = selectedTeam.name,
            offeringTeamId = selectedTeam.id,
            leagueName = selectedLeague.name,
            leagueLevel = 5,
            offeredSalary = offeredSalary,
            contractYears = 2,
            transferFee = 0,
            offerType = ManagerOfferType.HEAD_COACH.value,
            offerDate = gameDateManager.getGameDate(1).time,
            expiryDate = expiryDate,
            isMidSeason = false,
            isPromotion = false,
            message = "Join us in the ${selectedLeague.name}!"
        )

        managerOffersDao?.insert(offer)
        return offer
    }

    /**
     * GENERATE PROMOTION OFFERS
     */
    suspend fun generatePromotionOffers(managerId: Int, currentWeek: Int): List<ManagerOffersEntity> {
        val manager = managersDao?.getById(managerId) ?: return emptyList()
        if (!manager.isEmployed) return emptyList()

        val currentTeam = teamsDao?.getById(manager.teamId!!) ?: return emptyList()
        val currentLeague = leaguesDao?.getByName(currentTeam.league)
        val currentLevel = currentLeague?.level ?: return emptyList()

        if (currentLevel <= 1) return emptyList()

        val targetLevel = currentLevel - 1
        val winRate = if (manager.matchesManaged > 0) (manager.wins.toDouble() / manager.matchesManaged * 100) else 0.0

        val performanceThreshold = when (targetLevel) {
            4 -> 55.0
            3 -> 60.0
            2 -> 65.0
            1 -> 70.0
            else -> 75.0
        }

        if (winRate < performanceThreshold) return emptyList()

        val targetLeagues = leaguesDao?.getLeaguesByLevel(targetLevel)?.firstOrNull() ?: emptyList()
        val offers = mutableListOf<ManagerOffersEntity>()

        targetLeagues.forEach { league ->
            val teamsInLeague = teamsDao?.getTeamsByLeague(league.name)?.firstOrNull() ?: emptyList()
            val teamsWithoutManager = teamsInLeague.filter { it.managerId == null }

            teamsWithoutManager.shuffled().take(2).forEach { team ->
                val baseSalary = when (targetLevel) {
                    1 -> 5000000
                    2 -> 3000000
                    3 -> 2000000
                    4 -> 1200000
                    else -> 800000
                }

                val offeredSalary = baseSalary + (200000..500000).random()
                val transferFee = if (manager.contractEndDate != null) manager.calculateTransferFee() else 0

                val offer = ManagerOffersEntity(
                    managerId = manager.id,
                    managerName = manager.name,
                    offeringTeam = team.name,
                    offeringTeamId = team.id,
                    leagueName = league.name,
                    leagueLevel = targetLevel,
                    offeredSalary = offeredSalary,
                    contractYears = 3,
                    transferFee = transferFee,
                    offerType = ManagerOfferType.HEAD_COACH.value,
                    offerDate = gameDateManager.getGameDate(currentWeek).time,
                    expiryDate = gameDateManager.getGameDate(currentWeek + 1).time,
                    isMidSeason = false,
                    isPromotion = true,
                    message = "Congratulations on your success!"
                )

                managerOffersDao?.insert(offer)
                offers.add(offer)
            }
        }

        return offers
    }

    /**
     * ACCEPT OFFER
     */
    suspend fun acceptOffer(offerId: Int, currentWeek: Int): Boolean {
        val offer = managerOffersDao?.getById(offerId) ?: return false
        if (offer.status != "pending" || offer.isExpired) return false

        val updatedOffer = offer.copy(status = "accepted")
        managerOffersDao?.update(updatedOffer)

        val manager = managersDao?.getById(offer.managerId) ?: return false

        if (manager.isEmployed) {
            managersDao?.update(manager.leaveClub())
        }

        val calendar = Calendar.getInstance()
        calendar.time = gameDateManager.getGameDate(currentWeek)
        val currentYear = calendar.get(Calendar.YEAR)

        val updatedManager = manager.signContract(
            teamId = offer.offeringTeamId,
            salary = offer.offeredSalary,
            contractYears = offer.contractYears,
            currentYear = currentYear
        )
        managersDao?.update(updatedManager)

        val otherOffers = managerOffersDao?.getPendingOffersByManager(manager.id)?.firstOrNull() ?: emptyList()
        otherOffers.forEach { otherOffer ->
            if (otherOffer.id != offerId) {
                managerOffersDao?.update(otherOffer.copy(status = "rejected"))
            }
        }

        return true
    }

    suspend fun rejectOffer(offerId: Int): Boolean {
        val offer = managerOffersDao?.getById(offerId) ?: return false
        if (offer.status != "pending") return false

        val updatedOffer = offer.copy(status = "rejected")
        managerOffersDao?.update(updatedOffer)
        return true
    }

    // ============ QUERIES ============

    fun getOffersByManager(managerId: Int): Flow<List<ManagerOffersEntity>> =
        managerOffersDao?.getOffersByManager(managerId) ?: kotlinx.coroutines.flow.flowOf(emptyList())

    fun getPendingOffersByManager(managerId: Int): Flow<List<ManagerOffersEntity>> =
        managerOffersDao?.getPendingOffersByManager(managerId) ?: kotlinx.coroutines.flow.flowOf(emptyList())

    fun getOffersByLeagueLevel(level: Int): Flow<List<ManagerOffersEntity>> =
        managerOffersDao?.getOffersByLeagueLevel(level) ?: kotlinx.coroutines.flow.flowOf(emptyList())

    fun getPromotionOffers(): Flow<List<ManagerOffersEntity>> =
        managerOffersDao?.getPromotionOffers() ?: kotlinx.coroutines.flow.flowOf(emptyList())

    suspend fun getOfferWithDetails(offerId: Int): OfferWithDetails? =
        managerOffersDao?.getOfferWithDetails(offerId)

    // ============ DASHBOARD ============

    suspend fun getManagerOffersDashboard(managerId: Int): ManagerOffersDashboard {
        val pendingOffers = managerOffersDao?.getPendingOffersByManager(managerId)?.firstOrNull() ?: emptyList()
        val acceptedOffers = managerOffersDao?.getAcceptedOffersByManager(managerId)?.firstOrNull() ?: emptyList()
        val rejectedOffers = managerOffersDao?.getRejectedOffersByManager(managerId)?.firstOrNull() ?: emptyList()

        return ManagerOffersDashboard(
            totalOffers = pendingOffers.size + acceptedOffers.size + rejectedOffers.size,
            pendingCount = pendingOffers.size,
            acceptedCount = acceptedOffers.size,
            rejectedCount = rejectedOffers.size,
            pendingOffers = pendingOffers,
            level5Offers = pendingOffers.count { it.leagueLevel == 5 },
            level4Offers = pendingOffers.count { it.leagueLevel == 4 },
            level3Offers = pendingOffers.count { it.leagueLevel == 3 },
            level2Offers = pendingOffers.count { it.leagueLevel == 2 },
            level1Offers = pendingOffers.count { it.leagueLevel == 1 },
            highestLevelOffer = pendingOffers.minByOrNull { it.leagueLevel }?.leagueLevel,
            bestSalaryOffer = pendingOffers.maxByOrNull { it.offeredSalary }
        )
    }
}

data class ManagerOffersDashboard(
    val totalOffers: Int,
    val pendingCount: Int,
    val acceptedCount: Int,
    val rejectedCount: Int,
    val pendingOffers: List<ManagerOffersEntity>,
    val level5Offers: Int,
    val level4Offers: Int,
    val level3Offers: Int,
    val level2Offers: Int,
    val level1Offers: Int,
    val highestLevelOffer: Int?,
    val bestSalaryOffer: ManagerOffersEntity?
)
