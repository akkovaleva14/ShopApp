package com.example.domain.repositories

import com.example.network.ApiService
import com.example.network.LoginRequest
import com.example.network.LoginResponse
import com.example.network.RegistrationRequest
import com.example.network.RegistrationResponse
import com.example.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Call

class AuthRepository(private val tokenRepository: TokenRepository) {

    // Helper function to fetch the ApiService with a token
    private suspend fun getApiService(): ApiService {
        // Fetch the token in a background thread using the IO dispatcher
        val token = withContext(Dispatchers.IO) {
            tokenRepository.getToken()
        }
        // Create an ApiService with the token
        return RetrofitClient.createApiService { token }
    }

    // Function to log in a user by making a network call
    suspend fun loginUser(request: LoginRequest): Call<LoginResponse> {
        val apiService = getApiService() // Retrieve the service configured with a token
        return apiService.loginUser(request)
    }

    // Function to register a new user by making a network call
    suspend fun registerUser(request: RegistrationRequest): Call<RegistrationResponse> {
        val apiService = getApiService() // Retrieve the service with a token
        return apiService.registerUser(request)
    }
}