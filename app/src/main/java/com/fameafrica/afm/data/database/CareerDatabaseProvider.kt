package com.fameafrica.afm.data.database

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import com.fameafrica.afm.data.database.migrations.MIGRATION_5_6
import com.fameafrica.afm.data.database.migrations.MIGRATION_6_7
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CareerDatabaseProvider @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val staticDatabaseProvider: StaticDatabaseProvider  // Inject this
) {
    @Volatile
    private var activeDatabase: AFMDatabase? = null
    private var currentCareerId: Int = -1

    private val databaseMutex = Mutex()

    fun getCurrentCareerId(): Int = currentCareerId

    fun hasActiveDatabase(): Boolean {
        return activeDatabase != null
    }

    /**
     * Ensures a career database exists and is ready for use
     * Creates it if it doesn't exist with proper schema
     */
    suspend fun ensureCareerDatabase(careerId: Int): AFMDatabase = databaseMutex.withLock {
        Log.d("AFM_DB", "Ensuring career database exists for ID: $careerId")

        // Check if we already have this database active
        if (currentCareerId == careerId && activeDatabase != null) {
            Log.d("AFM_DB", "Career database already active for ID: $careerId")
            return@withLock activeDatabase!!
        }

        // Create or get the career database
        return@withLock switchToCareerDbInternal(careerId)
    }

    /**
     * Switches the active database to the one associated with the given careerId.
     * This is the primary database used by the app's repositories and GameManager.
     */
    suspend fun switchToCareerDb(careerId: Int): AFMDatabase = databaseMutex.withLock {
        return@withLock switchToCareerDbInternal(careerId)
    }

    private fun switchToCareerDbInternal(careerId: Int): AFMDatabase {
        if (currentCareerId == careerId && activeDatabase != null) {
            return activeDatabase!!
        }

        Log.d("AFM_DB", "Switching active database to career_$careerId.db")

        val newDb = buildDatabase(careerId)

        try {
            activeDatabase?.close()
        } catch (e: Exception) {
            Log.e("AFM_DB", "Error closing old database", e)
        }

        activeDatabase = newDb
        currentCareerId = careerId

        return newDb
    }

    /**
     * Returns the active database. Safely falls back to the static database
     * if no career database is currently active.
     */
    fun getActiveDatabase(): AFMDatabase {
        val db = activeDatabase
        if (db != null) {
            return db
        }

        // Log.v because this might happen during initialization
        Log.v("AFM_DB", "No active career database instance. Falling back to static database instance.")
        return staticDatabaseProvider.getStaticDatabase()
    }

    /**
     * Opens a separate database instance for reading metadata without switching the global active context.
     * The caller MUST close this database instance when finished.
     */
    fun openTemporaryDatabase(careerId: Int): AFMDatabase {
        Log.d("AFM_DB", "Opening temporary database for career_$careerId.db")
        return buildDatabase(careerId, forTempUse = true)
    }

    /**
     * Closes the database for the given careerId if it is the active one.
     * Useful for cleanup during deletion.
     */
    suspend fun closeDatabase(careerId: Int) = databaseMutex.withLock {
        if (currentCareerId == careerId) {
            Log.d("AFM_DB", "Closing active database for career_$careerId.db (invalidation/deletion)")
            try {
                activeDatabase?.close()
            } catch (e: Exception) {
                Log.e("AFM_DB", "Error closing database", e)
            } finally {
                activeDatabase = null
                currentCareerId = -1
            }
        }
    }

    /**
     * Switches the active database to the static read-only database.
     * Used for browsing leagues and teams during career setup without creating a save file.
     */
    suspend fun switchToStaticDb(): AFMDatabase = databaseMutex.withLock {
        if (currentCareerId == 0 && activeDatabase != null) {
            return@withLock activeDatabase!!
        }

        Log.d("AFM_DB", "Switching active database to static master data context")

        val newDb = staticDatabaseProvider.getStaticDatabase()

        try {
            activeDatabase?.close()
        } catch (e: Exception) {
            Log.e("AFM_DB", "Error closing old database", e)
        }

        activeDatabase = newDb
        currentCareerId = 0

        return@withLock newDb
    }

    /**
     * Professional Backup logic with Rotation Safety
     */
    suspend fun backupDatabase(careerId: Int) = databaseMutex.withLock {
        val dbPath = context.getDatabasePath("career_$careerId.db")
        val backupPath = File(dbPath.parent, "career_${careerId}_backup.db")
        val tempBackupPath = File(dbPath.parent, "career_${careerId}_backup_temp.db")
        
        if (!dbPath.exists()) return@withLock
        
        try {
            // 1. Copy to temp file first
            dbPath.copyTo(tempBackupPath, overwrite = true)
            
            // 2. Verify temp file exists and has size
            if (tempBackupPath.exists() && tempBackupPath.length() > 0) {
                // 3. Atomic rename/replace
                tempBackupPath.copyTo(backupPath, overwrite = true)
                tempBackupPath.delete()
                Log.d("AFM_DB", "✅ Safe backup rotation completed for career $careerId")
            }
        } catch (e: IOException) {
            Log.e("AFM_DB", "❌ Failed to create safe backup for career $careerId", e)
            if (tempBackupPath.exists()) tempBackupPath.delete()
        }
    }

    /**
     * Attempt to restore from backup
     */
    suspend fun restoreFromBackup(careerId: Int): Boolean = databaseMutex.withLock {
        val dbPath = context.getDatabasePath("career_$careerId.db")
        val backupPath = File(dbPath.parent, "career_${careerId}_backup.db")
        
        if (!backupPath.exists()) return@withLock false
        
        try {
            activeDatabase?.close()
            backupPath.copyTo(dbPath, overwrite = true)
            Log.d("AFM_DB", "✅ Successfully restored career $careerId from backup")
            return@withLock true
        } catch (e: IOException) {
            Log.e("AFM_DB", "❌ Failed to restore backup for career $careerId", e)
            return@withLock false
        }
    }

    private fun buildDatabase(careerId: Int, forTempUse: Boolean = false): AFMDatabase {
        val dbName = "career_$careerId.db"

        val builder = Room.databaseBuilder(
            context.applicationContext,
            AFMDatabase::class.java,
            dbName
        )
            .addMigrations(MIGRATION_5_6, MIGRATION_6_7)
            .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
            .enableMultiInstanceInvalidation()

        // Add callback only for non-temporary databases
        if (!forTempUse) {
            builder.addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    Log.d("AFM_DB", "✅ Career database created for ID: $careerId")
                }

                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                    Log.d("AFM_DB", "✅ Career database opened for ID: $careerId")
                }
            })
        }

        return builder.build()
    }
}