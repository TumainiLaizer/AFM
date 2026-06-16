package com.fameafrica.afm.data.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration from version 5 to 6.
 * Adds fields for Continental Logistics & Tiered Simulation:
 * - Nationalities: caf_zone, reputation_stars, latitude, longitude
 * - Teams: nation_id, latitude, longitude, is_playable
 * - Leagues: simulation_tier
 */
val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Update Nationalities Table
        db.execSQL("ALTER TABLE nationalities ADD COLUMN caf_zone TEXT NOT NULL DEFAULT 'CECAFA'")
        db.execSQL("ALTER TABLE nationalities ADD COLUMN reputation_stars INTEGER NOT NULL DEFAULT 1")
        db.execSQL("ALTER TABLE nationalities ADD COLUMN latitude REAL")
        db.execSQL("ALTER TABLE nationalities ADD COLUMN longitude REAL")

        // Update Teams Table
        db.execSQL("ALTER TABLE teams ADD COLUMN nation_id INTEGER")
        db.execSQL("ALTER TABLE teams ADD COLUMN latitude REAL")
        db.execSQL("ALTER TABLE teams ADD COLUMN longitude REAL")
        db.execSQL("ALTER TABLE teams ADD COLUMN is_playable INTEGER NOT NULL DEFAULT 1")

        // Update Leagues Table
        db.execSQL("ALTER TABLE leagues ADD COLUMN simulation_tier INTEGER NOT NULL DEFAULT 0")
    }
}
