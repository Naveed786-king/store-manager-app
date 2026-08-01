package com.storemanager.app.ui.screens.sales

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.storemanager.app.data.entity.Sale
import com.storemanager.app.data.entity.SaleItem
import com.storemanager.app.ui.repoViewModel
import com.storemanager.app.util.PdfInvoiceGenerator
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceDetailScreen(saleId: Long, onBack: () -> Unit) {
    val vm = repoViewModel { SalesViewModel(it) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var sale by remember { mutableStateOf<Sale?>(null) }
    var items by remember { mutableStateOf<List<SaleItem>>(emptyList()) }
    var customerName by remember { mutableStateOf("Walk-in Customer") }

    LaunchedEffect(saleId) {
        val (s, i) = vm.getSaleWithItems(saleId)
        sale = s; items = i
        customerName = vm.getCustomerName(s?.customerId)
    }

    Scaffold(topBar = {
        TopAppBar(title = { Text("Invoice") }, navigationIcon = {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
        })
    }) { padding ->
        val currentSale = sale
        if (currentSale == null) {
            Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }
        Column(Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            Text(currentSale.invoiceNumber, style = MaterialTheme.typography.headlineMedium)
            Text("Customer: $customerName")
            Text("Payment: ${currentSale.paymentMethod}")
            Spacer(Modifier.height(12.dp))
            LazyColumn(Modifier.weight(1f)) {
                items(items, key = { it.id }) { item ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${item.productName} x${item.quantity}")
                        Text("₹%.2f".format(item.lineTotal))
                    }
                }
            }
            Divider()
            Text("Subtotal: ₹%.2f".format(currentSale.subtotal))
            Text("Discount: ₹%.2f".format(currentSale.discount))
            Text("Tax: ₹%.2f".format(currentSale.tax))
            Text("Total: ₹%.2f".format(currentSale.total), style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = {
                    val uri = PdfInvoiceGenerator.generate(context, currentSale, items, customerName)
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, "application/pdf")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(Intent.createChooser(intent, "Open/Print Invoice"))
                }) { Icon(Icons.Filled.Print, contentDescription = null); Spacer(Modifier.width(6.dp)); Text("Print / PDF") }

                if (!currentSale.isReturn) {
                    OutlinedButton(onClick = {
                        scope.launch { vm.processReturn(currentSale, items, onBack) }
                    }) { Text("Process Return") }
                }
            }
        }
    }
}
