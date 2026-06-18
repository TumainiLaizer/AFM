package com.fameafrica.afm.data.repository

import com.fameafrica.afm.data.database.dao.SponsorsDao
import com.fameafrica.afm.data.database.dao.TransferFundingRequestsDao
import com.fameafrica.afm.data.database.dao.TeamsDao
import com.fameafrica.afm.data.database.dao.LeaguesDao
import com.fameafrica.afm.data.database.dao.CupsDao
import com.fameafrica.afm.data.database.entities.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class SponsorsRepository @Inject constructor(
    private val sponsorsDaoProvider: Provider<SponsorsDao>,
    private val transferFundingRequestsDaoProvider: Provider<TransferFundingRequestsDao>,
    private val teamsDaoProvider: Provider<TeamsDao>,
    private val leaguesDaoProvider: Provider<LeaguesDao>,
    private val cupsDaoProvider: Provider<CupsDao>,
    private val financesRepository: FinancesRepository
) {
    private val sponsorsDao get() = sponsorsDaoProvider.get()
    private val transferFundingRequestsDao get() = transferFundingRequestsDaoProvider.get()
    private val teamsDao get() = teamsDaoProvider.get()
    private val leaguesDao get() = leaguesDaoProvider.get()
    private val cupsDao get() = cupsDaoProvider.get()

    // ============ BASIC CRUD =============

    fun getAllSponsors(): Flow<List<SponsorsEntity>> = sponsorsDao.getAll()

    suspend fun getSponsorById(id: Int): SponsorsEntity? = sponsorsDao.getById(id)

    suspend fun getSponsorByName(name: String): SponsorsEntity? = sponsorsDao.getByName(name)

    suspend fun insertSponsor(sponsor: SponsorsEntity) = sponsorsDao.insert(sponsor)

    suspend fun updateSponsor(sponsor: SponsorsEntity) = sponsorsDao.update(sponsor)

    suspend fun deleteSponsor(sponsor: SponsorsEntity) = sponsorsDao.delete(sponsor)

    // ============ TEAM SPONSORS =============

    fun getTeamSponsors(teamId: Int): Flow<List<SponsorsEntity>> =
        sponsorsDao.getTeamSponsors(teamId)

    fun getClubSponsors(teamId: Int): Flow<List<SponsorsEntity>> =
        sponsorsDao.getClubSponsors(teamId)

    suspend fun getTotalSponsorshipValue(teamId: Int): Long? =
        sponsorsDao.getTotalSponsorshipValue(teamId)

    suspend fun getTeamPrimarySponsor(teamId: Int): SponsorsEntity? {
        val sponsors = sponsorsDao.getTeamSponsors(teamId).firstOrNull() ?: return null
        return sponsors.maxByOrNull { it.sponsorshipValue }
    }

    /**
     * Updates the annual sponsorship value after negotiation.
     */
    suspend fun updateSponsorshipValue(sponsorId: Int, newValue: Long) {
        sponsorsDao.updateSponsorshipValue(sponsorId, newValue)
    }

    // ============ TITLE SPONSORS =============

    fun getTitleSponsors(): Flow<List<SponsorsEntity>> = sponsorsDao.getTitleSponsors()

    suspend fun getLeagueTitleSponsor(leagueName: String): SponsorsEntity? {
        val league = leaguesDao.getLeagueByName(leagueName) ?: return null
        return league.sponsor?.let { sponsorsDao.getByName(it) }
    }

    suspend fun getCupTitleSponsor(cupName: String): SponsorsEntity? {
        val cup = cupsDao.getByName(cupName) ?: return null
        return cup.sponsor?.let { sponsorsDao.getByName(it) }
    }

    // ============ TRANSFER FUNDING =============

    fun getTransferFundingSponsors(teamId: Int): Flow<List<SponsorsEntity>> =
        sponsorsDao.getTransferFundingSponsors(teamId)

    /**
     * Request transfer funding from a sponsor
     */
    suspend fun requestTransferFunding(
        teamId: Int,
        teamName: String,
        sponsorId: Int,
        amount: Long,
        playerId: Int? = null,
        playerName: String? = null,
        playerNationality: String? = null
    ): TransferFundingRequestEntity? {

        val sponsor = sponsorsDao.getById(sponsorId) ?: return null

        // Check if sponsor can fund transfers
        if (!sponsor.canFundTransfers) return null

        // Check if sponsor has enough funding available
        val availableFunding = (sponsor.transferFundingLimit ?: 0) - sponsor.transferFundingUsed
        if (availableFunding <= 0) return null

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = dateFormat.format(Date())

        val isForeign = playerNationality?.let {
            // Check if player would be foreign in team's league
            true // Simplified for now
        } ?: false

        val request = TransferFundingRequestEntity(
            sponsorId = sponsorId,
            sponsorName = sponsor.name,
            teamId = teamId,
            teamName = teamName,
            playerId = playerId,
            playerName = playerName,
            requestedAmount = amount.coerceAtMost(availableFunding),
            requestDate = today,
            status = FundingRequestStatus.PENDING.value,
            requiresObjectivesCheck = sponsor.requiresObjectivesMet,
            isForeignPlayer = isForeign,
            playerNationality = playerNationality,
            notes = "Transfer funding request for ${playerName ?: "unnamed player"}"
        )

        transferFundingRequestsDao.insert(request)
        return request
    }

    /**
     * Process transfer funding request (called by board)
     */
    suspend fun processFundingRequest(
        requestId: Int,
        approve: Boolean,
        teamPerformanceRating: Int? = null,
        objectivesMet: Boolean? = null
    ): TransferFundingRequestEntity? {

        val request = transferFundingRequestsDao.getById(requestId) ?: return null
        val sponsor = sponsorsDao.getById(request.sponsorId) ?: return null

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = dateFormat.format(Date())

        // Check if objectives need to be met
        if (request.requiresObjectivesCheck && objectivesMet == false) {
            val updated = request.copy(
                status = FundingRequestStatus.REJECTED.value,
                decisionDate = today,
                objectivesMet = false,
                notes = "Rejected: Team objectives not met"
            )
            transferFundingRequestsDao.update(updated)
            return updated
        }

        // Check performance rating requirement
        if (sponsor.minPerformanceRating != null &&
            (teamPerformanceRating ?: 0) < sponsor.minPerformanceRating) {
            val updated = request.copy(
                status = FundingRequestStatus.REJECTED.value,
                decisionDate = today,
                teamPerformanceRating = teamPerformanceRating,
                notes = "Rejected: Performance rating too low"
            )
            transferFundingRequestsDao.update(updated)
            return updated
        }

        // Check foreign player limits
        if (request.isForeignPlayer && sponsor.maxForeignPlayers != null) {
            // Would need to check current foreign player count
            // Simplified for now
        }

        if (approve) {
            // Determine approved amount (could be partial)
            val availableFunding = (sponsor.transferFundingLimit ?: 0) - sponsor.transferFundingUsed
            val approvedAmount = request.requestedAmount.coerceAtMost(availableFunding)

            // Update sponsor's used funding
            sponsorsDao.updateTransferFundingUsed(sponsor.id, approvedAmount)

            // Add to team's budget
            val season = getCurrentSeason()
            financesRepository.addPlayerSale(request.teamId, season, approvedAmount)

            val updated = request.copy(
                status = if (approvedAmount == request.requestedAmount)
                    FundingRequestStatus.APPROVED.value
                else
                    FundingRequestStatus.PARTIALLY_APPROVED.value,
                approvedAmount = approvedAmount,
                decisionDate = today,
                objectivesMet = objectivesMet,
                teamPerformanceRating = teamPerformanceRating,
                notes = "Approved: $approvedAmount funding provided"
            )
            transferFundingRequestsDao.update(updated)
            return updated
        } else {
            val updated = request.copy(
                status = FundingRequestStatus.REJECTED.value,
                decisionDate = today,
                objectivesMet = objectivesMet,
                teamPerformanceRating = teamPerformanceRating,
                notes = "Rejected by board/sponsor"
            )
            transferFundingRequestsDao.update(updated)
            return updated
        }
    }

    /**
     * Get available transfer funding for a team
     */
    suspend fun getAvailableTransferFunding(teamId: Int): Long {
        return sponsorsDao.getTotalAvailableTransferFunding(teamId) ?: 0L
    }

    /**
     * Get pending funding requests for a team
     */
    fun getTeamFundingRequests(teamId: Int): Flow<List<TransferFundingRequestEntity>> =
        transferFundingRequestsDao.getRequestsByTeam(teamId)

    // ============ PLACEHOLDER SPONSORS =============

    /**
     * Get FAME Africa™ placeholder sponsor (always available for funding)
     */
    suspend fun getFameAfricaSponsor(): SponsorsEntity? {
        return sponsorsDao.getByName("FAME Africa™") ?: createFameAfricaSponsor()
    }

    private suspend fun createFameAfricaSponsor(): SponsorsEntity {
        val sponsor = SponsorsEntity(
            name = "FAME Africa™",
            logo = "sponsor/logos/fame_africa.webp",
            sponsorType = SponsorType.PLACEHOLDER.value,
            sponsorshipValue = 100_000_000,
            contractDuration = 5,
            performanceBonus = 20_000_000,
            transferFundingLimit = 50_000_000,
            fundingPerSeason = 50_000_000,
            canFundTransfers = true,
            requiresObjectivesMet = true,
            notes = "FAME Africa™ placeholder sponsor - available for transfer funding when objectives met"
        )
        sponsorsDao.insert(sponsor)
        return sponsor
    }

    // ============ END OF SEASON PROCESSING =============

    /**
     * Process end of season sponsor activities
     * - Deactivate expired sponsors
     * - Reset transfer funding used
     * - Check for renewals
     */
    suspend fun processEndOfSeason() {
        // Deactivate expired sponsors
        sponsorsDao.deactivateExpiredSponsors()

        // Reset transfer funding used for active sponsors
        val activeSponsors = sponsorsDao.getActiveSponsors().firstOrNull() ?: emptyList()
        activeSponsors.forEach { sponsor ->
            if (sponsor.fundingPerSeason != null) {
                val updated = sponsor.copy(
                    transferFundingUsed = 0,
                    transferFundingLimit = sponsor.fundingPerSeason
                )
                sponsorsDao.update(updated)
            }
        }

        // Generate renewal opportunities for expiring sponsors
        val expiringSponsors = sponsorsDao.getSponsorsNeedingRenewal().firstOrNull() ?: emptyList()
        expiringSponsors.forEach { sponsor ->
            // Would trigger board notification about renewal
        }
    }

    // ============ SPONSOR MATCHING =============

    /**
     * Find potential sponsors for a team based on performance
     */
    suspend fun findPotentialSponsors(teamId: Int, teamPerformanceRating: Int): List<SponsorsEntity> {
        val allSponsors = sponsorsDao.getAll().firstOrNull() ?: return emptyList()

        return allSponsors.filter { sponsor ->
            sponsor.teamId == null && // Not already tied to a team
                    sponsor.isActive &&
                    (sponsor.minPerformanceRating == null ||
                            teamPerformanceRating >= sponsor.minPerformanceRating)
        }.sortedByDescending { it.sponsorshipValue }
    }

    // ============ UTILITY =============

    private fun getCurrentSeason(): String {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)

        return if (month >= Calendar.AUGUST) {
            "$year/${year + 1}"
        } else {
            "${year - 1}/$year"
        }
    }

    // ============ DASHBOARD =============

    suspend fun getTeamSponsorDashboard(teamId: Int): TeamSponsorDashboard {
        val sponsors = sponsorsDao.getTeamSponsors(teamId).firstOrNull() ?: emptyList()
        val clubSponsors = sponsors.filter { it.sponsorType == SponsorType.CLUB_SPONSOR.value }
        val titleSponsors = sponsors.filter { it.isTitleSponsor }
        val transferFunders = sponsors.filter { it.canFundTransfers }

        val totalValue = sponsors.sumOf { it.sponsorshipValue }
        val totalFundingAvailable = sponsorsDao.getTotalAvailableTransferFunding(teamId) ?: 0L

        val primarySponsor = sponsors.maxByOrNull { it.sponsorshipValue }

        val pendingRequests = transferFundingRequestsDao.getRequestsByTeam(teamId)
            .firstOrNull()?.filter { it.isPending } ?: emptyList()

        return TeamSponsorDashboard(
            teamName = primarySponsor?.teamName ?: "Unknown",
            totalSponsors = sponsors.size,
            clubSponsors = clubSponsors.size,
            titleSponsors = titleSponsors.size,
            transferFunders = transferFunders.size,
            totalSponsorshipValue = totalValue,
            totalAvailableTransferFunding = totalFundingAvailable,
            primarySponsor = primarySponsor,
            sponsors = sponsors,
            pendingFundingRequests = pendingRequests
        )
    }
}

// ============ DATA CLASSES =============

data class TeamSponsorDashboard(
    val teamName: String,
    val totalSponsors: Int,
    val clubSponsors: Int,
    val titleSponsors: Int,
    val transferFunders: Int,
    val totalSponsorshipValue: Long,
    val totalAvailableTransferFunding: Long,
    val primarySponsor: SponsorsEntity?,
    val sponsors: List<SponsorsEntity>,
    val pendingFundingRequests: List<TransferFundingRequestEntity>
)
