package com.restaurant.offlinemanager.domain.usecase

import com.restaurant.offlinemanager.data.local.entity.DeliveryStatus
import com.restaurant.offlinemanager.data.local.entity.billableQuantity
import com.restaurant.offlinemanager.core.utils.PersianDateFormatter
import com.restaurant.offlinemanager.data.local.entity.ProjectStatus
import com.restaurant.offlinemanager.domain.model.DashboardStats
import com.restaurant.offlinemanager.domain.model.RestaurantSnapshot
import com.restaurant.offlinemanager.domain.repository.RestaurantRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class DashboardUseCase(
    private val repository: RestaurantRepository,
    private val projectFinanceUseCase: ProjectFinanceUseCase,
    private val supplierDebtUseCase: SupplierDebtUseCase,
    private val inventoryUseCase: InventoryUseCase,
    private val bankCardBalanceUseCase: BankCardBalanceUseCase
) {
    fun observeDashboard(): Flow<DashboardStats> =
        repository.observeSnapshot().map(::calculate)

    fun calculate(snapshot: RestaurantSnapshot): DashboardStats {
        val today = LocalDate.now()
        val currentMonthKey = PersianDateFormatter.currentMonthKey()
        val inventory = inventoryUseCase.calculateInventory(snapshot)
        return DashboardStats(
            activeProjectsCount = snapshot.projects.count { it.status == ProjectStatus.ACTIVE },
            todayMealCount = snapshot.mealDeliveries
                .filter { it.status == DeliveryStatus.DELIVERED && isSameDay(it.date, today) }
                .sumOf { it.billableQuantity },
            todayPurchasesTotal = snapshot.purchases.filter { isSameDay(it.date, today) }.sumOf { it.totalAmount },
            projectReceivablesTotal = projectFinanceUseCase.receivableTotal(snapshot),
            supplierDebtsTotal = supplierDebtUseCase.debtTotal(snapshot),
            lowStockItemCount = inventory.count { it.isLowStock },
            totalInventoryValue = inventory.sumOf { it.approximateValue },
            bankCardsTotalBalance = bankCardBalanceUseCase.totalBalance(snapshot),
            monthPurchasesTotal = snapshot.purchases.filter { PersianDateFormatter.monthKey(it.date) == currentMonthKey }.sumOf { it.totalAmount },
            monthReceivedTotal = snapshot.projectPayments.filter { PersianDateFormatter.monthKey(it.date) == currentMonthKey }.sumOf { it.amount },
            monthExpensesTotal = snapshot.expenses.filter { PersianDateFormatter.monthKey(it.date) == currentMonthKey }.sumOf { it.amount }
        )
    }

    private fun isSameDay(timestamp: Long, day: LocalDate): Boolean =
        Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate() == day

    fun todayLabel(): String = PersianDateFormatter.formatLong(PersianDateFormatter.nowMillis())
}
