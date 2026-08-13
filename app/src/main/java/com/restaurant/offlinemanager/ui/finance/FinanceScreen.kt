package com.restaurant.offlinemanager.ui.finance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.restaurant.offlinemanager.core.design.BottomSheetActionMenu
import com.restaurant.offlinemanager.core.design.AppCyan
import com.restaurant.offlinemanager.core.design.AppGreen
import com.restaurant.offlinemanager.core.design.AppOrange
import com.restaurant.offlinemanager.core.design.AppPurple
import com.restaurant.offlinemanager.core.design.AppRed
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
import com.restaurant.offlinemanager.core.design.MetricProgressBar
import com.restaurant.offlinemanager.core.design.MoneyField
import com.restaurant.offlinemanager.core.design.MoneyText
import com.restaurant.offlinemanager.core.design.MotionVisibility
import com.restaurant.offlinemanager.core.design.OptionSelector
import com.restaurant.offlinemanager.core.design.PersianDateText
import com.restaurant.offlinemanager.core.design.SectionHeader
import com.restaurant.offlinemanager.core.design.SecondaryGlassButton
import com.restaurant.offlinemanager.core.design.SheetAction
import com.restaurant.offlinemanager.core.design.StatCard
import com.restaurant.offlinemanager.core.design.StatusChip
import com.restaurant.offlinemanager.core.design.TextMuted
import com.restaurant.offlinemanager.core.design.TextPrimary
import com.restaurant.offlinemanager.core.design.TextSecondary
import com.restaurant.offlinemanager.core.utils.MoneyFormatter
import com.restaurant.offlinemanager.core.utils.PersianDateFormatter
import com.restaurant.offlinemanager.data.local.entity.BankCardEntity
import com.restaurant.offlinemanager.data.local.entity.ExpenseCategory
import com.restaurant.offlinemanager.data.local.entity.ExpenseEntity
import com.restaurant.offlinemanager.data.local.entity.PaymentMethod
import com.restaurant.offlinemanager.domain.model.BankCardBalance
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
    onEditBankCard: (Long) -> Unit,
    onAddExpense: () -> Unit,
    onEditProjectPayment: (Long) -> Unit,
    onEditSupplierPayment: (Long) -> Unit,
    onEditExpense: (Long) -> Unit,
    onDeleteBankCard: (Long) -> Unit,
    onDeleteProjectPayment: (Long) -> Unit,
    onDeleteSupplierPayment: (Long) -> Unit,
    onDeleteExpense: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var tab by remember { mutableIntStateOf(0) }
    var deleteProjectPaymentId by remember { mutableStateOf<Long?>(null) }
    var deleteSupplierPaymentId by remember { mutableStateOf<Long?>(null) }
    var deleteExpenseId by remember { mutableStateOf<Long?>(null) }
    var deleteBankCardId by remember { mutableStateOf<Long?>(null) }
    val tabs = listOf("مطالبات پروژه‌ها", "بدهی تامین‌کنندگان", "کارت‌های بانکی", "پرداخت‌ها", "هزینه‌ها")
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            GlassCard(Modifier.fillMaxWidth(), accent = AppPurple) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Outlined.AccountBalanceWallet, contentDescription = null, tint = AppPurple)
                    Column(Modifier.weight(1f)) {
                        Text("وضعیت مالی امروز", color = TextSecondary)
                        MoneyText(state.dashboard.projectReceivablesTotal, style = MaterialTheme.typography.headlineMedium)
                        Text("جمع مطالبات پروژه‌ها", color = TextMuted)
                    }
                    StatusChip("مالی", Gold)
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("جمع مطالبات", MoneyFormatter.format(state.dashboard.projectReceivablesTotal), Icons.Outlined.Payments, AppPurple, Modifier.weight(1f))
                StatCard("بدهی تامین‌کننده", MoneyFormatter.format(state.dashboard.supplierDebtsTotal), Icons.Outlined.Payments, AppRed, Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("موجودی کارت‌ها", MoneyFormatter.format(state.dashboard.bankCardsTotalBalance), Icons.Outlined.AccountBalanceWallet, AppCyan, Modifier.weight(1f))
                StatCard("خریدهای ماه", MoneyFormatter.format(state.dashboard.monthPurchasesTotal), Icons.Outlined.Payments, AppOrange, Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("دریافت‌های ماه", MoneyFormatter.format(state.dashboard.monthReceivedTotal), Icons.Outlined.Payments, AppGreen, Modifier.weight(1f))
                StatCard("هزینه‌های ماه", MoneyFormatter.format(state.dashboard.monthExpensesTotal), Icons.Outlined.Payments, AppRed, Modifier.weight(1f))
            }
        }
        item { FilterChipRow(tabs, tabs[tab], { tab = tabs.indexOf(it) }) }
        when (tab) {
            0 -> {
                val receivables = state.projectFinances.filter { it.receivable > 0 }
                item { GoldPrimaryButton("ثبت دریافت", onClick = onAddProjectPayment, icon = Icons.Outlined.Add) }
                if (receivables.isEmpty()) {
                    item { EmptyState("مطالبه‌ای وجود ندارد", "پس از ثبت پروژه و وعده، مانده پروژه‌ها اینجا نمایش داده می‌شود.") }
                } else {
                    items(receivables, key = { it.project.id }) { finance ->
                        FinanceRow(
                            title = finance.project.name,
                            subtitle = "تحویل ${MoneyFormatter.format(finance.totalDelivered)} • پرداخت ${MoneyFormatter.format(finance.totalPaid)}",
                            amount = finance.receivable,
                            chip = "مانده",
                            color = AppPurple
                        )
                    }
                }
            }
            1 -> {
                val debts = state.supplierDebts.filter { it.remaining > 0 }
                item { GoldPrimaryButton("ثبت پرداخت", onClick = onAddSupplierPayment, icon = Icons.Outlined.Add) }
                if (debts.isEmpty()) {
                    item { EmptyState("بدهی تامین‌کننده‌ای وجود ندارد", "خریدهای نسیه و پرداخت‌های تامین‌کننده بعد از ثبت اینجا دیده می‌شوند.") }
                } else {
                    items(debts, key = { it.supplier.id }) { debt ->
                        FinanceRow(
                            title = debt.supplier.name,
                            subtitle = "خرید نسیه ${MoneyFormatter.format(debt.totalCreditPurchases)} • پرداخت ${MoneyFormatter.format(debt.totalPaid)}",
                            amount = debt.remaining,
                            chip = "بدهی",
                            color = AppOrange
                        )
                    }
                }
            }
            2 -> {
                item { GoldPrimaryButton("افزودن کارت بانکی", onClick = onAddBankCard, icon = Icons.Outlined.Add) }
                if (state.bankBalances.isEmpty()) {
                    item { EmptyState("کارت بانکی ثبت نشده", "برای پیگیری پرداخت‌ها و موجودی، کارت بانکی اضافه کنید.") }
                } else {
                    val maxBalance = state.bankBalances.maxOfOrNull { it.balance.coerceAtLeast(0L) }?.coerceAtLeast(1L) ?: 1L
                    items(state.bankBalances, key = { it.card.id }) { balance ->
                        BankCardVisual(balance, maxBalance, onEditBankCard, onDeleteBankCard = { deleteBankCardId = it })
                    }
                }
            }
            3 -> {
                if (state.snapshot.projectPayments.isEmpty() && state.snapshot.supplierPayments.isEmpty()) {
                    item { EmptyState("پرداختی ثبت نشده", "دریافت از پروژه و پرداخت به تامین‌کننده در این بخش تجمیع می‌شود.") }
                } else {
                    val payments = (
                        state.snapshot.projectPayments.map { payment ->
                            PaymentListItem(
                                key = "project-${payment.id}",
                                kind = "دریافت پروژه",
                                name = state.snapshot.projects.firstOrNull { it.id == payment.projectId }?.name.orEmpty(),
                                amount = payment.amount,
                                date = payment.date,
                                method = payment.method.label(),
                                isProjectPayment = true,
                                id = payment.id
                            )
                        } + state.snapshot.supplierPayments.map { payment ->
                            PaymentListItem(
                                key = "supplier-${payment.id}",
                                kind = "پرداخت تامین‌کننده",
                                name = state.snapshot.suppliers.firstOrNull { it.id == payment.supplierId }?.name.orEmpty(),
                                amount = payment.amount,
                                date = payment.date,
                                method = payment.method.label(),
                                isProjectPayment = false,
                                id = payment.id
                            )
                        }
                    ).sortedWith(compareByDescending<PaymentListItem> { it.date }.thenByDescending { it.id })
                    items(payments, key = { it.key }) { payment ->
                        PaymentCard(
                            kind = payment.kind,
                            name = payment.name,
                            amount = payment.amount,
                            date = payment.date,
                            method = payment.method,
                            color = if (payment.isProjectPayment) AppGreen else AppOrange,
                            onEdit = {
                                if (payment.isProjectPayment) onEditProjectPayment(payment.id)
                                else onEditSupplierPayment(payment.id)
                            },
                            onDelete = {
                                if (payment.isProjectPayment) deleteProjectPaymentId = payment.id
                                else deleteSupplierPaymentId = payment.id
                            }
                        )
                    }
                }
            }
            4 -> {
                item { GoldPrimaryButton("افزودن هزینه", onClick = onAddExpense, icon = Icons.Outlined.Add) }
                if (state.snapshot.expenses.isEmpty()) {
                    item { EmptyState("هزینه‌ای ثبت نشده", "هزینه‌های جاری کسب‌وکار بعد از ثبت اینجا دیده می‌شود.") }
                } else {
                    items(state.snapshot.expenses, key = { it.id }) { expense ->
                        PaymentCard(
                            kind = expense.category.label(),
                            name = expense.title,
                            amount = expense.amount,
                            date = expense.date,
                            method = "هزینه",
                            color = AppRed,
                            onEdit = { onEditExpense(expense.id) },
                            onDelete = { deleteExpenseId = expense.id }
                        )
                    }
                }
            }
        }
        item { Spacer(Modifier.height(20.dp)) }
    }
    deleteProjectPaymentId?.let { id ->
        ConfirmDialog(
            title = "حذف دریافت پروژه",
            message = "این دریافت از محاسبه مطالبات و موجودی کارت حذف می‌شود.",
            confirmText = "حذف",
            onConfirm = {
                deleteProjectPaymentId = null
                onDeleteProjectPayment(id)
            },
            onDismiss = { deleteProjectPaymentId = null }
        )
    }
    deleteSupplierPaymentId?.let { id ->
        ConfirmDialog(
            title = "حذف پرداخت تامین‌کننده",
            message = "این پرداخت از محاسبه بدهی تامین‌کننده و موجودی کارت حذف می‌شود.",
            confirmText = "حذف",
            onConfirm = {
                deleteSupplierPaymentId = null
                onDeleteSupplierPayment(id)
            },
            onDismiss = { deleteSupplierPaymentId = null }
        )
    }
    deleteExpenseId?.let { id ->
        ConfirmDialog(
            title = "حذف هزینه",
            message = "این هزینه از گزارش‌ها و موجودی کارت حذف می‌شود.",
            confirmText = "حذف",
            onConfirm = {
                deleteExpenseId = null
                onDeleteExpense(id)
            },
            onDismiss = { deleteExpenseId = null }
        )
    }
    deleteBankCardId?.let { id ->
        ConfirmDialog(
            title = "حذف کارت بانکی",
            message = "اگر این کارت در پرداخت، خرید یا هزینه استفاده شده باشد، برای حفظ سوابق فقط غیرفعال می‌شود.",
            confirmText = "حذف",
            onConfirm = {
                deleteBankCardId = null
                onDeleteBankCard(id)
            },
            onDismiss = { deleteBankCardId = null }
        )
    }
}

@Composable
private fun BankCardVisual(
    balance: BankCardBalance,
    maxBalance: Long,
    onEditBankCard: (Long) -> Unit,
    onDeleteBankCard: (Long) -> Unit
) {
    val accent = if (balance.balance >= 0) AppCyan else AppRed
    GlassCard(Modifier.fillMaxWidth(), accent = accent) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(156.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            AppPurple.copy(alpha = 0.48f),
                            accent.copy(alpha = 0.34f),
                            Gold.copy(alpha = 0.20f)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(balance.card.title, color = TextPrimary, style = MaterialTheme.typography.titleLarge)
                        Text(balance.card.bankName.orEmpty().ifBlank { "کارت بانکی" }, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                    }
                    StatusChip(if (balance.card.isActive) "فعال" else "غیرفعال", if (balance.card.isActive) AppGreen else TextMuted)
                }
                Text(
                    maskCardNumber(balance.card.cardNumber),
                    color = TextPrimary,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Column(Modifier.weight(1f)) {
                        Text("دارنده", color = TextMuted, style = MaterialTheme.typography.bodySmall)
                        Text(balance.card.ownerName.orEmpty().ifBlank { "ثبت نشده" }, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("موجودی", color = TextMuted, style = MaterialTheme.typography.bodySmall)
                        MoneyText(balance.balance, color = Gold, style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        MetricProgressBar(
            label = "سهم از بیشترین موجودی کارت‌ها",
            value = balance.balance.coerceAtLeast(0L).toFloat(),
            max = maxBalance.toFloat().coerceAtLeast(1f),
            accent = accent,
            valueLabel = MoneyFormatter.format(balance.balance)
        )
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SecondaryGlassButton("ویرایش کارت", onClick = { onEditBankCard(balance.card.id) }, icon = Icons.Outlined.Save, accent = accent, modifier = Modifier.weight(1f))
            DangerButton("حذف", onClick = { onDeleteBankCard(balance.card.id) }, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun FinanceRow(title: String, subtitle: String, amount: Long, chip: String, color: androidx.compose.ui.graphics.Color) {
    GlassCard(Modifier.fillMaxWidth(), accent = color) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
            }
            Column(horizontalAlignment = Alignment.End) {
                StatusChip(chip, color)
                MoneyText(amount.coerceAtLeast(0), style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentCard(
    kind: String,
    name: String,
    amount: Long,
    date: Long,
    method: String,
    color: androidx.compose.ui.graphics.Color,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null
) {
    var showActions by remember { mutableStateOf(false) }
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart && onDelete != null) {
                onDelete()
            }
            false
        }
    )
    val cardContent: @Composable () -> Unit = {
        GlassCard(Modifier.fillMaxWidth(), accent = color) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(name, color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                    Text(method, color = TextSecondary)
                    PersianDateText(date)
                }
                Column(horizontalAlignment = Alignment.End) {
                    StatusChip(kind, color)
                    MoneyText(amount, style = MaterialTheme.typography.titleMedium)
                }
            }
            if (onEdit != null || onDelete != null) {
                Spacer(Modifier.height(8.dp))
                SecondaryGlassButton("عملیات", onClick = { showActions = true }, icon = Icons.Outlined.MoreVert, accent = color)
            }
        }
    }
    if (onDelete != null) {
        SwipeToDismissBox(
            state = dismissState,
            enableDismissFromStartToEnd = false,
            backgroundContent = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(22.dp))
                        .background(AppRed.copy(alpha = 0.20f))
                        .padding(18.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Icon(Icons.Outlined.Delete, contentDescription = "حذف", tint = AppRed)
                }
            },
            content = { cardContent() }
        )
    } else {
        cardContent()
    }
    if (showActions && (onEdit != null || onDelete != null)) {
        BottomSheetActionMenu(
            title = kind,
            actions = buildList {
                if (onEdit != null) add(SheetAction("ویرایش", Icons.Outlined.Edit, AppCyan, onEdit))
                if (onDelete != null) add(SheetAction("حذف", Icons.Outlined.Delete, AppRed, onDelete))
            },
            onDismiss = { showActions = false }
        )
    }
}

@Composable
private fun EmptyPaymentPrerequisite(
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { SectionHeader("ثبت پرداخت") }
        item { EmptyState(title, message) }
    }
}

@Composable
fun ProjectPaymentFormScreen(
    state: AppUiState,
    preselectedProjectId: Long?,
    paymentId: Long?,
    onSave: (ProjectPaymentInput) -> Unit,
    modifier: Modifier = Modifier
) {
    val editing = state.snapshot.projectPayments.firstOrNull { it.id == paymentId }
    val projectOptions = state.projectFinances
        .filter { it.receivable > 0 || it.project.id == editing?.projectId }
        .map { it.project }
    val cards = state.snapshot.bankCards.filter { it.isActive || it.id == editing?.bankCardId }
    if (projectOptions.isEmpty()) {
        EmptyPaymentPrerequisite("مطالبه‌ای برای دریافت وجود ندارد", "بعد از ثبت وعده برای پروژه، دریافت قابل ثبت می‌شود.", modifier)
        return
    }
    var project by remember(editing?.id, projectOptions) { mutableStateOf(projectOptions.firstOrNull { it.id == editing?.projectId } ?: projectOptions.firstOrNull { it.id == preselectedProjectId } ?: projectOptions.firstOrNull()) }
    var card by remember(editing?.id, cards) { mutableStateOf<BankCardEntity?>(cards.firstOrNull { it.id == editing?.bankCardId } ?: cards.firstOrNull()) }
    var amount by remember(editing?.id) { mutableStateOf(editing?.amount?.toString().orEmpty()) }
    var method by remember(editing?.id) { mutableStateOf(editing?.method ?: PaymentMethod.BANK_TRANSFER) }
    var date by remember(editing?.id) { mutableLongStateOf(editing?.date ?: PersianDateFormatter.todayStartMillis()) }
    var notes by remember(editing?.id) { mutableStateOf(editing?.notes.orEmpty()) }
    var error by remember { mutableStateOf<String?>(null) }
    val remaining = state.projectFinances
        .firstOrNull { it.project.id == project?.id }
        ?.receivable
        ?.coerceAtLeast(0) ?: 0L
    val editableRemaining = remaining + (editing?.takeIf { it.projectId == project?.id }?.amount ?: 0L)
    PaymentFormLayout(
        title = if (editing == null) "ثبت دریافت از پروژه" else "ویرایش دریافت از پروژه",
        partyLabel = "پروژه",
        parties = projectOptions,
        party = project,
        partyName = { it.name },
        onParty = { project = it },
        cards = cards,
        card = card,
        onCard = { card = it },
        amount = amount,
        onAmount = { amount = it },
        method = method,
        onMethod = { method = it },
        date = date,
        onDate = { date = it },
        notes = notes,
        onNotes = { notes = it },
        error = error,
        balanceLabel = "مانده مطالبات",
        balanceAmount = editableRemaining,
        onSave = {
            val value = MoneyFormatter.parse(amount)
            error = when {
                project == null -> "پروژه را انتخاب کنید"
                value <= 0 -> "مبلغ باید بیشتر از صفر باشد"
                value > editableRemaining -> "مبلغ پرداختی بیشتر از مانده است"
                method != PaymentMethod.CASH && card == null -> "برای پرداخت غیرنقدی کارت بانکی را انتخاب کنید"
                else -> null
            }
            val p = project
            if (error == null && p != null) {
                onSave(ProjectPaymentInput(id = editing?.id ?: 0, projectId = p.id, bankCardId = if (method == PaymentMethod.CASH) null else card?.id, amount = value, date = date, method = method, notes = notes))
            }
        },
        modifier = modifier
    )
}

@Composable
fun SupplierPaymentFormScreen(
    state: AppUiState,
    paymentId: Long?,
    onSave: (SupplierPaymentInput) -> Unit,
    modifier: Modifier = Modifier
) {
    val editing = state.snapshot.supplierPayments.firstOrNull { it.id == paymentId }
    val supplierOptions = state.supplierDebts
        .filter { it.remaining > 0 || it.supplier.id == editing?.supplierId }
        .map { it.supplier }
    val cards = state.snapshot.bankCards.filter { it.isActive || it.id == editing?.bankCardId }
    if (supplierOptions.isEmpty()) {
        EmptyPaymentPrerequisite("بدهی تامین‌کننده‌ای برای پرداخت وجود ندارد", "بعد از ثبت خرید نسیه، پرداخت تامین‌کننده قابل ثبت می‌شود.", modifier)
        return
    }
    var supplier by remember(editing?.id, supplierOptions) { mutableStateOf(supplierOptions.firstOrNull { it.id == editing?.supplierId } ?: supplierOptions.firstOrNull()) }
    var card by remember(editing?.id, cards) { mutableStateOf<BankCardEntity?>(cards.firstOrNull { it.id == editing?.bankCardId } ?: cards.firstOrNull()) }
    var amount by remember(editing?.id) { mutableStateOf(editing?.amount?.toString().orEmpty()) }
    var method by remember(editing?.id) { mutableStateOf(editing?.method ?: PaymentMethod.CARD_TO_CARD) }
    var date by remember(editing?.id) { mutableLongStateOf(editing?.date ?: PersianDateFormatter.todayStartMillis()) }
    var notes by remember(editing?.id) { mutableStateOf(editing?.notes.orEmpty()) }
    var error by remember { mutableStateOf<String?>(null) }
    val purchaseOptions = state.snapshot.purchases.filter { purchase ->
        if (purchase.supplierId != supplier?.id) return@filter false
        val allocated = state.snapshot.supplierPayments
            .filter { it.purchaseId == purchase.id && it.id != editing?.id }
            .sumOf { it.amount }
        purchase.totalAmount - purchase.paidAmount - allocated > 0 || purchase.id == editing?.purchaseId
    }
    var purchase by remember(editing?.id, supplier?.id, purchaseOptions) {
        mutableStateOf(purchaseOptions.firstOrNull { it.id == editing?.purchaseId })
    }
    val remaining = state.supplierDebts
        .firstOrNull { it.supplier.id == supplier?.id }
        ?.remaining
        ?.coerceAtLeast(0) ?: 0L
    val supplierEditableRemaining = remaining + (editing?.takeIf { it.supplierId == supplier?.id }?.amount ?: 0L)
    val invoiceEditableRemaining = purchase?.let { selected ->
        val allocated = state.snapshot.supplierPayments
            .filter { it.purchaseId == selected.id && it.id != editing?.id }
            .sumOf { it.amount }
        (selected.totalAmount - selected.paidAmount - allocated).coerceAtLeast(0)
    }
    val editableRemaining = invoiceEditableRemaining ?: supplierEditableRemaining
    PaymentFormLayout(
        title = if (editing == null) "ثبت پرداخت به تامین‌کننده" else "ویرایش پرداخت تامین‌کننده",
        partyLabel = "تامین‌کننده",
        parties = supplierOptions,
        party = supplier,
        partyName = { it.name },
        onParty = { supplier = it },
        cards = cards,
        card = card,
        onCard = { card = it },
        amount = amount,
        onAmount = { amount = it },
        method = method,
        onMethod = { method = it },
        date = date,
        onDate = { date = it },
        notes = notes,
        onNotes = { notes = it },
        error = error,
        balanceLabel = "مانده بدهی",
        balanceAmount = editableRemaining,
        additionalContent = {
            Spacer(Modifier.height(10.dp))
            OptionSelector(
                "تخصیص به فاکتور (اختیاری)",
                purchaseOptions,
                purchase,
                { invoice -> invoice.invoiceNumber?.let { "فاکتور $it" } ?: "فاکتور ${invoice.id}" },
                clearLabel = "پرداخت کلی تامین‌کننده",
                onClear = { purchase = null }
            ) { purchase = it }
        },
        onSave = {
            val value = MoneyFormatter.parse(amount)
            error = when {
                supplier == null -> "تامین‌کننده را انتخاب کنید"
                value <= 0 -> "مبلغ باید بیشتر از صفر باشد"
                value > editableRemaining -> "مبلغ پرداختی بیشتر از مانده است"
                method != PaymentMethod.CASH && card == null -> "برای پرداخت غیرنقدی کارت بانکی را انتخاب کنید"
                else -> null
            }
            val s = supplier
            if (error == null && s != null) {
                onSave(SupplierPaymentInput(id = editing?.id ?: 0, supplierId = s.id, bankCardId = if (method == PaymentMethod.CASH) null else card?.id, purchaseId = purchase?.id, amount = value, date = date, method = method, notes = notes))
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
    date: Long,
    onDate: (Long) -> Unit,
    notes: String,
    onNotes: (String) -> Unit,
    error: String?,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
    balanceLabel: String? = null,
    balanceAmount: Long? = null,
    additionalContent: @Composable ColumnScope.() -> Unit = {}
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { SectionHeader(title) }
        item {
            GlassCard(Modifier.fillMaxWidth(), accent = Gold) {
                OptionSelector(partyLabel, parties, party, partyName, onSelected = onParty)
                additionalContent()
                if (balanceLabel != null && balanceAmount != null) {
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(balanceLabel, color = TextSecondary, modifier = Modifier.weight(1f))
                        MoneyText(balanceAmount, style = MaterialTheme.typography.titleMedium)
                    }
                }
                Spacer(Modifier.height(10.dp))
                MoneyField(amount, onAmount, "مبلغ")
                Spacer(Modifier.height(10.dp))
                OptionSelector("روش پرداخت", PaymentMethod.entries, method, { it.label() }, onSelected = onMethod)
                MotionVisibility(method != PaymentMethod.CASH) {
                    Column {
                        Spacer(Modifier.height(10.dp))
                        OptionSelector("کارت بانکی", cards, card, { it.title }, onSelected = onCard)
                        if (cards.isEmpty()) {
                            Spacer(Modifier.height(6.dp))
                            Text("برای پرداخت غیرنقدی ابتدا یک کارت بانکی اضافه کنید.", color = AppRed)
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                LocalDateSelector("تاریخ", date, onDate)
                Spacer(Modifier.height(10.dp))
                DarkOutlinedTextField(notes, onNotes, "توضیحات", singleLine = false)
            }
        }
        if (error != null) item { Text(error, color = AppRed) }
        item { FormActionFooter("ثبت", onClick = onSave, icon = Icons.Outlined.Save) }
    }
}

@Composable
fun BankCardFormScreen(
    state: AppUiState,
    cardId: Long?,
    onSave: (BankCardEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val editing = state.snapshot.bankCards.firstOrNull { it.id == cardId }
    var title by remember(editing?.id) { mutableStateOf(editing?.title.orEmpty()) }
    var bank by remember(editing?.id) { mutableStateOf(editing?.bankName.orEmpty()) }
    var owner by remember(editing?.id) { mutableStateOf(editing?.ownerName.orEmpty()) }
    var number by remember(editing?.id) { mutableStateOf(editing?.cardNumber.orEmpty()) }
    var balance by remember(editing?.id) { mutableStateOf(editing?.initialBalance?.toString().orEmpty()) }
    var isActive by remember(editing?.id) { mutableStateOf(editing?.isActive ?: true) }
    var error by remember { mutableStateOf<String?>(null) }
    val now = PersianDateFormatter.nowMillis()
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { SectionHeader(if (editing == null) "افزودن کارت بانکی" else "ویرایش کارت بانکی") }
        item {
            GlassCard(Modifier.fillMaxWidth(), accent = Gold) {
                DarkOutlinedTextField(title, { title = it }, "عنوان کارت")
                Spacer(Modifier.height(10.dp))
                DarkOutlinedTextField(bank, { bank = it }, "نام بانک")
                Spacer(Modifier.height(10.dp))
                DarkOutlinedTextField(owner, { owner = it }, "نام صاحب کارت")
                Spacer(Modifier.height(10.dp))
                DarkOutlinedTextField(number, { number = it }, "شماره کارت", keyboardType = KeyboardType.Number)
                Spacer(Modifier.height(10.dp))
                MoneyField(balance, { balance = it }, "موجودی اولیه")
                Spacer(Modifier.height(10.dp))
                FilterChipRow(
                    options = listOf("فعال", "غیرفعال"),
                    selected = if (isActive) "فعال" else "غیرفعال",
                    onSelected = { isActive = it == "فعال" }
                )
            }
        }
        if (error != null) item { Text(error.orEmpty(), color = AppRed) }
        item {
            FormActionFooter("ذخیره", onClick = {
                val initial = MoneyFormatter.parse(balance)
                val cardDigits = number.filter(Char::isDigit)
                val existingDigits = editing?.cardNumber.orEmpty().filter(Char::isDigit)
                error = when {
                    title.isBlank() -> "عنوان کارت الزامی است"
                    cardDigits.isNotEmpty() && cardDigits.length != 16 && cardDigits != existingDigits -> "شماره کارت باید ۱۶ رقم باشد"
                    else -> null
                }
                if (error == null) {
                    onSave(
                        BankCardEntity(
                            id = editing?.id ?: 0,
                            title = title,
                            ownerName = owner,
                            bankName = bank,
                            cardNumber = number,
                            initialBalance = initial,
                            isActive = isActive,
                            notes = editing?.notes,
                            createdAt = editing?.createdAt ?: now,
                            updatedAt = now
                        )
                    )
                }
            }, icon = Icons.Outlined.Save)
        }
    }
}

private data class PaymentListItem(
    val key: String,
    val kind: String,
    val name: String,
    val amount: Long,
    val date: Long,
    val method: String,
    val isProjectPayment: Boolean,
    val id: Long
)

@Composable
fun ExpenseFormScreen(
    state: AppUiState,
    expenseId: Long?,
    onSave: (ExpenseEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val editing = state.snapshot.expenses.firstOrNull { it.id == expenseId }
    val cards = state.snapshot.bankCards.filter { it.isActive || it.id == editing?.bankCardId }
    var title by remember(editing?.id) { mutableStateOf(editing?.title.orEmpty()) }
    var category by remember(editing?.id) { mutableStateOf(editing?.category ?: ExpenseCategory.OTHER) }
    var amount by remember(editing?.id) { mutableStateOf(editing?.amount?.toString().orEmpty()) }
    var card by remember(editing?.id, cards) { mutableStateOf<BankCardEntity?>(cards.firstOrNull { it.id == editing?.bankCardId }) }
    var date by remember(editing?.id) { mutableLongStateOf(editing?.date ?: PersianDateFormatter.todayStartMillis()) }
    var notes by remember(editing?.id) { mutableStateOf(editing?.notes.orEmpty()) }
    var error by remember { mutableStateOf<String?>(null) }
    val now = PersianDateFormatter.nowMillis()
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { SectionHeader(if (editing == null) "ثبت هزینه" else "ویرایش هزینه") }
        item {
            GlassCard(Modifier.fillMaxWidth(), accent = Gold) {
                DarkOutlinedTextField(title, { title = it }, "عنوان هزینه")
                Spacer(Modifier.height(10.dp))
                OptionSelector("دسته‌بندی", ExpenseCategory.entries, category, { it.label() }) { category = it }
                Spacer(Modifier.height(10.dp))
                MoneyField(amount, { amount = it }, "مبلغ")
                Spacer(Modifier.height(10.dp))
                OptionSelector(
                    "کارت بانکی (اختیاری)",
                    cards,
                    card,
                    { it.title },
                    clearLabel = "بدون کارت",
                    onClear = { card = null }
                ) { card = it }
                Spacer(Modifier.height(10.dp))
                LocalDateSelector("تاریخ", date, { date = it })
                Spacer(Modifier.height(10.dp))
                DarkOutlinedTextField(notes, { notes = it }, "توضیحات", singleLine = false)
            }
        }
        if (error != null) item { Text(error.orEmpty(), color = AppRed) }
        item {
            FormActionFooter("ذخیره", onClick = {
                val value = MoneyFormatter.parse(amount)
                error = when {
                    title.isBlank() -> "عنوان هزینه الزامی است"
                    value <= 0 -> "مبلغ باید بیشتر از صفر باشد"
                    else -> null
                }
                if (error == null) {
                    onSave(
                        ExpenseEntity(
                            id = editing?.id ?: 0,
                            title = title,
                            category = category,
                            amount = value,
                            date = date,
                            bankCardId = card?.id,
                            notes = notes,
                            createdAt = editing?.createdAt ?: now,
                            updatedAt = now
                        )
                    )
                }
            }, icon = Icons.Outlined.Save)
        }
    }
}
