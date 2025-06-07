package com.akanksha.expensecalculator.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.akanksha.expensecalculator.ui.activities.MainActivity
import com.akanksha.expensecalculator.utils.NotificationUtils

class DailyReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        NotificationUtils.showExpenseReminderNotification(
            context,
            "Daily Expense Reminder",
            "Don't forget to log your expenses for today!",
            MainActivity::class.java
        )
        return Result.success()
    }
} 