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
    private const val TAG = "RetrofitClient"

    private val gson = GsonBuilder()
        .registerTypeAdapter(
            Any::class.java,
            ProductSpecificationsDeserializer()
        )
        .create()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)

    private fun createOkHttpClient(tokenProvider: () -> String?): OkHttpClient {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY

        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(Interceptor { chain ->
                val token = tokenProvider()
                val requestBuilder = chain.request().newBuilder()
                if (!token.isNullOrEmpty()) {
                    requestBuilder.addHeader("Authorization", "Bearer $token")
                    Log.d(TAG, "Added Authorization header: Bearer $token")
                }
                chain.proceed(requestBuilder.build())
            })
            .build()
    }

    private fun createRetrofit(tokenProvider: () -> String?): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(createOkHttpClient(tokenProvider))
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    fun createApiService(tokenProvider: () -> String?): ApiService {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(Interceptor { chain ->
                val requestBuilder: Request.Builder = chain.request().newBuilder()
                val token = tokenProvider()
                if (!token.isNullOrEmpty()) {
                    requestBuilder.addHeader("Authorization", "Bearer $token")
                    Log.d(TAG, "Added Authorization header: Bearer $token")
                }
                chain.proceed(requestBuilder.build())
            })
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
