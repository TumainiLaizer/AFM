package com.fameafrica.afm.utils.notifications

import com.fameafrica.afm.data.database.entities.*
import com.fameafrica.afm.data.database.entities.NotificationsEntity
import com.fameafrica.afm.data.database.entities.NotificationType

/**
 * Factory class for creating different types of notifications
 * Integrates with all game systems
 */
object NotificationFactory {

    // ============ MATCH NOTIFICATIONS ============

    fun createMatchReminder(
        fixture: FixturesEntity,
        homeTeam: String,
        awayTeam: String,
        minutesUntil: Int
    ): NotificationsEntity {
        val priority = when {
            minutesUntil < 60 -> 5  // Critical - less than 1 hour
            minutesUntil < 180 -> 4  // High - less than 3 hours
            minutesUntil < 720 -> 3  // Medium - less than 12 hours
            else -> 2  // Low
        }

        return NotificationsEntity(
            title = "⚽ Match Reminder: $homeTeam vs $awayTeam",
            message = "Your team's match starts in $minutesUntil minutes. Get ready!",
            notificationType = NotificationType.MATCH.value,
            priority = priority,
            icon = "⚽",
            actionUrl = "afm://match/${fixture.id}",
            actionText = "View Match",
            relatedEntityType = "MATCH",
            relatedEntityId = fixture.id,
            relatedEntityName = "$homeTeam vs $awayTeam",
            color = "#4CAF50",
            id = 0,
            timestamp = System.currentTimeMillis(),
            isRead = false,
            isArchived = false,
            imageUrl = null,
            userId = null,
            expiryTime = null,
            dismissible = true,
            dataJson = null
        )
    }

    fun createMatchResultNotification(
        result: FixturesResultsEntity
    ): NotificationsEntity {
        val title = "⚽ Match Complete: ${result.homeTeam} ${result.homeScore} - ${result.awayScore} ${result.awayTeam}"

        return NotificationsEntity(
            title = title,
            message = "Match report available. Tap to see details and player ratings.",
            notificationType = NotificationType.MATCH.value,
            priority = 2,
            icon = "⚽",
            actionUrl = "afm://match/${result.fixtureId}/result",
            actionText = "View Report",
            relatedEntityType = "MATCH",
            relatedEntityId = result.fixtureId,
            color = "#2196F3",
            id = 0,
            timestamp = System.currentTimeMillis(),
            isRead = false,
            isArchived = false,
            imageUrl = null,
            relatedEntityName = "${result.homeTeam} vs ${result.awayTeam}",
            userId = null,
            expiryTime = null,
            dismissible = true,
            dataJson = null
        )
    }

    fun createMatchResult(
        fixture: FixturesEntity,
        homeScore: Int,
        awayScore: Int,
        isUserTeam: Boolean,
        isUpset: Boolean
    ): NotificationsEntity {
        val result = if (isUserTeam) {
            if (homeScore > awayScore) "WON" else if (homeScore < awayScore) "LOST" else "DREW"
        } else null

        val title = when {
            isUserTeam && result == "WON" -> "🎉 VICTORY! ${fixture.homeTeam} $homeScore - $awayScore ${fixture.awayTeam}"
            isUserTeam && result == "LOST" -> "😔 DEFEAT: ${fixture.homeTeam} $homeScore - $awayScore ${fixture.awayTeam}"
            isUserTeam && result == "DREW" -> "🤝 DRAW: ${fixture.homeTeam} $homeScore - $awayScore ${fixture.awayTeam}"
            isUpset -> "⚡ MAJOR UPSET! ${fixture.homeTeam} $homeScore - $awayScore ${fixture.awayTeam}"
            else -> "⚽ Match Complete: ${fixture.homeTeam} $homeScore - $awayScore ${fixture.awayTeam}"
        }

        val priority = when {
            isUserTeam && result == "WON" -> 4
            isUserTeam && result == "LOST" -> 4
            isUpset -> 4
            else -> 2
        }

        return NotificationsEntity(
            title = title,
            message = "Match report available. Tap to see details and player ratings.",
            notificationType = NotificationType.MATCH.value,
            priority = priority,
            icon = if (isUserTeam && result == "WON") "🎉" else "⚽",
            actionUrl = "afm://match/${fixture.id}/result",
            actionText = "View Report",
            relatedEntityType = "MATCH",
            relatedEntityId = fixture.id,
            color = if (isUserTeam && result == "WON") "#4CAF50" else if (isUserTeam && result == "LOST") "#F44336" else "#2196F3",
            id = 0,
            timestamp = System.currentTimeMillis(),
            isRead = false,
            isArchived = false,
            imageUrl = null,
            relatedEntityName = "${fixture.homeTeam} vs ${fixture.awayTeam}",
            userId = null,
            expiryTime = null,
            dismissible = true,
            dataJson = null
        )
    }

    // ============ TRANSFER NOTIFICATIONS ============

    fun createTransferOffer(
        transfer: TransfersEntity,
        playerName: String,
        offeringTeam: String,
        fee: Int
    ): NotificationsEntity {
        return NotificationsEntity(
            title = "💰 Transfer Offer Received",
            message = "$offeringTeam has offered ${fee / 1_000_000}M for $playerName",
            notificationType = NotificationType.TRANSFER.value,
            priority = 4,
            icon = "💰",
            actionUrl = "afm://transfer/${transfer.id}",
            actionText = "View Offer",
            relatedEntityType = "TRANSFER",
            relatedEntityId = transfer.id,
            relatedEntityName = playerName,
            expiryTime = System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000,  // 7 days
            color = "#FF8C00",
            id = 0,
            timestamp = System.currentTimeMillis(),
            isRead = false,
            isArchived = false,
            imageUrl = null,
            userId = null,
            dismissible = true,
            dataJson = null
        )
    }

    fun createTransferCompleted(
        playerName: String,
        fromTeam: String,
        toTeam: String,
        fee: Int,
        isUserTeam: Boolean
    ): NotificationsEntity {
        val title = if (isUserTeam) {
            "✅ Transfer Complete: $playerName joins $toTeam"
        } else {
            "🔄 Transfer: $playerName moves from $fromTeam to $toTeam"
        }

        return NotificationsEntity(
            title = title,
            message = "Transfer fee: ${fee / 1_000_000}M",
            notificationType = NotificationType.TRANSFER.value,
            priority = 3,
            icon = "✅",
            actionUrl = "afm://player/${playerName}",
            actionText = "View Player",
            relatedEntityType = "PLAYER",
            relatedEntityName = playerName,
            color = "#4CAF50",
            id = 0,
            timestamp = System.currentTimeMillis(),
            isRead = false,
            isArchived = false,
            imageUrl = null,
            relatedEntityId = 0, // Should ideally pass playerId
            userId = null,
            expiryTime = null,
            dismissible = true,
            dataJson = null
        )
    }

    // ============ INJURY NOTIFICATIONS ============

    fun createInjuryNotification(
        player: PlayersEntity,
        injuryType: String,
        recoveryDays: Int,
        matchInvolved: Boolean = false
    ): NotificationsEntity {
        val severity = when (injuryType) {
            "MINOR" -> "Minor"
            "MODERATE" -> "Moderate"
            "SEVERE" -> "SEVERE"
            else -> "Injury"
        }

        val title = if (matchInvolved) {
            "🩹 INJURY during match: ${player.name}"
        } else {
            "🩹 Injury Update: ${player.name}"
        }

        val priority = when (injuryType) {
            "SEVERE" -> 5
            "MODERATE" -> 4
            else -> 3
        }

        return NotificationsEntity(
            title = title,
            message = "$severity injury. Estimated recovery: $recoveryDays days.",
            notificationType = NotificationType.INJURY.value,
            priority = priority,
            icon = "🩹",
            actionUrl = "afm://player/${player.id}",
            actionText = "View Player",
            relatedEntityType = "PLAYER",
            relatedEntityId = player.id,
            relatedEntityName = player.name,
            color = "#F44336",
            id = 0,
            timestamp = System.currentTimeMillis(),
            isRead = false,
            isArchived = false,
            imageUrl = null,
            userId = null,
            expiryTime = null,
            dismissible = true,
            dataJson = null
        )
    }

    fun createPlayerRecoveredNotification(
        player: PlayersEntity
    ): NotificationsEntity {
        return NotificationsEntity(
            title = "✅ Player Recovered: ${player.name}",
            message = "${player.name} is now fit and available for selection.",
            notificationType = NotificationType.INJURY.value,
            priority = 3,
            icon = "✅",
            actionUrl = "afm://player/${player.id}",
            actionText = "View Player",
            relatedEntityType = "PLAYER",
            relatedEntityId = player.id,
            relatedEntityName = player.name,
            color = "#4CAF50",
            id = 0,
            timestamp = System.currentTimeMillis(),
            isRead = false,
            isArchived = false,
            imageUrl = null,
            userId = null,
            expiryTime = null,
            dismissible = true,
            dataJson = null
        )
    }

    // ============ CONTRACT NOTIFICATIONS ============

    fun createContractExpiringNotification(
        player: PlayersEntity,
        contract: PlayerContractsEntity,
        monthsRemaining: Int
    ): NotificationsEntity {
        val priority = when {
            monthsRemaining <= 1 -> 5
            monthsRemaining <= 3 -> 4
            monthsRemaining <= 6 -> 3
            else -> 2
        }

        return NotificationsEntity(
            title = "⚠️ Contract Expiring: ${player.name}",
            message = "${player.name}'s contract expires in $monthsRemaining months. Renew now to avoid losing them on a free.",
            notificationType = NotificationType.CONTRACT.value,
            priority = priority,
            icon = "⚠️",
            actionUrl = "afm://contract/${player.id}",
            actionText = "Renew Contract",
            relatedEntityType = "PLAYER",
            relatedEntityId = player.id,
            relatedEntityName = player.name,
            color = "#FFD700",
            id = 0,
            timestamp = System.currentTimeMillis(),
            isRead = false,
            isArchived = false,
            imageUrl = null,
            userId = null,
            expiryTime = null,
            dismissible = true,
            dataJson = null
        )
    }

    fun createContractRenewedNotification(
        player: PlayersEntity,
        years: Int,
        salary: Int
    ): NotificationsEntity {
        return NotificationsEntity(
            title = "✅ Contract Renewed: ${player.name}",
            message = "${player.name} has signed a new $years-year contract worth ${salary / 1_000_000}M per year.",
            notificationType = NotificationType.CONTRACT.value,
            priority = 3,
            icon = "✅",
            actionUrl = "afm://player/${player.id}",
            actionText = "View Player",
            relatedEntityType = "PLAYER",
            relatedEntityId = player.id,
            relatedEntityName = player.name,
            color = "#4CAF50",
            id = 0,
            timestamp = System.currentTimeMillis(),
            isRead = false,
            isArchived = false,
            imageUrl = null,
            userId = null,
            expiryTime = null,
            dismissible = true,
            dataJson = null
        )
    }

    // ============ SCOUT NOTIFICATIONS ============

    fun createScoutReportCompleteNotification(
        scout: StaffEntity,
        player: PlayersEntity,
        report: ScoutAssignmentsEntity
    ): NotificationsEntity {
        val verdict = report.verdict ?: "Report Ready"

        return NotificationsEntity(
            title = "🔍 Scout Report Complete: ${player.name}",
            message = "Scout ${scout.name} has completed their report on ${player.name}. Verdict: $verdict",
            notificationType = NotificationType.SCOUT.value,
            priority = 3,
            icon = "🔍",
            actionUrl = "afm://scout/report/${report.id}",
            actionText = "View Report",
            relatedEntityType = "SCOUT",
            relatedEntityId = report.id,
            relatedEntityName = player.name,
            color = "#9C27B0",
            id = 0,
            timestamp = System.currentTimeMillis(),
            isRead = false,
            isArchived = false,
            imageUrl = null,
            userId = null,
            expiryTime = null,
            dismissible = true,
            dataJson = null
        )
    }

    // ============ BOARD NOTIFICATIONS ============

    fun createBoardRequestNotification(
        request: BoardRequestsEntity,
        boardSatisfaction: Int
    ): NotificationsEntity {
        val priority = when {
            boardSatisfaction < 30 -> 5
            boardSatisfaction < 50 -> 4
            else -> 3
        }

        return NotificationsEntity(
            title = "🏢 Board Request: ${request.requestType}",
            message = request.requestDescription,
            notificationType = NotificationType.BOARD_MESSAGE.value,
            priority = priority,
            icon = "🏢",
            actionUrl = "afm://board/request/${request.id}",
            actionText = "Review Request",
            relatedEntityType = "BOARD_REQUEST",
            relatedEntityId = request.id,
            color = "#795548",
            id = 0,
            timestamp = System.currentTimeMillis(),
            isRead = false,
            isArchived = false,
            imageUrl = null,
            relatedEntityName = request.requestType,
            userId = null,
            expiryTime = null,
            dismissible = true,
            dataJson = null
        )
    }

    fun createBoardEvaluationNotification(
        evaluation: BoardEvaluationEntity,
        status: String
    ): NotificationsEntity {
        val title = when (status) {
            "Safe" -> "✅ Board Evaluation: Your job is SAFE"
            "Under Review" -> "⚠️ Board Evaluation: Your position is UNDER REVIEW"
            "On Thin Ice" -> "❄️ Board Evaluation: You're on THIN ICE"
            "Critical" -> "🔥 Board Evaluation: Your job is CRITICAL"
            else -> "📊 Board Evaluation Update"
        }

        val priority = when (status) {
            "Critical" -> 5
            "On Thin Ice" -> 4
            "Under Review" -> 3
            else -> 2
        }

        return NotificationsEntity(
            title = title,
            message = "Board satisfaction: ${evaluation.boardSatisfaction}%",
            notificationType = NotificationType.BOARD_MESSAGE.value,
            priority = priority,
            icon = "📊",
            actionUrl = "afm://board/evaluation",
            actionText = "View Details",
            color = when (status) {
                "Critical" -> "#F44336"
                "On Thin Ice" -> "#FF8C00"
                "Under Review" -> "#FFD700"
                else -> "#4CAF50"
            },
            id = 0,
            timestamp = System.currentTimeMillis(),
            isRead = false,
            isArchived = false,
            imageUrl = null,
            relatedEntityType = "BOARD",
            relatedEntityId = evaluation.id,
            relatedEntityName = "Board Evaluation",
            userId = null,
            expiryTime = null,
            dismissible = true,
            dataJson = null
        )
    }

    // ============ FAN NOTIFICATIONS ============

    fun createFanReactionNotification(
        reaction: FanReactionsEntity,
        confidenceLevel: Int
    ): NotificationsEntity {
        val isPositive = reaction.sentiment == "Positive"

        return NotificationsEntity(
            title = if (isPositive) "📢 Fans are HAPPY!" else "📢 Fans are UNHAPPY",
            message = reaction.reaction,
            notificationType = NotificationType.FAN_MESSAGE.value,
            priority = if (isPositive) 2 else 3,
            icon = if (isPositive) "🎉" else "😠",
            actionUrl = "afm://fans",
            actionText = "View Fan Reactions",
            relatedEntityType = "FAN_REACTION",
            relatedEntityId = reaction.id,
            color = if (isPositive) "#4CAF50" else "#F44336",
            id = 0,
            timestamp = System.currentTimeMillis(),
            isRead = false,
            isArchived = false,
            imageUrl = null,
            relatedEntityName = "Fan Reaction",
            userId = null,
            expiryTime = null,
            dismissible = true,
            dataJson = null
        )
    }

    // ============ MEDIA NOTIFICATIONS ============

    fun createNewsNotification(
        news: NewsEntity
    ): NotificationsEntity {
        return NotificationsEntity(
            title = "📰 ${news.headline}",
            message = news.content.take(100) + "...",
            notificationType = NotificationType.MEDIA.value,
            priority = if (news.isTopNews == 1) 4 else 2,
            icon = "📰",
            actionUrl = "afm://news/${news.id}",
            actionText = "Read Article",
            relatedEntityType = "NEWS",
            relatedEntityId = news.id,
            color = "#2196F3",
            id = 0,
            timestamp = System.currentTimeMillis(),
            isRead = false,
            isArchived = false,
            imageUrl = null,
            relatedEntityName = "News Article",
            userId = null,
            expiryTime = null,
            dismissible = true,
            dataJson = null
        )
    }

    fun createInterviewRequestNotification(
        interview: InterviewsEntity,
        journalist: JournalistsEntity
    ): NotificationsEntity {
        return NotificationsEntity(
            title = "🎤 Interview Request from ${journalist.name}",
            message = "${journalist.name} (${journalist.mediaCompany}) would like to interview you about ${interview.topic}.",
            notificationType = NotificationType.MEDIA.value,
            priority = 3,
            icon = "🎤",
            actionUrl = "afm://interview/${interview.id}",
            actionText = "Respond",
            relatedEntityType = "INTERVIEW",
            relatedEntityId = interview.id,
            relatedEntityName = journalist.name,
            expiryTime = System.currentTimeMillis() + 3 * 24 * 60 * 60 * 1000,  // 3 days
            color = "#9C27B0",
            id = 0,
            timestamp = System.currentTimeMillis(),
            isRead = false,
            isArchived = false,
            imageUrl = null,
            userId = null,
            dismissible = true,
            dataJson = null
        )
    }

    // ============ ACHIEVEMENT & TROPHY NOTIFICATIONS ============

    fun createTrophyWonNotification(
        trophy: TrophiesEntity,
        clubName: String
    ): NotificationsEntity {
        val isMajor = trophy.isContinentalTitle || trophy.trophyType == "LEAGUE_TITLE"

        return NotificationsEntity(
            title = if (isMajor) "🏆🏆 MAJOR TROPHY WON! 🏆🏆" else "🏆 Trophy Won!",
            message = "$clubName has won the ${trophy.trophyName}!",
            notificationType = NotificationType.TROPHY.value,
            priority = if (isMajor) 5 else 4,
            icon = "🏆",
            actionUrl = "afm://trophy/${trophy.id}",
            actionText = "View Trophy",
            relatedEntityType = "TROPHY",
            relatedEntityId = trophy.id,
            color = "#FFD700",
            id = 0,
            timestamp = System.currentTimeMillis(),
            isRead = false,
            isArchived = false,
            imageUrl = null,
            relatedEntityName = trophy.trophyName,
            userId = null,
            expiryTime = null,
            dismissible = true,
            dataJson = null
        )
    }

    fun createAchievementUnlockedNotification(
        achievementName: String,
        description: String
    ): NotificationsEntity {
        return NotificationsEntity(
            title = "🏅 Achievement Unlocked: $achievementName",
            message = description,
            notificationType = NotificationType.ACHIEVEMENT.value,
            priority = 3,
            icon = "🏅",
            actionUrl = "afm://achievements",
            actionText = "View Achievements",
            color = "#FF8C00",
            id = 0,
            timestamp = System.currentTimeMillis(),
            isRead = false,
            isArchived = false,
            imageUrl = null,
            relatedEntityType = "ACHIEVEMENT",
            relatedEntityId = 0,
            relatedEntityName = achievementName,
            userId = null,
            expiryTime = null,
            dismissible = true,
            dataJson = null
        )
    }

    // ============ OFFER NOTIFICATIONS ============

    fun createManagerOfferNotification(
        offer: ManagerOffersEntity,
        teamName: String
    ): NotificationsEntity {
        return NotificationsEntity(
            title = "💼 New Job Offer from $teamName",
            message = "$teamName has offered you a position. Salary: ${offer.offeredSalary / 1_000_000}M for ${offer.contractYears} years.",
            notificationType = NotificationType.OFFER.value,
            priority = 4,
            icon = "💼",
            actionUrl = "afm://offer/${offer.id}",
            actionText = "Review Offer",
            relatedEntityType = "MANAGER_OFFER",
            relatedEntityId = offer.id,
            relatedEntityName = teamName,
            expiryTime = offer.expiryDate.toLong(),
            color = "#4CAF50",
            id = 0,
            timestamp = System.currentTimeMillis(),
            isRead = false,
            isArchived = false,
            imageUrl = null,
            userId = null,
            dismissible = true,
            dataJson = null
        )
    }

    fun createNationalJobOffer(
        teamName: String,
        message: String
    ): NotificationsEntity {
        return NotificationsEntity(
            title = "🌍 National Team Call: $teamName",
            message = message,
            notificationType = NotificationType.OFFER.value,
            priority = 5,
            icon = "🌍",
            actionUrl = "afm://national/jobs",
            actionText = "View Jobs",
            relatedEntityType = "NATIONAL_OFFER",
            relatedEntityId = 0,
            relatedEntityName = teamName,
            color = "#4CAF50",
            id = 0,
            timestamp = System.currentTimeMillis(),
            isRead = false,
            isArchived = false,
            imageUrl = null,
            userId = null,
            expiryTime = null,
            dismissible = true,
            dataJson = null
        )
    }

    // ============ REMINDER NOTIFICATIONS ============

    fun createTrainingReminderNotification(
        playerName: String,
        trainingType: String
    ): NotificationsEntity {
        return NotificationsEntity(
            title = "⚡ Training Reminder",
            message = "$playerName has ${trainingType.lowercase()} training scheduled today.",
            notificationType = NotificationType.REMINDER.value,
            priority = 2,
            icon = "⚡",
            actionUrl = "afm://training",
            actionText = "View Training",
            color = "#2196F3",
            id = 0,
            timestamp = System.currentTimeMillis(),
            isRead = false,
            isArchived = false,
            imageUrl = null,
            relatedEntityType = "TRAINING",
            relatedEntityId = 0,
            relatedEntityName = playerName,
            userId = null,
            expiryTime = null,
            dismissible = true,
            dataJson = null
        )
    }

    fun createTransferWindowReminder(
        windowType: String,
        daysRemaining: Int
    ): NotificationsEntity {
        val urgency = when {
            daysRemaining <= 3 -> 5
            daysRemaining <= 7 -> 4
            daysRemaining <= 14 -> 3
            else -> 2
        }

        return NotificationsEntity(
            title = "⏰ Transfer Window Closing Soon",
            message = "The $windowType transfer window closes in $daysRemaining days. Make your moves!",
            notificationType = NotificationType.REMINDER.value,
            priority = urgency,
            icon = "⏰",
            actionUrl = "afm://transfers",
            actionText = "View Transfers",
            color = "#FF8C00",
            id = 0,
            timestamp = System.currentTimeMillis(),
            isRead = false,
            isArchived = false,
            imageUrl = null,
            relatedEntityType = "TRANSFER_WINDOW",
            relatedEntityId = 0,
            relatedEntityName = windowType,
            userId = null,
            expiryTime = null,
            dismissible = true,
            dataJson = null
        )
    }

    // ============ DRAMA NOTIFICATIONS ============

    fun createPlayerComplaint(
        playerName: String,
        message: String
    ): NotificationsEntity {
        return NotificationsEntity(
            title = "🚨 Player Unrest: $playerName",
            message = "$playerName $message",
            notificationType = NotificationType.MEDIA.value,
            priority = 4,
            icon = "🚨",
            actionUrl = "afm://squad",
            actionText = "Manage Squad",
            relatedEntityType = "PLAYER",
            relatedEntityId = 0,
            relatedEntityName = playerName,
            color = "#F44336",
            id = 0,
            timestamp = System.currentTimeMillis(),
            isRead = false,
            isArchived = false,
            imageUrl = null,
            userId = null,
            expiryTime = null,
            dismissible = true,
            dataJson = null
        )
    }

    // ============ SYSTEM NOTIFICATIONS ============

    fun createGameSaveNotification(
        saveName: String,
        success: Boolean
    ): NotificationsEntity {
        return NotificationsEntity(
            title = if (success) "✅ Game Saved" else "❌ Save Failed",
            message = if (success) "Your game '$saveName' was saved successfully." else "Failed to save game. Check storage space.",
            notificationType = NotificationType.SYSTEM.value,
            priority = if (success) 1 else 4,
            icon = if (success) "✅" else "❌",
            color = if (success) "#4CAF50" else "#F44336",
            id = 0,
            timestamp = System.currentTimeMillis(),
            isRead = false,
            isArchived = false,
            imageUrl = null,
            actionUrl = "",
            actionText = "",
            relatedEntityType = "SYSTEM",
            relatedEntityId = 0,
            relatedEntityName = saveName,
            userId = null,
            expiryTime = null,
            dismissible = true,
            dataJson = null
        )
    }

    fun createDataSyncNotification(
        message: String,
        isComplete: Boolean
    ): NotificationsEntity {
        return NotificationsEntity(
            title = if (isComplete) "🔄 Sync Complete" else "🔄 Syncing Data",
            message = message,
            notificationType = NotificationType.SYSTEM.value,
            priority = 1,
            icon = "🔄",
            color = "#2196F3",
            id = 0,
            timestamp = System.currentTimeMillis(),
            isRead = false,
            isArchived = false,
            imageUrl = null,
            actionUrl = "",
            actionText = "",
            relatedEntityType = "SYSTEM",
            relatedEntityId = 0,
            relatedEntityName = "Data Sync",
            userId = null,
            expiryTime = null,
            dismissible = true,
            dataJson = null
        )
    }
}
