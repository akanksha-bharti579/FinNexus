package com.akanksha.expensecalculator.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.akanksha.expensecalculator.R
import com.akanksha.expensecalculator.data.models.Expense
import com.akanksha.expensecalculator.databinding.ItemExpenseBinding
import com.akanksha.expensecalculator.utils.CurrencyUtils
import com.akanksha.expensecalculator.utils.DateUtils
import com.google.android.material.chip.Chip

class ExpenseAdapter(
    private val onItemClick: ((Expense) -> Unit)? = null
) : ListAdapter<Expense, ExpenseAdapter.ExpenseViewHolder>(ExpenseDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val binding = ItemExpenseBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ExpenseViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ExpenseViewHolder(
        private val binding: ItemExpenseBinding,
        private val onItemClick: ((Expense) -> Unit)?
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(expense: Expense) {
            binding.apply {
                textVendor.text = expense.vendorName
                textDescription.text = expense.itemBought
                textDate.text = DateUtils.formatDate(expense.date)
                textAmount.text = CurrencyUtils.formatAmount(expense.amount)
                chipCategory.text = expense.category
                
                // Set click listener
                root.setOnClickListener {
                    onItemClick?.invoke(expense)
                }
                
                // Display tags
                chipGroupTags.removeAllViews()
                if (expense.tags.isNotEmpty()) {
                    for (tag in expense.tags) {
                        val chip = createTagChip(tag)
                        chipGroupTags.addView(chip)
                    }
                }
                
                // Display notes preview if available
                if (!expense.notes.isNullOrEmpty()) {
                    textNotesPreview.text = expense.notes
                    textNotesPreview.visibility = View.VISIBLE
                } else {
                    textNotesPreview.visibility = View.GONE
                }
            }
        }
        
        private fun createTagChip(tag: String): Chip {
            return Chip(binding.root.context).apply {
                text = tag
                setChipBackgroundColorResource(R.color.colorAccent)
                setTextColor(ContextCompat.getColor(context, R.color.white))
                isCheckable = false
                textSize = 10f
                chipMinHeight = 24f
            }
        }
    }

    private class ExpenseDiffCallback : DiffUtil.ItemCallback<Expense>() {
        override fun areItemsTheSame(oldItem: Expense, newItem: Expense): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Expense, newItem: Expense): Boolean {
            return oldItem == newItem
        }
    }
} 