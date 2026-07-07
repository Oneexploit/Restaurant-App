package com.restaurant.offlinemanager.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "purchase_items",
    indices = [Index("purchaseId"), Index("materialId")]
)
data class PurchaseItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val purchaseId: Long,
    val materialId: Long,
    val quantity: Double,
    val unit: UnitType,
    val unitPrice: Long,
    val totalAmount: Long,
    val createdAt: Long,
    val updatedAt: Long
)
