package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.Purchase
import com.example.data.local.entity.PurchaseItem
import com.example.data.local.entity.Sale
import com.example.data.local.entity.SaleItem
import com.example.data.local.entity.SalesReturn
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {
    @Query("SELECT * FROM sales ORDER BY saleDate DESC")
    fun getAllSales(): Flow<List<Sale>>

    @Query("SELECT * FROM sales ORDER BY saleDate DESC LIMIT :limit")
    fun getRecentSales(limit: Int): Flow<List<Sale>>

    @Query("SELECT * FROM sales WHERE id = :id")
    fun getSaleById(id: Long): Flow<Sale?>

    @Query("SELECT * FROM sales WHERE id = :id LIMIT 1")
    suspend fun getSaleByIdDirect(id: Long): Sale?

    @Query("SELECT * FROM sales WHERE invoiceNumber = :invoiceNumber LIMIT 1")
    suspend fun getSaleByInvoice(invoiceNumber: String): Sale?

    @Query("SELECT * FROM sales WHERE customerId = :customerId ORDER BY saleDate DESC")
    fun getSalesByCustomer(customerId: Long): Flow<List<Sale>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: Sale): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSales(sales: List<Sale>)

    @Update
    suspend fun updateSale(sale: Sale)

    @Query("SELECT * FROM sale_items WHERE saleId = :saleId")
    fun getSaleItems(saleId: Long): Flow<List<SaleItem>>

    @Query("SELECT * FROM sale_items WHERE saleId = :saleId")
    suspend fun getSaleItemsDirect(saleId: Long): List<SaleItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleItems(items: List<SaleItem>)

    @Query("SELECT * FROM sales_returns ORDER BY returnDate DESC")
    fun getAllSalesReturns(): Flow<List<SalesReturn>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSalesReturn(returnRecord: SalesReturn): Long

    @Query("SELECT * FROM purchases ORDER BY purchaseDate DESC")
    fun getAllPurchases(): Flow<List<Purchase>>

    @Query("SELECT * FROM purchases WHERE id = :id")
    fun getPurchaseById(id: Long): Flow<Purchase?>

    @Query("SELECT * FROM purchases WHERE id = :id LIMIT 1")
    suspend fun getPurchaseByIdDirect(id: Long): Purchase?

    @Query("SELECT * FROM purchases WHERE supplierId = :supplierId ORDER BY purchaseDate DESC")
    fun getPurchasesBySupplier(supplierId: Long): Flow<List<Purchase>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchase(purchase: Purchase): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchases(purchases: List<Purchase>)

    @Query("SELECT * FROM purchase_items WHERE purchaseId = :purchaseId")
    fun getPurchaseItems(purchaseId: Long): Flow<List<PurchaseItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchaseItems(items: List<PurchaseItem>)

    @Query("SELECT COUNT(*) FROM sales")
    suspend fun getSalesCount(): Int
}
