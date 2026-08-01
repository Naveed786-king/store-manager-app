package com.storemanager.app.data.dao

import androidx.room.*
import com.storemanager.app.data.entity.Purchase
import com.storemanager.app.data.entity.PurchaseItem
import kotlinx.coroutines.flow.Flow

@Dao
interface PurchaseDao {
    @Query("SELECT * FROM purchases ORDER BY createdAt DESC")
    fun getAll(): Flow<List<Purchase>>

    @Query("SELECT * FROM purchases WHERE supplierId = :supplierId ORDER BY createdAt DESC")
    fun getBySupplier(supplierId: Long): Flow<List<Purchase>>

    @Query("SELECT * FROM purchase_items WHERE purchaseId = :purchaseId")
    suspend fun getItems(purchaseId: Long): List<PurchaseItem>

    @Insert
    suspend fun insertPurchase(purchase: Purchase): Long

    @Insert
    suspend fun insertItems(items: List<PurchaseItem>)
}
