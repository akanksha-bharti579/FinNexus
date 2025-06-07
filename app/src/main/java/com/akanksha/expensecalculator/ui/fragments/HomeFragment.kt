package com.akanksha.expensecalculator.ui.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
 import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.akanksha.expensecalculator.R
import com.akanksha.expensecalculator.data.models.Expense
import com.akanksha.expensecalculator.databinding.DialogProfilePreviewBinding
import com.akanksha.expensecalculator.databinding.FragmentHomeBinding
import com.akanksha.expensecalculator.ui.adapters.ExpenseAdapter
import com.akanksha.expensecalculator.utils.CurrencyUtils
import com.akanksha.expensecalculator.utils.DateUtils
import com.akanksha.expensecalculator.utils.ThemeUtils
import com.akanksha.expensecalculator.viewmodel.ExpenseViewModel
import com.akanksha.expensecalculator.viewmodel.UserViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ExpenseViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()
    private lateinit var recentTransactionsAdapter: ExpenseAdapter
    private lateinit var topCategoriesAdapter: CategorySummaryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        CurrencyUtils.initialize(requireContext())
        
        setupUI()
        setupAdapters()
        updateGreeting()
        observeData()
        loadUserProfile()
    }

    private fun setupUI() {
        // Setup click listeners for quick actions
        binding.fabAddExpense.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_addExpense)
        }

        binding.actionAddExpense.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_addExpense)
        }

        binding.actionSetBudget.setOnClickListener {
            showBudgetDialog()
        }

        binding.actionViewStats.setOnClickListener {
            findNavController().navigate(R.id.statsFragment)
        }

        binding.actionViewHistory.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_history)
        }

        // Setup click listeners for cards
        binding.cardDailyExpenses.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_history)
        }

        binding.cardWeeklyExpenses.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_history)
        }

        binding.cardMonthlyExpenses.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_history)
        }

        // Setup view all button
        binding.textViewAll.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_history)
        }

        // Setup profile image click listener
        binding.imageProfile.setOnClickListener {
            showProfilePreviewDialog()
        }

        // Display current date
        val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
        binding.textDate.text = dateFormat.format(Date())
    }

    private fun setupAdapters() {
        // Setup recent transactions adapter
        recentTransactionsAdapter = ExpenseAdapter(
            onItemClick = { expense ->
                try {
                    val bundle = Bundle().apply {
                        putLong("expenseId", expense.id)
                    }
                    // Navigate directly to expense detail fragment
                    findNavController().navigate(R.id.expenseDetailFragment, bundle)
                } catch (e: Exception) {
                    // Log the error and show a toast
                    e.printStackTrace()
                    Toast.makeText(context, "Could not load expense details", Toast.LENGTH_SHORT).show()
                }
            }
        )
        binding.recyclerRecentTransactions.adapter = recentTransactionsAdapter

        // Setup top categories adapter
        topCategoriesAdapter = CategorySummaryAdapter()
        binding.recyclerTopCategories.adapter = topCategoriesAdapter
    }

    private fun updateGreeting() {
        val calendar = Calendar.getInstance()
        val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)

        binding.textGreeting.text = when {
            hourOfDay < 12 -> getString(R.string.good_morning)
            hourOfDay < 18 -> getString(R.string.good_afternoon)
            else -> getString(R.string.good_evening)
        }
    }

    private fun observeData() {
        // Observe daily expenses
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getDailyExpenses().collectLatest { expenses ->
                val total = expenses.sumOf { it.amount }
                binding.textDailyTotal.text = CurrencyUtils.formatAmount(total)
                binding.textDailyDate.text = getString(R.string.today)
            }
        }

        // Observe weekly expenses
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getWeeklyExpenses().collectLatest { expenses ->
                val total = expenses.sumOf { it.amount }
                binding.textWeeklyTotal.text = CurrencyUtils.formatAmount(total)
                binding.textWeeklyDate.text = getString(R.string.this_week)
            }
        }

        // Observe monthly expenses and budget
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getMonthlyExpenses().collectLatest { expenses ->
                val total = expenses.sumOf { it.amount }
                binding.textMonthlyTotal.text = CurrencyUtils.formatAmount(total)
                binding.textMonthlyDate.text = DateUtils.formatMonthYear(Date())
                
                // Update budget overview
                updateBudgetOverview(total)
            }
        }

        // Load recent transactions
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getAllExpenses().collectLatest { expenses ->
                val recentExpenses = expenses.take(5)
                recentTransactionsAdapter.submitList(recentExpenses)
            }
        }

        // Load top categories
        viewLifecycleOwner.lifecycleScope.launch {
            val expenses = viewModel.getMonthlyExpenses().first()
            updateTopCategories(expenses)
        }
    }

    private fun updateBudgetOverview(totalSpent: Double) {
        val budget = viewModel.getMonthlyBudget()
        
        // Update budget amount texts
        binding.textBudgetSpent.text = CurrencyUtils.formatAmount(totalSpent)
        binding.textBudgetTotal.text = "of ${CurrencyUtils.formatAmount(budget)}"
        
        // Update progress bar
        val progress = viewModel.getBudgetProgress(totalSpent)
        binding.progressBudget.progress = (progress * 100).toInt()
        
        // Update progress color based on budget status
        val color = when {
            progress >= 1.0 -> resources.getColor(R.color.colorError, null)
            progress >= 0.8 -> resources.getColor(R.color.colorWarning, null)
            else -> resources.getColor(R.color.colorSuccess, null)
        }
        binding.progressBudget.setIndicatorColor(color)
        
        // Calculate days left in month
        val calendar = Calendar.getInstance()
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        val daysLeft = daysInMonth - currentDay + 1
        
        binding.textDaysLeft.text = getString(R.string.days_left, daysLeft)
        
        // Calculate daily budget left
        val remainingBudget = budget - totalSpent
        val dailyBudget = if (daysLeft > 0) remainingBudget / daysLeft else 0.0
        binding.textDailyBudget.text = getString(
            R.string.daily_budget,
            CurrencyUtils.formatAmount(dailyBudget)
        )
    }

    private fun updateTopCategories(expenses: List<Expense>) {
        if (expenses.isEmpty()) {
            binding.recyclerTopCategories.visibility = View.GONE
            return
        }
        
        // Group expenses by category
        val categoryMap = expenses.groupBy { it.category }
            .mapValues { it.value.sumOf { expense -> expense.amount } }
            .toList()
            .sortedByDescending { it.second }
            .take(5)
        
        val totalAmount = expenses.sumOf { it.amount }
        
        // Create category summaries
        val categorySummaries = categoryMap.map { (category, amount) ->
            val percentage = if (totalAmount > 0) (amount / totalAmount * 100).toInt() else 0
            CategorySummary(
                category = category,
                amount = amount,
                percentage = percentage
            )
        }
        
        topCategoriesAdapter.submitList(categorySummaries)
        binding.recyclerTopCategories.visibility = View.VISIBLE
    }

    private fun showBudgetDialog() {
        val currentBudget = viewModel.getMonthlyBudget()
        
        val dialog = BudgetDialogFragment.newInstance(currentBudget) { newBudget ->
            viewModel.setMonthlyBudget(newBudget)
            // Refresh the UI
            viewLifecycleOwner.lifecycleScope.launch {
                val expenses = viewModel.getMonthlyExpenses().first()
                val total = expenses.sumOf { it.amount }
                updateBudgetOverview(total)
            }
        }
        
        dialog.show(parentFragmentManager, "BudgetDialog")
    }

    private fun showProfilePreviewDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        
        val dialogBinding = DialogProfilePreviewBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        
        // Load profile data into dialog
        val profile = userViewModel.getUserProfile()
        if (profile != null) {
            dialogBinding.textName.text = profile.name.ifEmpty { getString(R.string.app_name) }
            dialogBinding.textEmail.text = profile.email.ifEmpty { getString(R.string.profile) }
            dialogBinding.textCurrency.text = profile.currency
        }
        
        // Set theme text
        val themeMode = userViewModel.themeMode.value ?: ThemeUtils.MODE_SYSTEM
        dialogBinding.textTheme.text = when (themeMode) {
            ThemeUtils.MODE_LIGHT -> getString(R.string.theme_light)
            ThemeUtils.MODE_DARK -> getString(R.string.theme_dark)
            else -> getString(R.string.theme_system)
        }
        
        // Set biometric text
        val biometricEnabled = userViewModel.biometricEnabled.value ?: false
        dialogBinding.textBiometric.text = if (biometricEnabled) {
            getString(R.string.enabled)
        } else {
            getString(R.string.disabled)
        }
        
        // Set button click listeners
        dialogBinding.buttonViewProfile.setOnClickListener {
            dialog.dismiss()
            findNavController().navigate(R.id.profileFragment)
        }
        
        dialogBinding.buttonClose.setOnClickListener {
            dialog.dismiss()
        }
        
        dialog.show()
    }

    private fun loadUserProfile() {
        userViewModel.userProfile.observe(viewLifecycleOwner) { profile ->
            if (profile != null) {
                // Update greeting with user name if available
                if (profile.name.isNotEmpty()) {
                    val calendar = Calendar.getInstance()
                    val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
                    
                    val greeting = when {
                        hourOfDay < 12 -> getString(R.string.good_morning)
                        hourOfDay < 18 -> getString(R.string.good_afternoon)
                        else -> getString(R.string.good_evening)
                    }
                    
                    binding.textGreeting.text = "$greeting, ${profile.name}"
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    // Inner adapter class for category summaries
    inner class CategorySummaryAdapter : 
        androidx.recyclerview.widget.ListAdapter<CategorySummary, CategorySummaryAdapter.ViewHolder>(
            object : androidx.recyclerview.widget.DiffUtil.ItemCallback<CategorySummary>() {
                override fun areItemsTheSame(oldItem: CategorySummary, newItem: CategorySummary): Boolean {
                    return oldItem.category == newItem.category
                }
                
                override fun areContentsTheSame(oldItem: CategorySummary, newItem: CategorySummary): Boolean {
                    return oldItem == newItem
                }
            }
        ) {
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_category_summary, parent, false)
            return ViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(getItem(position))
        }
        
        inner class ViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
            private val textCategory = itemView.findViewById<android.widget.TextView>(R.id.text_category)
            private val textPercentage = itemView.findViewById<android.widget.TextView>(R.id.text_percentage)
            private val textAmount = itemView.findViewById<android.widget.TextView>(R.id.text_amount)
            private val progressBar = itemView.findViewById<android.widget.ProgressBar>(R.id.progress_bar)
            private val imageCategory = itemView.findViewById<android.widget.ImageView>(R.id.image_category)
            
            fun bind(item: CategorySummary) {
                textCategory.text = item.category
                textPercentage.text = "${item.percentage}% of total"
                textAmount.text = CurrencyUtils.formatAmount(item.amount)
                progressBar.progress = item.percentage
                
                // Set category icon based on category name
                val iconResId = getCategoryIconResId(item.category)
                imageCategory.setImageResource(iconResId)
            }
            
            private fun getCategoryIconResId(category: String): Int {
                return when (category.lowercase()) {
                    "food" -> R.drawable.ic_food
                    "travel" -> R.drawable.ic_travel
                    "shopping" -> R.drawable.ic_shopping
                    "entertainment" -> R.drawable.ic_entertainment
                    "bills" -> R.drawable.ic_bills
                    "health" -> R.drawable.ic_health
                    "education" -> R.drawable.ic_education
                    else -> R.drawable.ic_misc
                }
            }
        }
    }
    
    // Data class for category summary
    data class CategorySummary(
        val category: String,
        val amount: Double,
        val percentage: Int
    )
} 