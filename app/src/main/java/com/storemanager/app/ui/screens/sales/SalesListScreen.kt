package com.storemanager.app.ui.screens.sales

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
fun SalesListScreen(onNewSale: () -> Unit, onOpenInvoice: (Long) -> Unit) {
    val vm = repoViewModel { SalesViewModel(it) }
    val sales by vm.sales.collectAsState()
    val df = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }

    Scaffold(floatingActionButton = {
        FloatingActionButton(onClick = onNewSale) { Icon(Icons.Filled.Add, contentDescription = "New Sale") }
    }) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            Text("Sales", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(12.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(sales, key = { it.id }) { sale ->
                    Card(onClick = { onOpenInvoice(sale.id) }, modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(sale.invoiceNumber, style = MaterialTheme.typography.titleMedium)
                                Text(df.format(Date(sale.createdAt)), style = MaterialTheme.typography.bodyMedium)
                                Text(sale.paymentMethod, style = MaterialTheme.typography.bodyMedium)
                            }
                            Text(
                                (if (sale.isReturn) "-₹" else "₹") + "%.2f".format(kotlin.math.abs(sale.total)),
                                style = MaterialTheme.typography.titleMedium,
                                color = if (sale.isReturn) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}
