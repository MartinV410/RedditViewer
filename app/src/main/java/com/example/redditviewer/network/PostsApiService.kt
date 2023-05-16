package com.example.redditviewer.network

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query


/**
 * Timeframes for posts filters
 */
enum class PostsTimeframe(val value: String) {
    HOUR("hour"),
    DAY("day"),
    WEEK("week"),
    MONTH("month"),
    YEAR("year"),
    ALL("all"),
    NONE("")
}

/**
 * Filter types representing various posts filters. Top and controversial filters are omitted because they requires special parameter
 */
enum class PostsFilter(val value: String) {
    HOT("hot"),
    NEW("new"),
    RISING("rising"),
    TOP("top"),
    CONTROVERSIAL("controversial")
}

/**
 * Represents query with all the filters needed to retrieve post
 */
data class PostsFilterQuery(
    val filter: PostsFilter,
    val timeframe: PostsTimeframe,
    val subreddit: String = "",
    val after: String? = "null",
)


/**
 * Post vote directions
 */
enum class VoteDirection(val dir: Int){
    UPVOTE(1),
    DOWNVOTE(-1),
    UNVOTE(0)
}

/**
 * Subreddit subscribe action
 */
enum class SubscribeAction(val action: String) {
    SUBSCRIBE("sub"),
    UNSUBSCRIBE("unsub"),
}


/**
* Interface that provides all methods for receiving posts
*/
interface PostsApiService {
    /**
     * Receive posts from frontpage of logged user
     */
    @GET("/{filter}?sr_detail=1")
    suspend fun getPostsFrontpage(@Path(value = "filter") filter: String, @Query(value = "after") after: String): PostsResponse

    /**
     * Subscribe/unsubscribe to subreddit
     */
    @POST("/api/subscribe")
    suspend fun subscribeToSubreddit(@Query(value = "action") action: String, @Query(value = "sr_name") subreddit: String)

    /**
     * Receive posts from given subreddit with given filter (filters from PostsFilterType are accepted)
     */
    @GET("/r/{subreddit}/{filter}?sr_detail=1")
    suspend fun getPosts(
        @Path(value = "subreddit") subreddit: String,
        @Path(value = "filter") filter: String,
        @Query(value = "after") after: String
    ): PostsResponse

    /**
     * Receive posts from given [subreddit] with given [filter] (filters from PostsFilterTypeTimeframe are accepted)
     */
    @GET("/r/{subreddit}/{filter}/?sr_detail=1")
    suspend fun getPostsTimeframe(
        @Path(value = "subreddit") subreddit: String,
        @Path(value = "filter") filter: String,
        @Query(value = "t") timeframe: String,
        @Query(value = "after") after: String
    ): PostsResponse

    /**
     * Retrieves info about given [subreddit]
     */
    @GET("/r/{subreddit}/about")
    suspend fun getSubredditInfo(@Path(value = "subreddit") subreddit: String): SubredditResponse

    @Headers("Content-Type: application/x-www-form-urlencoded;charset=UTF-8")
    @POST("/api/vote/")
    suspend fun vote(@Query(value = "id") postFullname: String, @Query(value = "dir") direction: Int)


    /**
     * Get subscribed list of subreddits
     */
    @GET("/subreddits/mine/subscriber")
    suspend fun getSubscribedSubreddits(@Query(value = "after") after: String): SubscribedResponse


    /**
     * Get searched subreddits
     */
    @Headers("Content-Type: application/x-www-form-urlencoded;charset=UTF-8")
    @POST("/api/search_subreddits")
    suspend fun searchSubreddits(@Query(value = "query") searchedSubreddit: String): SearchSubredditResponse

    /**
     * Get comments from [postID]
     */
    @GET("/r/{subreddit}/comments/{postID}.json?showmore=true&showtitle=true&showmedia=true&showedits=true&sr_detail=true&limit=100")
    suspend fun getPostComments(@Path(value = "subreddit") subreddit: String, @Path(value = "postID") postID: String): Response<ResponseBody>
//    suspend fun getPostComments(@Path(value = "subreddit") subreddit: String, @Path(value = "postID") postID: String): PostCommentsResponse

    /**
     * Retrieves info about current logged user
     */
    @GET("/api/me")
    suspend fun getUserInfo(): UserResponse

}

