package com.example.redditviewer.view

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.redditviewer.RedditViewerApplication
import com.example.redditviewer.data.PostsRepository
import com.example.redditviewer.network.User
import kotlinx.coroutines.launch


sealed interface UserUIState {
    data class Success(val user: User) : UserUIState
    data class Error(val message: String) : UserUIState
    object Loading : UserUIState
}

class UserInfoViewModel(private val postsRepository: PostsRepository)  : ViewModel() {

    val userUIState: MutableLiveData<UserUIState> by lazy {
        MutableLiveData<UserUIState>(UserUIState.Loading)
    }

    init {
        fetchUserInfo()
    }

    /**
     * Fetch current logged user info
     */
    private fun fetchUserInfo() {
        userUIState.value = UserUIState.Loading

        viewModelScope.launch {
            userUIState.value = try {
                val userResponse = postsRepository.getUserInfo()

                UserUIState.Success(userResponse.data)
            } catch (e: Exception) {
                UserUIState.Error("Failed to load user info")
            }
        }
    }

    /**
     * companion object for ViewModel parameters
     */
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as RedditViewerApplication)
                val postsRepository = application.container.postsRepository
                UserInfoViewModel(postsRepository = postsRepository)
            }
        }
    }
}