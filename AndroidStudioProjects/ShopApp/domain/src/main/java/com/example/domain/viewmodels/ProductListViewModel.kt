package com.example.domain.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.repositories.ProductRepository
import com.example.domain.toProduct
import com.example.network.Product
import kotlinx.coroutines.launch

// ViewModel for managing a list of products, including fetching and caching.
class ProductListViewModel(private val repository: ProductRepository) : ViewModel() {

    // LiveData for observing the list of products.
    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> get() = _products

    // LiveData for observing error messages.
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    // Variable to manage pagination.
    private val pageSize = 4 // Number of products per page

    // Function to load products from the network or API.
    fun loadProducts(category: String?, sort: String?, page: Int) {
        viewModelScope.launch {
            val result = repository.loadProducts(category, sort, page, pageSize)
            result.onSuccess { products ->
                // Update LiveData with the fetched products.
                _products.value = products
            }.onFailure { exception ->
                // Update LiveData with the error message.
                _error.value = exception.message
            }
        }
    }

    // Function to load cached products from the local database.
    fun loadCachedProducts(sortOrder: String, page: Int) {
        viewModelScope.launch {
            // Retrieve cached products, map them to network Product objects, and update LiveData.
            val products =
                repository.getCachedProducts(sortOrder, page, pageSize).map { it.toProduct() }
            _products.value = products
        }
    }
}