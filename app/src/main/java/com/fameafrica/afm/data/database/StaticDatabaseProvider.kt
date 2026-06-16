package com.fameafrica.afm.data.database

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import com.fameafrica.afm.data.database.migrations.MIGRATION_5_6
import com.fameafrica.afm.data.database.migrations.MIGRATION_6_7
import androidx.room.withTransaction
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StaticDatabaseProvider @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val importer: RoomDataImporter
) {
    private var database: AFMDatabase? = null
    private val dbName = "afm_static.db"
    private val mutex = Mutex()

    /**
     * Returns the static database instance. 
     * IMPORTANT: This method only builds the Room instance. It does NOT perform 
     * any database access, making it safe to call on the main thread.
     */
    fun getStaticDatabase(): AFMDatabase {
        database?.let { return it }

        return synchronized(this) {
            database?.let { return it }

            Log.d("AFM_DB", "Building static database instance: $dbName")

            val db = Room.databaseBuilder(
                context.applicationContext,
                AFMDatabase::class.java,
                dbName
            )
                .addMigrations(MIGRATION_5_6, MIGRATION_6_7)
                .setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                        super.onCreate(db)
                        Log.d("AFM_DB", "Static database onCreate called (New file created)")
                    }
                    override fun onOpen(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                        super.onOpen(db)
                        Log.d("AFM_DB", "Static database onOpen called")
                    }
                    override fun onDestructiveMigration(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                        super.onDestructiveMigration(db)
                        Log.e("AFM_DB", "⚠️ STATIC DATABASE WIPED DUE TO DESTRUCTIVE MIGRATION!")
                    }
                })
                .build()

            database = db
            db
        }
    }

    /**
     * Ensures the database is initialized and imported from JSON if necessary.
     * This MUST be called from a background thread/coroutine.
     */
    suspend fun ensureInitialized(): AFMDatabase = withContext(Dispatchers.IO) {
        val db = getStaticDatabase()
        val dbFile = context.getDatabasePath(dbName)
        Log.d("AFM_DB", "Static DB file path: ${dbFile.absolutePath}, exists: ${dbFile.exists()}, size: ${if (dbFile.exists()) dbFile.length() else 0} bytes")
        
        mutex.withLock {
            if (isDatabaseEmpty(db)) {
                Log.d("AFM_DB", "Static database is empty. Starting JSON import...")
                
                try {
                    db.withTransaction {
                        val importResult = importer.importFromAssets(db) { table, current, total, count ->
                            Log.d("AFM_DB", "Importing $table ($current/$total): $count records")
                        }
                        if (importResult.isFailure) {
                            Log.e("AFM_DB", "JSON import failed result: ${importResult.exceptionOrNull()?.message}")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("AFM_DB", "JSON import failed with exception: ${e.message}", e)
                }

                if (isDatabaseEmpty(db)) {
                    Log.e("AFM_DB", "❌ Static database remains empty after JSON import!")
                } else {
                    Log.d("AFM_DB", "✅ Static database initialized successfully via JSON.")
                }
                
                // Verify data counts
                try {
                    val leagueCount = db.openHelper.readableDatabase.query("SELECT COUNT(*) FROM leagues").use {
                        if (it.moveToFirst()) it.getInt(0) else 0
                    }
                    val managerCount = db.openHelper.readableDatabase.query("SELECT COUNT(*) FROM managers").use {
                        if (it.moveToFirst()) it.getInt(0) else 0
                    }
                    Log.d("AFM_DB", "Final Verification - Leagues: $leagueCount, Managers: $managerCount")
                } catch (e: Exception) {
                    Log.e("AFM_DB", "Verification query failed", e)
                }
            }
        }
        return@withContext db
    }

    /**
     * Checks if the database has any data in critical tables.
     */
    private fun isDatabaseEmpty(db: AFMDatabase): Boolean {
        return try {
            val leagueCount = db.openHelper.readableDatabase.query("SELECT COUNT(*) FROM leagues").use {
                if (it.moveToFirst()) it.getInt(0) else 0
            }
            val teamCount = db.openHelper.readableDatabase.query("SELECT COUNT(*) FROM teams").use {
                if (it.moveToFirst()) it.getInt(0) else 0
            }
            Log.d("AFM_DB", "Emptiness check - Leagues: $leagueCount, Teams: $teamCount")
            leagueCount == 0 && teamCount == 0
        } catch (e: Exception) {
            Log.w("AFM_DB", "Error checking if database is empty (table might not exist yet): ${e.message}")
            true
        }
    }

    /**
     * Forces a re-import of all data from JSON assets.
     */
    suspend fun forceReimport() = mutex.withLock {
        val db = getStaticDatabase()
        Log.d("AFM_DB", "Forcing re-import of static data...")
        withContext(Dispatchers.IO) {
            db.withTransaction {
                importer.clearAllTables(db)
                importer.importFromAssets(db) { table, current, total, count ->
                    Log.d("AFM_DB", "Re-importing $table ($current/$total): $count records")
                }
            }
        }
    }

    /**
     * Closes the static database when no longer needed
     */
    fun closeStaticDatabase() {
        try {
            database?.close()
            database = null
            Log.d("AFM_DB", "Static database closed")
        } catch (e: Exception) {
            Log.e("AFM_DB", "Error closing static database", e)
        }
    }
}
