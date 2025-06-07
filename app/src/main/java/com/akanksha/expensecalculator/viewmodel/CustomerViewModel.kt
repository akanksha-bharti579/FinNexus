package com.akanksha.expensecalculator.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akanksha.expensecalculator.data.model.Customer
import com.akanksha.expensecalculator.repository.CustomerRepository
import kotlinx.coroutines.launch

class CustomerViewModel : ViewModel() {
    // This would typically be injected
    private val repository = CustomerRepository()
    
    private val _customers = MutableLiveData<List<Customer>>()
    val customers: LiveData<List<Customer>> = _customers
    
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading
    
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error
    
    init {
        loadCustomers()
    }
    
    fun loadCustomers() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val result = repository.getCustomers()
                _customers.value = result
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }
    
    fun searchCustomers(query: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val result = repository.searchCustomers(query)
                _customers.value = result
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }
    
    fun addCustomer(customer: Customer) {
        viewModelScope.launch {
            try {
                repository.addCustomer(customer)
                loadCustomers()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
    
    fun updateCustomer(customer: Customer) {
        viewModelScope.launch {
            try {
                repository.updateCustomer(customer)
                loadCustomers()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
    
    fun deleteCustomer(customerId: Long) {
        viewModelScope.launch {
            try {
                repository.deleteCustomer(customerId)
                loadCustomers()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
} 