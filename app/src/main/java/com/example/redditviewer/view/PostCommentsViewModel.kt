package com.example.redditviewer.view

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.redditviewer.RedditViewerApplication
import com.example.redditviewer.data.PostsRepository
import com.example.redditviewer.network.Comment
import com.example.redditviewer.network.Post
import com.example.redditviewer.network.VoteDirection
import com.example.redditviewer.voteAction
import kotlinx.coroutines.launch
import java.lang.Exception

sealed interface PostCommentsUiState {
    data class Success(val comments: List<Comment>) : PostCommentsUiState
    data class Error(val message: String) : PostCommentsUiState
    object Loading : PostCommentsUiState
}

class PostCommentsViewModel(private val postsRepository: PostsRepository)  : ViewModel() {

    val currentPost: MutableLiveData<Post> by lazy {
        MutableLiveData<Post>(null)
    }

    val postCommentsUIState: MutableLiveData<PostCommentsUiState> by lazy {
        MutableLiveData<PostCommentsUiState>(PostCommentsUiState.Loading)
    }

    private val comments = mutableListOf<Comment>()

    fun resetComments() {
        comments.clear()
    }

    fun fetchComments() {
        currentPost.value?.let {
            postCommentsUIState.value = PostCommentsUiState.Loading

            viewModelScope.launch {
                postCommentsUIState.value = try {
                    val result = postsRepository.getPostComments(it.data.subreddit.name, it.data.id)
                    comments.addAll(result!!.comment.data.children)
                    PostCommentsUiState.Success(comments)
                } catch (e: Exception) {
                    PostCommentsUiState.Error("Failed to load comments")
                }
            }
        }
    }

    private fun voteOnPost(direction: VoteDirection) {
        val post = currentPost.value?: return

        viewModelScope.launch {
            val action = voteAction(direction, post.data.voted)
            postsRepository.vote(post.kind + "_" + post.data.id, action.direction)
            val updatedData = post.data.copy(score = post.data.score + action.num, voted = action.voted)
            val updatedPost = post.copy(data = updatedData)

            currentPost.value = updatedPost
        }
    }


    fun upvotePost() {
        voteOnPost(VoteDirection.UPVOTE)
    }

    fun downvotePost() {
        voteOnPost(VoteDirection.DOWNVOTE)
    }

    private fun voteOnComment(comment: Comment, direction: VoteDirection) {
        viewModelScope.launch {
            postCommentsUIState.value = PostCommentsUiState.Loading

            postCommentsUIState.value = try {
                val action = voteAction(direction, comment.data.voted)

                postsRepository.vote(comment.kind + "_" + comment.data.id, action.direction)

                val updatedCommentData = comment.data.copy(score = comment.data.score + action.num, voted = action.voted)
                val updatedComment = comment.copy(data = updatedCommentData)

                comments[comments.indexOf(comment)] = updatedComment
                PostCommentsUiState.Success(comments)
            } catch (e: Exception) {
                PostCommentsUiState.Error("Error voting on comment")
            }

//            val action = voteAction(direction, comment.data.voted)
//
//            postsRepository.vote(comment.kind + "_" + comment.data.id, action.direction)
//
//            val updatedCommentData = comment.data.copy(score = action.num, voted = action.voted)
//            val updatedComment = comment.copy(data = updatedCommentData)
//
//            comments[comments.indexOf(comment)] = updatedComment
        }
    }

    fun upvoteComment(comment: Comment) {
        voteOnComment(comment, VoteDirection.UPVOTE)
    }

    fun downvoteComment(comment: Comment) {
        voteOnComment(comment, VoteDirection.DOWNVOTE)
    }

    /**
     * companion object for ViewModel parameters
     */
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as RedditViewerApplication)
                val postsRepository = application.container.postsRepository
                PostCommentsViewModel(postsRepository = postsRepository)
            }
        }
    }
}