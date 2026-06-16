package com.fameafrica.afm.data.repository

import com.fameafrica.afm.data.database.dao.JournalistsDao
import com.fameafrica.afm.data.database.entities.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import kotlin.collections.isNotEmpty

@Singleton
class JournalistsRepository @Inject constructor(
    private val journalistsDaoProvider: Provider<JournalistsDao>
) {

    private val journalistsDao: JournalistsDao?
        get() = try {
            journalistsDaoProvider.get()
        } catch (e: Exception) {
            null
        }

    // ============ BASIC CRUD ============

    fun getAllJournalists(): Flow<List<JournalistsEntity>> = journalistsDao?.getAll() ?: kotlinx.coroutines.flow.flowOf(emptyList())

    suspend fun getJournalistById(id: Int): JournalistsEntity? = journalistsDao?.getById(id)

    suspend fun getJournalistByName(name: String): JournalistsEntity? = journalistsDao?.getByName(name)

    suspend fun insertJournalist(journalist: JournalistsEntity) {
        journalistsDao?.insert(journalist)
    }

    suspend fun insertAllJournalists(journalists: List<JournalistsEntity>) {
        journalistsDao?.insertAll(journalists)
    }

    suspend fun updateJournalist(journalist: JournalistsEntity) {
        journalistsDao?.update(journalist)
    }

    suspend fun deleteJournalist(journalist: JournalistsEntity) {
        journalistsDao?.delete(journalist)
    }

    // ============ INITIALIZATION ============

    suspend fun initializeAfricanJournalists() {
        if (journalistsDao?.getAll()?.firstOrNull()?.isEmpty() != false) {
            val journalists = listOf(
                // Tanzania
                JournalistsEntity(
                    name = "Azam Sports",
                    mediaCompany = MediaCompany.AZAM_MEDIA.value,
                    expertise = JournalistExpertise.MATCH_REPORTING.value,
                    personality = JournalistPersonality.NEUTRAL.value
                ),
                JournalistsEntity(
                    name = "Jamal Kivumbi",
                    mediaCompany = MediaCompany.CITIZEN_TV.value,
                    expertise = JournalistExpertise.TRANSFER_NEWS.value,
                    personality = JournalistPersonality.SENSATIONALIST.value
                ),
                JournalistsEntity(
                    name = "Neema Mwakyusa",
                    mediaCompany = MediaCompany.AZAM_MEDIA.value,
                    expertise = JournalistExpertise.TACTICAL_ANALYSIS.value,
                    personality = JournalistPersonality.ANALYST.value
                ),

                // Kenya
                JournalistsEntity(
                    name = "KBC Sports",
                    mediaCompany = MediaCompany.KBC.value,
                    expertise = JournalistExpertise.MATCH_REPORTING.value,
                    personality = JournalistPersonality.NEUTRAL.value
                ),
                JournalistsEntity(
                    name = "James Omondi",
                    mediaCompany = MediaCompany.CITIZEN_TV.value,
                    expertise = JournalistExpertise.INVESTIGATIVE.value,
                    personality = JournalistPersonality.HOSTILE.value
                ),

                // Ghana
                JournalistsEntity(
                    name = "GBC Sports",
                    mediaCompany = MediaCompany.GBC.value,
                    expertise = JournalistExpertise.MATCH_REPORTING.value,
                    personality = JournalistPersonality.NEUTRAL.value
                ),
                JournalistsEntity(
                    name = "Kwame Asare",
                    mediaCompany = MediaCompany.SUPERSPORT.value,
                    expertise = JournalistExpertise.INTERVIEWS.value,
                    personality = JournalistPersonality.FRIENDLY.value
                ),

                // Nigeria
                JournalistsEntity(
                    name = "NTA Sports",
                    mediaCompany = MediaCompany.NTA.value,
                    expertise = JournalistExpertise.MATCH_REPORTING.value,
                    personality = JournalistPersonality.NEUTRAL.value
                ),
                JournalistsEntity(
                    name = "Chidi Obi",
                    mediaCompany = MediaCompany.BBC_AFRICA.value,
                    expertise = JournalistExpertise.OPINION.value,
                    personality = JournalistPersonality.SENSATIONALIST.value
                ),

                // South Africa
                JournalistsEntity(
                    name = "SABC Sport",
                    mediaCompany = MediaCompany.SABC.value,
                    expertise = JournalistExpertise.MATCH_REPORTING.value,
                    personality = JournalistPersonality.NEUTRAL.value
                ),
                JournalistsEntity(
                    name = "Mark Fish",
                    mediaCompany = MediaCompany.SUPERSPORT.value,
                    expertise = JournalistExpertise.TACTICAL_ANALYSIS.value,
                    personality = JournalistPersonality.ANALYST.value
                ),

                // CAF
                JournalistsEntity(
                    name = "CAF Media",
                    mediaCompany = MediaCompany.CAF_MEDIA.value,
                    expertise = JournalistExpertise.BREAKING_NEWS.value,
                    personality = JournalistPersonality.NEUTRAL.value
                ),
                JournalistsEntity(
                    name = "Fatou Diouf",
                    mediaCompany = MediaCompany.FRANCE24_AFRIQUE.value,
                    expertise = JournalistExpertise.INTERVIEWS.value,
                    personality = JournalistPersonality.FRIENDLY.value
                )
            )

            journalistsDao?.insertAll(journalists)
        }
    }

    // ============ QUERIES ============

    fun getByPersonality(personality: String): Flow<List<JournalistsEntity>> =
        journalistsDao?.getByPersonality(personality) ?: kotlinx.coroutines.flow.flowOf(emptyList())

    fun getFriendlyJournalists(): Flow<List<JournalistsEntity>> =
        journalistsDao?.getFriendlyJournalists() ?: kotlinx.coroutines.flow.flowOf(emptyList())

    fun getHostileJournalists(): Flow<List<JournalistsEntity>> =
        journalistsDao?.getHostileJournalists() ?: kotlinx.coroutines.flow.flowOf(emptyList())

    fun getSensationalistJournalists(): Flow<List<JournalistsEntity>> =
        journalistsDao?.getSensationalistJournalists() ?: kotlinx.coroutines.flow.flowOf(emptyList())

    fun getTransferExperts(): Flow<List<JournalistsEntity>> =
        journalistsDao?.getTransferExperts() ?: kotlinx.coroutines.flow.flowOf(emptyList())

    fun getTacticalAnalysts(): Flow<List<JournalistsEntity>> =
        journalistsDao?.getTacticalAnalysts() ?: kotlinx.coroutines.flow.flowOf(emptyList())

    // ============ RANDOM SELECTION ============

    suspend fun getRandomJournalist(): JournalistsEntity? {
        val all = journalistsDao?.getAll()?.firstOrNull() ?: return null
        return if (all.isNotEmpty()) all.random() else null
    }

    suspend fun getRandomJournalistByExpertise(expertise: String): JournalistsEntity? {
        val journalists = journalistsDao?.getByExpertise(expertise)?.firstOrNull() ?: return null
        return if (journalists.isNotEmpty()) journalists.random() else null
    }

    suspend fun getRandomJournalistByPersonality(personality: String): JournalistsEntity? {
        val journalists = journalistsDao?.getByPersonality(personality)?.firstOrNull() ?: return null
        return if (journalists.isNotEmpty()) journalists.random() else null
    }
}