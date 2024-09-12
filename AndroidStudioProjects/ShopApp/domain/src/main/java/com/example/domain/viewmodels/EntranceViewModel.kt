package com.example.domain.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.repositories.AuthRepository
import com.example.domain.repositories.TokenRepository
import com.example.network.LoginRequest
import com.example.network.LoginResponse
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// ViewModel responsible for handling user login operations.
class EntranceViewModel(
    private val authRepository: AuthRepository,
    private val tokenRepository: TokenRepository
) : ViewModel() {

    // LiveData to observe login results (success or failure).
    private val _loginResult = MutableLiveData<Boolean>()
    val loginResult: LiveData<Boolean> = _loginResult

    // LiveData to observe any error messages during login.
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    // LiveData to observe the loading state (for UI loading indication).
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Function to log in a user. Takes email and password as parameters and sends a login request.
    suspend fun loginUser(email: String, password: String) {
        _isLoading.value = true  // Start loading

        val request = LoginRequest(email, password)
        Log.d("EntranceViewModel", "Attempting to login user with email: $email")

        // Make a login request through the AuthRepository.
        authRepository.loginUser(request).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(
                call: Call<LoginResponse>,
                response: Response<LoginResponse>
            ) {
                _isLoading.value = false  // Stop loading after receiving a response

                if (response.isSuccessful) {
                    _loginResult.value = true
                    Log.d("EntranceViewModel", "Login successful for email: $email")
                    val token = response.body()?.token
                    if (token != null) {
                        Log.d("EntranceViewModel", "Saving token received from server: $token")
                        // Save the token received from the server
                        saveToken(token)
                    } else {
                        Log.e("EntranceViewModel", "No token received from server")
                    }
                } else {
                    _loginResult.value = false
                    val errorBody = response.errorBody()?.string()
                    Log.e(
                        "EntranceViewModel",
                        "Login failed. Response code: ${response.code()}, error: $errorBody"
                    )
                    _error.value = "Login failed"
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                _isLoading.value = false  // Stop loading if request fails
                _loginResult.value = false
                Log.e("EntranceViewModel", "Login error: ${t.message}")
                _error.value = "An error occurred: ${t.message}"
            }
        })
    }

    // Function to save the token received from the server in the TokenRepository.
    private fun saveToken(token: String) {
        viewModelScope.launch {
            try {
                tokenRepository.saveToken(token)
                Log.d("EntranceViewModel", "Token saved successfully in repository")
            } catch (e: Exception) {
                Log.e("EntranceViewModel", "Error saving token in repository: ${e.message}")
            }
        }
    }
}