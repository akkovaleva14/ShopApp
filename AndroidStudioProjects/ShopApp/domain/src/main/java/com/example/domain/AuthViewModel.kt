package com.example.domain

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
        authRepository.registerUser(request).enqueue(object : Callback<RegistrationResponse> {
            override fun onResponse(
                call: Call<RegistrationResponse>,
                response: Response<RegistrationResponse>
            ) {
                if (response.isSuccessful) {
                    _registrationResult.value = true
                } else {
                    _registrationResult.value = false
                    _error.value = "Registration failed"
                }
            }

            override fun onFailure(call: Call<RegistrationResponse>, t: Throwable) {
                _registrationResult.value = false
                _error.value = "An error occurred: ${t.message}"
            }
        })
    }
}
