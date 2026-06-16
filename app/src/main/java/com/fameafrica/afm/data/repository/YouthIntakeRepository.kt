package com.fameafrica.afm.data.repository

import com.fameafrica.afm.data.database.dao.ClubDNADao
import com.fameafrica.afm.data.database.dao.InfrastructureUpgradesDao
import com.fameafrica.afm.data.database.dao.NationalitiesDao
import com.fameafrica.afm.data.database.dao.PlayersDao
import com.fameafrica.afm.data.database.dao.TeamsDao
import com.fameafrica.afm.data.database.entities.PlayersEntity
import com.fameafrica.afm.data.database.entities.UpgradeType
import com.fameafrica.afm.domain.manager.PlayerGenerator
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class YouthIntakeRepository @Inject constructor(
    private val playersDaoProvider: Provider<PlayersDao>,
    private val teamsDaoProvider: Provider<TeamsDao>,
    private val clubDNADaoProvider: Provider<ClubDNADao>,
    private val upgradesDaoProvider: Provider<InfrastructureUpgradesDao>,
    private val nationalitiesDaoProvider: Provider<NationalitiesDao>,
    private val newsRepository: NewsRepository,
    private val playerGenerator: PlayerGenerator
) {
    private val playersDao get() = playersDaoProvider.get()
    private val teamsDao get() = teamsDaoProvider.get()
    private val clubDNADao get() = clubDNADaoProvider.get()
    private val upgradesDao get() = upgradesDaoProvider.get()
    private val nationalitiesDao get() = nationalitiesDaoProvider.get()

    /**
     * Preview of what the academy intake might look like (Sent to inbox).
     */
    suspend fun generateIntakePreview(teamId: Int): IntakePreview {
        val team = teamsDao.getById(teamId) ?: return IntakePreview.EMPTY
        val youthLevel = getAcademyLevel(teamId)
        
        // FAKE DETAIL: Predict the quality of the intake
        val qualityRoll = Random.nextInt(1, 101) + (youthLevel * 10)
        val quality = when {
            qualityRoll > 120 -> IntakeQuality.GOLDEN_GENERATION
            qualityRoll > 90 -> IntakeQuality.EXCELLENT
            qualityRoll > 60 -> IntakeQuality.GOOD
            qualityRoll > 30 -> IntakeQuality.AVERAGE
            else -> IntakeQuality.POOR
        }

        return IntakePreview(
            quality = quality,
            topProspectPosition = listOf("ST", "RW", "CM", "CB").random(),
            estimatedPotential = if (quality == IntakeQuality.GOLDEN_GENERATION) 90 else 75
        )
    }

    private suspend fun getAcademyLevel(teamId: Int): Int {
        return upgradesDao.getLatestUpgradeByType(teamId, UpgradeType.YOUTH_ACADEMY.value)
            ?.let { if (it.status == "Completed") it.targetLevel else it.upgradeLevel } ?: 1
    }

    /**
     * Processes the annual youth intake for all teams or a specific team.
     */
    suspend fun processYouthIntake(teamId: Int? = null) {
        val teams = if (teamId != null) {
            val team = teamsDao.getById(teamId)
            if (team != null) listOf(team) else emptyList()
        } else {
            teamsDao.getAllStatic()
        }

        for (team in teams) {
            val dna = clubDNADao.getClubDNA(team.id)
            val youthLevel = getAcademyLevel(team.id)
            
            // Intake size based on academy level (3-8 players)
            val intakeSize = (3 + (youthLevel / 2) + Random.nextInt(0, 3)).coerceAtMost(10)
            val intakePlayers = mutableListOf<PlayersEntity>()
            
            repeat(intakeSize) {
                var player = playerGenerator.generateYouthPlayer(
                    teamId = team.id,
                    teamName = team.name,
                    region = team.region,
                    nationality = team.country ?: "Tanzania",
                    academyLevel = youthLevel,
                    dna = dna
                )
                
                // Add "African Hotbed" bonus for certain regions (e.g. West Africa)
                if (team.region == "West Africa") {
                    player = player.copy(
                        potential = (player.potential + Random.nextInt(2, 6)).coerceAtMost(99),
                        pace = (player.pace + 5).coerceAtMost(99)
                    )
                }
                
                intakePlayers.add(player)
            }
            
            playersDao.insertAll(intakePlayers)
            
            // Create news for top prospects
            val superstar = intakePlayers.maxByOrNull { it.potential }
            if (superstar != null && superstar.potential >= 88) {
                newsRepository.createNewsArticle(
                    headline = "THE NEXT SUPERSTAR? ${superstar.name} emerges at ${team.name}",
                    content = "Scouts are calling 16-year-old ${superstar.name} the most exciting talent to come out of ${team.country} in a generation. One to watch!",
                    category = "YOUTH",
                    relatedTeam = team.name,
                    relatedPlayer = superstar.name,
                    isTopNews = true
                )
            }
        }
    }

    enum class IntakeQuality { POOR, AVERAGE, GOOD, EXCELLENT, GOLDEN_GENERATION }
    
    data class IntakePreview(
        val quality: IntakeQuality,
        val topProspectPosition: String,
        val estimatedPotential: Int
    ) {
        companion object {
            val EMPTY = IntakePreview(IntakeQuality.AVERAGE, "GK", 60)
        }
    }

    /**
     * Specifically used for "Golden Generation" events driven by StoryEngine
     */
    suspend fun triggerGoldenGeneration(teamId: Int) {
        val team = teamsDao.getById(teamId) ?: return
        val dna = clubDNADao.getClubDNA(teamId)
        val youthLevel = upgradesDao.getLatestUpgradeByType(teamId, UpgradeType.YOUTH_ACADEMY.value)
            ?.let { if (it.status == "Completed") it.targetLevel else it.upgradeLevel } ?: 1
            
        val intakePlayers = mutableListOf<PlayersEntity>()
        
        // Golden generation produces 4-8 very high potential players
        repeat(Random.nextInt(4, 9)) {
            val basePlayer = playerGenerator.generateYouthPlayer(
                teamId = team.id,
                teamName = team.name,
                region = team.region,
                nationality = team.country ?: "Tanzania",
                academyLevel = youthLevel.coerceAtLeast(3), // Minimum effective level 3 for golden generation
                dna = dna
            )
            
            // Boost potential for golden generation
            val boostedPlayer = basePlayer.copy(
                potential = Random.nextInt(85, 99),
                rating = (basePlayer.rating + 5).coerceAtMost(70)
            )
            intakePlayers.add(boostedPlayer)
        }
        
        playersDao.insertAll(intakePlayers)
    }
}
