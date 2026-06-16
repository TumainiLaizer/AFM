package com.fameafrica.afm.domain.manager

import com.fameafrica.afm.data.database.entities.NewsEntity
import com.fameafrica.afm.data.repository.LeaguesRepository
import com.fameafrica.afm.data.repository.NewsRepository
import com.fameafrica.afm.utils.GameDateManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SeasonPreviewNewsGenerator @Inject constructor(
    private val predictorEngine: SeasonPredictorEngine,
    private val leaguesRepository: LeaguesRepository,
    private val newsRepository: NewsRepository,
    private val gameDateManager: GameDateManager
) {

    suspend fun generateSeasonPreviews(week: Int) {
        val leagues = leaguesRepository.getAllLeaguesSync()
        val date = gameDateManager.formatGameDateForDb(week)

        for (league in leagues) {
            val prediction = predictorEngine.generateLeaguePrediction(league)
            
            if (prediction.predictedStandings.isEmpty()) continue

            val content = buildString {
                append("AFM Sports Special: ${league.name} Season Preview\n\n")
                append("As the new season approaches, our data analysts have crunched the numbers using the latest performance metrics and squad depths.\n\n")
                
                val champion = prediction.predictedStandings[0]
                append("### THE PREDICTED CHAMPIONS: ${champion.teamName}\n")
                append("With a strength rating of ${"%.1f".format(champion.strength)}, they are the favorites to lift the trophy.\n\n")

                append("### CONTINENTAL QUALIFICATION RACE\n")
                prediction.predictedStandings.filter { it.qualificationStatus != null }.forEach { p ->
                    append("- ${p.teamName}: Predicted ${p.predictedPosition}th (${p.qualificationStatus})\n")
                }
                
                append("\n### FULL PREDICTED TABLE\n")
                prediction.predictedStandings.forEach { p ->
                    append("${p.predictedPosition}. ${p.teamName} - ${p.predictedPoints} pts\n")
                }
                
                append("\n### KEY PLAYERS TO WATCH\n")
                prediction.predictedStandings.take(5).forEach { p ->
                    append("- ${p.teamName}: ${p.keyPlayerName ?: "Rising Star"}\n")
                }
            }

            val news = NewsEntity(
                headline = "${league.name}: Official Season Prediction Magazine",
                content = content,
                timestamp = date,
                category = "MAGAZINE",
                journalistName = "AFM Sports AI",
                journalistLogo = "assets/journalists/ai_logo.png",
                imageUrl = "assets/news/season_preview.webp"
            )
            newsRepository.insertNews(news)
        }
    }
}
