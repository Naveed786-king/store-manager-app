package com.storemanager.app.ui.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.storemanager.app.ui.screens.auth.PinLoginScreen
import com.storemanager.app.ui.screens.auth.PinSetupScreen
import com.storemanager.app.ui.screens.customers.CustomerAddEditScreen
import com.storemanager.app.ui.screens.customers.CustomerHistoryScreen
import com.storemanager.app.ui.screens.customers.CustomerListScreen
import com.storemanager.app.ui.screens.dashboard.DashboardScreen
import com.storemanager.app.ui.screens.expenses.ExpensesScreen
import com.storemanager.app.ui.screens.inventory.InventoryScreen
import com.storemanager.app.ui.screens.products.DeletedProductsScreen
import com.storemanager.app.ui.screens.products.ProductAddEditScreen
import com.storemanager.app.ui.screens.products.ProductListScreen
import com.storemanager.app.ui.screens.purchases.CreatePurchaseScreen
import com.storemanager.app.ui.screens.purchases.PurchasesListScreen
import com.storemanager.app.ui.screens.reports.ReportsScreen
import com.storemanager.app.ui.screens.sales.CreateInvoiceScreen
import com.storemanager.app.ui.screens.sales.InvoiceDetailScreen
import com.storemanager.app.ui.screens.sales.SalesListScreen
import com.storemanager.app.ui.screens.scanner.BarcodeScannerScreen
import com.storemanager.app.ui.screens.settings.SettingsScreen
import com.storemanager.app.ui.screens.suppliers.SupplierAddEditScreen
import com.storemanager.app.ui.screens.suppliers.SupplierHistoryScreen
import com.storemanager.app.ui.screens.suppliers.SupplierListScreen
import com.storemanager.app.util.PrefsManager

private data class BottomDest(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

private val bottomDestinations = listOf(
    BottomDest(Screen.Dashboard.route, "Home", Icons.Filled.Dashboard),
    BottomDest(Screen.Products.route, "Products", Icons.Filled.Inventory2),
    BottomDest(Screen.Sales.route, "Sales", Icons.Filled.PointOfSale),
    BottomDest(Screen.Inventory.route, "Stock", Icons.Filled.Warehouse),
    BottomDest(Screen.Settings.route, "More", Icons.Filled.Menu)
)

@Composable
fun StoreManagerNavHost() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val prefsManager = remember { PrefsManager(context) }
    val isSetup by prefsManager.isSetup.collectAsState(initial = null)
    var unlocked by remember { mutableStateOf(false) }

    when (isSetup) {
        null -> { /* loading */ }
        false -> PinSetupScreen(onSetupComplete = { unlocked = true; navController.navigate(Screen.Dashboard.route) })
        true -> {
            if (!unlocked) {
                PinLoginScreen(onUnlocked = { unlocked = true })
            } else {
                MainScaffold(navController)
            }
        }
    }
}

@Composable
private fun MainScaffold(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomDestinations.map { it.route }) {
                NavigationBar {
                    bottomDestinations.forEach { dest ->
                        NavigationBarItem(
                            selected = currentRoute == dest.route,
                            onClick = {
                                navController.navigate(dest.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(dest.icon, contentDescription = dest.label) },
                            label = { Text(dest.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            NavHost(navController = navController, startDestination = Screen.Dashboard.route) {
                composable(Screen.Dashboard.route) { DashboardScreen(onNavigate = { navController.navigate(it) }) }

                composable(Screen.Products.route) {
                    ProductListScreen(
                        onAddProduct = { navController.navigate(Screen.ProductAddEdit.withId(null)) },
                        onEditProduct = { id -> navController.navigate(Screen.ProductAddEdit.withId(id)) },
                        onViewDeleted = { navController.navigate(Screen.DeletedProducts.route) }
                    )
                }
                composable(
                    Screen.ProductAddEdit.route,
                    arguments = listOf(navArgument("productId") { type = NavType.LongType; defaultValue = -1L })
                ) { backEntry ->
                    val productId = backEntry.arguments?.getLong("productId") ?: -1L
                    val scannedBarcode by backEntry.savedStateHandle
                        .getStateFlow<String?>("scanned_barcode", null)
                        .collectAsState()
                    ProductAddEditScreen(
                        productId = productId,
                        onBack = { navController.popBackStack() },
                        onScanBarcode = { navController.navigate(Screen.BarcodeScanner.route) },
                        scannedBarcode = scannedBarcode
                    )
                }
                composable(Screen.DeletedProducts.route) { DeletedProductsScreen(onBack = { navController.popBackStack() }) }
                composable(Screen.BarcodeScanner.route) {
                    BarcodeScannerScreen(
                        onResult = { value ->
                            navController.previousBackStackEntry?.savedStateHandle?.set("scanned_barcode", value)
                            navController.popBackStack()
                        },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.Customers.route) {
                    CustomerListScreen(
                        onAdd = { navController.navigate(Screen.CustomerAddEdit.withId(null)) },
                        onEdit = { id -> navController.navigate(Screen.CustomerAddEdit.withId(id)) },
                        onHistory = { id -> navController.navigate(Screen.CustomerHistory.withId(id)) }
                    )
                }
                composable(
                    Screen.CustomerAddEdit.route,
                    arguments = listOf(navArgument("customerId") { type = NavType.LongType; defaultValue = -1L })
                ) { backEntry ->
                    CustomerAddEditScreen(backEntry.arguments?.getLong("customerId"), onBack = { navController.popBackStack() })
                }
                composable(
                    Screen.CustomerHistory.route,
                    arguments = listOf(navArgument("customerId") { type = NavType.LongType })
                ) { backEntry ->
                    CustomerHistoryScreen(backEntry.arguments?.getLong("customerId") ?: 0L, onBack = { navController.popBackStack() })
                }

                composable(Screen.Suppliers.route) {
                    SupplierListScreen(
                        onAdd = { navController.navigate(Screen.SupplierAddEdit.withId(null)) },
                        onEdit = { id -> navController.navigate(Screen.SupplierAddEdit.withId(id)) },
                        onHistory = { id -> navController.navigate(Screen.SupplierHistory.withId(id)) }
                    )
                }
                composable(
                    Screen.SupplierAddEdit.route,
                    arguments = listOf(navArgument("supplierId") { type = NavType.LongType; defaultValue = -1L })
                ) { backEntry ->
                    SupplierAddEditScreen(backEntry.arguments?.getLong("supplierId"), onBack = { navController.popBackStack() })
                }
                composable(
                    Screen.SupplierHistory.route,
                    arguments = listOf(navArgument("supplierId") { type = NavType.LongType })
                ) { backEntry ->
                    SupplierHistoryScreen(backEntry.arguments?.getLong("supplierId") ?: 0L, onBack = { navController.popBackStack() })
                }

                composable(Screen.Sales.route) {
                    SalesListScreen(
                        onNewSale = { navController.navigate(Screen.CreateInvoice.route) },
                        onOpenInvoice = { id -> navController.navigate(Screen.InvoiceDetail.withId(id)) }
                    )
                }
                composable(Screen.CreateInvoice.route) {
                    CreateInvoiceScreen(
                        onBack = { navController.popBackStack() },
                        onCheckoutComplete = { id ->
                            navController.popBackStack()
                            navController.navigate(Screen.InvoiceDetail.withId(id))
                        }
                    )
                }
                composable(
                    Screen.InvoiceDetail.route,
                    arguments = listOf(navArgument("saleId") { type = NavType.LongType })
                ) { backEntry ->
                    InvoiceDetailScreen(backEntry.arguments?.getLong("saleId") ?: 0L, onBack = { navController.popBackStack() })
                }

                composable(Screen.Purchases.route) {
                    PurchasesListScreen(onNewPurchase = { navController.navigate(Screen.CreatePurchase.route) })
                }
                composable(Screen.CreatePurchase.route) {
                    CreatePurchaseScreen(onBack = { navController.popBackStack() })
                }

                composable(Screen.Inventory.route) { InventoryScreen() }
                composable(Screen.Expenses.route) { ExpensesScreen() }
                composable(Screen.Reports.route) { ReportsScreen() }
                composable(Screen.Settings.route) { SettingsHubScreen(navController) }
            }
        }
    }
}

@Composable
private fun SettingsHubScreen(navController: NavHostController) {
    androidx.compose.foundation.layout.Column(Modifier.fillMaxSize()) {
        androidx.compose.foundation.layout.Column(Modifier.padding(16.dp)) {
            Text("More", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(12.dp))
            listOf(
                "Customers" to Screen.Customers.route,
                "Suppliers" to Screen.Suppliers.route,
                "Purchases" to Screen.Purchases.route,
                "Expenses" to Screen.Expenses.route,
                "Reports" to Screen.Reports.route
            ).forEach { (label, route) ->
                ListItem(
                    headlineContent = { Text(label) },
                    modifier = Modifier.clickable { navController.navigate(route) }
                )
                Divider()
            }
        }
        Divider()
        SettingsScreen()
    }
}
