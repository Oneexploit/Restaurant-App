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
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Inventory
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import com.restaurant.offlinemanager.core.design.MetricProgressBar
import com.restaurant.offlinemanager.core.design.MoneyField
import com.restaurant.offlinemanager.core.design.MoneyText
import com.restaurant.offlinemanager.core.design.MotionContent
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
import com.restaurant.offlinemanager.data.local.entity.StockReason
import com.restaurant.offlinemanager.data.local.entity.StockTransactionType
import com.restaurant.offlinemanager.data.local.entity.UnitType
import com.restaurant.offlinemanager.data.local.entity.WarehouseEntity
import com.restaurant.offlinemanager.data.local.entity.WarehouseType
import com.restaurant.offlinemanager.domain.model.InventoryItem
import com.restaurant.offlinemanager.domain.model.StockTransactionInput
import com.restaurant.offlinemanager.domain.model.label
import com.restaurant.offlinemanager.domain.usecase.InventoryIntegrityValidator
import com.restaurant.offlinemanager.ui.AppUiState
import java.time.Instant
import java.time.ZoneId

@Composable
fun WarehouseMainScreen(
    state: AppUiState,
    onStockIn: () -> Unit,
    onStockOut: () -> Unit,
    onTransfer: () -> Unit,
    onWaste: () -> Unit,
    onAdjustment: () -> Unit,
    onAddWarehouse: () -> Unit,
    onAddMaterial: () -> Unit,
    onEditWarehouse: (Long) -> Unit,
    onEditMaterial: (Long) -> Unit,
    onDeleteWarehouse: (Long) -> Unit,
    onDeleteMaterial: (Long) -> Unit,
    onDeleteStockTransaction: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var tab by remember { mutableIntStateOf(0) }
    val tabs = listOf("موجودی کالا", "انبارها", "تراکنش‌ها", "متریال‌ها")
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        FilterChipRow(tabs, tabs[tab], { tab = tabs.indexOf(it) })
        Spacer(Modifier.height(12.dp))
        MotionContent(targetState = tab, modifier = Modifier.weight(1f)) { selectedTab ->
            when (selectedTab) {
                0 -> InventoryTab(state, onStockIn, onStockOut, onTransfer, onWaste, onAdjustment)
                1 -> WarehousesTab(state, onAddWarehouse, onEditWarehouse, onDeleteWarehouse)
                2 -> TransactionsTab(state, onDeleteStockTransaction)
                else -> MaterialsTab(state, onAddMaterial, onEditMaterial, onDeleteMaterial)
            }
        }
    }
}

@Composable
private fun InventoryTab(
    state: AppUiState,
    onStockIn: () -> Unit,
    onStockOut: () -> Unit,
    onTransfer: () -> Unit,
    onWaste: () -> Unit,
    onAdjustment: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    var selectedWarehouse by remember { mutableStateOf(state.snapshot.warehouses.firstOrNull()?.name ?: "همه") }
    val warehouseOptions = listOf("همه") + state.snapshot.warehouses.map { it.name }
    val items = state.inventory.filter {
        (selectedWarehouse == "همه" || it.warehouseName == selectedWarehouse) &&
            (query.isBlank() || it.materialName.contains(query) || it.warehouseName.contains(query))
    }
    val lowStock = items.filter { it.isLowStock }
    val today = PersianDateFormatter.todayStartMillis()
    val todayTransactions = state.snapshot.stockTransactions.filter { it.date.isSameLocalDay(today) }
    val signedTodayTransactions = todayTransactions.map(InventoryIntegrityValidator::signedQuantity)
    val todayIn = signedTodayTransactions.filter { it > 0 }.sum()
    val todayOut = -signedTodayTransactions.filter { it < 0 }.sum()

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { AppSearchBar(query, { query = it }, label = "جستجو در موجودی") }
        item { FilterChipRow(warehouseOptions, selectedWarehouse, { selectedWarehouse = it }) }
        if (state.settings.lowStockNotificationsEnabled && lowStock.isNotEmpty()) {
            item {
                GlassCard(Modifier.fillMaxWidth(), accent = AppOrange) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Outlined.Warning, contentDescription = null, tint = AppOrange)
                        Column(Modifier.weight(1f)) {
                            Text("اقلام کالا با موجودی کم", color = AppOrange, style = MaterialTheme.typography.titleMedium)
                            Text("${NumberFormatter.format(lowStock.size)} قلم زیر حداقل موجودی قرار دارد", color = TextSecondary)
                        }
                    }
                }
            }
            item {
                ReorderSuggestionCard(lowStock.take(4), onStockIn)
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("ارزش کل انبار", MoneyFormatter.format(items.sumOf { it.approximateValue }), Icons.Outlined.AccountBalanceWallet, Gold, Modifier.weight(1f))
                StatCard("تعداد کالاها", NumberFormatter.format(items.size), Icons.Outlined.Inventory, AppCyan, Modifier.weight(1f), "قلم")
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("اقلام کم‌موجودی", NumberFormatter.format(lowStock.size), Icons.Outlined.Warning, AppOrange, Modifier.weight(1f), "هشدار")
                StatCard("کل تراکنش امروز", NumberFormatter.format(todayTransactions.size), Icons.Outlined.Inventory, AppGreen, Modifier.weight(1f), "ثبت")
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("ورود امروز", "${NumberFormatter.format(todayIn)}", Icons.Outlined.Inventory, Gold, Modifier.weight(1f), "واحد")
                StatCard("خروج امروز", "${NumberFormatter.format(todayOut)}", Icons.Outlined.Inventory, AppRed, Modifier.weight(1f), "واحد")
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GoldPrimaryButton("ورود کالا", onClick = onStockIn, icon = Icons.Outlined.Add, modifier = Modifier.weight(1f))
                GoldPrimaryButton("خروج کالا", onClick = onStockOut, icon = Icons.Outlined.Inventory, modifier = Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GoldPrimaryButton("انتقال", onClick = onTransfer, icon = Icons.Outlined.Inventory, modifier = Modifier.weight(1f))
                GoldPrimaryButton("ضایعات", onClick = onWaste, icon = Icons.Outlined.Warning, modifier = Modifier.weight(1f))
            }
        }
        item { GoldPrimaryButton("اصلاح موجودی", onClick = onAdjustment, icon = Icons.Outlined.Save) }
        if (items.isEmpty()) {
            item { EmptyState("موجودی خالی است", "پس از ثبت خرید یا ورود کالا، موجودی اینجا نمایش داده می‌شود.") }
        } else {
            items(items, key = { "${it.warehouseId}-${it.materialId}" }) { item ->
                InventoryCard(item, onStockIn, onStockOut)
            }
        }
        item { Spacer(Modifier.height(20.dp)) }
    }
}

@Composable
private fun InventoryCard(item: InventoryItem, onStockIn: () -> Unit, onStockOut: () -> Unit) {
    val accent by animateColorAsState(
        targetValue = if (item.isLowStock) AppOrange else AppCyan,
        animationSpec = tween(420),
        label = "inventoryRiskColor"
    )
    GlassCard(Modifier.fillMaxWidth(), accent = accent) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(item.emoji ?: "•", style = MaterialTheme.typography.headlineMedium)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(item.materialName, color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                Text(item.warehouseName, color = TextSecondary)
                Text("موجودی: ${NumberFormatter.format(item.quantity)} ${item.unit.label()}", color = TextPrimary)
                Text("حداقل: ${NumberFormatter.format(item.minimumStock)} ${item.unit.label()}", color = if (item.isLowStock) AppOrange else TextMuted)
                Text("میانگین بها: ${MoneyFormatter.format(item.averageUnitCost)}", color = TextMuted)
            }
            Column(horizontalAlignment = Alignment.End) {
                StatusChip(if (item.isLowStock) "کمبود" else "عادی", if (item.isLowStock) AppOrange else AppGreen)
                MoneyText(item.approximateValue, style = MaterialTheme.typography.titleMedium)
            }
        }
        Spacer(Modifier.height(10.dp))
        MetricProgressBar(
            label = "موجودی نسبت به حداقل",
            value = item.quantity.toFloat(),
            max = item.minimumStock.toFloat().coerceAtLeast(1f),
            accent = if (item.isLowStock) AppOrange else AppGreen,
            valueLabel = "${NumberFormatter.format(item.quantity)} / ${NumberFormatter.format(item.minimumStock)}"
        )
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SecondaryGlassButton("ورود", onClick = onStockIn, modifier = Modifier.weight(1f), accent = AppGreen)
            SecondaryGlassButton("خروج", onClick = onStockOut, modifier = Modifier.weight(1f), accent = if (item.isLowStock) AppOrange else AppCyan)
        }
    }
}

@Composable
private fun ReorderSuggestionCard(items: List<InventoryItem>, onStockIn: () -> Unit) {
    GlassCard(Modifier.fillMaxWidth(), accent = AppOrange) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(Icons.Outlined.Warning, contentDescription = null, tint = AppOrange)
            Column(Modifier.weight(1f)) {
                Text("پیشنهاد ورود کالا", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                Text("این اقلام زودتر از بقیه به شارژ موجودی نیاز دارند.", color = TextSecondary)
            }
            StatusChip("${NumberFormatter.format(items.size)} قلم", AppOrange)
        }
        Spacer(Modifier.height(10.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items.forEach { item ->
                val needed = (item.minimumStock - item.quantity).coerceAtLeast(0.0)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(item.emoji ?: "•", style = MaterialTheme.typography.titleLarge)
                    Column(Modifier.weight(1f)) {
                        Text(item.materialName, color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
                        Text("${item.warehouseName} • نیاز تقریبی ${NumberFormatter.format(needed)} ${item.unit.label()}", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        SecondaryGlassButton("ثبت ورود کالا", onClick = onStockIn, icon = Icons.Outlined.Add, accent = AppOrange)
    }
}

@Composable
private fun WarehousesTab(
    state: AppUiState,
    onAddWarehouse: () -> Unit,
    onEditWarehouse: (Long) -> Unit,
    onDeleteWarehouse: (Long) -> Unit
) {
    var deleteId by remember { mutableStateOf<Long?>(null) }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { GoldPrimaryButton("افزودن انبار", onClick = onAddWarehouse, icon = Icons.Outlined.Add) }
        if (state.snapshot.warehouses.isEmpty()) {
            item { EmptyState("انباری ثبت نشده", "برای مدیریت موجودی، اولین انبار را اضافه کنید.") }
        } else {
            items(state.snapshot.warehouses, key = { it.id }) { warehouse ->
                val inventory = state.inventory.filter { it.warehouseId == warehouse.id }
                GlassCard(Modifier.fillMaxWidth(), accent = Gold) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Inventory, contentDescription = null, tint = Gold)
                        Column(Modifier.weight(1f)) {
                            Text(warehouse.name, color = TextPrimary, style = MaterialTheme.typography.titleLarge)
                            Text(warehouse.type.label(), color = TextSecondary)
                            Text(warehouse.address.orEmpty(), color = TextMuted)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("${NumberFormatter.format(inventory.count())} قلم", color = TextSecondary)
                            MoneyText(inventory.sumOf { it.approximateValue }, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SecondaryGlassButton("ویرایش", onClick = { onEditWarehouse(warehouse.id) }, icon = Icons.Outlined.Save, accent = AppCyan, modifier = Modifier.weight(1f))
                        DangerButton("حذف", onClick = { deleteId = warehouse.id }, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
    deleteId?.let { id ->
        ConfirmDialog(
            title = "حذف انبار",
            message = "اگر این انبار در فاکتور یا تراکنش استفاده شده باشد، برای حفظ سوابق فقط غیرفعال می‌شود.",
            confirmText = "حذف",
            onConfirm = {
                deleteId = null
                onDeleteWarehouse(id)
            },
            onDismiss = { deleteId = null }
        )
    }
}

@Composable
private fun TransactionsTab(state: AppUiState, onDeleteStockTransaction: (Long) -> Unit) {
    var deleteId by remember { mutableStateOf<Long?>(null) }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        val transactions = state.snapshot.stockTransactions.take(60)
        if (transactions.isEmpty()) {
            item { EmptyState("تراکنشی ثبت نشده", "ورود، خروج یا انتقال کالا بعد از ثبت در اینجا دیده می‌شود.") }
        } else {
            items(transactions, key = { it.id }) { tx ->
                val warehouse = state.snapshot.warehouses.firstOrNull { it.id == tx.warehouseId }
                val material = state.snapshot.materials.firstOrNull { it.id == tx.materialId }
                val signedQuantity = InventoryIntegrityValidator.signedQuantity(tx)
                val positive = signedQuantity > 0
                GlassCard(Modifier.fillMaxWidth(), accent = if (positive) AppGreen else AppOrange) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Inventory, contentDescription = null, tint = if (positive) AppGreen else AppOrange)
                        Column(Modifier.weight(1f)) {
                            Text(material?.name ?: "کالای حذف‌شده", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                            Text("${warehouse?.name.orEmpty()} • ${tx.type.label()}", color = TextSecondary)
                            PersianDateText(tx.date)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            StatusChip(tx.reason.label(), if (positive) AppGreen else AppOrange)
                            Text(
                                "${if (signedQuantity > 0) "+" else ""}${NumberFormatter.format(signedQuantity)} ${tx.unit.label()}",
                                color = if (positive) AppGreen else AppOrange
                            )
                        }
                    }
                    if (tx.purchaseId == null) {
                        Spacer(Modifier.height(8.dp))
                        DangerButton("حذف تراکنش", onClick = { deleteId = tx.id })
                    }
                }
            }
        }
    }
    deleteId?.let { id ->
        ConfirmDialog(
            title = "حذف تراکنش انبار",
            message = "این تراکنش از محاسبه موجودی حذف می‌شود.",
            confirmText = "حذف",
            onConfirm = {
                deleteId = null
                onDeleteStockTransaction(id)
            },
            onDismiss = { deleteId = null }
        )
    }
}

@Composable
private fun MaterialsTab(
    state: AppUiState,
    onAddMaterial: () -> Unit,
    onEditMaterial: (Long) -> Unit,
    onDeleteMaterial: (Long) -> Unit
) {
    var deleteId by remember { mutableStateOf<Long?>(null) }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            GoldPrimaryButton("افزودن متریال", onClick = onAddMaterial, icon = Icons.Outlined.Add)
        }
        if (state.snapshot.materials.isEmpty()) {
            item { EmptyState("متریالی ثبت نشده", "مواد اولیه و اقلام مصرفی را برای استفاده در خرید و انبار اضافه کنید.") }
        } else {
            items(state.snapshot.materials, key = { it.id }) { material ->
                GlassCard(Modifier.fillMaxWidth(), accent = AppCyan) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(material.imageEmoji ?: "•", style = MaterialTheme.typography.headlineMedium)
                        Column(Modifier.weight(1f)) {
                            Text(material.name, color = TextPrimary, style = MaterialTheme.typography.titleLarge)
                            Text("واحد اصلی: ${material.mainUnit.label()}", color = TextSecondary)
                            Text("حداقل موجودی: ${NumberFormatter.format(material.minimumStock)}", color = TextSecondary)
                        }
                        StatusChip(if (material.isActive) "فعال" else "غیرفعال", if (material.isActive) AppGreen else TextMuted)
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SecondaryGlassButton("ویرایش", onClick = { onEditMaterial(material.id) }, icon = Icons.Outlined.Save, accent = AppCyan, modifier = Modifier.weight(1f))
                        DangerButton("حذف", onClick = { deleteId = material.id }, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
    deleteId?.let { id ->
        ConfirmDialog(
            title = "حذف متریال",
            message = "اگر این متریال در خرید یا انبار استفاده شده باشد، برای حفظ سوابق فقط غیرفعال می‌شود.",
            confirmText = "حذف",
            onConfirm = {
                deleteId = null
                onDeleteMaterial(id)
            },
            onDismiss = { deleteId = null }
        )
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
    val projects = state.snapshot.projects.filter { it.status == com.restaurant.offlinemanager.data.local.entity.ProjectStatus.ACTIVE }
    var fromWarehouse by remember { mutableStateOf(warehouses.firstOrNull()) }
    var toWarehouse by remember { mutableStateOf(warehouses.drop(1).firstOrNull() ?: warehouses.firstOrNull()) }
    var material by remember { mutableStateOf(materials.firstOrNull()) }
    var project by remember { mutableStateOf<com.restaurant.offlinemanager.data.local.entity.ProjectEntity?>(null) }
    var quantity by remember { mutableStateOf("") }
    var unitPrice by remember { mutableStateOf("") }
    var date by remember { mutableLongStateOf(PersianDateFormatter.todayStartMillis()) }
    var notes by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val availableStock = state.inventory
        .firstOrNull { it.warehouseId == fromWarehouse?.id && it.materialId == material?.id }
        ?.quantity ?: 0.0
    val currentAverageCost = state.inventory
        .firstOrNull { it.warehouseId == fromWarehouse?.id && it.materialId == material?.id }
        ?.averageUnitCost ?: 0L
    val isOutbound = mode == StockTransactionType.OUT || mode == StockTransactionType.TRANSFER_OUT || mode == StockTransactionType.WASTE
    val setupReady = warehouses.isNotEmpty() && materials.isNotEmpty()

    LaunchedEffect(warehouses) {
        if (fromWarehouse == null || warehouses.none { it.id == fromWarehouse?.id }) {
            fromWarehouse = warehouses.firstOrNull()
        }
        if (toWarehouse == null || warehouses.none { it.id == toWarehouse?.id }) {
            toWarehouse = warehouses.drop(1).firstOrNull() ?: warehouses.firstOrNull()
        }
    }

    LaunchedEffect(materials) {
        if (material == null || materials.none { it.id == material?.id }) {
            material = materials.firstOrNull()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { SectionHeader(mode.label()) }
        if (!setupReady) {
            item {
                EmptyState(
                    "پیش‌نیاز ثبت تراکنش کامل نیست",
                    "برای ثبت انبار، ابتدا حداقل یک انبار فعال و یک متریال فعال بسازید."
                )
            }
        }
        item {
            GlassCard(Modifier.fillMaxWidth(), accent = Gold) {
                OptionSelector(if (mode == StockTransactionType.TRANSFER_OUT) "انبار مبدا" else "انبار", warehouses, fromWarehouse, { it.name }) { fromWarehouse = it }
                if (mode == StockTransactionType.TRANSFER_OUT) {
                    Spacer(Modifier.height(10.dp))
                    OptionSelector("انبار مقصد", warehouses, toWarehouse, { it.name }) { toWarehouse = it }
                }
                Spacer(Modifier.height(10.dp))
                OptionSelector("متریال", materials, material, { it.name }) { material = it }
                if (mode == StockTransactionType.OUT) {
                    Spacer(Modifier.height(10.dp))
                    OptionSelector(
                        "پروژه مصرف‌کننده (اختیاری)",
                        projects,
                        project,
                        { it.name },
                        clearLabel = "مصرف عمومی",
                        onClear = { project = null }
                    ) { project = it }
                }
                if (isOutbound) {
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("موجودی قابل خروج", color = TextSecondary, modifier = Modifier.weight(1f))
                        Text(
                            "${NumberFormatter.format(availableStock)} ${material?.mainUnit?.label().orEmpty()}",
                            color = if (availableStock <= 0.0) AppRed else TextPrimary,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    QuantityField(quantity, { quantity = it }, if (mode == StockTransactionType.ADJUSTMENT) "مقدار اصلاحی (+/-)" else "مقدار", Modifier.weight(1f))
                    MoneyField(unitPrice, { unitPrice = it }, "قیمت واحد", Modifier.weight(1f))
                }
                Spacer(Modifier.height(10.dp))
                LocalDateSelector("تاریخ", date, { date = it })
                Spacer(Modifier.height(10.dp))
                DarkOutlinedTextField(notes, { notes = it }, "یادداشت", singleLine = false)
            }
        }
        if (error != null) item { Text(error.orEmpty(), color = AppRed) }
        item {
            FormActionFooter(
                text = "ثبت تراکنش",
                icon = Icons.Outlined.Save,
                enabled = setupReady,
                onClick = {
                    val qty = NumberFormatter.normalizeDigits(quantity).toDoubleOrNull() ?: 0.0
                    val price = MoneyFormatter.parse(unitPrice).takeIf { it > 0 }
                    error = when {
                        fromWarehouse == null -> "انبار را انتخاب کنید"
                        material == null -> "متریال را انتخاب کنید"
                        mode == StockTransactionType.ADJUSTMENT && qty == 0.0 -> "مقدار اصلاحی نمی‌تواند صفر باشد"
                        mode != StockTransactionType.ADJUSTMENT && qty <= 0.0 -> "مقدار باید بیشتر از صفر باشد"
                        mode == StockTransactionType.TRANSFER_OUT && toWarehouse == null -> "انبار مقصد را انتخاب کنید"
                        mode == StockTransactionType.TRANSFER_OUT && toWarehouse?.id == fromWarehouse?.id -> "انبار مبدا و مقصد نمی‌تواند یکسان باشد"
                        isOutbound && qty > availableStock -> "موجودی کافی نیست"
                        mode == StockTransactionType.ADJUSTMENT && availableStock + qty < 0.0 -> "اصلاح موجودی نمی‌تواند موجودی نهایی را منفی کند"
                        else -> null
                    }
                    val wh = fromWarehouse
                    val mat = material
                    if (error == null && wh != null && mat != null) {
                        val input = StockTransactionInput(
                            warehouseId = wh.id,
                            materialId = mat.id,
                            projectId = project?.id,
                            supplierId = null,
                            type = mode,
                            reason = if (mode == StockTransactionType.WASTE) StockReason.WASTE else if (mode == StockTransactionType.TRANSFER_OUT) StockReason.TRANSFER else StockReason.MANUAL_ADJUSTMENT,
                            quantity = qty,
                            unit = mat.mainUnit,
                            unitPrice = price ?: currentAverageCost.takeIf { it > 0 },
                            date = date,
                            notes = notes
                        )
                        if (mode == StockTransactionType.TRANSFER_OUT && toWarehouse != null) {
                            onSave(listOf(input, input.copy(warehouseId = toWarehouse!!.id, type = StockTransactionType.TRANSFER_IN)))
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
    state: AppUiState,
    warehouseId: Long?,
    onSave: (WarehouseEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val editing = state.snapshot.warehouses.firstOrNull { it.id == warehouseId }
    var name by remember(editing?.id) { mutableStateOf(editing?.name.orEmpty()) }
    var type by remember(editing?.id) { mutableStateOf(editing?.type ?: WarehouseType.GENERAL) }
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
        item { SectionHeader(if (editing == null) "افزودن انبار" else "ویرایش انبار") }
        item {
            GlassCard(Modifier.fillMaxWidth(), accent = Gold) {
                DarkOutlinedTextField(name, { name = it }, "نام انبار")
                Spacer(Modifier.height(10.dp))
                OptionSelector("نوع انبار", WarehouseType.entries, type, { it.label() }) { type = it }
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
            FormActionFooter("ذخیره", onClick = {
                error = if (name.isBlank()) "نام انبار الزامی است" else null
                if (error == null) {
                    onSave(
                        WarehouseEntity(
                            id = editing?.id ?: 0,
                            name = name,
                            type = type,
                            address = address,
                            notes = notes,
                            isActive = isActive,
                            createdAt = editing?.createdAt ?: now,
                            updatedAt = now
                        )
                    )
                }
            }, icon = Icons.Outlined.Save)
        }
    }
}

@Composable
fun MaterialFormScreen(
    state: AppUiState,
    materialId: Long?,
    onSave: (MaterialEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val editing = state.snapshot.materials.firstOrNull { it.id == materialId }
    var name by remember(editing?.id) { mutableStateOf(editing?.name.orEmpty()) }
    var unit by remember(editing?.id) { mutableStateOf(editing?.mainUnit ?: UnitType.KG) }
    var minStock by remember(editing?.id) { mutableStateOf(editing?.minimumStock?.toString().orEmpty()) }
    var emoji by remember(editing?.id) { mutableStateOf(editing?.imageEmoji ?: "🍽") }
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
        item { SectionHeader(if (editing == null) "افزودن متریال" else "ویرایش متریال") }
        item {
            GlassCard(Modifier.fillMaxWidth(), accent = Gold) {
                DarkOutlinedTextField(name, { name = it }, "نام متریال")
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OptionSelector("واحد", UnitType.entries, unit, { it.label() }, modifier = Modifier.weight(1f)) { unit = it }
                    DarkOutlinedTextField(minStock, { minStock = it }, "تعداد/حداقل", keyboardType = KeyboardType.Decimal, modifier = Modifier.weight(1f))
                }
                Spacer(Modifier.height(10.dp))
                DarkOutlinedTextField(emoji, { emoji = it }, "آیکن/ایموجی")
                Spacer(Modifier.height(10.dp))
                DarkOutlinedTextField(notes, { notes = it }, "توضیحات", singleLine = false)
                Spacer(Modifier.height(10.dp))
                FilterChipRow(
                    options = listOf("فعال", "غیرفعال"),
                    selected = if (isActive) "فعال" else "غیرفعال",
                    onSelected = { isActive = it == "فعال" }
                )
                Spacer(Modifier.height(10.dp))
                DarkOutlinedTextField(PersianDateFormatter.format(PersianDateFormatter.todayStartMillis()), {}, "تاریخ ثبت", readOnly = true)
            }
        }
        if (error != null) item { Text(error.orEmpty(), color = AppRed) }
        item {
            FormActionFooter("ثبت متریال", onClick = {
                val minimum = NumberFormatter.normalizeDigits(minStock).toDoubleOrNull() ?: 0.0
                error = when {
                    name.isBlank() -> "نام متریال الزامی است"
                    minimum < 0.0 -> "حداقل موجودی نمی‌تواند منفی باشد"
                    else -> null
                }
                if (error == null) {
                    onSave(
                        MaterialEntity(
                            id = editing?.id ?: 0,
                            name = name,
                            mainUnit = unit,
                            minimumStock = minimum,
                            imageEmoji = emoji,
                            notes = notes,
                            isActive = isActive,
                            createdAt = editing?.createdAt ?: now,
                            updatedAt = now
                        )
                    )
                }
            }, icon = Icons.Outlined.Save)
        }
    }
}

private fun Long.isSameLocalDay(dayStartMillis: Long): Boolean {
    val zone = ZoneId.systemDefault()
    val thisDay = Instant.ofEpochMilli(this).atZone(zone).toLocalDate()
    val targetDay = Instant.ofEpochMilli(dayStartMillis).atZone(zone).toLocalDate()
    return thisDay == targetDay
}
