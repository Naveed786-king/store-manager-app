package com.storemanager.app.ui.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.storemanager.app.data.repository.StoreRepository
import com.storemanager.app.ui.repoViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DashboardViewModel(private val repo: StoreRepository) : ViewModel() {
    var totalProducts by mutableStateOf(0); private set
    var todaySales by mutableStateOf(0.0); private set
    var monthlySales by mutableStateOf(0.0); private set
    var monthlyExpenses by mutableStateOf(0.0); private set
    var lowStockCount by mutableStateOf(0); private set

    init {
        viewModelScope.launch { repo.getTotalProductCount().collect { totalProducts = it } }
        viewModelScope.launch {
            repo.getTotalSalesBetween(StoreRepository.startOfToday(), StoreRepository.endOfToday()).collect { todaySales = it }
        }
        viewModelScope.launch {
            repo.getTotalSalesBetween(StoreRepository.startOfMonth(), StoreRepository.endOfMonth()).collect { monthlySales = it }
        }
        viewModelScope.launch {
            repo.getTotalExpensesBetween(StoreRepository.startOfMonth(), StoreRepository.endOfMonth()).collect { monthlyExpenses = it }
        }
        viewModelScope.launch { repo.getLowStock().collect { lowStockCount = it.size } }
    }

    val profitEstimate: Double get() = monthlySales - monthlyExpenses
}

data class DashboardCard(val title: String, val value: String, val icon: ImageVector, val tint: androidx.compose.ui.graphics.Color)

@Composable
fun DashboardScreen(onNavigate: (String) -> Unit) {
    val vm = repoViewModel { DashboardViewModel(it) }
    val cards = listOf(
        DashboardCard("Total Products", vm.totalProducts.toString(), Icons.Filled.Inventory2, MaterialTheme.colorScheme.primary),
        DashboardCard("Today's Sales", "₹%.2f".format(vm.todaySales), Icons.Filled.PointOfSale, MaterialTheme.colorScheme.secondary),
        DashboardCard("Monthly Sales", "₹%.2f".format(vm.monthlySales), Icons.Filled.TrendingUp, MaterialTheme.colorScheme.tertiary),
        DashboardCard("Est. Profit", "₹%.2f".format(vm.profitEstimate), Icons.Filled.AccountBalanceWallet, MaterialTheme.colorScheme.primary),
        DashboardCard("Low Stock Alerts", vm.lowStockCount.toString(), Icons.Filled.Warning, MaterialTheme.colorScheme.error)
    )

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Dashboard", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        LazyVerticalGrid(columns = GridCells.Fixed(2), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(cards) { card -> DashboardCardView(card) }
        }
        Spacer(Modifier.height(24.dp))
        Text("Quick Actions", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FilledTonalButton(onClick = { onNavigate("create_invoice") }) { Text("New Sale") }
            FilledTonalButton(onClick = { onNavigate("product_add_edit?productId=-1") }) { Text("Add Product") }
            FilledTonalButton(onClick = { onNavigate("create_purchase") }) { Text("New Purchase") }
        }
    }
}

@Composable
fun DashboardCardView(card: DashboardCard) {
    Card(modifier = Modifier.fillMaxWidth().height(110.dp)) {
        Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Icon(card.icon, contentDescription = null, tint = card.tint)
            Column {
                Text(card.value, style = MaterialTheme.typography.titleLarge)
                Text(card.title, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
