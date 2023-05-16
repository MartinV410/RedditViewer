package com.example.redditviewer.adapter

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.redditviewer.CommentItemClickInterface
import com.example.redditviewer.databinding.CommentItemBinding
import com.example.redditviewer.network.Comment

/**
 * Adapter for comment items of an post
 */
class CommentsAdapter(private val onClickListener: CommentItemClickInterface): ListAdapter<Comment, CommentsAdapter.ViewHolder>(
    CommentsAdapterDiffCallback()
){

    class CommentsAdapterDiffCallback: DiffUtil.ItemCallback<Comment>() {
        override fun areItemsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            return oldItem.data.id == newItem.data.id
        }

        override fun areContentsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            return oldItem == newItem
        }

        override fun getChangePayload(oldItem: Comment, newItem: Comment): Any? {
            return if (oldItem.data.score != newItem.data.score) true else null
        }

    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent, onClickListener)
    }


    /**
     * ViewHolder for post_item layout
     */
    class ViewHolder private constructor(val binding: CommentItemBinding, val onClickListener: CommentItemClickInterface) : RecyclerView.ViewHolder(binding.root){

        /**
         * Bind data to view
         */
        fun bind(comment: Comment) {
            binding.commentData = comment.data
            binding.executePendingBindings()

            binding.commentItemShare.setOnClickListener {
                onClickListener.onShareClick(comment.data.permalink)
            }

            binding.commentItemVote.setOnTouchListener(object : View.OnTouchListener {
                override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                    if (event?.action == MotionEvent.ACTION_UP) {
                        onClickListener.onVoteClick(
                            comment,
                            binding.commentItemVote,
                            event
                        )
                    }
                    return v?.onTouchEvent(event) ?: true
                }

            })

        }

        /**
         * Return inflated layout for post_item
         */
        companion object {
            fun from(parent: ViewGroup, onClickListener: CommentItemClickInterface): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = CommentItemBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding, onClickListener)
            }
        }
    }

}