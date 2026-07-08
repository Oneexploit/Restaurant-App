package com.restaurant.offlinemanager.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.restaurant.offlinemanager.data.local.entity.BankCardEntity
import com.restaurant.offlinemanager.data.local.entity.ExpenseEntity
import com.restaurant.offlinemanager.data.local.entity.MaterialCategoryEntity
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
import kotlinx.coroutines.withContext
import java.io.File

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

    fun saveProject(input: ProjectInput, onSuccess: () -> Unit = {}) = ioAction("پروژه ذخیره شد", onSuccess) {
        repository.saveProject(input)
    }

    fun archiveProject(projectId: Long, onSuccess: () -> Unit = {}) = ioAction("پروژه آرشیو شد", onSuccess) {
        repository.archiveProject(projectId).getOrThrow()
    }

    fun saveMealDelivery(input: MealDeliveryInput, onSuccess: () -> Unit = {}) = ioAction("وعده ثبت شد", onSuccess) {
        repository.saveMealDelivery(input).getOrThrow()
    }

    fun saveWarehouse(entity: WarehouseEntity, onSuccess: () -> Unit = {}) = ioAction("انبار ذخیره شد", onSuccess) {
        repository.saveWarehouse(entity)
    }

    fun saveMaterialCategory(entity: MaterialCategoryEntity, onSuccess: () -> Unit = {}) = ioAction("دسته‌بندی ذخیره شد", onSuccess) {
        repository.saveMaterialCategory(entity)
    }

    fun saveMaterial(entity: MaterialEntity, onSuccess: () -> Unit = {}) = ioAction("متریال ذخیره شد", onSuccess) {
        repository.saveMaterial(entity)
    }

    fun saveSupplier(entity: SupplierEntity, onSuccess: () -> Unit = {}) = ioAction("تامین‌کننده ذخیره شد", onSuccess) {
        repository.saveSupplier(entity)
    }

    fun saveStockTransaction(input: StockTransactionInput, onSuccess: () -> Unit = {}) = ioAction("تراکنش انبار ثبت شد", onSuccess) {
        repository.saveStockTransaction(input).getOrThrow()
    }

    fun saveStockTransactions(inputs: List<StockTransactionInput>, onSuccess: () -> Unit = {}) = ioAction("تراکنش‌های انبار ثبت شد", onSuccess) {
        repository.saveStockTransactions(inputs).getOrThrow()
    }

    fun savePurchase(input: PurchaseInput, onSuccess: () -> Unit = {}) = ioAction("فاکتور خرید ثبت شد", onSuccess) {
        repository.savePurchase(input).getOrThrow()
    }

    fun saveBankCard(entity: BankCardEntity, onSuccess: () -> Unit = {}) = ioAction("کارت بانکی ذخیره شد", onSuccess) {
        repository.saveBankCard(entity)
    }

    fun saveProjectPayment(input: ProjectPaymentInput, onSuccess: () -> Unit = {}) = ioAction("دریافت پروژه ثبت شد", onSuccess) {
        repository.saveProjectPayment(input).getOrThrow()
    }

    fun saveSupplierPayment(input: SupplierPaymentInput, onSuccess: () -> Unit = {}) = ioAction("پرداخت تامین‌کننده ثبت شد", onSuccess) {
        repository.saveSupplierPayment(input).getOrThrow()
    }

    fun saveExpense(entity: ExpenseEntity, onSuccess: () -> Unit = {}) = ioAction("هزینه ثبت شد", onSuccess) {
        repository.saveExpense(entity)
    }

    fun setLowStockNotifications(enabled: Boolean) = ioAction("تنظیمات هشدار ذخیره شد") {
        settingsRepository.setLowStockNotifications(enabled)
    }

    fun exportBackup(context: Context) = ioAction("پشتیبان JSON ذخیره شد") {
        val file = repository.exportBackup(context)
        _messages.tryEmit("فایل پشتیبان: ${file.absolutePath}")
        shareFile(context, file, "application/json")
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
        shareFile(context, file, "text/csv")
    }

    fun deleteMealDelivery(id: Long) = ioAction("وعده حذف شد") {
        repository.deleteMealDelivery(id).getOrThrow()
    }

    fun deleteStockTransaction(id: Long) = ioAction("تراکنش انبار حذف شد") {
        repository.deleteStockTransaction(id).getOrThrow()
    }

    fun deletePurchase(id: Long) = ioAction("فاکتور خرید حذف شد") {
        repository.deletePurchase(id).getOrThrow()
    }

    fun deleteProjectPayment(id: Long) = ioAction("دریافت پروژه حذف شد") {
        repository.deleteProjectPayment(id).getOrThrow()
    }

    fun deleteSupplierPayment(id: Long) = ioAction("پرداخت تامین‌کننده حذف شد") {
        repository.deleteSupplierPayment(id).getOrThrow()
    }

    fun deleteExpense(id: Long) = ioAction("هزینه حذف شد") {
        repository.deleteExpense(id).getOrThrow()
    }

    private fun ioAction(success: String, onSuccess: () -> Unit = {}, block: suspend () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                block()
                _messages.emit(success)
                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (error: Throwable) {
                _messages.emit(error.message ?: "خطای ناشناخته رخ داد")
            }
        }
    }

    private suspend fun shareFile(context: Context, file: File, mimeType: String) {
        withContext(Dispatchers.Main) {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "اشتراک‌گذاری فایل"))
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
