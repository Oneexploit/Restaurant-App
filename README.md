# Restaurant Offline Manager

Persian name: مدیریت غذای شرکتی

An offline-first Android app for corporate catering and restaurant operations. The app manages projects, daily meal deliveries, warehouses, materials, stock transactions, purchases, supplier debt, project receivables, bank cards, expenses, reports, CSV export, and JSON backup/restore.

## Tech Stack

- Kotlin
- Jetpack Compose + Material 3
- Room / SQLite
- MVVM with repository and use-case layers
- Kotlin Coroutines + Flow
- Navigation Compose
- DataStore Preferences
- Manual dependency injection
- RTL Persian UI and Jalali-style date formatting

## How To Run

1. Open the project in Android Studio.
2. Make sure the Android SDK path in `local.properties` points to your local SDK.
3. Build from Android Studio or run:

```powershell
.\gradlew.bat assembleDebug
```

Run unit tests:

```powershell
.\gradlew.bat testDebugUnitTest
```

## Release updates

Build and send only the signed release APK:

```bash
JAVA_HOME=/path/to/android-studio/jbr bash gradlew clean assembleRelease
```

For every new release:

1. Keep `applicationId` unchanged (`com.restaurant.offlinemanager`).
2. Increase `versionCode` to a number greater than every APK previously distributed.
3. Set a user-facing `versionName`.
4. Sign with the same `release-key.jks` and key alias.
5. Send `app/build/outputs/apk/release/app-release.apk`; never send the Debug APK.

Back up `release-key.jks` and its passwords securely. Losing this signing key makes future in-place updates impossible. Installing a correctly signed APK with a higher `versionCode` updates the existing app while Room migrations preserve its local data.

## Features

- Dark premium dashboard with glass cards and gold accents.
- Starts with an empty local database so operators create their own warehouses, materials, suppliers, cards, and projects.
- Project receivable calculation from Room data.
- Supplier debt calculation from credit purchases and supplier payments.
- Moving weighted-average inventory valuation with invoice-discount allocation.
- Accrual profit and loss, cash-flow, waste loss, and project profitability analysis.
- Optional supplier-payment allocation to individual credit invoices.
- Bank card balance calculation from initial balance, received project payments, card purchases, supplier payments, and expenses.
- Multi-item daily purchase invoices with automatic stock-in transactions.
- Edit support for meal deliveries, purchase invoices, project receipts, supplier payments, and expenses.
- Stock in, stock out, transfer, waste, and adjustment forms.
- Local CSV export for purchases, inventory, receivables, supplier debts, payments, profit/loss, project profit, and cash flow.
- Local JSON backup and guarded restore for all Room tables, including structure validation and an automatic pre-restore backup.
- DataStore setting for local low-stock warning visibility.

## Database Overview

Room database version: `4`

Main tables:

- `projects`
- `meal_deliveries`
- `warehouses`
- `materials`
- `suppliers`
- `stock_transactions`
- `purchases`
- `purchase_items`
- `bank_cards`
- `project_payments`
- `supplier_payments`
- `expenses`

Settings are stored in DataStore, not Room.

## Architecture

UI screens talk to `RestaurantViewModel`.

`RestaurantViewModel` calls use cases and repositories.

Use cases calculate dashboard, finance, inventory, bank card balances, and reports.

`RoomRestaurantRepository` owns persistence, write validation, purchase side effects, CSV export, and JSON backup/restore.

DAOs are the only layer that accesses Room tables directly.

More details:

- [Database Schema](docs/DATABASE_SCHEMA.md)
- [App Flow](docs/APP_FLOW.md)
- [Design System](docs/DESIGN_SYSTEM.md)
