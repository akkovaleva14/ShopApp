package com.example.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

data class RegistrationRequest(
    val name: String,
    val email: String,
    val password: String,
    val cpassword: String
)

data class RegistrationResponse(
    val status: String,
    val token: String
)

interface ApiService {
    @POST("app/v1/users")
    fun registerUser(@Body request: RegistrationRequest): Call<RegistrationResponse>
}