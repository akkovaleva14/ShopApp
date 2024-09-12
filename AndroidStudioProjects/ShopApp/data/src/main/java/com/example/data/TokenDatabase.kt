package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Room database for storing tokens, with a single table for TokenEntity
@Database(entities = [TokenEntity::class], version = 1)
abstract class TokenDatabase : RoomDatabase() {

    // Abstract method to get the TokenDao object for database operations
    abstract fun tokenDao(): TokenDao

    companion object {
        @Volatile
        private var INSTANCE: TokenDatabase? = null

        // Returns an instance of the TokenDatabase, creating it if necessary
        fun getDatabase(context: Context): TokenDatabase {
            return INSTANCE ?: synchronized(this) {
                // Create the database using Room if it doesn't already exist
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TokenDatabase::class.java,
                    "token_database" // Name of the database file
                ).build()
                INSTANCE = instance // Cache the instance
                instance
            }
        }
    }
}