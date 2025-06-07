package com.akanksha.expensecalculator.ui.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.akanksha.expensecalculator.R
import com.akanksha.expensecalculator.data.models.Expense
import com.akanksha.expensecalculator.data.models.ExpenseCategory
import com.akanksha.expensecalculator.data.models.RecurringType
import com.akanksha.expensecalculator.databinding.FragmentAddExpenseBinding
import com.akanksha.expensecalculator.utils.DateUtils
import com.akanksha.expensecalculator.utils.TagsManager
import com.akanksha.expensecalculator.viewmodel.ExpenseViewModel
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class AddExpenseFragment : Fragment() {
    private var _binding: FragmentAddExpenseBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ExpenseViewModel by viewModels()
    private var selectedDate: Date = Date()
    private lateinit var tagsManager: TagsManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddExpenseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tagsManager = TagsManager.getInstance(requireContext())
        setupUI()
    }

    private fun setupUI() {
        // Set current date by default
        updateDateDisplay()
        
        binding.buttonDatePicker.setOnClickListener {
            showDatePicker()
        }
        
        // Setup category spinner
        val categories = ExpenseCategory.values().map { it.displayName }
        val categoryAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categories
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.spinnerCategory.setAdapter(categoryAdapter)
        binding.spinnerCategory.setText(categories[0], false)
        
        // Setup recurring type spinner
        val recurringTypes = RecurringType.values().map { it.name }
        val recurringAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            recurringTypes
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.spinnerRecurringType.setAdapter(recurringAdapter)
        binding.spinnerRecurringType.setText(recurringTypes[0], false)
        
        // Handle recurring checkbox
        binding.checkboxRecurring.setOnCheckedChangeListener { _, isChecked ->
            binding.layoutRecurringType.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
        
        binding.buttonSave.setOnClickListener {
            saveExpense()
        }
        
        binding.buttonCancel.setOnClickListener {
            findNavController().navigateUp()
        }
    }
    
    private fun updateDateDisplay() {
        binding.textDate.text = DateUtils.formatDate(selectedDate)
    }
    
    private fun showDatePicker() {
        val calendar = Calendar.getInstance().apply { time = selectedDate }
        
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                selectedDate = calendar.time
                updateDateDisplay()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
    
    private fun saveExpense() {
        val amount = binding.editAmount.text.toString().toDoubleOrNull()
        if (amount == null || amount <= 0) {
            Toast.makeText(requireContext(), "Please enter a valid amount", Toast.LENGTH_SHORT).show()
            return
        }
        
        val vendorName = binding.editVendor.text.toString().trim()
        if (vendorName.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a vendor name", Toast.LENGTH_SHORT).show()
            return
        }

        val itemBought = binding.editDescription.text.toString().trim()
        if (itemBought.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter what you bought", Toast.LENGTH_SHORT).show()
            return
        }
        
        val category = binding.spinnerCategory.text.toString()
        val isRecurring = binding.checkboxRecurring.isChecked
        val recurringType = if (isRecurring) {
            RecurringType.valueOf(binding.spinnerRecurringType.text.toString())
        } else null
        
        // Get tags from TagsInputView
        val tags = binding.tagsInput.getTags()
        
        // Save tags to TagsManager
        viewLifecycleOwner.lifecycleScope.launch {
            tagsManager.addTags(tags)
        }
        
        // Get notes from NotesEditorView
        val notes = binding.notesEditor.getNoteText()
        
        val expense = Expense(
            id = 0, // Auto-generated
            vendorName = vendorName,
            itemBought = itemBought,
            amount = amount,
            date = selectedDate,
            category = category,
            isRecurring = isRecurring,
            recurringType = recurringType,
            notes = if (notes.isNotEmpty()) notes.toString() else null,
            tags = tags
        )
        
        viewModel.insert(expense)
        findNavController().navigateUp()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 