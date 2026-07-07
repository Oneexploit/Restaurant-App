# Design Review

## Final Direction

The app now follows a premium dark Persian RTL interface for offline restaurant and corporate catering operations. The visual direction is black/navy glass surfaces, gold primary actions, status accents for operational risk, and dense but readable business dashboards.

## Design System

### Colors

- Background: `#070B12`, `#0B1220`, `#101827`
- Glass surfaces: `#151C28`, `#1B2432`, `#202B3A`
- Borders/dividers: `#2C374A`, `#263244`
- Gold accent: `#D6A84F`, `#F0C978`, `#9B722B`
- Status colors: green `#22C55E`, red `#EF4444`, orange `#F59E0B`, cyan `#06B6D4`, purple `#A855F7`, blue `#3B82F6`
- Text: primary `#F8FAFC`, secondary `#A7B0C0`, muted `#64748B`

Files: `core/design/Colors.kt`, `core/design/AppTheme.kt`.

### Typography

Typography remains system sans-serif for dependable Persian rendering, with stronger hierarchy:

- Large executive values use `headlineSmall` / `headlineMedium`.
- Screen titles and card titles use bold title styles.
- Captions and helper text use muted body/label styles.
- Text fields and chips use one-line constraints where clipping risk is high.

Files: `core/design/Typography.kt`.

### Dimensions

- Screen padding: `16dp`
- Card padding: `18dp`
- Card radius: `24dp`, large radius `28dp`
- Button height: `56dp`
- Input height: `56dp`
- Minimum touch target: `48dp`
- Bottom nav radius: `24dp`

Files: `core/design/Dimensions.kt`.

### Components

Added or consolidated:

- `PremiumScaffold`, `PremiumTopBar`, `PremiumBackground`
- `GlassCard`, `ElevatedGlassCard`
- `StatCard`, `MiniStatCard`, `ActionCard`
- `GoldPrimaryButton`, `SecondaryGlassButton`, `DangerButton`
- `DarkOutlinedTextField`, `SearchTextField`, `AppDropdownField`
- `DatePickerField`, `MoneyInputField`, `QuantityInputField`
- `StatusChip`, `FilterChipRow`, `SectionHeader`
- `EmptyStateCard`, `LoadingState`, `ErrorStateCard`, `ConfirmDialog`
- `BottomSheetActionMenu`, `PremiumBottomNavigation`
- `MoneyText`, `PersianDateText`, `PercentChangeText`
- `LowStockWarningCard`, `TransactionListItem`
- `ProjectCard`, `MaterialCard`, `PurchaseCard`, `FinanceSummaryCard`, `ReportCard`

Files: `core/design/Components.kt`.

## Screen Decisions

### Home

Home is now an executive dashboard: summary cards remain first, quick actions are one tap away, stock-out is available, low-stock warnings are prominent, and recent activity gives a daily pulse.

Files: `ui/home/HomeScreen.kt`.

### Projects

Projects now support accurate status filtering, direct actions from each card, grouped add/edit fields, live project contract preview, and recent meals/payments on details.

Files: `ui/projects/ProjectsScreen.kt`.

### Meal Delivery

Meal entry prioritizes speed: searchable project selection, auto defaults, segmented meal type, quick quantity buttons, and explicit live calculation.

Files: `ui/meals/MealDeliveryScreen.kt`.

### Warehouse

Warehouse now shows inventory value, item count, low-stock risk, today’s movement, tab empty states, per-item in/out actions, and stock-out validation against available quantity.

Files: `ui/warehouse/WarehouseScreen.kt`.

### Purchases

Purchase list now exposes today/month/credit/invoice metrics. The invoice form keeps multi-item entry and adds a clearer final summary with discount and payment state.

Files: `ui/purchases/PurchasesScreen.kt`.

### Finance

Finance tabs now have helpful empty states. Payment forms show remaining balances and prevent obvious overpayment mistakes.

Files: `ui/finance/FinanceScreen.kt`.

### Reports

Reports now include actionable management insights alongside charts and CSV exports.

Files: `ui/reports/ReportsScreen.kt`.

### Search

Global search now supports type filtering and grouped result sections for faster scanning.

Files: `ui/search/SearchScreen.kt`.

### Settings

Settings includes grouped cards for appearance, business information, backup/restore, security, alerts, outputs, and about. Restore backup now uses a confirmation dialog.

Files: `ui/settings/SettingsScreen.kt`.

## Quality Verification

- `./gradlew.bat assembleDebug` completed successfully.
- No database schema, repository, use case, or ViewModel contract was changed.
- The app remains fully offline.

## Follow-Up Quality Work

- Add Compose previews for the key cards and forms.
- Add small-phone screenshot QA for bottom navigation and long Persian labels.
- Add persisted filter state if needed after real operator testing.
