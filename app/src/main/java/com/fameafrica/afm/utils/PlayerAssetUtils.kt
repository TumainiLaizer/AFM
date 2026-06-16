package com.fameafrica.afm.utils

import com.fameafrica.afm.data.database.entities.PlayersEntity
import java.util.Locale

/**
 * Utility for mapping players to their corresponding visual assets (faces, region cards).
 */
object PlayerAssetUtils {

    /**
     * Returns a deterministic player face asset path based on team region and playerId.
     * Ensures persistence across all screens by ignoring volatile club context for the selection.
     */
    fun getPlayerFace(
        player: PlayersEntity,
        clubColors: List<String> = emptyList() // Kept for compatibility but ignored for persistence
    ): String {
        if (!player.faceImage.isNullOrEmpty()) return player.faceImage
        return getPlayerFace(player.id, player.nationality)
    }

    /**
     * Core deterministic face selection logic.
     * Persistence is guaranteed because it only relies on stable player data (ID and Nationality).
     */
    fun getPlayerFace(
        playerId: Int,
        nationality: String,
        clubName: String = "", // Kept for compatibility
        clubColors: List<String> = emptyList() // Kept for compatibility
    ): String {
        val playerRegion = NationalityUtils.getNationalityItem(nationality)?.region ?: FootballRegion.EAST_AFRICA
        val faceRegion = if (playerRegion == FootballRegion.EUROPE) FootballRegion.NORTH_AFRICA else playerRegion
        
        // Use playerId to pick a stable "theme color" for this player.
        // This ensures the face is persistent even if the player moves clubs.
        val stableColors = listOf("blue", "green", "red", "white", "yellow", "black")
        val colorName = stableColors[kotlin.math.abs(playerId) % stableColors.size]

        // Build a pool based on the region and the assigned stable color.
        val pool = getSecondaryPool(faceRegion, colorName)
        val fallbackPool = getFallbackPool(faceRegion)
        
        val fullPool = (pool + fallbackPool).distinct()

        if (fullPool.isEmpty()) {
            return "file:///android_asset/player_faces/OIG2_1_1.jpg" // Ultra-fallback
        }

        // Deterministic selection from the stable pool
        val index = (kotlin.math.abs(playerId) % fullPool.size)
        return fullPool[index]
    }

    private fun generatePaths(prefix: String, range1: Int, range2: Int): List<String> {
        val list = mutableListOf<String>()
        for (i in 1..range1) {
            for (j in 1..range2) {
                // Add the standard pattern which is the most reliable
                list.add("file:///android_asset/player_faces/${prefix}_${i}_${j}.jpg")
                
                // Only add _001 for specific high-density prefixes where they are known to exist
                if (prefix.startsWith("OIG4") && i == 1) {
                    list.add("file:///android_asset/player_faces/${prefix}_${i}_${j}_001.jpg")
                }
            }
        }
        return list
    }

    private fun getSecondaryPool(region: FootballRegion, color: String): List<String> {
        return when (region) {
            FootballRegion.NORTH_AFRICA -> when (color) {
                "red" -> generatePaths("OIG1_redwhite_northafrica_squad", 2, 4) +
                        generatePaths("OIG1_redwhite_northafrica_squad2", 1, 4)
                "white" -> generatePaths("OIG2_white_northafrica_squad", 2, 4) +
                          generatePaths("OIG3_whiteblue_northafrica_squad", 2, 4)
                "yellow" -> generatePaths("OIG2_yellow_northafrica_squad", 2, 4) +
                           generatePaths("OIG4_yellow_northafrica_squad", 2, 4)
                "black" -> generatePaths("OIG2_blackyellow_northafrica_squad", 2, 4)
                else -> generatePaths("OIG2_white_northafrica_squad", 2, 4)
            }
            FootballRegion.WEST_AFRICA -> when (color) {
                "blue" -> generatePaths("OIG1_blue_westafrica_squad", 2, 4) +
                         generatePaths("OIG1_blue_westafrica_squad2", 1, 4)
                "green" -> generatePaths("OIG4_green_westafrica_squad", 2, 3) +
                          generatePaths("OIG4_green_westafrica_squad1", 2, 4)
                "white" -> generatePaths("OIG3_white_westafrica_squad", 2, 4) +
                          generatePaths("OIG1_whitegold_westafrica_squad", 2, 4)
                else -> generatePaths("OIG1_blue_westafrica_squad", 2, 4)
            }
            FootballRegion.CENTRAL_AFRICA -> when (color) {
                "blue" -> generatePaths("OIG3_bluewhite_centralafrica_squad", 2, 4) +
                         generatePaths("OIG1_yellowblue_centralafrica_squad", 2, 4)
                "red" -> generatePaths("OIG3_redwhite_centralafrica_squad", 2, 4)
                "green" -> generatePaths("OIG2_greenwhite_centralafrica_squad", 2, 4)
                else -> generatePaths("OIG3_bluewhite_centralafrica_squad", 2, 4)
            }
            FootballRegion.SOUTHERN_AFRICA -> {
                generatePaths("OIG1_southafrica_squad", 2, 3) +
                generatePaths("OIG1_yellowgreen_southafrica_squad", 2, 4)
            }
            else -> when (color) { // EAST_AFRICA
                "blue" -> generatePaths("OIG1_blue_eastafrica_squad", 2, 4) +
                         generatePaths("OIG2_blue_eastafrica_squad", 2, 4)
                "green" -> generatePaths("OIG1_green_eastafrica_squad", 2, 4)
                "gold" -> generatePaths("OIG1_gold_eastafrica_squad", 2, 4) +
                         generatePaths("OIG1_gold_eastafrica_squad6", 1, 4)
                "white" -> generatePaths("OIG3_white_eastafrica_squad", 2, 4)
                "black" -> generatePaths("OIG4_black_eastafrica_squad", 2, 4)
                "purple" -> generatePaths("OIG1_purple_eastafrica_squad", 2, 3) +
                           generatePaths("OIG2_purple_eastafrica_squad", 2, 4)
                "red" -> generatePaths("OIG1_redwhite_eastafrica_squad", 2, 4)
                else -> generatePaths("OIG1_blue_eastafrica_squad", 2, 4)
            }
        }
    }

    private fun getFallbackPool(region: FootballRegion): List<String> {
        return when (region) {
            FootballRegion.NORTH_AFRICA -> generatePaths("OIG2_white_northafrica_squad", 1, 4)
            FootballRegion.WEST_AFRICA -> generatePaths("OIG1_blue_westafrica_squad", 1, 4)
            FootballRegion.CENTRAL_AFRICA -> generatePaths("OIG3_bluewhite_centralafrica_squad", 1, 4)
            FootballRegion.SOUTHERN_AFRICA -> generatePaths("OIG1_southafrica_squad", 1, 4)
            else -> generatePaths("OIG1_blue_eastafrica_squad", 1, 4)
        }
    }

    /**
     * Returns the 48x48 region-based squad card background.
     */
    fun getRegionCardBackground(nationality: String): String {
        val region = NationalityUtils.getNationalityItem(nationality)?.region ?: FootballRegion.EAST_AFRICA
        return "file:///android_asset/maps/${region.name.lowercase(Locale.ROOT)}.png"
    }
}
