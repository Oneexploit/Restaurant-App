package com.restaurant.offlinemanager.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.restaurant.offlinemanager.core.design.AppSearchBar
import com.restaurant.offlinemanager.core.design.EmptyState
import com.restaurant.offlinemanager.core.design.FilterChipRow
import com.restaurant.offlinemanager.core.design.GlassCard
import com.restaurant.offlinemanager.core.design.Gold
import com.restaurant.offlinemanager.core.design.SectionHeader
import com.restaurant.offlinemanager.core.design.StatusChip
import com.restaurant.offlinemanager.core.design.TextPrimary
import com.restaurant.offlinemanager.core.design.TextSecondary
import com.restaurant.offlinemanager.core.utils.MoneyFormatter
import com.restaurant.offlinemanager.core.utils.NumberFormatter
import com.restaurant.offlinemanager.core.utils.PersianDateFormatter
import com.restaurant.offlinemanager.domain.model.label
import com.restaurant.offlinemanager.domain.model.maskCardNumber
import com.restaurant.offlinemanager.ui.AppUiState

data class SearchResultUi(
    val id: String,
    val rawId: Long,
    val title: String,
    val subtitle: String,
    val type: String
)

@Composable
fun SearchScreen(
    state: AppUiState,
    onResultClick: (SearchResultUi) -> Unit,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("همه") }
    val results = remember(query, state.snapshot) {
        val cleanQuery = query.trim()
        val normalizedQuery = NumberFormatter.normalizeDigits(cleanQuery)
        fun String.matchesSearch(): Boolean =
            contains(cleanQuery, ignoreCase = true) || NumberFormatter.normalizeDigits(this).contains(normalizedQuery, ignoreCase = true)
        fun Long.matchesSearch(): Boolean = toString().contains(normalizedQuery)
        fun Double.matchesSearch(): Boolean = toString().contains(normalizedQuery)

        if (cleanQuery.isBlank()) emptyList() else buildList {
            state.snapshot.projects.filter { it.name.matchesSearch() || it.companyName.orEmpty().matchesSearch() }.forEach {
                add(SearchResultUi("project-${it.id}", it.id, it.name, it.companyName.orEmpty(), "پروژه"))
            }
            state.snapshot.materials.filter { it.name.matchesSearch() || it.notes.orEmpty().matchesSearch() }.forEach {
                add(SearchResultUi("material-${it.id}", it.id, it.name, it.notes.orEmpty(), "متریال"))
            }
            state.snapshot.suppliers.filter { it.name.matchesSearch() || it.phone.orEmpty().matchesSearch() }.forEach {
                add(SearchResultUi("supplier-${it.id}", it.id, it.name, it.phone.orEmpty(), "تامین‌کننده"))
            }
            state.snapshot.warehouses.filter { it.name.matchesSearch() || it.address.orEmpty().matchesSearch() }.forEach {
                add(SearchResultUi("warehouse-${it.id}", it.id, it.name, it.address.orEmpty(), "انبار"))
            }
            state.snapshot.mealDeliveries.filter { delivery ->
                val project = state.snapshot.projects.firstOrNull { it.id == delivery.projectId }
                project?.name.orEmpty().matchesSearch() ||
                    delivery.mealType.label().matchesSearch() ||
                    delivery.status.label().matchesSearch() ||
                    delivery.recipientName.orEmpty().matchesSearch() ||
                    delivery.recipientPhone.orEmpty().matchesSearch() ||
                    PersianDateFormatter.format(delivery.date).matchesSearch() ||
                    delivery.totalAmount.matchesSearch()
            }.forEach {
                val project = state.snapshot.projects.firstOrNull { p -> p.id == it.projectId }?.name.orEmpty()
                add(SearchResultUi("meal-${it.id}", it.id, project, "${it.mealType.label()} • ${it.status.label()} • ${MoneyFormatter.format(it.totalAmount)}", "تحویل غذا"))
            }
            state.snapshot.stockTransactions.filter { tx ->
                val material = state.snapshot.materials.firstOrNull { it.id == tx.materialId }
                val warehouse = state.snapshot.warehouses.firstOrNull { it.id == tx.warehouseId }
                material?.name.orEmpty().matchesSearch() ||
                    warehouse?.name.orEmpty().matchesSearch() ||
                    tx.type.label().matchesSearch() ||
                    tx.quantity.matchesSearch()
            }.forEach {
                val material = state.snapshot.materials.firstOrNull { m -> m.id == it.materialId }?.name.orEmpty()
                add(SearchResultUi("stock-${it.id}", it.id, material, "${it.type.label()} • ${NumberFormatter.format(it.quantity)}", "تراکنش انبار"))
            }
            state.snapshot.purchases.filter { purchase ->
                val supplier = state.snapshot.suppliers.firstOrNull { it.id == purchase.supplierId }?.name.orEmpty()
                purchase.invoiceNumber.orEmpty().matchesSearch() || supplier.matchesSearch() || purchase.totalAmount.matchesSearch()
            }.forEach {
                add(SearchResultUi("purchase-${it.id}", it.id, "فاکتور ${it.invoiceNumber.orEmpty()}".trim(), MoneyFormatter.format(it.totalAmount), "خرید"))
            }
            state.snapshot.projectPayments.filter {
                val project = state.snapshot.projects.firstOrNull { p -> p.id == it.projectId }?.name.orEmpty()
                project.matchesSearch() || it.amount.matchesSearch()
            }.forEach {
                val project = state.snapshot.projects.firstOrNull { p -> p.id == it.projectId }?.name.orEmpty()
                add(SearchResultUi("project-payment-${it.id}", it.id, project, "${MoneyFormatter.format(it.amount)} • ${PersianDateFormatter.format(it.date)}", "دریافت"))
            }
            state.snapshot.supplierPayments.filter {
                val supplier = state.snapshot.suppliers.firstOrNull { s -> s.id == it.supplierId }?.name.orEmpty()
                supplier.matchesSearch() || it.amount.matchesSearch()
            }.forEach {
                val supplier = state.snapshot.suppliers.firstOrNull { s -> s.id == it.supplierId }?.name.orEmpty()
                add(SearchResultUi("supplier-payment-${it.id}", it.id, supplier, "${MoneyFormatter.format(it.amount)} • ${PersianDateFormatter.format(it.date)}", "پرداخت تامین‌کننده"))
            }
            state.snapshot.expenses.filter {
                it.title.matchesSearch() || it.category.label().matchesSearch() || it.amount.matchesSearch()
            }.forEach {
                add(SearchResultUi("expense-${it.id}", it.id, it.title, "${it.category.label()} • ${MoneyFormatter.format(it.amount)}", "هزینه"))
            }
            state.snapshot.bankCards.filter { it.title.matchesSearch() || it.cardNumber.orEmpty().matchesSearch() }.forEach {
                add(SearchResultUi("card-${it.id}", it.id, it.title, maskCardNumber(it.cardNumber), "کارت بانکی"))
            }
        }
    }
    val typeOptions = listOf("همه") + results.map { it.type }.distinct()
    val visibleResults = if (selectedType == "همه") results else results.filter { it.type == selectedType }
    LaunchedEffect(query) {
        selectedType = "همه"
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { SectionHeader("جستجوی عمومی") }
        item { AppSearchBar(query, { query = it }, label = "جستجو در همه بخش‌ها") }
        if (query.isBlank()) {
            item { EmptyState("عبارت جستجو را وارد کنید", "پروژه‌ها، متریال‌ها، تامین‌کنندگان، فاکتورها، پرداخت‌ها و کارت‌ها جستجو می‌شوند.") }
        } else if (results.isEmpty()) {
            item { EmptyState("نتیجه‌ای پیدا نشد", "عبارت دیگری را امتحان کنید.") }
        } else {
            item { FilterChipRow(typeOptions, selectedType, { selectedType = it }) }
            if (visibleResults.isEmpty()) {
                item { EmptyState("در این دسته چیزی پیدا نشد", "فیلتر نتیجه را تغییر دهید یا عبارت دیگری وارد کنید.") }
            } else {
                visibleResults.groupBy { it.type }.forEach { (type, groupedResults) ->
                    item { SectionHeader(type) }
                    items(groupedResults, key = { it.id }) { result ->
                        GlassCard(Modifier.fillMaxWidth().clickable { onResultClick(result) }) {
                            Column {
                                StatusChip(result.type, Gold)
                                Spacer(Modifier.height(8.dp))
                                Text(result.title, color = TextPrimary, style = MaterialTheme.typography.titleLarge)
                                Text(result.subtitle, color = TextSecondary)
                            }
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(20.dp)) }
    }
}
