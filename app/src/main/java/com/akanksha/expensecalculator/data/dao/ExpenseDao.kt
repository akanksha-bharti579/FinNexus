package com.akanksha.expensecalculator.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.akanksha.expensecalculator.data.models.Expense
import com.akanksha.expensecalculator.data.models.CategoryExpense
import java.util.Date
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Dao
interface ExpenseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: Expense)

    @Update
    suspend fun update(expense: Expense)

    @Delete
    suspend fun delete(expense: Expense)

    @Query("DELETE FROM expenses")
    suspend fun deleteAllExpenses()

    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getExpenseById(id: Long): Expense?

    @Query("SELECT * FROM expenses WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getExpensesByDateRange(startDate: Date, endDate: Date): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE category = :category ORDER BY date DESC")
    fun getExpensesByCategory(category: String): Flow<List<Expense>>

    @Query("SELECT DISTINCT category FROM expenses")
    fun getAllCategories(): Flow<List<String>>

    @Query("SELECT * FROM expenses WHERE isRecurring = 1 ORDER BY date DESC")
    fun getRecurringExpenses(): Flow<List<Expense>>

    @Query("SELECT category, SUM(amount) as total FROM expenses WHERE date BETWEEN :startDate AND :endDate GROUP BY category ORDER BY total DESC")
    fun getCategoryWiseExpenses(startDate: Date, endDate: Date): Flow<List<CategoryExpense>>

    @Query("""
        SELECT * FROM expenses 
        WHERE (
            vendorName LIKE '%' || :query || '%' 
            OR itemBought LIKE '%' || :query || '%' 
            OR category LIKE '%' || :query || '%'
            OR notes LIKE '%' || :query || '%'
            OR CAST(amount as TEXT) LIKE '%' || :query || '%'
        )
        ORDER BY date DESC
    """)
    fun searchExpenses(query: String): Flow<List<Expense>>

    @Query("SELECT SUM(amount) FROM expenses WHERE date BETWEEN :startDate AND :endDate")
    fun getTotalExpenseForPeriod(startDate: Date, endDate: Date): Flow<Double?>

    @Query("SELECT AVG(amount) FROM expenses WHERE date BETWEEN :startDate AND :endDate")
    fun getAverageDailySpend(startDate: Date, endDate: Date): Flow<Double?>

    @Query("SELECT * FROM expenses WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getExpensesBetweenDates(startDate: Date, endDate: Date): Flow<List<Expense>>

    // Helper extension function to convert List<CategoryExpense> to Map<String, Double>
    fun getCategoryWiseExpensesAsMap(startDate: Date, endDate: Date): Flow<Map<String, Double>> =
        getCategoryWiseExpenses(startDate, endDate).map { list ->
            list.associate { it.category to it.total }
        }

    @Query("SELECT SUM(amount) FROM expenses WHERE date BETWEEN :startDate AND :endDate")
    fun getTotalExpensesBetweenDates(startDate: Date, endDate: Date): Flow<Double?>

    @Query("""
        SELECT * FROM expenses 
        WHERE (vendorName LIKE '%' || :query || '%' 
        OR itemBought LIKE '%' || :query || '%' 
        OR category LIKE '%' || :query || '%'
        OR CAST(amount as TEXT) LIKE '%' || :query || '%')
    """)
    fun searchExpensesComprehensive(query: String): Flow<List<Expense>>
} 