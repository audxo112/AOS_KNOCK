package com.fleet.knock.info.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.fleet.knock.info.converter.DateConverter
import com.fleet.knock.info.converter.StringSetConverter
import com.fleet.knock.info.editor.ThemeFrame
import com.fleet.knock.info.editor.ThemeFrameDao
import com.fleet.knock.info.promotion.FProjectEntity
import com.fleet.knock.info.promotion.FProjectDao
import com.fleet.knock.info.promotion.FPromotion
import com.fleet.knock.info.promotion.FPromotionDao
import com.fleet.knock.info.tag.*
import com.fleet.knock.info.theme.*
import com.fleet.knock.info.user.*

@Database(entities = [User::class,
    UserConfig::class,
    FThemeEntity::class,
    FThemeRecommend::class,
    FThemeLocal::class,
    FThemeLike::class,
    FThemeStorage::class,
    FThemeShare::class,
    Tag::class,
    FPromotion::class,
    FProjectEntity::class,
    ThemeFrame::class], version = 6)
@TypeConverters(StringSetConverter::class, DateConverter::class)
abstract class KNOCKDatabase : RoomDatabase(){
    abstract fun userDao() : UserDao

    abstract fun userConfigDao() : UserConfigDao

    abstract fun themeDao() : FThemeDao

    abstract fun themeRecommendDao() : FThemeRecommendDao

    abstract fun themeLocalDao() : FThemeLocalDao

    abstract fun themeLikeDao() : FThemeLikeDao

    abstract fun themeStorageDao() : FThemeStorageDao

    abstract fun tagDao() : TagDao

    abstract fun promotionDao() : FPromotionDao

    abstract fun projectDao() : FProjectDao

    abstract fun themeFrameDao() : ThemeFrameDao

    companion object{
        private var INSTANCE: KNOCKDatabase? = null
        fun get(context: Context) = INSTANCE ?: synchronized(this){
            Room.databaseBuilder(context.applicationContext,
                KNOCKDatabase::class.java, "knock")
                .addMigrations(MIGRATION_1_2)
                .addMigrations(MIGRATION_2_3)
                .addMigrations(MIGRATION_3_4)
                .addMigrations(MIGRATION_4_5)
                .addMigrations(MIGRATION_5_6)
                .build().also {
                    INSTANCE = it
                }
        }
    }
}