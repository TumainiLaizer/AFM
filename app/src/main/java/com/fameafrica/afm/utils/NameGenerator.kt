package com.fameafrica.afm.utils

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Optimized Name Generator for AFM2026.
 * Supports on-demand loading of national name databases to minimize memory footprint.
 * Transitions from a single large CSV to individual country name files.
 */
@Singleton
class NameGenerator @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val nationCache = mutableMapOf<String, NationNameData>()

    data class NationNameData(
        val firstNames: List<String>,
        val lastNames: List<String>
    )

    /**
     * Generates a full name for a player/staff based on their nationality.
     * @param nationality The country name (e.g., "Tanzania")
     * @return A randomly generated full name (e.g., "Mbwana Samatta")
     */
    fun generateName(nationality: String): String {
        val data = getNationData(nationality) ?: getNationData("Tanzania") 
        
        if (data == null) {
            // Absolute fallback if no data found
            val fallbackLast = listOf("John", "Mohamed", "Mbwana", "Djené", "Mensah", "Traore", "Kamara", "Osei")
            return "${('A'..'Z').random()}. ${fallbackLast.random()}"
        }

        val firstName = if (data.firstNames.isNotEmpty()) data.firstNames.random() else "A."
        val lastName = if (data.lastNames.isNotEmpty()) data.lastNames.random() else "Player"
        
        return "$firstName $lastName"
    }

    private fun getNationData(nationality: String): NationNameData? {
        val normalizedNation = nationality.trim()
        if (normalizedNation.isEmpty()) return null
        
        // 1. Check Memory Cache
        nationCache[normalizedNation]?.let { return it }
        
        // 2. Try to load from optimized split files (Lower RAM usage)
        val splitData = loadFromSplitFiles(normalizedNation)
        if (splitData != null) {
            nationCache[normalizedNation] = splitData
            return splitData
        }
        
        // 3. Fallback to legacy single file (Migration phase)
        val legacyData = loadFromLegacyFile(normalizedNation)
        if (legacyData != null) {
            nationCache[normalizedNation] = legacyData
            return legacyData
        }
        
        return null
    }

    private fun loadFromSplitFiles(nationality: String): NationNameData? {
        try {
            val nationKey = nationality.lowercase().replace(" ", "_")
            val firstNamesPath = "databases/names/first_names_$nationKey.csv"
            val lastNamesPath = "databases/names/surnames_$nationKey.csv"
            
            val firsts = readNamesFromFile(firstNamesPath)
            val lasts = readNamesFromFile(lastNamesPath)
            
            if (firsts.isNotEmpty() || lasts.isNotEmpty()) {
                return NationNameData(firsts, lasts)
            }
        } catch (e: Exception) {
            // Expected if files don't exist yet
        }
        return null
    }

    private fun readNamesFromFile(path: String): List<String> {
        return try {
            val inputStream = context.assets.open(path)
            val reader = BufferedReader(InputStreamReader(inputStream))
            val line = reader.readLine() ?: ""
            line.splitTabOrComma()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun loadFromLegacyFile(nationality: String): NationNameData? {
        return try {
            val inputStream = context.assets.open("databases/nation_names.csv")
            val reader = BufferedReader(InputStreamReader(inputStream))
            
            var line: String? = reader.readLine()
            while (line != null) {
                if (line.trim().equals(nationality, ignoreCase = true)) {
                    val firsts = reader.readLine()?.splitTabOrComma() ?: emptyList()
                    val lasts = reader.readLine()?.splitTabOrComma() ?: emptyList()
                    
                    return NationNameData(firsts, lasts)
                }
                line = reader.readLine()
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    private fun String.splitTabOrComma(): List<String> {
        return this.split(Regex("[\t,]"))
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }
}
