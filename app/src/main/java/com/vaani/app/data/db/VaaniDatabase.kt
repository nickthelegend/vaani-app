package com.vaani.app.data.db

import androidx.room.*
import android.content.Context

@Database(entities = [TaskEntity::class], version = 1, exportSchema = false)
abstract class VaaniDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var INSTANCE: VaaniDatabase? = null

        fun getDatabase(context: Context): VaaniDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VaaniDatabase::class.java,
                    "vaani_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
