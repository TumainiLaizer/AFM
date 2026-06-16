package com.fameafrica.afm.domain.manager

import android.content.Context
import android.util.Log
import com.fameafrica.afm.data.database.CareerDatabaseProvider
import com.fameafrica.afm.data.database.StaticDatabaseProvider
import com.fameafrica.afm.data.repository.*
import com.fameafrica.afm.data.database.entities.*
import com.fameafrica.afm.data.model.CareerSaveModel
import com.fameafrica.afm.ui.theme.FootballThemePreset
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay

@Singleton
class CareerManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val databaseProvider: CareerDatabaseProvider,
    private val staticDatabaseProvider: StaticDatabaseProvider,
    private val gameManager: GameManager,
    private val gameStatesRepository: GameStatesRepository,
    private val managersRepository: ManagersRepository,
    private val chairmanRepository: ChairmanRepository,
    private val teamsRepository: TeamsRepository,
    private val newsRepository: NewsRepository,
    private val notificationsRepository: NotificationsRepository,
    private val objectivesRepository: ObjectivesRepository,
    private val leaguesRepository: LeaguesRepository,
    private val gameSettingsRepository: GameSettingsRepository,
    private val worldSimulationEngine: WorldSimulationEngine,
    private val storyEngine: StoryEngine,
    private val playersRepository: PlayersRepository
) {

    // Track currently loaded career
    private var currentCareerId: Int? = null
    private val careerCreationMutex = Mutex()

    /**
     * Ensures the correct database is active for the given career
     * Creates the database if it doesn't exist
     */
    suspend fun ensureCareerDatabase(careerId: Int) = withContext(Dispatchers.IO) {
        Log.d("AFM_CAREER", "Ensuring career database exists for ID: $careerId")

        try {
            // This will create the database file if it doesn't exist
            databaseProvider.ensureCareerDatabase(careerId)
            currentCareerId = careerId

            // Small delay to ensure database is ready
            delay(100)

            Log.d("AFM_CAREER", "✅ Career database ready for ID: $careerId")
        } catch (e: Exception) {
            Log.e("AFM_CAREER", "❌ Failed to ensure career database for ID: $careerId", e)
            throw e
        }
    }

    /**
     * Switches the global database context to the static template for browsing.
     */
    suspend fun switchToStaticUniverse() {
        Log.d("AFM_CAREER", "Switching to static universe for setup browsing")
        
        // Ensure static DB is initialized/imported if needed
        staticDatabaseProvider.ensureInitialized()

        databaseProvider.switchToStaticDb()
        currentCareerId = null // Reset current career when switching to static
    }

    /**
     * Finalizes a career by setting up the manager, starting turn, and saving metadata.
     * Assumes the database has already been created by ensureCareerDatabase
     */
    suspend fun createNewCareer(
        careerId: Int,
        managerName: String,
        managerAge: Int,
        nationality: String,
        coachingLicense: String,
        managerStyle: String,
        preferredFormation: String,
        youthDevelopment: Int,
        mediaHandling: Int,
        tacticalFlexibility: Int,
        playerMotivation: Int,
        disciplineLevel: Int,
        adaptability: Int,
        teamId: Int,
        difficulty: String,
        selectedAvatar: String? = null,
        agentId: Int? = null,
        careerVision: String? = null,
        personalityProfile: String? = null
    ): Int = careerCreationMutex.withLock {
        Log.d("AFM_CAREER", "Finalizing new career with ID: $careerId")

        // 1. Preparation Phase (Cancellable)
        val gameStateId: Int = try {
            withContext(Dispatchers.IO) {
                // Ensure database exists and is active
                ensureCareerDatabase(careerId)

                val team = teamsRepository.getTeamById(teamId)
                val teamName = team?.name ?: "Unknown Team"
                val league = team?.league?.let { leaguesRepository.getLeagueByName(it) }

                val manager = ManagersEntity(
                    name = managerName,
                    nationality = nationality,
                    age = managerAge,
                    teamId = teamId,
                    coachingLicense = coachingLicense,
                    reputation = 30,
                    reputationLevel = "Local",
                    preferredFormation = preferredFormation,
                    style = managerStyle,
                    youthDevelopmentFocus = youthDevelopment,
                    mediaHandling = mediaHandling,
                    tacticalFlexibility = tacticalFlexibility,
                    playerMotivation = playerMotivation,
                    disciplineLevel = disciplineLevel,
                    adaptability = adaptability,
                    faceImage = selectedAvatar,
                    agentId = agentId,
                    careerVision = careerVision,
                    personalityProfile = personalityProfile
                )
                val managerId = managersRepository.insertManager(manager)

                teamsRepository.assignManager(teamId, managerId.toInt())
                playersRepository.updatePlayersManager(teamId, managerId.toInt())

                val gameState = gameStatesRepository.createNewSave(
                    managerId = managerId.toInt(),
                    managerName = managerName,
                    teamId = teamId,
                    teamName = teamName,
                    saveName = "$managerName's $teamName Career"
                )

                objectivesRepository.generateSeasonObjectives(
                    teamId = teamId,
                    teamName = teamName,
                    season = gameState.season,
                    leagueLevel = league?.level ?: 1,
                    clubReputation = team?.reputation ?: 50
                )

                newsRepository.createNewsArticle(
                    headline = "Welcome to $teamName!",
                    content = "New manager $managerName has officially taken over at $teamName.",
                    category = "CLUB",
                    relatedTeamId = teamId,
                    relatedTeam = teamName
                )

                notificationsRepository.insertNotification(
                    NotificationsEntity.createBoardMessageNotification(
                        title = "Welcome Boss!",
                        message = "Welcome to $teamName. The board has set your objectives for the season.",
                        priority = 1,
                        userId = managerId.toInt()
                    )
                )

                gameManager.initializeGame(careerId)

                worldSimulationEngine.updateWorldState(gameState.week, gameState.season)
                worldSimulationEngine.updateLeagueContexts(gameState.week)
                storyEngine.processWeeklyStories(gameState.week, gameState.season, teamId)

                val theme = FootballThemePreset.MANAGER_MODE
                val settings = gameSettingsRepository.getSettingsSync() ?: GameSettingsEntity()
                gameSettingsRepository.updateSettings(settings.copy(themePreset = theme))
                
                gameState.id
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("AFM_CAREER", "❌ Failed to create career $careerId during preparation", e)
            throw e
        }

        // 2. Final Critical Phase (NonCancellable)
        withContext(NonCancellable + Dispatchers.IO) {
            try {
                // MARK SETUP COMPLETE - ATOMIC SUCCESS
                databaseProvider.getActiveDatabase().gameStatesDao().markSetupComplete(gameStateId)
                databaseProvider.backupDatabase(careerId)
                Log.d("AFM_CAREER", "✅ Career $careerId finalized successfully (Atomic)")
            } catch (e: Exception) {
                Log.e("AFM_CAREER", "❌ Critical failure during career finalization", e)
                throw e
            }
        }
        
        careerId
    }

    suspend fun loadCareer(careerId: Int) {
        Log.d("AFM_CAREER", "Loading career $careerId")

        try {
            // Switch to the career database
            ensureCareerDatabase(careerId)

            // Then initialize the game manager
            gameManager.ensureInitialized(careerId)

            Log.d("AFM_CAREER", "✅ Career $careerId loaded successfully")
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("AFM_CAREER", "❌ Failed to load career $careerId", e)
            throw e
        }
    }

    /**
     * Get the currently loaded career ID
     */
    fun getCurrentCareerId(): Int? = currentCareerId

    /**
     * Check if a career is currently loaded
     */
    fun isCareerLoaded(): Boolean = currentCareerId != null

    /**
     * Deletes a career database and its associated files.
     * Ensures the database is closed before deletion if it's currently active.
     */
    suspend fun deleteCareer(careerId: Int) {
        Log.d("AFM_CAREER", "Deleting career $careerId")

        // 1. Close the database if it's currently active to prevent file locks
        databaseProvider.closeDatabase(careerId)

        // 2. Delete the physical files
        withContext(Dispatchers.IO) {
            val dbFile = context.getDatabasePath("career_$careerId.db")
            if (dbFile.exists()) {
                dbFile.delete()
                File(dbFile.path + "-shm").delete()
                File(dbFile.path + "-wal").delete()
                File(dbFile.path + "-journal").delete()
                Log.d("AFM_CAREER", "Deleted database files for career $careerId")
            }
        }

        // Reset current career if this was the active one
        if (currentCareerId == careerId) {
            currentCareerId = null
        }
    }

    /**
     * Lists all saved careers by scanning for career databases.
     * Uses temporary database to prevent global state corruption
     */
    suspend fun listCareers(): List<CareerSaveModel> = withContext(Dispatchers.IO) {
        val dbDir = context.getDatabasePath("dummy").parentFile ?: return@withContext emptyList()
        val careerFiles = dbDir.listFiles { _, name -> name.startsWith("career_") && name.endsWith(".db") } ?: emptyArray()

        careerFiles.mapNotNull { file ->
            var tempDb: com.fameafrica.afm.data.database.AFMDatabase? = null
            try {
                val careerIdStr = file.name.removePrefix("career_").removeSuffix(".db")
                val careerId = careerIdStr.toIntOrNull() ?: return@mapNotNull null

                tempDb = databaseProvider.openTemporaryDatabase(careerId)
                val states = tempDb.gameStatesDao().getValidSaveGames().firstOrNull()
                val state = states?.firstOrNull()

                if (state != null) {
                    val manager = tempDb.managersDao().getById(state.managerId)
                    CareerSaveModel(
                        careerId = careerId,
                        managerId = state.managerId,
                        managerName = state.managerName,
                        managerAvatar = manager?.faceImage,
                        teamId = state.teamId,
                        teamName = state.teamName,
                        season = state.season,
                        week = state.week,
                        gameDate = state.lastPlayed ?: "",
                        difficulty = "Normal",
                        lastPlayed = state.lastPlayed ?: "",
                        saveName = state.name,
                        gameVersion = state.gameVersion ?: "1.0.0"
                    )
                } else {
                    null
                }
            } catch (e: Exception) {
                Log.e("AFM_CAREER", "Failed to read career file: ${file.name}", e)
                null
            } finally {
                tempDb?.close()
            }
        }.sortedByDescending { it.lastPlayed }
    }
}