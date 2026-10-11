package com.example.data.repository

import androidx.room.withTransaction
import com.example.data.local.AppDatabase
import com.example.data.local.entity.AccountTransfer
import com.example.data.local.entity.AccountType
import com.example.data.local.entity.AuditLog
import com.example.data.local.entity.BusinessSettings
import com.example.data.local.entity.Customer
import com.example.data.local.entity.CustomerPayment
import com.example.data.local.entity.DailyCashClosing
import com.example.data.local.entity.Expense
import com.example.data.local.entity.FinancialAccount
import com.example.data.local.entity.PaymentMethod
import com.example.data.local.entity.PaymentStatus
import com.example.data.local.entity.Product
import com.example.data.local.entity.ProductCategory
import com.example.data.local.entity.Purchase
import com.example.data.local.entity.PurchaseItem
import com.example.data.local.entity.Sale
import com.example.data.local.entity.SaleItem
import com.example.data.local.entity.SalesReturn
import com.example.data.local.entity.StockMovement
import com.example.data.local.entity.StockMovementType
import com.example.data.local.entity.Supplier
import com.example.data.local.entity.SupplierPayment
import com.example.data.local.entity.Warehouse
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class BusinessRepository(private val db: AppDatabase) {
    private val productDao = db.productDao()
    private val saleDao = db.saleDao()
    private val financeDao = db.financeDao()
    private val contactDao = db.contactDao()
    private val settingsDao = db.settingsDao()

    // Products & Stock
    val allProducts: Flow<List<Product>> = productDao.getAllProducts()
    val activeProducts: Flow<List<Product>> = productDao.getActiveProducts()
    val lowStockProducts: Flow<List<Product>> = productDao.getLowStockProducts()
    val categories: Flow<List<ProductCategory>> = productDao.getCategories()
    val warehouses: Flow<List<Warehouse>> = productDao.getWarehouses()
    val allStockMovements: Flow<List<StockMovement>> = productDao.getAllStockMovements()

    fun getProduct(id: Long): Flow<Product?> = productDao.getProductById(id)
    suspend fun getProductDirect(id: Long): Product? = productDao.getProductByIdDirect(id)
    suspend fun findProductByBarcode(barcode: String): Product? = productDao.getProductByBarcode(barcode)
    fun searchProducts(query: String): Flow<List<Product>> = productDao.searchProducts(query)

    suspend fun saveProduct(product: Product): Long {
        return if (product.id == 0L) {
            val id = productDao.insertProduct(product)
            settingsDao.insertAuditLog(
                AuditLog(action = "CREATE_PRODUCT", entityType = "Product", entityId = id.toString(), details = "Created ${product.name}")
            )
            id
        } else {
            productDao.updateProduct(product)
            settingsDao.insertAuditLog(
                AuditLog(action = "UPDATE_PRODUCT", entityType = "Product", entityId = product.id.toString(), details = "Updated ${product.name}")
            )
            product.id
        }
    }

    suspend fun adjustStock(
        productId: Long,
        newStock: Double,
        type: StockMovementType,
        reason: String,
        warehouseId: Long = 1,
        performedBy: String = "Admin"
    ) = db.withTransaction {
        val product = productDao.getProductByIdDirect(productId) ?: return@withTransaction
        val diff = newStock - product.currentStock
        productDao.updateStock(productId, newStock)
        productDao.insertStockMovement(
            StockMovement(
                productId = productId,
                warehouseId = warehouseId,
                movementType = type,
                quantity = diff,
                unitCostCents = product.costPriceCents,
                referenceNumber = "ADJ-" + System.currentTimeMillis().toString().takeLast(6),
                notes = reason,
                performedBy = performedBy
            )
        )
        settingsDao.insertAuditLog(
            AuditLog(
                action = "STOCK_ADJUSTMENT",
                entityType = "Product",
                entityId = productId.toString(),
                details = "Stock changed from ${product.currentStock} to $newStock: $reason",
                performedBy = performedBy
            )
        )
    }

    // Sales & Invoices
    val allSales: Flow<List<Sale>> = saleDao.getAllSales()
    val recentSales: Flow<List<Sale>> = saleDao.getRecentSales(15)
    val salesReturns: Flow<List<SalesReturn>> = saleDao.getAllSalesReturns()

    fun getSale(id: Long): Flow<Sale?> = saleDao.getSaleById(id)
    suspend fun getSaleDirect(id: Long): Sale? = saleDao.getSaleByIdDirect(id)
    fun getSaleItems(saleId: Long): Flow<List<SaleItem>> = saleDao.getSaleItems(saleId)

    data class SaleCartItem(
        val product: Product,
        val quantity: Double,
        val unitPriceCents: Long,
        val discountCents: Long = 0
    )

    suspend fun createSale(
        customerId: Long?,
        customerName: String,
        cartItems: List<SaleCartItem>,
        discountCents: Long,
        taxRatePercent: Double,
        paymentStatus: PaymentStatus,
        paymentMethod: PaymentMethod,
        paidAmountCents: Long,
        financialAccountId: Long?,
        warehouseId: Long = 1,
        notes: String = ""
    ): Long = db.withTransaction {
        val invoiceNum = "INV-" + (System.currentTimeMillis() % 1000000).toString().padStart(6, '0')
        var subtotal = 0L
        var totalCogs = 0L

        cartItems.forEach { item ->
            val lineTotal = (item.quantity * item.unitPriceCents).toLong() - item.discountCents
            subtotal += lineTotal
            totalCogs += (item.quantity * item.product.costPriceCents).toLong()
        }

        val taxable = (subtotal - discountCents).coerceAtLeast(0L)
        val taxCents = ((taxable * taxRatePercent) / 100.0).toLong()
        val total = taxable + taxCents

        val sale = Sale(
            invoiceNumber = invoiceNum,
            customerId = customerId,
            customerName = customerName,
            subtotalCents = subtotal,
            discountCents = discountCents,
            taxCents = taxCents,
            totalCents = total,
            paidAmountCents = paidAmountCents,
            paymentStatus = paymentStatus,
            paymentMethod = paymentMethod,
            financialAccountId = financialAccountId,
            cogsCents = totalCogs,
            warehouseId = warehouseId,
            notes = notes,
            saleDate = System.currentTimeMillis()
        )
        val saleId = saleDao.insertSale(sale)

        val saleItems = cartItems.map { item ->
            val lineTotal = (item.quantity * item.unitPriceCents).toLong() - item.discountCents
            SaleItem(
                saleId = saleId,
                productId = item.product.id,
                productName = item.product.name,
                sku = item.product.sku,
                quantity = item.quantity,
                unitPriceCents = item.unitPriceCents,
                costPriceCents = item.product.costPriceCents,
                discountCents = item.discountCents,
                totalCents = lineTotal
            )
        }
        saleDao.insertSaleItems(saleItems)

        // Deduct inventory & record movement
        cartItems.forEach { item ->
            val updatedStock = item.product.currentStock - item.quantity
            productDao.updateStock(item.product.id, updatedStock)
            productDao.insertStockMovement(
                StockMovement(
                    productId = item.product.id,
                    warehouseId = warehouseId,
                    movementType = StockMovementType.SALE,
                    quantity = -item.quantity,
                    unitCostCents = item.product.costPriceCents,
                    referenceNumber = invoiceNum,
                    notes = "Sale to $customerName"
                )
            )
        }

        // Update financial account balance for cash/card collected
        if (paidAmountCents > 0 && financialAccountId != null) {
            val account = financeDao.getAccountByIdDirect(financialAccountId)
            if (account != null) {
                financeDao.updateBalance(financialAccountId, account.balanceCents + paidAmountCents)
            }
        }

        // Update customer balance if unpaid / credit balance remains
        val balanceDue = total - paidAmountCents
        if (balanceDue > 0 && customerId != null) {
            val customer = contactDao.getCustomerByIdDirect(customerId)
            if (customer != null) {
                contactDao.updateCustomerBalance(customerId, customer.currentBalanceCents + balanceDue)
            }
        }

        settingsDao.insertAuditLog(
            AuditLog(
                action = "CREATE_SALE",
                entityType = "Sale",
                entityId = saleId.toString(),
                details = "Invoice $invoiceNum for total $total, paid $paidAmountCents"
            )
        )
        saleId
    }

    suspend fun processSalesReturn(
        saleId: Long,
        reason: String,
        refundAmountCents: Long,
        isRestocked: Boolean,
        financialAccountId: Long?
    ) = db.withTransaction {
        val sale = saleDao.getSaleByIdDirect(saleId) ?: return@withTransaction
        val returnNum = "RET-" + System.currentTimeMillis().toString().takeLast(6)
        val returnRecord = SalesReturn(
            saleId = saleId,
            returnNumber = returnNum,
            invoiceNumber = sale.invoiceNumber,
            reason = reason,
            refundAmountCents = refundAmountCents,
            isRestocked = isRestocked,
            financialAccountId = financialAccountId
        )
        saleDao.insertSalesReturn(returnRecord)
        saleDao.updateSale(sale.copy(isReturned = true, paymentStatus = PaymentStatus.REFUNDED))

        // Restock inventory if selected
        if (isRestocked) {
            val items = saleDao.getSaleItemsDirect(saleId)
            items.forEach { item ->
                val prod = productDao.getProductByIdDirect(item.productId)
                if (prod != null) {
                    val newStock = prod.currentStock + item.quantity
                    productDao.updateStock(prod.id, newStock)
                    productDao.insertStockMovement(
                        StockMovement(
                            productId = prod.id,
                            warehouseId = sale.warehouseId,
                            movementType = StockMovementType.SALE_RETURN,
                            quantity = item.quantity,
                            unitCostCents = item.costPriceCents,
                            referenceNumber = returnNum,
                            notes = "Return: $reason"
                        )
                    )
                }
            }
        }

        // Refund cash from account
        if (refundAmountCents > 0 && financialAccountId != null) {
            val account = financeDao.getAccountByIdDirect(financialAccountId)
            if (account != null) {
                financeDao.updateBalance(financialAccountId, account.balanceCents - refundAmountCents)
            }
        }

        // If customer had an outstanding balance on this invoice, reduce it
        val unpaidOnSale = (sale.totalCents - sale.paidAmountCents).coerceAtLeast(0L)
        if (sale.customerId != null && unpaidOnSale > 0) {
            val cust = contactDao.getCustomerByIdDirect(sale.customerId)
            if (cust != null) {
                val newCustBal = (cust.currentBalanceCents - unpaidOnSale).coerceAtLeast(0L)
                contactDao.updateCustomerBalance(cust.id, newCustBal)
            }
        }

        settingsDao.insertAuditLog(
            AuditLog(
                action = "SALES_RETURN",
                entityType = "Sale",
                entityId = saleId.toString(),
                details = "Return $returnNum processed. Refund: $refundAmountCents"
            )
        )
    }

    // Purchases
    val allPurchases: Flow<List<Purchase>> = saleDao.getAllPurchases()

    data class PurchaseCartItem(
        val product: Product,
        val quantity: Double,
        val unitCostCents: Long
    )

    suspend fun createPurchase(
        supplierId: Long?,
        supplierName: String,
        items: List<PurchaseCartItem>,
        paidAmountCents: Long,
        paymentStatus: PaymentStatus,
        financialAccountId: Long?,
        warehouseId: Long = 1,
        notes: String = ""
    ): Long = db.withTransaction {
        val poNum = "PO-" + (System.currentTimeMillis() % 1000000).toString().padStart(6, '0')
        var subtotal = 0L
        items.forEach { item ->
            subtotal += (item.quantity * item.unitCostCents).toLong()
        }

        val purchase = Purchase(
            purchaseNumber = poNum,
            supplierId = supplierId,
            supplierName = supplierName,
            subtotalCents = subtotal,
            totalCents = subtotal,
            paidAmountCents = paidAmountCents,
            paymentStatus = paymentStatus,
            financialAccountId = financialAccountId,
            warehouseId = warehouseId,
            notes = notes
        )
        val purchaseId = saleDao.insertPurchase(purchase)

        val pItems = items.map { item ->
            PurchaseItem(
                purchaseId = purchaseId,
                productId = item.product.id,
                productName = item.product.name,
                quantity = item.quantity,
                unitCostCents = item.unitCostCents,
                totalCents = (item.quantity * item.unitCostCents).toLong()
            )
        }
        saleDao.insertPurchaseItems(pItems)

        // Add to inventory & update cost
        items.forEach { item ->
            val currentStock = item.product.currentStock
            val newStock = currentStock + item.quantity
            // Weighted average cost calculation
            val currentVal = (currentStock.coerceAtLeast(0.0) * item.product.costPriceCents).toLong()
            val addedVal = (item.quantity * item.unitCostCents).toLong()
            val newUnitCost = if (newStock > 0) ((currentVal + addedVal) / newStock).toLong() else item.unitCostCents

            productDao.updateProduct(
                item.product.copy(
                    currentStock = newStock,
                    costPriceCents = newUnitCost
                )
            )
            productDao.insertStockMovement(
                StockMovement(
                    productId = item.product.id,
                    warehouseId = warehouseId,
                    movementType = StockMovementType.PURCHASE,
                    quantity = item.quantity,
                    unitCostCents = item.unitCostCents,
                    referenceNumber = poNum,
                    notes = "Purchase from $supplierName"
                )
            )
        }

        // Deduct from account if paid
        if (paidAmountCents > 0 && financialAccountId != null) {
            val account = financeDao.getAccountByIdDirect(financialAccountId)
            if (account != null) {
                financeDao.updateBalance(financialAccountId, account.balanceCents - paidAmountCents)
            }
        }

        // Update supplier balance if balance due remains
        val balanceDue = subtotal - paidAmountCents
        if (balanceDue > 0 && supplierId != null) {
            val supplier = contactDao.getSupplierByIdDirect(supplierId)
            if (supplier != null) {
                contactDao.updateSupplierBalance(supplierId, supplier.currentBalanceCents + balanceDue)
            }
        }

        settingsDao.insertAuditLog(
            AuditLog(
                action = "CREATE_PURCHASE",
                entityType = "Purchase",
                entityId = purchaseId.toString(),
                details = "PO $poNum created for $subtotal from $supplierName"
            )
        )
        purchaseId
    }

    // Finance & Accounts
    val allAccounts: Flow<List<FinancialAccount>> = financeDao.getAllAccounts()
    val allTransfers: Flow<List<AccountTransfer>> = financeDao.getAllTransfers()
    val allExpenses: Flow<List<Expense>> = financeDao.getAllExpenses()
    val recentExpenses: Flow<List<Expense>> = financeDao.getRecentExpenses(15)
    val allCustomerPayments: Flow<List<CustomerPayment>> = financeDao.getAllCustomerPayments()
    val allSupplierPayments: Flow<List<SupplierPayment>> = financeDao.getAllSupplierPayments()
    val allClosings: Flow<List<DailyCashClosing>> = financeDao.getAllClosings()

    suspend fun saveAccount(account: FinancialAccount): Long {
        return if (account.id == 0L) {
            financeDao.insertAccount(account)
        } else {
            financeDao.updateAccount(account)
            account.id
        }
    }

    suspend fun recordExpense(expense: Expense) = db.withTransaction {
        val expenseId = financeDao.insertExpense(expense)
        val account = financeDao.getAccountByIdDirect(expense.financialAccountId)
        if (account != null) {
            financeDao.updateBalance(expense.financialAccountId, account.balanceCents - expense.amountCents)
        }
        settingsDao.insertAuditLog(
            AuditLog(
                action = "CREATE_EXPENSE",
                entityType = "Expense",
                entityId = expenseId.toString(),
                details = "Expense of ${expense.amountCents} for ${expense.category}: ${expense.description}"
            )
        )
    }

    suspend fun transferFunds(
        fromAccountId: Long,
        toAccountId: Long,
        amountCents: Long,
        feeCents: Long = 0,
        notes: String = ""
    ) = db.withTransaction {
        val fromAcc = financeDao.getAccountByIdDirect(fromAccountId) ?: return@withTransaction
        val toAcc = financeDao.getAccountByIdDirect(toAccountId) ?: return@withTransaction

        financeDao.updateBalance(fromAccountId, fromAcc.balanceCents - (amountCents + feeCents))
        financeDao.updateBalance(toAccountId, toAcc.balanceCents + amountCents)

        val transfer = AccountTransfer(
            fromAccountId = fromAccountId,
            toAccountId = toAccountId,
            amountCents = amountCents,
            feeCents = feeCents,
            notes = notes
        )
        financeDao.insertTransfer(transfer)

        settingsDao.insertAuditLog(
            AuditLog(
                action = "ACCOUNT_TRANSFER",
                entityType = "FinancialAccount",
                entityId = "$fromAccountId -> $toAccountId",
                details = "Transferred $amountCents from ${fromAcc.name} to ${toAcc.name} (Fee: $feeCents)"
            )
        )
    }

    suspend fun receiveCustomerPayment(
        customerId: Long,
        saleId: Long?,
        amountCents: Long,
        paymentMethod: PaymentMethod,
        financialAccountId: Long,
        reference: String,
        notes: String = ""
    ) = db.withTransaction {
        val customer = contactDao.getCustomerByIdDirect(customerId) ?: return@withTransaction
        val paymentNum = "RCP-" + System.currentTimeMillis().toString().takeLast(6)

        val payment = CustomerPayment(
            customerId = customerId,
            saleId = saleId,
            paymentNumber = paymentNum,
            amountCents = amountCents,
            paymentMethod = paymentMethod,
            financialAccountId = financialAccountId,
            reference = reference,
            notes = notes
        )
        financeDao.insertCustomerPayment(payment)

        // Reduce customer outstanding balance
        val newBalance = (customer.currentBalanceCents - amountCents).coerceAtLeast(0L)
        contactDao.updateCustomerBalance(customerId, newBalance)

        // Increase cash/bank balance
        val account = financeDao.getAccountByIdDirect(financialAccountId)
        if (account != null) {
            financeDao.updateBalance(financialAccountId, account.balanceCents + amountCents)
        }

        // If linked to sale, update paid amount & status
        if (saleId != null) {
            val sale = saleDao.getSaleByIdDirect(saleId)
            if (sale != null) {
                val newPaid = sale.paidAmountCents + amountCents
                val newStatus = if (newPaid >= sale.totalCents) PaymentStatus.PAID else PaymentStatus.PARTIALLY_PAID
                saleDao.updateSale(sale.copy(paidAmountCents = newPaid, paymentStatus = newStatus))
            }
        }

        settingsDao.insertAuditLog(
            AuditLog(
                action = "CUSTOMER_PAYMENT",
                entityType = "Customer",
                entityId = customerId.toString(),
                details = "Received $amountCents from ${customer.name} (Ref: $reference)"
            )
        )
    }

    suspend fun paySupplier(
        supplierId: Long,
        purchaseId: Long?,
        amountCents: Long,
        paymentMethod: PaymentMethod,
        financialAccountId: Long,
        reference: String,
        notes: String = ""
    ) = db.withTransaction {
        val supplier = contactDao.getSupplierByIdDirect(supplierId) ?: return@withTransaction
        val paymentNum = "SPY-" + System.currentTimeMillis().toString().takeLast(6)

        val payment = SupplierPayment(
            supplierId = supplierId,
            purchaseId = purchaseId,
            paymentNumber = paymentNum,
            amountCents = amountCents,
            paymentMethod = paymentMethod,
            financialAccountId = financialAccountId,
            reference = reference,
            notes = notes
        )
        financeDao.insertSupplierPayment(payment)

        // Reduce payable balance
        val newBalance = (supplier.currentBalanceCents - amountCents).coerceAtLeast(0L)
        contactDao.updateSupplierBalance(supplierId, newBalance)

        // Deduct from account
        val account = financeDao.getAccountByIdDirect(financialAccountId)
        if (account != null) {
            financeDao.updateBalance(financialAccountId, account.balanceCents - amountCents)
        }

        settingsDao.insertAuditLog(
            AuditLog(
                action = "SUPPLIER_PAYMENT",
                entityType = "Supplier",
                entityId = supplierId.toString(),
                details = "Paid $amountCents to ${supplier.name} (Ref: $reference)"
            )
        )
    }

    suspend fun recordCashClosing(
        accountId: Long,
        actualCountCents: Long,
        notes: String = "",
        closedBy: String = "Cashier"
    ) = db.withTransaction {
        val account = financeDao.getAccountByIdDirect(accountId) ?: return@withTransaction
        val systemExpected = account.balanceCents
        val diff = actualCountCents - systemExpected

        val closing = DailyCashClosing(
            financialAccountId = accountId,
            openingBalanceCents = systemExpected,
            actualCashCountCents = actualCountCents,
            systemExpectedCents = systemExpected,
            differenceCents = diff,
            notes = notes,
            closedBy = closedBy
        )
        financeDao.insertClosing(closing)

        // Adjust account to match actual count
        financeDao.updateBalance(accountId, actualCountCents)

        settingsDao.insertAuditLog(
            AuditLog(
                action = "CASH_CLOSING",
                entityType = "FinancialAccount",
                entityId = accountId.toString(),
                details = "Closed cash for ${account.name}. Expected $systemExpected, counted $actualCountCents, diff $diff"
            )
        )
    }

    // Contacts
    val allCustomers: Flow<List<Customer>> = contactDao.getAllCustomers()
    val allSuppliers: Flow<List<Supplier>> = contactDao.getAllSuppliers()
    val customersWithDebt: Flow<List<Customer>> = contactDao.getCustomersWithDebt()
    val suppliersWithDebt: Flow<List<Supplier>> = contactDao.getSuppliersWithDebt()

    fun getCustomer(id: Long): Flow<Customer?> = contactDao.getCustomerById(id)
    suspend fun getCustomerDirect(id: Long): Customer? = contactDao.getCustomerByIdDirect(id)
    fun getSupplier(id: Long): Flow<Supplier?> = contactDao.getSupplierById(id)
    suspend fun getSupplierDirect(id: Long): Supplier? = contactDao.getSupplierByIdDirect(id)

    suspend fun saveCustomer(customer: Customer): Long {
        return if (customer.id == 0L) {
            val id = contactDao.insertCustomer(customer)
            settingsDao.insertAuditLog(
                AuditLog(action = "CREATE_CUSTOMER", entityType = "Customer", entityId = id.toString(), details = "Created customer ${customer.name}")
            )
            id
        } else {
            contactDao.updateCustomer(customer)
            settingsDao.insertAuditLog(
                AuditLog(action = "UPDATE_CUSTOMER", entityType = "Customer", entityId = customer.id.toString(), details = "Updated customer ${customer.name}")
            )
            customer.id
        }
    }

    suspend fun saveSupplier(supplier: Supplier): Long {
        return if (supplier.id == 0L) {
            val id = contactDao.insertSupplier(supplier)
            settingsDao.insertAuditLog(
                AuditLog(action = "CREATE_SUPPLIER", entityType = "Supplier", entityId = id.toString(), details = "Created supplier ${supplier.name}")
            )
            id
        } else {
            contactDao.updateSupplier(supplier)
            settingsDao.insertAuditLog(
                AuditLog(action = "UPDATE_SUPPLIER", entityType = "Supplier", entityId = supplier.id.toString(), details = "Updated supplier ${supplier.name}")
            )
            supplier.id
        }
    }

    // Settings & Audit Logs
    val settings: Flow<BusinessSettings?> = settingsDao.getSettings()
    val auditLogs: Flow<List<AuditLog>> = settingsDao.getAllAuditLogs()

    suspend fun getSettingsDirect(): BusinessSettings {
        return settingsDao.getSettingsDirect() ?: BusinessSettings().also {
            settingsDao.insertOrUpdateSettings(it)
        }
    }

    suspend fun updateSettings(settings: BusinessSettings) {
        settingsDao.insertOrUpdateSettings(settings)
        settingsDao.insertAuditLog(
            AuditLog(action = "UPDATE_SETTINGS", entityType = "BusinessSettings", entityId = "1", details = "Updated system settings")
        )
    }
}
