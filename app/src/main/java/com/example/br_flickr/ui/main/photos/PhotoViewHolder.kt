package com.example.br_flickr.ui.main.photos

import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import coil.load
import com.example.br_flickr.databinding.EmptyViewItemBinding
import com.example.br_flickr.databinding.PhotoViewItemBinding

internal sealed class PhotoViewHolder(bindings: ViewBinding) :
    RecyclerView.ViewHolder(bindings.root) {

    class PhotoListViewHolder(
        private val bindings: PhotoViewItemBinding,
    ) : PhotoViewHolder(bindings) {
        fun bind(
            item: PhotoViewItem.PhotoListItem,
            onClick: (String, String) -> Unit
        ) {
            val photo = item.photo
            val photoUrl =
                "https://live.staticflickr.com/${photo.server}/${photo.id}_${photo.secret}.jpg"
            bindings.photo.load(photoUrl)
            bindings.title.text = photo.title
            bindings.root.setOnClickListener {
                onClick(photoUrl, photo.title)
            }
        }
    }

    class SavedPhotoListViewHolder(
        private val bindings: PhotoViewItemBinding,
    ) : PhotoViewHolder(bindings) {
        fun bind(
            item: PhotoViewItem.SavedPhotoListItem
        ) {
            bindings.photo.load(item.bitmap)
        }
    }

    class EmptyViewHolder(
        bindings: EmptyViewItemBinding,
    ) : PhotoViewHolder(bindings)
}
