package com.fleet.knock.info.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object MIGRATION_2_3 : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("""
            CREATE TABLE theme_frame (
                theme_frame_id TEXT PRIMARY KEY NOT NULL,
                theme_frame_title TEXT NOT NULL DEFAULT '',
                theme_frame_type TEXT NOT NULL DEFAULT '',
                theme_frame_priority INTEGER NOT NULL DEFAULT '',
                theme_frame_scale_type TEXT NOT NULL DEFAULT '',
                theme_frame_repeat_mode TEXT NOT NULL DEFAULT '',
                theme_frame_offering_screen TEXT NOT NULL DEFAULT '',
                theme_frame_mini_thumbnail_ext TEXT NOT NULL DEFAULT '',
                theme_frame_mini_thumbnail_update INTEGER NOT NULL DEFAULT 0,
                theme_frame_thumbnail_ext TEXT NOT NULL DEFAULT '',
                theme_frame_thumbnail_update INTEGER NOT NULL DEFAULT 0,
                theme_frame_frame_ext TEXT NOT NULL DEFAULT '',
                theme_frame_frame_update INTEGER NOT NULL DEFAULT 0,
                theme_frame_update_time INTEGER NOT NULL DEFAULT 0,
                theme_frame_recent_used_time INTEGER NOT NULL DEFAULT 0,
                theme_frame_registered_time INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())
    }
}
