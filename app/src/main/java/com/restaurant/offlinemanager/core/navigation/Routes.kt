package com.restaurant.offlinemanager.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector

object Routes {
    const val Home = "home"
    const val ProjectsList = "projects"
    const val ProjectDetails = "projectDetails"
    const val AddEditProject = "addEditProject"
    const val MealDeliveryList = "mealDeliveries"
    const val AddEditMealDelivery = "addEditMealDelivery"
    const val WarehousesList = "warehouse"
    const val AddEditWarehouse = "addEditWarehouse"
    const val AddMaterialCategory = "addMaterialCategory"
    const val AddEditMaterial = "addEditMaterial"
    const val AddStockIn = "addStockIn"
    const val AddStockOut = "addStockOut"
    const val TransferStock = "transferStock"
    const val AddStockWaste = "addStockWaste"
    const val AddStockAdjustment = "addStockAdjustment"
    const val PurchasesList = "purchases"
    const val AddEditPurchase = "addEditPurchase"
    const val AddSupplier = "addSupplier"
    const val FinanceDashboard = "finance"
    const val AddEditBankCard = "addEditBankCard"
    const val AddProjectPayment = "addProjectPayment"
    const val AddSupplierPayment = "addSupplierPayment"
    const val AddExpense = "addExpense"
    const val Reports = "reports"
    const val GlobalSearch = "search"
    const val Settings = "settings"
}

data class BottomDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
)

val BottomDestinations = listOf(
    BottomDestination(Routes.Home, "خانه", Icons.Outlined.Home),
    BottomDestination(Routes.ProjectsList, "پروژه‌ها", Icons.AutoMirrored.Outlined.List),
    BottomDestination(Routes.WarehousesList, "انبار", Icons.Outlined.Inventory),
    BottomDestination(Routes.PurchasesList, "خرید", Icons.Outlined.ShoppingCart),
    BottomDestination(Routes.FinanceDashboard, "مالی", Icons.Outlined.AccountBalanceWallet),
    BottomDestination(Routes.Reports, "گزارش‌ها", Icons.Outlined.BarChart)
)
