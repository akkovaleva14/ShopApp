package com.example.shopapp.ui

import android.os.Bundle
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

    // Инициализация репозитория токена
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

        // Инициализация контроллера навигации
        navController = findNavController(R.id.nav_host_fragment_activity_main)

        // Конфигурация для работы с BottomNavigationView
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_search,
                R.id.navigation_favorite,
                R.id.navigation_products,
                R.id.navigation_message,
                R.id.navigation_profile
            )
        )

        // Настройка ActionBar с контроллером навигации
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Проверка токена и навигация
        checkTokenAndNavigate()
    }

    private fun checkTokenAndNavigate() {
        // Корутина для проверки токена
        lifecycleScope.launch {
            val token = tokenRepository.getToken()

            if (token != null) {
                // Если токен существует, направляем на экран списка продуктов
                navController.navigate(R.id.navigation_products)
            } else {
                // Если токен не найден, остаемся на экране профиля
                navController.navigate(R.id.navigation_profile)
            }
        }
    }

    // Метод для поддержки навигации через ActionBar (стрелка назад)
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
