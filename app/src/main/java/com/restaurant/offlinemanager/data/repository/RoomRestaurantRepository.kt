package com.restaurant.offlinemanager.data.repository

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.restaurant.offlinemanager.core.utils.PersianDateFormatter
import com.restaurant.offlinemanager.data.local.AppDatabase
import com.restaurant.offlinemanager.data.local.dao.RestaurantDao
import com.restaurant.offlinemanager.data.local.entity.BankCardEntity
import com.restaurant.offlinemanager.data.local.entity.CookingAllocationEntity
import com.restaurant.offlinemanager.data.local.entity.CookingBatchEntity
import com.restaurant.offlinemanager.data.local.entity.ExpenseCategory
import com.restaurant.offlinemanager.data.local.entity.ExpenseEntity
import com.restaurant.offlinemanager.data.local.entity.MaterialEntity
import com.restaurant.offlinemanager.data.local.entity.MealDeliveryEntity
import com.restaurant.offlinemanager.data.local.entity.DeliveryStatus
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
import com.restaurant.offlinemanager.data.mapper.toEntity
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
import com.restaurant.offlinemanager.domain.usecase.InventoryIntegrityValidator
import com.restaurant.offlinemanager.domain.usecase.InventoryUseCase
import com.restaurant.offlinemanager.domain.usecase.ProjectFinanceUseCase
import com.restaurant.offlinemanager.domain.usecase.SupplierDebtUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class RoomRestaurantRepository(
    private val database: AppDatabase
) : RestaurantRepository {
    private val dao: RestaurantDao = database.restaurantDao()

    override fun observeSnapshot(): Flow<RestaurantSnapshot> {
        val flows = arrayOf(
            dao.observeProjects().map { it as Any },
            dao.observeMealDeliveries().map { it as Any },
            dao.observeWarehouses().map { it as Any },
            dao.observeMaterials().map { it as Any },
            dao.observeSuppliers().map { it as Any },
            dao.observeStockTransactions().map { it as Any },
            dao.observePurchases().map { it as Any },
            dao.observePurchaseItems().map { it as Any },
            dao.observeBankCards().map { it as Any },
            dao.observeProjectPayments().map { it as Any },
            dao.observeSupplierPayments().map { it as Any },
            dao.observeExpenses().map { it as Any },
            dao.observeCookingBatches().map { it as Any },
            dao.observeCookingAllocations().map { it as Any }
        )
        return combine(*flows) { values ->
            @Suppress("UNCHECKED_CAST")
            RestaurantSnapshot(
                projects = values[0] as List<ProjectEntity>,
                mealDeliveries = values[1] as List<MealDeliveryEntity>,
                warehouses = values[2] as List<WarehouseEntity>,
                materials = values[3] as List<MaterialEntity>,
                suppliers = values[4] as List<SupplierEntity>,
                stockTransactions = values[5] as List<StockTransactionEntity>,
                purchases = values[6] as List<PurchaseEntity>,
                purchaseItems = values[7] as List<PurchaseItemEntity>,
                bankCards = values[8] as List<BankCardEntity>,
                projectPayments = values[9] as List<ProjectPaymentEntity>,
                supplierPayments = values[10] as List<SupplierPaymentEntity>,
                expenses = values[11] as List<ExpenseEntity>,
                cookingBatches = values[12] as List<CookingBatchEntity>,
                cookingAllocations = values[13] as List<CookingAllocationEntity>
            )
        }
    }

    override suspend fun currentSnapshot(): RestaurantSnapshot =
        RestaurantSnapshot(
            projects = dao.getProjects(),
            mealDeliveries = dao.getMealDeliveries(),
            warehouses = dao.getWarehouses(),
            materials = dao.getMaterials(),
            suppliers = dao.getSuppliers(),
            stockTransactions = dao.getStockTransactions(),
            purchases = dao.getPurchases(),
            purchaseItems = dao.getPurchaseItems(),
            bankCards = dao.getBankCards(),
            projectPayments = dao.getProjectPayments(),
            supplierPayments = dao.getSupplierPayments(),
            expenses = dao.getExpenses(),
            cookingBatches = dao.getCookingBatches(),
            cookingAllocations = dao.getCookingAllocations()
        )

    override suspend fun saveProject(input: ProjectInput): Long {
        require(input.name.isNotBlank()) { "نام پروژه الزامی است" }
        require(input.workerCount > 0) { "تعداد نفرات باید بیشتر از صفر باشد" }
        require(input.mealPrice > 0) { "قیمت هر وعده باید بیشتر از صفر باشد" }
        require(input.breakfastPrice > 0 && input.lunchPrice > 0 && input.dinnerPrice > 0) { "قیمت همه وعده‌ها باید بیشتر از صفر باشد" }
        require(input.endDate == null || input.endDate >= input.startDate) { "تاریخ پایان نمی‌تواند قبل از تاریخ شروع باشد" }
        val now = PersianDateFormatter.nowMillis()
        val existing = if (input.id == 0L) null else dao.getProject(input.id)
        return dao.insertProject(input.toEntity(existing?.createdAt ?: now, now))
    }

    override suspend fun archiveProject(projectId: Long): Result<Unit> =
        runCatching {
            val project = dao.getProject(projectId) ?: error("پروژه پیدا نشد")
            dao.updateProject(project.copy(status = ProjectStatus.ARCHIVED, updatedAt = PersianDateFormatter.nowMillis()))
        }

    override suspend fun saveMealDelivery(input: MealDeliveryInput): Result<Long> =
        runCatching {
            require(input.quantity > 0) { "تعداد وعده باید بیشتر از صفر باشد" }
            require(input.unitPrice > 0) { "قیمت واحد باید بیشتر از صفر باشد" }
            require(input.returnedQuantity in 0..input.quantity) { "تعداد برگشتی باید بین صفر و تعداد ارسالی باشد" }
            require(input.status != DeliveryStatus.RETURNED || input.returnedQuantity == input.quantity) {
                "برای وضعیت برگشت کامل، تعداد برگشتی باید برابر تعداد ارسالی باشد"
            }
            require(input.deliveryTimeMinutes == null || input.deliveryTimeMinutes in 0..1439) { "زمان تحویل نامعتبر است" }
            val snapshot = currentSnapshot()
            val project = snapshot.projects.firstOrNull { it.id == input.projectId } ?: error("پروژه پیدا نشد")
            val existing = snapshot.mealDeliveries.firstOrNull { it.id == input.id }
            require(project.status == ProjectStatus.ACTIVE || existing?.projectId == input.projectId) {
                "تحویل غذا فقط برای پروژه فعال مجاز است"
            }
            require(snapshot.mealDeliveries.none { it.id != input.id && it.projectId == input.projectId && it.date == input.date && it.mealType == input.mealType }) {
                "برای این پروژه، تاریخ و نوع غذا قبلاً ثبت شده است؛ همان رکورد را ویرایش کنید"
            }
            val now = PersianDateFormatter.nowMillis()
            val billableQuantity = if (input.status == DeliveryStatus.DELIVERED) input.quantity - input.returnedQuantity else 0
            val candidate = MealDeliveryEntity(
                    id = input.id,
                    projectId = input.projectId,
                    date = input.date,
                    deliveryTimeMinutes = input.deliveryTimeMinutes,
                    mealType = input.mealType,
                    status = input.status,
                    quantity = input.quantity,
                    returnedQuantity = input.returnedQuantity,
                    unitPrice = input.unitPrice,
                    totalAmount = Math.multiplyExact(billableQuantity.toLong(), input.unitPrice),
                    recipientName = input.recipientName?.trim()?.ifBlank { null },
                    recipientPhone = input.recipientPhone?.trim()?.ifBlank { null },
                    notes = input.notes?.ifBlank { null },
                    createdAt = existing?.createdAt ?: now,
                    updatedAt = now
                )
            val prospectiveDeliveries = snapshot.mealDeliveries.filterNot { it.id == input.id } + candidate
            setOfNotNull(existing?.projectId, input.projectId).forEach { affectedProjectId ->
                val earned = prospectiveDeliveries
                    .filter { it.projectId == affectedProjectId && it.status == DeliveryStatus.DELIVERED }
                    .sumOf { it.totalAmount }
                val received = snapshot.projectPayments.filter { it.projectId == affectedProjectId }.sumOf { it.amount }
                require(received <= earned) {
                    "با این تغییر، دریافتی پروژه از مبلغ تحویل‌های نهایی بیشتر می‌شود"
                }
            }
            dao.insertMealDelivery(candidate)
        }

    override suspend fun saveCookingBatch(input: CookingBatchInput): Result<Long> = runCatching {
        require(input.producedQuantity > 0) { "تعداد غذای پخته‌شده باید بیشتر از صفر باشد" }
        require(input.materials.isNotEmpty()) { "حداقل یک ماده مصرفی وارد کنید" }
        require(input.materials.all { it.quantity.isFinite() && it.quantity > 0.0 }) { "مقدار مواد مصرفی نامعتبر است" }
        require(input.materials.map { it.materialId }.distinct().size == input.materials.size) { "هر ماده فقط یک بار قابل ثبت است" }
        require(input.allocations.isNotEmpty()) { "حداقل یک شرکت مصرف‌کننده انتخاب کنید" }
        require(input.allocations.all { it.quantity > 0 }) { "تعداد تخصیص شرکت‌ها باید مثبت باشد" }
        require(input.allocations.map { it.projectId }.distinct().size == input.allocations.size) { "هر شرکت فقط یک بار قابل تخصیص است" }
        require(input.allocations.sumOf { it.quantity } == input.producedQuantity) {
            "جمع غذای تخصیص‌یافته باید با تعداد غذای پخته‌شده برابر باشد"
        }
        val snapshot = currentSnapshot()
        val existing = snapshot.cookingBatches.firstOrNull { it.id == input.id }
        val existingMaterialIds = snapshot.stockTransactions.filter { it.cookingBatchId == input.id }.map { it.materialId }.toSet()
        val existingProjectIds = snapshot.cookingAllocations.filter { it.batchId == input.id }.map { it.projectId }.toSet()
        require(input.id == 0L || existing != null) { "ثبت پخت برای ویرایش پیدا نشد" }
        require(snapshot.warehouses.any { it.id == input.warehouseId && (it.isActive || it.id == existing?.warehouseId) }) { "انبار مصرف معتبر نیست" }
        require(input.materials.all { item -> snapshot.materials.any { it.id == item.materialId && (it.isActive || it.id in existingMaterialIds) } }) { "ماده مصرفی معتبر نیست" }
        require(input.allocations.all { allocation -> snapshot.projects.any { it.id == allocation.projectId && (it.status == ProjectStatus.ACTIVE || it.id in existingProjectIds) } }) { "شرکت تخصیص‌یافته فعال نیست" }
        val now = PersianDateFormatter.nowMillis()
        val replacementTransactions = input.materials.map { item ->
            val material = snapshot.materials.first { it.id == item.materialId }
            StockTransactionEntity(
                warehouseId = input.warehouseId,
                materialId = item.materialId,
                cookingBatchId = input.id.takeIf { it != 0L },
                type = StockTransactionType.OUT,
                reason = StockReason.COOKING_USAGE,
                quantity = item.quantity,
                unit = material.mainUnit,
                date = input.date,
                notes = "مصرف پخت ${input.mealType.name}",
                createdAt = existing?.createdAt ?: now,
                updatedAt = now
            )
        }
        InventoryIntegrityValidator.validateNonNegative(
            snapshot,
            snapshot.stockTransactions.filterNot { it.cookingBatchId == input.id && input.id != 0L } + replacementTransactions
        )
        database.withTransaction {
            if (input.id != 0L) {
                dao.deleteStockTransactionsForCookingBatch(input.id)
                dao.deleteCookingAllocationsForBatch(input.id)
            }
            val batchId = dao.insertCookingBatch(
                CookingBatchEntity(
                    id = input.id,
                    warehouseId = input.warehouseId,
                    date = input.date,
                    mealType = input.mealType,
                    producedQuantity = input.producedQuantity,
                    notes = input.notes?.trim()?.ifBlank { null },
                    createdAt = existing?.createdAt ?: now,
                    updatedAt = now
                )
            )
            replacementTransactions.forEach { dao.insertStockTransaction(it.copy(cookingBatchId = batchId)) }
            input.allocations.forEach { allocation ->
                dao.insertCookingAllocation(
                    CookingAllocationEntity(
                        batchId = batchId,
                        projectId = allocation.projectId,
                        quantity = allocation.quantity,
                        createdAt = existing?.createdAt ?: now,
                        updatedAt = now
                    )
                )
            }
            batchId
        }
    }

    override suspend fun saveWarehouse(entity: WarehouseEntity): Long {
        require(entity.name.isNotBlank()) { "نام انبار الزامی است" }
        val now = PersianDateFormatter.nowMillis()
        return dao.insertWarehouse(
            entity.copy(
                name = entity.name.trim(),
                address = entity.address?.trim()?.ifBlank { null },
                notes = entity.notes?.trim()?.ifBlank { null },
                createdAt = entity.createdAt.takeIf { it > 0 } ?: now,
                updatedAt = now
            )
        )
    }

    override suspend fun saveMaterial(entity: MaterialEntity): Long {
        require(entity.name.isNotBlank()) { "نام متریال الزامی است" }
        require(entity.minimumStock.isFinite() && entity.minimumStock >= 0.0) { "حداقل موجودی نامعتبر است" }
        val now = PersianDateFormatter.nowMillis()
        return dao.insertMaterial(
            entity.copy(
                name = entity.name.trim(),
                notes = entity.notes?.trim()?.ifBlank { null },
                imageEmoji = entity.imageEmoji?.trim()?.ifBlank { null },
                createdAt = entity.createdAt.takeIf { it > 0 } ?: now,
                updatedAt = now
            )
        )
    }

    override suspend fun saveSupplier(entity: SupplierEntity): Long {
        require(entity.name.isNotBlank()) { "نام تامین‌کننده الزامی است" }
        val now = PersianDateFormatter.nowMillis()
        return dao.insertSupplier(
            entity.copy(
                name = entity.name.trim(),
                phone = entity.phone?.trim()?.ifBlank { null },
                address = entity.address?.trim()?.ifBlank { null },
                notes = entity.notes?.trim()?.ifBlank { null },
                createdAt = entity.createdAt.takeIf { it > 0 } ?: now,
                updatedAt = now
            )
        )
    }

    override suspend fun saveStockTransaction(input: StockTransactionInput): Result<Long> =
        runCatching { insertStockTransaction(input) }

    override suspend fun saveStockTransactions(inputs: List<StockTransactionInput>): Result<List<Long>> =
        runCatching {
            require(inputs.isNotEmpty()) { "حداقل یک تراکنش انبار باید ثبت شود" }
            database.withTransaction {
                inputs.map { insertStockTransaction(it) }
            }
        }

    private suspend fun insertStockTransaction(input: StockTransactionInput): Long {
        require(input.quantity.isFinite() && input.quantity != 0.0) { "مقدار کالا نامعتبر است" }
        require(input.type == StockTransactionType.ADJUSTMENT || input.quantity > 0.0) {
            "مقدار منفی فقط برای اصلاح موجودی مجاز است"
        }
        val snapshot = currentSnapshot()
        require(snapshot.warehouses.any { it.id == input.warehouseId && it.isActive }) {
            "انبار انتخاب‌شده معتبر نیست"
        }
        val material = snapshot.materials.firstOrNull { it.id == input.materialId && it.isActive }
            ?: error("متریال انتخاب‌شده معتبر نیست")
        require(input.unit == material.mainUnit) { "واحد تراکنش با واحد اصلی متریال سازگار نیست" }
        input.projectId?.let { projectId ->
            require(snapshot.projects.any { it.id == projectId }) { "پروژه انتخاب‌شده معتبر نیست" }
        }
        input.supplierId?.let { supplierId ->
            require(snapshot.suppliers.any { it.id == supplierId }) { "تامین‌کننده انتخاب‌شده معتبر نیست" }
        }
        val availableAtTransactionDate = snapshot.stockTransactions
            .filter {
                it.warehouseId == input.warehouseId &&
                    it.materialId == input.materialId &&
                    it.date <= input.date
            }
            .sumOf(InventoryIntegrityValidator::signedQuantity)
        val outgoing = input.type == StockTransactionType.OUT ||
            input.type == StockTransactionType.TRANSFER_OUT ||
            input.type == StockTransactionType.WASTE
        require(!outgoing || input.quantity <= availableAtTransactionDate) {
            "در تاریخ انتخاب‌شده موجودی کافی نیست. موجودی آن تاریخ: $availableAtTransactionDate"
        }
        require(input.type != StockTransactionType.ADJUSTMENT || availableAtTransactionDate + input.quantity >= 0.0) {
            "اصلاح موجودی نمی‌تواند موجودی نهایی را منفی کند"
        }
        val now = PersianDateFormatter.nowMillis()
        val candidate = StockTransactionEntity(
                warehouseId = input.warehouseId,
                materialId = input.materialId,
                projectId = input.projectId,
                supplierId = input.supplierId,
                type = input.type,
                reason = input.reason,
                quantity = input.quantity,
                unit = input.unit,
                unitPrice = input.unitPrice,
                totalAmount = input.unitPrice?.let { (it * input.quantity).toLong() },
                date = input.date,
                notes = input.notes?.ifBlank { null },
                createdAt = now,
                updatedAt = now
            )
        InventoryIntegrityValidator.validateNonNegative(snapshot, snapshot.stockTransactions + candidate)
        return dao.insertStockTransaction(candidate)
    }

    override suspend fun savePurchase(input: PurchaseInput): Result<Long> =
        runCatching {
            require(input.items.isNotEmpty()) { "فاکتور باید حداقل یک آیتم داشته باشد" }
            require(input.discountAmount >= 0) { "تخفیف نمی‌تواند منفی باشد" }
            require(input.items.all { it.quantity.isFinite() && it.quantity > 0 && it.unitPrice > 0 }) { "مقدار و قیمت همه آیتم‌ها باید معتبر و مثبت باشد" }
            val subtotal = input.items.sumOf { it.totalAmount }
            require(input.discountAmount <= subtotal) { "تخفیف نمی‌تواند بیشتر از جمع آیتم‌ها باشد" }
            val total = subtotal - input.discountAmount
            val snapshot = currentSnapshot()
            val existing = snapshot.purchases.firstOrNull { it.id == input.id }
            val existingItemMaterialIds = if (existing == null) {
                emptySet()
            } else {
                snapshot.purchaseItems.filter { it.purchaseId == existing.id }.map { it.materialId }.toSet()
            }
            val existingStockCreatedAt = existing?.let { purchase ->
                snapshot.stockTransactions.filter { it.purchaseId == purchase.id }.minOfOrNull { it.createdAt }
            }
            require(input.id == 0L || existing != null) { "فاکتور خرید برای ویرایش پیدا نشد" }
            val linkedPayments = snapshot.supplierPayments.filter { it.purchaseId == input.id && input.id != 0L }
            if (linkedPayments.isNotEmpty()) {
                require(input.paymentType == PurchasePaymentType.CREDIT && input.supplierId == existing?.supplierId) {
                    "فاکتور دارای پرداخت متصل است؛ نوع پرداخت یا تامین‌کننده آن قابل تغییر نیست"
                }
                require(linkedPayments.sumOf { it.amount } <= total) {
                    "مبلغ جدید فاکتور از پرداخت‌های ثبت‌شده برای آن کمتر است"
                }
            }
            require(snapshot.warehouses.any { it.id == input.warehouseId && (it.isActive || it.id == existing?.warehouseId) }) {
                "انبار مقصد معتبر نیست"
            }
            input.supplierId?.let { supplierId ->
                require(snapshot.suppliers.any { it.id == supplierId && (it.isActive || it.id == existing?.supplierId) }) { "تامین‌کننده معتبر نیست" }
            }
            require(input.paymentType != PurchasePaymentType.CREDIT || input.supplierId != null) {
                "برای خرید نسیه باید تامین‌کننده انتخاب شود"
            }
            val normalizedCardId = if (input.paymentType == PurchasePaymentType.CARD) input.bankCardId else null
            require(input.paymentType != PurchasePaymentType.CARD || normalizedCardId != null) {
                "برای خرید کارتی باید کارت بانکی انتخاب شود"
            }
            normalizedCardId?.let { cardId ->
                require(snapshot.bankCards.any { it.id == cardId && (it.isActive || it.id == existing?.bankCardId) }) { "کارت بانکی معتبر نیست" }
            }
            require(input.items.all { item -> snapshot.materials.any { it.id == item.materialId && (it.isActive || it.id in existingItemMaterialIds) && it.mainUnit == item.unit } }) {
                "همه آیتم‌های فاکتور باید متریال فعال و واحد معتبر داشته باشند"
            }
            val normalizedInvoice = input.invoiceNumber?.trim()?.ifBlank { null }
            require(normalizedInvoice == null || snapshot.purchases.none { it.id != input.id && it.supplierId == input.supplierId && it.invoiceNumber == normalizedInvoice }) {
                "شماره فاکتور برای این تامین‌کننده قبلا ثبت شده است"
            }
            setOfNotNull(existing?.supplierId, input.supplierId).forEach { affectedSupplierId ->
                val prospectiveCredit = snapshot.purchases
                    .filter { it.id != input.id && it.supplierId == affectedSupplierId && it.paymentType == PurchasePaymentType.CREDIT }
                    .sumOf { it.totalAmount } + if (input.supplierId == affectedSupplierId && input.paymentType == PurchasePaymentType.CREDIT) total else 0L
                val supplierPaid = snapshot.supplierPayments.filter { it.supplierId == affectedSupplierId }.sumOf { it.amount }
                require(supplierPaid <= prospectiveCredit) {
                    "با این تغییر، پرداخت تامین‌کننده از بدهی نسیه بیشتر می‌شود"
                }
            }
            if (input.id != 0L) {
                InventoryIntegrityValidator.validateNonNegative(
                    snapshot = snapshot,
                    transactions = stockTransactionsReplacingPurchase(snapshot, input.id, input.warehouseId, input.date, input.items)
                )
            }
            database.withTransaction {
                val now = PersianDateFormatter.nowMillis()
                val paid = if (input.paymentType == PurchasePaymentType.CREDIT) 0 else total
                if (input.id != 0L) {
                    dao.deleteStockTransactionsForPurchase(input.id)
                    dao.deletePurchaseItemsForPurchase(input.id)
                }
                val purchaseId = dao.insertPurchase(
                    PurchaseEntity(
                        id = input.id,
                        supplierId = input.supplierId,
                        warehouseId = input.warehouseId,
                        date = input.date,
                        invoiceNumber = normalizedInvoice,
                        paymentType = input.paymentType,
                        bankCardId = normalizedCardId,
                        discountAmount = input.discountAmount,
                        totalAmount = total,
                        paidAmount = paid,
                        notes = input.notes?.ifBlank { null },
                        createdAt = existing?.createdAt ?: now,
                        updatedAt = now
                    )
                )
                input.items.forEach { item ->
                    val itemId = dao.insertPurchaseItem(
                        PurchaseItemEntity(
                            purchaseId = purchaseId,
                            materialId = item.materialId,
                            quantity = item.quantity,
                            unit = item.unit,
                            unitPrice = item.unitPrice,
                            totalAmount = item.totalAmount,
                            createdAt = existingStockCreatedAt ?: now,
                            updatedAt = now
                        )
                    )
                    dao.insertStockTransaction(
                        StockTransactionEntity(
                            warehouseId = input.warehouseId,
                            materialId = item.materialId,
                            supplierId = input.supplierId,
                            purchaseId = purchaseId,
                            type = StockTransactionType.IN,
                            reason = StockReason.PURCHASE,
                            quantity = item.quantity,
                            unit = item.unit,
                            unitPrice = item.unitPrice,
                            totalAmount = item.totalAmount,
                            date = input.date,
                            notes = "ورود خودکار از آیتم خرید #$itemId",
                            createdAt = existingStockCreatedAt ?: now,
                            updatedAt = now
                        )
                    )
                }
                purchaseId
            }
        }

    override suspend fun saveBankCard(entity: BankCardEntity): Long {
        require(entity.title.isNotBlank()) { "عنوان کارت الزامی است" }
        require(entity.initialBalance >= 0) { "موجودی اولیه نمی‌تواند منفی باشد" }
        val now = PersianDateFormatter.nowMillis()
        return dao.insertBankCard(
            entity.copy(
                title = entity.title.trim(),
                ownerName = entity.ownerName?.trim()?.ifBlank { null },
                bankName = entity.bankName?.trim()?.ifBlank { null },
                cardNumber = entity.cardNumber?.let(::normalizeStoredCardNumber),
                notes = entity.notes?.trim()?.ifBlank { null },
                createdAt = entity.createdAt.takeIf { it > 0 } ?: now,
                updatedAt = now
            )
        )
    }

    override suspend fun saveProjectPayment(input: ProjectPaymentInput): Result<Long> =
        runCatching {
            require(input.amount > 0) { "مبلغ پرداخت باید بیشتر از صفر باشد" }
            val snapshot = currentSnapshot()
            val existing = snapshot.projectPayments.firstOrNull { it.id == input.id }
            require(input.id == 0L || existing != null) { "دریافت پروژه برای ویرایش پیدا نشد" }
            require(snapshot.projects.any { it.id == input.projectId }) { "پروژه معتبر نیست" }
            val normalizedCardId = if (input.method == PaymentMethod.CASH) null else input.bankCardId
            require(input.method == PaymentMethod.CASH || normalizedCardId != null) {
                "برای دریافت غیرنقدی باید کارت بانکی انتخاب شود"
            }
            normalizedCardId?.let { cardId ->
                require(snapshot.bankCards.any { it.id == cardId && (it.isActive || it.id == existing?.bankCardId) }) { "کارت بانکی معتبر نیست" }
            }
            val finance = ProjectFinanceUseCase(this).calculateProjectFinances(snapshot)
                .firstOrNull { it.project.id == input.projectId }
            val editableAllowance = finance?.receivable.orZero() + if (existing?.projectId == input.projectId) existing.amount else 0L
            require(finance != null && input.amount <= editableAllowance) {
                "مبلغ دریافت بیشتر از مانده مطالبات است"
            }
            val now = PersianDateFormatter.nowMillis()
            dao.insertProjectPayment(
                ProjectPaymentEntity(
                    id = input.id,
                    projectId = input.projectId,
                    bankCardId = normalizedCardId,
                    amount = input.amount,
                    date = input.date,
                    method = input.method,
                    notes = input.notes?.ifBlank { null },
                    createdAt = existing?.createdAt ?: now,
                    updatedAt = now
                )
            )
        }

    override suspend fun saveSupplierPayment(input: SupplierPaymentInput): Result<Long> =
        runCatching {
            require(input.amount > 0) { "مبلغ پرداخت باید بیشتر از صفر باشد" }
            val snapshot = currentSnapshot()
            val existing = snapshot.supplierPayments.firstOrNull { it.id == input.id }
            require(input.id == 0L || existing != null) { "پرداخت تامین‌کننده برای ویرایش پیدا نشد" }
            require(snapshot.suppliers.any { it.id == input.supplierId && (it.isActive || it.id == existing?.supplierId) }) { "تامین‌کننده معتبر نیست" }
            val linkedPurchase = input.purchaseId?.let { purchaseId ->
                snapshot.purchases.firstOrNull { it.id == purchaseId }
                    ?: error("فاکتور خرید انتخاب‌شده پیدا نشد")
            }
            require(linkedPurchase == null || linkedPurchase.supplierId == input.supplierId) {
                "فاکتور انتخاب‌شده متعلق به این تامین‌کننده نیست"
            }
            val normalizedCardId = if (input.method == PaymentMethod.CASH) null else input.bankCardId
            require(input.method == PaymentMethod.CASH || normalizedCardId != null) {
                "برای پرداخت غیرنقدی باید کارت بانکی انتخاب شود"
            }
            normalizedCardId?.let { cardId ->
                require(snapshot.bankCards.any { it.id == cardId && (it.isActive || it.id == existing?.bankCardId) }) { "کارت بانکی معتبر نیست" }
            }
            val debt = SupplierDebtUseCase(this).calculateSupplierDebts(snapshot)
                .firstOrNull { it.supplier.id == input.supplierId }
            val editableAllowance = debt?.remaining.orZero() + if (existing?.supplierId == input.supplierId) existing.amount else 0L
            require(debt != null && input.amount <= editableAllowance) {
                "مبلغ پرداخت بیشتر از بدهی تامین‌کننده است"
            }
            if (linkedPurchase != null) {
                val allocated = snapshot.supplierPayments
                    .filter { it.purchaseId == linkedPurchase.id && it.id != input.id }
                    .sumOf { it.amount }
                val invoiceRemaining = linkedPurchase.totalAmount - linkedPurchase.paidAmount - allocated
                require(input.amount <= invoiceRemaining) { "مبلغ پرداخت بیشتر از مانده این فاکتور است" }
            }
            val now = PersianDateFormatter.nowMillis()
            dao.insertSupplierPayment(
                SupplierPaymentEntity(
                    id = input.id,
                    supplierId = input.supplierId,
                    bankCardId = normalizedCardId,
                    purchaseId = input.purchaseId,
                    amount = input.amount,
                    date = input.date,
                    method = input.method,
                    notes = input.notes?.ifBlank { null },
                    createdAt = existing?.createdAt ?: now,
                    updatedAt = now
                )
            )
        }

    override suspend fun saveExpense(entity: ExpenseEntity): Long {
        require(entity.title.isNotBlank()) { "عنوان هزینه الزامی است" }
        require(entity.amount > 0) { "مبلغ هزینه باید بیشتر از صفر باشد" }
        val snapshot = currentSnapshot()
        val existing = snapshot.expenses.firstOrNull { it.id == entity.id }
        require(entity.id == 0L || existing != null) { "هزینه برای ویرایش پیدا نشد" }
        entity.bankCardId?.let { cardId ->
            require(snapshot.bankCards.any { it.id == cardId && (it.isActive || it.id == existing?.bankCardId) }) { "کارت بانکی معتبر نیست" }
        }
        entity.projectId?.let { projectId -> require(snapshot.projects.any { it.id == projectId }) { "شرکت هزینه معتبر نیست" } }
        entity.cookingBatchId?.let { batchId -> require(snapshot.cookingBatches.any { it.id == batchId }) { "نوبت پخت هزینه معتبر نیست" } }
        require(entity.projectId == null || entity.cookingBatchId == null) { "هزینه را فقط به شرکت یا یک نوبت پخت متصل کنید" }
        val now = PersianDateFormatter.nowMillis()
        return dao.insertExpense(
            entity.copy(
                title = entity.title.trim(),
                notes = entity.notes?.trim()?.ifBlank { null },
                createdAt = entity.createdAt.takeIf { it > 0 } ?: now,
                updatedAt = now
            )
        )
    }

    override suspend fun deleteMealDelivery(id: Long): Result<Unit> =
        runCatching {
            val snapshot = currentSnapshot()
            val delivery = snapshot.mealDeliveries.firstOrNull { it.id == id } ?: error("تحویل غذا پیدا نشد")
            val remainingRevenue = snapshot.mealDeliveries
                .filter { it.id != id && it.projectId == delivery.projectId && it.status == DeliveryStatus.DELIVERED }
                .sumOf { it.totalAmount }
            val received = snapshot.projectPayments.filter { it.projectId == delivery.projectId }.sumOf { it.amount }
            require(received <= remainingRevenue) {
                "این تحویل دارای اثر مالی وصول‌شده است؛ ابتدا دریافت‌های پروژه را اصلاح کنید"
            }
            dao.deleteMealDelivery(id)
        }

    override suspend fun deleteCookingBatch(id: Long): Result<Unit> = runCatching {
        val snapshot = currentSnapshot()
        require(snapshot.cookingBatches.any { it.id == id }) { "ثبت پخت پیدا نشد" }
        require(snapshot.expenses.none { it.cookingBatchId == id }) { "این پخت هزینه متصل دارد؛ ابتدا هزینه مرتبط را اصلاح یا حذف کنید" }
        InventoryIntegrityValidator.validateNonNegative(
            snapshot,
            snapshot.stockTransactions.filterNot { it.cookingBatchId == id }
        )
        database.withTransaction {
            dao.deleteStockTransactionsForCookingBatch(id)
            dao.deleteCookingAllocationsForBatch(id)
            dao.deleteCookingBatch(id)
        }
    }

    override suspend fun deleteWarehouse(id: Long): Result<Unit> =
        runCatching {
            val snapshot = currentSnapshot()
            val warehouse = snapshot.warehouses.firstOrNull { it.id == id }
                ?: error("انبار پیدا نشد")
            val isInUse = snapshot.stockTransactions.any { it.warehouseId == id } ||
                snapshot.purchases.any { it.warehouseId == id }
            if (isInUse) {
                dao.insertWarehouse(warehouse.copy(isActive = false, updatedAt = PersianDateFormatter.nowMillis()))
            } else {
                dao.deleteWarehouse(id)
            }
        }

    override suspend fun deleteMaterial(id: Long): Result<Unit> =
        runCatching {
            val snapshot = currentSnapshot()
            val material = snapshot.materials.firstOrNull { it.id == id }
                ?: error("متریال پیدا نشد")
            val isInUse = snapshot.stockTransactions.any { it.materialId == id } ||
                snapshot.purchaseItems.any { it.materialId == id }
            if (isInUse) {
                dao.insertMaterial(material.copy(isActive = false, updatedAt = PersianDateFormatter.nowMillis()))
            } else {
                dao.deleteMaterial(id)
            }
        }

    override suspend fun deleteSupplier(id: Long): Result<Unit> =
        runCatching {
            val snapshot = currentSnapshot()
            val supplier = snapshot.suppliers.firstOrNull { it.id == id }
                ?: error("تامین‌کننده پیدا نشد")
            val isInUse = snapshot.purchases.any { it.supplierId == id } ||
                snapshot.supplierPayments.any { it.supplierId == id } ||
                snapshot.stockTransactions.any { it.supplierId == id }
            if (isInUse) {
                dao.insertSupplier(supplier.copy(isActive = false, updatedAt = PersianDateFormatter.nowMillis()))
            } else {
                dao.deleteSupplier(id)
            }
        }

    override suspend fun deleteBankCard(id: Long): Result<Unit> =
        runCatching {
            val snapshot = currentSnapshot()
            val card = snapshot.bankCards.firstOrNull { it.id == id }
                ?: error("کارت بانکی پیدا نشد")
            val isInUse = snapshot.purchases.any { it.bankCardId == id } ||
                snapshot.projectPayments.any { it.bankCardId == id } ||
                snapshot.supplierPayments.any { it.bankCardId == id } ||
                snapshot.expenses.any { it.bankCardId == id }
            if (isInUse) {
                dao.insertBankCard(card.copy(isActive = false, updatedAt = PersianDateFormatter.nowMillis()))
            } else {
                dao.deleteBankCard(id)
            }
        }

    override suspend fun deleteStockTransaction(id: Long): Result<Unit> =
        runCatching {
            val snapshot = currentSnapshot()
            val tx = snapshot.stockTransactions.firstOrNull { it.id == id }
                ?: dao.getStockTransaction(id)
                ?: error("تراکنش انبار پیدا نشد")
            require(tx.purchaseId == null) { "تراکنش‌های خودکار خرید را از خود فاکتور خرید اصلاح یا حذف کنید" }
            require(tx.cookingBatchId == null) { "تراکنش مصرف پخت را از بخش مصرف و پخت اصلاح یا حذف کنید" }
            InventoryIntegrityValidator.validateNonNegative(
                snapshot = snapshot,
                transactions = snapshot.stockTransactions.filterNot { it.id == id }
            )
            dao.deleteStockTransaction(id)
        }

    override suspend fun deletePurchase(id: Long): Result<Unit> =
        runCatching {
            val purchase = dao.getPurchase(id) ?: error("فاکتور خرید پیدا نشد")
            val snapshot = currentSnapshot()
            require(snapshot.supplierPayments.none { it.purchaseId == id }) {
                "برای این فاکتور پرداخت ثبت شده است؛ ابتدا پرداخت متصل را حذف یا اصلاح کنید"
            }
            purchase.supplierId?.let { supplierId ->
                val remainingCredit = snapshot.purchases
                    .filter { it.id != id && it.supplierId == supplierId && it.paymentType == PurchasePaymentType.CREDIT }
                    .sumOf { it.totalAmount }
                val supplierPaid = snapshot.supplierPayments.filter { it.supplierId == supplierId }.sumOf { it.amount }
                require(supplierPaid <= remainingCredit) {
                    "حذف این فاکتور، پرداخت تامین‌کننده را بیشتر از بدهی باقی‌مانده می‌کند"
                }
            }
            InventoryIntegrityValidator.validateNonNegative(
                snapshot = snapshot,
                transactions = snapshot.stockTransactions.filterNot { it.purchaseId == id }
            )
            database.withTransaction {
                dao.deleteStockTransactionsForPurchase(id)
                dao.deletePurchaseItemsForPurchase(id)
                dao.deletePurchase(id)
            }
        }

    override suspend fun deleteProjectPayment(id: Long): Result<Unit> =
        runCatching {
            dao.deleteProjectPayment(id)
        }

    override suspend fun deleteSupplierPayment(id: Long): Result<Unit> =
        runCatching {
            dao.deleteSupplierPayment(id)
        }

    override suspend fun deleteExpense(id: Long): Result<Unit> =
        runCatching {
            dao.deleteExpense(id)
        }

    override suspend fun exportBackup(context: Context): File {
        val backup = BackupJson.encode(currentSnapshot())
        val dir = File(context.getExternalFilesDir(null), "backups").apply { mkdirs() }
        val file = File(dir, "restaurant-backup-${System.currentTimeMillis()}.json")
        file.writeText(backup, Charsets.UTF_8)
        return file
    }

    override suspend fun restoreBackup(context: Context, uri: Uri): Result<Unit> =
        runCatching {
            val json = context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
                ?: error("فایل پشتیبان قابل خواندن نیست")
            val snapshot = BackupJson.decode(json)
            BackupJson.validate(snapshot)
            exportBackup(context)
            database.withTransaction {
                clearAll()
                dao.insertWarehouses(snapshot.warehouses)
                dao.insertMaterials(snapshot.materials)
                dao.insertSuppliers(snapshot.suppliers)
                dao.insertBankCards(snapshot.bankCards)
                dao.insertProjects(snapshot.projects)
                dao.insertPurchases(snapshot.purchases)
                dao.insertPurchaseItems(snapshot.purchaseItems)
                dao.insertStockTransactions(snapshot.stockTransactions)
                dao.insertMealDeliveries(snapshot.mealDeliveries)
                dao.insertProjectPayments(snapshot.projectPayments)
                dao.insertSupplierPayments(snapshot.supplierPayments)
                dao.insertExpenses(snapshot.expenses)
                dao.insertCookingBatches(snapshot.cookingBatches)
                dao.insertCookingAllocations(snapshot.cookingAllocations)
            }
        }

    override suspend fun exportCsv(context: Context, fileName: String, csv: String): File {
        val dir = File(context.getExternalFilesDir(null), "reports").apply { mkdirs() }
        val file = File(dir, fileName)
        file.writeText(csv, Charsets.UTF_8)
        return file
    }

    private suspend fun clearAll() {
        dao.clearCookingAllocations()
        dao.clearCookingBatches()
        dao.clearExpenses()
        dao.clearSupplierPayments()
        dao.clearProjectPayments()
        dao.clearStockTransactions()
        dao.clearPurchaseItems()
        dao.clearPurchases()
        dao.clearProjects()
        dao.clearBankCards()
        dao.clearSuppliers()
        dao.clearMaterials()
        dao.clearWarehouses()
        dao.clearMealDeliveries()
    }
}

private fun Long?.orZero(): Long = this ?: 0L

private fun normalizeStoredCardNumber(value: String): String? {
    val digits = value.filter { it.isDigit() }
    return digits.ifBlank { null }
}

private fun stockTransactionsReplacingPurchase(
    snapshot: RestaurantSnapshot,
    purchaseId: Long,
    warehouseId: Long,
    purchaseDate: Long,
    items: List<PurchaseItemInput>
): List<StockTransactionEntity> {
    val originalCreatedAt = snapshot.stockTransactions
        .filter { it.purchaseId == purchaseId }
        .minOfOrNull { it.createdAt }
        ?: PersianDateFormatter.nowMillis()
    val replacementTransactions = items.map { item ->
        StockTransactionEntity(
            warehouseId = warehouseId,
            materialId = item.materialId,
            purchaseId = purchaseId,
            type = StockTransactionType.IN,
            reason = StockReason.PURCHASE,
            quantity = item.quantity,
            unit = item.unit,
            date = purchaseDate,
            createdAt = originalCreatedAt,
            updatedAt = PersianDateFormatter.nowMillis()
        )
    }
    return snapshot.stockTransactions.filterNot { it.purchaseId == purchaseId } + replacementTransactions
}

private object BackupJson {
    fun encode(snapshot: RestaurantSnapshot): String {
        val root = JSONObject()
        root.put("version", 5)
        root.put("exportedAt", System.currentTimeMillis())
        root.put("projects", snapshot.projects.toJsonArray {
            JSONObject()
                .put("id", it.id)
                .put("name", it.name)
                .putNullable("companyName", it.companyName)
                .putNullable("address", it.address)
                .putNullable("managerName", it.managerName)
                .putNullable("phone", it.phone)
                .put("workerCount", it.workerCount)
                .put("mealPrice", it.mealPrice)
                .put("breakfastPrice", it.breakfastPrice)
                .put("lunchPrice", it.lunchPrice)
                .put("dinnerPrice", it.dinnerPrice)
                .put("defaultMealType", it.defaultMealType)
                .put("startDate", it.startDate)
                .putNullable("endDate", it.endDate)
                .put("status", it.status.name)
                .putNullable("notes", it.notes)
                .put("createdAt", it.createdAt)
                .put("updatedAt", it.updatedAt)
        })
        root.put("mealDeliveries", snapshot.mealDeliveries.toJsonArray {
            JSONObject()
                .put("id", it.id)
                .put("projectId", it.projectId)
                .put("date", it.date)
                .putNullable("deliveryTimeMinutes", it.deliveryTimeMinutes)
                .put("mealType", it.mealType.name)
                .put("status", it.status.name)
                .put("quantity", it.quantity)
                .put("returnedQuantity", it.returnedQuantity)
                .put("unitPrice", it.unitPrice)
                .put("totalAmount", it.totalAmount)
                .putNullable("recipientName", it.recipientName)
                .putNullable("recipientPhone", it.recipientPhone)
                .putNullable("notes", it.notes)
                .put("createdAt", it.createdAt)
                .put("updatedAt", it.updatedAt)
        })
        root.put("warehouses", snapshot.warehouses.toJsonArray {
            JSONObject()
                .put("id", it.id)
                .put("name", it.name)
                .put("type", it.type.name)
                .putNullable("address", it.address)
                .putNullable("notes", it.notes)
                .put("isActive", it.isActive)
                .put("createdAt", it.createdAt)
                .put("updatedAt", it.updatedAt)
        })
        root.put("materials", snapshot.materials.toJsonArray {
            JSONObject()
                .put("id", it.id)
                .put("name", it.name)
                .put("mainUnit", it.mainUnit.name)
                .put("minimumStock", it.minimumStock)
                .putNullable("imageEmoji", it.imageEmoji)
                .putNullable("notes", it.notes)
                .put("isActive", it.isActive)
                .put("createdAt", it.createdAt)
                .put("updatedAt", it.updatedAt)
        })
        root.put("suppliers", snapshot.suppliers.toJsonArray {
            JSONObject()
                .put("id", it.id)
                .put("name", it.name)
                .putNullable("phone", it.phone)
                .putNullable("address", it.address)
                .putNullable("notes", it.notes)
                .put("isActive", it.isActive)
                .put("createdAt", it.createdAt)
                .put("updatedAt", it.updatedAt)
        })
        root.put("stockTransactions", snapshot.stockTransactions.toJsonArray {
            JSONObject()
                .put("id", it.id)
                .put("warehouseId", it.warehouseId)
                .put("materialId", it.materialId)
                .putNullable("projectId", it.projectId)
                .putNullable("supplierId", it.supplierId)
                .putNullable("purchaseId", it.purchaseId)
                .putNullable("cookingBatchId", it.cookingBatchId)
                .put("type", it.type.name)
                .put("reason", it.reason.name)
                .put("quantity", it.quantity)
                .put("unit", it.unit.name)
                .putNullable("unitPrice", it.unitPrice)
                .putNullable("totalAmount", it.totalAmount)
                .put("date", it.date)
                .putNullable("notes", it.notes)
                .put("createdAt", it.createdAt)
                .put("updatedAt", it.updatedAt)
        })
        root.put("purchases", snapshot.purchases.toJsonArray {
            JSONObject()
                .put("id", it.id)
                .putNullable("supplierId", it.supplierId)
                .put("warehouseId", it.warehouseId)
                .put("date", it.date)
                .putNullable("invoiceNumber", it.invoiceNumber)
                .put("paymentType", it.paymentType.name)
                .putNullable("bankCardId", it.bankCardId)
                .put("discountAmount", it.discountAmount)
                .put("totalAmount", it.totalAmount)
                .put("paidAmount", it.paidAmount)
                .putNullable("notes", it.notes)
                .put("createdAt", it.createdAt)
                .put("updatedAt", it.updatedAt)
        })
        root.put("purchaseItems", snapshot.purchaseItems.toJsonArray {
            JSONObject()
                .put("id", it.id)
                .put("purchaseId", it.purchaseId)
                .put("materialId", it.materialId)
                .put("quantity", it.quantity)
                .put("unit", it.unit.name)
                .put("unitPrice", it.unitPrice)
                .put("totalAmount", it.totalAmount)
                .put("createdAt", it.createdAt)
                .put("updatedAt", it.updatedAt)
        })
        root.put("bankCards", snapshot.bankCards.toJsonArray {
            JSONObject()
                .put("id", it.id)
                .put("title", it.title)
                .putNullable("ownerName", it.ownerName)
                .putNullable("bankName", it.bankName)
                .putNullable("cardNumber", it.cardNumber)
                .put("initialBalance", it.initialBalance)
                .put("isActive", it.isActive)
                .putNullable("notes", it.notes)
                .put("createdAt", it.createdAt)
                .put("updatedAt", it.updatedAt)
        })
        root.put("projectPayments", snapshot.projectPayments.toJsonArray {
            JSONObject()
                .put("id", it.id)
                .put("projectId", it.projectId)
                .putNullable("bankCardId", it.bankCardId)
                .put("amount", it.amount)
                .put("date", it.date)
                .put("method", it.method.name)
                .putNullable("notes", it.notes)
                .put("createdAt", it.createdAt)
                .put("updatedAt", it.updatedAt)
        })
        root.put("supplierPayments", snapshot.supplierPayments.toJsonArray {
            JSONObject()
                .put("id", it.id)
                .put("supplierId", it.supplierId)
                .putNullable("bankCardId", it.bankCardId)
                .putNullable("purchaseId", it.purchaseId)
                .put("amount", it.amount)
                .put("date", it.date)
                .put("method", it.method.name)
                .putNullable("notes", it.notes)
                .put("createdAt", it.createdAt)
                .put("updatedAt", it.updatedAt)
        })
        root.put("expenses", snapshot.expenses.toJsonArray {
            JSONObject()
                .put("id", it.id)
                .put("title", it.title)
                .put("category", it.category.name)
                .put("amount", it.amount)
                .put("date", it.date)
                .putNullable("bankCardId", it.bankCardId)
                .putNullable("projectId", it.projectId)
                .putNullable("cookingBatchId", it.cookingBatchId)
                .putNullable("notes", it.notes)
                .put("createdAt", it.createdAt)
                .put("updatedAt", it.updatedAt)
        })
        root.put("cookingBatches", snapshot.cookingBatches.toJsonArray {
            JSONObject().put("id", it.id).put("warehouseId", it.warehouseId).put("date", it.date)
                .put("mealType", it.mealType.name).put("producedQuantity", it.producedQuantity)
                .putNullable("notes", it.notes).put("createdAt", it.createdAt).put("updatedAt", it.updatedAt)
        })
        root.put("cookingAllocations", snapshot.cookingAllocations.toJsonArray {
            JSONObject().put("id", it.id).put("batchId", it.batchId).put("projectId", it.projectId)
                .put("quantity", it.quantity).put("createdAt", it.createdAt).put("updatedAt", it.updatedAt)
        })
        return root.toString(2)
    }

    fun decode(json: String): RestaurantSnapshot {
        val root = JSONObject(json)
        require(root.optInt("version", 0) in 1..5) { "نسخه فایل پشتیبان پشتیبانی نمی‌شود" }
        requiredArrays.forEach { name ->
            require(root.optJSONArray(name) != null) { "ساختار فایل پشتیبان ناقص است: $name" }
        }
        return RestaurantSnapshot(
            projects = root.array("projects").mapObjects {
                ProjectEntity(
                    id = getLong("id"),
                    name = getString("name"),
                    companyName = optStringOrNull("companyName"),
                    address = optStringOrNull("address"),
                    managerName = optStringOrNull("managerName"),
                    phone = optStringOrNull("phone"),
                    workerCount = getInt("workerCount"),
                    mealPrice = getLong("mealPrice"),
                    breakfastPrice = optLong("breakfastPrice", getLong("mealPrice")),
                    lunchPrice = optLong("lunchPrice", getLong("mealPrice")),
                    dinnerPrice = optLong("dinnerPrice", getLong("mealPrice")),
                    defaultMealType = getString("defaultMealType"),
                    startDate = getLong("startDate"),
                    endDate = optLongOrNull("endDate"),
                    status = ProjectStatus.valueOf(getString("status")),
                    notes = optStringOrNull("notes"),
                    createdAt = getLong("createdAt"),
                    updatedAt = getLong("updatedAt")
                )
            },
            mealDeliveries = root.array("mealDeliveries").mapObjects {
                MealDeliveryEntity(
                    id = getLong("id"),
                    projectId = getLong("projectId"),
                    date = getLong("date"),
                    deliveryTimeMinutes = optIntOrNull("deliveryTimeMinutes"),
                    mealType = MealType.valueOf(getString("mealType")),
                    status = optStringOrNull("status")?.let(DeliveryStatus::valueOf) ?: DeliveryStatus.DELIVERED,
                    quantity = getInt("quantity"),
                    returnedQuantity = optInt("returnedQuantity", 0),
                    unitPrice = getLong("unitPrice"),
                    totalAmount = getLong("totalAmount"),
                    recipientName = optStringOrNull("recipientName"),
                    recipientPhone = optStringOrNull("recipientPhone"),
                    notes = optStringOrNull("notes"),
                    createdAt = getLong("createdAt"),
                    updatedAt = getLong("updatedAt")
                )
            },
            warehouses = root.array("warehouses").mapObjects {
                WarehouseEntity(getLong("id"), getString("name"), WarehouseType.valueOf(getString("type")), optStringOrNull("address"), optStringOrNull("notes"), getBoolean("isActive"), getLong("createdAt"), getLong("updatedAt"))
            },
            materials = root.array("materials").mapObjects {
                MaterialEntity(getLong("id"), getString("name"), UnitType.valueOf(getString("mainUnit")), getDouble("minimumStock"), optStringOrNull("imageEmoji"), optStringOrNull("notes"), getBoolean("isActive"), getLong("createdAt"), getLong("updatedAt"))
            },
            suppliers = root.array("suppliers").mapObjects {
                SupplierEntity(getLong("id"), getString("name"), optStringOrNull("phone"), optStringOrNull("address"), optStringOrNull("notes"), getBoolean("isActive"), getLong("createdAt"), getLong("updatedAt"))
            },
            stockTransactions = root.array("stockTransactions").mapObjects {
                StockTransactionEntity(
                    id = getLong("id"), warehouseId = getLong("warehouseId"), materialId = getLong("materialId"),
                    projectId = optLongOrNull("projectId"), supplierId = optLongOrNull("supplierId"),
                    purchaseId = optLongOrNull("purchaseId"), cookingBatchId = optLongOrNull("cookingBatchId"),
                    type = StockTransactionType.valueOf(getString("type")), reason = StockReason.valueOf(getString("reason")),
                    quantity = getDouble("quantity"), unit = UnitType.valueOf(getString("unit")), unitPrice = optLongOrNull("unitPrice"),
                    totalAmount = optLongOrNull("totalAmount"), date = getLong("date"), notes = optStringOrNull("notes"),
                    createdAt = getLong("createdAt"), updatedAt = getLong("updatedAt")
                )
            },
            purchases = root.array("purchases").mapObjects {
                PurchaseEntity(getLong("id"), optLongOrNull("supplierId"), getLong("warehouseId"), getLong("date"), optStringOrNull("invoiceNumber"), PurchasePaymentType.valueOf(getString("paymentType")), optLongOrNull("bankCardId"), getLong("discountAmount"), getLong("totalAmount"), getLong("paidAmount"), optStringOrNull("notes"), getLong("createdAt"), getLong("updatedAt"))
            },
            purchaseItems = root.array("purchaseItems").mapObjects {
                PurchaseItemEntity(getLong("id"), getLong("purchaseId"), getLong("materialId"), getDouble("quantity"), UnitType.valueOf(getString("unit")), getLong("unitPrice"), getLong("totalAmount"), getLong("createdAt"), getLong("updatedAt"))
            },
            bankCards = root.array("bankCards").mapObjects {
                BankCardEntity(getLong("id"), getString("title"), optStringOrNull("ownerName"), optStringOrNull("bankName"), optStringOrNull("cardNumber"), getLong("initialBalance"), getBoolean("isActive"), optStringOrNull("notes"), getLong("createdAt"), getLong("updatedAt"))
            },
            projectPayments = root.array("projectPayments").mapObjects {
                ProjectPaymentEntity(getLong("id"), getLong("projectId"), optLongOrNull("bankCardId"), getLong("amount"), getLong("date"), PaymentMethod.valueOf(getString("method")), optStringOrNull("notes"), getLong("createdAt"), getLong("updatedAt"))
            },
            supplierPayments = root.array("supplierPayments").mapObjects {
                SupplierPaymentEntity(
                    id = getLong("id"),
                    supplierId = getLong("supplierId"),
                    bankCardId = optLongOrNull("bankCardId"),
                    purchaseId = optLongOrNull("purchaseId"),
                    amount = getLong("amount"),
                    date = getLong("date"),
                    method = PaymentMethod.valueOf(getString("method")),
                    notes = optStringOrNull("notes"),
                    createdAt = getLong("createdAt"),
                    updatedAt = getLong("updatedAt")
                )
            },
            expenses = root.array("expenses").mapObjects {
                ExpenseEntity(
                    id = getLong("id"), title = getString("title"), category = ExpenseCategory.valueOf(getString("category")),
                    amount = getLong("amount"), date = getLong("date"), bankCardId = optLongOrNull("bankCardId"),
                    projectId = optLongOrNull("projectId"), cookingBatchId = optLongOrNull("cookingBatchId"),
                    notes = optStringOrNull("notes"), createdAt = getLong("createdAt"), updatedAt = getLong("updatedAt")
                )
            },
            cookingBatches = root.optJSONArray("cookingBatches")?.mapObjects {
                CookingBatchEntity(getLong("id"), getLong("warehouseId"), getLong("date"), MealType.valueOf(getString("mealType")), getInt("producedQuantity"), optStringOrNull("notes"), getLong("createdAt"), getLong("updatedAt"))
            }.orEmpty(),
            cookingAllocations = root.optJSONArray("cookingAllocations")?.mapObjects {
                CookingAllocationEntity(getLong("id"), getLong("batchId"), getLong("projectId"), getInt("quantity"), getLong("createdAt"), getLong("updatedAt"))
            }.orEmpty()
        )
    }

    fun validate(snapshot: RestaurantSnapshot) {
        val projectIds = snapshot.projects.map { it.id }.toSet()
        val warehouseIds = snapshot.warehouses.map { it.id }.toSet()
        val materialIds = snapshot.materials.map { it.id }.toSet()
        val supplierIds = snapshot.suppliers.map { it.id }.toSet()
        val purchaseIds = snapshot.purchases.map { it.id }.toSet()
        val bankCardIds = snapshot.bankCards.map { it.id }.toSet()
        val cookingBatchIds = snapshot.cookingBatches.map { it.id }.toSet()

        require(snapshot.projects.all { it.id > 0 && it.name.isNotBlank() && it.workerCount > 0 && it.mealPrice > 0 && it.breakfastPrice > 0 && it.lunchPrice > 0 && it.dinnerPrice > 0 }) {
            "فایل پشتیبان پروژه نامعتبر دارد"
        }
        require(snapshot.warehouses.all { it.id > 0 && it.name.isNotBlank() }) {
            "فایل پشتیبان انبار نامعتبر دارد"
        }
        require(snapshot.materials.all { it.id > 0 && it.name.isNotBlank() && it.minimumStock.isFinite() && it.minimumStock >= 0 }) {
            "فایل پشتیبان متریال نامعتبر دارد"
        }
        require(snapshot.suppliers.all { it.id > 0 && it.name.isNotBlank() }) {
            "فایل پشتیبان تامین‌کننده نامعتبر دارد"
        }
        require(snapshot.bankCards.all { it.id > 0 && it.title.isNotBlank() && it.initialBalance >= 0 }) {
            "فایل پشتیبان کارت بانکی نامعتبر دارد"
        }
        require(snapshot.mealDeliveries.all {
            it.projectId in projectIds && it.quantity > 0 && it.returnedQuantity in 0..it.quantity &&
                (it.status != DeliveryStatus.RETURNED || it.returnedQuantity == it.quantity) &&
                it.unitPrice > 0 && it.totalAmount >= 0 && (it.deliveryTimeMinutes == null || it.deliveryTimeMinutes in 0..1439)
        }) {
            "فایل پشتیبان تحویل غذای نامعتبر دارد"
        }
        require(snapshot.mealDeliveries.all {
            it.totalAmount == if (it.status == DeliveryStatus.DELIVERED) {
                Math.multiplyExact((it.quantity - it.returnedQuantity).toLong(), it.unitPrice)
            } else 0
        }) {
            "فایل پشتیبان محاسبه تحویل غذا نامعتبر دارد"
        }
        require(snapshot.purchases.all { purchase ->
            purchase.id > 0 &&
                purchase.warehouseId in warehouseIds &&
                (purchase.supplierId == null || purchase.supplierId in supplierIds) &&
                (purchase.bankCardId == null || purchase.bankCardId in bankCardIds) &&
                (purchase.paymentType != PurchasePaymentType.CARD || purchase.bankCardId != null) &&
                (purchase.paymentType != PurchasePaymentType.CREDIT || purchase.supplierId != null) &&
                purchase.discountAmount >= 0 &&
                purchase.totalAmount >= 0 &&
                purchase.paidAmount >= 0 &&
                purchase.paidAmount == if (purchase.paymentType == PurchasePaymentType.CREDIT) 0 else purchase.totalAmount
        }) {
            "فایل پشتیبان فاکتور خرید نامعتبر دارد"
        }
        require(snapshot.purchaseItems.all {
            it.purchaseId in purchaseIds && it.materialId in materialIds && it.quantity.isFinite() && it.quantity > 0 && it.unitPrice > 0 && it.totalAmount >= 0
        }) {
            "فایل پشتیبان آیتم خرید نامعتبر دارد"
        }
        val purchaseItemsByPurchase = snapshot.purchaseItems.groupBy { it.purchaseId }
        require(snapshot.purchases.all { purchase ->
            val itemsTotal = purchaseItemsByPurchase[purchase.id].orEmpty().sumOf { it.totalAmount }
            itemsTotal > 0 && purchase.discountAmount <= itemsTotal && purchase.totalAmount == itemsTotal - purchase.discountAmount
        }) {
            "فایل پشتیبان جمع فاکتور نامعتبر دارد"
        }
        require(snapshot.stockTransactions.all {
            it.warehouseId in warehouseIds &&
                it.materialId in materialIds &&
                (it.projectId == null || it.projectId in projectIds) &&
                (it.supplierId == null || it.supplierId in supplierIds) &&
                (it.purchaseId == null || it.purchaseId in purchaseIds) &&
                (it.cookingBatchId == null || it.cookingBatchId in cookingBatchIds) &&
                it.quantity.isFinite() && it.quantity != 0.0
        }) {
            "فایل پشتیبان تراکنش انبار نامعتبر دارد"
        }
        require(snapshot.stockTransactions.all {
            it.type == StockTransactionType.ADJUSTMENT || it.quantity > 0.0
        }) {
            "فایل پشتیبان مقدار تراکنش انبار نامعتبر دارد"
        }
        InventoryIntegrityValidator.validateNonNegative(snapshot, snapshot.stockTransactions)
        require(snapshot.cookingBatches.all { it.id > 0 && it.warehouseId in warehouseIds && it.producedQuantity > 0 }) {
            "فایل پشتیبان ثبت پخت نامعتبر دارد"
        }
        require(snapshot.cookingAllocations.all { it.id > 0 && it.batchId in cookingBatchIds && it.projectId in projectIds && it.quantity > 0 }) {
            "فایل پشتیبان تخصیص پخت نامعتبر دارد"
        }
        val allocationsByBatch = snapshot.cookingAllocations.groupBy { it.batchId }
        require(snapshot.cookingBatches.all { batch -> allocationsByBatch[batch.id].orEmpty().sumOf { it.quantity } == batch.producedQuantity }) {
            "جمع تخصیص پخت در فایل پشتیبان نامعتبر است"
        }
        require(snapshot.cookingBatches.all { batch -> snapshot.stockTransactions.any { it.cookingBatchId == batch.id && it.reason == StockReason.COOKING_USAGE } }) {
            "مواد مصرفی ثبت پخت در فایل پشتیبان ناقص است"
        }
        require(snapshot.projectPayments.all {
            it.projectId in projectIds && (it.bankCardId == null || it.bankCardId in bankCardIds) && it.amount > 0
        }) {
            "فایل پشتیبان دریافت پروژه نامعتبر دارد"
        }
        require(snapshot.supplierPayments.all {
            it.supplierId in supplierIds &&
                (it.bankCardId == null || it.bankCardId in bankCardIds) &&
                (it.purchaseId == null || it.purchaseId in purchaseIds) &&
                (it.purchaseId == null || snapshot.purchases.firstOrNull { purchase -> purchase.id == it.purchaseId }?.supplierId == it.supplierId) &&
                it.amount > 0
        }) {
            "فایل پشتیبان پرداخت تامین‌کننده نامعتبر دارد"
        }
        require(snapshot.expenses.all {
            (it.bankCardId == null || it.bankCardId in bankCardIds) &&
                (it.projectId == null || it.projectId in projectIds) &&
                (it.cookingBatchId == null || it.cookingBatchId in cookingBatchIds) &&
                (it.projectId == null || it.cookingBatchId == null) && it.title.isNotBlank() && it.amount > 0
        }) {
            "فایل پشتیبان هزینه نامعتبر دارد"
        }
        val earnedByProject = snapshot.mealDeliveries
            .filter { it.status == DeliveryStatus.DELIVERED }
            .groupBy { it.projectId }
            .mapValues { (_, rows) -> rows.sumOf { it.totalAmount } }
        require(snapshot.projectPayments.groupBy { it.projectId }.all { (projectId, rows) ->
            rows.sumOf { it.amount } <= (earnedByProject[projectId] ?: 0L)
        }) {
            "فایل پشتیبان برای یک پروژه دریافتی بیشتر از درآمد دارد"
        }
        val creditBySupplier = snapshot.purchases
            .filter { it.paymentType == PurchasePaymentType.CREDIT && it.supplierId != null }
            .groupBy { it.supplierId!! }
            .mapValues { (_, rows) -> rows.sumOf { it.totalAmount } }
        require(snapshot.supplierPayments.groupBy { it.supplierId }.all { (supplierId, rows) ->
            rows.sumOf { it.amount } <= (creditBySupplier[supplierId] ?: 0L)
        }) {
            "فایل پشتیبان برای یک تامین‌کننده پرداخت بیشتر از بدهی دارد"
        }
        require(snapshot.supplierPayments.filter { it.purchaseId != null }.groupBy { it.purchaseId!! }.all { (purchaseId, rows) ->
            val purchase = snapshot.purchases.first { it.id == purchaseId }
            purchase.paymentType == PurchasePaymentType.CREDIT && rows.sumOf { it.amount } <= purchase.totalAmount
        }) {
            "فایل پشتیبان تخصیص پرداخت نامعتبر دارد"
        }
    }

    private val requiredArrays = listOf(
        "projects",
        "mealDeliveries",
        "warehouses",
        "materials",
        "suppliers",
        "stockTransactions",
        "purchases",
        "purchaseItems",
        "bankCards",
        "projectPayments",
        "supplierPayments",
        "expenses"
    )

    private fun JSONObject.array(name: String): JSONArray = getJSONArray(name)

    private fun <T> List<T>.toJsonArray(block: (T) -> JSONObject): JSONArray =
        JSONArray().also { array -> forEach { array.put(block(it)) } }

    private fun <T> JSONArray.mapObjects(block: JSONObject.() -> T): List<T> =
        buildList {
            for (index in 0 until length()) add(getJSONObject(index).block())
        }

    private fun JSONObject.putNullable(name: String, value: Any?): JSONObject =
        put(name, value ?: JSONObject.NULL)

    private fun JSONObject.optStringOrNull(name: String): String? =
        if (!has(name) || isNull(name)) null else getString(name)

    private fun JSONObject.optLongOrNull(name: String): Long? =
        if (!has(name) || isNull(name)) null else getLong(name)

    private fun JSONObject.optIntOrNull(name: String): Int? =
        if (!has(name) || isNull(name)) null else getInt(name)
}
