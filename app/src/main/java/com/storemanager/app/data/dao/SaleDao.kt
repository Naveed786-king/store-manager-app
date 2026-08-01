package com.storemanager.app.data.dao

import androidx.room.*
import com.storemanager.app.data.entity.Sale
import com.storemanager.app.data.entity.SaleItem
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {
    @Query("SELECT * FROM sales ORDER BY createdAt DESC")
    fun getAll(): Flow<List<Sale>>

    @Query("SELECT * FROM sales WHERE customerId = :customerId ORDER BY createdAt DESC")
    fun getByCustomer(customerId: Long): Flow<List<Sale>>

    @Query("SELECT * FROM sales WHERE id = :id")
    suspend fun getById(id: Long): Sale?

    @Query("SELECT * FROM sale_items WHERE saleId = :saleId")
    suspend fun getItems(saleId: Long): List<SaleItem>

    @Query("SELECT * FROM sales WHERE createdAt BETWEEN :start AND :end ORDER BY createdAt DESC")
    fun getBetween(start: Long, end: Long): Flow<List<Sale>>

    @Query("SELECT COALESCE(SUM(total),0) FROM sales WHERE createdAt BETWEEN :start AND :end AND isReturn = 0")
    fun getTotalSalesBetween(start: Long, end: Long): Flow<Double>

    @Insert
    suspend fun insertSale(sale: Sale): Long

    @Insert
    suspend fun insertItems(items: List<SaleItem>)

    @Query("SELECT COUNT(*) FROM sales WHERE invoiceNumber = :invoiceNumber")
    suspend fun countByInvoiceNumber(invoiceNumber: String): Int
}
