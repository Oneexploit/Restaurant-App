package com.restaurant.offlinemanager.ui.purchases

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.restaurant.offlinemanager.core.design.AppCyan
import com.restaurant.offlinemanager.core.design.AppGreen
import com.restaurant.offlinemanager.core.design.AppOrange
import com.restaurant.offlinemanager.core.design.AppRed
import com.restaurant.offlinemanager.core.design.AppSearchBar
import com.restaurant.offlinemanager.core.design.ConfirmDialog
import com.restaurant.offlinemanager.core.design.DangerButton
import com.restaurant.offlinemanager.core.design.DarkOutlinedTextField
import com.restaurant.offlinemanager.core.design.EmptyState
import com.restaurant.offlinemanager.core.design.FilterChipRow
import com.restaurant.offlinemanager.core.design.FormActionFooter
import com.restaurant.offlinemanager.core.design.GlassCard
import com.restaurant.offlinemanager.core.design.Gold
import com.restaurant.offlinemanager.core.design.GoldPrimaryButton
import com.restaurant.offlinemanager.core.design.LocalDateSelector
import com.restaurant.offlinemanager.core.design.MoneyField
import com.restaurant.offlinemanager.core.design.MoneyText
import com.restaurant.offlinemanager.core.design.OptionSelector
import com.restaurant.offlinemanager.core.design.PersianDateText
import com.restaurant.offlinemanager.core.design.QuantityField
import com.restaurant.offlinemanager.core.design.SectionHeader
import com.restaurant.offlinemanager.core.design.SecondaryGlassButton
import com.restaurant.offlinemanager.core.design.StatCard
import com.restaurant.offlinemanager.core.design.StatusChip
import com.restaurant.offlinemanager.core.design.TextMuted
import com.restaurant.offlinemanager.core.design.TextPrimary
import com.restaurant.offlinemanager.core.design.TextSecondary
import com.restaurant.offlinemanager.core.utils.MoneyFormatter
import com.restaurant.offlinemanager.core.utils.NumberFormatter
import com.restaurant.offlinemanager.core.utils.PersianDateFormatter
import com.restaurant.offlinemanager.data.local.entity.MaterialEntity
import com.restaurant.offlinemanager.data.local.entity.PurchasePaymentType
import com.restaurant.offlinemanager.data.local.entity.SupplierEntity
import com.restaurant.offlinemanager.domain.model.PurchaseInput
import com.restaurant.offlinemanager.domain.model.PurchaseItemInput
import com.restaurant.offlinemanager.domain.model.label
import com.restaurant.offlinemanager.ui.AppUiState

@Composable
fun PurchasesListScreen(
    state: AppUiState,
    onAddPurchase: () -> Unit,
    onEditPurchase: (Long) -> Unit,
    onAddSupplier: () -> Unit,
    onEditSupplier: (Long) -> Unit,
    onDeleteSupplier: (Long) -> Unit,
    onDeletePurchase: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf("همه") }
    var deleteId by remember { mutableStateOf<Long?>(null) }
    var deleteSupplierId by remember { mutableStateOf<Long?>(null) }
    val purchases = state.snapshot.purchases.filter { purchase ->
        val supplier = state.snapshot.suppliers.firstOrNull { it.id == purchase.supplierId }?.name.orEmpty()
        val queryMatch = query.isBlank() || supplier.contains(query) || purchase.invoiceNumber.orEmpty().contains(query)
        val filterMatch = filter == "همه" || purchase.paymentType.label() == filter
        queryMatch && filterMatch
    }
    val creditPurchases = state.snapshot.purchases
        .filter { it.paymentType == PurchasePaymentType.CREDIT }
        .sumOf { it.totalAmount }
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { AppSearchBar(query, { query = it }, label = "جستجو در خریدها") }
        item { FilterChipRow(listOf("همه", "نقدی", "کارت", "نسیه"), filter, { filter = it }) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("خرید امروز", MoneyFormatter.format(state.dashboard.todayPurchasesTotal), Icons.Outlined.ShoppingCart, AppCyan, Modifier.weight(1f))
                StatCard("خرید ماه", MoneyFormatter.format(state.dashboard.monthPurchasesTotal), Icons.Outlined.ShoppingCart, AppOrange, Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("خرید نسیه", MoneyFormatter.format(creditPurchases), Icons.Outlined.Payments, AppRed, Modifier.weight(1f))
                StatCard("تعداد فاکتور", NumberFormatter.format(purchases.size), Icons.Outlined.ShoppingCart, AppGreen, Modifier.weight(1f), "مورد")
            }
        }
        item { GoldPrimaryButton("ثبت فاکتور خرید", onClick = onAddPurchase, icon = Icons.Outlined.ShoppingCart) }
        item { GoldPrimaryButton("افزودن تامین‌کننده", onClick = onAddSupplier, icon = Icons.Outlined.Add) }
        if (state.snapshot.suppliers.isNotEmpty()) {
            item { SectionHeader("تامین‌کنندگان") }
            items(state.snapshot.suppliers, key = { "supplier-${it.id}" }) { supplier ->
                GlassCard(Modifier.fillMaxWidth(), accent = AppCyan) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.ShoppingCart, contentDescription = null, tint = AppCyan)
                        Column(Modifier.weight(1f)) {
                            Text(supplier.name, color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                            Text(supplier.phone.orEmpty().ifBlank { "شماره تماس ثبت نشده" }, color = TextSecondary)
                            Text(supplier.address.orEmpty().ifBlank { "آدرس ثبت نشده" }, color = TextMuted)
                        }
                        StatusChip(if (supplier.isActive) "فعال" else "غیرفعال", if (supplier.isActive) AppGreen else TextMuted)
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SecondaryGlassButton("ویرایش", onClick = { onEditSupplier(supplier.id) }, icon = Icons.Outlined.Save, accent = AppCyan, modifier = Modifier.weight(1f))
                        DangerButton("حذف", onClick = { deleteSupplierId = supplier.id }, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
        if (purchases.isEmpty()) {
            item { EmptyState("خریدی پیدا نشد", "برای شروع یک فاکتور روزانه ثبت کنید.") }
        } else {
            items(purchases, key = { it.id }) { purchase ->
                val supplier = state.snapshot.suppliers.firstOrNull { it.id == purchase.supplierId }
                val warehouse = state.snapshot.warehouses.firstOrNull { it.id == purchase.warehouseId }
                val purchaseItems = state.snapshot.purchaseItems.filter { it.purchaseId == purchase.id }
                val firstMaterial = state.snapshot.materials.firstOrNull { material -> purchaseItems.firstOrNull()?.materialId == material.id }
                val itemsTitle = if (purchaseItems.size > 1) {
                    "${NumberFormatter.format(purchaseItems.size)} قلم کالا"
                } else {
                    firstMaterial?.name ?: "کالای نامشخص"
                }
                val allocatedPayments = state.snapshot.supplierPayments.filter { it.purchaseId == purchase.id }.sumOf { it.amount }
                val remainingAmount = (purchase.totalAmount - purchase.paidAmount - allocatedPayments).coerceAtLeast(0)
                GlassCard(Modifier.fillMaxWidth(), accent = if (purchase.paymentType == PurchasePaymentType.CREDIT) AppOrange else AppGreen) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(firstMaterial?.imageEmoji ?: "🧾", style = MaterialTheme.typography.headlineMedium)
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(supplier?.name ?: "بدون تامین‌کننده", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                            Text(itemsTitle, color = TextSecondary)
                            Text("${warehouse?.name.orEmpty()} • ${NumberFormatter.format(purchaseItems.size)} آیتم", color = TextMuted)
                            PersianDateText(purchase.date)
                            if (remainingAmount > 0) {
                                Text("مانده: ${MoneyFormatter.format(remainingAmount)}", color = AppOrange)
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            StatusChip(purchase.paymentType.label(), if (purchase.paymentType == PurchasePaymentType.CREDIT) AppOrange else AppGreen)
                            MoneyText(purchase.totalAmount, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SecondaryGlassButton("ویرایش", onClick = { onEditPurchase(purchase.id) }, icon = Icons.Outlined.Edit, accent = AppCyan, modifier = Modifier.weight(1f))
                        DangerButton("حذف فاکتور", onClick = { deleteId = purchase.id }, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
        item { Spacer(Modifier.height(20.dp)) }
    }
    deleteId?.let { id ->
        ConfirmDialog(
            title = "حذف فاکتور خرید",
            message = "فاکتور، آیتم‌های آن و ورود خودکار انبار حذف می‌شود.",
            confirmText = "حذف",
            onConfirm = {
                deleteId = null
                onDeletePurchase(id)
            },
            onDismiss = { deleteId = null }
        )
    }
    deleteSupplierId?.let { id ->
        ConfirmDialog(
            title = "حذف تامین‌کننده",
            message = "اگر این تامین‌کننده در خرید یا پرداخت استفاده شده باشد، برای حفظ سوابق فقط غیرفعال می‌شود.",
            confirmText = "حذف",
            onConfirm = {
                deleteSupplierId = null
                onDeleteSupplier(id)
            },
            onDismiss = { deleteSupplierId = null }
        )
    }
}

@Composable
fun PurchaseFormScreen(
    state: AppUiState,
    purchaseId: Long?,
    onSave: (PurchaseInput) -> Unit,
    onAddSupplier: () -> Unit,
    modifier: Modifier = Modifier
) {
    val editing = state.snapshot.purchases.firstOrNull { it.id == purchaseId }
    val editingItems = state.snapshot.purchaseItems.filter { it.purchaseId == editing?.id }
    val suppliers = state.snapshot.suppliers.filter { it.isActive || it.id == editing?.supplierId }
    val warehouses = state.snapshot.warehouses.filter { it.isActive || it.id == editing?.warehouseId }
    val cards = state.snapshot.bankCards.filter { it.isActive || it.id == editing?.bankCardId }
    var supplier by remember(editing?.id, suppliers) { mutableStateOf(suppliers.firstOrNull { it.id == editing?.supplierId }) }
    var warehouse by remember(editing?.id, warehouses) { mutableStateOf(warehouses.firstOrNull { it.id == editing?.warehouseId } ?: warehouses.firstOrNull()) }
    var paymentType by remember(editing?.id) { mutableStateOf(editing?.paymentType ?: PurchasePaymentType.CASH) }
    var card by remember(editing?.id, cards) { mutableStateOf(cards.firstOrNull { it.id == editing?.bankCardId } ?: cards.firstOrNull()) }
    var invoice by remember(editing?.id) { mutableStateOf(editing?.invoiceNumber.orEmpty()) }
    var discount by remember(editing?.id) { mutableStateOf(editing?.discountAmount?.toString().orEmpty()) }
    var date by remember(editing?.id) { mutableLongStateOf(editing?.date ?: PersianDateFormatter.todayStartMillis()) }
    var notes by remember(editing?.id) { mutableStateOf(editing?.notes.orEmpty()) }
    var error by remember { mutableStateOf<String?>(null) }
    val rows = remember(editing?.id) {
        mutableStateListOf<PurchaseRow>().apply {
            if (editingItems.isEmpty()) {
                add(PurchaseRow())
            } else {
                addAll(editingItems.map { item ->
                    PurchaseRow(
                        localId = item.id,
                        materialId = item.materialId,
                        quantity = item.quantity.toString(),
                        unitPrice = item.unitPrice.toString()
                    )
                })
            }
        }
    }

    val editingMaterialIds = editingItems.map { it.materialId }.toSet()
    val activeMaterials = state.snapshot.materials.filter { it.isActive || it.id in editingMaterialIds }
    val setupReady = warehouses.isNotEmpty() && activeMaterials.isNotEmpty()
    val inputs = rows.mapNotNull { row ->
        val material = activeMaterials.firstOrNull { it.id == row.materialId }
        val qty = NumberFormatter.normalizeDigits(row.quantity).toDoubleOrNull() ?: 0.0
        val price = MoneyFormatter.parse(row.unitPrice)
        if (material != null && qty > 0 && price > 0) PurchaseItemInput(material.id, qty, material.mainUnit, price) else null
    }
    val hasIncompleteRows = rows.any { row ->
        val touched = row.materialId != null || row.quantity.isNotBlank() || row.unitPrice.isNotBlank()
        val qty = NumberFormatter.normalizeDigits(row.quantity).toDoubleOrNull() ?: 0.0
        val price = MoneyFormatter.parse(row.unitPrice)
        touched && (row.materialId == null || qty <= 0.0 || price <= 0)
    }
    val subtotal = inputs.sumOf { it.totalAmount }
    val discountAmount = MoneyFormatter.parse(discount)
    val total = (subtotal - discountAmount).coerceAtLeast(0)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { SectionHeader(if (editing == null) "ثبت فاکتور خرید" else "ویرایش فاکتور خرید") }
        if (!setupReady) {
            item {
                EmptyState(
                    "پیش‌نیاز خرید کامل نیست",
                    "برای ثبت فاکتور، ابتدا حداقل یک انبار و یک متریال فعال بسازید."
                )
            }
        }
        item {
            GlassCard(Modifier.fillMaxWidth(), accent = Gold) {
                OptionSelector(
                    "تامین‌کننده",
                    suppliers,
                    supplier,
                    { it.name },
                    clearLabel = "بدون تامین‌کننده",
                    onClear = { supplier = null }
                ) { supplier = it }
                Spacer(Modifier.height(10.dp))
                GoldPrimaryButton("افزودن تامین‌کننده", onClick = onAddSupplier, icon = Icons.Outlined.Add)
                Spacer(Modifier.height(10.dp))
                OptionSelector("انبار مقصد", warehouses, warehouse, { it.name }) { warehouse = it }
                Spacer(Modifier.height(10.dp))
                FilterChipRow(
                    options = PurchasePaymentType.entries.map { it.label() },
                    selected = paymentType.label(),
                    onSelected = { label -> paymentType = PurchasePaymentType.entries.first { it.label() == label } }
                )
                if (paymentType == PurchasePaymentType.CARD) {
                    Spacer(Modifier.height(10.dp))
                    OptionSelector("کارت بانکی", cards, card, { it.title }) { card = it }
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    DarkOutlinedTextField(invoice, { invoice = it }, "شماره فاکتور", modifier = Modifier.weight(1f))
                    MoneyField(discount, { discount = it }, "تخفیف", modifier = Modifier.weight(1f))
                }
                Spacer(Modifier.height(10.dp))
                LocalDateSelector("تاریخ", date, { date = it })
                Spacer(Modifier.height(10.dp))
                DarkOutlinedTextField(notes, { notes = it }, "توضیحات", singleLine = false)
            }
        }
        item { SectionHeader("آیتم‌های فاکتور") }
        items(rows, key = { it.localId }) { row ->
            PurchaseItemRow(
                materials = activeMaterials,
                row = row,
                onChange = { updated ->
                    val index = rows.indexOfFirst { it.localId == row.localId }
                    if (index >= 0) rows[index] = updated
                },
                canRemove = rows.size > 1,
                onRemove = { rows.remove(row) }
            )
        }
        item { GoldPrimaryButton("افزودن آیتم", onClick = { rows.add(PurchaseRow()) }, icon = Icons.Outlined.Add) }
        item {
            GlassCard(Modifier.fillMaxWidth(), accent = Gold) {
                Text("مبلغ نهایی فاکتور", color = TextSecondary)
                MoneyText(total, style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(6.dp))
                Text("تخفیف: ${MoneyFormatter.format(MoneyFormatter.parse(discount))}", color = TextMuted)
                Text("وضعیت پرداخت: ${paymentType.label()}", color = if (paymentType == PurchasePaymentType.CREDIT) AppOrange else AppGreen)
            }
        }
        if (error != null) item { Text(error.orEmpty(), color = AppRed) }
        item {
            FormActionFooter(
                text = "ثبت و ذخیره فاکتور",
                icon = Icons.Outlined.Save,
                enabled = setupReady,
                onClick = {
                    error = when {
                        warehouse == null -> "انبار مقصد را انتخاب کنید"
                        paymentType == PurchasePaymentType.CREDIT && supplier == null -> "برای خرید نسیه باید تامین‌کننده را انتخاب کنید"
                        inputs.isEmpty() -> "حداقل یک آیتم معتبر وارد کنید"
                        hasIncompleteRows -> "همه ردیف‌های تکمیل‌شده باید متریال، مقدار و قیمت معتبر داشته باشند"
                        discountAmount > subtotal -> "تخفیف نمی‌تواند بیشتر از جمع آیتم‌ها باشد"
                        paymentType == PurchasePaymentType.CARD && card == null -> "کارت بانکی را انتخاب کنید"
                        else -> null
                    }
                    val wh = warehouse
                    if (error == null && wh != null) {
                        onSave(
                            PurchaseInput(
                                id = editing?.id ?: 0,
                                supplierId = supplier?.id,
                                warehouseId = wh.id,
                                date = date,
                                invoiceNumber = invoice,
                                paymentType = paymentType,
                                bankCardId = if (paymentType == PurchasePaymentType.CARD) card?.id else null,
                                discountAmount = discountAmount,
                                notes = notes,
                                items = inputs
                            )
                        )
                    }
                }
            )
        }
        item { Spacer(Modifier.height(20.dp)) }
    }
}

@Composable
private fun PurchaseItemRow(
    materials: List<MaterialEntity>,
    row: PurchaseRow,
    onChange: (PurchaseRow) -> Unit,
    canRemove: Boolean,
    onRemove: () -> Unit
) {
    val selected = materials.firstOrNull { it.id == row.materialId }
    GlassCard(Modifier.fillMaxWidth(), accent = AppCyan) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
            Text(selected?.imageEmoji ?: "•", style = MaterialTheme.typography.headlineMedium)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OptionSelector("متریال", materials, selected, { it.name }) {
                    onChange(row.copy(materialId = it.id))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    QuantityField(row.quantity, { onChange(row.copy(quantity = it)) }, "مقدار", Modifier.weight(1f))
                    MoneyField(row.unitPrice, { onChange(row.copy(unitPrice = it)) }, "قیمت واحد", Modifier.weight(1f))
                }
                Text("واحد: ${selected?.mainUnit?.label() ?: "-"}", color = TextSecondary)
            }
            if (canRemove) {
                IconButton(onClick = onRemove) {
                    Icon(Icons.Outlined.Delete, contentDescription = "حذف", tint = AppRed)
                }
            }
        }
    }
}

private data class PurchaseRow(
    val localId: Long = System.nanoTime(),
    val materialId: Long? = null,
    val quantity: String = "",
    val unitPrice: String = ""
)

@Composable
fun SupplierFormScreen(
    state: AppUiState,
    supplierId: Long?,
    onSave: (SupplierEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val editing = state.snapshot.suppliers.firstOrNull { it.id == supplierId }
    var name by remember(editing?.id) { mutableStateOf(editing?.name.orEmpty()) }
    var phone by remember(editing?.id) { mutableStateOf(editing?.phone.orEmpty()) }
    var address by remember(editing?.id) { mutableStateOf(editing?.address.orEmpty()) }
    var notes by remember(editing?.id) { mutableStateOf(editing?.notes.orEmpty()) }
    var isActive by remember(editing?.id) { mutableStateOf(editing?.isActive ?: true) }
    var error by remember { mutableStateOf<String?>(null) }
    val now = PersianDateFormatter.nowMillis()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { SectionHeader(if (editing == null) "افزودن تامین‌کننده" else "ویرایش تامین‌کننده") }
        item {
            GlassCard(Modifier.fillMaxWidth(), accent = Gold) {
                DarkOutlinedTextField(name, { name = it }, "نام تامین‌کننده")
                Spacer(Modifier.height(10.dp))
                DarkOutlinedTextField(phone, { phone = it }, "شماره تماس", keyboardType = KeyboardType.Phone)
                Spacer(Modifier.height(10.dp))
                DarkOutlinedTextField(address, { address = it }, "آدرس", singleLine = false)
                Spacer(Modifier.height(10.dp))
                DarkOutlinedTextField(notes, { notes = it }, "توضیحات", singleLine = false)
                Spacer(Modifier.height(10.dp))
                FilterChipRow(
                    options = listOf("فعال", "غیرفعال"),
                    selected = if (isActive) "فعال" else "غیرفعال",
                    onSelected = { isActive = it == "فعال" }
                )
            }
        }
        if (error != null) item { Text(error.orEmpty(), color = AppRed) }
        item {
            FormActionFooter(
                text = "ذخیره تامین‌کننده",
                onClick = {
                    error = if (name.isBlank()) "نام تامین‌کننده الزامی است" else null
                    if (error == null) {
                        onSave(
                            SupplierEntity(
                                id = editing?.id ?: 0,
                                name = name,
                                phone = phone,
                                address = address,
                                notes = notes,
                                isActive = isActive,
                                createdAt = editing?.createdAt ?: now,
                                updatedAt = now
                            )
                        )
                    }
                },
                icon = Icons.Outlined.Save
            )
        }
        item { Spacer(Modifier.height(20.dp)) }
    }
}
