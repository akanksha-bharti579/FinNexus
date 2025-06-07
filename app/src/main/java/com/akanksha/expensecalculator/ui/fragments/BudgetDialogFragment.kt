package com.akanksha.expensecalculator.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.akanksha.expensecalculator.R
import com.akanksha.expensecalculator.databinding.DialogBudgetBinding
import com.akanksha.expensecalculator.utils.CurrencyUtils

class BudgetDialogFragment : DialogFragment() {
    private var _binding: DialogBudgetBinding? = null
    private val binding get() = _binding!!
    
    private var currentBudget: Double = 0.0
    private var onBudgetSet: ((Double) -> Unit)? = null
    
    companion object {
        private const val ARG_BUDGET = "budget"
        
        fun newInstance(currentBudget: Double, onBudgetSet: (Double) -> Unit): BudgetDialogFragment {
            val fragment = BudgetDialogFragment()
            fragment.currentBudget = currentBudget
            fragment.onBudgetSet = onBudgetSet
            
            val args = Bundle()
            args.putDouble(ARG_BUDGET, currentBudget)
            fragment.arguments = args
            
            return fragment
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_ExpenseCalculator_Dialog_FullWidth)
        
        arguments?.let {
            currentBudget = it.getDouble(ARG_BUDGET, 0.0)
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogBudgetBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Set dialog title
        dialog?.setTitle(R.string.set_budget)
        
        // Setup UI
        setupUI()
        
        // Show keyboard automatically
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
    }
    
    private fun setupUI() {
        // Set current budget value
        if (currentBudget > 0) {
            binding.editBudget.setText(CurrencyUtils.formatAmountWithoutSymbol(currentBudget))
        }
        
        // Add text change listener for formatting
        binding.editBudget.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {
                if (s.toString().isNotEmpty()) {
                    binding.buttonSave.isEnabled = true
                } else {
                    binding.buttonSave.isEnabled = false
                }
            }
        })
        
        // Setup save button
        binding.buttonSave.setOnClickListener {
            val budgetText = binding.editBudget.text.toString()
            val budget = CurrencyUtils.parseAmount(budgetText)
            
            onBudgetSet?.invoke(budget)
            dismiss()
        }
        
        // Setup cancel button
        binding.buttonCancel.setOnClickListener {
            dismiss()
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 