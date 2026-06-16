package com.fameafrica.afm.data.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration from version 6 to 7.
 * - Adds tables: training_schedules, training_days, rankings_cache, chairmen, rivalries, player_form, club_dna, world_state, league_context
 * - Updates staff table default name to 'J.Nsajigwa'
 */
val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 1. Create new tables
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `training_schedules` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                `team_id` INTEGER NOT NULL, 
                `month` INTEGER NOT NULL, 
                `year` INTEGER NOT NULL, 
                `is_approved` INTEGER NOT NULL DEFAULT 0, 
                `global_intensity` TEXT NOT NULL DEFAULT 'NORMAL', 
                `primary_focus` TEXT NOT NULL DEFAULT 'BALANCED', 
                `generated_by` TEXT
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_training_schedules_team_id` ON `training_schedules` (`team_id`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_training_schedules_month_year` ON `training_schedules` (`month`, `year`)")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `training_days` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                `schedule_id` INTEGER NOT NULL, 
                `date` TEXT NOT NULL, 
                `morning_session` TEXT NOT NULL, 
                `afternoon_session` TEXT NOT NULL, 
                `evening_session` TEXT NOT NULL, 
                `intensity_modifier` REAL NOT NULL, 
                `recovery_level` INTEGER NOT NULL, 
                FOREIGN KEY(`schedule_id`) REFERENCES `training_schedules`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE 
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_training_days_schedule_id` ON `training_days` (`schedule_id`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_training_days_date` ON `training_days` (`date`)")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `rankings_cache` (
                `type` TEXT PRIMARY KEY NOT NULL, 
                `json_data` TEXT NOT NULL, 
                `last_updated` INTEGER NOT NULL
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `chairmen` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                `team_id` INTEGER, 
                `name` TEXT NOT NULL, 
                `nationality` TEXT NOT NULL, 
                `age` INTEGER NOT NULL, 
                `wealth_level` INTEGER NOT NULL, 
                `patience_level` INTEGER NOT NULL, 
                `business_skill` INTEGER NOT NULL, 
                `football_knowledge` INTEGER NOT NULL, 
                `ambition_level` INTEGER NOT NULL, 
                `is_available` INTEGER NOT NULL DEFAULT 1, 
                `preferred_region` TEXT, 
                `personality_type` TEXT, 
                `entry_mode` TEXT, 
                `fan_trust` INTEGER NOT NULL DEFAULT 50, 
                `board_pressure` INTEGER NOT NULL DEFAULT 50, 
                `club_vision_id` INTEGER, 
                `total_investment` INTEGER NOT NULL, 
                `hire_date` INTEGER, 
                FOREIGN KEY(`team_id`) REFERENCES `teams`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL 
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_chairmen_team_id` ON `chairmen` (`team_id`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_chairmen_is_available` ON `chairmen` (`is_available`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_chairmen_nationality` ON `chairmen` (`nationality`)")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `rivalries` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                `team_a_id` INTEGER NOT NULL, 
                `team_b_id` INTEGER NOT NULL, 
                `rivalry_name` TEXT NOT NULL, 
                `intensity` REAL NOT NULL, 
                `last_results` TEXT NOT NULL
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `player_form` (
                `playerId` INTEGER PRIMARY KEY NOT NULL, 
                `last_5_ratings` TEXT NOT NULL, 
                `goals_last_5` INTEGER NOT NULL, 
                `assists_last_5` INTEGER NOT NULL, 
                `cleansheets_last_5` INTEGER NOT NULL, 
                `form_status` TEXT NOT NULL
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `club_dna` (
                `teamId` INTEGER PRIMARY KEY NOT NULL, 
                `region` TEXT NOT NULL, 
                `play_style` TEXT NOT NULL, 
                `play_style_secondary` TEXT, 
                `identity_strength` INTEGER NOT NULL DEFAULT 50, 
                `transfer_policy` TEXT NOT NULL, 
                `financial_behavior` TEXT NOT NULL, 
                `youth_priority` INTEGER NOT NULL, 
                FOREIGN KEY(`teamId`) REFERENCES `teams`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE 
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `world_state` (
                `id` INTEGER PRIMARY KEY NOT NULL, 
                `continental_rankings` TEXT NOT NULL, 
                `league_reputation` TEXT NOT NULL, 
                `dominant_clubs` TEXT NOT NULL, 
                `rising_clubs` TEXT NOT NULL, 
                `fallen_giants` TEXT NOT NULL, 
                `club_rankings` TEXT NOT NULL DEFAULT '[]', 
                `last_updated_week` INTEGER NOT NULL, 
                `season` TEXT NOT NULL
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `league_context` (
                `leagueName` TEXT PRIMARY KEY NOT NULL, 
                `title_race_teams` TEXT NOT NULL, 
                `relegation_battle_teams` TEXT NOT NULL, 
                `top_4_race_teams` TEXT NOT NULL, 
                `surprise_team_id` INTEGER, 
                `underperforming_team_id` INTEGER, 
                `last_updated_week` INTEGER NOT NULL
            )
        """.trimIndent())

        // 2. Recreate staff table to update default value of 'name'
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `staff_new` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                `name` TEXT NOT NULL DEFAULT 'J.Nsajigwa', 
                `role` TEXT NOT NULL, 
                `staff_type` TEXT NOT NULL, 
                `team_id` INTEGER NOT NULL, 
                `team_name` TEXT NOT NULL, 
                `specialization` TEXT NOT NULL DEFAULT 'General', 
                `impact_rating` INTEGER NOT NULL DEFAULT 70, 
                `salary` INTEGER NOT NULL DEFAULT 1200000, 
                `experience_level` INTEGER NOT NULL DEFAULT 0, 
                `face_image` TEXT, 
                `previous_player_id` INTEGER, 
                `previous_player` TEXT, 
                `nationality` TEXT, 
                `age` INTEGER, 
                `contract_end_date` TEXT, 
                `is_head_of_department` INTEGER NOT NULL, 
                `mentoring_ability` INTEGER NOT NULL, 
                `loyalty` INTEGER NOT NULL, 
                `adaptability` INTEGER NOT NULL
            )
        """.trimIndent())
        
        db.execSQL("""
            INSERT INTO `staff_new` (
                `id`, `name`, `role`, `staff_type`, `team_id`, `team_name`, `specialization`, 
                `impact_rating`, `salary`, `experience_level`, `face_image`, `previous_player_id`, 
                `previous_player`, `nationality`, `age`, `contract_end_date`, `is_head_of_department`, 
                `mentoring_ability`, `loyalty`, `adaptability`
            ) 
            SELECT 
                `id`, `name`, `role`, `staff_type`, `team_id`, `team_name`, `specialization`, 
                `impact_rating`, `salary`, `experience_level`, `face_image`, `previous_player_id`, 
                `previous_player`, `nationality`, `age`, `contract_end_date`, `is_head_of_department`, 
                `mentoring_ability`, `loyalty`, `adaptability` 
            FROM `staff`
        """.trimIndent())
        
        db.execSQL("DROP TABLE `staff`")
        db.execSQL("ALTER TABLE `staff_new` RENAME TO `staff`")
        
        // Recreate indices for staff
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_staff_team_id` ON `staff` (`team_id`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_staff_role` ON `staff` (`role`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_staff_specialization` ON `staff` (`specialization`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_staff_impact_rating` ON `staff` (`impact_rating`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_staff_experience_level` ON `staff` (`experience_level`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_staff_previous_player_id` ON `staff` (`previous_player_id`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_staff_staff_type` ON `staff` (`staff_type`)")
    }
}
