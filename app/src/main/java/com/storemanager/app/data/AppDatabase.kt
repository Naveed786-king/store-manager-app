package com.storemanager.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.storemanager.app.data.dao.*
import com.storemanager.app.data.entity.*

@Database(
    entities = [
        Product::class, Customer::class, Supplier::class,
        Sale::class, SaleItem::class, Purchase::class, PurchaseItem::class,
        Expense::class, ExpenseCategory::class, StockHistory::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun customerDao(): CustomerDao
    abstract fun supplierDao(): SupplierDao
    abstract fun saleDao(): SaleDao
    abstract fun purchaseDao(): PurchaseDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun stockHistoryDao(): StockHistoryDao

    companion object {
        const val DB_NAME = "store_manager.db"

        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                ).build()
                INSTANCE = instance
                instance
            }
        }

        fun closeAndClear() {
            INSTANCE?.close()
            INSTANCE = null
        }
    }
}
