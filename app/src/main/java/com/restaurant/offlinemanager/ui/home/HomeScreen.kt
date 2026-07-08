package com.restaurant.offlinemanager.ui.home

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.BusinessCenter
import androidx.compose.material.icons.outlined.Inventory
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.RemoveCircleOutline
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.restaurant.offlinemanager.core.design.AppCyan
import com.restaurant.offlinemanager.core.design.AppGreen
import com.restaurant.offlinemanager.core.design.AppOrange
import com.restaurant.offlinemanager.core.design.AppPurple
import com.restaurant.offlinemanager.core.design.AppRed
import com.restaurant.offlinemanager.core.design.AppLogoMark
import com.restaurant.offlinemanager.core.design.ActionCard
import com.restaurant.offlinemanager.core.design.EmptyState
import com.restaurant.offlinemanager.core.design.GlassCard
import com.restaurant.offlinemanager.core.design.Gold
import com.restaurant.offlinemanager.core.design.LowStockWarningCard
import com.restaurant.offlinemanager.core.design.MoneyText
import com.restaurant.offlinemanager.core.design.PersianDateText
import com.restaurant.offlinemanager.core.design.SectionHeader
import com.restaurant.offlinemanager.core.design.StatCard
import com.restaurant.offlinemanager.core.design.StatusChip
import com.restaurant.offlinemanager.core.design.TextMuted
import com.restaurant.offlinemanager.core.design.TextPrimary
import com.restaurant.offlinemanager.core.design.TextSecondary
import com.restaurant.offlinemanager.core.utils.MoneyFormatter
import com.restaurant.offlinemanager.core.utils.NumberFormatter
import com.restaurant.offlinemanager.core.utils.PersianDateFormatter
import com.restaurant.offlinemanager.data.local.entity.ProjectStatus
import com.restaurant.offlinemanager.domain.model.InventoryItem
import com.restaurant.offlinemanager.domain.model.label
import com.restaurant.offlinemanager.ui.AppUiState
import java.time.Instant
import java.time.ZoneId

@Composable
fun HomeScreen(
    state: AppUiState,
    onAddProject: () -> Unit,
    onAddMeal: () -> Unit,
    onStockIn: () -> Unit,
    onStockOut: () -> Unit,
    onAddPurchase: () -> Unit,
    onAddPayment: () -> Unit,
    onReports: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stats = state.dashboard
    val today = PersianDateFormatter.todayStartMillis()
    val lowStockAlertsEnabled = state.settings.lowStockNotificationsEnabled
    val activeProjects = state.projectFinances
        .filter { it.project.status == ProjectStatus.ACTIVE }
        .take(3)
    val todayMeals = state.snapshot.mealDeliveries
        .filter { it.date.isSameLocalDay(today) }
        .take(3)
    val lowStock = if (lowStockAlertsEnabled) state.inventory.filter { it.isLowStock }.take(4) else emptyList()
    val recentActivity = rememberHomeActivity(state)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            GreetingCard()
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard("پروژه‌های فعال", NumberFormatter.format(stats.activeProjectsCount), Icons.Outlined.BusinessCenter, AppGreen, Modifier.weight(1f), "پروژه")
                    StatCard("غذاهای امروز", NumberFormatter.format(stats.todayMealCount), Icons.Outlined.Restaurant, AppCyan, Modifier.weight(1f), "نفر")
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard("هشدارهای انبار", NumberFormatter.format(stats.lowStockItemCount), Icons.Outlined.Warning, AppOrange, Modifier.weight(1f), "قلم کالا")
                    StatCard("مطالبات پروژه‌ها", MoneyFormatter.format(stats.projectReceivablesTotal), Icons.AutoMirrored.Outlined.ReceiptLong, AppPurple, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard("بدهی تامین‌کنندگان", MoneyFormatter.format(stats.supplierDebtsTotal), Icons.Outlined.Payments, AppRed, Modifier.weight(1f))
                    StatCard("خریدهای امروز", MoneyFormatter.format(stats.todayPurchasesTotal), Icons.Outlined.ShoppingCart, AppCyan, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard("موجودی کارت‌ها", MoneyFormatter.format(stats.bankCardsTotalBalance), Icons.Outlined.AccountBalanceWallet, Gold, Modifier.weight(1f))
                    StatCard("ارزش انبار", MoneyFormatter.format(stats.totalInventoryValue), Icons.Outlined.Inventory, AppGreen, Modifier.weight(1f))
                }
            }
        }
        item {
            SectionHeader("دسترسی سریع")
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ActionCard("ثبت وعده", Icons.Outlined.Restaurant, AppCyan, onAddMeal, Modifier.weight(1f))
                    ActionCard("خرید روزانه", Icons.Outlined.ShoppingCart, Gold, onAddPurchase, Modifier.weight(1f))
                    ActionCard("ورود کالا", Icons.Outlined.Inventory, AppGreen, onStockIn, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ActionCard("خروج کالا", Icons.Outlined.RemoveCircleOutline, AppOrange, onStockOut, Modifier.weight(1f))
                    ActionCard("دریافت پروژه", Icons.Outlined.Payments, AppPurple, onAddPayment, Modifier.weight(1f))
                    ActionCard("گزارش‌ها", Icons.Outlined.BarChart, AppCyan, onReports, Modifier.weight(1f))
                }
            }
        }
        item {
            GoldPrimaryDashboardHint(onAddProject = onAddProject)
        }
        item {
            SectionHeader("پروژه‌های فعال")
        }
        if (activeProjects.isEmpty()) {
            item { EmptyState("پروژه فعالی ثبت نشده", "از دکمه افزودن پروژه، اولین پروژه را ثبت کنید.") }
        } else {
            items(activeProjects, key = { it.project.id }) { finance ->
                GlassCard(modifier = Modifier.fillMaxWidth(), accent = AppGreen) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(finance.project.name, color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                            Text(finance.project.address.orEmpty(), color = TextMuted, style = MaterialTheme.typography.bodyMedium)
                            Text("نفرات: ${NumberFormatter.format(finance.project.workerCount)}", color = TextSecondary)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            StatusChip(finance.project.status.label(), AppGreen)
                            MoneyText(finance.receivable.coerceAtLeast(0), style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }
        item {
            SectionHeader("وعده‌های امروز")
        }
        if (todayMeals.isEmpty()) {
            item { EmptyState("وعده‌ای برای امروز ثبت نشده", "ثبت وعده امروز، آمار داشبورد را به‌روز می‌کند.") }
        } else {
            items(todayMeals, key = { it.id }) { meal ->
                val project = state.snapshot.projects.firstOrNull { it.id == meal.projectId }
                GlassCard(modifier = Modifier.fillMaxWidth(), accent = AppCyan) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Restaurant, contentDescription = null, tint = AppCyan)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(project?.name ?: "پروژه", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                            Text("${meal.mealType.label()} • ${NumberFormatter.format(meal.quantity)} نفر", color = TextSecondary)
                        }
                        MoneyText(meal.totalAmount, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
        item {
            SectionHeader("هشدارهای انبار")
        }
        if (!lowStockAlertsEnabled) {
            item {
                EmptyState("هشدارهای کمبود خاموش است", "از تنظیمات می‌توانید نمایش هشدار کمبود موجودی را دوباره فعال کنید.")
            }
        } else if (lowStock.isEmpty()) {
            item {
                EmptyState("همه چیز مرتب است", "هیچ کالایی زیر حداقل موجودی نیست.")
            }
        } else {
            item {
                LowStockWarningCard(
                    title = "نیاز به رسیدگی انبار",
                    message = "${NumberFormatter.format(lowStock.size)} قلم کالا زیر حداقل موجودی است. خرید یا انتقال را در اولویت بگذارید."
                )
            }
            items(lowStock, key = { "${it.warehouseId}-${it.materialId}" }) { item ->
                LowStockRow(item)
            }
        }
        item {
            SectionHeader("آخرین فعالیت‌ها")
        }
        if (recentActivity.isEmpty()) {
            item { EmptyState("فعالیتی ثبت نشده", "بعد از ثبت وعده، خرید یا پرداخت، رویدادها اینجا دیده می‌شوند.") }
        } else {
            items(recentActivity, key = { it.id }) { activity ->
                RecentActivityRow(activity)
            }
        }
        item { Spacer(Modifier.height(18.dp)) }
    }
}

@Composable
private fun GreetingCard() {
    GlassCard(modifier = Modifier.fillMaxWidth(), accent = Gold) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            AppLogoMark(modifier = Modifier.size(58.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("سلام، خوش آمدید", color = TextPrimary, style = MaterialTheme.typography.titleLarge)
                Text("همه چیز تحت کنترل است", color = TextSecondary)
                PersianDateText(PersianDateFormatter.nowMillis(), long = true)
            }
            StatusChip("آفلاین", Gold)
        }
    }
}

@Composable
private fun GoldPrimaryDashboardHint(onAddProject: () -> Unit) {
    GlassCard(modifier = Modifier.fillMaxWidth(), accent = Gold) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Outlined.BusinessCenter, contentDescription = null, tint = Gold)
            Column(modifier = Modifier.weight(1f)) {
                Text("شروع پروژه جدید", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                Text("برای قراردادهای جدید، پروژه را ثبت کنید و سپس وعده‌ها و دریافت‌ها را به آن وصل کنید.", color = TextSecondary)
            }
            StatusChip("یک‌بار", Gold)
        }
        Spacer(Modifier.height(10.dp))
        com.restaurant.offlinemanager.core.design.SecondaryGlassButton(
            text = "افزودن پروژه",
            icon = Icons.Outlined.Add,
            onClick = onAddProject
        )
    }
}

@Composable
private fun LowStockRow(item: InventoryItem) {
    GlassCard(modifier = Modifier.fillMaxWidth(), accent = AppOrange) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(item.emoji ?: "•", style = MaterialTheme.typography.headlineMedium)
            Column(modifier = Modifier.weight(1f)) {
                Text(item.materialName, color = TextPrimary, fontWeight = FontWeight.Bold)
                Text("${item.warehouseName} • موجودی ${NumberFormatter.format(item.quantity)} ${item.unit.label()}", color = TextSecondary)
                Text("حداقل: ${NumberFormatter.format(item.minimumStock)} ${item.unit.label()}", color = TextMuted)
            }
            StatusChip("کمبود", AppOrange)
        }
    }
}

private data class HomeActivity(
    val id: String,
    val title: String,
    val subtitle: String,
    val date: Long,
    val amount: Long?,
    val icon: ImageVector,
    val color: Color
)

@Composable
private fun rememberHomeActivity(state: AppUiState): List<HomeActivity> =
    androidx.compose.runtime.remember(state.snapshot) {
        buildList {
            state.snapshot.mealDeliveries.take(8).forEach { meal ->
                val project = state.snapshot.projects.firstOrNull { it.id == meal.projectId }
                add(
                    HomeActivity(
                        id = "meal-${meal.id}",
                        title = "ثبت وعده ${meal.mealType.label()}",
                        subtitle = project?.name ?: "پروژه حذف‌شده",
                        date = meal.date,
                        amount = meal.totalAmount,
                        icon = Icons.Outlined.Restaurant,
                        color = AppCyan
                    )
                )
            }
            state.snapshot.purchases.take(8).forEach { purchase ->
                val supplier = state.snapshot.suppliers.firstOrNull { it.id == purchase.supplierId }
                add(
                    HomeActivity(
                        id = "purchase-${purchase.id}",
                        title = "ثبت فاکتور خرید",
                        subtitle = supplier?.name ?: purchase.invoiceNumber.orEmpty().ifBlank { "خرید روزانه" },
                        date = purchase.date,
                        amount = purchase.totalAmount,
                        icon = Icons.Outlined.ShoppingCart,
                        color = AppOrange
                    )
                )
            }
            state.snapshot.projectPayments.take(8).forEach { payment ->
                val project = state.snapshot.projects.firstOrNull { it.id == payment.projectId }
                add(
                    HomeActivity(
                        id = "project-payment-${payment.id}",
                        title = "دریافت از پروژه",
                        subtitle = project?.name ?: "پروژه",
                        date = payment.date,
                        amount = payment.amount,
                        icon = Icons.Outlined.Payments,
                        color = AppGreen
                    )
                )
            }
        }.sortedByDescending { it.date }.take(5)
    }

@Composable
private fun RecentActivityRow(activity: HomeActivity) {
    GlassCard(modifier = Modifier.fillMaxWidth(), accent = activity.color) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(activity.icon, contentDescription = null, tint = activity.color)
            Column(modifier = Modifier.weight(1f)) {
                Text(activity.title, color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                Text(activity.subtitle, color = TextSecondary, maxLines = 1)
                PersianDateText(activity.date, color = TextMuted)
            }
            activity.amount?.let {
                MoneyText(it, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

private fun Long.isSameLocalDay(dayStartMillis: Long): Boolean {
    val zone = ZoneId.systemDefault()
    val thisDay = Instant.ofEpochMilli(this).atZone(zone).toLocalDate()
    val targetDay = Instant.ofEpochMilli(dayStartMillis).atZone(zone).toLocalDate()
    return thisDay == targetDay
}
