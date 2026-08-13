package com.restaurant.offlinemanager.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.restaurant.offlinemanager.core.notifications.AppNotificationScheduler
import com.restaurant.offlinemanager.data.local.entity.BankCardEntity
import com.restaurant.offlinemanager.data.local.entity.ExpenseEntity
import com.restaurant.offlinemanager.data.local.entity.MaterialEntity
import com.restaurant.offlinemanager.data.local.entity.SupplierEntity
import com.restaurant.offlinemanager.data.local.entity.WarehouseEntity
import com.restaurant.offlinemanager.data.repository.AppSettings
import com.restaurant.offlinemanager.data.repository.AppSettingsRepository
import com.restaurant.offlinemanager.domain.model.BankCardBalance
import com.restaurant.offlinemanager.domain.model.AccountingSummary
import com.restaurant.offlinemanager.domain.model.DashboardStats
import com.restaurant.offlinemanager.domain.model.InventoryItem
import com.restaurant.offlinemanager.domain.model.MealDeliveryInput
import com.restaurant.offlinemanager.domain.model.MonthlyPoint
import com.restaurant.offlinemanager.domain.model.ProjectFinance
import com.restaurant.offlinemanager.domain.model.ProjectProfit
import com.restaurant.offlinemanager.domain.model.ProjectInput
import com.restaurant.offlinemanager.domain.model.ProjectPaymentInput
import com.restaurant.offlinemanager.domain.model.PurchaseInput
import com.restaurant.offlinemanager.domain.model.RestaurantSnapshot
import com.restaurant.offlinemanager.domain.model.StockTransactionInput
import com.restaurant.offlinemanager.domain.model.SupplierDebt
import com.restaurant.offlinemanager.domain.model.SupplierPaymentInput
import com.restaurant.offlinemanager.domain.repository.RestaurantRepository
import com.restaurant.offlinemanager.domain.usecase.AppUseCases
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

data class AppUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val snapshot: RestaurantSnapshot = RestaurantSnapshot(),
    val settings: AppSettings = AppSettings(),
    val dashboard: DashboardStats = DashboardStats(),
    val inventory: List<InventoryItem> = emptyList(),
    val projectFinances: List<ProjectFinance> = emptyList(),
    val supplierDebts: List<SupplierDebt> = emptyList(),
    val bankBalances: List<BankCardBalance> = emptyList(),
    val accounting: AccountingSummary = AccountingSummary(),
    val projectProfits: List<ProjectProfit> = emptyList(),
    val monthlyPoints: List<MonthlyPoint> = emptyList()
)

enum class CsvReportType(val filePrefix: String, val successLabel: String) {
    MEAL_DELIVERIES("meal-deliveries", "گزارش تحویل غذا"),
    PURCHASES("purchases", "گزارش خریدها"),
    INVENTORY("inventory", "گزارش موجودی"),
    RECEIVABLES("project-receivables", "گزارش مطالبات"),
    SUPPLIER_DEBTS("supplier-debts", "گزارش بدهی تامین‌کنندگان"),
    PAYMENTS("payments", "گزارش پرداخت‌ها"),
    EXPENSES("expenses", "گزارش هزینه‌ها"),
    PROFIT_LOSS("profit-loss", "گزارش سود و زیان"),
    PROJECT_PROFIT("project-profit", "گزارش سود پروژه‌ها"),
    CASH_FLOW("cash-flow", "گزارش جریان نقدی")
}

class RestaurantViewModel(
    private val repository: RestaurantRepository,
    private val settingsRepository: AppSettingsRepository,
    private val useCases: AppUseCases
) : ViewModel() {
    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val messages: SharedFlow<String> = _messages
    private val activeOperations = MutableStateFlow(0)

    val uiState = combine(repository.observeSnapshot(), settingsRepository.settings, activeOperations) { snapshot, settings, operations ->
        buildUiState(snapshot, settings).copy(isSaving = operations > 0)
    }.catch { error ->
        if (error is CancellationException) throw error
        _messages.tryEmit(error.userFacingMessage("خطا در بارگذاری داده‌ها رخ داد"))
        emit(AppUiState(isLoading = false))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = AppUiState()
    )

    private fun buildUiState(snapshot: RestaurantSnapshot, settings: AppSettings): AppUiState =
        try {
        AppUiState(
            isLoading = false,
            snapshot = snapshot,
            settings = settings,
            dashboard = useCases.dashboard.calculate(snapshot),
            inventory = useCases.inventory.calculateInventory(snapshot),
            projectFinances = useCases.projectFinance.calculateProjectFinances(snapshot),
            supplierDebts = useCases.supplierDebt.calculateSupplierDebts(snapshot),
            bankBalances = useCases.bankCards.calculateBalances(snapshot),
            accounting = useCases.accounting.summary(snapshot),
            projectProfits = useCases.accounting.projectProfits(snapshot),
            monthlyPoints = useCases.reports.monthlySummary(snapshot)
        )
    } catch (error: Throwable) {
        if (error is CancellationException) throw error
        _messages.tryEmit(error.userFacingMessage("خطا در آماده‌سازی اطلاعات رخ داد"))
        AppUiState(
            isLoading = false,
            snapshot = snapshot,
            settings = settings
        )
    }

    fun saveProject(input: ProjectInput, onSuccess: () -> Unit = {}) = ioAction("پروژه ذخیره شد", onSuccess) {
        repository.saveProject(input)
    }

    fun archiveProject(projectId: Long, onSuccess: () -> Unit = {}) = ioAction("پروژه آرشیو شد", onSuccess) {
        repository.archiveProject(projectId).getOrThrow()
    }

    fun saveMealDelivery(input: MealDeliveryInput, onSuccess: () -> Unit = {}) = ioAction("تحویل غذا ثبت شد", onSuccess) {
        repository.saveMealDelivery(input).getOrThrow()
    }

    fun saveWarehouse(entity: WarehouseEntity, onSuccess: () -> Unit = {}) = ioAction("انبار ذخیره شد", onSuccess) {
        repository.saveWarehouse(entity)
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

    fun setReducedMotion(enabled: Boolean) = ioAction("تنظیمات حرکت ذخیره شد") {
        settingsRepository.setReducedMotion(enabled)
    }

    fun setAppLock(enabled: Boolean) = ioAction(if (enabled) "قفل برنامه فعال شد" else "قفل برنامه غیرفعال شد") {
        settingsRepository.setAppLock(enabled)
    }

    fun setImportantNotifications(context: Context, enabled: Boolean) = ioAction(
        if (enabled) "نوتیفیکیشن‌های مهم فعال شد" else "نوتیفیکیشن‌های مهم غیرفعال شد"
    ) {
        settingsRepository.setImportantNotifications(enabled)
        if (enabled) {
            AppNotificationScheduler.schedule(context.applicationContext)
        } else {
            AppNotificationScheduler.cancel(context.applicationContext)
        }
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
            CsvReportType.MEAL_DELIVERIES -> useCases.reports.mealDeliveriesCsv(snapshot)
            CsvReportType.PURCHASES -> useCases.reports.purchasesCsv(snapshot)
            CsvReportType.INVENTORY -> useCases.reports.inventoryCsv(snapshot)
            CsvReportType.RECEIVABLES -> useCases.reports.receivablesCsv(snapshot)
            CsvReportType.SUPPLIER_DEBTS -> useCases.reports.supplierDebtsCsv(snapshot)
            CsvReportType.PAYMENTS -> useCases.reports.paymentsCsv(snapshot)
            CsvReportType.EXPENSES -> useCases.reports.expensesCsv(snapshot)
            CsvReportType.PROFIT_LOSS -> useCases.reports.profitLossCsv(snapshot)
            CsvReportType.PROJECT_PROFIT -> useCases.reports.projectProfitCsv(snapshot)
            CsvReportType.CASH_FLOW -> useCases.reports.cashFlowCsv(snapshot)
        }
        val file = repository.exportCsv(context, "${type.filePrefix}-${System.currentTimeMillis()}.csv", csv)
        _messages.tryEmit("فایل CSV: ${file.absolutePath}")
        shareFile(context, file, "text/csv")
    }

    fun deleteMealDelivery(id: Long) = ioAction("تحویل غذا حذف شد") {
        repository.deleteMealDelivery(id).getOrThrow()
    }

    fun deleteWarehouse(id: Long) = ioAction("انبار حذف یا غیرفعال شد") {
        repository.deleteWarehouse(id).getOrThrow()
    }

    fun deleteMaterial(id: Long) = ioAction("متریال حذف یا غیرفعال شد") {
        repository.deleteMaterial(id).getOrThrow()
    }

    fun deleteSupplier(id: Long) = ioAction("تامین‌کننده حذف یا غیرفعال شد") {
        repository.deleteSupplier(id).getOrThrow()
    }

    fun deleteBankCard(id: Long) = ioAction("کارت بانکی حذف یا غیرفعال شد") {
        repository.deleteBankCard(id).getOrThrow()
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
        if (activeOperations.value > 0) return
        activeOperations.value = 1
        viewModelScope.launch(Dispatchers.IO) {
            try {
                block()
                _messages.emit(success)
                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (error: Throwable) {
                if (error is CancellationException) throw error
                _messages.emit(error.userFacingMessage("خطای ناشناخته رخ داد"))
            } finally {
                activeOperations.value = 0
            }
        }
    }

    private fun Throwable.userFacingMessage(fallback: String): String =
        message?.takeIf { it.isNotBlank() } ?: fallback

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
