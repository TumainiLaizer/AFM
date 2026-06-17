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
            FootballRegion.NORTH_AFRICA -> "north_africa.webp"
            FootballRegion.WEST_AFRICA -> "west_africa.webp"
            FootballRegion.CENTRAL_AFRICA -> "central_africa.webp"
            FootballRegion.SOUTHERN_AFRICA -> "southern_africa.webp"
            else -> "east_africa.webp"
        }

        return "file:///android_asset/chairman_faces/$filename"
    }
}
