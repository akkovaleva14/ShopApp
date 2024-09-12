package com.example.domain.repositories

import com.example.network.ApiService
import com.example.network.ProductDetailsResponse
import retrofit2.HttpException
import retrofit2.Response
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProductItemRepository(private val apiService: ApiService) {

    // Function to fetch product details based on product ID
    suspend fun getProductDetails(productId: String): Result<ProductDetailsResponse> {
        return withContext(Dispatchers.IO) {
            try {
                // Make network request to fetch product details
                val response: Response<ProductDetailsResponse> =
                    apiService.getProductDetails(productId).execute()

                // If response is successful and body is not null, return success
                if (response.isSuccessful) {
                    val productDetailsResponse = response.body()
                    if (productDetailsResponse != null) {
                        Result.success(productDetailsResponse)
                    } else {
                        // Handle case where response body is empty
                        Result.failure(Exception("Empty response body"))
                    }
                } else {
                    // Handle unsuccessful response by throwing HttpException
                    Result.failure(HttpException(response))
                }
            } catch (e: Exception) {
                // Catch any exceptions during the network call and return failure
                Result.failure(e)
            }
        }
    }
}