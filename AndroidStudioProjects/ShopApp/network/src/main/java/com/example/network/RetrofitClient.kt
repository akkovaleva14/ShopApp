package com.example.network

import android.util.Log
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "https://fakeshopapi-l2ng.onrender.com/"
    private const val TAG = "RetrofitClient" // Tag for logging purposes.

    // Create a Gson instance with a custom deserializer for `Any` type.
    private val gson = GsonBuilder()
        .registerTypeAdapter(
            Any::class.java,
            ProductSpecificationsDeserializer()
        )
        .create()

    // Create a Retrofit instance with the base URL and a Gson converter.
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // Create an instance of ApiService using the Retrofit instance.
    val apiService: ApiService = retrofit.create(ApiService::class.java)

    // Create an OkHttpClient instance with logging and authorization token handling.
    private fun createOkHttpClient(tokenProvider: () -> String?): OkHttpClient {
        // Set up logging interceptor to log HTTP request and response bodies.
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY

        return OkHttpClient.Builder()
            .addInterceptor(logging) // Add logging interceptor to the client.
            .addInterceptor(Interceptor { chain ->
                // Retrieve the authorization token.
                val token = tokenProvider()
                val requestBuilder = chain.request().newBuilder()
                // If token is present, add it to the request header.
                if (!token.isNullOrEmpty()) {
                    requestBuilder.addHeader("Authorization", "Bearer $token")
                    Log.d(TAG, "Added Authorization header: Bearer $token")
                }
                chain.proceed(requestBuilder.build()) // Proceed with the request.
            })
            .build()
    }

    // Create a Retrofit instance with a custom OkHttpClient that includes an authorization token.
    private fun createRetrofit(tokenProvider: () -> String?): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(createOkHttpClient(tokenProvider)) // Use the custom OkHttpClient.
            .addConverterFactory(GsonConverterFactory.create(gson)) // Use custom Gson instance.
            .build()
    }

    // Create an ApiService instance with an authorization token.
    fun createApiService(tokenProvider: () -> String?): ApiService {
        // Set up logging interceptor to log HTTP request and response bodies.
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY

        // Create OkHttpClient with logging and authorization token handling.
        val client = OkHttpClient.Builder()
            .addInterceptor(logging) // Add logging interceptor to the client.
            .addInterceptor(Interceptor { chain ->
                val requestBuilder: Request.Builder = chain.request().newBuilder()
                // Retrieve the authorization token.
                val token = tokenProvider()
                // If token is present, add it to the request header.
                if (!token.isNullOrEmpty()) {
                    requestBuilder.addHeader("Authorization", "Bearer $token")
                    Log.d(TAG, "Added Authorization header: Bearer $token")
                }
                chain.proceed(requestBuilder.build()) // Proceed with the request.
            })
            .build()

        // Create and return the ApiService instance with the custom OkHttpClient.
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client) // Use the custom OkHttpClient.
            .addConverterFactory(GsonConverterFactory.create()) // Use default Gson instance.
            .build()
            .create(ApiService::class.java)
    }
}