package com.storemanager.app.data.dao

import androidx.room.*
import com.storemanager.app.data.entity.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE isDeleted = 0 ORDER BY name ASC")
    fun getAll(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE isDeleted = 1 ORDER BY name ASC")
    fun getDeleted(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE isDeleted = 0 AND (name LIKE '%' || :q || '%' OR sku LIKE '%' || :q || '%' OR barcode LIKE '%' || :q || '%')")
    fun search(q: String): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE barcode = :barcode LIMIT 1")
    suspend fun findByBarcode(barcode: String): Product?

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getById(id: Long): Product?

    @Query("SELECT * FROM products WHERE isDeleted = 0 AND quantity <= lowStockThreshold")
    fun getLowStock(): Flow<List<Product>>

    @Query("SELECT COUNT(*) FROM products WHERE isDeleted = 0")
    fun getTotalCount(): Flow<Int>

    @Insert
    suspend fun insert(product: Product): Long

    @Update
    suspend fun update(product: Product)

    @Query("UPDATE products SET isDeleted = 1 WHERE id = :id")
    suspend fun softDelete(id: Long)

    @Query("UPDATE products SET isDeleted = 0 WHERE id = :id")
    suspend fun restore(id: Long)

    @Query("UPDATE products SET quantity = quantity + :delta, updatedAt = :ts WHERE id = :id")
    suspend fun adjustQuantity(id: Long, delta: Int, ts: Long = System.currentTimeMillis())

    @Query("SELECT * FROM products")
    suspend fun getAllForExport(): List<Product>
}
