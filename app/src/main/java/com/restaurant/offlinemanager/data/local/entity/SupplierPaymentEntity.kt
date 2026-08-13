package com.restaurant.offlinemanager.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "supplier_payments",
    indices = [Index("supplierId"), Index("bankCardId"), Index("purchaseId"), Index("date")]
)
data class SupplierPaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val supplierId: Long,
    val bankCardId: Long? = null,
    val purchaseId: Long? = null,
    val amount: Long,
    val date: Long,
    val method: PaymentMethod,
    val notes: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)
