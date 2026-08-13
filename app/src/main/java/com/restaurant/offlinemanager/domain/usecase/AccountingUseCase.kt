package com.restaurant.offlinemanager.domain.usecase

import com.restaurant.offlinemanager.core.utils.PersianDateFormatter
import com.restaurant.offlinemanager.data.local.entity.PurchasePaymentType
import com.restaurant.offlinemanager.data.local.entity.DeliveryStatus
import com.restaurant.offlinemanager.domain.model.AccountingSummary
import com.restaurant.offlinemanager.domain.model.ProjectProfit
import com.restaurant.offlinemanager.domain.model.RestaurantSnapshot

class AccountingUseCase(private val inventoryUseCase: InventoryUseCase) {
    fun summary(snapshot: RestaurantSnapshot, monthKey: String? = null): AccountingSummary {
        fun inPeriod(timestamp: Long): Boolean = monthKey == null || PersianDateFormatter.monthKey(timestamp) == monthKey
        val earnedRevenue = snapshot.mealDeliveries
            .filter { it.status == DeliveryStatus.DELIVERED && inPeriod(it.date) }
            .sumOf { it.totalAmount }
        val cashReceived = snapshot.projectPayments.filter { inPeriod(it.date) }.sumOf { it.amount }
        val paidPurchases = snapshot.purchases
            .filter { inPeriod(it.date) && it.paymentType != PurchasePaymentType.CREDIT }
            .sumOf { it.paidAmount }
        val supplierPayments = snapshot.supplierPayments.filter { inPeriod(it.date) }.sumOf { it.amount }
        val expenses = snapshot.expenses.filter { inPeriod(it.date) }.sumOf { it.amount }

        val costOfGoods = inventoryUseCase.costOfGoodsConsumed(snapshot, monthKey)
        val waste = inventoryUseCase.wasteLoss(snapshot, monthKey)
        val grossProfit = earnedRevenue - costOfGoods
        return AccountingSummary(
            earnedRevenue = earnedRevenue,
            cashReceived = cashReceived,
            cashPaidForPurchases = paidPurchases,
            cashPaidToSuppliers = supplierPayments,
            operatingExpenses = expenses,
            costOfGoodsConsumed = costOfGoods,
            wasteLoss = waste,
            grossProfit = grossProfit,
            netProfit = grossProfit - expenses - waste,
            netCashFlow = cashReceived - paidPurchases - supplierPayments - expenses
        )
    }

    fun projectProfits(snapshot: RestaurantSnapshot): List<ProjectProfit> {
        val costs = inventoryUseCase.projectMaterialCosts(snapshot)
        return snapshot.projects.map { project ->
            val revenue = snapshot.mealDeliveries
                .filter { it.projectId == project.id && it.status == DeliveryStatus.DELIVERED }
                .sumOf { it.totalAmount }
            val materialCost = costs[project.id] ?: 0L
            val profit = revenue - materialCost
            ProjectProfit(
                project = project,
                earnedRevenue = revenue,
                materialCost = materialCost,
                grossProfit = profit,
                marginPercent = if (revenue > 0) profit * 100.0 / revenue else 0.0
            )
        }.sortedByDescending { it.grossProfit }
    }
}
