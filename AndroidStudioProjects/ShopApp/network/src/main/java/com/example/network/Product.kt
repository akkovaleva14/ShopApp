package com.example.network

import com.google.gson.*
import java.lang.reflect.Type
import com.google.gson.annotations.SerializedName

data class Product(
    @SerializedName("_id") val id: String,
    val name: String,
    val price: Double,
    @SerializedName("discounted_price") val discountedPrice: Double?,
    val images: List<String>,
    val description: String?,
    @SerializedName("product_rating") val productRating: Double?,
    val brand: String?,
    @SerializedName("product_specifications") val productSpecifications: Any // Используем Any, т.к. это может быть как объект, так и строка
)

data class ProductSpecification(
    val key: String,
    val value: String
)

class ProductSpecificationsDeserializer : JsonDeserializer<Any> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Any {
        return if (json != null && json.isJsonObject) {
            // Если это объект, возвращаем объект
            context!!.deserialize<JsonObject>(json, JsonObject::class.java)
        } else {
            // Если это строка, возвращаем строку
            json!!.asString
        }
    }
}

data class ProductResponse(
    val status: String,
    val count: Int,
    val Data: List<Product>
)

data class ProductDetailsResponse(
    val status: String,
    val data: Product
)