package com.fameafrica.afm.domain.manager

import com.fameafrica.afm.data.repository.*
import com.fameafrica.afm.data.database.entities.*
import kotlinx.serialization.json.Json
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class StoryEngine @Inject constructor(
    private val newsRepository: NewsRepository,
    private val worldStateRepository: WorldStateRepository,
    private val leagueContextRepository: LeagueContextRepository,
    private val teamsRepository: TeamsRepository,
    private val playersRepository: PlayersRepository,
    private val clubDNARepository: ClubDNARepository,
    private val leagueStandingsRepository: LeagueStandingsRepository,
    private val chairmanRepository: ChairmanRepository
) {
    suspend fun processWeeklyStories(week: Int, season: String, userTeamId: Int) {
        generateTitleRaceNarrative(week)
        generateRelegationNarrative(week)
        generatePlayerBreakthroughNarrative(week)
        generateDNABasedNarratives(week, userTeamId)
        generateFinancialNarratives(week)
        generateTransferPolicyNarratives(week)
        generateIdentityNarratives(week)
        generateManagerPressureNarratives(week, season, userTeamId)
        generateTakeoverNarratives(week)
        processNarrativeChains(week)
        checkCareerMilestones(userTeamId)
    }

    private suspend fun processNarrativeChains(week: Int) {
        // Implementation for linked stories across multiple weeks
        if (week % 4 == 0 && Random.nextInt(100) < 40) {
            val highPotentialYouth = playersRepository.getTopYoungPlayers(85).firstOrNull()?.randomOrNull()
            highPotentialYouth?.let { p ->
                newsRepository.createNewsArticle(
                    headline = "SCOUT'S NOTEBOOK: The Rise of ${p.name}",
                    content = "Our analysts have been tracking ${p.name} for weeks. The ${p.age}-year-old's technical ceiling is unlike anything we've seen in ${p.nationality} recently.",
                    category = "WORLD",
                    relatedPlayer = p.name,
                    isTopNews = true
                )
                // Next week, a rival might "enquire" about him (to be handled in TransfersRepository)
            }
        }
    }

    private suspend fun checkCareerMilestones(teamId: Int) {
        val team = teamsRepository.getTeamById(teamId) ?: return
        // FAKE DETAIL: Recognize manager longevity or success
        if (team.points > 50 && Random.nextInt(100) < 10) {
            newsRepository.createNewsArticle(
                headline = "THE MIDAS TOUCH: Manager's Impact at ${team.name}",
                content = "Statistics show a marked improvement in ${team.name}'s win percentage since the current manager took over. Fans are calling for a long-term contract extension.",
                category = "MANAGEMENT",
                relatedTeam = team.name,
                isTopNews = false
            )
        }
    }

    private suspend fun generateTakeoverNarratives(week: Int) {
        // Look for recent takeover news and add follow-up reactions
        val recentNews = newsRepository.getAllNews().firstOrNull() ?: return
        val takeoverNews = recentNews.filter { it.category == "TAKEOVER" && it.id > recentNews.size - 10 }

        for (news in takeoverNews) {
            if (Random.nextInt(100) < 30) {
                val teamId = news.relatedTeamId ?: continue
                val team = teamsRepository.getTeamById(teamId) ?: continue
                val chairman =chairmanRepository.getChairmanByTeam(teamId) ?: continue

                val (headline, content) = when {
                    (chairman.wealthLevel ?: 0) > 85 -> {
                        "WAR CHEST READY: ${team.name} Prepare for Spending Spree" to "With ${chairman.name}'s deep pockets, ${team.name} are expected to dominate the next transfer window. Rival clubs are on high alert."
                    }
                    (chairman.patienceLevel ?: 0) < 30 -> {
                        "TICKING TIME BOMB: Pressure at ${team.name}" to "Pundits warn that the new ownership at ${team.name} lacks patience. The manager is under immediate pressure to deliver results."
                    }
                    else -> {
                        "HONEYMOON PERIOD: ${team.name} Fans Embrace ${chairman.name}" to "The early signs under the new regime at ${team.name} are positive, with fans optimistic about the club's long-term vision."
                    }
                }

                newsRepository.createNewsArticle(
                    headline = headline,
                    content = content,
                    category = "CLUB",
                    relatedTeamId = teamId,
                    relatedTeam = team.name,
                    uniqueCheck = true
                )
            }
        }
    }

    private suspend fun generateManagerPressureNarratives(week: Int, season: String, userTeamId: Int) {
        if (week < 5) return // No pressure narratives in first month

        // Optimize: Pick from top 100 teams directly
        val candidateTeams = teamsRepository.getTopTeamsByElo(100).firstOrNull()?.shuffled()?.take(5) ?: return

        for (team in candidateTeams) {
            val seasonYear = season.split("/").firstOrNull()?.toIntOrNull() ?: 2024
            val standing = leagueStandingsRepository.getTeamStanding(team.id, team.league, seasonYear) ?: continue
            
            // Pressure thresholds based on performance and board confidence
            val matchesPlayed = standing.matchesPlayed
            if (matchesPlayed < 3) continue // Not enough data for pressure news

            val isBigClub = team.reputation > 70
            val position = standing.position
            val lowConfidence = team.boardConfidence < 40
            val criticalConfidence = team.boardConfidence < 25
            val lowSecurity = team.managerSecurity < 35
            
            val (headline, content, category) = when {
                criticalConfidence || (isBigClub && position > 12 && lowConfidence) -> {
                    Triple(
                        "ULTIMATUM AT ${team.name.uppercase()}: Manager on the Brink",
                        "The board's confidence in the management at ${team.name} has plummeted to ${team.boardConfidence}%. Sources suggest a loss in the next match could be the end of the road.",
                        "MANAGEMENT"
                    )
                }
                lowSecurity && team.fanSentiment < 30 -> {
                    Triple(
                        "VOTE OF NO CONFIDENCE: ${team.name} Fans Revolt",
                        "With fan sentiment at a seasonal low of ${team.fanSentiment}%, the atmosphere at ${team.name} has turned toxic. The manager is fighting a losing battle to keep their job.",
                        "MANAGEMENT"
                    )
                }
                isBigClub && position > 8 -> {
                    Triple(
                        "CRISIS AT ${team.name.uppercase()}: Fans Demand Results",
                        "With ${team.name} languishing in ${position}th place, pressure is mounting. Board confidence has dipped to ${team.boardConfidence}% as expectations remain unfulfilled.",
                        "MANAGEMENT"
                    )
                }
                !isBigClub && position > 15 -> {
                    Triple(
                        "SURVIVAL STRUGGLE: ${team.name} Facing Relegation Fear",
                        "Currently sitting ${position}th, ${team.name} are in desperate need of points. Manager security is rated at ${team.managerSecurity}%, reflecting the precarious nature of their position.",
                        "MANAGEMENT"
                    )
                }
                team.identityStrength > 80 && position > 6 && lowConfidence -> {
                    Triple(
                        "IDENTITY CRISIS: ${team.name} Losing Their Way?",
                        "Despite their strong club identity, ${team.name} are struggling for form in ${position}th. Questions are being asked if the current tactical approach is still effective under the current regime.",
                        "CLUB"
                    )
                }
                else -> continue
            }

            newsRepository.createNewsArticle(
                headline = headline,
                content = content,
                category = category,
                relatedTeamId = team.id,
                relatedTeam = team.name,
                uniqueCheck = true
            )
        }
    }

    private suspend fun generateFinancialNarratives(week: Int) {
        if (Random.nextInt(100) < 15) {
            val teams = teamsRepository.getAllTeams().firstOrNull() ?: return
            val randomTeam = teams.randomOrNull() ?: return

            val (headline, content) = when (randomTeam.financialBehavior) {
                FinancialBehavior.FRUGAL -> {
                    if (randomTeam.debtLevel > 0) {
                        "AUSTERITY MEASURES: ${randomTeam.name} Tightening Belts" to "${randomTeam.name} have adopted a frugal approach to handle their ${randomTeam.debtLevel} debt. The board is prioritizing long-term survival over short-term signings."
                    } else {
                        "BALANCED BOOKS: ${randomTeam.name}'s Sustainable Model" to "While others spend big, ${randomTeam.name} are proving that financial discipline can lead to stability in ${randomTeam.league}."
                    }
                }
                FinancialBehavior.SPENDER -> "BIG SPENDERS: ${randomTeam.name} Market Dominance" to "${randomTeam.name} continue to flex their financial muscles, leaving rivals wondering how to keep up with their recruitment drive."
                FinancialBehavior.RISKY -> "LIVING ON THE EDGE: ${randomTeam.name}'s Bold Gamble" to "${randomTeam.name}'s high-risk high-reward financial strategy is drawing both admiration and concern from league observers."
                FinancialBehavior.UNSTABLE -> "FINANCIAL TURMOIL: ${randomTeam.name} in Trouble" to "Reports indicate ${randomTeam.name} are entering a period of instability. Their current financial trend suggests urgent measures are needed to avoid a full-blown crisis."
                FinancialBehavior.EXPORT_CRISIS -> "FIRE SALE? ${randomTeam.name} Forced to Sell" to "Facing a severe financial crisis, ${randomTeam.name} may be forced to sell their best assets just to stay afloat. Fans are bracing for departures."
                FinancialBehavior.PLAYER_SALES_DEPENDENT -> "THE EXPORT MODEL: ${randomTeam.name}'s Talent Factory" to "${randomTeam.name} continue to rely on their ability to develop and sell talent to maintain their competitive edge."
                FinancialBehavior.SPONSOR_DEPENDENT -> "SPONSORSHIP BOOST: ${randomTeam.name}'s Commercial Growth" to "${randomTeam.name} have secured key sponsorship deals, reducing their reliance on direct revenue and stabilizing their financial outlook."
                FinancialBehavior.GOVERNMENT_BACKED -> "STATE SUPPORT: ${randomTeam.name}'s Public Funding" to "As a government-backed institution, ${randomTeam.name} enjoys a level of security that many of their rivals can only dream of."
                FinancialBehavior.TOURNAMENT_DRIVEN -> "PRIZE MONEY FOCUS: ${randomTeam.name} Eyeing Success" to "For ${randomTeam.name}, every cup run is vital. Their financial model depends heavily on deep runs in major competitions."
                FinancialBehavior.COMMUNITY_FUNDED -> "PEOPLE'S CLUB: ${randomTeam.name}'s Grassroots Model" to "${randomTeam.name} continues to be powered by its supporters, proving that a community-funded model can compete at the highest level."
                FinancialBehavior.CORPORATE_STRUCTURED -> "CORPORATE PRECISION: ${randomTeam.name}'s Business Approach" to "With a rigid corporate structure, ${randomTeam.name} operates with the efficiency of a multinational company, minimizing financial waste."
                FinancialBehavior.LOW_REVENUE_SURVIVAL -> "SURVIVAL MODE: ${randomTeam.name}'s Daily Struggle" to "Operating on a shoestring budget, ${randomTeam.name} are masters of making every cent count in their battle for survival."
                FinancialBehavior.AGGRESSIVE -> "AGGRESSIVE EXPANSION: ${randomTeam.name}'s Bold Moves" to "${randomTeam.name} are pushing their financial limits in a bid for rapid growth, a strategy that could lead to glory or ruin."
            }

            newsRepository.createNewsArticle(
                headline = headline,
                content = content,
                category = "FINANCE",
                relatedTeamId = randomTeam.id,
                relatedTeam = randomTeam.name,
                uniqueCheck = true
            )
        }
    }

    private suspend fun generateTransferPolicyNarratives(week: Int) {
        if (Random.nextInt(100) < 12) {
            val teams = teamsRepository.getAllTeams().firstOrNull() ?: return
            val randomTeam = teams.randomOrNull() ?: return
            val dna = clubDNARepository.getClubDNA(randomTeam.id) ?: return

            val (headline, content) = when (dna.transferPolicy) {
                "EXPORT_FOCUSED" -> "THE TALENT CONVEYOR BELT: ${randomTeam.name}'s Export Success" to "${randomTeam.name} are gaining a reputation as Africa's premier talent hub, with several stars linked to overseas moves."
                "LOCAL_TALENT_ONLY" -> "HOMEGROWN PRIDE: ${randomTeam.name}'s Local Vision" to "By focusing strictly on local talent, ${randomTeam.name} are building a unique bond with their community and a distinctive style."
                "DIASPORA_SCOUTING" -> "THE SEARCH BEYOND BORDERS: ${randomTeam.name}'s Global Network" to "${randomTeam.name}'s scouting network is leaving no stone unturned in their quest to bring diaspora talent back to the continent."
                "ACADEMY_TO_FIRST_TEAM" -> "THE ACADEMY BLUEPRINT: ${randomTeam.name}'s Youth Revolution" to "The pathway from the academy to the first team at ${randomTeam.name} is now the envy of the entire region."
                else -> return
            }

            newsRepository.createNewsArticle(
                headline = headline,
                content = content,
                category = "TRANSFER",
                relatedTeamId = randomTeam.id,
                relatedTeam = randomTeam.name,
                uniqueCheck = true
            )
        }
    }

    private suspend fun generateIdentityNarratives(week: Int) {
        if (Random.nextInt(100) < 10) {
            val teams = teamsRepository.getAllTeams().firstOrNull() ?: return
            val randomTeam = teams.randomOrNull() ?: return
            val dna = clubDNARepository.getClubDNA(randomTeam.id) ?: return

            if (dna.identityStrength < 40) {
                newsRepository.createNewsArticle(
                    headline = "IDENTITY CRISIS: ${randomTeam.name} Losing Their Way?",
                    content = "Pundits are questioning whether ${randomTeam.name} have abandoned the core philosophies that once defined the club.",
                    category = "CLUB",
                    relatedTeamId = randomTeam.id,
                    relatedTeam = randomTeam.name,
                    uniqueCheck = true
                )
            } else if (dna.identityStrength > 85) {
                newsRepository.createNewsArticle(
                    headline = "UNSHAKABLE IDENTITY: The ${randomTeam.name} Way",
                    content = "Success or failure, ${randomTeam.name} never waver from their principles. It's a commitment to a vision that fans have fully embraced.",
                    category = "CLUB",
                    relatedTeamId = randomTeam.id,
                    relatedTeam = randomTeam.name,
                    uniqueCheck = true
                )
            }
        }
    }

    private suspend fun generateDNABasedNarratives(week: Int, userTeamId: Int) {
        // Occasionally generate a story about the user's club DNA or a rival's DNA
        if (Random.nextInt(100) < 15) {
            val dna = clubDNARepository.getClubDNA(userTeamId)
            val team = teamsRepository.getTeamById(userTeamId)
            
            if (dna != null && team != null) {
                val (headline, content) = when (dna.playStyle) {
                    "DIRECT_PHYSICAL" -> "THE POWER GAME" to "${team.name}'s physical dominance is overwhelming opponents. They play it long, they play it hard, and it's working."
                    "FLAIR_EXPRESSIVE" -> "SAMBA IN AFRICA" to "Skill, trickery, and pure entertainment. ${team.name} are bringing flair back to the game, and the fans love every minute."
                    "DEFENSIVE" -> "THE IRON CURTAIN" to "Breaking down ${team.name} is becoming the hardest task in the league. Their defensive organization is a masterclass in discipline."
                    "POSSESSION" -> "THE ART OF CONTROL" to "${team.name}'s commitment to possession football is becoming the talk of the league. Manager's philosophy is clearly rubbing off on the squad."
                    "COUNTER" -> "LIVING ON THE EDGE" to "Opponents beware! ${team.name} have perfected the art of the counter-attack, turning defense into attack in seconds."
                    "GEGENPRESS" -> "THE FULL-THROTTLE ERA" to "Energy, intensity, and passion. ${team.name}'s high-pressing game is suffocating rivals and delighting fans."
                    "YOUTH_ENERGY" -> "THE KIDS ARE ALRIGHT" to "${team.name} continue to trust in youth. Their academy-first approach is setting a new standard for the region."
                    else -> "STABILITY AND VISION" to "${team.name} are building something special. Their clear identity and long-term planning are paying dividends on the pitch."
                }

                newsRepository.createNewsArticle(
                    headline = headline,
                    content = content,
                    category = "CLUB",
                    relatedTeamId = team.id,
                    relatedTeam = team.name,
                    isTopNews = false,
                    uniqueCheck = true
                )
            }
        }
    }

    private suspend fun generateTitleRaceNarrative(week: Int) {
        // Only start title race narratives after some games (e.g., week 10)
        if (week < 10) return
        
        val contexts = leagueContextRepository.getAllLeagueContexts().firstOrNull() ?: return
        
        contexts.forEach { context ->
            // Only generate news for a few leagues randomly each week to avoid clutter
            if (Random.nextInt(100) < 20) { 
                val titleRaceIds = try {
                    Json.decodeFromString<List<Int>>(context.titleRaceTeams)
                } catch (e: Exception) {
                    emptyList<Int>()
                }

                if (titleRaceIds.size >= 2) {
                    val team1 = teamsRepository.getTeamById(titleRaceIds[0])
                    val team2 = teamsRepository.getTeamById(titleRaceIds[1])
                    
                    if (team1 != null && team2 != null) {
                        newsRepository.createNewsArticle(
                            headline = "TITLE RACE HEATS UP: ${team1.name} vs ${team2.name}",
                            content = "The battle for the ${context.leagueName} title is reaching a boiling point. Experts predict a photo finish between ${team1.name} and ${team2.name}.",
                            category = "WORLD",
                            relatedTeamId = team1.id,
                            relatedTeam = team1.name,
                            isTopNews = true
                        )
                    }
                }
                
                context.surpriseTeamId?.let { surpriseId ->
                    val team = teamsRepository.getTeamById(surpriseId)
                    if (team != null) {
                        newsRepository.createNewsArticle(
                            headline = "THE SURPRISE PACKAGE: ${team.name} Defy Odds",
                            content = "${team.name} continue to shock the ${context.leagueName} as they maintain their incredible form despite their modest reputation.",
                            category = "WORLD",
                            relatedTeamId = team.id,
                            relatedTeam = team.name
                        )
                    }
                }
            }
        }
    }

    private suspend fun generateRelegationNarrative(week: Int) {
        if (week < 15) return // Too early for relegation talk
        
        val contexts = leagueContextRepository.getAllLeagueContexts().firstOrNull() ?: return
        
        contexts.forEach { context ->
            if (Random.nextInt(100) < 15) {
                val relegationIds = try {
                    Json.decodeFromString<List<Int>>(context.relegationBattleTeams)
                } catch (e: Exception) {
                    emptyList<Int>()
                }

                if (relegationIds.isNotEmpty()) {
                    val teamId = relegationIds.random()
                    val team = teamsRepository.getTeamById(teamId)
                    
                    if (team != null) {
                        newsRepository.createNewsArticle(
                            headline = "FIGHT FOR SURVIVAL: ${team.name} in Trouble",
                            content = "Tensions are high at ${team.name} as they find themselves embroiled in a fierce relegation battle. Every point counts now for the struggling side.",
                            category = "WORLD",
                            relatedTeamId = team.id,
                            relatedTeam = team.name
                        )
                    }
                }

                context.underperformingTeamId?.let { underId ->
                    val team = teamsRepository.getTeamById(underId)
                    if (team != null) {
                        newsRepository.createNewsArticle(
                            headline = "CRISIS POINT: ${team.name} Slump Continues",
                            content = "Questions are being asked about the management and squad depth at ${team.name} as their disappointing season shows no sign of improvement.",
                            category = "WORLD",
                            relatedTeamId = team.id,
                            relatedTeam = team.name,
                            isTopNews = true
                        )
                    }
                }
            }
        }
    }

    private suspend fun generatePlayerBreakthroughNarrative(week: Int) {
        val worldState = worldStateRepository.getWorldState().firstOrNull() ?: return
        
        val dominantIds = try {
            Json.decodeFromString<List<Int>>(worldState.dominantClubs)
        } catch (e: Exception) {
            emptyList<Int>()
        }

        if (dominantIds.isNotEmpty()) {
            val teamId = dominantIds.random()
            val team = teamsRepository.getTeamById(teamId)
            if (team != null) {
                val players = playersRepository.getPlayersByTeamId(team.id).firstOrNull() ?: emptyList()
                val risingStar = players.filter { it.age < 21 && it.rating > 70 }.randomOrNull()
                
                if (risingStar != null) {
                    newsRepository.createNewsArticle(
                        headline = "GEN-Z PHENOMENON: ${risingStar.name} Taking League by Storm",
                        content = "At just ${risingStar.age} years old, ${risingStar.name} is already being hailed as the future of African football. His recent performances at ${team.name} have scouts from across the globe paying attention.",
                        category = "WORLD",
                        relatedPlayerId = risingStar.id,
                        relatedPlayer = risingStar.name,
                        relatedTeamId = team.id,
                        relatedTeam = team.name,
                        isTopNews = true
                    )
                }
            }
        }
    }
}
