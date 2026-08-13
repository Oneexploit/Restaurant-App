package com.restaurant.offlinemanager.domain.usecase

import com.restaurant.offlinemanager.data.local.entity.StockTransactionEntity
import com.restaurant.offlinemanager.data.local.entity.StockTransactionType
import com.restaurant.offlinemanager.domain.model.RestaurantSnapshot

object InventoryIntegrityValidator {
    private const val EPSILON = 0.000001

    fun validateNonNegative(
        snapshot: RestaurantSnapshot,
        transactions: List<StockTransactionEntity>
    ) {
        val balances = mutableMapOf<Pair<Long, Long>, Double>()
        val offender = transactions
            .sortedWith(compareBy<StockTransactionEntity>({ it.date }, { it.createdAt }, { it.id }))
            .firstOrNull { transaction ->
            val key = transaction.warehouseId to transaction.materialId
            balances[key] = (balances[key] ?: 0.0) + signedQuantity(transaction)
            balances.getValue(key) < -EPSILON
        } ?: return
        val warehouse = snapshot.warehouses.firstOrNull { it.id == offender.warehouseId }?.name ?: "انبار"
        val material = snapshot.materials.firstOrNull { it.id == offender.materialId }?.name ?: "کالا"
        require(false) {
            "این تغییر موجودی $material در $warehouse را منفی می‌کند"
        }
    }

    fun signedQuantity(transaction: StockTransactionEntity): Double =
        when (transaction.type) {
            StockTransactionType.IN, StockTransactionType.TRANSFER_IN -> transaction.quantity
            StockTransactionType.OUT, StockTransactionType.TRANSFER_OUT, StockTransactionType.WASTE -> -transaction.quantity
            StockTransactionType.ADJUSTMENT -> transaction.quantity
        }
}
