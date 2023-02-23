package com.example.br_flickr.ui.main.details

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import javax.inject.Inject

@HiltViewModel
class PhotoDetailsViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    fun saveImageToInternalStorage(url: URL, fileName: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val bitmapImage = BitmapFactory.decodeStream(
                    url.openConnection().getInputStream()
                )
                // Get the directory for the app's private pictures directory.
                val directory = context?.getDir("images", Context.MODE_PRIVATE)
                // Create a file for the saved image.
                val imageFile = File(directory, fileName)

                var outputStream: FileOutputStream? = null
                try {
                    // Open an output stream to write the image data to the file.
                    outputStream = FileOutputStream(imageFile)

                    withContext(Dispatchers.IO) {
                        // Compress the bitmap image and write it to the output stream.
                        bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    }

                    outputStream.flush()
                    outputStream.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}