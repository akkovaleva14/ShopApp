package com.example.domain

import com.example.network.RegistrationRequest
import com.example.network.RegistrationResponse
import com.example.network.RetrofitClient
import retrofit2.Call

class AuthRepository {
    fun registerUser(request: RegistrationRequest): Call<RegistrationResponse> {
        return RetrofitClient.apiService.registerUser(request)
    }
}