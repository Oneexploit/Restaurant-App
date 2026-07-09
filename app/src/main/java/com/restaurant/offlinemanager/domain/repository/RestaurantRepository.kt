package com.restaurant.offlinemanager.domain.repository

import android.content.Context
import android.net.Uri
import com.restaurant.offlinemanager.data.local.entity.BankCardEntity
import com.restaurant.offlinemanager.data.local.entity.ExpenseEntity
import com.restaurant.offlinemanager.data.local.entity.MaterialCategoryEntity
import com.restaurant.offlinemanager.data.local.entity.MaterialEntity
import com.restaurant.offlinemanager.data.local.entity.SupplierEntity
import com.restaurant.offlinemanager.data.local.entity.WarehouseEntity
import com.restaurant.offlinemanager.domain.model.MealDeliveryInput
import com.restaurant.offlinemanager.domain.model.ProjectInput
import com.restaurant.offlinemanager.domain.model.ProjectPaymentInput
import com.restaurant.offlinemanager.domain.model.PurchaseInput
import com.restaurant.offlinemanager.domain.model.RestaurantSnapshot
import com.restaurant.offlinemanager.domain.model.StockTransactionInput
import com.restaurant.offlinemanager.domain.model.SupplierPaymentInput
import kotlinx.coroutines.flow.Flow
import java.io.File

interface RestaurantRepository {
    fun observeSnapshot(): Flow<RestaurantSnapshot>
    suspend fun currentSnapshot(): RestaurantSnapshot
    suspend fun saveProject(input: ProjectInput): Long
    suspend fun archiveProject(projectId: Long): Result<Unit>
    suspend fun saveMealDelivery(input: MealDeliveryInput): Result<Long>
    suspend fun saveWarehouse(entity: WarehouseEntity): Long
    suspend fun saveMaterialCategory(entity: MaterialCategoryEntity): Long
    suspend fun saveMaterial(entity: MaterialEntity): Long
    suspend fun saveSupplier(entity: SupplierEntity): Long
    suspend fun saveStockTransaction(input: StockTransactionInput): Result<Long>
    suspend fun saveStockTransactions(inputs: List<StockTransactionInput>): Result<List<Long>>
    suspend fun savePurchase(input: PurchaseInput): Result<Long>
    suspend fun saveBankCard(entity: BankCardEntity): Long
    suspend fun saveProjectPayment(input: ProjectPaymentInput): Result<Long>
    suspend fun saveSupplierPayment(input: SupplierPaymentInput): Result<Long>
    suspend fun saveExpense(entity: ExpenseEntity): Long
    suspend fun deleteMealDelivery(id: Long): Result<Unit>
    suspend fun deleteWarehouse(id: Long): Result<Unit>
    suspend fun deleteMaterialCategory(id: Long): Result<Unit>
    suspend fun deleteMaterial(id: Long): Result<Unit>
    suspend fun deleteSupplier(id: Long): Result<Unit>
    suspend fun deleteBankCard(id: Long): Result<Unit>
    suspend fun deleteStockTransaction(id: Long): Result<Unit>
    suspend fun deletePurchase(id: Long): Result<Unit>
    suspend fun deleteProjectPayment(id: Long): Result<Unit>
    suspend fun deleteSupplierPayment(id: Long): Result<Unit>
    suspend fun deleteExpense(id: Long): Result<Unit>
    suspend fun exportBackup(context: Context): File
    suspend fun restoreBackup(context: Context, uri: Uri): Result<Unit>
    suspend fun exportCsv(context: Context, fileName: String, csv: String): File
}
