package com.akanksha.expensecalculator.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.akanksha.expensecalculator.R
import com.akanksha.expensecalculator.utils.CurrencyUtils

/**
 * Adapter for displaying tag-based expense summaries with progress bars
 */
class TagExpenseSummaryAdapter(
    private val items: List<TagExpenseSummary>
) : RecyclerView.Adapter<TagExpenseSummaryAdapter.ViewHolder>() {

    private val maxAmount: Double = items.maxOfOrNull { it.amount } ?: 0.0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tag_expense_summary, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item, maxAmount)
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textTag: TextView = itemView.findViewById(R.id.text_tag)
        private val textAmount: TextView = itemView.findViewById(R.id.text_amount)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progress_bar)

        fun bind(item: TagExpenseSummary, maxAmount: Double) {
            textTag.text = item.tag
            textAmount.text = CurrencyUtils.formatAmount(item.amount)
            
            // Calculate progress percentage
            val progressPercentage = if (maxAmount > 0) {
                (item.amount / maxAmount * 100).toInt()
            } else {
                0
            }
            
            progressBar.progress = progressPercentage
        }
    }
    
    /**
     * Data class for tag expense summary
     */
    data class TagExpenseSummary(val tag: String, val amount: Double)
} 