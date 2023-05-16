package com.example.redditviewer.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.redditviewer.R
import com.example.redditviewer.databinding.SubredditTimeframeBottomSheetBinding
import com.example.redditviewer.network.PostsTimeframe
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * Bottom sheet fragment for choosing timeframe filter for subreddit posts
 */
class SubredditTimeframeBottomSheetFragment: BottomSheetDialogFragment() {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val binding = SubredditTimeframeBottomSheetBinding.inflate(inflater, container, false)
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogTheme);

        binding.subredditTimeframeHour.setOnClickListener {
            setResult(PostsTimeframe.HOUR)
            dismiss()
        }
        binding.subredditTimeframeDay.setOnClickListener {
            setResult(PostsTimeframe.DAY)
            dismiss()
        }
        binding.subredditTimeframeWeek.setOnClickListener {
            setResult(PostsTimeframe.WEEK)
            dismiss()
        }
        binding.subredditTimeframeMonth.setOnClickListener {
            setResult(PostsTimeframe.MONTH)
            dismiss()
        }
        binding.subredditTimeframeYear.setOnClickListener {
            setResult(PostsTimeframe.YEAR)
            dismiss()
        }
        binding.subredditTimeframeAll.setOnClickListener {
            setResult(PostsTimeframe.ALL)
            dismiss()
        }

        return binding.root
    }

    /**
     * Set [timeframe] result to savedStateBundle of nav controller
     */
    private fun setResult(timeframe: PostsTimeframe) {
        findNavController().previousBackStackEntry!!.savedStateHandle["timeframe"] = timeframe
    }

    /**
     * Override animations
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog!!.window!!.attributes.windowAnimations = R.style.DialogBottomAnimation
    }

    /**
     * For transparent bg color
     */
    override fun getTheme() = R.style.BottomSheetDialogTheme
}