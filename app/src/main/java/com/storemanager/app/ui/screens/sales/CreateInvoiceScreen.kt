package com.storemanager.app.ui.screens.sales

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateInvoiceScreen(onBack: () -> Unit, onCheckoutComplete: (Long) -> Unit) {
    val vm = repoViewModel { SalesViewModel(it) }
    val products by vm.products.collectAsState()
    val customers by vm.customers.collectAsState()
    var showProductPicker by remember { mutableStateOf(false) }
    var showCustomerPicker by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("New Sale") }, navigationIcon = {
                IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
            })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showProductPicker = true }) { Icon(Icons.Filled.Add, contentDescription = "Add item") }
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            OutlinedButton(onClick = { showCustomerPicker = true }, modifier = Modifier.fillMaxWidth()) {
                val custName = customers.find { it.id == vm.selectedCustomerId }?.name ?: "Walk-in Customer"
                Text("Customer: $custName")
            }
            Spacer(Modifier.height(12.dp))
            if (vm.cart.isEmpty()) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("Tap + to add products to this sale")
                }
            } else {
                LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(vm.cart, key = { it.product.id }) { line ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(line.product.name, style = MaterialTheme.typography.titleMedium)
                                    Text("₹${line.product.sellingPrice} each", style = MaterialTheme.typography.bodyMedium)
                                }
                                IconButton(onClick = { vm.updateQuantity(line.product.id, line.quantity - 1) }) {
                                    Icon(Icons.Filled.Remove, contentDescription = "Decrease")
                                }
                                Text(line.quantity.toString(), style = MaterialTheme.typography.titleMedium)
                                IconButton(onClick = { vm.updateQuantity(line.product.id, line.quantity + 1) }) {
                                    Icon(Icons.Filled.Add, contentDescription = "Increase")
                                }
                                IconButton(onClick = { vm.removeFromCart(line.product.id) }) {
                                    Icon(Icons.Filled.Close, contentDescription = "Remove")
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = if (vm.discount == 0.0) "" else vm.discount.toString(),
                    onValueChange = { vm.discount = it.toDoubleOrNull() ?: 0.0 },
                    label = { Text("Discount ₹") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = if (vm.taxPercent == 0.0) "" else vm.taxPercent.toString(),
                    onValueChange = { vm.taxPercent = it.toDoubleOrNull() ?: 0.0 },
                    label = { Text("Tax %") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(8.dp))
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = vm.paymentMethod, onValueChange = {}, readOnly = true,
                    label = { Text("Payment Method") }, modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    listOf("Cash", "Card", "UPI", "Bank Transfer").forEach { method ->
                        DropdownMenuItem(text = { Text(method) }, onClick = { vm.paymentMethod = method; expanded = false })
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Text("Subtotal: ₹%.2f".format(vm.subtotal))
            Text("Tax: ₹%.2f".format(vm.taxAmount))
            Text("Total: ₹%.2f".format(vm.total), style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { scope.launch { val id = vm.checkout(); onCheckoutComplete(id) } },
                enabled = vm.cart.isNotEmpty(),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) { Text("Complete Sale") }
        }
    }

    if (showProductPicker) {
        Dialog(onDismissRequest = { showProductPicker = false }) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Select Product", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(8.dp))
                    LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                        items(products, key = { it.id }) { product ->
                            Row(
                                Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(product.name)
                                TextButton(onClick = { vm.addToCart(product); showProductPicker = false }) { Text("Add") }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCustomerPicker) {
        Dialog(onDismissRequest = { showCustomerPicker = false }) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Select Customer", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = { vm.selectedCustomerId = null; showCustomerPicker = false }) { Text("Walk-in Customer") }
                    LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                        items(customers, key = { it.id }) { customer ->
                            Row(
                                Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(customer.name)
                                TextButton(onClick = { vm.selectedCustomerId = customer.id; showCustomerPicker = false }) { Text("Select") }
                            }
                        }
                    }
                }
            }
        }
    }
}
