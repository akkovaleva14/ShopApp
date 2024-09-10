package com.example.shopapp.ui

import android.os.Bundle
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.domain.viewmodelfactories.ProductViewModelFactory
import com.example.domain.repositories.ProductItemRepository
import com.example.domain.viewmodels.ProductViewModel
import com.example.shopapp.databinding.FragmentProductBinding
import com.example.shopapp.adapters.ProductImageAdapter
import com.example.network.RetrofitClient

class ProductFragment : Fragment() {

    private var _binding: FragmentProductBinding? = null
    private val binding get() = _binding!!
    private lateinit var productImageAdapter: ProductImageAdapter

    private val viewModel: ProductViewModel by activityViewModels {
        ProductViewModelFactory(ProductItemRepository(RetrofitClient.apiService))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Инициализируем адаптер для отображения изображений
        productImageAdapter = ProductImageAdapter(emptyList())
        binding.productImageRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = productImageAdapter
        }

        // Получаем ID продукта из аргументов
        val productId = arguments?.getString("productId") ?: return

        // Загрузка данных о продукте
        viewModel.loadProduct(productId)

        // Наблюдаем за изменениями в LiveData
        viewModel.product.observe(viewLifecycleOwner) { product ->
            product?.let {
                // Обновляем UI с помощью данных о продукте
                with(binding) {
                    productName.text = it.name
                    productPrice.text = "${it.price}₽"
                    productDiscountedPrice.text = it.discountedPrice?.let { price -> "${price}₽" } ?: ""
                    productDescription.text = it.description
                    productCategory.text = it.category.joinToString(", ")

                    // Зачёркивание старой цены
                    productPrice.paintFlags = productPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

                    // Обновляем адаптер с изображениями продукта
                    productImageAdapter = ProductImageAdapter(it.images)
                    productImageRecyclerView.adapter = productImageAdapter
                }
            }
        }

        // Обработка нажатия на кнопку назад
        binding.backButton.setOnClickListener {
            parentFragmentManager.setFragmentResult("REQUEST_KEY", Bundle().apply {
                putString("CATEGORY", arguments?.getString("CATEGORY"))
                putString("SORT_ORDER", arguments?.getString("SORT_ORDER"))
            })
            parentFragmentManager.popBackStack()
        }

        // Обработка нажатия на кнопку "Купить"
        binding.buyButton.setOnClickListener {
            Toast.makeText(requireContext(), "We are run out of this item. Sorry!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
