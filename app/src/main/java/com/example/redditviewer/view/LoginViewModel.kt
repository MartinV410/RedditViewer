package com.example.redditviewer.view

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.redditviewer.GenericUIState
import com.example.redditviewer.RedditViewerApplication
import com.kirkbushman.auth.RedditAuth
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Viewmodel for loginn fragment
 */
class LoginViewModel(private val authClient: RedditAuth): ViewModel()  {

    val loginUiState: MutableLiveData<GenericUIState> by lazy {
        MutableLiveData<GenericUIState>(GenericUIState.Loading)
    }

    /**
     * Retrieves token from reddit browser using oauth2
     */
    fun retrieveToken(browser: WebView, context: Context) {
        loginUiState.value = GenericUIState.Loading

        try {
            // try to revoke previous token
            authClient.getSavedBearer().revokeToken()
        } catch (e: Exception) {
            Log.e("TokenRevoke", "Failed to rekove old token: ${e.stackTrace}")
        }

        // code from https://github.com/KirkBushman/Android-Reddit-OAuth2
        browser.settings.setJavaScriptEnabled(true)

        // because https://www.reddit.com/r/redditdev/comments/10zarx9/new_oauth_issue_on_mobile_browsers/
        val updatedUrl = "https://old.reddit.com/${authClient.provideAuthorizeUrl().substringAfter("https://www.reddit.com/")}"
        browser.loadUrl(updatedUrl)

        browser.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                if (authClient.isRedirectedUrl(url)) {
                    browser.stopLoading()

                    GlobalScope.launch {

                        try{
                            // retrieve bearer that is automatically saved
                            val bearer = authClient.getTokenBearer(url)
                            loginUiState.postValue(GenericUIState.Success)

                        } catch (e: com.kirkbushman.auth.errors.AccessDeniedException) {
                            loginUiState.postValue(GenericUIState.Error("Permissions not granted"))
                        }
                    }
                }
            }
        }
    }

    /**
     * Retrieves if user is already logged in
     */
    fun needsLogin():Boolean {
        return !authClient.hasSavedBearer()
    }



    /**
     * companion object for ViewModel parameters
     */
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as RedditViewerApplication)
                val authClient = application.container.authClient
                LoginViewModel(authClient = authClient)
            }
        }
    }
}