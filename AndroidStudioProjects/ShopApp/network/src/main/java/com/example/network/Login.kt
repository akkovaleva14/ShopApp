package com.example.network

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val status: String,
    val token: String
)