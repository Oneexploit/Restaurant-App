package com.restaurant.offlinemanager.ui.search

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
import com.restaurant.offlinemanager.core.utils.PersianDateFormatter
import com.restaurant.offlinemanager.domain.model.maskCardNumber
import com.restaurant.offlinemanager.ui.AppUiState

data class SearchResultUi(
    val id: String,
    val title: String,
    val subtitle: String,
    val type: String
)

@Composable
fun SearchScreen(
    state: AppUiState,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("همه") }
    val results = remember(query, state.snapshot) {
        if (query.isBlank()) emptyList() else buildList {
            state.snapshot.projects.filter { it.name.contains(query) || it.companyName.orEmpty().contains(query) }.forEach {
                add(SearchResultUi("project-${it.id}", it.name, it.companyName.orEmpty(), "پروژه"))
            }
            state.snapshot.materials.filter { it.name.contains(query) }.forEach {
                add(SearchResultUi("material-${it.id}", it.name, it.notes.orEmpty(), "متریال"))
            }
            state.snapshot.suppliers.filter { it.name.contains(query) || it.phone.orEmpty().contains(query) }.forEach {
                add(SearchResultUi("supplier-${it.id}", it.name, it.phone.orEmpty(), "تامین‌کننده"))
            }
            state.snapshot.warehouses.filter { it.name.contains(query) }.forEach {
                add(SearchResultUi("warehouse-${it.id}", it.name, it.address.orEmpty(), "انبار"))
            }
            state.snapshot.purchases.filter { it.invoiceNumber.orEmpty().contains(query) }.forEach {
                add(SearchResultUi("purchase-${it.id}", "فاکتور ${it.invoiceNumber}", MoneyFormatter.format(it.totalAmount), "خرید"))
            }
            state.snapshot.projectPayments.filter { it.amount.toString().contains(query) }.forEach {
                val project = state.snapshot.projects.firstOrNull { p -> p.id == it.projectId }?.name.orEmpty()
                add(SearchResultUi("project-payment-${it.id}", project, "${MoneyFormatter.format(it.amount)} • ${PersianDateFormatter.format(it.date)}", "دریافت"))
            }
            state.snapshot.bankCards.filter { it.title.contains(query) || it.cardNumber.orEmpty().contains(query) }.forEach {
                add(SearchResultUi("card-${it.id}", it.title, maskCardNumber(it.cardNumber), "کارت بانکی"))
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
                        GlassCard(Modifier.fillMaxWidth()) {
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
