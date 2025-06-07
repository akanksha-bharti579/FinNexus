package com.akanksha.expensecalculator.utils

import android.content.Context
import android.graphics.Color
import androidx.core.content.ContextCompat
import com.akanksha.expensecalculator.R
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object CurrencyUtils {
    private var locale = Locale.getDefault()
    private var currencyCode = "INR" // Changed to INR as default
    private var context: Context? = null
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
    
    fun initialize(appContext: Context) {
        context = appContext.applicationContext
    }
    
    fun setCurrency(code: String) {
        currencyCode = code
    }
    
    fun setLocale(newLocale: Locale) {
        locale = newLocale
    }
    
    fun formatAmount(amount: Double): String {
        return currencyFormat.format(amount)
    }
    
    fun getColorForBudgetProgress(progress: Double): Int {
        val ctx = context ?: return Color.GRAY
        return when {
            progress < 0.5 -> ContextCompat.getColor(ctx, R.color.budget_safe)
            progress < 0.8 -> ContextCompat.getColor(ctx, R.color.budget_warning)
            else -> ContextCompat.getColor(ctx, R.color.budget_danger)
        }
    }
    
    fun getCurrencySymbol(): String {
        return Currency.getInstance(currencyCode).symbol
    }
    
    fun parseAmount(amountString: String): Double {
        return try {
            // Remove currency symbol and commas
            val cleanString = amountString
                .replace(currencyFormat.currency?.symbol ?: "", "")
                .replace(",", "")
                .trim()
            
            cleanString.toDoubleOrNull() ?: 0.0
        } catch (e: Exception) {
            0.0
        }
    }
    
    fun calculatePercentage(amount: Double, total: Double): Double {
        if (total <= 0) return 0.0
        return (amount / total) * 100
    }

    fun formatAmountWithoutSymbol(amount: Double): String {
        return formatAmount(amount).replace(currencyFormat.currency?.symbol ?: "", "").trim()
    }

    fun formatPercentage(percentage: Double): String {
        return String.format("%.1f%%", percentage)
    }

    fun roundToTwoDecimals(amount: Double): Double {
        return String.format("%.2f", amount).toDouble()
    }

    fun isValidAmount(amount: String): Boolean {
        return try {
            val value = amount.toDouble()
            value > 0 && value <= 999999999.99 // Reasonable maximum limit
        } catch (e: NumberFormatException) {
            false
        }
    }
} 