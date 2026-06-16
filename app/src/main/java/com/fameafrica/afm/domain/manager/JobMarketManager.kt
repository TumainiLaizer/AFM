package com.fameafrica.afm.domain.manager

import android.util.Log
import com.fameafrica.afm.data.database.entities.*
import com.fameafrica.afm.data.repository.*
import com.fameafrica.afm.domain.model.SimulationEvent
import com.fameafrica.afm.utils.GameDateManager
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * Handles the African Job Market logic: sackings, applications, and offers.
 * Inspired by FCM26's dynamic world.
 * This manager is responsible for making the career feel alive.
 */
@Singleton
class JobMarketManager @Inject constructor(
    private val managersRepository: ManagersRepository,
    private val teamsRepository: TeamsRepository,
    private val managerOffersRepository: ManagerOffersRepository,
    private val leaguesRepository: LeaguesRepository,
    private val newsRepository: NewsRepository,
    private val notificationsRepository: NotificationsRepository,
    private val gameDateManager: GameDateManager,
) {

    /**
     * Called daily by WorldSimulationEngine to process vacancies and job security.
     */
    suspend fun processDailyJobMarket(currentWeek: Int, userManagerId: Int): List<SimulationEvent> {
        val events = mutableListOf<SimulationEvent>()
        
        // 1. Check AI Managers job security and sack them if needed
        val sackings = processManagerSackings(userManagerId)
        events.addAll(sackings)
        
        // 2. Process Vacancies (Filling Job Centre)
        processVacancies()

        // 3. Handle AI Hiring (Filling vacancies)
        val hiringNews = processAIHiring()
        events.addAll(hiringNews)

        // 4. Generate random offers for the User Manager if they are doing well
        val userOffers = generateUserManagerOffers(userManagerId)
        events.addAll(userOffers)
        
        // 5. Process existing applications (interviews/offers/rejections)
        val applicationUpdates = processApplications(userManagerId, currentWeek)
        events.addAll(applicationUpdates)

        return events
    }

    private suspend fun processVacancies() {
        val teamsWithoutManagers = teamsRepository.getTeamsWithoutManager().firstOrNull() ?: return
        
        for (team in teamsWithoutManagers) {
            val existing = managerOffersRepository.getAllOffers().firstOrNull()?.find { 
                it.offeringTeamId == team.id && it.isVacancy && it.status == "open"
            }
            
            if (existing == null) {
                val league = team.league.let { leaguesRepository.getLeagueByName(it) }
                val vacancy = ManagerOffersEntity(
                    managerId = -1, 
                    managerName = "N/A",
                    offeringTeam = team.name,
                    offeringTeamId = team.id,
                    leagueName = team.league,
                    leagueLevel = league?.level ?: 3,
                    offeredSalary = 0,
                    contractYears = 0,
                    offerType = "HEAD_COACH",
                    offerDate = System.currentTimeMillis(),
                    expiryDate = System.currentTimeMillis() + (14 * 24 * 60 * 60 * 1000L), // Vacancy open for 2 weeks
                    isVacancy = true,
                    status = "open",
                    message = "Head Coach vacancy at ${team.name}"
                )
                managerOffersRepository.insertOffer(vacancy)
            }
        }
    }

    private suspend fun processAIHiring(): List<SimulationEvent> {
        val events = mutableListOf<SimulationEvent>()
        val teamsWithoutManagers = teamsRepository.getTeamsWithoutManager().firstOrNull()?.shuffled()?.take(3) ?: return emptyList()
        
        val availableManagers = managersRepository.getAvailableManagers().firstOrNull()?.toMutableList() ?: return emptyList()
        if (availableManagers.isEmpty()) return emptyList()

        for (team in teamsWithoutManagers) {
            val candidateIndex = availableManagers.indexOfFirst { 
                it.reputation in (team.reputation - 20)..(team.reputation + 30)
            }
            
            val candidate = if (candidateIndex != -1) {
                availableManagers.removeAt(candidateIndex)
            } else {
                if (availableManagers.isNotEmpty()) availableManagers.removeAt(0) else null
            }

            if (candidate != null) {
                managersRepository.signContract(
                    managerId = candidate.id,
                    teamId = team.id,
                    salary = (team.revenue / 1000).toInt().coerceIn(2000, 50000),
                    contractYears = Random.nextInt(1, 4)
                )
                
                teamsRepository.assignManager(team.id, candidate.id)
                
                val news = newsRepository.createNewsArticle(
                    headline = "${team.name} appoint ${candidate.name}",
                    content = "${team.name} have announced the appointment of ${candidate.name} as their new Head Coach.",
                    category = "MANAGER",
                    relatedTeamId = team.id,
                    relatedTeam = team.name,
                    relatedManagerId = candidate.id,
                    relatedManager = candidate.name
                )
                
                events.add(SimulationEvent.NewsHeadline(news))
            }
        }
        return events
    }

    private suspend fun processManagerSackings(userManagerId: Int): List<SimulationEvent> {
        val events = mutableListOf<SimulationEvent>()
        val allTeams = teamsRepository.getAllTeamsSync().filter { it.managerId != null }
        
        for (team in allTeams) {
            // User sacking is handled by board evaluation system in monthly review
            if (team.managerId == userManagerId) continue

            // AI Sacking logic: Board confidence critically low
            // In Africa, patience can be short for underperforming big clubs
            val sackThreshold = if (team.reputation > 70) 35 else 25
            
            if (team.boardConfidence < sackThreshold && Random.nextInt(100) < 15) {
                val manager = managersRepository.getManagerById(team.managerId!!) ?: continue
                
                Log.d("AFM_JOB", "SACKED: ${team.name} has fired ${manager.name}")
                
                managersRepository.leaveClub(manager.id)
                teamsRepository.updateTeam(team.copy(managerId = null, boardConfidence = 50))
                
                val news = newsRepository.createSackingNews(
                    managerName = manager.name,
                    managerId = manager.id,
                    teamName = team.name,
                    teamId = team.id
                )
                
                events.add(SimulationEvent.NewsHeadline(
                    news = news,
                    importance = 2,
                    shouldStop = false
                ))
            }
        }
        return events
    }

    private suspend fun generateUserManagerOffers(managerId: Int): List<SimulationEvent> {
        val events = mutableListOf<SimulationEvent>()
        
        // Chance of receiving an unsolicited offer if reputation is high or performance is great
        val manager = managersRepository.getManagerById(managerId) ?: return events
        
        // Only if unemployed or very successful
        if (manager.isEmployed && manager.performanceRating < 75) return events

        val baseChance = when (manager.reputationLevel) {
            "Local" -> 1
            "Respected" -> 3
            "Continental" -> 6
            "World Class" -> 12
            "Legendary" -> 20
            else -> 1
        }
        
        if (Random.nextInt(1000) < baseChance * 5) { // Adjusted for daily tick
            val offer = managerOffersRepository.generateRandomOfferIfEligible(managerId)
            offer?.let {
                events.add(SimulationEvent.JobOffer(
                    teamName = it.offeringTeam,
                    role = it.offerType
                ))
            }
        }
        
        return events
    }

    private suspend fun processApplications(managerId: Int, week: Int): List<SimulationEvent> {
        val events = mutableListOf<SimulationEvent>()
        // Filter for applications (isApplication = true)
        val applications = managerOffersRepository.getAllOffers().firstOrNull()?.filter { 
            it.managerId == managerId && it.isApplication && it.status == "pending" 
        } ?: return events
        
        for (app in applications) {
            val manager = managersRepository.getManagerById(managerId) ?: continue
            val team = teamsRepository.getTeamById(app.offeringTeamId) ?: continue
            
            // App evaluation logic
            val repDiff = manager.reputation - team.reputation
            val successChance = 40 + repDiff // Baseline 40% chance if rep matches
            
            val roll = Random.nextInt(100)
            when {
                roll < successChance / 3 -> {
                    // Application successful -> Contract Offer
                    val salary = (team.revenue / 1200).toInt().coerceIn(100000, 5000000)
                    val updatedApp = app.copy(
                        status = "pending",
                        isApplication = false,
                        offeredSalary = salary,
                        contractYears = Random.nextInt(2, 5),
                        message = "We are pleased with your application. Here is our contract offer."
                    )
                    managerOffersRepository.updateOffer(updatedApp)
                    
                    notificationsRepository.insertNotification(
                        NotificationsEntity.createBoardMessageNotification(
                            title = "Contract Offer: ${team.name}",
                            message = "Your application to ${team.name} was successful. They have sent a contract offer.",
                            priority = 3,
                            userId = managerId
                        )
                    )
                    
                    newsRepository.createNewsArticle(
                        headline = "Job Offer: ${team.name}",
                        content = "Good news! ${team.name} have responded to your application with a contract offer.",
                        category = "MANAGER",
                        relatedTeamId = team.id,
                        relatedTeam = team.name,
                        relatedManagerId = managerId,
                        relatedManager = manager.name
                    )

                    events.add(SimulationEvent.ContractOffer(
                        playerName = manager.name,
                        offerDetails = "Contract offer from ${team.name}"
                    ))
                }
                roll < successChance -> {
                    // Application leading to Interview
                    val updatedApp = app.copy(status = "interview")
                    managerOffersRepository.updateOffer(updatedApp)
                    
                    notificationsRepository.insertNotification(
                        NotificationsEntity.createBoardMessageNotification(
                            title = "Interview: ${team.name}",
                            message = "${team.name} have invited you to an interview for the manager position.",
                            priority = 2,
                            userId = managerId
                        )
                    )
                    
                    newsRepository.createNewsArticle(
                        headline = "Interview: ${team.name}",
                        content = "${team.name} have invited ${manager.name} to an interview for the vacant manager position.",
                        category = "MANAGER",
                        relatedTeamId = team.id,
                        relatedTeam = team.name,
                        relatedManagerId = managerId,
                        relatedManager = manager.name
                    )

                    events.add(SimulationEvent.Interview(
                        teamName = team.name
                    ))
                }
                roll > 85 -> {
                    // Rejected
                    managerOffersRepository.updateOffer(app.copy(status = "rejected"))
                    
                    notificationsRepository.insertNotification(
                        NotificationsEntity.createBoardMessageNotification(
                            title = "Application Rejected",
                            message = "Unfortunately, ${team.name} have rejected your application.",
                            priority = 1,
                            userId = managerId
                        )
                    )
                }
            }
        }
        
        return events
    }

    /**
     * User applies for a job.
     */
    suspend fun applyForJob(managerId: Int, teamId: Int, currentWeek: Int): Boolean {
        val manager = managersRepository.getManagerById(managerId) ?: return false
        val team = teamsRepository.getTeamById(teamId) ?: return false
        
        // Prevent multiple applications to same club
        val existing = managerOffersRepository.getAllOffers().firstOrNull()?.find { 
            it.managerId == managerId && it.offeringTeamId == teamId && (it.status == "pending" || it.status == "interview")
        }
        if (existing != null) return false

        val league = team.league.let { leaguesRepository.getLeagueByName(it) }
        
        val offer = ManagerOffersEntity(
            managerId = managerId,
            managerName = manager.name,
            offeringTeam = team.name,
            offeringTeamId = team.id,
            leagueName = team.league,
            leagueLevel = league?.level ?: 3,
            offeredSalary = 0,
            contractYears = 0,
            offerType = "HEAD_COACH",
            offerDate = gameDateManager.getGameDate(currentWeek).time,
            expiryDate = gameDateManager.getGameDate(currentWeek + 2).time,
            isApplication = true,
            status = "pending",
            message = "I am applying for the Head Coach vacancy at ${team.name}."
        )
        
        managerOffersRepository.insertOffer(offer)
        
        // Generate AI applicants for competition
        generateAIApplicantsForVacancy(teamId, managerId)
        
        return true
    }

    private suspend fun generateAIApplicantsForVacancy(teamId: Int, userManagerId: Int) {
        val team = teamsRepository.getTeamById(teamId) ?: return
        val availableManagers = managersRepository.getAvailableManagers().firstOrNull() ?: return
        
        // Pick 2-4 AI applicants with matching reputation
        val aiApplicants = availableManagers
            .filter { it.id != userManagerId && it.reputation in (team.reputation - 30)..(team.reputation + 20) }
            .shuffled()
            .take(Random.nextInt(2, 5))
            
        for (ai in aiApplicants) {
            val league = team.league.let { leaguesRepository.getLeagueByName(it) }
            val offer = ManagerOffersEntity(
                managerId = ai.id,
                managerName = ai.name,
                offeringTeam = team.name,
                offeringTeamId = team.id,
                leagueName = team.league,
                leagueLevel = league?.level ?: 3,
                offeredSalary = 0,
                contractYears = 0,
                offerType = "HEAD_COACH",
                offerDate = System.currentTimeMillis(),
                expiryDate = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000L),
                isApplication = true,
                status = "pending",
                message = "AI application"
            )
            managerOffersRepository.insertOffer(offer)
        }
    }

    /**
     * User resigns from their current club.
     */
    suspend fun resign(managerId: Int, teamId: Int) {
        val manager = managersRepository.getManagerById(managerId) ?: return
        if (manager.teamId != teamId) return

        managersRepository.leaveClub(managerId)
        teamsRepository.updateTeam(teamsRepository.getTeamById(teamId)!!.copy(managerId = null, boardConfidence = 50))

        newsRepository.createNewsArticle(
            headline = "${manager.name} Resigns!",
            content = "${manager.name} has stepped down as manager of ${teamsRepository.getTeamById(teamId)?.name}.",
            category = "WORLD",
            relatedTeamId = teamId
        )
    }
}
