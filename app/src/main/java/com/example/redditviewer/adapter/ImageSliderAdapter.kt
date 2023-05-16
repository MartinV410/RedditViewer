package com.example.redditviewer.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.redditviewer.databinding.ImageSliderItemBinding
import com.example.redditviewer.loadImage

/**
 * Adapter for image/images slider in post content
 */
class ImageSlideAdapter(private val context: Context, private var imageList: List<String>, private val onClickAction: () -> Unit) : PagerAdapter() {
    override fun getCount(): Int {
        return imageList.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val layoutInflater = LayoutInflater.from(context)
        val binding = ImageSliderItemBinding.inflate(layoutInflater, container, false)

        imageList[position].let {
            loadImage(container, imageList[position], -1, binding.slideImage, context)
        }
        binding.slideImage.setOnClickListener {
            onClickAction()
        }


        val vp = container as ViewPager
        vp.addView(binding.root, 0)
        return binding.root
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        val vp = container as ViewPager
        val view = `object` as View
        vp.removeView(view)
    }
}