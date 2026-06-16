package com.fameafrica.afm.domain.model

import com.fameafrica.afm.data.database.entities.FixturesResultsEntity
import com.fameafrica.afm.data.database.entities.NewsEntity

sealed class SimulationEvent {
    abstract val importance: Int
    abstract val shouldStop: Boolean

    data class MatchPlayed(
        val result: FixturesResultsEntity,
        override val importance: Int = 1,
        override val shouldStop: Boolean = false
    ) : SimulationEvent()

    data class UserMatchPlayed(
        val result: FixturesResultsEntity,
        override val importance: Int = 10,
        override val shouldStop: Boolean = true
    ) : SimulationEvent()

    data class TransferOffer(
        val playerName: String,
        val offeringTeam: String,
        val fee: Long,
        override val importance: Int = 8,
        override val shouldStop: Boolean = true
    ) : SimulationEvent()

    data class Injury(
        val playerName: String,
        val duration: Int,
        val isUserPlayer: Boolean,
        override val importance: Int = if (isUserPlayer) 7 else 1,
        override val shouldStop: Boolean = isUserPlayer
    ) : SimulationEvent()

    data class BoardMeeting(
        val title: String,
        val message: String,
        override val importance: Int = 9,
        override val shouldStop: Boolean = true
    ) : SimulationEvent()

    data class NewsHeadline(
        val news: NewsEntity,
        override val importance: Int = 5,
        override val shouldStop: Boolean = false
    ) : SimulationEvent()

    data class FinancialAlert(
        val message: String,
        val isCritical: Boolean,
        override val importance: Int = if (isCritical) { 8 } else { 3 },
        override val shouldStop: Boolean = isCritical
    ) : SimulationEvent()

    data class ContractOffer(
        val playerName: String,
        val offerDetails: String,
        override val importance: Int = 9,
        override val shouldStop: Boolean = true
    ) : SimulationEvent()

    data class JobOffer(
        val teamName: String,
        val role: String,
        override val importance: Int = 10,
        override val shouldStop: Boolean = true
    ) : SimulationEvent()

    data class SponsorshipOffer(
        val sponsorName: String,
        val value: Long,
        override val importance: Int = 8,
        override val shouldStop: Boolean = true
    ) : SimulationEvent()

    data class FacilityCompletion(
        val facilityName: String,
        val level: Int,
        override val importance: Int = 7,
        override val shouldStop: Boolean = true
    ) : SimulationEvent()

    data class AwardCeremony(
        val ceremonyName: String,
        val winnerName: String,
        override val importance: Int = 6,
        override val shouldStop: Boolean = true
    ) : SimulationEvent()

    data class YouthIntake(
        val teamName: String,
        val prospectCount: Int,
        override val importance: Int = 8,
        override val shouldStop: Boolean = true
    ) : SimulationEvent()

    data class Interview(
        val teamName: String,
        override val importance: Int = 9,
        override val shouldStop: Boolean = true
    ) : SimulationEvent()

    data class SeasonEnd(
        val season: String,
        override val importance: Int = 10,
        override val shouldStop: Boolean = true
    ) : SimulationEvent()

    data class Matchday(
        val fixtureId: Int,
        val opponentName: String,
        override val importance: Int = 10,
        override val shouldStop: Boolean = true
    ) : SimulationEvent()

    data class Story(
        val title: String,
        val message: String,
        override val importance: Int = 4,
        override val shouldStop: Boolean = false
    ) : SimulationEvent()

    data class CalendarMilestone(
        val title: String,
        val message: String,
        override val importance: Int = 7,
        override val shouldStop: Boolean = true
    ) : SimulationEvent()

    // --- FULL SCREEN IMMERSIVE EVENTS (Phase 7) ---

    data class PreseasonStart(
        val teamName: String,
        val season: String,
        val boardExpectations: String,
        val budget: Long,
        override val importance: Int = 10,
        override val shouldStop: Boolean = true
    ) : SimulationEvent()

    data class CommunityShield(
        val team1: String,
        val team2: String,
        val logo1: String?,
        val logo2: String?,
        override val importance: Int = 10,
        override val shouldStop: Boolean = true
    ) : SimulationEvent()

    data class LeagueKickoff(
        val leagueName: String,
        val predictedFinish: Int,
        val boardTarget: String,
        val openingOpponent: String,
        override val importance: Int = 10,
        override val shouldStop: Boolean = true
    ) : SimulationEvent()

    data class CupMilestone(
        val tournamentName: String,
        val roundName: String,
        val opponentName: String,
        val prizeMoney: Long,
        override val importance: Int = 10,
        override val shouldStop: Boolean = true
    ) : SimulationEvent()

    data class CAFGroupDraw(
        val groupName: String,
        val opponents: List<String>,
        val travelDistance: Int,
        override val importance: Int = 10,
        override val shouldStop: Boolean = true
    ) : SimulationEvent()

    data class CAFKnockoutDraw(
        val tournament: String,
        val round: String,
        val opponent: String,
        override val importance: Int = 10,
        override val shouldStop: Boolean = true
    ) : SimulationEvent()

    data class TransferWindowOpen(
        val budget: Long,
        val wageBudget: Long,
        val deadlineDay: String,
        override val importance: Int = 10,
        override val shouldStop: Boolean = true
    ) : SimulationEvent()

    data class DeadlineDay(
        val hoursRemaining: Int,
        val pendingDealsCount: Int,
        override val importance: Int = 10,
        override val shouldStop: Boolean = true
    ) : SimulationEvent()

    data class CAFFinal(
        val opponent: String,
        val stadium: String,
        val isFirstFinal: Boolean,
        override val importance: Int = 10,
        override val shouldStop: Boolean = true
    ) : SimulationEvent()

    data class AwardsGala(
        val season: String,
        val position: Int,
        val trophiesWon: List<String>,
        val topScorer: String,
        val revenue: Long,
        val fanApproval: Int,
        override val importance: Int = 10,
        override val shouldStop: Boolean = true
    ) : SimulationEvent()

    data class TrainingPlanProposed(
        val scheduleId: Int,
        val monthName: String,
        val proposedBy: String,
        override val importance: Int = 8,
        override val shouldStop: Boolean = true
    ) : SimulationEvent()

    data class NationalTeamCallUp(
        val countryName: String,
        val playersSelected: List<String>,
        override val importance: Int = 9,
        override val shouldStop: Boolean = false
    ) : SimulationEvent()

    data class InternationalBreak(
        val message: String,
        override val importance: Int = 10,
        override val shouldStop: Boolean = true
    ) : SimulationEvent()
}
