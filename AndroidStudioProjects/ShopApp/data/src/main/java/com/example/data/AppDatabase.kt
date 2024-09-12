package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Room database for storing product data
@Database(entities = [ProductEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    // Abstract method to get the DAO for product operations
    abstract fun productDao(): ProductDao

    companion object {
        // Volatile ensures that changes to INSTANCE are immediately visible to other threads
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Singleton pattern to get a single instance of the database
        fun getDatabase(context: Context): AppDatabase {
            // Return the existing instance if it exists
            return INSTANCE ?: synchronized(this) {
                // Create a new instance if it doesn't exist
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).build()
                // Assign the new instance to INSTANCE
                INSTANCE = instance
                instance
            }
        }
    }
}