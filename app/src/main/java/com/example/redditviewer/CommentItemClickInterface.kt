package com.example.redditviewer

import android.view.MotionEvent
import com.example.redditviewer.network.Comment
import com.google.android.material.button.MaterialButton

/**
 * Comment item onClick interactions
 */
interface CommentItemClickInterface {
    /**
     * Action on vote button click
     */
    fun onVoteClick(comment: Comment, button: MaterialButton, event: MotionEvent)

    /**
     * Action on share button click
     */
    fun onShareClick(destUrl: String)
}