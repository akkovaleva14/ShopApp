package com.example.network

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