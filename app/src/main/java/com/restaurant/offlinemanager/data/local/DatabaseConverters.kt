package com.restaurant.offlinemanager.data.local

import androidx.room.TypeConverter
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

class DatabaseConverters {
    @TypeConverter fun toProjectStatus(value: String): ProjectStatus = ProjectStatus.valueOf(value)
    @TypeConverter fun fromProjectStatus(value: ProjectStatus): String = value.name

    @TypeConverter fun toMealType(value: String): MealType = MealType.valueOf(value)
    @TypeConverter fun fromMealType(value: MealType): String = value.name

    @TypeConverter fun toDeliveryStatus(value: String): DeliveryStatus = DeliveryStatus.valueOf(value)
    @TypeConverter fun fromDeliveryStatus(value: DeliveryStatus): String = value.name

    @TypeConverter fun toWarehouseType(value: String): WarehouseType = WarehouseType.valueOf(value)
    @TypeConverter fun fromWarehouseType(value: WarehouseType): String = value.name

    @TypeConverter fun toUnitType(value: String): UnitType = UnitType.valueOf(value)
    @TypeConverter fun fromUnitType(value: UnitType): String = value.name

    @TypeConverter fun toStockTransactionType(value: String): StockTransactionType = StockTransactionType.valueOf(value)
    @TypeConverter fun fromStockTransactionType(value: StockTransactionType): String = value.name

    @TypeConverter fun toStockReason(value: String): StockReason = StockReason.valueOf(value)
    @TypeConverter fun fromStockReason(value: StockReason): String = value.name

    @TypeConverter fun toPurchasePaymentType(value: String): PurchasePaymentType = PurchasePaymentType.valueOf(value)
    @TypeConverter fun fromPurchasePaymentType(value: PurchasePaymentType): String = value.name

    @TypeConverter fun toPaymentMethod(value: String): PaymentMethod = PaymentMethod.valueOf(value)
    @TypeConverter fun fromPaymentMethod(value: PaymentMethod): String = value.name

    @TypeConverter fun toExpenseCategory(value: String): ExpenseCategory = ExpenseCategory.valueOf(value)
    @TypeConverter fun fromExpenseCategory(value: ExpenseCategory): String = value.name
}
