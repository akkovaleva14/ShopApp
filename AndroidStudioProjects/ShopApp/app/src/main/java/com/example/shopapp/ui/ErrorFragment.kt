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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_error, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Получаем переданные данные (productId, category, sortOrder)
        arguments?.let {
            productId = it.getString("productId")
            category = it.getString("CATEGORY")
            sortOrder = it.getString("SORT_ORDER")
        }

        val tryAgainButton: Button = view.findViewById(R.id.tryAgainButton)

        tryAgainButton.setOnClickListener {
            attemptRetry()
        }
    }

    private fun attemptRetry() {
        if (NetworkUtils.isInternetAvailable(requireContext())) {
            // Если интернет доступен, повторяем запрос и переходим на ProductFragment
            productId?.let {
                val bundle = Bundle().apply {
                    putString("productId", it)
                }
                findNavController().navigate(R.id.productFragment, bundle)
            } ?: run {
                Toast.makeText(context, "Не получилось! Попробуйте еще раз", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Если интернет недоступен, показываем тост с ошибкой
            Toast.makeText(context, "Не получилось! Попробуйте еще раз", Toast.LENGTH_SHORT).show()
        }
    }
}
