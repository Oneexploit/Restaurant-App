package com.restaurant.offlinemanager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val companyName: String? = null,
    val address: String? = null,
    val managerName: String? = null,
    val phone: String? = null,
    val workerCount: Int,
    val mealPrice: Long,
    val defaultMealType: String,
    val startDate: Long,
    val endDate: Long? = null,
    val status: ProjectStatus,
    val notes: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)
