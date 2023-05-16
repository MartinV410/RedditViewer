package com.example.redditviewer.data

import com.example.redditviewer.network.Comment
import com.example.redditviewer.network.CommentsResponse
import com.example.redditviewer.network.CommentsResponseData
import com.example.redditviewer.network.PostCommentsResponse
import com.example.redditviewer.network.PostsApiService
import com.example.redditviewer.network.PostsFilter
import com.example.redditviewer.network.PostsResponse
import com.example.redditviewer.network.PostsTimeframe
import com.example.redditviewer.network.SearchSubredditResponse
import com.example.redditviewer.network.SubredditResponse
import com.example.redditviewer.network.SubscribeAction
import com.example.redditviewer.network.SubscribedResponse
import com.example.redditviewer.network.UserResponse
import com.example.redditviewer.network.VoteDirection
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import org.json.JSONArray
import org.json.JSONObject


/**
 * Repository for fetching posts
 */
interface PostsRepository {
    suspend fun getPosts(subreddit: String, filter: PostsFilter, after: String): PostsResponse
    suspend fun getPostsTimeframe(subreddit: String, filter: PostsFilter, timeframe: PostsTimeframe, after: String): PostsResponse
    suspend fun getPostsFrontpage(filter: PostsFilter, after: String): PostsResponse
    suspend fun vote(postFullname: String, direction: VoteDirection)
    suspend fun getSubredditInfo(subreddit: String): SubredditResponse
    suspend fun subscribeToSubreddit(action: SubscribeAction, subreddit: String)
    suspend fun getSubscribedSubreddits(after: String): SubscribedResponse
    suspend fun searchSubreddits(searchedSubreddit: String): SearchSubredditResponse
    suspend fun getPostComments(subreddit: String, postID: String): PostCommentsResponse?
    suspend fun getUserInfo(): UserResponse
}


/**
 * Default repository for posts API
 */
class DefaultPostsRepository(private val postsApiService: PostsApiService): PostsRepository {
    override suspend fun getPosts(subreddit: String, filter: PostsFilter, after: String): PostsResponse {
        return postsApiService.getPosts(subreddit, filter.value, after)
    }

    override suspend fun getPostsTimeframe(subreddit: String, filter: PostsFilter, timeframe: PostsTimeframe, after: String): PostsResponse {
        return postsApiService.getPostsTimeframe(subreddit, filter.value, timeframe.value, after)
    }

    override suspend fun getPostsFrontpage(filter: PostsFilter, after: String): PostsResponse {
        return postsApiService.getPostsFrontpage(filter.value, after)
    }

    override suspend fun vote(postFullname: String, direction: VoteDirection) {
        return postsApiService.vote(postFullname, direction.dir)
    }
    override suspend fun getSubredditInfo(subreddit: String): SubredditResponse {
        return postsApiService.getSubredditInfo(subreddit)
    }

    override suspend fun subscribeToSubreddit(action: SubscribeAction, subreddit: String) {
        return postsApiService.subscribeToSubreddit(action.action, subreddit)
    }

    override suspend fun getSubscribedSubreddits(after: String): SubscribedResponse {
        return postsApiService.getSubscribedSubreddits(after)
    }

    override suspend fun searchSubreddits(searchedSubreddit: String): SearchSubredditResponse {
        return postsApiService.searchSubreddits(searchedSubreddit)
    }

    override suspend fun getPostComments(subreddit: String, postID: String): PostCommentsResponse? {
//        return postsApiService.getPostComments(subreddit, postID)
        val response = postsApiService.getPostComments(subreddit, postID)

        if(response.isSuccessful) {
            response.body()?.string()?.let {
                val jsonArray = JSONArray(it)
                val jsonParser = Json{
                    coerceInputValues = true
                    ignoreUnknownKeys = true
                }

                val commentJson = JSONObject(jsonArray[1].toString())
                val commentDataJson = commentJson.getJSONObject("data")
                val childrens = commentDataJson.getJSONArray("children")
                val comments: MutableList<Comment> = mutableListOf()
                for(i in 0 until childrens.length()) {
                    val jsonString = childrens[i].toString()
                    if(JSONObject(jsonString).get("kind") == "t1") {
                        comments.add(jsonParser.decodeFromString(jsonString))
                    }
                }
                val postResponse = jsonParser.decodeFromString<PostsResponse>(jsonArray[0].toString())
                val commentResponse = CommentsResponse(
                    kind = commentJson.get("kind").toString(),
                    data = CommentsResponseData(
                        after = commentDataJson.get("after").toString(),
                        dist = commentDataJson.get("dist").toString(),
                        modhash = commentDataJson.get("modhash").toString(),
                        geoFilter = commentDataJson.get("geo_filter").toString(),
                        children = comments
                    )
                )
//                val commentResponse = jsonParser.decodeFromString<CommentsResponse>(jsonArray[1].toString())
                return PostCommentsResponse(post = postResponse, comment = commentResponse)
            }
        }
        return null
    }

    override suspend fun getUserInfo(): UserResponse {
        return postsApiService.getUserInfo()
    }
}