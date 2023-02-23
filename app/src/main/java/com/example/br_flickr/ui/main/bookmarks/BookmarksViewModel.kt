package com.example.br_flickr.ui.main.bookmarks

import android.content.Context
import android.graphics.BitmapFactory
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.br_flickr.ui.main.photos.PhotoViewItem
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import java.io.File
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
                val files = getAllImagesFromFolder("images")
                files.map {
                    val bitmap = BitmapFactory.decodeFile(it.absolutePath)
                    bitmap?.let { bm ->
                        viewItems.add(
                            PhotoViewItem.SavedPhotoListItem(
                                id = it.hashCode().toString(),
                                bitmap = bm
                            )
                        )
                    }
                }
                _photos.value = viewItems

            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
        }
    }

    private fun getAllImagesFromFolder(folderName: String): List<File> {
        val images = mutableListOf<File>()
        // Get the directory for the app's private pictures directory.
        val directory = context.getDir(folderName, Context.MODE_PRIVATE)
        // Get a list of all the files in the directory.
        val files = directory?.listFiles()
        // Loop through the files and add any image files to the list.
        for (file in files!!) {
            if (file.isFile) {
                images.add(file)
            }
        }
        return images
    }
}