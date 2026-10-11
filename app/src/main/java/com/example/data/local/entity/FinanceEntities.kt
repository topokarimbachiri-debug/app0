package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AccountType {
    CASH,
    BANK,
    WALLET,
    RESTRICTED // Excluded from immediate liquidity calculation!
}

@Entity(tableName = "financial_accounts")
data class FinancialAccount(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val accountType: AccountType = AccountType.CASH,
    val balanceCents: Long = 0,
    val accountNumber: String = "",
    val isRestricted: Boolean = false,
    val isDefault: Boolean = false
)

@Entity(tableName = "account_transfers")
data class AccountTransfer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fromAccountId: Long,
    val toAccountId: Long,
    val amountCents: Long,
    val feeCents: Long = 0,
    val transferDate: Long = System.currentTimeMillis(),
    val notes: String = ""
)

enum class ExpenseCategory {
    RENT,
    UTILITIES,
    SALARIES,
    MARKETING,
    SUPPLIES,
    TAXES,
    MAINTENANCE,
    LOGISTICS,
    OTHER
}

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: ExpenseCategory,
    val amountCents: Long,
    val financialAccountId: Long,
    val recipient: String = "",
    val description: String = "",
    val expenseDate: Long = System.currentTimeMillis()
)

@Entity(tableName = "customer_payments")
data class CustomerPayment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long,
    val saleId: Long? = null,
    val paymentNumber: String,
    val amountCents: Long,
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val financialAccountId: Long,
    val reference: String = "",
    val paymentDate: Long = System.currentTimeMillis(),
    val notes: String = ""
)

@Entity(tableName = "supplier_payments")
data class SupplierPayment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val supplierId: Long,
    val purchaseId: Long? = null,
    val paymentNumber: String,
    val amountCents: Long,
    val paymentMethod: PaymentMethod = PaymentMethod.BANK_TRANSFER,
    val financialAccountId: Long,
    val reference: String = "",
    val paymentDate: Long = System.currentTimeMillis(),
    val notes: String = ""
)

@Entity(tableName = "daily_cash_closings")
data class DailyCashClosing(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long = System.currentTimeMillis(),
    val financialAccountId: Long,
    val openingBalanceCents: Long,
    val actualCashCountCents: Long,
    val systemExpectedCents: Long,
    val differenceCents: Long,
    val notes: String = "",
    val closedBy: String = "Cashier"
)
