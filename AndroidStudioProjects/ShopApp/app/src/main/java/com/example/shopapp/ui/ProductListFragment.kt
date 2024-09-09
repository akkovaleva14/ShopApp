package com.example.shopapp.ui

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.data.AppDatabase
import com.example.domain.ProductListViewModel
import com.example.domain.ProductListViewModelFactory
import com.example.domain.ProductRepository
import com.example.network.RetrofitClient
import com.example.shopapp.ProductAdapter
import com.example.shopapp.R
import com.example.shopapp.databinding.FragmentProductListBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel

class ProductListFragment : Fragment() {

    private lateinit var viewModel: ProductListViewModel
    private var _binding: FragmentProductListBinding? = null
    private val binding get() = _binding!!
    private lateinit var productAdapter: ProductAdapter
    private var isAscending = true // Default to ascending order
    private var selectedCategory: String? = "computers" // Default category
    private val ioScope = CoroutineScope(Dispatchers.IO) // CoroutineScope for IO operations

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("ProductListFragment", "onViewCreated called")

        // Initialize ViewModel with factory
        val productDao = AppDatabase.getDatabase(requireContext()).productDao()
        val repository = ProductRepository(RetrofitClient.apiService, productDao)
        viewModel = ViewModelProvider(this, ProductListViewModelFactory(repository)).get(
            ProductListViewModel::class.java
        )

        productAdapter = ProductAdapter()
        binding.productRecyclerView.layoutManager = GridLayoutManager(context, 2) // 2 columns
        binding.productRecyclerView.adapter = productAdapter

        // Observe products data
        viewModel.products.observe(viewLifecycleOwner, Observer { products ->
            productAdapter.submitList(products)
            binding.errorTextView.visibility = View.GONE
            binding.retryButton.visibility = View.GONE
            binding.productProgressBar.visibility = View.GONE // Hide progress bar after loading
            Log.d("ProductListFragment", "Products loaded successfully: ${products.size} items")
        })

        // Observe error messages
        viewModel.error.observe(viewLifecycleOwner, Observer { error ->
            binding.errorTextView.text = error
            binding.errorTextView.visibility = View.VISIBLE
            binding.retryButton.visibility = View.VISIBLE
            binding.productProgressBar.visibility = View.GONE // Hide progress bar on error
            Log.e("ProductListFragment", "Error loading products: $error")
        })

        // Handle filter toggle button
        binding.filterToggleButton.setImageResource(R.drawable.ic_up)

        // Handle filter toggle button
        binding.filterToggleButton.setOnClickListener {
            Log.d(
                "ProductListFragment",
                "Filter toggle button clicked. isAscending before toggle: $isAscending"
            )
            isAscending = !isAscending
            binding.filterToggleButton.setImageResource(if (isAscending) R.drawable.ic_up else R.drawable.ic_down)
            Log.d("ProductListFragment", "isAscending after toggle: $isAscending")
            loadProducts(selectedCategory, getNetworkSortOrder())
        }

        // Handle category selection (hardcoded categories)
        setCategoryListeners()

        // Load initial data with default category and ascending price order
        loadProducts(selectedCategory, getNetworkSortOrder())
    }

    private fun setCategoryListeners() {
        Log.d("ProductListFragment", "Setting category listeners")
        val categories = mapOf(
            binding.categoryComputers to "computers",
            binding.categoryBags to "bags",
            binding.categoryClothing to "clothing",
            binding.categoryFurniture to "furniture",
            binding.categoryFootwear to "footwear"
        )

        for ((view, category) in categories) {
            view.setOnClickListener {
                Log.d("ProductListFragment", "Category selected: $category")
                selectedCategory = category
                loadProducts(selectedCategory, getNetworkSortOrder())
            }
        }
    }

    private fun loadProducts(category: String?, sort: String) {
        Log.d("ProductListFragment", "Loading products for category: $category with sort: $sort")
        if (isNetworkAvailable()) {
            Log.d("ProductListFragment", "Internet available, fetching from server")
            binding.productProgressBar.visibility = View.VISIBLE // Show progress bar during load
            viewModel.loadProducts(category, sort)
        } else {
            Log.d("ProductListFragment", "No internet connection, fetching from cache")
            loadCachedProducts(getCacheSortOrder())
        }
    }

    private fun loadCachedProducts(sortOrder: String) {
        Log.d("ProductListFragment", "Loading cached products with sort: $sortOrder")
        viewModel.loadCachedProducts(sortOrder)
    }

    private fun getNetworkSortOrder(): String {
        return if (isAscending) "price" else "-price"
    }

    private fun getCacheSortOrder(): String {
        return if (isAscending) "price_asc" else "price_desc"
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        return networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        ioScope.cancel() // Cancel any ongoing coroutines
        Log.d("ProductListFragment", "onDestroyView called")
    }
}
