package com.example.br_flickr.ui.main.details

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.br_flickr.di.CoroutinesDispatcherProvider
import com.example.br_flickr.repository.PhotoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class PhotoDetailsViewModel @Inject constructor(
    private val photoRepository: PhotoRepository,
    @ApplicationContext private val context: Context,
    private val coroutinesDispatcherProvider: CoroutinesDispatcherProvider
) : ViewModel() {

}