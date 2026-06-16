package com.fameafrica.afm.domain.repository

import com.fameafrica.afm.domain.model.core.Match
import com.fameafrica.afm.domain.model.core.Player
import com.fameafrica.afm.domain.model.core.Team
import kotlinx.coroutines.flow.Flow

/**
 * Shared interface for core game data access.
 */
interface GameRepository {
    fun getPlayersForTeam(teamId: Int): Flow<List<Player>>
    fun getTeam(teamId: Int): Flow<Team?>
    fun getNextMatch(teamId: Int): Flow<Match?>
}
