package com.example.shopapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.network.Product
import com.example.shopapp.databinding.ItemProductBinding

class ProductAdapter : ListAdapter<Product, ProductAdapter.ProductViewHolder>(ProductDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = getItem(position)
        holder.bind(product)
    }

    class ProductViewHolder(private val binding: ItemProductBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(product: Product) {
            binding.productName.text = product.name
            binding.productPrice.text = "${product.price} ${itemView.context.getString(R.string.currency_symbol)}"

            // Проверка наличия скидочной цены
            if (product.discountedPrice != null) {
                binding.productDiscountedPrice.text = "${product.discountedPrice} ${itemView.context.getString(R.string.currency_symbol)}"
                binding.productDiscountedPrice.visibility = android.view.View.VISIBLE
            } else {
                binding.productDiscountedPrice.visibility = android.view.View.GONE
            }

            // Использование Glide для загрузки изображения
            Glide.with(itemView.context)
                .load(product.images.firstOrNull()) // Загружаем первое изображение из списка
                .placeholder(R.drawable.placeholder_image) // Добавляем placeholder, пока изображение загружается
                .error(R.drawable.error_image) // Изображение ошибки, если не удалось загрузить
                .into(binding.productImage) // Назначаем ImageView для вывода изображения
        }
    }

    class ProductDiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }
}