package com.restaurant.offlinemanager.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "meal_deliveries",
    indices = [Index("projectId"), Index("date")]
)
data class MealDeliveryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val date: Long,
    val deliveryTimeMinutes: Int? = null,
    val mealType: MealType,
    @ColumnInfo(defaultValue = "'DELIVERED'") val status: DeliveryStatus = DeliveryStatus.DELIVERED,
    val quantity: Int,
    @ColumnInfo(defaultValue = "0") val returnedQuantity: Int = 0,
    val unitPrice: Long,
    val totalAmount: Long,
    val recipientName: String? = null,
    val recipientPhone: String? = null,
    val notes: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)

val MealDeliveryEntity.billableQuantity: Int
    get() = if (status == DeliveryStatus.DELIVERED) (quantity - returnedQuantity).coerceAtLeast(0) else 0
