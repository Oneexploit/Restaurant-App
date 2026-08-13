package com.restaurant.offlinemanager.data.local.entity

enum class ProjectStatus { ACTIVE, PAUSED, SETTLED, ARCHIVED }
enum class MealType { BREAKFAST, LUNCH, DINNER }
enum class DeliveryStatus { PREPARING, DISPATCHED, DELIVERED, RETURNED, CANCELLED }
enum class WarehouseType { GENERAL, COLD_STORAGE, FREEZER, DRY }
enum class UnitType { KG, GRAM, LITER, NUMBER, CARTON, PACKAGE }
enum class StockTransactionType { IN, OUT, TRANSFER_IN, TRANSFER_OUT, WASTE, ADJUSTMENT }
enum class StockReason { PURCHASE, COOKING_USAGE, WASTE, TRANSFER, MANUAL_ADJUSTMENT, RETURN }
enum class PurchasePaymentType { CASH, CARD, CREDIT }
enum class PaymentMethod { CASH, CARD_TO_CARD, BANK_TRANSFER, OTHER }
enum class ExpenseCategory { RENT, SALARY, TRANSPORT, BILLS, REPAIR, OTHER }
