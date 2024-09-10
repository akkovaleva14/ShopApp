package com.example.domain.repositories

import com.example.network.LoginRequest
import com.example.network.LoginResponse
import com.example.network.RegistrationRequest
import com.example.network.RegistrationResponse
import com.example.network.RetrofitClient
import retrofit2.Call

class AuthRepository {
    fun registerUser(request: RegistrationRequest): Call<RegistrationResponse> {
        return RetrofitClient.apiService.registerUser(request)
    }
    fun loginUser(request: LoginRequest): Call<LoginResponse> {
        return RetrofitClient.apiService.loginUser(request)
    }
}