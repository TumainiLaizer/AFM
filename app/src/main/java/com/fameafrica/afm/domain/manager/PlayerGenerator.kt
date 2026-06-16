package com.fameafrica.afm.domain.manager

import com.fameafrica.afm.data.database.entities.ClubDNAEntity
import com.fameafrica.afm.data.database.entities.LeaguesEntity
import com.fameafrica.afm.data.database.entities.PlayersEntity
import com.fameafrica.afm.data.database.entities.TeamsEntity
import com.fameafrica.afm.data.repository.LeaguesRepository
import com.fameafrica.afm.data.repository.PlayersRepository
import com.fameafrica.afm.data.repository.TeamsRepository
import com.fameafrica.afm.utils.LeagueRankings
import com.fameafrica.afm.utils.PlayerAssetUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class PlayerGenerator @Inject constructor(
    private val teamsRepository: TeamsRepository,
    private val playersRepository: PlayersRepository,
    private val leaguesRepository: LeaguesRepository,
    private val nameGenerator: com.fameafrica.afm.utils.NameGenerator,
) {

    suspend fun generateMissingPlayers(onProgress: (Float) -> Unit = {}) {
        withContext(Dispatchers.IO) {
            // Phase 10 & 5 Optimization: Bulk identify teams needing players
            val teamIdsWithFewPlayers = playersRepository.getTeamIdsWithFewPlayers(15)
            if (teamIdsWithFewPlayers.isEmpty()) {
                onProgress(1.0f)
                return@withContext
            }

            val totalToProcess = teamIdsWithFewPlayers.size
            teamIdsWithFewPlayers.forEachIndexed { index, teamId ->
                val team = teamsRepository.getTeamById(teamId)
                if (team != null) {
                    val league = leaguesRepository.getLeagueByName(team.league)
                    val squad = generateSquadForTeam(team, league)
                    playersRepository.insertAllPlayers(squad)
                }
                onProgress((index + 1).toFloat() / totalToProcess)
            }
        }
    }

    fun generateSquadForTeam(team: TeamsEntity, league: LeaguesEntity?): List<PlayersEntity> {
        val players = mutableListOf<PlayersEntity>()
        val usedNames = mutableSetOf<String>()
        val positions = mapOf(
            "GK" to 3,
            "CB" to 4,
            "LB" to 2,
            "RB" to 2,
            "CDM" to 2,
            "CM" to 4,
            "CAM" to 2,
            "LW" to 2,
            "RW" to 2,
            "ST" to 2
        )

        // Base rating from team reputation (0-100) and country/league quality
        val qualityMultiplier = LeagueRankings.getQualityMultiplier(team.country, league?.level ?: 1)

        val baseReputationRating =
            (
                    (team.reputation * 0.45) +
                            ((6 - (league?.level ?: 3)) * 3.5)
                    ).coerceIn(35.0, 78.0)
        val baseRating =
            (baseReputationRating * qualityMultiplier)
                .toInt()
                .coerceIn(45, 80)
        positions.forEach { (pos, count) ->
            repeat(count) {
                val player = generatePlayerForSquad(team, pos, baseRating, league, usedNames)
                players.add(player)
                usedNames.add(player.name)
            }
        }
        return players
    }

    private fun generatePlayerForSquad(
        team: TeamsEntity,
        position: String,
        baseRating: Int,
        league: LeaguesEntity?,
        usedNames: Set<String>
    ): PlayersEntity {
        // Max rating cap based on league rank
        val countryRank = LeagueRankings.getRank(team.country, league?.level ?: 1)
        val maxRatingBase = when {
            countryRank <= 3 -> 90 // Elite top 3 (Egypt, Morocco, SA)
            countryRank <= 5 -> 88 // Elite top 5 (Algeria & Tanzania)
            countryRank <= 10 -> 85 // Top (Tunisia etc)
            countryRank <= 15 -> 80 // High
            countryRank <= 20 -> 75 // Medium Higher
            countryRank <= 40 -> 70 // Medium Lower
            else -> 65 // Lower
        }
        val levelPenalty = ((league?.level ?: 1) - 1) * 8
        val maxRating = (maxRatingBase - levelPenalty).coerceAtLeast(45)
        
        // Use team abilities to influence rating
        val teamAbility = when (PlayersEntity.calculatePositionCategory(position)) {
            "Forward" -> team.avgAttackingAbility
            "Midfielder" -> team.avgPlaymakingAbility
            "Defender", "Goalkeeper" -> team.avgDefenceAbility
            else -> null
        }

        val targetRating = if (teamAbility != null && teamAbility > 1.0) {
            // Blend generic baseRating with team ability
            (baseRating * 0.3 + teamAbility.toInt() * 0.7).toInt()
        } else {
            baseRating
        }

        val rating = (targetRating + Random.nextInt(-5, 6)).coerceIn(50, maxRating)
        val age = generateAge()
        val potential = if (age < 23) {
            (rating + Random.nextInt(4, 15)).coerceAtMost(maxRating + 10).coerceAtMost(99)
        } else {
            (rating + Random.nextInt(0, 3)).coerceAtMost(99)
        }
        val positionCategory = PlayersEntity.calculatePositionCategory(position)
        val nationality = determineNationality(team)
        
        val attributes = generateAttributes(position, rating, null, team)

        var name = nameGenerator.generateName(nationality)
        var attempts = 0
        while (usedNames.contains(name) && attempts < 50) {
            name = nameGenerator.generateName(nationality)
            attempts++
        }

        return PlayersEntity(
            name = name,
            teamId = team.id,
            teamName = team.name,
            region = team.region,
            nationality = nationality,
            age = age,
            height = generateHeight(position),
            preferredFoot = if (Random.nextInt(100) < 80) "RIGHT" else "LEFT",
            position = position,
            positionCategory = positionCategory,
            shirtNumber = Random.nextInt(1, 99),
            personalityType = generatePersonality(),
            archetype = null,
            primaryTrait = null,
            secondaryTrait = null,
            gameplayFocus = null,
            rating = rating,
            reputation = (rating * 0.8).toInt(),
            potential = potential,
            marketValue = calculateInitialValue(rating, potential),
            salary = calculateInitialSalary(rating),
            contractExpiry = "2028-06-30",
            isHomegrown = Random.nextInt(100) < 70,
            season = getCurrentSeason(),
            // Defensive mapping for all attributes to ensure rating consistency
            finishing = (attributes["finishing"] ?: rating).coerceIn(1, 99),
            passing = (attributes["passing"] ?: rating).coerceIn(1, 99),
            dribbling = (attributes["dribbling"] ?: rating).coerceIn(1, 99),
            skill = (attributes["skill"] ?: rating).coerceIn(1, 99),
            crossing = (attributes["crossing"] ?: rating).coerceIn(1, 99),
            defending = (attributes["defending"] ?: rating).coerceIn(1, 99),
            heading = (attributes["heading"] ?: rating).coerceIn(1, 99),
            longShots = (attributes["longShots"] ?: rating).coerceIn(1, 99),
            pace = (attributes["pace"] ?: rating).coerceIn(1, 99),
            stamina = (attributes["stamina"] ?: rating).coerceIn(1, 99),
            strength = (attributes["strength"] ?: rating).coerceIn(1, 99),
            acceleration = (attributes["acceleration"] ?: rating).coerceIn(1, 99),
            agility = (attributes["agility"] ?: rating).coerceIn(1, 99),
            aggression = (attributes["aggression"] ?: rating).coerceIn(1, 99),
            leadership = (attributes["leadership"] ?: rating).coerceIn(1, 99),
            motivation = (attributes["motivation"] ?: rating).coerceIn(1, 99),
            composure = (attributes["composure"] ?: rating).coerceIn(1, 99),
            vision = (attributes["vision"] ?: rating).coerceIn(1, 99),
            positioning = (attributes["positioning"] ?: rating).coerceIn(1, 99),
            anticipation = (attributes["anticipation"] ?: rating).coerceIn(1, 99),
            decisions = (attributes["decisions"] ?: rating).coerceIn(1, 99),
            creativity = (attributes["creativity"] ?: rating).coerceIn(1, 99),
            teamwork = (attributes["teamwork"] ?: rating).coerceIn(1, 99),
            goalkeeping = (attributes["goalkeeping"] ?: 10).coerceIn(1, 99),
            reflexes = (attributes["reflexes"] ?: (if(position == "GK") rating else 10)).coerceIn(1, 99),
            handling = (attributes["handling"] ?: (if(position == "GK") rating else 10)).coerceIn(1, 99),
            aerialAbility = (attributes["aerialAbility"] ?: (if(position == "GK") rating else 10)).coerceIn(1, 99),
            commandOfArea = (attributes["commandOfArea"] ?: (if(position == "GK") rating else 10)).coerceIn(1, 99),
            kicking = (attributes["kicking"] ?: (if(position == "GK") rating else 10)).coerceIn(1, 99),
            managerId = team.id,
            futureRole = null,
            faceImage = PlayerAssetUtils.getPlayerFace(name.hashCode(), nationality), // Use name hash as stable seed until ID is assigned
            imageUrl = null,
            createdAt = Calendar.getInstance().time.toString(),
            updatedAt = Calendar.getInstance().time.toString()
        )
    }

    private fun generateAge(): Int {
        val rand = Random.nextInt(100)
        return when {
            rand < 15 -> Random.nextInt(16, 20) // Young
            rand < 85 -> Random.nextInt(20, 30) // Prime
            else -> Random.nextInt(30, 38)      // Veteran
        }
    }

    private fun determineNationality(team: TeamsEntity): String {
        val rand = Random.nextInt(100)
        return if (rand < 90) team.country
        else listOf("Togo", "Nigeria", "Ghana", "Senegal", "Ivory Coast", "Cameroon", "Mali", "South Africa", "Egypt").random()
    }

    fun generateYouthPlayer(
        teamId: Int,
        teamName: String,
        region: String?,
        nationality: String,
        academyLevel: Int,
        dna: ClubDNAEntity?
    ): PlayersEntity {
        val age = 15 + Random.nextInt(4) // 15-18 years old
        val position = generateRandomPosition()
        val positionCategory = PlayersEntity.calculatePositionCategory(position)
        
        // Base rating influenced by academy level (1-5)
        // Level 1: 45-50, Level 5: 55-65
        val baseRatingMin = 42 + (academyLevel * 3)
        val baseRatingMax = baseRatingMin + 8
        val rating = Random.nextInt(baseRatingMin, baseRatingMax).coerceAtLeast(45)
        
        // Potential influenced by DNA youth priority and academy level
        val youthPriorityBonus = (dna?.youthPriority ?: 50) / 10.0
        val basePotentialMin = 68 + (academyLevel * 3) + youthPriorityBonus.toInt()
        val basePotentialMax = (basePotentialMin + 15).coerceAtMost(99)
        val potential = Random.nextInt(basePotentialMin, basePotentialMax).coerceAtLeast(rating + 5)

        // DNA Influence on attributes
        val attributes = generateAttributes(position, rating, dna)

        val playerName = nameGenerator.generateName(nationality)

        return PlayersEntity(
            name = playerName,
            teamId = teamId,
            teamName = teamName,
            region = region,
            nationality = nationality,
            age = age,
            height = generateHeight(position),
            preferredFoot = if (Random.nextInt(100) < 80) "RIGHT" else "LEFT",
            position = position,
            positionCategory = positionCategory,
            shirtNumber = Random.nextInt(1, 99),
            personalityType = generatePersonality(),
            archetype = null, // Can be assigned later
            primaryTrait = null,
            secondaryTrait = null,
            gameplayFocus = null,
            rating = rating,
            potential = potential,
            marketValue = calculateInitialValue(rating, potential),
            salary = calculateInitialSalary(rating),
            contractExpiry = "2030-06-30",
            isHomegrown = true,
            season = getCurrentSeason(),
            // Map generated attributes
            finishing = attributes["finishing"] ?: 50,
            passing = attributes["passing"] ?: 50,
            dribbling = attributes["dribbling"] ?: 50,
            skill = attributes["skill"] ?: 50,
            crossing = attributes["crossing"] ?: 50,
            defending = attributes["defending"] ?: 50,
            heading = attributes["heading"] ?: 50,
            longShots = attributes["longShots"] ?: 50,
            pace = attributes["pace"] ?: 50,
            stamina = attributes["stamina"] ?: 50,
            strength = attributes["strength"] ?: 50,
            acceleration = attributes["acceleration"] ?: 50,
            agility = attributes["agility"] ?: 50,
            aggression = attributes["aggression"] ?: 30,
            leadership = attributes["leadership"] ?: 50,
            motivation = attributes["motivation"] ?: 50,
            composure = attributes["composure"] ?: 50,
            vision = attributes["vision"] ?: 50,
            positioning = attributes["positioning"] ?: 50,
            anticipation = attributes["anticipation"] ?: 50,
            decisions = attributes["decisions"] ?: 50,
            creativity = attributes["creativity"] ?: 50,
            teamwork = attributes["teamwork"] ?: 50,
            goalkeeping = attributes["goalkeeping"] ?: 10,
            reflexes = attributes["reflexes"] ?: 50,
            handling = attributes["handling"] ?: 50,
            aerialAbility = attributes["aerialAbility"] ?: 50,
            commandOfArea = attributes["commandOfArea"] ?: 50,
            kicking = attributes["kicking"] ?: 50,
            futureRole = null,
            faceImage = PlayerAssetUtils.getPlayerFace(playerName.hashCode(), nationality),
            imageUrl = null,
            createdAt = Calendar.getInstance().time.toString(),
            updatedAt = Calendar.getInstance().time.toString()
        )
    }

    private fun generateRandomPosition(): String {
        val positions = listOf(
            "GK", "CB", "LB", "RB", "CDM", "CM", "CAM", "LM", "RM", "LW", "RW", "ST"
        )
        // Weighted towards common positions
        val weights = listOf(1, 3, 1, 1, 2, 3, 2, 1, 1, 1, 1, 2)
        val totalWeight = weights.sum()
        var random = Random.nextInt(totalWeight)
        
        for (i in positions.indices) {
            random -= weights[i]
            if (random < 0) return positions[i]
        }
        return "CM"
    }

    private fun generateAttributes(position: String, rating: Int, dna: ClubDNAEntity?, team: TeamsEntity? = null): Map<String, Int> {
        val attrs = mutableMapOf<String, Int>()
        
        // Base attribute calculation
        // High quality players should have higher minimum attributes
        val baseMin = (rating * 0.85).toInt().coerceIn(1, 95)
        val baseMax = (rating * 1.15).toInt().coerceIn(1, 99)
        val variance = (baseMax - baseMin).coerceAtLeast(5)

        val allAttrs = listOf(
            "finishing", "passing", "dribbling", "skill", "crossing", "defending", "heading",
            "longShots", "pace", "stamina", "strength", "acceleration", "agility", "aggression",
            "leadership", "motivation", "composure", "vision", "positioning", "anticipation",
            "decisions", "creativity", "teamwork", "goalkeeping", "reflexes", "handling",
            "aerialAbility", "commandOfArea", "kicking"
        )
        
        allAttrs.forEach { attrs[it] = (baseMin + Random.nextInt(variance)).coerceIn(1, 99) }

        // Boost based on team abilities
        team?.let { t ->
            val attacking = t.avgAttackingAbility?.toInt() ?: rating
            val defending = t.avgDefenceAbility?.toInt() ?: rating
            val playmaking = t.avgPlaymakingAbility?.toInt() ?: rating

            // Attacking boosts
            listOf("finishing", "longShots", "dribbling", "acceleration").forEach {
                attrs[it] = (attrs[it]!! * 0.4 + attacking * 0.6).toInt().coerceIn(1, 99)
            }
            // Playmaking boosts
            listOf("passing", "vision", "creativity", "skill").forEach {
                attrs[it] = (attrs[it]!! * 0.4 + playmaking * 0.6).toInt().coerceIn(1, 99)
            }
            // Defending boosts
            listOf("defending", "heading", "positioning", "strength").forEach {
                attrs[it] = (attrs[it]!! * 0.4 + defending * 0.6).toInt().coerceIn(1, 99)
            }
        }

        // Boost based on position (Fine-tuning)
        when (position) {
            "GK" -> {
                attrs["goalkeeping"] = (rating + 10).coerceAtMost(99)
                attrs["reflexes"] = (rating + 8).coerceAtMost(99)
                attrs["handling"] = (rating + 5).coerceAtMost(99)
            }
            "ST" -> {
                attrs["finishing"] = (rating + 12).coerceAtMost(99)
                attrs["pace"] = (rating + 5).coerceAtMost(99)
            }
            "CB" -> {
                attrs["defending"] = (rating + 10).coerceAtMost(99)
                attrs["strength"] = (rating + 8).coerceAtMost(99)
                attrs["heading"] = (rating + 5).coerceAtMost(99)
            }
            "CM", "CAM" -> {
                attrs["passing"] = (rating + 10).coerceAtMost(99)
                attrs["vision"] = (rating + 8).coerceAtMost(99)
            }
        }

        // Boost based on DNA playstyle
        dna?.playStyle?.let { style ->
            when (style.uppercase()) {
                "POSSESSION" -> {
                    attrs["passing"] = (attrs["passing"]!! + 5).coerceAtMost(99)
                    attrs["vision"] = (attrs["vision"]!! + 5).coerceAtMost(99)
                    attrs["composure"] = (attrs["composure"]!! + 5).coerceAtMost(99)
                }
                "GEGENPRESS" -> {
                    attrs["stamina"] = (attrs["stamina"]!! + 5).coerceAtMost(99)
                    attrs["aggression"] = (attrs["aggression"]!! + 5).coerceAtMost(99)
                    attrs["pace"] = (attrs["pace"]!! + 5).coerceAtMost(99)
                }
                "DIRECT_PHYSICAL" -> {
                    attrs["strength"] = (attrs["strength"]!! + 5).coerceAtMost(99)
                    attrs["heading"] = (attrs["heading"]!! + 5).coerceAtMost(99)
                    attrs["stamina"] = (attrs["stamina"]!! + 5).coerceAtMost(99)
                }
                "FLAIR_EXPRESSIVE" -> {
                    attrs["dribbling"] = (attrs["dribbling"]!! + 8).coerceAtMost(99)
                    attrs["skill"] = (attrs["skill"]!! + 8).coerceAtMost(99)
                    attrs["creativity"] = (attrs["creativity"]!! + 5).coerceAtMost(99)
                }
                "WING_PLAY" -> {
                    attrs["crossing"] = (attrs["crossing"]!! + 8).coerceAtMost(99)
                    attrs["pace"] = (attrs["pace"]!! + 5).coerceAtMost(99)
                    attrs["acceleration"] = (attrs["acceleration"]!! + 5).coerceAtMost(99)
                }
                "TRANSITION_HEAVY" -> {
                    attrs["pace"] = (attrs["pace"]!! + 6).coerceAtMost(99)
                    attrs["stamina"] = (attrs["stamina"]!! + 6).coerceAtMost(99)
                    attrs["anticipation"] = (attrs["anticipation"]!! + 5).coerceAtMost(99)
                }
                "TACTICAL_DISCIPLINE" -> {
                    attrs["positioning"] = (attrs["positioning"]!! + 7).coerceAtMost(99)
                    attrs["decisions"] = (attrs["decisions"]!! + 7).coerceAtMost(99)
                    attrs["teamwork"] = (attrs["teamwork"]!! + 5).coerceAtMost(99)
                }
                "YOUTH_ENERGY" -> {
                    attrs["stamina"] = (attrs["stamina"]!! + 8).coerceAtMost(99)
                    attrs["aggression"] = (attrs["aggression"]!! + 5).coerceAtMost(99)
                }
            }
        }

        return attrs
    }

    private fun generateHeight(position: String): Int {
        return when (position) {
            "GK" -> Random.nextInt(185, 202)
            "CB" -> Random.nextInt(182, 198)
            "ST" -> Random.nextInt(175, 195)
            else -> Random.nextInt(165, 188)
        }
    }

    private fun generatePersonality(): String {
        val types = listOf("PROFESSIONAL", "AMBITIOUS", "DETERMINED", "BALANCED", "LOYAL", "TEMPERAMENTAL")
        return types.random()
    }

    private fun calculateInitialValue(rating: Int, potential: Int): Int {
        val base = rating * 10_000
        val potentialBonus = (potential - rating) * 20_000
        return base + potentialBonus
    }

    private fun calculateInitialSalary(rating: Int): Double {
        return (rating * 500.0).coerceAtLeast(1000.0)
    }

    private fun getCurrentSeason(): String {
        val year = Calendar.getInstance().get(Calendar.YEAR)
        return "$year/${year + 1}"
    }
}
