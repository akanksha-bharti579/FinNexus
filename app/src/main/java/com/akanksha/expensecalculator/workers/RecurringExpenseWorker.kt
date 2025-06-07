package com.akanksha.expensecalculator.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.akanksha.expensecalculator.data.database.AppDatabase
import com.akanksha.expensecalculator.data.models.Expense
import com.akanksha.expensecalculator.data.models.RecurringType
import com.akanksha.expensecalculator.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.first
import java.util.Calendar
import java.util.Date

class RecurringExpenseWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val repository: ExpenseRepository
    
    init {
        val expenseDao = AppDatabase.getDatabase(context).expenseDao()
        repository = ExpenseRepository(expenseDao)
    }

    override suspend fun doWork(): Result {
        try {
            val recurringExpenses = repository.getRecurringExpenses().first()
            val today = Calendar.getInstance()

            recurringExpenses.forEach { expense ->
                if (shouldAddExpenseToday(expense, today)) {
                    val newExpense = expense.copy(
                        id = 0,
                        date = today.time
                    )
                    repository.insert(newExpense)
                }
            }

            return Result.success()
        } catch (e: Exception) {
            return Result.failure()
        }
    }

    private fun shouldAddExpenseToday(expense: Expense, today: Calendar): Boolean {
        val expenseDate = Calendar.getInstance().apply { time = expense.date }

        return when (expense.recurringType) {
            RecurringType.DAILY -> true
            
            RecurringType.WEEKLY -> 
                today.get(Calendar.DAY_OF_WEEK) == expenseDate.get(Calendar.DAY_OF_WEEK)
            
            RecurringType.MONTHLY -> 
                today.get(Calendar.DAY_OF_MONTH) == expenseDate.get(Calendar.DAY_OF_MONTH)
            
            null -> false
        }
    }
} 