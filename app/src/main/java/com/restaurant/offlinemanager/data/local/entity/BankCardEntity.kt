package com.restaurant.offlinemanager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bank_cards")
data class BankCardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val ownerName: String? = null,
    val bankName: String? = null,
    val cardNumber: String? = null,
    val initialBalance: Long,
    val isActive: Boolean,
    val notes: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)
