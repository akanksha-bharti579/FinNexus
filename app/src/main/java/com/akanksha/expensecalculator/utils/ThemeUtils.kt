package com.akanksha.expensecalculator.utils

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import com.akanksha.expensecalculator.R

object ThemeUtils {
    private const val PREFERENCE_NAME = "theme_preferences"
    private const val KEY_THEME_MODE = "theme_mode"
    
    // Theme modes
    const val MODE_LIGHT = 1
    const val MODE_DARK = 2
    const val MODE_SYSTEM = 0
    
    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
    }
    
    fun applyTheme(activity: Activity) {
        val themeMode = getThemeMode(activity)
        when (themeMode) {
            MODE_LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            MODE_DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            MODE_SYSTEM -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
    
    fun setThemeMode(context: Context, mode: Int) {
        getPreferences(context).edit().putInt(KEY_THEME_MODE, mode).apply()
        
        when (mode) {
            MODE_LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            MODE_DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            MODE_SYSTEM -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
    
    fun getThemeMode(context: Context): Int {
        return getPreferences(context).getInt(KEY_THEME_MODE, MODE_SYSTEM)
    }
    
    fun isNightMode(context: Context): Boolean {
        return when (getThemeMode(context)) {
            MODE_DARK -> true
            MODE_LIGHT -> false
            else -> AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
        }
    }
    
    fun toggleTheme(context: Context) {
        val currentMode = getThemeMode(context)
        val newMode = when (currentMode) {
            MODE_LIGHT -> MODE_DARK
            MODE_DARK -> MODE_LIGHT
            else -> if (isNightMode(context)) MODE_LIGHT else MODE_DARK
        }
        setThemeMode(context, newMode)
    }
} 