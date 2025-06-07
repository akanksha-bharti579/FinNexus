package com.akanksha.expensecalculator.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.akanksha.expensecalculator.data.models.Expense
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportUtils {
    private const val FILE_PROVIDER_AUTHORITY = "com.akanksha.expensecalculator.fileprovider"
    
    fun exportExpensesToCsv(context: Context, expenses: List<Expense>): Uri? {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "expenses_$timestamp.csv"
        
        try {
            val file = File(context.filesDir, fileName)
            FileOutputStream(file).use { fos ->
                // Write header
                fos.write("Date,Amount,Vendor,Item,Category,Recurring,Tags,Notes\n".toByteArray())
                
                // Write data
                for (expense in expenses) {
                    val line = "${DateUtils.formatDate(expense.date)}," +
                            "${expense.amount}," +
                            "\"${expense.vendorName}\"," +
                            "\"${expense.itemBought}\"," +
                            "\"${expense.category}\"," +
                            "${if (expense.isRecurring) "Yes" else "No"}," +
                            "\"${expense.tags.joinToString(",")}\"," +
                            "\"${expense.notes ?: ""}\"\n"
                    
                    fos.write(line.toByteArray())
                }
            }
            
            return FileProvider.getUriForFile(context, FILE_PROVIDER_AUTHORITY, file)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
    
    fun shareExpensesCsv(context: Context, uri: Uri) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_SUBJECT, "Expense Calculator Data")
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        context.startActivity(Intent.createChooser(intent, "Share Expenses"))
    }
    
    fun exportExpensesAndShare(context: Context, expenses: List<Expense>) {
        val uri = exportExpensesToCsv(context, expenses)
        uri?.let { shareExpensesCsv(context, it) }
    }
} 