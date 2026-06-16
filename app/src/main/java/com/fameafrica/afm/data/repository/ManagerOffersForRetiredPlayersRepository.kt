package com.fameafrica.afm.data.repository

import com.fameafrica.afm.data.database.dao.ManagerOffersForRetiredPlayersDao
import com.fameafrica.afm.data.database.dao.RetiredPlayerOfferWithDetails
import com.fameafrica.afm.data.database.dao.RoleTypeDistribution
import com.fameafrica.afm.data.database.entities.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import java.util.Calendar

@Singleton
class ManagerOffersForRetiredPlayersRepository @Inject constructor(
    private val retiredPlayerOffersDaoProvider: Provider<ManagerOffersForRetiredPlayersDao>,
    private val playersRepository: PlayersRepository,
    private val teamsRepository: TeamsRepository,
    private val leaguesRepository: LeaguesRepository
) {
    private val retiredPlayerOffersDao get() = retiredPlayerOffersDaoProvider.get()

    // ============ BASIC CRUD ============

    fun getAllOffers(): Flow<List<ManagerOffersForRetiredPlayersEntity>> =
        retiredPlayerOffersDao.getAll()

    suspend fun getOfferById(id: Int): ManagerOffersForRetiredPlayersEntity? =
        retiredPlayerOffersDao.getById(id)

    suspend fun insertOffer(offer: ManagerOffersForRetiredPlayersEntity) =
        retiredPlayerOffersDao.insert(offer)

    suspend fun insertAllOffers(offers: List<ManagerOffersForRetiredPlayersEntity>) =
        retiredPlayerOffersDao.insertAll(offers)

    suspend fun updateOffer(offer: ManagerOffersForRetiredPlayersEntity) =
        retiredPlayerOffersDao.update(offer)

    suspend fun deleteOffer(offer: ManagerOffersForRetiredPlayersEntity) =
        retiredPlayerOffersDao.delete(offer)

    // ============ ATTRIBUTE-BASED ROLE DETERMINATION ============

    /**
     * Determine the appropriate staff role for a retired player based on their attributes
     */
    fun determineStaffRoleForRetiredPlayer(player: PlayersEntity): RetiredPlayerRoleWithDescription {
        val leadership = player.leadership
        val mediaHandling = player.mediaHandling
        val position = player.position
        val age = player.age
        val experience = player.experience
        val decisions = player.decisions
        val anticipation = player.anticipation
        val passing = player.passing
        val finishing = player.finishing
        val defending = player.defending
        val stamina = player.stamina
        val rating = player.rating

        return when {
            // ============ SENIOR MANAGEMENT ROLES ============
            leadership >= 85 -> {
                val role = if (experience >= 300)
                    RetiredPlayerRoleType.SPORTING_DIRECTOR
                else
                    RetiredPlayerRoleType.TECHNICAL_DIRECTOR

                RetiredPlayerRoleWithDescription(
                    roleType = role.value,
                    description = "With exceptional leadership qualities, this player is suited for a senior management role overseeing club strategy."
                )
            }

            leadership >= 80 && mediaHandling >= 65 -> {
                RetiredPlayerRoleWithDescription(
                    roleType = RetiredPlayerRoleType.ASSISTANT_MANAGER.value,
                    description = "Natural leader with good communication skills - ideal as right-hand to the first team manager."
                )
            }

            // ============ PLAYER AGENT ROLE ============
            (decisions >= 75 && mediaHandling >= 60) || (rating >= 80 && leadership < 75) -> {
                val agentSpecialization = determineAgentSpecialization(player)
                val agentPersonality = determineAgentPersonality(player)
                RetiredPlayerRoleWithDescription(
                    roleType = RetiredPlayerRoleType.PLAYER_AGENT.value,
                    description = "An expert in the business side of football. Specialization: $agentSpecialization, Personality: $agentPersonality. Ready to manage player careers."
                )
            }

            // ============ COACHING ROLES ============
            position == "GK" && leadership >= 60 -> {
                RetiredPlayerRoleWithDescription(
                    roleType = RetiredPlayerRoleType.GOALKEEPER_COACH.value,
                    description = "Experienced goalkeeper with ability to train the next generation of shot-stoppers."
                )
            }

            finishing >= 75 && leadership >= 55 -> {
                RetiredPlayerRoleWithDescription(
                    roleType = RetiredPlayerRoleType.ATTACKING_COACH.value,
                    description = "Sharp eye for goal - perfect for improving the squad's clinical edge and forward movement."
                )
            }

            defending >= 75 && leadership >= 55 -> {
                RetiredPlayerRoleWithDescription(
                    roleType = RetiredPlayerRoleType.DEFENSIVE_COACH.value,
                    description = "Solid defensive technician - can teach positional awareness and tackling excellence."
                )
            }

            stamina >= 80 && leadership >= 50 -> {
                RetiredPlayerRoleWithDescription(
                    roleType = RetiredPlayerRoleType.FITNESS_COACH.value,
                    description = "Known for physical endurance, this player can push the squad to peak physical condition."
                )
            }

            passing >= 75 && leadership >= 60 -> {
                RetiredPlayerRoleWithDescription(
                    roleType = RetiredPlayerRoleType.TECHNICAL_COACH.value,
                    description = "Master of technique - ideal for developing the squad's ball retention and distribution."
                )
            }

            // ============ ADMIN & YOUTH ROLES ============
            age >= 30 && (player.potential - player.rating >= 8) -> {
                RetiredPlayerRoleWithDescription(
                    roleType = RetiredPlayerRoleType.ACADEMY_DIRECTOR.value,
                    description = "Knows what it takes to maximize potential - ideal for leading the youth academy."
                )
            }

            leadership >= 70 && experience >= 200 -> {
                RetiredPlayerRoleWithDescription(
                    roleType = RetiredPlayerRoleType.HEAD_OF_YOUTH.value,
                    description = "Experienced professional ready to mentor and guide the next generation of talent."
                )
            }

            // ============ SCOUTING ROLES ============
            decisions >= 70 && anticipation >= 70 -> {
                val role = if (experience >= 250)
                    RetiredPlayerRoleType.CHIEF_SCOUT
                else if (rating >= 75)
                    RetiredPlayerRoleType.REGIONAL_SCOUT
                else
                    RetiredPlayerRoleType.SCOUT

                RetiredPlayerRoleWithDescription(
                    roleType = role.value,
                    description = "Excellent judge of talent - can identify and evaluate players for the club."
                )
            }

            // ============ MEDICAL & MISC ROLES ============
            experience >= 250 && decisions >= 60 -> {
                RetiredPlayerRoleWithDescription(
                    roleType = RetiredPlayerRoleType.SPORTS_SCIENTIST.value,
                    description = "Deep understanding of the physical demands of the game and player recovery."
                )
            }

            mediaHandling >= 70 -> {
                RetiredPlayerRoleWithDescription(
                    roleType = RetiredPlayerRoleType.CLUB_MEDIA_OFFICER.value,
                    description = "Comfortable in front of cameras - perfect for handling club communications."
                )
            }

            // ============ DEFAULT ROLE ============
            else -> {
                RetiredPlayerRoleWithDescription(
                    roleType = RetiredPlayerRoleType.YOUTH_COACH.value,
                    description = "Starts their journey in coaching, focusing on developing younger talents."
                )
            }
        }
    }

    private fun determineAgentSpecialization(player: PlayersEntity): String {
        return when {
            player.rating >= 85 -> "STARS"
            player.potential >= 88 -> "YOUNG_TALENT"
            player.fanPopularity >= 90 -> "INTERNATIONAL"
            player.experience < 100 -> "LOCAL"
            else -> "ALL_ROUNDER"
        }
    }

    private fun determineAgentPersonality(player: PlayersEntity): String {
        return when (player.personalityType) {
            "AMBITIOUS" -> "AGGRESSIVE"
            "PROFESSIONAL" -> "BALANCED"
            "LOYAL" -> "LOYAL"
            "DETERMINED" -> "DEVELOPMENT_FOCUSED"
            "MERCENARY" -> "GREEDY"
            "TEMPERAMENTAL" -> "AGGRESSIVE"
            else -> "BALANCED"
        }
    }

    /**
     * GENERATE OFFER FOR RETIRED PLAYER
     */
    suspend fun generateOfferForRetiredPlayer(
        playerId: Int,
        preferredTeamId: Int? = null
    ): ManagerOffersForRetiredPlayersEntity? {
        val player = playersRepository.getPlayerById(playerId) ?: return null

        if (!player.retired) return null

        val roleWithDescription = determineStaffRoleForRetiredPlayer(player)

        // Find offering team
        val offeringTeam = if (preferredTeamId != null) {
            teamsRepository.getTeamById(preferredTeamId)
        } else {
            val lastTeam = player.teamId?.let { teamsRepository.getTeamById(it) }
            if (lastTeam != null) {
                lastTeam
            } else {
                val targetLevel = when (player.rating) {
                    in 80..99 -> 1..2
                    in 70..79 -> 2..3
                    else -> 3..4
                }
                val leagues = targetLevel.flatMap { level ->
                    leaguesRepository.getLeaguesByLevel(level).firstOrNull() ?: emptyList()
                }
                if (leagues.isNotEmpty()) {
                    val selectedLeague = leagues.random()
                    teamsRepository.getTeamsByLeague(selectedLeague.name).firstOrNull()?.firstOrNull()
                } else null
            }
        } ?: return null

        val league = leaguesRepository.getLeagueByName(offeringTeam.league)
        val leagueLevel = league?.level ?: 3

        // Calculate salary based on role
        val baseSalary = when (roleWithDescription.roleType) {
            RetiredPlayerRoleType.SPORTING_DIRECTOR.value -> 3500000
            RetiredPlayerRoleType.TECHNICAL_DIRECTOR.value -> 3000000
            RetiredPlayerRoleType.ASSISTANT_MANAGER.value -> 2500000
            RetiredPlayerRoleType.ACADEMY_DIRECTOR.value -> 2200000
            RetiredPlayerRoleType.HEAD_OF_YOUTH.value -> 2000000
            RetiredPlayerRoleType.CHIEF_SCOUT.value -> 1800000
            RetiredPlayerRoleType.FIRST_TEAM_COACH.value -> 1600000
            RetiredPlayerRoleType.ATTACKING_COACH.value,
            RetiredPlayerRoleType.DEFENSIVE_COACH.value,
            RetiredPlayerRoleType.TECHNICAL_COACH.value -> 1400000
            RetiredPlayerRoleType.PLAYER_AGENT.value -> 1500000 // Agents have base "expectation"
            RetiredPlayerRoleType.GOALKEEPER_COACH.value,
            RetiredPlayerRoleType.FITNESS_COACH.value -> 1200000
            RetiredPlayerRoleType.REGIONAL_SCOUT.value -> 1100000
            RetiredPlayerRoleType.YOUTH_COACH.value -> 1000000
            RetiredPlayerRoleType.SCOUT.value -> 800000
            RetiredPlayerRoleType.SPORTS_SCIENTIST.value -> 900000
            RetiredPlayerRoleType.CLUB_MEDIA_OFFICER.value -> 700000
            else -> 600000
        }

        val ratingBonus = (player.rating - 50) * 15000
        val offeredSalary = (baseSalary + ratingBonus).coerceAtLeast(600000)

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 14)
        val expiryDate = calendar.timeInMillis

        val offer = ManagerOffersForRetiredPlayersEntity(
            playerId = player.id,
            playerName = player.name,
            offeredTeam = offeringTeam.name,
            offeredTeamId = offeringTeam.id,
            leagueName = offeringTeam.league,
            leagueLevel = leagueLevel,
            offeredSalary = offeredSalary,
            contractYears = if (roleWithDescription.roleType == RetiredPlayerRoleType.SPORTING_DIRECTOR.value) 4 else 2,
            status = "Pending",
            roleType = roleWithDescription.roleType,
            roleDescription = roleWithDescription.description,
            offerDate = System.currentTimeMillis(),
            expiryDate = expiryDate,
            message = buildRetiredPlayerOfferMessage(player, roleWithDescription, offeringTeam)
        )

        retiredPlayerOffersDao.insert(offer)
        return offer
    }

    suspend fun generateOffersForAllRetiredPlayers(): List<ManagerOffersForRetiredPlayersEntity> {
        val retiredPlayers = playersRepository.getRetiredPlayers().firstOrNull() ?: emptyList()
        val offers = mutableListOf<ManagerOffersForRetiredPlayersEntity>()

        retiredPlayers.forEach { player ->
            val existingOffers = retiredPlayerOffersDao.getOffersByPlayer(player.id)
                .firstOrNull() ?: emptyList()

            if (existingOffers.isEmpty()) {
                generateOfferForRetiredPlayer(player.id)?.let { offers.add(it) }
            }
        }
        return offers
    }

    private fun buildRetiredPlayerOfferMessage(
        player: PlayersEntity,
        role: RetiredPlayerRoleWithDescription,
        team: TeamsEntity
    ): String {
        val prefix = when(role.roleType) {
            RetiredPlayerRoleType.PLAYER_AGENT.value -> "We see you transitioning into the business side of the game. "
            RetiredPlayerRoleType.SPORTING_DIRECTOR.value, RetiredPlayerRoleType.TECHNICAL_DIRECTOR.value -> "Your leadership has been legendary. "
            else -> "Your football intelligence is highly valued. "
        }

        return "Dear ${player.name},\n\n" +
                prefix +
                "We believe you have the qualities to become an excellent ${role.roleType.replace('_', ' ').lowercase()}.\n\n" +
                "${role.description}\n\n" +
                "Join us at ${team.name} and start your new chapter in football.\n\n" +
                "Best regards,\n" +
                "${team.name} Board"
    }

    // ============ OFFER RESPONSE HANDLING ============

    suspend fun acceptOffer(offerId: Int): Boolean {
        val offer = retiredPlayerOffersDao.getById(offerId) ?: return false
        if (offer.status != "Pending" || offer.isExpired) return false

        val updatedOffer = offer.copy(status = "Accepted")
        retiredPlayerOffersDao.update(updatedOffer)

        val otherOffers = retiredPlayerOffersDao.getPendingOffersByPlayer(offer.playerId)
            .firstOrNull() ?: emptyList()

        otherOffers.forEach { otherOffer ->
            if (otherOffer.id != offerId) {
                retiredPlayerOffersDao.update(otherOffer.copy(status = "Rejected"))
            }
        }

        return true
    }

    suspend fun rejectOffer(offerId: Int): Boolean {
        val offer = retiredPlayerOffersDao.getById(offerId) ?: return false
        if (offer.status != "Pending") return false

        val updatedOffer = offer.copy(status = "Rejected")
        retiredPlayerOffersDao.update(updatedOffer)
        return true
    }

    // ============ QUERIES ============

    fun getOffersByPlayer(playerId: Int): Flow<List<ManagerOffersForRetiredPlayersEntity>> =
        retiredPlayerOffersDao.getOffersByPlayer(playerId)

    fun getPendingOffersByPlayer(playerId: Int): Flow<List<ManagerOffersForRetiredPlayersEntity>> =
        retiredPlayerOffersDao.getPendingOffersByPlayer(playerId)

    fun getCoachingOffers(): Flow<List<ManagerOffersForRetiredPlayersEntity>> =
        retiredPlayerOffersDao.getCoachingOffers()

    fun getScoutingOffers(): Flow<List<ManagerOffersForRetiredPlayersEntity>> =
        retiredPlayerOffersDao.getScoutingOffers()

    fun getDirectorOffers(): Flow<List<ManagerOffersForRetiredPlayersEntity>> =
        retiredPlayerOffersDao.getDirectorOffers()

    fun getMediaOffers(): Flow<List<ManagerOffersForRetiredPlayersEntity>> =
        retiredPlayerOffersDao.getMediaOffers()

    fun getMedicalOffers(): Flow<List<ManagerOffersForRetiredPlayersEntity>> =
        retiredPlayerOffersDao.getMedicalOffers()

    fun getRoleTypeDistribution(): Flow<List<RoleTypeDistribution>> =
        retiredPlayerOffersDao.getRoleTypeDistribution()

    suspend fun getRetiredPlayerOfferWithDetails(offerId: Int): RetiredPlayerOfferWithDetails? =
        retiredPlayerOffersDao.getRetiredPlayerOfferWithDetails(offerId)
}

data class RetiredPlayerRoleWithDescription(
    val roleType: String,
    val description: String
)
