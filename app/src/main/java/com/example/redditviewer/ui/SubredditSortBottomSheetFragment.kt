package com.example.redditviewer.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.fragment.findNavController
import com.example.redditviewer.R
import com.example.redditviewer.databinding.SubredditSortBottomSheetBinding
import com.example.redditviewer.network.PostsFilter
import com.example.redditviewer.network.PostsFilterQuery
import com.example.redditviewer.network.PostsTimeframe
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * Bottom sheet fragment for choosing sort filter for subreddit posts
 */
class SubredditSortBottomSheetFragment: BottomSheetDialogFragment() {

    private var currentFilter: PostsFilter = PostsFilter.HOT

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val binding = SubredditSortBottomSheetBinding.inflate(inflater, container, false)
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogTheme);

        binding.subredditSortHot.setOnClickListener {
            setFilter(PostsFilterQuery(PostsFilter.HOT, PostsTimeframe.NONE))
            dismiss()
        }
        binding.subredditSortNew.setOnClickListener {
            setFilter(PostsFilterQuery(PostsFilter.NEW, PostsTimeframe.NONE))
            dismiss()
        }
        binding.subredditSortRising.setOnClickListener {
            setFilter(PostsFilterQuery(PostsFilter.RISING, PostsTimeframe.NONE))
            dismiss()
        }
        binding.subredditSortTop.setOnClickListener {
            currentFilter = PostsFilter.TOP
            navigateToTimeframe()
        }
        binding.subredditSortControversial.setOnClickListener {
            currentFilter = PostsFilter.CONTROVERSIAL
            navigateToTimeframe()
        }

        // retrieves timeframe from TimeframeBottomSheetFragment
        val fragment = findNavController().getBackStackEntry(R.id.subredditSortBottomSheetFragment2)
        val dialogObserver = LifecycleEventObserver {_, event ->
            if(event == Lifecycle.Event.ON_RESUME && fragment.savedStateHandle.contains("timeframe")) {
                val timeframe = fragment.savedStateHandle.get<PostsTimeframe>("timeframe")
                if(timeframe != null){
                    setFilter(PostsFilterQuery(currentFilter, timeframe))
                }
                dismiss()
            }
        }
        val dialogLifecycle = fragment.getLifecycle()
        dialogLifecycle.addObserver(dialogObserver)
        viewLifecycleOwner.lifecycle.addObserver(LifecycleEventObserver{_, event ->
            if(event == Lifecycle.Event.ON_DESTROY) {
                dialogLifecycle.removeObserver(dialogObserver)
            }
        })

        return binding.root
    }

    /**
     * Set [filter] to savedStateHandle of nav controller
     */
    private fun setFilter(filter: PostsFilterQuery) {
        val stateHandle = findNavController().previousBackStackEntry!!.savedStateHandle
        stateHandle["filter"] = filter.filter
        stateHandle["timeframe"] = filter.timeframe
    }

    /**
     * Navigate to timeframe dialog picker
     */
    private fun navigateToTimeframe() {
        findNavController().navigate(R.id.action_subredditSortBottomSheetFragment2_to_subredditTimeframeBottomSheetFragment)
    }

    /**
     * Ovveride animations
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