package com.example.domain.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.domain.repositories.AuthRepository
import com.example.domain.repositories.TokenRepository
import com.example.network.RegistrationRequest
import com.example.network.RegistrationResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// ViewModel responsible for handling authentication operations such as user registration.
class AuthViewModel(
    private val authRepository: AuthRepository,
    private val tokenRepository: TokenRepository
) : ViewModel() {

    // LiveData to observe registration results (success or failure).
    private val _registrationResult = MutableLiveData<Boolean>()
    val registrationResult: LiveData<Boolean> = _registrationResult

    // LiveData to observe any error messages during registration.
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    // LiveData to observe the loading state (for UI loading indication).
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Function to register a user. Takes user details as parameters and sends a registration request.
    suspend fun registerUser(name: String, email: String, password: String, cpassword: String) {
        val request = RegistrationRequest(name, email, password, cpassword)
        Log.d("AuthViewModel", "Attempting to register user with email: $email")

        _isLoading.value = true  // Start loading

        // Make a registration request through the AuthRepository.
        authRepository.registerUser(request).enqueue(object : Callback<RegistrationResponse> {
            override fun onResponse(
                call: Call<RegistrationResponse>,
                response: Response<RegistrationResponse>
            ) {
                _isLoading.value = false  // Stop loading after receiving a response
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

                    // Handle specific error cases, like email conflict.
                    if (response.code() == 409) {
                        _error.value = "Email already exists"
                    } else {
                        _error.value = "Registration failed"
                    }
                }
            }

            override fun onFailure(call: Call<RegistrationResponse>, t: Throwable) {
                _isLoading.value = false  // Stop loading if request fails
                _registrationResult.value = false
                Log.e("AuthViewModel", "Registration error: ${t.message}")

                // Handle network issues separately from other errors.
                if (t.message?.contains("Unable to resolve host") == true || t.message?.contains("failed to connect") == true) {
                    _error.value = "No internet connection. Please try again."
                } else {
                    _error.value = "An error occurred: ${t.message}"
                }
            }
        })
    }
}