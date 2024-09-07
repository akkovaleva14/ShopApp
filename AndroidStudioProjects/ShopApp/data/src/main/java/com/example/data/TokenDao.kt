package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TokenDao {
    @Insert
    suspend fun insert(tokenEntity: TokenEntity)

    @Insert
    suspend fun insertToken(token: TokenEntity)

    @Query("SELECT * FROM tokens LIMIT 1")
    suspend fun getToken(): TokenEntity?
}