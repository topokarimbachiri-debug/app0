package com.example.data.local

import androidx.room.withTransaction
import com.example.data.local.entity.AccountTransfer
import com.example.data.local.entity.AccountType
import com.example.data.local.entity.AuditLog
import com.example.data.local.entity.BusinessSettings
import com.example.data.local.entity.Customer
import com.example.data.local.entity.Expense
import com.example.data.local.entity.ExpenseCategory
import com.example.data.local.entity.FinancialAccount
import com.example.data.local.entity.PaymentMethod
import com.example.data.local.entity.PaymentStatus
import com.example.data.local.entity.Product
import com.example.data.local.entity.ProductCategory
import com.example.data.local.entity.Sale
import com.example.data.local.entity.SaleItem
import com.example.data.local.entity.StockMovement
import com.example.data.local.entity.StockMovementType
import com.example.data.local.entity.Supplier
import com.example.data.local.entity.Warehouse

object DemoDataLoader {
    suspend fun populateDemoData(database: AppDatabase) = database.withTransaction {
        val productDao = database.productDao()
        val saleDao = database.saleDao()
        val financeDao = database.financeDao()
        val contactDao = database.contactDao()
        val settingsDao = database.settingsDao()

        // 1. Settings
        val settings = BusinessSettings(
            id = 1,
            businessName = "Apex Commerce & Distribution",
            phone = "+1 (800) 555-0199",
            email = "finance@apexcommerce.com",
            address = "742 Evergreen Blvd, Suite 200",
            taxNumber = "TX-9982410",
            currencyCode = "USD",
            currencySymbol = "$",
            currencyDecimals = 2,
            taxRatePercent = 5.0,
            invoicePrefix = "INV-",
            invoiceNotes = "Net 30 days. Thank you for your partnership!",
            allowNegativeStock = false,
            isDemoLoaded = true
        )
        settingsDao.insertOrUpdateSettings(settings)

        // 2. Warehouses
        val warehouses = listOf(
            Warehouse(id = 1, name = "Central Logistics Hub", location = "Sector 4 Dock A", isDefault = true),
            Warehouse(id = 2, name = "Downtown Retail Depot", location = "4th Street Branch", isDefault = false),
            Warehouse(id = 3, name = "Cold & Secure Storage", location = "Building C", isDefault = false)
        )
        productDao.insertWarehouses(warehouses)

        // 3. Categories
        val categories = listOf(
            ProductCategory(id = 1, name = "Electronics & Hardware", colorHex = "#3B82F6", iconName = "Devices"),
            ProductCategory(id = 2, name = "Office Supplies", colorHex = "#10B981", iconName = "Description"),
            ProductCategory(id = 3, name = "Beverages & Pantry", colorHex = "#F59E0B", iconName = "Fastfood"),
            ProductCategory(id = 4, name = "Industrial Tools", colorHex = "#8B5CF6", iconName = "Build")
        )
        productDao.insertCategories(categories)

        // 4. Products
        val products = listOf(
            Product(
                id = 1,
                name = "Wireless Barcode Scanner Pro",
                sku = "EL-BC-01",
                barcode = "890123456789",
                categoryId = 1,
                description = "High-speed 2D handheld wireless Bluetooth scanner",
                costPriceCents = 4500, // $45.00
                sellingPriceCents = 8900, // $89.00
                taxRatePercent = 5.0,
                unit = "pcs",
                minStockLevel = 5.0,
                currentStock = 18.0,
                warehouseId = 1
            ),
            Product(
                id = 2,
                name = "Thermal Receipt Paper 80mm (Box 50)",
                sku = "OF-TP-50",
                barcode = "890123456790",
                categoryId = 2,
                description = "BPA-free high sensitivity thermal paper rolls",
                costPriceCents = 2200, // $22.00
                sellingPriceCents = 4200, // $42.00
                taxRatePercent = 5.0,
                unit = "box",
                minStockLevel = 10.0,
                currentStock = 4.0, // LOW STOCK!
                warehouseId = 1
            ),
            Product(
                id = 3,
                name = "Ergonomic Office Chair Titan",
                sku = "OF-CH-09",
                barcode = "890123456791",
                categoryId = 2,
                description = "Breathable mesh with lumbar support and 3D armrests",
                costPriceCents = 9500, // $95.00
                sellingPriceCents = 17500, // $175.00
                taxRatePercent = 5.0,
                unit = "pcs",
                minStockLevel = 4.0,
                currentStock = 12.0,
                warehouseId = 1
            ),
            Product(
                id = 4,
                name = "Premium Arabica Coffee Beans 1kg",
                sku = "BV-CF-01",
                barcode = "890123456792",
                categoryId = 3,
                description = "Artisan medium dark roast single-origin whole bean",
                costPriceCents = 1100, // $11.00
                sellingPriceCents = 2400, // $24.00
                taxRatePercent = 0.0,
                unit = "kg",
                minStockLevel = 15.0,
                currentStock = 28.0,
                warehouseId = 2
            ),
            Product(
                id = 5,
                name = "Digital Heavy-Duty Platform Scale",
                sku = "TL-SC-10",
                barcode = "890123456793",
                categoryId = 4,
                description = "Stainless steel 300kg industrial floor scale",
                costPriceCents = 12000, // $120.00
                sellingPriceCents = 21000, // $210.00
                taxRatePercent = 5.0,
                unit = "pcs",
                minStockLevel = 3.0,
                currentStock = 2.0, // LOW STOCK!
                warehouseId = 1
            ),
            Product(
                id = 6,
                name = "USB-C Multiport Docking Station",
                sku = "EL-DK-44",
                barcode = "890123456794",
                categoryId = 1,
                description = "10-in-1 Dual 4K HDMI, 100W PD charging hub",
                costPriceCents = 3200, // $32.00
                sellingPriceCents = 6500, // $65.00
                taxRatePercent = 5.0,
                unit = "pcs",
                minStockLevel = 8.0,
                currentStock = 22.0,
                warehouseId = 1
            ),
            Product(
                id = 7,
                name = "Heavy Duty Pallet Stretch Film",
                sku = "OF-SF-20",
                barcode = "890123456795",
                categoryId = 2,
                description = "500m transparent puncture-resistant wrap roll",
                costPriceCents = 1400, // $14.00
                sellingPriceCents = 2800, // $28.00
                taxRatePercent = 5.0,
                unit = "box",
                minStockLevel = 6.0,
                currentStock = 1.0, // CRITICAL LOW STOCK!
                warehouseId = 1
            )
        )
        productDao.insertProducts(products)

        // 5. Financial Accounts
        val accounts = listOf(
            FinancialAccount(id = 1, name = "Main Cash Register", accountType = AccountType.CASH, balanceCents = 345000, isRestricted = false, isDefault = true),
            FinancialAccount(id = 2, name = "Safe Vault Cash", accountType = AccountType.CASH, balanceCents = 180000, isRestricted = false),
            FinancialAccount(id = 3, name = "Operating Bank (Chase)", accountType = AccountType.BANK, balanceCents = 2450000, accountNumber = "**** 4920", isRestricted = false),
            FinancialAccount(id = 4, name = "Tax & Escrow Reserve", accountType = AccountType.RESTRICTED, balanceCents = 500000, accountNumber = "**** 8812", isRestricted = true)
        )
        financeDao.insertAccounts(accounts)

        // 6. Customers
        val customers = listOf(
            Customer(id = 1, name = "Metro Mart Superstore", phone = "+1 (555) 302-9912", email = "orders@metromart.com", creditLimitCents = 500000, currentBalanceCents = 125000), // owes $1,250.00
            Customer(id = 2, name = "Horizon Tech Labs", phone = "+1 (555) 778-1200", email = "procurement@horizon.io", creditLimitCents = 800000, currentBalanceCents = 0),
            Customer(id = 3, name = "Blue Oak Bistro & Cafe", phone = "+1 (555) 441-8933", email = "manager@blueoak.com", creditLimitCents = 200000, currentBalanceCents = 48000), // owes $480.00
            Customer(id = 4, name = "Apex Industrial Supplies", phone = "+1 (555) 902-3344", email = "info@apexind.com", creditLimitCents = 1000000, currentBalanceCents = 210000) // owes $2,100.00
        )
        contactDao.insertCustomers(customers)

        // 7. Suppliers
        val suppliers = listOf(
            Supplier(id = 1, name = "Global Tech Manufacturing", phone = "+1 (800) 443-0911", email = "sales@globaltechmfg.com", currentBalanceCents = 220000), // we owe $2,200.00
            Supplier(id = 2, name = "Cascade Paper & Pulp Co.", phone = "+1 (800) 229-8810", email = "orders@cascadepaper.com", currentBalanceCents = 45000), // we owe $450.00
            Supplier(id = 3, name = "San Pedro Coffee Importers", phone = "+1 (800) 912-7744", email = "contact@sanpedrocoffee.com", currentBalanceCents = 0)
        )
        contactDao.insertSuppliers(suppliers)

        // 8. Recent Sales & Items
        val now = System.currentTimeMillis()
        val dayMs = 86400000L

        val sales = listOf(
            Sale(
                id = 1,
                invoiceNumber = "INV-100201",
                customerId = 1,
                customerName = "Metro Mart Superstore",
                subtotalCents = 17800,
                discountCents = 0,
                taxCents = 890,
                totalCents = 18690,
                paidAmountCents = 18690,
                paymentStatus = PaymentStatus.PAID,
                paymentMethod = PaymentMethod.BANK_TRANSFER,
                financialAccountId = 3,
                cogsCents = 9000,
                saleDate = now - (dayMs * 2)
            ),
            Sale(
                id = 2,
                invoiceNumber = "INV-100202",
                customerId = 3,
                customerName = "Blue Oak Bistro & Cafe",
                subtotalCents = 48000,
                discountCents = 0,
                taxCents = 0,
                totalCents = 48000,
                paidAmountCents = 0, // Unpaid credit sale!
                paymentStatus = PaymentStatus.UNPAID,
                paymentMethod = PaymentMethod.CREDIT,
                financialAccountId = null,
                cogsCents = 22000,
                saleDate = now - (dayMs * 1),
                dueDate = now + (dayMs * 14)
            ),
            Sale(
                id = 3,
                invoiceNumber = "INV-100203",
                customerId = 2,
                customerName = "Horizon Tech Labs",
                subtotalCents = 13000,
                discountCents = 1000,
                taxCents = 600,
                totalCents = 12600,
                paidAmountCents = 12600,
                paymentStatus = PaymentStatus.PAID,
                paymentMethod = PaymentMethod.CARD,
                financialAccountId = 1,
                cogsCents = 6400,
                saleDate = now - 3600000L
            )
        )
        saleDao.insertSales(sales)

        val saleItems = listOf(
            SaleItem(saleId = 1, productId = 1, productName = "Wireless Barcode Scanner Pro", sku = "EL-BC-01", quantity = 2.0, unitPriceCents = 8900, costPriceCents = 4500, totalCents = 17800),
            SaleItem(saleId = 2, productId = 4, productName = "Premium Arabica Coffee Beans 1kg", sku = "BV-CF-01", quantity = 20.0, unitPriceCents = 2400, costPriceCents = 1100, totalCents = 48000),
            SaleItem(saleId = 3, productId = 6, productName = "USB-C Multiport Docking Station", sku = "EL-DK-44", quantity = 2.0, unitPriceCents = 6500, costPriceCents = 3200, discountCents = 1000, totalCents = 12000)
        )
        saleDao.insertSaleItems(saleItems)

        // 9. Initial Stock movements
        val movements = listOf(
            StockMovement(productId = 1, warehouseId = 1, movementType = StockMovementType.SALE, quantity = -2.0, unitCostCents = 4500, referenceNumber = "INV-100201", notes = "Sale to Metro Mart"),
            StockMovement(productId = 4, warehouseId = 2, movementType = StockMovementType.SALE, quantity = -20.0, unitCostCents = 1100, referenceNumber = "INV-100202", notes = "Sale to Blue Oak"),
            StockMovement(productId = 6, warehouseId = 1, movementType = StockMovementType.SALE, quantity = -2.0, unitCostCents = 3200, referenceNumber = "INV-100203", notes = "Sale to Horizon Tech")
        )
        movements.forEach { productDao.insertStockMovement(it) }

        // 10. Expenses
        val expenses = listOf(
            Expense(category = ExpenseCategory.RENT, amountCents = 120000, financialAccountId = 3, recipient = "Empire Properties LLC", description = "Monthly Commercial Warehouse Lease", expenseDate = now - (dayMs * 5)),
            Expense(category = ExpenseCategory.UTILITIES, amountCents = 24000, financialAccountId = 3, recipient = "Metro Power & Light", description = "High-speed Fiber & Electricity", expenseDate = now - (dayMs * 4)),
            Expense(category = ExpenseCategory.LOGISTICS, amountCents = 18500, financialAccountId = 1, recipient = "Swift Freight Courier", description = "Express pallet distribution fee", expenseDate = now - (dayMs * 2)),
            Expense(category = ExpenseCategory.SALARIES, amountCents = 250000, financialAccountId = 3, recipient = "Payroll Direct Deposit", description = "Mid-month staff advances", expenseDate = now - (dayMs * 1))
        )
        financeDao.insertExpenses(expenses)

        // 11. Account Transfer
        val transfer = AccountTransfer(
            fromAccountId = 1,
            toAccountId = 2,
            amountCents = 100000,
            feeCents = 0,
            transferDate = now - (dayMs * 3),
            notes = "End-of-day register excess moved to safe"
        )
        financeDao.insertTransfer(transfer)

        // 12. Audit Log
        settingsDao.insertAuditLog(
            AuditLog(action = "INIT_DEMO_DATA", entityType = "System", entityId = "0", details = "Demo business dataset initialized successfully")
        )
    }
}
