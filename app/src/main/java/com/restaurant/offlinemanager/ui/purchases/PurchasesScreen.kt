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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.restaurant.offlinemanager.core.design.AppCyan
import com.restaurant.offlinemanager.core.design.AppGreen
import com.restaurant.offlinemanager.core.design.AppOrange
import com.restaurant.offlinemanager.core.design.AppRed
import com.restaurant.offlinemanager.core.design.AppSearchBar
import com.restaurant.offlinemanager.core.design.DarkOutlinedTextField
import com.restaurant.offlinemanager.core.design.EmptyState
import com.restaurant.offlinemanager.core.design.FilterChipRow
import com.restaurant.offlinemanager.core.design.GlassCard
import com.restaurant.offlinemanager.core.design.Gold
import com.restaurant.offlinemanager.core.design.GoldPrimaryButton
import com.restaurant.offlinemanager.core.design.MoneyField
import com.restaurant.offlinemanager.core.design.MoneyText
import com.restaurant.offlinemanager.core.design.OptionSelector
import com.restaurant.offlinemanager.core.design.PersianDateText
import com.restaurant.offlinemanager.core.design.QuantityField
import com.restaurant.offlinemanager.core.design.SectionHeader
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
import com.restaurant.offlinemanager.domain.model.PurchaseInput
import com.restaurant.offlinemanager.domain.model.PurchaseItemInput
import com.restaurant.offlinemanager.domain.model.label
import com.restaurant.offlinemanager.ui.AppUiState

@Composable
fun PurchasesListScreen(
    state: AppUiState,
    onAddPurchase: () -> Unit,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf("همه") }
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
        if (purchases.isEmpty()) {
            item { EmptyState("خریدی پیدا نشد", "برای شروع یک فاکتور روزانه ثبت کنید.") }
        } else {
            items(purchases, key = { it.id }) { purchase ->
                val supplier = state.snapshot.suppliers.firstOrNull { it.id == purchase.supplierId }
                val warehouse = state.snapshot.warehouses.firstOrNull { it.id == purchase.warehouseId }
                val purchaseItems = state.snapshot.purchaseItems.filter { it.purchaseId == purchase.id }
                val firstMaterial = state.snapshot.materials.firstOrNull { material -> purchaseItems.firstOrNull()?.materialId == material.id }
                GlassCard(Modifier.fillMaxWidth(), accent = if (purchase.paymentType == PurchasePaymentType.CREDIT) AppOrange else AppGreen) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(firstMaterial?.imageEmoji ?: "🧾", style = MaterialTheme.typography.headlineMedium)
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(supplier?.name ?: "بدون تامین‌کننده", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                            Text(firstMaterial?.name ?: "چند قلم کالا", color = TextSecondary)
                            Text("${warehouse?.name.orEmpty()} • ${NumberFormatter.format(purchaseItems.size)} آیتم", color = TextMuted)
                            PersianDateText(purchase.date)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            StatusChip(purchase.paymentType.label(), if (purchase.paymentType == PurchasePaymentType.CREDIT) AppOrange else AppGreen)
                            MoneyText(purchase.totalAmount, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(20.dp)) }
    }
}

@Composable
fun PurchaseFormScreen(
    state: AppUiState,
    onSave: (PurchaseInput) -> Unit,
    modifier: Modifier = Modifier
) {
    val suppliers = state.snapshot.suppliers.filter { it.isActive }
    val warehouses = state.snapshot.warehouses.filter { it.isActive }
    val cards = state.snapshot.bankCards.filter { it.isActive }
    var supplier by remember { mutableStateOf(suppliers.firstOrNull()) }
    var warehouse by remember { mutableStateOf(warehouses.firstOrNull()) }
    var paymentType by remember { mutableStateOf(PurchasePaymentType.CASH) }
    var card by remember { mutableStateOf(cards.firstOrNull()) }
    var invoice by remember { mutableStateOf("") }
    var discount by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val rows = remember { mutableStateListOf(PurchaseRow()) }

    val inputs = rows.mapNotNull { row ->
        val material = state.snapshot.materials.firstOrNull { it.id == row.materialId }
        val qty = NumberFormatter.normalizeDigits(row.quantity).toDoubleOrNull() ?: 0.0
        val price = MoneyFormatter.parse(row.unitPrice)
        if (material != null && qty > 0 && price > 0) PurchaseItemInput(material.id, qty, material.mainUnit, price) else null
    }
    val total = (inputs.sumOf { it.totalAmount } - MoneyFormatter.parse(discount)).coerceAtLeast(0)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { SectionHeader("ثبت فاکتور خرید") }
        item {
            GlassCard(Modifier.fillMaxWidth(), accent = Gold) {
                OptionSelector("تامین‌کننده", suppliers, supplier, { it.name }) { supplier = it }
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
                DarkOutlinedTextField(PersianDateFormatter.format(PersianDateFormatter.todayStartMillis()), {}, "تاریخ", readOnly = true)
                Spacer(Modifier.height(10.dp))
                DarkOutlinedTextField(notes, { notes = it }, "توضیحات", singleLine = false)
            }
        }
        item { SectionHeader("آیتم‌های فاکتور") }
        items(rows, key = { it.localId }) { row ->
            PurchaseItemRow(
                materials = state.snapshot.materials.filter { it.isActive },
                row = row,
                onChange = { updated ->
                    val index = rows.indexOfFirst { it.localId == row.localId }
                    if (index >= 0) rows[index] = updated
                },
                onRemove = { if (rows.size > 1) rows.remove(row) }
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
            GoldPrimaryButton(
                text = "ثبت و ذخیره فاکتور",
                icon = Icons.Outlined.Save,
                onClick = {
                    error = when {
                        warehouse == null -> "انبار مقصد را انتخاب کنید"
                        inputs.isEmpty() -> "حداقل یک آیتم معتبر وارد کنید"
                        paymentType == PurchasePaymentType.CARD && card == null -> "کارت بانکی را انتخاب کنید"
                        else -> null
                    }
                    val wh = warehouse
                    if (error == null && wh != null) {
                        onSave(
                            PurchaseInput(
                                supplierId = supplier?.id,
                                warehouseId = wh.id,
                                date = PersianDateFormatter.todayStartMillis(),
                                invoiceNumber = invoice,
                                paymentType = paymentType,
                                bankCardId = if (paymentType == PurchasePaymentType.CARD) card?.id else null,
                                discountAmount = MoneyFormatter.parse(discount),
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
            IconButton(onClick = onRemove) {
                Icon(Icons.Outlined.Delete, contentDescription = "حذف", tint = AppRed)
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
