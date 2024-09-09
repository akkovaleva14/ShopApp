package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ProductDao {

    @Query("DELETE FROM products")
    fun clearCache()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertProducts(products: List<ProductEntity>)

    @Query("SELECT * FROM products ORDER BY price ASC")
    fun getAllProductsSortedByPriceAsc(): List<ProductEntity>

    @Query("SELECT * FROM products ORDER BY price DESC")
    fun getAllProductsSortedByPriceDesc(): List<ProductEntity>
}
