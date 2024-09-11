package com.example.shopapp.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.data.TokenDatabase
import com.example.domain.repositories.AuthRepository
import com.example.domain.repositories.TokenRepository
import com.example.domain.viewmodels.AuthViewModel
import com.example.domain.viewmodelfactories.AuthViewModelFactory
import com.example.shopapp.R
import com.example.shopapp.databinding.FragmentProfileBinding
import com.example.shopapp.utils.NetworkUtils
import kotlinx.coroutines.launch

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private val tokenDatabase by lazy {
        TokenDatabase.getDatabase(requireContext())
    }
    private val tokenRepository by lazy {
        TokenRepository(tokenDatabase.tokenDao())
    }
    private val authRepository by lazy {
        AuthRepository(tokenRepository)
    }

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(authRepository, tokenRepository)
    }

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnRegister.setOnClickListener {
            if (NetworkUtils.isInternetAvailable(requireContext())) {
                val name = binding.etName.text.toString()
                val email = binding.etEmail.text.toString()
                val password = binding.etPassword.text.toString()
                val confirmPassword = binding.etConfirmPassword.text.toString()

                if (validateInput(name, email, password, confirmPassword)) {
                    // Launch coroutine to call suspend function
                    viewLifecycleOwner.lifecycleScope.launch {
                        Log.d("ProfileFragment", "Attempting to register user with email: $email")
                        authViewModel.registerUser(name, email, password, confirmPassword)
                    }
                }
            } else {
                Toast.makeText(requireContext(), "No internet connection", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        authViewModel.registrationResult.observe(viewLifecycleOwner, Observer { isSuccess ->
            if (isSuccess) {
                Toast.makeText(context, "Registration successful", Toast.LENGTH_SHORT).show()
                Log.d(
                    "ProfileFragment",
                    "Registration successful for email: ${binding.etEmail.text}"
                )
                findNavController().navigate(R.id.action_profileFragment_to_entranceFragment)
            }
        })

        authViewModel.error.observe(viewLifecycleOwner, Observer { errorMessage ->
            Log.e("ProfileFragment", "Registration error: $errorMessage")
            when (errorMessage) {
                "Email already exists" -> {
                    Toast.makeText(context, "This email is already registered.", Toast.LENGTH_SHORT)
                        .show()
                }

                else -> {
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
        })

        authViewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        })
    }

    private fun validateInput(
        name: String, email: String, password: String, confirmPassword: String
    ): Boolean {
        val trimmedPassword = password.trim()
        val trimmedConfirmPassword = confirmPassword.trim()

        if (name.isEmpty() || email.isEmpty() || trimmedPassword.isEmpty() || trimmedConfirmPassword.isEmpty()) {
            Toast.makeText(context, "All fields are required", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!isValidEmail(email)) {
            Toast.makeText(context, "Invalid email format", Toast.LENGTH_SHORT).show()
            return false
        }

        if (trimmedPassword.length < 8) {
            Toast.makeText(context, "Password must be at least 8 characters", Toast.LENGTH_SHORT)
                .show()
            return false
        }

        if (trimmedPassword.length > 24) {
            Toast.makeText(context, "Password should not exceed 24 characters", Toast.LENGTH_SHORT)
                .show()
            return false
        }

        if (trimmedPassword != trimmedConfirmPassword) {
            Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        return email.matches(emailRegex.toRegex())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}