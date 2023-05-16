package com.example.redditviewer.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.redditviewer.SubscribedItemClickInterface
import com.example.redditviewer.databinding.SubscribedItemBinding
import com.example.redditviewer.network.Subscribed
import com.example.redditviewer.network.SubscribedChildren

/**
 * Adapter for subscribed subreddits items
 */
class SubscribedAdapter(private val onClickListener: SubscribedItemClickInterface): ListAdapter<SubscribedChildren, SubscribedAdapter.ViewHolder>(SubscribedAdapterDiffCallback()){

    class SubscribedAdapterDiffCallback: DiffUtil.ItemCallback<SubscribedChildren>() {
        override fun areItemsTheSame(oldItem: SubscribedChildren, newItem: SubscribedChildren): Boolean {
            return oldItem.data == newItem.data
        }

        override fun areContentsTheSame(oldItem: SubscribedChildren, newItem: SubscribedChildren): Boolean {
            return oldItem == newItem
        }

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val searchData = getItem(position).data
        holder.bind(searchData)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent, onClickListener)
    }



    /**
     * ViewHolder for search_item layout
     */
    class ViewHolder private constructor(val binding: SubscribedItemBinding, val onClickListener: SubscribedItemClickInterface) : RecyclerView.ViewHolder(binding.root){

        /**
         * Bind data to view
         */
        fun bind(subscribedData: Subscribed) {
            binding.subscribed = subscribedData
            binding.executePendingBindings()

//            binding.searchItemUnsubscribe.setOnClickListener {
//                onClickListener.onDeleteClick(bindingAdapterPosition)
//            }

            binding.searchItemLayout.setOnClickListener {
                onClickListener.onLayoutClick(subscribedData.name)
            }

        }

        /**
         * Return inflated layout for search_item
         */
        companion object {
            fun from(parent: ViewGroup, onClickListener: SubscribedItemClickInterface): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = SubscribedItemBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding, onClickListener)
            }
        }
    }

}