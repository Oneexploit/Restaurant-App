package com.restaurant.offlinemanager.ui.warehouse

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Inventory
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
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
import com.restaurant.offlinemanager.core.design.OptionSelector
import com.restaurant.offlinemanager.core.design.QuantityField
import com.restaurant.offlinemanager.core.design.SectionHeader
import com.restaurant.offlinemanager.core.design.StatusChip
import com.restaurant.offlinemanager.core.design.TextMuted
import com.restaurant.offlinemanager.core.design.TextPrimary
import com.restaurant.offlinemanager.core.design.TextSecondary
import com.restaurant.offlinemanager.core.utils.MoneyFormatter
import com.restaurant.offlinemanager.core.utils.NumberFormatter
import com.restaurant.offlinemanager.core.utils.PersianDateFormatter
import com.restaurant.offlinemanager.data.local.entity.MaterialEntity
import com.restaurant.offlinemanager.data.local.entity.StockReason
import com.restaurant.offlinemanager.data.local.entity.StockTransactionType
import com.restaurant.offlinemanager.data.local.entity.UnitType
import com.restaurant.offlinemanager.data.local.entity.WarehouseEntity
import com.restaurant.offlinemanager.data.local.entity.WarehouseType
import com.restaurant.offlinemanager.domain.model.InventoryItem
import com.restaurant.offlinemanager.domain.model.StockTransactionInput
import com.restaurant.offlinemanager.domain.model.label
import com.restaurant.offlinemanager.ui.AppUiState

@Composable
fun WarehouseMainScreen(
    state: AppUiState,
    onStockIn: () -> Unit,
    onStockOut: () -> Unit,
    onTransfer: () -> Unit,
    onAddWarehouse: () -> Unit,
    onAddMaterial: () -> Unit,
    modifier: Modifier = Modifier
) {
    var tab by remember { mutableIntStateOf(0) }
    val tabs = listOf("موجودی کالا", "انبارها", "تراکنش‌ها", "متریال‌ها")
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp)
    ) {
        ScrollableTabRow(
            selectedTabIndex = tab,
            containerColor = com.restaurant.offlinemanager.core.design.SurfaceGlass,
            contentColor = Gold,
            edgePadding = 0.dp
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(selected = tab == index, onClick = { tab = index }, text = { Text(title) })
            }
        }
        Spacer(Modifier.height(12.dp))
        when (tab) {
            0 -> InventoryTab(state, onStockIn, onStockOut, onTransfer)
            1 -> WarehousesTab(state, onAddWarehouse)
            2 -> TransactionsTab(state)
            3 -> MaterialsTab(state, onAddMaterial)
        }
    }
}

@Composable
private fun InventoryTab(
    state: AppUiState,
    onStockIn: () -> Unit,
    onStockOut: () -> Unit,
    onTransfer: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val items = state.inventory.filter { query.isBlank() || it.materialName.contains(query) || it.warehouseName.contains(query) }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { AppSearchBar(query, { query = it }, label = "جستجو در موجودی") }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GoldPrimaryButton("ورود کالا", onClick = onStockIn, icon = Icons.Outlined.Add, modifier = Modifier.weight(1f))
                GoldPrimaryButton("خروج کالا", onClick = onStockOut, icon = Icons.Outlined.Inventory, modifier = Modifier.weight(1f))
            }
        }
        item { GoldPrimaryButton("انتقال کالا", onClick = onTransfer, icon = Icons.Outlined.Inventory) }
        if (items.isEmpty()) {
            item { EmptyState("موجودی خالی است", "پس از ثبت خرید یا ورود کالا، موجودی اینجا نمایش داده می‌شود.") }
        } else {
            items(items, key = { "${it.warehouseId}-${it.materialId}" }) { item ->
                InventoryCard(item)
            }
        }
        item { Spacer(Modifier.height(20.dp)) }
    }
}

@Composable
private fun InventoryCard(item: InventoryItem) {
    GlassCard(Modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(item.emoji ?: "•", style = MaterialTheme.typography.headlineMedium)
            Column(Modifier.weight(1f)) {
                Text(item.materialName, color = TextPrimary, style = MaterialTheme.typography.titleLarge)
                Text(item.warehouseName, color = TextSecondary)
                Text("${NumberFormatter.format(item.quantity)} ${item.unit.label()}", color = TextPrimary)
                Text("ارزش تقریبی: ${MoneyFormatter.format(item.approximateValue)}", color = TextMuted)
            }
            StatusChip(if (item.isLowStock) "کمبود" else "عادی", if (item.isLowStock) AppOrange else AppGreen)
        }
    }
}

@Composable
private fun WarehousesTab(state: AppUiState, onAddWarehouse: () -> Unit) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { GoldPrimaryButton("افزودن انبار", onClick = onAddWarehouse, icon = Icons.Outlined.Add) }
        items(state.snapshot.warehouses, key = { it.id }) { warehouse ->
            val inventory = state.inventory.filter { it.warehouseId == warehouse.id }
            GlassCard(Modifier.fillMaxWidth()) {
                Text(warehouse.name, color = TextPrimary, style = MaterialTheme.typography.titleLarge)
                Text(warehouse.type.label(), color = TextSecondary)
                Text("تعداد کالا: ${NumberFormatter.format(inventory.count())}", color = TextSecondary)
                Text("ارزش موجودی: ${MoneyFormatter.format(inventory.sumOf { it.approximateValue })}", color = Gold)
            }
        }
    }
}

@Composable
private fun TransactionsTab(state: AppUiState) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(state.snapshot.stockTransactions.take(60), key = { it.id }) { tx ->
            val warehouse = state.snapshot.warehouses.firstOrNull { it.id == tx.warehouseId }
            val material = state.snapshot.materials.firstOrNull { it.id == tx.materialId }
            GlassCard(Modifier.fillMaxWidth()) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(Modifier.weight(1f)) {
                        Text(material?.name ?: "کالای حذف‌شده", color = TextPrimary, style = MaterialTheme.typography.titleLarge)
                        Text("${warehouse?.name.orEmpty()} • ${PersianDateFormatter.format(tx.date)}", color = TextSecondary)
                        Text("${NumberFormatter.format(tx.quantity)} ${tx.unit.label()}", color = TextSecondary)
                    }
                    StatusChip(tx.type.label(), if (tx.type == StockTransactionType.IN || tx.type == StockTransactionType.TRANSFER_IN) AppGreen else AppOrange)
                }
            }
        }
    }
}

@Composable
private fun MaterialsTab(state: AppUiState, onAddMaterial: () -> Unit) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { GoldPrimaryButton("افزودن متریال", onClick = onAddMaterial, icon = Icons.Outlined.Add) }
        items(state.snapshot.materials, key = { it.id }) { material ->
            val category = state.snapshot.materialCategories.firstOrNull { it.id == material.categoryId }
            GlassCard(Modifier.fillMaxWidth()) {
                Text("${material.imageEmoji ?: "•"} ${material.name}", color = TextPrimary, style = MaterialTheme.typography.titleLarge)
                Text("واحد اصلی: ${material.mainUnit.label()}", color = TextSecondary)
                Text("حداقل موجودی: ${NumberFormatter.format(material.minimumStock)}", color = TextSecondary)
                Text("دسته‌بندی: ${category?.name ?: "بدون دسته"}", color = TextMuted)
            }
        }
    }
}

@Composable
fun StockTransactionFormScreen(
    state: AppUiState,
    mode: StockTransactionType,
    onSave: (List<StockTransactionInput>) -> Unit,
    modifier: Modifier = Modifier
) {
    val warehouses = state.snapshot.warehouses.filter { it.isActive }
    val materials = state.snapshot.materials.filter { it.isActive }
    var fromWarehouse by remember { mutableStateOf(warehouses.firstOrNull()) }
    var toWarehouse by remember { mutableStateOf(warehouses.drop(1).firstOrNull() ?: warehouses.firstOrNull()) }
    var material by remember { mutableStateOf(materials.firstOrNull()) }
    var quantity by remember { mutableStateOf("") }
    var unitPrice by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { SectionHeader(mode.label()) }
        item { OptionSelector(if (mode == StockTransactionType.TRANSFER_OUT) "انبار مبدا" else "انبار", warehouses, fromWarehouse, { it.name }) { fromWarehouse = it } }
        if (mode == StockTransactionType.TRANSFER_OUT) {
            item { OptionSelector("انبار مقصد", warehouses, toWarehouse, { it.name }) { toWarehouse = it } }
        }
        item { OptionSelector("متریال", materials, material, { it.name }) { material = it } }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuantityField(quantity, { quantity = it }, "مقدار", Modifier.weight(1f))
                MoneyField(unitPrice, { unitPrice = it }, "قیمت واحد", Modifier.weight(1f))
            }
        }
        item { DarkOutlinedTextField(notes, { notes = it }, "توضیحات", singleLine = false) }
        if (error != null) item { Text(error.orEmpty(), color = AppRed) }
        item {
            GoldPrimaryButton(
                text = "ثبت",
                icon = Icons.Outlined.Save,
                onClick = {
                    val qty = NumberFormatter.normalizeDigits(quantity).toDoubleOrNull() ?: 0.0
                    val price = MoneyFormatter.parse(unitPrice).takeIf { it > 0 }
                    error = when {
                        fromWarehouse == null -> "انبار را انتخاب کنید"
                        material == null -> "متریال را انتخاب کنید"
                        qty <= 0.0 -> "مقدار باید بیشتر از صفر باشد"
                        mode == StockTransactionType.TRANSFER_OUT && toWarehouse == null -> "انبار مقصد را انتخاب کنید"
                        mode == StockTransactionType.TRANSFER_OUT && toWarehouse?.id == fromWarehouse?.id -> "انبار مبدا و مقصد نمی‌تواند یکسان باشد"
                        else -> null
                    }
                    val wh = fromWarehouse
                    val mat = material
                    if (error == null && wh != null && mat != null) {
                        val date = PersianDateFormatter.todayStartMillis()
                        val input = StockTransactionInput(
                            warehouseId = wh.id,
                            materialId = mat.id,
                            projectId = null,
                            supplierId = null,
                            type = mode,
                            reason = if (mode == StockTransactionType.WASTE) StockReason.WASTE else if (mode == StockTransactionType.TRANSFER_OUT) StockReason.TRANSFER else StockReason.MANUAL_ADJUSTMENT,
                            quantity = qty,
                            unit = mat.mainUnit,
                            unitPrice = price,
                            date = date,
                            notes = notes
                        )
                        if (mode == StockTransactionType.TRANSFER_OUT && toWarehouse != null) {
                            onSave(
                                listOf(
                                    input,
                                    input.copy(
                                        warehouseId = toWarehouse!!.id,
                                        type = StockTransactionType.TRANSFER_IN
                                    )
                                )
                            )
                        } else {
                            onSave(listOf(input))
                        }
                    }
                }
            )
        }
        item { Spacer(Modifier.height(20.dp)) }
    }
}

@Composable
fun WarehouseFormScreen(
    onSave: (WarehouseEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(WarehouseType.GENERAL) }
    var address by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val now = PersianDateFormatter.nowMillis()
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { SectionHeader("افزودن انبار") }
        item { DarkOutlinedTextField(name, { name = it }, "نام انبار") }
        item { OptionSelector("نوع انبار", WarehouseType.entries, type, { it.label() }) { type = it } }
        item { DarkOutlinedTextField(address, { address = it }, "آدرس", singleLine = false) }
        item { DarkOutlinedTextField(notes, { notes = it }, "توضیحات", singleLine = false) }
        if (error != null) item { Text(error.orEmpty(), color = AppRed) }
        item {
            GoldPrimaryButton("ذخیره", onClick = {
                error = if (name.isBlank()) "نام انبار الزامی است" else null
                if (error == null) onSave(WarehouseEntity(name = name, type = type, address = address, notes = notes, isActive = true, createdAt = now, updatedAt = now))
            }, icon = Icons.Outlined.Save)
        }
    }
}

@Composable
fun MaterialFormScreen(
    state: AppUiState,
    onSave: (MaterialEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf(UnitType.KG) }
    var minStock by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf("🍽") }
    var category by remember { mutableStateOf(state.snapshot.materialCategories.firstOrNull()) }
    var error by remember { mutableStateOf<String?>(null) }
    val now = PersianDateFormatter.nowMillis()
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { SectionHeader("افزودن متریال") }
        item { DarkOutlinedTextField(name, { name = it }, "نام متریال") }
        item { OptionSelector("دسته‌بندی", state.snapshot.materialCategories, category, { it.name }) { category = it } }
        item { OptionSelector("واحد اصلی", UnitType.entries, unit, { it.label() }) { unit = it } }
        item { DarkOutlinedTextField(minStock, { minStock = it }, "حداقل موجودی", keyboardType = KeyboardType.Decimal) }
        item { DarkOutlinedTextField(emoji, { emoji = it }, "آیکن/ایموجی") }
        if (error != null) item { Text(error.orEmpty(), color = AppRed) }
        item {
            GoldPrimaryButton("ذخیره", onClick = {
                val minimum = NumberFormatter.normalizeDigits(minStock).toDoubleOrNull() ?: 0.0
                error = if (name.isBlank()) "نام متریال الزامی است" else null
                if (error == null) {
                    onSave(
                        MaterialEntity(
                            name = name,
                            categoryId = category?.id,
                            mainUnit = unit,
                            minimumStock = minimum,
                            imageEmoji = emoji,
                            isActive = true,
                            createdAt = now,
                            updatedAt = now
                        )
                    )
                }
            }, icon = Icons.Outlined.Save)
        }
    }
}
