package com.fameafrica.afm.data.repository

import com.fameafrica.afm.data.database.dao.ClubDNADao
import com.fameafrica.afm.data.database.dao.ClubVisionDao
import com.fameafrica.afm.data.database.dao.TeamsDao
import com.fameafrica.afm.data.database.entities.ClubDNAEntity
import com.fameafrica.afm.data.database.entities.FinancialBehavior
import com.fameafrica.afm.data.database.entities.mapDNAtoVision
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import kotlin.math.max
import kotlin.math.min

@Singleton
class ClubDNARepository @Inject constructor(
    private val clubDNADaoProvider: Provider<ClubDNADao>,
    private val clubVisionDaoProvider: Provider<ClubVisionDao>,
    private val teamsDaoProvider: Provider<TeamsDao>
) {
    private val clubDNADao get() = clubDNADaoProvider.get()
    private val clubVisionDao get() = clubVisionDaoProvider.get()
    private val teamsDao get() = teamsDaoProvider.get()

    suspend fun getClubDNA(teamId: Int): ClubDNAEntity? =
        clubDNADao.getClubDNA(teamId)

    fun observeClubDNA(teamId: Int): Flow<ClubDNAEntity?> =
        clubDNADao.observeClubDNA(teamId)

    suspend fun updateClubDNA(dna: ClubDNAEntity) {
        clubDNADao.insertOrUpdate(dna)
    }

    // =========================================================
    // 🧠 DNA EVOLUTION ENGINE (CRITICAL SYSTEM)
    // =========================================================
    suspend fun processWeeklyDNAEvolution(
        teamId: Int,
        performanceScore: Int,     // 0–100
        financialHealth: Int,      // 0–100
        alignmentScore: Int        // from ClubVision
    ) {

        val dna = clubDNADao.getClubDNA(teamId) ?: return

        var newIdentityStrength = dna.identityStrength
        var newPlayStyleSecondary = dna.playStyleSecondary
        var newTransferPolicy = dna.transferPolicy
        var newFinancialBehavior = dna.financialBehavior

        // =====================================================
        // 🎯 IDENTITY STRENGTH EVOLUTION
        // =====================================================
        newIdentityStrength = when {
            performanceScore > 75 -> min(100, dna.identityStrength + 2)
            performanceScore < 40 -> max(10, dna.identityStrength - 3)
            else -> dna.identityStrength
        }

        // =====================================================
        // ⚽ PLAYSTYLE ADAPTATION (HYBRID EVOLUTION)
        // =====================================================
        if (alignmentScore < 40 && dna.identityStrength < 60) {
            // Club is struggling + weak identity → adapt
            newPlayStyleSecondary = when (dna.playStyle) {
                "POSSESSION" -> "COUNTER"
                "GEGENPRESS" -> "TRANSITION_HEAVY"
                "DEFENSIVE" -> "DIRECT_PHYSICAL"
                else -> "HYBRID_BALANCED"
            }
        }

        // =====================================================
        // 💰 FINANCIAL BEHAVIOR EVOLUTION
        // =====================================================
        newFinancialBehavior = when {
            financialHealth < 30 && dna.financialBehavior == FinancialBehavior.RISKY -> FinancialBehavior.UNSTABLE
            financialHealth < 40 && dna.financialBehavior == FinancialBehavior.SPENDER -> FinancialBehavior.FRUGAL
            financialHealth > 70 && dna.financialBehavior == FinancialBehavior.FRUGAL -> FinancialBehavior.CORPORATE_STRUCTURED // Using CORPORATE_STRUCTURED as a "BALANCED/STABLE" equivalent if BALANCED isn't in enum
            else -> dna.financialBehavior
        }

        // =====================================================
        // 🔁 TRANSFER POLICY EVOLUTION
        // =====================================================
        newTransferPolicy = when {
            performanceScore < 40 && dna.transferPolicy == "YOUTH" -> "BALANCED"
            performanceScore > 70 && dna.transferPolicy == "BALANCED" -> "AGGRESSIVE"
            else -> dna.transferPolicy
        }

        val updatedDNA = dna.copy(
            identityStrength = newIdentityStrength,
            playStyleSecondary = newPlayStyleSecondary,
            transferPolicy = newTransferPolicy,
            financialBehavior = newFinancialBehavior
        )

        clubDNADao.insertOrUpdate(updatedDNA)

        // 🔗 Sync with Vision System (IMPORTANT)
        syncDNAWithVision(updatedDNA)
    }

    // Updating ClubState
    suspend fun updateClubState(
        teamId: Int,
        boardConfidence: Int,
        fanSentiment: Int,
        managerSecurity: Int
    ) {
        // Logic to save these values to your Room Database
        teamsDao.updateSentiment(teamId, boardConfidence, fanSentiment, managerSecurity)
    }

    // =========================================================
    // 🔗 DNA → VISION SYNC
    // =========================================================
    private suspend fun syncDNAWithVision(dna: ClubDNAEntity) {

        val vision = clubVisionDao.getVisionForTeam(dna.teamId).firstOrNull() ?: return

        val updatedVision = mapDNAtoVision(dna).copy(id = vision.id)

        clubVisionDao.update(updatedVision)
    }

    // =========================================================
    // 🧠 MANAGER STYLE IMPACT ON DNA
    // =========================================================
    suspend fun applyManagerInfluence(
        teamId: Int,
        managerStyle: String
    ) {
        val dna = clubDNADao.getClubDNA(teamId) ?: return

        val compatibility = dna.calculateManagerCompatibility(managerStyle)

        val newIdentityStrength = when {
            compatibility > 0.8 -> min(100, dna.identityStrength + 1)
            compatibility < 0.4 -> max(10, dna.identityStrength - 2)
            else -> dna.identityStrength
        }

        clubDNADao.insertOrUpdate(
            dna.copy(identityStrength = newIdentityStrength)
        )
    }
    data class ClubDNA(
        val teamId: Int,
        val financialBehavior: FinancialBehavior,
        var boardConfidence: Int = 50, // Persistent state
        var fanSentiment: Int = 50,    // Persistent state
        var managerSecurity: Int = 80   // Persistent state
    )
}