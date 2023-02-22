package com.example.br_flickr.ui.main.bookmarks

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.br_flickr.ui.main.photos.PhotoViewItem
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import java.io.FileNotFoundException
import javax.inject.Inject


@HiltViewModel
class BookmarksViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _photos = MutableLiveData<List<PhotoViewItem>?>()
    val photos: LiveData<List<PhotoViewItem>?> = _photos

    init {
        viewModelScope.launch {

        }
    }

    fun fetchLocalPhotos() {
        viewModelScope.launch {
            try {
                val viewItems = mutableListOf<PhotoViewItem>()
                _photos.value = viewItems

            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
        }
    }
}