package com.fleet.knock.info.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object MIGRATION_5_6 : Migration(5, 6){
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE promotion ADD COLUMN promotion_enable_main_banner INTEGER NOT NULL DEFAULT 1")
    }
}