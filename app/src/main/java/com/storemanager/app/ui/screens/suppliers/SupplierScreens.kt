package com.storemanager.app.ui.screens.suppliers

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
fun SupplierListScreen(onAdd: () -> Unit, onEdit: (Long) -> Unit, onHistory: (Long) -> Unit) {
    val vm = repoViewModel { SuppliersViewModel(it) }
    val suppliers by vm.suppliers.collectAsState()

    Scaffold(floatingActionButton = {
        FloatingActionButton(onClick = onAdd) { Icon(Icons.Filled.Add, contentDescription = "Add Supplier") }
    }) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            Text("Suppliers", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(12.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(suppliers, key = { it.id }) { supplier ->
                    Card(onClick = { onHistory(supplier.id) }, modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(supplier.name, style = MaterialTheme.typography.titleMedium)
                                Text(supplier.phone, style = MaterialTheme.typography.bodyMedium)
                            }
                            IconButton(onClick = { onEdit(supplier.id) }) { Icon(Icons.Filled.Edit, contentDescription = "Edit") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SupplierAddEditScreen(supplierId: Long?, onBack: () -> Unit) {
    val vm = repoViewModel { SuppliersViewModel(it) }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var loaded by remember { mutableStateOf(supplierId == null || supplierId == -1L) }

    LaunchedEffect(supplierId) {
        if (supplierId != null && supplierId != -1L && !loaded) {
            vm.getSupplier(supplierId)?.let { name = it.name; phone = it.phone; email = it.email; address = it.address }
            loaded = true
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
            Text(if (supplierId == null || supplierId == -1L) "Add Supplier" else "Edit Supplier", style = MaterialTheme.typography.headlineMedium)
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
            onClick = { vm.saveSupplier(supplierId, name, phone, email, address, onBack) },
            enabled = name.isNotBlank(), modifier = Modifier.fillMaxWidth().height(50.dp)
        ) { Text("Save Supplier") }
    }
}

@Composable
fun SupplierHistoryScreen(supplierId: Long, onBack: () -> Unit) {
    val vm = repoViewModel { SuppliersViewModel(it) }
    val purchases by vm.getSupplierPurchases(supplierId).collectAsState(initial = emptyList())
    val df = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
            Text("Purchase History", style = MaterialTheme.typography.headlineMedium)
        }
        Spacer(Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(purchases, key = { it.id }) { purchase ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text(purchase.referenceNumber, style = MaterialTheme.typography.titleMedium)
                        Text(df.format(Date(purchase.createdAt)), style = MaterialTheme.typography.bodyMedium)
                        Text("Total: ₹${purchase.total}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
