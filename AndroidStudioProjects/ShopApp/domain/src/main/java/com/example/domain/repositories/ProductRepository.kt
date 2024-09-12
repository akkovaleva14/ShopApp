package com.example.domain.repositories

import com.example.data.ProductDao
import com.example.data.ProductEntity
import com.example.domain.toEntity
import com.example.network.Product
import com.example.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProductRepository(
    private val apiService: ApiService,
    private val productDao: ProductDao
) {

    // Function to load products from the server
    suspend fun loadProducts(
        category: String?,
        sort: String?,
        page: Int,
        limit: Int
    ): Result<List<Product>> {
        return withContext(Dispatchers.IO) {
            try {
                // Make API call to fetch products based on category, sort, page, and limit
                val response = apiService.getProducts(
                    category = category,
                    minPrice = null,
                    maxPrice = null,
                    sort = sort,
                    page = page,
                    limit = limit
                ).execute()

                // Check if the response is successful
                if (response.isSuccessful) {
                    val productResponse = response.body()

                    // Check if the response status is "success"
                    if (productResponse?.status == "success") {
                        val products = productResponse.Data

                        // Clear cached products and insert the new products into the database
                        productDao.clearCache()
                        productDao.insertProducts(products.map { it.toEntity() })

                        // Return success with the list of products
                        Result.success(products)
                    } else {
                        // Handle error in the product response status
                        Result.failure(Exception("Error loading products: ${productResponse?.status}"))
                    }
                } else {
                    // Handle server error with HTTP response code
                    Result.failure(Exception("Server error: ${response.code()}"))
                }
            } catch (e: Exception) {
                // Handle any exceptions that occur during the API call
                Result.failure(e)
            }
        }
    }

    // Function to retrieve cached products from the database
    suspend fun getCachedProducts(sortOrder: String, page: Int, limit: Int): List<ProductEntity> {
        return withContext(Dispatchers.IO) {
            // Calculate the offset based on the page number
            val offset = (page - 1) * limit

            // Retrieve products from the cache based on the sort order
            when (sortOrder) {
                "price_asc" -> productDao.getProductsSortedByPriceAsc(limit, offset)
                "price_desc" -> productDao.getProductsSortedByPriceDesc(limit, offset)
                else -> productDao.getProductsSortedByPriceAsc(limit, offset)
            }
        }
    }
}