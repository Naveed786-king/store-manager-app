package com.storemanager.app.ui.screens.suppliers

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.storemanager.app.data.entity.Supplier
import com.storemanager.app.data.repository.StoreRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SuppliersViewModel(private val repo: StoreRepository) : ViewModel() {
    val suppliers = repo.getSuppliers().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteSupplier(supplier: Supplier) = viewModelScope.launch { repo.deleteSupplier(supplier) }

    fun saveSupplier(id: Long?, name: String, phone: String, email: String, address: String, onDone: () -> Unit) {
        viewModelScope.launch {
            if (id == null || id == -1L) {
                repo.addSupplier(Supplier(name = name, phone = phone, email = email, address = address))
            } else {
                repo.getSupplierById(id)?.let {
                    repo.updateSupplier(it.copy(name = name, phone = phone, email = email, address = address))
                }
            }
            onDone()
        }
    }

    suspend fun getSupplier(id: Long): Supplier? = repo.getSupplierById(id)
    fun getSupplierPurchases(supplierId: Long) = repo.getSupplierPurchases(supplierId)
}
