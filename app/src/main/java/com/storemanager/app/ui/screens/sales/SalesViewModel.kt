package com.storemanager.app.ui.screens.sales

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.storemanager.app.data.entity.*
import com.storemanager.app.data.repository.StoreRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CartLine(val product: Product, var quantity: Int)

class SalesViewModel(private val repo: StoreRepository) : ViewModel() {
    val sales = repo.getSales().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val products = repo.getProducts().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val customers = repo.getCustomers().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cart = mutableStateListOf<CartLine>()
    var selectedCustomerId by mutableStateOf<Long?>(null)
    var discount by mutableStateOf(0.0)
    var taxPercent by mutableStateOf(0.0)
    var paymentMethod by mutableStateOf("Cash")

    fun addToCart(product: Product) {
        val existing = cart.find { it.product.id == product.id }
        if (existing != null) existing.quantity++ else cart.add(CartLine(product, 1))
    }

    fun updateQuantity(productId: Long, quantity: Int) {
        cart.find { it.product.id == productId }?.let { it.quantity = quantity.coerceAtLeast(1) }
    }

    fun removeFromCart(productId: Long) { cart.removeAll { it.product.id == productId } }

    fun clearCart() { cart.clear(); discount = 0.0; taxPercent = 0.0; selectedCustomerId = null }

    val subtotal: Double get() = cart.sumOf { it.product.sellingPrice * it.quantity }
    val taxAmount: Double get() = subtotal * taxPercent / 100.0
    val total: Double get() = (subtotal - discount + taxAmount).coerceAtLeast(0.0)

    suspend fun checkout(): Long {
        val invoiceNumber = repo.nextInvoiceNumber()
        val sale = Sale(
            invoiceNumber = invoiceNumber,
            customerId = selectedCustomerId,
            subtotal = subtotal,
            discount = discount,
            tax = taxAmount,
            total = total,
            paymentMethod = paymentMethod
        )
        val items = cart.map {
            SaleItem(
                saleId = 0, productId = it.product.id, productName = it.product.name,
                quantity = it.quantity, unitPrice = it.product.sellingPrice, lineTotal = it.product.sellingPrice * it.quantity
            )
        }
        val id = repo.createSale(sale, items)
        clearCart()
        return id
    }

    suspend fun getSaleWithItems(saleId: Long): Pair<Sale?, List<SaleItem>> {
        val sale = repo.getSaleById(saleId)
        val items = repo.getSaleItems(saleId)
        return sale to items
    }

    suspend fun getCustomerName(customerId: Long?): String {
        if (customerId == null) return "Walk-in Customer"
        return repo.getCustomerById(customerId)?.name ?: "Walk-in Customer"
    }

    fun processReturn(sale: Sale, items: List<SaleItem>, onDone: () -> Unit) {
        viewModelScope.launch {
            repo.createReturn(sale, items)
            onDone()
        }
    }
}
