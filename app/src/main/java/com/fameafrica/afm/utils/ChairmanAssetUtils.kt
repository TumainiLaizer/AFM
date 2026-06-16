package com.fameafrica.afm.utils

/**
 * Utility for mapping chairmen to their corresponding visual assets.
 */
object ChairmanAssetUtils {

    /**
     * Returns a deterministic chairman face asset path based on region.
     */
    fun getChairmanFace(
        chairmanId: Int,
        nationality: String
    ): String {
        val region = NationalityUtils.getNationalityItem(nationality)?.region ?: FootballRegion.EAST_AFRICA
        
        val filename = when (region) {
            FootballRegion.NORTH_AFRICA -> "north_africa.jpg"
            FootballRegion.WEST_AFRICA -> "west_africa.jpg"
            FootballRegion.CENTRAL_AFRICA -> "central_africa.jpg"
            FootballRegion.SOUTHERN_AFRICA -> "southern_africa.jpg"
            else -> "east_africa.jpg"
        }

        return "file:///android_asset/chairman_faces/$filename"
    }
}
