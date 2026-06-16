package com.fameafrica.afm.domain.manager.inbox

import com.fameafrica.afm.domain.model.SimulationEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InboxActionEngine @Inject constructor() {

    enum class InboxCategory {
        SEASON_EVENT, MATCHDAY, TRANSFER, CONTRACT, STAFF, BOARD, SPONSOR, FINANCE, YOUTH_ACADEMY, WORLD_NEWS, AWARDS, CAF_COMPETITION, NATIONAL_TEAM, INTERNATIONAL_BREAK, SCOUTING, INJURY, SUSPENSION
    }

    enum class InboxPriority {
        CRITICAL, HIGH, MEDIUM, LOW
    }

    enum class ActionType {
        NAVIGATE, PLAY_MATCH, RESPOND_OFFER, VIEW_SCREEN, ACCEPT, DECLINE, SELECT_OPPONENT, SUBMIT_SQUAD, CONTINUE_SEASON
    }

    data class InboxAction(
        val id: Long,
        val title: String,
        val description: String,
        val category: InboxCategory,
        val priority: InboxPriority,
        val actionType: ActionType,
        val targetRoute: String?,
        val requiresUserAction: Boolean,
        val canDismiss: Boolean,
        val reward: String? = null,
        val deadlineWeek: Int? = null,
        val metadata: Map<String, String> = emptyMap()
    )

    private val _inboxItems = MutableStateFlow<List<InboxAction>>(emptyList())
    val inboxItems: StateFlow<List<InboxAction>> = _inboxItems

    fun addAction(action: InboxAction) {
        _inboxItems.value = (_inboxItems.value + action).sortedBy { it.priority }
    }

    fun removeAction(id: Long) {
        _inboxItems.value = _inboxItems.value.filter { it.id != id }
    }

    // Maps SimulationEvent to InboxAction
    fun processEvent(event: SimulationEvent): InboxAction? {
        return when (event) {
            is SimulationEvent.TransferOffer -> InboxAction(
                id = System.currentTimeMillis(),
                title = "Transfer Offer: ${event.playerName}",
                description = "Offer from ${event.offeringTeam} for ${event.fee} TZS",
                category = InboxCategory.TRANSFER,
                priority = InboxPriority.HIGH,
                actionType = ActionType.RESPOND_OFFER,
                targetRoute = "transfers_screen",
                requiresUserAction = true,
                canDismiss = false
            )
            is SimulationEvent.SponsorshipOffer -> InboxAction(
                id = System.currentTimeMillis(),
                title = "Sponsorship Offer",
                description = "New offer from ${event.sponsorName} for ${event.value} TZS",
                category = InboxCategory.SPONSOR,
                priority = InboxPriority.HIGH,
                actionType = ActionType.RESPOND_OFFER,
                targetRoute = "sponsorships_screen",
                requiresUserAction = true,
                canDismiss = true
            )
            is SimulationEvent.Matchday -> InboxAction(
                id = System.currentTimeMillis(),
                title = "Matchday: vs ${event.opponentName}",
                description = "Get ready for the match",
                category = InboxCategory.MATCHDAY,
                priority = InboxPriority.CRITICAL,
                actionType = ActionType.PLAY_MATCH,
                targetRoute = "match_prep_screen/${event.fixtureId}",
                requiresUserAction = true,
                canDismiss = false
            )
            is SimulationEvent.PreseasonStart -> InboxAction(
                id = System.currentTimeMillis(),
                title = "Preseason Begins",
                description = "Prepare for the new season",
                category = InboxCategory.SEASON_EVENT,
                priority = InboxPriority.CRITICAL,
                actionType = ActionType.SELECT_OPPONENT,
                targetRoute = "preseason_screen",
                requiresUserAction = true,
                canDismiss = false
            )
            is SimulationEvent.LeagueKickoff -> InboxAction(
                id = System.currentTimeMillis(),
                title = "League Season Begins",
                description = "The league starts soon",
                category = InboxCategory.SEASON_EVENT,
                priority = InboxPriority.CRITICAL,
                actionType = ActionType.PLAY_MATCH,
                targetRoute = "league_kickoff_screen",
                requiresUserAction = true,
                canDismiss = false
            )
            is SimulationEvent.CupMilestone -> InboxAction(
                id = System.currentTimeMillis(),
                title = "${event.tournamentName} ${event.roundName}",
                description = "Prepare for the match against ${event.opponentName}",
                category = InboxCategory.CAF_COMPETITION,
                priority = InboxPriority.HIGH,
                actionType = ActionType.VIEW_SCREEN,
                targetRoute = "cup_bracket_screen",
                requiresUserAction = true,
                canDismiss = false
            )
            is SimulationEvent.DeadlineDay -> InboxAction(
                id = System.currentTimeMillis(),
                title = "Deadline Day",
                description = "Final hours to sign players",
                category = InboxCategory.TRANSFER,
                priority = InboxPriority.CRITICAL,
                actionType = ActionType.NAVIGATE,
                targetRoute = "transfers_screen",
                requiresUserAction = true,
                canDismiss = false
            )
            is SimulationEvent.AwardsGala -> InboxAction(
                id = System.currentTimeMillis(),
                title = "Awards Ceremony",
                description = "See season award winners",
                category = InboxCategory.AWARDS,
                priority = InboxPriority.HIGH,
                actionType = ActionType.VIEW_SCREEN,
                targetRoute = "awards_screen",
                requiresUserAction = true,
                canDismiss = false
            )
            is SimulationEvent.SeasonEnd -> InboxAction(
                id = System.currentTimeMillis(),
                title = "Season Review",
                description = "Review your team's season performance",
                category = InboxCategory.SEASON_EVENT,
                priority = InboxPriority.CRITICAL,
                actionType = ActionType.CONTINUE_SEASON,
                targetRoute = "season_review_screen",
                requiresUserAction = true,
                canDismiss = false
            )
            is SimulationEvent.NationalTeamCallUp -> InboxAction(
                id = System.currentTimeMillis(),
                title = "National Team Call-Up",
                description = "${event.countryName} has called up: ${event.playersSelected.joinToString()}",
                category = InboxCategory.NATIONAL_TEAM,
                priority = InboxPriority.HIGH,
                actionType = ActionType.VIEW_SCREEN,
                targetRoute = "national_team_screen",
                requiresUserAction = false,
                canDismiss = true
            )
            is SimulationEvent.InternationalBreak -> InboxAction(
                id = System.currentTimeMillis(),
                title = "International Break",
                description = event.message,
                category = InboxCategory.INTERNATIONAL_BREAK,
                priority = InboxPriority.CRITICAL,
                actionType = ActionType.PLAY_MATCH,
                targetRoute = "international_matches_screen",
                requiresUserAction = true,
                canDismiss = false
            )
            else -> null
        }
    }
}
