package com.restaurant.offlinemanager.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "project_payments",
    indices = [Index("projectId"), Index("bankCardId"), Index("date")]
)
data class ProjectPaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val bankCardId: Long? = null,
    val amount: Long,
    val date: Long,
    val method: PaymentMethod,
    val notes: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)
