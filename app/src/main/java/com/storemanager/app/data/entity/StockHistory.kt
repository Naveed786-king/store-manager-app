package com.storemanager.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stock_history")
data class StockHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    val productName: String,
    val changeType: String,
    val quantityChange: Int,
    val newQuantity: Int,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
