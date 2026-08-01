package com.storemanager.app.ui.screens.products

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(onAddProduct: () -> Unit, onEditProduct: (Long) -> Unit, onViewDeleted: () -> Unit) {
    val vm = repoViewModel { ProductsViewModel(it) }
    val products by vm.products.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddProduct) { Icon(Icons.Filled.Add, contentDescription = "Add Product") }
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Products", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
                IconButton(onClick = onViewDeleted) { Icon(Icons.Filled.Delete, contentDescription = "Deleted products") }
            }
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = vm.searchQuery, onValueChange = { vm.searchQuery = it },
                label = { Text("Search by name, SKU, barcode") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(12.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(products, key = { it.id }) { product ->
                    Card(onClick = { onEditProduct(product.id) }, modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(product.name, style = MaterialTheme.typography.titleMedium)
                                Text("SKU: ${product.sku} • ${product.category}", style = MaterialTheme.typography.bodyMedium)
                                Text("Stock: ${product.quantity}", style = MaterialTheme.typography.bodyMedium,
                                    color = if (product.quantity <= product.lowStockThreshold) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("₹${product.sellingPrice}", style = MaterialTheme.typography.titleMedium)
                                IconButton(onClick = { vm.deleteProduct(product.id) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DeletedProductsScreen(onBack: () -> Unit) {
    val vm = repoViewModel { ProductsViewModel(it) }
    val deleted by vm.deletedProducts.collectAsState()
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
            Text("Deleted Products", style = MaterialTheme.typography.headlineMedium)
        }
        Spacer(Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(deleted, key = { it.id }) { product ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(product.name, style = MaterialTheme.typography.titleMedium)
                            Text("SKU: ${product.sku}", style = MaterialTheme.typography.bodyMedium)
                        }
                        TextButton(onClick = { vm.restoreProduct(product.id) }) { Text("Restore") }
                    }
                }
            }
        }
    }
}
