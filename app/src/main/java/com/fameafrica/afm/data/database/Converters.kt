package com.fameafrica.afm.data.database

import androidx.room.TypeConverter
import kotlinx.serialization.json.Json
import kotlinx.serialization.SerializationException
import java.util.Date

class Converters {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
    }

    // ============ STRING LIST CONVERTERS ============
    // For achievements, notes, etc.

    @TypeConverter
    fun fromStringList(value: String?): List<String>? {
        if (value.isNullOrBlank()) return emptyList()

        return try {
            // First try JSON parsing (for compatibility with newer data)
            json.decodeFromString<List<String>>(value)
        } catch (e: SerializationException) {
            // Fallback to comma-separated format (for legacy data)
            value.split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun toStringList(list: List<String>?): String? {
        if (list.isNullOrEmpty()) return "[]"

        return try {
            // Store as JSON for better compatibility with SQLite JSON functions
            json.encodeToString(list)
        } catch (e: Exception) {
            // Fallback to comma-separated if JSON fails, 
            // but return empty array if it was null/empty to satisfy triggers
            "[]"
        }
    }

    // ============ INT LIST CONVERTERS ============
    // For storing lists of IDs, positions, etc.

    @TypeConverter
    fun fromIntList(value: String?): List<Int>? {
        if (value.isNullOrBlank()) return emptyList()

        return try {
            // Try JSON parsing first
            json.decodeFromString<List<Int>>(value)
        } catch (e: SerializationException) {
            // Fallback to comma-separated
            value.split(",")
                .mapNotNull { it.trim().toIntOrNull() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun toIntList(list: List<Int>?): String? {
        if (list.isNullOrEmpty()) return "[]"

        return try {
            json.encodeToString(list)
        } catch (e: Exception) {
            "[]"
        }
    }

    // ============ LONG LIST CONVERTERS ============

    @TypeConverter
    fun fromLongList(value: String?): List<Long>? {
        if (value.isNullOrBlank()) return emptyList()
        return try {
            json.decodeFromString<List<Long>>(value)
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun toLongList(list: List<Long>?): String? {
        if (list.isNullOrEmpty()) return "[]"
        return try {
            json.encodeToString(list)
        } catch (e: Exception) {
            "[]"
        }
    }

    // ============ GENERIC JSON CONVERTERS ============
    // For storing any Map as JSON

    @TypeConverter
    fun fromJson(value: String?): Map<String, String>? {
        if (value.isNullOrBlank()) return emptyMap()

        return try {
            json.decodeFromString<Map<String, String>>(value)
        } catch (e: Exception) {
            emptyMap()
        }
    }

    @TypeConverter
    fun toJson(map: Map<String, String>?): String? {
        if (map.isNullOrEmpty()) return "{}"

        return try {
            json.encodeToString(map)
        } catch (e: Exception) {
            "{}"
        }
    }

    // ============ BOOLEAN CONVERTERS ============
    // SQLite stores booleans as 0/1

    @TypeConverter
    fun fromBoolean(value: Int?): Boolean = value == 1

    @TypeConverter
    fun toBoolean(value: Boolean?): Int = if (value == true) 1 else 0

    // ============ TIMESTAMP CONVERTERS ============
    // Using Long for milliseconds

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}
