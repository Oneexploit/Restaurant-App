package com.restaurant.offlinemanager.domain.usecase

import com.restaurant.offlinemanager.core.utils.MoneyFormatter
import com.restaurant.offlinemanager.core.utils.NumberFormatter
import com.restaurant.offlinemanager.core.utils.PersianDateFormatter
import com.restaurant.offlinemanager.domain.model.MonthlyPoint
import com.restaurant.offlinemanager.domain.model.RestaurantSnapshot
import com.restaurant.offlinemanager.domain.model.label
import com.restaurant.offlinemanager.domain.repository.RestaurantRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ReportsUseCase(
    private val repository: RestaurantRepository,
    private val projectFinanceUseCase: ProjectFinanceUseCase,
    private val supplierDebtUseCase: SupplierDebtUseCase,
    private val inventoryUseCase: InventoryUseCase
) {
    fun observeMonthlySummary(): Flow<List<MonthlyPoint>> =
        repository.observeSnapshot().map(::monthlySummary)

    fun monthlySummary(snapshot: RestaurantSnapshot): List<MonthlyPoint> {
        val keys = (snapshot.projectPayments.map { PersianDateFormatter.monthKey(it.date) } +
            snapshot.expenses.map { PersianDateFormatter.monthKey(it.date) } +
            snapshot.purchases.map { PersianDateFormatter.monthKey(it.date) })
            .distinct()
            .sorted()
            .takeLast(6)
        return keys.map { key ->
            MonthlyPoint(
                label = NumberFormatter.toPersianDigits(key),
                income = snapshot.projectPayments.filter { PersianDateFormatter.monthKey(it.date) == key }.sumOf { it.amount },
                expense = snapshot.expenses.filter { PersianDateFormatter.monthKey(it.date) == key }.sumOf { it.amount },
                purchases = snapshot.purchases.filter { PersianDateFormatter.monthKey(it.date) == key }.sumOf { it.totalAmount }
            )
        }
    }

    fun purchasesCsv(snapshot: RestaurantSnapshot): String = buildString {
        appendLine("date,supplier,invoice,payment,total,paid")
        snapshot.purchases.forEach { purchase ->
            val supplier = snapshot.suppliers.firstOrNull { it.id == purchase.supplierId }?.name.orEmpty()
            appendLine("${PersianDateFormatter.format(purchase.date)},$supplier,${purchase.invoiceNumber.orEmpty()},${purchase.paymentType.label()},${purchase.totalAmount},${purchase.paidAmount}")
        }
    }

    fun inventoryCsv(snapshot: RestaurantSnapshot): String = buildString {
        appendLine("warehouse,material,quantity,unit,value,status")
        inventoryUseCase.calculateInventory(snapshot).forEach { item ->
            appendLine("${item.warehouseName},${item.materialName},${item.quantity},${item.unit.label()},${item.approximateValue},${if (item.isLowStock) "low" else "normal"}")
        }
    }

    fun receivablesCsv(snapshot: RestaurantSnapshot): String = buildString {
        appendLine("project,total_delivered,paid,remaining")
        projectFinanceUseCase.calculateProjectFinances(snapshot).forEach {
            appendLine("${it.project.name},${it.totalDelivered},${it.totalPaid},${it.receivable}")
        }
    }

    fun supplierDebtsCsv(snapshot: RestaurantSnapshot): String = buildString {
        appendLine("supplier,credit_purchases,paid,remaining")
        supplierDebtUseCase.calculateSupplierDebts(snapshot).forEach {
            appendLine("${it.supplier.name},${it.totalCreditPurchases},${it.totalPaid},${it.remaining}")
        }
    }

    fun paymentsCsv(snapshot: RestaurantSnapshot): String = buildString {
        appendLine("kind,date,name,amount,method")
        snapshot.projectPayments.forEach { payment ->
            val project = snapshot.projects.firstOrNull { it.id == payment.projectId }?.name.orEmpty()
            appendLine("project,${PersianDateFormatter.format(payment.date)},$project,${payment.amount},${payment.method.label()}")
        }
        snapshot.supplierPayments.forEach { payment ->
            val supplier = snapshot.suppliers.firstOrNull { it.id == payment.supplierId }?.name.orEmpty()
            appendLine("supplier,${PersianDateFormatter.format(payment.date)},$supplier,${payment.amount},${payment.method.label()}")
        }
    }

    fun humanSummary(snapshot: RestaurantSnapshot): List<Pair<String, String>> =
        listOf(
            "ارزش انبار" to MoneyFormatter.format(inventoryUseCase.calculateInventory(snapshot).sumOf { it.approximateValue }),
            "مطالبات پروژه‌ها" to MoneyFormatter.format(projectFinanceUseCase.receivableTotal(snapshot)),
            "بدهی تامین‌کنندگان" to MoneyFormatter.format(supplierDebtUseCase.debtTotal(snapshot))
        )
}
