package com.restaurant.offlinemanager.domain.usecase

import com.restaurant.offlinemanager.data.local.entity.StockTransactionType
import com.restaurant.offlinemanager.domain.model.InventoryItem
import com.restaurant.offlinemanager.domain.model.RestaurantSnapshot
import com.restaurant.offlinemanager.domain.repository.RestaurantRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class InventoryUseCase(private val repository: RestaurantRepository) {
    fun observeInventory(): Flow<List<InventoryItem>> =
        repository.observeSnapshot().map(::calculateInventory)

    fun calculateInventory(snapshot: RestaurantSnapshot): List<InventoryItem> {
        val latestPrices = snapshot.purchaseItems
            .groupBy { it.materialId }
            .mapValues { (_, items) -> items.maxByOrNull { it.createdAt }?.unitPrice ?: 0L }

        val grouped = snapshot.stockTransactions.groupBy { it.warehouseId to it.materialId }
        val inventory = mutableListOf<InventoryItem>()
        snapshot.warehouses.filter { it.isActive }.forEach { warehouse ->
            snapshot.materials.filter { it.isActive }.forEach { material ->
                val transactions = grouped[warehouse.id to material.id].orEmpty()
                val quantity = transactions.sumOf { tx ->
                    when (tx.type) {
                        StockTransactionType.IN, StockTransactionType.TRANSFER_IN -> tx.quantity
                        StockTransactionType.OUT, StockTransactionType.TRANSFER_OUT, StockTransactionType.WASTE -> -tx.quantity
                        StockTransactionType.ADJUSTMENT -> tx.quantity
                    }
                }
                if (transactions.isNotEmpty()) {
                    val unitPrice = latestPrices[material.id] ?: 0L
                    inventory += InventoryItem(
                        materialId = material.id,
                        materialName = material.name,
                        warehouseId = warehouse.id,
                        warehouseName = warehouse.name,
                        unit = material.mainUnit,
                        quantity = quantity,
                        approximateValue = (quantity.coerceAtLeast(0.0) * unitPrice).toLong(),
                        minimumStock = material.minimumStock,
                        isLowStock = quantity < material.minimumStock,
                        emoji = material.imageEmoji
                    )
                }
            }
        }
        return inventory.sortedWith(compareByDescending<InventoryItem> { it.isLowStock }.thenBy { it.materialName })
    }

    fun availableStock(snapshot: RestaurantSnapshot, warehouseId: Long, materialId: Long): Double =
        calculateInventory(snapshot)
            .firstOrNull { it.warehouseId == warehouseId && it.materialId == materialId }
            ?.quantity ?: 0.0
}
