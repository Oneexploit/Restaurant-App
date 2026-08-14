package com.restaurant.offlinemanager.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "stock_transactions",
    indices = [
        Index("warehouseId"),
        Index("materialId"),
        Index("projectId"),
        Index("supplierId"),
        Index("purchaseId"),
        Index("cookingBatchId"),
        Index("date")
    ]
)
data class StockTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val warehouseId: Long,
    val materialId: Long,
    val projectId: Long? = null,
    val supplierId: Long? = null,
    val purchaseId: Long? = null,
    val cookingBatchId: Long? = null,
    val type: StockTransactionType,
    val reason: StockReason,
    val quantity: Double,
    val unit: UnitType,
    val unitPrice: Long? = null,
    val totalAmount: Long? = null,
    val date: Long,
    val notes: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)
