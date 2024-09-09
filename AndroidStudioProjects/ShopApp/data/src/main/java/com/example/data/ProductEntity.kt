package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

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
    val productSpecifications: String // Store as JSON string
)