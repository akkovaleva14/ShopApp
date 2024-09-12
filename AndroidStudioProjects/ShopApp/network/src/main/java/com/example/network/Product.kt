package com.example.network

import com.google.gson.*
import java.lang.reflect.Type
import com.google.gson.annotations.SerializedName

// Data class representing a product retrieved from the network.
data class Product(
    @SerializedName("_id") val id: String,
    val name: String,
    val category: List<String>,
    val price: Double,
    @SerializedName("discounted_price") val discountedPrice: Double?,
    val images: List<String>,
    val description: String?,
    @SerializedName("product_rating") val productRating: Double?,
    val brand: String?,
    @SerializedName("product_specifications") val productSpecifications: Any?
)

// Custom deserializer for product specifications.
// Handles the deserialization of product specifications which can be a JSON object or a string.
class ProductSpecificationsDeserializer : JsonDeserializer<Any> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Any {
        return if (json != null && json.isJsonObject) {
            // If the JSON element is an object, deserialize it into a JsonObject.
            context!!.deserialize<JsonObject>(json, JsonObject::class.java)
        } else {
            // If the JSON element is a string, return it as a string.
            json!!.asString
        }
    }
}

// Data class representing the response for fetching a list of products.
data class ProductResponse(
    val status: String,
    val count: Int,
    val Data: List<Product>
)

// Data class representing the response for fetching details of a single product.
data class ProductDetailsResponse(
    val status: String,
    val data: Product?
)