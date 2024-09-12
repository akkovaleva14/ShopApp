package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// Represents a product entity in the database
@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val name: String,
    val price: Double,
    val discountedPrice: Double?,
    val images: String, // Comma-separated list of image URLs
    val description: String?,
    val productRating: Double?,
    val brand: String?,
    val productSpecifications: String, // Specifications of the product stored as a JSON string
    val category: String
)