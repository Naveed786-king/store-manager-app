package com.storemanager.app.ui.screens.products

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import coil.compose.AsyncImage
import com.storemanager.app.ui.repoViewModel

@Composable
fun ProductAddEditScreen(
    productId: Long?,
    onBack: () -> Unit,
    onScanBarcode: (() -> Unit)? = null,
    scannedBarcode: String? = null
) {
    val vm = repoViewModel { ProductsViewModel(it) }
    var name by remember { mutableStateOf("") }
    var sku by remember { mutableStateOf("") }
    var barcode by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("General") }
    var brand by remember { mutableStateOf("") }
    var costPrice by remember { mutableStateOf("") }
    var sellingPrice by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var lowStock by remember { mutableStateOf("5") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var loaded by remember { mutableStateOf(productId == null || productId == -1L) }

    LaunchedEffect(productId) {
        if (productId != null && productId != -1L && !loaded) {
            vm.getProduct(productId)?.let { p ->
                name = p.name; sku = p.sku; barcode = p.barcode ?: ""; category = p.category
                brand = p.brand; costPrice = p.costPrice.toString(); sellingPrice = p.sellingPrice.toString()
                quantity = p.quantity.toString(); lowStock = p.lowStockThreshold.toString()
                p.imagePath?.let { imageUri = Uri.parse(it) }
            }
            loaded = true
        }
    }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> uri?.let { imageUri = it } }

    LaunchedEffect(scannedBarcode) { if (!scannedBarcode.isNullOrBlank()) barcode = scannedBarcode }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
            Text(if (productId == null || productId == -1L) "Add Product" else "Edit Product", style = MaterialTheme.typography.headlineMedium)
        }
        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier.size(120.dp).align(Alignment.CenterHorizontally),
            contentAlignment = Alignment.Center
        ) {
            if (imageUri != null) {
                AsyncImage(model = imageUri, contentDescription = "Product image", modifier = Modifier.size(120.dp))
            } else {
                OutlinedButton(onClick = { imagePicker.launch("image/*") }) { Icon(Icons.Filled.AddAPhoto, contentDescription = "Add photo") }
            }
        }
        if (imageUri != null) {
            TextButton(onClick = { imagePicker.launch("image/*") }, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("Change Photo") }
        }

        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Product Name") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = sku, onValueChange = { sku = it }, label = { Text("SKU") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = barcode, onValueChange = { barcode = it }, label = { Text("Barcode") },
            trailingIcon = {
                IconButton(onClick = { onScanBarcode?.invoke() }) {
                    Icon(Icons.Filled.QrCodeScanner, contentDescription = "Scan barcode")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = brand, onValueChange = { brand = it }, label = { Text("Brand") }, modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = costPrice, onValueChange = { costPrice = it }, label = { Text("Cost Price") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.weight(1f))
            OutlinedTextField(value = sellingPrice, onValueChange = { sellingPrice = it }, label = { Text("Selling Price") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = quantity, onValueChange = { quantity = it }, label = { Text("Quantity") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
            OutlinedTextField(value = lowStock, onValueChange = { lowStock = it }, label = { Text("Low Stock Alert At") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                vm.saveProduct(
                    id = productId, name = name, sku = sku, barcode = barcode.ifBlank { null },
                    category = category, brand = brand,
                    costPrice = costPrice.toDoubleOrNull() ?: 0.0,
                    sellingPrice = sellingPrice.toDoubleOrNull() ?: 0.0,
                    quantity = quantity.toIntOrNull() ?: 0,
                    lowStockThreshold = lowStock.toIntOrNull() ?: 5,
                    imagePath = imageUri?.toString(),
                    onDone = onBack
                )
            },
            enabled = name.isNotBlank() && sku.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) { Text("Save Product") }
        Spacer(Modifier.height(24.dp))
    }
}
