package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TokenDao {
    @Insert
    suspend fun insert(tokenEntity: TokenEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertToken(tokenEntity: TokenEntity)

    @Query("SELECT * FROM tokens LIMIT 1")
    suspend fun getToken(): TokenEntity?
}