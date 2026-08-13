package com.restaurant.offlinemanager

import android.app.Application
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.restaurant.offlinemanager.data.local.AppDatabase
import com.restaurant.offlinemanager.data.repository.AppSettingsRepository
import com.restaurant.offlinemanager.data.repository.RoomRestaurantRepository
import com.restaurant.offlinemanager.domain.usecase.AppUseCases
import com.restaurant.offlinemanager.domain.usecase.BankCardBalanceUseCase
import com.restaurant.offlinemanager.domain.usecase.DashboardUseCase
import com.restaurant.offlinemanager.domain.usecase.InventoryUseCase
import com.restaurant.offlinemanager.domain.usecase.ProjectFinanceUseCase
import com.restaurant.offlinemanager.domain.usecase.ReportsUseCase
import com.restaurant.offlinemanager.domain.usecase.SupplierDebtUseCase

class RestaurantOfflineApp : Application() {
    val container: AppContainer by lazy { AppContainer(this) }

    override fun onCreate() {
        super.onCreate()
    }
}

class AppContainer(private val app: Application) {
    private val migration1To2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """CREATE TABLE IF NOT EXISTS materials_new (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    name TEXT NOT NULL,
                    mainUnit TEXT NOT NULL,
                    minimumStock REAL NOT NULL,
                    imageEmoji TEXT,
                    notes TEXT,
                    isActive INTEGER NOT NULL,
                    createdAt INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL
                )""".trimIndent()
            )
            db.execSQL(
                """INSERT INTO materials_new
                    (id, name, mainUnit, minimumStock, imageEmoji, notes, isActive, createdAt, updatedAt)
                    SELECT id, name, mainUnit, minimumStock, imageEmoji, notes, isActive, createdAt, updatedAt
                    FROM materials""".trimIndent()
            )
            db.execSQL("DROP TABLE materials")
            db.execSQL("ALTER TABLE materials_new RENAME TO materials")
            db.execSQL("DROP TABLE IF EXISTS material_categories")
        }
    }

    val database: AppDatabase by lazy {
        Room.databaseBuilder(app, AppDatabase::class.java, "restaurant_offline_manager.db")
            .addMigrations(migration1To2)
            .build()
    }

    val repository: RoomRestaurantRepository by lazy {
        RoomRestaurantRepository(database)
    }

    val settingsRepository: AppSettingsRepository by lazy {
        AppSettingsRepository(app)
    }

    val useCases: AppUseCases by lazy {
        val projectFinance = ProjectFinanceUseCase(repository)
        val supplierDebt = SupplierDebtUseCase(repository)
        val inventory = InventoryUseCase(repository)
        val bankCards = BankCardBalanceUseCase(repository)
        val dashboard = DashboardUseCase(repository, projectFinance, supplierDebt, inventory, bankCards)
        val reports = ReportsUseCase(repository, projectFinance, supplierDebt, inventory)
        AppUseCases(
            inventory = inventory,
            projectFinance = projectFinance,
            supplierDebt = supplierDebt,
            bankCards = bankCards,
            dashboard = dashboard,
            reports = reports
        )
    }
}
