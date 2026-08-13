package com.restaurant.offlinemanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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
import kotlinx.coroutines.flow.Flow

@Dao
interface RestaurantDao {
    @Query("SELECT * FROM projects ORDER BY updatedAt DESC")
    fun observeProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM meal_deliveries ORDER BY date DESC, id DESC")
    fun observeMealDeliveries(): Flow<List<MealDeliveryEntity>>

    @Query("SELECT * FROM warehouses ORDER BY id")
    fun observeWarehouses(): Flow<List<WarehouseEntity>>

    @Query("SELECT * FROM materials ORDER BY name")
    fun observeMaterials(): Flow<List<MaterialEntity>>

    @Query("SELECT * FROM suppliers ORDER BY name")
    fun observeSuppliers(): Flow<List<SupplierEntity>>

    @Query("SELECT * FROM stock_transactions ORDER BY date DESC, id DESC")
    fun observeStockTransactions(): Flow<List<StockTransactionEntity>>

    @Query("SELECT * FROM purchases ORDER BY date DESC, id DESC")
    fun observePurchases(): Flow<List<PurchaseEntity>>

    @Query("SELECT * FROM purchase_items ORDER BY purchaseId DESC, id")
    fun observePurchaseItems(): Flow<List<PurchaseItemEntity>>

    @Query("SELECT * FROM bank_cards ORDER BY id")
    fun observeBankCards(): Flow<List<BankCardEntity>>

    @Query("SELECT * FROM project_payments ORDER BY date DESC, id DESC")
    fun observeProjectPayments(): Flow<List<ProjectPaymentEntity>>

    @Query("SELECT * FROM supplier_payments ORDER BY date DESC, id DESC")
    fun observeSupplierPayments(): Flow<List<SupplierPaymentEntity>>

    @Query("SELECT * FROM expenses ORDER BY date DESC, id DESC")
    fun observeExpenses(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM projects ORDER BY id")
    suspend fun getProjects(): List<ProjectEntity>

    @Query("SELECT * FROM meal_deliveries ORDER BY id")
    suspend fun getMealDeliveries(): List<MealDeliveryEntity>

    @Query("SELECT * FROM warehouses ORDER BY id")
    suspend fun getWarehouses(): List<WarehouseEntity>

    @Query("SELECT * FROM materials ORDER BY id")
    suspend fun getMaterials(): List<MaterialEntity>

    @Query("SELECT * FROM suppliers ORDER BY id")
    suspend fun getSuppliers(): List<SupplierEntity>

    @Query("SELECT * FROM stock_transactions ORDER BY id")
    suspend fun getStockTransactions(): List<StockTransactionEntity>

    @Query("SELECT * FROM purchases ORDER BY id")
    suspend fun getPurchases(): List<PurchaseEntity>

    @Query("SELECT * FROM purchase_items ORDER BY id")
    suspend fun getPurchaseItems(): List<PurchaseItemEntity>

    @Query("SELECT * FROM bank_cards ORDER BY id")
    suspend fun getBankCards(): List<BankCardEntity>

    @Query("SELECT * FROM project_payments ORDER BY id")
    suspend fun getProjectPayments(): List<ProjectPaymentEntity>

    @Query("SELECT * FROM supplier_payments ORDER BY id")
    suspend fun getSupplierPayments(): List<SupplierPaymentEntity>

    @Query("SELECT * FROM expenses ORDER BY id")
    suspend fun getExpenses(): List<ExpenseEntity>

    @Query("SELECT * FROM projects WHERE id = :id LIMIT 1")
    suspend fun getProject(id: Long): ProjectEntity?

    @Query("SELECT * FROM bank_cards WHERE id = :id LIMIT 1")
    suspend fun getBankCard(id: Long): BankCardEntity?

    @Query("SELECT * FROM stock_transactions WHERE id = :id LIMIT 1")
    suspend fun getStockTransaction(id: Long): StockTransactionEntity?

    @Query("SELECT * FROM purchases WHERE id = :id LIMIT 1")
    suspend fun getPurchase(id: Long): PurchaseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(entity: ProjectEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealDelivery(entity: MealDeliveryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWarehouse(entity: WarehouseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaterial(entity: MaterialEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplier(entity: SupplierEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockTransaction(entity: StockTransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchase(entity: PurchaseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchaseItem(entity: PurchaseItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBankCard(entity: BankCardEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProjectPayment(entity: ProjectPaymentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplierPayment(entity: SupplierPaymentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(entity: ExpenseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProjects(entities: List<ProjectEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealDeliveries(entities: List<MealDeliveryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWarehouses(entities: List<WarehouseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaterials(entities: List<MaterialEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSuppliers(entities: List<SupplierEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockTransactions(entities: List<StockTransactionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchases(entities: List<PurchaseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchaseItems(entities: List<PurchaseItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBankCards(entities: List<BankCardEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProjectPayments(entities: List<ProjectPaymentEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplierPayments(entities: List<SupplierPaymentEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenses(entities: List<ExpenseEntity>)

    @Update
    suspend fun updateProject(entity: ProjectEntity)

    @Query("DELETE FROM projects")
    suspend fun clearProjects()

    @Query("DELETE FROM meal_deliveries")
    suspend fun clearMealDeliveries()

    @Query("DELETE FROM warehouses")
    suspend fun clearWarehouses()

    @Query("DELETE FROM materials")
    suspend fun clearMaterials()

    @Query("DELETE FROM suppliers")
    suspend fun clearSuppliers()

    @Query("DELETE FROM stock_transactions")
    suspend fun clearStockTransactions()

    @Query("DELETE FROM purchases")
    suspend fun clearPurchases()

    @Query("DELETE FROM purchase_items")
    suspend fun clearPurchaseItems()

    @Query("DELETE FROM bank_cards")
    suspend fun clearBankCards()

    @Query("DELETE FROM project_payments")
    suspend fun clearProjectPayments()

    @Query("DELETE FROM supplier_payments")
    suspend fun clearSupplierPayments()

    @Query("DELETE FROM expenses")
    suspend fun clearExpenses()

    @Query("DELETE FROM warehouses WHERE id = :id")
    suspend fun deleteWarehouse(id: Long)

    @Query("DELETE FROM materials WHERE id = :id")
    suspend fun deleteMaterial(id: Long)

    @Query("DELETE FROM suppliers WHERE id = :id")
    suspend fun deleteSupplier(id: Long)

    @Query("DELETE FROM bank_cards WHERE id = :id")
    suspend fun deleteBankCard(id: Long)

    @Query("DELETE FROM meal_deliveries WHERE id = :id")
    suspend fun deleteMealDelivery(id: Long)

    @Query("DELETE FROM stock_transactions WHERE id = :id")
    suspend fun deleteStockTransaction(id: Long)

    @Query("DELETE FROM stock_transactions WHERE purchaseId = :purchaseId")
    suspend fun deleteStockTransactionsForPurchase(purchaseId: Long)

    @Query("DELETE FROM purchase_items WHERE purchaseId = :purchaseId")
    suspend fun deletePurchaseItemsForPurchase(purchaseId: Long)

    @Query("DELETE FROM purchases WHERE id = :id")
    suspend fun deletePurchase(id: Long)

    @Query("DELETE FROM project_payments WHERE id = :id")
    suspend fun deleteProjectPayment(id: Long)

    @Query("DELETE FROM supplier_payments WHERE id = :id")
    suspend fun deleteSupplierPayment(id: Long)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpense(id: Long)
}
