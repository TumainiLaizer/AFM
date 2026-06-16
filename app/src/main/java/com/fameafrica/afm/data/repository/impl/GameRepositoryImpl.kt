package com.fameafrica.afm.data.repository.impl

import com.fameafrica.afm.data.database.dao.FixturesDao
import com.fameafrica.afm.data.database.dao.PlayersDao
import com.fameafrica.afm.data.database.dao.TeamsDao
import com.fameafrica.afm.data.database.model.core.Match
import com.fameafrica.afm.data.database.model.core.Player
import com.fameafrica.afm.data.database.model.core.Team
import com.fameafrica.afm.data.repository.GameRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GameRepositoryImpl @Inject constructor(
    private val playersDao: PlayersDao,
    private val teamsDao: TeamsDao,
    private val fixturesDao: FixturesDao,
) : GameRepository {

    override fun getPlayersForTeam(teamId: Int): Flow<List<com.fameafrica.afm.data.database.model.core.Player>> {
        return playersDao.getPlayersByTeamId(teamId).map { entities ->
            entities.map { entity ->
                _root_ide_package_.com.fameafrica.afm.data.database.model.core.Player(
                    id = entity.id,
                    name = entity.name,
                    teamId = entity.teamId,
                    teamName = entity.teamName,
                    position = entity.position,
                    rating = entity.rating,
                    age = entity.age,
                    nationality = entity.nationality,
                    fitness = entity.stamina,
                    morale = entity.morale,
                    form = entity.currentForm.toDouble(),
                    marketValue = entity.marketValue.toLong(),
                    salary = entity.salary
                )
            }
        }
    }

    override fun getTeam(teamId: Int): Flow<com.fameafrica.afm.data.database.model.core.Team?> {
        return teamsDao.getByIdFlow(teamId).map { entity ->
            entity?.let {
                _root_ide_package_.com.fameafrica.afm.data.database.model.core.Team(
                    id = it.id,
                    name = it.name,
                    league = it.league,
                    logoPath = it.logoPath,
                    stadiumName = it.homeStadium,
                    stadiumCapacity = it.stadiumCapacity,
                    budget = it.revenue.toLong(),
                    wageBill = it.wageBudget ?: 0L,
                    boardConfidence = it.boardConfidence,
                    fanSatisfaction = it.fanSentiment,
                    rating = it.overallRating.toInt(),
                    leaguePosition = 0,
                    points = it.points
                )
            }
        }
    }

    override fun getNextMatch(teamId: Int): Flow<com.fameafrica.afm.data.database.model.core.Match?> {
        // We'll use getUpcomingFixturesByTeam and take the first one
        return fixturesDao.getUpcomingFixturesByTeam(teamId, "2025-01-01").map { entities ->
            entities.firstOrNull()?.let {
                _root_ide_package_.com.fameafrica.afm.data.database.model.core.Match(
                    id = it.id,
                    homeTeamId = it.homeTeamId,
                    homeTeamName = it.homeTeam,
                    awayTeamId = it.awayTeamId,
                    awayTeamName = it.awayTeam,
                    matchDate = it.matchDate,
                    competition = it.matchType,
                    venue = it.stadium,
                    homeScore = it.homeScore,
                    awayScore = it.awayScore,
                    status = it.matchStatus ?: "SCHEDULED"
                )
            }
        }
    }
}
