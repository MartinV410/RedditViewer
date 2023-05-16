package com.example.redditviewer.data

import android.content.Context
import android.util.Log
import com.example.redditviewer.network.PostsApiService
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.kirkbushman.auth.RedditAuth
import com.kirkbushman.auth.managers.SharedPrefsStorageManager
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

/**
 * Interceptor to add auth token to requests
 */
class AuthInterceptor(val authClient: RedditAuth) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()

        try{
            val token = authClient.getSavedBearer().getAccessToken()?: ""
            requestBuilder.addHeader("Authorization", "Bearer $token")
        } catch (e: Exception) {
            Log.e("AuthInterceptor", "Cannot retrieve access token: ${e.stackTrace}")
        }


        return chain.proceed(requestBuilder.build())
    }
}

interface AppContainer {
    val postsRepository: PostsRepository
    val authClient: RedditAuth
}

/**
 * Main app container
 */
class DefaultAppContainer(context: Context): AppContainer {
    private val BASE_URL = "https://oauth.reddit.com/"

    // auth client for reddit oauth2
    override val authClient: RedditAuth by lazy {
        RedditAuth.Builder()// TODO hide key!
            .setApplicationCredentials("",  "http://localhost:8000")
            .setScopes(arrayOf("identity", "read", "vote", "subscribe", "mysubreddits"))
            .setStorageManager(SharedPrefsStorageManager(context))
            .build()
    }

    // clients for API calls
    private fun okhttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC)
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(AuthInterceptor(authClient))
            .build()
    }

    private val json = Json {
        coerceInputValues = true
        ignoreUnknownKeys = true
    }
    // retrofit instance for making API calls
    private val retrofit = Retrofit.Builder()
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .client(okhttpClient())
        //    .addConverterFactory(GsonConverterFactory.create(postDeserializer))
        .baseUrl(BASE_URL)
        .build()

    private val retrofitServicePosts : PostsApiService by lazy {
        retrofit.create(PostsApiService::class.java)
    }

    override val postsRepository: PostsRepository by lazy {
        DefaultPostsRepository(retrofitServicePosts)
    }

}