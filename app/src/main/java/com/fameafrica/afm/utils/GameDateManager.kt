package com.fameafrica.afm.utils

import java.util.*
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameDateManager @Inject constructor() {

    companion object {
        const val START_YEAR = 2025
        const val START_MONTH = Calendar.JUNE
        const val START_DAY = 1

        private val DATE_FORMATTER = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        private val DB_FORMATTER = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        private val TIME_FORMATTER = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        val START_DATE: Date by lazy {
            Calendar.getInstance().apply {
                set(START_YEAR, START_MONTH, START_DAY, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }.time
        }
    }

    data class GameDate(
        val year: Int,
        val month: Int,
        val day: Int,
        val weekOfSeason: Int
    ) {
        fun toDisplayString(): String {
            val monthNames = listOf(
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
            )
            return "${day} ${monthNames[month - 1]} ${year}"
        }

        fun toDbDate(): String = String.format(Locale.US, "%04d-%02d-%02d", year, month, day)
    }

    /**
     * Get the exact date for a given day of the career
     */
    fun getGameDate(day: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.time = START_DATE
        calendar.add(Calendar.DAY_OF_YEAR, day - 1)
        return calendar.time
    }

    /**
     * Get the day index from a date string (YYYY-MM-DD)
     */
    fun getDayIndexFromDate(dateStr: String): Int {
        return try {
            val date = DB_FORMATTER.parse(dateStr) ?: return 1
            val diffInMillis = date.time - START_DATE.time
            TimeUnit.MILLISECONDS.toDays(diffInMillis).toInt() + 1
        } catch (e: Exception) {
            1
        }
    }
    fun getGameDateModel(week: Int): GameDate {
        val calendar = Calendar.getInstance()
        calendar.time = getGameDate(week)
        return GameDate(
            year = calendar.get(Calendar.YEAR),
            month = calendar.get(Calendar.MONTH) + 1,
            day = calendar.get(Calendar.DAY_OF_MONTH),
            weekOfSeason = week
        )
    }

    fun formatGameDate(week: Int): String = DATE_FORMATTER.format(getGameDate(week))

    fun formatGameDateForDb(week: Int): String = DB_FORMATTER.format(getGameDate(week))

    fun formatGameDateTime(week: Int): String = TIME_FORMATTER.format(getGameDate(week))

    /**
     * Get season string (e.g., "2025/26")
     */
    fun getSeasonString(week: Int): String {
        val calendar = Calendar.getInstance()
        calendar.time = getGameDate(week)
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)

        // Season transition in June
        return if (month >= Calendar.JUNE) {
            "$year/${(year + 1) % 100}"
        } else {
            "${year - 1}/$year"
        }
    }

    /**
     * Get the year for a given season string
     */
    fun getSeasonYear(season: String): Int = season.split("/").first().toInt()

    /**
     * Get the number of days since the start of the season
     */
    fun getDaysSinceStart(week: Int): Long {
        val diffInMillis = getGameDate(week).time - START_DATE.time
        return TimeUnit.MILLISECONDS.toDays(diffInMillis)
    }

    /**
     * Calculate age from birth year using current game date
     */
    fun calculateAgeFromBirthYear(birthYear: Int, currentWeek: Int): Int {
        val calendar = Calendar.getInstance()
        calendar.time = getGameDate(currentWeek)
        val currentYear = calendar.get(Calendar.YEAR)
        return currentYear - birthYear
    }
}