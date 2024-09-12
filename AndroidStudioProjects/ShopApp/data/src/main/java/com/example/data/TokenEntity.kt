package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// Defines a Room entity for storing tokens in the "tokens" table
@Entity(tableName = "tokens")
data class TokenEntity(
    @PrimaryKey val id: Int = 1, // Single entry, so ID is constant
    val token: String // Field to store the token value
)