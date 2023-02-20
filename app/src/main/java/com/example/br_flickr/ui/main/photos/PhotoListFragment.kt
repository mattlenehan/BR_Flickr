package com.example.br_flickr.ui.main.photos

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.br_flickr.R
import com.example.br_flickr.databinding.EmptyViewItemBinding
import com.example.br_flickr.databinding.FragmentPhotoListBinding
import com.example.br_flickr.databinding.PhotoViewItemBinding
import com.example.br_flickr.databinding.SearchHistoryViewItemBinding
import com.example.br_flickr.ui.main.MainViewModel
import com.example.br_flickr.ui.main.util.showSnackbar
import com.example.networking.util.ApiResult
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PhotoListFragment : Fragment() {

    private val viewModel: MainViewModel
            by navGraphViewModels(R.id.nav_graph) {
                defaultViewModelProviderFactory
            }

    private val photosAdapter by lazy {
        ListAdapter(listener = adapterListener)
    }

    private val searchResultsAdapter by lazy {
        ListAdapter(listener = adapterListener)
    }

    private var _binding: FragmentPhotoListBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
        subscribeUi()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPhotoListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun init() {
        binding.recycler.apply {
            adapter = photosAdapter
            layoutManager = GridLayoutManager(binding.root.context, 2).apply {
                spanSizeLookup = photosAdapter.spanSizeLookup
            }
        }

        binding.searchResultsRecycler.apply {
            adapter = searchResultsAdapter
        }

        binding.searchView.editText.setOnEditorActionListener { _, _, _ ->
            binding.searchBar.text = binding.searchView.text
            binding.searchView.hide()
            false
        }
        binding.searchView.editText.addTextChangedListener {
            binding.searchBar.text = it.toString()
            viewModel.onSearchTextChanged(text = it.toString())
        }
        binding.searchView.editText.setOnKeyListener { _, i, keyEvent ->
            if (keyEvent.action != KeyEvent.ACTION_DOWN) {
                false
            } else {
                if (i == KeyEvent.KEYCODE_ENTER) {
                    viewModel.onSearchSubmitted(binding.searchView.text.toString())
                    true
                } else {
                    false
                }
            }
        }
    }

    private fun subscribeUi() {
        viewModel.photos.observe(
            viewLifecycleOwner
        ) {
            when (it.status) {
                ApiResult.Status.SUCCESS -> {
                    binding.recycler.visibility = View.VISIBLE
                    binding.loading.visibility = View.GONE
                    photosAdapter.accept(it.data ?: emptyList())
                }
                ApiResult.Status.ERROR -> {
                    binding.recycler.visibility = View.VISIBLE
                    binding.loading.visibility = View.GONE
                    view?.showSnackbar(it.error?.statusMessage ?: it.message)
                }
                ApiResult.Status.LOADING -> {
                    binding.recycler.visibility = View.GONE
                    binding.loading.visibility = View.VISIBLE
                }
            }
        }

        viewModel.searchTextFlow.asLiveData().observe(
            viewLifecycleOwner
        ) {
            binding.searchBar.text =
                it ?: binding.root.context.getString(R.string.search_photos)
        }

        viewModel.searchHistory.observe(
            viewLifecycleOwner
        ) {
            binding.searchResultsRecycler.visibility = View.VISIBLE
            binding.loading.visibility = View.GONE
            searchResultsAdapter.accept(it?.toList() ?: emptyList()) {
                binding.searchResultsRecycler.scrollToPosition(0)
            }
        }
    }

    private val adapterListener = object : AdapterListener {
        override fun onPhotoClick(url: String, title: String) {
            val direction = PhotoListFragmentDirections.openPhotoDetails(url, title)
            findNavController().navigate(direction)
        }

        override fun onSearchItemSelected(query: String) {
            viewModel.onSearchSubmitted(query)
            binding.searchBar.text = query
            binding.searchView.hide()
        }
    }
}

private interface AdapterListener {
    fun onPhotoClick(url: String, title: String)
    fun onSearchItemSelected(query: String)
}

private class ListAdapter(private val listener: AdapterListener) :
    RecyclerView.Adapter<PhotoViewHolder>() {

    private val diffCallback = object : DiffUtil.ItemCallback<PhotoViewItem>() {
        override fun areItemsTheSame(
            oldItem: PhotoViewItem,
            newItem: PhotoViewItem
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: PhotoViewItem,
            newItem: PhotoViewItem
        ): Boolean {
            return oldItem.id == newItem.id
        }
    }

    val differ = AsyncListDiffer(this, diffCallback)

    fun accept(newItems: List<PhotoViewItem>, commitCallback: Runnable? = null) {
        differ.submitList(newItems, commitCallback)
    }

    override fun getItemViewType(position: Int) =
        differ.currentList[position].type.ordinal

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (val itemType = PhotoViewItemType.values()[viewType]) {
            PhotoViewItemType.PHOTO_LIST_ITEM -> {
                val bindings: PhotoViewItemBinding =
                    DataBindingUtil.inflate(
                        inflater, itemType.layoutId, parent, false
                    )
                PhotoViewHolder.PhotoListViewHolder(bindings)
            }
            PhotoViewItemType.SEARCH_HISTORY_ITEM -> {
                val bindings: SearchHistoryViewItemBinding =
                    DataBindingUtil.inflate(
                        inflater, itemType.layoutId, parent, false
                    )
                PhotoViewHolder.SearchHistoryViewHolder(bindings)
            }
            PhotoViewItemType.EMPTY -> {
                val bindings: EmptyViewItemBinding =
                    DataBindingUtil.inflate(
                        inflater, itemType.layoutId, parent, false
                    )
                PhotoViewHolder.EmptyViewHolder(bindings)
            }
        }
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val item = differ.currentList[position]
        when (holder) {
            is PhotoViewHolder.PhotoListViewHolder -> holder.bind(
                item as PhotoViewItem.PhotoListItem,
                onClick = { url, title -> listener.onPhotoClick(url, title) }
            )
            is PhotoViewHolder.SearchHistoryViewHolder -> holder.bind(
                item as PhotoViewItem.SearchHistoryItem,
                onSearchItemSelected = { query -> listener.onSearchItemSelected(query) }
            )
            is PhotoViewHolder.EmptyViewHolder -> {}
        }
    }

    override fun getItemCount() = differ.currentList.size

    val spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
        override fun getSpanSize(position: Int): Int {
            return when (differ.currentList[position]) {
                is PhotoViewItem.PhotoListItem -> 1
                is PhotoViewItem.SearchHistoryItem -> 1
                is PhotoViewItem.EmptyState -> 2
            }
        }
    }
}