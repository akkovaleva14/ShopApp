package com.example.shopapp.adapters

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.network.Product
import com.example.shopapp.R
import com.example.shopapp.databinding.ItemProductBinding

class ProductAdapter(private val onItemClick: (Product) -> Unit) :
    ListAdapter<Product, ProductAdapter.ProductViewHolder>(ProductDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = getItem(position)
        holder.bind(product)
    }

    inner class ProductViewHolder(private val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(product: Product) {
            binding.productName.text = product.name
            binding.productPrice.text =
                "${product.price} ${itemView.context.getString(R.string.currency_symbol)}"

            if (product.discountedPrice != null) {
                binding.productDiscountedPrice.text =
                    "${product.discountedPrice} ${itemView.context.getString(R.string.currency_symbol)}"
                binding.productDiscountedPrice.visibility = android.view.View.VISIBLE
                binding.productPrice.paintFlags =
                    binding.productPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                binding.productDiscountedPrice.visibility = android.view.View.GONE
            }

            Glide.with(itemView.context)
                .load(product.images.firstOrNull())
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .into(binding.productImage)

            binding.root.setOnClickListener {
                onItemClick(product)
            }
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
