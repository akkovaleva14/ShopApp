package com.example.shopapp.ui

import android.os.Bundle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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

        productAdapter = ProductAdapter { product ->
            val bundle = Bundle().apply {
                putString("productId", product.id)
            }
            findNavController().navigate(R.id.productFragment, bundle)
        }

        binding.productRecyclerView.layoutManager = GridLayoutManager(context, 2)
        binding.productRecyclerView.adapter = productAdapter

        binding.retryButton.setOnClickListener {
            // Check network availability and attempt to reload products
            if (NetworkUtils.isInternetAvailable(requireContext())) {
                // Retry the category selection that failed
                retryCategory?.let {
                    loadProducts(it, getNetworkSortOrder())
                }
            } else {
                // Show Toast message and keep retry button visible
                Toast.makeText(requireContext(), "No internet connection", Toast.LENGTH_SHORT)
                    .show()
            }
        }

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

        binding.filterToggleButton.setOnClickListener {
            Log.d(
                "ProductListFragment",
                "Filter toggle button clicked. isAscending before toggle: $isAscending"
            )

            // Переключаем порядок сортировки
            isAscending = !isAscending
            binding.filterToggleButton.setImageResource(if (isAscending) R.drawable.ic_up else R.drawable.ic_down)
            Log.d("ProductListFragment", "isAscending after toggle: $isAscending")

            // Загружаем продукты, учитывая текущий статус интернет-соединения
            if (NetworkUtils.isInternetAvailable(requireContext())) {
                Log.d(
                    "ProductListFragment",
                    "Internet is available, reloading products from server"
                )
                loadProducts(selectedCategory, getNetworkSortOrder())
            } else {
                Log.d("ProductListFragment", "No internet connection, loading products from cache")
                loadCachedProducts(getCacheSortOrder())
            }
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

                if (NetworkUtils.isInternetAvailable(requireContext())) {
                    selectedCategory = category
                    retryCategory = null // Clear retryCategory when internet is available
                    loadProducts(selectedCategory, getNetworkSortOrder())
                } else {
                    retryCategory = category // Save the category to retry later
                    Toast.makeText(requireContext(), "No internet connection", Toast.LENGTH_SHORT)
                        .show()
                    binding.retryButton.visibility = View.VISIBLE
                    binding.productProgressBar.visibility = View.GONE
                }
            }
        }
    }

    private fun loadProducts(category: String?, sort: String) {
        Log.d("ProductListFragment", "Loading products for category: $category with sort: $sort")

        if (NetworkUtils.isInternetAvailable(requireContext())) {
            Log.d("ProductListFragment", "Internet available, fetching from server")

            // Показываем индикатор загрузки только если интернет доступен
            binding.productProgressBar.visibility = View.VISIBLE
            binding.retryButton.visibility = View.GONE // Скрываем кнопку повтора во время загрузки

            // Стартуем корутину для скрытия ProgressBar через 5 секунд, если товары не загрузились
            viewLifecycleOwner.lifecycleScope.launch {
                delay(5000) // Задержка 5 секунд

                // Если товары не загружены через 5 секунд, показываем кнопку повтора и скрываем ProgressBar
                if (viewModel.products.value.isNullOrEmpty()) {
                    binding.productProgressBar.visibility = View.GONE
                    binding.retryButton.visibility = View.VISIBLE
                }
            }

            // Загружаем товары из ViewModel
            viewModel.loadProducts(category, sort)
        } else {
            Log.d("ProductListFragment", "No internet connection, fetching from cache")

            // Если интернета нет, ProgressBar не показываем
            loadCachedProducts(getCacheSortOrder())
        }
    }

    private fun loadCachedProducts(sortOrder: String) {
        Log.d("ProductListFragment", "Loading cached products with sort: $sortOrder")
        viewModel.loadCachedProducts(sortOrder)
        // Убедитесь, что кнопка "Повторить" скрыта, если данные загружены из кэша
        binding.retryButton.visibility = View.GONE
    }

    private fun getNetworkSortOrder(): String {
        return if (isAscending) "price" else "-price"
    }

    private fun getCacheSortOrder(): String {
        return if (isAscending) "price_asc" else "price_desc"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        ioScope.cancel() // Cancel any ongoing coroutines
        Log.d("ProductListFragment", "onDestroyView called")
    }
}
