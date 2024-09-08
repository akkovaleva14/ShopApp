package com.example.network

data class ProductResponse(
    val status: String,
    val count: Int,
    val Data: List<Product>
)

data class Product(
    val _id: String,
    val name: String,
    val category: List<String>,
    val price: Int,
    val discounted_price: Int,
    val images: List<String>,
    val description: String,
    val product_rating: Double,
    val brand: String,
    val product_specifications: List<ProductSpecification>
)

data class ProductSpecification(
    val key: String,
    val value: String
)

data class ProductDetailsResponse(
    val status: String,
    val data: Product
)