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

    private suspend fun getApiService(): ApiService {
        val token = withContext(Dispatchers.IO) {
            tokenRepository.getToken() // Fetch the token on the IO dispatcher
        }
        return RetrofitClient.createApiService { token }
    }

    suspend fun loginUser(request: LoginRequest): Call<LoginResponse> {
        val apiService = getApiService() // Use the service with the token
        return apiService.loginUser(request)
    }

    suspend fun registerUser(request: RegistrationRequest): Call<RegistrationResponse> {
        val apiService = getApiService()
        return apiService.registerUser(request)
    }
}
