package com.fleet.knock.info.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object MIGRATION_4_5 : Migration(4, 5){
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE theme_frame ADD COLUMN theme_frame_confirm_time INTEGER NOT NULL DEFAULT 0")
    }
}