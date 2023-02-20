package com.example.br_flickr.ui.main.photos

import com.example.models.Photo
import com.example.br_flickr.R
import com.example.br_flickr.ui.main.util.ViewItem

sealed class PhotoViewItem(
    open val id: String,
    open val type: PhotoViewItemType
) : ViewItem<PhotoViewItem> {

    override fun compareTo(other: PhotoViewItem): Int = this.id.compareTo(other.id)

    override fun areContentsTheSame(other: PhotoViewItem): Boolean = this == other

    override fun areItemsTheSame(other: PhotoViewItem): Boolean =
        type == other.type && id == other.id

    data class PhotoListItem(
        override val id: String,
        val photo: Photo
    ) : PhotoViewItem(
        id = id,
        type = PhotoViewItemType.PHOTO_LIST_ITEM,
    )

    data class SearchHistoryItem(
        override val id: String,
        val query: String
    ) : PhotoViewItem(
        id = id,
        type = PhotoViewItemType.SEARCH_HISTORY_ITEM
    )

    data class EmptyState(
        override val id: String = PhotoViewItemType.EMPTY.toString(),
    ) : PhotoViewItem(
        id = id,
        type = PhotoViewItemType.EMPTY
    )
}

enum class PhotoViewItemType(
    val layoutId: Int,
) {
    PHOTO_LIST_ITEM(R.layout.photo_view_item),
    SEARCH_HISTORY_ITEM(R.layout.search_history_view_item),
    EMPTY(R.layout.empty_view_item)
}

fun String.toSearchViewItem(): PhotoViewItem.SearchHistoryItem {
    return PhotoViewItem.SearchHistoryItem(id = this.hashCode().toString(), query = this)
}
