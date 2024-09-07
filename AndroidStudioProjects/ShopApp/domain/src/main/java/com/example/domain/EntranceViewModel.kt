package com.example.domain

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.network.LoginRequest
import com.example.network.LoginResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EntranceViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _loginResult = MutableLiveData<Boolean>()
    val loginResult: LiveData<Boolean> = _loginResult

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun loginUser(email: String, password: String) {
        val request = LoginRequest(email, password)
        Log.d("EntranceViewModel", "Attempting to login user with email: $email")

        authRepository.loginUser(request).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(
                call: Call<LoginResponse>,
                response: Response<LoginResponse>
            ) {
                if (response.isSuccessful) {
                    _loginResult.value = true
                    Log.d("EntranceViewModel", "Login successful for email: $email")
                    // Save token in local storage
                    val token = response.body()?.token
                    // Save token logic here
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
                _loginResult.value = false
                Log.e("EntranceViewModel", "Login error: ${t.message}")
                _error.value = "An error occurred: ${t.message}"
            }
        })
    }
}
