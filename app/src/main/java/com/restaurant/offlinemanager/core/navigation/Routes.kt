package com.restaurant.offlinemanager.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Inventory
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.ui.graphics.vector.ImageVector

object Routes {
    const val Home = "home"
    const val ProjectsList = "projects"
    const val ProjectDetails = "projectDetails"
    const val AddEditProject = "addEditProject"
    const val MealDeliveryList = "mealDeliveries"
    const val AddEditMealDelivery = "addEditMealDelivery"
    const val CookingList = "cooking"
    const val AddEditCooking = "addEditCooking"
    const val WarehousesList = "warehouse"
    const val AddEditWarehouse = "addEditWarehouse"
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
    const val DailyReport = "dailyReport"
    const val MonthlyReport = "monthlyReport"
    const val GlobalSearch = "search"
    const val Settings = "settings"
}

data class BottomDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
)

val BottomDestinations = listOf(
    BottomDestination(Routes.ProjectsList, "پروژه", Icons.AutoMirrored.Outlined.List),
    BottomDestination(Routes.WarehousesList, "انبار", Icons.Outlined.Inventory),
    BottomDestination(Routes.CookingList, "مصرف", Icons.Outlined.RestaurantMenu),
    BottomDestination(Routes.MealDeliveryList, "تحویل غذا", Icons.Outlined.Restaurant),
    BottomDestination(Routes.DailyReport, "گزارش روزانه", Icons.Outlined.CalendarToday),
    BottomDestination(Routes.MonthlyReport, "ماهانه", Icons.Outlined.DateRange)
)
