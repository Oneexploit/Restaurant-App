package com.restaurant.offlinemanager.core.notifications

import android.content.Context
import androidx.room.Room
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.restaurant.offlinemanager.core.utils.MoneyFormatter
import com.restaurant.offlinemanager.core.utils.NumberFormatter
import com.restaurant.offlinemanager.data.local.AppDatabase
import com.restaurant.offlinemanager.data.local.DatabaseMigrations
import com.restaurant.offlinemanager.data.repository.AppSettingsRepository
import com.restaurant.offlinemanager.data.repository.RoomRestaurantRepository
import com.restaurant.offlinemanager.domain.usecase.InventoryUseCase
import com.restaurant.offlinemanager.domain.usecase.ProjectFinanceUseCase
import com.restaurant.offlinemanager.domain.usecase.SupplierDebtUseCase
import kotlinx.coroutines.flow.first

class ImportantAlertsWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val settings = AppSettingsRepository(applicationContext).settings.first()
        if (!settings.importantNotificationsEnabled) return Result.success()
        if (!AppNotificationManager.canPostNotifications(applicationContext)) return Result.success()

        val database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "restaurant_offline_manager.db"
        ).addMigrations(*DatabaseMigrations.all).build()

        return try {
            val repository = RoomRestaurantRepository(database)
            val snapshot = repository.currentSnapshot()
            val inventory = InventoryUseCase(repository).calculateInventory(snapshot)
            val projectFinances = ProjectFinanceUseCase(repository).calculateProjectFinances(snapshot)
            val supplierDebts = SupplierDebtUseCase(repository).calculateSupplierDebts(snapshot)

            val lowStockCount = if (settings.lowStockNotificationsEnabled) {
                inventory.count { it.isLowStock }
            } else {
                0
            }
            val receivableTotal = projectFinances.sumOf { it.receivable.coerceAtLeast(0) }
            val receivableCount = projectFinances.count { it.receivable > 0 }
            val supplierDebtTotal = supplierDebts.sumOf { it.remaining.coerceAtLeast(0) }
            val supplierDebtCount = supplierDebts.count { it.remaining > 0 }

            val lines = buildList {
                if (lowStockCount > 0) {
                    add("${NumberFormatter.format(lowStockCount)} قلم کالا زیر حداقل موجودی است.")
                }
                if (receivableTotal > 0) {
                    add("${NumberFormatter.format(receivableCount)} پروژه مطالبات باز دارد: ${MoneyFormatter.format(receivableTotal)}")
                }
                if (supplierDebtTotal > 0) {
                    add("${NumberFormatter.format(supplierDebtCount)} تامین‌کننده بدهی باز دارد: ${MoneyFormatter.format(supplierDebtTotal)}")
                }
            }

            if (lines.isEmpty()) {
                AppNotificationManager.clearImportantAlertSignature(applicationContext)
            } else {
                val signature = listOf(lowStockCount, receivableTotal, receivableCount, supplierDebtTotal, supplierDebtCount)
                    .joinToString(separator = ":")
                AppNotificationManager.showImportantAlert(
                    context = applicationContext,
                    title = "یادآوری‌های مهم رستوران",
                    message = lines.joinToString(separator = "\n"),
                    signature = signature
                )
            }
            Result.success()
        } catch (_: Throwable) {
            Result.retry()
        } finally {
            database.close()
        }
    }
}
