package com.restaurant.offlinemanager.ui.reports

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Inventory
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.restaurant.offlinemanager.core.design.AppCyan
import com.restaurant.offlinemanager.core.design.AppGreen
import com.restaurant.offlinemanager.core.design.AppOrange
import com.restaurant.offlinemanager.core.design.AppPurple
import com.restaurant.offlinemanager.core.design.AppRed
import com.restaurant.offlinemanager.core.design.GlassCard
import com.restaurant.offlinemanager.core.design.Gold
import com.restaurant.offlinemanager.core.design.GoldPrimaryButton
import com.restaurant.offlinemanager.core.design.MoneyText
import com.restaurant.offlinemanager.core.design.ReportCard
import com.restaurant.offlinemanager.core.design.SectionHeader
import com.restaurant.offlinemanager.core.design.StatCard
import com.restaurant.offlinemanager.core.design.TextPrimary
import com.restaurant.offlinemanager.core.design.TextSecondary
import com.restaurant.offlinemanager.core.utils.MoneyFormatter
import com.restaurant.offlinemanager.core.utils.NumberFormatter
import com.restaurant.offlinemanager.domain.model.MonthlyPoint
import com.restaurant.offlinemanager.ui.AppUiState
import com.restaurant.offlinemanager.ui.CsvReportType

@Composable
fun ReportsScreen(
    state: AppUiState,
    context: Context,
    onExport: (Context, CsvReportType) -> Unit,
    modifier: Modifier = Modifier
) {
    val bestProject = state.projectFinances.maxByOrNull { it.totalDelivered }
    val topDebtorProject = state.projectFinances.filter { it.receivable > 0 }.maxByOrNull { it.receivable }
    val topSupplierDebt = state.supplierDebts.filter { it.remaining > 0 }.maxByOrNull { it.remaining }
    val lowStock = state.inventory.filter { it.isLowStock }.take(3)
    val chartPoints = state.monthlyPoints.takeLast(6)
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { SectionHeader("گزارش‌ها") }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("خرید امروز", MoneyFormatter.format(state.dashboard.todayPurchasesTotal), Icons.Outlined.ShoppingCart, AppGreen, Modifier.weight(1f))
                StatCard("ارزش انبار", MoneyFormatter.format(state.dashboard.totalInventoryValue), Icons.Outlined.Inventory, AppCyan, Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("مطالبات", MoneyFormatter.format(state.dashboard.projectReceivablesTotal), Icons.AutoMirrored.Outlined.ReceiptLong, AppOrange, Modifier.weight(1f))
                StatCard("بدهی تامین‌کننده", MoneyFormatter.format(state.dashboard.supplierDebtsTotal), Icons.Outlined.Payments, AppRed, Modifier.weight(1f))
            }
        }
        item {
            GlassCard(Modifier.fillMaxWidth(), accent = AppPurple) {
                Text("روند خرید ماهانه", color = TextPrimary, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(10.dp))
                MonthlyBars(chartPoints, valueSelector = { it.purchases }, color = AppPurple)
            }
        }
        item {
            GlassCard(Modifier.fillMaxWidth(), accent = Gold) {
                Text("درآمد در برابر هزینه", color = TextPrimary, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(10.dp))
                IncomeExpenseChart(chartPoints)
            }
        }
        item {
            GlassCard(Modifier.fillMaxWidth(), accent = AppCyan) {
                Text("خلاصه خروجی‌ها", color = TextPrimary, style = MaterialTheme.typography.titleLarge)
                Text("فایل‌های CSV در حافظه محلی برنامه ذخیره می‌شوند.", color = TextSecondary)
            }
        }
        item { SectionHeader("بینش‌های مدیریتی") }
        item {
            ReportCard(
                title = "بهترین پروژه از نظر درآمد",
                subtitle = bestProject?.let { "${it.project.name} • ${MoneyFormatter.format(it.totalDelivered)}" } ?: "هنوز درآمد پروژه‌ای ثبت نشده است.",
                icon = Icons.Outlined.Business,
                accent = Gold
            )
        }
        item {
            ReportCard(
                title = "بدهکارترین پروژه",
                subtitle = topDebtorProject?.let { "${it.project.name} • ${MoneyFormatter.format(it.receivable.coerceAtLeast(0))}" } ?: "مطالبه فعالی وجود ندارد.",
                icon = Icons.AutoMirrored.Outlined.ReceiptLong,
                accent = AppOrange
            )
        }
        item {
            ReportCard(
                title = "بیشترین تامین‌کننده بدهکار",
                subtitle = topSupplierDebt?.let { "${it.supplier.name} • ${MoneyFormatter.format(it.remaining.coerceAtLeast(0))}" } ?: "بدهی تامین‌کننده ثبت نشده است.",
                icon = Icons.Outlined.Payments,
                accent = AppRed
            )
        }
        item {
            ReportCard(
                title = "اقلام نزدیک به کمبود",
                subtitle = if (lowStock.isEmpty()) "همه اقلام بالاتر از حداقل موجودی هستند." else lowStock.joinToString("، ") { "${it.materialName} (${NumberFormatter.format(it.quantity)})" },
                icon = Icons.Outlined.Warning,
                accent = AppOrange
            )
        }
        item { GoldPrimaryButton("خروجی خریدها", onClick = { onExport(context, CsvReportType.PURCHASES) }, icon = Icons.Outlined.FileDownload) }
        item { GoldPrimaryButton("خروجی موجودی", onClick = { onExport(context, CsvReportType.INVENTORY) }, icon = Icons.Outlined.FileDownload) }
        item { GoldPrimaryButton("خروجی مطالبات", onClick = { onExport(context, CsvReportType.RECEIVABLES) }, icon = Icons.Outlined.FileDownload) }
        item { GoldPrimaryButton("خروجی بدهی تامین‌کنندگان", onClick = { onExport(context, CsvReportType.SUPPLIER_DEBTS) }, icon = Icons.Outlined.FileDownload) }
        item { GoldPrimaryButton("خروجی پرداخت‌ها", onClick = { onExport(context, CsvReportType.PAYMENTS) }, icon = Icons.Outlined.FileDownload) }
        item { Spacer(Modifier.height(20.dp)) }
    }
}

@Composable
private fun MonthlyBars(
    points: List<MonthlyPoint>,
    valueSelector: (MonthlyPoint) -> Long,
    color: Color
) {
    if (points.isEmpty()) {
        Text("داده‌ای برای نمودار وجود ندارد", color = TextSecondary)
        return
    }
    val max = points.maxOfOrNull(valueSelector)?.coerceAtLeast(1L) ?: 1L
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            val spacing = 10.dp.toPx()
            val barWidth = (size.width - spacing * (points.size + 1)) / points.size.coerceAtLeast(1)
            points.forEachIndexed { index, point ->
                val value = valueSelector(point).toFloat() / max.toFloat()
                val height = size.height * value
                val x = spacing + index * (barWidth + spacing)
                drawRoundRect(
                    color = color,
                    topLeft = Offset(x, size.height - height),
                    size = Size(barWidth, height),
                    cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            points.forEach { point ->
                Text(point.label, color = TextSecondary, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun IncomeExpenseChart(points: List<MonthlyPoint>) {
    if (points.isEmpty()) {
        Text("داده‌ای برای نمودار وجود ندارد", color = TextSecondary)
        return
    }
    val max = points.flatMap { listOf(it.income, it.expense) }.maxOrNull()?.coerceAtLeast(1L) ?: 1L
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            val spacing = 12.dp.toPx()
            val groupWidth = (size.width - spacing * (points.size + 1)) / points.size.coerceAtLeast(1)
            val barWidth = groupWidth / 2.4f
            points.forEachIndexed { index, point ->
                val x = spacing + index * (groupWidth + spacing)
                val incomeHeight = size.height * (point.income.toFloat() / max.toFloat())
                val expenseHeight = size.height * (point.expense.toFloat() / max.toFloat())
                drawRoundRect(Gold, Offset(x, size.height - incomeHeight), Size(barWidth, incomeHeight), CornerRadius(8.dp.toPx()))
                drawRoundRect(AppRed, Offset(x + barWidth + 4.dp.toPx(), size.height - expenseHeight), Size(barWidth, expenseHeight), CornerRadius(8.dp.toPx()))
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            points.forEach { point ->
                Text(point.label, color = TextSecondary, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("درآمد", color = Gold)
            Text("هزینه", color = AppRed)
        }
    }
}
