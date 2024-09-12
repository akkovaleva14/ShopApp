package com.example.shopapp.ui

import android.os.Bundle
import android.text.InputType
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
import com.example.domain.viewmodels.EntranceViewModel
import com.example.domain.viewmodelfactories.ViewModelFactory
import com.example.shopapp.R
import com.example.shopapp.databinding.FragmentEntranceBinding
import com.example.shopapp.utils.NetworkUtils
import kotlinx.coroutines.launch

// EntranceFragment handles the login process including password visibility toggle and user input validation.
class EntranceFragment : Fragment(R.layout.fragment_entrance) {

    private lateinit var tokenDatabase: TokenDatabase
    private lateinit var tokenRepository: TokenRepository

    private val entranceViewModel: EntranceViewModel by viewModels {
        ViewModelFactory(AuthRepository(tokenRepository), tokenRepository)
    }

    // ViewBinding to manage view references
    private var _binding: FragmentEntranceBinding? = null
    private val binding get() = _binding!!

    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize tokenDatabase and tokenRepository before they are used by ViewModel
        tokenDatabase = TokenDatabase.getDatabase(requireContext())
        tokenRepository = TokenRepository(tokenDatabase.tokenDao())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEntranceBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Handle user interactions and observe ViewModel states after the view is created
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Toggle password visibility when button is clicked
        binding.btnShowHidePassword.setOnClickListener {
            togglePasswordVisibility()
        }

        // Handle login button click
        binding.btnLogin.setOnClickListener {
            // Check if there is an internet connection before proceeding
            if (NetworkUtils.isInternetAvailable(requireContext())) {
                val phoneOrEmail = binding.etPhoneOrEmail.text.toString()
                val password = binding.etPassword.text.toString()

                // Validate input fields
                if (validateInput(phoneOrEmail, password)) {
                    // Perform login using a coroutine
                    viewLifecycleOwner.lifecycleScope.launch {
                        Log.d("EntranceFragment", "Attempting login")
                        entranceViewModel.loginUser(phoneOrEmail, password)
                    }
                }
            } else {
                // Display a toast if no internet connection is detected
                Toast.makeText(
                    requireContext(),
                    getString(R.string.no_internet_connection),
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        }

        // Observe loading state and show/hide progress bar accordingly
        entranceViewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            if (isLoading) {
                Log.d("EntranceFragment", "Showing progress bar")
                binding.progressBar.visibility = View.VISIBLE
                binding.btnLogin.visibility = View.GONE
            } else {
                Log.d("EntranceFragment", "Hiding progress bar")
                binding.progressBar.visibility = View.GONE
                binding.btnLogin.visibility = View.VISIBLE
            }
        })

        // Observe login result and navigate to the appropriate fragment on success/failure
        entranceViewModel.loginResult.observe(viewLifecycleOwner, Observer { isSuccess ->
            if (isSuccess) {
                // Navigate to the products fragment after successful login
                findNavController().navigate(R.id.action_entranceFragment_to_productsFragment)
            } else {
                // Show a failure message and navigate to the profile fragment
                Toast.makeText(context, getString(R.string.login_failed), Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_entranceFragment_to_profileFragment)
            }
        })

        // Observe and display errors from the ViewModel
        entranceViewModel.error.observe(viewLifecycleOwner, Observer { errorMessage ->
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        })
    }

    // Function to toggle the visibility of the password field
    private fun togglePasswordVisibility() {
        if (isPasswordVisible) {
            // Set input type to password (hidden)
            binding.etPassword.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding.btnShowHidePassword.setImageResource(R.drawable.ic_eye_hide)
        } else {
            // Set input type to visible password
            binding.etPassword.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            binding.btnShowHidePassword.setImageResource(R.drawable.ic_eye_show)
        }
        // Set the cursor position to the end of the text
        binding.etPassword.setSelection(binding.etPassword.text.length)
        isPasswordVisible = !isPasswordVisible
    }

    // Function to validate the user input for login
    private fun validateInput(email: String, password: String): Boolean {
        val trimmedEmail = email.trim()
        val trimmedPassword = password.trim()

        // Check if any field is empty
        if (trimmedEmail.isEmpty() || trimmedPassword.isEmpty()) {
            Toast.makeText(context, "All fields are required", Toast.LENGTH_SHORT).show()
            return false
        }

        // Validate email format
        if (!isValidEmail(trimmedEmail)) {
            Toast.makeText(context, "Invalid email format", Toast.LENGTH_SHORT).show()
            return false
        }

        // Ensure the password length does not exceed the maximum
        if (trimmedPassword.length > 24) {
            Toast.makeText(context, "Password should not exceed 24 characters", Toast.LENGTH_SHORT)
                .show()
            return false
        }

        return true
    }

    // Function to validate email format using regex
    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        return email.matches(emailRegex.toRegex())
    }

    // Clean up references when the view is destroyed to prevent memory leaks
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}