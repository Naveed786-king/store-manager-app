package com.storemanager.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val sku: String,
    val barcode: String? = null,
    val category: String = "General",
    val brand: String = "",
    val costPrice: Double,
    val sellingPrice: Double,
    val quantity: Int,
    val lowStockThreshold: Int = 5,
    val imagePath: String? = null,
    val isDeleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
