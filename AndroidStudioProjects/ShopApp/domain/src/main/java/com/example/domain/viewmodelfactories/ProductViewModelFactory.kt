package com.example.domain.viewmodelfactories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.domain.repositories.ProductItemRepository
import com.example.domain.viewmodels.ProductViewModel
import com.example.network.RetrofitClient

class ProductViewModelFactory(
    private val productItemRepository: ProductItemRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductViewModel::class.java)) {
            return ProductViewModel(productItemRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}