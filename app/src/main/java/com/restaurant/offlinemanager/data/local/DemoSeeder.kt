package com.restaurant.offlinemanager.data.local

import com.restaurant.offlinemanager.core.utils.PersianDateFormatter
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

object DemoSeeder {
    suspend fun seedIfNeeded(dao: RestaurantDao) {
        if (dao.projectCount() > 0) return
        val now = PersianDateFormatter.nowMillis()
        val today = PersianDateFormatter.todayStartMillis()

        val dryCategory = dao.insertMaterialCategory(MaterialCategoryEntity(name = "خشکبار", iconName = "grain", createdAt = now, updatedAt = now))
        val proteinCategory = dao.insertMaterialCategory(MaterialCategoryEntity(name = "پروتئین", iconName = "restaurant", createdAt = now, updatedAt = now))
        val groceryCategory = dao.insertMaterialCategory(MaterialCategoryEntity(name = "خواربار", iconName = "basket", createdAt = now, updatedAt = now))
        val breadCategory = dao.insertMaterialCategory(MaterialCategoryEntity(name = "نان", iconName = "bakery", createdAt = now, updatedAt = now))

        val centralWarehouse = dao.insertWarehouse(
            WarehouseEntity(name = "انبار مرکزی", type = WarehouseType.GENERAL, address = "محوطه اصلی شرکت", notes = "ورودی اصلی کالا", isActive = true, createdAt = now, updatedAt = now)
        )
        val coldWarehouse = dao.insertWarehouse(
            WarehouseEntity(name = "سردخانه", type = WarehouseType.COLD_STORAGE, address = "بلوک خدمات", notes = "مواد پروتئینی تازه", isActive = true, createdAt = now, updatedAt = now)
        )
        val dryWarehouse = dao.insertWarehouse(
            WarehouseEntity(name = "خشکبار", type = WarehouseType.DRY, address = "سالن شماره ۲", notes = "اقلام خشک و حبوبات", isActive = true, createdAt = now, updatedAt = now)
        )

        val rice = dao.insertMaterial(MaterialEntity(name = "برنج دودی", categoryId = dryCategory, mainUnit = UnitType.KG, minimumStock = 280.0, imageEmoji = "🍚", isActive = true, createdAt = now, updatedAt = now))
        val oil = dao.insertMaterial(MaterialEntity(name = "روغن مایع", categoryId = groceryCategory, mainUnit = UnitType.LITER, minimumStock = 90.0, imageEmoji = "🛢", isActive = true, createdAt = now, updatedAt = now))
        val chicken = dao.insertMaterial(MaterialEntity(name = "مرغ تازه", categoryId = proteinCategory, mainUnit = UnitType.KG, minimumStock = 130.0, imageEmoji = "🍗", isActive = true, createdAt = now, updatedAt = now))
        val onion = dao.insertMaterial(MaterialEntity(name = "پیاز", categoryId = groceryCategory, mainUnit = UnitType.KG, minimumStock = 80.0, imageEmoji = "🧅", isActive = true, createdAt = now, updatedAt = now))
        val meat = dao.insertMaterial(MaterialEntity(name = "گوشت گوساله", categoryId = proteinCategory, mainUnit = UnitType.KG, minimumStock = 75.0, imageEmoji = "🥩", isActive = true, createdAt = now, updatedAt = now))
        val potato = dao.insertMaterial(MaterialEntity(name = "سیب‌زمینی", categoryId = groceryCategory, mainUnit = UnitType.KG, minimumStock = 90.0, imageEmoji = "🥔", isActive = true, createdAt = now, updatedAt = now))
        val bread = dao.insertMaterial(MaterialEntity(name = "نان", categoryId = breadCategory, mainUnit = UnitType.NUMBER, minimumStock = 500.0, imageEmoji = "🥖", isActive = true, createdAt = now, updatedAt = now))
        val lentil = dao.insertMaterial(MaterialEntity(name = "عدس", categoryId = dryCategory, mainUnit = UnitType.KG, minimumStock = 60.0, imageEmoji = "🫘", isActive = true, createdAt = now, updatedAt = now))
        val tomatoPaste = dao.insertMaterial(MaterialEntity(name = "رب گوجه", categoryId = groceryCategory, mainUnit = UnitType.KG, minimumStock = 70.0, imageEmoji = "🥫", isActive = true, createdAt = now, updatedAt = now))

        val supplierIranian = dao.insertSupplier(SupplierEntity(name = "تامین کننده ایرانیان", phone = "09120001122", address = "تهران، بازار مواد غذایی", isActive = true, createdAt = now, updatedAt = now))
        val supplierMeat = dao.insertSupplier(SupplierEntity(name = "گوشتیران", phone = "09125554433", address = "تهران، خیابان دامپزشکی", isActive = true, createdAt = now, updatedAt = now))
        val supplierPars = dao.insertSupplier(SupplierEntity(name = "پخش مواد غذایی پارس", phone = "02188990011", address = "کرج، انبار مرکزی", isActive = true, createdAt = now, updatedAt = now))
        val supplierVeg = dao.insertSupplier(SupplierEntity(name = "سبزیجات پاک", phone = "09370001234", address = "میدان تره‌بار", isActive = true, createdAt = now, updatedAt = now))

        val mainCard = dao.insertBankCard(
            BankCardEntity(
                title = "کارت اصلی رستوران",
                ownerName = "شرکت خدمات غذایی",
                bankName = "ملت",
                cardNumber = "6104337788991234",
                initialBalance = 820_000_000,
                isActive = true,
                notes = "دریافت‌های پروژه",
                createdAt = now,
                updatedAt = now
            )
        )
        val purchaseCard = dao.insertBankCard(
            BankCardEntity(
                title = "کارت خرید روزانه",
                ownerName = "مسئول خرید",
                bankName = "سامان",
                cardNumber = "6219861122334455",
                initialBalance = 260_000_000,
                isActive = true,
                notes = "پرداخت خریدهای خرد",
                createdAt = now,
                updatedAt = now
            )
        )

        val p1 = dao.insertProject(ProjectEntity(name = "پروژه پتروشیمی بندر امام", companyName = "پتروشیمی بندر امام", address = "ماهشهر، سایت صنعتی", managerName = "مهندس صادقی", phone = "09160001100", workerCount = 620, mealPrice = 145_000, defaultMealType = "ناهار", startDate = PersianDateFormatter.addDays(-35), endDate = null, status = ProjectStatus.ACTIVE, notes = "دو وعده در روزهای کاری", createdAt = now, updatedAt = now))
        val p2 = dao.insertProject(ProjectEntity(name = "پروژه ساخت نیروگاه سیکل", companyName = "نیروگستر جنوب", address = "عسلویه، فاز ۳", managerName = "خانم احمدی", phone = "09170002200", workerCount = 410, mealPrice = 132_000, defaultMealType = "ناهار", startDate = PersianDateFormatter.addDays(-22), endDate = null, status = ProjectStatus.ACTIVE, notes = null, createdAt = now, updatedAt = now))
        val p3 = dao.insertProject(ProjectEntity(name = "پروژه مجتمع مس سرچشمه", companyName = "ملی مس", address = "رفسنجان، مجتمع مس", managerName = "آقای کریمی", phone = "09130003300", workerCount = 280, mealPrice = 158_000, defaultMealType = "شام", startDate = PersianDateFormatter.addDays(-60), endDate = null, status = ProjectStatus.PAUSED, notes = "فعلا ظرفیت کمتر شده است", createdAt = now, updatedAt = now))
        val p4 = dao.insertProject(ProjectEntity(name = "پروژه خط لوله کوه جاسک", companyName = "پیمانکاری ساحل", address = "جاسک، کمپ شماره ۴", managerName = "آقای موسوی", phone = "09150004400", workerCount = 350, mealPrice = 138_000, defaultMealType = "ناهار", startDate = PersianDateFormatter.addDays(-12), endDate = null, status = ProjectStatus.ACTIVE, notes = "ارسال با خودروی یخچال‌دار", createdAt = now, updatedAt = now))

        createPurchase(dao, supplierIranian, dryWarehouse, today - 4 * DAY, "INV-1404-101", PurchasePaymentType.CREDIT, null, listOf(
            Item(rice, 700.0, UnitType.KG, 82_000),
            Item(lentil, 130.0, UnitType.KG, 74_000),
            Item(tomatoPaste, 120.0, UnitType.KG, 68_000)
        ), discount = 1_500_000, now = now)
        createPurchase(dao, supplierMeat, coldWarehouse, today - 2 * DAY, "INV-1404-118", PurchasePaymentType.CARD, purchaseCard, listOf(
            Item(chicken, 240.0, UnitType.KG, 112_000),
            Item(meat, 120.0, UnitType.KG, 420_000)
        ), discount = 0, now = now)
        createPurchase(dao, supplierVeg, centralWarehouse, today, "INV-1404-127", PurchasePaymentType.CASH, null, listOf(
            Item(onion, 140.0, UnitType.KG, 24_000),
            Item(potato, 160.0, UnitType.KG, 22_000),
            Item(bread, 900.0, UnitType.NUMBER, 4_500),
            Item(oil, 70.0, UnitType.LITER, 82_000)
        ), discount = 450_000, now = now)

        listOf(
            StockTransactionEntity(warehouseId = coldWarehouse, materialId = chicken, projectId = p1, type = StockTransactionType.OUT, reason = StockReason.COOKING_USAGE, quantity = 80.0, unit = UnitType.KG, date = today, notes = "مصرف ناهار پتروشیمی", createdAt = now, updatedAt = now),
            StockTransactionEntity(warehouseId = dryWarehouse, materialId = rice, projectId = p1, type = StockTransactionType.OUT, reason = StockReason.COOKING_USAGE, quantity = 170.0, unit = UnitType.KG, date = today, notes = "مصرف ناهار", createdAt = now, updatedAt = now),
            StockTransactionEntity(warehouseId = coldWarehouse, materialId = meat, projectId = p4, type = StockTransactionType.OUT, reason = StockReason.COOKING_USAGE, quantity = 55.0, unit = UnitType.KG, date = today, notes = "خورشت شام کمپ", createdAt = now, updatedAt = now),
            StockTransactionEntity(warehouseId = centralWarehouse, materialId = bread, projectId = p2, type = StockTransactionType.OUT, reason = StockReason.COOKING_USAGE, quantity = 430.0, unit = UnitType.NUMBER, date = today, notes = "صبحانه نیروگاه", createdAt = now, updatedAt = now),
            StockTransactionEntity(warehouseId = centralWarehouse, materialId = onion, type = StockTransactionType.WASTE, reason = StockReason.WASTE, quantity = 8.0, unit = UnitType.KG, date = today, notes = "ضایعات پاکسازی", createdAt = now, updatedAt = now)
        ).forEach { dao.insertStockTransaction(it) }

        listOf(
            MealDeliveryEntity(projectId = p1, date = today, mealType = MealType.LUNCH, quantity = 620, unitPrice = 145_000, totalAmount = 620 * 145_000L, notes = "چلو مرغ", createdAt = now, updatedAt = now),
            MealDeliveryEntity(projectId = p2, date = today, mealType = MealType.BREAKFAST, quantity = 410, unitPrice = 58_000, totalAmount = 410 * 58_000L, notes = "صبحانه گرم", createdAt = now, updatedAt = now),
            MealDeliveryEntity(projectId = p4, date = today, mealType = MealType.DINNER, quantity = 350, unitPrice = 138_000, totalAmount = 350 * 138_000L, notes = "خورشت قیمه", createdAt = now, updatedAt = now),
            MealDeliveryEntity(projectId = p1, date = today - DAY, mealType = MealType.LUNCH, quantity = 615, unitPrice = 145_000, totalAmount = 615 * 145_000L, notes = null, createdAt = now, updatedAt = now),
            MealDeliveryEntity(projectId = p3, date = today - 3 * DAY, mealType = MealType.DINNER, quantity = 260, unitPrice = 158_000, totalAmount = 260 * 158_000L, notes = null, createdAt = now, updatedAt = now)
        ).forEach { dao.insertMealDelivery(it) }

        listOf(
            ProjectPaymentEntity(projectId = p1, bankCardId = mainCard, amount = 95_000_000, date = today - DAY, method = PaymentMethod.BANK_TRANSFER, notes = "پرداخت مرحله اول", createdAt = now, updatedAt = now),
            ProjectPaymentEntity(projectId = p2, bankCardId = mainCard, amount = 45_000_000, date = today - 2 * DAY, method = PaymentMethod.CARD_TO_CARD, notes = "واریزی حسابداری", createdAt = now, updatedAt = now),
            SupplierPaymentEntity(supplierId = supplierIranian, bankCardId = purchaseCard, amount = 32_000_000, date = today - DAY, method = PaymentMethod.CARD_TO_CARD, notes = "بابت بخشی از فاکتور برنج", createdAt = now, updatedAt = now),
            SupplierPaymentEntity(supplierId = supplierPars, bankCardId = purchaseCard, amount = 8_000_000, date = today - 6 * DAY, method = PaymentMethod.CARD_TO_CARD, notes = "تسویه قدیمی", createdAt = now, updatedAt = now)
        ).forEach {
            when (it) {
                is ProjectPaymentEntity -> dao.insertProjectPayment(it)
                is SupplierPaymentEntity -> dao.insertSupplierPayment(it)
            }
        }

        listOf(
            ExpenseEntity(title = "کرایه حمل مواد اولیه", category = ExpenseCategory.TRANSPORT, amount = 6_800_000, date = today, bankCardId = purchaseCard, notes = "وانت یخچال‌دار", createdAt = now, updatedAt = now),
            ExpenseEntity(title = "حقوق پرسنل آشپزخانه", category = ExpenseCategory.SALARY, amount = 72_000_000, date = today - 5 * DAY, bankCardId = mainCard, notes = "علی‌الحساب", createdAt = now, updatedAt = now),
            ExpenseEntity(title = "تعمیر دیگ بخار", category = ExpenseCategory.REPAIR, amount = 18_500_000, date = today - 9 * DAY, bankCardId = purchaseCard, notes = null, createdAt = now, updatedAt = now)
        ).forEach { dao.insertExpense(it) }
    }

    private suspend fun createPurchase(
        dao: RestaurantDao,
        supplierId: Long,
        warehouseId: Long,
        date: Long,
        invoiceNumber: String,
        paymentType: PurchasePaymentType,
        bankCardId: Long?,
        items: List<Item>,
        discount: Long,
        now: Long
    ) {
        val subtotal = items.sumOf { (it.quantity * it.unitPrice).toLong() }
        val total = (subtotal - discount).coerceAtLeast(0)
        val purchaseId = dao.insertPurchase(
            PurchaseEntity(
                supplierId = supplierId,
                warehouseId = warehouseId,
                date = date,
                invoiceNumber = invoiceNumber,
                paymentType = paymentType,
                bankCardId = bankCardId,
                discountAmount = discount,
                totalAmount = total,
                paidAmount = if (paymentType == PurchasePaymentType.CREDIT) 0 else total,
                notes = "داده نمونه اولیه",
                createdAt = now,
                updatedAt = now
            )
        )
        items.forEach { item ->
            val itemTotal = (item.quantity * item.unitPrice).toLong()
            dao.insertPurchaseItem(
                PurchaseItemEntity(
                    purchaseId = purchaseId,
                    materialId = item.materialId,
                    quantity = item.quantity,
                    unit = item.unit,
                    unitPrice = item.unitPrice,
                    totalAmount = itemTotal,
                    createdAt = now,
                    updatedAt = now
                )
            )
            dao.insertStockTransaction(
                StockTransactionEntity(
                    warehouseId = warehouseId,
                    materialId = item.materialId,
                    supplierId = supplierId,
                    purchaseId = purchaseId,
                    type = StockTransactionType.IN,
                    reason = StockReason.PURCHASE,
                    quantity = item.quantity,
                    unit = item.unit,
                    unitPrice = item.unitPrice,
                    totalAmount = itemTotal,
                    date = date,
                    notes = "ورود خودکار از فاکتور $invoiceNumber",
                    createdAt = now,
                    updatedAt = now
                )
            )
        }
    }

    private data class Item(
        val materialId: Long,
        val quantity: Double,
        val unit: UnitType,
        val unitPrice: Long
    )

    private const val DAY = 24L * 60L * 60L * 1000L
}
