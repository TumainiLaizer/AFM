package com.fameafrica.afm.utils

/**
 * Utility for mapping managers to their corresponding visual assets.
 */
object ManagerAssetUtils {

    /**
     * Returns a deterministic manager face asset path based on region.
     */
    fun getManagerFace(
        managerId: Int,
        nationality: String
    ): String {
        val region = NationalityUtils.getNationalityItem(nationality)?.region ?: FootballRegion.EAST_AFRICA
        
        val regionKey = region.name
        val prefix = when(regionKey) {
            "SOUTHERN_AFRICA" -> "south_africa"
            else -> regionKey.lowercase()
        }
        
        val (r1, r2) = when(regionKey) {
            "NORTH_AFRICA" -> 4 to 4
            "WEST_AFRICA" -> 5 to 4
            "CENTRAL_AFRICA" -> 3 to 4
            "SOUTHERN_AFRICA" -> 4 to 4
            else -> 6 to 4 // EAST
        }

        val pool = mutableListOf<String>()
        // Pattern 1: prefix_i_j.webp
        for (i in 1..r1) {
            for (j in 1..r2) {
                pool.add("file:///android_asset/manager_faces/${prefix}_${i}_${j}.webp")
            }
        }
        // Pattern 2: prefixX_Y_Z.webp
        for (i in 1..r1) {
            for (j in 1..r2) {
                pool.add("file:///android_asset/manager_faces/${prefix}${i}_1_${j}.webp")
            }
        }
        
        val finalPool = pool.distinct()

        if (finalPool.isEmpty()) return "file:///android_asset/manager_faces/coach_male_official.webp"

        val index = (managerId.hashCode() % finalPool.size).let { if (it < 0) it + finalPool.size else it }
        return finalPool[index]
    }

}