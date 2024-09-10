package com.example.domain.repositories

import android.util.Log
import com.example.data.TokenDao
import com.example.data.TokenEntity

class TokenRepository(private val tokenDao: TokenDao) {

    companion object {
        private const val TAG = "TokenRepository"
    }

    suspend fun saveToken(token: String) {
        Log.d(TAG, "Saving token: $token")
        try {
            tokenDao.insert(TokenEntity(token = token))
            Log.d(TAG, "Token saved successfully: $token")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving token: ${e.message}")
        }
    }

    suspend fun getToken(): String? {
        Log.d(TAG, "Retrieving token")
        return try {
            val tokenEntity = tokenDao.getToken()
            val token = tokenEntity?.token
            Log.d(TAG, "Token retrieved successfully: $token")
            token
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving token: ${e.message}")
            null
        }
    }
}