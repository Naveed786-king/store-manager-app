package com.storemanager.app.data.dao

import androidx.room.Insert
import androidx.room.Dao
import androidx.room.Query
import com.storemanager.app.data.entity.StockHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface StockHistoryDao {
    @Query("SELECT * FROM stock_history ORDER BY createdAt DESC")
    fun getAll(): Flow<List<StockHistory>>

    @Query("SELECT * FROM stock_history WHERE productId = :productId ORDER BY createdAt DESC")
    fun getForProduct(productId: Long): Flow<List<StockHistory>>

    @Insert
    suspend fun insert(entry: StockHistory)
}
