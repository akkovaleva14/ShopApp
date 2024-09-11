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
    private var pageNumber: Int = 1  // По умолчанию 1-я страница

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_error, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            productId = it.getString("productId")
            category = it.getString("CATEGORY")
            sortOrder = it.getString("SORT_ORDER")
            pageNumber = it.getInt("PAGE_NUMBER", 1)
        }

        val tryAgainButton: Button = view.findViewById(R.id.tryAgainButton)

        tryAgainButton.setOnClickListener {
            attemptRetry()
        }
    }

    private fun attemptRetry() {
        if (NetworkUtils.isInternetAvailable(requireContext())) {
            productId?.let {
                val bundle = Bundle().apply {
                    putString("productId", it)
                    putString("CATEGORY", category)
                    putString("SORT_ORDER", sortOrder)
                    putInt("PAGE_NUMBER", pageNumber)
                }
                findNavController().navigate(R.id.productFragment, bundle)
            } ?: run {
                Toast.makeText(context, "Не получилось! Попробуйте еще раз", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Не получилось! Попробуйте еще раз", Toast.LENGTH_SHORT).show()
        }
    }
}