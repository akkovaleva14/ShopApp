package com.example.domain

import com.example.data.ProductEntity
import com.example.network.Product

fun Product.toEntity() = ProductEntity(
    id = id,
    name = name,
    price = price,
    discountedPrice = discountedPrice,
    images = images.joinToString(","),
    description = description,
    productRating = productRating,
    brand = brand,
    productSpecifications = productSpecifications.toString()
)

fun ProductEntity.toProduct() = Product(
    id = id,
    name = name,
    price = price,
    discountedPrice = discountedPrice,
    images = images.split(","),
    description = description,
    productRating = productRating,
    brand = brand,
    productSpecifications = productSpecifications
)
