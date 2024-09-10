package com.example.domain.repositories

import com.example.network.ApiService
import com.example.network.ProductDetailsResponse
import retrofit2.HttpException
import retrofit2.Response
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProductItemRepository(private val apiService: ApiService) {

    suspend fun getProductDetails(productId: String): Result<ProductDetailsResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response: Response<ProductDetailsResponse> =
                    apiService.getProductDetails(productId).execute()
                if (response.isSuccessful) {
                    val productDetailsResponse = response.body()
                    if (productDetailsResponse != null) {
                        Result.success(productDetailsResponse)
                    } else {
                        Result.failure(Exception("Empty response body"))
                    }
                } else {
                    Result.failure(HttpException(response))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
