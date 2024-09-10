package com.example.shopapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.shopapp.R
import com.example.shopapp.databinding.ItemProductImageBinding

class ProductImageAdapter(private val images: List<String>) :
    RecyclerView.Adapter<ProductImageAdapter.ImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ItemProductImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageUrl = images[position]
        // Load image using an image loading library like Glide or Picasso
        // Example with Glide:
        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .placeholder(R.drawable.placeholder_image) // Use your placeholder image
            .into(holder.binding.productItemImage)
    }

    override fun getItemCount() = images.size

    inner class ImageViewHolder(val binding: ItemProductImageBinding) : RecyclerView.ViewHolder(binding.root)
}
