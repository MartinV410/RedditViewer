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
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.redditviewer.ButtonClickPosition
import com.example.redditviewer.CommentItemClickInterface
import com.example.redditviewer.PostItemClickInterface
import com.example.redditviewer.R
import com.example.redditviewer.adapter.CommentsAdapter
import com.example.redditviewer.databinding.FragmentPostCommentsBinding
import com.example.redditviewer.getButtonClickPosition
import com.example.redditviewer.getPostItemImages
import com.example.redditviewer.network.Comment
import com.example.redditviewer.network.Post
import com.example.redditviewer.network.PostData
import com.example.redditviewer.setPostItemContentImage
import com.example.redditviewer.view.PostCommentsUiState
import com.example.redditviewer.view.PostCommentsViewModel
import com.google.android.material.button.MaterialButton

/**
 * Fragment for displaying [Post] content and comments.
 */
class PostCommentsFragment: Fragment(), PostItemClickInterface, CommentItemClickInterface {

    private val viewModel: PostCommentsViewModel by viewModels { PostCommentsViewModel.Factory}
    private val adapter = CommentsAdapter(this)
    private val args: PostCommentsFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding: FragmentPostCommentsBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_post_comments, container, false)
        binding.lifecycleOwner = this
        binding.comments.adapter = adapter

        // set post item binding and its listeners
        val post = args.post
        if(viewModel.currentPost.value == null) {
            viewModel.currentPost.value = post
            viewModel.fetchComments()
        }

        viewModel.currentPost.value.let {
            it?.let {
                binding.includePostItem.post = it.data
                setPostItemContentImage(it.data, binding.includePostItem, this)
            }
        }

        viewModel.currentPost.observe(viewLifecycleOwner, Observer {
            viewModel.currentPost.value.let {
                it?.let {
                    binding.includePostItem.post = it.data
                }
            }
        })

        viewModel.postCommentsUIState.observe(viewLifecycleOwner, Observer {
            it?.let {
                when(it) {
                    is PostCommentsUiState.Error -> {
                        binding.refreshLayout.isRefreshing = false
                        Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                    }
                    PostCommentsUiState.Loading -> {
                        binding.refreshLayout.isRefreshing = true
                    }
                    is PostCommentsUiState.Success -> {
                        binding.refreshLayout.isRefreshing = false
                        adapter.submitList(it.comments.toList())
                    }
                }
            }
        })

        binding.refreshLayout.setOnRefreshListener {
            viewModel.resetComments()
            viewModel.fetchComments()
        }

        binding.includePostItem.postItemShare.setOnClickListener {
            onShareClick(post.data.shareUrl)
        }

        binding.includePostItem.postItemComments.setOnClickListener {
            onMessageClick(post)
        }

        binding.includePostItem.postItemVote.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                if (event?.action == MotionEvent.ACTION_UP) {
                    onVoteClick(
                        post,
                        binding.includePostItem.postItemVote,
                        event
                    )
                }
                return v?.onTouchEvent(event) ?: true
            }

        })

        binding.includePostItem.postItemLayoutLink.setOnClickListener {
            onLinkClick(post.data.urlDest)
        }

        binding.includePostItem.postItemSubredditIcon.setOnClickListener {
            onIconClick(post.data.subreddit.name)
        }

        return binding.root
    }

    /**
     * Vote on given [comment]
     */
    override fun onVoteClick(comment: Comment, button: MaterialButton, event: MotionEvent) {
        when(getButtonClickPosition(button, event)) {
            ButtonClickPosition.LEFT -> viewModel.upvoteComment(comment)
            ButtonClickPosition.RIGHT -> viewModel.downvoteComment(comment)
            ButtonClickPosition.NONE -> {}
        }
    }

    /**
     * Start android share intent with given [destUrl]
     */
    override fun onShareClick(destUrl: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "https://reddit.com${destUrl}")
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    override fun onMessageClick(post: Post) {}

    /**
     * Vote on given [post]
     */
    override fun onVoteClick(post: Post, button: MaterialButton, event: MotionEvent) {
        when(getButtonClickPosition(button, event)) {
            ButtonClickPosition.LEFT -> viewModel.upvotePost()
            ButtonClickPosition.RIGHT -> viewModel.downvotePost()
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
     * Navigate to fullscreen gallery of [postData] images
     */
    override fun onImageClick(postData: PostData) {
        val images = getPostItemImages(postData)
        if(images.isNotEmpty()) {
            val direction = PostCommentsFragmentDirections.actionPostCommentsFragmentToImageGalleryFragment(images.toTypedArray())
            findNavController().navigate(direction)
        }
    }

    /**
     * Navigate to main subreddit fragment with given [subreddit] as subreddit
     */
    override fun onIconClick(subreddit: String) {
        val direction = PostCommentsFragmentDirections.actionPostCommentsFragmentToSubredditActivity(subreddit)
        findNavController().navigate(direction)
    }
}