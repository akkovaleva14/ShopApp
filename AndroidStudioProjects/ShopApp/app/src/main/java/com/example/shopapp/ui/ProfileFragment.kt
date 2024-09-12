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
import com.example.shopapp.utils.StringUtils
import kotlinx.coroutines.launch

// ProfileFragment handles user registration, validation, and UI interaction.
class ProfileFragment : Fragment() {

    // Initialize token database, repository, and ViewModel lazily
    private val tokenDatabase by lazy {
        TokenDatabase.getDatabase(requireContext())
    }
    private val tokenRepository by lazy {
        TokenRepository(tokenDatabase.tokenDao())
    }
    private val authRepository by lazy {
        AuthRepository(tokenRepository)
    }

    // Use the ViewModel by delegating with the appropriate ViewModelFactory
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

    // Set up button click listeners and observe ViewModel states when the view is created
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Handle the registration button click
        binding.btnRegister.setOnClickListener {
            // Check if there is an internet connection before proceeding
            if (NetworkUtils.isInternetAvailable(requireContext())) {
                val name = binding.etName.text.toString()
                val email = binding.etEmail.text.toString()
                val password = binding.etPassword.text.toString()
                val confirmPassword = binding.etConfirmPassword.text.toString()

                // Validate the input fields
                if (validateInput(name, email, password, confirmPassword)) {
                    // Perform registration using a coroutine
                    viewLifecycleOwner.lifecycleScope.launch {
                        Log.d("ProfileFragment", "Attempting to register user with email: $email")
                        authViewModel.registerUser(name, email, password, confirmPassword)
                    }
                }
            } else {
                // Display a toast if no internet connection is detected
                Toast.makeText(requireContext(), "No internet connection", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        // Observe registration result to navigate to another fragment upon success
        authViewModel.registrationResult.observe(viewLifecycleOwner, Observer { isSuccess ->
            if (isSuccess) {
                Toast.makeText(context, "Registration successful", Toast.LENGTH_SHORT).show()
                Log.d(
                    "ProfileFragment",
                    "Registration successful for email: ${binding.etEmail.text}"
                )
                // Navigate to the entrance fragment after registration
                findNavController().navigate(R.id.action_profileFragment_to_entranceFragment)
            }
        })

        // Observe errors from ViewModel and display appropriate messages
        authViewModel.error.observe(viewLifecycleOwner, Observer { errorMessage ->
            Log.e("ProfileFragment", "Registration error: $errorMessage")
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        })

        // Observe the loading state and show/hide the progress bar accordingly
        authViewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        })
    }

    // Function to validate the user input for registration
    private fun validateInput(
        name: String, email: String, password: String, confirmPassword: String
    ): Boolean {
        val trimmedPassword = password.trim()
        val trimmedConfirmPassword = confirmPassword.trim()

        // Check if any field is empty
        if (name.isBlank() || email.isBlank() || trimmedPassword.isBlank() || trimmedConfirmPassword.isBlank()) {
            Toast.makeText(context, getString(R.string.all_fields_are_required), Toast.LENGTH_SHORT)
                .show()
            return false
        }

        // Validate email format
        if (!StringUtils.isValidEmail(email)) {
            Toast.makeText(context, getString(R.string.invalid_email_format), Toast.LENGTH_SHORT)
                .show()
            return false
        }

        // Ensure the password meets length requirements
        if (trimmedPassword.length < 8) {
            Toast.makeText(
                context,
                getString(R.string.password_must_be_at_least_8_characters), Toast.LENGTH_SHORT
            )
                .show()
            return false
        }

        if (trimmedPassword.length > 24) {
            Toast.makeText(
                context,
                getString(R.string.password_should_not_exceed_24_characters), Toast.LENGTH_SHORT
            )
                .show()
            return false
        }

        // Ensure the password and confirmation password match
        if (trimmedPassword != trimmedConfirmPassword) {
            Toast.makeText(context, getString(R.string.passwords_do_not_match), Toast.LENGTH_SHORT)
                .show()
            return false
        }

        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}