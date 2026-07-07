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

## Features

- Dark premium dashboard with glass cards and gold accents.
- Seed data on first launch for projects, warehouses, materials, suppliers, cards, purchases, stock, deliveries, payments, and expenses.
- Project receivable calculation from Room data.
- Supplier debt calculation from credit purchases and supplier payments.
- Inventory calculation from stock transactions.
- Bank card balance calculation from initial balance, received project payments, card purchases, supplier payments, and expenses.
- Multi-item daily purchase invoices with automatic stock-in transactions.
- Stock in, stock out, transfer, waste, and adjustment forms.
- Local CSV export for purchases, inventory, receivables, supplier debts, and payments.
- Local JSON backup and restore for all Room tables.
- DataStore settings for dark mode, app lock, low stock notifications, and default warehouse.

## Database Overview

Room database version: `1`

Main tables:

- `projects`
- `meal_deliveries`
- `warehouses`
- `material_categories`
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
