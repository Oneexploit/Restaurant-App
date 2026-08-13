package com.restaurant.offlinemanager

import android.app.Application
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.restaurant.offlinemanager.data.local.AppDatabase
import com.restaurant.offlinemanager.data.repository.AppSettingsRepository
import com.restaurant.offlinemanager.data.repository.RoomRestaurantRepository
import com.restaurant.offlinemanager.domain.usecase.AppUseCases
import com.restaurant.offlinemanager.domain.usecase.AccountingUseCase
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

    private val migration2To3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE supplier_payments ADD COLUMN purchaseId INTEGER")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_supplier_payments_purchaseId ON supplier_payments(purchaseId)")
        }
    }

    val database: AppDatabase by lazy {
        Room.databaseBuilder(app, AppDatabase::class.java, "restaurant_offline_manager.db")
            .addMigrations(migration1To2, migration2To3)
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
        val accounting = AccountingUseCase(inventory)
        val dashboard = DashboardUseCase(repository, projectFinance, supplierDebt, inventory, bankCards)
        val reports = ReportsUseCase(repository, projectFinance, supplierDebt, inventory, accounting)
        AppUseCases(
            inventory = inventory,
            projectFinance = projectFinance,
            supplierDebt = supplierDebt,
            bankCards = bankCards,
            accounting = accounting,
            dashboard = dashboard,
            reports = reports
        )
    }
}
