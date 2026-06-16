package com.fameafrica.afm.data.repository

import com.fameafrica.afm.data.database.dao.PersonalityStatistics
import com.fameafrica.afm.data.database.dao.PersonalityTypesDao
import com.fameafrica.afm.data.database.entities.PersonalityTypesEntity
import com.fameafrica.afm.data.database.entities.PlayerPersonality
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class PersonalityTypesRepository @Inject constructor(
    private val personalityTypesDaoProvider: Provider<PersonalityTypesDao>
) {

    private val personalityTypesDao: PersonalityTypesDao?
        get() = try {
            personalityTypesDaoProvider.get()
        } catch (e: Exception) {
            null
        }

    // ============ BASIC CRUD ============

    fun getAllPersonalities(): Flow<List<PersonalityTypesEntity>> = personalityTypesDao?.getAll() ?: kotlinx.coroutines.flow.flowOf(emptyList())

    suspend fun getPersonalityById(id: Int): PersonalityTypesEntity? = personalityTypesDao?.getById(id)

    suspend fun getPersonalityByName(name: String): PersonalityTypesEntity? = personalityTypesDao?.getByName(name)

    suspend fun insertPersonality(personality: PersonalityTypesEntity) {
        personalityTypesDao?.insert(personality)
    }

    suspend fun insertAllPersonalities(personalities: List<PersonalityTypesEntity>) {
        personalityTypesDao?.insertAll(personalities)
    }

    suspend fun updatePersonality(personality: PersonalityTypesEntity) {
        personalityTypesDao?.update(personality)
    }

    suspend fun deletePersonality(personality: PersonalityTypesEntity) {
        personalityTypesDao?.delete(personality)
    }

    // ============ INITIALIZATION ============

    suspend fun initializeDefaultPersonalities() {
        if (personalityTypesDao?.getCount() == 0) {
            val defaultPersonalities = listOf(
                PersonalityTypesEntity(
                    name = PlayerPersonality.PROFESSIONAL.value,
                    description = "Takes career seriously, maintains consistent form, rarely misses training.",
                    positiveEffects = "Consistency, Reliability",
                    negativeEffects = "Low flexibility",
                    moraleEffect = 1.1,
                    formConsistency = 1.2
                ),
                PersonalityTypesEntity(
                    name = PlayerPersonality.AGGRESSIVE.value,
                    description = "Plays with high intensity, commits more fouls, can be a discipline risk.",
                    positiveEffects = "High Intensity, Tough Tackling",
                    negativeEffects = "Discipline Risk, Frequent Fouls",
                    moraleEffect = 1.0,
                    formConsistency = 0.9
                ),
                PersonalityTypesEntity(
                    name = PlayerPersonality.LOYAL.value,
                    description = "Deeply committed to the club, unlikely to request transfer, good mentor.",
                    positiveEffects = "Club commitment, Mentoring",
                    negativeEffects = "Low ambition",
                    moraleEffect = 1.1,
                    formConsistency = 1.0
                ),
                PersonalityTypesEntity(
                    name = PlayerPersonality.MEDIA_FRIENDLY.value,
                    description = "Handles press well, boosts club reputation, good for merchandise.",
                    positiveEffects = "Reputation Boost, Fan engagement",
                    negativeEffects = "Distraction",
                    moraleEffect = 1.05,
                    formConsistency = 1.0
                ),
                PersonalityTypesEntity(
                    name = PlayerPersonality.MEDIA_HOSTILE.value,
                    description = "Avoids interviews, can create negative headlines, causes media drama.",
                    positiveEffects = "Focus on pitch",
                    negativeEffects = "Bad Publicity, Press friction",
                    moraleEffect = 0.9,
                    formConsistency = 0.95
                ),
                PersonalityTypesEntity(
                    name = PlayerPersonality.NATURAL_LEADER.value,
                    description = "Inspires teammates, helps younger players develop, captain material.",
                    positiveEffects = "Inspiration, Youth Development",
                    negativeEffects = "Strong personality",
                    moraleEffect = 1.15,
                    formConsistency = 1.1
                ),
                PersonalityTypesEntity(
                    name = PlayerPersonality.AMBITIOUS.value,
                    description = "Driven to succeed, may request moves to bigger clubs, works hard.",
                    positiveEffects = "High Workrate, Performance Drive",
                    negativeEffects = "Transfer Requests, Restlessness",
                    moraleEffect = 1.1,
                    formConsistency = 1.05
                ),
                PersonalityTypesEntity(
                    name = PlayerPersonality.TEMPERAMENTAL.value,
                    description = "Inconsistent performer, can be brilliant or invisible, emotional.",
                    positiveEffects = "Brilliant Moments",
                    negativeEffects = "Inconsistency, Emotional Outbursts",
                    moraleEffect = 0.85,
                    formConsistency = 0.7
                ),
                PersonalityTypesEntity(
                    name = PlayerPersonality.TEAM_PLAYER.value,
                    description = "Puts team first, unselfish, good chemistry with all teammates.",
                    positiveEffects = "Chemistry, Unselfishness",
                    negativeEffects = "Low individual stats",
                    moraleEffect = 1.05,
                    formConsistency = 1.05
                ),
                PersonalityTypesEntity(
                    name = PlayerPersonality.INDIVIDUALIST.value,
                    description = "Prefers individual glory, can be selfish, high risk high reward.",
                    positiveEffects = "Individual Brilliance",
                    negativeEffects = "Selfishness, Low Chemistry",
                    moraleEffect = 0.95,
                    formConsistency = 0.85
                )
            )

            personalityTypesDao?.insertAll(defaultPersonalities)
        }
    }

    // ============ UTILITY ============

    suspend fun getMoraleEffect(personalityName: String): Double {
        return personalityTypesDao?.getByName(personalityName)?.moraleEffect ?: 1.0
    }

    suspend fun getFormConsistency(personalityName: String): Double {
        return personalityTypesDao?.getByName(personalityName)?.formConsistency ?: 1.0
    }

    fun getPersonalityStatistics(): Flow<List<PersonalityStatistics>> =
        personalityTypesDao?.getPersonalityStatistics() ?: kotlinx.coroutines.flow.flowOf(emptyList())
}
