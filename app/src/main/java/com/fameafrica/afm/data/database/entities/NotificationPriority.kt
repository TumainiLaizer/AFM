package com.fameafrica.afm.data.database.entities

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.fameafrica.afm.R

/**
 * Notification Priority Enum
 * Defines the importance level of notifications (1-5)
 * Used for sorting, filtering, and visual differentiation
 */
enum class NotificationPriority(
    val value: Int,
    val level: String,
    val descriptionText: String,
    @param:ColorRes val color: Color,
    @param:DrawableRes val icon: Int,
    val iconChar: String,
    val bgColor: String,
    val textColorHex: String
) {
    INFO(
        value = 1,
        level = "INFO",
        descriptionText = "General information, no urgent action needed",
        color = Color(0xFF0288D1),
        icon = R.drawable.ic_info,
        iconChar = "ℹ️",
        bgColor = "#2196F3",
        textColorHex = "#FFFFFF"
    ),

    LOW(
        value = 2,
        level = "LOW",
        descriptionText = "Low priority - can be addressed later",
        color = Color(0xFF0F5A36),
        icon = R.drawable.ic_arrow_downward,
        iconChar = "🔵",
        bgColor = "#4CAF50",
        textColorHex = "#FFFFFF"
    ),

    MEDIUM(
        value = 3,
        level = "MEDIUM",
        descriptionText = "Medium priority - should be addressed soon",
        color = Color(0xFFFF7A00),
        icon = R.drawable.ic_remove,
        iconChar = "🟡",
        bgColor = "#FFD700",
        textColorHex = "#000000"
    ),

    HIGH(
        value = 4,
        level = "HIGH",
        descriptionText = "High priority - requires attention",
        color = Color(0xFFD4A017),
        icon = R.drawable.ic_arrow_upward,
        iconChar = "🟠",
        bgColor = "#FF8C00",
        textColorHex = "#FFFFFF"
    ),

    CRITICAL(
        value = 5,
        level = "CRITICAL",
        descriptionText = "Critical - immediate action required",
        color = Color(0xFF9E1B1B),
        icon = R.drawable.ic_error,
        iconChar = "🔴",
        bgColor = "#F44336",
        textColorHex = "#FFFFFF"
    );

    companion object {
        /**
         * Get NotificationPriority from integer value
         */
        fun fromValue(value: Int): NotificationPriority {
            return entries.find { it.value == value } ?: MEDIUM
        }

        /**
         * Get priority level description for analytics
         */
        fun getPriorityLevels(): Map<Int, String> {
            return entries.associate { it.value to it.level }
        }

        /**
         * Get color resource for priority level
         */
        fun getColorRes(priority: Int): Color {
            return fromValue(priority).color
        }

        /**
         * Get icon resource for priority level
         */
        fun getIconRes(priority: Int): Int {
            return fromValue(priority).icon
        }

        /**
         * Get display icon emoji for priority level
         */
        fun getDisplayIcon(priority: Int): String {
            return fromValue(priority).iconChar
        }

        /**
         * Get background color hex for priority level
         */
        fun getBackgroundColor(priority: Int): String {
            return fromValue(priority).bgColor
        }

        /**
         * Sort priorities in descending order (highest first)
         */
        fun sortDescending(priorities: List<NotificationPriority>): List<NotificationPriority> {
            return priorities.sortedByDescending { it.value }
        }

        /**
         * Sort priorities in ascending order (lowest first)
         */
        fun sortAscending(priorities: List<NotificationPriority>): List<NotificationPriority> {
            return priorities.sortedBy { it.value }
        }
    }
}

/**
 * Extension functions for NotificationPriority
 */
fun Int.toNotificationPriority(): NotificationPriority = NotificationPriority.fromValue(this)

fun Int.isCriticalPriority(): Boolean = this >= NotificationPriority.HIGH.value

fun Int.isHighPriority(): Boolean = this == NotificationPriority.HIGH.value

fun Int.isMediumPriority(): Boolean = this == NotificationPriority.MEDIUM.value

fun Int.isHighPriorityOnly(): Boolean = this == NotificationPriority.HIGH.value

fun Int.isLowPriority(): Boolean = this == NotificationPriority.LOW.value

fun Int.isInfoPriority(): Boolean = this == NotificationPriority.INFO.value
