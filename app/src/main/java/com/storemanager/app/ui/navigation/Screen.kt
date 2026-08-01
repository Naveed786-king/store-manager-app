package com.storemanager.app.ui.navigation

sealed class Screen(val route: String) {
    object PinSetup : Screen("pin_setup")
    object PinLogin : Screen("pin_login")
    object Dashboard : Screen("dashboard")

    object Products : Screen("products")
    object ProductAddEdit : Screen("product_add_edit?productId={productId}") {
        fun withId(productId: Long? = null) = "product_add_edit?productId=${productId ?: -1}"
    }
    object DeletedProducts : Screen("deleted_products")

    object Customers : Screen("customers")
    object CustomerAddEdit : Screen("customer_add_edit?customerId={customerId}") {
        fun withId(customerId: Long? = null) = "customer_add_edit?customerId=${customerId ?: -1}"
    }
    object CustomerHistory : Screen("customer_history/{customerId}") {
        fun withId(customerId: Long) = "customer_history/$customerId"
    }

    object Suppliers : Screen("suppliers")
    object SupplierAddEdit : Screen("supplier_add_edit?supplierId={supplierId}") {
        fun withId(supplierId: Long? = null) = "supplier_add_edit?supplierId=${supplierId ?: -1}"
    }
    object SupplierHistory : Screen("supplier_history/{supplierId}") {
        fun withId(supplierId: Long) = "supplier_history/$supplierId"
    }

    object Sales : Screen("sales")
    object CreateInvoice : Screen("create_invoice")
    object InvoiceDetail : Screen("invoice_detail/{saleId}") {
        fun withId(saleId: Long) = "invoice_detail/$saleId"
    }

    object Purchases : Screen("purchases")
    object CreatePurchase : Screen("create_purchase")

    object Inventory : Screen("inventory")
    object Expenses : Screen("expenses")
    object Reports : Screen("reports")
    object Settings : Screen("settings")

    object BarcodeScanner : Screen("barcode_scanner")
}
