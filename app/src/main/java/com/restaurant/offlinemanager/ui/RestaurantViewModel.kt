package com.restaurant.offlinemanager.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.restaurant.offlinemanager.data.local.entity.BankCardEntity
import com.restaurant.offlinemanager.data.local.entity.ExpenseEntity
import com.restaurant.offlinemanager.data.local.entity.MaterialEntity
import com.restaurant.offlinemanager.data.local.entity.SupplierEntity
import com.restaurant.offlinemanager.data.local.entity.WarehouseEntity
import com.restaurant.offlinemanager.data.repository.AppSettings
import com.restaurant.offlinemanager.data.repository.AppSettingsRepository
import com.restaurant.offlinemanager.domain.model.BankCardBalance
import com.restaurant.offlinemanager.domain.model.DashboardStats
import com.restaurant.offlinemanager.domain.model.InventoryItem
import com.restaurant.offlinemanager.domain.model.MealDeliveryInput
import com.restaurant.offlinemanager.domain.model.MonthlyPoint
import com.restaurant.offlinemanager.domain.model.ProjectFinance
import com.restaurant.offlinemanager.domain.model.ProjectInput
import com.restaurant.offlinemanager.domain.model.ProjectPaymentInput
import com.restaurant.offlinemanager.domain.model.PurchaseInput
import com.restaurant.offlinemanager.domain.model.RestaurantSnapshot
import com.restaurant.offlinemanager.domain.model.StockTransactionInput
import com.restaurant.offlinemanager.domain.model.SupplierDebt
import com.restaurant.offlinemanager.domain.model.SupplierPaymentInput
import com.restaurant.offlinemanager.domain.repository.RestaurantRepository
import com.restaurant.offlinemanager.domain.usecase.AppUseCases
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AppUiState(
    val isLoading: Boolean = true,
    val snapshot: RestaurantSnapshot = RestaurantSnapshot(),
    val settings: AppSettings = AppSettings(),
    val dashboard: DashboardStats = DashboardStats(),
    val inventory: List<InventoryItem> = emptyList(),
    val projectFinances: List<ProjectFinance> = emptyList(),
    val supplierDebts: List<SupplierDebt> = emptyList(),
    val bankBalances: List<BankCardBalance> = emptyList(),
    val monthlyPoints: List<MonthlyPoint> = emptyList()
)

enum class CsvReportType(val filePrefix: String, val successLabel: String) {
    PURCHASES("purchases", "گزارش خریدها"),
    INVENTORY("inventory", "گزارش موجودی"),
    RECEIVABLES("project-receivables", "گزارش مطالبات"),
    SUPPLIER_DEBTS("supplier-debts", "گزارش بدهی تامین‌کنندگان"),
    PAYMENTS("payments", "گزارش پرداخت‌ها")
}

class RestaurantViewModel(
    private val repository: RestaurantRepository,
    private val settingsRepository: AppSettingsRepository,
    private val useCases: AppUseCases
) : ViewModel() {
    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val messages: SharedFlow<String> = _messages

    val uiState = combine(repository.observeSnapshot(), settingsRepository.settings) { snapshot, settings ->
        AppUiState(
            isLoading = false,
            snapshot = snapshot,
            settings = settings,
            dashboard = useCases.dashboard.calculate(snapshot),
            inventory = useCases.inventory.calculateInventory(snapshot),
            projectFinances = useCases.projectFinance.calculateProjectFinances(snapshot),
            supplierDebts = useCases.supplierDebt.calculateSupplierDebts(snapshot),
            bankBalances = useCases.bankCards.calculateBalances(snapshot),
            monthlyPoints = useCases.reports.monthlySummary(snapshot)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppUiState()
    )

    fun saveProject(input: ProjectInput) = ioAction("پروژه ذخیره شد") {
        repository.saveProject(input)
    }

    fun archiveProject(projectId: Long) = ioAction("پروژه آرشیو شد") {
        repository.archiveProject(projectId).getOrThrow()
    }

    fun saveMealDelivery(input: MealDeliveryInput) = ioAction("وعده ثبت شد") {
        repository.saveMealDelivery(input).getOrThrow()
    }

    fun saveWarehouse(entity: WarehouseEntity) = ioAction("انبار ذخیره شد") {
        repository.saveWarehouse(entity)
    }

    fun saveMaterial(entity: MaterialEntity) = ioAction("متریال ذخیره شد") {
        repository.saveMaterial(entity)
    }

    fun saveSupplier(entity: SupplierEntity) = ioAction("تامین‌کننده ذخیره شد") {
        repository.saveSupplier(entity)
    }

    fun saveStockTransaction(input: StockTransactionInput) = ioAction("تراکنش انبار ثبت شد") {
        repository.saveStockTransaction(input).getOrThrow()
    }

    fun saveStockTransactions(inputs: List<StockTransactionInput>) = ioAction("تراکنش‌های انبار ثبت شد") {
        inputs.forEach { repository.saveStockTransaction(it).getOrThrow() }
    }

    fun savePurchase(input: PurchaseInput) = ioAction("فاکتور خرید ثبت شد") {
        repository.savePurchase(input).getOrThrow()
    }

    fun saveBankCard(entity: BankCardEntity) = ioAction("کارت بانکی ذخیره شد") {
        repository.saveBankCard(entity)
    }

    fun saveProjectPayment(input: ProjectPaymentInput) = ioAction("دریافت پروژه ثبت شد") {
        repository.saveProjectPayment(input).getOrThrow()
    }

    fun saveSupplierPayment(input: SupplierPaymentInput) = ioAction("پرداخت تامین‌کننده ثبت شد") {
        repository.saveSupplierPayment(input).getOrThrow()
    }

    fun saveExpense(entity: ExpenseEntity) = ioAction("هزینه ثبت شد") {
        repository.saveExpense(entity)
    }

    fun setDarkMode(enabled: Boolean) = ioAction("تنظیمات ذخیره شد") {
        settingsRepository.setDarkMode(enabled)
    }

    fun setAppLock(enabled: Boolean) = ioAction("تنظیمات امنیت ذخیره شد") {
        settingsRepository.setAppLock(enabled)
    }

    fun setLowStockNotifications(enabled: Boolean) = ioAction("تنظیمات هشدار ذخیره شد") {
        settingsRepository.setLowStockNotifications(enabled)
    }

    fun exportBackup(context: Context) = ioAction("پشتیبان JSON ذخیره شد") {
        val file = repository.exportBackup(context)
        _messages.tryEmit("فایل پشتیبان: ${file.absolutePath}")
    }

    fun restoreBackup(context: Context, uri: Uri) = ioAction("بازیابی انجام شد") {
        repository.restoreBackup(context, uri).getOrThrow()
    }

    fun exportCsv(context: Context, type: CsvReportType) = ioAction("${type.successLabel} ذخیره شد") {
        val snapshot = repository.currentSnapshot()
        val csv = when (type) {
            CsvReportType.PURCHASES -> useCases.reports.purchasesCsv(snapshot)
            CsvReportType.INVENTORY -> useCases.reports.inventoryCsv(snapshot)
            CsvReportType.RECEIVABLES -> useCases.reports.receivablesCsv(snapshot)
            CsvReportType.SUPPLIER_DEBTS -> useCases.reports.supplierDebtsCsv(snapshot)
            CsvReportType.PAYMENTS -> useCases.reports.paymentsCsv(snapshot)
        }
        val file = repository.exportCsv(context, "${type.filePrefix}-${System.currentTimeMillis()}.csv", csv)
        _messages.tryEmit("فایل CSV: ${file.absolutePath}")
    }

    private fun ioAction(success: String, block: suspend () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                block()
                _messages.emit(success)
            } catch (error: Throwable) {
                _messages.emit(error.message ?: "خطای ناشناخته رخ داد")
            }
        }
    }
}

class RestaurantViewModelFactory(
    private val repository: RestaurantRepository,
    private val settingsRepository: AppSettingsRepository,
    private val useCases: AppUseCases
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RestaurantViewModel(repository, settingsRepository, useCases) as T
    }
}
