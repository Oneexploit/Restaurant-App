package com.restaurant.offlinemanager.domain.usecase

import com.restaurant.offlinemanager.data.local.entity.StockTransactionEntity
import com.restaurant.offlinemanager.data.local.entity.StockTransactionType
import com.restaurant.offlinemanager.core.utils.PersianDateFormatter
import com.restaurant.offlinemanager.domain.model.InventoryItem
import com.restaurant.offlinemanager.domain.model.CookingBatchCost
import com.restaurant.offlinemanager.domain.model.RestaurantSnapshot
import com.restaurant.offlinemanager.domain.repository.RestaurantRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.math.abs
import kotlin.math.roundToLong
import java.time.Instant
import java.time.ZoneId

class InventoryUseCase(private val repository: RestaurantRepository) {
    fun observeInventory(): Flow<List<InventoryItem>> =
        repository.observeSnapshot().map(::calculateInventory)

    fun calculateInventory(snapshot: RestaurantSnapshot): List<InventoryItem> {
        val valuation = calculateValuation(snapshot)
        return snapshot.warehouses.filter { it.isActive }.flatMap { warehouse ->
            snapshot.materials.filter { it.isActive }.map { material ->
                val balance = valuation.balances[warehouse.id to material.id] ?: CostBalance()
                val quantity = balance.quantity.cleanZero()
                val value = balance.value.coerceAtLeast(0.0)
                InventoryItem(
                    materialId = material.id,
                    materialName = material.name,
                    warehouseId = warehouse.id,
                    warehouseName = warehouse.name,
                    unit = material.mainUnit,
                    quantity = quantity,
                    approximateValue = value.roundToLong(),
                    averageUnitCost = balance.averageCost.roundToLong(),
                    minimumStock = material.minimumStock,
                    isLowStock = quantity < material.minimumStock,
                    emoji = material.imageEmoji
                )
            }
        }.sortedWith(compareByDescending<InventoryItem> { it.isLowStock }.thenBy { it.materialName })
    }

    fun costOfGoodsConsumed(snapshot: RestaurantSnapshot, monthKey: String? = null): Long {
        val valuation = calculateValuation(snapshot)
        return (if (monthKey == null) valuation.costOfGoodsConsumed else valuation.monthlyConsumed[monthKey] ?: 0.0).roundToLong()
    }

    fun wasteLoss(snapshot: RestaurantSnapshot, monthKey: String? = null): Long {
        val valuation = calculateValuation(snapshot)
        return (if (monthKey == null) valuation.wasteLoss else valuation.monthlyWaste[monthKey] ?: 0.0).roundToLong()
    }

    fun costOfGoodsConsumedOnDate(snapshot: RestaurantSnapshot, timestamp: Long): Long =
        (calculateValuation(snapshot).dailyConsumed[localDateKey(timestamp)] ?: 0.0).roundToLong()

    fun wasteLossOnDate(snapshot: RestaurantSnapshot, timestamp: Long): Long =
        (calculateValuation(snapshot).dailyWaste[localDateKey(timestamp)] ?: 0.0).roundToLong()

    fun projectMaterialCosts(snapshot: RestaurantSnapshot): Map<Long, Long> =
        calculateValuation(snapshot).projectCosts.mapValues { it.value.roundToLong() }

    fun cookingBatchCosts(snapshot: RestaurantSnapshot): List<CookingBatchCost> {
        val valuation = calculateValuation(snapshot)
        return snapshot.cookingBatches.map { batch ->
            val total = (valuation.batchCosts[batch.id] ?: 0.0).roundToLong()
            CookingBatchCost(batch.id, total, if (batch.producedQuantity > 0) total / batch.producedQuantity else 0)
        }
    }

    fun availableStock(snapshot: RestaurantSnapshot, warehouseId: Long, materialId: Long): Double =
        calculateInventory(snapshot)
            .firstOrNull { it.warehouseId == warehouseId && it.materialId == materialId }
            ?.quantity ?: 0.0

    private fun calculateValuation(snapshot: RestaurantSnapshot): ValuationResult {
        val balances = mutableMapOf<Pair<Long, Long>, CostBalance>()
        val projectCosts = mutableMapOf<Long, Double>()
        val batchCosts = mutableMapOf<Long, Double>()
        val pendingTransfers = mutableMapOf<Long, ArrayDeque<Double>>()
        var consumed = 0.0
        var waste = 0.0
        val monthlyConsumed = mutableMapOf<String, Double>()
        val monthlyWaste = mutableMapOf<String, Double>()
        val dailyConsumed = mutableMapOf<String, Double>()
        val dailyWaste = mutableMapOf<String, Double>()
        val purchaseCosts = effectivePurchaseUnitCosts(snapshot)
        val fallbackPurchaseCosts = snapshot.purchaseItems
            .groupBy { it.materialId }
            .mapValues { (_, items) -> items.maxByOrNull { it.createdAt }?.unitPrice?.toDouble() ?: 0.0 }

        snapshot.stockTransactions
            .sortedWith(compareBy<StockTransactionEntity>({ it.date }, { it.createdAt }, { it.id }))
            .forEach { transaction ->
                val key = transaction.warehouseId to transaction.materialId
                val balance = balances.getOrPut(key) { CostBalance() }
                when (transaction.type) {
                    StockTransactionType.IN -> {
                        val unitCost = purchaseCosts[transaction.purchaseId to transaction.materialId]
                            ?: transaction.unitPrice?.toDouble()
                            ?: fallbackPurchaseCosts[transaction.materialId]
                            ?: balance.averageCost
                        balance.add(transaction.quantity, unitCost)
                    }
                    StockTransactionType.TRANSFER_IN -> {
                        val transferredCost = pendingTransfers[transaction.materialId]?.removeFirstOrNull()
                        balance.add(transaction.quantity, transferredCost ?: transaction.unitPrice?.toDouble() ?: balance.averageCost)
                    }
                    StockTransactionType.OUT -> {
                        val cost = balance.remove(transaction.quantity)
                        consumed += cost
                        val month = PersianDateFormatter.monthKey(transaction.date)
                        monthlyConsumed[month] = (monthlyConsumed[month] ?: 0.0) + cost
                        val day = localDateKey(transaction.date)
                        dailyConsumed[day] = (dailyConsumed[day] ?: 0.0) + cost
                        transaction.projectId?.let { projectId ->
                            projectCosts[projectId] = (projectCosts[projectId] ?: 0.0) + cost
                        }
                        transaction.cookingBatchId?.let { batchId ->
                            batchCosts[batchId] = (batchCosts[batchId] ?: 0.0) + cost
                        }
                    }
                    StockTransactionType.TRANSFER_OUT -> {
                        val averageCost = balance.averageCost
                        balance.remove(transaction.quantity)
                        pendingTransfers.getOrPut(transaction.materialId) { ArrayDeque() }.addLast(averageCost)
                    }
                    StockTransactionType.WASTE -> {
                        val cost = balance.remove(transaction.quantity)
                        waste += cost
                        val month = PersianDateFormatter.monthKey(transaction.date)
                        monthlyWaste[month] = (monthlyWaste[month] ?: 0.0) + cost
                        val day = localDateKey(transaction.date)
                        dailyWaste[day] = (dailyWaste[day] ?: 0.0) + cost
                    }
                    StockTransactionType.ADJUSTMENT -> {
                        if (transaction.quantity > 0) {
                            balance.add(transaction.quantity, transaction.unitPrice?.toDouble() ?: balance.averageCost)
                        } else {
                            balance.remove(abs(transaction.quantity))
                        }
                    }
                }
            }
        snapshot.cookingAllocations.groupBy { it.batchId }.forEach { (batchId, allocations) ->
            val batchCost = batchCosts[batchId] ?: 0.0
            val totalQuantity = allocations.sumOf { it.quantity }
            if (batchCost > 0.0 && totalQuantity > 0) allocations.forEach { allocation ->
                val allocatedCost = batchCost * allocation.quantity / totalQuantity
                projectCosts[allocation.projectId] = (projectCosts[allocation.projectId] ?: 0.0) + allocatedCost
            }
        }
        return ValuationResult(balances, projectCosts, batchCosts, consumed, waste, monthlyConsumed, monthlyWaste, dailyConsumed, dailyWaste)
    }

    private fun effectivePurchaseUnitCosts(snapshot: RestaurantSnapshot): Map<Pair<Long?, Long>, Double> {
        val purchaseById = snapshot.purchases.associateBy { it.id }
        return snapshot.purchaseItems.groupBy { it.purchaseId }.flatMap { (purchaseId, items) ->
            val purchase = purchaseById[purchaseId]
            val subtotal = items.sumOf { it.totalAmount }.toDouble()
            val netRatio = if (purchase != null && subtotal > 0) purchase.totalAmount / subtotal else 1.0
            items.groupBy { it.materialId }.map { (materialId, materialItems) ->
                val quantity = materialItems.sumOf { it.quantity }
                val gross = materialItems.sumOf { it.totalAmount }.toDouble()
                Pair(purchaseId as Long?, materialId) to if (quantity > 0) gross * netRatio / quantity else 0.0
            }
        }.toMap()
    }

    private data class CostBalance(var quantity: Double = 0.0, var value: Double = 0.0) {
        val averageCost: Double get() = if (quantity > EPSILON) value / quantity else 0.0

        fun add(amount: Double, unitCost: Double) {
            if (amount <= 0) return
            quantity += amount
            value += amount * unitCost.coerceAtLeast(0.0)
        }

        fun remove(amount: Double): Double {
            if (amount <= 0 || quantity <= EPSILON) return 0.0
            val removed = amount.coerceAtMost(quantity)
            val cost = removed * averageCost
            quantity -= removed
            value = (value - cost).coerceAtLeast(0.0)
            if (quantity <= EPSILON) {
                quantity = 0.0
                value = 0.0
            }
            return cost
        }
    }

    private data class ValuationResult(
        val balances: Map<Pair<Long, Long>, CostBalance>,
        val projectCosts: Map<Long, Double>,
        val batchCosts: Map<Long, Double>,
        val costOfGoodsConsumed: Double,
        val wasteLoss: Double,
        val monthlyConsumed: Map<String, Double>,
        val monthlyWaste: Map<String, Double>,
        val dailyConsumed: Map<String, Double>,
        val dailyWaste: Map<String, Double>
    )

    private fun Double.cleanZero(): Double = if (abs(this) < EPSILON) 0.0 else this

    private fun localDateKey(timestamp: Long): String =
        Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate().toString()

    private companion object { const val EPSILON = 0.000001 }
}
