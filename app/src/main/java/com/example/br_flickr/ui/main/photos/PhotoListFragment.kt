package com.example.br_flickr.ui.main.photos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.*
import com.example.br_flickr.databinding.EmptyViewItemBinding
import com.example.br_flickr.databinding.FragmentPhotoListBinding
import com.example.br_flickr.databinding.PhotoViewItemBinding
import com.example.br_flickr.ui.main.util.PaginationScrollListener
import com.example.br_flickr.ui.main.util.showSnackbar
import com.example.networking.util.ApiResult
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PhotoListFragment : Fragment() {

    private val args: PhotoListFragmentArgs by navArgs()

    private val viewModel: PhotoListViewModel by viewModels()

    private val photosAdapter by lazy {
        ListAdapter(listener = adapterListener)
    }

    private var _binding: FragmentPhotoListBinding? = null
    private val binding get() = _binding!!

    private var currentPage = 0

    private val paginationScrollListener = object : PaginationScrollListener() {
        override var isCurrentlyLoading: Boolean = false

        override fun loadMoreItems() {
            if (photosAdapter.itemCount > 1) {
                isCurrentlyLoading = true
                currentPage++
                viewModel.fetchPhotos(args.query, currentPage)
            }
        }
    }

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
        viewModel.fetchPhotos(args.query, 0)
        binding.recycler.apply {
            adapter = photosAdapter
            layoutManager = GridLayoutManager(binding.root.context, 2).apply {
                spanSizeLookup = photosAdapter.spanSizeLookup
            }
            paginationScrollListener.layoutManager = layoutManager as GridLayoutManager
            addOnScrollListener(paginationScrollListener)
        }
    }

    private fun subscribeUi() {
        viewModel.photos.observe(
            viewLifecycleOwner
        ) {
            val result = it.getContentIfNotHandled()
            when (result?.status) {
                ApiResult.Status.SUCCESS -> {
                    paginationScrollListener.isCurrentlyLoading = false
                    binding.recycler.visibility = View.VISIBLE
                    binding.loading.visibility = View.GONE
                    photosAdapter.accept(result.data ?: emptyList())
                }
                ApiResult.Status.ERROR -> {
                    paginationScrollListener.isCurrentlyLoading = false
                    binding.recycler.visibility = View.VISIBLE
                    binding.loading.visibility = View.GONE
                    view?.showSnackbar(result.message)
                }
                ApiResult.Status.LOADING -> {
                    paginationScrollListener.isCurrentlyLoading = true
                    binding.recycler.visibility = View.GONE
                    binding.loading.visibility = View.VISIBLE
                }
                null -> {}
            }
        }
    }

    private val adapterListener = object : AdapterListener {
        override fun onPhotoClick(url: String, title: String) {
            val direction = PhotoListFragmentDirections.openPhotoDetails(url, title)
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