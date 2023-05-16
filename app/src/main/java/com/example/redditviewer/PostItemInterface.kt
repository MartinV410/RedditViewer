package com.example.redditviewer

import android.view.MotionEvent
import com.example.redditviewer.network.Post
import com.example.redditviewer.network.PostData
import com.google.android.material.button.MaterialButton

/**
 * Post item onClick interactions
 */
interface PostItemClickInterface {
    /**
     * Action on share buton click
     */
    fun onShareClick(destUrl: String)

    /**
     * Action on message button click
     */
    fun onMessageClick(post: Post)

    /**
     * Action on vote button click
     */
    fun onVoteClick(post: Post, button: MaterialButton, event: MotionEvent)

    /**
     * Action on link content click
     */
    fun onLinkClick(destUrl: String)

    /**
     * Action on image content click
     */
    fun onImageClick(postData: PostData)

    /**
     * Action on subreddit icon click
     */
    fun onIconClick(subreddit: String)
}