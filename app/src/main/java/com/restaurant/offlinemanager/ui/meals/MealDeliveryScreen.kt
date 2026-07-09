package com.restaurant.offlinemanager.ui.meals

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
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.restaurant.offlinemanager.core.design.AppCyan
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
import com.restaurant.offlinemanager.core.design.OptionSelector
import com.restaurant.offlinemanager.core.design.QuantityField
import com.restaurant.offlinemanager.core.design.SectionHeader
import com.restaurant.offlinemanager.core.design.SecondaryGlassButton
import com.restaurant.offlinemanager.core.design.StatusChip
import com.restaurant.offlinemanager.core.design.TextPrimary
import com.restaurant.offlinemanager.core.design.TextSecondary
import com.restaurant.offlinemanager.core.utils.MoneyFormatter
import com.restaurant.offlinemanager.core.utils.NumberFormatter
import com.restaurant.offlinemanager.core.utils.PersianDateFormatter
import com.restaurant.offlinemanager.data.local.entity.MealType
import com.restaurant.offlinemanager.data.local.entity.ProjectEntity
import com.restaurant.offlinemanager.data.local.entity.ProjectStatus
import com.restaurant.offlinemanager.domain.model.MealDeliveryInput
import com.restaurant.offlinemanager.domain.model.label
import com.restaurant.offlinemanager.ui.AppUiState

@Composable
fun MealDeliveryListScreen(
    state: AppUiState,
    onAddMeal: () -> Unit,
    onEditMeal: (Long) -> Unit,
    onDeleteMeal: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val deliveries = state.snapshot.mealDeliveries.take(40)
    var deleteId by remember { mutableStateOf<Long?>(null) }
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { GoldPrimaryButton("ثبت وعده", onClick = onAddMeal, icon = Icons.Outlined.Restaurant) }
        if (deliveries.isEmpty()) {
            item { EmptyState("وعده‌ای ثبت نشده", "برای شروع، یک وعده جدید ثبت کنید.") }
        } else {
            items(deliveries, key = { it.id }) { delivery ->
                val project = state.snapshot.projects.firstOrNull { it.id == delivery.projectId }
                GlassCard(Modifier.fillMaxWidth()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Column(Modifier.weight(1f)) {
                            Text(project?.name ?: "پروژه حذف‌شده", color = TextPrimary, style = MaterialTheme.typography.titleLarge)
                            Text(PersianDateFormatter.format(delivery.date), color = TextSecondary)
                            Text("تعداد: ${NumberFormatter.format(delivery.quantity)}", color = TextSecondary)
                        }
                        Column {
                            StatusChip(delivery.mealType.label(), AppCyan)
                            Text(MoneyFormatter.format(delivery.totalAmount), color = Gold)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        GoldPrimaryButton("ویرایش", onClick = { onEditMeal(delivery.id) }, icon = Icons.Outlined.Edit, modifier = Modifier.weight(1f))
                        DangerButton("حذف وعده", onClick = { deleteId = delivery.id }, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
        item { Spacer(Modifier.height(20.dp)) }
    }
    deleteId?.let { id ->
        ConfirmDialog(
            title = "حذف وعده",
            message = "این وعده از محاسبات پروژه حذف می‌شود.",
            confirmText = "حذف",
            onConfirm = {
                deleteId = null
                onDeleteMeal(id)
            },
            onDismiss = { deleteId = null }
        )
    }
}

private fun String?.toMealTypeOrDefault(): MealType =
    when (this) {
        MealType.BREAKFAST.label() -> MealType.BREAKFAST
        MealType.DINNER.label() -> MealType.DINNER
        else -> MealType.LUNCH
    }

@Composable
fun MealDeliveryFormScreen(
    state: AppUiState,
    preselectedProjectId: Long?,
    deliveryId: Long?,
    onSave: (MealDeliveryInput) -> Unit,
    modifier: Modifier = Modifier
) {
    val editing = state.snapshot.mealDeliveries.firstOrNull { it.id == deliveryId }
    val projects = state.snapshot.projects.filter { it.status == ProjectStatus.ACTIVE || it.id == editing?.projectId }
    val setupReady = projects.isNotEmpty()
    var projectQuery by remember { mutableStateOf("") }
    val filteredProjects = projects.filter {
        projectQuery.isBlank() ||
            it.name.contains(projectQuery) ||
            it.companyName.orEmpty().contains(projectQuery)
    }
    var selectedProject by remember(editing?.id, projects) {
        mutableStateOf<ProjectEntity?>(
            projects.firstOrNull { it.id == editing?.projectId }
                ?: projects.firstOrNull { it.id == preselectedProjectId }
                ?: projects.firstOrNull()
        )
    }
    var mealType by remember(editing?.id) { mutableStateOf(editing?.mealType ?: selectedProject?.defaultMealType.toMealTypeOrDefault()) }
    var quantity by remember(editing?.id) { mutableStateOf(editing?.quantity?.toString() ?: selectedProject?.workerCount?.toString().orEmpty()) }
    var unitPrice by remember(editing?.id) { mutableStateOf(editing?.unitPrice?.toString() ?: selectedProject?.mealPrice?.toString().orEmpty()) }
    var date by remember(editing?.id) { mutableStateOf(editing?.date ?: PersianDateFormatter.todayStartMillis()) }
    var notes by remember(editing?.id) { mutableStateOf(editing?.notes.orEmpty()) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(selectedProject?.id) {
        selectedProject?.let {
            if (editing == null) {
                mealType = it.defaultMealType.toMealTypeOrDefault()
                quantity = it.workerCount.toString()
                unitPrice = it.mealPrice.toString()
            } else if (quantity.isBlank()) {
                quantity = it.workerCount.toString()
            }
        }
    }

    val count = NumberFormatter.normalizeDigits(quantity).toIntOrNull() ?: 0
    val price = MoneyFormatter.parse(unitPrice)
    val total = count * price

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { SectionHeader("ثبت وعده") }
        if (!setupReady) {
            item { EmptyState("پروژه فعالی برای ثبت وعده وجود ندارد", "ابتدا یک پروژه فعال بسازید، سپس وعده‌های آن را ثبت کنید.") }
        }
        item { AppSearchBar(projectQuery, { projectQuery = it }, label = "جستجوی پروژه") }
        item {
            OptionSelector("انتخاب پروژه", filteredProjects, selectedProject, { "${it.name} • ${it.workerCount} نفر" }) {
                selectedProject = it
                quantity = it.workerCount.toString()
                unitPrice = it.mealPrice.toString()
            }
        }
        item {
            FilterChipRow(
                options = MealType.entries.map { it.label() },
                selected = mealType.label(),
                onSelected = { label -> mealType = MealType.entries.first { it.label() == label } }
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SecondaryGlassButton(
                    text = "نفرات پروژه",
                    onClick = { quantity = selectedProject?.workerCount?.toString().orEmpty() },
                    modifier = Modifier.weight(1f),
                    accent = AppCyan
                )
                SecondaryGlassButton(
                    text = "+۵",
                    onClick = { quantity = (count + 5).toString() },
                    modifier = Modifier.weight(1f),
                    accent = Gold
                )
                SecondaryGlassButton(
                    text = "+۱۰",
                    onClick = { quantity = (count + 10).toString() },
                    modifier = Modifier.weight(1f),
                    accent = Gold
                )
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuantityField(quantity, { quantity = it }, "تعداد", modifier = Modifier.weight(1f))
                MoneyField(unitPrice, { unitPrice = it }, "قیمت واحد", modifier = Modifier.weight(1f))
            }
        }
        item { LocalDateSelector("تاریخ", date, { date = it }) }
        item { DarkOutlinedTextField(notes, { notes = it }, "توضیحات", singleLine = false) }
        item {
            GlassCard(Modifier.fillMaxWidth(), accent = Gold) {
                Text("محاسبه زنده", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                Text("${NumberFormatter.format(count)} × ${MoneyFormatter.format(price)}", color = TextSecondary)
                Spacer(Modifier.height(6.dp))
                Text(MoneyFormatter.format(total), color = Gold, style = MaterialTheme.typography.headlineMedium)
            }
        }
        if (error != null) item { Text(error.orEmpty(), color = com.restaurant.offlinemanager.core.design.AppRed) }
        item {
            FormActionFooter(
                text = "ثبت وعده",
                icon = Icons.Outlined.Save,
                enabled = setupReady,
                onClick = {
                    error = when {
                        selectedProject == null -> "پروژه را انتخاب کنید"
                        count <= 0 -> "تعداد باید بیشتر از صفر باشد"
                        price <= 0 -> "قیمت واحد باید بیشتر از صفر باشد"
                        else -> null
                    }
                    val project = selectedProject
                    if (error == null && project != null) {
                        onSave(
                            MealDeliveryInput(
                                id = editing?.id ?: 0,
                                projectId = project.id,
                                date = date,
                                mealType = mealType,
                                quantity = count,
                                unitPrice = price,
                                notes = notes
                            )
                        )
                    }
                }
            )
        }
        item { Spacer(Modifier.height(20.dp)) }
    }
}
