package com.example.redditviewer

import android.app.Application
import com.example.redditviewer.data.AppContainer
import com.example.redditviewer.data.DefaultAppContainer

/**
 * Application providing app container for repositories
 */
class RedditViewerApplication: Application() {
    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(applicationContext)
    }

}