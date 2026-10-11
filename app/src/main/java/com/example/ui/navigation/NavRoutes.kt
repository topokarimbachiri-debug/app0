package com.example.ui.navigation

sealed class Screen(val route: String) {
    // Bottom Navigation destinations
    data object Dashboard : Screen("dashboard")
    data object Sales : Screen("sales")
    data object Inventory : Screen("inventory")
    data object Finance : Screen("finance")
    data object More : Screen("more")

    // Sub-screens
    data object NewSale : Screen("new_sale")
    data object InvoiceDetail : Screen("invoice_detail/{saleId}") {
        fun createRoute(saleId: Long) = "invoice_detail/$saleId"
    }
    data object SalesReturns : Screen("sales_returns")

    data object AddEditProduct : Screen("add_edit_product/{productId}") {
        fun createRoute(productId: Long) = "add_edit_product/$productId"
    }
    data object BarcodeScanner : Screen("barcode_scanner")
    data object StockAdjustment : Screen("stock_adjustment")
    data object StockMovementLedger : Screen("stock_movement_ledger")
    data object WarehouseManagement : Screen("warehouse_management")

    data object AddExpense : Screen("add_expense")
    data object AccountTransfer : Screen("account_transfer")
    data object CashClosing : Screen("cash_closing")
    data object ReceivePayment : Screen("receive_payment")
    data object PaySupplier : Screen("pay_supplier")

    data object Customers : Screen("customers")
    data object Suppliers : Screen("suppliers")
    data object ContactDetail : Screen("contact_detail/{type}/{contactId}") {
        fun createRoute(type: String, contactId: Long) = "contact_detail/$type/$contactId"
    }

    data object Reports : Screen("reports")
    data object SmartAdvisor : Screen("smart_advisor")
    data object Settings : Screen("settings")
    data object SecurityLock : Screen("security_lock")
}
