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

    suspend fun loadProducts(category: String?, sort: String?, page: Int, limit: Int): Result<List<Product>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getProducts(
                    category = category,
                    minPrice = null,
                    maxPrice = null,
                    sort = sort,
                    page = page,
                    limit = limit
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

    suspend fun getCachedProducts(sortOrder: String, page: Int, limit: Int): List<ProductEntity> {
        return withContext(Dispatchers.IO) {
            val offset = (page - 1) * limit
            when (sortOrder) {
                "price_asc" -> productDao.getProductsSortedByPriceAsc(limit, offset)
                "price_desc" -> productDao.getProductsSortedByPriceDesc(limit, offset)
                else -> productDao.getProductsSortedByPriceAsc(limit, offset)
            }
        }
    }
}

