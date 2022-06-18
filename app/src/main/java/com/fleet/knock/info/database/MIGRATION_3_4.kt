package com.fleet.knock.info.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object MIGRATION_3_4 : Migration(3, 4){
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE theme_local ADD COLUMN local_saved_file_name TEXT NOT NULL DEFAULT ''")
    }

}