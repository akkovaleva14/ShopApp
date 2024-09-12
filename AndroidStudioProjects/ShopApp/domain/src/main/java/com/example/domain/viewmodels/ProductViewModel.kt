package com.example.domain.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.domain.repositories.ProductItemRepository
import com.example.network.Product
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

// ViewModel responsible for loading product details and managing product data.
class ProductViewModel(private val repository: ProductItemRepository) : ViewModel() {

    // LiveData to observe the product details. MutableLiveData is used to update product details internally.
    private val _product = MutableLiveData<Product?>()
    val product: LiveData<Product?> get() = _product

    // Job and CoroutineScope for managing background operations.
    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    // Function to load product details based on the provided product ID.
    fun loadProduct(productId: String) {
        scope.launch {
            try {
                // Request product details from the repository.
                val result = repository.getProductDetails(productId)
                if (result.isSuccess) {
                    // If successful, update the LiveData with the product details.
                    _product.value = result.getOrNull()?.data
                } else {
                    // If failed, handle the failure case by setting product to null.
                    _product.value = null
                }
            } catch (e: Exception) {
                // Handle any exceptions that occur during the request.
                _product.value = null
            }
        }
    }

    // Clean up resources when the ViewModel is cleared.
    override fun onCleared() {
        super.onCleared()
        job.cancel()  // Cancel the coroutine job to prevent memory leaks.
    }
}