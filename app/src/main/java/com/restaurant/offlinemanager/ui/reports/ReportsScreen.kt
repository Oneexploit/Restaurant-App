package com.restaurant.offlinemanager.ui.reports

import android.content.Context
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Inventory
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.restaurant.offlinemanager.core.design.LocalMotionEnabled
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
    val mostProfitableProject = state.projectProfits.firstOrNull { it.earnedRevenue > 0 }
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
        item { SectionHeader("عملکرد مالی واقعی") }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("درآمد تحقق‌یافته", MoneyFormatter.format(state.accounting.earnedRevenue), Icons.Outlined.Payments, AppGreen, Modifier.weight(1f))
                StatCard("بهای مواد مصرفی", MoneyFormatter.format(state.accounting.costOfGoodsConsumed), Icons.Outlined.Inventory, AppOrange, Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("سود ناخالص", MoneyFormatter.format(state.accounting.grossProfit), Icons.Outlined.BarChart, Gold, Modifier.weight(1f))
                StatCard("سود خالص", MoneyFormatter.format(state.accounting.netProfit), Icons.Outlined.BarChart, if (state.accounting.netProfit >= 0) AppGreen else AppRed, Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("جریان نقد خالص", MoneyFormatter.format(state.accounting.netCashFlow), Icons.Outlined.Payments, if (state.accounting.netCashFlow >= 0) AppCyan else AppRed, Modifier.weight(1f))
                StatCard("زیان ضایعات", MoneyFormatter.format(state.accounting.wasteLoss), Icons.Outlined.Warning, AppRed, Modifier.weight(1f))
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
                title = "سودآورترین پروژه",
                subtitle = mostProfitableProject?.let {
                    "${it.project.name} • سود خالص ${MoneyFormatter.format(it.netProfit)} • هزینه عملیاتی ${MoneyFormatter.format(it.operatingCost)} • حاشیه ${NumberFormatter.format(it.marginPercent)}٪"
                } ?: "برای محاسبه سود پروژه، خروج مواد را به پروژه مربوط متصل کنید.",
                icon = Icons.Outlined.BarChart,
                accent = AppGreen
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
        item { GoldPrimaryButton("خروجی پخت و مصرف", onClick = { onExport(context, CsvReportType.COOKING) }, icon = Icons.Outlined.FileDownload) }
        item { GoldPrimaryButton("خروجی سود وعده‌ها", onClick = { onExport(context, CsvReportType.MEAL_PROFIT) }, icon = Icons.Outlined.FileDownload) }
        item { GoldPrimaryButton("خروجی تحویل غذا", onClick = { onExport(context, CsvReportType.MEAL_DELIVERIES) }, icon = Icons.Outlined.FileDownload) }
        item { GoldPrimaryButton("خروجی خریدها", onClick = { onExport(context, CsvReportType.PURCHASES) }, icon = Icons.Outlined.FileDownload) }
        item { GoldPrimaryButton("خروجی موجودی", onClick = { onExport(context, CsvReportType.INVENTORY) }, icon = Icons.Outlined.FileDownload) }
        item { GoldPrimaryButton("خروجی مطالبات", onClick = { onExport(context, CsvReportType.RECEIVABLES) }, icon = Icons.Outlined.FileDownload) }
        item { GoldPrimaryButton("خروجی بدهی تامین‌کنندگان", onClick = { onExport(context, CsvReportType.SUPPLIER_DEBTS) }, icon = Icons.Outlined.FileDownload) }
        item { GoldPrimaryButton("خروجی پرداخت‌ها", onClick = { onExport(context, CsvReportType.PAYMENTS) }, icon = Icons.Outlined.FileDownload) }
        item { GoldPrimaryButton("خروجی هزینه‌ها", onClick = { onExport(context, CsvReportType.EXPENSES) }, icon = Icons.Outlined.FileDownload) }
        item { GoldPrimaryButton("خروجی سود و زیان", onClick = { onExport(context, CsvReportType.PROFIT_LOSS) }, icon = Icons.Outlined.FileDownload) }
        item { GoldPrimaryButton("خروجی سود پروژه‌ها", onClick = { onExport(context, CsvReportType.PROJECT_PROFIT) }, icon = Icons.Outlined.FileDownload) }
        item { GoldPrimaryButton("خروجی جریان نقدی", onClick = { onExport(context, CsvReportType.CASH_FLOW) }, icon = Icons.Outlined.FileDownload) }
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
    val motionEnabled = LocalMotionEnabled.current
    var entered by remember(points) { mutableStateOf(false) }
    LaunchedEffect(points) { entered = true }
    val reveal by animateFloatAsState(
        targetValue = if (motionEnabled && entered) 1f else if (motionEnabled) 0f else 1f,
        animationSpec = tween(720),
        label = "monthlyBarsReveal"
    )
    val max = points.maxOfOrNull(valueSelector)?.coerceAtLeast(1L) ?: 1L
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            val spacing = 10.dp.toPx()
            val barWidth = (size.width - spacing * (points.size + 1)) / points.size.coerceAtLeast(1)
            repeat(4) { step ->
                val y = size.height * (step + 1) / 5f
                drawLine(
                    color = TextSecondary.copy(alpha = 0.12f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx()
                )
            }
            points.forEachIndexed { index, point ->
                val value = valueSelector(point).toFloat() / max.toFloat()
                val height = size.height * value * reveal
                val x = spacing + index * (barWidth + spacing)
                drawRoundRect(
                    color = color.copy(alpha = 0.92f),
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
    val motionEnabled = LocalMotionEnabled.current
    var entered by remember(points) { mutableStateOf(false) }
    LaunchedEffect(points) { entered = true }
    val reveal by animateFloatAsState(
        targetValue = if (motionEnabled && entered) 1f else if (motionEnabled) 0f else 1f,
        animationSpec = tween(760),
        label = "incomeExpenseReveal"
    )
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
            repeat(4) { step ->
                val y = size.height * (step + 1) / 5f
                drawLine(
                    color = TextSecondary.copy(alpha = 0.12f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx()
                )
            }
            points.forEachIndexed { index, point ->
                val x = spacing + index * (groupWidth + spacing)
                val incomeHeight = size.height * (point.income.toFloat() / max.toFloat()) * reveal
                val expenseHeight = size.height * (point.expense.toFloat() / max.toFloat()) * reveal
                drawRoundRect(Gold.copy(alpha = 0.95f), Offset(x, size.height - incomeHeight), Size(barWidth, incomeHeight), CornerRadius(8.dp.toPx()))
                drawRoundRect(AppRed.copy(alpha = 0.9f), Offset(x + barWidth + 4.dp.toPx(), size.height - expenseHeight), Size(barWidth, expenseHeight), CornerRadius(8.dp.toPx()))
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
