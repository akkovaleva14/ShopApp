package com.example.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

// Retrofit interface for API service calls.
interface ApiService {

    // Endpoint to register a new user.
    @POST("app/v1/users")
    fun registerUser(@Body request: RegistrationRequest): Call<RegistrationResponse>

    // Endpoint for user login. Optionally includes an Authorization header with a token.
    @POST("app/v1/users/auth/login")
    fun loginUser(
        @Body request: LoginRequest,
        @Header("Authorization") token: String? = null // Optional Authorization header for token-based authentication.
    ): Call<LoginResponse>

    // Endpoint to retrieve a list of products with optional query parameters for filtering and sorting.
    @GET("app/v1/products")
    fun getProducts(
        @Query("category") category: String? = null,
        @Query("price[gte]") minPrice: Int? = null,
        @Query("price[lte]") maxPrice: Int? = null,
        @Query("sort") sort: String? = null,
        @Query("limit") limit: Int? = null,
        @Query("page") page: Int? = null
    ): Call<ProductResponse>

    // Endpoint to retrieve detailed information about a specific product by its ID.
    @GET("app/v1/products/{id}")
    fun getProductDetails(@Path("id") productId: String): Call<ProductDetailsResponse>
}