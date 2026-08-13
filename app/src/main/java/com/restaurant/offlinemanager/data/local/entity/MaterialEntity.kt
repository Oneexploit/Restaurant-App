package com.restaurant.offlinemanager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "materials")
data class MaterialEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val mainUnit: UnitType,
    val minimumStock: Double,
    val imageEmoji: String? = null,
    val notes: String? = null,
    val isActive: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)
