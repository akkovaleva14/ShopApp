package com.example.shopapp.ui

import android.os.Bundle
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
    private var retryCategory: String? = null // To store the category for retrying
    private var lastCategory: String? = null
    private var lastSortOrder: String? = null
    private var currentPage = 1
    private val pageSize = 4 // Number of products per page

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Restore category, sort order, and page number from arguments, if available
        arguments?.let {
            val category = it.getString("CATEGORY")
            val sortOrder = it.getString("SORT_ORDER")
            val pageNumber = it.getInt("PAGE_NUMBER", 1)  // Default to page 1 if not provided
            if (category != null && sortOrder != null) {
                selectedCategory = category
                setNetworkSortOrder(sortOrder)
                currentPage = pageNumber  // Restore the current page
            }
        }

        val productDao = AppDatabase.getDatabase(requireContext()).productDao()
        val repository = ProductRepository(RetrofitClient.apiService, productDao)
        viewModel = ViewModelProvider(this, ProductListViewModelFactory(repository)).get(
            ProductListViewModel::class.java
        )

        productAdapter = ProductAdapter { product ->
            // Check for internet connection before navigating to product details
            if (NetworkUtils.isInternetAvailable(requireContext())) {
                val bundle = Bundle().apply {
                    putString("productId", product.id)
                    putString("CATEGORY", selectedCategory)
                    putString("SORT_ORDER", getSortOrder(true))
                    putInt("PAGE_NUMBER", currentPage)
                }
                findNavController().navigate(R.id.productFragment, bundle)
            } else {
                lastCategory = selectedCategory
                lastSortOrder = getSortOrder(true)
                showErrorFragment(product.id)
            }
        }

        binding.productRecyclerView.layoutManager = GridLayoutManager(context, 2)
        binding.productRecyclerView.adapter = productAdapter

        binding.nextPageButton.setOnClickListener {
            currentPage++
            loadProducts(selectedCategory, currentPage)
        }

        binding.previousPageButton.setOnClickListener {
            if (currentPage > 1) {
                currentPage--
                loadProducts(selectedCategory, currentPage)
            }
        }

        binding.retryButton.setOnClickListener {
            if (NetworkUtils.isInternetAvailable(requireContext())) {
                retryCategory?.let {
                    loadProducts(it, currentPage)
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    R.string.no_internet_connection,
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        }

        // Observe the list of products and update the UI accordingly
        viewModel.products.observe(viewLifecycleOwner, Observer { products ->
            productAdapter.submitList(products)
            binding.retryButton.visibility = View.GONE
            binding.productProgressBar.visibility = View.GONE

            // Disable previousPageButton on the first page
            binding.previousPageButton.isEnabled = currentPage > 1

            // Disable nextPageButton if fewer products than pageSize
            binding.nextPageButton.isEnabled = products.size == pageSize
        })

        viewModel.error.observe(viewLifecycleOwner, Observer { error ->
            binding.retryButton.visibility = View.VISIBLE
            binding.productProgressBar.visibility = View.GONE
        })

        // Toggle sorting order and reload products when the sort button is clicked
        binding.filterToggleButton.setOnClickListener {
            isAscending = !isAscending
            binding.filterToggleButton.setImageResource(if (isAscending) R.drawable.ic_up else R.drawable.ic_down)
            if (NetworkUtils.isInternetAvailable(requireContext())) {
                loadProducts(selectedCategory, currentPage)
            } else {
                loadCachedProducts(currentPage)
            }
        }

        setCategoryListeners()
        loadProducts(selectedCategory, currentPage)
    }

    // Set click listeners for category buttons
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
                    currentPage = 1
                    loadProducts(selectedCategory, currentPage)
                } else {
                    retryCategory = category
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.no_internet_connection), Toast.LENGTH_SHORT
                    )
                        .show()
                    binding.retryButton.visibility = View.VISIBLE
                    binding.productProgressBar.visibility = View.GONE
                }
            }
        }
    }

    // Load products based on the selected category and page number
    private fun loadProducts(category: String?, page: Int) {
        val sortOrder = getSortOrder(forNetwork = true)
        if (NetworkUtils.isInternetAvailable(requireContext())) {
            binding.productProgressBar.visibility = View.VISIBLE
            binding.retryButton.visibility = View.GONE
            viewLifecycleOwner.lifecycleScope.launch {
                delay(5000)
                if (viewModel.products.value.isNullOrEmpty()) {
                    binding.productProgressBar.visibility = View.GONE
                    binding.retryButton.visibility = View.VISIBLE
                }
            }
            viewModel.loadProducts(category, sortOrder, page)
        } else {
            loadCachedProducts(page)
        }
    }

    // Load cached products when there is no network
    private fun loadCachedProducts(page: Int) {
        val sortOrder = getSortOrder(forNetwork = false)
        viewModel.loadCachedProducts(sortOrder, page)
        binding.retryButton.visibility = View.GONE
    }

    // Get the sort order string based on ascending/descending and if it's for network or cache
    private fun getSortOrder(forNetwork: Boolean): String {
        return if (isAscending) {
            if (forNetwork) "price" else "price_asc"
        } else {
            if (forNetwork) "-price" else "price_desc"
        }
    }

    // Set the sort order based on the network response
    private fun setNetworkSortOrder(sortOrder: String) {
        lastSortOrder = sortOrder
        isAscending = sortOrder == "price"
        binding.filterToggleButton.setImageResource(if (isAscending) R.drawable.ic_up else R.drawable.ic_down)
    }

    // Navigate to the error fragment in case of network failure
    private fun showErrorFragment(productId: String) {
        val bundle = Bundle().apply {
            putString("productId", productId)
            putString("CATEGORY", lastCategory)
            putString("SORT_ORDER", lastSortOrder)
            putInt("PAGE_NUMBER", currentPage)
        }
        findNavController().navigate(R.id.errorFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}