package com.example.domain.viewmodelfactories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.domain.repositories.AuthRepository
import com.example.domain.repositories.TokenRepository
import com.example.domain.viewmodels.EntranceViewModel

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