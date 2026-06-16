package com.fameafrica.afm.utils.commentary

import com.fameafrica.afm.data.database.entities.*

/**
 * Advanced African Football Commentary Generator
 *
 * Provides realistic, culturally-authentic commentary for African football matches
 * with context-aware reactions to events, crowd atmosphere, and tactical analysis.
 */
object AfricanFootballCommentaryGenerator {

    private val usedCommentary = mutableSetOf<Int>()

    private val africanExclamations = listOf(
        "Eeeeeh!", "Woiye!", "Mama mia!", "Yebo!", "Ayeega!", "Eish!", "Haiya!",
        "Azza!", "Twe!", "Agoo!", "Ehee!", "Oya!", "Kai!", "Nkosi!", "Mambo!", "Sawa!"
    )

    fun resetUsedCommentary() {
        usedCommentary.clear()
    }

    private fun getVariatedLine(pool: List<String>): String {
        val available = pool.filter { it.hashCode() !in usedCommentary }
        val selected = if (available.isNotEmpty()) available.random() else pool.random()
        usedCommentary.add(selected.hashCode())
        return selected
    }

    // ============ COMMENTARY GENERATION FROM EVENTS ============

    fun generateCommentaryFromEvent(
        event: MatchEventsEntity,
        homeTeam: String,
        awayTeam: String,
        homeManager: ManagersEntity?,
        awayManager: ManagersEntity?,
        referee: RefereesEntity?
    ): String {
        val isHomeEvent = event.teamName == homeTeam
        val teamName = event.teamName
        val opponent = event.opponentTeam ?: (if (isHomeEvent) awayTeam else homeTeam)
        val playerName = event.playerName
        val managerName = if (isHomeEvent) homeManager?.name ?: "The home manager"
        else awayManager?.name ?: "The away manager"
        val refereeName = referee?.name ?: "The referee"

        val exclamation = africanExclamations.random()

        return when (event.eventType) {
            "GOAL" -> generateGoalCommentary(playerName, teamName, event.homeScore, event.awayScore, event.shotType, exclamation)
            "PENALTY_SCORED" -> generatePenaltyScoredCommentary(playerName, teamName, exclamation)
            "PENALTY_MISSED" -> generatePenaltyMissedCommentary(playerName, teamName, event.penaltySaved, event.penaltyPost, exclamation)
            "OWN_GOAL" -> generateOwnGoalCommentary(playerName, teamName, exclamation)
            "YELLOW_CARD" -> generateYellowCardCommentary(playerName, teamName, exclamation)
            "RED_CARD" -> generateRedCardCommentary(playerName, teamName, managerName, exclamation)
            "SUBSTITUTION" -> generateSubstitutionCommentary(event.substitutionInPlayer, event.substitutionOutPlayer, teamName, managerName)
            "INJURY" -> generateInjuryCommentary(playerName, teamName, event.injuryType, managerName, exclamation)
            "VAR" -> generateVarCommentary(event.description, event.varOverturned)
            "SAVE" -> generateSaveCommentary(playerName, teamName, isHomeEvent, exclamation)
            "CORNER" -> generateCornerCommentary(playerName, teamName)
            "FOUL" -> generateFoulCommentary(playerName, teamName, opponent, refereeName, exclamation)
            "OFFSIDE" -> generateOffsideCommentary(playerName, teamName, exclamation)
            else -> event.description ?: "${event.displayMinute}' - ${event.eventType} for $teamName"
        }
    }

    // ============ SPECIALIZED FLAVOR COMMENTARY ============

    fun generateManagerCommentary(managerName: String, action: String): String {
        val pool = when(action) {
            "ANGRY" -> listOf("$managerName is EXPLODING on the touchline!", "The fourth official is having a hard time with $managerName!", "$managerName looks ready to jump onto the pitch himself!")
            "TACTICAL" -> listOf("$managerName is deep in thought, adjusting his notes.", "A flurry of instructions from $managerName to his captain.", "$managerName signals a tactical shift. He's not happy with the current shape.")
            else -> listOf("$managerName watches on calmly as the match unfolds.", "$managerName is applauding his team's effort from the technical area.")
        }
        return getVariatedLine(pool)
    }

    fun generateRefereeCommentary(refName: String, context: String): String {
        val pool = when(context) {
            "STRICT" -> listOf("$refName is taking no nonsense today!", "Another lecture from $refName. The players need to calm down.", "The whistle is working overtime in the hands of $refName.")
            "DRAMA" -> listOf("The players are surrounding $refName! High tension!", "$refName points to his head, urging for calm.", "A controversial call from $refName! The stadium is in uproar!")
            else -> listOf("$refName is letting the game flow nicely.", "A confident performance from the man in the middle, $refName.")
        }
        return getVariatedLine(pool)
    }

    fun generateFanReactionCommentary(reaction: String, team: String): String {
        val pool = when(reaction) {
            "CHEERING" -> listOf("The $team supporters are making an UNREAL noise!", "The vuvuzelas are deafening! Pure joy for $team!", "Absolute CARNAGE in the $team end! They're loving it!")
            "BOOING" -> listOf("A chorus of boos rings out from the stands.", "The fans are making their frustration CLEAR.", "Tension rising in the terraces as the performance dips.")
            else -> listOf("The drums are beating a steady rhythm for $team.", "A wave of colorful banners fills the stadium.")
        }
        return getVariatedLine(pool)
    }

    fun generateCelebrationCommentary(player: String, team: String): String {
        val pool = listOf(
            "$player is off to the corner flag! WHAT A DANCE!",
            "The whole $team bench has joined the celebration!",
            "$player points to the name on his back. A hero for $team today!",
            "A backflip celebration from $player! The athleticism is incredible!"
        )
        return getVariatedLine(pool)
    }

    fun generateControversyCommentary(desc: String): String {
        val pool = listOf(
            "That's going to be debated in every cafe tomorrow!",
            "A HUGE moment of controversy! Was that really the right call?",
            "The replay might tell a different story. High drama!",
            "Tensions boiling over after that incident. This is African football at its fiercest!"
        )
        return getVariatedLine(pool)
    }

    fun generateStatisticCommentary(stat: String, value: String, team: String): String {
        return "DATA DASHBOARD: $team currently leading with $value $stat. Efficiency is the name of the game."
    }

    // ============ GOAL & EVENT LOGIC ============

    private fun generateGoalCommentary(player: String, team: String, h: Int?, a: Int?, type: String?, excl: String): String {
        val score = if (h != null && a != null) "[$h-$a]" else ""
        val typeText = when (type) {
            "HEADER" -> "with a CRUSHING header"
            "VOLLEY" -> "with a THUNDEROUS volley"
            "FREE_KICK" -> "with a MAGICAL free-kick"
            else -> "with a CLINICAL finish"
        }
        val pool = listOf(
            "GOOOOOAAAAAL! $excl $player finds the net $typeText for $team $score!",
            "UNBELIEVABLE! $player has BURIED it! $team lead $score! $excl",
            "THE STADIUM SHAKES! $player with a MASTERCLASS goal $score! $excl"
        )
        return getVariatedLine(pool)
    }

    private fun generatePenaltyScoredCommentary(player: String, team: String, excl: String): String {
        val pool = listOf(
            "$excl $player STEPS UP... AND SCORES! Absolute composure from the spot for $team.",
            "GOAL! $player sends the keeper to the market and slots it home!",
            "NO MISTAKE! $player drills the penalty into the bottom corner. Clinical!"
        )
        return getVariatedLine(pool)
    }

    private fun generatePenaltyMissedCommentary(player: String, team: String, saved: Boolean, post: Boolean, excl: String): String {
        val outcome = when {
            saved -> "THE KEEPER SAVES IT! Incredible reflexes!"
            post -> "IT RATTLES THE WOODWORK! Heartbreak for $player!"
            else -> "HE'S BLAZED IT WIDE! $excl"
        }
        val pool = listOf(
            "DRAMA! $player MISSES the penalty! $outcome",
            "OH NO! $player has missed a golden opportunity for $team! $outcome"
        )
        return getVariatedLine(pool)
    }

    private fun generateOwnGoalCommentary(player: String, team: String, excl: String): String {
        return "DISASTER! $player puts it into his own net! $excl OWN GOAL!"
    }

    private fun generateYellowCardCommentary(player: String, team: String, excl: String): String {
        return "🟨 YELLOW CARD! $player is booked. He's walking a tightrope now for $team."
    }

    private fun generateRedCardCommentary(player: String, team: String, manager: String, excl: String): String {
        return "🟥 RED CARD! $player IS SENT OFF! $manager looks ABSOLUTELY LIVID! $team down to 10!"
    }

    private fun generateSubstitutionCommentary(pIn: String?, pOut: String?, team: String, manager: String): String {
        return "🔄 CHANGE for $team. $manager brings on $pIn to replace $pOut."
    }

    private fun generateInjuryCommentary(player: String, team: String, type: String?, manager: String, excl: String): String {
        return "🩹 CONCERN! $player is down for $team. $excl Looks like a $type injury."
    }

    private fun generateVarCommentary(desc: String?, overturned: Boolean): String {
        val outcome = if (overturned) "OVERTURNED" else "CONFIRMED"
        return "📺 VAR CHECK! THE DECISION IS $outcome! ${desc ?: ""}"
    }

    private fun generateSaveCommentary(player: String, team: String, isHome: Boolean, excl: String): String {
        return "🧤 WHAT A SAVE! $player keeps $team in the game with a STUNNING stop! $excl"
    }

    private fun generateCornerCommentary(player: String, team: String): String {
        return "⛳ CORNER for $team. $player steps up to deliver the cross."
    }

    private fun generateFoulCommentary(player: String, team: String, opp: String, ref: String, excl: String): String {
        return "🚫 FOUL! $player trips the $opp player. $ref blows the whistle. $excl"
    }

    private fun generateOffsideCommentary(player: String, team: String, excl: String): String {
        return "🚩 OFFSIDE! $player was caught in an offside position for $team. $excl"
    }

    fun generateAfricanFootballProverb(team: String, country: String): String {
        val proverbs = listOf(
            "As they say in $country, 'Smooth seas do not make skillful sailors.' $team are being TESTED today!",
            "There's a $country proverb: 'A roaring lion kills no game.' $team must be QUIETLY EFFECTIVE.",
            "In Africa, we say 'If you want to go fast, go alone. If you want to go far, go together.' $team are working as ONE!",
            "The $country elders say 'However long the night, the dawn will break.' $team are waiting for their MOMENT.",
            "A $country proverb: 'The axe forgets, but the tree remembers.' $team will not FORGET this lesson!",
            "In $country, they say: 'The strength of the crocodile is in the water.' $team are in their element today!"
        )
        return getVariatedLine(proverbs)
    }

    fun generateTacticalCommentary(formation: String, style: String, team: String, attacking: Boolean): String {
        val tone = if (attacking) "PUSHING HIGHER" else "SITTING DEEP"
        return "TACTICAL ANALYSIS: $team are currently $tone in their $formation setup, looking to play a $style game."
    }

    fun generateHalfTimeCommentary(homeTeam: String, awayTeam: String, homeScore: Int, awayScore: Int, homePossession: Int, homeShots: Int, awayShots: Int, homeCorners: Int, awayCorners: Int): String {
        return "⏱️ HALF TIME: $homeTeam $homeScore - $awayTeam $awayScore. Stats: Possession $homePossession% - ${100-homePossession}%. Shots: $homeShots - $awayShots. A lot to discuss in the dressing rooms!"
    }

    fun generateFullTimeCommentary(homeTeam: String, awayTeam: String, homeScore: Int, awayScore: Int, isUpset: Boolean, winner: String?, attendance: Int): String {
        val tag = if (isUpset) "AN ABSOLUTE SHOCKER!" else "A CLASSIC ENCOUNTER!"
        return "🏁 FULL TIME: $homeTeam $homeScore - $awayTeam $awayScore. $tag ${winner?.let { "$it win!" } ?: "It ends in a draw!"} Attendance: $attendance fans."
    }

    fun getCommentatorsForCountry(country: String): List<String> {
        return listOf(
            "Peter Drury", "Jon Champion", "Jim Beglin", "Martin Tyler", "Alan Smith",
            "Ernest Okonkwo", "Zama Masondo", "Baba Mthethwa", "Charles Anazodo"
        )
    }
}
