package com.restaurant.offlinemanager.domain.model

import com.restaurant.offlinemanager.data.local.entity.ExpenseCategory
import com.restaurant.offlinemanager.data.local.entity.DeliveryStatus
import com.restaurant.offlinemanager.data.local.entity.MealType
import com.restaurant.offlinemanager.data.local.entity.PaymentMethod
import com.restaurant.offlinemanager.data.local.entity.ProjectStatus
import com.restaurant.offlinemanager.data.local.entity.PurchasePaymentType
import com.restaurant.offlinemanager.data.local.entity.StockReason
import com.restaurant.offlinemanager.data.local.entity.StockTransactionType
import com.restaurant.offlinemanager.data.local.entity.UnitType
import com.restaurant.offlinemanager.data.local.entity.WarehouseType

fun ProjectStatus.label(): String = when (this) {
    ProjectStatus.ACTIVE -> "فعال"
    ProjectStatus.PAUSED -> "متوقف"
    ProjectStatus.SETTLED -> "تسویه‌شده"
    ProjectStatus.ARCHIVED -> "آرشیو"
}

fun MealType.label(): String = when (this) {
    MealType.BREAKFAST -> "صبحانه"
    MealType.LUNCH -> "ناهار"
    MealType.DINNER -> "شام"
}

fun DeliveryStatus.label(): String = when (this) {
    DeliveryStatus.PREPARING -> "در حال آماده‌سازی"
    DeliveryStatus.DISPATCHED -> "ارسال‌شده"
    DeliveryStatus.DELIVERED -> "تحویل‌شده"
    DeliveryStatus.RETURNED -> "برگشت کامل"
    DeliveryStatus.CANCELLED -> "لغوشده"
}

fun WarehouseType.label(): String = when (this) {
    WarehouseType.GENERAL -> "عمومی"
    WarehouseType.COLD_STORAGE -> "سردخانه"
    WarehouseType.FREEZER -> "فریزر"
    WarehouseType.DRY -> "خشکبار"
}

fun UnitType.label(): String = when (this) {
    UnitType.KG -> "کیلوگرم"
    UnitType.GRAM -> "گرم"
    UnitType.LITER -> "لیتر"
    UnitType.NUMBER -> "عدد"
    UnitType.CARTON -> "کارتن"
    UnitType.PACKAGE -> "بسته"
}

fun StockTransactionType.label(): String = when (this) {
    StockTransactionType.IN -> "ورود کالا"
    StockTransactionType.OUT -> "خروج کالا"
    StockTransactionType.TRANSFER_IN -> "ورود انتقالی"
    StockTransactionType.TRANSFER_OUT -> "خروج انتقالی"
    StockTransactionType.WASTE -> "ضایعات"
    StockTransactionType.ADJUSTMENT -> "اصلاح موجودی"
}

fun StockReason.label(): String = when (this) {
    StockReason.PURCHASE -> "خرید"
    StockReason.COOKING_USAGE -> "مصرف پخت"
    StockReason.WASTE -> "ضایعات"
    StockReason.TRANSFER -> "انتقال"
    StockReason.MANUAL_ADJUSTMENT -> "اصلاح دستی"
    StockReason.RETURN -> "برگشت"
}

fun PurchasePaymentType.label(): String = when (this) {
    PurchasePaymentType.CASH -> "نقدی"
    PurchasePaymentType.CARD -> "کارت"
    PurchasePaymentType.CREDIT -> "نسیه"
}

fun PaymentMethod.label(): String = when (this) {
    PaymentMethod.CASH -> "نقدی"
    PaymentMethod.CARD_TO_CARD -> "کارت به کارت"
    PaymentMethod.BANK_TRANSFER -> "انتقال بانکی"
    PaymentMethod.OTHER -> "سایر"
}

fun ExpenseCategory.label(): String = when (this) {
    ExpenseCategory.RENT -> "اجاره"
    ExpenseCategory.SALARY -> "حقوق"
    ExpenseCategory.TRANSPORT -> "حمل‌ونقل"
    ExpenseCategory.BILLS -> "قبوض"
    ExpenseCategory.REPAIR -> "تعمیرات"
    ExpenseCategory.OTHER -> "سایر"
}

fun maskCardNumber(cardNumber: String?): String {
    val digits = cardNumber.orEmpty().filter { it.isDigit() }
    if (digits.length < 8) return "ثبت نشده"
    return "${digits.take(4)} **** **** ${digits.takeLast(4)}"
}
