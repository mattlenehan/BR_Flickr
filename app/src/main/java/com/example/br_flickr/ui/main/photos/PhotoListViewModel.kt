package com.example.br_flickr.ui.main.photos

import androidx.lifecycle.*
import com.example.br_flickr.repository.PhotoRepository
import com.example.br_flickr.ui.main.util.Event
import com.example.networking.util.ApiResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PhotoListViewModel @Inject constructor(
    private val photoRepository: PhotoRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _photos = MutableLiveData<Event<ApiResult<List<PhotoViewItem>?>>>()
    val photos: LiveData<Event<ApiResult<List<PhotoViewItem>?>>> = _photos

    private val query: String =
        (savedStateHandle["query"] ?: "")

    init {
        viewModelScope.launch {
            photoRepository.photosFlow.collect { photosResult ->
                val viewItems =
                    if (photos.value?.peekContent()?.data?.isEmpty() == true) {
                        mutableListOf()
                    } else {
                        photos.value?.peekContent()?.data?.toMutableList()
                            ?: mutableListOf()
                    }
                viewItems.removeIf {
                    it.type == PhotoViewItemType.EMPTY
                }
                when (photosResult?.status) {
                    ApiResult.Status.SUCCESS -> {
                        val photos = photosResult.data?.get(query)?.photoPage?.photos ?: emptyList()
                        photos.forEach {
                            viewItems.add(
                                PhotoViewItem.PhotoListItem(
                                    id = it.id,
                                    photo = it
                                )
                            )
                        }
                        _photos.value = Event(ApiResult.success(viewItems))
                    }
                    ApiResult.Status.ERROR -> {
                        _photos.value = Event(
                            ApiResult.error(
                                photosResult.message ?: "Unable to fetch photos",
                                photosResult.error
                            )
                        )
                    }
                    ApiResult.Status.LOADING -> {
                        _photos.value = Event(ApiResult.loading())
                    }
                    null -> {}
                }
            }
        }
    }

    fun fetchPhotos(query: String, page: Int) {
        viewModelScope.launch {
            photoRepository.getPhotos(query = query, page = page).collect()
        }
    }
}