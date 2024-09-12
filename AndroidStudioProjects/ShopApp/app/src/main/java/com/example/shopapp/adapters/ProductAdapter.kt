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

// Adapter for displaying products in a RecyclerView.
// Uses ListAdapter to manage a list of products and efficiently update changes.
class ProductAdapter(private val onItemClick: (Product) -> Unit) :
    ListAdapter<Product, ProductAdapter.ProductViewHolder>(ProductDiffCallback()) {

    // Creates a new ViewHolder when there are no existing ViewHolders available to reuse.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    // Binds the product data to the ViewHolder at the given position.
    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = getItem(position)
        holder.bind(product)
    }

    // ViewHolder class responsible for managing the individual product views in the RecyclerView.
    inner class ProductViewHolder(private val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // Binds product data to the UI components of the item view.
        fun bind(product: Product) {
            // Set the product name and price.
            binding.productName.text = product.name
            binding.productPrice.text =
                "${product.price} ${itemView.context.getString(R.string.currency_symbol)}"

            // If the product has a discounted price, display it and strike-through the original price.
            if (product.discountedPrice != null) {
                binding.productDiscountedPrice.text =
                    "${product.discountedPrice} ${itemView.context.getString(R.string.currency_symbol)}"
                binding.productDiscountedPrice.visibility = android.view.View.VISIBLE
                binding.productPrice.paintFlags =
                    binding.productPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                binding.productDiscountedPrice.visibility = android.view.View.GONE
            }

            // Load the product image using Glide. If no image is available, show a placeholder or error image.
            Glide.with(itemView.context)
                .load(product.images.firstOrNull()) // Load the first image if available.
                .placeholder(R.drawable.placeholder_image) // Placeholder while loading.
                .error(R.drawable.error_image) // Error image if loading fails.
                .into(binding.productImage)

            // Set an onClickListener to handle item clicks, passing the clicked product to the provided lambda.
            binding.root.setOnClickListener {
                onItemClick(product)
            }
        }
    }

    // DiffUtil callback to calculate the differences between old and new lists of products.
    class ProductDiffCallback : DiffUtil.ItemCallback<Product>() {
        // Checks if two products are the same based on their unique IDs.
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.id == newItem.id
        }

        // Checks if the contents of two products are the same.
        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }
}