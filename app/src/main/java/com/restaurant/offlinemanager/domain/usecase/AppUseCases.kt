package com.restaurant.offlinemanager.domain.usecase

data class AppUseCases(
    val inventory: InventoryUseCase,
    val projectFinance: ProjectFinanceUseCase,
    val supplierDebt: SupplierDebtUseCase,
    val bankCards: BankCardBalanceUseCase,
    val dashboard: DashboardUseCase,
    val reports: ReportsUseCase
)
