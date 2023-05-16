package com.example.redditviewer.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Response from API current logged user
 */
@Serializable
data class UserResponse(
    val loid: String,
    @SerialName("loid_created")
    val loidCreated: Long,
    val kind: String,
    val data: User
)

/**
 * Current logged user data
 */
@Serializable
data class User(
    @SerialName("num_friends")
    val friends: Int,
    @SerialName("total_karma")
    val totalKarma: Int,
    val subreddit: UserSubreddit,
)

/**
 * Current logged user private subreddit
 */
@Serializable
data class UserSubreddit(
    val subscribers: Int,
    @SerialName("icon_img")
    val iconUrl: String,
    @SerialName("display_name_prefixed")
    val namePrefixed: String,
    @SerialName("display_name")
    val name: String,
)