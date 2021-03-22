package com.example.c_tracker.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = arrayOf(ReachedPrefecture::class, ReachedCity::class), version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun reachedPrefectureDao(): ReachedPrefectureDao
    abstract fun reachedCityDao(): ReachedCityDao

    companion object {
        fun getInstance(applicationContext: Context): AppDatabase {
            return Room.databaseBuilder(
                applicationContext,
                AppDatabase::class.java, "city-tracker"
            ).build()
        }
    }
}