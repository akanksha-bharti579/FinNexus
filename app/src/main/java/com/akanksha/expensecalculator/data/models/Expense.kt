package com.akanksha.expensecalculator.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val vendorName: String,
    val itemBought: String,
    val amount: Double,
    val date: Date,
    val category: String,
    val isRecurring: Boolean = false,
    val recurringType: RecurringType? = null,
    val notes: String? = null,
    val tags: List<String> = emptyList()
)

enum class RecurringType {
    DAILY, WEEKLY, MONTHLY
}

enum class ExpenseCategory(val displayName: String) {
    FOOD("Food"),
    TRAVEL("Travel"),
    SHOPPING("Shopping"),
    ENTERTAINMENT("Entertainment"),
    BILLS("Bills"),
    HEALTH("Health"),
    EDUCATION("Education"),
    OTHER("Other")
} 