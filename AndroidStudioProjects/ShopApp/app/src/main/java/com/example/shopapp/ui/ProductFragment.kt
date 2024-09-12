package com.example.shopapp.ui

import android.os.Bundle
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.domain.viewmodelfactories.ProductViewModelFactory
import com.example.domain.repositories.ProductItemRepository
import com.example.domain.viewmodels.ProductViewModel
import com.example.shopapp.databinding.FragmentProductBinding
import com.example.shopapp.adapters.ProductImageAdapter
import com.example.network.RetrofitClient
import com.example.shopapp.R

class ProductFragment : Fragment() {

    private var _binding: FragmentProductBinding? = null
    private val binding get() = _binding!!
    private lateinit var productImageAdapter: ProductImageAdapter

    // ViewModel initialized with the factory and repository
    private val viewModel: ProductViewModel by activityViewModels {
        ProductViewModelFactory(ProductItemRepository(RetrofitClient.apiService))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize adapter for displaying product images
        productImageAdapter = ProductImageAdapter(emptyList())
        binding.productImageRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = productImageAdapter
        }

        // Get product ID from the arguments passed to this fragment
        val productId = arguments?.getString("productId") ?: return

        // Show ProgressBar and hide Buy button initially
        binding.productProgressBar.visibility = View.VISIBLE
        binding.buyButton.visibility = View.GONE

        // Load product data based on the product ID
        viewModel.loadProduct(productId)

        // Observe changes in product LiveData and update the UI accordingly
        viewModel.product.observe(viewLifecycleOwner) { product ->
            if (product != null) {
                // If the product data is successfully loaded, update the UI with product details
                with(binding) {
                    productProgressBar.visibility = View.GONE // Hide the ProgressBar
                    buyButton.visibility = View.VISIBLE // Show the Buy button

                    productName.text = product.name
                    productPrice.text = "${product.price}₽"
                    productDiscountedPrice.text =
                        product.discountedPrice?.let { price -> "${price}₽" }
                            ?: getString(R.string.no_discounted_price_available)
                    productDescription.text = product.description
                    productCategory.text = product.category.joinToString(", ")

                    // Strike through the original price
                    productPrice.paintFlags = productPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

                    // Update the image adapter with the product images
                    productImageAdapter = ProductImageAdapter(product.images)
                    productImageRecyclerView.adapter = productImageAdapter

                    // Set the visibility of views based on the presence of data
                    productName.visibility =
                        if (product.name.isNotEmpty()) View.VISIBLE else View.GONE
                    productPrice.visibility = if (product.price > 0) View.VISIBLE else View.GONE
                    productDiscountedPrice.visibility =
                        if (product.discountedPrice != null) View.VISIBLE else View.GONE
                    productDescription.visibility =
                        if (product.description?.isNotEmpty() == true) View.VISIBLE else View.GONE
                    productCategory.visibility =
                        if (product.category.isNotEmpty()) View.VISIBLE else View.GONE
                }
            } else {
                // If the product is null or there is an error, show an error message
                with(binding) {
                    productProgressBar.visibility = View.GONE // Hide the ProgressBar
                    buyButton.visibility = View.GONE // Hide the Buy button

                    errorTextView.visibility = View.VISIBLE
                    errorTextView.text = getString(R.string.product_not_found_or_an_error_occurred)

                    // Hide all the product detail views
                    productName.visibility = View.GONE
                    productPrice.visibility = View.GONE
                    productDiscountedPrice.visibility = View.GONE
                    productDescription.visibility = View.GONE
                    productCategory.visibility = View.GONE
                    productImageRecyclerView.visibility = View.GONE
                }
            }
        }

        // Set an OnClickListener for the back button to navigate back to the product list
        binding.backButton.setOnClickListener {
            val bundle = Bundle().apply {
                putString("CATEGORY", arguments?.getString("CATEGORY"))
                putString("SORT_ORDER", arguments?.getString("SORT_ORDER"))
                arguments?.getInt("PAGE_NUMBER", 1)?.let { it1 ->
                    putInt("PAGE_NUMBER", it1)
                }  // Return to the previous page number
            }
            findNavController().navigate(R.id.action_productFragment_to_productListFragment, bundle)
        }

        // Set an OnClickListener for the Buy button to show a Toast message
        binding.buyButton.setOnClickListener {
            Toast.makeText(
                requireContext(),
                getString(R.string.we_are_run_out_of_this_item_sorry),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}