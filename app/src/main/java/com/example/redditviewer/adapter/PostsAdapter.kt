package com.example.redditviewer.adapter

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.redditviewer.PostItemClickInterface
import com.example.redditviewer.databinding.PostItemBinding
import com.example.redditviewer.network.Post
import com.example.redditviewer.setPostItemContentImage

/**
 * Adapter for posts items in subreddit fragment
 */
class PostsAdapter(private val onClickListener: PostItemClickInterface): ListAdapter<Post, PostsAdapter.ViewHolder>(
    PostsAdapterDiffCallback()
){

    class PostsAdapterDiffCallback: DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem.data.id == newItem.data.id
        }

        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem == newItem
        }

        override fun getChangePayload(oldItem: Post, newItem: Post): Any? {
            return if (oldItem.data.score != newItem.data.score) true else null
        }

    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = getItem(position)
        holder.itemView.layout(0,0,0,0) // reset layout so glide wont load incorrect size after reusing recycle item
        holder.bind(post)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent, onClickListener)
    }


    /**
     * ViewHolder for post_item layout
     */
    class ViewHolder private constructor(val binding: PostItemBinding, val onClickListener: PostItemClickInterface) : RecyclerView.ViewHolder(binding.root){

        /**
         * Bind data to view
         */
        fun bind(post: Post) {
            binding.post = post.data
            binding.executePendingBindings()

            setPostItemContentImage(post.data, binding, onClickListener)

            binding.postItemShare.setOnClickListener {
                onClickListener.onShareClick(post.data.shareUrl)
            }

            binding.postItemComments.setOnClickListener {
                onClickListener.onMessageClick(post)
            }

            binding.postItemVote.setOnTouchListener(object : View.OnTouchListener {
                override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                    if (event?.action == MotionEvent.ACTION_UP) {
                        onClickListener.onVoteClick(
                            post,
                            binding.postItemVote,
                            event
                        )
                    }
                    return v?.onTouchEvent(event) ?: true
                }

            })

            binding.postItemLayoutLink.setOnClickListener {
                onClickListener.onLinkClick(post.data.urlDest)
            }

            binding.postItemSubredditIcon.setOnClickListener {
                onClickListener.onIconClick(post.data.subreddit.name)
            }
        }

        /**
         * Return inflated layout for post_item
         */
        companion object {
            fun from(parent: ViewGroup, onClickListener: PostItemClickInterface): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = PostItemBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding, onClickListener)
            }
        }
    }

}