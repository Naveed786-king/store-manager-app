package com.storemanager.app.ui.screens.inventory

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.storemanager.app.data.repository.StoreRepository
import com.storemanager.app.ui.repoViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class InventoryViewModel(repo: StoreRepository) : ViewModel() {
    val products = repo.getProducts().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val lowStock = repo.getLowStock().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val history = repo.getStockHistory().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

@Composable
fun InventoryScreen() {
    val vm = repoViewModel { InventoryViewModel(it) }
    val products by vm.products.collectAsState()
    val lowStock by vm.lowStock.collectAsState()
    val history by vm.history.collectAsState()
    val tabs = listOf("Current Stock", "Low Stock", "Stock History")
    var selectedTab by remember { mutableStateOf(0) }
    val df = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Inventory", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp))
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(title) })
            }
        }
        Spacer(Modifier.height(12.dp))
        when (selectedTab) {
            0 -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(products, key = { it.id }) { p ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(p.name); Text("Qty: ${p.quantity}")
                        }
                    }
                }
            }
            1 -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(lowStock, key = { it.id }) { p ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(p.name)
                            Text("Qty: ${p.quantity} (min ${p.lowStockThreshold})", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
            2 -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(history, key = { it.id }) { h ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Text("${h.productName} • ${h.changeType}", style = MaterialTheme.typography.titleMedium)
                            Text("Change: ${if (h.quantityChange >= 0) "+" else ""}${h.quantityChange} → New: ${h.newQuantity}")
                            Text(df.format(Date(h.createdAt)), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}
