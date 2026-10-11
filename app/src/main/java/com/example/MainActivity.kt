package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.local.AppDatabase
import com.example.data.local.DemoDataLoader
import com.example.data.local.entity.BusinessSettings
import com.example.data.repository.BusinessRepository
import com.example.ui.components.FloatingSpeedDial
import com.example.ui.navigation.Screen
import com.example.ui.screens.barcode.BarcodeScannerScreen
import com.example.ui.screens.contacts.CustomersScreen
import com.example.ui.screens.contacts.SuppliersScreen
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.finance.AccountTransferScreen
import com.example.ui.screens.finance.AddExpenseScreen
import com.example.ui.screens.finance.CashClosingScreen
import com.example.ui.screens.finance.FinanceOverviewScreen
import com.example.ui.screens.finance.PaySupplierScreen
import com.example.ui.screens.finance.ReceivePaymentScreen
import com.example.ui.screens.inventory.AddEditProductScreen
import com.example.ui.screens.inventory.InventoryScreen
import com.example.ui.screens.inventory.StockAdjustmentScreen
import com.example.ui.screens.inventory.StockMovementLedgerScreen
import com.example.ui.screens.inventory.WarehouseManagementScreen
import com.example.ui.screens.more.MoreScreen
import com.example.ui.screens.reports.ReportsScreen
import com.example.ui.screens.sales.InvoiceDetailScreen
import com.example.ui.screens.sales.NewSaleScreen
import com.example.ui.screens.sales.SalesListScreen
import com.example.ui.screens.sales.SalesReturnsScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.smart.SmartAdvisorScreen
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.MyApplicationTheme
import com.example.util.Localization
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = BusinessRepository(database)

        setContent {
            val settingsState by repository.settings.collectAsStateWithLifecycle(initialValue = null)
            val settings = settingsState ?: BusinessSettings()

            // Initialize Demo Data automatically if never loaded
            LaunchedEffect(Unit) {
                withContext(Dispatchers.IO) {
                    val current = repository.getSettingsDirect()
                    if (!current.isDemoLoaded) {
                        DemoDataLoader.populateDemoData(database)
                    }
                }
            }

            val layoutDirection = if (Localization.isRtl(settings.language)) LayoutDirection.Rtl else LayoutDirection.Ltr

            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                MyApplicationTheme {
                    MainAppContent(repository = repository, settings = settings)
                }
            }
        }
    }
}

@Composable
fun MainAppContent(
    repository: BusinessRepository,
    settings: BusinessSettings
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val lang = settings.language

    val bottomNavItems = listOf(
        NavigationItem(Screen.Dashboard.route, Localization.getString("dashboard", lang), Icons.Default.Dashboard, "nav_dashboard"),
        NavigationItem(Screen.Sales.route, Localization.getString("sales", lang), Icons.Default.PointOfSale, "nav_sales"),
        NavigationItem(Screen.Inventory.route, Localization.getString("inventory", lang), Icons.Default.Inventory2, "nav_inventory"),
        NavigationItem(Screen.Finance.route, Localization.getString("finance", lang), Icons.Default.AccountBalanceWallet, "nav_finance"),
        NavigationItem(Screen.More.route, Localization.getString("more", lang), Icons.Default.MoreHoriz, "nav_more")
    )

    val isTopLevelDestination = bottomNavItems.any { it.route == currentRoute }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (isTopLevelDestination) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentRoute == item.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = EmeraldGreen.copy(alpha = 0.2f),
                                selectedIconColor = EmeraldGreen,
                                selectedTextColor = EmeraldGreen
                            ),
                            modifier = Modifier.testTag(item.tag)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Dashboard.route,
                modifier = Modifier.fillMaxSize()
            ) {
                // Bottom tabs
                composable(Screen.Dashboard.route) {
                    DashboardScreen(
                        repository = repository,
                        onNavigateToSales = { navController.navigate(Screen.Sales.route) },
                        onNavigateToInventory = { navController.navigate(Screen.Inventory.route) },
                        onNavigateToFinance = { navController.navigate(Screen.Finance.route) },
                        onNavigateToInvoice = { saleId -> navController.navigate(Screen.InvoiceDetail.createRoute(saleId)) },
                        onNavigateToProduct = { prodId -> navController.navigate(Screen.AddEditProduct.createRoute(prodId)) }
                    )
                }

                composable(Screen.Sales.route) {
                    SalesListScreen(
                        repository = repository,
                        onNavigateToNewSale = { navController.navigate(Screen.NewSale.route) },
                        onNavigateToInvoice = { saleId -> navController.navigate(Screen.InvoiceDetail.createRoute(saleId)) },
                        onNavigateToReturns = { navController.navigate(Screen.SalesReturns.route) }
                    )
                }

                composable(Screen.Inventory.route) {
                    InventoryScreen(
                        repository = repository,
                        onNavigateToAddProduct = { navController.navigate(Screen.AddEditProduct.createRoute(0L)) },
                        onNavigateToEditProduct = { prodId -> navController.navigate(Screen.AddEditProduct.createRoute(prodId)) },
                        onNavigateToStockAdjustment = { navController.navigate(Screen.StockAdjustment.route) },
                        onNavigateToBarcodeScanner = { navController.navigate(Screen.BarcodeScanner.route) },
                        onNavigateToLedger = { navController.navigate(Screen.StockMovementLedger.route) },
                        onNavigateToWarehouses = { navController.navigate(Screen.WarehouseManagement.route) }
                    )
                }

                composable(Screen.Finance.route) {
                    FinanceOverviewScreen(
                        repository = repository,
                        onNavigateToAddExpense = { navController.navigate(Screen.AddExpense.route) },
                        onNavigateToTransfer = { navController.navigate(Screen.AccountTransfer.route) },
                        onNavigateToCashClosing = { navController.navigate(Screen.CashClosing.route) },
                        onNavigateToReceivePayment = { navController.navigate(Screen.ReceivePayment.route) },
                        onNavigateToPaySupplier = { navController.navigate(Screen.PaySupplier.route) }
                    )
                }

                composable(Screen.More.route) {
                    MoreScreen(
                        repository = repository,
                        onNavigateToCustomers = { navController.navigate(Screen.Customers.route) },
                        onNavigateToSuppliers = { navController.navigate(Screen.Suppliers.route) },
                        onNavigateToReports = { navController.navigate(Screen.Reports.route) },
                        onNavigateToAdvisor = { navController.navigate(Screen.SmartAdvisor.route) },
                        onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
                    )
                }

                // Sub-screens
                composable(Screen.NewSale.route) {
                    NewSaleScreen(
                        repository = repository,
                        onNavigateBack = { navController.popBackStack() },
                        onSaleCreated = { saleId ->
                            navController.popBackStack()
                            navController.navigate(Screen.InvoiceDetail.createRoute(saleId))
                        }
                    )
                }

                composable(
                    route = Screen.InvoiceDetail.route,
                    arguments = listOf(navArgument("saleId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val saleId = backStackEntry.arguments?.getLong("saleId") ?: 0L
                    InvoiceDetailScreen(
                        saleId = saleId,
                        repository = repository,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.SalesReturns.route) {
                    SalesReturnsScreen(
                        repository = repository,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(
                    route = Screen.AddEditProduct.route,
                    arguments = listOf(navArgument("productId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val prodId = backStackEntry.arguments?.getLong("productId") ?: 0L
                    AddEditProductScreen(
                        productId = prodId,
                        repository = repository,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.BarcodeScanner.route) {
                    BarcodeScannerScreen(
                        repository = repository,
                        onNavigateBack = { navController.popBackStack() },
                        onProductFound = { prodId ->
                            navController.popBackStack()
                            navController.navigate(Screen.AddEditProduct.createRoute(prodId))
                        }
                    )
                }

                composable(Screen.StockAdjustment.route) {
                    StockAdjustmentScreen(
                        repository = repository,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.StockMovementLedger.route) {
                    StockMovementLedgerScreen(
                        repository = repository,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.WarehouseManagement.route) {
                    WarehouseManagementScreen(
                        repository = repository,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.AddExpense.route) {
                    AddExpenseScreen(
                        repository = repository,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.AccountTransfer.route) {
                    AccountTransferScreen(
                        repository = repository,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.CashClosing.route) {
                    CashClosingScreen(
                        repository = repository,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.ReceivePayment.route) {
                    ReceivePaymentScreen(
                        repository = repository,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.PaySupplier.route) {
                    PaySupplierScreen(
                        repository = repository,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.Customers.route) {
                    CustomersScreen(
                        repository = repository,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.Suppliers.route) {
                    SuppliersScreen(
                        repository = repository,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.Reports.route) {
                    ReportsScreen(
                        repository = repository,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.SmartAdvisor.route) {
                    SmartAdvisorScreen(
                        repository = repository,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.Settings.route) {
                    SettingsScreen(
                        repository = repository,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }

            // Speed Dial Floating Action Button (shown on top-level tabs)
            if (isTopLevelDestination) {
                FloatingSpeedDial(
                    onNewSale = { navController.navigate(Screen.NewSale.route) },
                    onAddProduct = { navController.navigate(Screen.AddEditProduct.createRoute(0L)) },
                    onAddExpense = { navController.navigate(Screen.AddExpense.route) },
                    onReceivePayment = { navController.navigate(Screen.ReceivePayment.route) },
                    onStockAdjustment = { navController.navigate(Screen.StockAdjustment.route) },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 16.dp, end = 16.dp)
                )
            }
        }
    }
}

private data class NavigationItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val tag: String
)
