package com.restaurant.offlinemanager

import android.app.Application
import androidx.room.Room
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
    val database: AppDatabase by lazy {
        Room.databaseBuilder(app, AppDatabase::class.java, "restaurant_offline_manager.db")
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
