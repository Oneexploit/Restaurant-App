# Database Schema

Room database: `AppDatabase`

Version: `3`

All money values are stored as `Long` in تومان. Quantities are stored as `Double`. Dates are stored as epoch milliseconds.

## Tables

### projects

Stores corporate catering projects.

Fields: `id`, `name`, `companyName`, `address`, `managerName`, `phone`, `workerCount`, `mealPrice`, `defaultMealType`, `startDate`, `endDate`, `status`, `notes`, `createdAt`, `updatedAt`.

Status enum: `ACTIVE`, `PAUSED`, `SETTLED`, `ARCHIVED`.

### meal_deliveries

Stores daily delivered meals.

Fields: `id`, `projectId`, `date`, `mealType`, `quantity`, `unitPrice`, `totalAmount`, `notes`, `createdAt`, `updatedAt`.

Rule: `totalAmount = quantity * unitPrice`.

### warehouses

Fields: `id`, `name`, `type`, `address`, `notes`, `isActive`, `createdAt`, `updatedAt`.

Type enum: `GENERAL`, `COLD_STORAGE`, `FREEZER`, `DRY`.

### materials

Fields: `id`, `name`, `mainUnit`, `minimumStock`, `imageEmoji`, `notes`, `isActive`, `createdAt`, `updatedAt`.

Unit enum: `KG`, `GRAM`, `LITER`, `NUMBER`, `CARTON`, `PACKAGE`.

### suppliers

Fields: `id`, `name`, `phone`, `address`, `notes`, `isActive`, `createdAt`, `updatedAt`.

### stock_transactions

Fields: `id`, `warehouseId`, `materialId`, `projectId`, `supplierId`, `purchaseId`, `type`, `reason`, `quantity`, `unit`, `unitPrice`, `totalAmount`, `date`, `notes`, `createdAt`, `updatedAt`.

Stock is calculated from transactions:

`IN + TRANSFER_IN + ADJUSTMENT - OUT - TRANSFER_OUT - WASTE`

Inventory value uses moving weighted-average cost. Purchase discounts are proportionally allocated to invoice items. Transfers preserve cost, stock-out records cost of consumption, and waste records a separate loss.

### purchases

Fields: `id`, `supplierId`, `warehouseId`, `date`, `invoiceNumber`, `paymentType`, `bankCardId`, `discountAmount`, `totalAmount`, `paidAmount`, `notes`, `createdAt`, `updatedAt`.

Payment enum: `CASH`, `CARD`, `CREDIT`.

### purchase_items

Fields: `id`, `purchaseId`, `materialId`, `quantity`, `unit`, `unitPrice`, `totalAmount`, `createdAt`, `updatedAt`.

Saving purchase items creates stock `IN` transactions.

### bank_cards

Fields: `id`, `title`, `ownerName`, `bankName`, `cardNumber`, `initialBalance`, `isActive`, `notes`, `createdAt`, `updatedAt`.

Card numbers are masked in UI.

### project_payments

Fields: `id`, `projectId`, `bankCardId`, `amount`, `date`, `method`, `notes`, `createdAt`, `updatedAt`.

### supplier_payments

Fields: `id`, `supplierId`, `bankCardId`, `purchaseId`, `amount`, `date`, `method`, `notes`, `createdAt`, `updatedAt`.

`purchaseId` is optional. When present, payment cannot exceed that invoice's remaining balance.

## Accounting Rules

- Accrual revenue comes from delivered meals.
- Project receivable equals delivered-meal revenue minus project receipts.
- Cost of goods consumed is valued at the moving weighted-average cost at the transaction date.
- Gross profit equals accrual revenue minus material consumption cost.
- Net profit equals gross profit minus operating expenses and waste loss.
- Cash flow is reported separately from profit and uses actual receipts and payments.
- Material stock-out can optionally be linked to a project for project-profit analysis.

### expenses

Fields: `id`, `title`, `category`, `amount`, `date`, `bankCardId`, `notes`, `createdAt`, `updatedAt`.

Expense enum: `RENT`, `SALARY`, `TRANSPORT`, `BILLS`, `REPAIR`, `OTHER`.

## DataStore Settings

`app_settings` stores:

- `lowStockNotificationsEnabled`
