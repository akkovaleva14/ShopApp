package com.example.domain

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.network.Product
import com.example.network.ProductResponse
import com.example.network.RetrofitClient
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProductListViewModel : ViewModel() {

    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> get() = _products

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    fun loadProducts(category: String?, sort: String?) {
        viewModelScope.launch {
            RetrofitClient.apiService.getProducts(
                category = category,
                sort = sort
            ).enqueue(object : Callback<ProductResponse> {
                override fun onResponse(call: Call<ProductResponse>, response: Response<ProductResponse>) {
                    if (response.isSuccessful) {
                        val productResponse = response.body()
                        if (productResponse?.status == "success") {
                            _products.value = productResponse.Data
                        } else {
                            _error.value = "Error loading products"
                        }
                    } else {
                        _error.value = "Server error"
                    }
                }

                override fun onFailure(call: Call<ProductResponse>, t: Throwable) {
                    _error.value = "Network error"
                }
            })
        }
    }
}
