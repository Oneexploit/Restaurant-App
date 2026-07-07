package com.restaurant.offlinemanager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "material_categories")
data class MaterialCategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val iconName: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)
