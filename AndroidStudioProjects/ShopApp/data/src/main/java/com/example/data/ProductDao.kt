package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

// Data Access Object (DAO) for product operations
@Dao
interface ProductDao {

    // Clears all product entries from the database
    @Query("DELETE FROM products")
    fun clearCache()

    // Inserts a list of products into the database, replacing existing entries with the same ID
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertProducts(products: List<ProductEntity>)

    // Retrieves a list of products sorted by price in ascending order
    // The number of results returned is limited by the 'limit' parameter, and pagination is handled with the 'offset' parameter
    @Query("SELECT * FROM products ORDER BY price ASC LIMIT :limit OFFSET :offset")
    fun getProductsSortedByPriceAsc(limit: Int, offset: Int): List<ProductEntity>

    // Retrieves a list of products sorted by price in descending order
    // The number of results returned is limited by the 'limit' parameter, and pagination is handled with the 'offset' parameter
    @Query("SELECT * FROM products ORDER BY price DESC LIMIT :limit OFFSET :offset")
    fun getProductsSortedByPriceDesc(limit: Int, offset: Int): List<ProductEntity>
}