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
    private var currentPage = 1
    private val pageSize = 4 // Количество товаров на одной странице

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            val category = it.getString("CATEGORY")
            val sortOrder = it.getString("SORT_ORDER")
            val pageNumber = it.getInt("PAGE_NUMBER", 1)  // Восстанавливаем номер страницы, по умолчанию 1
            if (category != null && sortOrder != null) {
                selectedCategory = category
                setNetworkSortOrder(sortOrder)
                currentPage = pageNumber  // Восстанавливаем текущую страницу
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
                Toast.makeText(requireContext(), "No internet connection", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        viewModel.products.observe(viewLifecycleOwner, Observer { products ->
            productAdapter.submitList(products)
            binding.retryButton.visibility = View.GONE
            binding.productProgressBar.visibility = View.GONE

            // Деактивируем previousPageButton на первой странице
            binding.previousPageButton.isEnabled = currentPage > 1

            // Деактивируем nextPageButton, если меньше товаров, чем pageSize
            binding.nextPageButton.isEnabled = products.size == pageSize
        })

        viewModel.error.observe(viewLifecycleOwner, Observer { error ->
            binding.retryButton.visibility = View.VISIBLE
            binding.productProgressBar.visibility = View.GONE
        })

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
                    loadProducts(selectedCategory, currentPage)
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

    private fun loadCachedProducts(page: Int) {
        val sortOrder = getSortOrder(forNetwork = false)
        viewModel.loadCachedProducts(sortOrder, page)
        binding.retryButton.visibility = View.GONE
    }

    private fun getSortOrder(forNetwork: Boolean): String {
        return if (isAscending) {
            if (forNetwork) "price" else "price_asc"
        } else {
            if (forNetwork) "-price" else "price_desc"
        }
    }

    private fun setNetworkSortOrder(sortOrder: String) {
        lastSortOrder = sortOrder
        isAscending = sortOrder == "price"
        binding.filterToggleButton.setImageResource(if (isAscending) R.drawable.ic_up else R.drawable.ic_down)
    }

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
        ioScope.cancel()
    }
}