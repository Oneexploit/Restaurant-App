package com.restaurant.offlinemanager.core.navigation

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
import com.restaurant.offlinemanager.ui.reports.ReportsScreen
import com.restaurant.offlinemanager.ui.search.SearchScreen
import com.restaurant.offlinemanager.ui.settings.SettingsScreen
import com.restaurant.offlinemanager.ui.warehouse.MaterialFormScreen
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
        snackbarHostState = snackbarHostState
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.Home,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.Home) {
                HomeScreen(
                    state = state,
                    onAddProject = { navController.navigate("${Routes.AddEditProject}/0") },
                    onAddMeal = { navController.navigate("${Routes.AddEditMealDelivery}/0") },
                    onStockIn = { navController.navigate(Routes.AddStockIn) },
                    onAddPurchase = { navController.navigate(Routes.AddEditPurchase) },
                    onAddPayment = { navController.navigate("${Routes.AddProjectPayment}/0") },
                    onReports = { navController.navigate(Routes.Reports) }
                )
            }
            composable(Routes.ProjectsList) {
                ProjectsListScreen(
                    state = state,
                    onAddProject = { navController.navigate("${Routes.AddEditProject}/0") },
                    onProjectDetails = { navController.navigate("${Routes.ProjectDetails}/$it") }
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
                    viewModel.saveProject(it)
                    navController.popBackStack()
                })
            }
            composable(Routes.MealDeliveryList) {
                MealDeliveryListScreen(state, onAddMeal = { navController.navigate("${Routes.AddEditMealDelivery}/0") })
            }
            composable(
                route = "${Routes.AddEditMealDelivery}/{projectId}",
                arguments = listOf(navArgument("projectId") { type = NavType.LongType })
            ) { entry ->
                val projectId = entry.arguments?.getLong("projectId")?.takeIf { it != 0L }
                MealDeliveryFormScreen(state, projectId, onSave = {
                    viewModel.saveMealDelivery(it)
                    navController.popBackStack()
                })
            }
            composable(Routes.WarehousesList) {
                WarehouseMainScreen(
                    state = state,
                    onStockIn = { navController.navigate(Routes.AddStockIn) },
                    onStockOut = { navController.navigate(Routes.AddStockOut) },
                    onTransfer = { navController.navigate(Routes.TransferStock) },
                    onAddWarehouse = { navController.navigate(Routes.AddEditWarehouse) },
                    onAddMaterial = { navController.navigate(Routes.AddEditMaterial) }
                )
            }
            composable(Routes.AddStockIn) {
                StockTransactionFormScreen(state, StockTransactionType.IN, onSave = {
                    viewModel.saveStockTransactions(it)
                    navController.popBackStack()
                })
            }
            composable(Routes.AddStockOut) {
                StockTransactionFormScreen(state, StockTransactionType.OUT, onSave = {
                    viewModel.saveStockTransactions(it)
                    navController.popBackStack()
                })
            }
            composable(Routes.TransferStock) {
                StockTransactionFormScreen(state, StockTransactionType.TRANSFER_OUT, onSave = {
                    viewModel.saveStockTransactions(it)
                    navController.popBackStack()
                })
            }
            composable(Routes.AddEditWarehouse) {
                WarehouseFormScreen(onSave = {
                    viewModel.saveWarehouse(it)
                    navController.popBackStack()
                })
            }
            composable(Routes.AddEditMaterial) {
                MaterialFormScreen(state, onSave = {
                    viewModel.saveMaterial(it)
                    navController.popBackStack()
                })
            }
            composable(Routes.PurchasesList) {
                PurchasesListScreen(state, onAddPurchase = { navController.navigate(Routes.AddEditPurchase) })
            }
            composable(Routes.AddEditPurchase) {
                PurchaseFormScreen(state, onSave = {
                    viewModel.savePurchase(it)
                    navController.popBackStack()
                })
            }
            composable(Routes.FinanceDashboard) {
                FinanceDashboardScreen(
                    state = state,
                    onAddProjectPayment = { navController.navigate("${Routes.AddProjectPayment}/0") },
                    onAddSupplierPayment = { navController.navigate(Routes.AddSupplierPayment) },
                    onAddBankCard = { navController.navigate(Routes.AddEditBankCard) },
                    onAddExpense = { navController.navigate(Routes.AddExpense) }
                )
            }
            composable(
                route = "${Routes.AddProjectPayment}/{projectId}",
                arguments = listOf(navArgument("projectId") { type = NavType.LongType })
            ) { entry ->
                val projectId = entry.arguments?.getLong("projectId")?.takeIf { it != 0L }
                ProjectPaymentFormScreen(state, projectId, onSave = {
                    viewModel.saveProjectPayment(it)
                    navController.popBackStack()
                })
            }
            composable(Routes.AddSupplierPayment) {
                SupplierPaymentFormScreen(state, onSave = {
                    viewModel.saveSupplierPayment(it)
                    navController.popBackStack()
                })
            }
            composable(Routes.AddEditBankCard) {
                BankCardFormScreen(onSave = {
                    viewModel.saveBankCard(it)
                    navController.popBackStack()
                })
            }
            composable(Routes.AddExpense) {
                ExpenseFormScreen(state, onSave = {
                    viewModel.saveExpense(it)
                    navController.popBackStack()
                })
            }
            composable(Routes.Reports) {
                ReportsScreen(state, context, onExport = viewModel::exportCsv)
            }
            composable(Routes.GlobalSearch) {
                SearchScreen(state)
            }
            composable(Routes.Settings) {
                SettingsScreen(
                    state = state,
                    context = context,
                    onDarkMode = viewModel::setDarkMode,
                    onAppLock = viewModel::setAppLock,
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
        route.startsWith(Routes.WarehousesList) || route.startsWith(Routes.AddStockIn) || route.startsWith(Routes.AddStockOut) || route.startsWith(Routes.TransferStock) || route.startsWith(Routes.AddEditWarehouse) || route.startsWith(Routes.AddEditMaterial) -> Routes.WarehousesList
        route.startsWith(Routes.PurchasesList) || route.startsWith(Routes.AddEditPurchase) -> Routes.PurchasesList
        route.startsWith(Routes.FinanceDashboard) || route.startsWith(Routes.AddProjectPayment) || route.startsWith(Routes.AddSupplierPayment) || route.startsWith(Routes.AddEditBankCard) || route.startsWith(Routes.AddExpense) -> Routes.FinanceDashboard
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
        route.startsWith(Routes.AddEditWarehouse) -> "فرم انبار"
        route.startsWith(Routes.AddEditMaterial) -> "فرم متریال"
        route.startsWith(Routes.PurchasesList) || route.startsWith(Routes.AddEditPurchase) -> "خرید روزانه"
        route.startsWith(Routes.FinanceDashboard) -> "مالی"
        route.startsWith(Routes.AddProjectPayment) -> "دریافت پروژه"
        route.startsWith(Routes.AddSupplierPayment) -> "پرداخت تامین‌کننده"
        route.startsWith(Routes.AddEditBankCard) -> "کارت بانکی"
        route.startsWith(Routes.AddExpense) -> "هزینه"
        route.startsWith(Routes.Reports) -> "گزارش‌ها"
        route.startsWith(Routes.GlobalSearch) -> "جستجو"
        route.startsWith(Routes.Settings) -> "تنظیمات"
        else -> "مدیریت غذای شرکتی"
    }
