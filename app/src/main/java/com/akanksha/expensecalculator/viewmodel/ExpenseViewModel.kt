package com.akanksha.expensecalculator.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.akanksha.expensecalculator.data.database.AppDatabase
import com.akanksha.expensecalculator.data.models.Expense
import com.akanksha.expensecalculator.data.repository.ExpenseRepository
import com.akanksha.expensecalculator.utils.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ExpenseRepository
    private val _monthlyBudget = MutableStateFlow(0.0)
    val monthlyBudget: StateFlow<Double> = _monthlyBudget
    private val sharedPreferences = application.getSharedPreferences("expense_preferences", Application.MODE_PRIVATE)

    init {
        val expenseDao = AppDatabase.getDatabase(application).expenseDao()
        repository = ExpenseRepository(expenseDao)
        _monthlyBudget.value = getMonthlyBudget()
    }

    // Budget related functions
    fun setMonthlyBudget(budget: Double) {
        _monthlyBudget.value = budget
        sharedPreferences.edit().putFloat("monthly_budget", budget.toFloat()).apply()
    }

    fun getMonthlyBudget(): Double {
        return sharedPreferences.getFloat("monthly_budget", 0f).toDouble()
    }

    fun getBudgetProgress(monthlyTotal: Double): Float {
        val budget = getMonthlyBudget()
        return if (budget <= 0) 0f else (monthlyTotal / budget).toFloat()
    }

    // Expense related functions
    fun getAllExpenses(): Flow<List<Expense>> {
        return repository.getAllExpenses()
    }

    fun getDailyExpenses(date: Date = Date()): Flow<List<Expense>> {
        return repository.getDailyExpenses(date)
    }

    fun getWeeklyExpenses(date: Date = Date()): Flow<List<Expense>> {
        return repository.getWeeklyExpenses(date)
    }

    fun getMonthlyExpenses(date: Date = Date()): Flow<List<Expense>> {
        return repository.getMonthlyExpenses(date)
    }

    fun getExpensesByCategory(category: String): Flow<List<Expense>> {
        return repository.getExpensesByCategory(category)
    }

    fun getCategoryWiseExpenses(): Flow<Map<String, Double>> {
        val startOfMonth = DateUtils.getStartOfMonth()
        val endOfMonth = DateUtils.getEndOfMonth()
        return repository.getCategoryWiseExpenses(startOfMonth, endOfMonth)
    }

    fun getMonthlyTrends(): Flow<List<Pair<String, Double>>> {
        // Get last 6 months of data
        val endDate = Date()
        val calendar = Calendar.getInstance()
        calendar.time = endDate
        calendar.add(Calendar.MONTH, -5)
        val startDate = calendar.time
        
        return repository.getAllExpenses().map { expenses ->
            val monthlyData = mutableListOf<Pair<String, Double>>()
            
            // Group expenses by month
            for (i in 0 until 6) {
                calendar.time = startDate
                calendar.add(Calendar.MONTH, i)
                
                val monthStart = DateUtils.getStartOfMonth(calendar.time)
                val monthEnd = DateUtils.getEndOfMonth(calendar.time)
                val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())
                val monthLabel = monthFormat.format(calendar.time)
                
                // Calculate total for this month
                val monthTotal = expenses
                    .filter { it.date in monthStart..monthEnd }
                    .sumOf { it.amount }
                
                monthlyData.add(monthLabel to monthTotal)
            }
            
            monthlyData
        }
    }

    fun insert(expense: Expense) {
        viewModelScope.launch {
            repository.insert(expense)
        }
    }

    fun update(expense: Expense) {
        viewModelScope.launch {
            repository.update(expense)
        }
    }

    fun delete(expense: Expense) {
        viewModelScope.launch {
            repository.delete(expense)
        }
    }

    fun deleteAllExpenses() {
        viewModelScope.launch {
            repository.deleteAllExpenses()
        }
    }

    suspend fun getExpenseById(id: Long): Expense? {
        return repository.getExpenseById(id)
    }

    fun getAllCategories(): Flow<List<String>> {
        return repository.getAllCategories()
    }

    fun searchExpenses(query: String): Flow<List<Expense>> = 
        repository.searchExpenses(query)

    // Analytics operations
    fun getTotalExpenseForPeriod(startDate: Date, endDate: Date): Flow<Double> = 
        repository.getTotalExpenseForPeriod(startDate, endDate)

    fun getRecurringExpenses(): Flow<List<Expense>> = 
        repository.getRecurringExpenses()

    fun getAverageDailySpend(startDate: Date, endDate: Date): Flow<Double> = 
        repository.getAverageDailySpend(startDate, endDate)

    fun isOverBudget(currentMonthExpenses: Double): Boolean {
        return currentMonthExpenses > _monthlyBudget.value
    }

    fun isNearingBudget(currentMonthExpenses: Double): Boolean {
        return currentMonthExpenses >= (_monthlyBudget.value * 0.8)
    }
} 