package com.example.redditviewer.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.example.redditviewer.adapter.ImageSliderGalleryAdapter
import com.example.redditviewer.databinding.ImageGalleryFragmentBinding

/**
 * Fullscreen image gallery fragment
 */
class ImageGalleryFragment: Fragment() {

    private val args: ImageGalleryFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = ImageGalleryFragmentBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this

        val images = args.images.toList()
        binding.imageGalleryViewpager.adapter = ImageSliderGalleryAdapter(binding.imageGalleryViewpager.context, images)
        binding.imageGalleryIndicator.setViewPager(binding.imageGalleryViewpager)


        return binding.root
    }
}