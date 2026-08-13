package com.restaurant.offlinemanager.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigrations {
    val migration1To2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """CREATE TABLE IF NOT EXISTS materials_new (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    name TEXT NOT NULL,
                    mainUnit TEXT NOT NULL,
                    minimumStock REAL NOT NULL,
                    imageEmoji TEXT,
                    notes TEXT,
                    isActive INTEGER NOT NULL,
                    createdAt INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL
                )""".trimIndent()
            )
            db.execSQL(
                """INSERT INTO materials_new
                    (id, name, mainUnit, minimumStock, imageEmoji, notes, isActive, createdAt, updatedAt)
                    SELECT id, name, mainUnit, minimumStock, imageEmoji, notes, isActive, createdAt, updatedAt
                    FROM materials""".trimIndent()
            )
            db.execSQL("DROP TABLE materials")
            db.execSQL("ALTER TABLE materials_new RENAME TO materials")
            db.execSQL("DROP TABLE IF EXISTS material_categories")
        }
    }

    val migration2To3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE supplier_payments ADD COLUMN purchaseId INTEGER")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_supplier_payments_purchaseId ON supplier_payments(purchaseId)")
        }
    }

    val migration3To4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE meal_deliveries ADD COLUMN deliveryTimeMinutes INTEGER")
            db.execSQL("ALTER TABLE meal_deliveries ADD COLUMN status TEXT NOT NULL DEFAULT 'DELIVERED'")
            db.execSQL("ALTER TABLE meal_deliveries ADD COLUMN returnedQuantity INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE meal_deliveries ADD COLUMN recipientName TEXT")
            db.execSQL("ALTER TABLE meal_deliveries ADD COLUMN recipientPhone TEXT")
        }
    }

    val all = arrayOf(migration1To2, migration2To3, migration3To4)
}
