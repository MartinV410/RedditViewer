package com.example.redditviewer.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.redditviewer.GenericUIState
import com.example.redditviewer.R
import com.example.redditviewer.SearchSubredditsClickInterface
import com.example.redditviewer.SubscribedItemClickInterface
import com.example.redditviewer.adapter.SearchedSubredditAdapter
import com.example.redditviewer.adapter.SubscribedAdapter
import com.example.redditviewer.databinding.FragmentSearchBinding
import com.example.redditviewer.view.SearchViewModel
import com.example.redditviewer.view.SubredditGlobal

/**
 * Fragment for searching new subreddits, navigating to global feeds and subscribed subreddits
 */
class SearchFragment: Fragment(), SubscribedItemClickInterface, SearchSubredditsClickInterface {

    private val subscribedAdapter = SubscribedAdapter(this)
    private val searchedAdapter = SearchedSubredditAdapter(this)
    private val viewModel: SearchViewModel by viewModels {SearchViewModel.Factory}

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding: FragmentSearchBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_search, container, false)
        binding.lifecycleOwner = this
        binding.subscribed.adapter = subscribedAdapter
        binding.searched.adapter = searchedAdapter

        // listener for user subscribed UI state
        viewModel.subscribedUiState.observe(viewLifecycleOwner, Observer {
            it?.let {
                when(it) {
                    is GenericUIState.Error -> {
                        Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                        binding.progressBar.visibility = View.GONE
                    }
                    GenericUIState.Loading -> {binding.progressBar.visibility = View.VISIBLE}
                    GenericUIState.Success -> {
                        subscribedAdapter.submitList(viewModel.getSubscribed().toList())
                        binding.progressBar.visibility = View.GONE
                    }
                }
            }
        })

        // listener for searched subreddits ui state
        viewModel.searchedSubredditUIState.observe(viewLifecycleOwner, Observer {
            it?.let {
                when(it) {
                    is GenericUIState.Error -> {
                        Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                        binding.progressBar.visibility = View.GONE
                    }
                    GenericUIState.Loading -> {binding.progressBar.visibility = View.VISIBLE}
                    GenericUIState.Success -> {
                        searchedAdapter.submitList(viewModel.getSearchedSubreddits().toList())
                        binding.progressBar.visibility = View.GONE
                    }
                }
            }
        })

        // listener for bottom of recyclerview to fetch next subscribed subreddits
        binding.subscribed.addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager?
            if (viewModel.subscribedUiState.value !is GenericUIState.Loading) {
                val adapter = binding.subscribed.adapter?: return
                if (linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() == adapter.itemCount - 1) {
                    viewModel.fetchSubscribed()
                }
            }
        }})

        // buttons for feeds navigation
        binding.feedAll.setOnClickListener {
            navigateToSubreddit(SubredditGlobal.ALL.id)
        }
        binding.feedFrontpage.setOnClickListener {
            navigateToSubreddit(SubredditGlobal.FRONTPAGE.id)
        }
        binding.feedPopular.setOnClickListener {
            navigateToSubreddit(SubredditGlobal.POPULAR.id)
        }

        // listener for input text on "done" button click
        binding.searchInput.setOnEditorActionListener(TextView.OnEditorActionListener{ _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(requireView().windowToken, 0)

                val inputText = binding.searchInput.text.toString()
                if(inputText == "") {
                    binding.contentDefaultLayout.visibility = View.VISIBLE
                    binding.contentSearchedLayout.visibility = View.GONE
                } else {
                    viewModel.fetchSearchSubreddits(inputText)
                    binding.contentDefaultLayout.visibility = View.GONE
                    binding.contentSearchedLayout.visibility = View.VISIBLE
                }

                return@OnEditorActionListener true
            }
            false
        })

        return  binding.root
    }

    /**
     * Navigate to main [SubredditFragment] with given [subreddit]
     */
    private fun navigateToSubreddit(subreddit: String) {
        val direction = SearchFragmentDirections.actionSearchFragmentToSubredditActivity(subreddit)
        findNavController().navigate(direction)
    }

    /**
     * Navigate to main [SubredditFragment] with given [subreddit] when searched/subscribed item is clicked
     */
    override fun onLayoutClick(subreddit: String) {
        navigateToSubreddit(subreddit)
    }

}
