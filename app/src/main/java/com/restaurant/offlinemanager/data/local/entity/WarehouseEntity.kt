package com.restaurant.offlinemanager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "warehouses")
data class WarehouseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: WarehouseType,
    val address: String? = null,
    val notes: String? = null,
    val isActive: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)
