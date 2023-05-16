package com.example.redditviewer.view

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.redditviewer.R
import com.example.redditviewer.RedditViewerApplication
import com.example.redditviewer.data.PostsRepository
import com.example.redditviewer.network.Post
import com.example.redditviewer.network.PostsFilter
import com.example.redditviewer.network.PostsFilterQuery
import com.example.redditviewer.network.PostsResponse
import com.example.redditviewer.network.PostsTimeframe
import com.example.redditviewer.network.Subreddit
import com.example.redditviewer.network.SubscribeAction
import com.example.redditviewer.network.VoteDirection
import com.example.redditviewer.voteAction
import kotlinx.coroutines.launch
import java.lang.Exception

/**
 * Global (feeds) subreddits
 */
enum class SubredditGlobal(val title: Int, val id: String, val message: String, val image: Int) {
    ALL(title = R.string.all, id= "all", message = "Showing all subreddits posts", R.drawable.baseline_bar_chart_24),
    FRONTPAGE(title = R.string.frontpage, id= "frontpage", message = "Showing your subscribed subreddits posts", R.drawable.baseline_home_24),
    POPULAR(title = R.string.popular,id= "popular", message = "Showing popular posts", R.drawable.baseline_show_chart_24);

    companion object {
        fun getByID(id: String) = values().firstOrNull { it.id == id }
    }
}

/**
 * Representing posts items UI state
 */
sealed interface PostsUiState {
    data class Success(val posts:  PostsResponse) : PostsUiState
    data class Error(val message: String) : PostsUiState
    object Loading : PostsUiState
}

/**
 * Representing top subreddit UI state
 */
sealed interface SubredditUiState {
    data class Success(val subreddit:  Subreddit) : SubredditUiState
    data class Error(val message: String) : SubredditUiState
    object Loading : SubredditUiState
}

/**
 * View model for main subreddit screen fragment
 */
class SubredditViewModel(private val postsRepository: PostsRepository)  : ViewModel() {

    // holds UI state with posts from API
    val postsUiState: MutableLiveData<PostsUiState> by lazy {
        MutableLiveData<PostsUiState>(PostsUiState.Loading)
    }

    val posts: MutableLiveData<MutableList<Post>> by lazy {
        MutableLiveData<MutableList<Post>>(mutableListOf())
    }

    // holds UI state for subreddit title
    val subredditUiState: MutableLiveData<SubredditUiState> by lazy {
        MutableLiveData<SubredditUiState>(SubredditUiState.Loading)
    }

    val currentQuery: MutableLiveData<PostsFilterQuery> by lazy {
        MutableLiveData<PostsFilterQuery>(PostsFilterQuery(PostsFilter.HOT, PostsTimeframe.NONE, SubredditGlobal.FRONTPAGE.id))
    }

    init {
        // fetch initial data
        fetchSubreddit()
        fetchPosts()
    }

    /**
     * Reset posts query so that it starts from the start
     */
    fun resetPosts() {
        val query = currentQuery.value ?: return
        posts.value = mutableListOf()
        currentQuery.value = query.copy(after = "null")
    }

    /**
     * Set new subreddit and fetch posts from it
     */
    fun setSubreddit(subreddit: String) {
        resetPosts()
        val query = currentQuery.value ?: return
        currentQuery.value = query.copy(subreddit = subreddit)
        fetchSubreddit()
        fetchPosts()
    }

    /**
     * Retrieves current subreddit name
     */
    fun getCurrentSubreddit(): String{
        val currentQ =currentQuery.value
        if(currentQ != null) {
            return currentQ.subreddit
        }
        return ""
    }


    /**
     * Retrieve initial posts for given subreddit and applied filters (updates [postsUiState])
     */
    fun fetchPosts() {
        viewModelScope.launch {
            postsUiState.value = PostsUiState.Loading
            val query = currentQuery.value ?: return@launch
            if(query.after == null) {
                postsUiState.value = PostsUiState.Error("No more posts to load")
                return@launch
            }

            postsUiState.value = try {
                val listResult =
                    if(query.subreddit == SubredditGlobal.FRONTPAGE.id) { // frontpage
                        postsRepository.getPostsFrontpage(query.filter, query.after)
                    } else { // other subreddits
                        if(query.timeframe == PostsTimeframe.NONE) {
                            postsRepository.getPosts(query.subreddit, query.filter, query.after)
                        } else {
                            postsRepository.getPostsTimeframe(query.subreddit, query.filter, query.timeframe, query.after)
                        }
                    }
                val queryCopy = currentQuery.value?.copy(after = listResult.data.after)
                if(queryCopy != null) currentQuery.value = queryCopy

                if(posts.value != null) {
                    val newPosts = posts.value!!
                    newPosts.addAll(listResult.data.posts)
                    posts.value = newPosts
                }
                PostsUiState.Success(listResult)

            } catch (e: Exception) {
                PostsUiState.Error("Failed to load posts")
            }
        }
    }

    /**
     * Retrieve info about given subreddit (updates [subredditUiState])
     */
    private fun fetchSubreddit() {
        val subreddit: String = currentQuery.value?.subreddit ?: return

        if(subreddit in listOf<String>(SubredditGlobal.ALL.id, SubredditGlobal.POPULAR.id, SubredditGlobal.FRONTPAGE.id)) {
            val sub = SubredditGlobal.getByID(subreddit) ?: return
            val result = Subreddit(
                bannerImgUrl = "",
                name = sub.name,
                namePrefixed = "r/${sub.name}",
                iconID = sub.image,
                publicDescription = sub.message,
                subscribed = false,
                global = true,
                subscribers = 0)
            subredditUiState.value = SubredditUiState.Success(result)
            return
        }

        subredditUiState.value = SubredditUiState.Loading
        viewModelScope.launch {
            subredditUiState.value = try {
                SubredditUiState.Success(postsRepository.getSubredditInfo(subreddit).data)
            } catch (e: retrofit2.HttpException) {
                SubredditUiState.Error("Loading post failed: ${e.code()}")
            }
        }
    }

    /**
     * Vote on given [post] based on [direction] it should take
     */
    private fun voteOnPost(post: Post, direction: VoteDirection) {
        if(postsUiState.value !is PostsUiState.Success) return

        viewModelScope.launch {
            try {
                val action = voteAction(direction, post.data.voted)
                postsRepository.vote(post.kind + "_" + post.data.id, action.direction)

                val updatedData = post.data.copy(score = post.data.score + action.num, voted = action.voted)
                val updatedPost = post.copy(data = updatedData)
                val updatedPosts = posts.value!!.toMutableList()
                updatedPosts[updatedPosts.indexOf(post)] = updatedPost

                posts.value = updatedPosts
            } catch (e: Exception) {

            }

        }
    }

    /**
     * Upvote given [post] (is previously up-voted - change to non-voted)
     */
    fun upvotePost(post: Post) {
        voteOnPost(post, VoteDirection.UPVOTE)
    }

    /**
     * Downvote given [post] (is previously down-voted - change to non-voted)
     */
    fun downvotePost(post: Post) {
        voteOnPost(post, VoteDirection.DOWNVOTE)
    }

    /**
     * Join or un-join current subreddit based on current join status.
     */
    fun switchSubredditJoin() {
        if(subredditUiState.value !is SubredditUiState.Success) return
        val curSubreddit = (subredditUiState.value as SubredditUiState.Success).subreddit
        viewModelScope.launch {
            subredditUiState.value = try {
                when (curSubreddit.subscribed) {
                    true -> {
                        postsRepository.subscribeToSubreddit(SubscribeAction.UNSUBSCRIBE, curSubreddit.name)
                        SubredditUiState.Success(curSubreddit.copy(subscribed = false))
                    }
                    false -> {
                        postsRepository.subscribeToSubreddit(SubscribeAction.SUBSCRIBE, curSubreddit.name)
                        SubredditUiState.Success(curSubreddit.copy(subscribed = true))
                    }
                }
            } catch (e: Exception) {
                SubredditUiState.Error("Switching join failed")
            }
        }

    }


    /**
     * companion object for ViewModel parameters
     */
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as RedditViewerApplication)
                val postsRepository = application.container.postsRepository
                SubredditViewModel(postsRepository = postsRepository)
            }
        }
    }
}