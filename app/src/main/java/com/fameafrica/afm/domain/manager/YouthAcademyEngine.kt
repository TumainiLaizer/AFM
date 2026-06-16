package com.fameafrica.afm.domain.manager

import com.fameafrica.afm.data.database.entities.*
import com.fameafrica.afm.data.repository.*
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * Handles the generation of regional youth talent and annual youth intakes.
 */
@Singleton
class YouthAcademyEngine @Inject constructor(
    private val teamsRepository: TeamsRepository,
    private val playersRepository: PlayersRepository,
    private val clubDNARepository: ClubDNARepository,
    private val newsRepository: NewsRepository,
    private val playerGenerator: PlayerGenerator
) {

    /**
     * Triggers the annual youth intake for a team.
     * Usually happens once per season in March/April (Africa context: mid-season).
     */
    suspend fun generateAnnualIntake(teamId: Int): List<PlayersEntity> {
        val team = teamsRepository.getTeamById(teamId) ?: return emptyList()
        val dna = clubDNARepository.getClubDNA(teamId)
        
        // Academy Level (1-5) - influenced by facilities
        // For now, derived from team reputation as a proxy
        val academyLevel = (team.reputation / 20).coerceIn(1, 5)
        
        val intakeCount = Random.nextInt(4, 9)
        val prospects = mutableListOf<PlayersEntity>()
        
        repeat(intakeCount) {
            val player = playerGenerator.generateYouthPlayer(
                teamId = teamId,
                teamName = team.name,
                region = team.region,
                nationality = team.country,
                academyLevel = academyLevel,
                dna = dna
            )
            prospects.add(player)
        }
        
        // Sort by potential to identify the "Star" of the intake
        val sortedProspects = prospects.sortedByDescending { it.potential }
        
        // Log news for the best prospect if they are high potential
        if (sortedProspects.first().potential >= 85) {
            newsRepository.createNewsArticle(
                headline = "WONDERKID ALERT: ${sortedProspects.first().name} Graduates",
                content = "${team.name} academy has produced a special talent. ${sortedProspects.first().name} is being hailed as the next big thing in ${team.country} football.",
                category = "CLUB",
                relatedTeamId = teamId,
                relatedTeam = team.name,
                isTopNews = true
            )
        }

        playersRepository.insertAllPlayers(sortedProspects)
        return sortedProspects
    }

    /**
     * Generates a "Wonderkid" somewhere in Africa.
     * Called occasionally during the season to keep the world interesting.
     */
    suspend fun spawnContinentalWonderkid() {
        val allTeams = teamsRepository.getAllTeams().firstOrNull() ?: return
        val luckyTeam = allTeams.random()
        
        val wonderkid = playerGenerator.generateYouthPlayer(
            teamId = luckyTeam.id,
            teamName = luckyTeam.name,
            region = luckyTeam.region,
            nationality = luckyTeam.country,
            academyLevel = 5, // High level for wonderkids
            dna = null
        ).copy(
            potential = Random.nextInt(88, 99),
            reputation = 40 // Slightly higher starting reputation
        )
        
        playersRepository.insertPlayer(wonderkid)
        
        newsRepository.createNewsArticle(
            headline = "African Scouting: Hidden Gem found in ${luckyTeam.country}",
            content = "Reports are coming in about a 16-year-old at ${luckyTeam.name} with incredible technical ability. European scouts are already on alert.",
            category = "WORLD",
            relatedTeamId = luckyTeam.id,
            relatedTeam = luckyTeam.name,
            isTopNews = true
        )
    }
}
