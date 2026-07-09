package com.restaurant.offlinemanager.domain.usecase

import android.content.Context
import android.net.Uri
import com.restaurant.offlinemanager.data.local.entity.BankCardEntity
import com.restaurant.offlinemanager.data.local.entity.ExpenseCategory
import com.restaurant.offlinemanager.data.local.entity.ExpenseEntity
import com.restaurant.offlinemanager.data.local.entity.MaterialCategoryEntity
import com.restaurant.offlinemanager.data.local.entity.MaterialEntity
import com.restaurant.offlinemanager.data.local.entity.MealDeliveryEntity
import com.restaurant.offlinemanager.data.local.entity.MealType
import com.restaurant.offlinemanager.data.local.entity.PaymentMethod
import com.restaurant.offlinemanager.data.local.entity.ProjectEntity
import com.restaurant.offlinemanager.data.local.entity.ProjectPaymentEntity
import com.restaurant.offlinemanager.data.local.entity.ProjectStatus
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
import com.restaurant.offlinemanager.data.local.entity.WarehouseType
import com.restaurant.offlinemanager.domain.model.MealDeliveryInput
import com.restaurant.offlinemanager.domain.model.ProjectInput
import com.restaurant.offlinemanager.domain.model.ProjectPaymentInput
import com.restaurant.offlinemanager.domain.model.PurchaseInput
import com.restaurant.offlinemanager.domain.model.RestaurantSnapshot
import com.restaurant.offlinemanager.domain.model.StockTransactionInput
import com.restaurant.offlinemanager.domain.model.SupplierPaymentInput
import com.restaurant.offlinemanager.domain.repository.RestaurantRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class UseCaseCalculationTest {
    private val now = 1_000L

    @Test
    fun projectReceivableSubtractsPaymentsFromDeliveries() {
        val project = project()
        val snapshot = RestaurantSnapshot(
            projects = listOf(project),
            mealDeliveries = listOf(meal(project.id, 1_000), meal(project.id, 2_000)),
            projectPayments = listOf(projectPayment(project.id, 750))
        )
        val useCase = ProjectFinanceUseCase(FakeRepository(snapshot))

        val result = useCase.calculateProjectFinances(snapshot).single()

        assertEquals(3_000, result.totalDelivered)
        assertEquals(750, result.totalPaid)
        assertEquals(2_250, result.receivable)
    }

    @Test
    fun supplierDebtUsesOnlyCreditPurchasesAndSubtractsPayments() {
        val supplier = supplier()
        val snapshot = RestaurantSnapshot(
            suppliers = listOf(supplier),
            purchases = listOf(
                purchase(supplier.id, 2_000, PurchasePaymentType.CREDIT),
                purchase(supplier.id, 900, PurchasePaymentType.CARD)
            ),
            supplierPayments = listOf(supplierPayment(supplier.id, 500))
        )
        val useCase = SupplierDebtUseCase(FakeRepository(snapshot))

        val result = useCase.calculateSupplierDebts(snapshot).single()

        assertEquals(2_000, result.totalCreditPurchases)
        assertEquals(500, result.totalPaid)
        assertEquals(1_500, result.remaining)
    }

    @Test
    fun inventoryCalculatesSignedStockAndApproximateValue() {
        val warehouse = warehouse()
        val material = material()
        val snapshot = RestaurantSnapshot(
            warehouses = listOf(warehouse),
            materials = listOf(material),
            purchaseItems = listOf(purchaseItem(material.id, unitPrice = 100)),
            stockTransactions = listOf(
                stock(warehouse.id, material.id, StockTransactionType.IN, 10.0),
                stock(warehouse.id, material.id, StockTransactionType.OUT, 3.0),
                stock(warehouse.id, material.id, StockTransactionType.WASTE, 1.0),
                stock(warehouse.id, material.id, StockTransactionType.ADJUSTMENT, -2.0)
            )
        )
        val useCase = InventoryUseCase(FakeRepository(snapshot))

        val result = useCase.calculateInventory(snapshot).single()

        assertEquals(4.0, result.quantity, 0.001)
        assertEquals(400, result.approximateValue)
        assertEquals(true, result.isLowStock)
    }

    @Test
    fun bankCardBalanceCombinesIncomeAndOutflows() {
        val card = bankCard(initial = 1_000)
        val snapshot = RestaurantSnapshot(
            bankCards = listOf(card),
            projectPayments = listOf(projectPayment(projectId = 1, amount = 300, cardId = card.id)),
            purchases = listOf(purchase(supplierId = 1, total = 200, paymentType = PurchasePaymentType.CARD, cardId = card.id)),
            supplierPayments = listOf(supplierPayment(supplierId = 1, amount = 50, cardId = card.id)),
            expenses = listOf(expense(amount = 70, cardId = card.id))
        )
        val useCase = BankCardBalanceUseCase(FakeRepository(snapshot))

        val result = useCase.calculateBalances(snapshot).single()

        assertEquals(980, result.balance)
    }

    @Test
    fun dashboardUsesCurrentJalaliMonthForMonthlyTotals() {
        val today = com.restaurant.offlinemanager.core.utils.PersianDateFormatter.todayStartMillis()
        val older = com.restaurant.offlinemanager.core.utils.PersianDateFormatter.shiftDays(today, -45)
        val snapshot = RestaurantSnapshot(
            purchases = listOf(
                purchase(supplierId = 1, total = 1_000, paymentType = PurchasePaymentType.CASH).copy(date = today),
                purchase(supplierId = 1, total = 9_000, paymentType = PurchasePaymentType.CASH).copy(id = 9_000, date = older)
            ),
            projectPayments = listOf(
                projectPayment(projectId = 1, amount = 700).copy(date = today),
                projectPayment(projectId = 1, amount = 4_000).copy(id = 4_000, date = older)
            ),
            expenses = listOf(
                expense(amount = 300, cardId = 1).copy(date = today),
                expense(amount = 2_000, cardId = 1).copy(id = 2_000, date = older)
            )
        )
        val repository = FakeRepository(snapshot)
        val dashboard = DashboardUseCase(
            repository,
            ProjectFinanceUseCase(repository),
            SupplierDebtUseCase(repository),
            InventoryUseCase(repository),
            BankCardBalanceUseCase(repository)
        )

        val result = dashboard.calculate(snapshot)

        assertEquals(1_000, result.monthPurchasesTotal)
        assertEquals(700, result.monthReceivedTotal)
        assertEquals(300, result.monthExpensesTotal)
    }

    @Test
    fun reportsExportExpensesCsv() {
        val card = bankCard(initial = 0)
        val snapshot = RestaurantSnapshot(
            bankCards = listOf(card),
            expenses = listOf(expense(amount = 120, cardId = card.id).copy(title = "Fuel"))
        )
        val repository = FakeRepository(snapshot)
        val reports = ReportsUseCase(
            repository,
            ProjectFinanceUseCase(repository),
            SupplierDebtUseCase(repository),
            InventoryUseCase(repository)
        )

        val csv = reports.expensesCsv(snapshot)

        assertTrue(csv.contains("date,title,category,amount,bank_card,notes"))
        assertTrue(csv.contains("Fuel"))
        assertTrue(csv.contains("120"))
    }

    @Test
    fun inventoryIntegrityRejectsPurchaseEditThatWouldMakeStockNegative() {
        val warehouse = warehouse()
        val material = material()
        val originalPurchaseIn = stock(warehouse.id, material.id, StockTransactionType.IN, 10.0)
            .copy(id = 1, purchaseId = 1, reason = StockReason.PURCHASE)
        val consumed = stock(warehouse.id, material.id, StockTransactionType.OUT, 8.0)
            .copy(id = 2)
        val reducedPurchaseIn = originalPurchaseIn.copy(id = 3, quantity = 5.0)
        val snapshot = RestaurantSnapshot(
            warehouses = listOf(warehouse),
            materials = listOf(material),
            stockTransactions = listOf(originalPurchaseIn, consumed)
        )

        assertThrows(IllegalArgumentException::class.java) {
            InventoryIntegrityValidator.validateNonNegative(snapshot, listOf(consumed, reducedPurchaseIn))
        }
    }

    @Test
    fun inventoryIntegrityAllowsPurchaseEditWhenStockStaysPositive() {
        val warehouse = warehouse()
        val material = material()
        val originalPurchaseIn = stock(warehouse.id, material.id, StockTransactionType.IN, 10.0)
            .copy(id = 1, purchaseId = 1, reason = StockReason.PURCHASE)
        val consumed = stock(warehouse.id, material.id, StockTransactionType.OUT, 3.0)
            .copy(id = 2)
        val reducedPurchaseIn = originalPurchaseIn.copy(id = 3, quantity = 5.0)
        val snapshot = RestaurantSnapshot(
            warehouses = listOf(warehouse),
            materials = listOf(material),
            stockTransactions = listOf(originalPurchaseIn, consumed)
        )

        InventoryIntegrityValidator.validateNonNegative(snapshot, listOf(consumed, reducedPurchaseIn))
    }

    @Test
    fun inventoryIntegrityRejectsDeletingManualInTransactionWhenConsumed() {
        val warehouse = warehouse()
        val material = material()
        val manualIn = stock(warehouse.id, material.id, StockTransactionType.IN, 5.0)
            .copy(id = 1)
        val consumed = stock(warehouse.id, material.id, StockTransactionType.OUT, 5.0)
            .copy(id = 2)
        val snapshot = RestaurantSnapshot(
            warehouses = listOf(warehouse),
            materials = listOf(material),
            stockTransactions = listOf(manualIn, consumed)
        )

        assertThrows(IllegalArgumentException::class.java) {
            InventoryIntegrityValidator.validateNonNegative(snapshot, listOf(consumed))
        }
    }

    private fun project(id: Long = 1): ProjectEntity =
        ProjectEntity(id, "پروژه تست", null, null, null, null, 10, 100, "ناهار", now, null, ProjectStatus.ACTIVE, null, now, now)

    private fun meal(projectId: Long, total: Long): MealDeliveryEntity =
        MealDeliveryEntity(id = total, projectId = projectId, date = now, mealType = MealType.LUNCH, quantity = 1, unitPrice = total, totalAmount = total, createdAt = now, updatedAt = now)

    private fun projectPayment(projectId: Long, amount: Long, cardId: Long? = null): ProjectPaymentEntity =
        ProjectPaymentEntity(id = amount, projectId = projectId, bankCardId = cardId, amount = amount, date = now, method = PaymentMethod.BANK_TRANSFER, createdAt = now, updatedAt = now)

    private fun supplier(id: Long = 1): SupplierEntity =
        SupplierEntity(id, "تامین‌کننده تست", isActive = true, createdAt = now, updatedAt = now)

    private fun supplierPayment(supplierId: Long, amount: Long, cardId: Long? = null): SupplierPaymentEntity =
        SupplierPaymentEntity(id = amount, supplierId = supplierId, bankCardId = cardId, amount = amount, date = now, method = PaymentMethod.CARD_TO_CARD, createdAt = now, updatedAt = now)

    private fun warehouse(id: Long = 1): WarehouseEntity =
        WarehouseEntity(id, "انبار تست", WarehouseType.GENERAL, isActive = true, createdAt = now, updatedAt = now)

    private fun material(id: Long = 1): MaterialEntity =
        MaterialEntity(id, "برنج تست", null, UnitType.KG, minimumStock = 5.0, isActive = true, createdAt = now, updatedAt = now)

    private fun stock(warehouseId: Long, materialId: Long, type: StockTransactionType, quantity: Double): StockTransactionEntity =
        StockTransactionEntity(id = quantity.toLong() + type.ordinal, warehouseId = warehouseId, materialId = materialId, type = type, reason = StockReason.MANUAL_ADJUSTMENT, quantity = quantity, unit = UnitType.KG, date = now, createdAt = now, updatedAt = now)

    private fun purchaseItem(materialId: Long, unitPrice: Long): PurchaseItemEntity =
        PurchaseItemEntity(id = 1, purchaseId = 1, materialId = materialId, quantity = 1.0, unit = UnitType.KG, unitPrice = unitPrice, totalAmount = unitPrice, createdAt = now, updatedAt = now)

    private fun purchase(supplierId: Long, total: Long, paymentType: PurchasePaymentType, cardId: Long? = null): PurchaseEntity =
        PurchaseEntity(id = total, supplierId = supplierId, warehouseId = 1, date = now, paymentType = paymentType, bankCardId = cardId, discountAmount = 0, totalAmount = total, paidAmount = if (paymentType == PurchasePaymentType.CREDIT) 0 else total, createdAt = now, updatedAt = now)

    private fun bankCard(initial: Long): BankCardEntity =
        BankCardEntity(id = 1, title = "کارت تست", initialBalance = initial, isActive = true, createdAt = now, updatedAt = now)

    private fun expense(amount: Long, cardId: Long): ExpenseEntity =
        ExpenseEntity(id = amount, title = "هزینه تست", category = ExpenseCategory.OTHER, amount = amount, date = now, bankCardId = cardId, createdAt = now, updatedAt = now)
}

private class FakeRepository(private val snapshot: RestaurantSnapshot) : RestaurantRepository {
    override fun observeSnapshot(): Flow<RestaurantSnapshot> = flowOf(snapshot)
    override suspend fun currentSnapshot(): RestaurantSnapshot = snapshot
    override suspend fun saveProject(input: ProjectInput): Long = unsupported()
    override suspend fun archiveProject(projectId: Long): Result<Unit> = unsupported()
    override suspend fun saveMealDelivery(input: MealDeliveryInput): Result<Long> = unsupported()
    override suspend fun saveWarehouse(entity: WarehouseEntity): Long = unsupported()
    override suspend fun saveMaterialCategory(entity: MaterialCategoryEntity): Long = unsupported()
    override suspend fun saveMaterial(entity: MaterialEntity): Long = unsupported()
    override suspend fun saveSupplier(entity: SupplierEntity): Long = unsupported()
    override suspend fun saveStockTransaction(input: StockTransactionInput): Result<Long> = unsupported()
    override suspend fun saveStockTransactions(inputs: List<StockTransactionInput>): Result<List<Long>> = unsupported()
    override suspend fun savePurchase(input: PurchaseInput): Result<Long> = unsupported()
    override suspend fun saveBankCard(entity: BankCardEntity): Long = unsupported()
    override suspend fun saveProjectPayment(input: ProjectPaymentInput): Result<Long> = unsupported()
    override suspend fun saveSupplierPayment(input: SupplierPaymentInput): Result<Long> = unsupported()
    override suspend fun saveExpense(entity: ExpenseEntity): Long = unsupported()
    override suspend fun deleteMealDelivery(id: Long): Result<Unit> = unsupported()
    override suspend fun deleteWarehouse(id: Long): Result<Unit> = unsupported()
    override suspend fun deleteMaterialCategory(id: Long): Result<Unit> = unsupported()
    override suspend fun deleteMaterial(id: Long): Result<Unit> = unsupported()
    override suspend fun deleteSupplier(id: Long): Result<Unit> = unsupported()
    override suspend fun deleteBankCard(id: Long): Result<Unit> = unsupported()
    override suspend fun deleteStockTransaction(id: Long): Result<Unit> = unsupported()
    override suspend fun deletePurchase(id: Long): Result<Unit> = unsupported()
    override suspend fun deleteProjectPayment(id: Long): Result<Unit> = unsupported()
    override suspend fun deleteSupplierPayment(id: Long): Result<Unit> = unsupported()
    override suspend fun deleteExpense(id: Long): Result<Unit> = unsupported()
    override suspend fun exportBackup(context: Context): File = unsupported()
    override suspend fun restoreBackup(context: Context, uri: Uri): Result<Unit> = unsupported()
    override suspend fun exportCsv(context: Context, fileName: String, csv: String): File = unsupported()

    private fun unsupported(): Nothing = error("این متد در تست محاسبات استفاده نمی‌شود")
}
