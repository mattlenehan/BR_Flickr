package com.example.br_flickr.ui.main.details

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import coil.load
import com.example.br_flickr.R
import com.example.br_flickr.databinding.FragmentPhotoDetailsBinding
import com.example.br_flickr.ui.main.bookmarks.BookmarksViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream
import java.net.URL

@AndroidEntryPoint
class PhotoDetailsFragment : Fragment() {

    private val args: PhotoDetailsFragmentArgs by navArgs()

    private var _binding: FragmentPhotoDetailsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PhotoDetailsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPhotoDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun init() {
        binding.imageDetail.load(args.photoUrl)
        binding.saveButton.setOnClickListener {
            binding.saveButton.text = getString(R.string.saved_check)
            binding.saveButton.isEnabled = false
            val url = URL(args.photoUrl)

            viewModel.saveImageToInternalStorage(
                url, "Br_flickr_${System.currentTimeMillis()}"
            )
        }
    }
}