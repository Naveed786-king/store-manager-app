package com.storemanager.app.ui.screens.purchases

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.storemanager.app.ui.repoViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PurchasesListScreen(onNewPurchase: () -> Unit) {
    val vm = repoViewModel { PurchasesViewModel(it) }
    val purchases by vm.purchases.collectAsState()
    val df = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }

    Scaffold(floatingActionButton = {
        FloatingActionButton(onClick = onNewPurchase) { Icon(Icons.Filled.Add, contentDescription = "New Purchase") }
    }) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            Text("Purchases", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(12.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(purchases, key = { it.id }) { purchase ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Text(purchase.referenceNumber, style = MaterialTheme.typography.titleMedium)
                            Text(df.format(Date(purchase.createdAt)), style = MaterialTheme.typography.bodyMedium)
                            Text("Total: ₹${purchase.total}")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePurchaseScreen(onBack: () -> Unit) {
    val vm = repoViewModel { PurchasesViewModel(it) }
    val suppliers by vm.suppliers.collectAsState()
    val products by vm.products.collectAsState()
    var showProductPicker by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("New Purchase") }, navigationIcon = {
                IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
            })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showProductPicker = true }) { Icon(Icons.Filled.Add, contentDescription = "Add product") }
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = suppliers.find { it.id == vm.selectedSupplierId }?.name ?: "",
                    onValueChange = {}, readOnly = true, label = { Text("Supplier") },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    suppliers.forEach { supplier ->
                        DropdownMenuItem(text = { Text(supplier.name) }, onClick = { vm.selectedSupplierId = supplier.id; expanded = false })
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(vm.cart, key = { it.product.id }) { line ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(line.product.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                                IconButton(onClick = { vm.removeFromCart(line.product.id) }) { Icon(Icons.Filled.Close, contentDescription = "Remove") }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = line.quantity.toString(),
                                    onValueChange = { vm.updateQuantity(line.product.id, it.toIntOrNull() ?: 1) },
                                    label = { Text("Qty") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = line.unitCost.toString(),
                                    onValueChange = { vm.updateCost(line.product.id, it.toDoubleOrNull() ?: 0.0) },
                                    label = { Text("Unit Cost") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
            Text("Total: ₹%.2f".format(vm.total), style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { scope.launch { vm.submitPurchase(); onBack() } },
                enabled = vm.selectedSupplierId != null && vm.cart.isNotEmpty(),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) { Text("Save Purchase (Stock will increase)") }
        }
    }

    if (showProductPicker) {
        Dialog(onDismissRequest = { showProductPicker = false }) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Select Product", style = MaterialTheme.typography.titleLarge)
                    LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                        items(products, key = { it.id }) { product ->
                            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(product.name)
                                TextButton(onClick = { vm.addToCart(product); showProductPicker = false }) { Text("Add") }
                            }
                        }
                    }
                }
            }
        }
    }
}
