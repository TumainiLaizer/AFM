package com.fameafrica.afm.domain.agent

import kotlin.random.Random

data class AgentDialogueContext(
    val agentPersonality: String,
    val playerName: String,
    val playerHappiness: Int,
    val offerAmount: Long,
    val expectedAmount: Long,
    val isFinalOffer: Boolean = false,
    val isContractNegotiation: Boolean = false,
    val negotiationRounds: Int = 0,
    val isSuperAgent: Boolean = false
)

data class DialogueMemory(
    val lastOffer: Long,
    val lastResponse: String,
    val negotiationRounds: Int
)

object AgentDialogueEngine {

    fun generateDialogue(context: AgentDialogueContext): String {
        // Super Agent Override
        if (context.isSuperAgent && context.negotiationRounds > 0 && Random.nextFloat() < 0.3f) {
            return "Top clubs are already interested. You must act decisively if you want to keep ${context.playerName}."
        }

        // Escalation System
        if (context.negotiationRounds > 3) {
            return when (context.agentPersonality) {
                "AGGRESSIVE" -> "This is going nowhere. We are ending talks."
                "GREEDY" -> "My time is money. This is our final demand. Take it or leave it."
                else -> "We've been talking for a while. Let's reach a conclusion quickly."
            }
        }

        return when (context.agentPersonality) {
            "GREEDY" -> greedyDialogue(context)
            "AGGRESSIVE" -> aggressiveDialogue(context)
            "LOYAL" -> loyalDialogue(context)
            "DEVELOPMENT_FOCUSED" -> developmentDialogue(context)
            else -> balancedDialogue(context)
        }
    }

    private fun greedyDialogue(ctx: AgentDialogueContext): String {
        return when {
            ctx.offerAmount < ctx.expectedAmount * 0.7 ->
                randomOf(
                    "This offer is far below my client's value.",
                    "You must do much better financially.",
                    "We are not even close. Increase the bid substantially."
                )
            ctx.offerAmount < ctx.expectedAmount ->
                randomOf(
                    "We're getting closer, but the numbers still need improvement.",
                    "Add more financial incentives and we can talk.",
                    "This deal lacks ambition financially. Show us the money."
                )
            else ->
                randomOf(
                    "Now we are talking. This reflects my client's worth.",
                    "This is a respectable offer. We can proceed with these numbers.",
                    "Good. This meets our financial expectations."
                )
        }
    }

    private fun aggressiveDialogue(ctx: AgentDialogueContext): String {
        return when {
            ctx.playerHappiness < 50 ->
                randomOf(
                    "My client is unhappy. We expect immediate action.",
                    "You are risking losing the player. Fix this offer now.",
                    "We won’t tolerate this situation much longer."
                )
            ctx.offerAmount < ctx.expectedAmount ->
                randomOf(
                    "Other clubs are already offering better deals.",
                    "If you hesitate, you lose the player. Simple as that.",
                    "We have options. Don’t waste our time with minor increases."
                )
            else ->
                randomOf(
                    "Fine. Let’s move quickly before others interfere.",
                    "Acceptable. Let’s finalize immediately.",
                    "We agree, but don’t delay the paperwork."
                )
        }
    }

    private fun loyalDialogue(ctx: AgentDialogueContext): String {
        return when {
            ctx.playerHappiness < 50 ->
                randomOf(
                    "We prefer stability, but the player is concerned about his future.",
                    "Let’s resolve this respectfully for both sides.",
                    "We value this relationship, but improvements are needed to keep him happy."
                )
            ctx.offerAmount < ctx.expectedAmount ->
                randomOf(
                    "This is a fair start. Perhaps we can improve slightly?",
                    "We are open to discussion. Let's find a middle ground.",
                    "Let’s work together to find a deal that fits everyone."
                )
            else ->
                randomOf(
                    "This is a fair and respectful offer. We appreciate it.",
                    "We are happy to proceed. It's good to feel valued.",
                    "This works well for everyone. Let's sign."
                )
        }
    }

    private fun developmentDialogue(ctx: AgentDialogueContext): String {
        return when {
            ctx.isContractNegotiation ->
                randomOf(
                    "My client needs guaranteed playing time above all else.",
                    "Development is the priority here. We want to see a clear path.",
                    "We want a clear growth plan, including individual training focus."
                )
            ctx.offerAmount < ctx.expectedAmount ->
                randomOf(
                    "Financials matter, but his career development matters more.",
                    "Where will my client fit in your tactical system?",
                    "We need assurances about his role in the squad."
                )
            else ->
                randomOf(
                    "This looks promising for his career trajectory.",
                    "We like the direction of this deal. It's a good step forward.",
                    "Good move for his development. We are satisfied."
                )
        }
    }

    private fun balancedDialogue(ctx: AgentDialogueContext): String {
        return when {
            ctx.offerAmount < ctx.expectedAmount ->
                randomOf(
                    "We need a slightly better offer to close this.",
                    "This is close, but not enough yet to convince us.",
                    "Let’s improve the terms a little more."
                )
            else ->
                randomOf(
                    "This looks acceptable to us.",
                    "We can proceed with this offer.",
                    "Agreed. Let's finalize."
                )
        }
    }

    private fun randomOf(vararg lines: String): String = lines.random()
}
