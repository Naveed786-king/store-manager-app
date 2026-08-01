package com.storemanager.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sales")
data class Sale(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceNumber: String,
    val customerId: Long? = null,
    val subtotal: Double,
    val discount: Double = 0.0,
    val tax: Double = 0.0,
    val total: Double,
    val paymentMethod: String = "Cash",
    val isReturn: Boolean = false,
    val originalSaleId: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
