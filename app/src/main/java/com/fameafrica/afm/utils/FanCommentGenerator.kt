package com.fameafrica.afm.utils

import kotlin.random.Random

object FanCommentGenerator {

    private val positiveMatchComments = listOf(
        "What a performance! That's why I love this club.",
        "Absolute masterclass today. We're going places!",
        "Three points in the bag. Keep 'em coming!",
        "Brilliant win! The atmosphere was incredible.",
        "Total dominance from start to finish. Proud fan today."
    )

    private val negativeMatchComments = listOf(
        "Embarrassing. The manager needs to take responsibility.",
        "I've seen enough. Something needs to change.",
        "Waste of time and money. No passion on that pitch.",
        "Shocking performance. We're a laughing stock right now.",
        "How much longer do we have to endure this?"
    )

    private val neutralMatchComments = listOf(
        "A point is a point, but we should have won that.",
        "Average game. Not much to shout about.",
        "Frustrating result. We need to be more clinical.",
        "Steady performance, but we need more creativity.",
        "It was okay, but we've played better."
    )

    private val transferInComments = listOf(
        "Finally a serious signing! This guy is a beast.",
        "Great addition to the squad. Exactly what we needed.",
        "Interesting signing. Let's see if he can handle the pressure.",
        "Welcome to the best club in Africa! Show them what you've got.",
        "Solid business. The board is actually listening to us for once."
    )

    private val transferOutComments = listOf(
        "Can't believe we're selling him. This is a massive mistake.",
        "End of an era. Thanks for everything, legend.",
        "Good riddance. He was never good enough for this shirt.",
        "We've been robbed. That fee is way too low.",
        "Hard to see him go, but I guess it's part of the game."
    )

    fun generateMatchComment(sentiment: String, playerName: String? = null): String {
        val base = when(sentiment.uppercase()) {
            "POSITIVE" -> positiveMatchComments.random()
            "NEGATIVE" -> negativeMatchComments.random()
            else -> neutralMatchComments.random()
        }
        
        return if (playerName != null && Random.nextBoolean()) {
            val playerHighlight = listOf(
                "$playerName was unreal today!",
                "Build the statue for $playerName already.",
                "Imagine if we didn't have $playerName in the team...",
                "Not happy with $playerName's performance at all.",
                "Is it just me or is $playerName looking a bit off lately?"
            ).random()
            "$base $playerHighlight"
        } else base
    }

    fun generateTransferComment(isIncoming: Boolean, playerName: String): String {
        val base = if (isIncoming) transferInComments.random() else transferOutComments.random()
        return "$base #$playerName #AFM2026"
    }
}
