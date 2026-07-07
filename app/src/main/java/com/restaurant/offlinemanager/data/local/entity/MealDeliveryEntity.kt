package com.restaurant.offlinemanager.data.local.entity

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
    val mealType: MealType,
    val quantity: Int,
    val unitPrice: Long,
    val totalAmount: Long,
    val notes: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)
