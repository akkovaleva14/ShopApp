package com.example.domain

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.network.RegistrationRequest
import com.example.network.RegistrationResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _registrationResult = MutableLiveData<Boolean>()
    val registrationResult: LiveData<Boolean> = _registrationResult

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun registerUser(name: String, email: String, password: String, cpassword: String) {
        val request = RegistrationRequest(name, email, password, cpassword)
        Log.d("AuthViewModel", "Attempting to register user with email: $email")

        authRepository.registerUser(request).enqueue(object : Callback<RegistrationResponse> {
            override fun onResponse(
                call: Call<RegistrationResponse>,
                response: Response<RegistrationResponse>
            ) {
                if (response.isSuccessful) {
                    _registrationResult.value = true
                    Log.d("AuthViewModel", "Registration successful for email: $email")
                } else {
                    _registrationResult.value = false
                    val errorBody = response.errorBody()?.string()
                    Log.e(
                        "AuthViewModel",
                        "Registration failed. Response code: ${response.code()}, error: $errorBody"
                    )

                    if (response.code() == 409) {
                        _error.value = "Email already exists"
                    } else {
                        _error.value = "Registration failed"
                    }
                }
            }

            override fun onFailure(call: Call<RegistrationResponse>, t: Throwable) {
                _registrationResult.value = false
                Log.e("AuthViewModel", "Registration error: ${t.message}")

                if (t.message?.contains("Unable to resolve host") == true || t.message?.contains("failed to connect") == true) {
                    _error.value = "No internet connection. Please try again."
                } else {
                    _error.value = "An error occurred: ${t.message}"
                }
            }
        })
    }
}