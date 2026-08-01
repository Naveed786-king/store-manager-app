package com.storemanager.app.ui.screens.customers

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.storemanager.app.data.entity.Customer
import com.storemanager.app.data.repository.StoreRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CustomersViewModel(private val repo: StoreRepository) : ViewModel() {
    var searchQuery by mutableStateOf("")
    val customers = snapshotFlow { searchQuery }
        .flatMapLatest { q -> if (q.isBlank()) repo.getCustomers() else repo.searchCustomers(q) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteCustomer(customer: Customer) = viewModelScope.launch { repo.deleteCustomer(customer) }

    fun saveCustomer(id: Long?, name: String, phone: String, email: String, address: String, onDone: () -> Unit) {
        viewModelScope.launch {
            if (id == null || id == -1L) {
                repo.addCustomer(Customer(name = name, phone = phone, email = email, address = address))
            } else {
                repo.getCustomerById(id)?.let {
                    repo.updateCustomer(it.copy(name = name, phone = phone, email = email, address = address))
                }
            }
            onDone()
        }
    }

    suspend fun getCustomer(id: Long): Customer? = repo.getCustomerById(id)
    fun getCustomerSales(customerId: Long) = repo.getCustomerSales(customerId)
}
