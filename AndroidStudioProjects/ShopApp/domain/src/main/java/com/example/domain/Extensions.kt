package com.example.domain

import com.example.data.ProductEntity
import com.example.network.Product

// Extension function to convert a Product object from the network layer to a ProductEntity object for the database layer.
fun Product.toEntity() = ProductEntity(
    id = id,
    name = name,
    price = price,
    discountedPrice = discountedPrice,
    images = images.toString(), // Convert the list of image URLs to a comma-separated string.
    description = description,
    productRating = productRating,
    brand = brand,
    productSpecifications = productSpecifications.toString(), // Convert the list of product specifications to a comma-separated string.
    category = category.toString() // Convert the list of categories to a comma-separated string.
)

// Extension function to convert a ProductEntity object from the database layer to a Product object for the network layer.
fun ProductEntity.toProduct() = Product(
    id = id,
    name = name,
    price = price,
    discountedPrice = discountedPrice,
    images = images.split(","), // Split the comma-separated string into a list of image URLs.
    description = description,
    productRating = productRating,
    brand = brand,
    productSpecifications = productSpecifications.split(","), // Split the comma-separated string into a list of product specifications.
    category = category.split(",") // Split the comma-separated string into a list of categories.
)