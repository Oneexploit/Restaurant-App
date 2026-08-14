package com.restaurant.offlinemanager.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses",
    indices = [Index("bankCardId"), Index("projectId"), Index("cookingBatchId"), Index("date")]
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: ExpenseCategory,
    val amount: Long,
    val date: Long,
    val bankCardId: Long? = null,
    val projectId: Long? = null,
    val cookingBatchId: Long? = null,
    val notes: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)
