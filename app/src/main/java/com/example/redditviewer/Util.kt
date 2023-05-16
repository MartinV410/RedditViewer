package com.example.redditviewer

import android.content.Context
import android.graphics.Rect
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.redditviewer.adapter.ImageSlideAdapter
import com.example.redditviewer.databinding.PostItemBinding
import com.example.redditviewer.network.PostContentType
import com.example.redditviewer.network.PostData
import com.example.redditviewer.network.VoteDirection
import com.google.android.material.button.MaterialButton


sealed interface GenericUIState {
    object Success : GenericUIState
    data class Error(val message: String) : GenericUIState
    object Loading : GenericUIState
}

/**
 * Loads icon from [iconUrl] or [iconID] to [imageView]. If loading failed, replace icon with default
 * subreddit icon drawable
 */
fun loadSubredditIcon(view: View, iconUrl: String = "", iconID: Int = -1, imageView: ImageView, context: Context) {

    Glide.with(view)
        .load(if(iconID != -1) iconID else iconUrl)
        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
        .transition(DrawableTransitionOptions.withCrossFade())
        .error(R.drawable.subreddit_icon_placeholder_foreground)
        .fitCenter()
        .into(imageView)
}

/**
 * Loads image from [imageUrl] or [imageID] to [imageView]. Add loading animation when fetching the image
 */
fun loadImage(view: View, imageUrl: String = "", imageID: Int = -1, imageView: ImageView, context: Context) {
    // create a ProgressDrawable object which we will show as placeholder
    val drawable = CircularProgressDrawable(context).apply {
        setColorSchemeColors(R.color.red)
        centerRadius = 30f
        strokeWidth = 5f
    }

    drawable.start()

    Glide.with(view)
        .load(if(imageID != -1) imageID else imageUrl)
        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
        .transition(DrawableTransitionOptions.withCrossFade())
        .placeholder(drawable)
        .fitCenter()
        .into(imageView)

}

/**
 * Get subreddit post main [PostContentType] from post [data].
 */
fun getPostContentType(data: PostData) : PostContentType {
    val urlDestSplit = data.urlDest.split(".")
    val urlDestEnd: String = urlDestSplit[urlDestSplit.size - 1]

//    if(urlDestSplit.size >= 4) urlDestEnd = urlDestSplit[3]

    val supportedImageTypes = listOf<String>("png", "jpg", "jpeg")
    val supportedGifTypes = listOf<String>("gif", "gifv")

    // CHECK IF IMAGE_GALLERY
    if(data.galleryData != null) return PostContentType.IMAGE_GALERY

    // CHECK IF IMAGE
    if(urlDestEnd in supportedImageTypes) {
        return PostContentType.IMAGE
    }

    // CHECK IF LINK
    if(data.postHint == "link" && urlDestEnd !in supportedGifTypes && urlDestEnd !in supportedImageTypes) {
        return PostContentType.LINK
    }

    // VIDEO
    if(urlDestEnd in supportedGifTypes || data.postHint == "hosted:video") {
        return PostContentType.VIDEO
    }

    return PostContentType.UNSUPPORTED
}

/**
 * Set subreddit post main content image/images from [postData] and set [binding] viewpager adapter.
 */
fun setPostItemContentImage(postData: PostData, binding: PostItemBinding, onClickListener: PostItemClickInterface) {
    // set image or images of post based on content type
    val contentType = getPostContentType(postData)
    if(contentType == PostContentType.IMAGE || contentType == PostContentType.IMAGE_GALERY) {
        val images = getPostItemImages(postData)
        val onClick: () -> Unit = {onClickListener.onImageClick(postData)}
        val viewPagerAdapter = ImageSlideAdapter(binding.postItemImageLayout.context, images, onClick)
        binding.postItemViewpager.adapter = viewPagerAdapter
        binding.postItemIndicator.setViewPager(binding.postItemViewpager)
    }
}

/**
 * Get subreddit post main content image/images from [postData] (if [PostContentType] is IMAGE | IMAGE_GALERY)
 */
fun getPostItemImages(postData: PostData): ArrayList<String> {
    val images = ArrayList<String>()

    if(getPostContentType(postData) != PostContentType.IMAGE && getPostContentType(postData) != PostContentType.IMAGE_GALERY) return images

    if(postData.galleryData != null) {
        postData.galleryData.let {
            for(image in it.items) {
                images.add("https://i.redd.it/${image.mediaID}.jpg")
            }
        }
    } else {
        images.add(postData.urlDest)
    }

    return images
}


/**
 * Possible button click positions
 */
enum class ButtonClickPosition{
    LEFT,
    RIGHT,
    NONE
}

/**
 * Retrieves [ButtonClickPosition] based on [button] and [event] click positions.
 */
fun getButtonClickPosition(button: MaterialButton, event: MotionEvent): ButtonClickPosition {
    val rectangle: Rect = Rect()
    button.getDrawingRect(rectangle)

    val leftRect: Rect = Rect(rectangle.left, rectangle.top, rectangle.right / 2, rectangle.bottom)
    val rightRect: Rect = Rect(rectangle.left + (rectangle.right / 2), rectangle.top, rectangle.right, rectangle.bottom)

    if(leftRect.contains(event.x.toInt(), event.y.toInt())) {
        return ButtonClickPosition.LEFT
    }
    if(rightRect.contains(event.x.toInt(), event.y.toInt())) {
        return ButtonClickPosition.RIGHT
    }
    return ButtonClickPosition.NONE
}

/**
 * Data representing post/comment vote actions
 * [voted] - null(no vote), true(up-voted), false(down-voted)
 */
data class VoteActionData(
    val voted: Boolean?,
    val num: Int,
    val direction: VoteDirection
)

/**
 * Retrieves [VoteActionData] based on current [direction] and [voted]. This allows to un-vote if
 * previously voted, down-vote if previously up-voted....
 */
fun voteAction(direction: VoteDirection, voted: Boolean?): VoteActionData{
    var newVoted: Boolean? = null
    var num = 0
    var newDirection = VoteDirection.UNVOTE
    when (direction) {
        VoteDirection.UPVOTE -> {
            when(voted) {
                true -> {
                    num = -1
                    newVoted = null
                    newDirection = VoteDirection.UNVOTE
                }
                false -> {
                    num = 2
                    newVoted = true
                    newDirection = VoteDirection.UPVOTE
                }
                null -> {
                    num = 1
                    newVoted = true
                    newDirection = VoteDirection.UPVOTE
                }
            }
        }
        VoteDirection.DOWNVOTE -> {
            when(voted) {
                true -> {
                    num = -2
                    newVoted = false
                    newDirection = VoteDirection.DOWNVOTE
                }
                false -> {
                    num = 1
                    newVoted = null
                    newDirection = VoteDirection.UNVOTE
                }
                null -> {
                    num = -1
                    newVoted = false
                    newDirection = VoteDirection.DOWNVOTE
                }
            }
        }
        VoteDirection.UNVOTE -> {}
    }

    return VoteActionData(newVoted, num, newDirection)
}