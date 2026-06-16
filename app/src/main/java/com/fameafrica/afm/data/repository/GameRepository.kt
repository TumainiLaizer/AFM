package com.fameafrica.afm.data.repository

import com.fameafrica.afm.data.database.model.core.Match
import com.fameafrica.afm.data.database.model.core.Player
import com.fameafrica.afm.data.database.model.core.Team
import kotlinx.coroutines.flow.Flow

/**
 * Shared interface for core game data access.
 */
interface GameRepository {
    fun getPlayersForTeam(teamId: Int): Flow<List<com.fameafrica.afm.data.database.model.core.Player>>
    fun getTeam(teamId: Int): Flow<com.fameafrica.afm.data.database.model.core.Team?>
    fun getNextMatch(teamId: Int): Flow<com.fameafrica.afm.data.database.model.core.Match?>
}