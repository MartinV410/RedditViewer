package com.example.redditviewer.binding

import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.example.redditviewer.R
import com.example.redditviewer.loadImage
import com.example.redditviewer.loadSubredditIcon
import com.example.redditviewer.network.PostsFilter
import com.example.redditviewer.network.PostsFilterQuery
import com.example.redditviewer.network.PostsTimeframe
import com.example.redditviewer.view.SubredditUiState
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView

/**
 * Sets filter context (image, text) of a filter button in subreddit fragment
 */
@BindingAdapter("setFilterContent")
fun MaterialButton.setFilterContent(filter: PostsFilterQuery) {
    val filterText: Int = when(filter.filter) {
        PostsFilter.HOT -> R.string.hot
        PostsFilter.NEW -> R.string.new_s
        PostsFilter.RISING -> R.string.rising
        PostsFilter.TOP -> R.string.top
        PostsFilter.CONTROVERSIAL -> R.string.controversial
    }
    val timeframeText: Int = when(filter.timeframe) {
        PostsTimeframe.HOUR -> R.string.hour
        PostsTimeframe.DAY -> R.string.day
        PostsTimeframe.WEEK -> R.string.week
        PostsTimeframe.MONTH -> R.string.month
        PostsTimeframe.YEAR -> R.string.year
        PostsTimeframe.ALL -> R.string.all
        PostsTimeframe.NONE -> R.string.empty
    }
    text = if(filter.timeframe != PostsTimeframe.NONE) {
        "${context.getString(filterText)} (${context.getString(timeframeText)})"
    } else {
        context.getString(filterText)
    }

    val filterIcon: Int = when(filter.filter) {
        PostsFilter.HOT -> R.drawable.outline_local_fire_department_24
        PostsFilter.NEW -> R.drawable.baseline_star_border_24
        PostsFilter.RISING -> R.drawable.baseline_show_chart_24
        PostsFilter.TOP -> R.drawable.baseline_bar_chart_24
        PostsFilter.CONTROVERSIAL -> R.drawable.outline_contrast_24
    }

    icon = context.getDrawable(filterIcon)
}

/**
 * Sets content of join button in subreddit fragment
 */
@BindingAdapter("setJoin")
fun MaterialButton.setJoin(state: SubredditUiState) {
    if(state !is SubredditUiState.Success) {
        visibility = View.GONE
        return
    }

    val subreddit = state.subreddit
    if(subreddit.global) {
        visibility = View.GONE
        return
    }
    if(subreddit.subscribed) {
        backgroundTintList = context.resources.getColorStateList(R.color.green)
        text = context.resources.getString(R.string.joined)
        icon = context.resources.getDrawable(R.drawable.baseline_check_24)
    } else {
        backgroundTintList = context.resources.getColorStateList(R.color.gray_2)
        text = context.resources.getString(R.string.join)
        icon = context.resources.getDrawable(R.drawable.outline_add_24)
    }
    visibility = View.VISIBLE
}

/**
 * Sets about text in subreddit fragment
 */
@BindingAdapter("setAboutText")
fun TextView.setAboutText(state: SubredditUiState) {
    if(state !is SubredditUiState.Success) return

    text = state.subreddit.publicDescription
}

/**
 * Sets following text in subreddit fragment
 */
@BindingAdapter("setFollowingText")
fun TextView.setFollowingText(state: SubredditUiState) {
    if(state !is SubredditUiState.Success || state.subreddit.global){
        visibility = View.GONE
        return
    }

    visibility = View.VISIBLE
    text = state.subreddit.subscribers.toString()
}

/**
 * Sets subreddit name in subreddit fragment
 */
@BindingAdapter("setSubredditName")
fun TextView.setSubredditName(state: SubredditUiState) {
    if(state !is SubredditUiState.Success) {
        return
    }

//    setBackgroundColor(resources.getColor(android.R.color.transparent, null))
    text = state.subreddit.namePrefixed
}

/**
 * Sets subreddit icon in subreddit fragment
 */
@BindingAdapter("setSubredditIcon")
fun ShapeableImageView.setSubredditIcon(state: SubredditUiState) {
    if(state !is SubredditUiState.Success) {
        setImageResource(R.drawable.placeholder)
        return
    }
    val subreddit = state.subreddit

    when (subreddit.global) {
        true -> loadSubredditIcon(this, iconID = subreddit.iconID, imageView = this, context = this.context)
        false -> loadSubredditIcon(this, iconUrl = subreddit.iconUrl, imageView = this, context = this.context)
    }
}

/**
 * Set subreddit banner img
 */
@BindingAdapter("setSubredditBanner")
fun ShapeableImageView.setSubredditBanner(state: SubredditUiState) {
    if(state !is SubredditUiState.Success) {
        this.setImageDrawable(null)
        this.foreground = null
        return
    }
    val subreddit = state.subreddit

    when (subreddit.global) {
        true -> {
            this.setImageDrawable(null)
            this.foreground = null
        }
        false -> {
            loadImage(this, imageUrl = subreddit.bannerImgUrl, imageView = this, context = this.context)
//            foreground = context.resources.getDrawable(R.drawable.fade_bottom)
        }
    }
}

/**
 * Set subscribers count visibility
 */
@BindingAdapter("setSubscribersVisibility")
fun ShapeableImageView.setSubscribersVisibility(state: SubredditUiState) {
    if(state !is SubredditUiState.Success)return

    val subreddit = state.subreddit

    visibility = when (subreddit.global) {
        true -> View.GONE
        false -> View.VISIBLE
    }
}