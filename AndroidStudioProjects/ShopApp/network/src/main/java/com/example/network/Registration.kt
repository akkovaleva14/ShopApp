package com.example.network

// Data class representing the request body for user registration.
data class RegistrationRequest(
    val name: String,
    val email: String,
    val password: String,
    val cpassword: String
)

// Data class representing the response received after a user registration attempt.
data class RegistrationResponse(
    val status: String,
    val token: String
)