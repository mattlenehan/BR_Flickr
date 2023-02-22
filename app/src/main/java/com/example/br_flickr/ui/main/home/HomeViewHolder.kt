package com.example.br_flickr.ui.main.home

import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.br_flickr.databinding.CtaSavedViewItemBinding
import com.example.br_flickr.databinding.EmptyViewItemBinding
import com.example.br_flickr.databinding.SearchHistoryViewItemBinding
import com.example.br_flickr.ui.main.photos.PhotoViewItem

internal sealed class HomeViewHolder(bindings: ViewBinding) :
    RecyclerView.ViewHolder(bindings.root) {

    class SearchHistoryViewHolder(
        private val bindings: SearchHistoryViewItemBinding,
    ) : HomeViewHolder(bindings) {
        fun bind(
            item: HomeViewItem.SearchHistoryItem,
            onSearchItemSelected: (String) -> Unit
        ) {
            bindings.searchHistoryText.text = item.query
            bindings.root.setOnClickListener {
                onSearchItemSelected(item.query)
            }
        }
    }

    class BookmarkCtaViewHolder(
        private val bindings: CtaSavedViewItemBinding,
    ) : HomeViewHolder(bindings) {
        fun bind(
            item: HomeViewItem.BookmarkCtaViewItem,
            onSavedCtaPressed: () -> Unit
        ) {
            bindings.cta.setOnClickListener {
                onSavedCtaPressed()
            }
        }
    }

    class EmptyViewHolder(
        bindings: EmptyViewItemBinding,
    ) : HomeViewHolder(bindings)
}
