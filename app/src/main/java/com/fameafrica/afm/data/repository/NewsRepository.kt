package com.fameafrica.afm.data.repository

import com.fameafrica.afm.data.database.dao.NewsCategoryDistribution
import com.fameafrica.afm.data.database.dao.NewsDao
import com.fameafrica.afm.data.database.entities.NewsEntity
import com.fameafrica.afm.data.database.entities.JournalistsEntity
import com.fameafrica.afm.domain.manager.MatchSimulationEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class NewsRepository @Inject constructor(
    private val newsDaoProvider: Provider<NewsDao>,
    private val journalistsRepository: JournalistsRepository
) {
    private val newsDao get() = newsDaoProvider.get()

    private val _newsEvents = kotlinx.coroutines.flow.MutableSharedFlow<NewsEntity>(extraBufferCapacity = 16)
    val newsEvents: Flow<NewsEntity> = _newsEvents

    // ============ BASIC CRUD ============

    fun getAllNews(): Flow<List<NewsEntity>> = newsDao.getAll()

    suspend fun getNewsById(id: Int): NewsEntity? = newsDao.getById(id)

    suspend fun insertNews(news: NewsEntity) {
        newsDao.insert(news)
        newsDao.keepOnlyLatest(100) // Phase 4: Enforce 100-item limit
        _newsEvents.emit(news)
    }

    suspend fun updateNews(news: NewsEntity) = newsDao.update(news)

    suspend fun deleteNews(news: NewsEntity) = newsDao.delete(news)

    // ============ CATEGORY-BASED ============

    fun getNewsByCategory(category: String): Flow<List<NewsEntity>> =
        newsDao.getByCategory(category)

    fun getTransferNews(): Flow<List<NewsEntity>> =
        newsDao.getByCategory("TRANSFER")

    fun getMatchNews(): Flow<List<NewsEntity>> =
        newsDao.getByCategory("MATCH")

    fun getInterviewNews(): Flow<List<NewsEntity>> =
        newsDao.getByCategory("INTERVIEW")

    // ============ TOP NEWS ============

    fun getTopNews(limit: Int = 5): Flow<List<NewsEntity>> =
        newsDao.getTopNews(limit)

    // ============ RELATED ENTITIES ============

    fun getNewsByTeam(teamId: Int): Flow<List<NewsEntity>> =
        newsDao.getNewsByTeam(teamId)

    fun getNewsByPlayer(playerId: Int): Flow<List<NewsEntity>> =
        newsDao.getNewsByPlayer(playerId)

    fun getNewsByManager(managerId: Int): Flow<List<NewsEntity>> =
        newsDao.getNewsByManager(managerId)

    // ============ NEWS CREATION ============

    suspend fun createNewsArticle(
        headline: String,
        content: String,
        category: String,
        journalistName: String? = null,
        journalistLogo: String? = null,
        relatedTeamId: Int? = null,
        relatedTeam: String? = null,
        relatedPlayerId: Int? = null,
        relatedPlayer: String? = null,
        relatedManagerId: Int? = null,
        relatedManager: String? = null,
        imageUrl: String? = null,
        isTopNews: Boolean = false,
        uniqueCheck: Boolean = false
    ): NewsEntity {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val timestamp = dateFormat.format(Date())

        if (uniqueCheck) {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -7)
            val since = dateFormat.format(calendar.time)
            if (newsDao.newsExists(headline, since)) {
                return NewsEntity(
                    headline = headline,
                    content = content,
                    category = category,
                    timestamp = timestamp,
                    journalistName = null,
                    journalistLogo = null
                )
            }
        }

        val news = NewsEntity(
            headline = headline,
            content = content,
            category = category,
            journalistName = journalistName,
            journalistLogo = journalistLogo,
            timestamp = timestamp,
            isTopNews = if (isTopNews) 1 else 0,
            relatedTeamId = relatedTeamId,
            relatedTeam = relatedTeam,
            relatedPlayerId = relatedPlayerId,
            relatedPlayer = relatedPlayer,
            relatedManagerId = relatedManagerId,
            relatedManager = relatedManager,
            imageUrl = imageUrl
        )

        insertNews(news)
        return news
    }

    suspend fun createTransferRumor(
        playerName: String,
        playerId: Int? = null,
        fromTeam: String,
        fromTeamId: Int? = null,
        toTeam: String,
        toTeamId: Int? = null,
        fee: Int? = null,
        playerRating: Int = 70,
        journalist: JournalistsEntity? = null
    ): NewsEntity {
        val activeJournalist = journalist ?: journalistsRepository.getRandomJournalistByExpertise("Transfer News")
        
        val headline = when (activeJournalist?.personality) {
            "Sensationalist" -> "MEGA DEAL! $playerName to $toTeam? Insiders say it's happening!"
            "Analyst" -> "TRANSFER SPOTLIGHT: Why $toTeam are targeting $playerName"
            "Hostile" -> "DESPERATE: $toTeam turn to $playerName as options dry up"
            else -> if (fee != null && fee >= 1_000_000) {
                "EXCLUSIVE: $playerName set for $toTeam move in ${fee / 1_000_000}M deal"
            } else {
                "RUMOR: $playerName linked with move to $toTeam"
            }
        }

        val content = buildString {
            when (activeJournalist?.personality) {
                "Sensationalist" -> {
                    appendLine("BOMBSHELL! $playerName is reportedly packing his bags for a move to $toTeam.")
                    appendLine("Our sources say the player is 'extremely excited' about the project.")
                }
                "Analyst" -> {
                    appendLine("From a tactical perspective, $playerName fits the $toTeam system perfectly.")
                    appendLine("His ability to progress the ball would solve a major weakness in their current squad.")
                }
                else -> {
                    appendLine("Sources close to the player indicate that $playerName could be on his way to $toTeam.")
                }
            }
            
            if (fee != null && fee > 0) {
                val formattedFee = if (fee >= 1_000_000) "${fee / 1_000_000}M" else "${fee / 1_000}K"
                appendLine("The deal is reportedly worth in the region of $formattedFee.")
            }
            appendLine("${fromTeam} officials have remained tight-lipped, but insiders suggest they may be willing to negotiate if the valuation is met.")
        }

        // Context-aware Image Selection
        val image = when {
            playerRating >= 85 -> "player_superstar"
            playerRating >= 75 -> "player_dribbling"
            else -> "player"
        }

        return createNewsArticle(
            headline = headline,
            content = content,
            category = "TRANSFER",
            journalistName = activeJournalist?.name ?: "African Football Insider",
            journalistLogo = activeJournalist?.logo,
            relatedPlayerId = playerId,
            relatedPlayer = playerName,
            relatedTeamId = fromTeamId,
            relatedTeam = fromTeam,
            imageUrl = image,
            isTopNews = (fee != null && fee >= 10_000_000) || activeJournalist?.personality == "Sensationalist"
        )
    }

    suspend fun createMatchReport(
        matchResult: MatchSimulationEngine.MatchResult,
        isUpset: Boolean = false,
        journalist: JournalistsEntity? = null
    ): NewsEntity {
        val res = matchResult
        val homeTeam = res.homeTeamObj.name
        val awayTeam = res.awayTeamObj.name
        val homeScore = res.homeScore
        val awayScore = res.awayScore
        
        val activeJournalist = journalist ?: journalistsRepository.getRandomJournalistByExpertise("Match Reporting")
        
        val winner = if (homeScore > awayScore) homeTeam else awayTeam
        val winnerId = if (homeScore > awayScore) res.fixture.homeTeamId else res.fixture.awayTeamId
        val loser = if (homeScore > awayScore) awayTeam else homeTeam
        val isDraw = homeScore == awayScore
        
        // Systemic Event Detection
        val lastMinuteGoal = res.events.find { it.isGoal && it.minute >= 88 }
        val hatTrickPlayer = res.events.filter { it.isGoal }.groupBy { it.playerId }.filter { it.value.size >= 3 }.keys.firstOrNull()
        val hatTrickPlayerName = if (hatTrickPlayer != null) res.events.find { it.playerId == hatTrickPlayer }?.playerName else null
        val heavyDefeat = (homeScore - awayScore).let { if (it < 0) -it else it } >= 4

        // Journalist Personality Impact on Headline
        val headline = when (activeJournalist?.personality) {
            "Sensationalist" -> when {
                isUpset -> "TOTAL CHAOS! $winner DESTROY the odds in $loser DISASTER!"
                heavyDefeat -> "ANNIHILATION! $winner leave $loser in ruins after $homeScore-$awayScore bloodbath!"
                lastMinuteGoal != null -> "LIMBS! ${lastMinuteGoal.playerName} steals it at the death for $winner!"
                else -> "UNBELIEVABLE! You won't believe what happened in $homeTeam vs $awayTeam!"
            }
            "Analyst" -> when {
                isDraw -> "TACTICAL STALEMATE: How $homeTeam and $awayTeam canceled each other out"
                heavyDefeat -> "SYSTEM FAILURE: Breaking down $winner's $homeScore-$awayScore tactical masterclass"
                else -> "POST-MATCH ANALYSIS: $winner's structure proves too much for $loser"
            }
            "Hostile" -> when {
                homeScore > awayScore -> "TRAGIC: $awayTeam collapse under pressure at $homeTeam"
                awayScore > homeScore -> "SHAMEFUL: $homeTeam fans let down by gutless performance against $awayTeam"
                else -> "BOOED OFF: Both sets of fans demand more after $homeScore-$awayScore bore draw"
            }
            else -> when { // Neutral / Friendly / Default
                isUpset -> "SHOCK: $winner stun $loser in $homeScore-$awayScore thriller!"
                hatTrickPlayerName != null -> "HAT-TRICK HERO: $hatTrickPlayerName inspires $winner to victory"
                isDraw && homeScore >= 2 -> "GOALFEST: $homeTeam and $awayTeam share points in $homeScore-$awayScore classic"
                isDraw -> "STALEMATE: $homeTeam and $awayTeam play out $homeScore-$awayScore draw"
                heavyDefeat -> "DOMINANCE: $winner crush $loser $homeScore-$awayScore"
                else -> "$winner edge out $loser in tight $homeScore-$awayScore contest"
            }
        }

        val content = buildString {
            // Intro
            append("${activeJournalist?.name ?: "FAME Sports"} reports from ${res.fixture.stadium ?: "stadium"}: ")
            appendLine("$homeTeam hosted $awayTeam in what proved to be a ${if (isUpset) "monumental upset" else "significant encounter"}.")
            
            // Core Narrative
            if (hatTrickPlayerName != null) {
                appendLine("The story of the day was undoubtedly $hatTrickPlayerName, who netted a brilliant hat-trick to dismantle the $loser defense.")
            } else if (lastMinuteGoal != null) {
                appendLine("Drama erupted in the ${lastMinuteGoal.displayMinute}th minute when ${lastMinuteGoal.playerName} found the back of the net, sparking wild celebrations for $winner.")
            }
            
            if (heavyDefeat) {
                appendLine("It was a long afternoon for $loser as they were systematically torn apart by a rampant $winner side.")
            }
            
            if (isDraw) {
                appendLine("Both managers will be reflecting on missed opportunities as the points were shared in a $homeScore-$awayScore result.")
            } else {
                appendLine("$winner secured a deserved $homeScore-$awayScore victory, moving them into a strong position in the ${res.fixture.league} standings.")
            }

            // Journalist specific closing
            when(activeJournalist?.personality) {
                "Analyst" -> appendLine("Stats show $winner dominated the transitions, finishing with ${res.homeStats.shotsOnTarget + res.awayStats.shotsOnTarget} total shots on target.")
                "Sensationalist" -> appendLine("Questions will be asked! Heads might roll after this explosive result!")
                "Hostile" -> appendLine("A pathetic showing that will leave $loser supporters fuming for weeks.")
                "Friendly" -> appendLine("A great day for football and a testament to the spirit of $winner.")
                else -> appendLine("Fans left the stadium discussing what this means for the title race.")
            }
        }

        return createNewsArticle(
            headline = headline,
            content = content,
            category = "MATCH",
            journalistName = activeJournalist?.name,
            journalistLogo = activeJournalist?.logo,
            relatedTeamId = winnerId,
            relatedTeam = winner,
            imageUrl = "team_logo:$winner",
            isTopNews = isUpset || heavyDefeat || hatTrickPlayerName != null || lastMinuteGoal != null
        )
    }

    suspend fun createManagerAppointmentNews(
        managerName: String,
        managerId: Int,
        teamName: String,
        teamId: Int,
        nationality: String,
        reputation: String,
        endYear: Int
    ): NewsEntity {
        val headline = "NEW ERA: $managerName takes charge at $teamName"
        val content = "$teamName have officially unveiled $managerName as their new manager. The $nationality tactician has signed a contract until $endYear. \"We are delighted to welcome $managerName to the club,\" said the club chairman."
        
        return createNewsArticle(
            headline = headline,
            content = content,
            category = "MANAGER",
            relatedTeamId = teamId,
            relatedTeam = teamName,
            relatedManagerId = managerId,
            relatedManager = managerName,
            imageUrl = "team_logo:$teamName", // Manager joined this team
            isTopNews = true
        )
    }

    suspend fun createInjuryNews(
        playerName: String,
        playerId: Int,
        teamName: String,
        teamId: Int,
        injuryType: String,
        durationDays: Int
    ): NewsEntity {
        val timeStr = when {
            durationDays >= 30 -> "${durationDays / 30} months"
            durationDays >= 7 -> "${durationDays / 7} weeks"
            else -> "$durationDays days"
        }
        
        val headline = "INJURY BLOW: $teamName star $playerName sidelined"
        val content = "The medical department at $teamName has confirmed that $playerName is set for a spell on the sidelines with a $injuryType. Expected recovery: $timeStr."
        
        return createNewsArticle(
            headline = headline,
            content = content,
            category = "INJURY",
            relatedTeamId = teamId,
            relatedTeam = teamName,
            relatedPlayerId = playerId,
            relatedPlayer = playerName,
            imageUrl = "player_injury"
        )
    }

    suspend fun createTitleWinningNews(
        teamName: String,
        teamId: Int,
        competitionName: String,
        isCup: Boolean = false
    ): NewsEntity {
        val headline = "CHAMPIONS! $teamName crown themselves $competitionName winners"
        val content = "Scenes of pure jubilation as $teamName have mathematically secured the $competitionName title. After a grueling season, they have proven themselves to be the best."
        
        val imagePrefix = if (isCup) "cup_logo" else "league_logo"

        return createNewsArticle(
            headline = headline,
            content = content,
            category = "WORLD",
            relatedTeamId = teamId,
            relatedTeam = teamName,
            imageUrl = "$imagePrefix:$competitionName",
            isTopNews = true,
            uniqueCheck = true
        )
    }

    suspend fun createSackingNews(
        managerName: String,
        managerId: Int,
        teamName: String,
        teamId: Int,
        reason: String = "poor results"
    ): NewsEntity {
        val headline = "SACKED: $teamName part ways with $managerName"
        val content = "Breaking news from $teamName as the club has confirmed the dismissal of manager $managerName with immediate effect. Following a period of $reason, the board felt a change was necessary."
        
        return createNewsArticle(
            headline = headline,
            content = content,
            category = "MANAGER",
            relatedTeamId = teamId,
            relatedTeam = teamName,
            relatedManagerId = managerId,
            relatedManager = managerName,
            imageUrl = "team_logo:$teamName", // Club focused news
            isTopNews = true
        )
    }

    suspend fun createTransferConfirmedNews(
        playerName: String,
        playerId: Int,
        targetTeam: String,
        targetTeamId: Int,
        fromTeam: String,
        fromTeamId: Int,
        fee: Long,
        playerRating: Int = 70,
        isLate: Boolean = false
    ): NewsEntity {
        val headline = when {
            isLate -> "DEADLINE DAY: $playerName joins $targetTeam!"
            playerRating >= 85 -> "BLOCKBUSTER: Superstar $playerName signs for $targetTeam"
            else -> "CONFIRMED: $playerName completes $targetTeam move"
        }
        
        val image = when {
            playerRating >= 85 -> "player_superstar"
            playerRating >= 75 -> "player_dribbling"
            else -> "player"
        }

        return createNewsArticle(
            headline = headline,
            content = "$playerName has completed his move from $fromTeam to $targetTeam for a fee of ${if (fee >= 1_000_000) "${fee/1_000_000}M" else "${fee/1_000}K"}.",
            category = "TRANSFER",
            relatedTeamId = targetTeamId,
            relatedTeam = targetTeam,
            relatedPlayerId = playerId,
            relatedPlayer = playerName,
            imageUrl = image,
            isTopNews = playerRating >= 80 || isLate
        )
    }

    suspend fun createNationalTeamNews(
        headline: String,
        content: String,
        nationality: String
    ): NewsEntity {
        return createNewsArticle(
            headline = headline,
            content = content,
            category = "WORLD",
            imageUrl = "country_flag:$nationality",
            isTopNews = true
        )
    }

    suspend fun createBoardDecisionNews(
        teamName: String,
        teamId: Int,
        decisionType: String,
        details: String,
        isPositive: Boolean
    ): NewsEntity {
        val headline = when(decisionType) {
            "BUDGET_INCREASE" -> "INVESTMENT: $teamName board boost transfer kitty"
            "BUDGET_CUT" -> "AUSTERITY: $teamName board announce budget cuts"
            "FACILITY_UPGRADE" -> "PROGRESS: $teamName approve infrastructure expansion"
            "VISION_CHANGE" -> "NEW DIRECTION: $teamName board update club philosophy"
            else -> "BOARDROOM UPDATE: Changes announced at $teamName"
        }

        return createNewsArticle(
            headline = headline,
            content = "The board at $teamName has officially announced that $details. The decision is seen as ${if (isPositive) "a major step forward" else "a challenging moment"} for the club's long-term strategy.",
            category = "BOARD",
            relatedTeamId = teamId,
            relatedTeam = teamName,
            imageUrl = "boardroom",
            isTopNews = !isPositive || decisionType == "BUDGET_INCREASE"
        )
    }

    suspend fun createFanReactionNews(
        teamName: String,
        teamId: Int,
        reactionType: String,
        context: String
    ): NewsEntity {
        val headline = when(reactionType) {
            "PROTEST" -> "OUTRAGE: $teamName fans protest outside stadium"
            "CELEBRATION" -> "EUPHORIA: $teamName supporters dream big after $context"
            "DEBATE" -> "DIVIDED: $teamName fanbase split over $context"
            else -> "FAN FEEDBACK: Supporters voice opinions on $teamName"
        }

        return createNewsArticle(
            headline = headline,
            content = "Social media has been abuzz with reactions from $teamName supporters. Following $context, the general sentiment is one of ${reactionType.lowercase()}. \"We deserve better,\" said one fan on a local radio show.",
            category = "FANS",
            relatedTeamId = teamId,
            relatedTeam = teamName,
            imageUrl = "fans_group",
            isTopNews = reactionType == "PROTEST"
        )
    }

    suspend fun createInfrastructureCompletionNews(
        teamName: String,
        teamId: Int,
        upgradeType: String,
        level: Int
    ): NewsEntity {
        val headline = "COMPLETED: $teamName unveil new $upgradeType facilities"
        val content = "A ribbon-cutting ceremony was held at $teamName as they officially opened their state-of-the-art Level $level $upgradeType centre. The investment is expected to significantly boost the club's ${if (upgradeType.contains("Youth")) "talent pipeline" else "performance levels"}."

        return createNewsArticle(
            headline = headline,
            content = content,
            category = "INFRASTRUCTURE",
            relatedTeamId = teamId,
            relatedTeam = teamName,
            imageUrl = "facility_upgrade",
            isTopNews = level >= 4
        )
    }

    // ============ STATISTICS ============

    fun getNewsCategoryDistribution(): Flow<List<NewsCategoryDistribution>> =
        newsDao.getNewsCategoryDistribution()

    // ============ CLEANUP ============

    suspend fun deleteOldNews(daysToKeep: Int = 30) {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -daysToKeep)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cutoffDate = dateFormat.format(calendar.time)
        newsDao.deleteOldNews(cutoffDate)
    }

    // ============ DASHBOARD ============

    suspend fun getNewsDashboard(): NewsDashboard {
        val allNews = newsDao.getAll().firstOrNull() ?: emptyList()
        val topNews = newsDao.getTopNews(5).firstOrNull() ?: emptyList()
        val transferNews = allNews.filter { it.category == "TRANSFER" }
        val matchNews = allNews.filter { it.category == "MATCH" }
        val interviewNews = allNews.filter { it.category == "INTERVIEW" }

        return NewsDashboard(
            totalArticles = allNews.size,
            topNews = topNews,
            transferNews = transferNews.take(5),
            matchNews = matchNews.take(5),
            interviewNews = interviewNews.take(5),
            categoryDistribution = getNewsCategoryDistribution().firstOrNull() ?: emptyList()
        )
    }
}

// ============ DATA CLASSES ============

data class NewsDashboard(
    val totalArticles: Int,
    val topNews: List<NewsEntity>,
    val transferNews: List<NewsEntity>,
    val matchNews: List<NewsEntity>,
    val interviewNews: List<NewsEntity>,
    val categoryDistribution: List<NewsCategoryDistribution>
)
