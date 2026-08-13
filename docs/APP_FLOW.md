# App Flow

## Launch

`RestaurantOfflineApp` creates `AppContainer`, builds Room, and wires repositories/use cases. The app intentionally starts with an empty local database; users create warehouses, materials, suppliers, cards, and projects themselves.

## Main Navigation

Bottom navigation:

- خانه
- پروژه‌ها
- انبار
- خرید روزانه
- مالی
- گزارش‌ها

Settings and global search are opened from the top bar.

## Main Workflows

### Projects

Create or edit a project, then register meal deliveries and payments. Project receivable is calculated as:

`sum(meal_deliveries.totalAmount) - sum(project_payments.amount)`

### Meal Delivery

Select a project, choose meal type, enter quantity and unit price. The form calculates total live and saves a Room delivery row.

Meal delivery rows can be edited later. The app blocks duplicate delivery rows for the same project, date, and meal type.

### Warehouse

Inventory is calculated from stock transactions. Users can register stock in, stock out, transfer between warehouses, waste, and adjustment.

Stock out is blocked when requested quantity is greater than available stock.

### Purchases

Daily purchases support multiple invoice items. Saving a purchase:

1. Calculates `totalAmount = sum(items) - discount`.
2. Sets `paidAmount` based on payment type.
3. Inserts the purchase.
4. Inserts purchase items.
5. Creates stock `IN` transactions for each item.

Editing a purchase replaces its purchase items and regenerates the automatic stock `IN` transactions in one Room transaction.

### Finance

Finance dashboard shows:

- Project receivables
- Supplier debts
- Bank card balances
- Monthly purchases
- Monthly received payments
- Monthly expenses

Bank card balance is calculated as:

`initialBalance + project payments - card purchases - supplier payments - expenses`

### Reports

Reports use the same Room snapshot and use cases as the dashboard. CSV files are exported to the app external files `reports` directory.

### Backup / Restore

Backup exports all Room tables to a JSON file in the app external files `backups` directory.

Restore validates the selected JSON document, creates an automatic pre-restore backup of current data, clears current tables in a Room transaction, and reinserts preserved IDs where possible.

Backup version 2 no longer stores material categories. Version 1 backups remain restorable; their category data is ignored while all materials are preserved.
