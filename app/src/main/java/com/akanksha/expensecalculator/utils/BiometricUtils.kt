package com.akanksha.expensecalculator.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.akanksha.expensecalculator.R

object BiometricUtils {
    private const val PREFERENCE_NAME = "biometric_preferences"
    private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
    private const val KEY_PIN_CODE = "pin_code"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
    }

    fun isBiometricAvailable(context: Context): Boolean {
        val biometricManager = BiometricManager.from(context)
        return biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == 
                BiometricManager.BIOMETRIC_SUCCESS
    }

    fun setBiometricEnabled(context: Context, enabled: Boolean) {
        getPreferences(context).edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }

    fun isBiometricEnabled(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_BIOMETRIC_ENABLED, false)
    }

    fun setPinCode(context: Context, pin: String) {
        getPreferences(context).edit().putString(KEY_PIN_CODE, pin).apply()
    }

    fun getPinCode(context: Context): String? {
        return getPreferences(context).getString(KEY_PIN_CODE, null)
    }

    fun showBiometricPrompt(
        activity: FragmentActivity,
        title: String = activity.getString(R.string.biometric_title),
        subtitle: String = activity.getString(R.string.biometric_subtitle),
        description: String = activity.getString(R.string.biometric_description),
        negativeButtonText: String = activity.getString(R.string.biometric_negative_button),
        onSuccess: () -> Unit,
        onError: (Int, CharSequence) -> Unit = { _, _ -> },
        onFailed: () -> Unit = {}
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }
            
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onError(errorCode, errString)
            }
            
            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onFailed()
            }
        }
        
        val biometricPrompt = BiometricPrompt(activity, executor, callback)
        
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setDescription(description)
            .setNegativeButtonText(negativeButtonText)
            .build()
        
        biometricPrompt.authenticate(promptInfo)
    }

    fun validatePin(context: Context, enteredPin: String): Boolean {
        val storedPin = getPinCode(context)
        return storedPin != null && storedPin == enteredPin
    }

    fun toggleBiometric(context: Context): Boolean {
        val newState = !isBiometricEnabled(context)
        setBiometricEnabled(context, newState)
        return newState
    }
} 