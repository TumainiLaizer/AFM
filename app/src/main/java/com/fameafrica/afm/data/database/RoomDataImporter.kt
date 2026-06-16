package com.fameafrica.afm.data.database

import android.content.Context
import android.util.Log
import com.fameafrica.afm.data.database.entities.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomDataImporter @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val moshi = Moshi.Builder()
        .add(BooleanAdapter())
        .add(IntAdapter())
        .add(LongAdapter())
        .add(KotlinJsonAdapterFactory())
        .build()

    /**
     * Imports data from JSON files in assets/databases/json/ into the provided database.
     * Respects foreign key order.
     */
    suspend fun importFromAssets(
        database: AFMDatabase,
        onProgress: (table: String, current: Int, total: Int, recordCount: Int) -> Unit
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val tablesToImport = getImportOrder(database)
            val totalTables = tablesToImport.size

            tablesToImport.forEachIndexed { index, table ->
                val fileName = "databases/static_data_export/${table.tableName}.json"
                val fallbackFileName = "databases/json/${table.tableName}.json"
                
                var inputStream = try {
                    context.assets.open(fileName)
                } catch (e: Exception) {
                    null
                }
                
                if (inputStream == null) {
                    inputStream = try {
                        context.assets.open(fallbackFileName)
                    } catch (e: Exception) {
                        null
                    }
                }

                if (inputStream != null) {
                    inputStream.use { stream ->
                        val json = stream.bufferedReader().use { it.readText() }
                        if (json.trim() == "[]" || json.trim().isEmpty()) {
                            Log.d("AFM_IMPORT", "Skipping empty table ${table.tableName}")
                        } else {
                            val recordCount = table.importAction(json)
                            onProgress(table.tableName, index + 1, totalTables, recordCount)
                        }
                    }
                } else {
                    Log.w("AFM_IMPORT", "Could not find asset $fileName or $fallbackFileName, skipping table ${table.tableName}")
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("AFM_IMPORT", "Import failed", e)
            Result.failure(e)
        }
    }

    private interface TableImporter {
        val tableName: String
        suspend fun importAction(json: String): Int
    }

    private inline fun <reified T> createImporter(
        tableName: String,
        crossinline insertAction: suspend (List<T>) -> Unit
    ): TableImporter {
        return object : TableImporter {
            override val tableName = tableName
            override suspend fun importAction(json: String): Int {
                val type = Types.newParameterizedType(List::class.java, T::class.java)
                val adapter = moshi.adapter<List<T>>(type)
                val data = adapter.fromJson(json) ?: return 0
                if (data.isEmpty()) return 0
                insertAction(data)
                return data.size
            }
        }
    }

    private fun getImportOrder(database: AFMDatabase): List<TableImporter> {
        return listOf(
            // Level 0: Static & Independent
            createImporter<NationalitiesEntity>("nationalities") { database.nationalitiesDao().insertAll(it) },
            createImporter<RegionalSettingsEntity>("regional_settings") { database.regionalSettingsDao().insertAll(it) },
            createImporter<PersonalityTypesEntity>("personality_types") { database.personalityTypesDao().insertAll(it) },
            createImporter<ArchetypeTraitsEntity>("archetype_traits") { database.archetypeTraitsDao().insertAll(it) },
            createImporter<JournalistsEntity>("journalists") { database.journalistsDao().insertAll(it) },
            createImporter<RefereesEntity>("referees") { database.refereesDao().insertAll(it) },
            createImporter<SponsorsEntity>("sponsors") { database.sponsorsDao().insertAll(it) },
            createImporter<CurrencyExchangeRatesEntity>("currency_exchange_rates") { database.currencyExchangeRatesDao().insertAll(it) },
            createImporter<GameSettingsEntity>("game_settings") { database.gameSettingsDao().insertAll(it) },
            createImporter<UserPreferencesEntity>("user_preferences") { database.userPreferencesDao().insertAll(it) },
            createImporter<RivalryEntity>("rivalries") { database.rivalryDao().insertAll(it) },

            // Level 1: Primary Entities
            createImporter<LeaguesEntity>("leagues") { database.leaguesDao().insertAll(it) },
            createImporter<CupsEntity>("cups") { database.cupsDao().insertAll(it) },
            createImporter<TeamsEntity>("teams") { database.teamsDao().insertAll(it) },
            createImporter<ClubDNAEntity>("club_dna") { database.clubDNADao().insertAll(it) },
            createImporter<NationalTeamsEntity>("national_teams") { database.nationalTeamsDao().insertAll(it) },
            createImporter<PlayerAgentsEntity>("player_agents") { database.playerAgentsDao().insertAll(it) },

            // Level 2: Personnel
            createImporter<ChairmanEntity>("chairmen") { database.chairmanDao().insertAll(it) },
            createImporter<ManagersEntity>("managers") { database.managersDao().insertAll(it) },
            createImporter<PlayersEntity>("players") { database.playersDao().insertAll(it) },
            createImporter<StaffEntity>("staff") { database.staffDao().insertAll(it) },
            createImporter<ClubLegendsEntity>("club_legends") { database.clubLegendsDao().insertAll(it) },
            createImporter<CommunityShieldEntity>("community_shield") { database.communityShieldDao().insertAll(it) },
            createImporter<ClubVisionEntity>("club_vision") { database.clubVisionDao().insertAll(it) },
            createImporter<IdentityMappingEntity>("identity_mappings") { database.identityMappingDao().insertAll(it) }
        )
    }

    suspend fun clearAllTables(database: AFMDatabase) = withContext(Dispatchers.IO) {
        database.runInTransaction {
            val tables = listOf(
                "club_legends", "staff", "players", "managers", "chairmen",
                "teams", "national_teams", "player_agents", "club_dna", "cups", "leagues",
                "rivalries", "currency_exchange_rates", "sponsors", "referees", 
                "journalists", "archetype_traits", "personality_types", "regional_settings", 
                "nationalities", "game_settings", "user_preferences", "community_shield",
                "club_vision", "identity_mappings"
            )
            tables.forEach { table ->
                database.openHelper.writableDatabase.execSQL("DELETE FROM $table")
            }
        }
    }
}
