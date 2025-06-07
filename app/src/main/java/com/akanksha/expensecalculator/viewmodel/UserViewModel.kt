package com.akanksha.expensecalculator.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.akanksha.expensecalculator.utils.BiometricUtils
import com.akanksha.expensecalculator.utils.ThemeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class UserProfile(
    val name: String,
    val email: String,
    val currency: String
)

class UserViewModel(application: Application) : AndroidViewModel(application) {
    private val _biometricEnabled = MutableLiveData(BiometricUtils.isBiometricEnabled(application))
    val biometricEnabled: LiveData<Boolean> = _biometricEnabled
    
    private val _themeMode = MutableLiveData(ThemeUtils.getThemeMode(application))
    val themeMode: LiveData<Int> = _themeMode
    
    private val _userProfile = MutableLiveData<UserProfile>()
    val userProfile: LiveData<UserProfile> = _userProfile
    
    init {
        loadUserPreferences()
    }
    
    private fun loadUserPreferences() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val prefs = getApplication<Application>().getSharedPreferences("user_preferences", Application.MODE_PRIVATE)
                val name = prefs.getString("user_name", "") ?: ""
                val email = prefs.getString("user_email", "") ?: ""
                val currency = prefs.getString("currency", "USD ($)") ?: "USD ($)"
                _userProfile.postValue(UserProfile(name, email, currency))
            }
        }
    }
    
    fun getUserProfile(): UserProfile? = _userProfile.value
    
    fun getUserCurrency(): String? = _userProfile.value?.currency
    
    fun setUserCurrency(currency: String) {
        val currentProfile = _userProfile.value
        if (currentProfile != null) {
            _userProfile.value = currentProfile.copy(currency = currency)
            saveUserProfile(currentProfile.name, currentProfile.email, currency)
        }
    }
    
    fun saveUserProfile(name: String, email: String, currency: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val prefs = getApplication<Application>().getSharedPreferences("user_preferences", Application.MODE_PRIVATE)
                prefs.edit()
                    .putString("user_name", name)
                    .putString("user_email", email)
                    .putString("currency", currency)
                    .commit()
                
                _userProfile.postValue(UserProfile(name, email, currency))
            }
        }
    }
    
    fun toggleBiometricAuthentication() {
        val newState = BiometricUtils.toggleBiometric(getApplication())
        _biometricEnabled.value = newState
    }
    
    fun setThemeMode(mode: Int) {
        ThemeUtils.setThemeMode(getApplication(), mode)
        _themeMode.value = mode
    }
    
    fun toggleTheme() {
        ThemeUtils.toggleTheme(getApplication())
        _themeMode.value = ThemeUtils.getThemeMode(getApplication())
    }
} 