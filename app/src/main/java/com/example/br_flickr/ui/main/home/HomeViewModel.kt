package com.example.br_flickr.ui.main.home

import androidx.lifecycle.*
import com.example.br_flickr.repository.PhotoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val photoRepository: PhotoRepository
) : ViewModel() {

    private val _homeItems = MutableLiveData<List<HomeViewItem>?>()
    val homeItems: LiveData<List<HomeViewItem>?> = _homeItems

    private val _searchHistory = MutableLiveData<Set<HomeViewItem>?>()
    val searchHistory: LiveData<Set<HomeViewItem>?> = _searchHistory

    val searchTextFlow = MutableStateFlow("")

    private var searchTextChangedJob: Job? = null

    init {
        viewModelScope.launch {
            combine(
                photoRepository.searchQueries.asFlow(),
                searchTextFlow
            ) { searchHistory, query ->
                Pair(searchHistory, query)
            }.collect { (searchHistory, query) ->
                val viewItems = mutableListOf<HomeViewItem>()
                val searchViewItems = mutableListOf<HomeViewItem>()
                val regex = Regex(".*$query.*")
                searchViewItems.addAll(searchHistory.filter {
                    it.lowercase().matches(regex)
                }.map {
                    it.toSearchViewItem()
                })
                _searchHistory.value = searchViewItems.toSet()

                viewItems.add(HomeViewItem.EmptyState())
                viewItems.add(HomeViewItem.BookmarkCtaViewItem())
                _homeItems.value = viewItems.toList()
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
        }
    }
}