package com.example.shopapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.shopapp.R
import com.example.shopapp.utils.NetworkUtils

class ErrorFragment : Fragment() {

    private var productId: String? = null
    private var category: String? = null
    private var sortOrder: String? = null
    private var pageNumber: Int = 1  // Default to the 1st page

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_error, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retrieve arguments passed to the fragment
        arguments?.let {
            productId = it.getString("productId")
            category = it.getString("CATEGORY")
            sortOrder = it.getString("SORT_ORDER")
            pageNumber = it.getInt("PAGE_NUMBER", 1) // Default to page 1 if not provided
        }

        // Initialize the "Try Again" button
        val tryAgainButton: Button = view.findViewById(R.id.tryAgainButton)

        // Set a click listener for the "Try Again" button
        tryAgainButton.setOnClickListener {
            attemptRetry()  // Attempt to retry loading the product
        }
    }

    // Method to attempt reloading the product when the user clicks "Try Again"
    private fun attemptRetry() {
        // Check if the internet connection is available
        if (NetworkUtils.isInternetAvailable(requireContext())) {
            productId?.let {
                // If productId is available, navigate to the product page with the same arguments
                val bundle = Bundle().apply {
                    putString("productId", it)
                    putString("CATEGORY", category)
                    putString("SORT_ORDER", sortOrder)
                    putInt("PAGE_NUMBER", pageNumber)
                }
                findNavController().navigate(R.id.productFragment, bundle)
            } ?: run {
                // If productId is null, show a failure message
                Toast.makeText(context, getString(R.string.failure_try_again), Toast.LENGTH_SHORT)
                    .show()
            }
        } else {
            // Show a failure message if no internet connection is available
            Toast.makeText(context, getString(R.string.failure_try_again), Toast.LENGTH_SHORT)
                .show()
        }
    }
}