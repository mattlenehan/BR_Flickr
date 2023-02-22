package com.example.br_flickr.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.br_flickr.database.DatabaseSearchHistoryItem
import com.example.br_flickr.database.SearchHistoryDatabase
import com.example.br_flickr.database.asDomainModel
import com.example.br_flickr.ui.main.photos.PhotoViewItem
import com.example.models.PhotosResponse
import com.example.networking.util.ApiResult
import com.example.networking.webservices.PhotosWebservice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import timber.log.Timber

class PhotoRepositoryImpl(
    private val photosWebservice: PhotosWebservice,
    private val database: SearchHistoryDatabase,
    retrofit: Retrofit,
) : BaseRepository(retrofit), PhotoRepository {

    private val _photosFlow: MutableStateFlow<ApiResult<Map<String, PhotosResponse>>?> =
        MutableStateFlow(null)
    override val photosFlow: StateFlow<ApiResult<Map<String, PhotosResponse>>?> = _photosFlow

    private val _searchQueries: LiveData<List<String>> =
        Transformations.map(database.searchesDao.getSearchQueries()) {
            it.asDomainModel()
        }
    override val searchQueries: LiveData<List<String>> = _searchQueries

    override suspend fun getPhotos(query: String, page: Int): Flow<ApiResult<PhotosResponse>> {
        return flow {
            emit(ApiResult.loading())
            val result = getResponse(
                request = {
                    photosWebservice.getPhotos(
                        text = query,
                        page = page
                    )
                },
                defaultErrorMessage = "Error fetching photos"
            )
            if (photosFlow.value?.data?.containsKey(query) == true) {
                val map = photosFlow.value?.data?.toMutableMap() ?: mutableMapOf()
                result.data?.let {
                    map.put(query, it)
                }
                _photosFlow.value = ApiResult(
                    result.status,
                    map,
                    result.error,
                    result.message ?: result.error?.statusMessage ?: "Unable to fetch photos"
                )
            } else {
                val map = photosFlow.value?.data?.toMutableMap() ?: mutableMapOf()
                result.data?.let {
                    map.put(query, it)
                }
                _photosFlow.value = ApiResult(
                    result.status,
                    map,
                    result.error,
                    result.message ?: result.error?.statusMessage ?: "Unable to fetch photos"
                )
            }

            emit(result)
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun insertSearchQuery(query: String) {
        withContext(Dispatchers.Default) {
            try {
                database.searchesDao.insertSearchQuery(
                    DatabaseSearchHistoryItem(
                        id = query.hashCode(),
                        query = query
                    )
                )
            } catch (e: Exception) {
                Timber.w(e)
            }
        }
    }
}