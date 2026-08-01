package com.storemanager.app.ui.screens.settings

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.storemanager.app.StoreManagerApp
import com.storemanager.app.util.BackupUtils
import com.storemanager.app.util.CsvUtils
import com.storemanager.app.util.PrefsManager
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val app = context.applicationContext as StoreManagerApp
    val prefsManager = remember { PrefsManager(context) }
    val darkMode by prefsManager.darkMode.collectAsState(initial = false)
    val scope = rememberCoroutineScope()
    var statusMessage by remember { mutableStateOf<String?>(null) }

    val restoreLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val ok = BackupUtils.restore(context, it)
            statusMessage = if (ok) "Database restored. Please restart the app." else "Restore failed."
        }
    }
    val importCsvLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            scope.launch {
                val products = CsvUtils.importProducts(context, it)
                products.forEach { p -> app.repository.addProduct(p) }
                statusMessage = "Imported ${products.size} products."
            }
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        SettingsRow(icon = Icons.Filled.DarkMode, title = "Dark Mode") {
            Switch(checked = darkMode, onCheckedChange = { scope.launch { prefsManager.setDarkMode(it) } })
        }

        Divider(Modifier.padding(vertical = 8.dp))
        Text("Data Management", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        SettingsActionRow(Icons.Filled.Backup, "Backup Database") {
            val uri = BackupUtils.backup(context)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/octet-stream"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share Backup"))
        }
        SettingsActionRow(Icons.Filled.Restore, "Restore Database") { restoreLauncher.launch("*/*") }
        SettingsActionRow(Icons.Filled.Upload, "Export Products (CSV)") {
            scope.launch {
                val products = app.repository.exportAllProducts()
                val uri = CsvUtils.exportProducts(context, products)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Share CSV"))
            }
        }
        SettingsActionRow(Icons.Filled.Download, "Import Products (CSV)") { importCsvLauncher.launch("text/*") }

        statusMessage?.let {
            Spacer(Modifier.height(16.dp))
            Text(it, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun SettingsRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, trailing: @Composable () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null)
        Spacer(Modifier.width(12.dp))
        Text(title, modifier = Modifier.weight(1f))
        trailing()
    }
}

@Composable
private fun SettingsActionRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = onClick) {
            Icon(icon, contentDescription = null)
            Spacer(Modifier.width(12.dp))
            Text(title)
        }
    }
}
