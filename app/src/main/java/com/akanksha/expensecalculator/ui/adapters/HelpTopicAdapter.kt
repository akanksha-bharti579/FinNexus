package com.akanksha.expensecalculator.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.akanksha.expensecalculator.R
import com.akanksha.expensecalculator.data.model.HelpTopic

class HelpTopicAdapter : ListAdapter<HelpTopic, HelpTopicAdapter.HelpTopicViewHolder>(HelpTopicDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HelpTopicViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_help_topic, parent, false)
        return HelpTopicViewHolder(view)
    }

    override fun onBindViewHolder(holder: HelpTopicViewHolder, position: Int) {
        val helpTopic = getItem(position)
        holder.bind(helpTopic)
    }

    inner class HelpTopicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.help_topic_title)
        private val contentTextView: TextView = itemView.findViewById(R.id.help_topic_content)
        private val expandIcon: ImageView = itemView.findViewById(R.id.expand_icon)

        fun bind(helpTopic: HelpTopic) {
            titleTextView.text = helpTopic.title
            contentTextView.text = helpTopic.content
            
            // Set the initial expanded state
            updateExpandedState(helpTopic.isExpanded)
            
            // Set click listener for the entire item
            itemView.setOnClickListener {
                // Toggle expanded state
                helpTopic.isExpanded = !helpTopic.isExpanded
                updateExpandedState(helpTopic.isExpanded)
            }
        }
        
        private fun updateExpandedState(isExpanded: Boolean) {
            if (isExpanded) {
                contentTextView.visibility = View.VISIBLE
                expandIcon.setImageResource(R.drawable.ic_expand_less)
            } else {
                contentTextView.visibility = View.GONE
                expandIcon.setImageResource(R.drawable.ic_expand_more)
            }
        }
    }

    private class HelpTopicDiffCallback : DiffUtil.ItemCallback<HelpTopic>() {
        override fun areItemsTheSame(oldItem: HelpTopic, newItem: HelpTopic): Boolean {
            return oldItem.title == newItem.title
        }

        override fun areContentsTheSame(oldItem: HelpTopic, newItem: HelpTopic): Boolean {
            return oldItem.content == newItem.content && oldItem.isExpanded == newItem.isExpanded
        }
    }
} 