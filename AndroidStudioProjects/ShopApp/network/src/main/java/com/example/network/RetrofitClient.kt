package com.example.network

import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://fakeshopapi-l2ng.onrender.com/"

    private val gson = GsonBuilder()
        .registerTypeAdapter(Any::class.java, ProductSpecificationsDeserializer()) // Регистрируем десериализатор
        .create()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson)) // Используем кастомный GSON
            .build()
            .create(ApiService::class.java)
    }
}