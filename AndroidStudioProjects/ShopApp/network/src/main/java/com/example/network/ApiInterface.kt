package com.example.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("app/v1/users")
    fun registerUser(@Body request: RegistrationRequest): Call<RegistrationResponse>

    @POST("app/v1/users/auth/login")
    fun loginUser(
        @Body request: LoginRequest,
        @Header("Authorization") token: String? = null // Add token as a header
    ): Call<LoginResponse>

    @GET("app/v1/products")
    fun getProducts(
        @Query("category") category: String? = null,
        @Query("price[gte]") minPrice: Int? = null,
        @Query("price[lte]") maxPrice: Int? = null,
        @Query("sort") sort: String? = null,
        @Query("limit") limit: Int? = null,
        @Query("page") page: Int? = null
    ): Call<ProductResponse>

    @GET("app/v1/products/{id}")
    fun getProductDetails(@Path("id") productId: String): Call<ProductDetailsResponse>
}