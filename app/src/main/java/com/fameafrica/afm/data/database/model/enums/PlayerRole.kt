package com.fameafrica.afm.data.database.model.enums

enum class PlayerRole(val displayName: String) {
    INSIDE_FORWARD("Inside Forward"),
    SHADOW_STRIKER("Shadow Striker"),
    DEEP_LYING_PLAYMAKER("Deep Lying Playmaker"),
    ADVANCED_PLAYMAKER("Advanced Playmaker"),
    CENTRAL_DEFENDER("Central Defender"),
    NO_NONSENSE_DEFENDER("No-Nonsense Defender"),
    DEEP_LYING_FORWARD("Deep Lying Forward"),
    TARGET_MAN("Target Man"),
    BOX_TO_BOX("Box-to-Box"),
    CARRILERO("Carrilero"),
    SWEEPER_KEEPER("Sweeper Keeper"),
    GOALKEEPER("Goalkeeper"),
    POACHER("Poacher"),
    WINGER("Winger"),
    ANCHOR_MAN("Anchor Man"),
    DEFENSIVE_MIDFIELDER("Defensive Midfielder"),
    REGISTA("Regista"),
    LIBERO("Libero"),
    BALL_PLAYING_DEFENDER("Ball Playing Defender"),
    ENGANCHE("Enganche"),
    ADVANCED_FORWARD("Advanced Forward"),
    PRESSING_FORWARD("Pressing Forward"),
    FALSE_9("False 9"),
    BALL_WINNING_MIDFIELDER("Ball Winning Midfielder"),
    WINGBACK("Wingback");

    companion object {
        fun fromString(value: String): PlayerRole? {
            return entries.find { it.name == value || it.displayName == value }
        }
    }
}
