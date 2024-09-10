package com.example.shopapp.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.data.AppDatabase
import com.example.domain.viewmodels.ProductListViewModel
import com.example.domain.viewmodelfactories.ProductListViewModelFactory
import com.example.domain.repositories.ProductRepository
import com.example.network.RetrofitClient
import com.example.shopapp.adapters.ProductAdapter
import com.example.shopapp.R
import com.example.shopapp.databinding.FragmentProductListBinding
import com.example.shopapp.utils.NetworkUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancel

class ProductListFragment : Fragment() {

    private lateinit var viewModel: ProductListViewModel
    private var _binding: FragmentProductListBinding? = null
    private val binding get() = _binding!!
    private lateinit var productAdapter: ProductAdapter
    private var isAscending = true // Default to ascending order
    private var selectedCategory: String? = "computers" // Default category
    private var retryCategory: String? = null // To store the category to retry
    private val ioScope = CoroutineScope(Dispatchers.IO) // CoroutineScope for IO operations
    private var lastCategory: String? = null
    private var lastSortOrder: String? = null

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

        arguments?.let {
            val category = it.getString("CATEGORY")
            val sortOrder = it.getString("SORT_ORDER")
            if (category != null && sortOrder != null) {
                // Восстанавливаем категорию и сортировку
                selectedCategory = category
                setNetworkSortOrder(sortOrder) // Обновляет иконку и состояние сортировки
            }
        }

        val productDao = AppDatabase.getDatabase(requireContext()).productDao()
        val repository = ProductRepository(RetrofitClient.apiService, productDao)
        viewModel = ViewModelProvider(this, ProductListViewModelFactory(repository)).get(
            ProductListViewModel::class.java
        )

        productAdapter = ProductAdapter { product ->
            if (NetworkUtils.isInternetAvailable(requireContext())) {
                val bundle = Bundle().apply {
                    putString("productId", product.id)
                    putString("CATEGORY", selectedCategory)
                    putString("SORT_ORDER", getNetworkSortOrder())
                }
                findNavController().navigate(R.id.productFragment, bundle)
            } else {
                lastCategory = selectedCategory
                lastSortOrder = getNetworkSortOrder()
                showErrorFragment(product.id)
            }
        }

        binding.productRecyclerView.layoutManager = GridLayoutManager(context, 2)
        binding.productRecyclerView.adapter = productAdapter

        binding.retryButton.setOnClickListener {
            // Check network availability and attempt to reload products
            if (NetworkUtils.isInternetAvailable(requireContext())) {
                retryCategory?.let {
                    loadProducts(it, getNetworkSortOrder())
                }
            } else {
                Toast.makeText(requireContext(), "No internet connection", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        // Observe products data
        viewModel.products.observe(viewLifecycleOwner, Observer { products ->
            productAdapter.submitList(products)
            binding.retryButton.visibility = View.GONE
            binding.productProgressBar.visibility = View.GONE // Hide progress bar after loading
            Log.d("ProductListFragment", "Products loaded successfully: ${products.size} items")
        })

        // Observe error messages
        viewModel.error.observe(viewLifecycleOwner, Observer { error ->
            binding.retryButton.visibility = View.VISIBLE
            binding.productProgressBar.visibility = View.GONE // Hide progress bar on error
            Log.e("ProductListFragment", "Error loading products: $error")
        })

        binding.filterToggleButton.setOnClickListener {
            isAscending = !isAscending
            binding.filterToggleButton.setImageResource(if (isAscending) R.drawable.ic_up else R.drawable.ic_down)
            if (NetworkUtils.isInternetAvailable(requireContext())) {
                loadProducts(selectedCategory, getNetworkSortOrder())
            } else {
                loadCachedProducts(getCacheSortOrder())
            }
        }

        // Handle category selection (hardcoded categories)
        setCategoryListeners()

        // Load initial data with default category and ascending price order
        loadProducts(selectedCategory, getNetworkSortOrder())
    }

    private fun setCategoryListeners() {
        val categories = mapOf(
            binding.categoryComputers to "computers",
            binding.categoryBags to "bags",
            binding.categoryClothing to "clothing",
            binding.categoryFurniture to "furniture",
            binding.categoryFootwear to "footwear"
        )

        for ((view, category) in categories) {
            view.setOnClickListener {
                if (NetworkUtils.isInternetAvailable(requireContext())) {
                    selectedCategory = category
                    retryCategory = null
                    loadProducts(selectedCategory, getNetworkSortOrder())
                } else {
                    retryCategory = category
                    Toast.makeText(requireContext(), "No internet connection", Toast.LENGTH_SHORT)
                        .show()
                    binding.retryButton.visibility = View.VISIBLE
                    binding.productProgressBar.visibility = View.GONE
                }
            }
        }
    }

    private fun loadProducts(category: String?, sort: String) {
        if (NetworkUtils.isInternetAvailable(requireContext())) {
            binding.productProgressBar.visibility = View.VISIBLE
            binding.retryButton.visibility = View.GONE
            viewLifecycleOwner.lifecycleScope.launch {
                delay(5000) // 5 seconds delay
                if (viewModel.products.value.isNullOrEmpty()) {
                    binding.productProgressBar.visibility = View.GONE
                    binding.retryButton.visibility = View.VISIBLE
                }
            }
            viewModel.loadProducts(category, sort)
        } else {
            loadCachedProducts(getCacheSortOrder())
        }
    }

    private fun loadCachedProducts(sortOrder: String) {
        viewModel.loadCachedProducts(sortOrder)
        binding.retryButton.visibility = View.GONE
    }

    private fun getNetworkSortOrder(): String {
        return if (isAscending) "price" else "-price"
    }

    private fun getCacheSortOrder(): String {
        return if (isAscending) "price_asc" else "price_desc"
    }

    private fun showErrorFragment(productId: String) {
        val bundle = Bundle().apply {
            putString("productId", productId)
            putString("CATEGORY", lastCategory)
            putString("SORT_ORDER", lastSortOrder)
        }
        findNavController().navigate(R.id.errorFragment, bundle)
    }

    private fun setNetworkSortOrder(sortOrder: String) {
        lastSortOrder = sortOrder
        isAscending = sortOrder == "price" // Проверка на тип сортировки
        // Устанавливаем соответствующую иконку для filterToggleButton
        binding.filterToggleButton.setImageResource(if (isAscending) R.drawable.ic_up else R.drawable.ic_down)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        ioScope.cancel() // Cancel any ongoing coroutines
        Log.d("ProductListFragment", "onDestroyView called")
    }
}