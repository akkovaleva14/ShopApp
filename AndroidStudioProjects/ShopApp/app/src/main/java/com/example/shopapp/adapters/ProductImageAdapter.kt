package com.example.shopapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.shopapp.R
import com.example.shopapp.databinding.ItemProductImageBinding

// Adapter for displaying a list of product images in a RecyclerView.
class ProductImageAdapter(private val images: List<String>) :
    RecyclerView.Adapter<ProductImageAdapter.ImageViewHolder>() {

    // Creates a new ViewHolder when there are no existing ViewHolders available for reuse.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        // Inflate the layout for individual image items and create a ViewHolder.
        val binding =
            ItemProductImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding)
    }

    // Binds the image data to the ViewHolder at the specified position.
    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        // Get the image URL for the current position.
        val imageUrl = images[position]

        // Use Glide to load the image into the ImageView in the ViewHolder.
        Glide.with(holder.itemView.context)
            .load(imageUrl) // Load image from URL.
            .placeholder(R.drawable.placeholder_image) // Show a placeholder while loading.
            .error(R.drawable.error_image) // Show an error image if loading fails.
            .into(holder.binding.productItemImage) // Set the loaded image into the ImageView.
    }

    // Returns the total number of images to be displayed.
    override fun getItemCount() = images.size

    // ViewHolder class responsible for holding and recycling views for each image item.
    inner class ImageViewHolder(val binding: ItemProductImageBinding) :
        RecyclerView.ViewHolder(binding.root)
}