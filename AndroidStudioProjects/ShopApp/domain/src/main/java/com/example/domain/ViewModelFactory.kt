package com.example.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ViewModelFactory(
    private val authRepository: AuthRepository,
    private val tokenRepository: TokenRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EntranceViewModel::class.java)) {
            return EntranceViewModel(authRepository, tokenRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}