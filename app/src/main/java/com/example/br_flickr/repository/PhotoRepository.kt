package com.example.br_flickr.repository

import androidx.lifecycle.LiveData
import com.example.models.PhotosResponse
import com.example.networking.util.ApiResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface PhotoRepository {

    val photosFlow: StateFlow<ApiResult<PhotosResponse>?>
    val searchQueries: LiveData<List<String>>

    suspend fun getPhotos(query: String, page: Int): Flow<ApiResult<PhotosResponse>?>
    suspend fun insertSearchQuery(query: String)
}