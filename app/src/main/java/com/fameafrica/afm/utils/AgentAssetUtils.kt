package com.fameafrica.afm.utils

/**
 * Utility for mapping agents to their corresponding visual assets.
 */
object AgentAssetUtils {

    /**
     * Returns a deterministic agent face asset path.
     */
    fun getAgentFace(
        agentId: Int,
        nationality: String
    ): String {
        val region = NationalityUtils.getNationalityItem(nationality)?.region ?: FootballRegion.EAST_AFRICA
        
        // pool of agent images
        val pool = when (region) {
            FootballRegion.NORTH_AFRICA -> generatePaths("northafrica", 1, 3) + generatePaths("Prof_2", 2, 4)
            FootballRegion.WEST_AFRICA -> listOf("file:///android_asset/agent_faces/westafrica_3_3.webp") + generatePaths("OIG1", 3, 6)
            else -> generatePaths("Prof", 5, 4) + generatePaths("OIG4", 3, 6)
        }

        if (pool.isEmpty()) return "file:///android_asset/agent_faces/northafrica_5_4_4.webp"

        val index = (agentId.hashCode() % pool.size).let { if (it < 0) it + pool.size else it }
        return pool[index]
    }

    private fun generatePaths(prefix: String, range1: Int, range2: Int): List<String> {
        val list = mutableListOf<String>()
        for (i in 1..range1) {
            for (j in 1..range2) {
                // Adjusting for various naming patterns seen in agent_faces
                list.add("file:///android_asset/agent_faces/${prefix}_${i}_${j}.webp")
                list.add("file:///android_asset/agent_faces/${prefix}__${i}_${j}.webp")
                list.add("file:///android_asset/agent_faces/${prefix}_${i}_1_${j}.webp")
            }
        }
        return list.distinct()
    }
}
