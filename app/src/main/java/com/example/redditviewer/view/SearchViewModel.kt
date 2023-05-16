package com.example.redditviewer.view

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.redditviewer.GenericUIState
import com.example.redditviewer.RedditViewerApplication
import com.example.redditviewer.data.PostsRepository
import com.example.redditviewer.network.SearchedSubreddit
import com.example.redditviewer.network.SubscribedChildren
import com.example.redditviewer.network.SubscribedResponse
import kotlinx.coroutines.launch
import java.lang.Exception



class SearchViewModel(private val postsRepository: PostsRepository)  : ViewModel()  {

    // TODO make private with getter
    // holds UI state for subscribed subreddits from API
    val subscribedUiState: MutableLiveData<GenericUIState> by lazy {
        MutableLiveData<GenericUIState>(GenericUIState.Loading)
    }

    private var currentResponse: SubscribedResponse? = null
    private val subscribed = mutableListOf<SubscribedChildren>()


    // holds UI state for searched subreddits from API
    val searchedSubredditUIState: MutableLiveData<GenericUIState> by lazy {
        MutableLiveData<GenericUIState>(GenericUIState.Loading)
    }
    private val searchedSubreddits = mutableListOf<SearchedSubreddit>()


    init {
        fetchSubscribed()
    }

    fun getSubscribed(): MutableList<SubscribedChildren> {
        return subscribed
    }

    /**
     * Fetch list of subscribed subreddits
     */
    fun fetchSubscribed() {
        viewModelScope.launch {
            subscribedUiState.value = GenericUIState.Loading

            subscribedUiState.value = try {

                if(currentResponse == null) {
                    val result = postsRepository.getSubscribedSubreddits("")
                    currentResponse = result
                    subscribed.addAll(result.data.children)
                    GenericUIState.Success
                } else {
                    if(currentResponse?.data?.after != null) {
                        val result = postsRepository.getSubscribedSubreddits(currentResponse?.data?.after?: "")
                        currentResponse = result
                        subscribed.addAll(result.data.children)
                        GenericUIState.Success
                    } else{
                        GenericUIState.Error("No more subreddits to load")
                    }
                }
            } catch (e: Exception) {
                GenericUIState.Error("Error loading subscribed")
            }
        }
    }


    /**
     * Search subreddits
     */
    fun fetchSearchSubreddits(subreddit: String) {
        viewModelScope.launch {
            searchedSubredditUIState.value = GenericUIState.Loading

            searchedSubredditUIState.value = try {
                val result = postsRepository.searchSubreddits(subreddit)
                searchedSubreddits.clear()
                searchedSubreddits.addAll(result.subreddits)
                GenericUIState.Success
            } catch (e: retrofit2.HttpException) {
                GenericUIState.Error("Error loading searched subreddits")
            }
        }
    }


    /**
     * Retrieves searched subreddits
     */
    fun getSearchedSubreddits(): MutableList<SearchedSubreddit> {
        return searchedSubreddits
    }

    /**
     * companion object for ViewModel parameters
     */
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as RedditViewerApplication)
                val postsRepository = application.container.postsRepository
                SearchViewModel(postsRepository = postsRepository)
            }
        }
    }

}