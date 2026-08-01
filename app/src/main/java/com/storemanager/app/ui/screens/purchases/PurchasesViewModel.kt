package com.storemanager.app.ui.screens.purchases

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.storemanager.app.data.entity.*
import com.storemanager.app.data.repository.StoreRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

data class PurchaseCartLine(val product: Product, var quantity: Int, var unitCost: Double)

class PurchasesViewModel(private val repo: StoreRepository) : ViewModel() {
    val purchases = repo.getPurchases().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val suppliers = repo.getSuppliers().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val products = repo.getProducts().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var selectedSupplierId by mutableStateOf<Long?>(null)
    val cart = mutableStateListOf<PurchaseCartLine>()

    fun addToCart(product: Product) {
        if (cart.none { it.product.id == product.id }) cart.add(PurchaseCartLine(product, 1, product.costPrice))
    }
    fun removeFromCart(productId: Long) { cart.removeAll { it.product.id == productId } }
    fun updateQuantity(productId: Long, qty: Int) { cart.find { it.product.id == productId }?.quantity = qty.coerceAtLeast(1) }
    fun updateCost(productId: Long, cost: Double) { cart.find { it.product.id == productId }?.unitCost = cost }

    val total: Double get() = cart.sumOf { it.quantity * it.unitCost }

    suspend fun submitPurchase(): Long? {
        val supplierId = selectedSupplierId ?: return null
        val ref = "PO-${System.currentTimeMillis()}"
        val purchase = Purchase(supplierId = supplierId, referenceNumber = ref, total = total)
        val items = cart.map {
            PurchaseItem(
                purchaseId = 0, productId = it.product.id, productName = it.product.name,
                quantity = it.quantity, unitCost = it.unitCost, lineTotal = it.quantity * it.unitCost
            )
        }
        val id = repo.createPurchase(purchase, items)
        cart.clear(); selectedSupplierId = null
        return id
    }
}
