package com.akanksha.expensecalculator

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.akanksha.expensecalculator.utils.NotificationUtils
import com.akanksha.expensecalculator.utils.ThemeUtils
import com.akanksha.expensecalculator.workers.DailyReminderWorker
import com.akanksha.expensecalculator.workers.RecurringExpenseWorker
import java.util.concurrent.TimeUnit

class ExpenseCalculatorApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize theme
        AppCompatDelegate.setDefaultNightMode(ThemeUtils.getThemeMode(this))

        // Create notification channel
        NotificationUtils.createNotificationChannel(this)

        // Schedule daily reminder worker
        scheduleDailyReminder()

        // Schedule recurring expense worker
        scheduleRecurringExpenseCheck()
    }

    private fun scheduleDailyReminder() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val reminderWorkRequest = PeriodicWorkRequestBuilder<DailyReminderWorker>(
            1, TimeUnit.DAYS
        )
        .setConstraints(constraints)
        .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "daily_reminder",
            ExistingPeriodicWorkPolicy.KEEP,
            reminderWorkRequest
        )
    }

    private fun scheduleRecurringExpenseCheck() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val recurringExpenseWorkRequest = PeriodicWorkRequestBuilder<RecurringExpenseWorker>(
            1, TimeUnit.DAYS
        )
        .setConstraints(constraints)
        .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "recurring_expense_check",
            ExistingPeriodicWorkPolicy.KEEP,
            recurringExpenseWorkRequest
        )
    }
} 