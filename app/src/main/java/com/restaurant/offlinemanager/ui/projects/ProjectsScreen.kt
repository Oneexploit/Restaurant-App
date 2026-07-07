package com.restaurant.offlinemanager.ui.projects

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
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.restaurant.offlinemanager.core.design.AppCyan
import com.restaurant.offlinemanager.core.design.AppGreen
import com.restaurant.offlinemanager.core.design.AppOrange
import com.restaurant.offlinemanager.core.design.AppRed
import com.restaurant.offlinemanager.core.design.AppSearchBar
import com.restaurant.offlinemanager.core.design.ConfirmDialog
import com.restaurant.offlinemanager.core.design.DarkOutlinedTextField
import com.restaurant.offlinemanager.core.design.EmptyState
import com.restaurant.offlinemanager.core.design.FilterChipRow
import com.restaurant.offlinemanager.core.design.GlassCard
import com.restaurant.offlinemanager.core.design.Gold
import com.restaurant.offlinemanager.core.design.GoldPrimaryButton
import com.restaurant.offlinemanager.core.design.MoneyField
import com.restaurant.offlinemanager.core.design.OptionSelector
import com.restaurant.offlinemanager.core.design.SectionHeader
import com.restaurant.offlinemanager.core.design.StatusChip
import com.restaurant.offlinemanager.core.design.TextMuted
import com.restaurant.offlinemanager.core.design.TextPrimary
import com.restaurant.offlinemanager.core.design.TextSecondary
import com.restaurant.offlinemanager.core.utils.MoneyFormatter
import com.restaurant.offlinemanager.core.utils.NumberFormatter
import com.restaurant.offlinemanager.core.utils.PersianDateFormatter
import com.restaurant.offlinemanager.data.local.entity.ProjectEntity
import com.restaurant.offlinemanager.data.local.entity.ProjectStatus
import com.restaurant.offlinemanager.domain.model.ProjectFinance
import com.restaurant.offlinemanager.domain.model.ProjectInput
import com.restaurant.offlinemanager.domain.model.label
import com.restaurant.offlinemanager.ui.AppUiState

@Composable
fun ProjectsListScreen(
    state: AppUiState,
    onAddProject: () -> Unit,
    onProjectDetails: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf("همه") }
    val filters = listOf("همه", "فعال", "در حال اجرا", "تسویه‌شده", "آرشیو")
    val finances = state.projectFinances.filter { finance ->
        val matchesQuery = query.isBlank() ||
            finance.project.name.contains(query, ignoreCase = true) ||
            finance.project.companyName.orEmpty().contains(query, ignoreCase = true) ||
            finance.project.address.orEmpty().contains(query, ignoreCase = true)
        val matchesFilter = when (filter) {
            "فعال", "در حال اجرا" -> finance.project.status == ProjectStatus.ACTIVE
            "تسویه‌شده" -> finance.project.status == ProjectStatus.SETTLED
            "آرشیو" -> finance.project.status == ProjectStatus.ARCHIVED
            else -> true
        }
        matchesQuery && matchesFilter
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { AppSearchBar(query, { query = it }, label = "جستجو در پروژه‌ها") }
        item { FilterChipRow(filters, filter, { filter = it }) }
        item {
            GoldPrimaryButton("افزودن پروژه", onClick = onAddProject)
        }
        if (finances.isEmpty()) {
            item { EmptyState("پروژه‌ای پیدا نشد", "فیلتر یا عبارت جستجو را تغییر دهید.") }
        } else {
            items(finances, key = { it.project.id }) { finance ->
                ProjectCard(finance, onClick = { onProjectDetails(finance.project.id) })
            }
        }
        item { Spacer(Modifier.height(20.dp)) }
    }
}

@Composable
private fun ProjectCard(finance: ProjectFinance, onClick: () -> Unit) {
    val statusColor = when (finance.project.status) {
        ProjectStatus.ACTIVE -> AppGreen
        ProjectStatus.PAUSED -> AppOrange
        ProjectStatus.SETTLED -> AppCyan
        ProjectStatus.ARCHIVED -> TextMuted
    }
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(finance.project.name, color = TextPrimary, style = MaterialTheme.typography.titleLarge)
                Text(finance.project.companyName.orEmpty(), color = TextSecondary)
                Text(finance.project.address.orEmpty(), color = TextMuted, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(8.dp))
                Text("تعداد نفرات: ${NumberFormatter.format(finance.project.workerCount)}", color = TextSecondary)
                Text("قیمت وعده: ${MoneyFormatter.format(finance.project.mealPrice)}", color = TextSecondary)
                Text("مانده مطالبات: ${MoneyFormatter.format(finance.receivable)}", color = Gold, fontWeight = FontWeight.Bold)
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusChip(finance.project.status.label(), statusColor)
                TextButton(onClick = onClick) { Text("مشاهده", color = Gold) }
            }
        }
    }
}

@Composable
fun ProjectFormScreen(
    state: AppUiState,
    projectId: Long?,
    onSave: (ProjectInput) -> Unit,
    modifier: Modifier = Modifier
) {
    val editing = state.snapshot.projects.firstOrNull { it.id == projectId }
    var name by remember(editing?.id) { mutableStateOf(editing?.name.orEmpty()) }
    var company by remember(editing?.id) { mutableStateOf(editing?.companyName.orEmpty()) }
    var address by remember(editing?.id) { mutableStateOf(editing?.address.orEmpty()) }
    var manager by remember(editing?.id) { mutableStateOf(editing?.managerName.orEmpty()) }
    var phone by remember(editing?.id) { mutableStateOf(editing?.phone.orEmpty()) }
    var workers by remember(editing?.id) { mutableStateOf(editing?.workerCount?.toString().orEmpty()) }
    var mealPrice by remember(editing?.id) { mutableStateOf(editing?.mealPrice?.toString().orEmpty()) }
    var defaultMeal by remember(editing?.id) { mutableStateOf(editing?.defaultMealType ?: "ناهار") }
    var status by remember(editing?.id) { mutableStateOf(editing?.status ?: ProjectStatus.ACTIVE) }
    var notes by remember(editing?.id) { mutableStateOf(editing?.notes.orEmpty()) }
    var error by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader(if (editing == null) "افزودن پروژه" else "ویرایش پروژه")
        }
        item { DarkOutlinedTextField(name, { name = it }, "نام پروژه") }
        item { DarkOutlinedTextField(company, { company = it }, "نام شرکت/کارفرما") }
        item { DarkOutlinedTextField(address, { address = it }, "آدرس پروژه", singleLine = false) }
        item { DarkOutlinedTextField(manager, { manager = it }, "نام مدیر / مسئول") }
        item { DarkOutlinedTextField(phone, { phone = it }, "شماره تماس", keyboardType = KeyboardType.Phone) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DarkOutlinedTextField(workers, { workers = it }, "تعداد نفرات", modifier = Modifier.weight(1f), keyboardType = KeyboardType.Number)
                MoneyField(mealPrice, { mealPrice = it }, "قیمت هر وعده", modifier = Modifier.weight(1f))
            }
        }
        item {
            OptionSelector("نوع وعده پیش‌فرض", listOf("صبحانه", "ناهار", "شام"), defaultMeal, { it }) { defaultMeal = it }
        }
        item {
            OptionSelector("وضعیت", ProjectStatus.entries, status, { it.label() }) { status = it }
        }
        item { DarkOutlinedTextField(notes, { notes = it }, "توضیحات", singleLine = false) }
        if (error != null) item { Text(error.orEmpty(), color = AppRed) }
        item {
            GoldPrimaryButton(
                text = "ذخیره",
                icon = Icons.Outlined.Save,
                onClick = {
                    val workerCount = NumberFormatter.normalizeDigits(workers).toIntOrNull() ?: 0
                    val price = MoneyFormatter.parse(mealPrice)
                    error = when {
                        name.isBlank() -> "نام پروژه الزامی است"
                        workerCount <= 0 -> "تعداد نفرات باید بیشتر از صفر باشد"
                        price <= 0 -> "قیمت هر وعده باید بیشتر از صفر باشد"
                        else -> null
                    }
                    if (error == null) {
                        onSave(
                            ProjectInput(
                                id = editing?.id ?: 0,
                                name = name,
                                companyName = company,
                                address = address,
                                managerName = manager,
                                phone = phone,
                                workerCount = workerCount,
                                mealPrice = price,
                                defaultMealType = defaultMeal,
                                startDate = editing?.startDate ?: PersianDateFormatter.todayStartMillis(),
                                endDate = editing?.endDate,
                                status = status,
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

@Composable
fun ProjectDetailsScreen(
    state: AppUiState,
    projectId: Long,
    onEdit: (Long) -> Unit,
    onAddMeal: (Long) -> Unit,
    onAddPayment: (Long) -> Unit,
    onArchive: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val finance = state.projectFinances.firstOrNull { it.project.id == projectId }
    var confirmArchive by remember { mutableStateOf(false) }
    if (finance == null) {
        EmptyState("پروژه پیدا نشد", "ممکن است پروژه آرشیو یا بازیابی شده باشد.", modifier.padding(18.dp))
        return
    }
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            GlassCard(Modifier.fillMaxWidth()) {
                Text(finance.project.name, color = TextPrimary, style = MaterialTheme.typography.headlineMedium)
                Text(finance.project.companyName.orEmpty(), color = TextSecondary)
                Text(finance.project.address.orEmpty(), color = TextMuted)
                Spacer(Modifier.height(10.dp))
                StatusChip(finance.project.status.label(), if (finance.project.status == ProjectStatus.ACTIVE) AppGreen else AppOrange)
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DetailMetric("کل وعده‌ها", NumberFormatter.format(finance.totalMeals), Modifier.weight(1f))
                DetailMetric("کل درآمد", MoneyFormatter.format(finance.totalDelivered), Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DetailMetric("پرداخت‌شده", MoneyFormatter.format(finance.totalPaid), Modifier.weight(1f))
                DetailMetric("مانده", MoneyFormatter.format(finance.receivable), Modifier.weight(1f))
            }
        }
        item { GoldPrimaryButton(text = "ثبت وعده", icon = Icons.Outlined.Restaurant, onClick = { onAddMeal(projectId) }) }
        item { GoldPrimaryButton(text = "ثبت پرداخت", icon = Icons.Outlined.Payments, onClick = { onAddPayment(projectId) }) }
        item { GoldPrimaryButton(text = "ویرایش پروژه", icon = Icons.Outlined.Edit, onClick = { onEdit(projectId) }) }
        item { GoldPrimaryButton(text = "آرشیو پروژه", icon = Icons.Outlined.Archive, onClick = { confirmArchive = true }) }
        item { Spacer(Modifier.height(20.dp)) }
    }
    if (confirmArchive) {
        ConfirmDialog(
            title = "آرشیو پروژه",
            message = "پروژه حذف نمی‌شود و فقط از لیست فعال خارج خواهد شد.",
            confirmText = "آرشیو",
            onConfirm = {
                confirmArchive = false
                onArchive(projectId)
            },
            onDismiss = { confirmArchive = false }
        )
    }
}

@Composable
private fun DetailMetric(title: String, value: String, modifier: Modifier = Modifier) {
    GlassCard(modifier = modifier) {
        Text(title, color = TextSecondary)
        Text(value, color = TextPrimary, style = MaterialTheme.typography.titleLarge)
    }
}
