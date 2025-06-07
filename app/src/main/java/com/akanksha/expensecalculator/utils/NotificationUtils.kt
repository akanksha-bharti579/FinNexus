package com.akanksha.expensecalculator.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.akanksha.expensecalculator.R
import com.akanksha.expensecalculator.ui.activities.MainActivity

object NotificationUtils {
    private const val PREFERENCE_NAME = "notification_preferences"
    private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
    private const val KEY_REMINDER_TIME = "reminder_time"
    private const val KEY_BUDGET_ALERTS = "budget_alerts"
    
    // Notification channels
    const val CHANNEL_REMINDERS = "reminders"
    const val CHANNEL_BUDGET_ALERTS = "budget_alerts"
    
    // Notification IDs
    const val NOTIFICATION_ID_REMINDER = 1001
    const val NOTIFICATION_ID_BUDGET_ALERT = 1002
    
    const val CHANNEL_ID = "expense_calculator_channel"
    const val EXPENSE_REMINDER_NOTIFICATION_ID = 1
    const val RECURRING_EXPENSE_NOTIFICATION_ID = 2
    
    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
    }
    
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val reminderChannel = NotificationChannel(
                CHANNEL_REMINDERS,
                context.getString(R.string.channel_reminders_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.channel_reminders_description)
            }
            
            val budgetChannel = NotificationChannel(
                CHANNEL_BUDGET_ALERTS,
                context.getString(R.string.channel_budget_alerts_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.channel_budget_alerts_description)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannels(listOf(reminderChannel, budgetChannel))
        }
    }
    
    fun createNotificationChannel(context: Context) {
        // Create the notification channel only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Expense Calculator"
            val descriptionText = "Notifications for expense reminders and updates"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            
            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    fun showExpenseReminderNotification(
        context: Context,
        title: String,
        message: String,
        activityClass: Class<*>
    ) {
        val intent = Intent(context, activityClass).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_REMINDERS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        with(NotificationManagerCompat.from(context)) {
            notify(NOTIFICATION_ID_REMINDER, builder.build())
        }
    }
    
    fun showBudgetAlertNotification(
        context: Context,
        title: String,
        message: String,
        activityClass: Class<*>
    ) {
        val intent = Intent(context, activityClass).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_BUDGET_ALERTS)
            .setSmallIcon(R.drawable.ic_warning)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        with(NotificationManagerCompat.from(context)) {
            notify(NOTIFICATION_ID_BUDGET_ALERT, builder.build())
        }
    }
    
    fun areNotificationsEnabled(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
    }
    
    fun setNotificationsEnabled(context: Context, enabled: Boolean) {
        getPreferences(context).edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
    }
    
    fun areBudgetAlertsEnabled(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_BUDGET_ALERTS, true)
    }
    
    fun setBudgetAlertsEnabled(context: Context, enabled: Boolean) {
        getPreferences(context).edit().putBoolean(KEY_BUDGET_ALERTS, enabled).apply()
    }
    
    fun setReminderTime(context: Context, timeInMillis: Long) {
        getPreferences(context).edit().putLong(KEY_REMINDER_TIME, timeInMillis).apply()
    }
    
    fun getReminderTime(context: Context): Long {
        return getPreferences(context).getLong(KEY_REMINDER_TIME, 0)
    }

    fun createExpenseReminderNotification(context: Context, title: String, content: String): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
    }

    fun createRecurringExpenseNotification(context: Context, title: String, content: String): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
    }
} 