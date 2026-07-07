package com.restaurant.offlinemanager.ui.finance

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
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.restaurant.offlinemanager.core.design.AppCyan
import com.restaurant.offlinemanager.core.design.AppGreen
import com.restaurant.offlinemanager.core.design.AppOrange
import com.restaurant.offlinemanager.core.design.AppPurple
import com.restaurant.offlinemanager.core.design.AppRed
import com.restaurant.offlinemanager.core.design.DarkOutlinedTextField
import com.restaurant.offlinemanager.core.design.EmptyState
import com.restaurant.offlinemanager.core.design.FilterChipRow
import com.restaurant.offlinemanager.core.design.GlassCard
import com.restaurant.offlinemanager.core.design.Gold
import com.restaurant.offlinemanager.core.design.GoldPrimaryButton
import com.restaurant.offlinemanager.core.design.MoneyField
import com.restaurant.offlinemanager.core.design.OptionSelector
import com.restaurant.offlinemanager.core.design.SectionHeader
import com.restaurant.offlinemanager.core.design.StatCard
import com.restaurant.offlinemanager.core.design.StatusChip
import com.restaurant.offlinemanager.core.design.TextMuted
import com.restaurant.offlinemanager.core.design.TextPrimary
import com.restaurant.offlinemanager.core.design.TextSecondary
import com.restaurant.offlinemanager.core.utils.MoneyFormatter
import com.restaurant.offlinemanager.core.utils.NumberFormatter
import com.restaurant.offlinemanager.core.utils.PersianDateFormatter
import com.restaurant.offlinemanager.data.local.entity.BankCardEntity
import com.restaurant.offlinemanager.data.local.entity.ExpenseCategory
import com.restaurant.offlinemanager.data.local.entity.ExpenseEntity
import com.restaurant.offlinemanager.data.local.entity.PaymentMethod
import com.restaurant.offlinemanager.domain.model.ProjectPaymentInput
import com.restaurant.offlinemanager.domain.model.SupplierPaymentInput
import com.restaurant.offlinemanager.domain.model.label
import com.restaurant.offlinemanager.domain.model.maskCardNumber
import com.restaurant.offlinemanager.ui.AppUiState

@Composable
fun FinanceDashboardScreen(
    state: AppUiState,
    onAddProjectPayment: () -> Unit,
    onAddSupplierPayment: () -> Unit,
    onAddBankCard: () -> Unit,
    onAddExpense: () -> Unit,
    modifier: Modifier = Modifier
) {
    var tab by remember { mutableIntStateOf(0) }
    val tabs = listOf("مطالبات پروژه‌ها", "بدهی تامین‌کنندگان", "کارت‌های بانکی", "پرداخت‌ها", "هزینه‌ها")
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("جمع مطالبات", MoneyFormatter.format(state.dashboard.projectReceivablesTotal), Icons.Outlined.Payments, AppOrange, Modifier.weight(1f))
                StatCard("بدهی تامین‌کننده", MoneyFormatter.format(state.dashboard.supplierDebtsTotal), Icons.Outlined.Payments, AppRed, Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("موجودی کارت‌ها", MoneyFormatter.format(state.dashboard.bankCardsTotalBalance), Icons.Outlined.AccountBalanceWallet, AppCyan, Modifier.weight(1f))
                StatCard("خریدهای ماه", MoneyFormatter.format(state.dashboard.monthPurchasesTotal), Icons.Outlined.Payments, AppPurple, Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("دریافت‌های ماه", MoneyFormatter.format(state.dashboard.monthReceivedTotal), Icons.Outlined.Payments, AppGreen, Modifier.weight(1f))
                StatCard("هزینه‌های ماه", MoneyFormatter.format(state.dashboard.monthExpensesTotal), Icons.Outlined.Payments, AppRed, Modifier.weight(1f))
            }
        }
        item {
            ScrollableTabRow(selectedTabIndex = tab, edgePadding = 0.dp, containerColor = com.restaurant.offlinemanager.core.design.SurfaceGlass, contentColor = Gold) {
                tabs.forEachIndexed { index, title -> Tab(selected = tab == index, onClick = { tab = index }, text = { Text(title) }) }
            }
        }
        when (tab) {
            0 -> {
                item { GoldPrimaryButton("ثبت دریافت", onClick = onAddProjectPayment, icon = Icons.Outlined.Add) }
                items(state.projectFinances, key = { it.project.id }) { finance ->
                    GlassCard(Modifier.fillMaxWidth()) {
                        Text(finance.project.name, color = TextPrimary, style = MaterialTheme.typography.titleLarge)
                        Text("تحویل: ${MoneyFormatter.format(finance.totalDelivered)}", color = TextSecondary)
                        Text("پرداخت‌شده: ${MoneyFormatter.format(finance.totalPaid)}", color = TextSecondary)
                        Text("مانده: ${MoneyFormatter.format(finance.receivable)}", color = Gold)
                    }
                }
            }
            1 -> {
                item { GoldPrimaryButton("ثبت پرداخت", onClick = onAddSupplierPayment, icon = Icons.Outlined.Add) }
                items(state.supplierDebts, key = { it.supplier.id }) { debt ->
                    GlassCard(Modifier.fillMaxWidth()) {
                        Text(debt.supplier.name, color = TextPrimary, style = MaterialTheme.typography.titleLarge)
                        Text("خرید نسیه: ${MoneyFormatter.format(debt.totalCreditPurchases)}", color = TextSecondary)
                        Text("پرداخت‌شده: ${MoneyFormatter.format(debt.totalPaid)}", color = TextSecondary)
                        Text("مانده: ${MoneyFormatter.format(debt.remaining)}", color = Gold)
                    }
                }
            }
            2 -> {
                item { GoldPrimaryButton("افزودن کارت بانکی", onClick = onAddBankCard, icon = Icons.Outlined.Add) }
                items(state.bankBalances, key = { it.card.id }) { balance ->
                    GlassCard(Modifier.fillMaxWidth()) {
                        Text(balance.card.title, color = TextPrimary, style = MaterialTheme.typography.titleLarge)
                        Text("${balance.card.bankName.orEmpty()} • ${maskCardNumber(balance.card.cardNumber)}", color = TextSecondary)
                        Text(MoneyFormatter.format(balance.balance), color = Gold, style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
            3 -> {
                items(state.snapshot.projectPayments, key = { "p${it.id}" }) { payment ->
                    val project = state.snapshot.projects.firstOrNull { it.id == payment.projectId }
                    PaymentCard("دریافت پروژه", project?.name.orEmpty(), payment.amount, payment.date, payment.method.label(), AppGreen)
                }
                items(state.snapshot.supplierPayments, key = { "s${it.id}" }) { payment ->
                    val supplier = state.snapshot.suppliers.firstOrNull { it.id == payment.supplierId }
                    PaymentCard("پرداخت تامین‌کننده", supplier?.name.orEmpty(), payment.amount, payment.date, payment.method.label(), AppOrange)
                }
            }
            4 -> {
                item { GoldPrimaryButton("افزودن هزینه", onClick = onAddExpense, icon = Icons.Outlined.Add) }
                items(state.snapshot.expenses, key = { it.id }) { expense ->
                    PaymentCard(expense.category.label(), expense.title, expense.amount, expense.date, "هزینه", AppRed)
                }
            }
        }
        item { Spacer(Modifier.height(20.dp)) }
    }
}

@Composable
private fun PaymentCard(kind: String, name: String, amount: Long, date: Long, method: String, color: androidx.compose.ui.graphics.Color) {
    GlassCard(Modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f)) {
                Text(name, color = TextPrimary, style = MaterialTheme.typography.titleLarge)
                Text("${PersianDateFormatter.format(date)} • $method", color = TextSecondary)
            }
            Column {
                StatusChip(kind, color)
                Text(MoneyFormatter.format(amount), color = Gold)
            }
        }
    }
}

@Composable
fun ProjectPaymentFormScreen(
    state: AppUiState,
    preselectedProjectId: Long?,
    onSave: (ProjectPaymentInput) -> Unit,
    modifier: Modifier = Modifier
) {
    var project by remember { mutableStateOf(state.snapshot.projects.firstOrNull { it.id == preselectedProjectId } ?: state.snapshot.projects.firstOrNull()) }
    var card by remember { mutableStateOf(state.snapshot.bankCards.firstOrNull()) }
    var amount by remember { mutableStateOf("") }
    var method by remember { mutableStateOf(PaymentMethod.BANK_TRANSFER) }
    var notes by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    PaymentFormLayout(
        title = "ثبت دریافت از پروژه",
        partyLabel = "پروژه",
        parties = state.snapshot.projects,
        party = project,
        partyName = { it.name },
        onParty = { project = it },
        cards = state.snapshot.bankCards,
        card = card,
        onCard = { card = it },
        amount = amount,
        onAmount = { amount = it },
        method = method,
        onMethod = { method = it },
        notes = notes,
        onNotes = { notes = it },
        error = error,
        onSave = {
            val value = MoneyFormatter.parse(amount)
            error = when {
                project == null -> "پروژه را انتخاب کنید"
                value <= 0 -> "مبلغ باید بیشتر از صفر باشد"
                else -> null
            }
            val p = project
            if (error == null && p != null) {
                onSave(ProjectPaymentInput(p.id, card?.id, value, PersianDateFormatter.todayStartMillis(), method, notes))
            }
        },
        modifier = modifier
    )
}

@Composable
fun SupplierPaymentFormScreen(
    state: AppUiState,
    onSave: (SupplierPaymentInput) -> Unit,
    modifier: Modifier = Modifier
) {
    var supplier by remember { mutableStateOf(state.snapshot.suppliers.firstOrNull()) }
    var card by remember { mutableStateOf(state.snapshot.bankCards.firstOrNull()) }
    var amount by remember { mutableStateOf("") }
    var method by remember { mutableStateOf(PaymentMethod.CARD_TO_CARD) }
    var notes by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    PaymentFormLayout(
        title = "ثبت پرداخت به تامین‌کننده",
        partyLabel = "تامین‌کننده",
        parties = state.snapshot.suppliers,
        party = supplier,
        partyName = { it.name },
        onParty = { supplier = it },
        cards = state.snapshot.bankCards,
        card = card,
        onCard = { card = it },
        amount = amount,
        onAmount = { amount = it },
        method = method,
        onMethod = { method = it },
        notes = notes,
        onNotes = { notes = it },
        error = error,
        onSave = {
            val value = MoneyFormatter.parse(amount)
            error = when {
                supplier == null -> "تامین‌کننده را انتخاب کنید"
                value <= 0 -> "مبلغ باید بیشتر از صفر باشد"
                else -> null
            }
            val s = supplier
            if (error == null && s != null) {
                onSave(SupplierPaymentInput(s.id, card?.id, value, PersianDateFormatter.todayStartMillis(), method, notes))
            }
        },
        modifier = modifier
    )
}

@Composable
private fun <T> PaymentFormLayout(
    title: String,
    partyLabel: String,
    parties: List<T>,
    party: T?,
    partyName: (T) -> String,
    onParty: (T) -> Unit,
    cards: List<BankCardEntity>,
    card: BankCardEntity?,
    onCard: (BankCardEntity) -> Unit,
    amount: String,
    onAmount: (String) -> Unit,
    method: PaymentMethod,
    onMethod: (PaymentMethod) -> Unit,
    notes: String,
    onNotes: (String) -> Unit,
    error: String?,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { SectionHeader(title) }
        item { OptionSelector(partyLabel, parties, party, partyName, onSelected = onParty) }
        item { MoneyField(amount, onAmount, "مبلغ") }
        item { OptionSelector("روش پرداخت", PaymentMethod.entries, method, { it.label() }, onSelected = onMethod) }
        item { OptionSelector("کارت بانکی", cards, card, { it.title }, onSelected = onCard) }
        item { DarkOutlinedTextField(notes, onNotes, "توضیحات", singleLine = false) }
        if (error != null) item { Text(error, color = AppRed) }
        item { GoldPrimaryButton("ثبت", onClick = onSave, icon = Icons.Outlined.Save) }
    }
}

@Composable
fun BankCardFormScreen(
    onSave: (BankCardEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf("") }
    var bank by remember { mutableStateOf("") }
    var owner by remember { mutableStateOf("") }
    var number by remember { mutableStateOf("") }
    var balance by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val now = PersianDateFormatter.nowMillis()
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { SectionHeader("افزودن کارت بانکی") }
        item { DarkOutlinedTextField(title, { title = it }, "عنوان کارت") }
        item { DarkOutlinedTextField(bank, { bank = it }, "نام بانک") }
        item { DarkOutlinedTextField(owner, { owner = it }, "نام صاحب کارت") }
        item { DarkOutlinedTextField(number, { number = it }, "شماره کارت", keyboardType = KeyboardType.Number) }
        item { MoneyField(balance, { balance = it }, "موجودی اولیه") }
        if (error != null) item { Text(error.orEmpty(), color = AppRed) }
        item {
            GoldPrimaryButton("ذخیره", onClick = {
                val initial = MoneyFormatter.parse(balance)
                error = if (title.isBlank()) "عنوان کارت الزامی است" else null
                if (error == null) onSave(BankCardEntity(title = title, ownerName = owner, bankName = bank, cardNumber = number, initialBalance = initial, isActive = true, createdAt = now, updatedAt = now))
            }, icon = Icons.Outlined.Save)
        }
    }
}

@Composable
fun ExpenseFormScreen(
    state: AppUiState,
    onSave: (ExpenseEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(ExpenseCategory.OTHER) }
    var amount by remember { mutableStateOf("") }
    var card by remember { mutableStateOf(state.snapshot.bankCards.firstOrNull()) }
    var notes by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val now = PersianDateFormatter.nowMillis()
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { SectionHeader("ثبت هزینه") }
        item { DarkOutlinedTextField(title, { title = it }, "عنوان هزینه") }
        item { OptionSelector("دسته‌بندی", ExpenseCategory.entries, category, { it.label() }) { category = it } }
        item { MoneyField(amount, { amount = it }, "مبلغ") }
        item { OptionSelector("کارت بانکی", state.snapshot.bankCards, card, { it.title }) { card = it } }
        item { DarkOutlinedTextField(notes, { notes = it }, "توضیحات", singleLine = false) }
        if (error != null) item { Text(error.orEmpty(), color = AppRed) }
        item {
            GoldPrimaryButton("ذخیره", onClick = {
                val value = MoneyFormatter.parse(amount)
                error = when {
                    title.isBlank() -> "عنوان هزینه الزامی است"
                    value <= 0 -> "مبلغ باید بیشتر از صفر باشد"
                    else -> null
                }
                if (error == null) onSave(ExpenseEntity(title = title, category = category, amount = value, date = PersianDateFormatter.todayStartMillis(), bankCardId = card?.id, notes = notes, createdAt = now, updatedAt = now))
            }, icon = Icons.Outlined.Save)
        }
    }
}
