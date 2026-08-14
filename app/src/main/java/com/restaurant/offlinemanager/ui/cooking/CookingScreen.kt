package com.restaurant.offlinemanager.ui.cooking

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.restaurant.offlinemanager.core.design.AppCyan
import com.restaurant.offlinemanager.core.design.AppGreen
import com.restaurant.offlinemanager.core.design.AppRed
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
import com.restaurant.offlinemanager.core.design.MoneyText
import com.restaurant.offlinemanager.core.design.OptionSelector
import com.restaurant.offlinemanager.core.design.QuantityField
import com.restaurant.offlinemanager.core.design.SectionHeader
import com.restaurant.offlinemanager.core.design.TextMuted
import com.restaurant.offlinemanager.core.design.TextPrimary
import com.restaurant.offlinemanager.core.design.TextSecondary
import com.restaurant.offlinemanager.core.utils.MoneyFormatter
import com.restaurant.offlinemanager.core.utils.NumberFormatter
import com.restaurant.offlinemanager.core.utils.PersianDateFormatter
import com.restaurant.offlinemanager.data.local.entity.MealType
import com.restaurant.offlinemanager.data.local.entity.ProjectStatus
import com.restaurant.offlinemanager.domain.model.CookingAllocationInput
import com.restaurant.offlinemanager.domain.model.CookingBatchInput
import com.restaurant.offlinemanager.domain.model.CookingMaterialInput
import com.restaurant.offlinemanager.domain.model.label
import com.restaurant.offlinemanager.ui.AppUiState

@Composable
fun CookingListScreen(
    state: AppUiState,
    onAdd: () -> Unit,
    onEdit: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var deleteId by remember { mutableStateOf<Long?>(null) }
    LazyColumn(modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { GoldPrimaryButton("ثبت پخت و مصرف", onClick = onAdd, icon = Icons.Outlined.RestaurantMenu) }
        if (state.snapshot.cookingBatches.isEmpty()) {
            item { EmptyState("مصرف پختی ثبت نشده", "مواد مصرف‌شده، تعداد غذای تولیدی و سهم شرکت‌ها را یکجا ثبت کنید.") }
        } else items(state.snapshot.cookingBatches, key = { it.id }) { batch ->
            val cost = state.cookingBatchCosts.firstOrNull { it.batchId == batch.id }
            val allocations = state.snapshot.cookingAllocations.filter { it.batchId == batch.id }
            GlassCard(Modifier.fillMaxWidth(), accent = AppGreen) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Outlined.RestaurantMenu, null, tint = AppGreen)
                    Column(Modifier.weight(1f)) {
                        Text("${batch.mealType.label()} • ${NumberFormatter.format(batch.producedQuantity)} پرس", color = TextPrimary, style = MaterialTheme.typography.titleLarge)
                        Text(PersianDateFormatter.format(batch.date), color = TextSecondary)
                        Text("${NumberFormatter.format(allocations.size)} شرکت • هزینه هر پرس ${MoneyFormatter.format(cost?.costPerMeal ?: 0)}", color = TextMuted)
                    }
                    MoneyText(cost?.totalCost ?: 0)
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GoldPrimaryButton("ویرایش", { onEdit(batch.id) }, modifier = Modifier.weight(1f), icon = Icons.Outlined.Edit)
                    DangerButton("حذف", { deleteId = batch.id }, Modifier.weight(1f))
                }
            }
        }
        item { Spacer(Modifier.height(20.dp)) }
    }
    deleteId?.let { id ->
        ConfirmDialog("حذف ثبت پخت", "مصرف مواد این پخت به انبار بازگردانده می‌شود.", "حذف", {
            deleteId = null
            onDelete(id)
        }, { deleteId = null })
    }
}

private data class MaterialRow(val key: Long = System.nanoTime(), val materialId: Long? = null, val quantity: String = "")
private data class AllocationRow(val key: Long = System.nanoTime(), val projectId: Long? = null, val quantity: String = "")

@Composable
fun CookingFormScreen(
    state: AppUiState,
    batchId: Long?,
    onSave: (CookingBatchInput) -> Unit,
    modifier: Modifier = Modifier
) {
    val editing = state.snapshot.cookingBatches.firstOrNull { it.id == batchId }
    val existingMaterials = state.snapshot.stockTransactions.filter { it.cookingBatchId == batchId }
    val existingAllocations = state.snapshot.cookingAllocations.filter { it.batchId == batchId }
    val warehouses = state.snapshot.warehouses.filter { it.isActive || it.id == editing?.warehouseId }
    val materials = state.snapshot.materials.filter { it.isActive || existingMaterials.any { tx -> tx.materialId == it.id } }
    val projects = state.snapshot.projects.filter { it.status == ProjectStatus.ACTIVE || existingAllocations.any { row -> row.projectId == it.id } }
    var warehouse by remember(editing?.id, warehouses) { mutableStateOf(warehouses.firstOrNull { it.id == editing?.warehouseId } ?: warehouses.firstOrNull()) }
    var mealType by remember(editing?.id) { mutableStateOf(editing?.mealType ?: MealType.LUNCH) }
    var produced by remember(editing?.id) { mutableStateOf(editing?.producedQuantity?.toString().orEmpty()) }
    var date by remember(editing?.id) { mutableLongStateOf(editing?.date ?: PersianDateFormatter.todayStartMillis()) }
    var notes by remember(editing?.id) { mutableStateOf(editing?.notes.orEmpty()) }
    var error by remember { mutableStateOf<String?>(null) }
    val materialRows = remember(editing?.id) { mutableStateListOf<MaterialRow>().apply {
        if (existingMaterials.isEmpty()) add(MaterialRow()) else addAll(existingMaterials.map { MaterialRow(materialId = it.materialId, quantity = it.quantity.toString()) })
    } }
    val allocationRows = remember(editing?.id) { mutableStateListOf<AllocationRow>().apply {
        if (existingAllocations.isEmpty()) add(AllocationRow()) else addAll(existingAllocations.map { AllocationRow(projectId = it.projectId, quantity = it.quantity.toString()) })
    } }
    val count = NumberFormatter.normalizeDigits(produced).toIntOrNull() ?: 0

    LazyColumn(modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { SectionHeader(if (editing == null) "ثبت پخت و مصرف" else "ویرایش پخت و مصرف") }
        if (warehouses.isEmpty() || materials.isEmpty() || projects.isEmpty()) item {
            EmptyState("پیش‌نیازها کامل نیست", "حداقل یک انبار، ماده اولیه و شرکت فعال لازم است.")
        }
        item {
            GlassCard(Modifier.fillMaxWidth(), accent = Gold) {
                OptionSelector("انبار مصرف", warehouses, warehouse, { it.name }) { warehouse = it }
                Spacer(Modifier.height(10.dp))
                FilterChipRow(MealType.entries.map { it.label() }, mealType.label(), { label -> mealType = MealType.entries.first { it.label() == label } })
                Spacer(Modifier.height(10.dp))
                QuantityField(produced, { produced = it }, "تعداد غذای پخته‌شده")
                Spacer(Modifier.height(10.dp))
                LocalDateSelector("تاریخ پخت", date, { date = it })
            }
        }
        item { SectionHeader("مواد مصرف‌شده") }
        items(materialRows, key = { it.key }) { row ->
            GlassCard(Modifier.fillMaxWidth(), accent = AppCyan) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(Modifier.weight(1f)) {
                        val selected = materials.firstOrNull { it.id == row.materialId }
                        OptionSelector("ماده اولیه", materials, selected, { "${it.name} • ${it.mainUnit.label()}" }) { chosen ->
                            materialRows[materialRows.indexOfFirst { it.key == row.key }] = row.copy(materialId = chosen.id)
                        }
                        Spacer(Modifier.height(8.dp))
                        QuantityField(row.quantity, { value -> materialRows[materialRows.indexOfFirst { it.key == row.key }] = row.copy(quantity = value) }, "مقدار مصرف")
                    }
                    if (materialRows.size > 1) IconButton({ materialRows.remove(row) }) { Icon(Icons.Outlined.Delete, "حذف", tint = AppRed) }
                }
            }
        }
        item { GoldPrimaryButton("افزودن ماده", { materialRows.add(MaterialRow()) }, icon = Icons.Outlined.Add) }
        item { SectionHeader("تخصیص غذا به شرکت‌ها") }
        items(allocationRows, key = { it.key }) { row ->
            GlassCard(Modifier.fillMaxWidth(), accent = AppGreen) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(Modifier.weight(1f)) {
                        val selected = projects.firstOrNull { it.id == row.projectId }
                        OptionSelector("شرکت", projects, selected, { it.name }) { chosen ->
                            allocationRows[allocationRows.indexOfFirst { it.key == row.key }] = row.copy(projectId = chosen.id)
                        }
                        Spacer(Modifier.height(8.dp))
                        QuantityField(row.quantity, { value -> allocationRows[allocationRows.indexOfFirst { it.key == row.key }] = row.copy(quantity = value) }, "تعداد پرس")
                    }
                    if (allocationRows.size > 1) IconButton({ allocationRows.remove(row) }) { Icon(Icons.Outlined.Delete, "حذف", tint = AppRed) }
                }
            }
        }
        item { GoldPrimaryButton("افزودن شرکت", { allocationRows.add(AllocationRow()) }, icon = Icons.Outlined.Add) }
        item {
            val allocated = allocationRows.sumOf { NumberFormatter.normalizeDigits(it.quantity).toIntOrNull() ?: 0 }
            GlassCard(Modifier.fillMaxWidth(), accent = if (allocated == count && count > 0) AppGreen else AppRed) {
                Text("تولید: ${NumberFormatter.format(count)} • تخصیص: ${NumberFormatter.format(allocated)}", color = TextPrimary)
                Text(if (allocated == count && count > 0) "تخصیص کامل است" else "جمع تخصیص باید دقیقاً با تولید برابر باشد", color = TextSecondary)
            }
        }
        item { DarkOutlinedTextField(notes, { notes = it }, "توضیحات", singleLine = false) }
        if (error != null) item { Text(error.orEmpty(), color = AppRed) }
        item {
            FormActionFooter(
                text = "ذخیره پخت و مصرف",
                icon = Icons.Outlined.Save,
                enabled = warehouses.isNotEmpty() && materials.isNotEmpty() && projects.isNotEmpty(),
                onClick = {
                val materialInputs = materialRows.mapNotNull { row ->
                    val qty = NumberFormatter.normalizeDigits(row.quantity).toDoubleOrNull()
                    if (row.materialId != null && qty != null && qty > 0) CookingMaterialInput(row.materialId, qty) else null
                }
                val allocations = allocationRows.mapNotNull { row ->
                    val qty = NumberFormatter.normalizeDigits(row.quantity).toIntOrNull()
                    if (row.projectId != null && qty != null && qty > 0) CookingAllocationInput(row.projectId, qty) else null
                }
                error = when {
                    warehouse == null -> "انبار را انتخاب کنید"
                    count <= 0 -> "تعداد تولید باید بیشتر از صفر باشد"
                    materialInputs.size != materialRows.size -> "همه مواد و مقدار مصرف را کامل کنید"
                    materialInputs.map { it.materialId }.distinct().size != materialInputs.size -> "ماده تکراری را حذف کنید"
                    allocations.size != allocationRows.size -> "همه شرکت‌ها و تعداد پرس را کامل کنید"
                    allocations.map { it.projectId }.distinct().size != allocations.size -> "شرکت تکراری را حذف کنید"
                    allocations.sumOf { it.quantity } != count -> "جمع تخصیص شرکت‌ها باید با تعداد تولید برابر باشد"
                    else -> null
                }
                    warehouse?.let { wh -> if (error == null) onSave(CookingBatchInput(editing?.id ?: 0, wh.id, date, mealType, count, materialInputs, allocations, notes)) }
                }
            )
        }
        item { Spacer(Modifier.height(20.dp)) }
    }
}
