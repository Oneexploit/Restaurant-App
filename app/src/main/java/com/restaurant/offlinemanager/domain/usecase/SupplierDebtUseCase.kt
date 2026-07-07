package com.restaurant.offlinemanager.domain.usecase

import com.restaurant.offlinemanager.data.local.entity.PurchasePaymentType
import com.restaurant.offlinemanager.domain.model.RestaurantSnapshot
import com.restaurant.offlinemanager.domain.model.SupplierDebt
import com.restaurant.offlinemanager.domain.repository.RestaurantRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SupplierDebtUseCase(private val repository: RestaurantRepository) {
    fun observeSupplierDebts(): Flow<List<SupplierDebt>> =
        repository.observeSnapshot().map(::calculateSupplierDebts)

    fun calculateSupplierDebts(snapshot: RestaurantSnapshot): List<SupplierDebt> =
        snapshot.suppliers.map { supplier ->
            val credit = snapshot.purchases
                .filter { it.supplierId == supplier.id && it.paymentType == PurchasePaymentType.CREDIT }
                .sumOf { it.totalAmount }
            val paid = snapshot.supplierPayments.filter { it.supplierId == supplier.id }.sumOf { it.amount }
            SupplierDebt(supplier, credit, paid, credit - paid)
        }.sortedByDescending { it.remaining }

    fun debtTotal(snapshot: RestaurantSnapshot): Long =
        calculateSupplierDebts(snapshot).sumOf { it.remaining.coerceAtLeast(0) }
}
