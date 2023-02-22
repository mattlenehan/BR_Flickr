package com.example.br_flickr.ui.main.home

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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.br_flickr.R
import com.example.br_flickr.databinding.CtaSavedViewItemBinding
import com.example.br_flickr.databinding.EmptyViewItemBinding
import com.example.br_flickr.databinding.FragmentHomeBinding
import com.example.br_flickr.databinding.SearchHistoryViewItemBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private val viewModel: HomeViewModel
            by navGraphViewModels(R.id.nav_graph) {
                defaultViewModelProviderFactory
            }

    private val homeAdapter by lazy {
        ListAdapter(listener = adapterListener)
    }

    private val searchResultsAdapter by lazy {
        ListAdapter(listener = adapterListener)
    }

    private var _binding: FragmentHomeBinding? = null
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
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun init() {
        binding.recycler.apply {
            adapter = homeAdapter
            layoutManager = LinearLayoutManager(binding.root.context)
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
                    val query = binding.searchView.text.toString()
                    viewModel.onSearchSubmitted(query)
                    val direction = HomeFragmentDirections.openPhotoList(query)
                    findNavController().navigate(direction)
                    true
                } else {
                    false
                }
            }
        }
    }

    private fun subscribeUi() {
        viewModel.homeItems.observe(
            viewLifecycleOwner
        ) {
            homeAdapter.accept(it ?: emptyList())
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
        override fun onSavedCtaPressed() {
            val direction = HomeFragmentDirections.openBookmarks()
            findNavController().navigate(direction)
        }

        override fun onSearchItemSelected(query: String) {
            binding.searchBar.text = query
            binding.searchView.hide()

            val direction = HomeFragmentDirections.openPhotoList(query)
            findNavController().navigate(direction)
        }
    }
}

private interface AdapterListener {
    fun onSavedCtaPressed()
    fun onSearchItemSelected(query: String)
}

private class ListAdapter(private val listener: AdapterListener) :
    RecyclerView.Adapter<HomeViewHolder>() {

    private val diffCallback = object : DiffUtil.ItemCallback<HomeViewItem>() {
        override fun areItemsTheSame(
            oldItem: HomeViewItem,
            newItem: HomeViewItem
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: HomeViewItem,
            newItem: HomeViewItem
        ): Boolean {
            return oldItem.id == newItem.id
        }
    }

    val differ = AsyncListDiffer(this, diffCallback)

    fun accept(newItems: List<HomeViewItem>, commitCallback: Runnable? = null) {
        differ.submitList(newItems, commitCallback)
    }

    override fun getItemViewType(position: Int) =
        differ.currentList[position].type.ordinal

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (val itemType = HomeViewItemType.values()[viewType]) {
            HomeViewItemType.BOOKMARK_CTA -> {
                val bindings: CtaSavedViewItemBinding =
                    DataBindingUtil.inflate(
                        inflater, itemType.layoutId, parent, false
                    )
                HomeViewHolder.BookmarkCtaViewHolder(bindings)
            }
            HomeViewItemType.SEARCH_HISTORY_ITEM -> {
                val bindings: SearchHistoryViewItemBinding =
                    DataBindingUtil.inflate(
                        inflater, itemType.layoutId, parent, false
                    )
                HomeViewHolder.SearchHistoryViewHolder(bindings)
            }
            HomeViewItemType.EMPTY -> {
                val bindings: EmptyViewItemBinding =
                    DataBindingUtil.inflate(
                        inflater, itemType.layoutId, parent, false
                    )
                HomeViewHolder.EmptyViewHolder(bindings)
            }
        }
    }

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
        val item = differ.currentList[position]
        when (holder) {
            is HomeViewHolder.BookmarkCtaViewHolder -> holder.bind(
                item as HomeViewItem.BookmarkCtaViewItem,
                onSavedCtaPressed = { listener.onSavedCtaPressed() }
            )
            is HomeViewHolder.SearchHistoryViewHolder -> holder.bind(
                item as HomeViewItem.SearchHistoryItem,
                onSearchItemSelected = { query -> listener.onSearchItemSelected(query) }
            )
            is HomeViewHolder.EmptyViewHolder -> {}
        }
    }

    override fun getItemCount() = differ.currentList.size
}