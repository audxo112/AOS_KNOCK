package com.fleet.knock.info.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object MIGRATION_1_2 : Migration(1, 2){
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("""
            CREATE TABLE new_promotion (
                promotion_id TEXT PRIMARY KEY NOT NULL,
                promotion_title TEXT NOT NULL DEFAULT '',
                promotion_announce TEXT NOT NULL DEFAULT '',
                promotion_event_link TEXT NOT NULL DEFAULT '',
                promotion_banner_file_name TEXT NOT NULL DEFAULT '',
                promotion_banner_ratio TEXT NOT NULL DEFAULT '',
                promotion_update_banner INTEGER NOT NULL DEFAULT 0,
                promotion_update_banner_time INTEGER NOT NULL DEFAULT 0,
                promotion_main_file_name TEXT NOT NULL DEFAULT '',
                promotion_main_ratio TEXT NOT NULL DEFAULT '',
                promotion_update_main INTEGER NOT NULL DEFAULT 0,
                promotion_update_main_time INTEGER NOT NULL DEFAULT 0,
                promotion_priority INTEGER NOT NULL DEFAULT 0,
                promotion_update_time INTEGER NOT NULL DEFAULT 0,
                promotion_registered_time INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent())

        database.execSQL("""
            INSERT INTO new_promotion (
                promotion_id,
                promotion_title,
                promotion_announce,
                promotion_event_link,
                promotion_banner_file_name,
                promotion_main_file_name,
                promotion_priority,
                promotion_registered_time
            ) 
            SELECT 
                promotion_id,
                promotion_title,
                promotion_announce,
                promotion_event_link,
                promotion_banner_file_name,
                promotion_main_file_name,
                promotion_priority,
                promotion_registered_time
            FROM promotion
            """.trimIndent())
        database.execSQL("DROP TABLE promotion")
        database.execSQL("ALTER TABLE new_promotion RENAME TO promotion")

        database.execSQL("""
            CREATE TABLE new_theme (
                theme_id TEXT PRIMARY KEY NOT NULL,
                theme_promotion_id TEXT NOT NULL DEFAULT '',
                theme_project_id TEXT NOT NULL DEFAULT '',
                theme_resource_thumbnail_file_name TEXT NOT NULL DEFAULT '',
                theme_resource_update_thumbnail INTEGER NOT NULL DEFAULT 0,
                theme_resource_update_thumbnail_time INTEGER NOT NULL DEFAULT 0,
                theme_resource_preload_file_name TEXT NOT NULL DEFAULT '',
                theme_resource_update_preload INTEGER NOT NULL DEFAULT 0,
                theme_resource_theme_file_name TEXT NOT NULL DEFAULT '',
                theme_resource_update_theme INTEGER NOT NULL DEFAULT 0,
                theme_resource_offering_screen TEXT NOT NULL DEFAULT '',
                theme_info_title TEXT NOT NULL DEFAULT '',
                theme_info_content TEXT NOT NULL DEFAULT '',
                theme_info_hash_tag TEXT NOT NULL DEFAULT '',
                theme_info_provide_type TEXT NOT NULL DEFAULT '',
                theme_info_link TEXT NOT NULL DEFAULT '',
                theme_info_price INTEGER NOT NULL DEFAULT 0,
                theme_info_provision_day INTEGER NOT NULL DEFAULT 0,
                theme_allow_post INTEGER NOT NULL DEFAULT 0,
                theme_allow_post_start_time INTEGER NOT NULL DEFAULT 0,
                theme_apply_count INTEGER NOT NULL DEFAULT 0,
                theme_like_count INTEGER NOT NULL DEFAULT 0,
                theme_owner_uid TEXT NOT NULL DEFAULT '',
                theme_update_time INTEGER NOT NULL DEFAULT 0,
                theme_registered_time INTEGER NOT NULL DEFAULT 0,
                theme_post_start_time INTEGER NOT NULL DEFAULT 0,
                theme_post_end_time INTEGER NOT NULL DEFAULT 0
            )""".trimIndent())

        database.execSQL("""
            INSERT INTO new_theme (
                theme_id,
                theme_promotion_id,
                theme_project_id,
                theme_resource_thumbnail_file_name,
                theme_resource_preload_file_name,
                theme_resource_theme_file_name,
                theme_resource_offering_screen,
                theme_info_title,
                theme_info_content,
                theme_info_hash_tag,
                theme_info_provide_type,
                theme_info_price,
                theme_info_provision_day,
                theme_allow_post,
                theme_allow_post_start_time,
                theme_apply_count,
                theme_like_count,
                theme_owner_uid,
                theme_registered_time,
                theme_post_start_time,
                theme_post_end_time
            )
            SELECT
                theme_id,
                theme_promotion_id,
                theme_project_id,
                theme_resource_thumbnail_file_name,
                theme_resource_preload_file_name,
                theme_resource_theme_file_name,
                theme_resource_offering_screen,
                theme_info_title,
                theme_info_content,
                theme_info_hash_tag,
                theme_info_provide_type,
                theme_info_price,
                theme_info_provision_day,
                theme_allow_post,
                theme_allow_post_start_time,
                theme_apply_count,
                theme_like_count,
                theme_owner_uid,
                theme_registered_time,
                theme_post_start_time,
                theme_post_end_time
            FROM theme
            """.trimIndent())
        database.execSQL("DROP TABLE theme")
        database.execSQL("ALTER TABLE new_theme RENAME TO theme")
    }
}