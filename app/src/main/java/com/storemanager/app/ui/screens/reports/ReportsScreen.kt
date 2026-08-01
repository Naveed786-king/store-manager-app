package com.storemanager.app.ui.screens.reports

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.storemanager.app.data.repository.StoreRepository
import com.storemanager.app.ui.repoViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ReportsViewModel(private val repo: StoreRepository) : ViewModel() {
    var dailySales by mutableStateOf(0.0); private set
    var weeklySales by mutableStateOf(0.0); private set
    var monthlySales by mutableStateOf(0.0); private set
    var monthlyExpenses by mutableStateOf(0.0); private set
    var lowStockCount by mutableStateOf(0); private set
    val totalProducts = repo.getTotalProductCount().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        viewModelScope.launch { repo.getTotalSalesBetween(StoreRepository.startOfToday(), StoreRepository.endOfToday()).collect { dailySales = it } }
        viewModelScope.launch { repo.getTotalSalesBetween(StoreRepository.startOfWeek(), StoreRepository.endOfWeek()).collect { weeklySales = it } }
        viewModelScope.launch { repo.getTotalSalesBetween(StoreRepository.startOfMonth(), StoreRepository.endOfMonth()).collect { monthlySales = it } }
        viewModelScope.launch { repo.getTotalExpensesBetween(StoreRepository.startOfMonth(), StoreRepository.endOfMonth()).collect { monthlyExpenses = it } }
        viewModelScope.launch { repo.getLowStock().collect { lowStockCount = it.size } }
    }

    val profit: Double get() = monthlySales - monthlyExpenses
}

@Composable
fun ReportsScreen() {
    val vm = repoViewModel { ReportsViewModel(it) }
    val totalProducts by vm.totalProducts.collectAsState()

    val rows = listOf(
        "Daily Sales" to "₹%.2f".format(vm.dailySales),
        "Weekly Sales" to "₹%.2f".format(vm.weeklySales),
        "Monthly Sales" to "₹%.2f".format(vm.monthlySales),
        "Monthly Expenses" to "₹%.2f".format(vm.monthlyExpenses),
        "Estimated Profit" to "₹%.2f".format(vm.profit),
        "Total Products" to totalProducts.toString(),
        "Low Stock Items" to vm.lowStockCount.toString()
    )

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Reports", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(rows) { (label, value) ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(label, style = MaterialTheme.typography.titleMedium)
                        Text(value, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}
