package com.example.shopapp.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.data.TokenDatabase
import com.example.domain.repositories.TokenRepository
import com.example.shopapp.R
import com.example.shopapp.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    // Initialization of tokenDatabase and tokenRepository
    private val tokenDatabase by lazy {
        TokenDatabase.getDatabase(this)
    }
    private val tokenRepository by lazy {
        TokenRepository(tokenDatabase.tokenDao())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        // Initialization of the navigation controller
        navController = findNavController(R.id.nav_host_fragment_activity_main)

        // Configuration for working with BottomNavigationView
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_search,
                R.id.navigation_favorite,
                R.id.navigation_products,
                R.id.navigation_message,
                R.id.navigation_profile
            )
        )

        // Setup ActionBar with the navigation controller
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Check the token and navigate accordingly
        checkTokenAndNavigate()

        // Add listeners to bottom navigation items
        setupBottomNavigationListener(navView)
    }

    private fun checkTokenAndNavigate() {
        // Coroutine for token check
        lifecycleScope.launch {
            val token = tokenRepository.getToken()

            if (token != null) {
                // If token exists, navigate to the products screen
                navController.navigate(R.id.navigation_products)
            } else {
                // If token is not found, stay on the profile screen
                navController.navigate(R.id.navigation_profile)
            }
        }
    }

    private fun setupBottomNavigationListener(navView: BottomNavigationView) {
        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_search, R.id.navigation_favorite, R.id.navigation_message -> {
                    // Show toast for INVALID action
                    Toast.makeText(this,
                        getString(R.string.doesn_t_work_yet_sorry), Toast.LENGTH_SHORT).show()
                    false // Returning false to indicate the action is not handled (no navigation)
                }
                else -> {
                    // Let the navController handle other actions (products, profile)
                    navController.navigate(item.itemId)
                    true
                }
            }
        }
    }
}