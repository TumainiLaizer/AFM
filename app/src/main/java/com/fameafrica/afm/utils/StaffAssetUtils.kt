package com.fameafrica.afm.utils

/**
 * Utility for mapping non-manager coaching staff to their corresponding visual assets.
 */
object StaffAssetUtils {

    /**
     * Returns a deterministic staff face asset path.
     */
    fun getStaffFace(
        staffId: Int,
        role: String = ""
    ): String {
        // Updated pool based on existing assets in assets/staff_faces/
        val pool = generatePaths("staff", 2, 4) + // staff_1_1 to staff_2_4
                   generatePaths("staff1", 3, 4) + // staff1_1_1 to staff1_3_4
                   generatePaths("staff2", 3, 4) + // staff2_1_1 to staff2_3_4
                   generatePaths("staff_3_1", 1, 9) + // staff_3_1_1 to staff_3_1_9
                   generatePaths("staff_3_2", 1, 5)   // staff_3_2_1 to staff_3_2_5

        if (pool.isEmpty()) return "file:///android_asset/staff_faces/default_staff.webp"

        val index = (staffId.hashCode() % pool.size).let { if (it < 0) it + pool.size else it }
        return pool[index]
    }

    private fun generatePaths(prefix: String, range1: Int, range2: Int): List<String> {
        val list = mutableListOf<String>()
        for (i in 1..range1) {
            for (j in 1..range2) {
                list.add("file:///android_asset/staff_faces/${prefix}_${i}_${j}.webp")
            }
        }
        return list
    }
}
