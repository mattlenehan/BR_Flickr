package com.example.br_flickr.ui.main.bookmarks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.br_flickr.databinding.EmptyViewItemBinding
import com.example.br_flickr.databinding.FragmentBookmarksBinding
import com.example.br_flickr.databinding.FragmentPhotoListBinding
import com.example.br_flickr.databinding.PhotoViewItemBinding
import com.example.br_flickr.ui.main.photos.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BookmarksFragment : Fragment() {

    private val viewModel: BookmarksViewModel by viewModels()

    private val photosAdapter by lazy {
        ListAdapter(listener = adapterListener)
    }

    private var _binding: FragmentBookmarksBinding? = null
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
        _binding = FragmentBookmarksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun init() {
        viewModel.fetchLocalPhotos()
        binding.recycler.apply {
            adapter = photosAdapter
            layoutManager = GridLayoutManager(binding.root.context, 2).apply {
                spanSizeLookup = photosAdapter.spanSizeLookup
            }
        }
    }

    private fun subscribeUi() {
        viewModel.photos.observe(
            viewLifecycleOwner
        ) {
            binding.recycler.visibility = View.VISIBLE
            binding.loading.visibility = View.GONE
            photosAdapter.accept(it ?: emptyList())
        }
    }

    private val adapterListener = object : AdapterListener {
        override fun onPhotoClick(url: String, title: String) {
            val direction = BookmarksFragmentDirections.openPhotoDetails(url, title)
            findNavController().navigate(direction)
        }
    }
}

private interface AdapterListener {
    fun onPhotoClick(url: String, title: String)
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
            PhotoViewItemType.EMPTY -> {
                val bindings: EmptyViewItemBinding =
                    DataBindingUtil.inflate(
                        inflater, itemType.layoutId, parent, false
                    )
                PhotoViewHolder.EmptyViewHolder(bindings)
            }
            PhotoViewItemType.SAVED_PHOTO_LIST_ITEM -> {
                val bindings: PhotoViewItemBinding =
                    DataBindingUtil.inflate(
                        inflater, itemType.layoutId, parent, false
                    )
                PhotoViewHolder.SavedPhotoListViewHolder(bindings)
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
            is PhotoViewHolder.EmptyViewHolder -> {}
            is PhotoViewHolder.SavedPhotoListViewHolder -> holder.bind(
                item as PhotoViewItem.SavedPhotoListItem
            )
        }
    }

    override fun getItemCount() = differ.currentList.size

    val spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
        override fun getSpanSize(position: Int): Int {
            return when (differ.currentList[position]) {
                is PhotoViewItem.PhotoListItem -> 1
                is PhotoViewItem.EmptyState -> 2
                is PhotoViewItem.SavedPhotoListItem -> 1
            }
        }
    }
}