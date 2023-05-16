package com.example.redditviewer.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Response from API for subscribed subreddits
 */
@Serializable
data class SubscribedResponse(
    val kind: String,
    val data: SubscribedResponseData
)

/**
 * Data for SubscribedResponse
 */
@Serializable
data class SubscribedResponseData(
    val after: String? = null,
    val dist: Int,
    val modhash: String = "",
    @SerialName(value = "geo_filter")
    val geoFilter: String,
    val children: List<SubscribedChildren>
)

/**
 * One child of SearchResponseData
 */
@Serializable
data class SubscribedChildren(
    val kind: String,
    val data: Subscribed
)


/**
 *  All of search children data
 */
@Serializable
data class Subscribed(
    @SerialName(value = "icon_img")
    val iconUrl: String,
    @SerialName(value = "display_name_prefixed")
    val namePrefixed: String,
    @SerialName(value = "display_name")
    val name: String,
    val id: String
)