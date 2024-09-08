package com.example.shopapp.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log

object NetworkUtils {

    private const val TAG = "NetworkUtils"

    fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            if (network == null) {
                Log.d(TAG, "No active network")
                return false
            }

            val activeNetwork = connectivityManager.getNetworkCapabilities(network)
            if (activeNetwork == null) {
                Log.d(TAG, "No network capabilities")
                return false
            }

            Log.d(TAG, "Checking network capabilities...")

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