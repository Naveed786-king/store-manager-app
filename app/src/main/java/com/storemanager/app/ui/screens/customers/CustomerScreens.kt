package com.storemanager.app.ui.screens.customers

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.storemanager.app.ui.repoViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CustomerListScreen(onAdd: () -> Unit, onEdit: (Long) -> Unit, onHistory: (Long) -> Unit) {
    val vm = repoViewModel { CustomersViewModel(it) }
    val customers by vm.customers.collectAsState()

    Scaffold(floatingActionButton = {
        FloatingActionButton(onClick = onAdd) { Icon(Icons.Filled.Add, contentDescription = "Add Customer") }
    }) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            Text("Customers", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = vm.searchQuery, onValueChange = { vm.searchQuery = it },
                label = { Text("Search customers") }, leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )
            Spacer(Modifier.height(12.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(customers, key = { it.id }) { customer ->
                    Card(onClick = { onHistory(customer.id) }, modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(customer.name, style = MaterialTheme.typography.titleMedium)
                                Text(customer.phone, style = MaterialTheme.typography.bodyMedium)
                            }
                            IconButton(onClick = { onEdit(customer.id) }) { Icon(Icons.Filled.Edit, contentDescription = "Edit") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerAddEditScreen(customerId: Long?, onBack: () -> Unit) {
    val vm = repoViewModel { CustomersViewModel(it) }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var loaded by remember { mutableStateOf(customerId == null || customerId == -1L) }

    LaunchedEffect(customerId) {
        if (customerId != null && customerId != -1L && !loaded) {
            vm.getCustomer(customerId)?.let { name = it.name; phone = it.phone; email = it.email; address = it.address }
            loaded = true
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
            Text(if (customerId == null || customerId == -1L) "Add Customer" else "Edit Customer", style = MaterialTheme.typography.headlineMedium)
        }
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { vm.saveCustomer(customerId, name, phone, email, address, onBack) },
            enabled = name.isNotBlank(), modifier = Modifier.fillMaxWidth().height(50.dp)
        ) { Text("Save Customer") }
    }
}

@Composable
fun CustomerHistoryScreen(customerId: Long, onBack: () -> Unit) {
    val vm = repoViewModel { CustomersViewModel(it) }
    val sales by vm.getCustomerSales(customerId).collectAsState(initial = emptyList())
    val df = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
            Text("Purchase History", style = MaterialTheme.typography.headlineMedium)
        }
        Spacer(Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(sales, key = { it.id }) { sale ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text(sale.invoiceNumber, style = MaterialTheme.typography.titleMedium)
                        Text(df.format(Date(sale.createdAt)), style = MaterialTheme.typography.bodyMedium)
                        Text("Total: ₹${sale.total}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
