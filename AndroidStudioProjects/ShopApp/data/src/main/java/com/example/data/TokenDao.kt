package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

// Data Access Object (DAO) interface for managing token data in the database
@Dao
interface TokenDao {

    // Inserts a token into the database
    @Insert
    suspend fun insert(tokenEntity: TokenEntity)

    // Retrieves the first token from the database, or returns null if no token exists
    @Query("SELECT * FROM tokens LIMIT 1")
    suspend fun getToken(): TokenEntity?
}