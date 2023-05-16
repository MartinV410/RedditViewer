package com.example.redditviewer.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.redditviewer.ButtonClickPosition
import com.example.redditviewer.PostItemClickInterface
import com.example.redditviewer.adapter.PostsAdapter
import com.example.redditviewer.R
import com.example.redditviewer.databinding.FragmentSubredditBinding
import com.example.redditviewer.getButtonClickPosition
import com.example.redditviewer.getPostItemImages
import com.example.redditviewer.network.Post
import com.example.redditviewer.network.PostData
import com.example.redditviewer.network.PostsFilter
import com.example.redditviewer.network.PostsFilterQuery
import com.example.redditviewer.network.PostsTimeframe
import com.example.redditviewer.view.PostsUiState
import com.example.redditviewer.view.SubredditUiState
import com.example.redditviewer.view.SubredditViewModel
import com.google.android.material.button.MaterialButton

/**
 * Subreddit fragment displaying current subreddit info and its posts
 */
class SubredditFragment: Fragment(), PostItemClickInterface{

    private val viewModel: SubredditViewModel by viewModels { SubredditViewModel.Factory }
    private val adapter = PostsAdapter(this)
    private val args: SubredditFragmentArgs by navArgs()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // data binding to activity
        val binding: FragmentSubredditBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_subreddit, container, false)
        binding.lifecycleOwner = this
        binding.subredditViewModel= viewModel

        // add posts adapter to posts recyclerview
        binding.posts.adapter = adapter

        if(savedInstanceState == null) {
            val subreddit = args.subreddit
            if(subreddit != null && subreddit != viewModel.getCurrentSubreddit()) {
                setSubreddit(subreddit)
            }
        }

        // subreddit share button
        binding.buttonShare.setOnClickListener {
            if(viewModel.subredditUiState.value is SubredditUiState.Success) {
                onShareClick("/r/${(viewModel.subredditUiState.value as SubredditUiState.Success).subreddit.name}")
            }
        }

        // subreddit join button
        binding.buttonJoin.setOnClickListener{
            viewModel.switchSubredditJoin()
        }

        // home button at bottom
        binding.buttonHome.setOnClickListener {
            binding.posts.smoothScrollToPosition(-50)
        }

        // search button at bottom
        binding.buttonSearch.setOnClickListener {
            val direction = SubredditFragmentDirections.actionSubredditActivityToSearchFragment()
            findNavController().navigate(direction)
        }

        // user icon button at bottom
        binding.buttonUser.setOnClickListener {
            val direction = SubredditFragmentDirections.actionSubredditActivityToUserInfoBottomSheetFragment()
            findNavController().navigate(direction)
        }

        // Livedata binding to posts
        viewModel.postsUiState.observe(viewLifecycleOwner, Observer {
            it?.let {
                when (it){
                    is PostsUiState.Error -> {
                        binding.refreshLayout.isRefreshing = false
                        Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                    }
                    PostsUiState.Loading -> {
                        binding.refreshLayout.isRefreshing = true
                    }
                    is PostsUiState.Success -> {
                        binding.refreshLayout.isRefreshing = false
                    }
                }
            }
        })

        // subreddituistate observer
        viewModel.subredditUiState.observe(viewLifecycleOwner, Observer {
            it?.let {
                when(it) {
                    is SubredditUiState.Error -> {
                        Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                    }
                    SubredditUiState.Loading -> {}
                    is SubredditUiState.Success -> {}
                }
            }
        })

        // posts observer
        viewModel.posts.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.submitList(it.toList())
            }
        })

        // listener for bottom of recyclerview to fetch next posts
        binding.posts.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager?
                if (viewModel.postsUiState.value !is PostsUiState.Loading) {
                    val adapter = binding.posts.adapter?: return
                    if (linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() == adapter.itemCount - 1) {
                        //bottom of list!
                        viewModel.fetchPosts()
                    }
                }
        }})


        // filter button listener
        binding.buttonFilter.setOnClickListener {
            findNavController().navigate(R.id.action_subredditActivity_to_subredditSortBottomSheetFragment2)
        }


        // observe sort sheet for filter changes
        val fragment = findNavController().getBackStackEntry(R.id.subredditActivity)
        val dialogObserver = LifecycleEventObserver {_, event ->
            if(event == Lifecycle.Event.ON_RESUME && fragment.savedStateHandle.contains("filter") && fragment.savedStateHandle.contains("timeframe")) {
                val filter = fragment.savedStateHandle.get<PostsFilter>("filter")
                val timeframe = fragment.savedStateHandle.get<PostsTimeframe>("timeframe")
                if(filter != null && timeframe != null) {
                    // set new current query
                    val query = viewModel.currentQuery.value ?: return@LifecycleEventObserver
                    viewModel.currentQuery.value = PostsFilterQuery(filter, timeframe, query.subreddit)
                    fetchNewPosts()
                    // remove existing
                    fragment.savedStateHandle.remove<PostsFilter>("filter")
                    fragment.savedStateHandle.remove<PostsTimeframe>("timeframe")
                }
            }
        }
        val dialogLifecycle = fragment.getLifecycle()
        dialogLifecycle.addObserver(dialogObserver)
        viewLifecycleOwner.lifecycle.addObserver(LifecycleEventObserver{_, event ->
            if(event == Lifecycle.Event.ON_DESTROY) {
                dialogLifecycle.removeObserver(dialogObserver)
            }
        })

        // binding for user refresh
        binding.refreshLayout.setOnRefreshListener {
            fetchNewPosts()
        }

        return binding.root
    }

    /**
     * Reset current posts and fetch new
     */
    private fun fetchNewPosts() {
        viewModel.resetPosts()
        viewModel.fetchPosts()
    }

    /**
     * Set [subreddit] to view model
     */
    private fun setSubreddit(subreddit: String) {
        viewModel.resetPosts()
        viewModel.setSubreddit(subreddit)
    }

    /**
     * Start android share intent with given [destUrl]
     */
    override fun onShareClick(destUrl: String) {
        // sharesheet of android
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "https://reddit.com${destUrl}")
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    /**
     * Navigate to [post] comments fragment
     */
    override fun onMessageClick(post: Post) {
        val direction = SubredditFragmentDirections.actionSubredditActivityToPostCommentsFragment(post)
        findNavController().navigate(direction)
    }

    /**
     * Vote on given [post]
     */
    override fun onVoteClick(post: Post, button: MaterialButton, event: MotionEvent) {

        when(getButtonClickPosition(button, event)) {
            ButtonClickPosition.LEFT -> viewModel.upvotePost(post)
            ButtonClickPosition.RIGHT -> viewModel.downvotePost(post)
            ButtonClickPosition.NONE -> {}
        }
    }

    /**
     * Start web intent with given [destUrl]
     */
    override fun onLinkClick(destUrl: String) {
        val intent: Intent = Intent(Intent.ACTION_VIEW);
        intent.data = Uri.parse(destUrl);
        startActivity(intent)
    }

    /**
     * Navigate to fullscreen image gallery with given [postData] images
     */
    override fun onImageClick(postData: PostData) {
        val images = getPostItemImages(postData)
        if(images.isNotEmpty()) {
            val direction = SubredditFragmentDirections.actionSubredditActivityToImageGalleryFragment(images.toTypedArray())
            findNavController().navigate(direction)
        }
    }

    /**
     * Set given [subreddit] as current subreddit for this fragment
     */
    override fun onIconClick(subreddit: String) {
        setSubreddit(subreddit)
    }


}