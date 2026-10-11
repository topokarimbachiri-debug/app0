package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.Customer
import com.example.data.local.entity.Supplier
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE id = :id")
    fun getCustomerById(id: Long): Flow<Customer?>

    @Query("SELECT * FROM customers WHERE id = :id LIMIT 1")
    suspend fun getCustomerByIdDirect(id: Long): Customer?

    @Query("SELECT * FROM customers WHERE currentBalanceCents > 0 ORDER BY currentBalanceCents DESC")
    fun getCustomersWithDebt(): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%'")
    fun searchCustomers(query: String): Flow<List<Customer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomers(customers: List<Customer>)

    @Update
    suspend fun updateCustomer(customer: Customer)

    @Query("UPDATE customers SET currentBalanceCents = :balanceCents WHERE id = :customerId")
    suspend fun updateCustomerBalance(customerId: Long, balanceCents: Long)

    @Delete
    suspend fun deleteCustomer(customer: Customer)

    @Query("SELECT * FROM suppliers ORDER BY name ASC")
    fun getAllSuppliers(): Flow<List<Supplier>>

    @Query("SELECT * FROM suppliers WHERE id = :id")
    fun getSupplierById(id: Long): Flow<Supplier?>

    @Query("SELECT * FROM suppliers WHERE id = :id LIMIT 1")
    suspend fun getSupplierByIdDirect(id: Long): Supplier?

    @Query("SELECT * FROM suppliers WHERE currentBalanceCents > 0 ORDER BY currentBalanceCents DESC")
    fun getSuppliersWithDebt(): Flow<List<Supplier>>

    @Query("SELECT * FROM suppliers WHERE name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%'")
    fun searchSuppliers(query: String): Flow<List<Supplier>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplier(supplier: Supplier): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSuppliers(suppliers: List<Supplier>)

    @Update
    suspend fun updateSupplier(supplier: Supplier)

    @Query("UPDATE suppliers SET currentBalanceCents = :balanceCents WHERE id = :supplierId")
    suspend fun updateSupplierBalance(supplierId: Long, balanceCents: Long)

    @Delete
    suspend fun deleteSupplier(supplier: Supplier)
}
