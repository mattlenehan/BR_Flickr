package com.example.br_flickr.ui.main.util

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

abstract class PaginationScrollListener(var layoutManager: GridLayoutManager? = null) :
    RecyclerView.OnScrollListener() {

    abstract var isCurrentlyLoading: Boolean

    abstract fun loadMoreItems()

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        layoutManager?.let {
            val totalItemCount = it.itemCount
            val lastVisibleItem = it.findLastCompletelyVisibleItemPosition()

            if (!isCurrentlyLoading && lastVisibleItem >= totalItemCount - 2) {
                loadMoreItems()
            }
        }
    }
}
