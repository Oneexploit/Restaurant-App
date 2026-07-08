package com.restaurant.offlinemanager.core.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.material3.SnackbarHostState
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.restaurant.offlinemanager.core.design.AppScaffold
import com.restaurant.offlinemanager.data.local.entity.StockTransactionType
import com.restaurant.offlinemanager.ui.RestaurantViewModel
import com.restaurant.offlinemanager.ui.finance.BankCardFormScreen
import com.restaurant.offlinemanager.ui.finance.ExpenseFormScreen
import com.restaurant.offlinemanager.ui.finance.FinanceDashboardScreen
import com.restaurant.offlinemanager.ui.finance.ProjectPaymentFormScreen
import com.restaurant.offlinemanager.ui.finance.SupplierPaymentFormScreen
import com.restaurant.offlinemanager.ui.home.HomeScreen
import com.restaurant.offlinemanager.ui.meals.MealDeliveryFormScreen
import com.restaurant.offlinemanager.ui.meals.MealDeliveryListScreen
import com.restaurant.offlinemanager.ui.projects.ProjectDetailsScreen
import com.restaurant.offlinemanager.ui.projects.ProjectFormScreen
import com.restaurant.offlinemanager.ui.projects.ProjectsListScreen
import com.restaurant.offlinemanager.ui.purchases.PurchaseFormScreen
import com.restaurant.offlinemanager.ui.purchases.PurchasesListScreen
import com.restaurant.offlinemanager.ui.purchases.SupplierFormScreen
import com.restaurant.offlinemanager.ui.reports.ReportsScreen
import com.restaurant.offlinemanager.ui.search.SearchScreen
import com.restaurant.offlinemanager.ui.settings.SettingsScreen
import com.restaurant.offlinemanager.ui.warehouse.MaterialFormScreen
import com.restaurant.offlinemanager.ui.warehouse.MaterialCategoryFormScreen
import com.restaurant.offlinemanager.ui.warehouse.StockTransactionFormScreen
import com.restaurant.offlinemanager.ui.warehouse.WarehouseFormScreen
import com.restaurant.offlinemanager.ui.warehouse.WarehouseMainScreen

@Composable
fun AppNavGraph(viewModel: RestaurantViewModel) {
    val navController = rememberNavController()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val topLevelRoutes = remember { BottomDestinations.map { it.route }.toSet() + Routes.Home }
    val showBack = currentRoute != null && currentRoute !in topLevelRoutes

    LaunchedEffect(Unit) {
        viewModel.messages.collect { snackbarHostState.showSnackbar(it) }
    }

    AppScaffold(
        title = titleFor(currentRoute),
        currentRoute = bottomRouteFor(currentRoute),
        bottomDestinations = BottomDestinations,
        onNavigate = { route ->
            navController.navigate(route) {
                popUpTo(Routes.Home) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        },
        onOpenSettings = { navController.navigate(Routes.Settings) },
        onOpenSearch = { navController.navigate(Routes.GlobalSearch) },
        onBack = if (showBack) ({ navController.popBackStack() }) else null,
        snackbarHostState = snackbarHostState
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.Home,
            modifier = Modifier.padding(padding),
            enterTransition = {
                fadeIn(tween(220)) + slideInHorizontally(tween(320)) { it / 6 }
            },
            exitTransition = {
                fadeOut(tween(160)) + slideOutHorizontally(tween(240)) { -it / 10 }
            },
            popEnterTransition = {
                fadeIn(tween(220)) + slideInHorizontally(tween(320)) { -it / 6 }
            },
            popExitTransition = {
                fadeOut(tween(160)) + slideOutHorizontally(tween(240)) { it / 10 }
            }
        ) {
            composable(Routes.Home) {
                HomeScreen(
                    state = state,
                    onAddProject = { navController.navigate("${Routes.AddEditProject}/0") },
                    onAddMeal = { navController.navigate("${Routes.AddEditMealDelivery}/0") },
                    onStockIn = { navController.navigate(Routes.AddStockIn) },
                    onStockOut = { navController.navigate(Routes.AddStockOut) },
                    onAddPurchase = { navController.navigate(Routes.AddEditPurchase) },
                    onAddPayment = { navController.navigate("${Routes.AddProjectPayment}/0") },
                    onReports = { navController.navigate(Routes.Reports) },
                    onAddWarehouse = { navController.navigate("${Routes.AddEditWarehouse}/0") },
                    onAddMaterialCategory = { navController.navigate("${Routes.AddMaterialCategory}/0") },
                    onAddMaterial = { navController.navigate("${Routes.AddEditMaterial}/0") },
                    onAddSupplier = { navController.navigate("${Routes.AddSupplier}/0") },
                    onAddBankCard = { navController.navigate("${Routes.AddEditBankCard}/0") }
                )
            }
            composable(Routes.ProjectsList) {
                ProjectsListScreen(
                    state = state,
                    onAddProject = { navController.navigate("${Routes.AddEditProject}/0") },
                    onProjectDetails = { navController.navigate("${Routes.ProjectDetails}/$it") },
                    onAddMeal = { navController.navigate("${Routes.AddEditMealDelivery}/$it") },
                    onAddPayment = { navController.navigate("${Routes.AddProjectPayment}/$it") }
                )
            }
            composable(
                route = "${Routes.ProjectDetails}/{projectId}",
                arguments = listOf(navArgument("projectId") { type = NavType.LongType })
            ) { entry ->
                val projectId = entry.arguments?.getLong("projectId") ?: 0L
                ProjectDetailsScreen(
                    state = state,
                    projectId = projectId,
                    onEdit = { navController.navigate("${Routes.AddEditProject}/$it") },
                    onAddMeal = { navController.navigate("${Routes.AddEditMealDelivery}/$it") },
                    onAddPayment = { navController.navigate("${Routes.AddProjectPayment}/$it") },
                    onArchive = { viewModel.archiveProject(it) }
                )
            }
            composable(
                route = "${Routes.AddEditProject}/{projectId}",
                arguments = listOf(navArgument("projectId") { type = NavType.LongType })
            ) { entry ->
                val projectId = entry.arguments?.getLong("projectId")?.takeIf { it != 0L }
                ProjectFormScreen(state, projectId, onSave = {
                    viewModel.saveProject(it) { navController.popBackStack() }
                })
            }
            composable(Routes.MealDeliveryList) {
                MealDeliveryListScreen(
                    state,
                    onAddMeal = { navController.navigate("${Routes.AddEditMealDelivery}/0") },
                    onDeleteMeal = viewModel::deleteMealDelivery
                )
            }
            composable(
                route = "${Routes.AddEditMealDelivery}/{projectId}",
                arguments = listOf(navArgument("projectId") { type = NavType.LongType })
            ) { entry ->
                val projectId = entry.arguments?.getLong("projectId")?.takeIf { it != 0L }
                MealDeliveryFormScreen(state, projectId, onSave = {
                    viewModel.saveMealDelivery(it) { navController.popBackStack() }
                })
            }
            composable(Routes.WarehousesList) {
                WarehouseMainScreen(
                    state = state,
                    onStockIn = { navController.navigate(Routes.AddStockIn) },
                    onStockOut = { navController.navigate(Routes.AddStockOut) },
                    onTransfer = { navController.navigate(Routes.TransferStock) },
                    onWaste = { navController.navigate(Routes.AddStockWaste) },
                    onAdjustment = { navController.navigate(Routes.AddStockAdjustment) },
                    onAddWarehouse = { navController.navigate("${Routes.AddEditWarehouse}/0") },
                    onAddMaterialCategory = { navController.navigate("${Routes.AddMaterialCategory}/0") },
                    onAddMaterial = { navController.navigate("${Routes.AddEditMaterial}/0") },
                    onEditWarehouse = { navController.navigate("${Routes.AddEditWarehouse}/$it") },
                    onEditMaterialCategory = { navController.navigate("${Routes.AddMaterialCategory}/$it") },
                    onEditMaterial = { navController.navigate("${Routes.AddEditMaterial}/$it") },
                    onDeleteStockTransaction = viewModel::deleteStockTransaction
                )
            }
            composable(Routes.AddStockIn) {
                StockTransactionFormScreen(state, StockTransactionType.IN, onSave = {
                    viewModel.saveStockTransactions(it) { navController.popBackStack() }
                })
            }
            composable(Routes.AddStockOut) {
                StockTransactionFormScreen(state, StockTransactionType.OUT, onSave = {
                    viewModel.saveStockTransactions(it) { navController.popBackStack() }
                })
            }
            composable(Routes.TransferStock) {
                StockTransactionFormScreen(state, StockTransactionType.TRANSFER_OUT, onSave = {
                    viewModel.saveStockTransactions(it) { navController.popBackStack() }
                })
            }
            composable(Routes.AddStockWaste) {
                StockTransactionFormScreen(state, StockTransactionType.WASTE, onSave = {
                    viewModel.saveStockTransactions(it) { navController.popBackStack() }
                })
            }
            composable(Routes.AddStockAdjustment) {
                StockTransactionFormScreen(state, StockTransactionType.ADJUSTMENT, onSave = {
                    viewModel.saveStockTransactions(it) { navController.popBackStack() }
                })
            }
            composable(
                route = "${Routes.AddEditWarehouse}/{warehouseId}",
                arguments = listOf(navArgument("warehouseId") { type = NavType.LongType })
            ) { entry ->
                val warehouseId = entry.arguments?.getLong("warehouseId")?.takeIf { it != 0L }
                WarehouseFormScreen(state, warehouseId, onSave = {
                    viewModel.saveWarehouse(it) { navController.popBackStack() }
                })
            }
            composable(
                route = "${Routes.AddMaterialCategory}/{categoryId}",
                arguments = listOf(navArgument("categoryId") { type = NavType.LongType })
            ) { entry ->
                val categoryId = entry.arguments?.getLong("categoryId")?.takeIf { it != 0L }
                MaterialCategoryFormScreen(state, categoryId, onSave = {
                    viewModel.saveMaterialCategory(it) { navController.popBackStack() }
                })
            }
            composable(
                route = "${Routes.AddEditMaterial}/{materialId}",
                arguments = listOf(navArgument("materialId") { type = NavType.LongType })
            ) { entry ->
                val materialId = entry.arguments?.getLong("materialId")?.takeIf { it != 0L }
                MaterialFormScreen(
                    state,
                    materialId,
                    onSave = {
                        viewModel.saveMaterial(it) { navController.popBackStack() }
                    },
                    onAddCategory = { navController.navigate("${Routes.AddMaterialCategory}/0") }
                )
            }
            composable(Routes.PurchasesList) {
                PurchasesListScreen(
                    state,
                    onAddPurchase = { navController.navigate(Routes.AddEditPurchase) },
                    onAddSupplier = { navController.navigate("${Routes.AddSupplier}/0") },
                    onEditSupplier = { navController.navigate("${Routes.AddSupplier}/$it") },
                    onDeletePurchase = viewModel::deletePurchase
                )
            }
            composable(Routes.AddEditPurchase) {
                PurchaseFormScreen(
                    state,
                    onSave = {
                        viewModel.savePurchase(it) { navController.popBackStack() }
                    },
                    onAddSupplier = { navController.navigate("${Routes.AddSupplier}/0") }
                )
            }
            composable(
                route = "${Routes.AddSupplier}/{supplierId}",
                arguments = listOf(navArgument("supplierId") { type = NavType.LongType })
            ) { entry ->
                val supplierId = entry.arguments?.getLong("supplierId")?.takeIf { it != 0L }
                SupplierFormScreen(state, supplierId, onSave = {
                    viewModel.saveSupplier(it) { navController.popBackStack() }
                })
            }
            composable(Routes.FinanceDashboard) {
                FinanceDashboardScreen(
                    state = state,
                    onAddProjectPayment = { navController.navigate("${Routes.AddProjectPayment}/0") },
                    onAddSupplierPayment = { navController.navigate(Routes.AddSupplierPayment) },
                    onAddBankCard = { navController.navigate("${Routes.AddEditBankCard}/0") },
                    onEditBankCard = { navController.navigate("${Routes.AddEditBankCard}/$it") },
                    onAddExpense = { navController.navigate(Routes.AddExpense) },
                    onDeleteProjectPayment = viewModel::deleteProjectPayment,
                    onDeleteSupplierPayment = viewModel::deleteSupplierPayment,
                    onDeleteExpense = viewModel::deleteExpense
                )
            }
            composable(
                route = "${Routes.AddProjectPayment}/{projectId}",
                arguments = listOf(navArgument("projectId") { type = NavType.LongType })
            ) { entry ->
                val projectId = entry.arguments?.getLong("projectId")?.takeIf { it != 0L }
                ProjectPaymentFormScreen(state, projectId, onSave = {
                    viewModel.saveProjectPayment(it) { navController.popBackStack() }
                })
            }
            composable(Routes.AddSupplierPayment) {
                SupplierPaymentFormScreen(state, onSave = {
                    viewModel.saveSupplierPayment(it) { navController.popBackStack() }
                })
            }
            composable(
                route = "${Routes.AddEditBankCard}/{cardId}",
                arguments = listOf(navArgument("cardId") { type = NavType.LongType })
            ) { entry ->
                val cardId = entry.arguments?.getLong("cardId")?.takeIf { it != 0L }
                BankCardFormScreen(state, cardId, onSave = {
                    viewModel.saveBankCard(it) { navController.popBackStack() }
                })
            }
            composable(Routes.AddExpense) {
                ExpenseFormScreen(state, onSave = {
                    viewModel.saveExpense(it) { navController.popBackStack() }
                })
            }
            composable(Routes.Reports) {
                ReportsScreen(state, context, onExport = viewModel::exportCsv)
            }
            composable(Routes.GlobalSearch) {
                SearchScreen(
                    state,
                    onResultClick = { result ->
                        when {
                            result.id.startsWith("project-payment-") || result.id.startsWith("card-") -> navController.navigate(Routes.FinanceDashboard)
                            result.id.startsWith("project-") -> navController.navigate("${Routes.ProjectDetails}/${result.rawId}")
                            result.id.startsWith("material-") || result.id.startsWith("warehouse-") -> navController.navigate(Routes.WarehousesList)
                            result.id.startsWith("supplier-") || result.id.startsWith("purchase-") -> navController.navigate(Routes.PurchasesList)
                        }
                    }
                )
            }
            composable(Routes.Settings) {
                SettingsScreen(
                    state = state,
                    context = context,
                    onLowStockNotifications = viewModel::setLowStockNotifications,
                    onExportBackup = viewModel::exportBackup,
                    onRestoreBackup = viewModel::restoreBackup
                )
            }
        }
    }
}

private fun bottomRouteFor(route: String?): String? =
    when {
        route == null -> null
        route.startsWith(Routes.Home) -> Routes.Home
        route.startsWith(Routes.ProjectsList) || route.startsWith(Routes.ProjectDetails) || route.startsWith(Routes.AddEditProject) || route.startsWith(Routes.MealDeliveryList) || route.startsWith(Routes.AddEditMealDelivery) -> Routes.ProjectsList
        route.startsWith(Routes.WarehousesList) || route.startsWith(Routes.AddStockIn) || route.startsWith(Routes.AddStockOut) || route.startsWith(Routes.TransferStock) || route.startsWith(Routes.AddStockWaste) || route.startsWith(Routes.AddStockAdjustment) || route.startsWith(Routes.AddEditWarehouse) || route.startsWith(Routes.AddMaterialCategory) || route.startsWith(Routes.AddEditMaterial) -> Routes.WarehousesList
        route.startsWith(Routes.FinanceDashboard) || route.startsWith(Routes.AddProjectPayment) || route.startsWith(Routes.AddSupplierPayment) || route.startsWith(Routes.AddEditBankCard) || route.startsWith(Routes.AddExpense) -> Routes.FinanceDashboard
        route.startsWith(Routes.PurchasesList) || route.startsWith(Routes.AddEditPurchase) || route.startsWith(Routes.AddSupplier) -> Routes.PurchasesList
        route.startsWith(Routes.Reports) -> Routes.Reports
        else -> null
    }

private fun titleFor(route: String?): String =
    when {
        route == null || route.startsWith(Routes.Home) -> "خانه"
        route.startsWith(Routes.ProjectsList) -> "پروژه‌ها"
        route.startsWith(Routes.ProjectDetails) -> "جزئیات پروژه"
        route.startsWith(Routes.AddEditProject) -> "فرم پروژه"
        route.startsWith(Routes.MealDeliveryList) || route.startsWith(Routes.AddEditMealDelivery) -> "ثبت وعده"
        route.startsWith(Routes.WarehousesList) -> "انبار"
        route.startsWith(Routes.AddStockIn) -> "ورود کالا"
        route.startsWith(Routes.AddStockOut) -> "خروج کالا"
        route.startsWith(Routes.TransferStock) -> "انتقال کالا"
        route.startsWith(Routes.AddStockWaste) -> "ضایعات کالا"
        route.startsWith(Routes.AddStockAdjustment) -> "اصلاح موجودی"
        route.startsWith(Routes.AddEditWarehouse) -> "فرم انبار"
        route.startsWith(Routes.AddMaterialCategory) -> "دسته‌بندی کالا"
        route.startsWith(Routes.AddEditMaterial) -> "فرم متریال"
        route.startsWith(Routes.FinanceDashboard) -> "مالی"
        route.startsWith(Routes.AddProjectPayment) -> "دریافت پروژه"
        route.startsWith(Routes.AddSupplierPayment) -> "پرداخت تامین‌کننده"
        route.startsWith(Routes.AddEditBankCard) -> "کارت بانکی"
        route.startsWith(Routes.AddExpense) -> "هزینه"
        route.startsWith(Routes.PurchasesList) || route.startsWith(Routes.AddEditPurchase) -> "خرید روزانه"
        route.startsWith(Routes.AddSupplier) -> "تامین‌کننده"
        route.startsWith(Routes.Reports) -> "گزارش‌ها"
        route.startsWith(Routes.GlobalSearch) -> "جستجو"
        route.startsWith(Routes.Settings) -> "تنظیمات"
        else -> "مدیریت غذای شرکتی"
    }
