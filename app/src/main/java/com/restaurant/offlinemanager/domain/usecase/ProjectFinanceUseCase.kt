package com.restaurant.offlinemanager.domain.usecase

import com.restaurant.offlinemanager.domain.model.ProjectFinance
import com.restaurant.offlinemanager.domain.model.RestaurantSnapshot
import com.restaurant.offlinemanager.domain.repository.RestaurantRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProjectFinanceUseCase(private val repository: RestaurantRepository) {
    fun observeProjectFinances(): Flow<List<ProjectFinance>> =
        repository.observeSnapshot().map(::calculateProjectFinances)

    fun calculateProjectFinances(snapshot: RestaurantSnapshot): List<ProjectFinance> =
        snapshot.projects.map { project ->
            val deliveries = snapshot.mealDeliveries.filter { it.projectId == project.id }
            val delivered = deliveries.sumOf { it.totalAmount }
            val paid = snapshot.projectPayments.filter { it.projectId == project.id }.sumOf { it.amount }
            ProjectFinance(
                project = project,
                totalDelivered = delivered,
                totalPaid = paid,
                receivable = delivered - paid,
                totalMeals = deliveries.sumOf { it.quantity }
            )
        }.sortedByDescending { it.receivable }

    fun receivableTotal(snapshot: RestaurantSnapshot): Long =
        calculateProjectFinances(snapshot).sumOf { it.receivable.coerceAtLeast(0) }
}
