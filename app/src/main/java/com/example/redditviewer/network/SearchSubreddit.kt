package com.example.redditviewer.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Response from subreddits API call
 */
@Serializable
data class SearchSubredditResponse(
    val subreddits: List<SearchedSubreddit>
)


/**
 * Subscribe search item
 */
@Serializable
data class SearchedSubreddit(
    @SerialName(value = "icon_img")
    val iconUrl: String,
    val name: String,
    @SerialName(value = "subscriber_count")
    val subscriberCount: Int,

)