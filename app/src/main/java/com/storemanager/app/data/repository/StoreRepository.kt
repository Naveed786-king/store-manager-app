package com.storemanager.app.data.repository

import com.storemanager.app.data.AppDatabase
import com.storemanager.app.data.entity.*
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class StoreRepository(private val db: AppDatabase) {

    // ---------- Products ----------
    fun getProducts(): Flow<List<Product>> = db.productDao().getAll()
    fun getDeletedProducts(): Flow<List<Product>> = db.productDao().getDeleted()
    fun searchProducts(q: String): Flow<List<Product>> = db.productDao().search(q)
    fun getLowStock(): Flow<List<Product>> = db.productDao().getLowStock()
    fun getTotalProductCount(): Flow<Int> = db.productDao().getTotalCount()
    suspend fun getProductById(id: Long) = db.productDao().getById(id)
    suspend fun findByBarcode(barcode: String) = db.productDao().findByBarcode(barcode)

    suspend fun addProduct(product: Product): Long = db.productDao().insert(product)
    suspend fun updateProduct(product: Product) = db.productDao().update(product.copy(updatedAt = System.currentTimeMillis()))
    suspend fun deleteProduct(id: Long) = db.productDao().softDelete(id)
    suspend fun restoreProduct(id: Long) = db.productDao().restore(id)

    suspend fun adjustStock(productId: Long, productName: String, delta: Int, type: String, note: String = "") {
        db.productDao().adjustQuantity(productId, delta)
        val product = db.productDao().getById(productId)
        db.stockHistoryDao().insert(
            StockHistory(
                productId = productId,
                productName = productName,
                changeType = type,
                quantityChange = delta,
                newQuantity = product?.quantity ?: 0,
                note = note
            )
        )
    }

    fun getStockHistory(): Flow<List<StockHistory>> = db.stockHistoryDao().getAll()
    fun getStockHistoryForProduct(productId: Long): Flow<List<StockHistory>> = db.stockHistoryDao().getForProduct(productId)

    suspend fun exportAllProducts() = db.productDao().getAllForExport()

    // ---------- Customers ----------
    fun getCustomers(): Flow<List<Customer>> = db.customerDao().getAll()
    fun searchCustomers(q: String): Flow<List<Customer>> = db.customerDao().search(q)
    suspend fun getCustomerById(id: Long) = db.customerDao().getById(id)
    suspend fun addCustomer(customer: Customer): Long = db.customerDao().insert(customer)
    suspend fun updateCustomer(customer: Customer) = db.customerDao().update(customer)
    suspend fun deleteCustomer(customer: Customer) = db.customerDao().delete(customer)
    fun getCustomerSales(customerId: Long): Flow<List<Sale>> = db.saleDao().getByCustomer(customerId)

    // ---------- Suppliers ----------
    fun getSuppliers(): Flow<List<Supplier>> = db.supplierDao().getAll()
    suspend fun getSupplierById(id: Long) = db.supplierDao().getById(id)
    suspend fun addSupplier(supplier: Supplier): Long = db.supplierDao().insert(supplier)
    suspend fun updateSupplier(supplier: Supplier) = db.supplierDao().update(supplier)
    suspend fun deleteSupplier(supplier: Supplier) = db.supplierDao().delete(supplier)
    fun getSupplierPurchases(supplierId: Long): Flow<List<Purchase>> = db.purchaseDao().getBySupplier(supplierId)

    // ---------- Sales ----------
    fun getSales(): Flow<List<Sale>> = db.saleDao().getAll()
    suspend fun getSaleById(id: Long) = db.saleDao().getById(id)
    suspend fun getSaleItems(saleId: Long) = db.saleDao().getItems(saleId)
    fun getSalesBetween(start: Long, end: Long): Flow<List<Sale>> = db.saleDao().getBetween(start, end)
    fun getTotalSalesBetween(start: Long, end: Long): Flow<Double> = db.saleDao().getTotalSalesBetween(start, end)

    suspend fun createSale(sale: Sale, items: List<SaleItem>): Long {
        val saleId = db.saleDao().insertSale(sale)
        val itemsWithSaleId = items.map { it.copy(saleId = saleId) }
        db.saleDao().insertItems(itemsWithSaleId)
        for (item in itemsWithSaleId) {
            adjustStock(item.productId, item.productName, -item.quantity, "SALE", "Invoice ${sale.invoiceNumber}")
        }
        return saleId
    }

    suspend fun createReturn(originalSale: Sale, items: List<SaleItem>): Long {
        val returnSale = Sale(
            invoiceNumber = "${originalSale.invoiceNumber}-R",
            customerId = originalSale.customerId,
            subtotal = -items.sumOf { it.lineTotal },
            total = -items.sumOf { it.lineTotal },
            isReturn = true,
            originalSaleId = originalSale.id
        )
        val saleId = db.saleDao().insertSale(returnSale)
        val itemsWithSaleId = items.map { it.copy(saleId = saleId, quantity = -it.quantity, lineTotal = -it.lineTotal) }
        db.saleDao().insertItems(itemsWithSaleId)
        for (item in itemsWithSaleId) {
            adjustStock(item.productId, item.productName, -item.quantity, "RETURN", "Return of ${originalSale.invoiceNumber}")
        }
        return saleId
    }

    suspend fun nextInvoiceNumber(): String {
        val prefix = "INV-" + java.text.SimpleDateFormat("yyyyMMdd").format(java.util.Date())
        var counter = 1
        var candidate = "$prefix-$counter"
        while (db.saleDao().countByInvoiceNumber(candidate) > 0) {
            counter++
            candidate = "$prefix-$counter"
        }
        return candidate
    }

    // ---------- Purchases ----------
    fun getPurchases(): Flow<List<Purchase>> = db.purchaseDao().getAll()
    suspend fun getPurchaseItems(purchaseId: Long) = db.purchaseDao().getItems(purchaseId)

    suspend fun createPurchase(purchase: Purchase, items: List<PurchaseItem>): Long {
        val purchaseId = db.purchaseDao().insertPurchase(purchase)
        val itemsWithId = items.map { it.copy(purchaseId = purchaseId) }
        db.purchaseDao().insertItems(itemsWithId)
        for (item in itemsWithId) {
            adjustStock(item.productId, item.productName, item.quantity, "PURCHASE", "Ref ${purchase.referenceNumber}")
        }
        return purchaseId
    }

    // ---------- Expenses ----------
    fun getExpenses(): Flow<List<Expense>> = db.expenseDao().getAll()
    fun getExpensesBetween(start: Long, end: Long): Flow<List<Expense>> = db.expenseDao().getBetween(start, end)
    fun getTotalExpensesBetween(start: Long, end: Long): Flow<Double> = db.expenseDao().getTotalBetween(start, end)
    suspend fun addExpense(expense: Expense): Long = db.expenseDao().insert(expense)
    fun getExpenseCategories(): Flow<List<ExpenseCategory>> = db.expenseDao().getCategories()
    suspend fun addExpenseCategory(category: ExpenseCategory): Long = db.expenseDao().insertCategory(category)
    suspend fun deleteExpenseCategory(category: ExpenseCategory) = db.expenseDao().deleteCategory(category)

    // ---------- Time range helpers ----------
    companion object {
        fun startOfToday(): Long {
            val cal = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
            return cal.timeInMillis
        }
        fun endOfToday(): Long = startOfToday() + 24L * 60 * 60 * 1000 - 1

        fun startOfMonth(): Long {
            val cal = Calendar.getInstance()
            cal.set(Calendar.DAY_OF_MONTH, 1); cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
            return cal.timeInMillis
        }
        fun endOfMonth(): Long {
            val cal = Calendar.getInstance()
            cal.timeInMillis = startOfMonth()
            cal.add(Calendar.MONTH, 1)
            cal.add(Calendar.MILLISECOND, -1)
            return cal.timeInMillis
        }
        fun startOfWeek(): Long {
            val cal = Calendar.getInstance()
            cal.firstDayOfWeek = Calendar.MONDAY
            cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
            return cal.timeInMillis
        }
        fun endOfWeek(): Long = startOfWeek() + 7L * 24 * 60 * 60 * 1000 - 1
    }
}
