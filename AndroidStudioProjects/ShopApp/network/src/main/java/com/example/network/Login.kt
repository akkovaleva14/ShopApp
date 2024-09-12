package com.example.network

// Data class for login request body.
// Contains user credentials required for login.
data class LoginRequest(
    val email: String,
    val password: String
)

// Data class for login response body.
// Contains the server's response to a login attempt.
data class LoginResponse(
    val status: String,
    val token: String
)