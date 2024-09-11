package com.example.domain.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.repositories.ProductRepository
import com.example.domain.toProduct
import com.example.network.Product
import kotlinx.coroutines.launch

class ProductListViewModel(private val repository: ProductRepository) : ViewModel() {

    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> get() = _products

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    private var currentPage = 1
    private val pageSize = 4 // 4 товара на странице

    fun loadProducts(category: String?, sort: String?, page: Int) {
        viewModelScope.launch {
            val result = repository.loadProducts(category, sort, page, pageSize)
            result.onSuccess { products ->
                _products.value = products
            }.onFailure { exception ->
                _error.value = exception.message
            }
        }
    }

    fun loadCachedProducts(sortOrder: String, page: Int) {
        viewModelScope.launch {
            val products = repository.getCachedProducts(sortOrder, page, pageSize).map { it.toProduct() }
            _products.value = products
        }
    }
}
