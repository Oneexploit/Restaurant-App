package com.restaurant.offlinemanager.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cooking_batches",
    indices = [Index("warehouseId"), Index("date")]
)
data class CookingBatchEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val warehouseId: Long,
    val date: Long,
    val mealType: MealType,
    val producedQuantity: Int,
    val notes: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(
    tableName = "cooking_allocations",
    indices = [Index("batchId"), Index("projectId")]
)
data class CookingAllocationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val batchId: Long,
    val projectId: Long,
    val quantity: Int,
    val createdAt: Long,
    val updatedAt: Long
)
