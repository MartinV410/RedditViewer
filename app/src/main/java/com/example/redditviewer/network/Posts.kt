package com.example.redditviewer.network
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * List of supported main post content types
 */
enum class PostContentType() {
    LINK,
    IMAGE,
    IMAGE_GALERY,
    VIDEO,
    UNSUPPORTED
}

// Dataclasses represent JSON structure from reddit API call for posts data (oauth.reddit.com/api/r/<subreddit>)

/**
 * Main post response class (root of response from API)
 */
@Serializable
data class PostsResponse(
    val kind: String,
    val data: PostsResponseData
)

/**
 * Post data (root->data)
 */
@Serializable
data class PostsResponseData(
    val after: String? = null,
    val dist: Int,
    @SerialName(value = "modhash")
    val modHash: String = "", // can be null in response
    val geoFilter: String = "", // can be null in response
    @SerialName(value = "children")
    val posts: List<Post>
)

/**
 * Single post from response (root->data->children[0...]])
 */
@Serializable
@Parcelize
data class Post(
    val kind: String,
    val data: PostData
): Parcelable

/**
 * Dataclass representing single post data (root->data->children[0...]->data)
 */
@Serializable
@Parcelize
data class PostData(
    // GENERAL
    @SerialName(value = "post_hint")
    val postHint: String = "", // indicates type of post
    val id: String, // ID of post
    @SerialName(value = "sr_detail")
    val subreddit: Subreddit, // subreddit of post
    @SerialName(value = "likes")
    val voted: Boolean? = null, // if user voted (true = upvote, false = downvote, null = not voted)
    // TOP
    @SerialName(value = "author")
    val author: String, //author
    @SerialName(value = "created_utc")
    val created: Float, // created
    @SerialName(value = "domain")
    val domain: String, // domain
    @SerialName(value = "title")
    val title: String, // title

    // MIDDLE CONTENT
    @SerialName(value = "media")
    val media: PostMedia? = null,
    @SerialName(value = "gallery_data")
    val galleryData: PostGallery? = null,
    @SerialName(value = "url_overridden_by_dest")
    val urlDest: String = "", // destination content url (image, video, link...). Not present if theres only text in post

    @SerialName(value = "thumbnail")
    val linkThumbnailUrl: String, // link thumbnail image url

    // TEXT (post_hint not present)
    @SerialName(value = "selftext")
    val text: String = "", // selftext

    // ! MIDDLE CONTENT

    // Bottom
    @SerialName(value = "num_comments")
    val comments: Int, //num_comments
    @SerialName(value = "score")
    var score: Int, // score
    @SerialName(value = "permalink")
    val shareUrl:  String, // permalink


) : Parcelable

/**
 * Response from subreddit info endpoint
 */
@Serializable
data class SubredditResponse(
    val kind: String,
    val data: Subreddit
)

/**
 * Single subreddit data
 */
@Serializable
@Parcelize
data class Subreddit(
    @SerialName(value = "banner_img")
    val bannerImgUrl: String,
    @SerialName(value = "display_name")
    val name: String,
    @SerialName(value = "display_name_prefixed")
    val namePrefixed: String, // name prefixed with /r
    @SerialName(value = "icon_img")
    val iconUrl: String = "",
    val iconID: Int = -1,
    @SerialName(value = "public_description")
    val publicDescription: String,
    @SerialName(value = "user_is_subscriber")
    val subscribed: Boolean,
    @SerialName(value = "subscribers")
    val subscribers: Int,
    val global: Boolean = false

) : Parcelable

/**
 * Media for single post
 */
@Serializable
@Parcelize
data class PostMedia(
    @SerialName(value = "reddit_video")
    val video: PostVideo? = null,
) : Parcelable

/**
 * Image gallery for single post
 */
@Serializable
@Parcelize
data class PostGallery(
    @SerialName(value = "items")
    val items: List<PostGalleryItem>
): Parcelable

/**
 * Single gallery item
 */
@Serializable
@Parcelize
data class PostGalleryItem(
    @SerialName(value = "caption")
    val caption: String = "",
    @SerialName(value = "media_id")
    val mediaID: String,
    @SerialName(value = "id")
    val id: Int,

): Parcelable

/**
 * Video for post
 */
@Serializable
@Parcelize
data class PostVideo(
    @SerialName(value = "fallback_url")
    val url: String
): Parcelable

