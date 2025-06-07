package com.akanksha.expensecalculator.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    private val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault())

    fun formatDate(date: Date): String = dateFormat.format(date)
    
    fun formatMonthYear(date: Date): String = monthYearFormat.format(date)
    
    fun getDayOfWeek(date: Date): String = dayFormat.format(date)

    fun parseDate(dateStr: String): Date = dateFormat.parse(dateStr) ?: Date()

    fun getStartOfDay(date: Date = Date()): Date {
        return Calendar.getInstance().apply {
            time = date
            setStartOfDay(this)
        }.time
    }

    fun getEndOfDay(date: Date = Date()): Date {
        return Calendar.getInstance().apply {
            time = date
            setEndOfDay(this)
        }.time
    }

    fun setStartOfDay(calendar: Calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
    }

    fun setEndOfDay(calendar: Calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
    }

    fun getStartOfWeek(date: Date = Date()): Date {
        return Calendar.getInstance().apply {
            time = date
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            setStartOfDay(this)
        }.time
    }

    fun getEndOfWeek(date: Date = Date()): Date {
        return Calendar.getInstance().apply {
            time = date
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            add(Calendar.DAY_OF_WEEK, 6)
            setEndOfDay(this)
        }.time
    }

    fun getStartOfMonth(date: Date = Date()): Date {
        return Calendar.getInstance().apply {
            time = date
            set(Calendar.DAY_OF_MONTH, 1)
            setStartOfDay(this)
        }.time
    }

    fun getEndOfMonth(date: Date = Date()): Date {
        return Calendar.getInstance().apply {
            time = date
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            setEndOfDay(this)
        }.time
    }

    fun isToday(date: Date): Boolean {
        val today = Calendar.getInstance()
        val calendar = Calendar.getInstance().apply { time = date }
        
        return today.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR)
    }

    fun isThisWeek(date: Date): Boolean {
        val today = Calendar.getInstance()
        val calendar = Calendar.getInstance().apply { time = date }
        
        return today.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                today.get(Calendar.WEEK_OF_YEAR) == calendar.get(Calendar.WEEK_OF_YEAR)
    }

    fun isThisMonth(date: Date): Boolean {
        val today = Calendar.getInstance()
        val calendar = Calendar.getInstance().apply { time = date }
        
        return today.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                today.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)
    }
} 