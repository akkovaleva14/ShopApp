package com.example.domain.viewmodelfactories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.domain.repositories.AuthRepository
import com.example.domain.repositories.TokenRepository
import com.example.domain.viewmodels.AuthViewModel

// Factory class for creating instances of AuthViewModel.
class AuthViewModelFactory(
    private val authRepository: AuthRepository,
    private val tokenRepository: TokenRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Check if the requested ViewModel class is AuthViewModel.
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            // Return a new instance of AuthViewModel with authRepository and tokenRepository.
            return AuthViewModel(authRepository, tokenRepository) as T
        }
        // If the class is not AuthViewModel, throw an exception.
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}