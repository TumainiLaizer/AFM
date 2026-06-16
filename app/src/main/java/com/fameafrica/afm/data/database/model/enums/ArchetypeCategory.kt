package com.fameafrica.afm.data.database.model.enums

enum class PlayerTrait(val value: String) {
    BOLD("BOLD"),
    CONFIDENT("CONFIDENT"),
    CREATIVE("CREATIVE"),
    FORTHRIGHT("FORTHRIGHT"),
    ADAPTABLE("ADAPTABLE"),
    TEAM_ORIENTED("TEAM_ORIENTED"),
    DISCIPLINED("DISCIPLINED"),
    RESILIENT("RESILIENT"),
    COMPOSED("COMPOSED"),
    EMOTIONALLY_STABLE("EMOTIONALLY_STABLE"),
    SOCIALLY_WARM("SOCIALLY_WARM"),
    VERSATILE("VERSATILE"),
    INTELLIGENT("INTELLIGENT"),
    ENERGETIC("ENERGETIC"),
    DETERMINED("DETERMINED"),
    BRAVE("BRAVE"),
    DECISIVE("DECISIVE");

    companion object {
        fun fromString(value: String): PlayerTrait? {
            return entries.find { it.value == value }
        }
    }
}
