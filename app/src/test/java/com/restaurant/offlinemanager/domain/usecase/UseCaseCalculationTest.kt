package com.restaurant.offlinemanager.domain.usecase

import android.content.Context
import android.net.Uri
import com.restaurant.offlinemanager.data.local.entity.BankCardEntity
import com.restaurant.offlinemanager.data.local.entity.CookingAllocationEntity
import com.restaurant.offlinemanager.data.local.entity.CookingBatchEntity
import com.restaurant.offlinemanager.data.local.entity.ExpenseCategory
import com.restaurant.offlinemanager.data.local.entity.ExpenseEntity
import com.restaurant.offlinemanager.data.local.entity.DeliveryStatus
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
import com.restaurant.offlinemanager.domain.model.CookingBatchInput
import com.restaurant.offlinemanager.domain.model.ProjectInput
import com.restaurant.offlinemanager.domain.model.ProjectPaymentInput
import com.restaurant.offlinemanager.domain.model.PurchaseItemInput
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
    fun quantityInputKeepsDecimalsAndAddsThousandsSeparators() {
        val formatted = com.restaurant.offlinemanager.core.utils.NumberFormatter.formatQuantityInput("۱۲۵۰۰٫۷۵")

        assertEquals("۱۲,۵۰۰.۷۵", formatted)
        assertEquals("12500.75", com.restaurant.offlinemanager.core.utils.NumberFormatter.normalizeDigits(formatted))
    }

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
    fun projectFinanceCountsOnlyFinalDeliveryAndSubtractsReturns() {
        val project = project()
        val delivered = MealDeliveryEntity(
            id = 1,
            projectId = project.id,
            date = now,
            mealType = MealType.LUNCH,
            status = DeliveryStatus.DELIVERED,
            quantity = 10,
            returnedQuantity = 2,
            unitPrice = 100,
            totalAmount = 800,
            createdAt = now,
            updatedAt = now
        )
        val dispatched = delivered.copy(id = 2, status = DeliveryStatus.DISPATCHED, returnedQuantity = 0, totalAmount = 0)
        val cancelled = delivered.copy(id = 3, status = DeliveryStatus.CANCELLED, returnedQuantity = 0, totalAmount = 0)
        val snapshot = RestaurantSnapshot(projects = listOf(project), mealDeliveries = listOf(delivered, dispatched, cancelled))

        val result = ProjectFinanceUseCase(FakeRepository(snapshot)).calculateProjectFinances(snapshot).single()

        assertEquals(8, result.totalMeals)
        assertEquals(800, result.totalDelivered)
        assertEquals(800, result.receivable)
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
                stock(warehouse.id, material.id, StockTransactionType.IN, 10.0).copy(id = 1, createdAt = now),
                stock(warehouse.id, material.id, StockTransactionType.OUT, 3.0).copy(id = 2, createdAt = now + 1),
                stock(warehouse.id, material.id, StockTransactionType.WASTE, 1.0).copy(id = 3, createdAt = now + 2),
                stock(warehouse.id, material.id, StockTransactionType.ADJUSTMENT, -2.0).copy(id = 4, createdAt = now + 3)
            )
        )
        val useCase = InventoryUseCase(FakeRepository(snapshot))

        val result = useCase.calculateInventory(snapshot).single()

        assertEquals(4.0, result.quantity, 0.001)
        assertEquals(400, result.approximateValue)
        assertEquals(true, result.isLowStock)
    }

    @Test
    fun inventoryIncludesMaterialsWithNoTransactionsAsZeroStock() {
        val warehouse = warehouse()
        val material = material()
        val snapshot = RestaurantSnapshot(
            warehouses = listOf(warehouse),
            materials = listOf(material)
        )

        val result = InventoryUseCase(FakeRepository(snapshot)).calculateInventory(snapshot).single()

        assertEquals(0.0, result.quantity, 0.001)
        assertEquals(0, result.approximateValue)
        assertTrue(result.isLowStock)
    }

    @Test
    fun inventoryUsesLatestManualTransactionPrice() {
        val warehouse = warehouse()
        val material = material()
        val snapshot = RestaurantSnapshot(
            warehouses = listOf(warehouse),
            materials = listOf(material),
            purchaseItems = listOf(purchaseItem(material.id, unitPrice = 100)),
            stockTransactions = listOf(
                stock(warehouse.id, material.id, StockTransactionType.IN, 3.0)
                    .copy(unitPrice = 250, date = now + 1, updatedAt = now + 1)
            )
        )

        val result = InventoryUseCase(FakeRepository(snapshot)).calculateInventory(snapshot).single()

        assertEquals(750, result.approximateValue)
    }

    @Test
    fun inventoryUsesMovingWeightedAverageAndAllocatesPurchaseDiscount() {
        val warehouse = warehouse()
        val material = material()
        val purchase = purchase(supplierId = 1, total = 180, paymentType = PurchasePaymentType.CASH)
        val item = purchaseItem(material.id, unitPrice = 100).copy(purchaseId = purchase.id, quantity = 2.0, totalAmount = 200)
        val snapshot = RestaurantSnapshot(
            warehouses = listOf(warehouse),
            materials = listOf(material),
            purchases = listOf(purchase),
            purchaseItems = listOf(item),
            stockTransactions = listOf(
                stock(warehouse.id, material.id, StockTransactionType.IN, 2.0).copy(id = 1, purchaseId = purchase.id, createdAt = now),
                stock(warehouse.id, material.id, StockTransactionType.OUT, 1.0).copy(id = 2, createdAt = now + 1)
            )
        )
        val inventory = InventoryUseCase(FakeRepository(snapshot))

        val result = inventory.calculateInventory(snapshot).single()

        assertEquals(1.0, result.quantity, 0.001)
        assertEquals(90, result.averageUnitCost)
        assertEquals(90, result.approximateValue)
        assertEquals(90, inventory.costOfGoodsConsumed(snapshot))
    }

    @Test
    fun cookingCostIsAllocatedBetweenCompaniesByMealQuantity() {
        val warehouse = warehouse()
        val material = material()
        val firstProject = project(1)
        val secondProject = project(2)
        val batch = CookingBatchEntity(10, warehouse.id, now + 1, MealType.LUNCH, 4, createdAt = now + 1, updatedAt = now + 1)
        val snapshot = RestaurantSnapshot(
            projects = listOf(firstProject, secondProject), warehouses = listOf(warehouse), materials = listOf(material),
            cookingBatches = listOf(batch),
            cookingAllocations = listOf(
                CookingAllocationEntity(1, batch.id, firstProject.id, 1, now, now),
                CookingAllocationEntity(2, batch.id, secondProject.id, 3, now, now)
            ),
            stockTransactions = listOf(
                stock(warehouse.id, material.id, StockTransactionType.IN, 10.0).copy(id = 1, unitPrice = 100, date = now, createdAt = now),
                stock(warehouse.id, material.id, StockTransactionType.OUT, 4.0).copy(id = 2, cookingBatchId = batch.id, reason = StockReason.COOKING_USAGE, date = now + 1, createdAt = now + 1)
            )
        )

        val inventory = InventoryUseCase(FakeRepository(snapshot))
        val costs = inventory.projectMaterialCosts(snapshot)
        val batchCost = inventory.cookingBatchCosts(snapshot).single()

        assertEquals(100L, costs[firstProject.id])
        assertEquals(300L, costs[secondProject.id])
        assertEquals(400L, batchCost.totalCost)
        assertEquals(100L, batchCost.costPerMeal)
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
    fun accountingSeparatesAccrualProfitFromCashFlow() {
        val warehouse = warehouse()
        val material = material()
        val project = project()
        val stockIn = stock(warehouse.id, material.id, StockTransactionType.IN, 10.0)
            .copy(id = 1, unitPrice = 100, createdAt = now)
        val projectConsumption = stock(warehouse.id, material.id, StockTransactionType.OUT, 2.0)
            .copy(id = 2, projectId = project.id, createdAt = now + 1)
        val snapshot = RestaurantSnapshot(
            projects = listOf(project),
            warehouses = listOf(warehouse),
            materials = listOf(material),
            mealDeliveries = listOf(meal(project.id, 1_000)),
            projectPayments = listOf(projectPayment(project.id, 600)),
            purchases = listOf(purchase(supplierId = 1, total = 1_000, paymentType = PurchasePaymentType.CASH)),
            expenses = listOf(expense(amount = 50, cardId = 1)),
            stockTransactions = listOf(stockIn, projectConsumption)
        )
        val inventory = InventoryUseCase(FakeRepository(snapshot))
        val accounting = AccountingUseCase(inventory)

        val summary = accounting.summary(snapshot)
        val projectProfit = accounting.projectProfits(snapshot).single()

        assertEquals(1_000, summary.earnedRevenue)
        assertEquals(200, summary.costOfGoodsConsumed)
        assertEquals(750, summary.netProfit)
        assertEquals(-450, summary.netCashFlow)
        assertEquals(800, projectProfit.grossProfit)
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
            InventoryUseCase(repository),
            AccountingUseCase(InventoryUseCase(repository))
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
        val reducedPurchaseIn = originalPurchaseIn.copy(id = 0, quantity = 5.0)
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
        val reducedPurchaseIn = originalPurchaseIn.copy(id = 0, quantity = 5.0)
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

    @Test
    fun inventoryIntegrityRejectsBackdatedTransactionThatBreaksLaterBalance() {
        val warehouse = warehouse()
        val material = material()
        val incoming = stock(warehouse.id, material.id, StockTransactionType.IN, 5.0)
            .copy(id = 1, date = now, createdAt = now)
        val consumed = stock(warehouse.id, material.id, StockTransactionType.OUT, 5.0)
            .copy(id = 2, date = now + 2, createdAt = now + 2)
        val backdatedWaste = stock(warehouse.id, material.id, StockTransactionType.WASTE, 1.0)
            .copy(id = 3, date = now + 1, createdAt = now + 3)
        val snapshot = RestaurantSnapshot(warehouses = listOf(warehouse), materials = listOf(material))

        assertThrows(IllegalArgumentException::class.java) {
            InventoryIntegrityValidator.validateNonNegative(snapshot, listOf(incoming, consumed, backdatedWaste))
        }
    }

    @Test
    fun purchaseItemRejectsNonFiniteQuantity() {
        val item = PurchaseItemInput(materialId = 1, quantity = Double.POSITIVE_INFINITY, unit = UnitType.KG, unitPrice = 100)

        assertThrows(IllegalArgumentException::class.java) { item.totalAmount }
    }

    private fun project(id: Long = 1): ProjectEntity =
        ProjectEntity(
            id = id, name = "پروژه تست", workerCount = 10, mealPrice = 100,
            breakfastPrice = 100, lunchPrice = 100, dinnerPrice = 100,
            defaultMealType = "ناهار", startDate = now, status = ProjectStatus.ACTIVE,
            createdAt = now, updatedAt = now
        )

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
        MaterialEntity(id, "برنج تست", UnitType.KG, minimumStock = 5.0, isActive = true, createdAt = now, updatedAt = now)

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
    override suspend fun saveCookingBatch(input: CookingBatchInput): Result<Long> = unsupported()
    override suspend fun saveWarehouse(entity: WarehouseEntity): Long = unsupported()
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
    override suspend fun deleteCookingBatch(id: Long): Result<Unit> = unsupported()
    override suspend fun deleteWarehouse(id: Long): Result<Unit> = unsupported()
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
