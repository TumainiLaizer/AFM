package com.fameafrica.afm.utils.notifications

import com.fameafrica.afm.R
import com.fameafrica.afm.data.database.entities.NotificationPriority

/**
 * Utility class for Notification Priority operations
 * With African-themed sound resources and level descriptions
 */
object NotificationPriorityUtils {

    // ============ LEVEL DESCRIPTIONS ============

    /**
     * Get priority level name
     */
    fun getPriorityLevel(priority: NotificationPriority): String {
        return when (priority) {
            NotificationPriority.INFO -> "Information"
            NotificationPriority.LOW -> "Low Priority"
            NotificationPriority.MEDIUM -> "Medium Priority"
            NotificationPriority.HIGH -> "High Priority"
            NotificationPriority.CRITICAL -> "CRITICAL"
        }
    }

    /**
     * Get priority description
     */
    fun getPriorityDescription(priority: NotificationPriority): String {
        return when (priority) {
            NotificationPriority.INFO -> "General information, no urgent action needed"
            NotificationPriority.LOW -> "Low priority - can be addressed later"
            NotificationPriority.MEDIUM -> "Medium priority - should be addressed soon"
            NotificationPriority.HIGH -> "High priority - requires your attention"
            NotificationPriority.CRITICAL -> "CRITICAL - immediate action required!"
        }
    }

    /**
     * Get African-style sound resource ID based on priority
     * 
     * Sound Descriptions:
     * - CRITICAL: Urgent djembe drumming + talking drum - commands attention, urgent, powerful
     * - HIGH: Kora + djembe - important, purposeful, energetic
     * - MEDIUM: Kalimba + shekere - noticeable, pleasant, engaging
     * - LOW: Soft kalimba + gentle shaker - subtle, calm, smooth
     * - INFO: Light shaker + soft drum - ambient, peaceful, informative
     */
    fun getPrioritySoundRes(priority: NotificationPriority): Int {
        return when (priority) {
            NotificationPriority.CRITICAL -> R.raw.notification_critical   // Urgent djembe + talking drum
            NotificationPriority.HIGH -> R.raw.notification_high             // Kora + djembe - important
            NotificationPriority.MEDIUM -> R.raw.notification_medium      // Kalimba + shekere - engaging
            NotificationPriority.LOW -> R.raw.notification_low                // Soft kalimba - subtle
            NotificationPriority.INFO -> R.raw.notification_info                // Light shaker - informative
        }
    }

    /**
     * Get African instrument name for the notification sound
     */
    fun getPrioritySoundInstrument(priority: NotificationPriority): String {
        return when (priority) {
            NotificationPriority.CRITICAL -> "Djembe + Talking Drum"
            NotificationPriority.HIGH -> "Kora + Djembe"
            NotificationPriority.MEDIUM -> "Kalimba + Shekere"
            NotificationPriority.LOW -> "Kalimba"
            NotificationPriority.INFO -> "Shekere"
        }
    }

    /**
     * Get sound description for debugging/UI
     */
    fun getPrioritySoundDescription(priority: NotificationPriority): String {
        return when (priority) {
            NotificationPriority.CRITICAL -> "Urgent djembe drumming with talking drum - commands immediate attention"
            NotificationPriority.HIGH -> "Energetic kora with djembe - important and purposeful"
            NotificationPriority.MEDIUM -> "Engaging kalimba with shekere - noticeable but not urgent"
            NotificationPriority.LOW -> "Soft kalimba melody - subtle and smooth"
            NotificationPriority.INFO -> "Light shekere shaker - calm and informative"
        }
    }

    // ============ CALCULATION METHODS ============

    /**
     * Calculate priority based on time sensitivity
     */
    fun calculateTimeBasedPriority(
        deadlineTimestamp: Long,
        currentTime: Long = System.currentTimeMillis()
    ): NotificationPriority {
        val timeRemaining = deadlineTimestamp - currentTime

        return when {
            timeRemaining < 0 -> NotificationPriority.CRITICAL  // Overdue
            timeRemaining < 1 * 60 * 60 * 1000 -> NotificationPriority.CRITICAL  // < 1 hour
            timeRemaining < 3 * 60 * 60 * 1000 -> NotificationPriority.HIGH      // < 3 hours
            timeRemaining < 12 * 60 * 60 * 1000 -> NotificationPriority.MEDIUM   // < 12 hours
            timeRemaining < 24 * 60 * 60 * 1000 -> NotificationPriority.LOW      // < 24 hours
            else -> NotificationPriority.INFO                                     // > 24 hours
        }
    }

    /**
     * Calculate priority based on match importance
     */
    fun calculateMatchPriority(
        isUserTeam: Boolean,
        isDerby: Boolean = false,
        isCupFinal: Boolean = false,
        isTournamentMatch: Boolean = false
    ): NotificationPriority {
        return when {
            isCupFinal && isUserTeam -> NotificationPriority.CRITICAL
            isDerby && isUserTeam -> NotificationPriority.HIGH
            isTournamentMatch && isUserTeam -> NotificationPriority.HIGH
            isCupFinal -> NotificationPriority.HIGH
            isDerby -> NotificationPriority.MEDIUM
            isUserTeam -> NotificationPriority.MEDIUM
            else -> NotificationPriority.LOW
        }
    }

    /**
     * Calculate priority based on board satisfaction
     */
    fun calculateBoardPriority(boardSatisfaction: Int): NotificationPriority {
        return when {
            boardSatisfaction < 20 -> NotificationPriority.CRITICAL
            boardSatisfaction < 40 -> NotificationPriority.HIGH
            boardSatisfaction < 60 -> NotificationPriority.MEDIUM
            boardSatisfaction < 80 -> NotificationPriority.LOW
            else -> NotificationPriority.INFO
        }
    }

    /**
     * Calculate priority based on injury severity
     */
    fun calculateInjuryPriority(
        injuryType: String,
        isKeyPlayer: Boolean = false
    ): NotificationPriority {
        return when (injuryType.uppercase()) {
            "SEVERE", "CAREER_ENDING" -> NotificationPriority.CRITICAL
            "MODERATE" -> if (isKeyPlayer) NotificationPriority.HIGH else NotificationPriority.MEDIUM
            "MINOR" -> if (isKeyPlayer) NotificationPriority.MEDIUM else NotificationPriority.LOW
            else -> NotificationPriority.INFO
        }
    }

    /**
     * Calculate priority based on transfer fee
     */
    fun calculateTransferPriority(
        transferFee: Int,
        isUserTeamInvolved: Boolean = true
    ): NotificationPriority {
        val feeInMillions = transferFee / 1_000_000.0

        return when {
            feeInMillions >= 50 && isUserTeamInvolved -> NotificationPriority.CRITICAL
            feeInMillions >= 20 && isUserTeamInvolved -> NotificationPriority.HIGH
            feeInMillions >= 10 && isUserTeamInvolved -> NotificationPriority.MEDIUM
            feeInMillions >= 5 && isUserTeamInvolved -> NotificationPriority.LOW
            feeInMillions >= 50 -> NotificationPriority.HIGH
            feeInMillions >= 20 -> NotificationPriority.MEDIUM
            else -> NotificationPriority.INFO
        }
    }

    /**
     * Calculate priority based on contract expiry
     */
    fun calculateContractPriority(
        monthsRemaining: Int,
        playerRating: Int
    ): NotificationPriority {
        val ratingFactor = when {
            playerRating >= 85 -> 3
            playerRating >= 75 -> 2
            playerRating >= 65 -> 1
            else -> 0
        }

        return when {
            monthsRemaining <= 1 -> NotificationPriority.CRITICAL
            monthsRemaining <= 3 -> if (ratingFactor >= 2) NotificationPriority.HIGH else NotificationPriority.MEDIUM
            monthsRemaining <= 6 -> if (ratingFactor >= 1) NotificationPriority.MEDIUM else NotificationPriority.LOW
            else -> NotificationPriority.INFO
        }
    }

    /**
     * Calculate priority based on scout report rating
     */
    fun calculateScoutPriority(scoutRating: Int): NotificationPriority {
        return when {
            scoutRating >= 85 -> NotificationPriority.HIGH
            scoutRating >= 75 -> NotificationPriority.MEDIUM
            scoutRating >= 65 -> NotificationPriority.LOW
            else -> NotificationPriority.INFO
        }
    }

    /**
     * Calculate priority based on fan confidence
     */
    fun calculateFanPriority(fanConfidence: Int): NotificationPriority {
        return when {
            fanConfidence <= 20 -> NotificationPriority.CRITICAL
            fanConfidence <= 40 -> NotificationPriority.HIGH
            fanConfidence <= 60 -> NotificationPriority.MEDIUM
            fanConfidence <= 80 -> NotificationPriority.LOW
            else -> NotificationPriority.INFO
        }
    }

    /**
     * Calculate priority based on achievement rarity
     */
    fun calculateAchievementPriority(
        isRare: Boolean,
        isFirstTime: Boolean
    ): NotificationPriority {
        return when {
            isRare && isFirstTime -> NotificationPriority.HIGH
            isRare -> NotificationPriority.MEDIUM
            isFirstTime -> NotificationPriority.MEDIUM
            else -> NotificationPriority.LOW
        }
    }

    // ============ UI STYLING METHODS ============

    /**
     * Get CSS class name for priority styling
     */
    fun getPriorityCssClass(priority: NotificationPriority): String {
        return when (priority) {
            NotificationPriority.INFO -> "priority-info"
            NotificationPriority.LOW -> "priority-low"
            NotificationPriority.MEDIUM -> "priority-medium"
            NotificationPriority.HIGH -> "priority-high"
            NotificationPriority.CRITICAL -> "priority-critical"
        }
    }

    /**
     * Get progress bar color for priority
     */
    fun getPriorityProgressColor(priority: NotificationPriority): String {
        return when (priority) {
            NotificationPriority.INFO -> "#2196F3"
            NotificationPriority.LOW -> "#4CAF50"
            NotificationPriority.MEDIUM -> "#FFD700"
            NotificationPriority.HIGH -> "#FF8C00"
            NotificationPriority.CRITICAL -> "#F44336"
        }
    }

    /**
     * Get animation speed multiplier based on priority
     * Higher priority = faster animation
     */
    fun getPriorityAnimationSpeed(priority: NotificationPriority): Float {
        return when (priority) {
            NotificationPriority.CRITICAL -> 2.0f
            NotificationPriority.HIGH -> 1.5f
            NotificationPriority.MEDIUM -> 1.2f
            NotificationPriority.LOW -> 1.0f
            NotificationPriority.INFO -> 0.8f
        }
    }

    /**
     * Get vibration pattern for priority
     * (Android vibration pattern: [delay, on, off, on, off, ...])
     */
    fun getPriorityVibrationPattern(priority: NotificationPriority): LongArray {
        return when (priority) {
            NotificationPriority.CRITICAL -> longArrayOf(0, 1000, 500, 1000, 500, 1000)  // Long urgent pulses
            NotificationPriority.HIGH -> longArrayOf(0, 500, 250, 500)                    // Double pulse
            NotificationPriority.MEDIUM -> longArrayOf(0, 300, 150, 300)                  // Medium pulse
            NotificationPriority.LOW -> longArrayOf(0, 200, 100, 200)                     // Short pulse
            NotificationPriority.INFO -> longArrayOf(0, 100)                               // Single short pulse
        }
    }

    /**
     * Get sound file name for debugging
     */
    fun getPrioritySoundFileName(priority: NotificationPriority): String {
        return when (priority) {
            NotificationPriority.CRITICAL -> "notification_critical.mp3"
            NotificationPriority.HIGH -> "notification_high.mp3"
            NotificationPriority.MEDIUM -> "notification_medium.mp3"
            NotificationPriority.LOW -> "notification_low.mp3"
            NotificationPriority.INFO -> "notification_info.mp3"
        }
    }
}

/**
 * Extension function to get priority level name
 */
fun NotificationPriority.getLevelName(): String {
    return NotificationPriorityUtils.getPriorityLevel(this)
}

/**
 * Extension function to get priority description
 */
fun NotificationPriority.getDescription(): String {
    return NotificationPriorityUtils.getPriorityDescription(this)
}

/**
 * Extension function to get sound resource
 */
fun NotificationPriority.getSoundRes(): Int {
    return NotificationPriorityUtils.getPrioritySoundRes(this)
}

/**
 * Extension function to get instrument name
 */
fun NotificationPriority.getInstrumentName(): String {
    return NotificationPriorityUtils.getPrioritySoundInstrument(this)
}

/**
 * Extension function to check if priority requires immediate attention
 */
fun NotificationPriority.requiresImmediateAttention(): Boolean =
    this == NotificationPriority.CRITICAL || this == NotificationPriority.HIGH

/**
 * Extension function to convert to push notification priority
 */
fun NotificationPriority.toPushNotificationPriority(): Int {
    return when (this) {
        NotificationPriority.CRITICAL -> android.app.NotificationManager.IMPORTANCE_MAX
        NotificationPriority.HIGH -> android.app.NotificationManager.IMPORTANCE_HIGH
        NotificationPriority.MEDIUM -> android.app.NotificationManager.IMPORTANCE_DEFAULT
        NotificationPriority.LOW -> android.app.NotificationManager.IMPORTANCE_LOW
        NotificationPriority.INFO -> android.app.NotificationManager.IMPORTANCE_MIN
    }
}
