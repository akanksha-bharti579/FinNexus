package com.akanksha.expensecalculator.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akanksha.expensecalculator.repository.AdminRepository
import kotlinx.coroutines.launch

class AdminViewModel : ViewModel() {
    // This would typically be injected
    private val repository = AdminRepository()
    
    private val _backupStatus = MutableLiveData<BackupStatus>()
    val backupStatus: LiveData<BackupStatus> = _backupStatus
    
    private val _restoreStatus = MutableLiveData<RestoreStatus>()
    val restoreStatus: LiveData<RestoreStatus> = _restoreStatus
    
    private val _exportStatus = MutableLiveData<ExportStatus>()
    val exportStatus: LiveData<ExportStatus> = _exportStatus
    
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error
    
    fun backupData() {
        viewModelScope.launch {
            _backupStatus.value = BackupStatus.LOADING
            try {
                val success = repository.backupData()
                _backupStatus.value = if (success) BackupStatus.SUCCESS else BackupStatus.FAILURE
            } catch (e: Exception) {
                _backupStatus.value = BackupStatus.FAILURE
                _error.value = e.message
            }
        }
    }
    
    fun restoreData() {
        viewModelScope.launch {
            _restoreStatus.value = RestoreStatus.LOADING
            try {
                val success = repository.restoreData()
                _restoreStatus.value = if (success) RestoreStatus.SUCCESS else RestoreStatus.FAILURE
            } catch (e: Exception) {
                _restoreStatus.value = RestoreStatus.FAILURE
                _error.value = e.message
            }
        }
    }
    
    fun exportToCsv() {
        viewModelScope.launch {
            _exportStatus.value = ExportStatus.LOADING
            try {
                val filePath = repository.exportToCsv()
                _exportStatus.value = if (filePath != null) ExportStatus.SUCCESS else ExportStatus.FAILURE
            } catch (e: Exception) {
                _exportStatus.value = ExportStatus.FAILURE
                _error.value = e.message
            }
        }
    }
    
    enum class BackupStatus {
        LOADING, SUCCESS, FAILURE
    }
    
    enum class RestoreStatus {
        LOADING, SUCCESS, FAILURE
    }
    
    enum class ExportStatus {
        LOADING, SUCCESS, FAILURE
    }
} 