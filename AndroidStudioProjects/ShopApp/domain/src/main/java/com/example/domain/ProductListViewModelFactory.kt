package com.example.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ProductListViewModelFactory(
    private val repository: ProductRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductListViewModel::class.java)) {
            return ProductListViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}