package com.akanksha.expensecalculator.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.akanksha.expensecalculator.R
import com.akanksha.expensecalculator.data.model.Customer

class CustomersAdapter(
    private val onCustomerClick: (Customer) -> Unit,
    private val onCustomerOptionsClick: (Customer, View) -> Unit
) : ListAdapter<Customer, CustomersAdapter.CustomerViewHolder>(CustomerDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_customer, parent, false)
        return CustomerViewHolder(view)
    }

    override fun onBindViewHolder(holder: CustomerViewHolder, position: Int) {
        val customer = getItem(position)
        holder.bind(customer, onCustomerClick, onCustomerOptionsClick)
    }

    class CustomerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.customer_name)
        private val emailTextView: TextView = itemView.findViewById(R.id.customer_email)
        private val phoneTextView: TextView = itemView.findViewById(R.id.customer_phone)
        private val transactionCountTextView: TextView = itemView.findViewById(R.id.transaction_count)
        private val optionsButton: ImageButton = itemView.findViewById(R.id.customer_options)

        fun bind(
            customer: Customer,
            onCustomerClick: (Customer) -> Unit,
            onCustomerOptionsClick: (Customer, View) -> Unit
        ) {
            nameTextView.text = customer.name
            emailTextView.text = customer.email
            phoneTextView.text = customer.phone
            
            // Placeholder for transaction count - in a real app this would come from database
            transactionCountTextView.text = itemView.context.getString(
                R.string.placeholder_transactions, 
                (0..20).random()
            )

            itemView.setOnClickListener {
                onCustomerClick(customer)
            }

            optionsButton.setOnClickListener {
                onCustomerOptionsClick(customer, it)
            }
        }
    }

    private class CustomerDiffCallback : DiffUtil.ItemCallback<Customer>() {
        override fun areItemsTheSame(oldItem: Customer, newItem: Customer): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Customer, newItem: Customer): Boolean {
            return oldItem == newItem
        }
    }
} 