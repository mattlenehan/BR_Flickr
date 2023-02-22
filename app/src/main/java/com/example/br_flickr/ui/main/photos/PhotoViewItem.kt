package com.example.br_flickr.ui.main.photos

import android.graphics.Bitmap
import com.example.br_flickr.R
import com.example.br_flickr.ui.main.util.ViewItem
import com.example.models.Photo

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

    data class SavedPhotoListItem(
        override val id: String,
        val bitmap: Bitmap
    ) : PhotoViewItem(
        id = id,
        type = PhotoViewItemType.SAVED_PHOTO_LIST_ITEM,
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
    SAVED_PHOTO_LIST_ITEM(R.layout.photo_view_item),
    EMPTY(R.layout.empty_view_item)
}

//fun String.toPhotoViewItem(): PhotoViewItem.SavedPhotoListItem {
//    return PhotoViewItem.SavedPhotoListItem(id = this.hashCode().toString(), bitmap = this)
//}

fun Bitmap.toPhotoViewItem(): PhotoViewItem.SavedPhotoListItem {
    return PhotoViewItem.SavedPhotoListItem(id = this.hashCode().toString(), bitmap = this)
}
