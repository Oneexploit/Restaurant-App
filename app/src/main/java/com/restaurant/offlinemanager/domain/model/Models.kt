package com.restaurant.offlinemanager.domain.model

import com.restaurant.offlinemanager.data.local.entity.BankCardEntity
import com.restaurant.offlinemanager.data.local.entity.ExpenseEntity
import com.restaurant.offlinemanager.data.local.entity.MaterialCategoryEntity
import com.restaurant.offlinemanager.data.local.entity.MaterialEntity
import com.restaurant.offlinemanager.data.local.entity.MealDeliveryEntity
import com.restaurant.offlinemanager.data.local.entity.PaymentMethod
import com.restaurant.offlinemanager.data.local.entity.ProjectEntity
import com.restaurant.offlinemanager.data.local.entity.ProjectPaymentEntity
import com.restaurant.offlinemanager.data.local.entity.PurchaseEntity
import com.restaurant.offlinemanager.data.local.entity.PurchaseItemEntity
import com.restaurant.offlinemanager.data.local.entity.PurchasePaymentType
import com.restaurant.offlinemanager.data.local.entity.StockReason
import com.restaurant.offlinemanager.data.local.entity.StockTransactionEntity
import com.restaurant.offlinemanager.data.local.entity.StockTransactionType
import com.restaurant.offlinemanager.data.local.entity.SupplierEntity
import com.restaurant.offlinemanager.data.local.entity.SupplierPaymentEntity
import com.restaurant.offlinemanager.data.local.entity.UnitType
import com.restaurant.offlinemanager.data.local.entity.WarehouseEntity

data class RestaurantSnapshot(
    val projects: List<ProjectEntity> = emptyList(),
    val mealDeliveries: List<MealDeliveryEntity> = emptyList(),
    val warehouses: List<WarehouseEntity> = emptyList(),
    val materialCategories: List<MaterialCategoryEntity> = emptyList(),
    val materials: List<MaterialEntity> = emptyList(),
    val suppliers: List<SupplierEntity> = emptyList(),
    val stockTransactions: List<StockTransactionEntity> = emptyList(),
    val purchases: List<PurchaseEntity> = emptyList(),
    val purchaseItems: List<PurchaseItemEntity> = emptyList(),
    val bankCards: List<BankCardEntity> = emptyList(),
    val projectPayments: List<ProjectPaymentEntity> = emptyList(),
    val supplierPayments: List<SupplierPaymentEntity> = emptyList(),
    val expenses: List<ExpenseEntity> = emptyList()
)

data class DashboardStats(
    val activeProjectsCount: Int = 0,
    val todayMealCount: Int = 0,
    val todayPurchasesTotal: Long = 0,
    val projectReceivablesTotal: Long = 0,
    val supplierDebtsTotal: Long = 0,
    val lowStockItemCount: Int = 0,
    val totalInventoryValue: Long = 0,
    val bankCardsTotalBalance: Long = 0,
    val monthPurchasesTotal: Long = 0,
    val monthReceivedTotal: Long = 0,
    val monthExpensesTotal: Long = 0
)

data class InventoryItem(
    val materialId: Long,
    val materialName: String,
    val warehouseId: Long,
    val warehouseName: String,
    val unit: UnitType,
    val quantity: Double,
    val approximateValue: Long,
    val minimumStock: Double,
    val isLowStock: Boolean,
    val emoji: String?
)

data class ProjectFinance(
    val project: ProjectEntity,
    val totalDelivered: Long,
    val totalPaid: Long,
    val receivable: Long,
    val totalMeals: Int
)

data class SupplierDebt(
    val supplier: SupplierEntity,
    val totalCreditPurchases: Long,
    val totalPaid: Long,
    val remaining: Long
)

data class BankCardBalance(
    val card: BankCardEntity,
    val balance: Long
)

data class MonthlyPoint(
    val label: String,
    val income: Long,
    val expense: Long,
    val purchases: Long
)

data class ProjectInput(
    val id: Long = 0,
    val name: String,
    val companyName: String?,
    val address: String?,
    val managerName: String?,
    val phone: String?,
    val workerCount: Int,
    val mealPrice: Long,
    val defaultMealType: String,
    val startDate: Long,
    val endDate: Long?,
    val status: com.restaurant.offlinemanager.data.local.entity.ProjectStatus,
    val notes: String?
)

data class MealDeliveryInput(
    val projectId: Long,
    val date: Long,
    val mealType: com.restaurant.offlinemanager.data.local.entity.MealType,
    val quantity: Int,
    val unitPrice: Long,
    val notes: String?
)

data class StockTransactionInput(
    val warehouseId: Long,
    val materialId: Long,
    val projectId: Long?,
    val supplierId: Long?,
    val type: StockTransactionType,
    val reason: StockReason,
    val quantity: Double,
    val unit: UnitType,
    val unitPrice: Long?,
    val date: Long,
    val notes: String?
)

data class PurchaseItemInput(
    val materialId: Long,
    val quantity: Double,
    val unit: UnitType,
    val unitPrice: Long
) {
    val totalAmount: Long get() = (quantity * unitPrice).toLong()
}

data class PurchaseInput(
    val supplierId: Long?,
    val warehouseId: Long,
    val date: Long,
    val invoiceNumber: String?,
    val paymentType: PurchasePaymentType,
    val bankCardId: Long?,
    val discountAmount: Long,
    val notes: String?,
    val items: List<PurchaseItemInput>
)

data class ProjectPaymentInput(
    val projectId: Long,
    val bankCardId: Long?,
    val amount: Long,
    val date: Long,
    val method: PaymentMethod,
    val notes: String?
)

data class SupplierPaymentInput(
    val supplierId: Long,
    val bankCardId: Long?,
    val amount: Long,
    val date: Long,
    val method: PaymentMethod,
    val notes: String?
)
