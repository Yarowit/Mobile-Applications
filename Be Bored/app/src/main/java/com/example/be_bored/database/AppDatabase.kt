package com.example.be_bored.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Stats::class, Goals::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): DatabaseDao
}
