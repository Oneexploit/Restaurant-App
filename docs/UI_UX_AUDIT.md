# UI/UX Audit

## Scope

Reviewed the existing offline restaurant / corporate catering Android app across the shared Compose design system, navigation shell, Home, Projects, Meal Delivery, Warehouse, Purchases, Finance, Reports, Search, and Settings screens.

The audit focused on visual quality, RTL/Persian usability, screen structure, business workflows, accessibility, and day-to-day operator speed. The database, repositories, use cases, and ViewModels were intentionally left unchanged.

## Global Design System

### Visual Design Problems

- Problem: The premium palette was only partially tokenized, with missing mid-background, elevated surfaces, divider, gold border, and blue accent tokens.
  Why it hurts the user: Screens can drift visually and lose the premium dark/glass identity.
  Recommended fix: Expand `Colors.kt`, use one dark gradient background, and reuse surface/accent tokens.
  Files changed: `core/design/Colors.kt`, `core/design/AppTheme.kt`.
  Status: Implemented.

- Problem: Component coverage was incomplete. Several requested components existed only as local screen code or not at all.
  Why it hurts the user: Repeated UI becomes inconsistent and harder to maintain.
  Recommended fix: Add reusable components for scaffold, background, elevated cards, action cards, secondary/danger buttons, loading/error/empty states, bottom sheet actions, finance/report/material/purchase/list items.
  Files changed: `core/design/Components.kt`.
  Status: Implemented.

### Layout Problems

- Problem: Screen padding was inconsistent at 18dp while the target spec asked for 16dp.
  Why it hurts the user: Alignment feels slightly uneven across screens.
  Recommended fix: Standardize screen padding and reusable dimensions.
  Files changed: `core/design/Dimensions.kt`, screen files.
  Status: Implemented on redesigned screens.

- Problem: Some repeated controls did not enforce clear touch sizing.
  Why it hurts the user: Daily-use actions are harder on smaller phones.
  Recommended fix: Add `MinimumTouchTarget`, 56dp button/input heights, and larger bottom navigation constraints.
  Files changed: `core/design/Dimensions.kt`, `core/design/Components.kt`.
  Status: Implemented.

### Persian / RTL Problems

- Problem: Top bar subtitle used English product text.
  Why it hurts the user: It weakens the Persian-first experience.
  Recommended fix: Replace with Persian Jalali date context.
  Files changed: `core/design/Components.kt`.
  Status: Implemented.

- Problem: Deprecated non-auto-mirrored list/receipt icons were used in RTL context.
  Why it hurts the user: Directional icons can feel visually wrong in RTL.
  Recommended fix: Use AutoMirrored icons.
  Files changed: `core/navigation/Routes.kt`, `ui/home/HomeScreen.kt`, `ui/reports/ReportsScreen.kt`.
  Status: Implemented.

## Home / خانه

### Problems

- Problem: Quick actions did not include stock-out, even though خروج کالا is a core warehouse workflow.
  Why it hurts the user: Common daily operation required extra navigation.
  Recommended fix: Add خروج کالا beside ثبت وعده, خرید روزانه, ورود کالا, دریافت پروژه, and گزارش‌ها.
  Files changed: `ui/home/HomeScreen.kt`, `core/navigation/AppNavGraph.kt`.
  Status: Implemented.

- Problem: Low-stock warnings were present but not visually prioritized enough.
  Why it hurts the user: Inventory risk can be missed during a quick dashboard scan.
  Recommended fix: Add a prominent warning card before low-stock rows.
  Files changed: `ui/home/HomeScreen.kt`.
  Status: Implemented.

- Problem: Dashboard lacked recent business activity.
  Why it hurts the user: Owner cannot quickly understand what changed today.
  Recommended fix: Add recent meals, purchases, and project receipts sorted by date.
  Files changed: `ui/home/HomeScreen.kt`.
  Status: Implemented.

## Projects / پروژه‌ها

### Problems

- Problem: Project status filters duplicated active state and missed paused state.
  Why it hurts the user: Operators cannot reliably isolate stopped projects.
  Recommended fix: Use همه، فعال، متوقف، تسویه‌شده، آرشیو.
  Files changed: `ui/projects/ProjectsScreen.kt`.
  Status: Implemented.

- Problem: Project cards only had details action.
  Why it hurts the user: Registering meals or receipts required extra taps.
  Recommended fix: Add quick actions for جزئیات، ثبت وعده، ثبت دریافت.
  Files changed: `ui/projects/ProjectsScreen.kt`, `core/navigation/AppNavGraph.kt`.
  Status: Implemented.

- Problem: Add/edit project form was a long mixed form.
  Why it hurts the user: User has to parse identity, contact, contract, and status fields together.
  Recommended fix: Group into اطلاعات پروژه، اطلاعات تماس، قرارداد و وعده، وضعیت و توضیحات, plus preview summary.
  Files changed: `ui/projects/ProjectsScreen.kt`.
  Status: Implemented.

- Problem: Project detail lacked recent operational history.
  Why it hurts the user: The user could not verify recent deliveries/payments in context.
  Recommended fix: Add recent meal deliveries and recent project payments.
  Files changed: `ui/projects/ProjectsScreen.kt`.
  Status: Implemented.

## Meal Delivery / ثبت وعده

### Problems

- Problem: Project selector was not searchable.
  Why it hurts the user: Long project lists slow down meal entry.
  Recommended fix: Add a project search field before selector.
  Files changed: `ui/meals/MealDeliveryScreen.kt`.
  Status: Implemented.

- Problem: Quantity entry lacked quick defaults.
  Why it hurts the user: Common counts require repetitive typing.
  Recommended fix: Add نفرات پروژه, +۵, +۱۰ buttons.
  Files changed: `ui/meals/MealDeliveryScreen.kt`.
  Status: Implemented.

- Problem: Live calculation did not show the equation.
  Why it hurts the user: User has less confidence in the final amount.
  Recommended fix: Show تعداد × قیمت and total.
  Files changed: `ui/meals/MealDeliveryScreen.kt`.
  Status: Implemented.

## Warehouse / انبار

### Problems

- Problem: Inventory tab had tabs and list but weak summary coverage.
  Why it hurts the user: User cannot scan total value, item count, and risk quickly.
  Recommended fix: Add value, item count, low-stock count, transaction count, and in/out stats.
  Files changed: `ui/warehouse/WarehouseScreen.kt`.
  Status: Implemented.

- Problem: Warehouse/material/transaction tabs lacked helpful empty states.
  Why it hurts the user: Empty tabs can feel broken.
  Recommended fix: Add Persian empty-state cards per tab.
  Files changed: `ui/warehouse/WarehouseScreen.kt`.
  Status: Implemented.

- Problem: Stock-out/transfer did not block quantity greater than current stock.
  Why it hurts the user: Offline inventory can become inaccurate.
  Recommended fix: Show available stock and prevent invalid outbound transactions.
  Files changed: `ui/warehouse/WarehouseScreen.kt`.
  Status: Implemented.

- Problem: Inventory cards had no quick in/out actions.
  Why it hurts the user: More taps for the most common warehouse actions.
  Recommended fix: Add ورود and خروج buttons on material inventory cards.
  Files changed: `ui/warehouse/WarehouseScreen.kt`.
  Status: Implemented.

## Daily Purchases / خرید روزانه

### Problems

- Problem: Purchase list summary only highlighted today.
  Why it hurts the user: Monthly spend and credit pressure were not visible.
  Recommended fix: Add today, month, credit purchase, and invoice count metrics.
  Files changed: `ui/purchases/PurchasesScreen.kt`.
  Status: Implemented.

- Problem: Invoice save summary was too minimal.
  Why it hurts the user: Operator cannot double-check discount/payment state before save.
  Recommended fix: Add discount and payment status to final summary.
  Files changed: `ui/purchases/PurchasesScreen.kt`.
  Status: Implemented.

## Finance / مالی

### Problems

- Problem: Several finance tabs had no empty state.
  Why it hurts the user: Empty state gives no instruction for next action.
  Recommended fix: Add empty cards for receivables, debts, bank cards, payments, and expenses.
  Files changed: `ui/finance/FinanceScreen.kt`.
  Status: Implemented.

- Problem: Payment forms did not show remaining balance.
  Why it hurts the user: Accidental overpayment is easier.
  Recommended fix: Show remaining receivable/debt and block amount greater than remaining where known.
  Files changed: `ui/finance/FinanceScreen.kt`.
  Status: Implemented.

## Reports / گزارش‌ها

### Problems

- Problem: Reports were chart/export oriented but not actionable enough.
  Why it hurts the user: Owner must interpret raw metrics manually.
  Recommended fix: Add insight cards for best revenue project, biggest debtor project, biggest supplier debt, and low-stock items.
  Files changed: `ui/reports/ReportsScreen.kt`.
  Status: Implemented.

## Search / جستجوی عمومی

### Problems

- Problem: Search results were ungrouped.
  Why it hurts the user: Mixed result types are hard to scan.
  Recommended fix: Add type filters and group results by type.
  Files changed: `ui/search/SearchScreen.kt`.
  Status: Implemented.

## Settings / تنظیمات

### Problems

- Problem: Restore backup had no confirmation.
  Why it hurts the user: Restore is a high-impact action.
  Recommended fix: Add confirmation dialog before file picker.
  Files changed: `ui/settings/SettingsScreen.kt`.
  Status: Implemented.

- Problem: Settings missed business info/export guidance sections.
  Why it hurts the user: User has less confidence in app purpose and available outputs.
  Recommended fix: Add business info and CSV output guidance cards.
  Files changed: `ui/settings/SettingsScreen.kt`.
  Status: Implemented.

## Accessibility

- Problem: Some compact controls risked small touch targets and clipped labels.
  Why it hurts the user: Daily operators need fast, reliable tapping on small phones.
  Recommended fix: Standardize button/input heights, bottom navigation height, chip/card padding, and one-line ellipsis behavior.
  Files changed: `core/design/Dimensions.kt`, `core/design/Components.kt`, screen files.
  Status: Implemented.

## Remaining Recommendations

- Add true sticky bottom save bars for long forms using a custom scaffold slot.
- Add persisted per-screen filters through ViewModel state if users frequently navigate away and back.
- Add richer chart labels and date range filters for report screens.
- Add editable business profile fields if a future schema/settings extension is acceptable.
- Add UI screenshot tests or Compose previews for small-phone RTL validation.
