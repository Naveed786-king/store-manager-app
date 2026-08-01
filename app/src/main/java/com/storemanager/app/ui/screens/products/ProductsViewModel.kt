package com.storemanager.app.ui.screens.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.*
import com.storemanager.app.data.entity.Product
import com.storemanager.app.data.repository.StoreRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProductsViewModel(private val repo: StoreRepository) : ViewModel() {
    var searchQuery by mutableStateOf("")
    val products = snapshotFlow { searchQuery }
        .flatMapLatest { q -> if (q.isBlank()) repo.getProducts() else repo.searchProducts(q) }
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), emptyList())

    val deletedProducts = repo.getDeletedProducts()
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteProduct(id: Long) = viewModelScope.launch { repo.deleteProduct(id) }
    fun restoreProduct(id: Long) = viewModelScope.launch { repo.restoreProduct(id) }

    fun saveProduct(
        id: Long?, name: String, sku: String, barcode: String?, category: String, brand: String,
        costPrice: Double, sellingPrice: Double, quantity: Int, lowStockThreshold: Int, imagePath: String?,
        onDone: () -> Unit
    ) {
        viewModelScope.launch {
            if (id == null || id == -1L) {
                repo.addProduct(
                    Product(
                        name = name, sku = sku, barcode = barcode, category = category, brand = brand,
                        costPrice = costPrice, sellingPrice = sellingPrice, quantity = quantity,
                        lowStockThreshold = lowStockThreshold, imagePath = imagePath
                    )
                )
            } else {
                val existing = repo.getProductById(id)
                if (existing != null) {
                    repo.updateProduct(
                        existing.copy(
                            name = name, sku = sku, barcode = barcode, category = category, brand = brand,
                            costPrice = costPrice, sellingPrice = sellingPrice, quantity = quantity,
                            lowStockThreshold = lowStockThreshold, imagePath = imagePath
                        )
                    )
                }
            }
            onDone()
        }
    }

    suspend fun getProduct(id: Long): Product? = repo.getProductById(id)
}
