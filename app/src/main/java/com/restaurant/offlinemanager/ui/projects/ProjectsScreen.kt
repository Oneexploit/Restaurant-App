package com.restaurant.offlinemanager.ui.projects

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.restaurant.offlinemanager.core.design.AppCyan
import com.restaurant.offlinemanager.core.design.AppGreen
import com.restaurant.offlinemanager.core.design.AppOrange
import com.restaurant.offlinemanager.core.design.AppPurple
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
import com.restaurant.offlinemanager.core.design.GoldLight
import com.restaurant.offlinemanager.core.design.GoldPrimaryButton
import com.restaurant.offlinemanager.core.design.LocalDateSelector
import com.restaurant.offlinemanager.core.design.MetricProgressBar
import com.restaurant.offlinemanager.core.design.MoneyField
import com.restaurant.offlinemanager.core.design.MoneyText
import com.restaurant.offlinemanager.core.design.OptionSelector
import com.restaurant.offlinemanager.core.design.SectionHeader
import com.restaurant.offlinemanager.core.design.SecondaryGlassButton
import com.restaurant.offlinemanager.core.design.StatusChip
import com.restaurant.offlinemanager.core.design.TextMuted
import com.restaurant.offlinemanager.core.design.TextPrimary
import com.restaurant.offlinemanager.core.design.TextSecondary
import com.restaurant.offlinemanager.core.utils.MoneyFormatter
import com.restaurant.offlinemanager.core.utils.NumberFormatter
import com.restaurant.offlinemanager.core.utils.PersianDateFormatter
import com.restaurant.offlinemanager.data.local.entity.ProjectStatus
import com.restaurant.offlinemanager.data.local.entity.billableQuantity
import com.restaurant.offlinemanager.domain.model.ProjectFinance
import com.restaurant.offlinemanager.domain.model.ProjectInput
import com.restaurant.offlinemanager.domain.model.label
import com.restaurant.offlinemanager.ui.AppUiState

@Composable
fun ProjectsListScreen(
    state: AppUiState,
    onAddProject: () -> Unit,
    onProjectDetails: (Long) -> Unit,
    onAddMeal: (Long) -> Unit,
    onAddPayment: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf("همه") }
    val filters = listOf("همه", "فعال", "متوقف", "تسویه‌شده", "آرشیو")
    val finances = state.projectFinances.filter { finance ->
        val matchesQuery = query.isBlank() ||
            finance.project.name.contains(query, ignoreCase = true) ||
            finance.project.companyName.orEmpty().contains(query, ignoreCase = true) ||
            finance.project.address.orEmpty().contains(query, ignoreCase = true)
        val matchesFilter = when (filter) {
            "فعال" -> finance.project.status == ProjectStatus.ACTIVE
            "متوقف" -> finance.project.status == ProjectStatus.PAUSED
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
        item { GoldPrimaryButton("افزودن پروژه", onClick = onAddProject) }
        if (finances.isEmpty()) {
            item { EmptyState("پروژه‌ای پیدا نشد", "فیلتر یا عبارت جستجو را تغییر دهید.") }
        } else {
            items(finances, key = { it.project.id }) { finance ->
                ProjectCard(
                    finance = finance,
                    onClick = { onProjectDetails(finance.project.id) },
                    onAddMeal = { onAddMeal(finance.project.id) },
                    onAddPayment = { onAddPayment(finance.project.id) }
                )
            }
        }
        item { Spacer(Modifier.height(20.dp)) }
    }
}

@Composable
private fun ProjectCard(
    finance: ProjectFinance,
    onClick: () -> Unit,
    onAddMeal: () -> Unit,
    onAddPayment: () -> Unit
) {
    val statusColor = when (finance.project.status) {
        ProjectStatus.ACTIVE -> AppGreen
        ProjectStatus.PAUSED -> AppOrange
        ProjectStatus.SETTLED -> AppCyan
        ProjectStatus.ARCHIVED -> TextMuted
    }
    val canRegisterMeal = finance.project.status == ProjectStatus.ACTIVE
    val canRegisterPayment = finance.receivable > 0
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        accent = statusColor
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            ProjectThumb(finance.project.name)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(finance.project.name, color = TextPrimary, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    StatusChip(finance.project.status.label(), statusColor)
                }
                Text(finance.project.companyName.orEmpty(), color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = TextMuted, modifier = Modifier.size(15.dp))
                    Text(finance.project.address.orEmpty(), color = TextMuted, style = MaterialTheme.typography.bodyMedium)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    MiniInfo(Icons.Outlined.Groups, "${NumberFormatter.format(finance.project.workerCount)} نفر")
                    MiniInfo(Icons.Outlined.Restaurant, MoneyFormatter.format(finance.project.mealPrice))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("مانده مطالبات", color = TextSecondary, modifier = Modifier.weight(1f))
                    MoneyText(finance.receivable.coerceAtLeast(0), style = MaterialTheme.typography.titleMedium)
                }
                MetricProgressBar(
                    label = "پرداخت‌شده از درآمد",
                    value = finance.totalPaid.toFloat(),
                    max = finance.totalDelivered.toFloat().coerceAtLeast(1f),
                    accent = if (finance.receivable <= 0) AppGreen else Gold,
                    valueLabel = if (finance.totalDelivered > 0) {
                        "${NumberFormatter.format((finance.totalPaid * 100.0 / finance.totalDelivered).coerceIn(0.0, 100.0))}٪"
                    } else {
                        "بدون تحویل"
                    }
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.align(Alignment.End)) {
            TextButton(onClick = onClick) {
                Text("جزئیات", color = Gold)
            }
            if (canRegisterMeal) {
                TextButton(onClick = onAddMeal) {
                    Text("تحویل غذا", color = AppCyan)
                }
            }
            if (canRegisterPayment) {
                TextButton(onClick = onAddPayment) {
                    Text("ثبت دریافت", color = AppGreen)
                }
            }
        }
    }
}

@Composable
private fun ProjectThumb(name: String) {
    Box(
        modifier = Modifier
            .size(76.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.linearGradient(listOf(Gold.copy(alpha = 0.32f), AppCyan.copy(alpha = 0.16f)))),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Outlined.Business, contentDescription = null, tint = GoldLight, modifier = Modifier.size(34.dp))
        Text(name.take(1), color = TextPrimary, style = MaterialTheme.typography.titleLarge, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp))
    }
}

@Composable
private fun MiniInfo(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(icon, contentDescription = null, tint = Gold, modifier = Modifier.size(15.dp))
        Text(text, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
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
    var startDate by remember(editing?.id) { mutableLongStateOf(editing?.startDate ?: PersianDateFormatter.todayStartMillis()) }
    var endDate by remember(editing?.id) { mutableStateOf(editing?.endDate) }
    var notes by remember(editing?.id) { mutableStateOf(editing?.notes.orEmpty()) }
    var error by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { SectionHeader(if (editing == null) "افزودن پروژه" else "ویرایش پروژه") }
        item { SectionHeader("اطلاعات پروژه") }
        item {
            GlassCard(Modifier.fillMaxWidth(), accent = Gold) {
                DarkOutlinedTextField(name, { name = it }, "نام پروژه")
                Spacer(Modifier.height(10.dp))
                DarkOutlinedTextField(company, { company = it }, "نام شرکت/کارفرما")
                Spacer(Modifier.height(10.dp))
                DarkOutlinedTextField(address, { address = it }, "آدرس پروژه", singleLine = false)
            }
        }
        item { SectionHeader("اطلاعات تماس") }
        item {
            GlassCard(Modifier.fillMaxWidth(), accent = AppCyan) {
                DarkOutlinedTextField(manager, { manager = it }, "نام مدیر / مسئول")
                Spacer(Modifier.height(10.dp))
                DarkOutlinedTextField(phone, { phone = it }, "شماره تماس", keyboardType = KeyboardType.Phone)
            }
        }
        item { SectionHeader("قرارداد و وعده") }
        item {
            GlassCard(Modifier.fillMaxWidth(), accent = AppPurple) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    DarkOutlinedTextField(workers, { workers = it }, "تعداد نفرات", modifier = Modifier.weight(1f), keyboardType = KeyboardType.Number)
                    MoneyField(mealPrice, { mealPrice = it }, "قیمت هر وعده", modifier = Modifier.weight(1f))
                }
                Spacer(Modifier.height(10.dp))
                OptionSelector("نوع وعده پیش‌فرض", listOf("صبحانه", "ناهار", "شام"), defaultMeal, { it }) { defaultMeal = it }
            }
        }
        item { SectionHeader("تاریخ قرارداد") }
        item {
            GlassCard(Modifier.fillMaxWidth(), accent = AppCyan) {
                LocalDateSelector("تاریخ شروع", startDate, { startDate = it })
                Spacer(Modifier.height(10.dp))
                if (endDate == null) {
                    SecondaryGlassButton(
                        text = "افزودن تاریخ پایان",
                        onClick = { endDate = PersianDateFormatter.todayStartMillis() },
                        accent = AppCyan
                    )
                } else {
                    LocalDateSelector("تاریخ پایان", endDate ?: startDate, { endDate = it })
                    Spacer(Modifier.height(8.dp))
                    SecondaryGlassButton(
                        text = "حذف تاریخ پایان",
                        onClick = { endDate = null },
                        accent = TextMuted
                    )
                }
            }
        }
        item { SectionHeader("وضعیت و توضیحات") }
        item {
            GlassCard(Modifier.fillMaxWidth(), accent = AppOrange) {
                OptionSelector("وضعیت", ProjectStatus.entries, status, { it.label() }) { status = it }
                Spacer(Modifier.height(10.dp))
                DarkOutlinedTextField(notes, { notes = it }, "توضیحات", singleLine = false)
            }
        }
        item {
            val workerCount = NumberFormatter.normalizeDigits(workers).toIntOrNull() ?: 0
            val price = MoneyFormatter.parse(mealPrice)
            GlassCard(Modifier.fillMaxWidth(), accent = Gold) {
                Text("پیش‌نمایش قرارداد", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text("وعده پیش‌فرض: $defaultMeal", color = TextSecondary)
                Text("تعداد نفرات: ${NumberFormatter.format(workerCount)} نفر", color = TextSecondary)
                Text("مبلغ هر وعده برای پروژه", color = TextMuted)
                MoneyText((workerCount * price).coerceAtLeast(0), style = MaterialTheme.typography.headlineSmall)
            }
        }
        if (error != null) item { Text(error.orEmpty(), color = AppRed) }
        item {
            FormActionFooter(
                text = "ذخیره",
                icon = Icons.Outlined.Save,
                onClick = {
                    val workerCount = NumberFormatter.normalizeDigits(workers).toIntOrNull() ?: 0
                    val price = MoneyFormatter.parse(mealPrice)
                    error = when {
                        name.isBlank() -> "نام پروژه الزامی است"
                        workerCount <= 0 -> "تعداد نفرات باید بیشتر از صفر باشد"
                        price <= 0 -> "قیمت هر وعده باید بیشتر از صفر باشد"
                        endDate != null && endDate!! < startDate -> "تاریخ پایان نمی‌تواند قبل از تاریخ شروع باشد"
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
                                startDate = startDate,
                                endDate = endDate,
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
    val recentMeals = state.snapshot.mealDeliveries
        .filter { it.projectId == projectId }
        .sortedByDescending { it.date }
        .take(5)
    val recentPayments = state.snapshot.projectPayments
        .filter { it.projectId == projectId }
        .sortedByDescending { it.date }
        .take(5)
    val statusColor = when (finance.project.status) {
        ProjectStatus.ACTIVE -> AppGreen
        ProjectStatus.PAUSED -> AppOrange
        ProjectStatus.SETTLED -> AppCyan
        ProjectStatus.ARCHIVED -> TextMuted
    }
    val canRegisterMeal = finance.project.status == ProjectStatus.ACTIVE
    val canRegisterPayment = finance.receivable > 0
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            GlassCard(Modifier.fillMaxWidth(), accent = Gold) {
                Text(finance.project.name, color = TextPrimary, style = MaterialTheme.typography.headlineMedium)
                Text(finance.project.companyName.orEmpty(), color = TextSecondary)
                Text(finance.project.address.orEmpty(), color = TextMuted)
                Spacer(Modifier.height(10.dp))
                StatusChip(finance.project.status.label(), statusColor)
                Spacer(Modifier.height(12.dp))
                MetricProgressBar(
                    label = "وصولی نسبت به کل تحویل",
                    value = finance.totalPaid.toFloat(),
                    max = finance.totalDelivered.toFloat().coerceAtLeast(1f),
                    accent = if (finance.receivable <= 0) AppGreen else Gold,
                    valueLabel = if (finance.totalDelivered > 0) {
                        "${NumberFormatter.format((finance.totalPaid * 100.0 / finance.totalDelivered).coerceIn(0.0, 100.0))}٪"
                    } else {
                        "بدون تحویل"
                    }
                )
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DetailMetric("غذای تحویل‌شده", NumberFormatter.format(finance.totalMeals), Modifier.weight(1f))
                DetailMetric("کل درآمد", MoneyFormatter.format(finance.totalDelivered), Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DetailMetric("پرداخت‌شده", MoneyFormatter.format(finance.totalPaid), Modifier.weight(1f))
                DetailMetric("مانده", MoneyFormatter.format(finance.receivable), Modifier.weight(1f))
            }
        }
        if (canRegisterMeal) {
            item { GoldPrimaryButton(text = "تحویل غذا", icon = Icons.Outlined.Restaurant, onClick = { onAddMeal(projectId) }) }
        }
        if (canRegisterPayment) {
            item { GoldPrimaryButton(text = "ثبت پرداخت", icon = Icons.Outlined.Payments, onClick = { onAddPayment(projectId) }) }
        }
        item { GoldPrimaryButton(text = "ویرایش پروژه", icon = Icons.Outlined.Edit, onClick = { onEdit(projectId) }) }
        if (finance.project.status != ProjectStatus.ARCHIVED) {
            item { DangerButton(text = "آرشیو پروژه", icon = Icons.Outlined.Archive, onClick = { confirmArchive = true }) }
        }
        item { SectionHeader("آخرین تحویل‌های غذا") }
        if (recentMeals.isEmpty()) {
            item { EmptyState("تحویل غذایی برای این پروژه ثبت نشده", "از دکمه تحویل غذا، ارسال‌های پروژه را وارد و پیگیری کنید.") }
        } else {
            items(recentMeals, key = { it.id }) { meal ->
                GlassCard(Modifier.fillMaxWidth(), accent = AppCyan) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Restaurant, contentDescription = null, tint = AppCyan)
                        Column(Modifier.weight(1f)) {
                            Text("${meal.mealType.label()} • ${meal.status.label()}", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                            Text("${PersianDateFormatter.format(meal.date)} • ارسالی ${NumberFormatter.format(meal.quantity)} • خالص ${NumberFormatter.format(meal.billableQuantity)}", color = TextSecondary)
                        }
                        MoneyText(meal.totalAmount, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
        item { SectionHeader("آخرین دریافت‌ها") }
        if (recentPayments.isEmpty()) {
            item { EmptyState("دریافتی ثبت نشده", "پرداخت‌های مشتری اینجا به تفکیک تاریخ دیده می‌شود.") }
        } else {
            items(recentPayments, key = { it.id }) { payment ->
                GlassCard(Modifier.fillMaxWidth(), accent = AppGreen) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Payments, contentDescription = null, tint = AppGreen)
                        Column(Modifier.weight(1f)) {
                            Text(payment.method.label(), color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                            Text(PersianDateFormatter.format(payment.date), color = TextSecondary)
                        }
                        MoneyText(payment.amount, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
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
    GlassCard(modifier = modifier, accent = Gold) {
        Text(title, color = TextSecondary)
        Text(value, color = TextPrimary, style = MaterialTheme.typography.titleLarge)
    }
}
