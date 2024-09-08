package com.example.shopapp.ui

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.data.TokenDatabase
import com.example.domain.TokenRepository
import com.example.domain.AuthRepository
import com.example.domain.EntranceViewModel
import com.example.domain.ViewModelFactory
import com.example.shopapp.R
import com.example.shopapp.databinding.FragmentEntranceBinding
import com.example.shopapp.utils.NetworkUtils

class EntranceFragment : Fragment(R.layout.fragment_entrance) {

    private val tokenDatabase by lazy {
        TokenDatabase.getDatabase(requireContext())
    }
    private val tokenRepository by lazy {
        TokenRepository(tokenDatabase.tokenDao())
    }
    private val authRepository = AuthRepository()
    private val entranceViewModel: EntranceViewModel by viewModels {
        ViewModelFactory(authRepository, tokenRepository)
    }

    private var _binding: FragmentEntranceBinding? = null
    private val binding get() = _binding!!

    private var isPasswordVisible = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEntranceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Логика для отображения и скрытия пароля
        binding.btnShowHidePassword.setOnClickListener {
            togglePasswordVisibility()
        }

        // Клик на кнопку входа
        binding.btnLogin.setOnClickListener {
            if (NetworkUtils.isInternetAvailable(requireContext())) {
                val phoneOrEmail = binding.etPhoneOrEmail.text.toString()
                val password = binding.etPassword.text.toString()

                if (validateInput(phoneOrEmail, password)) {
                    entranceViewModel.loginUser(phoneOrEmail, password)
                }
            } else {
                Toast.makeText(requireContext(), "No internet connection", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        // Наблюдаем за состоянием загрузки
        entranceViewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            if (isLoading) {
                binding.progressBar.visibility = View.VISIBLE
                binding.btnLogin.visibility = View.GONE
            } else {
                binding.progressBar.visibility = View.GONE
                binding.btnLogin.visibility = View.VISIBLE
            }
        })

        // Наблюдаем за результатом авторизации
        entranceViewModel.loginResult.observe(viewLifecycleOwner, Observer { isSuccess ->
            if (isSuccess) {
                findNavController().navigate(R.id.action_entranceFragment_to_productsFragment)
            } else {
                Toast.makeText(context, "Login failed", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_entranceFragment_to_profileFragment)
            }
        })

        entranceViewModel.error.observe(viewLifecycleOwner, Observer { errorMessage ->
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        })
    }

    private fun togglePasswordVisibility() {
        if (isPasswordVisible) {
            binding.etPassword.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding.btnShowHidePassword.setImageResource(R.drawable.eye_hide)
        } else {
            binding.etPassword.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            binding.btnShowHidePassword.setImageResource(R.drawable.eye_show)
        }
        binding.etPassword.setSelection(binding.etPassword.text.length)
        isPasswordVisible = !isPasswordVisible
    }

    private fun validateInput(email: String, password: String): Boolean {
        val trimmedEmail = email.trim()
        val trimmedPassword = password.trim()

        if (trimmedEmail.isEmpty() || trimmedPassword.isEmpty()) {
            Toast.makeText(context, "All fields are required", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!isValidEmail(trimmedEmail)) {
            Toast.makeText(context, "Invalid email format", Toast.LENGTH_SHORT).show()
            return false
        }

        if (trimmedPassword.length > 24) {
            Toast.makeText(context, "Password should not exceed 24 characters", Toast.LENGTH_SHORT)
                .show()
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
