package com.fameafrica.afm.data.initializer

import android.util.Log
import androidx.room.withTransaction
import com.fameafrica.afm.data.database.CareerDatabaseProvider
import com.fameafrica.afm.data.database.RoomDataImporter
import com.fameafrica.afm.data.database.entities.*
import com.fameafrica.afm.domain.manager.PlayerGenerator
import com.fameafrica.afm.domain.manager.StaffGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameInitializer @Inject constructor(
    private val databaseProvider: CareerDatabaseProvider,
    private val importer: RoomDataImporter,
    private val playerGenerator: PlayerGenerator,
    private val staffGenerator: StaffGenerator
) {
    /**
     * Entry point for starting a new career.
     * Imports data from JSON assets and populates the Room Career DB.
     *
     * @param careerId The slot ID to use for this career.
     * @param managerName The name of the manager provided by the user.
     * @param selectedTeamId The ID of the team chosen by the user.
     * @param onProgress Callback to report progress (0.0 to 1.0) and status message.
     */
    suspend fun initializeNewCareer(
        careerId: Int,
        managerName: String,
        selectedTeamId: Int,
        onProgress: (Float, String) -> Unit = { _, _ -> }
    ) = withContext(Dispatchers.IO) {
        val careerDb = databaseProvider.switchToCareerDb(careerId)
        val totalSteps = 12.0f // Increased steps
        var currentStep = 0f

        fun updateProgress(message: String, delta: Float = 1f) {
            currentStep += delta
            onProgress(currentStep / totalSteps, message)
            Log.d("AFM_INIT", "Progress: ${currentStep / totalSteps * 100}% - $message")
        }

        try {
            Log.d("AFM_INIT", "Starting Career ETL Process on database: ${careerDb.openHelper.databaseName}")
            onProgress(0f, "Initializing database...")

            careerDb.withTransaction {
                Log.d("AFM_INIT", "Transaction started for data migration...")

                // 1. Bulk Import from JSON
                updateProgress("Importing World Data from JSON...")
                val importResult = importer.importFromAssets(careerDb) { table, current, total, count ->
                    // Optional: detailed progress
                    Log.d("AFM_INIT", "Imported table $table ($current/$total)")
                }
                
                if (importResult.isFailure) {
                    throw importResult.exceptionOrNull() ?: Exception("Unknown import error")
                }

                updateProgress("World Data Imported.", 1f)

                // 2. Handle Manager Displacement and User Setup
                updateProgress("Setting up User Profile...")
                val selectedTeam = careerDb.teamsDao().getById(selectedTeamId)

                // 2a. Handle ID conflict
                val conflictManager = careerDb.managersDao().getById(selectedTeamId)
                if (conflictManager != null) {
                    val newId = careerDb.managersDao().insert(conflictManager.copy(id = 0)).toInt()

                    val teamManagedByConflict = careerDb.teamsDao().getTeamByManager(conflictManager.id)
                    if (teamManagedByConflict != null) {
                        careerDb.teamsDao().update(teamManagedByConflict.copy(managerId = newId))
                        careerDb.playersDao().updatePlayersManager(teamManagedByConflict.id, newId)
                    }

                    val ntManagedByConflict = careerDb.nationalTeamsDao().getTeamByManager(conflictManager.id)
                    if (ntManagedByConflict != null) {
                        careerDb.nationalTeamsDao().assignManager(ntManagedByConflict.id, newId)
                    }

                    careerDb.managersDao().delete(conflictManager)
                }

                // 2b. Handle incumbent manager
                val incumbentManager = careerDb.managersDao().getManagerByTeam(selectedTeamId)
                if (incumbentManager != null) {
                    careerDb.managersDao().update(incumbentManager.copy(teamId = null))
                }

                // 2c. Link selected team to user
                if (selectedTeam != null) {
                    careerDb.teamsDao().update(selectedTeam.copy(managerId = null))
                }

                // 2d. Resolve Rivalries
                updateProgress("Mapping Team Rivalries...")

                val allTeams = careerDb.teamsDao().getAllStatic()
                val clubRivalMappings = allTeams
                    .filter { !it.rivalTeam.isNullOrEmpty() }
                    .mapNotNull { team ->
                        val rivalId = careerDb.teamsDao().getByName(team.rivalTeam!!)?.id
                        if (rivalId != null) {
                            IdentityMappingEntity(
                                sourceName = team.name,
                                targetId = rivalId,
                                category = "TEAM_RIVAL"
                            )
                        } else null
                    }
                careerDb.identityMappingDao().insertAll(clubRivalMappings)

                val allNationalTeams = careerDb.nationalTeamsDao().getAllStatic()
                val nationalRivalMappings = allNationalTeams
                    .filter { !it.rivalTeam.isNullOrEmpty() }
                    .mapNotNull { team ->
                        val rivalId = careerDb.nationalTeamsDao().getByName(team.rivalTeam!!)?.id
                        if (rivalId != null) {
                            IdentityMappingEntity(
                                sourceName = team.name,
                                targetId = rivalId,
                                category = "NATIONAL_RIVAL"
                            )
                        } else null
                    }
                careerDb.identityMappingDao().insertAll(nationalRivalMappings)

                // 2e. Generate Missing Players
                updateProgress("Populating World with Players...")
                playerGenerator.generateMissingPlayers { progress ->
                    // We don't want to spam the UI with 877 updates, but maybe every 10%
                    if ((progress * 100).toInt() % 10 == 0) {
                        Log.d("AFM_INIT", "Player Generation: ${(progress * 100).toInt()}%")
                    }
                }

                // 2f. Generate Default Staff
                updateProgress("Hiring Club Staff...")
                staffGenerator.generateDefaultStaffForAllClubs { progress ->
                    if ((progress * 100).toInt() % 10 == 0) {
                        Log.d("AFM_INIT", "Staff Generation: ${(progress * 100).toInt()}%")
                    }
                }

                // 3. Finalizing
                updateProgress("Finalizing Setup...")
                Log.d("AFM_INIT", "Career ETL Process Success!")
                onProgress(1.0f, "African Football Universe Ready!")
            }
        } catch (e: Exception) {
            Log.e("AFM_INIT", "CRITICAL ERROR during initialization", e)
            onProgress(-1f, "Error: ${e.message}")
            throw e
        }
    }
}