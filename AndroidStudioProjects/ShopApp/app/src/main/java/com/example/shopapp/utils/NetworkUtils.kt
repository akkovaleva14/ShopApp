package com.example.shopapp.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log

// Utility object for network-related functions
object NetworkUtils {

    private const val TAG = "NetworkUtils"

    // Function to check if there is an active internet connection
    fun isInternetAvailable(context: Context): Boolean {
        // Get the ConnectivityManager system service
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // Check for network availability on devices running Android Marshmallow (API 23) or later
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Get the active network
            val network = connectivityManager.activeNetwork
            if (network == null) {
                Log.d(TAG, "No active network")
                return false
            }

            // Get the network capabilities of the active network
            val activeNetwork = connectivityManager.getNetworkCapabilities(network)
            if (activeNetwork == null) {
                Log.d(TAG, "No network capabilities")
                return false
            }

            Log.d(TAG, "Checking network capabilities...")

            // Check if the active network has any of the known transport types
            return when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                    Log.d(TAG, "Connected to WiFi")
                    true
                }

                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                    Log.d(TAG, "Connected to Cellular")
                    true
                }

                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                    Log.d(TAG, "Connected to Ethernet")
                    true
                }

                else -> {
                    Log.d(TAG, "No available transport method")
                    false
                }
            }
        } else {
            // For devices running pre-API 23 (Marshmallow), use the deprecated method to check network status
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            if (networkInfo != null && networkInfo.isConnected) {
                Log.d(TAG, "Connected (pre-API 23): ${networkInfo.typeName}")
                return true
            } else {
                Log.d(TAG, "No connection (pre-API 23)")
                return false
            }
        }
    }
}
