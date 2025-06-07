package com.akanksha.expensecalculator.data.repository

import com.akanksha.expensecalculator.data.dao.ExpenseDao
import com.akanksha.expensecalculator.data.models.Expense
import com.akanksha.expensecalculator.utils.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date

class ExpenseRepository(private val expenseDao: ExpenseDao) {
    
    fun getAllExpenses(): Flow<List<Expense>> = expenseDao.getAllExpenses()
    
    fun getDailyExpenses(date: Date = Date()): Flow<List<Expense>> = 
        expenseDao.getExpensesByDateRange(
            DateUtils.getStartOfDay(date),
            DateUtils.getEndOfDay(date)
        )
    
    fun getWeeklyExpenses(date: Date = Date()): Flow<List<Expense>> = 
        expenseDao.getExpensesByDateRange(
            DateUtils.getStartOfWeek(date),
            DateUtils.getEndOfWeek(date)
        )
    
    fun getMonthlyExpenses(date: Date = Date()): Flow<List<Expense>> = 
        expenseDao.getExpensesByDateRange(
            DateUtils.getStartOfMonth(date),
            DateUtils.getEndOfMonth(date)
        )
    
    fun getExpensesByCategory(category: String): Flow<List<Expense>> =
        expenseDao.getExpensesByCategory(category)
    
    fun getCategoryWiseExpenses(startDate: Date = DateUtils.getStartOfMonth(), 
                               endDate: Date = DateUtils.getEndOfMonth()): Flow<Map<String, Double>> =
        expenseDao.getCategoryWiseExpenses(startDate, endDate).map { categoryExpenses ->
            categoryExpenses.associate { it.category to it.total }
        }
    
    suspend fun insert(expense: Expense) = expenseDao.insert(expense)
    
    suspend fun update(expense: Expense) = expenseDao.update(expense)
    
    suspend fun delete(expense: Expense) = expenseDao.delete(expense)
    
    suspend fun deleteAllExpenses() = expenseDao.deleteAllExpenses()
    
    suspend fun getExpenseById(id: Long): Expense? = expenseDao.getExpenseById(id)
    
    fun getAllCategories(): Flow<List<String>> = expenseDao.getAllCategories()
    
    fun searchExpenses(query: String): Flow<List<Expense>> = expenseDao.searchExpenses(query)
    
    fun getTotalExpenseForPeriod(startDate: Date, endDate: Date): Flow<Double> =
        expenseDao.getTotalExpenseForPeriod(startDate, endDate).map { it ?: 0.0 }
    
    fun getRecurringExpenses(): Flow<List<Expense>> = expenseDao.getRecurringExpenses()
    
    fun getAverageDailySpend(startDate: Date, endDate: Date): Flow<Double> =
        expenseDao.getAverageDailySpend(startDate, endDate).map { it ?: 0.0 }
} 