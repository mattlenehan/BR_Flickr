package com.example.br_flickr.ui.main

import androidx.lifecycle.*
import com.example.br_flickr.repository.PhotoRepository
import com.example.br_flickr.ui.main.photos.PhotoViewItem
import com.example.br_flickr.ui.main.photos.PhotoViewItemType
import com.example.br_flickr.ui.main.photos.toSearchViewItem
import com.example.networking.util.ApiResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val photoRepository: PhotoRepository
) : ViewModel() {

    private val _photos = MutableLiveData<ApiResult<List<PhotoViewItem>?>>()
    val photos: LiveData<ApiResult<List<PhotoViewItem>?>> = _photos

    private val _searchHistory = MutableLiveData<Set<PhotoViewItem>?>()
    val searchHistory: LiveData<Set<PhotoViewItem>?> = _searchHistory

    val searchTextFlow = MutableStateFlow("")

    private var searchTextChangedJob: Job? = null

    init {
        viewModelScope.launch {
            photoRepository.getPhotos(query = searchTextFlow.value, page = 0).collect()
            combine(
                photoRepository.photosFlow,
                photoRepository.searchQueries.asFlow(),
                searchTextFlow
            ) { photosResult, searchHistory, query ->
                Triple(photosResult, searchHistory, query)
            }.collect { (photosResult, searchHistory, query) ->
                val viewItems = mutableListOf<PhotoViewItem>()
                viewItems.removeIf {
                    it.type == PhotoViewItemType.EMPTY
                }
                when (photosResult?.status) {
                    ApiResult.Status.SUCCESS -> {
                        val regex = Regex(".*$query.*")
                        val photos = photosResult.data?.photoPage?.photos ?: emptyList()
                        photos.forEach {
                            viewItems.add(
                                PhotoViewItem.PhotoListItem(
                                    id = it.id,
                                    photo = it
                                )
                            )
                        }
                        if (viewItems.isEmpty()) {
                            viewItems.add(PhotoViewItem.EmptyState())
                        }
                        _photos.value = ApiResult.success(viewItems)

                        val searchViewItems = mutableListOf<PhotoViewItem>()
                        searchViewItems.addAll(searchHistory.filter {
                            it.lowercase().matches(regex)
                        }.map {
                            it.toSearchViewItem()
                        })
                        _searchHistory.value = searchViewItems.toSet()
                    }
                    ApiResult.Status.ERROR -> {
                        _photos.value = ApiResult.error(
                            photosResult.message ?: "Unable to fetch photos",
                            photosResult.error
                        )
                    }
                    ApiResult.Status.LOADING -> {
                        _photos.value = ApiResult.loading()
                    }
                    null -> {}
                }
            }
        }
    }

    fun onSearchTextChanged(text: String = "") {
        searchTextChangedJob?.cancel()
        searchTextChangedJob = viewModelScope.launch {
            searchTextFlow.value = text
        }
    }

    fun onSearchSubmitted(query: String) {
        viewModelScope.launch {
            if (query.isNotEmpty()) {
                photoRepository.insertSearchQuery(query)
            }
            photoRepository.getPhotos(query, 0).collect {}
        }
    }
}