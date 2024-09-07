package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "token_table")
data class Token(
    @PrimaryKey val id: Int = 1,
    val token: String
)