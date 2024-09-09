package com.example.shopapp.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.domain.ProductListViewModel
import com.example.shopapp.ProductAdapter
import com.example.shopapp.R
import com.example.shopapp.databinding.FragmentProductListBinding

class ProductListFragment : Fragment() {

    private lateinit var viewModel: ProductListViewModel
    private var _binding: FragmentProductListBinding? = null
    private val binding get() = _binding!!
    private var isAscending = false // Default to descending order (убывание цены)
    private var selectedCategory: String? = "computers" // Default category

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProductListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("ProductListFragment", "onViewCreated called")

        viewModel = ViewModelProvider(this).get(ProductListViewModel::class.java)

        val productAdapter = ProductAdapter()
        val gridLayoutManager = GridLayoutManager(context, 2) // 2 columns

        binding.productRecyclerView.layoutManager = gridLayoutManager
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

        binding.retryButton.setOnClickListener {
            Log.d("ProductListFragment", "Retry button clicked")
            loadProducts(selectedCategory, getSortOrder())
        }

        // Set default filter icon to descending (убывание цены)
        binding.filterToggleButton.setImageResource(R.drawable.ic_down)

        // Handle filter toggle button
        binding.filterToggleButton.setOnClickListener {
            Log.d("ProductListFragment", "Filter toggle button clicked. isAscending before toggle: $isAscending")
            isAscending = !isAscending
            binding.filterToggleButton.setImageResource(
                if (isAscending) R.drawable.ic_up else R.drawable.ic_down
            )
            Log.d("ProductListFragment", "isAscending after toggle: $isAscending")
            loadProducts(selectedCategory, getSortOrder())
        }

        // Handle category selection (hardcoded categories)
        setCategoryListeners()

        // Load initial data with default category and descending price order
        loadProducts(selectedCategory, getSortOrder())
    }

    private fun setCategoryListeners() {
        Log.d("ProductListFragment", "Setting category listeners")
        val categories = mapOf(
            binding.categoryComputers to "computers",
            binding.categoryBags to "bags",
            binding.categoryClothing to "clothing"
        )

        for ((view, category) in categories) {
            view.setOnClickListener {
                Log.d("ProductListFragment", "Category selected: $category")
                selectedCategory = category
                loadProducts(selectedCategory, getSortOrder())
            }
        }
    }

    private fun loadProducts(category: String?, sort: String?) {
        Log.d("ProductListFragment", "Loading products for category: $category with sort: $sort")
        binding.productProgressBar.visibility = View.VISIBLE // Show progress bar during load
        viewModel.loadProducts(category, sort)
    }

    private fun getSortOrder(): String {
        // Return "price" for ascending and "-price" for descending
        return if (isAscending) "price" else "-price"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        Log.d("ProductListFragment", "onDestroyView called")
    }
}