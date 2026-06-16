package com.fameafrica.afm.data.repository

import com.fameafrica.afm.data.database.dao.StoryEventsDao
import com.fameafrica.afm.data.database.entities.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class StoryEventsRepository @Inject constructor(
    private val storyEventsDaoProvider: Provider<StoryEventsDao>,
    private val newsRepository: NewsRepository,
    private val playersRepository: PlayersRepository,
    private val teamsRepository: TeamsRepository,
    private val leaguesRepository: LeaguesRepository
) {

    private val storyEventsDao: StoryEventsDao?
        get() = try {
            storyEventsDaoProvider.get()
        } catch (e: Exception) {
            null
        }

    private val random = Random(System.currentTimeMillis())

    // ============ Public API ============

    fun getAllEvents(): Flow<List<StoryEventsEntity>> = storyEventsDao?.getAll() ?: kotlinx.coroutines.flow.flowOf(emptyList())

    suspend fun getUnresolvedEvents(teamId: Int? = null): List<StoryEventsEntity> =
        storyEventsDao?.getUnresolved(teamId)?.firstOrNull() ?: emptyList()

    /**
     * Generate a random story event for a given team (or global).
     * Probability can be adjusted via chance parameter.
     */
    suspend fun generateRandomEvent(
        teamId: Int,
        teamName: String,
        forceChance: Int = 15 // percentage
    ): StoryEventsEntity? {
        if (random.nextInt(100) >= forceChance) return null

        // Load context data
        val team = teamsRepository.getTeamById(teamId) ?: return null
        val league = leaguesRepository.getLeagueByName(team.league)
        val players = playersRepository.getPlayersByTeamId(teamId).firstOrNull() ?: emptyList()
        val starPlayer = players.maxByOrNull { it.overallRating }

        // Choose a random template from the combined list
        val template = allTemplates.random(random)

        // Determine if this is a global event (no team attached)
        val isGlobal = template.category == "GLOBAL" || template.category.startsWith("LEAGUE_")

        // For templates that need a player, pick one (prefer star player if available)
        val targetPlayer = if (template.needsPlayer && players.isNotEmpty()) {
            // 70% chance to use the star player if exists, otherwise random
            if (starPlayer != null && random.nextInt(100) < 70) starPlayer else players.random()
        } else null

        // Fill placeholders
        val title = fillPlaceholders(template.titleTemplate, teamName, targetPlayer?.name, league?.name)
        val description = fillPlaceholders(template.descriptionTemplate, teamName, targetPlayer?.name, league?.name)

        // Calculate impact value
        val impactValue = if (template.impactRange != null) {
            random.nextInt(template.impactRange.first, template.impactRange.last + 1)
        } else {
            template.fixedImpact ?: 0
        }

        val event = StoryEventsEntity(
            title = title,
            description = description,
            category = template.category,
            impactType = template.impactType,
            impactValue = impactValue,
            teamId = if (isGlobal) null else teamId,
            playerId = targetPlayer?.id,
            isGlobal = isGlobal,
            timestamp = System.currentTimeMillis(),
            isResolved = false
        )

        val eventId = storyEventsDao?.insert(event) ?: 0L

        // Wired to News Screen instead of Notifications
        convertToNews(event)

        return event
    }

    /**
     * Generate a story specifically about a player (e.g., after a match or transfer).
     */
    suspend fun generateStoryForPlayer(
        playerId: Int,
        context: String = "general"
    ): StoryEventsEntity? {
        val player = playersRepository.getPlayerById(playerId) ?: return null
        val team = player.teamId?.let { teamsRepository.getTeamById(it) } ?: return null
        val league = leaguesRepository.getLeagueByName(team.league)

        // Use player-centric templates
        val templates = playerTemplates + mickyJuniorTemplates.filter { it.needsPlayer }
        if (templates.isEmpty()) return null

        val template = templates.random(random)

        val title = fillPlaceholders(template.titleTemplate, team.name, player.name, league?.name)
        val description = fillPlaceholders(template.descriptionTemplate, team.name, player.name, league?.name)

        val impactValue = if (template.impactRange != null) {
            random.nextInt(template.impactRange.first, template.impactRange.last + 1)
        } else {
            template.fixedImpact ?: 0
        }

        val event = StoryEventsEntity(
            title = title,
            description = description,
            category = template.category,
            impactType = template.impactType,
            impactValue = impactValue,
            teamId = team.id,
            playerId = player.id,
            isGlobal = false,
            timestamp = System.currentTimeMillis(),
            isResolved = false
        )

        val eventId = storyEventsDao?.insert(event) ?: 0L
        
        // Wired to News Screen
        convertToNews(event)
        
        return event
    }

    suspend fun resolveEvent(eventId: Int) {
        storyEventsDao?.resolveEvent(eventId)
    }

    // ============ Internal Helpers ============

    private fun fillPlaceholders(template: String, team: String, player: String?, league: String?): String {
        return template
            .replace("{team}", team)
            .replace("{player}", player ?: "a player")
            .replace("{league}", league ?: "the league")
    }

    private suspend fun convertToNews(event: StoryEventsEntity) {
        newsRepository.createNewsArticle(
            headline = event.title,
            content = event.description,
            category = event.category,
            relatedTeamId = event.teamId,
            relatedTeam = event.teamId?.let { teamsRepository.getTeamById(it)?.name },
            relatedPlayerId = event.playerId,
            relatedPlayer = event.playerId?.let { playersRepository.getPlayerById(it)?.name },
            isTopNews = event.impactValue >= 15 || event.impactValue <= -15
        )
    }

    // ============ Story Template Data Class ============

    private data class StoryTemplate(
        val titleTemplate: String,
        val descriptionTemplate: String,
        val category: String,           // e.g., "TRANSFER", "SCANDAL", "TAKEOVER", "FAN_PROTEST", "GLOBAL"
        val impactType: String,          // "MORALE", "REPUTATION", "FINANCES", "STATUS"
        val impactRange: IntRange? = null,
        val fixedImpact: Int? = null,
        val needsPlayer: Boolean = false
    )

    // ============ Template Collections ============

    private val mickyJuniorTemplates: List<StoryTemplate> = listOf(
        StoryTemplate(
            titleTemplate = "EXCL: {player} Future Update",
            descriptionTemplate = "Sources tell me that {player} is attracting serious interest from top clubs in the North. Negotiations ongoing. 🇹🇿⚽ #AfricanFootball",
            category = "TRANSFER",
            impactType = "REPUTATION",
            impactRange = 5..10,
            needsPlayer = true
        ),
        StoryTemplate(
            titleTemplate = "Micky Junior: {team} on the move",
            descriptionTemplate = "{team} is looking to bolster their squad before the window closes. Expect some big names. 💼🔥",
            category = "TRANSFER",
            impactType = "REPUTATION",
            fixedImpact = 5,
            needsPlayer = false
        )
    )

    private val storyTemplates: List<StoryTemplate> = listOf(
        // ---------- Transfer Drama ----------
        StoryTemplate(
            titleTemplate = "Transfer Saga: {player} wants out!",
            descriptionTemplate = "Star player {player} has handed in a transfer request, citing desire to play in Europe. The board faces a dilemma.",
            category = "TRANSFER",
            impactType = "MORALE",
            impactRange = -15..-5,
            needsPlayer = true
        ),
        StoryTemplate(
            titleTemplate = "Last‑minute Deal: {team} signs a new star!",
            descriptionTemplate = "In a dramatic deadline‑day move, {team} has secured the signature of a highly rated striker. Fans are ecstatic!",
            category = "TRANSFER",
            impactType = "MORALE",
            impactRange = 10..20,
            needsPlayer = false
        ),
        StoryTemplate(
            titleTemplate = "Agent Claims: {player} being tapped up",
            descriptionTemplate = "The agent of {player} has accused a rival club of illegal approach. The league may investigate.",
            category = "SCANDAL",
            impactType = "REPUTATION",
            impactRange = -10..-3,
            needsPlayer = true
        ),

        // ---------- Boardroom Drama ----------
        StoryTemplate(
            titleTemplate = "Takeover Talks: Local businessman eyes {team}",
            descriptionTemplate = "A wealthy local investor has expressed interest in buying {team}. The fans are hopeful for a new era.",
            category = "TAKEOVER",
            impactType = "REPUTATION",
            impactRange = 5..15,
            needsPlayer = false
        ),
        StoryTemplate(
            titleTemplate = "Financial Crisis at {team}",
            descriptionTemplate = "Reports emerge that {team} is struggling to pay wages. Players may be forced to take a pay cut.",
            category = "FINANCIAL",
            impactType = "MORALE",
            impactRange = -25..-15,
            needsPlayer = false
        ),
        StoryTemplate(
            titleTemplate = "Board Split: Disagreement over manager's future",
            descriptionTemplate = "Behind closed doors, the board is divided on whether to sack the under‑performing coach.",
            category = "BOARD",
            impactType = "STATUS",
            impactRange = -10..0,
            needsPlayer = false
        ),

        // ---------- Rivalry & Fan Incidents ----------
        StoryTemplate(
            titleTemplate = "Derby Fever: Tensions rise before the big match",
            descriptionTemplate = "Ahead of the local derby, fans from both sides clashed in the city centre. Security has been increased.",
            category = "FAN_VIOLENCE",
            impactType = "REPUTATION",
            impactRange = -15..-5,
            needsPlayer = false
        ),
        StoryTemplate(
            titleTemplate = "Pitch Invasion! Fans celebrate wildly",
            descriptionTemplate = "After a last‑gasp winner, hundreds of {team} fans invaded the pitch to celebrate with the players.",
            category = "FAN_CELEBRATION",
            impactType = "MORALE",
            impactRange = 5..15,
            needsPlayer = false
        ),
        StoryTemplate(
            titleTemplate = "Referee Controversy: {team} robbed?",
            descriptionTemplate = "A controversial penalty decision has infuriated {team}. The manager has been charged by the FA for post‑match comments.",
            category = "CONTROVERSY",
            impactType = "STATUS",
            impactRange = -5..5,
            needsPlayer = false
        ),

        // ---------- Injury & Comeback ----------
        StoryTemplate(
            titleTemplate = "Devastating Injury: {player} out for season",
            descriptionTemplate = "Key midfielder {player} suffered a torn ACL in training and will miss the rest of the campaign.",
            category = "INJURY",
            impactType = "MORALE",
            impactRange = -25..-15,
            needsPlayer = true
        ),
        StoryTemplate(
            titleTemplate = "Miraculous Recovery: {player} back earlier than expected",
            descriptionTemplate = "After intense rehab, {player} has returned to full training – a huge boost for the team.",
            category = "INJURY",
            impactType = "MORALE",
            impactRange = 15..25,
            needsPlayer = true
        ),

        // ---------- Media Scandals ----------
        StoryTemplate(
            titleTemplate = "Social Media Storm: {player} under fire",
            descriptionTemplate = "An old tweet from {player} has resurfaced, causing outrage among fans. The club issued a statement.",
            category = "SCANDAL",
            impactType = "REPUTATION",
            impactRange = -20..-10,
            needsPlayer = true
        ),
        StoryTemplate(
            titleTemplate = "Charity Hero: {player} donates to local hospital",
            descriptionTemplate = "In a heartwarming gesture, {player} has donated a significant amount to a children's hospital.",
            category = "POSITIVE",
            impactType = "REPUTATION",
            impactRange = 10..20,
            needsPlayer = true
        ),

        // ---------- African Specific Stories ----------
        StoryTemplate(
            titleTemplate = "Juju Accusations: Opponent claims witchcraft",
            descriptionTemplate = "Before the match, the opposing coach alleged that {team} used juju. The accusation has gone viral.",
            category = "CONTROVERSY",
            impactType = "REPUTATION",
            impactRange = -10..10,
            needsPlayer = false
        ),
        StoryTemplate(
            titleTemplate = "Buses late! {team} arrives just before kickoff",
            descriptionTemplate = "The team bus got stuck in traffic, forcing a delayed kickoff. The league may fine the club.",
            category = "LOGISTICS",
            impactType = "STATUS",
            impactRange = -5..0,
            needsPlayer = false
        ),
        StoryTemplate(
            titleTemplate = "Stadium Upgrade: New floodlights installed",
            descriptionTemplate = "{team} has completed a major stadium renovation, now eligible for CAF night matches.",
            category = "INFRASTRUCTURE",
            impactType = "REPUTATION",
            impactRange = 5..15,
            needsPlayer = false
        ),
        StoryTemplate(
            titleTemplate = "Youth Academy Gem: 16‑year‑old signs first pro contract",
            descriptionTemplate = "The academy has produced another talent: a 16‑year‑old winger has signed his first professional contract.",
            category = "YOUTH",
            impactType = "MORALE",
            impactRange = 10..20,
            needsPlayer = false
        ),

        // ---------- Player Awards / Achievements ----------
        StoryTemplate(
            titleTemplate = "{player} named African Young Player of the Year",
            descriptionTemplate = "The Confederation of African Football has awarded {player} the prestigious Young Player of the Year award.",
            category = "AWARD",
            impactType = "REPUTATION",
            impactRange = 20..30,
            needsPlayer = true
        ),
        StoryTemplate(
            titleTemplate = "{player} wins Goal of the Season",
            descriptionTemplate = "A stunning strike from {player} has been voted the league's Goal of the Season.",
            category = "AWARD",
            impactType = "REPUTATION",
            impactRange = 10..20,
            needsPlayer = true
        ),
        StoryTemplate(
            titleTemplate = "{player} in scintillating form",
            descriptionTemplate = "{player} has scored in three consecutive matches, drawing praise from fans and pundits alike.",
            category = "PLAYER_FORM",
            impactType = "MORALE",
            impactRange = 5..15,
            needsPlayer = true
        ),

        // ---------- Global / League Events ----------
        StoryTemplate(
            titleTemplate = "CAF announces new club competition format",
            descriptionTemplate = "The Confederation of African Football has unveiled a revamped Champions League format, with more prize money.",
            category = "GLOBAL",
            impactType = "STATUS",
            impactRange = 5..10,
            needsPlayer = false
        ),
        StoryTemplate(
            titleTemplate = "League suspension due to political unrest",
            descriptionTemplate = "All matches in {league} have been postponed indefinitely following recent events in the country.",
            category = "GLOBAL",
            impactType = "STATUS",
            impactRange = -20..-10,
            needsPlayer = false
        ),
        StoryTemplate(
            titleTemplate = "Record TV deal for {league}",
            descriptionTemplate = "A new broadcasting contract worth millions has been signed, boosting every club's finances.",
            category = "GLOBAL",
            impactType = "FINANCES",
            impactRange = 20..40,
            needsPlayer = false
        )
    )

    private val playerTemplates: List<StoryTemplate> get() = storyTemplates.filter { it.needsPlayer }
    private val allTemplates: List<StoryTemplate> get() = storyTemplates + mickyJuniorTemplates
}
