package com.restaurant.offlinemanager.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.restaurant.offlinemanager.data.local.dao.RestaurantDao
import com.restaurant.offlinemanager.data.local.entity.BankCardEntity
import com.restaurant.offlinemanager.data.local.entity.ExpenseEntity
import com.restaurant.offlinemanager.data.local.entity.MaterialEntity
import com.restaurant.offlinemanager.data.local.entity.MealDeliveryEntity
import com.restaurant.offlinemanager.data.local.entity.ProjectEntity
import com.restaurant.offlinemanager.data.local.entity.ProjectPaymentEntity
import com.restaurant.offlinemanager.data.local.entity.PurchaseEntity
import com.restaurant.offlinemanager.data.local.entity.PurchaseItemEntity
import com.restaurant.offlinemanager.data.local.entity.StockTransactionEntity
import com.restaurant.offlinemanager.data.local.entity.SupplierEntity
import com.restaurant.offlinemanager.data.local.entity.SupplierPaymentEntity
import com.restaurant.offlinemanager.data.local.entity.WarehouseEntity

@Database(
    entities = [
        ProjectEntity::class,
        MealDeliveryEntity::class,
        WarehouseEntity::class,
        MaterialEntity::class,
        SupplierEntity::class,
        StockTransactionEntity::class,
        PurchaseEntity::class,
        PurchaseItemEntity::class,
        BankCardEntity::class,
        ProjectPaymentEntity::class,
        SupplierPaymentEntity::class,
        ExpenseEntity::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(DatabaseConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun restaurantDao(): RestaurantDao
}
