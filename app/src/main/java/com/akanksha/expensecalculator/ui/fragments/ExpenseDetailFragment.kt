package com.akanksha.expensecalculator.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.akanksha.expensecalculator.R
import com.akanksha.expensecalculator.data.models.Expense
import com.akanksha.expensecalculator.databinding.FragmentExpenseDetailBinding
import com.akanksha.expensecalculator.utils.CurrencyUtils
import com.akanksha.expensecalculator.utils.DateUtils
import com.akanksha.expensecalculator.viewmodel.ExpenseViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class ExpenseDetailFragment : Fragment() {
    private var _binding: FragmentExpenseDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ExpenseViewModel by viewModels()
    private var expenseId: Long = -1L

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExpenseDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Get expense ID from arguments
        arguments?.let {
            expenseId = it.getLong("expenseId", -1L)
        }
        
        if (expenseId == -1L) {
            // Invalid expense ID, navigate back
            findNavController().navigateUp()
            return
        }
        
        setupUI()
        loadExpenseDetails()
    }
    
    private fun setupUI() {
        // Setup back button
        binding.buttonBack.setOnClickListener {
            findNavController().navigateUp()
        }
        
        // Setup edit button
        binding.buttonEdit.setOnClickListener {
            val bundle = Bundle().apply {
                putLong("expenseId", expenseId)
                putBoolean("isEditing", true)
            }
            findNavController().navigate(R.id.addExpenseFragment, bundle)
        }
        
        // Setup delete button
        binding.buttonDelete.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }
    
    private fun loadExpenseDetails() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val expense = viewModel.getExpenseById(expenseId)
                if (expense != null) {
                    // Populate UI with expense details
                    binding.apply {
                        textAmount.text = CurrencyUtils.formatAmount(expense.amount)
                        textVendor.text = expense.vendorName
                        textDescription.text = expense.itemBought
                        textCategory.text = expense.category
                        textDate.text = DateUtils.formatDate(expense.date)
                        
                        // Show notes if available
                        if (!expense.notes.isNullOrEmpty()) {
                            textNotes.text = expense.notes
                            cardNotes.visibility = View.VISIBLE
                        } else {
                            cardNotes.visibility = View.GONE
                        }
                        
                        // Add tags
                        chipGroupTags.removeAllViews()
                        if (expense.tags.isNotEmpty()) {
                            for (tag in expense.tags) {
                                val chip = createTagChip(tag)
                                chipGroupTags.addView(chip)
                            }
                            cardTags.visibility = View.VISIBLE
                        } else {
                            cardTags.visibility = View.GONE
                        }
                    }
                } else {
                    // Expense not found, navigate back
                    findNavController().navigateUp()
                }
            } catch (e: Exception) {
                // Handle error and navigate back
                findNavController().navigateUp()
            }
        }
    }
    
    private fun createTagChip(tag: String): Chip {
        return Chip(requireContext()).apply {
            text = tag
            isCheckable = false
            isClickable = false
        }
    }
    
    private fun showDeleteConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_expense)
            .setMessage(R.string.delete_expense_confirmation)
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(R.string.delete) { _, _ ->
                deleteExpense()
            }
            .show()
    }
    
    private fun deleteExpense() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val expense = viewModel.getExpenseById(expenseId)
                if (expense != null) {
                    viewModel.delete(expense)
                }
                findNavController().navigateUp()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 