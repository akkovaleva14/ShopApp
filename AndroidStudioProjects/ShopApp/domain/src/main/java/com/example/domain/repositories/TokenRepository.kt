package com.example.domain.repositories

import android.util.Log
import com.example.data.TokenDao
import com.example.data.TokenEntity

class TokenRepository(private val tokenDao: TokenDao) {

    companion object {
        private const val TAG = "TokenRepository"
    }

    // Function to save a token to the database.
    suspend fun saveToken(token: String) {
        Log.d(TAG, "Saving token: $token")
        try {
            tokenDao.insert(TokenEntity(token = token)) // Insert the token into the database.
            Log.d(TAG, "Token saved successfully: $token")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving token: ${e.message}") // Log any error that occurs.
        }
    }

    // Function to retrieve the token from the database.
    suspend fun getToken(): String? {
        Log.d(TAG, "Retrieving token")
        return try {
            val tokenEntity = tokenDao.getToken() // Fetch the token entity from the database.
            val token = tokenEntity?.token // Extract the token from the entity.
            Log.d(TAG, "Token retrieved successfully: $token")
            token // Return the token.
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving token: ${e.message}") // Log any error that occurs.
            null // Return null if an error occurs.
        }
    }
}