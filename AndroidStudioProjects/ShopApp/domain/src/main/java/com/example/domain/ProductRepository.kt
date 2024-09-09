package com.example.domain

import com.example.data.ProductDao
import com.example.data.ProductEntity
import com.example.network.ApiService
import com.example.network.Product
import com.example.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProductRepository(private val apiService: ApiService,
                        private val productDao: ProductDao) {

    suspend fun loadProducts(category: String?, sort: String?): Result<List<Product>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = RetrofitClient.apiService.getProducts(
                    category,
                    minPrice = null,
                    maxPrice = null,
                    sort
                ).execute()
                if (response.isSuccessful) {
                    val productResponse = response.body()
                    if (productResponse?.status == "success") {
                        val products = productResponse.Data
                        productDao.clearCache()
                        productDao.insertProducts(products.map { it.toEntity() })
                        Result.success(products)
                    } else {
                        Result.failure(Exception("Error loading products: ${productResponse?.status}"))
                    }
                } else {
                    Result.failure(Exception("Server error: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // Function to get cached products from the database with sorting
    suspend fun getCachedProducts(sortOrder: String): List<ProductEntity> {
        return withContext(Dispatchers.IO) {
            when (sortOrder) {
                "price_asc" -> productDao.getAllProductsSortedByPriceAsc()
                "price_desc" -> productDao.getAllProductsSortedByPriceDesc()
                else -> productDao.getAllProductsSortedByPriceAsc() // Default sorting
            }
        }
    }

}