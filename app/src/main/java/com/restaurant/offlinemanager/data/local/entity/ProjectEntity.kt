package com.restaurant.offlinemanager.data.local.entity

import androidx.room.Entity
import androidx.room.ColumnInfo
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
    @ColumnInfo(defaultValue = "0") val breakfastPrice: Long = mealPrice,
    @ColumnInfo(defaultValue = "0") val lunchPrice: Long = mealPrice,
    @ColumnInfo(defaultValue = "0") val dinnerPrice: Long = mealPrice,
    val defaultMealType: String,
    val startDate: Long,
    val endDate: Long? = null,
    val status: ProjectStatus,
    val notes: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)

fun ProjectEntity.priceFor(mealType: MealType): Long = when (mealType) {
    MealType.BREAKFAST -> breakfastPrice.takeIf { it > 0 } ?: mealPrice
    MealType.LUNCH -> lunchPrice.takeIf { it > 0 } ?: mealPrice
    MealType.DINNER -> dinnerPrice.takeIf { it > 0 } ?: mealPrice
}
