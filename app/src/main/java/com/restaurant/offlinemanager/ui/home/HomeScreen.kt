package com.restaurant.offlinemanager.ui.home

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
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.BusinessCenter
import androidx.compose.material.icons.outlined.Inventory
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.ReceiptLong
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.restaurant.offlinemanager.core.design.AppCyan
import com.restaurant.offlinemanager.core.design.AppGreen
import com.restaurant.offlinemanager.core.design.AppOrange
import com.restaurant.offlinemanager.core.design.AppPurple
import com.restaurant.offlinemanager.core.design.AppRed
import com.restaurant.offlinemanager.core.design.EmptyState
import com.restaurant.offlinemanager.core.design.GlassCard
import com.restaurant.offlinemanager.core.design.Gold
import com.restaurant.offlinemanager.core.design.GoldPrimaryButton
import com.restaurant.offlinemanager.core.design.SectionHeader
import com.restaurant.offlinemanager.core.design.StatCard
import com.restaurant.offlinemanager.core.design.StatusChip
import com.restaurant.offlinemanager.core.design.TextPrimary
import com.restaurant.offlinemanager.core.design.TextSecondary
import com.restaurant.offlinemanager.core.utils.MoneyFormatter
import com.restaurant.offlinemanager.core.utils.NumberFormatter
import com.restaurant.offlinemanager.core.utils.PersianDateFormatter
import com.restaurant.offlinemanager.domain.model.InventoryItem
import com.restaurant.offlinemanager.domain.model.label
import com.restaurant.offlinemanager.ui.AppUiState

@Composable
fun HomeScreen(
    state: AppUiState,
    onAddProject: () -> Unit,
    onAddMeal: () -> Unit,
    onStockIn: () -> Unit,
    onAddPurchase: () -> Unit,
    onAddPayment: () -> Unit,
    onReports: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stats = state.dashboard
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("سلام، روز کاری خوبی داشته باشید", color = TextPrimary, style = MaterialTheme.typography.titleLarge)
                Text(PersianDateFormatter.formatLong(PersianDateFormatter.nowMillis()), color = TextSecondary)
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard("پروژه‌های فعال", NumberFormatter.format(stats.activeProjectsCount), Icons.Outlined.BusinessCenter, Gold, Modifier.weight(1f))
                    StatCard("وعده‌های امروز", NumberFormatter.format(stats.todayMealCount), Icons.Outlined.Restaurant, AppCyan, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard("خریدهای امروز", MoneyFormatter.format(stats.todayPurchasesTotal), Icons.Outlined.ShoppingCart, AppGreen, Modifier.weight(1f))
                    StatCard("مطالبات پروژه‌ها", MoneyFormatter.format(stats.projectReceivablesTotal), Icons.Outlined.ReceiptLong, AppOrange, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard("بدهی تامین‌کنندگان", MoneyFormatter.format(stats.supplierDebtsTotal), Icons.Outlined.Payments, AppRed, Modifier.weight(1f))
                    StatCard("هشدارهای انبار", NumberFormatter.format(stats.lowStockItemCount), Icons.Outlined.Warning, AppPurple, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard("موجودی کارت‌ها", MoneyFormatter.format(stats.bankCardsTotalBalance), Icons.Outlined.AccountBalanceWallet, AppCyan, Modifier.weight(1f))
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
                    QuickAction("افزودن پروژه", Icons.Outlined.Add, onAddProject, Modifier.weight(1f))
                    QuickAction("ثبت وعده", Icons.Outlined.Restaurant, onAddMeal, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    QuickAction("ورود کالا", Icons.Outlined.Inventory, onStockIn, Modifier.weight(1f))
                    QuickAction("خرید روزانه", Icons.Outlined.ShoppingCart, onAddPurchase, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    QuickAction("دریافت از پروژه", Icons.Outlined.Payments, onAddPayment, Modifier.weight(1f))
                    QuickAction("گزارش‌ها", Icons.Outlined.BarChart, onReports, Modifier.weight(1f))
                }
            }
        }
        item {
            SectionHeader("کمبود موجودی")
        }
        val lowStock = state.inventory.filter { it.isLowStock }.take(5)
        if (lowStock.isEmpty()) {
            item {
                EmptyState("همه چیز مرتب است", "هیچ کالایی زیر حداقل موجودی نیست.")
            }
        } else {
            items(lowStock, key = { "${it.warehouseId}-${it.materialId}" }) { item ->
                LowStockRow(item)
            }
        }
        item { Spacer(Modifier.height(18.dp)) }
    }
}

@Composable
private fun QuickAction(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GoldPrimaryButton(
        text = label,
        icon = icon,
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
private fun LowStockRow(item: InventoryItem) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(item.emoji ?: "•", style = MaterialTheme.typography.titleLarge)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(item.materialName, color = TextPrimary, fontWeight = FontWeight.Bold)
                Text("${item.warehouseName} • ${NumberFormatter.format(item.quantity)} ${item.unit.label()}", color = TextSecondary)
            }
            StatusChip("کمبود موجودی", AppOrange)
        }
    }
}
