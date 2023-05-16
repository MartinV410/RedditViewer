package com.example.redditviewer.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.redditviewer.SearchSubredditsClickInterface
import com.example.redditviewer.databinding.SearchedSubredditItemBinding
import com.example.redditviewer.network.SearchedSubreddit

/**
 * Adapter for searched subreddits items
 */
class SearchedSubredditAdapter(private val onClickListener: SearchSubredditsClickInterface): ListAdapter<SearchedSubreddit, SearchedSubredditAdapter.ViewHolder>(SearchedSubredditAdapterDiffCallback()){

    class SearchedSubredditAdapterDiffCallback: DiffUtil.ItemCallback<SearchedSubreddit>() {
        override fun areItemsTheSame(oldItem: SearchedSubreddit, newItem: SearchedSubreddit): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: SearchedSubreddit, newItem: SearchedSubreddit): Boolean {
            return oldItem == newItem
        }

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val searchData = getItem(position)
        holder.bind(searchData)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent, onClickListener)
    }



    /**
     * ViewHolder for search_item layout
     */
    class ViewHolder private constructor(val binding: SearchedSubredditItemBinding, val onClickListener: SearchSubredditsClickInterface) : RecyclerView.ViewHolder(binding.root){

        /**
         * Bind data to view
         */
        fun bind(searchedData: SearchedSubreddit) {
            binding.searchedSubreddit = searchedData
            binding.executePendingBindings()

//            binding.searchItemUnsubscribe.setOnClickListener {
//                onClickListener.onDeleteClick(bindingAdapterPosition)
//            }

            binding.searchItemLayout.setOnClickListener {
                onClickListener.onLayoutClick(searchedData.name)
            }

        }

        /**
         * Return inflated layout for search_item
         */
        companion object {
            fun from(parent: ViewGroup, onClickListener: SearchSubredditsClickInterface): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = SearchedSubredditItemBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding, onClickListener)
            }
        }
    }

}