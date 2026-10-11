package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.AccountTransfer
import com.example.data.local.entity.CustomerPayment
import com.example.data.local.entity.DailyCashClosing
import com.example.data.local.entity.Expense
import com.example.data.local.entity.FinancialAccount
import com.example.data.local.entity.SupplierPayment
import kotlinx.coroutines.flow.Flow

@Dao
interface FinanceDao {
    @Query("SELECT * FROM financial_accounts ORDER BY id ASC")
    fun getAllAccounts(): Flow<List<FinancialAccount>>

    @Query("SELECT * FROM financial_accounts WHERE id = :id")
    fun getAccountById(id: Long): Flow<FinancialAccount?>

    @Query("SELECT * FROM financial_accounts WHERE id = :id LIMIT 1")
    suspend fun getAccountByIdDirect(id: Long): FinancialAccount?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: FinancialAccount): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccounts(accounts: List<FinancialAccount>)

    @Update
    suspend fun updateAccount(account: FinancialAccount)

    @Query("UPDATE financial_accounts SET balanceCents = :newBalanceCents WHERE id = :accountId")
    suspend fun updateBalance(accountId: Long, newBalanceCents: Long)

    @Query("SELECT * FROM account_transfers ORDER BY transferDate DESC")
    fun getAllTransfers(): Flow<List<AccountTransfer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransfer(transfer: AccountTransfer): Long

    @Query("SELECT * FROM expenses ORDER BY expenseDate DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Query("SELECT * FROM expenses ORDER BY expenseDate DESC LIMIT :limit")
    fun getRecentExpenses(limit: Int): Flow<List<Expense>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenses(expenses: List<Expense>)

    @Delete
    suspend fun deleteExpense(expense: Expense)

    @Query("SELECT * FROM customer_payments ORDER BY paymentDate DESC")
    fun getAllCustomerPayments(): Flow<List<CustomerPayment>>

    @Query("SELECT * FROM customer_payments WHERE customerId = :customerId ORDER BY paymentDate DESC")
    fun getCustomerPayments(customerId: Long): Flow<List<CustomerPayment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomerPayment(payment: CustomerPayment): Long

    @Query("SELECT * FROM supplier_payments ORDER BY paymentDate DESC")
    fun getAllSupplierPayments(): Flow<List<SupplierPayment>>

    @Query("SELECT * FROM supplier_payments WHERE supplierId = :supplierId ORDER BY paymentDate DESC")
    fun getSupplierPayments(supplierId: Long): Flow<List<SupplierPayment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplierPayment(payment: SupplierPayment): Long

    @Query("SELECT * FROM daily_cash_closings ORDER BY date DESC")
    fun getAllClosings(): Flow<List<DailyCashClosing>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClosing(closing: DailyCashClosing): Long
}
