package com.example.shopapp.ui

import android.os.Bundle
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

        viewModel.products.observe(viewLifecycleOwner, Observer { products ->
            // Update RecyclerView with the new list of products
            productAdapter.submitList(products)
            binding.errorTextView.visibility = View.GONE
            binding.retryButton.visibility = View.GONE
        })

        viewModel.error.observe(viewLifecycleOwner, Observer { error ->
            // Show error and retry button
            binding.errorTextView.text = error
            binding.errorTextView.visibility = View.VISIBLE
            binding.retryButton.visibility = View.VISIBLE
        })

        binding.retryButton.setOnClickListener {
            // Retry loading products
            viewModel.loadProducts(null, null)
        }

        // Load data when the fragment is created
        viewModel.loadProducts(null, null)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
