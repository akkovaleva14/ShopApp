package com.example.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("app/v1/users")
    fun registerUser(@Body request: RegistrationRequest): Call<RegistrationResponse>

    @POST("app/v1/users/auth/login")
    fun loginUser(@Body request: LoginRequest): Call<LoginResponse>
}
