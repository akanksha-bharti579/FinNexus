package com.akanksha.expensecalculator.repository

import com.akanksha.expensecalculator.data.model.Customer

class CustomerRepository {
    // This would typically use Room Database or a remote API
    
    private val dummyCustomers = mutableListOf(
        Customer(
            id = 1,
            name = "John Doe",
            email = "john.doe@example.com",
            phone = "+1 (555) 123-4567"
        ),
        Customer(
            id = 2,
            name = "Jane Smith",
            email = "jane.smith@example.com",
            phone = "+1 (555) 987-6543"
        ),
        Customer(
            id = 3,
            name = "Bob Johnson",
            email = "bob.johnson@example.com",
            phone = "+1 (555) 456-7890"
        )
    )
    
    suspend fun getCustomers(): List<Customer> {
        // In a real app, this would get data from Room or an API
        return dummyCustomers
    }
    
    suspend fun searchCustomers(query: String): List<Customer> {
        // Simple search implementation
        return dummyCustomers.filter {
            it.name.contains(query, ignoreCase = true) ||
            it.email.contains(query, ignoreCase = true) ||
            it.phone.contains(query, ignoreCase = true)
        }
    }
    
    suspend fun getCustomerById(id: Long): Customer? {
        return dummyCustomers.find { it.id == id }
    }
    
    suspend fun addCustomer(customer: Customer) {
        // In a real app, this would insert into Room or call an API
        val newId = (dummyCustomers.maxByOrNull { it.id }?.id ?: 0) + 1
        dummyCustomers.add(customer.copy(id = newId))
    }
    
    suspend fun updateCustomer(customer: Customer) {
        // In a real app, this would update in Room or call an API
        val index = dummyCustomers.indexOfFirst { it.id == customer.id }
        if (index != -1) {
            dummyCustomers[index] = customer
        }
    }
    
    suspend fun deleteCustomer(customerId: Long) {
        // In a real app, this would delete from Room or call an API
        dummyCustomers.removeIf { it.id == customerId }
    }
} 