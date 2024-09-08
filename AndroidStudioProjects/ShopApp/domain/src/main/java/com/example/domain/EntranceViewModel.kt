package com.example.domain

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.network.LoginRequest
import com.example.network.LoginResponse
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EntranceViewModel(
    private val authRepository: AuthRepository,
    private val tokenRepository: TokenRepository
) : ViewModel() {

    private val _loginResult = MutableLiveData<Boolean>()
    val loginResult: LiveData<Boolean> = _loginResult

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    // LiveData для отслеживания состояния загрузки
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun loginUser(email: String, password: String) {
        _isLoading.value = true // Начало загрузки

        val request = LoginRequest(email, password)
        Log.d("EntranceViewModel", "Attempting to login user with email: $email")

        authRepository.loginUser(request).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(
                call: Call<LoginResponse>,
                response: Response<LoginResponse>
            ) {
                _isLoading.value = false // Окончание загрузки

                if (response.isSuccessful) {
                    _loginResult.value = true
                    Log.d("EntranceViewModel", "Login successful for email: $email")
                    // Сохраняем токен в локальное хранилище
                    val token = response.body()?.token
                    if (token != null) {
                        Log.d("EntranceViewModel", "Saving token received from server: $token")
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
                _isLoading.value = false // Окончание загрузки при ошибке

                _loginResult.value = false
                Log.e("EntranceViewModel", "Login error: ${t.message}")
                _error.value = "An error occurred: ${t.message}"
            }
        })
    }

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