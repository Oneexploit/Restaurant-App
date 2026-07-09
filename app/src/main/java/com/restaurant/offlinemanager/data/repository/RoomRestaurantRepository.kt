package com.restaurant.offlinemanager.data.repository

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.restaurant.offlinemanager.core.utils.PersianDateFormatter
import com.restaurant.offlinemanager.data.local.AppDatabase
import com.restaurant.offlinemanager.data.local.dao.RestaurantDao
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
import com.restaurant.offlinemanager.data.mapper.toEntity
import com.restaurant.offlinemanager.domain.model.MealDeliveryInput
import com.restaurant.offlinemanager.domain.model.ProjectInput
import com.restaurant.offlinemanager.domain.model.ProjectPaymentInput
import com.restaurant.offlinemanager.domain.model.PurchaseInput
import com.restaurant.offlinemanager.domain.model.RestaurantSnapshot
import com.restaurant.offlinemanager.domain.model.StockTransactionInput
import com.restaurant.offlinemanager.domain.model.SupplierPaymentInput
import com.restaurant.offlinemanager.domain.repository.RestaurantRepository
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
            dao.observeMaterialCategories().map { it as Any },
            dao.observeMaterials().map { it as Any },
            dao.observeSuppliers().map { it as Any },
            dao.observeStockTransactions().map { it as Any },
            dao.observePurchases().map { it as Any },
            dao.observePurchaseItems().map { it as Any },
            dao.observeBankCards().map { it as Any },
            dao.observeProjectPayments().map { it as Any },
            dao.observeSupplierPayments().map { it as Any },
            dao.observeExpenses().map { it as Any }
        )
        return combine(*flows) { values ->
            @Suppress("UNCHECKED_CAST")
            RestaurantSnapshot(
                projects = values[0] as List<ProjectEntity>,
                mealDeliveries = values[1] as List<MealDeliveryEntity>,
                warehouses = values[2] as List<WarehouseEntity>,
                materialCategories = values[3] as List<MaterialCategoryEntity>,
                materials = values[4] as List<MaterialEntity>,
                suppliers = values[5] as List<SupplierEntity>,
                stockTransactions = values[6] as List<StockTransactionEntity>,
                purchases = values[7] as List<PurchaseEntity>,
                purchaseItems = values[8] as List<PurchaseItemEntity>,
                bankCards = values[9] as List<BankCardEntity>,
                projectPayments = values[10] as List<ProjectPaymentEntity>,
                supplierPayments = values[11] as List<SupplierPaymentEntity>,
                expenses = values[12] as List<ExpenseEntity>
            )
        }
    }

    override suspend fun currentSnapshot(): RestaurantSnapshot =
        RestaurantSnapshot(
            projects = dao.getProjects(),
            mealDeliveries = dao.getMealDeliveries(),
            warehouses = dao.getWarehouses(),
            materialCategories = dao.getMaterialCategories(),
            materials = dao.getMaterials(),
            suppliers = dao.getSuppliers(),
            stockTransactions = dao.getStockTransactions(),
            purchases = dao.getPurchases(),
            purchaseItems = dao.getPurchaseItems(),
            bankCards = dao.getBankCards(),
            projectPayments = dao.getProjectPayments(),
            supplierPayments = dao.getSupplierPayments(),
            expenses = dao.getExpenses()
        )

    override suspend fun saveProject(input: ProjectInput): Long {
        require(input.name.isNotBlank()) { "نام پروژه الزامی است" }
        require(input.workerCount > 0) { "تعداد نفرات باید بیشتر از صفر باشد" }
        require(input.mealPrice > 0) { "قیمت هر وعده باید بیشتر از صفر باشد" }
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
            val snapshot = currentSnapshot()
            val project = snapshot.projects.firstOrNull { it.id == input.projectId } ?: error("پروژه پیدا نشد")
            require(project.status == ProjectStatus.ACTIVE) { "ثبت وعده فقط برای پروژه فعال مجاز است" }
            require(snapshot.mealDeliveries.none { it.id != input.id && it.projectId == input.projectId && it.date == input.date && it.mealType == input.mealType }) {
                "برای این پروژه، تاریخ و نوع وعده قبلا ثبت شده است"
            }
            val existing = snapshot.mealDeliveries.firstOrNull { it.id == input.id }
            val now = PersianDateFormatter.nowMillis()
            dao.insertMealDelivery(
                MealDeliveryEntity(
                    id = input.id,
                    projectId = input.projectId,
                    date = input.date,
                    mealType = input.mealType,
                    quantity = input.quantity,
                    unitPrice = input.unitPrice,
                    totalAmount = input.quantity * input.unitPrice,
                    notes = input.notes?.ifBlank { null },
                    createdAt = existing?.createdAt ?: now,
                    updatedAt = now
                )
            )
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

    override suspend fun saveMaterialCategory(entity: MaterialCategoryEntity): Long {
        require(entity.name.isNotBlank()) { "نام دسته‌بندی الزامی است" }
        val now = PersianDateFormatter.nowMillis()
        return dao.insertMaterialCategory(
            entity.copy(
                name = entity.name.trim(),
                iconName = entity.iconName?.trim()?.ifBlank { null },
                createdAt = entity.createdAt.takeIf { it > 0 } ?: now,
                updatedAt = now
            )
        )
    }

    override suspend fun saveMaterial(entity: MaterialEntity): Long {
        require(entity.name.isNotBlank()) { "نام متریال الزامی است" }
        require(entity.minimumStock >= 0.0) { "حداقل موجودی نمی‌تواند منفی باشد" }
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
        require(input.quantity != 0.0) { "مقدار کالا نمی‌تواند صفر باشد" }
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
        val available = InventoryUseCase(this).availableStock(snapshot, input.warehouseId, input.materialId)
        val outgoing = input.type == StockTransactionType.OUT ||
            input.type == StockTransactionType.TRANSFER_OUT ||
            input.type == StockTransactionType.WASTE
        require(!outgoing || input.quantity <= available) {
            "موجودی کافی نیست. موجودی فعلی: $available"
        }
        require(input.type != StockTransactionType.ADJUSTMENT || available + input.quantity >= 0.0) {
            "اصلاح موجودی نمی‌تواند موجودی نهایی را منفی کند"
        }
        val now = PersianDateFormatter.nowMillis()
        return dao.insertStockTransaction(
            StockTransactionEntity(
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
        )
    }

    override suspend fun savePurchase(input: PurchaseInput): Result<Long> =
        runCatching {
            require(input.items.isNotEmpty()) { "فاکتور باید حداقل یک آیتم داشته باشد" }
            require(input.discountAmount >= 0) { "تخفیف نمی‌تواند منفی باشد" }
            require(input.items.all { it.quantity > 0 && it.unitPrice > 0 }) { "مقدار و قیمت همه آیتم‌ها باید مثبت باشد" }
            val subtotal = input.items.sumOf { it.totalAmount }
            require(input.discountAmount <= subtotal) { "تخفیف نمی‌تواند بیشتر از جمع آیتم‌ها باشد" }
            val snapshot = currentSnapshot()
            val existing = snapshot.purchases.firstOrNull { it.id == input.id }
            require(input.id == 0L || existing != null) { "فاکتور خرید برای ویرایش پیدا نشد" }
            require(snapshot.warehouses.any { it.id == input.warehouseId && it.isActive }) {
                "انبار مقصد معتبر نیست"
            }
            input.supplierId?.let { supplierId ->
                require(snapshot.suppliers.any { it.id == supplierId && it.isActive }) { "تامین‌کننده معتبر نیست" }
            }
            require(input.paymentType != PurchasePaymentType.CREDIT || input.supplierId != null) {
                "برای خرید نسیه باید تامین‌کننده انتخاب شود"
            }
            val normalizedCardId = if (input.paymentType == PurchasePaymentType.CARD) input.bankCardId else null
            require(input.paymentType != PurchasePaymentType.CARD || normalizedCardId != null) {
                "برای خرید کارتی باید کارت بانکی انتخاب شود"
            }
            normalizedCardId?.let { cardId ->
                require(snapshot.bankCards.any { it.id == cardId && it.isActive }) { "کارت بانکی معتبر نیست" }
            }
            require(input.items.all { item -> snapshot.materials.any { it.id == item.materialId && it.isActive && it.mainUnit == item.unit } }) {
                "همه آیتم‌های فاکتور باید متریال فعال و واحد معتبر داشته باشند"
            }
            val normalizedInvoice = input.invoiceNumber?.trim()?.ifBlank { null }
            require(normalizedInvoice == null || snapshot.purchases.none { it.id != input.id && it.supplierId == input.supplierId && it.invoiceNumber == normalizedInvoice }) {
                "شماره فاکتور برای این تامین‌کننده قبلا ثبت شده است"
            }
            database.withTransaction {
                val now = PersianDateFormatter.nowMillis()
                val total = subtotal - input.discountAmount
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
                            createdAt = now,
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
                            createdAt = now,
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
                cardNumber = entity.cardNumber?.let(::safeStoredCardNumber),
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
            require(input.method == PaymentMethod.CASH || input.bankCardId != null) {
                "برای دریافت غیرنقدی باید کارت بانکی انتخاب شود"
            }
            input.bankCardId?.let { cardId ->
                require(snapshot.bankCards.any { it.id == cardId && it.isActive }) { "کارت بانکی معتبر نیست" }
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
                    bankCardId = input.bankCardId,
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
            require(snapshot.suppliers.any { it.id == input.supplierId && it.isActive }) { "تامین‌کننده معتبر نیست" }
            require(input.method == PaymentMethod.CASH || input.bankCardId != null) {
                "برای پرداخت غیرنقدی باید کارت بانکی انتخاب شود"
            }
            input.bankCardId?.let { cardId ->
                require(snapshot.bankCards.any { it.id == cardId && it.isActive }) { "کارت بانکی معتبر نیست" }
            }
            val debt = SupplierDebtUseCase(this).calculateSupplierDebts(snapshot)
                .firstOrNull { it.supplier.id == input.supplierId }
            val editableAllowance = debt?.remaining.orZero() + if (existing?.supplierId == input.supplierId) existing.amount else 0L
            require(debt != null && input.amount <= editableAllowance) {
                "مبلغ پرداخت بیشتر از بدهی تامین‌کننده است"
            }
            val now = PersianDateFormatter.nowMillis()
            dao.insertSupplierPayment(
                SupplierPaymentEntity(
                    id = input.id,
                    supplierId = input.supplierId,
                    bankCardId = input.bankCardId,
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
        entity.bankCardId?.let { cardId ->
            require(currentSnapshot().bankCards.any { it.id == cardId && it.isActive }) { "کارت بانکی معتبر نیست" }
        }
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
            dao.deleteMealDelivery(id)
        }

    override suspend fun deleteStockTransaction(id: Long): Result<Unit> =
        runCatching {
            val tx = dao.getStockTransaction(id) ?: error("تراکنش انبار پیدا نشد")
            require(tx.purchaseId == null) { "تراکنش‌های خودکار خرید را از خود فاکتور خرید اصلاح یا حذف کنید" }
            dao.deleteStockTransaction(id)
        }

    override suspend fun deletePurchase(id: Long): Result<Unit> =
        runCatching {
            dao.getPurchase(id) ?: error("فاکتور خرید پیدا نشد")
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
                dao.insertMaterialCategories(snapshot.materialCategories)
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
            }
        }

    override suspend fun exportCsv(context: Context, fileName: String, csv: String): File {
        val dir = File(context.getExternalFilesDir(null), "reports").apply { mkdirs() }
        val file = File(dir, fileName)
        file.writeText(csv, Charsets.UTF_8)
        return file
    }

    private suspend fun clearAll() {
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
        dao.clearMaterialCategories()
        dao.clearWarehouses()
        dao.clearMealDeliveries()
    }
}

private fun Long?.orZero(): Long = this ?: 0L

private fun safeStoredCardNumber(value: String): String? {
    val digits = value.filter { it.isDigit() }
    if (digits.isBlank()) return null
    return if (digits.length <= 8) digits else digits.take(4) + digits.takeLast(4)
}

private object BackupJson {
    fun encode(snapshot: RestaurantSnapshot): String {
        val root = JSONObject()
        root.put("version", 1)
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
                .put("mealType", it.mealType.name)
                .put("quantity", it.quantity)
                .put("unitPrice", it.unitPrice)
                .put("totalAmount", it.totalAmount)
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
        root.put("materialCategories", snapshot.materialCategories.toJsonArray {
            JSONObject()
                .put("id", it.id)
                .put("name", it.name)
                .putNullable("iconName", it.iconName)
                .put("createdAt", it.createdAt)
                .put("updatedAt", it.updatedAt)
        })
        root.put("materials", snapshot.materials.toJsonArray {
            JSONObject()
                .put("id", it.id)
                .put("name", it.name)
                .putNullable("categoryId", it.categoryId)
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
                .putNullable("notes", it.notes)
                .put("createdAt", it.createdAt)
                .put("updatedAt", it.updatedAt)
        })
        return root.toString(2)
    }

    fun decode(json: String): RestaurantSnapshot {
        val root = JSONObject(json)
        require(root.optInt("version", 0) == 1) { "نسخه فایل پشتیبان پشتیبانی نمی‌شود" }
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
                MealDeliveryEntity(getLong("id"), getLong("projectId"), getLong("date"), MealType.valueOf(getString("mealType")), getInt("quantity"), getLong("unitPrice"), getLong("totalAmount"), optStringOrNull("notes"), getLong("createdAt"), getLong("updatedAt"))
            },
            warehouses = root.array("warehouses").mapObjects {
                WarehouseEntity(getLong("id"), getString("name"), WarehouseType.valueOf(getString("type")), optStringOrNull("address"), optStringOrNull("notes"), getBoolean("isActive"), getLong("createdAt"), getLong("updatedAt"))
            },
            materialCategories = root.array("materialCategories").mapObjects {
                MaterialCategoryEntity(getLong("id"), getString("name"), optStringOrNull("iconName"), getLong("createdAt"), getLong("updatedAt"))
            },
            materials = root.array("materials").mapObjects {
                MaterialEntity(getLong("id"), getString("name"), optLongOrNull("categoryId"), UnitType.valueOf(getString("mainUnit")), getDouble("minimumStock"), optStringOrNull("imageEmoji"), optStringOrNull("notes"), getBoolean("isActive"), getLong("createdAt"), getLong("updatedAt"))
            },
            suppliers = root.array("suppliers").mapObjects {
                SupplierEntity(getLong("id"), getString("name"), optStringOrNull("phone"), optStringOrNull("address"), optStringOrNull("notes"), getBoolean("isActive"), getLong("createdAt"), getLong("updatedAt"))
            },
            stockTransactions = root.array("stockTransactions").mapObjects {
                StockTransactionEntity(getLong("id"), getLong("warehouseId"), getLong("materialId"), optLongOrNull("projectId"), optLongOrNull("supplierId"), optLongOrNull("purchaseId"), StockTransactionType.valueOf(getString("type")), StockReason.valueOf(getString("reason")), getDouble("quantity"), UnitType.valueOf(getString("unit")), optLongOrNull("unitPrice"), optLongOrNull("totalAmount"), getLong("date"), optStringOrNull("notes"), getLong("createdAt"), getLong("updatedAt"))
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
                SupplierPaymentEntity(getLong("id"), getLong("supplierId"), optLongOrNull("bankCardId"), getLong("amount"), getLong("date"), PaymentMethod.valueOf(getString("method")), optStringOrNull("notes"), getLong("createdAt"), getLong("updatedAt"))
            },
            expenses = root.array("expenses").mapObjects {
                ExpenseEntity(getLong("id"), getString("title"), ExpenseCategory.valueOf(getString("category")), getLong("amount"), getLong("date"), optLongOrNull("bankCardId"), optStringOrNull("notes"), getLong("createdAt"), getLong("updatedAt"))
            }
        )
    }

    fun validate(snapshot: RestaurantSnapshot) {
        val projectIds = snapshot.projects.map { it.id }.toSet()
        val warehouseIds = snapshot.warehouses.map { it.id }.toSet()
        val categoryIds = snapshot.materialCategories.map { it.id }.toSet()
        val materialIds = snapshot.materials.map { it.id }.toSet()
        val supplierIds = snapshot.suppliers.map { it.id }.toSet()
        val purchaseIds = snapshot.purchases.map { it.id }.toSet()
        val bankCardIds = snapshot.bankCards.map { it.id }.toSet()

        require(snapshot.projects.all { it.id > 0 && it.name.isNotBlank() && it.workerCount > 0 && it.mealPrice > 0 }) {
            "فایل پشتیبان پروژه نامعتبر دارد"
        }
        require(snapshot.warehouses.all { it.id > 0 && it.name.isNotBlank() }) {
            "فایل پشتیبان انبار نامعتبر دارد"
        }
        require(snapshot.materialCategories.all { it.id > 0 && it.name.isNotBlank() }) {
            "فایل پشتیبان دسته‌بندی نامعتبر دارد"
        }
        require(snapshot.materials.all { it.id > 0 && it.name.isNotBlank() && it.minimumStock >= 0 && (it.categoryId == null || it.categoryId in categoryIds) }) {
            "فایل پشتیبان متریال نامعتبر دارد"
        }
        require(snapshot.suppliers.all { it.id > 0 && it.name.isNotBlank() }) {
            "فایل پشتیبان تامین‌کننده نامعتبر دارد"
        }
        require(snapshot.bankCards.all { it.id > 0 && it.title.isNotBlank() && it.initialBalance >= 0 }) {
            "فایل پشتیبان کارت بانکی نامعتبر دارد"
        }
        require(snapshot.mealDeliveries.all { it.projectId in projectIds && it.quantity > 0 && it.unitPrice > 0 && it.totalAmount >= 0 }) {
            "فایل پشتیبان وعده نامعتبر دارد"
        }
        require(snapshot.mealDeliveries.all { it.totalAmount == it.quantity * it.unitPrice }) {
            "فایل پشتیبان محاسبه وعده نامعتبر دارد"
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
            it.purchaseId in purchaseIds && it.materialId in materialIds && it.quantity > 0 && it.unitPrice > 0 && it.totalAmount >= 0
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
                it.quantity != 0.0
        }) {
            "فایل پشتیبان تراکنش انبار نامعتبر دارد"
        }
        require(snapshot.stockTransactions.all {
            it.type == StockTransactionType.ADJUSTMENT || it.quantity > 0.0
        }) {
            "فایل پشتیبان مقدار تراکنش انبار نامعتبر دارد"
        }
        require(snapshot.projectPayments.all {
            it.projectId in projectIds && (it.bankCardId == null || it.bankCardId in bankCardIds) && it.amount > 0
        }) {
            "فایل پشتیبان دریافت پروژه نامعتبر دارد"
        }
        require(snapshot.supplierPayments.all {
            it.supplierId in supplierIds && (it.bankCardId == null || it.bankCardId in bankCardIds) && it.amount > 0
        }) {
            "فایل پشتیبان پرداخت تامین‌کننده نامعتبر دارد"
        }
        require(snapshot.expenses.all {
            (it.bankCardId == null || it.bankCardId in bankCardIds) && it.title.isNotBlank() && it.amount > 0
        }) {
            "فایل پشتیبان هزینه نامعتبر دارد"
        }
    }

    private val requiredArrays = listOf(
        "projects",
        "mealDeliveries",
        "warehouses",
        "materialCategories",
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
}
