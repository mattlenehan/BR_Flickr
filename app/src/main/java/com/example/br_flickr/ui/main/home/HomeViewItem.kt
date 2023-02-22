package com.example.br_flickr.ui.main.home

import com.example.br_flickr.R
import com.example.br_flickr.ui.main.util.ViewItem

sealed class HomeViewItem(
    open val id: String,
    open val type: HomeViewItemType
) : ViewItem<HomeViewItem> {

    override fun compareTo(other: HomeViewItem): Int = this.id.compareTo(other.id)

    override fun areContentsTheSame(other: HomeViewItem): Boolean = this == other

    override fun areItemsTheSame(other: HomeViewItem): Boolean =
        type == other.type && id == other.id

    data class SearchHistoryItem(
        override val id: String,
        val query: String
    ) : HomeViewItem(
        id = id,
        type = HomeViewItemType.SEARCH_HISTORY_ITEM
    )

    data class EmptyState(
        override val id: String = HomeViewItemType.EMPTY.toString(),
    ) : HomeViewItem(
        id = id,
        type = HomeViewItemType.EMPTY
    )

    data class BookmarkCtaViewItem(
        override val id: String = HomeViewItemType.BOOKMARK_CTA.toString(),
    ) : HomeViewItem(
        id = id,
        type = HomeViewItemType.BOOKMARK_CTA
    )
}

enum class HomeViewItemType(
    val layoutId: Int,
) {
    SEARCH_HISTORY_ITEM(R.layout.search_history_view_item),
    EMPTY(R.layout.empty_view_item),
    BOOKMARK_CTA(R.layout.cta_saved_view_item)
}

fun String.toSearchViewItem(): HomeViewItem.SearchHistoryItem {
    return HomeViewItem.SearchHistoryItem(id = this.hashCode().toString(), query = this)
}
