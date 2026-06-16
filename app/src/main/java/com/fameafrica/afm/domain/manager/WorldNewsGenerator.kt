package com.fameafrica.afm.domain.manager

import com.fameafrica.afm.domain.model.SimulationEvent
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorldNewsGenerator @Inject constructor() {

    fun generateHeadline(event: SimulationEvent): String? {
        return when (event) {
            is SimulationEvent.MatchPlayed -> {
                val res = event.result
                if (res.homeScore > res.awayScore + 2) {
                    "${res.homeTeam} DOMINATE IN ${res.homeScore}-${res.awayScore} THRASHING OF ${res.awayTeam}"
                } else if (res.homeScore == res.awayScore) {
                    "STALEMATE: ${res.homeTeam} AND ${res.awayTeam} SETTLE FOR A DRAW"
                } else {
                    "${res.homeTeam} EDGE PAST ${res.awayTeam} IN TIGHT ENCOUNTER"
                }
            }
            is SimulationEvent.Injury -> {
                if (event.duration > 14) {
                    "MAJOR BLOW: ${event.playerName} RULED OUT FOR ${event.duration} DAYS"
                } else {
                    "${event.playerName} PICKED UP A MINOR KNOCK"
                }
            }
            is SimulationEvent.TransferOffer -> {
                "TRANSFER RUMOR: ${event.offeringTeam} EYEING ${event.playerName} WITH €${event.fee/1_000_000}M BID"
            }
            is SimulationEvent.BoardMeeting -> event.title
            is SimulationEvent.FinancialAlert -> "FINANCIAL NEWS: ${event.message}"
            else -> null
        }
    }
}
