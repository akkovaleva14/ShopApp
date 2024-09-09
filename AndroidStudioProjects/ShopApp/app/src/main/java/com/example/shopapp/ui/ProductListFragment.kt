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
    private var isAscending = true // Track the sorting order
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
            Log.d("ProductListFragment", "Products loaded successfully")
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
            loadProducts(selectedCategory, if (isAscending) "asc" else "desc")
        }

        // Handle filter toggle button
        binding.filterToggleButton.setOnClickListener {
            isAscending = !isAscending
            binding.filterToggleButton.setImageResource(
                if (isAscending) R.drawable.ic_down else R.drawable.ic_up
            )
            loadProducts(selectedCategory, if (isAscending) "asc" else "desc")
        }

        // Handle category selection (hardcoded categories)
        setCategoryListeners()

        // Load initial data with default category and ascending price order
        loadProducts(selectedCategory, "asc")
    }

    private fun setCategoryListeners() {
        val categories = mapOf(
            binding.categoryComputers to "computers",
            binding.categoryBags to "bags",
            binding.categoryClothing to "clothing"
        )

        for ((view, category) in categories) {
            view.setOnClickListener {
                selectedCategory = category
                loadProducts(selectedCategory, if (isAscending) "asc" else "desc")
                Log.d("ProductListFragment", "Selected category: $category")
            }
        }
    }

    private fun loadProducts(category: String?, sort: String?) {
        binding.productProgressBar.visibility = View.VISIBLE // Show progress bar during load
        viewModel.loadProducts(category, sort)
        Log.d("ProductListFragment", "Loading products for category: $category with sort: $sort")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}