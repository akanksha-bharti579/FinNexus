package com.akanksha.expensecalculator.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.akanksha.expensecalculator.R
import com.akanksha.expensecalculator.databinding.FragmentHistoryBinding
import com.akanksha.expensecalculator.ui.adapters.ExpenseAdapter
import com.akanksha.expensecalculator.utils.CurrencyUtils
import com.akanksha.expensecalculator.utils.TagsManager
import com.akanksha.expensecalculator.viewmodel.ExpenseViewModel
import com.google.android.material.chip.Chip
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Date

class HistoryFragment : Fragment() {
    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ExpenseViewModel by viewModels()
    private lateinit var expenseAdapter: ExpenseAdapter
    private lateinit var tagsManager: TagsManager
    
    // Currently selected tag filter
    private var selectedTag: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tagsManager = TagsManager.getInstance(requireContext())
        setupUI()
        observeData()
        setupTagFilters()
    }

    private fun setupUI() {
        // Initialize adapter
        expenseAdapter = ExpenseAdapter(
            onItemClick = { expense ->
                try {
                    val bundle = Bundle().apply {
                        putLong("expenseId", expense.id)
                    }
                    findNavController().navigate(R.id.action_history_to_expenseDetail, bundle)
                } catch (e: Exception) {
                    // Log the error and show a toast
                    e.printStackTrace()
                    Toast.makeText(context, "Could not load expense details", Toast.LENGTH_SHORT).show()
                }
            }
        )
        binding.recyclerExpenses.adapter = expenseAdapter

        // Setup chip group listeners - using individual chip click listeners instead
        binding.chipAll.setOnClickListener { observeAllExpenses() }
        binding.chipToday.setOnClickListener { observeDailyExpenses() }
        binding.chipWeek.setOnClickListener { observeWeeklyExpenses() }
        binding.chipMonth.setOnClickListener { observeMonthlyExpenses() }
        
        // Setup search functionality
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
                    searchExpenses(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    // Reset to current filter
                    when {
                        binding.chipAll.isChecked -> observeAllExpenses()
                        binding.chipToday.isChecked -> observeDailyExpenses()
                        binding.chipWeek.isChecked -> observeWeeklyExpenses()
                        binding.chipMonth.isChecked -> observeMonthlyExpenses()
                    }
                }
                return true
            }
        })
        
        // Clear tag filter button
        binding.buttonClearTagFilter.setOnClickListener {
            clearTagFilter()
        }
    }
    
    private fun setupTagFilters() {
        viewLifecycleOwner.lifecycleScope.launch {
            val popularTags = tagsManager.getPopularTags(5)
            binding.chipGroupTags.removeAllViews()
            
            for (tag in popularTags) {
                val chip = createFilterChip(tag)
                binding.chipGroupTags.addView(chip)
            }
            
            updateTagFilterVisibility()
        }
    }
    
    private fun createFilterChip(tag: String): Chip {
        return Chip(requireContext()).apply {
            text = tag
            isCheckable = true
            
            // Use the non-deprecated OnClickListener instead
            setOnClickListener {
                if (isChecked) {
                    selectedTag = tag
                    filterByTag(tag)
                } else if (selectedTag == tag) {
                    selectedTag = null
                    // Reset to current filter
                    when {
                        binding.chipAll.isChecked -> observeAllExpenses()
                        binding.chipToday.isChecked -> observeDailyExpenses()
                        binding.chipWeek.isChecked -> observeWeeklyExpenses()
                        binding.chipMonth.isChecked -> observeMonthlyExpenses()
                    }
                }
                updateTagFilterVisibility()
            }
        }
    }
    
    private fun updateTagFilterVisibility() {
        binding.buttonClearTagFilter.visibility = if (selectedTag != null) View.VISIBLE else View.GONE
        binding.textActiveFilter.visibility = if (selectedTag != null) View.VISIBLE else View.GONE
        binding.textActiveFilter.text = selectedTag?.let { "Filtered by tag: $it" } ?: ""
    }
    
    private fun clearTagFilter() {
        selectedTag = null
        
        // Uncheck all tag chips
        for (i in 0 until binding.chipGroupTags.childCount) {
            val chip = binding.chipGroupTags.getChildAt(i) as? Chip
            chip?.isChecked = false
        }
        
        // Reset to current filter
        when {
            binding.chipAll.isChecked -> observeAllExpenses()
            binding.chipToday.isChecked -> observeDailyExpenses()
            binding.chipWeek.isChecked -> observeWeeklyExpenses()
            binding.chipMonth.isChecked -> observeMonthlyExpenses()
        }
        
        updateTagFilterVisibility()
    }

    private fun observeData() {
        // Start with all expenses
        observeAllExpenses()
    }

    private fun observeAllExpenses() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getAllExpenses().collectLatest { expenses ->
                val filteredExpenses = if (selectedTag != null) {
                    expenses.filter { it.tags.contains(selectedTag) }
                } else {
                    expenses
                }
                expenseAdapter.submitList(filteredExpenses)
                updateTotal(filteredExpenses.sumOf { it.amount })
            }
        }
    }

    private fun observeDailyExpenses() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getDailyExpenses(Date()).collectLatest { expenses ->
                val filteredExpenses = if (selectedTag != null) {
                    expenses.filter { it.tags.contains(selectedTag) }
                } else {
                    expenses
                }
                expenseAdapter.submitList(filteredExpenses)
                updateTotal(filteredExpenses.sumOf { it.amount })
            }
        }
    }

    private fun observeWeeklyExpenses() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getWeeklyExpenses(Date()).collectLatest { expenses ->
                val filteredExpenses = if (selectedTag != null) {
                    expenses.filter { it.tags.contains(selectedTag) }
                } else {
                    expenses
                }
                expenseAdapter.submitList(filteredExpenses)
                updateTotal(filteredExpenses.sumOf { it.amount })
            }
        }
    }

    private fun observeMonthlyExpenses() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getMonthlyExpenses(Date()).collectLatest { expenses ->
                val filteredExpenses = if (selectedTag != null) {
                    expenses.filter { it.tags.contains(selectedTag) }
                } else {
                    expenses
                }
                expenseAdapter.submitList(filteredExpenses)
                updateTotal(filteredExpenses.sumOf { it.amount })
            }
        }
    }
    
    private fun searchExpenses(query: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.searchExpenses(query).collectLatest { expenses ->
                val filteredExpenses = if (selectedTag != null) {
                    expenses.filter { it.tags.contains(selectedTag) }
                } else {
                    expenses
                }
                expenseAdapter.submitList(filteredExpenses)
                updateTotal(filteredExpenses.sumOf { it.amount })
            }
        }
    }
    
    private fun filterByTag(tag: String) {
        when {
            binding.chipAll.isChecked -> observeAllExpenses()
            binding.chipToday.isChecked -> observeDailyExpenses()
            binding.chipWeek.isChecked -> observeWeeklyExpenses()
            binding.chipMonth.isChecked -> observeMonthlyExpenses()
        }
    }

    private fun updateTotal(total: Double) {
        binding.textTotal.text = CurrencyUtils.formatAmount(total)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 