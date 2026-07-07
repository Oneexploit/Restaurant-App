package com.restaurant.offlinemanager.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "purchases",
    indices = [Index("supplierId"), Index("warehouseId"), Index("date"), Index("bankCardId")]
)
data class PurchaseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val supplierId: Long? = null,
    val warehouseId: Long,
    val date: Long,
    val invoiceNumber: String? = null,
    val paymentType: PurchasePaymentType,
    val bankCardId: Long? = null,
    val discountAmount: Long,
    val totalAmount: Long,
    val paidAmount: Long,
    val notes: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)
