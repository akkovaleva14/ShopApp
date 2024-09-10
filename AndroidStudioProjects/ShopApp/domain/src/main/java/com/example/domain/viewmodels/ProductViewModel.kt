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

class ProductViewModel(private val repository: ProductItemRepository) : ViewModel() {

    private val _product = MutableLiveData<Product?>()
    val product: LiveData<Product?> get() = _product

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    fun loadProduct(productId: String) {
        scope.launch {
            try {
                val result = repository.getProductDetails(productId)
                if (result.isSuccess) {
                    _product.value = result.getOrNull()?.data
                } else {
                    // Handle the failure case, e.g., show an error message to the user
                    _product.value = null
                }
            } catch (e: Exception) {
                // Handle error appropriately, e.g., show a message to the user
                _product.value = null
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        job.cancel()
    }
}
