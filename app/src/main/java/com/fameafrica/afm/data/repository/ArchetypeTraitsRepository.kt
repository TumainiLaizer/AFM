package com.fameafrica.afm.data.repository

import com.fameafrica.afm.data.database.dao.ArchetypeTraitsDao
import com.fameafrica.afm.data.database.entities.ArchetypeTraitsEntity
import com.fameafrica.afm.data.database.entities.PlayerArchetype
import com.fameafrica.afm.data.database.model.enums.PlayerTrait
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class ArchetypeTraitsRepository @Inject constructor(
    private val archetypeTraitsDaoProvider: Provider<ArchetypeTraitsDao>
) {
    private val archetypeTraitsDao get() = archetypeTraitsDaoProvider.get()

    private val json = Json { 
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    // ============ BASIC CRUD ============

    fun getAllArchetypes(): Flow<List<ArchetypeTraitsEntity>> = archetypeTraitsDao.getAll()

    suspend fun getArchetypeById(id: Int): ArchetypeTraitsEntity? = archetypeTraitsDao.getById(id)

    suspend fun getArchetypeByName(name: String): ArchetypeTraitsEntity? = archetypeTraitsDao.getByName(name)

    suspend fun insertArchetype(archetype: ArchetypeTraitsEntity) = archetypeTraitsDao.insert(archetype)

    suspend fun insertAllArchetypes(archetypes: List<ArchetypeTraitsEntity>) = archetypeTraitsDao.insertAll(archetypes)

    suspend fun updateArchetype(archetype: ArchetypeTraitsEntity) = archetypeTraitsDao.update(archetype)

    suspend fun deleteArchetype(archetype: ArchetypeTraitsEntity) = archetypeTraitsDao.delete(archetype)

    // ============ INITIALIZATION ============

    suspend fun initializeDefaultArchetypes() {
        if (archetypeTraitsDao.getCount() == 0) {
            val defaultArchetypes = listOf(
                ArchetypeTraitsEntity(
                    archetypeName = PlayerArchetype.DYNAMIC_FORWARD.value,
                    primaryTrait = _root_ide_package_.com.fameafrica.afm.data.database.model.enums.PlayerTrait.DETERMINED.value,
                    secondaryTrait = _root_ide_package_.com.fameafrica.afm.data.database.model.enums.PlayerTrait.CREATIVE.value,
                    gameplayFocus = "ATTACKING,GOAL_SCORING,DRIBBLING",
                    attributeBoost = """{"finishing":5,"dribbling":5,"pace":3,"acceleration":3}""",
                    description = "A forward who combines pace, skill, and determination to constantly threaten the opposition's defense."
                ),
                ArchetypeTraitsEntity(
                    archetypeName = PlayerArchetype.STRATEGIC_MIDFIELDER.value,
                    primaryTrait = _root_ide_package_.com.fameafrica.afm.data.database.model.enums.PlayerTrait.INTELLIGENT.value,
                    secondaryTrait = _root_ide_package_.com.fameafrica.afm.data.database.model.enums.PlayerTrait.DECISIVE.value,
                    gameplayFocus = "PASSING,VISION,TACTICAL",
                    attributeBoost = """{"passing":5,"vision":5,"decisions":3,"creativity":3}""",
                    description = "A midfielder who dictates play with intelligent passing and exceptional game reading ability."
                ),
                ArchetypeTraitsEntity(
                    archetypeName = PlayerArchetype.RESILIENT_DEFENDER.value,
                    primaryTrait = _root_ide_package_.com.fameafrica.afm.data.database.model.enums.PlayerTrait.RESILIENT.value,
                    secondaryTrait = _root_ide_package_.com.fameafrica.afm.data.database.model.enums.PlayerTrait.DISCIPLINED.value,
                    gameplayFocus = "DEFENDING,POSITIONING,TACKLING",
                    attributeBoost = """{"defending":5,"positioning":5,"strength":3,"anticipation":3}""",
                    description = "A defender who remains composed under pressure and rarely loses duels."
                ),
                ArchetypeTraitsEntity(
                    archetypeName = PlayerArchetype.INSPIRATIONAL_CAPTAIN.value,
                    primaryTrait = _root_ide_package_.com.fameafrica.afm.data.database.model.enums.PlayerTrait.CONFIDENT.value,
                    secondaryTrait = _root_ide_package_.com.fameafrica.afm.data.database.model.enums.PlayerTrait.TEAM_ORIENTED.value,
                    gameplayFocus = "LEADERSHIP,TEAMWORK,MOTIVATION",
                    attributeBoost = """{"leadership":10,"motivation":5,"teamwork":5,"composure":3}""",
                    description = "A natural leader who inspires teammates and elevates performance of those around them."
                ),
                ArchetypeTraitsEntity(
                    archetypeName = PlayerArchetype.COMPLETE_FORWARD.value,
                    primaryTrait = _root_ide_package_.com.fameafrica.afm.data.database.model.enums.PlayerTrait.VERSATILE.value,
                    secondaryTrait = _root_ide_package_.com.fameafrica.afm.data.database.model.enums.PlayerTrait.BRAVE.value,
                    gameplayFocus = "FINISHING,HEADING,STRENGTH",
                    attributeBoost = """{"finishing":4,"heading":4,"strength":4,"composure":3}""",
                    description = "A well-rounded striker who can score with both feet and head from any situation."
                ),
                ArchetypeTraitsEntity(
                    archetypeName = PlayerArchetype.BOX_TO_BOX.value,
                    primaryTrait = _root_ide_package_.com.fameafrica.afm.data.database.model.enums.PlayerTrait.ENERGETIC.value,
                    secondaryTrait = _root_ide_package_.com.fameafrica.afm.data.database.model.enums.PlayerTrait.DETERMINED.value,
                    gameplayFocus = "STAMINA,DEFENDING,ATTACKING",
                    attributeBoost = """{"stamina":10,"work_rate":10,"defending":3,"shooting":3}""",
                    description = "A tireless midfielder who contributes at both ends of the pitch for the full 90 minutes."
                ),
                ArchetypeTraitsEntity(
                    archetypeName = PlayerArchetype.SWEEPING_KEEPER.value,
                    primaryTrait = _root_ide_package_.com.fameafrica.afm.data.database.model.enums.PlayerTrait.COMPOSED.value,
                    secondaryTrait = _root_ide_package_.com.fameafrica.afm.data.database.model.enums.PlayerTrait.DECISIVE.value,
                    gameplayFocus = "REFLEXES,COMMAND,SWEEPING",
                    attributeBoost = """{"reflexes":5,"command_of_area":5,"kicking":4,"pace":3}""",
                    description = "A goalkeeper who excels at coming off their line and acting as an extra defender."
                ),
                ArchetypeTraitsEntity(
                    archetypeName = PlayerArchetype.TRADITIONAL_STRIKER.value,
                    primaryTrait = _root_ide_package_.com.fameafrica.afm.data.database.model.enums.PlayerTrait.BRAVE.value,
                    secondaryTrait = _root_ide_package_.com.fameafrica.afm.data.database.model.enums.PlayerTrait.DETERMINED.value,
                    gameplayFocus = "FINISHING,HEADING,POSITIONING",
                    attributeBoost = """{"finishing":6,"heading":6,"positioning":4,"strength":4}""",
                    description = "A classic number 9 who thrives in the penalty box and converts chances."
                ),
                ArchetypeTraitsEntity(
                    archetypeName = PlayerArchetype.WING_WIZARD.value,
                    primaryTrait = _root_ide_package_.com.fameafrica.afm.data.database.model.enums.PlayerTrait.CREATIVE.value,
                    secondaryTrait = _root_ide_package_.com.fameafrica.afm.data.database.model.enums.PlayerTrait.BOLD.value,
                    gameplayFocus = "DRIBBLING,CROSSING,PACE",
                    attributeBoost = """{"dribbling":6,"crossing":5,"pace":4,"acceleration":4}""",
                    description = "A wide player who beats defenders for fun and delivers dangerous crosses."
                ),
                ArchetypeTraitsEntity(
                    archetypeName = PlayerArchetype.ANCHOR_MAN.value,
                    primaryTrait = _root_ide_package_.com.fameafrica.afm.data.database.model.enums.PlayerTrait.DISCIPLINED.value,
                    secondaryTrait = _root_ide_package_.com.fameafrica.afm.data.database.model.enums.PlayerTrait.COMPOSED.value,
                    gameplayFocus = "TACKLING,POSITIONING,SCREENING",
                    attributeBoost = """{"defending":6,"positioning":5,"strength":4,"anticipation":4}""",
                    description = "A defensive midfielder who breaks up play and protects the back line."
                ),
                ArchetypeTraitsEntity(
                    archetypeName = PlayerArchetype.REGISTA.value,
                    primaryTrait = _root_ide_package_.com.fameafrica.afm.data.database.model.enums.PlayerTrait.INTELLIGENT.value,
                    secondaryTrait = _root_ide_package_.com.fameafrica.afm.data.database.model.enums.PlayerTrait.CREATIVE.value,
                    gameplayFocus = "PASSING,VISION,DICTATING",
                    attributeBoost = """{"passing":6,"vision":6,"creativity":5,"decisions":3}""",
                    description = "A deep-lying playmaker who orchestrates attacks from midfield."
                ),
                ArchetypeTraitsEntity(
                    archetypeName = PlayerArchetype.LIBERO.value,
                    primaryTrait = _root_ide_package_.com.fameafrica.afm.data.database.model.enums.PlayerTrait.VERSATILE.value,
                    secondaryTrait = _root_ide_package_.com.fameafrica.afm.data.database.model.enums.PlayerTrait.COMPOSED.value,
                    gameplayFocus = "BALL_PLAYING,POSITIONING,LEADERSHIP",
                    attributeBoost = """{"passing":4,"composure":5,"leadership":5,"anticipation":4}""",
                    description = "A sweeper who reads the game brilliantly and initiates attacks from the back."
                )
            )

            archetypeTraitsDao.insertAll(defaultArchetypes)
        }
    }

    // ============ UTILITY ============

    fun getArchetypeByTrait(trait: String): Flow<List<ArchetypeTraitsEntity>> =
        archetypeTraitsDao.getByTrait(trait)

    fun getArchetypesByFocus(focus: String): Flow<List<ArchetypeTraitsEntity>> =
        archetypeTraitsDao.getByGameplayFocus(focus)

    suspend fun getAttributeBoostForArchetype(archetypeName: String): Map<String, Int> {
        val archetype = archetypeTraitsDao.getByName(archetypeName) ?: return emptyMap()
        return parseAttributeBoost(archetype.attributeBoost)
    }

    private fun parseAttributeBoost(boostJson: String?): Map<String, Int> {
        if (boostJson.isNullOrBlank()) return emptyMap()
        return try {
            json.decodeFromString<Map<String, Int>>(boostJson)
        } catch (e: Exception) {
            emptyMap()
        }
    }
}
