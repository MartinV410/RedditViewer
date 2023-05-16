package com.example.redditviewer.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * API response for comments of a post (both [PostsResponse] and [CommentsResponse] are retrieved)
 */
@Serializable
data class PostCommentsResponse(
    val post: PostsResponse,
    val comment: CommentsResponse
)

/**
 * Response from comments API for given post
 */
@Serializable
data class CommentsResponse(
    val kind: String,
    val data: CommentsResponseData
)

/**
 * Data of an [CommentsResponse]
 */
@Serializable
data class CommentsResponseData(
    val after: String?,
    val dist: String?,
    val modhash: String?,
    @SerialName(value = "geo_filter")
    val geoFilter: String,
    val children: List<Comment>
)

/**
 * Single comment
 */
@Serializable
data class Comment(
    val kind: String,
    val data: CommentData
)

/**
 * Comment data
 */
@Serializable
data class CommentData(
    val id: String,
    val body: String,
    val created: Int,
    val author: String,
    val score: Int,
    @SerialName(value = "likes")
    val voted: Boolean?,
    val permalink: String
)

