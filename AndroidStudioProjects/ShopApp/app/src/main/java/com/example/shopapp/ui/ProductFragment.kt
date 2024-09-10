package com.example.shopapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.network.Product
import com.example.network.ProductDetailsResponse
import com.example.shopapp.databinding.FragmentProductBinding
import com.example.shopapp.adapters.ProductImageAdapter

class ProductFragment : Fragment() {

    private var _binding: FragmentProductBinding? = null
    private val binding get() = _binding!!
    private lateinit var productImageAdapter: ProductImageAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val images = listOf(
            // Example image URLs; replace with actual data
            "https://example.com/image1.jpg",
            "https://example.com/image2.jpg"
        )

        productImageAdapter = ProductImageAdapter(images)
        binding.productImageRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = productImageAdapter
        }

        // Load product details and update UI
        loadProductDetails()
    }

    private fun loadProductDetails() {
        // Example function to load product details and set UI data
        // Replace with actual logic to fetch and bind product details
        val product = ProductDetailsResponse(
            status = "success",
            data = Product(
                id = "productId",
                name = "Sample Product",
                price = 1000.0,
                discountedPrice = 800.0,
                images = listOf("https://example.com/image1.jpg", "https://example.com/image2.jpg"),
                description = "Product description",
                productRating = 4.5,
                brand = "Brand",
                productSpecifications = null,
                category = emptyList()
            )
        )

        with(binding) {
            productName.text = product.data!!.name
            productPrice.text = "${product.data!!.price}₽"
            productDiscountedPrice.text = "${product.data!!.discountedPrice}₽"
            productDescription.text = product.data!!.description
            productCategory.text = product.data!!.category.joinToString(", ")

            // Update adapter with product images
            productImageAdapter = ProductImageAdapter(product.data!!.images)
            productImageRecyclerView.adapter = productImageAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}