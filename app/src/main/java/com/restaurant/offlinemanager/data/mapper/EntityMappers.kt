package com.restaurant.offlinemanager.data.mapper

import com.restaurant.offlinemanager.data.local.entity.ProjectEntity
import com.restaurant.offlinemanager.domain.model.ProjectInput

fun ProjectInput.toEntity(createdAt: Long, updatedAt: Long): ProjectEntity =
    ProjectEntity(
        id = id,
        name = name.trim(),
        companyName = companyName?.trim()?.ifBlank { null },
        address = address?.trim()?.ifBlank { null },
        managerName = managerName?.trim()?.ifBlank { null },
        phone = phone?.trim()?.ifBlank { null },
        workerCount = workerCount,
        mealPrice = mealPrice,
        breakfastPrice = breakfastPrice,
        lunchPrice = lunchPrice,
        dinnerPrice = dinnerPrice,
        defaultMealType = defaultMealType,
        startDate = startDate,
        endDate = endDate,
        status = status,
        notes = notes?.trim()?.ifBlank { null },
        createdAt = createdAt,
        updatedAt = updatedAt
    )
