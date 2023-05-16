package com.example.redditviewer.binding

import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.example.redditviewer.R
import com.example.redditviewer.getPostContentType
import com.example.redditviewer.loadImage
import com.example.redditviewer.loadSubredditIcon
import com.example.redditviewer.network.PostContentType
import com.example.redditviewer.network.PostData
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView

/**
 * Sets icon on ShapeableImageView
 */
@BindingAdapter("iconUrl")
fun setIcon(view: ShapeableImageView, iconUrl: String?) {
    if(iconUrl == null) return
    loadSubredditIcon(view, iconUrl, imageView = view, context = view.context)
}


/**
 * Shows text based on required content type
 */
@BindingAdapter("postData", "text", "requiredPostContentType")
fun setTextContentType(view: TextView, postData: PostData, text: String, requiredPostContentType: PostContentType) {
    val contentType: PostContentType = getPostContentType(postData)

    if(contentType == requiredPostContentType) {
        view.visibility = View.VISIBLE
        view.text = text
    } else {
        view.visibility = View.GONE
        view.text = ""
    }
}


/**
 * Shows text based on if text is not empty
 */
@BindingAdapter("text")
fun setText(view: TextView, text: String) {
    if(text.isNotEmpty()) {
        view.visibility = View.VISIBLE
        view.text = text
    } else {
        view.visibility = View.GONE
        view.text = ""
    }
}

/**
 * Sets post content image
 */
@BindingAdapter("postData", "imageUrl", "requiredPostContentType")
fun setContentImage(view: ShapeableImageView, postData: PostData, imageUrl: String, requiredPostContentType: PostContentType) {
    val contentType: PostContentType = getPostContentType(postData)

    if(contentType == requiredPostContentType) {
        view.visibility = View.VISIBLE
        loadImage(view, imageUrl, imageView = view, context = view.context)
    } else {
        view.visibility = View.GONE
        view.setImageDrawable(null)
    }
}

///**
// * Share url with android sharesheet
// */
//@BindingAdapter("onShare")
//fun MaterialButton.onShare(destUrl: String) {
//    this.setOnClickListener {
//        // sharesheet of android
//        val sendIntent: Intent = Intent().apply {
//            action = Intent.ACTION_SEND
//            putExtra(Intent.EXTRA_TEXT, "https://reddit.com$destUrl")
//            type = "text/plain"
//        }
//
//        val shareIntent = Intent.createChooser(sendIntent, null)
//        this.context.startActivity(shareIntent)
//    }
//}

/**
 * Set main post image content visibility
 */
@BindingAdapter("setImageContentVisible")
fun ConstraintLayout.setImageContentVisible(postData: PostData) {
    val content = getPostContentType(postData)
    if(content == PostContentType.IMAGE_GALERY || content == PostContentType.IMAGE) {
        visibility = View.VISIBLE
        return
    }
    visibility= View.GONE
}

/**
 * Set main post link content visibility
 */
@BindingAdapter("setLinkContentVisible")
fun ConstraintLayout.setLinkContentVisible(postData: PostData) {
    val content = getPostContentType(postData)
    if(content == PostContentType.LINK) {
        visibility = View.VISIBLE
        return
    }
    visibility= View.GONE
}

/**
 * Set vote button background tint based on [voted]
 */
@BindingAdapter("voteTint")
fun MaterialButton.voteTint(voted: Boolean?) {
    when(voted) {
        true -> this.backgroundTintList = ContextCompat.getColorStateList(this.context, R.color.orange)
        false -> this.backgroundTintList = ContextCompat.getColorStateList(this.context, R.color.blue)
        null -> this.backgroundTintList = ContextCompat.getColorStateList(this.context, R.color.gray_2)
    }
}

